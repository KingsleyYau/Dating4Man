package com.qpidnetwork.request;

import com.qpidnetwork.request.item.Lady;

public interface OnQueryLadyListCallback {
	public void OnQueryLadyList(boolean isSuccess, String errno, String errmsg, Lady[] ladyList, int totalCount);
}
