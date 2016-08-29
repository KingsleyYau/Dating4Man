package com.qpidnetwork.dating.authorization;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.Signature;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.telephony.TelephonyManager;
import android.util.Base64;

import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.SharedPreferencesTokenCachingStrategy;
import com.facebook.model.GraphUser;
import com.google.ads.conversiontracking.AdWordsConversionReporter;
import com.qpidnetwork.dating.QpidApplication;
import com.qpidnetwork.dating.authorization.LoginParam.LoginType;
import com.qpidnetwork.dating.bean.RequestBaseResponse;
import com.qpidnetwork.dating.credit.AutoChargeManager;
import com.qpidnetwork.dating.googleanalytics.AnalyticsManager;
import com.qpidnetwork.framework.util.Log;
import com.qpidnetwork.manager.ConfigManager;
import com.qpidnetwork.manager.ConfigManager.OnConfigManagerCallback;
import com.qpidnetwork.manager.MonthlyFeeManager;
import com.qpidnetwork.manager.ThemeConfigManager;
import com.qpidnetwork.request.OnGetThemeConfigCallback;
import com.qpidnetwork.request.OnLoginCallback;
import com.qpidnetwork.request.OnLoginWithFacebookCallback;
import com.qpidnetwork.request.RequestErrorCode;
import com.qpidnetwork.request.RequestJni;
import com.qpidnetwork.request.RequestJniAuthorization;
import com.qpidnetwork.request.item.LoginErrorItem;
import com.qpidnetwork.request.item.LoginFacebookItem;
import com.qpidnetwork.request.item.LoginItem;
import com.qpidnetwork.request.item.OtherSynConfigItem;
import com.qpidnetwork.request.item.RegisterItem;
import com.qpidnetwork.request.item.ThemeConfig;

/**
 * 认证模块
 * 登录状态管理器 
 * @author Max.Chiu
 *
 */
