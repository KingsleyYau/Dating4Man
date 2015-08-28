package com.qpidnetwork.dating.googleanalytics;

import android.app.Activity;
import android.app.Application;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

/**
 * GoogleAnalytics管理类
 * @author Samson Fan
 *
 */
public class GAManager 
{
	/** 
	 * 单例
	 */
	private static GAManager sInstance = null;
	
	private Tracker mTracker = null;
	private GoogleAnalytics mAnalytics = null;
	
	/**
	 * 获取单例
	 * @param application
	 * @return
	 */
	public static GAManager newInstance()
	{
		if (null == sInstance) {
			sInstance = new GAManager();
		}
		return sInstance;
	}
	
	/**
	 * 构造函数
	 */
	private GAManager()
	{
	}
	
	/**
	 * 初始化函数
	 * @param application
	 * @return
	 */
	public boolean init(Application application, int configResId)
	{
		boolean result = false;
		if (null != application) {
			mAnalytics = GoogleAnalytics.getInstance(application);
			if (null != mAnalytics) {
				mTracker = mAnalytics.newTracker(configResId);
			}
			result = (null != mTracker && null != mAnalytics);
		}
		return result;
	}
	
	/**
	 * 开始activity统计
	 * @param activity
	 */
	public void getReportStart(Activity activity) 
	{
		if (null != mAnalytics) {
			mAnalytics.reportActivityStart(activity);
		}
	}
	
	/**
	 * 停止activity统计
	 * @param activity
	 */
	public void getReportStop(Activity activity) 
	{
		if (null != mAnalytics) {
			mAnalytics.reportActivityStop(activity);
		}
	}
	
	/**
	 * 设置Google Analytics的user id
	 * @param gaUserId	GA's user id
	 */
	public void setGAUserId(String gaUserId)
	{
		if (null != mTracker
			&& null != gaUserId
			&& !gaUserId.isEmpty()) 
		{
			mTracker.set("&uid", gaUserId);
			mTracker.send(new HitBuilders.EventBuilder().setCategory("userid").setAction("User Sign In").build());
			mTracker.send(new HitBuilders.EventBuilder().setCategory("userid").setCustomDimension(2, gaUserId).build());
		}
	}
}
