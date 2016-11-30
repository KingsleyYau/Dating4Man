package com.qpidnetwork.dating.gcm;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.os.AsyncTask;
import android.telephony.TelephonyManager;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.qpidnetwork.framework.util.Log;
import com.qpidnetwork.request.OnRequestCallback;
import com.qpidnetwork.request.RequestJni;
import com.qpidnetwork.request.RequestJniAuthorization;
import com.qpidnetwork.request.RequestOperator;

public class GCMPushManager {
	public static final String PROPERTY_REG_ID = "registration_id";
	private static final String PROPERTY_APP_VERSION = "appVersion";
	private static final String APP_TOKEN_PREFERENCE = "tokenPref";
	static final String TAG = "Hunter";
	
	private Context mContext;
	private GoogleCloudMessaging gcm;
	private String SENDER_ID ="303534397316";
	private String regid;
	private static GCMPushManager mGcmPushManager;
	
	public static GCMPushManager getInstance(Context context){
		if(mGcmPushManager == null){
			mGcmPushManager = new GCMPushManager(context);
		}
		return mGcmPushManager;
	}
	
	private GCMPushManager(Context context){
		mContext = context.getApplicationContext();
	}
	
	public void register(){
		if(checkPlayServices()){
			gcm = GoogleCloudMessaging.getInstance(mContext);
			regid = getRegistrationId(mContext);
			if(regid.isEmpty()){
				registerInBackground();
			}else{
				Log.i(TAG, "regId = " + regid);
				sendRegistrationIdToBackend();
			}
		}else{
			Log.i(TAG, "No valid Google Play Services APK found.");
		}
	}
	
	private boolean checkPlayServices(){
		int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(mContext);
		if(resultCode != ConnectionResult.SUCCESS){
			if(GooglePlayServicesUtil.isUserRecoverableError(resultCode)){
				Toast.makeText(mContext, GooglePlayServicesUtil.getErrorString(resultCode), Toast.LENGTH_LONG).show();
			}else{
				Log.i(TAG, "This device is not support.");
			}
			return false;
		}
		return true;
	}
	
	private void registerInBackground(){
		new AsyncTask<Void, Void, String>() {

			@Override
			protected String doInBackground(Void... params) {
				// TODO Auto-generated method stub
				String msg = "";
				try {
					if(gcm == null){
						gcm = GoogleCloudMessaging.getInstance(mContext); 
					}
					regid = gcm.register(SENDER_ID);
					Log.i(TAG, "Register regid : " + regid + " length: " + regid.length());
					msg = "Device registered, registration ID=" + regid;
					
					sendRegistrationIdToBackend();
					
					storeRegistrationId(mContext, regid);
				} catch (Exception e) {
					// TODO: handle exception
					msg = "Error : " + e.getMessage();
				}
				return msg;
			}
			
			@Override
			protected void onPostExecute(String result) {
				// TODO Auto-generated method stub
				super.onPostExecute(result);
			}
		}.execute(null, null, null);
	}
	
	/**
	 * 注销时解除绑定
	 */
	public void UnbindAppToken(OnRequestCallback callback){
		RequestJniAuthorization.UnbindAppToken(callback);
	}
	
	private void storeRegistrationId(Context context, String regId){
		final SharedPreferences prefs = getGcmPreferences(context);
		int appVersion = getAppVersion(context);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putString(PROPERTY_REG_ID, regId);
		editor.putInt(PROPERTY_APP_VERSION, appVersion);
		editor.commit();
	}
	
	private String getRegistrationId(Context context){
		final SharedPreferences prefs = getGcmPreferences(context);
		String registrationId = prefs.getString(PROPERTY_REG_ID, "");
		if(registrationId.isEmpty()){
			Log.i(TAG, "Registration not found");
			return "";
		}
		
		int registeredVersion = prefs.getInt(PROPERTY_APP_VERSION, Integer.MIN_VALUE);
		int currentVersion = getAppVersion(context);
		if(registeredVersion !=  currentVersion){
			Log.i(TAG, "App version changed");
			return null;
		}
		return registrationId;
	}
	
	private static int getAppVersion(Context context){
		try {
			PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
			return packageInfo.versionCode;
		} catch (Exception e) {
			// TODO: handle exception
			throw new RuntimeException("Could not get package name: " + e);
		}
	}
	
	
	private SharedPreferences getGcmPreferences(Context context){
		return mContext.getSharedPreferences(APP_TOKEN_PREFERENCE, Context.MODE_PRIVATE);
	}
	
	private void sendRegistrationIdToBackend(){
		TelephonyManager tm = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
		RequestOperator.getInstance().SummitTokenId(RequestJni.GetDeviceId(tm), regid, new OnRequestCallback() {
			
			@Override
			public void OnRequest(boolean isSuccess, String errno, String errmsg) {
				// TODO Auto-generated method stub
				Log.i(TAG, "Summit token isSuccess : " + isSuccess);
			}
		});
	}
	
	
}
