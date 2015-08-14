package com.qpidnetwork.request;

import com.qpidnetwork.request.item.OtherEmotionConfigItem;

public interface OnOtherEmotionConfigCallback {
	public void OnOtherEmotionConfig(boolean isSuccess, String errno, String errmsg, OtherEmotionConfigItem item);
}
