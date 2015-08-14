package com.qpidnetwork.request;

import com.qpidnetwork.request.item.AdMainAdvert;

/**
 * 主界面浮窗广告回调
 */
public interface OnAdMainAdvertCallback {
	public void OnAdMainAdvert(boolean isSuccess, String errno, String errmsg, AdMainAdvert advert);
}
