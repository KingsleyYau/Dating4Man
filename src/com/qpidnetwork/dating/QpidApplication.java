package com.qpidnetwork.dating;

import java.io.File;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;

import com.qpidnetwork.dating.advertisement.AdvertService;
import com.qpidnetwork.dating.advertisement.AdvertisementManager;
import com.qpidnetwork.dating.advertisement.IAdvertBinder;
import com.qpidnetwork.dating.authorization.LoginManager;
import com.qpidnetwork.dating.authorization.LoginManager.OnLoginManagerCallback;
import com.qpidnetwork.dating.authorization.PhoneInfoManager;
import com.qpidnetwork.dating.contacts.ContactManager;
import com.qpidnetwork.dating.googleanalytics.GAManager;
import com.qpidnetwork.dating.lady.LadyDetailManager;
import com.qpidnetwork.dating.quickmatch.QuickMatchManager;
import com.qpidnetwork.framework.util.Log;
import com.qpidnetwork.framework.util.SystemUtil;
import com.qpidnetwork.livechat.LiveChatManager;
import com.qpidnetwork.manager.ConfigManager;
import com.qpidnetwork.manager.FileCacheManager;
import com.qpidnetwork.manager.VirtualGiftManager;
import com.qpidnetwork.manager.WebSiteManager;
import com.qpidnetwork.request.RequestJni;
import com.qpidnetwork.request.RequestOperator;
import com.qpidnetwork.request.item.LoginErrorItem;
import com.qpidnetwork.request.item.LoginItem;
import com.qpidnetwork.tool.CrashHandler;
import com.qpidnetwork.tool.CrashHandlerJni;

public class QpidApplication extends Application implements OnLoginManagerCallback {
	
	public static boolean isDemo = false;
	public static boolean isDebug = false;
	public static int versionCode = 1;
	public static String versionName = "";
	public static Context mContext;
	private IAdvertBinder mAdvertBinder = null;
	private ServiceConnection mAdvertConnection = null;
	private Handler mHandler = null;
	
	private enum ApplicationHandleType {
		LOGIN_SUCCESS,
		LOGOUT,
	};
	
	
	public static String DEVICE_TYPE = "30"; 
	
