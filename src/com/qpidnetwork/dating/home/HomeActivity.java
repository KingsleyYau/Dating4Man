package com.qpidnetwork.dating.home;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import me.tangke.slidemenu.SlideMenu;
import me.tangke.slidemenu.SlideMenu.OnSlideStateChangeListener;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.DrawerLayout.DrawerListener;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.FrameLayout;

import com.google.ads.conversiontracking.AdWordsConversionReporter;
import com.qpidnetwork.dating.QpidApplication;
import com.qpidnetwork.dating.R;
import com.qpidnetwork.dating.R.string;
import com.qpidnetwork.dating.advertisement.AdvertisementManager;
import com.qpidnetwork.dating.analysis.AdAnakysisManager;
import com.qpidnetwork.dating.authorization.LoginManager;
import com.qpidnetwork.dating.authorization.LoginManager.LoginStatus;
import com.qpidnetwork.dating.authorization.LoginManager.OnLoginManagerCallback;
import com.qpidnetwork.dating.authorization.LoginParam;
import com.qpidnetwork.dating.authorization.RegisterActivity;
import com.qpidnetwork.dating.bean.ContactBean;
import com.qpidnetwork.dating.contacts.ContactManager;
import com.qpidnetwork.dating.contacts.OnContactUpdateCallback;
import com.qpidnetwork.dating.contacts.OnNewInviteUpdateCallback;
import com.qpidnetwork.dating.contactus.TicketDetailListActivity;
import com.qpidnetwork.dating.emf.EMFListActivity;
import com.qpidnetwork.dating.emf.EMFNotification;
import com.qpidnetwork.dating.home.HomeContentViewController.HomeContentViewControllerCallback;
import com.qpidnetwork.dating.home.MenuHelper.MenuType;
import com.qpidnetwork.dating.setting.SettingPerfence;
import com.qpidnetwork.dating.setting.SettingPerfence.NotificationItem;
import com.qpidnetwork.dating.setting.SettingPerfence.NotificationSetting;
import com.qpidnetwork.framework.base.BaseFragmentActivity;
import com.qpidnetwork.framework.util.Log;
import com.qpidnetwork.livechat.LCMessageItem;
import com.qpidnetwork.livechat.LCUserItem;
import com.qpidnetwork.livechat.LiveChatManager;
import com.qpidnetwork.livechat.LiveChatManagerMessageListener;
import com.qpidnetwork.livechat.LiveChatManagerOtherListener;
import com.qpidnetwork.livechat.jni.LiveChatClientListener.KickOfflineType;
import com.qpidnetwork.livechat.jni.LiveChatClientListener.LiveChatErrType;
import com.qpidnetwork.livechat.jni.LiveChatClientListener.TalkEmfNoticeType;
import com.qpidnetwork.livechat.jni.LiveChatTalkUserListItem;
import com.qpidnetwork.manager.ConfigManager;
import com.qpidnetwork.manager.ConfigManager.OnConfigManagerCallback;
import com.qpidnetwork.manager.FileCacheManager;
import com.qpidnetwork.manager.WebSiteManager;
import com.qpidnetwork.manager.WebSiteManager.OnChangeWebsiteCallback;
import com.qpidnetwork.manager.WebSiteManager.WebSite;
import com.qpidnetwork.manager.WebSiteManager.WebSiteType;
import com.qpidnetwork.request.OnAppPromotionAdvertCallback;
import com.qpidnetwork.request.OnEMFMsgTotalCallback;
import com.qpidnetwork.request.OnOtherGetCountCallback;
import com.qpidnetwork.request.OnQueryLoveCallRequestCountCallback;
import com.qpidnetwork.request.OnRequestCallback;
import com.qpidnetwork.request.RequestJni;
import com.qpidnetwork.request.RequestJniEMF.SortType;
import com.qpidnetwork.request.RequestJniLoveCall;
import com.qpidnetwork.request.RequestJniOther;
import com.qpidnetwork.request.RequestOperator;
import com.qpidnetwork.request.item.AdMainAdvert.OpenType;
import com.qpidnetwork.request.item.EMFMsgTotalItem;
import com.qpidnetwork.request.item.LoginErrorItem;
import com.qpidnetwork.request.item.LoginItem;
import com.qpidnetwork.request.item.OtherGetCountItem;
import com.qpidnetwork.request.item.OtherSynConfigItem;
import com.qpidnetwork.tool.CrashPerfence;
import com.qpidnetwork.tool.CrashPerfence.CrashParam;
import com.qpidnetwork.view.MaterialDialogAlert;
import com.qpidnetwork.view.NormalWebviewDialog;

