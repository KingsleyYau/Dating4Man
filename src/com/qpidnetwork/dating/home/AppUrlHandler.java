package com.qpidnetwork.dating.home;

import android.content.Context;
import android.content.Intent;

import com.qpidnetwork.dating.WebViewActivity;
import com.qpidnetwork.dating.admirer.AdmirersListActivity;
import com.qpidnetwork.dating.authorization.LoginManager;
import com.qpidnetwork.dating.contacts.ContactsListActivity;
import com.qpidnetwork.dating.emf.EMFListActivity;
import com.qpidnetwork.dating.lovecall.LoveCallListActivity;
import com.qpidnetwork.dating.quickmatch.QuickMatchActivity;
import com.qpidnetwork.dating.setting.SettingActivity;
import com.qpidnetwork.manager.WebSiteManager;

/**
 * app内部链接处理器
 * @author Samson Fan
 *
 */
public class AppUrlHandler {
	static public final String quickMatchUrl = "qpidnetwork://app/quickmatch";
	static public final String emfUrl = "qpidnetwork://app/emf";
	static public final String loveCallUrl = "qpidnetwork://app/lovecall";
	static public final String myAdmirerUrl = "qpidnetwork://app/admirer";
	static public final String myContactUrl = "qpidnetwork://app/contact";
	static public final String settingUrl = "qpidnetwork://app/setting";
	static public final String helpsUrl = "qpidnetwork://app/helps";
	static private Context mContext = null;
	
	/**
	 * 内部链接处理函数
	 * @param url	内部链接
	 */
	static public void AppUrlHandle(Context context, String url)
	{
		mContext = context;
		
		if (!NeedLogin(url))
		{
			// 不用登录模块，直接进入界面
			AppUrlHandleProc(url);
		}
		else 
		{
			// 需要登录
			if (LoginManager.getInstance().CheckLogin(mContext, true, url))
			{
				// 已经登录，直接进入界面
				AppUrlHandleProc(url);
			}
			else {
				// 未登录，已经弹出登录界面，不用处理
			}
		}
	}

	/**
	 * 判断是否需要登录
	 * @param url	链接
	 * @return
	 */
	static private boolean NeedLogin(String url)
	{
		boolean result = false;
		if (null != url && !url.isEmpty()) 
		{
			if (url.compareTo(emfUrl) == 0
				|| url.compareTo(loveCallUrl) == 0
				|| url.compareTo(myAdmirerUrl) == 0
				|| url.compareTo(myContactUrl) == 0) 
			{
				result = true;
			}
		}
		return result;
	}
	
	/**
	 * 内部链接跳转函数
	 * @param context
	 * @param url		内部链接
	 */
	static public void AppUrlHandleProc(String url)
	{
		if (null != mContext
			&& (null != url && !url.isEmpty())) 
		{
			if (url.compareTo(quickMatchUrl) == 0) {
				// 跳转至Quick Match
				Intent intent = new Intent();
				intent.setClass(mContext, QuickMatchActivity.class);
				mContext.startActivity(intent);
			}
			else if (url.compareTo(emfUrl) == 0) {
				// 跳转到EMF
				Intent intent = new Intent();
				intent.setClass(mContext, EMFListActivity.class);
				mContext.startActivity(intent);
			}
			else if (url.compareTo(loveCallUrl) == 0) {
				// 跳转至LoveCall
				Intent intent = new Intent();
				intent.setClass(mContext, LoveCallListActivity.class);
				mContext.startActivity(intent);
			}
			else if (url.compareTo(myAdmirerUrl) == 0) {
				// 跳转至My Admirer
				Intent intent = new Intent();
				intent.setClass(mContext, AdmirersListActivity.class);
				mContext.startActivity(intent);
			}
			else if (url.compareTo(myContactUrl) == 0) {
				// 跳转至My Contact
				Intent intent = new Intent();
				intent.setClass(mContext, ContactsListActivity.class);
				mContext.startActivity(intent);
			}
			else if (url.compareTo(settingUrl) == 0) {
				// 跳转至Setting
				Intent intent = new Intent();
				intent.setClass(mContext, SettingActivity.class);
				mContext.startActivity(intent);
			}
			else if (url.compareTo(helpsUrl) == 0) {
				// 跳转至Help
				String helpUrl = WebSiteManager.newInstance(mContext).GetWebSite().getHelpLink();
				Intent intent = WebViewActivity.getIntent(mContext, helpUrl);
				intent.putExtra(WebViewActivity.WEB_TITLE, "Help");
				mContext.startActivity(intent);
			}
		}
	}
}
