package com.qpidnetwork.request;


/**
 * LiveChat开聊自动买点
 */
public interface OnLCChatRechargeCallback {
	public void OnLCChatRecharge(long requestId, boolean isSuccess, String errno, String errmsg, double credits);
}
