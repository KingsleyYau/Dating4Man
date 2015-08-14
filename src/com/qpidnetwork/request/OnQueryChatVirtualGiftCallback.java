package com.qpidnetwork.request;

import com.qpidnetwork.request.item.Gift;

public interface OnQueryChatVirtualGiftCallback {
	public void OnQueryChatVirtualGift(boolean isSuccess, String errno, String errmsg, Gift[] list, 
			int totalCount, String path, String version);
}
