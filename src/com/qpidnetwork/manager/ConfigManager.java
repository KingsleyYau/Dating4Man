package com.qpidnetwork.manager;

import android.content.Context;
import android.os.Handler;
import android.os.Message;

import com.qpidnetwork.request.OnOtherSynConfigCallback;
import com.qpidnetwork.request.RequestJni;
import com.qpidnetwork.request.RequestJniOther;
import com.qpidnetwork.request.item.OtherSynConfigItem;

/**
 * 同步配置管理器 
 * @author Max.Chiu
 *
 */
public class ConfigManager {
	/**
	 *	回调函数 
	 */
	public interface OnConfigManagerCallback {
		/**
		 * 查询同步配置回调
		 * @param isSuccess
		 * @param item
		 */
		public void OnGetOtherSynConfigItem(
				boolean isSuccess, 
				String errno,
				String errmsg, 
				OtherSynConfigItem item
				);
	}
	
	/**
	 * 界面消息
	 */
	private class MessageCallbackItem {
		/**
		 * 
		 * @param errno				接口错误码
		 * @param errmsg			错误提示
		 */
		public MessageCallbackItem(
				boolean isSuccess,
				String errno, 
				String errmsg,
				OtherSynConfigItem otherSynConfigItem,
				OnConfigManagerCallback callback
				) {
			this.isSuccess = isSuccess;
			this.errno = errno;
			this.errmsg = errmsg;
			this.otherSynConfigItem = otherSynConfigItem;
			this.callback = callback;
		}
		public boolean isSuccess;
		public String errno;
		public String errmsg;
		public OtherSynConfigItem otherSynConfigItem = null;
		public OnConfigManagerCallback callback = null;
	}
	
	private static ConfigManager gConfigManager;
	private OtherSynConfigItem mOtherSynConfigItem = null;
	
	public static ConfigManager newInstance(Context context) {
		if( gConfigManager == null ) {
			gConfigManager = new ConfigManager(context);
		} 
		return gConfigManager;
	}
	
	public static ConfigManager getInstance() {
		return gConfigManager;
	}
	
	
	/**
	 * 实例变量
	 */
//	private Context mContext = null;
	private Handler mHandler = null;
	
	public ConfigManager(Context context) {
//		mContext = context;
		mHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				MessageCallbackItem obj = (MessageCallbackItem) msg.obj;
				
				if( obj.isSuccess ) {
					// 设置语音站点
					mOtherSynConfigItem = obj.otherSynConfigItem;
					RequestJni.SetPublicWebSite(mOtherSynConfigItem.pub.chatVoiceHostUrl);
				}
				
				if( obj.callback != null ) {
					obj.callback.OnGetOtherSynConfigItem(obj.isSuccess, obj.errno, obj.errmsg, mOtherSynConfigItem);
				}
			}
		};
	}
	
	public void GetOtherSynConfigItem(final OnConfigManagerCallback callback) {
		if( mOtherSynConfigItem != null ) {
			// 直接返回列表
			if( callback != null ) {
				callback.OnGetOtherSynConfigItem(true, "", "", mOtherSynConfigItem);
			}
		} else {
			// 调用接口
			RequestJniOther.SynConfig(new OnOtherSynConfigCallback() {
				
				@Override
				public void OnOtherSynConfig(boolean isSuccess, String errno,
						String errmsg, OtherSynConfigItem item) {
					// TODO Auto-generated method stub
			    	
					Message msg = Message.obtain();
					MessageCallbackItem obj = new MessageCallbackItem(isSuccess, errno, errmsg, item, callback);
					obj.otherSynConfigItem = item;
					msg.obj = obj;
					mHandler.sendMessage(msg);
				}
			});
		}
	}
	
	public OtherSynConfigItem getSynConfigItem(){
		return mOtherSynConfigItem;
	}
}
