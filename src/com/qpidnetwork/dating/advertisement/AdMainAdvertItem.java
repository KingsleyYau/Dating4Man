package com.qpidnetwork.dating.advertisement;

import java.io.Serializable;

import android.content.Context;

import com.qpidnetwork.request.item.AdMainAdvert;

/**
 * 本地主界面浮窗广告
 */
public class AdMainAdvertItem implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -6532166415357339295L;

	/**
	 * 主界面浮窗广告
	 */
	public AdMainAdvert adMainAdvert;

	/**
	 * 上一次获取的广告显示次数
	 */
	public int showTimes = 0;
	/**
	 * 上一次获取的广告点击次数
	 */
	public int clickTimes = 0;

	/**
	 * 显示一次
	 */
	public void Show(Context context) {
		showTimes++;
		AdMainAdvertItem latestAdvert = AdvertPerfence
				.GetAdMainAdvert(context);
		if ((latestAdvert != null)
				&& (latestAdvert.adMainAdvert.id
						.equals(this.adMainAdvert.id))) {
			/* 显示广告仍为最新广告未被替换 */
			AdvertPerfence.SaveAdMainAdvertItem(context, this);
		}
	}

	/**
	 * 点击一次
	 */
	public void Click(Context context) {
		clickTimes++;
		AdMainAdvertItem latestAdvert = AdvertPerfence
				.GetAdMainAdvert(context);
		if ((latestAdvert != null)
				&& (latestAdvert.adMainAdvert.id
						.equals(this.adMainAdvert.id))) {
			/* 显示广告仍为最新广告未被替换 */
			AdvertPerfence.SaveAdMainAdvertItem(context, this);
		}
	}
}