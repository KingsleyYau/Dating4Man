package com.qpidnetwork.dating.authorization;

import java.net.URLEncoder;

import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.telephony.TelephonyManager;

import com.qpidnetwork.dating.QpidApplication;
import com.qpidnetwork.dating.analysis.AdAnakysisManager;
import com.qpidnetwork.dating.analysis.AnalysisItem;
import com.qpidnetwork.dating.bean.RequestBaseResponse;
import com.qpidnetwork.dating.googleanalytics.AnalyticsManager;
import com.qpidnetwork.manager.ConfigManager;
import com.qpidnetwork.manager.ConfigManager.OnConfigManagerCallback;
import com.qpidnetwork.request.OnLoginWithFacebookCallback;
import com.qpidnetwork.request.OnRequestCallback;
import com.qpidnetwork.request.RequestJni;
import com.qpidnetwork.request.RequestJniAuthorization;
import com.qpidnetwork.request.item.LoginErrorItem;
import com.qpidnetwork.request.item.LoginFacebookItem;
import com.qpidnetwork.request.item.OtherSynConfigItem;

public class FacebookRegisterTask implements OnLoginWithFacebookCallback, 
											 OnRequestCallback, 
											 OnConfigManagerCallback 
{

	private OnLoginWithFacebookCallback mRegisterCallback;
	private Handler mHandler;
	private Context mContext;

	/* 公共 */
	private String email = "";
	private String password = "";
	private String finalAccessToken = "";
	private String error = "";
	private String year = "";
	private String month = "";
	private String day = "";

	private enum RequestFlag {
		SYNCONFIG_CALLBACK, UPLOAD_UTMREFERENCE_CALLBACK, REGISTER_CALLBACK,
	}

	/**
	 * 界面消息
	 */
	private class MessageCallbackItem {
		/**
		 * 
		 * @param errno
		 *            接口错误码
		 * @param errmsg
		 *            错误提示
		 * @param loginItem
		 *            登录正常返回
		 */
		public MessageCallbackItem(boolean isSuccess, String errno,
				String errmsg) {
			this.isSuccess = isSuccess;
			this.errno = errno;
			this.errmsg = errmsg;
		}

		public String errno;
		public String errmsg;
		public boolean isSuccess;
		public LoginFacebookItem loginItem = null;
		public LoginErrorItem loginErrorItem = null;
	}

	public FacebookRegisterTask(Context context, String email, String password,
			String finalAccessToken, String error, String year, String month,
			String day, OnLoginWithFacebookCallback registerCallback) {
		this.mContext = context;
		this.mRegisterCallback = registerCallback;
		this.email = email;
		this.password = password;
		this.finalAccessToken = finalAccessToken;
		this.error = error;
		this.year = year;
		this.month = month;
		this.day = day;

		mHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {

				switch (RequestFlag.values()[msg.what]) {
				case SYNCONFIG_CALLBACK: {
					RequestBaseResponse response = (RequestBaseResponse) msg.obj;
					if ((!response.isSuccess) && (mRegisterCallback != null)) {
						mRegisterCallback.OnLoginWithFacebook(false,
								response.errno, response.errmsg, null, null);
					} else {
						/* 同步配置成功 */
						AnalysisItem item = AdAnakysisManager.getInstance()
								.getAnalysisItem();
						if ((item != null) && (item.isSummit)) {
							// 安装已上传，直接调用注册接口
							facebookRegister();
						} else {
							// 安装未上传，调用提交安装信息接口
							summitUtmreference();
						}
					}
				}
					break;
				case UPLOAD_UTMREFERENCE_CALLBACK: {
					RequestBaseResponse response = (RequestBaseResponse) msg.obj;
					if ((!response.isSuccess) && (mRegisterCallback != null)) {
						mRegisterCallback.OnLoginWithFacebook(false,
								response.errno, response.errmsg, null, null);
					} else {
						/* 上传安装信息成功 */
						facebookRegister();
					}
				}

					break;
				case REGISTER_CALLBACK: {
					MessageCallbackItem response = (MessageCallbackItem) msg.obj;
					if( response.isSuccess ) {
						// 注册跟踪
						AnalyticsManager.newInstance().RegisterSuccess(AnalyticsManager.RegisterType.Facebook);
					}
					
					if (mRegisterCallback != null) {
						mRegisterCallback.OnLoginWithFacebook(
								response.isSuccess, response.errno,
								response.errmsg, response.loginItem,
								response.loginErrorItem);
					}
				}
					break;

				default:
					break;
				}

			}
		};
	}

	public void execute() {
		ConfigManager.getInstance().GetOtherSynConfigItem(this);
	}

	/**
	 * 上传安装跟踪信息
	 */
	public void summitUtmreference() {
		AdAnakysisManager.getInstance().summitUtmReference(this);
	}

	public void register() {

	}

	private void facebookRegister() {

		TelephonyManager tm = (TelephonyManager) mContext
				.getSystemService(Context.TELEPHONY_SERVICE);
		String reference = "";
		AnalysisItem item = AdAnakysisManager.getInstance().getAnalysisItem();
		if ((item != null) && (item.utm_referrer != null)
				&& (item.utm_referrer.length() > 0)) {
			try {
				reference = URLEncoder.encode(item.utm_referrer, "US-ASCII");
			} catch (Exception e) {

			}
		}
		RequestJniAuthorization.LoginWithFacebook(finalAccessToken, email,
				password, RequestJni.GetDeviceId(tm),
				String.valueOf(QpidApplication.versionCode), Build.MODEL,
				Build.MANUFACTURER, error, year, month, day, reference,
				this);
	}

	@Override
	public void OnLoginWithFacebook(boolean isSuccess, String errno,
			String errmsg, LoginFacebookItem item, LoginErrorItem errItem) {
		// TODO Auto-generated method stub
		Message msg = Message.obtain();
		msg.what = RequestFlag.REGISTER_CALLBACK.ordinal();
		MessageCallbackItem obj = new MessageCallbackItem(
				isSuccess, errno, errmsg);
		obj.loginItem = item;
		obj.loginErrorItem = errItem;
		msg.obj = obj;
		mHandler.sendMessage(msg);
	}

	@Override
	public void OnRequest(boolean isSuccess, String errno, String errmsg) {
		// TODO Auto-generated method stub
		Message msg = Message.obtain();
		RequestBaseResponse response = new RequestBaseResponse(
				isSuccess, errno, errmsg, null);
		msg.what = RequestFlag.UPLOAD_UTMREFERENCE_CALLBACK
				.ordinal();
		msg.obj = response;
		mHandler.sendMessage(msg);
	}

	@Override
	public void OnGetOtherSynConfigItem(boolean isSuccess, String errno,
			String errmsg, OtherSynConfigItem item) {
		// TODO Auto-generated method stub
		Message msg = Message.obtain();
		RequestBaseResponse response = new RequestBaseResponse(
				isSuccess, errno, errmsg, item);
		msg.what = RequestFlag.SYNCONFIG_CALLBACK.ordinal();
		msg.obj = response;
		mHandler.sendMessage(msg);
	}
}
