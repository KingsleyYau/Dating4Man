package com.qpidnetwork.request.item;

import java.io.Serializable;

import com.qpidnetwork.request.item.AdMainAdvert.OpenType;

/**
 * 女士列表广告
 */
public class AdWomanListAdvert implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -2817598787905328071L;
	
	public AdWomanListAdvert() {
		id = "";
		image = "";
		width = 0;
		height = 0;
		adurl = "";
		openType = OpenType.HIDE;
	}

	/**
	 * 女士列表广告
	 * @param id		广告ID
	 * @param image		广告图片URL
	 * @param width		图片宽度
	 * @param height	图片高度
	 * @param adurl		广告点击打开的URL
	 * @param openType	广告点击打开方式
	 */
	public AdWomanListAdvert(
			String id,
			String image,
			int width,
			int height,
			String adurl,
			int openType
			) 
	{
		this.id = id;
		this.image = image;
		this.width = width;
		this.height = height;
		this.adurl = adurl;
		this.openType = OpenType.values()[openType];
	}
	
	public String id;
	public String image;
	public int width;
	public int height;
	public String adurl;
	public OpenType openType;
}
