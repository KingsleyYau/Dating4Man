package com.qpidnetwork.request;

import com.qpidnetwork.request.item.LadyCall;

public interface OnQueryLadyCallCallback {
	public void OnQueryLadyCall(boolean isSuccess, String errno, String errmsg, LadyCall item);
}
