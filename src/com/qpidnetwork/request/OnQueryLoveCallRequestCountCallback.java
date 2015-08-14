package com.qpidnetwork.request;


public interface OnQueryLoveCallRequestCountCallback {
	public void OnQueryLoveCallRequestCount(boolean isSuccess, String errno, String errmsg, int count);
}
