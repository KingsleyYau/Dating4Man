package com.qpidnetwork.request;

import com.qpidnetwork.request.item.VSWatchedVideoListItem;

public interface OnVSWatchedVideoListCallback {
	public void OnVSWatchedVideoList(boolean isSuccess, String errno, String errmsg, int pageIndex, int pageSize
			, int dataCount, VSWatchedVideoListItem[] listArray);
}
