package com.qpidnetwork.dating;

import com.qpidnetwork.framework.util.Log;
import com.qpidnetwork.framework.util.StringUtil;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * 用于接收app安装广播
 * @author Samson Fan
 *
 */
public class InstallReferrerReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		// 获取广播参数字符串
		String referrer = intent.getStringExtra("referrer");
		
		// 处理广播参数字符串
		if (!StringUtil.isEmpty(referrer)) {
			Log.e("InstallReferrerReceiver", "referrer:%s", referrer);
		}
		else {
			Log.e("InstallReferrerReceiver", "referrer is empty");
		}
	}
}
