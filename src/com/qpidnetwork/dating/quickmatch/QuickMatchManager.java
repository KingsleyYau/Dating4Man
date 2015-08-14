package com.qpidnetwork.dating.quickmatch;

import java.util.ArrayList;
import java.util.List;

import com.qpidnetwork.dating.authorization.LoginManager.OnLoginManagerCallback;
import com.qpidnetwork.manager.WebSiteManager;
import com.qpidnetwork.manager.WebSiteManager.OnChangeWebsiteCallback;
import com.qpidnetwork.manager.WebSiteManager.WebSite;
import com.qpidnetwork.request.OnQueryQuickMatchLikeLadyListCallback;
import com.qpidnetwork.request.OnQueryQuickMatchLadyListCallback;
import com.qpidnetwork.request.OnRequestCallback;
import com.qpidnetwork.request.RequestJni;
import com.qpidnetwork.request.RequestJniQuickMatch;
import com.qpidnetwork.request.RequestOperator;
import com.qpidnetwork.request.item.LoginErrorItem;
import com.qpidnetwork.request.item.LoginItem;
import com.qpidnetwork.request.item.QuickMatchLady;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.telephony.TelephonyManager;
import android.text.format.Time;


/**
 * 认证模块
 * QuickMatch管理器 
 * @author Max.Chiu
 *
 */
public class QuickMatchManager implements OnLoginManagerCallback, OnChangeWebsiteCallback {
	/**
	 *	回调函数 
	 *
	 */
	public interface OnQueryQuickMatchManagerLadyListCallback {
		/**
		 * 查询女士列表
		 * @param isSuccess			是否成功
		 * @param itemList			女士列表
		 * @param index				上次选择的下标
		 */
		public void OnQueryLadyList(
				boolean isSuccess, 
				String errno, 
				String errmsg, 
				List<QuickMatchLady> itemList, 
				int index
				);
	}
	
	public interface OnQueryQuickMatchManagerLikeLadyListCallback {
		/**
		 * 查询喜爱女士列表
		 * @param isSuccess			是否成功
		 * @param itemList			喜爱女士列表
		 * @param hasMore			是否有更多
		 */
		public void OnQueryLikeLadyList(
				boolean isSuccess, 
				String errno, 
				String errmsg, 
				List<QuickMatchLady> itemList, 
				boolean hasMore
				);
	}
	
	protected Handler mHandler = null;
	private Context mContenxt = null;
	private WebSite mWebsite = null;
	private static QuickMatchManager gQuickMatchManager = null;
	
	public static QuickMatchManager newInstance(Context context) {
		if (gQuickMatchManager == null) {
			gQuickMatchManager = new QuickMatchManager(context);
		}
		return gQuickMatchManager;
	}
	
	public static QuickMatchManager getInstance() {
		return gQuickMatchManager;
	}
	
	/**
	 * 服务器女士列表
	 * @see 其他几个列表中当天的item都是这个列表item的浅引用
	 */
	private List<QuickMatchLady> mQuickMatchLadyList = new ArrayList<QuickMatchLady>();
	/**
	 * 已经选择到的下标
	 */
	private int mIndex = 0;
	
	/**
	 * 最后更新时间
	 */
	private Time mLastUpdateTime; 
	
	/**
	 * 本地喜爱女士列表
	 */
	private List<QuickMatchLady> mQuickMatchLadyLocalLikeList = new ArrayList<QuickMatchLady>();
	/**
	 * 服务器喜爱女士列表
	 */
	private List<QuickMatchLady> mQuickMatchLadyLikeList = new ArrayList<QuickMatchLady>();
	/**
	 * 本地喜爱女士显示列表
	 */
	private List<QuickMatchLady> mQuickMatchLadyLocalShowLikeList = new ArrayList<QuickMatchLady>();
	
	/**
	 *  需要服务器删除的喜爱女士列表
	 */
	private List<QuickMatchLady> mQuickMatchLadyRemoveList = new ArrayList<QuickMatchLady>();
	
	/**
	 * 本地不喜爱女士列表
	 */
	private List<QuickMatchLady> mQuickMatchLadyLocalUnLikeList = new ArrayList<QuickMatchLady>();
	
	public QuickMatchManager(Context context) {
		// TODO Auto-generated constructor stub
		mContenxt = context;
		
		// 初始化事件监听
		InitHandler();
	}
	
	/**
	 * 标记女士
	 * @param index		服务器女士列表中下标
	 * @param like		是否喜欢
	 */
	public void MarkLady(int index, boolean like) {
		mIndex = index + 1;
		if( like ) {
			// 喜爱
			AddLikeLady(index);
		} else {
			// 不喜爱
			AddUnLikeLady(index);
		}
		
		// 执行数据持久化
		SaveData();
	}
	
