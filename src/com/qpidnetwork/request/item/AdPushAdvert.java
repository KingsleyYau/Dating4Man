package com.qpidnetwork.request.item;

import java.io.Serializable;

import com.qpidnetwork.request.item.AdMainAdvert.OpenType;

/**
 * Push广告
 */
public class AdPushAdvert implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -1954442991423789276L;
	
	public AdPushAdvert() {
		
	}

	/**
	 * Push广告
	 * @param id		Push广告ID
	 * @param message	Push广告内容
	 * @param adurl		广告点击打开的URL
	 * @param openType	广告点击打开方式
	 */
	public AdPushAdvert(
			String id,
			String message,
			String adurl,
			int openType
			) 
	{
		this.id = id;
		this.message = message;
		this.adurl = adurl;
		this.openType = OpenType.values()[openType];
	}
	
	public String id;
	public String message;
	public String adurl;
	public OpenType openType;
}
