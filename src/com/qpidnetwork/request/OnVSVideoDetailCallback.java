package com.qpidnetwork.request;

import com.qpidnetwork.request.item.VSVideoDetailItem;

public interface OnVSVideoDetailCallback {
	public void OnVSVideoDetail(boolean isSuccess, String errno, String errmsg, VSVideoDetailItem[] item);
}
