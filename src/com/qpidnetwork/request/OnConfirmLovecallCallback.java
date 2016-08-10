package com.qpidnetwork.request;

public interface OnConfirmLovecallCallback {
	public void OnConfirmLovecall(boolean isSuccess, String errno, String errmsg, int memberType);
}
