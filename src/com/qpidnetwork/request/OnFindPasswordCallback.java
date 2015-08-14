package com.qpidnetwork.request;

public interface OnFindPasswordCallback {
	public void OnFindPassword(boolean isSuccess, String errno, String errmsg, String tips);
}
