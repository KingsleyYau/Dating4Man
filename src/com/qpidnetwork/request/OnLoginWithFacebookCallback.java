package com.qpidnetwork.request;

import com.qpidnetwork.request.item.LoginErrorItem;
import com.qpidnetwork.request.item.LoginFacebookItem;

public interface OnLoginWithFacebookCallback {
	public void OnLoginWithFacebook(boolean isSuccess, String errno, String errmsg, 
			LoginFacebookItem item, LoginErrorItem errItem);
}
