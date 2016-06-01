package com.qpidnetwork.manager;

import java.util.Arrays;
import java.util.List;

import android.content.Context;
import android.os.Handler;
import android.os.Message;

import com.qpidnetwork.dating.authorization.LoginManager;
import com.qpidnetwork.dating.authorization.LoginManager.OnLoginManagerCallback;
import com.qpidnetwork.dating.authorization.LoginParam;
import com.qpidnetwork.request.OnQueryChatVirtualGiftCallback;
import com.qpidnetwork.request.RequestOperator;
import com.qpidnetwork.request.item.Gift;
import com.qpidnetwork.request.item.LoginErrorItem;
import com.qpidnetwork.request.item.LoginItem;
import com.qpidnetwork.tool.Arithmetic;

/**
 * 虚拟礼物管理器 
 * @author Max.Chiu
 *
 */
public class VirtualGiftManager implements OnLoginManagerCallback {
	
	/**
	 *	回调函数 
	 *
	 */
	public interface OnOnGetVirtualGiftCallback {
		public void OnGetVirtualGift(boolean isSuccess, List<Gift> itemList, String errno, String errmsg);
	}
	
	private static VirtualGiftManager gVirtualGiftManager;
	private Context mContext = null;
	private Handler mHandler = null;
	private boolean mIsUpdate = false;
	private String mVersion = "";
	
	/**
	 * 虚拟礼物列表
	 */
	private List<Gift> mGiftList = null;
	
	public static VirtualGiftManager newInstance(Context context) {
		if( gVirtualGiftManager == null ) {
			gVirtualGiftManager = new VirtualGiftManager(context);
		} 
		return gVirtualGiftManager;
	}
	
	public static VirtualGiftManager getInstance() {
		return gVirtualGiftManager;
	}
	
	public VirtualGiftManager(Context context) {
		mContext = context;
		
		InitHandler();
		
		// 读取本地缓存
		mGiftList = VirtualGiftPerfence.GetGiftList(mContext);
		mVersion = VirtualGiftPerfence.GetGiftVersion(mContext);
	}
	
	/**
	 * 虚拟礼物url前序
	 */
	private String VIRUAL_GIFT_PRE;// = "/virtual_img/pic/";
	
	/**
	 * 获取虚拟礼物图片url
	 * @param vgId		虚拟礼物Id
	 * @return
	 */
	public String GetVirtualGiftImage(String vgId) {
		String md5 = Arithmetic.MD5(vgId.getBytes(), vgId.getBytes().length);
		String url = String.format(
				"%s/%s/%s/%s_big.jpg", 
				WebSiteManager.getInstance().GetWebSite().getWebSiteHost(), 
				VIRUAL_GIFT_PRE, 
				vgId, 
				md5
				);
		return url;
	}
	
	/**
	 * 获取虚拟礼物视频url
	 * @param vgId		虚拟礼物Id
	 * @return
	 */
	public String GetVirtualGiftVideo(String vgId) {
		String md5 = Arithmetic.MD5(vgId.getBytes(), vgId.getBytes().length);
		String url = String.format(
				"%s/%s/%s/%s_video.3gp", 
				WebSiteManager.getInstance().GetWebSite().getWebSiteHost(), 
				VIRUAL_GIFT_PRE, 
				vgId, 
				md5
				);
		return url;
	}
	
	/**
	 * 根据vgId获取虚拟礼物图片缓存路径
	 * @param vgId		虚拟礼物Id
	 * @return			图片缓存路径
	 */
	public String CacheVirtualGiftImagePath(String vgId) {
		return FileCacheManager.getInstance().CacheVirtualGiftImagePath(GetVirtualGiftImage(vgId));
	}
	
	/**
	 * 根据vgId获取视频缓存路径
	 * @param vgId		虚拟礼物Id
	 * @return			视频缓存路径
	 */
	public String CacheVirtualGiftVideoPath(String vgId) {
		return FileCacheManager.getInstance().CacheVirtualGiftVideoPath(GetVirtualGiftImage(vgId));
	}

	/**
	 * 根据vgId获取礼物名字
	 * @param vgId		虚拟礼物Id
	 * @return			礼物名字
	 */
	public String GetVirtualGiftName(String vgId) {
		String name = "";
		if( mGiftList != null ) {
			for(Gift item : mGiftList) {
				if( item.vgid.compareTo(vgId) == 0 ) {
					name = item.title;
					break;
				}
			}
		}
		return name;
	}
	
	/**
	 * 获取虚拟礼物列表
	 * @return
	 */
	public void GetVirtualGift(final OnOnGetVirtualGiftCallback callback) {
		// 先获取本地
		if( mGiftList != null && mIsUpdate ) {
			if( callback != null ) {
				callback.OnGetVirtualGift(true, mGiftList, "", "");
			}
		}
		
		// 获取接口
		LoginParam param = LoginManager.getInstance().GetLoginParam();
		if( param != null && param.item != null) {
			RequestOperator.getInstance().QueryChatVirtualGift(
					param.item.sessionid, 
					param.item.manid, 
					new OnQueryChatVirtualGiftCallback() {
						
						@Override
						public void OnQueryChatVirtualGift(boolean isSuccess, String errno,
								String errmsg, Gift[] list, int totalCount, String path,
								String version) {
							// TODO Auto-generated method stub
							if( isSuccess ) {
								VIRUAL_GIFT_PRE = path;
								mGiftList = Arrays.asList(list);
								
								// 标记已经请求成功
								mIsUpdate = true;
								
								// 缓存到本地
								VirtualGiftPerfence.SaveGiftList(mContext, mGiftList);
								
								// 更新版本号, 清空旧缓存
								if( mVersion.compareTo(version) < 0 ) {
									mVersion = version;
									VirtualGiftPerfence.SaveGiftVersion(mContext, mVersion);
									FileCacheManager.getInstance().ClearVirtualGift();
								}
							}
							
							if( callback != null ) {
								callback.OnGetVirtualGift(isSuccess, mGiftList, "", "");
							}
						}
					});
		}
	}
	
	@Override
	public void OnLogin(boolean isSuccess, String errno, String errmsg,
			LoginItem item, LoginErrorItem errItem) {
		// TODO Auto-generated method stub
		if( isSuccess ) {
			// 登录成功，获取虚拟礼物列表
			Message msg = Message.obtain();
			mHandler.sendMessage(msg);
		}
	}
	
	@Override
	public void OnLogout(boolean bActive) {
		// TODO Auto-generated method stub
		
	}
	
	public void InitHandler() {
		mHandler = new Handler() {
			@Override
			public void handleMessage(android.os.Message msg) {
				// 登录成功，获取虚拟礼物列表
				GetVirtualGift(null);
			}
		};
	}
}
