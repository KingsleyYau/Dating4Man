package com.qpidnetwork.request;

import com.qpidnetwork.request.item.Coupon;

public interface OnCheckCouponCallCallback {
	public void OnCheckCoupon(long requestId, boolean isSuccess, String errno, String errmsg, Coupon item);
}
