package com.qpidnetwork.request;

import com.qpidnetwork.request.item.QuickMatchLady;

public interface OnQueryQuickMatchLikeLadyListCallback {
	public void OnQueryQuickMatchLikeLadyList(boolean isSuccess, String errno, String errmsg, QuickMatchLady[] itemList, int totalCount);
}
