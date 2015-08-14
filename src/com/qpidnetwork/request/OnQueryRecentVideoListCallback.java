package com.qpidnetwork.request;

import com.qpidnetwork.request.item.LCVideoItem;

public interface OnQueryRecentVideoListCallback {
	public void OnQueryRecentVideoList(boolean isSuccess, String errno, String errmsg, LCVideoItem[] itemList);
}
