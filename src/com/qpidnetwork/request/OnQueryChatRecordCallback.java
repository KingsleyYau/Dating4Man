package com.qpidnetwork.request;

import com.qpidnetwork.request.item.Record;

public interface OnQueryChatRecordCallback {
	public void OnQueryChatRecord(boolean isSuccess, String errno, String errmsg, int dbTime, Record[] recordList, String inviteId);
}
