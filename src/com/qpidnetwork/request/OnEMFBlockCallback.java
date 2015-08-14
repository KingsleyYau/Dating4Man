package com.qpidnetwork.request;

public interface OnEMFBlockCallback {
	public void OnEMFBlock(boolean isSuccess, String errno, String errmsg);
}