	@Override
	public void onCreate() {
		super.onCreate();
		
		// 获取是否demo标志
		isDemo = getResources().getBoolean(R.bool.demo);
		// 判断是否测试（是，则打log，并提交到测试的GA环境）
		try {
			PackageInfo pInfo;
			pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
			isDebug = pInfo.versionName.matches(".*[a-zA-Z].*");
		} catch (NameNotFoundException e1) {
			// TODO Auto-generated catch block
		}
		
		// 设置log级别（demo环境才打印log）
		if (isDebug) {
			Log.SetLevel(android.util.Log.DEBUG);
		}
		else {
			Log.SetLevel(android.util.Log.ERROR);
		}
		
		// Jni错误捕捉
		CrashHandlerJni.SetCrashLogDirectory(WebSiteManager.CACHE_PATH_PRE + "/crash");
		
		// 初始化handler
		initHandler();
		
		mContext = this.getApplicationContext();
		
		// 初始化GA管理器
		if (isDebug || isDemo) {
			GAManager.newInstance().init(this, R.xml.tracker_demo);
		}
		else {
			GAManager.newInstance().init(this, R.xml.tracker);
		}
		
		// 设置demo请求环境
		if( isDemo ) {
			RequestJni.SetAuthorization("test", "5179");
		}
		
		// 设置客户端版本号
	   	try {
	   		PackageManager pm = getPackageManager();  
	        PackageInfo pi = pm.getPackageInfo(getPackageName(), PackageManager.GET_ACTIVITIES);  
	        if (pi != null) {
	        	// 版本号
	        	versionCode = pi.versionCode;
	        	versionName = pi.versionName;
	        	RequestJni.SetVersionCode(String.valueOf(versionCode));
	        	
	        	// 设备Id
	    		TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
	    		RequestJni.SetDeviceId(tm);
	        }  
	    } catch (NameNotFoundException e) {  
	    } 
	   	
	   	//deviceType
	   	DisplayMetrics dm = SystemUtil.getDisplayMetrics(this);
	   	double diagonalPixels = Math.sqrt(Math.pow(dm.widthPixels, 2) + Math.pow(dm.heightPixels, 2));
		double screenSize = diagonalPixels / (160 * dm.density); // 求出几寸（不是很精确）
		DEVICE_TYPE = screenSize >= 6 ? "34" : "30";

	   	// 请求管理类
	   	RequestOperator.newInstance(this);
	   	
		// 创建文件缓存管理器
	   	FileCacheManager.newInstance(this);
		
		// 创建站点切换管理器
		WebSiteManager wm = WebSiteManager.newInstance(this);
		RequestJni.SetCookiesDirectory(WebSiteManager.CACHE_PATH_PRE);
		RequestJni.CleanCookies();
		
		// crash日志管理器
		CrashHandler ch = CrashHandler.newInstance(this);
		
		// 创建登录状态个管理器
		LoginManager lm = LoginManager.newInstance(this);
		lm.AddListenner(this);
		
		// LiveChat
		LiveChatManager liveChatManager = LiveChatManager.newInstance(this);
		lm.AddListenner(liveChatManager);
		
		// 创建虚拟礼物管理器
		VirtualGiftManager vgm = VirtualGiftManager.newInstance(this);
		lm.AddListenner(vgm);
		
		// 创建QuickMatch管理器
		QuickMatchManager qmm = QuickMatchManager.newInstance(this);
		lm.AddListenner(qmm);
		wm.AddListenner(qmm);
		
		// 同步配置
		ConfigManager cm = ConfigManager.newInstance(this);
		cm.GetOtherSynConfigItem(null);
		
		// 女士详情管理器
		LadyDetailManager ldm = LadyDetailManager.newInstance(this);
		liveChatManager.RegisterOtherListener(ldm);
		
		// 最近联系人
		ContactManager ctm = ContactManager.newInstance(this);
		lm.AddListenner(ctm);

		// 初始化站点
		wm.LoadData();
		
		// 广告管理
		AdvertisementManager.newInstance(this);
		
		// 创建广告服务
		mAdvertConnection = new ServiceConnection() {
	        public void onServiceDisconnected(ComponentName name) {
	        	Log.d("QpidApplication","onServiceDisconnected()");
	        	mAdvertBinder = null;
	        }
	        public void onServiceConnected(ComponentName name, IBinder service) {
	        	Log.d("QpidApplication","onServiceConnected( )");
	        	mAdvertBinder = IAdvertBinder.Stub.asInterface(service);
	        	
	        	try {
	        		// 同步广告
					mAdvertBinder.Start();
				} catch (RemoteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	        }
		};
		
		Intent intentAdvertService = new Intent(this, AdvertService.class);
		startService(intentAdvertService);
		bindService(intentAdvertService, mAdvertConnection, Context.BIND_AUTO_CREATE);
		
//		EnableHttpResponseCache();
		
		// test for install referrer by Samson 
//		Intent it = new Intent("com.android.vending.INSTALL_REFERRER");
//		it.setPackage("com.qpidnetwork.dating");
//		it.putExtra("referrer", "utm_source%3DQpidnetworkCom%26utm_medium%3Dcpc%26utm_term%3Dandroid%252Bbrowser%26utm_content%3Dmaxthon%2520browser%2520for%2520android%26utm_campaign%3DYou%2520never%2520know%2520fast.");
//		sendBroadcast(it);
	}
	
	public static synchronized Context getContext(){
		return mContext;
	}
	
	/**
	 * 初始化handler
	 */
	@SuppressLint("HandlerLeak")
	private void initHandler()
	{
		mHandler = new Handler() {
			@Override
			public void handleMessage(Message msg)
			{
				switch (ApplicationHandleType.values()[msg.what]) {
				case LOGIN_SUCCESS: {
					if (msg.obj instanceof LoginItem) {
						LoginItem item = (LoginItem)msg.obj;
						// 发送收集手机硬件信息请求
						PhoneInfoManager.RequestPhoneInfo(mContext, item);
					}
				}break;
				case LOGOUT: {
					
				}break;
				}
			}
		};
	}


	@Override
	public void OnLogin(boolean isSuccess, String errno, String errmsg,
			LoginItem item, LoginErrorItem errItem) {
		// TODO Auto-generated method stub
		if (isSuccess) {
			Message msg = Message.obtain();
			msg.what = ApplicationHandleType.LOGIN_SUCCESS.ordinal();
			msg.obj = item;
			mHandler.sendMessage(msg);
		}
	}


	@Override
	public void OnLogout() {
		/*注销，清除所有Livechat 拍照临时图片*/
		String path = FileCacheManager.getInstance().getPrivatePhotoTempSavePath();
		File file = new File(path);
		if(file.isDirectory()){
			FileCacheManager.getInstance().doDelete(path);
		}
	}

	
//	/**
//	 * 缓存http
//	 * 为了兼容4.0以前版本，采用反射的方式获取api
//	 */
//	private void EnableHttpResponseCache() {
//		Log.d("QpidApplication", "EnableHttpResponseCache()");
//	    try {
//	        long httpCacheSize = 10 * 1024 * 1024; // 10 MiB
//	        File httpCacheDir = new File(FileCacheManager.getInstance().GetHttpPath()/*getCacheDir()*/);
//	        Class.forName("android.net.http.HttpResponseCache")
//	            .getMethod("install", File.class, long.class)
//	            .invoke(null, httpCacheDir, httpCacheSize);
//	    } catch (Exception ex) {
//	    	ex.printStackTrace();
//	    	Log.d("QpidApplication", "EnableHttpResponseCache( Exception : " + ex.toString() + " )");
//	    }
//	}
//	
//	private void DisableHttpResponseCache() {
//	    try {
//	        Object cache = Class.forName("android.net.http.HttpResponseCache")
//	            .getMethod("getInstalled")
//	            .invoke(null);
//	        
//	        if( cache != null ) {
//		        cache.getClass()
//		        .getMethod("flush")
//		        .invoke(cache);
//	        }
//	    } catch (Exception ex) {
//	    	ex.printStackTrace();
//	    	Log.d("QpidApplication", "DisableHttpResponseCache( Exception : " + ex.toString() + " )");
//	    }
//	}
}
