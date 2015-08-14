package com.qpidnetwork.request;

import com.qpidnetwork.request.item.LadyDetail;

public interface OnQueryLadyDetailCallback {
	public void OnQueryLadyDetail(boolean isSuccess, String errno, String errmsg, LadyDetail item);
}
