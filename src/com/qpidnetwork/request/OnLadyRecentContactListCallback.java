package com.qpidnetwork.request;

import com.qpidnetwork.request.item.LadyRecentContactItem;

public interface OnLadyRecentContactListCallback {
	public void OnLadyRecentContactList(boolean isSuccess, String errno, String errmsg, LadyRecentContactItem[] listArray);
}
