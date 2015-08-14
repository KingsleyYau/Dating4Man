package com.qpidnetwork.request;

import com.qpidnetwork.request.item.OtherIntegralCheckItem;

public interface OnOtherIntegralCheckCallback {
	public void OnOtherIntegralCheck(boolean isSuccess, String errno, String errmsg, OtherIntegralCheckItem item);
}
