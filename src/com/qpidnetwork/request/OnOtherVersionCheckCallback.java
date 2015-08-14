package com.qpidnetwork.request;

import com.qpidnetwork.request.item.OtherVersionCheckItem;

public interface OnOtherVersionCheckCallback {
	public void OnOtherVersionCheck(boolean isSuccess, String errno, String errmsg, OtherVersionCheckItem item);
}
