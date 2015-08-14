package com.qpidnetwork.request;


/**
 * LiveChat使用试聊券
 */
public interface OnLCUseCouponCallback {
	public void OnLCUseCoupon(long requestId, boolean isSuccess, String errno, String errmsg, String userId);
}
