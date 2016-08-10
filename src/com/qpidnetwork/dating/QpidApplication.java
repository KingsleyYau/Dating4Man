package com.qpidnetwork.dating;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.lang.reflect.Field;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ApplicationInfo;
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
import com.qpidnetwork.dating.analysis.AdAnakysisManager;
import com.qpidnetwork.dating.authorization.LoginManager;
import com.qpidnetwork.dating.authorization.LoginManager.OnLoginManagerCallback;
import com.qpidnetwork.dating.authorization.PhoneInfoManager;
import com.qpidnetwork.dating.authorization.RegisterManager;
import com.qpidnetwork.dating.contacts.ContactManager;
import com.qpidnetwork.dating.googleanalytics.AnalyticsManager;
import com.qpidnetwork.dating.lady.LadyDetailManager;
import com.qpidnetwork.dating.quickmatch.QuickMatchManager;
import com.qpidnetwork.framework.util.Log;
import com.qpidnetwork.framework.util.SystemUtil;
import com.qpidnetwork.livechat.LiveChatManager;
import com.qpidnetwork.livechat.jni.LiveChatClientListener.KickOfflineType;
import com.qpidnetwork.manager.ConfigManager;
import com.qpidnetwork.manager.FileCacheManager;
import com.qpidnetwork.manager.MonthlyFeeManager;
import com.qpidnetwork.manager.VirtualGiftManager;
import com.qpidnetwork.manager.WebSiteManager;
import com.qpidnetwork.request.RequestJni;
import com.qpidnetwork.request.RequestOperator;
import com.qpidnetwork.request.item.LoginErrorItem;
import com.qpidnetwork.request.item.LoginItem;
import com.qpidnetwork.tool.CrashHandler;
import com.qpidnetwork.tool.CrashHandlerJni;

import dalvik.system.DexClassLoader;

public class QpidApplication extends Application implements OnLoginManagerCallback {
	
	public static boolean isDemo = false;
	public static boolean isDebug = false;
	public static int versionCode = 1;
	public static String versionName = "";
	public static Context mContext;
	private IAdvertBinder mAdvertBinder = null;
	private ServiceConnection mAdvertConnection = null;
	private Handler mHandler = null;
	
	//存放被踢时标志位及被踢时间，用于判断再次进入是否弹出被T提示
	public static boolean isKickOff = false;
	public static int lastestKickoffTime = 0;
	public static KickOfflineType kickOffType = KickOfflineType.Unknow;
	
	public static long mHomeActicityDestroyTime = 0;//用于推广计算弹出推广广告设计
	
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
		
		// 初始化handler
		initHandler();
		
		mContext = this.getApplicationContext();
		
		// 初始化GA管理器
		if (isDebug || isDemo) {
			AnalyticsManager.newInstance().init(this, R.xml.tracker_demo);
		}
		else {
			AnalyticsManager.newInstance().init(this, R.xml.tracker);
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

		// crash日志管理器
		CrashHandler.newInstance(this);

		// 创建月费管理器
		MonthlyFeeManager.newInstance(this);

		// 设置Jni错误捕捉log目录
		CrashHandlerJni.SetCrashLogDirectory(WebSiteManager.getInstance().GetCachePath() + "/crash");
		
		// 设置RequestJni的Cookies目录
		RequestJni.SetCookiesDirectory(WebSiteManager.getInstance().GetCachePath());
		RequestJni.CleanCookies();
		
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
	    		
//	    		Log.d("test", "deviceId:%s", RequestJni.GetDeviceId(tm));
	        }  
	    } catch (NameNotFoundException e) {  
	    } 
		
		// 创建登录状态个管理器
		LoginManager lm = LoginManager.newInstance(this);
		lm.AddListenner(this);
		
		//注册管理器
		RegisterManager.newInstance(this);
		
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
		lm.AddListenner(ldm);
		
		// 最近联系人
		ContactManager ctm = ContactManager.newInstance(this);
		lm.AddListenner(ctm);

		// 初始化站点
		wm.LoadData();
		
		//推广跟踪使用
		AdAnakysisManager.newInstance(this);
		
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
		
		/*清除EMF本地缓存的私密照及Video缓存*/
		clearLocalSource();
		
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
	 * 清除本地缓存文件
	 */
	private void clearLocalSource(){
		//清除本地缓存私密照（EMF）
		FileCacheManager.getInstance().clearAllPrivatePhotoCache();
		//清除本地缓存video(EMF)
		FileCacheManager.getInstance().clearAllVideoCache();
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
	public void OnLogout(boolean bActive) {
		/*注销，清除所有Livechat 拍照临时图片*/
		String path = FileCacheManager.getInstance().getPrivatePhotoTempSavePath();
		File file = new File(path);
		if(file.isDirectory()){
			FileCacheManager.getInstance().doDelete(path);
		}
	}

	
	/**
     * Copy the following code and call dexTool() after super.onCreate() in
     * Application.onCreate()
     * <p>
     * This method hacks the default PathClassLoader and load the secondary dex
     * file as it's parent.
     */
    @SuppressLint("NewApi")
    private void dexTool() {

        File dexDir = new File(getFilesDir(), "dlibs");
        dexDir.mkdir();
        File dexFile = new File(dexDir, "libs.apk");
        File dexOpt = new File(dexDir, "opt");
        dexOpt.mkdir();
        try {
            InputStream ins = getAssets().open("libs.apk");
            if (dexFile.length() != ins.available()) {
                FileOutputStream fos = new FileOutputStream(dexFile);
                byte[] buf = new byte[4096];
                int l;
                while ((l = ins.read(buf)) != -1) {
                    fos.write(buf, 0, l);
                }
                fos.close();
            }
            ins.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        ClassLoader cl = getClassLoader();
        ApplicationInfo ai = getApplicationInfo();
        String nativeLibraryDir = ai.nativeLibraryDir;
        DexClassLoader dcl = new DexClassLoader(dexFile.getAbsolutePath(),
                dexOpt.getAbsolutePath(), nativeLibraryDir, cl.getParent());

        try {
            Field f = ClassLoader.class.getDeclaredField("parent");
            f.setAccessible(true);
            f.set(cl, dcl);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
