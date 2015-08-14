package com.qpidnetwork.request;

import com.qpidnetwork.request.item.LadyMatch;

public interface OnQueryLadyMatchCallback {
	public void OnQueryLadyMatch(boolean isSuccess, String errno, String errmsg, LadyMatch item);
}
