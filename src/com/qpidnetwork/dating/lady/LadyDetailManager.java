package com.qpidnetwork.dating.lady;

import java.util.HashMap;
import java.util.Map;

import android.content.Context;

import com.qpidnetwork.livechat.LCUserItem;
import com.qpidnetwork.livechat.LiveChatManagerOtherListener;
import com.qpidnetwork.livechat.jni.LiveChatClientListener.KickOfflineType;
import com.qpidnetwork.livechat.jni.LiveChatClientListener.LiveChatErrType;
import com.qpidnetwork.livechat.jni.LiveChatClientListener.TalkEmfNoticeType;
import com.qpidnetwork.manager.FileCacheManager;
import com.qpidnetwork.request.OnQueryLadyDetailCallback;
import com.qpidnetwork.request.OnQueryLadyListCallback;
import com.qpidnetwork.request.OnRequestCallback;
import com.qpidnetwork.request.RequestJniLady;
import com.qpidnetwork.request.RequestJniLady.OnlineType;
import com.qpidnetwork.request.RequestJniLady.OrderType;
import com.qpidnetwork.request.RequestJniLady.SearchType;
import com.qpidnetwork.request.RequestOperator;
import com.qpidnetwork.request.item.Lady;
import com.qpidnetwork.request.item.LadyDetail;
import com.qpidnetwork.tool.FileDownloader;

/**
 * 女士详情管理器 
 * @author Max.Chiu
 *
 */
public class LadyDetailManager implements LiveChatManagerOtherListener {
	/**
	 *	回调函数 
	 */
	public interface OnLadyDetailManagerQueryLadyDetailCallback {
		/**
		 * 查询女士详细信息回调
		 * @param isSuccess			是否成功
		 * @param errno				错误码
		 * @param errmsg			错误提示
		 * @param item				女士资料详情
		 */
		public void OnQueryLadyDetailCallback(boolean isSuccess, String errno, String errmsg, LadyDetail item);
	}
	
	private static LadyDetailManager gLadyDetailManager;
	private Context mContext;
	public static LadyDetailManager newInstance(Context context) {
		if( gLadyDetailManager == null ) {
			gLadyDetailManager = new LadyDetailManager(context);
		} 
		return gLadyDetailManager;
	}
	
	public static LadyDetailManager getInstance() {
		return gLadyDetailManager;
	}
	
	public LadyDetailManager(Context context) {
		mContext = context;
		
		
	}
	
	/**
	 * 缓存女士香型Map表
	 */
	private Map<String, LadyDetail> mLadyDetailMap = new HashMap<>();
	
	/**
	 * 当前请求Id
	 */
	private long mRequestId = -1;
	
	/**
	 * 获取女士详情
	 */
	public void QueryLadyDetail(String womanId, final OnLadyDetailManagerQueryLadyDetailCallback callback) {
		// 本地查找
		String womanIdUpCase = womanId.toUpperCase();
		synchronized (mLadyDetailMap) {
			if( mLadyDetailMap.containsKey(womanIdUpCase) ) {
				LadyDetail item = mLadyDetailMap.get(womanIdUpCase);
				callback.OnQueryLadyDetailCallback(true, "", "", item);
			} else {
				// 调用接口
				mRequestId = RequestOperator.getInstance().QueryLadyDetail(womanIdUpCase, new OnQueryLadyDetailCallback() {
					
					@Override
					public void OnQueryLadyDetail(boolean isSuccess, String errno,
							String errmsg, LadyDetail item) {
						// TODO Auto-generated method stub
						if( isSuccess ) {
							mLadyDetailMap.put(item.womanid, item);
							
							// 下载头像
							String localPath = FileCacheManager.getInstance().CacheImagePathFromUrl(item.photoURL);
							new FileDownloader(mContext).StartDownload(item.photoURL, localPath, null);
						}
						
						mRequestId = -1;
						callback.OnQueryLadyDetailCallback(isSuccess, errno, errmsg, item);
					}
				});
			}
		}

	}
	
