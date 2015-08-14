package com.qpidnetwork.request;

import com.qpidnetwork.request.item.VSVideoListItem;

public interface OnVSVideoListCallback {
	public void OnVSVideoList(boolean isSuccess, String errno, String errmsg, int pageIndex, int pageSize
			, int dataCount, VSVideoListItem[] listArray);
}
