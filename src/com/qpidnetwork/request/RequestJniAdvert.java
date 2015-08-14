package com.qpidnetwork.request;


/**
 * 广告模块接口
 */
public class RequestJniAdvert {

	/**
	 * 查询主界面浮窗广告
	 * @param deviceId		设备唯一标识
	 * @param advertId		上一次获取的广告ID
	 * @param showTimes		上一次获取的广告显示次数
	 * @param clickTimes	上一次获取的广告点击次数
	 * @param callback		回调object
	 * @return
	 */
	static public native long MainAdvert(String deviceId, String advertId, int showTimes, int clickTimes, OnAdMainAdvertCallback callback);
	
	/**
	 * 查询女士列表广告
	 * @param deviceId		设备唯一标识
	 * @param advertId		上一次获取的广告ID
	 * @param showTimes		上一次获取的广告显示次数
	 * @param clickTimes	上一次获取的广告点击次数
	 * @param callback		回调object
	 * @return
	 */
	static public native long WomanListAdvert(String deviceId, String advertId, int showTimes, int clickTimes, OnAdWomanListAdvertCallback callback);
	
	/**
	 * 查询Push广告
	 * @param deviceId		设备唯一标识
	 * @param pushId		上一次获取的最后一条Push广告ID
	 * @param callback		回调object
	 * @return
	 */
	static public native long PushAdvert(String deviceId, String pushId, OnAdPushAdvertCallback callback);
}
