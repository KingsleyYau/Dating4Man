package com.qpidnetwork.dating.authorization;

import com.qpidnetwork.dating.authorization.RegisterPasswordActivity.RegisterParam;
import com.qpidnetwork.request.OnLoginWithFacebookCallback;
import com.qpidnetwork.request.OnRegisterCallback;

import android.content.Context;

public class RegisterManager {
	
	private static RegisterManager mRegisterManager = null;
	private Context mContext;
	
	private RegisterTask registerTask;//传统注册
	private FacebookRegisterTask facebookRegisterTask; //facebook注册登录
	
	public static RegisterManager newInstance(Context context) {
		if (mRegisterManager == null) {
			mRegisterManager = new RegisterManager(context);
		}
		return mRegisterManager;
	}
	
	public static RegisterManager getInstance() {
		return mRegisterManager;
	}
	
	public RegisterManager(Context context){
		this.mContext = context;
	}
	
	/**
	 * email 注册
	 * @param email
	 * @param password
	 * @param registerParam
	 * @param registerCallback
	 */
	public void emailRegister(String email, String password,
			RegisterParam registerParam, OnRegisterCallback registerCallback){
		registerTask = new RegisterTask(mContext, email, password, registerParam, registerCallback);
		registerTask.execute();
	}
	
	/**
	 * facebook 注册
	 * @param email
	 * @param password
	 * @param finalAccessToken
	 * @param error
	 * @param year
	 * @param month
	 * @param day
	 * @param registerCallback
	 */
	public void facebookRegister(String email, String password,
			String finalAccessToken, String error, String year, String month,
			String day, OnLoginWithFacebookCallback registerCallback){
		facebookRegisterTask = new FacebookRegisterTask(mContext, email, password, finalAccessToken, error, year, month, day, registerCallback);
		facebookRegisterTask.execute();
	}
}
