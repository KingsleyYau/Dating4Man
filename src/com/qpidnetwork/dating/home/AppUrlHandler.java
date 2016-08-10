package com.qpidnetwork.dating.home;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import com.qpidnetwork.dating.WebViewActivity;
import com.qpidnetwork.dating.admirer.AdmirersListActivity;
import com.qpidnetwork.dating.authorization.LoginManager;
import com.qpidnetwork.dating.contacts.ContactsListActivity;
import com.qpidnetwork.dating.credit.BuyCreditActivity;
import com.qpidnetwork.dating.emf.EMFListActivity;
import com.qpidnetwork.dating.emf.MailEditActivity;
import com.qpidnetwork.dating.lady.LadyDetailActivity;
import com.qpidnetwork.dating.livechat.ChatActivity;
import com.qpidnetwork.dating.livechat.invite.LivechatInviteListActivity;
import com.qpidnetwork.dating.lovecall.LoveCallListActivity;
import com.qpidnetwork.dating.profile.MyProfileActivity;
import com.qpidnetwork.dating.quickmatch.QuickMatchActivity;
import com.qpidnetwork.dating.setting.SettingActivity;
import com.qpidnetwork.manager.WebSiteManager;
import com.qpidnetwork.request.RequestJniEMF.ReplyType;

/**
 * app内部链接处理器
 * 
 * @author Samson Fan
 * 
 */
public class AppUrlHandler {
	public static final String base_url = "qpidnetwork://app/open?module=";
	static public final String quickMatchUrl = base_url + "quickmatch";// "qpidnetwork://app/quickmatch";
	static public final String emfUrl = base_url + "emf";// "qpidnetwork://app/emf";
	static public final String loveCallUrl = base_url + "lovecall";// "qpidnetwork://app/lovecall";
	static public final String myAdmirerUrl = base_url + "admirer";// "qpidnetwork://app/admirer";
	static public final String myContactUrl = base_url + "contact";// "qpidnetwork://app/contact";
	static public final String settingUrl = base_url + "setting";// "qpidnetwork://app/setting";
	static public final String helpsUrl = base_url + "helps";// "qpidnetwork://app/helps";
	static public final String overViewUrl = base_url + "overview";// "qpidnetwork://app/overview";
	static public final String chatListUrl = base_url + "chatlist";// "qpidnetwork://app/chatlist";
	static public final String buyCreditUrl = base_url + "buycredit";// "qpidnetwork://app/buycredit";
	static public final String myProfileUrl = base_url + "myprofile";// "qpidnetwork://app/myprofile";
	static public final String chatInviteUrl = base_url + "chatinvite";// "qpidnetwork://app/chatinvite";
	static public final String ladyDetailUrl = base_url + "ladydetail";// "qpidnetwork://app/ladydetail";
	static public final String chatLadyUrl = base_url + "chatlady";// "qpidnetwork://app/chatlady";
	static public final String sendemfUrl = base_url + "sendemf";// "qpidnetwork://app/sendemf";
	static private Context mContext = null;

	/**
	 * 链接打开App指定模块
	 * 
	 * @param context
	 * @param module
	 */
	public static void WebLinkOpenModule(Context context, String module) {
		if (!TextUtils.isEmpty(module)) {
			AppUrlHandle(context, base_url + module);
		}
	}
	
	/***
	 * 根据已给模块名及参数创建跳转Url
	 * @return
	 */
	public static String CreateLinkUrl(String moduleName, String ladyID, String source){
		String url = "";
		if(!TextUtils.isEmpty(moduleName)){
			url += base_url;
			url += moduleName;
			if(!TextUtils.isEmpty(ladyID)){
				url += "&";
				url += "ladyid=";
				url += ladyID;
			}
			if(!TextUtils.isEmpty(source)){
				url += "&";
				url += "source=";
				url += source;
			}
		}
		return url;
	}

	/**
	 * 内部链接处理函数
	 * 
	 * @param url
	 *            内部链接
	 */
	static public void AppUrlHandle(Context context, String url) {
		mContext = context;

		if (!NeedLogin(url)) {
			// 不用登录模块，直接进入界面
			AppUrlHandleProc(url);
		} else {
			// 需要登录
			if (LoginManager.getInstance().CheckLogin(mContext, true, url)) {
				// 已经登录，直接进入界面
				AppUrlHandleProc(url);
			} else {
				// 未登录，已经弹出登录界面，不用处理
			}
		}
	}

	/**
	 * 判断是否需要登录
	 * 
	 * @param url
	 *            链接
	 * @return
	 */
	static private boolean NeedLogin(String url) {
		boolean result = false;
		if (null != url && !url.isEmpty()) {
			if (url.compareTo(emfUrl) == 0 || url.compareTo(loveCallUrl) == 0
					|| url.compareTo(myAdmirerUrl) == 0
					|| url.compareTo(myContactUrl) == 0
					|| url.compareTo(buyCreditUrl) == 0
					|| url.compareTo(myProfileUrl) == 0
					|| url.contains(chatInviteUrl)
					|| url.contains(chatLadyUrl) 
					|| url.contains(sendemfUrl)) {
				result = true;
			}
		}
		return result;
	}

