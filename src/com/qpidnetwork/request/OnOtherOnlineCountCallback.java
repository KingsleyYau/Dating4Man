package com.qpidnetwork.request;

import com.qpidnetwork.request.item.OtherOnlineCountItem;

public interface OnOtherOnlineCountCallback {
	public void OnOtherOnlineCount(boolean isSuccess, String errno, String errmsg, OtherOnlineCountItem[] item);
}
