package com.qpidnetwork.request;

import com.qpidnetwork.request.item.OtherGetCountItem;

public interface OnOtherGetCountCallback {
	public void OnOtherGetCount(boolean isSuccess, String errno, String errmsg, OtherGetCountItem item);
}