public class LoginManager implements Session.StatusCallback, 
									 Request.GraphUserCallback
{
	
	private static final int ADWORDS_REGISTER_SUCCESS_UPDATE = 1;//adwords 注册成功跟踪
	private static final int LOGIN_STATUS_UPDATE = ADWORDS_REGISTER_SUCCESS_UPDATE + 1; // 登录状态改变
	private static final int REGISTER_STATUS_UPDATE = LOGIN_STATUS_UPDATE + 1; //注册成功更新
	private static final int GET_THEME_CONFIG_CALLBACK = REGISTER_STATUS_UPDATE + 1;//获取主题配置成功返回

	public interface OnLoginManagerCallback {
		/**
		 * 登录成功回调
		 * @param isSuccess			是否登录成功
		 * @param errno				错误代码
		 * @param errmsg			错误信息
		 * @param item				登录成功结构（可空）
		 * @param errItem			登录错误结构（可空）
		 */
		public void OnLogin(boolean isSuccess, String errno, String errmsg,
				LoginItem item, LoginErrorItem errItem);
		
		/**
		 * 注销成功回调
		 * @param bActive			是否超时
		 */
		public void OnLogout(boolean bActive);
	}
	
	/**
	 * 登录状态
	 * @param NONE			未登录
	 * @param LOGINING		登录中
	 * @param LOGINED		已登录
	 */
	public enum LoginStatus {
		NONE,
		LOGINING,
		LOGINED,
	}
	
	/**
	 * 界面消息
	 */
	private class MessageCallbackItem {
		/**
		 * 
		 * @param errno				接口错误码
		 * @param errmsg			错误提示
		 * @param loginItem			登录正常返回
		 */
		public MessageCallbackItem(
				String errno, 
				String errmsg
				) {
			this.errno = errno;
			this.errmsg = errmsg;
		}
		public String errno;
		public String errmsg;
		public boolean isSuccess;
		public LoginItem loginItem = null;
		public LoginErrorItem loginErrorItem = null;
		public String email;
		public String password;
		public String accessToken;
		public LoginType type;
	}
	
	/**
	 * 实例变量
	 */
	private Context mContext = null;
	private Handler mHandler = null;
	private boolean mbIsAutoLogin = true;
	private static LoginManager gLoginManager = null;
	
	public static LoginManager newInstance(Context context) {
		if (gLoginManager == null) {
			gLoginManager = new LoginManager(context);
		}
		return gLoginManager;
	}
	
	public static LoginManager getInstance() {
		return gLoginManager;
	}
	
	/**
	 * 登录状态改变监听
	 */
	private List<OnLoginManagerCallback> mCallbackList = new ArrayList<OnLoginManagerCallback>();
	
	/**
	 * facebook变量
	 */
	private static final String TOKEN_CACHE_NAME = "TOKEN_CACHE_NAME";
    private Session currentSession;
    private SharedPreferencesTokenCachingStrategy tokenCache;
    private GraphUser graphUser;
    
    /**
     * 当前登录状态
     */
    private LoginStatus mLoginStatus = LoginStatus.NONE;
    
	@SuppressLint("HandlerLeak")
	public LoginManager(Context context) {
		// TODO Auto-generated constructor stub
		mContext = context;
		
		mHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				if (msg.what == LOGIN_STATUS_UPDATE) 
				{
					MessageCallbackItem obj = (MessageCallbackItem) msg.obj;
					if(!obj.isSuccess){
						//failed 
						LoginStatusChangeProc(obj);
					}else{
						// 登录成功, 处理是否记住密码, 自动登录等
						LoginPerfence.SaveLoginParam(
								mContext, 
								new LoginParam(obj.email, obj.password, obj.accessToken, obj.type, obj.loginItem));
						
						//更新自动充值配置到AutoChargeManager
						if(obj.loginItem != null){
							AutoChargeManager.getInstatnce().setRechargeCredit(obj.loginItem.rechargeCredit);
						}
						
						AnalyticsManager.newInstance().setGAUserId(obj.loginItem.ga_uid);
						AnalyticsManager.newInstance().setGAActivity(obj.loginItem.gaActivity);
						
						getThemeConfig(obj.loginItem.sessionid, obj.loginItem.manid);
					}
					
					// test
//					String cookies = "";
//					String[] cookiesList = RequestJni.GetCookiesInfo();
//					for (String cookie : cookiesList)
//					{
//						if (!cookies.isEmpty()) {
//							cookies += ", ";
//						}
//						cookies += cookie;
//					}
//					Log.d("test", "cookies:%s", cookies);
				}
				else if (msg.what == ADWORDS_REGISTER_SUCCESS_UPDATE)
				{
					//添加Google推广监控
					if(!QpidApplication.isDemo){
						AdWordsConversionReporter.reportWithConversionId(mContext.getApplicationContext(), "1072471539", "WDf5CKKc-VUQ87uy_wM", "0.00", true);
					}
				}else if(msg.what == REGISTER_STATUS_UPDATE){
					MessageCallbackItem obj = (MessageCallbackItem) msg.obj;
					LoginStatusChangeProc(obj);
				}else if(msg.what == GET_THEME_CONFIG_CALLBACK){
					RequestBaseResponse response = (RequestBaseResponse)msg.obj;
					if(response.isSuccess){
						// 改变状态
			        	mLoginStatus = LoginStatus.LOGINED;
			        	mbIsAutoLogin = true;
					}else{
						mLoginStatus = LoginStatus.NONE;
					}
					// 通知其他模块
					LoginParam param = GetLoginParam();
					if(param != null && param.item != null){
						notifyAllListener(response.isSuccess, "", "", param.item, null);
					}else{
						notifyAllListener(false, "", "", null, null);
					}
				}
			}
		};
		
		// 检测apk签名
        PackageInfo info;
		try {
			info = mContext.getPackageManager().getPackageInfo(
			        "com.qpidnetwork.dating", 
			        PackageManager.GET_SIGNATURES);
	        for (Signature signature : info.signatures) {
	            MessageDigest md;
					try {
						md = MessageDigest.getInstance("SHA");
			            md.update(signature.toByteArray());
			            Log.d("LoginManager", "LoginManager( KeyHash: " + Base64.encodeToString(md.digest(), Base64.DEFAULT) + " )");
					} catch (NoSuchAlgorithmException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
	        }
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// 初始化facebook
		tokenCache = new SharedPreferencesTokenCachingStrategy(mContext, TOKEN_CACHE_NAME);
		InitFacebook();
	}
	
    
	/**
	 * 初始化facebook
	 */
	public void InitFacebook() {
		if( currentSession != null ) {
			currentSession.removeCallback(this);
		}
		
		currentSession = new Session.Builder(mContext)
        .setTokenCachingStrategy(tokenCache)
        .build();

		currentSession.addCallback(this);
	}
	
	/**
	 * 获取facebook session
	 * @return
	 */
	public Session GetFacebookSession() {
		return currentSession;
	}
	
	/**
	 * 获取facebook user info
	 */
	private void FetchUserInfo() {
		Log.d("LoginManager", "FetchUserInfo()");
        if ( currentSession != null && currentSession.isOpened() ) {
            Request request = Request.newMeRequest(currentSession, this);
            request.executeAsync();
        }
    }
	
	public GraphUser GetFacebookUserInfo() {
		return graphUser;
	}
	
	/**
	 * 增加登录结果监听器
	 * @param callback
	 */
	public void AddListenner(OnLoginManagerCallback callback) {
		mCallbackList.add(callback);
	}
	
	/**
	 * 删除登录结果监听器
	 * @param callback
	 */
	public void RemoveListenner(OnLoginManagerCallback callback) {
		mCallbackList.remove(callback);
	}
	
	public LoginParam GetLoginParam() {
		return LoginPerfence.GetLoginParam(mContext);
	}
	
	/**
	 * 登录接口
	 * @param email			账号
	 * @param password		密码
	 * @return				
	 */
	public void Login(final String email, final String password) {
		Login(email, password, "");
	}
	
	/**
	 * 登录接口
	 * @param email			账号
	 * @param password		密码
	 * @param checkcode		验证码
	 * @return				
	 */
	public void Login(final String email, final String password, final String checkcode) {
		Log.d("LoginManager", "Login( email:%s, password:%s, mLoginStatus:%s )", email, password, mLoginStatus.name());
		
		if( mLoginStatus != LoginStatus.NONE ) {
			return;
		}
		
    	mLoginStatus = LoginStatus.LOGINING;
    	
		// 先同步配置
		ConfigManager.getInstance().GetOtherSynConfigItem(new OnConfigManagerCallback() {
			
			@Override
			public void OnGetOtherSynConfigItem(boolean isSuccess, String errno,
					String errmsg, OtherSynConfigItem item) {
				// TODO Auto-generated method stub
				if( isSuccess ) {
					// 同步配置成功, 这里是主线程
					TelephonyManager tm = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
			    	
					RequestJniAuthorization.Login(
							email, 
							password, 
							checkcode,
							RequestJni.GetDeviceId(tm), 
							String.valueOf(QpidApplication.versionCode), 
							Build.MODEL, 
							Build.MANUFACTURER, 
							new OnLoginCallback() {
								
								@Override
								public void OnLogin(boolean isSuccess, String errno, String errmsg,
										LoginItem item) {
									// TODO Auto-generated method stub
							    	
									Message msg = Message.obtain();
									MessageCallbackItem obj = new MessageCallbackItem(errno, errmsg);
									obj.isSuccess = isSuccess;
									obj.loginItem = item;
									obj.email = email;
									obj.password = password;
									obj.accessToken = "";
									obj.type = LoginType.Default;
									msg.obj = obj;
									msg.what = LOGIN_STATUS_UPDATE;
									mHandler.sendMessage(msg);

								}
							});
				} else {
					Message msg = Message.obtain();
					MessageCallbackItem obj = new MessageCallbackItem(errno, errmsg);
					obj.isSuccess = false;
					msg.obj = obj;
					msg.what = LOGIN_STATUS_UPDATE;
					mHandler.sendMessage(msg);
				}
			}
		});
	}
	
	
	/**
	 * Facebook登录接口
	 * @param token			Facebook登录返回的token
	 * @param email			账号
	 * @param password		密码
	 * @return				请求Id
	 */
    public void LoginWithFacebook(
    		final String email, 
    		final String password, 
    		final String error,
    		final String year,
    		final String month,
    		final String day
    		) {  
    	// 优先使用当前token
    	String accessToken = currentSession.getAccessToken();
    	
    	if( accessToken == null || accessToken.length() ==0 ) {
    		// 使用本地token
        	LoginParam param = LoginPerfence.GetLoginParam(mContext);
       		if( param != null && param.accessToken != null ) {
    			accessToken = param.accessToken;
        	}
    	}
    	
   		// 最终token
    	final String finalAccessToken = (accessToken != null)?accessToken:"";
    	
		Log.d("LoginManager", "LoginWithFacebook( " +
				"email : " + email + ", " +
				"password : " + password + ", " + 
				"accessToken : " + accessToken + ", " +
				"error : " + error + ", " +
				"mLoginStatus : " + mLoginStatus.name() + 
				" )");
		
    	if( mLoginStatus != LoginStatus.NONE ) {
			return;
		}
		
    	mLoginStatus = LoginStatus.LOGINING;

//		// 先同步配置
//		ConfigManager.getInstance().GetOtherSynConfigItem(new OnConfigManagerCallback() {
//			
//			@Override
//			public void OnGetOtherSynConfigItem(boolean isSuccess, String errno,
//					String errmsg, OtherSynConfigItem item) {
//				// TODO Auto-generated method stub
//				if( isSuccess ) {
//					// 同步配置成功, 这里是主线程
//					TelephonyManager tm = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
//
//					RequestJniAuthorization.LoginWithFacebook(
//							finalAccessToken,
//							email, 
//							password, 
//							RequestJni.GetDeviceId(tm), 
//							String.valueOf(QpidApplication.versionCode),
//							Build.MODEL, 
//							Build.MANUFACTURER, 
//							error,
//							year,
//							month,
//							day,
//							new OnLoginWithFacebookCallback() {
//								
//								@Override
//								public void OnLoginWithFacebook(boolean isSuccess, String errno,
//										String errmsg, LoginFacebookItem item, LoginErrorItem errItem) {
//									// TODO Auto-generated method stub
//							    	
//									Message msg = Message.obtain();
//									MessageCallbackItem obj = new MessageCallbackItem(errno, errmsg);
//									obj.loginItem = new LoginItem(item);
//									obj.loginErrorItem = errItem;
//									obj.isSuccess = isSuccess;
//									obj.email = email;
//									obj.password = password;
//									obj.accessToken = finalAccessToken;
//									obj.type = LoginType.Facebook;
//									msg.obj = obj;
//									msg.what = LOGIN_STATUS_UPDATE;
//									mHandler.sendMessage(msg);
//									
//									if(isSuccess 
//											&& (item != null)
//											&&(item.is_reg)){
//										mAdwordsHandler.sendEmptyMessage(ADWORDS_REGISTER_SUCCESS_UPDATE);
//									}
//								}
//							});
//				} else {
//					Message msg = Message.obtain();
//					MessageCallbackItem obj = new MessageCallbackItem(errno, errmsg);
//					obj.isSuccess = false;
//					msg.obj = obj;
//					msg.what = LOGIN_STATUS_UPDATE;
//					mHandler.sendMessage(msg);
//				}
//			}
//		});
    	
    	RegisterManager.getInstance().facebookRegister(email, password, finalAccessToken, error, year, month, day, new OnLoginWithFacebookCallback() {
			
			@Override
			public void OnLoginWithFacebook(boolean isSuccess, String errno,
					String errmsg, LoginFacebookItem item, LoginErrorItem errItem) {
				Message msg = Message.obtain();
				MessageCallbackItem obj = new MessageCallbackItem(errno, errmsg);
				obj.loginItem = new LoginItem(item);
				obj.loginErrorItem = errItem;
				obj.isSuccess = isSuccess;
				obj.email = email;
				obj.password = password;
				obj.accessToken = finalAccessToken;
				obj.type = LoginType.Facebook;
				msg.obj = obj;
				if(item != null && item.is_reg){
					msg.what = REGISTER_STATUS_UPDATE;
				}else{
					msg.what = LOGIN_STATUS_UPDATE;
				}
				mHandler.sendMessage(msg);
				
				if(isSuccess 
					&& item != null
					&& item.is_reg)
				{
					mHandler.sendEmptyMessage(ADWORDS_REGISTER_SUCCESS_UPDATE);
				}
			}
		});

    }
    
    /**
     * 注销
     * @param bActive			是否超时
     */
    public void Logout(boolean bActive) {
    	RequestJni.CleanCookies();
    	
		Log.d("LoginManager", "Logout( " + 
				"mLoginStatus : " + mLoginStatus.name() + 
				" )");
		
		if( mLoginStatus != LoginStatus.LOGINED ) {
			return;
		}
    	mLoginStatus = LoginStatus.NONE;
    	
		// 通知其他模块
		for(OnLoginManagerCallback callback : mCallbackList) {
			if( callback != null ) {
				callback.OnLogout(bActive);
			} 
		}
    }
    
    /**
     * 非主动注销
     */
    public void Logout() {
    	Logout(false);
    }
    
    public void LogoutAndClean(boolean bKick) {
    	Logout(true);
    	
		LoginParam param = LoginPerfence.GetLoginParam(mContext);
		if( param != null ) {
			mbIsAutoLogin = false;
			if( !bKick ) {
				param.password = "";
				param.accessToken = "";
			}
			// 修改为主动注销时，才清除用户信息，防止底层session过期重登陆过程中，使用到manId等地方异常
        	param.item = null;
			LoginPerfence.SaveLoginParam(mContext, param);
		}
    }
    
    /**
     * 自动登录
     */
    public void AutoLogin() {
		Log.d("LoginManager", "AutoLogin( " + 
				"mLoginStatus : " + mLoginStatus.name() + 
				" )");
		
		if( mLoginStatus != LoginStatus.NONE ) {
			return;
		}
		
		if( !mbIsAutoLogin ) {
			return;
		}
		
		boolean bCallback = true;
    	LoginParam param = LoginPerfence.GetLoginParam(mContext);
    	if( param != null ) {
    		switch (param.type) {
			case Default:{
				Log.d("LoginManager", "AutoLogin( Default )");
				if( param.email != null && param.email.length() > 0 &&
			    		param.password != null && param.password.length() > 0 ) {
					Login(param.email, param.password);
					bCallback = false;
				}
			}break;
			case Facebook:{
				Log.d("LoginManager", "AutoLogin( Facebook )");
				if( param.accessToken != null && param.accessToken.length() > 0 ) {
					LoginWithFacebook("", "", "", "", "", "");
					bCallback = false;
				}
			}break;
			default:{
			}break;
			}
    	} 
		
    	if( bCallback ) {
    		// 通知其他模块
    		notifyAllListener(false, RequestErrorCode.LOCAL_ERROR_CODE_NERVER_LOGIN, "Login fail!", null, null);
    	}
    }
    
    /**
     * 获取当前登录状态
     * @return
     */
    public LoginStatus GetLoginStatus() {
    	return mLoginStatus;
    }
    
	/**
	 * 判断是否登录, 并弹出对应界面
	 */
	public boolean CheckLogin(Context context) {
		return CheckLogin(context, true);
	}
	
	/**
	 * 判断是否登录, 并弹出对应界面
	 */
	public boolean CheckLogin(Context context, boolean bShowLoginActivity) {
		return CheckLogin(context, bShowLoginActivity, "");
	}
	
	/**
	 * 判断是否登录, 并弹出对应界面, 并传递参数
	 */
	public boolean CheckLogin(Context context, boolean bShowLoginActivity, String param) {
		boolean bFlag = false;
		// 判断是否登录
		switch (LoginManager.getInstance().GetLoginStatus()) {
		case NONE: {
			// 处于未登录状态
		}			
		case LOGINING:{
			// 处于未登录状态, 点击弹出登录界面
			if( bShowLoginActivity ) {
				Intent intent = new Intent(mContext, RegisterActivity.class);
				//传递参数 
				if (null != param && !param.isEmpty()) {
					intent.putExtra("param", param);
				}
				
				context.startActivity(intent);
			}
		}break;
		case LOGINED: {
			// 处于登录状态
			bFlag = true;
		}break;
		default:
			break;
		}
		return bFlag;
	}
	
	/**
	 * 注册成功, 并且登录成功处理
	 */
	public void OnRegisterSuccess(String email, String password, RegisterItem item) {
		LoginItem loginItem = new LoginItem(item);
//		loginItem.manid = item.manid;
//		loginItem.email = item.email;
//		loginItem.firstname = item.firstname;
//		loginItem.lastname = item.lastname;
//		loginItem.photoURL = item.photoURL;
//		loginItem.reg_step = item.reg_step;
//		loginItem.sessionid = item.sessionid;
//		loginItem.ga_uid = item.ga_uid;
//		loginItem.photosend = item.photosend;
//		loginItem.photoreceived = item.photoreceived;
		
		
		//添加Google推广监控
//		AdWordsConversionReporter.reportWithConversionId(mContext.getApplicationContext(), "1072471539", "WDf5CKKc-VUQ87uy_wM", "0.00", true);
		
		Message msg = Message.obtain();
		MessageCallbackItem obj = new MessageCallbackItem("", "");
		obj.loginItem = loginItem;
		obj.isSuccess = true;
		obj.email = email;
		obj.password = password;
		obj.type = LoginType.Default;
		msg.obj = obj;
		msg.what = REGISTER_STATUS_UPDATE;
		mHandler.sendMessage(msg);
		
		if(item.login) {
			//非非法注册账户才提交
			mHandler.sendEmptyMessage(ADWORDS_REGISTER_SUCCESS_UPDATE);
		}
	}
	
	/**
	 * 登录状态改变处理函数
	 * @param obj
	 */
	private void LoginStatusChangeProc(MessageCallbackItem obj)
	{
		if( obj.isSuccess ) {
			// 改变状态
        	mLoginStatus = LoginStatus.LOGINED;
        	mbIsAutoLogin = true;
        	
			// 登录成功, 处理是否记住密码, 自动登录等
			LoginPerfence.SaveLoginParam(
					mContext, 
					new LoginParam(obj.email, obj.password, obj.accessToken, obj.type, obj.loginItem));
			
			//更新自动充值配置到AutoChargeManager
			if(obj.loginItem != null){
				AutoChargeManager.getInstatnce().setRechargeCredit(obj.loginItem.rechargeCredit);
			}
			
			// 提交GAUserID到Google Analytics
			//测试GA提交用户Id
//			Log.file("LoginManager", "AnalyticsManager setGAUserId usrId： " + obj.loginItem.ga_uid);
			
			AnalyticsManager.newInstance().setGAUserId(obj.loginItem.ga_uid);
			AnalyticsManager.newInstance().setGAActivity(obj.loginItem.gaActivity);
		} else {
        	mLoginStatus = LoginStatus.NONE;
			switch (obj.errno) {
			case RequestErrorCode.MBCE64005:{
				// 清空本地token
				LoginParam param = LoginPerfence.GetLoginParam(mContext);
				if( param != null ) {
					param.accessToken = "";
					LoginPerfence.SaveLoginParam(mContext, param);
				}
			}break;
			default:
				break;
			}
		}
		notifyAllListener(obj.isSuccess, obj.errno, obj.errmsg, obj.loginItem, obj.loginErrorItem);
		
	}
	
	/**
	 * 登陆完成通知其他模块
	 * @param isSuccess
	 * @param errno
	 * @param errmsg
	 * @param item
	 * @param errItem
	 */
	private void notifyAllListener(boolean isSuccess, String errno, String errmsg, LoginItem item, LoginErrorItem errItem){
		if(isSuccess){
			//登陆成功刷新月费配置
			MonthlyFeeManager.getInstance().QueryMemberType();
			MonthlyFeeManager.getInstance().GetMonthlyFeeTips();
		}
		// 通知其他模块
		for(OnLoginManagerCallback callback : mCallbackList) {
			if( callback != null ) {
				callback.OnLogin(isSuccess, errno, errmsg, item, errItem);
			} 
		}
	}

	@Override
	public void call(Session session, SessionState state, Exception exception) {
		// TODO Auto-generated method stub
		if( session != null ) {
        	Log.d("LoginManager", "Session.StatusCallback.call( "
    				+ String.format(
    						"Session : %s, state : %s", 
    						session.toString(), 
    						state.toString()
    						)
    				+ " )");
    	}

    	if( exception != null ) {
        	Log.d("LoginManager", "Session.StatusCallback.call( "
    				+ String.format("Error: %s", exception.toString())
    				+ " )");
			Message msg = Message.obtain();
			MessageCallbackItem obj = new MessageCallbackItem(RequestErrorCode.LOCAL_ERROR_CODE_FACEBOOK_FAIL, "Facebook login error!");
			obj.isSuccess = false;
			msg.obj = obj;
			msg.what = LOGIN_STATUS_UPDATE; 
			mHandler.sendMessage(msg);
    	}
    	
    	if( currentSession != session ) {
    		Log.d("LoginManager", "Session.StatusCallback.call( currentSession != session )");
    		return;
    	}
    	
    	if( state.isOpened() ) {
    		// facebook授权成功, 调用facebook登录接口
    		Log.d("LoginManager", "Session.StatusCallback.call( facebook session opened )");
    		FetchUserInfo();
    	} else {
    		Log.d("LoginManager", "Session.StatusCallback.call( facebook session not open )");
    	}
	}

	@Override
	public void onCompleted(GraphUser user, Response response) {
		// TODO Auto-generated method stub
		Log.d("LoginManager", "FetchUserInfo。onCompleted( " +
				"response : " + response.toString() + 
				" )");
		
		if (response.getRequest().getSession() == currentSession && user != null ) {
			Log.d("LoginManager", "FetchUserInfo.onCompleted( facebook user info ok )");
			graphUser = user;
			LoginWithFacebook("", "", "", "", "", "");
        }
	}
	
	/**
	 * 获取主题配置
	 * @param user_sid
	 * @param user_id
	 */
	private void getThemeConfig(String user_sid, String user_id){
		ThemeConfigManager.newInstance().GetThemeConfig(user_sid, user_id, new OnGetThemeConfigCallback() {
			
			@Override
			public void OnGetThemeConfig(boolean isSuccess, String errno,
					String errmsg, ThemeConfig config) {
				// TODO Auto-generated method stub
				Message msg = Message.obtain();
				RequestBaseResponse response = new RequestBaseResponse(isSuccess, errno, errmsg, config);
				msg.what = GET_THEME_CONFIG_CALLBACK;
				msg.obj = response;
				mHandler.sendMessage(msg);
			}
		});
	}
}
