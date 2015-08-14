package com.qpidnetwork.request;

public interface OnEMFUploadAttachCallback {
	public void OnEMFUploadAttach(boolean isSuccess, String errno, String errmsg, String attachId);
}
