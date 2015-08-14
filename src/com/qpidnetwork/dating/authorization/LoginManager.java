package com.qpidnetwork.dating.authorization;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
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

import com.facebook.AccessToken;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionLoginBehavior;
import com.facebook.SessionState;
import com.facebook.SharedPreferencesTokenCachingStrategy;
import com.facebook.model.GraphUser;
import com.google.ads.conversiontracking.AdWordsConversionReporter;
import com.qpidnetwork.dating.QpidApplication;
import com.qpidnetwork.dating.authorization.LoginParam.LoginType;
import com.qpidnetwork.dating.googleanalytics.GAManager;
import com.qpidnetwork.framework.util.Log;
import com.qpidnetwork.manager.ConfigManager;
import com.qpidnetwork.manager.ConfigManager.OnConfigManagerCallback;
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

/**
 * 认证模块
 * 登录状态管理器 
 * @author Max.Chiu
 *
 */
public class LoginManager {
	
	private static final int ADWORDS_REGISTER_SUCCESS_UPDATE = 1;//adwords 注册成功跟踪

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
		 */
		public void OnLogout();
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
	private Handler mAdwordsHandler = null;
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
	private List<OnLoginManagerCallback> mCallbackList = new ArrayList<>();
	
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
				MessageCallbackItem obj = (MessageCallbackItem) msg.obj;
				if( obj.isSuccess ) {
					// 改变状态
		        	mLoginStatus = LoginStatus.LOGINED;
					
					// 登录成功, 处理是否记住密码, 自动登录等
					LoginPerfence.SaveLoginParam(
							mContext, 
							new LoginParam(obj.email, obj.password, obj.accessToken, obj.type, obj.loginItem));
					
					// 提交GAUserID到Google Analytics
					GAManager.newInstance().setGAUserId(obj.loginItem.ga_uid);
					// for test
					//String gaUserId = "53FB07A0EF8972C1DB43324E1A339F11";	// MD5("test_samson_2015")
					//GAManager.newInstance().setGAUserId(gaUserId);
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
				
				// 通知其他模块
				for(OnLoginManagerCallback callback : mCallbackList) {
					if( callback != null ) {
						callback.OnLogin(obj.isSuccess, obj.errno, obj.errmsg, obj.loginItem, obj.loginErrorItem);
					} 
				}
			}
		};
		
		mAdwordsHandler = new Handler(){
			public void handleMessage(Message msg) {
				if(msg.what == ADWORDS_REGISTER_SUCCESS_UPDATE){
					//添加Google推广监控
					AdWordsConversionReporter.reportWithConversionId(mContext.getApplicationContext(), "1072471539", "WDf5CKKc-VUQ87uy_wM", "0.00", true);
				}	
			};
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
	
	// facebook 登录状态改变回调
    private Session.StatusCallback sessionStatusCallback = new Session.StatusCallback() {
        @Override
        public void call(Session session, SessionState state, Exception exception) {
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
    };
    
	/**
	 * 初始化facebook
	 */
	public void InitFacebook() {
		if( currentSession != null ) {
			currentSession.removeCallback(sessionStatusCallback);
		}
		
		currentSession = new Session.Builder(mContext)
        .setTokenCachingStrategy(tokenCache)
        .build();

		currentSession.addCallback(sessionStatusCallback);
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
            Request request = Request.newMeRequest(currentSession, new Request.GraphUserCallback() {
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
            });
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
		Log.d("LoginManager", "Login( " +
								"email : " + email + ", " +
								"password : " + password + ", " + 
								"mLoginStatus : " + mLoginStatus.name() + 
								" )");
		
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
									mHandler.sendMessage(msg);

								}
							});
				} else {
					Message msg = Message.obtain();
					MessageCallbackItem obj = new MessageCallbackItem(errno, errmsg);
					obj.isSuccess = false;
					msg.obj = obj;
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

		// 先同步配置
		ConfigManager.getInstance().GetOtherSynConfigItem(new OnConfigManagerCallback() {
			
			@Override
			public void OnGetOtherSynConfigItem(boolean isSuccess, String errno,
					String errmsg, OtherSynConfigItem item) {
				// TODO Auto-generated method stub
				if( isSuccess ) {
					// 同步配置成功, 这里是主线程
					TelephonyManager tm = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);

					RequestJniAuthorization.LoginWithFacebook(
							finalAccessToken,
							email, 
							password, 
							RequestJni.GetDeviceId(tm), 
							String.valueOf(QpidApplication.versionCode),
							Build.MODEL, 
							Build.MANUFACTURER, 
							error,
							year,
							month,
							day,
							new OnLoginWithFacebookCallback() {
								
								@Override
								public void OnLoginWithFacebook(boolean isSuccess, String errno,
										String errmsg, LoginFacebookItem item, LoginErrorItem errItem) {
									// TODO Auto-generated method stub
							    	
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
									mHandler.sendMessage(msg);
									
									if(isSuccess 
											&& (item != null)
											&&(item.is_reg)){
										mAdwordsHandler.sendEmptyMessage(ADWORDS_REGISTER_SUCCESS_UPDATE);
									}
								}
							});
				} else {
					Message msg = Message.obtain();
					MessageCallbackItem obj = new MessageCallbackItem(errno, errmsg);
					obj.isSuccess = false;
					msg.obj = obj;
					mHandler.sendMessage(msg);
				}
			}
		});

    }
    
    /**
     * 注销
     */
    public void Logout() {
    	RequestJni.CleanCookies();
    	
		Log.d("LoginManager", "Logout( " + 
				"mLoginStatus : " + mLoginStatus.name() + 
				" )");
		
		if( mLoginStatus != LoginStatus.LOGINED ) {
			return;
		}
    	mLoginStatus = LoginStatus.NONE;
    	
    	LoginParam param = LoginPerfence.GetLoginParam(mContext);
    	if( param != null ) {
        	param.item = null;
    	}

    	LoginPerfence.SaveLoginParam(mContext, param);
    	
		// 通知其他模块
		for(OnLoginManagerCallback callback : mCallbackList) {
			if( callback != null ) {
				callback.OnLogout();
			} 
		}
    }
    
    public void LogoutAndClean() {
    	Logout();
		LoginParam param = LoginPerfence.GetLoginParam(mContext);
		if( param != null ) {
			param.password = "";
			param.accessToken = "";
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
    		for(OnLoginManagerCallback callback : mCallbackList) {
    			if( callback != null ) {
    				callback.OnLogin(false, RequestErrorCode.LOCAL_ERROR_CODE_NERVER_LOGIN, "Login fail!", null, null);
    			} 
    		}
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
		mHandler.sendMessage(msg);
		
		if(item.login) {
			//非非法注册账户才提交
			mAdwordsHandler.sendEmptyMessage(ADWORDS_REGISTER_SUCCESS_UPDATE);
		}
	}
}
