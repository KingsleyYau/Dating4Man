package com.qpidnetwork.request;

import com.qpidnetwork.request.item.ThemeConfig;

/**
 * 获取主题相关配置回调
 * @author Hunter
 * @since 2016.4.19
 */
public interface OnGetThemeConfigCallback {
	public void OnGetThemeConfig(boolean isSuccess, String errno, String errmsg, ThemeConfig config);
}
