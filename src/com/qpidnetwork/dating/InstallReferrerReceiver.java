package com.qpidnetwork.dating;

import java.net.URLDecoder;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import com.qpidnetwork.dating.analysis.AdAnakysisManager;
import com.qpidnetwork.dating.analysis.AnalysisItem;
import com.qpidnetwork.framework.util.Log;
import com.qpidnetwork.framework.util.StringUtil;
import com.qpidnetwork.manager.WebSiteManager;
import com.qpidnetwork.manager.WebSiteManager.WebSiteType;

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
			try {
				item.utm_referrer = URLDecoder.decode(referrer, "US-ASCII");
				
				int webSiteId = getDefaultWiteSetting(item.utm_referrer);
				Log.e("InstallReferrerReceiver", "webSiteId: " + webSiteId);
				if(webSiteId >= 0){
					WebSiteType siteType = null;
					switch (webSiteId) {
					case 0:
						siteType = WebSiteType.ChnLove;
						break;
					case 1:
						siteType = WebSiteType.IDateAsia;
						break;
					case 4:
						siteType = WebSiteType.CharmDate;
						break;
					case 5:
						siteType = WebSiteType.LatamDate;
						break;
					default:
						break;
					}
					if(siteType != null){
						WebSiteManager siteManager = WebSiteManager.newInstance(context);
						if(siteManager.IsDefaultWebSite()){
							siteManager.ChangeWebSite(siteType);
						}
					}
				}
			} catch (Exception e) {
				// TODO: handle exception
			}
			Log.e("InstallReferrerReceiver", "referrer:%s", referrer);
		}
		else {
			Log.e("InstallReferrerReceiver", "referrer is empty");
		}
		item.isSummit = false;
		item.versionCode = QpidApplication.versionCode;
		item.installTime = (int)(System.currentTimeMillis()/1000);
		
		AdAnakysisManager.getInstance().setAnalysisItem(item);
		AdAnakysisManager.getInstance().summitUtmReference(null);
	}
	
	/**
	 * 通过referrer 参数获取默认网站设置
	 * @return
	 */
	private int getDefaultWiteSetting(String referrer){
		int defaultValue = -1;
		if(!TextUtils.isEmpty(referrer)){
			String[] table = referrer.split("&");
			for(int i=0; i<table.length; i++){
				if(table[i].contains("qpidsiteid")){
					String[] qpidSite = table[i].split("=");
					if((qpidSite.length > 1)){
						try{
							defaultValue = Integer.valueOf(qpidSite[1]);
						}catch(NumberFormatException e){
							e.printStackTrace();
						}
					}
				}
			}
		}
		return defaultValue;
	}
}
