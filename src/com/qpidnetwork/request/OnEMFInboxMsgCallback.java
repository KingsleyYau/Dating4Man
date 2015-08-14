package com.qpidnetwork.request;

import com.qpidnetwork.request.item.EMFInboxMsgItem;

public interface OnEMFInboxMsgCallback {
	public void OnEMFInboxMsg(boolean isSuccess, String errno, String errmsg, EMFInboxMsgItem item);
}
