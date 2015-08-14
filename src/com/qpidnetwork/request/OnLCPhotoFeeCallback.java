package com.qpidnetwork.request;


/**
 * LiveChat付费获取私密照片
 */
public interface OnLCPhotoFeeCallback {
	public void OnLCPhotoFee(long requestId, boolean isSuccess, String errno, String errmsg);
}
