package com.qpidnetwork.request;

import com.qpidnetwork.request.item.LoveCall;

public interface OnQueryLoveCallListCallback {
	public void OnQueryLoveCallList(boolean isSuccess, String errno, String errmsg, LoveCall[] itemList, int totalCount);
}
