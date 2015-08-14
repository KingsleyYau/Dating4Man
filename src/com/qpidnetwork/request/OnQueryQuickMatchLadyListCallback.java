package com.qpidnetwork.request;

import com.qpidnetwork.request.item.QuickMatchLady;

public interface OnQueryQuickMatchLadyListCallback {
	public void OnQueryQuickMatchLadyList(boolean isSuccess, String errno, String errmsg, QuickMatchLady[] itemList);
}
