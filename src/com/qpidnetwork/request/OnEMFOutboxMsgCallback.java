package com.qpidnetwork.request;

import com.qpidnetwork.request.item.EMFOutboxMsgItem;

public interface OnEMFOutboxMsgCallback {
	public void OnEMFOutboxMsg(boolean isSuccess, String errno, String errmsg, EMFOutboxMsgItem item);
}
