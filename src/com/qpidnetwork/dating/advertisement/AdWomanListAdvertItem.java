package com.qpidnetwork.dating.advertisement;

import java.io.Serializable;

import android.content.Context;

import com.qpidnetwork.framework.util.StringUtil;
import com.qpidnetwork.request.item.AdWomanListAdvert;

/**
 * 本地女士列表广告
 */
public class AdWomanListAdvertItem implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -7320491134695847600L;

	/**
	 * 女士列表广告
	 */
	public AdWomanListAdvert adWomanListAdvert = null;

	/**
	 * 上一次获取的广告显示次数
	 */
	public int showTimes = 0;
	/**
	 * 上一次获取的广告点击次数
	 */
	public int clickTimes = 0;
	/**
	 * 是否显示
	 */
	public boolean isShow = false;
	
	public AdWomanListAdvertItem()
	{
		
	}
	
	public AdWomanListAdvertItem(AdWomanListAdvertItem item)
	{
		if (null != item.adWomanListAdvert)
		{
			this.adWomanListAdvert = new AdWomanListAdvert(item.adWomanListAdvert.id,
															item.adWomanListAdvert.image,
															item.adWomanListAdvert.width,
															item.adWomanListAdvert.height,
															item.adWomanListAdvert.adurl,
															item.adWomanListAdvert.openType.ordinal());
		}
		this.showTimes = item.showTimes;
		this.clickTimes = item.clickTimes;
		this.isShow = item.isShow;
	}

	/**
	 * 显示一次
	 */
	public void Show(Context context) {
		showTimes++;
		AdvertPerfence.SaveAdWomanListAdvertItem(context, this);
	}

	/**
	 * 点击一次
	 */
	public void Click(Context context) {
		clickTimes++;
		AdvertPerfence.SaveAdWomanListAdvertItem(context, this);
	}
	
	/**
	 * 设置女士列表广告
	 * @param advert
	 */
	public void SetWomanListAdvert(Context context, AdWomanListAdvert advert)
	{
		if (null != advert
			&& !StringUtil.isEmpty(advert.id)
			&& !StringUtil.isEmpty(advert.image))
		{
			if (null == adWomanListAdvert
				|| null == adWomanListAdvert.id
				|| adWomanListAdvert.id.isEmpty()
				|| advert.id.compareTo(adWomanListAdvert.id) != 0)
			{
				adWomanListAdvert = advert;
				showTimes = 0;
				clickTimes = 0;
			}
			isShow = true;
		}
		else {
			isShow = false;
		}
		
		AdvertPerfence.SaveAdWomanListAdvertItem(context, this);
	}
}
