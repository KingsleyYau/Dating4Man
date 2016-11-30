package com.qpidnetwork.dating.home;

import java.util.HashMap;

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
	static public final String quickMatchModuleName = "quickmatch";// "qpidnetwork://app/quickmatch";
	static public final String emfModuleName = "emf";// "qpidnetwork://app/emf";
	static public final String loveCallModuleName = "lovecall";// "qpidnetwork://app/lovecall";
	static public final String myAdmirerModuleName = "admirer";// "qpidnetwork://app/admirer";
	static public final String myContactModuleName = "contact";// "qpidnetwork://app/contact";
	static public final String settingModuleName = "setting";// "qpidnetwork://app/setting";
	static public final String helpsModuleName = "helps";// "qpidnetwork://app/helps";
	static public final String overViewModuleName = "overview";// "qpidnetwork://app/overview";
	static public final String chatListModuleName = "chatlist";// "qpidnetwork://app/chatlist";
	static public final String buyCreditModuleName = "buycredit";// "qpidnetwork://app/buycredit";
	static public final String myProfileModuleName = "myprofile";// "qpidnetwork://app/myprofile";
	static public final String chatInviteModuleName = "chatinvite";// "qpidnetwork://app/chatinvite";
	static public final String ladyDetailModuleName = "ladydetail";// "qpidnetwork://app/ladydetail";
	static public final String chatLadyModuleName = "chatlady";// "qpidnetwork://app/chatlady";
	static public final String sendemfModuleName = "sendemf";// "qpidnetwork://app/sendemf";
	static public final String MODULE_KEY = "module";
	static public final String LADY_ID_KEY = "ladyid";
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
	 * Url 是否符合打开指定模块规则
	 * @return
	 */
	public static boolean isCanAppUrlHandler(String url){
		boolean isCanHandler = false;
		if(!TextUtils.isEmpty(url)
				&& url.contains(base_url)){
			isCanHandler = true;
		}
		return isCanHandler;
	}

	/**
	 * 内部链接处理函数
	 * 
	 * @param url
	 *            内部链接
	 */
	static public void AppUrlHandle(Context context, String url) {
		mContext = context;
		
		HashMap<String, String> keyValuesMap = parseUrlKeyValue(url); 
		if (!NeedLogin(keyValuesMap)) {
			// 不用登录模块，直接进入界面
			AppUrlHandleProc(keyValuesMap);
		} else {
			// 需要登录
			if (LoginManager.getInstance().CheckLogin(mContext, true, url)) {
				// 已经登录，直接进入界面
				AppUrlHandleProc(keyValuesMap);
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
	static private boolean NeedLogin(HashMap<String, String> keyValueMap) {
		boolean result = false;
		if (null != keyValueMap && keyValueMap.containsKey(MODULE_KEY)) {
			String moduleName = keyValueMap.get(MODULE_KEY);
			if(!TextUtils.isEmpty(moduleName)
					&& (moduleName.equals(emfModuleName) 
							|| moduleName.equals(loveCallModuleName) 
							|| moduleName.equals(myAdmirerModuleName)
							|| moduleName.equals(myContactModuleName)
							|| moduleName.equals(buyCreditModuleName)
							|| moduleName.equals(myProfileModuleName) 
							|| moduleName.equals(chatInviteModuleName)
							|| moduleName.equals(chatLadyModuleName)
							|| moduleName.equals(sendemfModuleName))){
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
	static public void AppUrlHandleProc(HashMap<String, String> keyValueMap) {
		if (null != mContext && (null != keyValueMap && keyValueMap.containsKey(MODULE_KEY))) {
			String moduleName = keyValueMap.get(MODULE_KEY);
			if(!TextUtils.isEmpty(moduleName)){
				if (moduleName.equals(quickMatchModuleName)) {
					// 跳转至Quick Match
					Intent intent = new Intent();
					intent.setClass(mContext, QuickMatchActivity.class);
					mContext.startActivity(intent);
				} else if (moduleName.equals(emfModuleName)) {
					// 跳转到EMF
					Intent intent = new Intent();
					intent.setClass(mContext, EMFListActivity.class);
					mContext.startActivity(intent);
				} else if (moduleName.equals(loveCallModuleName)) {
					// 跳转至LoveCall
					Intent intent = new Intent();
					intent.setClass(mContext, LoveCallListActivity.class);
					mContext.startActivity(intent);
				} else if (moduleName.equals(myAdmirerModuleName)) {
					// 跳转至My Admirer
					Intent intent = new Intent();
					intent.setClass(mContext, AdmirersListActivity.class);
					mContext.startActivity(intent);
				} else if (moduleName.equals(myContactModuleName)) {
					// 跳转至My Contact
					Intent intent = new Intent();
					intent.setClass(mContext, ContactsListActivity.class);
					mContext.startActivity(intent);
				} else if (moduleName.equals(settingModuleName)) {
					// 跳转至Setting
					Intent intent = new Intent();
					intent.setClass(mContext, SettingActivity.class);
					mContext.startActivity(intent);
				} else if (moduleName.equals(helpsModuleName)) {
					// 跳转至Help
					String helpUrl = WebSiteManager.getInstance().GetWebSite()
							.getHelpLink();
					Intent intent = WebViewActivity.getIntent(mContext, helpUrl);
					intent.putExtra(WebViewActivity.WEB_TITLE, "Help");
					mContext.startActivity(intent);
				} else if (moduleName.equals(overViewModuleName)) {
					// 打开Home的左侧导航界面
					Intent intent = new Intent();
					intent.setClass(mContext, HomeActivity.class);
					intent.putExtra(HomeActivity.OPEN_LEFT_MENU, true);
					intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
							| Intent.FLAG_ACTIVITY_NEW_TASK);
					mContext.startActivity(intent);
	
				} else if (moduleName.equals(chatListModuleName)) {
					// 打开Home的右侧chat列表界面
					Intent intent = new Intent();
					intent.setClass(mContext, HomeActivity.class);
					intent.putExtra(HomeActivity.OPEN_RIGHT_MENU, true);
					intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
							| Intent.FLAG_ACTIVITY_NEW_TASK);
					mContext.startActivity(intent);
	
				} else if (moduleName.equals(buyCreditModuleName)) {
					// 进入购买信息点界面
					Intent intent = new Intent(mContext, BuyCreditActivity.class);
					mContext.startActivity(intent);
	
				} else if (moduleName.equals(myProfileModuleName)) {
					// 进入男士资料界面
					Intent intent = new Intent(mContext, MyProfileActivity.class);
					mContext.startActivity(intent);
	
				} else if (moduleName.equals(chatInviteModuleName)) {
					// 进入LiveChat邀请列表界面
					Intent intent = new Intent(mContext,LivechatInviteListActivity.class);
					mContext.startActivity(intent);
	
				} else if (moduleName.equals(ladyDetailModuleName)) {
					// 打开女士资料界面（必需附带"ladyid"参数来指定女士）
					String womanId = keyValueMap.get(LADY_ID_KEY);
					if (womanId != null) {
						LadyDetailActivity.launchLadyDetailActivity(mContext,womanId, true);
					}
				} else if (moduleName.equals(chatLadyModuleName)) {
					// 打开与指定女士的LiveChat聊天界面（必需附带"ladyid"参数来指定女士）
					String womanId = keyValueMap.get(LADY_ID_KEY);
					if (womanId != null) {
						ChatActivity.launchChatActivity(mContext, womanId, null, null);
					}
	
				} else if (moduleName.equals(sendemfModuleName)) {
					// 打开与指定女士的写信界面（必需附带"ladyid"参数来指定女士）
					String womanId = keyValueMap.get(LADY_ID_KEY);
					if(womanId != null){
						MailEditActivity.launchMailEditActivity(mContext, womanId, ReplyType.DEFAULT, "", "");
					}
				}
			}

		}
	}
	
	/**
	 * 
	 * @param url
	 */
	static public void AppUrlHandleProc(String url){
		HashMap<String, String> keyValuesMap = parseUrlKeyValue(url);
		AppUrlHandleProc(keyValuesMap);
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
	
	/**
	 * 解析Url中参数
	 * @param url
	 * @return
	 */
	private static HashMap<String, String> parseUrlKeyValue(String url){
		HashMap<String, String> argMap = new HashMap<String, String>();
		if(!TextUtils.isEmpty(url)){
			if(url.contains("?")){
				String[] result = url.split("\\?");
				if(result != null && result.length > 1){
					String[] params = result[1].split("&");
					if(params != null){
						for(String param : params){
							String[] keyValue = param.split("=");
							if(keyValue != null && keyValue.length > 1){
								argMap.put(keyValue[0], keyValue[1]);
							}
						}
					}
				}
			}
		}
		return argMap;
	}
}
