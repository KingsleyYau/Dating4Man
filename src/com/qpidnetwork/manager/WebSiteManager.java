package com.qpidnetwork.manager;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.os.Environment;

import com.qpidnetwork.database.DatabaseHelper;
import com.qpidnetwork.dating.R;
import com.qpidnetwork.request.RequestJni;
import com.qpidnetwork.tool.CrashHandler;
import com.qpidnetwork.tool.CrashHandlerJni;

/**
 * 站点管理
 * @author Max.Chiu 
 */
public class WebSiteManager {
	public interface OnChangeWebsiteCallback {
		public void OnChangeWebsite(WebSite website);
	}
	/**
	 * 切换站点
	 */
	private List<OnChangeWebsiteCallback> mCallbackList = new ArrayList<OnChangeWebsiteCallback>();
	
	/* 站点基本信息数据持久化 */
	static String WEBSITE_MANAGER_PREFERENCES = "WEBSITE_MANAGER_PREFERENCES";
	/* 最后一次选择的站点 */
	static String LAST_WEBSITE_TYPE = "LAST_WEBSITE_TYPE";

	/**
	 * 站点类型
	 */
	public enum WebSiteType {
		ChnLove, CharmDate, IDateAsia, LatamDate,
	}

	private Context mContext = null;
	private static WebSiteManager gWebSiteManager;
	public Map<String, WebSite> mWebSiteMap = new HashMap<String, WebSite>();

	/**
	 * 缓存路径前序
	 */
//	public static String CACHE_PATH_PRE = Environment.getExternalStorageDirectory().getAbsolutePath() + "/qpidnetwork/";
	public static String CACHE_PATH_PRE = Environment.getExternalStorageDirectory().getAbsolutePath() + "/QpidDating/";

	/**
	 * 本地配置文件
	 */
	private SharedPreferences mSharedPreferences = null;

	/**
	 * 当前站点信息
	 */
	private WebSite mWebSite;
	
	/**
	 * 默认四站排序，用于换站选择时使用
	 */
	private WebSiteType[] defaultSortedWebSite = {
		WebSiteType.CharmDate, 
		WebSiteType.ChnLove, 
		WebSiteType.IDateAsia, 
		WebSiteType.LatamDate, 
	};

	/**
	 * 创建站点管理类实例
	 * 
	 * @param context
	 * @return 站点管理类实例
	 */
	public static WebSiteManager newInstance(Context context) {
		if (gWebSiteManager == null) {
			gWebSiteManager = new WebSiteManager(context);
		}
		return gWebSiteManager;
	}
	
	/**
	 * 获取单例
	 * @return
	 */
	public static WebSiteManager getInstance(){
		return gWebSiteManager;
	}

	private WebSiteManager(Context context) {
		mContext = context;
	}

	/**
	 * 初始化数据
	 */
	public void LoadData() {
		
		initConfig(mContext);
		
		/* 获取最后一次站点类型 */
		WebSiteType type = WebSiteType.ChnLove;
		mSharedPreferences = mContext.getSharedPreferences(
				WEBSITE_MANAGER_PREFERENCES, Context.MODE_PRIVATE);
		int index = mSharedPreferences.getInt(LAST_WEBSITE_TYPE, -1);
		if (index > -1 && index < WebSiteType.values().length) {
			type = WebSiteType.values()[index];
			ChangeWebSite(type);
		} 
	}
	
	/**
	 * 获取四站的默认排序的站名数组
	 * @return
	 */
	public String[] getDefaultSortedSiteNames(){
		String[] siteNames = new String[4];
		for(int i=0; i<defaultSortedWebSite.length; i++){
			siteNames[i] = mWebSiteMap.get(defaultSortedWebSite[i].name()).siteName;
		}
		return siteNames;
	}
	
	/**
	 * 获取四站的默认排序的站描述数组
	 * @return
	 */
	public String[] getDefaultSortedSiteDescs(){
		String[] siteDesc = new String[4];
		for(int i=0; i<defaultSortedWebSite.length; i++){
			siteDesc[i] = mWebSiteMap.get(defaultSortedWebSite[i].name()).siteDesc;
		}
		return siteDesc;
	}

	/**
	 * 增加监听器
	 * @param callback
	 */
	public void AddListenner(OnChangeWebsiteCallback callback) {
		mCallbackList.add(callback);
	}
	
