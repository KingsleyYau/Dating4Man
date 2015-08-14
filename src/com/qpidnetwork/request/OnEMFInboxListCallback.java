package com.qpidnetwork.request;

import com.qpidnetwork.request.item.EMFInboxListItem;

public interface OnEMFInboxListCallback {
	public void OnEMFInboxList(boolean isSuccess, String errno, String errmsg, int pageIndex, int pageSize
			, int dataCount, EMFInboxListItem[] listArray);
}
