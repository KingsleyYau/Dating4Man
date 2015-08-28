package com.qpidnetwork.dating;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.qpidnetwork.dating.analysis.AdAnakysisManager;
import com.qpidnetwork.dating.analysis.AnalysisItem;
import com.qpidnetwork.framework.util.Log;
import com.qpidnetwork.framework.util.StringUtil;

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
		
		AnalysisItem item = new AnalysisItem();
		// 处理广播参数字符串
		if (!StringUtil.isEmpty(referrer)) {
			item.utm_referrer = referrer;
			Log.e("InstallReferrerReceiver", "referrer:%s", referrer);
		}
		else {
			Log.e("InstallReferrerReceiver", "referrer is empty");
		}
		item.isSummit = false;
		item.installTime = (int)(System.currentTimeMillis()/1000);
		
		AdAnakysisManager.getInstance().setAnalysisItem(item);
		AdAnakysisManager.getInstance().summitUtmReference(null);
	}
}
