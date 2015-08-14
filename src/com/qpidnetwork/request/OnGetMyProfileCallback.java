package com.qpidnetwork.request;

import com.qpidnetwork.request.item.ProfileItem;

public interface OnGetMyProfileCallback {
	public void OnGetMyProfile(boolean isSuccess, String errno, String errmsg, ProfileItem item);
}
