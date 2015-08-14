package com.qpidnetwork.request;

import com.qpidnetwork.request.item.EMFOutboxListItem;

public interface OnEMFOutboxListCallback {
	public void OnEMFOutboxList(boolean isSuccess, String errno, String errmsg, int pageIndex, int pageSize
			, int dataCount, EMFOutboxListItem[] listArray);
}
