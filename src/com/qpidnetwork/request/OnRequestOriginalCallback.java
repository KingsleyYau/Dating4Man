package com.qpidnetwork.request;

public interface OnRequestOriginalCallback {
	public void OnRequestData(boolean isSuccess, String errno, String errmsg, byte[] data);
}