	/**
	 * 增加不喜爱女士
	 * @param index		服务器女士列表中下标	
	 */
	private void AddUnLikeLady(int index) {
		if( index < 0 || index >= mQuickMatchLadyList.size() ) {
			return;
		}
		
		// 从服务器女士列表寻找
		QuickMatchLady item = mQuickMatchLadyList.get(index);
		if( item != null ) {
			// 删除本地喜爱女士列表
			if( !CheckLadyExist(mQuickMatchLadyLocalLikeList, item.womanid) ) {
				mQuickMatchLadyLocalLikeList.remove(item);
			}
			
			// 加到本地不喜爱女士列表
			if( !CheckLadyExist(mQuickMatchLadyLocalUnLikeList, item.womanid) ) {
				mQuickMatchLadyLocalUnLikeList.add(item);
			}
		}
	}
	
	/**
	 * 增加喜爱女士
	 * @param index		服务器女士列表中下标
	 */
	private void AddLikeLady(int index) {
		if( index < 0 || index >= mQuickMatchLadyList.size() ) {
			return;
		}
		
		// 从服务器女士列表寻找
		QuickMatchLady item = mQuickMatchLadyList.get(index);
		if( item != null ) {
			// 删除本地不喜爱女士列表
			if( CheckLadyExist(mQuickMatchLadyLocalUnLikeList, item.womanid) ) {
				mQuickMatchLadyLocalUnLikeList.remove(item);
			}
			
			// 加到本地喜爱女士列表
			if( !CheckLadyExist(mQuickMatchLadyLocalLikeList, item.womanid) ) {
				mQuickMatchLadyLocalLikeList.add(item);
			}
		}
	}
	
	/**
	 * 删除喜爱
	 */
	public void RemoveLikeLady(String womanId) {
		// 本地删除喜爱
		for( QuickMatchLady item : mQuickMatchLadyLocalLikeList ) {
			if( item.womanid.compareTo(womanId) == 0 ) {
				mQuickMatchLadyLocalLikeList.remove(item);
				return;
			}
		}
		
		// 加入到服务器删除列表
		// 是否在服务器喜爱列表
		for( QuickMatchLady item : mQuickMatchLadyLikeList ) {
			if( item.womanid.compareTo(womanId) == 0 ) {
				// 加入本地删除列表
				if( !CheckLadyExist(mQuickMatchLadyRemoveList, womanId) ) {
					mQuickMatchLadyRemoveList.add(item);
				}
			}
		}
		
		// 执行数据持久化
		SaveData();
	}
	
	/**
	 * 判断女士是否已经存在列表
	 * @param list			女士列表
	 * @param womanId		女士id
	 * @return				是否已经存在
	 */
	public boolean CheckLadyExist(List<QuickMatchLady> list, String womanId) {
		boolean bFlag = false;
		for( QuickMatchLady item : list ) {
			if( item.womanid.compareTo(womanId) == 0 ) {
				bFlag = true;
				break;
			}
		}
		return bFlag;
	}
	
	/**
	 * 获取匹配女士列表
	 * @return
	 */
	public void QueryMatchLadyList(final OnQueryQuickMatchManagerLadyListCallback callback) {
		// 同一天内，是否已经获取列表到本地
		if( IsSameDay() ) {
			// 直接返回列表
			if( callback != null ) {
				callback.OnQueryLadyList(true, "", "", mQuickMatchLadyList, mIndex);
			}
		} else {
			// 请求接口
			TelephonyManager tm = (TelephonyManager) mContenxt.getSystemService(Context.TELEPHONY_SERVICE);
			
			// 调用jni接口
			RequestJniQuickMatch.QueryQuickMatchLadyList(
					RequestJni.GetDeviceId(tm), 
					new OnQueryQuickMatchLadyListCallback() {
						
						@Override
						public void OnQueryQuickMatchLadyList(boolean isSuccess, String errno, String errmsg,
								QuickMatchLady[] itemList) {
							// TODO Auto-generated method stub
							
							if( isSuccess ) {
								// 保存最后更新时间
								mLastUpdateTime = new Time();
								mLastUpdateTime.setToNow();
								
								mQuickMatchLadyList.clear();
								mIndex = 0;
								
								if( itemList != null ) {
									for(QuickMatchLady item : itemList) {
//										if( !mQuickMatchLadyList.contains(item) ) {
											mQuickMatchLadyList.add(item);
//										}
									}
								}
								
								// 保存数据
								SaveData();
							}
							
							if( callback != null ) {
								callback.OnQueryLadyList(isSuccess, errno, errmsg, mQuickMatchLadyList, mIndex);
							}
						}
					});
		}
	}
	
