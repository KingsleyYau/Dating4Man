package com.qpidnetwork.request;

public interface OnUpdateMyProfileCallback {
	public void OnUpdateMyProfile(boolean isSuccess, String errno, String errmsg, boolean rsModified);
}