@SuppressLint({ "RtlHardcoded", "UseValueOf" })
public class HomeActivity extends BaseFragmentActivity implements
		OnLoginManagerCallback, OnChangeWebsiteCallback,
		LiveChatManagerOtherListener, LiveChatManagerMessageListener,
		OnSlideStateChangeListener, DrawerListener, OnContactUpdateCallback,
		OnNewInviteUpdateCallback, HomeContentViewControllerCallback,
		OnQueryLoveCallRequestCountCallback, OnOtherGetCountCallback, OnConfigManagerCallback{

	public static final String NEED_RELOGIN_OPERATE = "needLogin";

	public static final String START_EMF_LIST = "start_emf_list";
	public static final String START_LIVECHAT_LIST = "start_livechat_list";
	public static final String START_BROWSER_LINK = "start_brower_link";
	public static final String START_SITE_ID = "start_site_id";
	public static final String OPEN_LEFT_MENU = "open_left_menu";// 打开左侧导航菜单
	public static final String OPEN_RIGHT_MENU = "open_right_menu";// 打开右侧chat列表
	public static final String REFRESH_ONLINE_LADY="refresh_online_lady";//广播刷新女士列表
	public static final String REFRESH_NEWEST_LADY="refresh_newest_lady";//广播刷新女士列表
	public static final String REFRESH_AVAIABLE_CALL_LADY="refresh_avaable_call_lady";//广播刷新女士列表

	// 广告传入参数
	public static final String START_ADVERT = "";
	public static final String OPENTYPE = "openType";
	public static final String URL = "url";

	private long mRequestId = RequestJni.InvalidRequestId;

	// ********************* 界面相关 *********************
	public DrawerLayout mDrawerLayout;
	private SlideMenu slideMenu;
	private MenuFragment mMenuFragment;
	private ContactManager mContactManager;

	/**
	 * 女士列表界面
	 */
	private HomeContentViewController contentViewController;

	/**
	 * 右边最近联系人列表
	 */
	private HomeContactViewController contactViewController;

	/**
	 * 抽屉是否打开
	 */
	private boolean isDrawOpen = false;

	private boolean isForceClose = false;

	private MaterialDialogAlert mCrashDialog = null;
	
	private MaterialDialogAlert mChangeWebSiteDialog = null;

	// ********************* 界面相关 *********************

	// ********************* 接口相关 *********************
	/**
	 * 请求消息
	 */
	private enum RequestFlag {
		REQUEST_MSG_TOTAL_SUCCESS, REQUEST_LIVECHAT_SET_STATUS_SUCCESS, REQUEST_COUNT_SUCCESS, REQUEST_LOVECALL_REQUESTCOUNT_SUCCESS, REQUEST_NEED_UPLOAD_CRASH,
		/**
		 * 强制显示客服消息
		 */
		REQUEST_FORCED_SHOW_TICKET,REQUEST_LOGIN_CALLBACK,REQUEST_APP_EXTENSION_ACTIVITY,REQUEST_UPDATE_APP_EXTENSION_CALLBACK
	}

	/**
	 * 界面消息
	 */
	@SuppressWarnings("unused")
	private class MessageCallbackItem {
		/**
		 * 
		 * @param errno
		 *            接口错误码
		 * @param errmsg
		 *            错误提示
		 */
		public MessageCallbackItem() {
			bShowNotification = false;
		}

		public String errno;
		public String errmsg;
		public EMFMsgTotalItem emfMsgTotalItem;
		public TalkEmfNoticeType noticeType;
		public OtherGetCountItem otherGetCountItem;
		public boolean bShowNotification;
		public Integer lovecallRequestCount;
	}

	// 弹出窗口消息列表
	private List<Integer> mPopMsgList = new LinkedList<Integer>();
	private LoginItem mLoginItem = null;
	
	/* 广播用于activity刷新在线女士列表 */
	private BroadcastReceiver mBroadcastReceiver;
	
	/*推广广告参数，登陆成功和主动触发（AppPromotion接口）是更新*/
	private String mAdOverview = "";

	// ********************* 界面相关 *********************

	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
//		showContacts();
		// 初始化登录状态管理
		LoginManager.getInstance().AddListenner(this);

		// 初始化livechat
		LiveChatManager.getInstance().RegisterOtherListener(this);
		LiveChatManager.getInstance().RegisterMessageListener(this);

		// 初始化站点改变响应
		WebSiteManager.getInstance().AddListenner(this);

		mContactManager = ContactManager.getInstance();
		mContactManager.registerContactUpdate(this);
		mContactManager.registerInviteUpdate(this);

		// 根据消息弹出界面
		StartFromNotification(getIntent());

		this.getWindow().setBackgroundDrawable(
				new ColorDrawable(getResources().getColor(
						WebSiteManager.getInstance().GetWebSite()
								.getSiteColor())));
		//adwords 启动报告
		if(!QpidApplication.isDemo){
			AdWordsConversionReporter.reportWithConversionId(this.getApplicationContext(),"1072471539", "TPsJCNDx1GAQ87uy_wM", "0.00", false);
		}

		// 统计中间页(OnlineLady)
		onAnalyticsPageSelected(1);
		
		initReceiver();
		
		//当用户在登陆状态下推出界面重新启动界面时，由于不执行登陆逻辑，需要客户端主动和后台同步App推广相关
		CheckPromotionActivity();
		
		QpidApplication.isAppOpen = true;//用于Push打开App默认换站还是弹窗控制
	}
	/**
	 * 刷新在线女士列表广播
	 */
	private void initReceiver() {
		mBroadcastReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				// TODO Auto-generated method stub
				String action = intent.getAction();
				if (action.equals(REFRESH_ONLINE_LADY)) {
					contentViewController.refreshOnlineLady();
				}else if (action.equals(REFRESH_NEWEST_LADY)) {
					contentViewController.refreshNewestLady();
				}else if (action.equals(REFRESH_AVAIABLE_CALL_LADY)) {
					contentViewController.refreshAvaiableCallLady();
				}
			}
		};

		IntentFilter filter = new IntentFilter();
		filter.addAction(REFRESH_ONLINE_LADY);
		filter.addAction(REFRESH_NEWEST_LADY);
		filter.addAction(REFRESH_AVAIABLE_CALL_LADY);
		registerReceiver(mBroadcastReceiver, filter);
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		LiveChatManager.getInstance().UnregisterOtherListener(this);
		LiveChatManager.getInstance().UnregisterMessageListener(this);
		WebSiteManager.getInstance().RemoveListenner(this);
		mContactManager.unregisterContactUpdata(this);
		mContactManager.unregisterInviteUpdata(this);
		unregisterReceiver(mBroadcastReceiver);
		QpidApplication.mHomeActicityDestroyTime = System.currentTimeMillis();
		super.onDestroy();
	}

	@Override
	protected void onNewIntent(Intent intent) {
		// TODO Auto-generated method stub
		super.onNewIntent(intent);
		Log.d("EMFLisActivity", "onNewIntent( TaskId : " + getTaskId() + " )");
		StartFromNotification(intent);
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub

		// 自动登录
		LoginManager.getInstance().AutoLogin();

		// 需要刷新未读EMF条数
		MsgTotal(false);

		// 获取LoveCall未处理数
		GetLoveCallRequestCount();

		// 获取男士余额
		GetCount();

		// 获取同步配置
		OnGetConfig();

		// 弹出窗口
		if (!mPopMsgList.isEmpty()) {
			Integer msgWhat = mPopMsgList.remove(0);
			Message msg = Message.obtain();
			msg.what = msgWhat;
			sendUiMessage(msg);
		}

		// 广告跟踪提交
		if (!AdAnakysisManager.getInstance().getAnalysisItem().isSummit) {
			AdAnakysisManager.getInstance().summitUtmReference(null);
		}

		super.onResume();
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();

		mHandler.removeMessages(RequestFlag.REQUEST_MSG_TOTAL_SUCCESS.ordinal());
		mHandler.removeMessages(RequestFlag.REQUEST_LIVECHAT_SET_STATUS_SUCCESS
				.ordinal());
		mHandler.removeMessages(RequestFlag.REQUEST_COUNT_SUCCESS.ordinal());
		mHandler.removeMessages(RequestFlag.REQUEST_LOVECALL_REQUESTCOUNT_SUCCESS
				.ordinal());
		mHandler.removeMessages(RequestFlag.REQUEST_NEED_UPLOAD_CRASH.ordinal());
		mHandler.removeMessages(RequestFlag.REQUEST_FORCED_SHOW_TICKET
				.ordinal());
		mHandler.removeMessages(RequestFlag.REQUEST_LOGIN_CALLBACK
				.ordinal());
		mHandler.removeMessages(RequestFlag.REQUEST_APP_EXTENSION_ACTIVITY
				.ordinal());
		mHandler.removeMessages(RequestFlag.REQUEST_UPDATE_APP_EXTENSION_CALLBACK
				.ordinal());
	}

	public void scrollToLeftEdge() {
		if (contactViewController.getOffset() == 0) {
			contactViewController.setOffset(contactViewController
					.getStaticOffset());
			slideMenu
					.smoothScrollContentTo(0 - contactViewController.getSize().x);
		} else {
			contactViewController.setOffset(0);
			slideMenu
					.smoothScrollContentTo(0 - contactViewController.getSize().x);
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (isForceClose) {
				finish();
				System.exit(9);
			}

			if (isDrawOpen) {
				if (mDrawerLayout != null) {
					mDrawerLayout.closeDrawer(Gravity.LEFT);
				}
				return true;
			}
		}
		return super.onKeyDown(keyCode, event);
	}

	/**
	 * 初始化界面
	 */
	@Override
	public void InitView() {
		// TODO Auto-generated method stub
		setContentView(R.layout.activity_home_main);

		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, Gravity.LEFT);
		slideMenu = (SlideMenu) findViewById(R.id.slideMenu);
		slideMenu.setSecondaryShadowWidth(4.0f * getResources()
				.getDisplayMetrics().density);

		// 创建OnlineLady列表界面
		contentViewController = new HomeContentViewController(this);
		contentViewController.SetCallback(this);

		// 创建联系人列表界面
		contactViewController = new HomeContactViewController(this);

		slideMenu.addView(contactViewController.getView(),
				new SlideMenu.LayoutParams(contactViewController.getSize().x,
						android.view.ViewGroup.LayoutParams.MATCH_PARENT,
						SlideMenu.LayoutParams.ROLE_SECONDARY_MENU));

		slideMenu.addView(contentViewController.getView(),
				new SlideMenu.LayoutParams(
						android.view.ViewGroup.LayoutParams.MATCH_PARENT,
						android.view.ViewGroup.LayoutParams.MATCH_PARENT,
						SlideMenu.LayoutParams.ROLE_CONTENT));

		slideMenu.setSlideMode(SlideMenu.MODE_SLIDE_CONTENT);
		slideMenu.setOnSlideStateChangeListener(this);

		/* 设置抽屉offset */
		FrameLayout left_drawer = (FrameLayout) findViewById(R.id.left_drawer);
		DrawerLayout.LayoutParams params = (DrawerLayout.LayoutParams) left_drawer
				.getLayoutParams();
		params.width = HomeHelper.getMenuWidth(this);

		// 设置主菜单界面
		if (mMenuFragment == null) {
			mMenuFragment = new MenuFragment();
		}
		getSupportFragmentManager().beginTransaction()
				.replace(R.id.left_drawer, mMenuFragment).commit();

		mDrawerLayout.setDrawerListener(this);
	}

	// ********************* 接口相关 *********************

	/**
	 * 刷新未读EMF条数
	 * 
	 * @param bShowNotification
	 *            是否需要显示到消息中心
	 */
	public void MsgTotal(final boolean bShowNotification) {
		if (!LoginManager.getInstance().CheckLogin(mContext, false)) {
			return;
		}

		RequestOperator.getInstance().MsgTotal(SortType.UNREAD,
				new OnEMFMsgTotalCallback() {

					@Override
					public void OnEMFMsgTotal(boolean isSuccess, String errno,
							String errmsg, EMFMsgTotalItem item) {
						// TODO Auto-generated method stub
						if (isSuccess) {
							Message msg = Message.obtain();
							MessageCallbackItem obj = new MessageCallbackItem();
							msg.obj = obj;
							obj.errmsg = errmsg;
							obj.errno = errno;
							obj.bShowNotification = bShowNotification;
							obj.emfMsgTotalItem = item;
							msg.what = RequestFlag.REQUEST_MSG_TOTAL_SUCCESS
									.ordinal();
							sendUiMessage(msg);
						}
					}
				});
	}

	/**
	 * 获取LoveCall未处理数
	 */
	public void GetLoveCallRequestCount() {
		if (!LoginManager.getInstance().CheckLogin(mContext, false)) {
			return;
		}
		RequestOperator.getInstance().QueryLoveCallRequestCount(
				RequestJniLoveCall.SearchType.REQUEST, this);
	}

	@Override
	protected void handleUiMessage(Message msg) {
		// TODO Auto-generated method stub
		super.handleUiMessage(msg);
		MessageCallbackItem obj = (MessageCallbackItem) msg.obj;
		switch (RequestFlag.values()[msg.what]) {
		case REQUEST_MSG_TOTAL_SUCCESS: {
			// 刷新未读EMF条数成功
			mMenuFragment.menuHelper.updateMenuItem(MenuType.MENU_MAIL_BOX,
					obj.emfMsgTotalItem.msgTotal);

			if (obj.bShowNotification) {
				// 显示到消息中心
				NotificationItem ni = SettingPerfence
						.GetNotificationItem(QpidApplication.getContext());
				if(ni.mMailNotification != NotificationSetting.None){
					boolean bSound = false;
					boolean bVibrate = true;
	
					switch (ni.mMailNotification) {
					case SoundWithVibrate: {
						bSound = true;
						bVibrate = true;
					}
						break;
					case Vibrate: {
						bSound = false;
						bVibrate = true;
					}
						break;
					case Silent: {
						bSound = false;
						bVibrate = false;
					}
					default: {
						bSound = false;
						bVibrate = false;
					}
						break;
					}
	
					EMFNotification.newInstance(mContext).ShowNotification(
							com.qpidnetwork.dating.R.drawable.logo_40dp,
							String.format(getString(string.emf_notification_unread,
									obj.emfMsgTotalItem.msgTotal)), bVibrate,
							bSound);
				}
			}
		}
			break;
		case REQUEST_LIVECHAT_SET_STATUS_SUCCESS: {
			// 收到EMF更新通知
			if (obj.noticeType == TalkEmfNoticeType.EMF) {
				// 主界面菜单， 需要刷新未读EMF条数
				MsgTotal(true);
			}
		}
			break;
		case REQUEST_COUNT_SUCCESS: {
			mMenuFragment.tvCredit.setText(String
					.valueOf(obj.otherGetCountItem.money));
			mMenuFragment.menuHelper.updateMenuItem(MenuType.MENU_MY_ADMIRERS,
					obj.otherGetCountItem.admirerUr);
		}
			break;
		case REQUEST_LOVECALL_REQUESTCOUNT_SUCCESS: {
			if (obj.lovecallRequestCount != null) {
				mMenuFragment.menuHelper.updateMenuItem(
						MenuType.MENU_LOVE_CALLS,
						obj.lovecallRequestCount.intValue());
			}
		}
			break;
		case REQUEST_NEED_UPLOAD_CRASH: {
			// 弹出CrashLog上传询问
			CheckCrashLog();
		}
			break;
		case REQUEST_FORCED_SHOW_TICKET: {
			// 弹出显示客户消息
			if (null != mLoginItem && null != mLoginItem.ticketid
					&& !mLoginItem.ticketid.isEmpty()) {
				ShowTicketDetail(mLoginItem.ticketid);
			}
		}
			break;
		case REQUEST_LOGIN_CALLBACK: {
			// 需要刷新未读EMF条数
			MsgTotal(false);

			// 获取LoveCall未处理数
			GetLoveCallRequestCount();

			// 获取男士余额
			GetCount();
			
			//刷新女士列表
			contentViewController.QueryOnlineLadyList(false);
		}
			break;
		case REQUEST_APP_EXTENSION_ACTIVITY: {
			// App 推广等活动弹窗提示
			showAppExtensionDialog();
		}
			break;
		case REQUEST_UPDATE_APP_EXTENSION_CALLBACK: {
			// App主动更新推广成功
			if(isActivityVisible()){
				//在当前页面，直接弹框
				showAppExtensionDialog();
			}else{
				mPopMsgList.add(RequestFlag.REQUEST_APP_EXTENSION_ACTIVITY
						.ordinal());
			}
		}
			break;
		default:
			break;
		}
	}

	public SlideMenu getSlideMenu() {
		return slideMenu;
	}

	/**
	 * 统计男士数据
	 */
	public void GetCount() {
		if (!LoginManager.getInstance().CheckLogin(mContext, false)) {
			return;
		}

		// 此处应有菊花
		RequestOperator.getInstance().GetCount(true, true, true, true, true,
				true, this);
	}

	/**
	 * 获取同步配置
	 */
	public void OnGetConfig() {
		// 获取同步配置
		ConfigManager.getInstance().GetOtherSynConfigItem(this);
	}

	@Override
	public void OnChangeWebsite(WebSite website) {
		// TODO Auto-generated method stub
		if (mDrawerLayout != null) {
			mDrawerLayout.closeDrawer(Gravity.LEFT);
		}

		// 重置左边未读
		mMenuFragment.menuHelper.updateMenuItem(MenuType.MENU_MAIL_BOX, 0);
		mMenuFragment.menuHelper.updateMenuItem(MenuType.MENU_LOVE_CALLS, 0);
		mMenuFragment.menuHelper.updateMenuItem(MenuType.MENU_MY_ADMIRERS, 0);

		// 重新自动登录
		LoginManager.getInstance().Logout(true, false);
		LoginManager.getInstance().AutoLogin();

		contentViewController.OnChangeWebsite(website);

		this.getWindow().setBackgroundDrawable(
				new ColorDrawable(getResources().getColor(
						website.getSiteColor())));

	}

	@Override
	public void OnLogin(LiveChatErrType errType, String errmsg,
			boolean isAutoLogin) {
		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				contactViewController.listenToLoginActivity();
			}

		});
	}

	@Override
	public void OnLogout(LiveChatErrType errType, String errmsg,
			boolean isAutoLogin) {
		// TODO Auto-generated method stub
		if(!isAutoLogin){
			//修改原有刷新机制，只有当被踢注销时才清除，自动重连时不清楚刷新
			runOnUiThread(new Runnable() {
	
				@Override
				public void run() {
					// TODO Auto-generated method stub
					contactViewController.listenToLogoutActivity();
				}
	
			});
		}

	}

	@Override
	public void OnGetTalkList(LiveChatErrType errType, String errmsg) {
		// TODO Auto-generated method stub

	}

	@Override
	public void OnGetHistoryMessage(boolean success, String errno,
			String errmsg, LCUserItem userItem) {
		// TODO Auto-generated method stub

	}

	@Override
	public void OnGetUsersHistoryMessage(boolean success, String errno,
			String errmsg, LCUserItem[] userItems) {
		// TODO Auto-generated method stub

	}

	@Override
	public void OnSetStatus(LiveChatErrType errType, String errmsg) {
		// TODO Auto-generated method stub

	}

	@Override
	public void OnGetUserStatus(LiveChatErrType errType, String errmsg,
			LCUserItem[] userList) {
		// TODO Auto-generated method stub

	}
	
	@Override
	public void OnGetUsersInfo(LiveChatErrType errType, String errmsg,
			LiveChatTalkUserListItem[] itemList) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void OnUpdateStatus(LCUserItem userItem) {
		// TODO Auto-generated method stub

	}

	@Override
	public void OnChangeOnlineStatus(LCUserItem userItem) {
		// TODO Auto-generated method stub

	}

	@Override
	public void OnRecvKickOffline(KickOfflineType kickType) {
		// TODO Auto-generated method stub

	}

	@Override
	public void OnRecvEMFNotice(String fromId, TalkEmfNoticeType noticeType) {
		// TODO Auto-generated method stub
		// 收到EMF更新通知
		Message msg = Message.obtain();
		MessageCallbackItem obj = new MessageCallbackItem();
		msg.what = RequestFlag.REQUEST_LIVECHAT_SET_STATUS_SUCCESS.ordinal();
		msg.obj = obj;
		obj.noticeType = noticeType;
		sendUiMessage(msg);
	}

	@Override
	public void OnSendMessage(LiveChatErrType errType, String errmsg,
			LCMessageItem item) {
		// TODO Auto-generated method stub

	}

	@Override
	public void OnRecvMessage(LCMessageItem item) {
		// TODO Auto-generated method stub
		if (slideMenu.getCurrentState() != SlideMenu.STATE_OPEN_RIGHT) {
			if (contentViewController != null) {
				contentViewController.OnRecvMessage(item);
			}
		}
	}

	@Override
	public void OnRecvWarning(LCMessageItem item) {
		// TODO Auto-generated method stub

	}

	@Override
	public void OnRecvEditMsg(String fromId) {
		// TODO Auto-generated method stub

	}

	@Override
	public void OnRecvSystemMsg(LCMessageItem item) {
		// TODO Auto-generated method stub

	}

	@Override
	public void OnSendMessageListFail(LiveChatErrType errType,
			ArrayList<LCMessageItem> msgList) {
		// TODO Auto-generated method stub

	}

	/**
	 * 从消息中心进入
	 * 
	 * @param intent
	 */
	public void StartFromNotification(Intent intent) {
		Log.d("EMFLisActivity", "StartFromNotification( TaskId : "
				+ getTaskId() + " )");

		// 如果未选择过站点
		if (WebSiteManager.getInstance().GetWebSite() == null) {
			WebSiteManager.getInstance().ChangeWebSite(
					WebSiteType.ChnLove);
		}

		Bundle bundle = intent.getExtras();
		if (bundle != null) {
			if (bundle.containsKey(ContactManager.LIVE_CHAT_KICK_OFF)) {
				// KickOfflineType type =
				// KickOfflineType.values()[bundle.getInt(ContactManager.LIVE_CHAT_KICK_OFF)];
//				Intent loginIntent = new Intent(HomeActivity.this,
//						RegisterActivity.class);
//				startActivity(loginIntent);
			} else if (bundle.containsKey(NEED_RELOGIN_OPERATE)) {
				Intent loginIntent = new Intent(HomeActivity.this,
						RegisterActivity.class);
				startActivity(loginIntent);
			} else if (bundle.containsKey(START_EMF_LIST)
					&& bundle.getBoolean(START_EMF_LIST)) {
				// 收到EMF消息, 跳转emf界面
				if (LoginManager.getInstance().CheckLogin(mContext)) {
					Intent newIntent = new Intent(HomeActivity.this,
							EMFListActivity.class);
					startActivity(newIntent);
				}
			} else if (bundle.containsKey(START_LIVECHAT_LIST)
					&& bundle.getBoolean(START_LIVECHAT_LIST)) {
				// 收到最近联系人的消息（包括邀请消息）, 打开右边界面
				mHandler.postDelayed(new Runnable() {

					@Override
					public void run() {
						// TODO Auto-generated method stub
						slideMenu.open(true, false);
					}
				}, 1000);

				// 清空消息中心
				// LiveChatNotification.newInstance(mContext).Cancel();
			} else if (bundle.containsKey(START_ADVERT)
					&& bundle.getBoolean(START_ADVERT)) {
				// 打开广告
				OpenType openType = OpenType.UNKNOW;
				String url = "";
				if (bundle.containsKey(OPENTYPE)) {
					if (bundle.getInt(OPENTYPE) > 0
							&& bundle.getInt(OPENTYPE) < OpenType.values().length) {
						openType = OpenType.values()[bundle.getInt(OPENTYPE)];
						if (bundle.containsKey(URL)) {
							url = bundle.getString(URL);
							AdvertisementManager.getInstance()
									.parseAdvertisment(this, url, openType);
						}
					}
				}
			} else if (bundle.containsKey(START_BROWSER_LINK)) {
				if(!needChangeWebSite(bundle)){
					String moduleName = bundle.getString(START_BROWSER_LINK);
					if (!TextUtils.isEmpty(moduleName)) {
						// 打开指定模块
						AppUrlHandler.AppUrlHandle(this, moduleName);
					}
				}
			} else if (bundle.containsKey(OPEN_LEFT_MENU)) {//打开左侧菜单
				if (mDrawerLayout != null) {
					mDrawerLayout.openDrawer(Gravity.LEFT);
				}

			} else if (bundle.containsKey(OPEN_RIGHT_MENU)) {//打开右侧菜单
				slideMenu.open(true, true);
			}
		}
	}

	@SuppressLint("UseValueOf")
	@Override
	public void OnLogin(boolean isSuccess, String errno, String errmsg,
			LoginItem item, LoginErrorItem errItem) {
		// TODO Auto-generated method stub
		if (isSuccess) {
			mLoginItem = item;
			// 弹出活动消息（目前用于App推广）
			if(null != mLoginItem 
					&& !TextUtils.isEmpty(mLoginItem.adOverview)){
				mAdOverview = mLoginItem.adOverview; 
				mPopMsgList.add(RequestFlag.REQUEST_APP_EXTENSION_ACTIVITY
						.ordinal());
			}
			
			// 弹出客服消息
			if (null != mLoginItem && null != mLoginItem.ticketid
					&& !mLoginItem.ticketid.isEmpty()) {
				mPopMsgList.add(RequestFlag.REQUEST_FORCED_SHOW_TICKET
						.ordinal());
			}
			// 弹出CrashLog上传询问
			mPopMsgList.add(RequestFlag.REQUEST_NEED_UPLOAD_CRASH.ordinal());
			//自动登陆或者换站登陆成功刷新点数、未读意向信及未读Lovecall数目
			Message msg = Message.obtain();
			msg.what = RequestFlag.REQUEST_LOGIN_CALLBACK.ordinal();
			sendUiMessage(msg);
		}
	}

	@Override
	public void OnLogout(boolean bActive) {
		// TODO Auto-generated method stub

	}

	public void UploadCrashLog() {
		TelephonyManager tm = (TelephonyManager) mContext
				.getSystemService(Context.TELEPHONY_SERVICE);
		mRequestId = RequestJniOther.UploadCrashLog(RequestJni.GetDeviceId(tm),
				FileCacheManager.getInstance().GetCrashInfoPath(),
				FileCacheManager.getInstance().GetTempPath(),
				new OnRequestCallback() {

					@Override
					public void OnRequest(boolean isSuccess, String errno,
							String errmsg) {
						// TODO Auto-generated method stub
						if (isSuccess) {
							FileCacheManager.getInstance().ClearCrashLog();
						}
						mRequestId = RequestJni.InvalidRequestId;
					}
				});
	}

	/**
	 * 判断是否需要弹出CrashLog上传窗口，若是则弹出
	 */
	public void CheckCrashLog() {
		if (!LoginManager.getInstance().CheckLogin(this, false)) {
			// 未登录
			return;
		}

		File file = new File(FileCacheManager.getInstance().GetCrashInfoPath());
		boolean bFlag = false;
		if (file.exists() && file.isDirectory() && file.list() != null
				&& file.list().length > 0) {
			for (File item : file.listFiles()) {
				if (item != null && item.isFile()) {
					// 有文件需要上传
					bFlag = true;
					break;
				}
			}
		}

		if (bFlag) {
			boolean bUploadCrash = false;
			boolean bRemember = false;

			CrashParam param = CrashPerfence.GetCrashParam(mContext);
			if (param != null) {
				bUploadCrash = param.bUploadCrash;
				bRemember = param.bRemember;
			}

			// 登录成功并且不在上传中
			if ((mRequestId == RequestJni.InvalidRequestId)) {
				// 已经记住
				if (bRemember) {
					if (bUploadCrash) {
						// 直接上传
						UploadCrashLog();
					}
				} else {
					if (mCrashDialog == null) {
						mCrashDialog = new MaterialDialogAlert(mContext);
						mCrashDialog.setTitle(getResources().getString(
								R.string.Crash_report_detected));
						mCrashDialog
								.setMessage(getResources()
										.getString(
												R.string.Do_you_want_report_an_incident_to_help_us_for_app_improvement));
						mCrashDialog.setCheckBox(
								getResources().getString(
										R.string.Remember_my_choise), false);

						mCrashDialog.addButton(mCrashDialog.createButton(
								getResources().getString(
										R.string.common_btn_yes),
								new OnClickListener() {

									@Override
									public void onClick(View v) {
										// TODO Auto-generated method stub
										boolean checked = mCrashDialog
												.getCheckBox().getChecked();
										CrashPerfence.SaveCrashParam(mContext,
												new CrashParam(true, checked));
										UploadCrashLog();
									}
								}));

						mCrashDialog.addButton(mCrashDialog.createButton(
								getResources()
										.getString(R.string.common_btn_no),
								new OnClickListener() {

									@Override
									public void onClick(View v) {
										// TODO Auto-generated method stub
										boolean checked = mCrashDialog
												.getCheckBox().getChecked();
										CrashPerfence.SaveCrashParam(mContext,
												new CrashParam(false, checked));
									}

								}));
					}
					mCrashDialog.show();
				}
			}
		}
	}

	/**
	 * 显示客服消息
	 */
	void ShowTicketDetail(final String ticketId) {
		MaterialDialogAlert dialog = new MaterialDialogAlert(mContext);
		dialog.setMessage(getString(R.string.ticket_force_read_tips));
		dialog.addButton(dialog.createButton(
				mContext.getString(R.string.common_btn_read_now),
				new OnClickListener() {

					@Override
					public void onClick(View v) {
						Intent intent = new Intent(mContext,
								TicketDetailListActivity.class);
						intent.putExtra(TicketDetailListActivity.TICKET_ID,
								ticketId);
						startActivity(intent);
					}
				}));
		dialog.show();
	}

	private void onRightSlideMenuOpen() {
		contactViewController.reloadDataIfNull();
		contentViewController.OnOpen();
		mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
		
		// 统计右侧页(Contact)
		onAnalyticsPageSelected(2);
	}

	private void onRightSlideMenuClose() {
		mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
		
		// 统计中间页(OnlineLady)
		onAnalyticsPageSelected(1);
	}

	private void onRightSlideMenuScrolling() {
		mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
	}

	private void onLeftDrawerOpen() {
		// 统计左侧页(Menu)
		onAnalyticsPageSelected(0);
	}

	private void onLeftDrawerClose() {
		// 统计中间页(OnlineLady)
		onAnalyticsPageSelected(1);
	}

	@Override
	public void onSlideStateChange(int slideState) {
		switch (slideState) {
		case SlideMenu.STATE_CLOSE:
			onRightSlideMenuClose();
			break;
		case SlideMenu.STATE_OPEN_LEFT:
		case SlideMenu.STATE_OPEN_RIGHT:
			onRightSlideMenuOpen();
		case SlideMenu.STATE_OPEN_MASK:
		case SlideMenu.STATE_SCROLL:
			onRightSlideMenuScrolling();
			break;
		default:
			mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
		}
	}

	@Override
	public void onSlideOffsetChange(float offsetPercent) {

	}

	@Override
	public void onDrawerClosed(View arg0) {
		isDrawOpen = false;
		onLeftDrawerClose();
	}

	@Override
	public void onDrawerOpened(View arg0) {
		isDrawOpen = true;
		onLeftDrawerOpen();
	}

	@Override
	public void onDrawerSlide(View arg0, float arg1) {

	}

	@Override
	public void onDrawerStateChanged(int arg0) {
		if (arg0 == DrawerLayout.LOCK_MODE_UNLOCKED) {
			if (!isDrawOpen) {
			}
		}
	}

	@Override
	public void onNewInviteUpdate() {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				contactViewController.onNewInviteUpdate();
			}

		});
	}

	@Override
	public void onContactUpdate(final List<ContactBean> contactList) {
		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				contactViewController.listenToContactListUpdate(contactList);
			}

		});
	}

	@Override
	public void OnClickOpenDrawer(View v) {
		// 打开左边菜单
		if (mDrawerLayout != null) {
			mDrawerLayout.openDrawer(Gravity.LEFT);
		}
	}

	@Override
	public void OnClickSearch(boolean bShowSearch) {

	}

	@Override
	public void OnClickContact(View v) {
		// 打开右边菜单
		slideMenu.open(true, true);
	}
	
	@Override
	public void OnListSelected(int index) {
		// 统计中间页的子页(OnlineLady)
		onAnalyticsPageSelected(1, index);
	}

	@Override
	public void OnRequest(String tips) {
		showToastProgressing(tips);
	}

	@Override
	public void OnRequestFinish(boolean bSuccess, String tips) {
		if (tips == null) {
			cancelToastImmediately();
			return;
		}
		if (bSuccess) {
			showToastDone(tips);
		} else {
			showToastFailed(tips);
		}
	}

	@Override
	public void OnQueryLoveCallRequestCount(boolean isSuccess, String errno,
			String errmsg, int count) {
		if (isSuccess) {
			Message msg = Message.obtain();
			MessageCallbackItem obj = new MessageCallbackItem();
			msg.obj = obj;
			obj.errmsg = errmsg;
			obj.errno = errno;
			obj.lovecallRequestCount = new Integer(count);
			msg.what = RequestFlag.REQUEST_LOVECALL_REQUESTCOUNT_SUCCESS
					.ordinal();
			sendUiMessage(msg);
		}
	}

	@Override
	public void OnOtherGetCount(boolean isSuccess, String errno, String errmsg,
			OtherGetCountItem item) {
		// 统计男士数据成功
		if (isSuccess) {
			Message msg = Message.obtain();
			MessageCallbackItem obj = new MessageCallbackItem();
			obj.errno = errno;
			obj.errmsg = errmsg;
			obj.otherGetCountItem = item;
			msg.what = RequestFlag.REQUEST_COUNT_SUCCESS.ordinal();
			msg.obj = obj;
			sendUiMessage(msg);
		}
	}

	@Override
	public void OnGetOtherSynConfigItem(boolean isSuccess, String errno,
			String errmsg, final OtherSynConfigItem item) {
		if (isSuccess) {
			if (item.pub.apkForceUpdate) {
				if (item.pub.apkVerCode > QpidApplication.versionCode) {
					isForceClose = true;
					MaterialDialogAlert dialog = new MaterialDialogAlert(
							mContext);
					dialog.addButton(dialog.createButton(
							mContext.getString(R.string.common_btn_ok),
							new OnClickListener() {
								@Override
								public void onClick(View v) {
									// TODO Auto-generated
									// method stub
									Uri uri = Uri
											.parse(item.pub.storeUrl);
									Intent intent = new Intent();
									intent.setAction(Intent.ACTION_VIEW);
									intent.setData(uri);
									startActivity(intent);
									finish();
									System.exit(9);
								}
							}));
					dialog.setTitle(mContext
							.getString(R.string.upgrade_title));
					dialog.setOnCancelListener(new OnCancelListener() {

						@Override
						public void onCancel(
								DialogInterface dialog) {
							// TODO Auto-generated method stub
							finish();
							System.exit(9);
						}
					});
					dialog.show();

				}
			}
		}		
	}
	
	/**
	 * 检测推广活动
	 */
	private void CheckPromotionActivity(){
		if(LoginManager.getInstance().GetLoginStatus() == LoginStatus.LOGINED){
			LoginParam loginParams = LoginManager.getInstance().GetLoginParam();
			long timestamp = 0;
			if(null != loginParams 
					&& null != loginParams.item){
				timestamp = loginParams.item.adTimestamp;
			}
			if(timestamp > 0){
				//timestamp有效
				long currentTime = System.currentTimeMillis();
				if(QpidApplication.mHomeActicityDestroyTime > 0
						&& ((currentTime - QpidApplication.mHomeActicityDestroyTime) > timestamp*1000)){
					promotionNotify();
				}
			}
		}
	}
	
	/**
	 * 和服务器同步App推广信息
	 */
	private void promotionNotify(){
		TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
		RequestOperator.getInstance().AppPromotionAdvert(RequestJni.GetDeviceId(tm), new OnAppPromotionAdvertCallback() {
			
			@Override
			public void OnAppPromotionAdvert(boolean isSuccess, String errno,
					String errmsg, String adOverview) {
				if(isSuccess && !TextUtils.isEmpty(adOverview)){
					mAdOverview = adOverview;
					Message msg = Message.obtain();
					msg.what = RequestFlag.REQUEST_UPDATE_APP_EXTENSION_CALLBACK.ordinal();
					sendUiMessage(msg);
				}
			}
		});
	}
	
	/**
	 * 弹出推广信息
	 */
	private void showAppExtensionDialog(){
		if(LoginManager.getInstance().GetLoginStatus() == LoginStatus.LOGINED){
			//已登录弹出提示，否则取消
			if(!TextUtils.isEmpty(mAdOverview)){
				TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
				String domain = WebSiteManager.getInstance().GetWebSite().getAppSiteHost();
				String url = domain;
				url += "/advert/overviewadvert";
				url += "/deviceId/";
				url += RequestJni.GetDeviceId(tm);
				url += "/adkey/";
				url += mAdOverview;
				NormalWebviewDialog dialog = new NormalWebviewDialog(this);
				dialog.loadUrl(url);
				if(isActivityVisible()){
					dialog.show();
				}
			}
		}
	}
	
	/**
	 * 是否需要切换站点
	 * @param bundle
	 * @return
	 */
	private boolean needChangeWebSite(Bundle bundle){
		boolean isNeedChange = false;
		if(bundle != null && bundle.containsKey(START_SITE_ID)){
			int siteId = bundle.getInt(START_SITE_ID);
			final WebSiteType siteType = WebSiteType.values()[siteId];
			if(!WebSiteManager.getInstance().isCurrentSite(siteType)){
				isNeedChange = true;
				if(mChangeWebSiteDialog == null){
					mChangeWebSiteDialog = new MaterialDialogAlert(mContext);
					mChangeWebSiteDialog.setMessage(String.format(getString(R.string.website_change_notify), siteType.name()));
					mChangeWebSiteDialog.addButton(mChangeWebSiteDialog.createButton(
							mContext.getString(R.string.common_btn_ok),
							new OnClickListener() {

								@Override
								public void onClick(View v) {
									if(mMenuFragment != null){
										mMenuFragment.onWebSiteChange(siteType);
									}
								}
							}));
					mChangeWebSiteDialog.addButton(mChangeWebSiteDialog.createButton(
							mContext.getString(R.string.common_btn_cancel), null));
				}
				if(!mChangeWebSiteDialog.isShowing()){
					mChangeWebSiteDialog.show();
				}
			}
		}
		return isNeedChange;
	}

//	 private void showContacts() {
//	     if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
//	             != PackageManager.PERMISSION_GRANTED) {
//	    	 Log.d("test", "showContacts");
//	         requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
//	                 1);
//	     } else {
//	    	 Log.d("test", "showContacts1");
//	     }
//	 }
//
//	 @Override
//	 public void onRequestPermissionsResult(int requestCode, String[] permissions,
//	         int[] grantResults) {
//		 Log.d("test", "showContacts2");
//	     if (requestCode == 1
//	             && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//	         showContacts();
//	         Log.d("test", "showContacts3");
//	     }
//	     
//	 }

}
