package com.qpidnetwork.request;

/**
 * App 推广广告
 * @author Hunter
 *
 */
public interface OnAppPromotionAdvertCallback {
	public void OnAppPromotionAdvert(boolean isSuccess, String errno, String errmsg, String adOverview);
}