	/**
	 * 上传标记女士列表
	 * @return*/
	public void UploadToServer() {
		// 上传删除的女士列表
		// 生成需要删除的列表
		List<String> removeLadyId = new ArrayList<String>();
		if( mQuickMatchLadyRemoveList.size() > 0 ) {
			final List<QuickMatchLady> removeList = new ArrayList<QuickMatchLady>(mQuickMatchLadyRemoveList);
			for(QuickMatchLady item : removeList) {
				removeLadyId.add(item.womanid);
			}
			// 调用接口
			RequestOperator.getInstance().RemoveQuickMatchLikeLadyList(
					(String[]) removeLadyId.toArray(new String[removeLadyId.size()]), 
					new OnRequestCallback() {
						
						@Override
						public void OnRequest(boolean isSuccess, String errno, String errmsg) {
							// TODO Auto-generated method stub
							
							// 清空已经上传的女士
							mQuickMatchLadyRemoveList.removeAll(removeList);
							
							// 保存数据
							SaveData();
						}
					});
		}
		
		// 上传标记的女士列表
		if( mQuickMatchLadyLocalLikeList.size() > 0 || mQuickMatchLadyLocalUnLikeList.size() > 0 ) {
			// 喜爱的女士
			List<String> likeLadyId = new ArrayList<String>();
			final List<QuickMatchLady> likeList = new ArrayList<QuickMatchLady>(mQuickMatchLadyLocalLikeList);
			for(QuickMatchLady item : likeList) {
				likeLadyId.add(item.womanid);
			}
			
			// 不喜爱的女士
			List<String> unlikeLadyId = new ArrayList<String>();
			final List<QuickMatchLady> unlikeList = new ArrayList<QuickMatchLady>(mQuickMatchLadyLocalUnLikeList);
			for(QuickMatchLady item : unlikeList) {
				unlikeLadyId.add(item.womanid);
			}

			
			// 调用接口
			RequestOperator.getInstance().SubmitQuickMatchMarkLadyList(
					(String[]) likeLadyId.toArray(new String[likeLadyId.size()]),
					(String[]) unlikeLadyId.toArray(new String[unlikeLadyId.size()]),
					new OnRequestCallback() {
						@Override
						public void OnRequest(boolean isSuccess, String errno, String errmsg) {
							// TODO Auto-generated method stub
							
							// 清空已经上传的女士
							mQuickMatchLadyLocalLikeList.removeAll(likeList);
							mQuickMatchLadyLocalUnLikeList.removeAll(unlikeList);
		
							// 保存数据
							SaveData();
						}
					});
		}
	}

	/**
	 * 获取喜爱的女士列表
	 * @param loadMore		是否请求更多
	 * @param callback
	 */
	public void QueryLikeLadyList(boolean loadMore, 
			final OnQueryQuickMatchManagerLikeLadyListCallback callback) {
		
		// 当次需要获取结果的第几页
		int pageIndex = 1;
		// 每页最大纪录数
		final int pageSize = 10;
		
		// 需要从服务器获取的记录
		final int maxOnlineCount = mQuickMatchLadyLikeList.size() + pageSize;
		
	    // 当次需要获取结果的第几页
		pageIndex = (maxOnlineCount - 1) / pageSize + 1;
		pageIndex = (pageIndex > 0)?pageIndex:1;
	    
		if( !loadMore ) {
			// 刷最新
			pageIndex = 1;
		}
		
	    // 同步数据
		mQuickMatchLadyLocalShowLikeList.clear();
		// 先加入本地
		mQuickMatchLadyLocalShowLikeList.addAll(mQuickMatchLadyLocalLikeList);
		// 再加入服务器, 并且不在本地删除列表
		for(QuickMatchLady item : mQuickMatchLadyLikeList) {
			// 加入到本地显示列表
			if( !CheckLadyExist(mQuickMatchLadyRemoveList, item.womanid) ) {
				mQuickMatchLadyLocalShowLikeList.add(item);
			}
		}
		
		// 请求喜爱女士列表
		RequestOperator.getInstance().QueryQuickMatchLikeLadyList(
				pageIndex,
				pageSize,
				new OnQueryQuickMatchLikeLadyListCallback() {
					@Override
					public void OnQueryQuickMatchLikeLadyList(
							boolean isSuccess,
							String errno, 
							String errmsg,
							QuickMatchLady[] itemList, 
							int totalCount) {
						// TODO Auto-generated method stub
						
						boolean hasMore = false;
						if( itemList != null ) {
							hasMore = !(pageSize <= itemList.length);
							
							for(QuickMatchLady item : itemList) {
								// 加入到本地显示喜爱列表
								boolean bLikeFlag = CheckLadyExist(mQuickMatchLadyLikeList, item.womanid);
								if( !bLikeFlag ) {
									mQuickMatchLadyLikeList.add(item);
								}
								
								// 是否存在本地删除列表
								boolean bRemoveFlag = CheckLadyExist(mQuickMatchLadyRemoveList, item.womanid);
								
								// 加入到本地显示列表
								if( !bLikeFlag && !bRemoveFlag ) {
									mQuickMatchLadyLocalShowLikeList.add(item);
								}
							}

						}

						// 执行数据持久化
						SaveData();
						
						// 回调界面, 请求回来的记录数和发起请求的时候一样表示有更多
						isSuccess = (mQuickMatchLadyLocalShowLikeList.size() > 0)?true:isSuccess;
						if( callback != null ) {
							callback.OnQueryLikeLadyList(
									isSuccess, 
									errno,
									errmsg,
									mQuickMatchLadyLocalShowLikeList,
									hasMore
									);
						}
					}
				});
	}
	
