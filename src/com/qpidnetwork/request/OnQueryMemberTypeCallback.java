package com.qpidnetwork.request;


public interface OnQueryMemberTypeCallback {
	public void OnQueryMemberType(boolean isSuccess, String errno, String errmsg, int memberType);
}
