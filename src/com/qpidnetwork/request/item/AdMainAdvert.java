package com.qpidnetwork.request.item;

import java.io.Serializable;

/**
 * 主界面浮窗广告
 */
public class AdMainAdvert implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -8521553208842419233L;
	/**
	 * 广告URL打开方式
	 */
	public enum OpenType {
		HIDE,			// 隐藏打开
		SYSTEMBROWER,	// 系统浏览器打开
		APPBROWER,		// app内嵌浏览器打开
		UNKNOW			// 未知类型
	}
	
	public AdMainAdvert() {
		
	}

	/**
	 * 主界面广告
	 * @param id		广告ID
	 * @param image		广告图片URL
	 * @param width		图片宽度
	 * @param height	图片高度
	 * @param adurl		广告点击打开的URL
	 * @param openType	广告点击打开方式
	 * @param isShow	是否显示
	 * @param validTime	有效时间(Unix Timestamp)
	 */
	public AdMainAdvert(
			String id,
			String image,
			int width,
			int height,
			String adurl,
			int openType,
			boolean isShow,
			int validTime
			) 
	{
		this.id = id;
		this.image = image;
		this.width = width;
		this.height = height;
		this.adurl = adurl;
		this.openType = OpenType.values()[openType];
		this.isShow = isShow;
		this.validTime = validTime;
	}
	
	public String id ;
	public String image;
	public int width;
	public int height;
	public String adurl;
	public OpenType openType;
	public boolean isShow;
	public int validTime;
}
