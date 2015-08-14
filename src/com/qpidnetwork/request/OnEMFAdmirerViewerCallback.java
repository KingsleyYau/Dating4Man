package com.qpidnetwork.request;

import com.qpidnetwork.request.item.EMFAdmirerViewerItem;

public interface OnEMFAdmirerViewerCallback {
	public void OnEMFAdmirerViewer(boolean isSuccess, String errno, String errmsg, EMFAdmirerViewerItem item);
}