	/**
	 * 删除监听器
	 * @param callback
	 */
	public void RemoveListenner(OnChangeWebsiteCallback callback) {
		mCallbackList.remove(callback);
	}
	
	/**
	 * 切换整站数据
	 * 
	 * @param type
	 *            站点类型
	 */
	public void ChangeWebSite(WebSiteType type) {
		
		mWebSite = mWebSiteMap.get(type.name());

		/* 切换缓存目录 */
		FileCacheManager.getInstance().ChangeMainPath(mWebSite.cachePath);
		
		/* 切换日志目录 */
		RequestJni.SetLogDirectory(FileCacheManager.getInstance().GetLogPath());
		
		/* 切换错误日志目录 */
		CrashHandlerJni.SetCrashLogDirectory(FileCacheManager.getInstance().GetCrashInfoPath());
		CrashHandler.getInstance().SaveAppVersionFile();
		
		/* 切换请求站点 */
		RequestJni.SetWebSite(mWebSite.webSiteHost,
				mWebSite.appSiteHost);
		
		/* 保存最后一次站点类型 */
		Editor editor = mSharedPreferences.edit();
		editor.putInt(LAST_WEBSITE_TYPE, type.ordinal());
		editor.commit();
		
		// 通知其他模块
		for(OnChangeWebsiteCallback callback : mCallbackList) {
			if( callback != null ) {
				callback.OnChangeWebsite(mWebSite);
			} 
		}
	}
	
	public WebSite GetWebSite() {
		return mWebSite;
	}

	/**
	 * 获取数据库实例
	 * 
	 * @return 数据库实例
	 */
	public DatabaseHelper GetDatabaseHelper() {
		return DatabaseHelper.getInstance(mContext, mWebSite.databaseName);
	}

	/**
	 * 获取缓存路径
	 * 
	 * @return 缓存路径
	 */
	public String GetCachePath() {
		return mWebSite.cachePath;
	}

	/**
	 * 获取facebook关注连接
	 * @return
	 */
	public String GetFacebookLink() {
		return mWebSite.facebookLink;
	}
	
	public void initConfig(Context context) {
		Resources res = context.getResources();
		TypedArray siteIdArray = res.obtainTypedArray(R.array.siteIdArray);
		TypedArray webHostArray = res.obtainTypedArray(R.array.webHostArray);
		TypedArray webKeyArray = res.obtainTypedArray(R.array.webKeyArray);
		TypedArray webNameArray = res.obtainTypedArray(R.array.webNameArray);
		TypedArray webDescArray = res.obtainTypedArray(R.array.webDescArray);
		TypedArray wwwHostArray = res.obtainTypedArray(R.array.wwwHostArray);
		TypedArray appThemeArray = res.obtainTypedArray(R.array.appThemeArray);
		TypedArray appColorArray = res.obtainTypedArray(R.array.appColorArray);
		TypedArray facebookArray = res.obtainTypedArray(R.array.facebookArray);
		TypedArray helpArray = res.obtainTypedArray(R.array.helpArray);
		TypedArray bounsArray = res.obtainTypedArray(R.array.bounsArray);
		
		int siteCount = siteIdArray.length();
		if(mWebSiteMap == null){
			mWebSiteMap = new HashMap<String, WebSite>();
		}
		for (int index = 0; index < siteCount; index++) {
			int siteId = siteIdArray.getInteger(index, 0);
			String appSiteHost = webHostArray.getString(index);
			String siteKey = webKeyArray.getString(index);
			String siteName = webNameArray.getString(index);
			String siteDesc = webDescArray.getString(index);
			String webSiteHost = wwwHostArray.getString(index);
			int siteTheme = appThemeArray.getResourceId(index, R.style.AppTheme);
			int siteColor = appColorArray.getResourceId(index, R.color.blue_color);
			String cachePath = CACHE_PATH_PRE + siteName;
			String databaseName = siteName;
			String facebookLink = facebookArray.getString(index);
			String helpLink = helpArray.getString(index);
			String bounsLink = bounsArray.getString(index);
			WebSite webSite = new WebSite(
					siteId, 
					appSiteHost, 
					siteKey,
					siteName, 
					siteDesc,
					webSiteHost, 
					siteTheme, 
					siteColor,
					cachePath, 
					databaseName, 
					facebookLink, 
					helpLink,
					bounsLink
					);
			mWebSiteMap.put(siteKey, webSite);
		}
		if(siteIdArray != null){
			siteIdArray.recycle();
		}
		if(webHostArray != null){
			webHostArray.recycle();
		}
		if(webNameArray != null){
			webNameArray.recycle();
		}
		if(webDescArray != null){
			webDescArray.recycle();
		}
		if(wwwHostArray != null){
			wwwHostArray.recycle();
		}
		if(appThemeArray != null){
			appThemeArray.recycle();
		}
		if(facebookArray != null){
			facebookArray.recycle();
		}
		if(helpArray != null){
			helpArray.recycle();
		}
	}

