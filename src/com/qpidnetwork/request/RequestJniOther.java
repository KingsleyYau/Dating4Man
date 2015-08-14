package com.qpidnetwork.request;


/**
 * 女士模块接口
 * @author Samson.Fan
 *
 */
public class RequestJniOther {
	
	static public final int SiteTypeCL = 1;
	static public final int SiteTypeIDA = 2;
	static public final int SiteTypeCD = 4;
	static public final int SiteTypeLA = 8;
	static public final int SiteTypeAll = 0;
	static public final int SiteTypeUnknow = SiteTypeAll;

	/**
	 * 查询高级表情配置
	 * @param callback
	 * @return
	 */
	static public native long EmotionConfig(OnOtherEmotionConfigCallback callback);
	
	/**
	 * 男士会员统计
	 * @param money			是否需要money操作
	 * @param coupon		是否需要coupon操作
	 * @param regStep		是否需要regStep操作
	 * @param allowAlbum	是否需要allowAlbum操作
	 * @param admirerUr		是否需要admirerUr操作
	 * @param integral		是否需要积分
	 * @param callback
	 * @return
	 */
	static public native long GetCount(boolean money, boolean coupon, boolean regStep
			, boolean allowAlbum, boolean admirerUr, boolean integral, OnOtherGetCountCallback callback);
	
	public enum ActionType {
		SETUP,		// 新安装
		NEWUSER		// 新用户
	}
	
	/**
	 * 收集手机硬件信息
	 * @param manId			男士ID
	 * @param verCode		客户端内部版本号
	 * @param verName		客户端显示版本号
	 * @param action		新用户类型
	 * @param siteId		站点ID
	 * @param density		屏幕密度
	 * @param width			屏幕宽度
	 * @param height		屏幕高度
	 * @param lineNumber	电话号码
	 * @param simOptName	sim卡服务商名字
	 * @param simOpt		sim卡移动国家码
	 * @param simCountryIso	sim卡ISO国家码
	 * @param simState		sim卡状态
	 * @param phoneType		手机类型
	 * @param networkType	网络类型
	 * @param deviceId		设备唯一标识
	 * @param callback
	 * @return
	 */
	static public long PhoneInfo(String manId, int verCode, String verName, ActionType action, int siteId
			, double density, int width, int height, String lineNumber, String simOptName, String simOpt, String simCountryIso
			, String simState, int phoneType, int networkType, String deviceId
			, OnOtherPhoneInfoCallback callback) {
		return PhoneInfo(manId, verCode, verName, action.ordinal(), siteId
				, density, width, height, lineNumber, simOptName, simOpt, simCountryIso
				, simState, phoneType, networkType, deviceId
				, callback);
	}
	static protected native long PhoneInfo(String manId, int verCode, String verName, int action, int siteId
			, double density, int width, int height, String lineNumber, String simOptName, String simOpt, String simCountryIso
			, String simState, int phoneType, int networkType, String deviceId
			, OnOtherPhoneInfoCallback callback);
	
	/**
	 * 查询可否对某女士使用积分
	 * @param womanId	女士ID
	 * @param callback
	 * @return
	 */
	static public native long IntegralCheck(String womanId, OnOtherIntegralCheckCallback callback);
	
	/**
	 * 检查客户端更新
	 * @param currVer	当前客户端内部版本号
	 * @param callback
	 * @return
	 */
	static public native long VersionCheck(int currVer, OnOtherVersionCheckCallback callback);
	
	/**
	 * 同步配置
	 * @param callback
	 * @return
	 */
	static public native long SynConfig(OnOtherSynConfigCallback callback);
	
	/**
	 * 查询站点当前在线人数
	 * @param site		站点ID
	 * @param callback	
	 * @return
	 */
	static public native long OnlineCount(int site, OnOtherOnlineCountCallback callback);
	
	/**
	 * 上传错误日志
	 * @param directory		错误日志目录
	 * @param tmpDicectory	临时目录
	 * @param callback	
	 * @return
	 */
	static public native long UploadCrashLog(
			String deviceId, 
			String directory, 
			String tmpDicectory, 
			OnRequestCallback callback
			);
}
