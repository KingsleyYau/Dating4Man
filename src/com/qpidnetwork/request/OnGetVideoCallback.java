package com.qpidnetwork.request;


/**
 * LiveChat获取私密照片
 */
public interface OnGetVideoCallback {
	public void OnLCGetVideo(long requestId, boolean isSuccess, String errno, String errmsg, String url);
}
