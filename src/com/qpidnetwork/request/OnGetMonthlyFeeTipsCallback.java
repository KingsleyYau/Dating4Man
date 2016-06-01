package com.qpidnetwork.request;

import com.qpidnetwork.request.item.MonthLyFeeTipItem;

public interface OnGetMonthlyFeeTipsCallback {
	public void OnGetMonthlyFeeTips(boolean isSuccess, String errno, String errmsg, MonthLyFeeTipItem[] tipList);
}
