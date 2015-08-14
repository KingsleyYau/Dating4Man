package com.qpidnetwork.request;

import com.qpidnetwork.request.item.VSSavedVideoListItem;

public interface OnVSSavedVideoListCallback {
	public void OnVSSavedVideoList(boolean isSuccess, String errno, String errmsg, int pageIndex, int pageSize
			, int dataCount, VSSavedVideoListItem[] listArray);
}