	public long QueryLadyList(
    		int pageIndex, 
    		int pageSize, 
    		SearchType searchType, 
    		String womanId, 
    		OnlineType isOnline, 
			int ageRangeFrom, 
			int ageRangeTo, 
			String country, 
			OrderType orderType,
			String deviceId,
			final OnQueryLadyListCallback callback
			) {
		return RequestJniLady.QueryLadyList(pageIndex, pageSize, searchType, womanId, isOnline, ageRangeFrom, ageRangeTo,
				country, orderType, deviceId, new OnQueryLadyListCallback() {
					
					@Override
					public void OnQueryLadyList(boolean isSuccess, String errno, String errmsg,
							Lady[] ladyList, int totalCount) {
						// TODO Auto-generated method stub
						if( ladyList != null ) {
							for(Lady lady : ladyList) {
								synchronized (mLadyDetailMap) {
									String womanIdUpCase = lady.womanid.toUpperCase();
									if( mLadyDetailMap.containsKey(womanIdUpCase) ) {
										LadyDetail item = mLadyDetailMap.get(womanIdUpCase);
										switch (lady.onlineStatus) {
										case Online:{
											item.isonline = true;
										}break;
										case Hidden:
										case Offline:{
											item.isonline = false;
										}break;
										default:
											break;
										}
									}
								}
							}
						}
						
						if( callback != null ) {
							callback.OnQueryLadyList(isSuccess, errno, errmsg, ladyList, totalCount);
						}
					}
				});
	}
	
	/**
	 * 请求收藏女士
	 */
	public void AddFavour(final String womanId, final OnRequestCallback callback) {
		RequestOperator.getInstance().AddFavouritesLady(womanId, new OnRequestCallback() {
			@Override
			public void OnRequest(boolean isSuccess, String errno, String errmsg) {
				// TODO Auto-generated method stub
				if( isSuccess ) {
					synchronized (mLadyDetailMap) {
						String womanIdUpCase = womanId.toUpperCase();
						if( mLadyDetailMap.containsKey(womanIdUpCase) ) {
							LadyDetail item = mLadyDetailMap.get(womanIdUpCase);
							item.isfavorite = true;
						}
					}
				}
				
				if( callback != null ) {
					callback.OnRequest(isSuccess, errno, errmsg);
				}
			}
		});
	}
	
	public void RemoveFavour(final String womanId, final OnRequestCallback callback) {
		RequestOperator.getInstance().RemoveFavouritesLady(womanId, new OnRequestCallback() {
			
			@Override
			public void OnRequest(boolean isSuccess, String errno, String errmsg) {
				// TODO Auto-generated method stub
				if( isSuccess ) {
					synchronized (mLadyDetailMap) {
						String womanIdUpCase = womanId.toUpperCase();
						if( mLadyDetailMap.containsKey(womanIdUpCase) ) {
							LadyDetail item = mLadyDetailMap.get(womanIdUpCase);
							item.isfavorite = false;
						}
					}
				}
				
				if( callback != null ) {
					callback.OnRequest(isSuccess, errno, errmsg);
				}
			}
		});
	}

	@Override
	public void OnLogin(LiveChatErrType errType, String errmsg,
			boolean isAutoLogin) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void OnLogout(LiveChatErrType errType, String errmsg,
			boolean isAutoLogin) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void OnGetTalkList(LiveChatErrType errType, String errmsg) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void OnGetHistoryMessage(boolean success, String errno,
			String errmsg, LCUserItem userItem) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void OnGetUsersHistoryMessage(boolean success, String errno,
			String errmsg, LCUserItem[] userItems) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void OnSetStatus(LiveChatErrType errType, String errmsg) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void OnGetUserStatus(LiveChatErrType errType, String errmsg,
			LCUserItem[] userList) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void OnUpdateStatus(LCUserItem userItem) {
		// TODO Auto-generated method stub
	}

	@Override
	public void OnRecvKickOffline(KickOfflineType kickType) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void OnRecvEMFNotice(String fromId, TalkEmfNoticeType noticeType) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void OnChangeOnlineStatus(LCUserItem userItem) {
		// TODO Auto-generated method stub
		synchronized (mLadyDetailMap) {
			String womanIdUpCase = userItem.userId.toUpperCase();
			if( mLadyDetailMap.containsKey(womanIdUpCase) ) {
				LadyDetail item = mLadyDetailMap.get(womanIdUpCase);
				switch (userItem.statusType) {
				case USTATUS_UNKNOW:
				case USTATUS_OFFLINE_OR_HIDDEN:{
					item.isonline = false;
				}break;
				case USTATUS_ONLINE:{
					item.isonline = true;
				}break;
				default:
					break;
				}
			}
		}
	}
	
}
