package com.qpidnetwork.request;

import com.qpidnetwork.request.item.RecordMutiple;

public interface OnQueryChatRecordMutipleCallback {
	public void OnQueryChatRecordMutiple(boolean isSuccess, String errno, String errmsg, int dbTime, RecordMutiple[] recordMutipleList);
}
