package com.qpidnetwork.request;

import com.qpidnetwork.request.item.RegisterItem;

public interface OnRegisterCallback {
	public void OnRegister(boolean isSuccess, String errno, String errmsg, RegisterItem item);
}
