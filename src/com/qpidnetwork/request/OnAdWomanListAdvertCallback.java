package com.qpidnetwork.request;

import com.qpidnetwork.request.item.AdWomanListAdvert;

/**
 * 女士列表广告回调
 */
public interface OnAdWomanListAdvertCallback {
	public void OnAdWomanListAdvert(boolean isSuccess, String errno, String errmsg, AdWomanListAdvert advert);
}