	public class WebSite {

		private int siteId; 			// 站点Id
		private String appSiteHost;		// app 使用服务器地址
		private String siteKey; 		// 站点名字
		private String siteName; 		// 站点名字
		private String siteDesc;		// 站点描述
		private String webSiteHost; 	// 网站 对应服务器地址
		private int siteTheme; 			// 当前站点使用主题资源Id
		private int siteColor;			// 当前站点使用主题颜色
		private String cachePath; 		// 缓存路径
		private String databaseName;	// 数据库名称
		private String facebookLink;	// facebook关注
		private String helpLink;		// 帮助
		private String bounsLink; 		// 积分链接

		public WebSite(
				int siteId, 
				String appSiteHost, 
				String siteKey,
				String siteName,
				String siteDesc, 
				String webSiteHost, 
				int siteTheme,
				int siteColor,
				String cachePath, 
				String databaseName, 
				String facebookLink, 
				String helpLink,
				String bounsLink
				) {
			this.siteId = siteId;
			this.appSiteHost = appSiteHost;
			this.siteKey = siteKey;
			this.siteName = siteName;
			this.siteDesc = siteDesc;
			this.webSiteHost = webSiteHost;
			this.siteTheme = siteTheme;
			this.siteColor = siteColor;
			this.cachePath = cachePath;
			this.databaseName = databaseName;
			this.facebookLink = facebookLink;
			this.helpLink = helpLink;
			this.bounsLink = bounsLink;
			
			File file = new File(cachePath);
			if( !file.exists() ) {
				file.mkdirs();
			}
		}

		public String getAppSiteHost() {
			return appSiteHost;
		}

		public void setAppSiteHost(String appSiteHost) {
			this.appSiteHost = appSiteHost;
		}

		public int getSiteId() {
			return siteId;
		}

		public void setSiteId(int siteId) {
			this.siteId = siteId;
		}

		public String getWebSiteHost() {
			return webSiteHost;
		}

		public void setWebSiteHost(String webSiteHost) {
			this.webSiteHost = webSiteHost;
		}

		public String getSiteKey() {
			return siteKey;
		}

		public void setSiteKey(String siteKey) {
			this.siteKey = siteKey;
		}
		
		public String getSiteName() {
			return siteName;
		}

		public void setSiteName(String siteName) {
			this.siteName = siteName;
		}

		public String getSiteDesc() {
			return siteDesc;
		}

		public void setSiteDesc(String siteDesc) {
			this.siteDesc = siteDesc;
		}

		public String getCachePath() {
			return cachePath;
		}

		public void setCachePath(String cachePath) {
			this.cachePath = cachePath;
		}
		
		public String getDatabaseName() {
			return databaseName;
		}

		public void setDatabaseName(String databaseName) {
			this.databaseName = databaseName;
		}
		
		public int getSiteTheme() {
			return siteTheme;
		}

		public void setSiteTheme(int siteTheme) {
			this.siteTheme = siteTheme;
		}
		
		public int getSiteColor() {
			return siteColor;
		}
		
		public void setSiteColor(int siteColor) {
			this.siteColor = siteColor;
		}
 		
		public String getFacebookLink() {
			return facebookLink;
		}
		
		public void setFacebookLink(String facebookLink) {
			this.facebookLink = facebookLink;
		}
		
		public String getHelpLink() {
			return helpLink;
		}
		
		public void setHelpLink(String helpLink) {
			this.helpLink = helpLink;
		}
		
		public String getBounsLink() {
			return bounsLink;
		}
		
		public void setBounsLink(String bounsLink) {
			this.bounsLink = bounsLink;
		}
		
	}
}