	/**
	 * 保存数据
	 */
	public void SaveData() {
		// 缓存最后更新时间和下标
		if( mLastUpdateTime != null ) {
			QuickMatchPerfence.SaveLastUpdateTime(mContenxt, mWebsite, mLastUpdateTime);
		}
		QuickMatchPerfence.SaveLastIndex(mContenxt, mWebsite, mIndex);
		
		// 缓存所有列表
		QuickMatchPerfence.SaveQuickMatchLadyList(mContenxt, mWebsite, mQuickMatchLadyList);
		QuickMatchPerfence.SaveQuickMatchLadyLocalLikeList(mContenxt, mWebsite, mQuickMatchLadyLocalLikeList);
		QuickMatchPerfence.SaveQuickMatchLadyLikeList(mContenxt, mWebsite, mQuickMatchLadyLikeList);
		QuickMatchPerfence.SaveQuickMatchLadyRemoveList(mContenxt, mWebsite, mQuickMatchLadyRemoveList);
		QuickMatchPerfence.SaveQuickMatchLadyUnLikeList(mContenxt, mWebsite, mQuickMatchLadyLocalUnLikeList);
	}
	
	/**
	 * 读取数据
	 */
	public void LoadData() {
		// 读取最后更新时间和下标
		mLastUpdateTime = QuickMatchPerfence.GetLastUpdateTime(mContenxt, mWebsite);
		mIndex = QuickMatchPerfence.GetLastIndex(mContenxt, mWebsite);
		
		// 读取所有列表
		mQuickMatchLadyList = QuickMatchPerfence.GetQuickMatchLadyList(mContenxt, mWebsite);
		mQuickMatchLadyLocalLikeList = QuickMatchPerfence.GetQuickMatchLadyLocalLikeList(mContenxt, mWebsite);
		mQuickMatchLadyLikeList = QuickMatchPerfence.GetQuickMatchLadyLikeList(mContenxt, mWebsite);
		mQuickMatchLadyRemoveList = QuickMatchPerfence.GetQuickMatchLadyRemoveList(mContenxt, mWebsite);
		mQuickMatchLadyLocalUnLikeList = QuickMatchPerfence.GetQuickMatchLadyUnLikeList(mContenxt, mWebsite);
	}
	
	/**
	 * 当前时间和最后更新时间是否同一天
	 * @return		是否同一天
	 */
	public boolean IsSameDay() {
		Time time = new Time();
		time.setToNow();
		
		if( mLastUpdateTime != null && 
				time.year == mLastUpdateTime.year 
				&& time.month == mLastUpdateTime.month 
				&& time.weekDay == mLastUpdateTime.weekDay 
				) {
			return true;
		}
		
		return false;
	}
	
	@Override
	public void OnLogin(boolean isSuccess, String errno, String errmsg,
			LoginItem item, LoginErrorItem errItem) {
		// TODO Auto-generated method stub
		
		// 登录成功，上传本地标记女士列表
		Message msg = Message.obtain();
		if( isSuccess ) {
			// 登录成功
			mHandler.sendMessage(msg);
		}
	}
	
	public void InitHandler() {
		mHandler = new Handler() {
			@Override
			public void handleMessage(android.os.Message msg) {
				// 登录成功，上传本地标记女士列表
				UploadToServer();
			}
		};
	}

	@Override
	public void OnLogout() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void OnChangeWebsite(WebSite website) {
		// TODO Auto-generated method stub
		// 切换站点
		// 先保存旧数据
		SaveData();
		mWebsite = website;
		// 加载本地新数据
		LoadData();
	}

}
