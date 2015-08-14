package com.qpidnetwork.request;

import com.qpidnetwork.request.item.EMFAdmirerListItem;

public interface OnEMFAdmirerListCallback {
	public void OnEMFAdmirerList(boolean isSuccess, String errno, String errmsg, int pageIndex, int pageSize
			, int dataCount, EMFAdmirerListItem[] listArray);
}
