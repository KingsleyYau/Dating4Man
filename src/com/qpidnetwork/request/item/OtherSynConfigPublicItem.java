package com.qpidnetwork.request.item;

public class OtherSynConfigPublicItem {
	public OtherSynConfigPublicItem() {
		
	}

	/**
	 * 
	 * @param vgVer				虚拟礼物版本号
	 * @param apkVerCode		客户端内部版本号
	 * @param apkVerName		客户端显示版本号
	 * @param apkForceUpdate	是否强制更新
	 * @param facebook_enable   是否开通facebook登录
	 * @param apkFileVerify		安装包文件校验码
	 * @param url				安装包下载URL
	 * @param storeUrl			store url
	 * @param chatVoiceUrl		LiveChat语音下载/上传host
	 * @param addCreditsUrl		选择点数充值页面url
	 * @param addCredits2Url	指定点数充值页面url
	 */
	public OtherSynConfigPublicItem(
			int vgVer,
			int apkVerCode,
			String apkVerName,
			boolean apkForceUpdate,
			boolean facebook_enable,
			String apkFileVerify,
			String url,
			String storeUrl,
			String chatVoiceHostUrl,
			String addCreditsUrl,
			String addCredits2Url
			) 
	{
		this.vgVer = vgVer;
		this.apkVerCode = apkVerCode;
		this.apkVerName = apkVerName;
		this.apkForceUpdate = apkForceUpdate;
		this.facebook_enable = facebook_enable;
		this.apkFileVerify = apkFileVerify;
		this.url = url;
		this.storeUrl = storeUrl;
		this.chatVoiceHostUrl = chatVoiceHostUrl;
		this.addCreditsUrl = addCreditsUrl;
		this.addCredits2Url = addCredits2Url;
	}
	
	public int vgVer;
	public int apkVerCode;
	public String apkVerName;
	public boolean apkForceUpdate;
	public boolean facebook_enable;
	public String apkFileVerify;
	public String url;
	public String storeUrl;
	public String chatVoiceHostUrl;
	public String addCreditsUrl;
	public String addCredits2Url;
}
