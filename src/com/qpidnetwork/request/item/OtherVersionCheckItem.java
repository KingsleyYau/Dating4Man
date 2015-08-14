package com.qpidnetwork.request.item;

import java.io.Serializable;

public class OtherVersionCheckItem implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 8173045249677502189L;
	public OtherVersionCheckItem() {
		
	}

	/**
	 * 
	 * @param verCode	客户端内部版本号
	 * @param verName	客户端显示版本号
	 * @param verDesc	版本描述
	 * @param url		Android客户端下载URL
	 * @param pubTime	发布时间
	 * @param checkTime	检测更新时间
	 */
	public OtherVersionCheckItem (
			int verCode,
			String verName,
			String verDesc,
			String url,
			String storeUrl,
			String pubTime,
			int checkTime
			) {
		this.verCode = verCode;
		this.verName = verName;
		this.verDesc = verDesc;
		this.url = url;
		this.storeUrl = storeUrl;
		this.pubTime = pubTime;
		this.checkTime = checkTime;
	}
	
	public int verCode;
	public String verName;
	public String verDesc;
	public String url;
	public String storeUrl;
	public String pubTime;
	public int checkTime;
}
