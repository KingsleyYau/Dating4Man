package com.qpidnetwork.request;

import com.qpidnetwork.request.item.EMFBlockListItem;

public interface OnEMFBlockListCallback {
	public void OnEMFBlockList(boolean isSuccess, String errno, String errmsg, int pageIndex, int pageSize
			, int dataCount, EMFBlockListItem[] listArray);
}