	/**
	 * 内部链接跳转函数
	 * 
	 * @param context
	 * @param url
	 *            内部链接
	 */
	static public void AppUrlHandleProc(String url) {
		if (null != mContext && (null != url && !url.isEmpty())) {
			if (url.compareTo(quickMatchUrl) == 0) {
				// 跳转至Quick Match
				Intent intent = new Intent();
				intent.setClass(mContext, QuickMatchActivity.class);
				mContext.startActivity(intent);
			} else if (url.compareTo(emfUrl) == 0) {
				// 跳转到EMF
				Intent intent = new Intent();
				intent.setClass(mContext, EMFListActivity.class);
				mContext.startActivity(intent);
			} else if (url.compareTo(loveCallUrl) == 0) {
				// 跳转至LoveCall
				Intent intent = new Intent();
				intent.setClass(mContext, LoveCallListActivity.class);
				mContext.startActivity(intent);
			} else if (url.compareTo(myAdmirerUrl) == 0) {
				// 跳转至My Admirer
				Intent intent = new Intent();
				intent.setClass(mContext, AdmirersListActivity.class);
				mContext.startActivity(intent);
			} else if (url.compareTo(myContactUrl) == 0) {
				// 跳转至My Contact
				Intent intent = new Intent();
				intent.setClass(mContext, ContactsListActivity.class);
				mContext.startActivity(intent);
			} else if (url.compareTo(settingUrl) == 0) {
				// 跳转至Setting
				Intent intent = new Intent();
				intent.setClass(mContext, SettingActivity.class);
				mContext.startActivity(intent);
			} else if (url.compareTo(helpsUrl) == 0) {
				// 跳转至Help
				String helpUrl = WebSiteManager.getInstance().GetWebSite()
						.getHelpLink();
				Intent intent = WebViewActivity.getIntent(mContext, helpUrl);
				intent.putExtra(WebViewActivity.WEB_TITLE, "Help");
				mContext.startActivity(intent);
			} else if (url.compareTo(overViewUrl) == 0) {
				// 打开Home的左侧导航界面
				Intent intent = new Intent();
				intent.setClass(mContext, HomeActivity.class);
				intent.putExtra(HomeActivity.OPEN_LEFT_MENU, true);
				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
						| Intent.FLAG_ACTIVITY_NEW_TASK);
				mContext.startActivity(intent);

			} else if (url.compareTo(chatListUrl) == 0) {
				// 打开Home的右侧chat列表界面
				Intent intent = new Intent();
				intent.setClass(mContext, HomeActivity.class);
				intent.putExtra(HomeActivity.OPEN_RIGHT_MENU, true);
				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
						| Intent.FLAG_ACTIVITY_NEW_TASK);
				mContext.startActivity(intent);

			} else if (url.compareTo(buyCreditUrl) == 0) {
				// 进入购买信息点界面
				Intent intent = new Intent(mContext, BuyCreditActivity.class);
				mContext.startActivity(intent);

			} else if (url.compareTo(myProfileUrl) == 0) {
				// 进入男士资料界面
				Intent intent = new Intent(mContext, MyProfileActivity.class);
				mContext.startActivity(intent);

			} else if (url.compareTo(chatInviteUrl) == 0) {
				// 进入LiveChat邀请列表界面
				Intent intent = new Intent(mContext,LivechatInviteListActivity.class);
				mContext.startActivity(intent);

			} else if (url.contains(ladyDetailUrl)) {
				// 打开女士资料界面（必需附带"ladyid"参数来指定女士）
				String womanId = getSplitWomanId(url);
				if (womanId != null) {
					LadyDetailActivity.launchLadyDetailActivity(mContext,womanId, true);
				}
			} else if (url.contains(chatLadyUrl)) {
				// 打开与指定女士的LiveChat聊天界面（必需附带"ladyid"参数来指定女士）
				String womanId = getSplitWomanId(url);
				if (womanId != null) {
					ChatActivity.launchChatActivity(mContext, womanId, null, null);
				}

			} else if (url.contains(sendemfUrl)) {
				// 打开与指定女士的写信界面（必需附带"ladyid"参数来指定女士）
				String womanId = getSplitWomanId(url);
				if(womanId != null){
					MailEditActivity.launchMailEditActivity(mContext, womanId, ReplyType.DEFAULT, "", "");
				}
			}

		}
	}

	/**
	 * 根据url解出womanid参数
	 * 
	 * @param url
	 * @return womanId
	 */
	private static String getSplitWomanId(String url) {
		String tag = "ladyid=";//参数名
		int index = url.indexOf(tag);
		String[] temp = null;
		String womanId = null;
		if (index != -1) {
			String subStr = url.substring(url.indexOf(tag) + tag.length(),url.length());
			temp = subStr.split("&");
			if (temp != null && temp.length > 0) {
				womanId = temp[0];
			}
		}
		return womanId;
	}
}
