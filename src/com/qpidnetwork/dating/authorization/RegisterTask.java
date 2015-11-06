package com.qpidnetwork.dating.authorization;

import java.net.URLEncoder;

import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.telephony.TelephonyManager;

import com.facebook.AppEventsLogger;
import com.qpidnetwork.dating.analysis.AdAnakysisManager;
import com.qpidnetwork.dating.analysis.AnalysisItem;
import com.qpidnetwork.dating.authorization.RegisterPasswordActivity.RegisterParam;
import com.qpidnetwork.dating.bean.RequestBaseResponse;
import com.qpidnetwork.manager.ConfigManager;
import com.qpidnetwork.manager.ConfigManager.OnConfigManagerCallback;
import com.qpidnetwork.request.OnRegisterCallback;
import com.qpidnetwork.request.OnRequestCallback;
import com.qpidnetwork.request.RequestJni;
import com.qpidnetwork.request.RequestJniAuthorization;
import com.qpidnetwork.request.item.OtherSynConfigItem;
import com.qpidnetwork.request.item.RegisterItem;

/**
 * 完成注册3步骤 ： 1.同步配置； 2.提交utm数据； 3.注册
 * 
 * @author Hunter
 * 
 */
public class RegisterTask implements OnConfigManagerCallback,
									 OnRequestCallback,
									 OnRegisterCallback
{

	private OnRegisterCallback mRegisterCallback;
	private Handler mHandler;
	private Context mContext;

	/* 公共 */
	private String email = "";
	private String password = "";
	private RegisterParam mRegisterParam;

	private enum RequestFlag {
		SYNCONFIG_CALLBACK, UPLOAD_UTMREFERENCE_CALLBACK, REGISTER_CALLBACK,
	}

	public RegisterTask(Context context, String email, String password,
			RegisterParam registerParam, OnRegisterCallback registerCallback) {
		this.mContext = context;
		this.email = email;
		this.password = password;
		this.mRegisterParam = registerParam;
		this.mRegisterCallback = registerCallback;
		mHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				RequestBaseResponse response = (RequestBaseResponse) msg.obj;
				if ((!response.isSuccess) && (mRegisterCallback != null)) {
					mRegisterCallback.OnRegister(false, response.errno,
							response.errmsg, null);
					return;
				}
				switch (RequestFlag.values()[msg.what]) {
				case SYNCONFIG_CALLBACK: {
					if (response.isSuccess) {
						/* 同步配置成功 */
						AnalysisItem item = AdAnakysisManager.getInstance()
								.getAnalysisItem();
						if ((item != null) && (item.isSummit)) {
							// 安装已上传，直接调用注册接口
							emailRegister();
						} else {
							// 安装未上传，调用提交安装信息接口
							summitUtmreference();
						}
					}
				}
					break;
				case UPLOAD_UTMREFERENCE_CALLBACK: {
					if (response.isSuccess) {
						// 上传安装信息成功，注册
						emailRegister();
					}
				}
					break;
				case REGISTER_CALLBACK:
					if (response.isSuccess && (mRegisterCallback != null)) {
						mRegisterCallback.OnRegister(true, response.errno,
								response.errmsg, (RegisterItem) response.body);
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
	 * 提交安装信息
	 */
	private void summitUtmreference() {
		AdAnakysisManager.getInstance().summitUtmReference(this);
	}

	private void emailRegister() {
		if (mRegisterParam != null) {
			TelephonyManager tm = (TelephonyManager) mContext
					.getSystemService(Context.TELEPHONY_SERVICE);
			String reference = "";
			AnalysisItem item = AdAnakysisManager.getInstance()
					.getAnalysisItem();
			if ((item != null) && (item.utm_referrer != null)
					&& (item.utm_referrer.length() > 0)) {
				try {
					reference = URLEncoder
							.encode(item.utm_referrer, "US-ASCII");
				} catch (Exception e) {

				}
			}
			RequestJniAuthorization.Register(email, password,
					mRegisterParam.male, mRegisterParam.firstname,
					mRegisterParam.lastname, mRegisterParam.country,
					mRegisterParam.year, mRegisterParam.month,
					mRegisterParam.day, false, Build.MODEL,
					RequestJni.GetDeviceId(tm), Build.MANUFACTURER, reference,
					this);
		}
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
	public void OnRegister(boolean isSuccess, String errno, String errmsg,
			RegisterItem item) {
		// TODO Auto-generated method stub
		Message msg = Message.obtain();
		RequestBaseResponse response = new RequestBaseResponse(
				isSuccess, errno, errmsg, item);
		msg.what = RequestFlag.REGISTER_CALLBACK.ordinal();
		msg.obj = response;
		mHandler.sendMessage(msg);
	}
}
