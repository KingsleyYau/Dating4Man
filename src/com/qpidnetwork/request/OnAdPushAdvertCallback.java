package com.qpidnetwork.request;

import com.qpidnetwork.request.item.AdPushAdvert;

/**
 * Push广告回调
 */
public interface OnAdPushAdvertCallback {
	public void OnAdPushAdvert(boolean isSuccess, String errno, String errmsg, AdPushAdvert[] advert);
}
