package com.qpidnetwork.request;

import com.qpidnetwork.request.item.OtherSynConfigItem;

public interface OnOtherSynConfigCallback {
	public void OnOtherSynConfig(boolean isSuccess, String errno, String errmsg, OtherSynConfigItem item);
}
