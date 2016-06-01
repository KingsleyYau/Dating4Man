package com.qpidnetwork.request;

import com.qpidnetwork.request.item.ThemeItem;

public interface OnGetThemeDetailCallback {
	public void OnGetThemeDetail(boolean isSuccess, String errno, String errmsg, ThemeItem[] themeList);
}
