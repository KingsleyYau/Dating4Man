package com.qpidnetwork.request;

import com.qpidnetwork.request.item.VSPlayVideoItem;

public interface OnVSPlayVideoCallback {
	public void OnVSPlayVideo(boolean isSuccess, String errno, String errmsg, int memberType, VSPlayVideoItem item);
}
