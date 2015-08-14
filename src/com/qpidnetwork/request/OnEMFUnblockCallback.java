package com.qpidnetwork.request;

public interface OnEMFUnblockCallback {
	public void OnEMFUnblock(boolean isSuccess, String errno, String errmsg);
}
