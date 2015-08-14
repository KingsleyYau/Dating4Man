package com.qpidnetwork.request;

import com.qpidnetwork.request.item.LadySignItem;

public interface OnLadySignListCallback {
	public void OnLadySignList(boolean isSuccess, String errno, String errmsg, LadySignItem[] listArray);
}
