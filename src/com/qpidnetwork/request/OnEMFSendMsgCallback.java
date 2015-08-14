package com.qpidnetwork.request;

import com.qpidnetwork.request.item.EMFSendMsgErrorItem;
import com.qpidnetwork.request.item.EMFSendMsgItem;

public interface OnEMFSendMsgCallback {
	public void OnEMFSendMsg(boolean isSuccess, String errno, String errmsg, EMFSendMsgItem item, EMFSendMsgErrorItem errItem);
}
