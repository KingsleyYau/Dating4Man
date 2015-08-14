package com.qpidnetwork.request;

import com.qpidnetwork.request.item.EMFMsgTotalItem;

public interface OnEMFMsgTotalCallback {
	public void OnEMFMsgTotal(boolean isSuccess, String errno, String errmsg, EMFMsgTotalItem item);
}
