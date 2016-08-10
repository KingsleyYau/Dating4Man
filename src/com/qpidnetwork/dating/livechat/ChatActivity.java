package com.qpidnetwork.dating.livechat;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.text.Editable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.ImageSpan;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.qpidnetwork.dating.R;
import com.qpidnetwork.dating.authorization.LoginManager;
import com.qpidnetwork.dating.authorization.LoginParam;
import com.qpidnetwork.dating.authorization.LoginPerfence;
import com.qpidnetwork.dating.bean.RequestBaseResponse;
import com.qpidnetwork.dating.contacts.ContactManager;
import com.qpidnetwork.dating.emf.MailEditActivity;
import com.qpidnetwork.dating.home.HomeActivity;
import com.qpidnetwork.dating.lady.LadyDetailActivity;
import com.qpidnetwork.dating.lady.LadyDetailManager;
import com.qpidnetwork.dating.lady.LadyDetailManager.OnLadyDetailManagerQueryLadyDetailCallback;
import com.qpidnetwork.dating.livechat.normalexp.NormalExprssionFragment;
import com.qpidnetwork.dating.livechat.picture.PictureHelper;
import com.qpidnetwork.dating.livechat.picture.PictureSelectActivity;
import com.qpidnetwork.dating.livechat.picture.PictureSelectFragment;
import com.qpidnetwork.dating.livechat.theme.ThemeConfigItem;
import com.qpidnetwork.dating.livechat.theme.ThemeImageHandler;
import com.qpidnetwork.dating.livechat.theme.ThemeImageHandler.ThemeImageHandlerListener;
import com.qpidnetwork.dating.livechat.theme.ThemeParse;
import com.qpidnetwork.dating.livechat.theme.ThemePopupWindow;
import com.qpidnetwork.dating.livechat.theme.ThemePopupWindow.OnItemClickListener;
import com.qpidnetwork.dating.livechat.theme.ThemePreviewer;
import com.qpidnetwork.dating.livechat.theme.store.ThemeMainActivity;
import com.qpidnetwork.dating.livechat.video.VideoHistoryListActivity;
import com.qpidnetwork.dating.livechat.voice.VoiceRecordFragment;
import com.qpidnetwork.framework.base.BaseFragmentActivity;
import com.qpidnetwork.framework.util.ImageUtil;
import com.qpidnetwork.framework.util.StringUtil;
import com.qpidnetwork.framework.util.SystemUtil;
import com.qpidnetwork.framework.util.UnitConversion;
import com.qpidnetwork.framework.widget.CircleImageView;
import com.qpidnetwork.livechat.LCEmotionItem;
import com.qpidnetwork.livechat.LCMagicIconItem;
import com.qpidnetwork.livechat.LCMessageItem;
import com.qpidnetwork.livechat.LCMessageItem.MessageType;
import com.qpidnetwork.livechat.LCMessageItem.SendType;
import com.qpidnetwork.livechat.LCNotifyItem.NotifyType;
import com.qpidnetwork.livechat.LCSystemItem;
import com.qpidnetwork.livechat.LCSystemLinkItem.SystemLinkOptType;
import com.qpidnetwork.livechat.LCUserItem;
import com.qpidnetwork.livechat.LCUserItem.ChatType;
import com.qpidnetwork.livechat.LiveChatManager;
import com.qpidnetwork.livechat.LiveChatManagerEmotionListener;
import com.qpidnetwork.livechat.LiveChatManagerMagicIconListener;
import com.qpidnetwork.livechat.LiveChatManagerMessageListener;
import com.qpidnetwork.livechat.LiveChatManagerOtherListener;
import com.qpidnetwork.livechat.LiveChatManagerPhotoListener;
import com.qpidnetwork.livechat.LiveChatManagerThemeListener;
import com.qpidnetwork.livechat.LiveChatManagerTryTicketListener;
import com.qpidnetwork.livechat.LiveChatManagerVideoListener;
import com.qpidnetwork.livechat.LiveChatManagerVoiceListener;
import com.qpidnetwork.livechat.jni.LCPaidThemeInfo;
import com.qpidnetwork.livechat.jni.LiveChatClient.UserStatusType;
import com.qpidnetwork.livechat.jni.LiveChatClientListener.KickOfflineType;
import com.qpidnetwork.livechat.jni.LiveChatClientListener.LiveChatErrType;
import com.qpidnetwork.livechat.jni.LiveChatClientListener.TalkEmfNoticeType;
import com.qpidnetwork.livechat.jni.LiveChatClientListener.TryTicketEventType;
import com.qpidnetwork.livechat.jni.LiveChatTalkUserListItem;
import com.qpidnetwork.manager.ConfigManager;
import com.qpidnetwork.manager.FileCacheManager;
import com.qpidnetwork.manager.ThemeConfigManager;
import com.qpidnetwork.request.OnRequestCallback;
import com.qpidnetwork.request.RequestJniEMF.ReplyType;
import com.qpidnetwork.request.RequestJniLiveChat.VideoPhotoType;
import com.qpidnetwork.request.RequestOperator;
import com.qpidnetwork.request.item.Coupon;
import com.qpidnetwork.request.item.Coupon.CouponStatus;
import com.qpidnetwork.request.item.LadyDetail;
import com.qpidnetwork.request.item.MagicIconConfig;
import com.qpidnetwork.request.item.OtherEmotionConfigItem;
import com.qpidnetwork.request.item.OtherSynConfigItem;
import com.qpidnetwork.request.item.ThemeItem;
import com.qpidnetwork.tool.ImageViewLoader;
import com.qpidnetwork.view.ButtonFloat;
import com.qpidnetwork.view.GetMoreCreditDialog;
import com.qpidnetwork.view.MaterialDialogAlert;
import com.qpidnetwork.view.MaterialDropDownMenu;
import com.qpidnetwork.view.MaterialThreeButtonDialog;

public class ChatActivity extends BaseFragmentActivity implements
		LiveChatManagerMessageListener, LiveChatManagerEmotionListener,
		LiveChatManagerPhotoListener, LiveChatManagerTryTicketListener,
		LiveChatManagerVoiceListener, LiveChatManagerOtherListener,
		LiveChatManagerVideoListener, LiveChatManagerMagicIconListener,
		OnItemClickListener, LiveChatManagerThemeListener{

	public static final String SEND_VOICE_ACTION = "livechat.sendvoice";
	public static final String SEND_EMTOTION_ACTION = "livechat.sendemotion";
	public static final String SEND_MAGICICON_ACTION = "livechat.sendmagicicon";
	public static final String REFRESH_THEME_CONFIG = "refresh.theme.config";// 主题购买成功
	public static final String WOMAN_ID = "woman_id";// 女士id
	public static final String EMOTION_ID = "emotion_id";
	public static final String MAGICICON_ID = "magicicon_id";

	private static final String CHAT_TARGET_ID = "targetId";
	private static final String CHAT_TARGET_NAME = "targetName";
	private static final String CHAT_TARGET_PHOTO_URL = "targetPhotoUrl";

	private static final int MIN_HEIGHT = 200;// 单位dp

	public static final int CHAT_SELECT_PHOTO = 1001;
	public static final int RESULT_LOAD_IMAGE_CAPTURE = 1002;

	private static final int RECEIVE_CHAT_MESSAGE = 0;
	private static final int PHOTO_FEE_SUCCESS = 1;
	private static final int PRIVATE_SHOW_PHOTO_DOWNLOADED = 2;
	private static final int RECEIVE_CHECK_SEND_MESSAGE_ERROR = 3;
	private static final int GET_HISTORY_MESSAGE_UPDATE = 5;
	private static final int CHECK_COUPON_UPDATE = 6;
	private static final int SEND_MESSAGE_CALLBACK = 7;
	private static final int TARGET_PHOTO_UPDATE = 8;
	private static final int END_CHAT = 9;
	private static final int REQUEST_ADD_FAVOUR_SUCCESS = 10;
	private static final int REQUEST_ADD_FAVOUR_FAIL = 11;
	// video 相关
	private static final int VIDEO_FEE_SUCCESS = 12;
	private static final int VIDEO_THUMB_PHOTO_DOWNLOADED = 13;
	private static final int VIDEO_DOWNLOAD_STATUS_NOTIFY = 14;
	private static final int GET_TARGET_STATUS_CALLBACK = 15;
	// theme 相关
	private static final int LOAD_THEME_ONFINISH = 16;
	private static final int DOWNLOAD_THEME_SOURCE_CALLBACK = 17;
	private static final int RENEW_THEME_CALLBACK = 18;
	private static final int RECOMMAND_THEME_CALLBACK = 19;
	private static final int RECIEVE_THEME_PLAY_REQUEST = 20;

	// fragments
	private NormalExprssionFragment normalExprssionFragment;
	private PictureSelectFragment pictureSelectFragment;
	private VoiceRecordFragment voiceRecordFragment;
//	private EmotionMainFragment emotionMainFragment;
	private CameraViewFragment cameraViewFragment;
	
	/* root */
	private RelativeLayout llRoot;
	private ImageView bg_img;
	private LinearLayout contentBody;
	private FrameLayout flChatArea;

	/* title */
	private LinearLayout llBack;
	private CircleImageView ivPhoto;
	private TextView tvUnread;
	private TextView tvName;
	private ImageButton btnSceneStore;// 场景
	private ImageButton btnMore;
	private MaterialDropDownMenu dropDown;
	
	private LinearLayout llHead;

	private EditText etMessage;
	private ImageButton btnSend;
	private MessageListView msgList;

	private LinearLayout llVoiceBody;
	private LinearLayout llThemeBody;
	private ImageButton btnExpression;
	private ImageButton btnTakePhoto;
	private ImageButton btnSelectPhoto;
	private ImageButton btnVoice;
	private ImageButton btnEmotion;
	private ImageButton btnTheme;// 主题选项

	private ThemePopupWindow themePopupWindow;// 主题选项弹出层
	private ThemePreviewer themePreviewer;// 主题动画播放

	private ThemeConfigItem mConfigItem;// 主题配置数据对象

	/* 底部pane */
	private FrameLayout flBottom;
	private boolean isResizeInOnLayout = false;

	/* 广播用于activity间数据传递 */
	private BroadcastReceiver mBroadcastReceiver;

	// dialogs
	MaterialThreeButtonDialog takePhotoAlert;

	/* 当前聊天对象信息 */
	String targetId;
	String targetName;
	String targetUrl;

	/* 数据管理区 */
	private LCUserItem chatTarget; // 存储当前聊天对象
	private LiveChatManager mLiveChatManager;

	private String takePhotoTempPath = "";

	/* 处理弹出小菜单功能时，返回键首先关闭小菜单功能，再关闭窗口及点击msgList空白区域，关闭小菜单功能 */
	public boolean isMenuOpen = false;

	private ContactManager mContactManager;// 联系人列表管理类，此处主要用于在聊设置，及未读条数显示
	private int unreadCount = 0;// 未读消息条数

	private ThemeImageHandler mThemeHandler;// 生成主题工具类

	private boolean isCurrActivityVisible = true;// 简单判断当前Activity是否可见，用于下载私密照返回处理是否更新

	private boolean photosendEnable = true; // 风控模块，判断是否可以发送私密照，以便控制按钮响应及显示
	
	//用于解决消息输入框6.0删除表情需要多次问题
	private ArrayList<ImageSpan> mEmoticonsToRemove = new ArrayList<ImageSpan>();
	
	//不在线通知
	private MaterialDialogAlert offlineDialog;
	private boolean onlineChecked = false;//防止联系人获取状态回调影响不停弹出检测
	
	//记录当前使用的主题
	private boolean isThemeEnable = false;
	LCPaidThemeInfo mCurrentTheme;
	ThemeItem mCurThemeIDetail;

	public static void launchChatActivity(Context context, String targetId,
			String targetName, String photoUrl) {
		Intent intent = new Intent();
		/* 未登录成功跳转登陆界面 */
		if (LoginManager.getInstance().CheckLogin(context)) {
			LoginParam loginParam = LoginManager.getInstance().GetLoginParam();
			if (loginParam.item.premit && !loginParam.item.livechat) {
				/* 账号未被冻结且livechat未被风控，可直接进入聊天界面 */
				intent.setClass(context, ChatActivity.class);
				intent.putExtra(CHAT_TARGET_ID, targetId);
				intent.putExtra(CHAT_TARGET_NAME, targetName);
				intent.putExtra(CHAT_TARGET_PHOTO_URL, photoUrl);
				context.startActivity(intent);
			} else {
				/* 账号被冻结或者livechat被风控则不可聊天,弹框提示 */
				MaterialDialogAlert dialog = new MaterialDialogAlert(context);
				dialog.setMessage(context
						.getString(R.string.common_risk_control_notify));
				dialog.addButton(dialog.createButton(
						context.getString(R.string.common_btn_ok), null));
				dialog.show();
			}
		}
	}

	@Override
	protected void onCreate(Bundle arg0) {
		// TODO Auto-generated method stub
		super.onCreate(arg0);
		setContentView(R.layout.activity_livechat_chat);

		/* 风控条件初始化 */
		if((LoginManager.getInstance().GetLoginParam() != null)&&(LoginManager.getInstance().GetLoginParam().item != null)){
			photosendEnable = LoginManager.getInstance().GetLoginParam().item.photosend;
		}
		
		OtherSynConfigItem configItem = ConfigManager.getInstance().getSynConfigItem();
		if(configItem != null &&
				configItem.pub != null){
			isThemeEnable = configItem.pub.chatscene_enable;
		}
		
		mLiveChatManager = LiveChatManager.getInstance();
		initLivechatConfig();
		initReceive();
		initViews();
		initData();
		initKeyboardDetect();
		
		if(isThemeEnable){
			initAndApplyTheme();
		}
	}

	/**
	 * 初始化配置LivechatManager，监听消息请求及推送事件
	 */
	private void initLivechatConfig() {
		/* 绑定监听回调事件 */
		mLiveChatManager.RegisterMessageListener(this);
		mLiveChatManager.RegisterEmotionListener(this);
		mLiveChatManager.RegisterPhotoListener(this);
		mLiveChatManager.RegisterTryTicketListener(this);
		mLiveChatManager.RegisterVoiceListener(this);
		mLiveChatManager.RegisterOtherListener(this);
		mLiveChatManager.RegisterVideoListener(this);
		mLiveChatManager.RegisterMagicIconListener(this);
		mLiveChatManager.RegisterThemeListener(this);

	}

	private void initViews() {
		/*background*/
		bg_img = (ImageView)findViewById(R.id.bg_img);
		contentBody = (LinearLayout)findViewById(R.id.contentBody);
		flChatArea = (FrameLayout)findViewById(R.id.flChatArea);

		/* title */
		llBack = (LinearLayout) findViewById(R.id.llBack);
		llBack.setOnClickListener(this);
		tvUnread = (TextView) findViewById(R.id.tvUnread);
		ivPhoto = (CircleImageView) findViewById(R.id.ivPhoto);
		tvName = (TextView) findViewById(R.id.tvName);
		btnSceneStore = (ImageButton) findViewById(R.id.btnSceneStore);
		btnMore = (ImageButton) findViewById(R.id.btnMore);
		
		llHead = (LinearLayout)findViewById(R.id.llHead);

		msgList = (MessageListView) findViewById(R.id.msgList);
		msgList.setOnTouchListener(onMessageListTouchListener);

		/* 文字信息编辑 */
		etMessage = (EditText) findViewById(R.id.etMessage);
		etMessage.addTextChangedListener(edtInputWatcher);
		etMessage.setOnTouchListener(edtInputTouch);

		btnSend = (ImageButton) findViewById(R.id.btnSend);
		btnSend.setOnClickListener(this);
		// btnSend.setEnabled(etMessage.getText().length() > 0 ? true : false);

		if (Build.VERSION.SDK_INT >= 21) {
			findViewById(R.id.back_button_icon).getLayoutParams().height = UnitConversion
					.dip2px(this, 48);
			findViewById(R.id.back_button_icon).getLayoutParams().width = UnitConversion
					.dip2px(this, 48);
			btnMore.getLayoutParams().height = UnitConversion
					.dip2px(this, 48);
			btnMore.getLayoutParams().width = UnitConversion
					.dip2px(this, 48);
			((LinearLayout.LayoutParams)btnMore.getLayoutParams()).rightMargin = 0;
			
		}

		/* 工具栏操作区 */
		themePopupWindow = new ThemePopupWindow(this);

		themePopupWindow.setOnItemClickListener(this);


		btnExpression = (ImageButton) findViewById(R.id.btnExpression);
		btnTakePhoto = (ImageButton) findViewById(R.id.btnTakePhoto);
		btnSelectPhoto = (ImageButton) findViewById(R.id.btnSelectPhoto);
		btnVoice = (ImageButton) findViewById(R.id.btnVoice);
		btnEmotion = (ImageButton) findViewById(R.id.btnEmotion);
		btnTheme = (ImageButton) findViewById(R.id.btnTheme);
		if (photosendEnable) {
			btnTakePhoto.setOnClickListener(this);
			btnSelectPhoto.setOnClickListener(this);
		} else {
			btnTakePhoto
					.setImageResource(R.drawable.ic_photo_camera_greyc8c8c8_24dp);
			btnSelectPhoto
					.setImageResource(R.drawable.ic_photo_greyc8c8c8_24dp);
			btnTakePhoto.setClickable(false);
			btnSelectPhoto.setClickable(false);
		}
		btnExpression.setOnClickListener(this);
		btnVoice.setOnClickListener(this);
		btnEmotion.setOnClickListener(this);
		btnTheme.setOnClickListener(this);
		btnSceneStore.setOnClickListener(this);
		btnMore.setOnClickListener(this);
		ivPhoto.setOnClickListener(this);
		
		/*主题隐藏相关*/

		llVoiceBody = (LinearLayout)findViewById(R.id.llVoiceBody);
		llThemeBody = (LinearLayout)findViewById(R.id.llThemeBody);
		if(!isThemeEnable){
			LinearLayout.LayoutParams voiceParams = (LinearLayout.LayoutParams)llVoiceBody.getLayoutParams();
			voiceParams.width = LinearLayout.LayoutParams.WRAP_CONTENT;
			voiceParams.weight = 0;
			llThemeBody.setVisibility(View.GONE);
			btnSceneStore.setVisibility(View.GONE);
		}
		
		/* 底部pane */
		flBottom = (FrameLayout) findViewById(R.id.flBottom);
	}

	private void initData() {
		Bundle bundle = getIntent().getExtras();
		if (bundle != null) {
			if (bundle.containsKey(CHAT_TARGET_ID)) {
				targetId = bundle.getString(CHAT_TARGET_ID);
			}
			if (bundle.containsKey(CHAT_TARGET_NAME)) {
				targetName = bundle.getString(CHAT_TARGET_NAME);
			}
			if (bundle.containsKey(CHAT_TARGET_PHOTO_URL)) {
				targetUrl = bundle.getString(CHAT_TARGET_PHOTO_URL);
			}
		}

		if ((StringUtil.isEmpty(targetId))) {
			finish();
			return;
		} else {
			/* 初始化正在聊天对象，方便统计未读 */
			mContactManager = ContactManager.getInstance();
			mContactManager.mWomanId = targetId;
			mContactManager.clearContactUnreadCount(targetId);

			/* 初始化未读条数 */
			unreadCount = mContactManager.getAllUnreadCount();
			if (unreadCount > 0) {
				tvUnread.setText("" + unreadCount);
				tvUnread.setVisibility(View.VISIBLE);
			} else {
				tvUnread.setVisibility(View.GONE);
			}

			/* 初始化Title */
			tvName.setText(targetName);
			if (!StringUtil.isEmpty(targetUrl)) {
				String localPath = FileCacheManager.getInstance()
						.CacheImagePathFromUrl(targetUrl);
				new ImageViewLoader(this).DisplayImage(ivPhoto, targetUrl,
						localPath, null);
			} else {
				QueryLadyDetail(targetId);
			}

			/* 是否在聊天列表（即有消息来往） */
			chatTarget = mLiveChatManager.GetUserWithId(targetId);
			if (chatTarget != null) {
				/* 加载历史消息 */
				boolean isMsgListEmpty = false;
				synchronized (chatTarget.getMsgList())
				{
					isMsgListEmpty = chatTarget.getMsgList().isEmpty();
				}
				if (!isMsgListEmpty) {
					showMsgBeans(chatTarget.getMsgList(), true);
				} else if ((chatTarget.chatType == ChatType.InChatCharge)
						|| (chatTarget.chatType == ChatType.InChatUseTryTicket)) {
					mLiveChatManager.GetHistoryMessage(chatTarget.userId);
				}

				/* 未开始聊天获取试聊状态 */
				if ((chatTarget.chatType == ChatType.Invite)
						|| (chatTarget.chatType == ChatType.Other)) {
					checkTrychat(chatTarget.userId);
				}
			}
			
			/*检测聊天女士是否在线*/
			mLiveChatManager.GetUserStatus(new String[]{targetId});
		}
	}

	/**
	 * 设置监控软键盘弹出时，计算软键盘高度，以此确定子view高度
	 */
	private void initKeyboardDetect() {
		llRoot = (RelativeLayout) findViewById(R.id.llRoot);
		llRoot.getViewTreeObserver().addOnGlobalLayoutListener(
				new ViewTreeObserver.OnGlobalLayoutListener() {

					@SuppressWarnings("deprecation")
					@Override
					public void onGlobalLayout() {
						// TODO Auto-generated method stub
						Rect r = new Rect();
						llRoot.getRootView().getWindowVisibleDisplayFrame(r);
						
						WindowManager windowManager = getWindowManager();  
						Display display = windowManager.getDefaultDisplay();   
						int screenHeight = display.getHeight(); 

//						int screenHeight = llRoot.getRootView().getHeight();
						int heightDifference = screenHeight
								- (r.bottom - r.top);
//						 Log.i("hunter", "onGlobalLayout heightDifference: " +
//						 heightDifference);
//						 Log.i("hunter", " screenHeight: " + screenHeight + " bottom: " + r.bottom + " top: " +
//						 r.top);
						if (heightDifference - r.top >= 0) {
//							 Log.i("hunter", "onGlobalLayout height: " +
//							 heightDifference);
							/*
							 * 之前并未保存，第一次获取virtual keyboard 高度，保存到本地，以方便设置Bottom
							 * view 高度， 、 否则可能是因为中英文切换导致virtual
							 * keyboard改变，非真实高度不保存
							 */
							if (heightDifference - r.top > 0) {
								saveKeyboardHeight(heightDifference - r.top);
							}
							/* 根据键盘高度，设置底部高度 */
							if (isResizeInOnLayout) {
//								 Log.i("hunter",
//								 "onGlobalLayout isResizeInOnLayout");

								/* 此处判断弹出软键盘时的菜单控制 */
								if (heightDifference - r.top == 0) {
									/* 软键盘收起来了 */
									isMenuOpen = false;
									/* 防止收起键盘重新弹出后闪烁 */
									// Fragment fragment =
									// getSupportFragmentManager().findFragmentById(R.id.flPane);
									// if(fragment != null){
									// FragmentTransaction transaction =
									// getSupportFragmentManager().beginTransaction();
									// transaction.remove(fragment);
									// transaction.commit();
									// }
									// /*还原按钮未选中图标*/
									// resetMenuButtonBg();
								} else if (heightDifference - r.top > 0) {
									/* 软键盘打开 */
									isMenuOpen = true;
									LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) flBottom
											.getLayoutParams();
									params.height = heightDifference - r.top;
									flBottom.setLayoutParams(params);
									flBottom.setVisibility(View.GONE);
									getWindow()
											.setSoftInputMode(
													WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
									msgList.scrollToBottom(true);
								}
							}
						}
					}
				});
		if (getKeyboardHeight() < 0) {
			/* 之前未能获取虚拟键盘高度，需通过弹出键盘，计算虚拟键盘高度 */
			etMessage.requestFocus();
			isResizeInOnLayout = true;
			getWindow().setSoftInputMode(
					WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
		} else {
			hideSoftInput();
			isMenuOpen = false;
			getWindow()
					.setSoftInputMode(
							WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN
									| WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
		}
	}
	
	/**
	 * 初始化并加载主题
	 */
	private void initAndApplyTheme(){
		mCurrentTheme = mLiveChatManager.getCurrentUsedTheme(targetId);
		if(themePreviewer != null){
			//加载新主题前，关闭动画
			themePreviewer.dismiss();
		}
		if(mCurrentTheme != null){
			mCurThemeIDetail = ThemeConfigManager.newInstance().getThemeItemByThemeId(mCurrentTheme.themeId);
			//是否正在购买当前主题中
			if(mLiveChatManager.isThemeBuying(targetId, mCurrentTheme.themeId)){
				String message = String.format(mContext.getString(R.string.livechat_theme_renewing_notify), getCurrentThemeTitle());
				mLiveChatManager.BuildTSystemLinkMsg(targetId, message, "", SystemLinkOptType.Unknow, mCurrentTheme);
				return;
			}
			
			//当前有主题
			if(mLiveChatManager.isThemeExpired(mCurrentTheme)){
				//主题过期
				LCUserItem userItem = mLiveChatManager.GetUserWithId(targetId);
				if(userItem != null && 
						(userItem.chatType == ChatType.InChatCharge 
						|| userItem.chatType == ChatType.InChatCharge)){
					//构成会话
					if(!ThemeConfigManager.newInstance().isThemeOffShelf(mCurrentTheme.themeId)){
						//未下架弹出续费提示
						showThemeRenewDialog();
						return;
					}
				}
				//已下架或者未构成会话，提示并清除过期主题，加载另外一套可用主题（如果存在）
				String message = String.format(mContext.getString(R.string.livechat_theme_expired_notify), getCurrentThemeTitle());
				mLiveChatManager.BuildTSystemLinkMsg(targetId, message, "", SystemLinkOptType.Unknow, mCurrentTheme);
				mLiveChatManager.removeOverTimeTheme(targetId);//清除过期主题
				//加载用于另外一套主题
				mCurrentTheme = mLiveChatManager.getCurrentUsedTheme(targetId);
				if(mCurrentTheme != null){
					mCurThemeIDetail = ThemeConfigManager.newInstance().getThemeItemByThemeId(mCurrentTheme.themeId);
				}
			}
		}
		//加载应用主题
		if(mCurrentTheme != null){
			loadTheme();
		}
	}
	
	/**
	 * 获取当前主题title
	 * @return
	 */
	private String getCurrentThemeTitle(){
		String title = "";
		if(mCurThemeIDetail != null){
			title = mCurThemeIDetail.title;
		}
		if(TextUtils.isEmpty(title)){
			if(mCurrentTheme != null){
				title = mCurrentTheme.themeId;
			}
		}
		return title;
	}
	
	/**
	 * 提示主题过期续费
	 */
	private void showThemeRenewDialog(){
		MaterialDialogAlert dialog = new MaterialDialogAlert(this);
		String message = String.format(mContext.getString(R.string.livechat_theme_expired_renew_notify), getCurrentThemeTitle());
		if(mCurThemeIDetail != null){
			//添加价格标示
			message += "(" + mCurThemeIDetail.price + " credits)";
		}
		dialog.setMessage(message);
		dialog.addButton(dialog.createButton(
				mContext.getString(R.string.common_btn_ok),
				new OnClickListener() {

					@Override
					public void onClick(View v) {
						//进入续费流程
						renewCurrentTheme();
					}
				}));
		dialog.addButton(dialog.createButton(
				mContext.getString(R.string.common_btn_no),
				new OnClickListener() {

					@Override
					public void onClick(View v) {
						//已下架或者未构成会话，提示并清除过期主题，加载另外一套可用主题（如果存在）
						String message = String.format(mContext.getString(R.string.livechat_theme_expired_notify), getCurrentThemeTitle());
						mLiveChatManager.BuildTSystemLinkMsg(targetId, message, "", SystemLinkOptType.Unknow, mCurrentTheme);
						mLiveChatManager.removeOverTimeTheme(targetId);//清除过期主题
						mCurrentTheme = mLiveChatManager.getCurrentUsedTheme(targetId);
						if(mCurrentTheme != null){
							mCurThemeIDetail = ThemeConfigManager.newInstance().getThemeItemByThemeId(mCurrentTheme.themeId);
						}
						if(mCurrentTheme != null){
							loadTheme();
						}
					}

				}));

		dialog.show();
	}
	
	/**
	 * 异步加载主题
	 */
	public void loadTheme(){
		if(mCurrentTheme != null){
			//通知用户加载中...
			String message = String.format(mContext.getString(R.string.livechat_theme_applying_notify), getCurrentThemeTitle());
			mLiveChatManager.BuildTSystemLinkMsg(targetId, message, "", SystemLinkOptType.Unknow, null);
			
			//记载主题
			if(!mLiveChatManager.GetThemeResource(mCurrentTheme.themeId)){
				Message msg = Message.obtain();
				msg.what = DOWNLOAD_THEME_SOURCE_CALLBACK;
				msg.obj = new RequestBaseResponse(false, "", "", null);
				sendUiMessage(msg);
			}
		}
	}
	
	/**
	 * 资源下载完成，生成配置文件及背景图片
	 * @param themeId
	 */
	private void parseThemeConfig(String themeId){
		Log.i("hunter", "parseThemeConfig themeId: " + themeId);
		mConfigItem = ThemeParse.parseThemeConfig(this, themeId);
		int width = SystemUtil.getDisplayMetrics(this).widthPixels;
		int height = SystemUtil.getDisplayMetrics(this).heightPixels - UnitConversion.dip2px(this, 25 + 56 + 92);//状态栏高度25
		String tmpBgPath = FileCacheManager.getInstance().getThemeSavePath() + themeId + "/" + "bg.png";
		File bgFile = new File(tmpBgPath);
		if(!bgFile.exists()){
			try {
				bgFile.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if(mConfigItem != null){
			mThemeHandler = new ThemeImageHandler();
			mThemeHandler.SetListener(new ThemeImageHandlerListener() {
				
				@Override
				public void OnFinish(ThemeImageHandler handler, Bitmap bgBitmap) {
					Message msg = Message.obtain();
					msg.what = LOAD_THEME_ONFINISH;
					msg.obj = bgBitmap;
					sendUiMessage(msg);
				}
			});
			mThemeHandler.LoadThemeBgImage(this, tmpBgPath, width, height, mConfigItem);
		}
	}
	
	/**
	 * 重新购买主题
	 */
	public void renewCurrentTheme(){
		//去续费，提示正在续费中
		String message = String.format(mContext.getString(R.string.livechat_theme_renewing_notify), getCurrentThemeTitle());
		mLiveChatManager.BuildTSystemLinkMsg(targetId, message, "", SystemLinkOptType.Unknow, mCurrentTheme);
		if(!mLiveChatManager.ManFeeTheme(targetId, mCurrentTheme.themeId)){
			Message msg = Message.obtain();
			msg.what = RENEW_THEME_CALLBACK;
			msg.arg1 = Integer.valueOf(LiveChatErrType.Fail.ordinal());
			sendUiMessage(msg);
		}
	}
	
	/**
	 * 设置默认拍照路径
	 * @param filePath
	 */
	public void setTempPicturePath(String filePath){
		takePhotoTempPath = filePath;
	}

	private void initReceive() {
		mBroadcastReceiver = new BroadcastReceiver() {

			@Override
			public void onReceive(Context context, Intent intent) {
				// TODO Auto-generated method stub
				String action = intent.getAction();
				if (action.equals(SEND_EMTOTION_ACTION)) {
					String emotionId = intent.getExtras().getString(EMOTION_ID);
					sendEmotionItem(emotionId);
				}else if(action.equals(SEND_MAGICICON_ACTION)){
					String magicIconId = intent.getExtras().getString(MAGICICON_ID);
					sendMagicIcon(magicIconId);
				} else if (action.equals(REFRESH_THEME_CONFIG)) {// 主题购买成功
					String womanId = intent.getExtras().getString(WOMAN_ID);// 获取女士ID
					if (targetId.equals(womanId)) {
						initAndApplyTheme();// 主题购买的女士为当前聊天界面的女士则重新获取应用主题
					}
				}
			}
		};
		IntentFilter filter = new IntentFilter();
		filter.addAction(SEND_VOICE_ACTION);
		filter.addAction(SEND_EMTOTION_ACTION);
		filter.addAction(SEND_MAGICICON_ACTION);
		filter.addAction(REFRESH_THEME_CONFIG);// 主题购买成功
		registerReceiver(mBroadcastReceiver, filter);
	}

	/**
	 * 编辑框选中
	 */
	private OnTouchListener edtInputTouch = new OnTouchListener() {
		@Override
		public boolean onTouch(View v, MotionEvent event) {
			if (event.getAction() == MotionEvent.ACTION_DOWN) {
				isResizeInOnLayout = true;
				msgList.scrollToBottom(true);// 消息列表滚到底部，防止多过一页，键盘弹起时部分被覆盖
				resetMenuButtonBg();
				btnExpression
						.setImageResource(R.drawable.ic_tag_faces_grey600_24dp);
				btnExpression.setTag("false");
			}
			return false;
		}
	};

	@Override
	protected void onResume() {
		super.onResume();
		isCurrActivityVisible = true;
		mContactManager.mWomanId = targetId;
		LiveChatNotification.newInstance(this).Cancel();
	}

	@Override
	protected void onPause() {
		super.onPause();
		isCurrActivityVisible = false;
//		/* 当前界面不可见时，置空当前联系人，可接收push推送 */
//		mContactManager.mWomanId = "";
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		msgList.onDestroy();
		unregisterReceiver(mBroadcastReceiver);
		mLiveChatManager.UnregisterEmotionListener(this);
		mLiveChatManager.UnregisterMessageListener(this);
		mLiveChatManager.UnregisterOtherListener(this);
		mLiveChatManager.UnregisterPhotoListener(this);
		mLiveChatManager.UnregisterTryTicketListener(this);
		mLiveChatManager.UnregisterVoiceListener(this);
		mLiveChatManager.UnregisterVideoListener(this);
		mLiveChatManager.UnregisterMagicIconListener(this);
		mLiveChatManager.UnregisterThemeListener(this);

		/* 清除正在聊天对象 */
		mContactManager.mWomanId = "";
		
		//关闭动画播放
		if(themePreviewer!=null){
			themePreviewer.dismiss();
		}
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		super.onClick(v);
		switch (v.getId()) {
		case R.id.ivPhoto:
			if (targetId != null && targetId.length() > 0) {
				LadyDetailActivity.launchLadyDetailActivity(this, targetId,
						false);
			}
			break;
		case R.id.llBack:
			if (isMenuOpen) {
				/* 拦截返回键事件，菜单打开时优先关闭menu */
				closeSubMenu();
			} else {
				/* 解决onDestroy调用慢，导致在聊未读状态错误问题 */
				mContactManager.mWomanId = "";

				finish();
			}
			break;
		case R.id.btnSend:
			sendTextMsg();
			break;
		case R.id.btnExpression:
			onMenuButtonClick(MenuBtnType.NORMAL_EXPRESSION);
			
			// 统计event
			onAnalyticsEvent(getString(R.string.LiveChatF_Category)
					, getString(R.string.LiveChatF_Action_MagicIcon)
					, getString(R.string.LiveChatF_Label_MagicIcon));
			break;
		case R.id.btnTakePhoto:
			
			//Android api 11 or above will use in app camera. otherwise will open system camera.
			if (Build.VERSION.SDK_INT < 11){
				takePhoto();
			}else{
				onMenuButtonClick(MenuBtnType.USE_CAMERA);
			}
			
			// 统计event
			onAnalyticsEvent(getString(R.string.LiveChatF_Category)
					, getString(R.string.LiveChatF_Action_TakePhoto)
					, getString(R.string.LiveChatF_Label_TakePhoto));
			break;
		case R.id.btnSelectPhoto:
			onMenuButtonClick(MenuBtnType.SELECT_PHOTO);
			
			// 统计event
			onAnalyticsEvent(getString(R.string.LiveChatF_Category)
					, getString(R.string.LiveChatF_Action_Photo)
					, getString(R.string.LiveChatF_Label_Photo));
			break;
		case R.id.btnVoice:
			onMenuButtonClick(MenuBtnType.RECORD_VOICE);
			
			// 统计event
			onAnalyticsEvent(getString(R.string.LiveChatF_Category)
					, getString(R.string.LiveChatF_Action_Voice)
					, getString(R.string.LiveChatF_Label_Voice));
			break;
		case R.id.btnEmotion:
			onMenuButtonClick(MenuBtnType.EMOTION_PAN);

			// 统计event
			onAnalyticsEvent(getString(R.string.LiveChatF_Category),
					getString(R.string.LiveChatF_Action_AnimatedEmotions),
					getString(R.string.LiveChatF_Label_AnimatedEmotions));
			break;
		case R.id.btnTheme:// 主题选项
			
			//隐藏键盘
			InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(btnTheme.getWindowToken(), 0);
			
			resetMenuButtonBg();
			flBottom.setVisibility(View.GONE);

			if (themePopupWindow.isShowing()) {
				themePopupWindow.dismiss();
				//btnTheme.setImageResource(R.drawable.ic_add_scene);
			} else {
				if(mCurrentTheme == null){
					themePopupWindow.setPlayShowFlags(false);
				}else{
					themePopupWindow.setPlayShowFlags(true);
				}
				Rect r = new Rect();
				llRoot.getRootView().getWindowVisibleDisplayFrame(r);
				WindowManager windowManager = getWindowManager();  
				Display display = windowManager.getDefaultDisplay();   
				int screenHeight = display.getHeight();
				themePopupWindow.setHeight(screenHeight - r.top);
				themePopupWindow.show(btnTheme, r.top);
				//btnTheme.setImageResource(R.drawable.ic_scene_close);
			}
			break;
		case R.id.btnSceneStore:
			// 进入主题购买界面
			Intent intent = new Intent(ChatActivity.this,ThemeMainActivity.class);
			intent.putExtra("womanId", targetId);
			startActivity(intent);
			break;
		case R.id.btnMore:
			String[] menu = new String[] { getString(R.string.livechat_recent_watched_videos) };
			if (dropDown != null) {
				dropDown.showAsDropDown(btnMore);
				return;
			}
			dropDown = new MaterialDropDownMenu(ChatActivity.this, menu,
					new MaterialDropDownMenu.OnClickCallback() {

						@Override
						public void onClick(AdapterView<?> adptView, View v,
								int which) {
							// TODO Auto-generated method stub
							switch (which) {
							case 0: {
								VideoHistoryListActivity.launchVideoHistoryActivity(ChatActivity.this, targetId);
							}
								break;
							}
						}

					}, new Point((int) (220.0f * getResources()
							.getDisplayMetrics().density),
							LayoutParams.WRAP_CONTENT));

			dropDown.showAsDropDown(btnMore);

			break;
		default:
			break;
		}
	}

	/**
	 * 普通表情菜单点击响应
	 */
	private void onMenuButtonClick(MenuBtnType menutype) {
		/* 隐藏软键盘 */
		isResizeInOnLayout = false;
		isMenuOpen = true;
		msgList.scrollToBottom(true);// 消息列表滚到底部，防止多过一页，键盘弹起时部分被覆盖
		getWindow().setSoftInputMode(
				WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
		hideSoftInput();

		LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) flBottom.getLayoutParams();
		int keyboardHeight = (getKeyboardHeight() > UnitConversion.dip2px(this, MIN_HEIGHT)) ? getKeyboardHeight() : UnitConversion.dip2px(this, MIN_HEIGHT);
		params.height = keyboardHeight;
		//if (getKeyboardHeight() > UnitConversion.dip2px(this, MIN_HEIGHT)) {
		//	params.height = getKeyboardHeight();
		//} else {
		//	params.height = UnitConversion.dip2px(this, MIN_HEIGHT);
		//}

		flBottom.setLayoutParams(params);
		flBottom.setVisibility(View.VISIBLE);

		FragmentTransaction transaction = getSupportFragmentManager()
				.beginTransaction();

		/**
		 * set tap selected status
		 */
		resetMenuButtonBg();

		switch (menutype) {
		case NORMAL_EXPRESSION:

			if (normalExprssionFragment == null)
				normalExprssionFragment = new NormalExprssionFragment();
			transaction.replace(R.id.flPane, normalExprssionFragment);
			btnExpression.setImageResource(R.drawable.ic_tag_faces_blue_24dp);
			// btnExpression.setImageResource();

			// if (btnExpression.getTag() == null
			// || btnExpression.getTag().equals("false")) {
			// btnExpression
			// .setImageResource(R.drawable.ic_keyboard_grey600_24dp);
			// transaction.replace(R.id.flPane, new NormalExprssionFragment());
			// btnExpression.setTag("true");
			// } else {
			// btnExpression
			// .setImageResource(R.drawable.ic_tag_faces_grey600_24dp);
			// btnExpression.setTag("false");
			// showSoftInput();
			// isResizeInOnLayout = true;
			// }

			break;
		case SELECT_PHOTO:
			if (pictureSelectFragment == null)
				pictureSelectFragment = new PictureSelectFragment();
			transaction.replace(R.id.flPane, pictureSelectFragment);
			btnSelectPhoto.setImageResource(R.drawable.ic_photo_blue_24dp);
			break;
		case RECORD_VOICE:
			if (voiceRecordFragment == null)
				voiceRecordFragment = new VoiceRecordFragment();
			transaction.replace(R.id.flPane, voiceRecordFragment);
			btnVoice.setImageResource(R.drawable.ic_mic_blue_24dp);
			break;
		case EMOTION_PAN:
//			if (emotionMainFragment == null)
//				emotionMainFragment = new EmotionMainFragment();
//			transaction.replace(R.id.flPane, emotionMainFragment);
//			btnEmotion.setImageResource(R.drawable.ic_premium_emotion_blue_24dp);
			break;
		case USE_CAMERA:
			//Android api 11 or above will use in app camera. otherwise will open system camera.
			if (cameraViewFragment == null) cameraViewFragment = CameraViewFragment.newInstance(keyboardHeight);
			transaction.replace(R.id.flPane, cameraViewFragment);
			btnTakePhoto.setImageResource(R.drawable.ic_photo_camera_blue_24dp);
			break;
		default:
			break;
		}
		transaction.commitAllowingStateLoss();
	}

	@SuppressLint("RtlHardcoded")
	private void takePhoto() {
		/* 先关闭功能按钮块 */
		closeSubMenu();

		/* 拍照模块,调用系统拍照 */

		if (LoginPerfence.GetStringPreference(this,
				"donnot_tell_me_photo_fee_live_chat").equals("true")) {
			Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
			takePhotoTempPath = FileCacheManager.getInstance()
					.getPrivatePhotoTempSavePath()
					+ PictureHelper.getPhotoFileName();
			intent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT,
					Uri.fromFile(new File(takePhotoTempPath)));
			startActivityForResult(intent, RESULT_LOAD_IMAGE_CAPTURE);
			return;
		}

		if (takePhotoAlert != null) {
			takePhotoAlert.show();
			return;
		}

		takePhotoAlert = new MaterialThreeButtonDialog(this,
				new MaterialThreeButtonDialog.OnClickCallback() {

					@Override
					public void OnSecondButtonClick(View v) {
						// TODO Auto-generated method stub
						Intent intent = new Intent(
								MediaStore.ACTION_IMAGE_CAPTURE);
						takePhotoTempPath = FileCacheManager.getInstance()
								.getPrivatePhotoTempSavePath()
								+ PictureHelper.getPhotoFileName();
						intent.putExtra(
								android.provider.MediaStore.EXTRA_OUTPUT,
								Uri.fromFile(new File(takePhotoTempPath)));

						startActivityForResult(intent,
								RESULT_LOAD_IMAGE_CAPTURE);
						LoginPerfence.SaveStringPreference(ChatActivity.this,
								"donnot_tell_me_photo_fee_live_chat", "true");
					}

					@Override
					public void OnFirstButtonClick(View v) {
						// TODO Auto-generated method stub
						Intent intent = new Intent(
								MediaStore.ACTION_IMAGE_CAPTURE);
						takePhotoTempPath = FileCacheManager.getInstance()
								.getPrivatePhotoTempSavePath()
								+ PictureHelper.getPhotoFileName();
						intent.putExtra(
								android.provider.MediaStore.EXTRA_OUTPUT,
								Uri.fromFile(new File(takePhotoTempPath)));

						startActivityForResult(intent,
								RESULT_LOAD_IMAGE_CAPTURE);
					}

					@Override
					public void OnCancelButtonClick(View v) {
						// TODO Auto-generated method stub

					}
				});

		takePhotoAlert
				.setTitle(getString(R.string.livechat_take_photo_fee_note));
		takePhotoAlert
				.setMessage(getString(R.string.x_credits_will_be_charged_for_each_photo_sent));
		takePhotoAlert.hideImageView();
		takePhotoAlert.setFirstButtonText(getString(R.string.common_continue));
		takePhotoAlert
				.setSecondButtonText(getString(R.string.ok_and_donnot_tell_me_again));
		takePhotoAlert.getMessage().setGravity(Gravity.LEFT);
		takePhotoAlert.getTitle().setGravity(Gravity.LEFT);
		takePhotoAlert.show();

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == RESULT_OK) {
			switch (requestCode) {
			case CHAT_SELECT_PHOTO:
				String photoPath = data.getExtras().getString(
						PictureSelectActivity.SELECT_PICTURE_PATH);
				sendPrivatePhoto(photoPath);
				break;
			case RESULT_LOAD_IMAGE_CAPTURE:
				if (resultCode == RESULT_OK) {
					if ((takePhotoTempPath != null)
							&& (!takePhotoTempPath.equals(""))) {
						// 保存到相册
						ImageUtil.SaveImageToGallery(this, null, takePhotoTempPath,
								PictureHelper.getPhotoFileName(), null);
						// 增加附件
						sendPrivatePhoto(takePhotoTempPath);
					}
				}
				takePhotoTempPath = "";
				break;
			default:
				break;
			}
		}
	}

	/**
	 * 发送文本及普通表情
	 */
	private void sendTextMsg() {

		String content = etMessage.getText().toString();

		if (content.length() < 1) {
			shakeView(etMessage, true);
			return;
		}

		LCMessageItem item = mLiveChatManager.SendMessage(chatTarget.userId,
				content);
		// LCMessageItem item = new LCMessageItem();
		// item.sendType = SendType.Send;
		// LCTextItem textItem = new LCTextItem();
		// textItem.message = content;
		// textItem.illegal = false;
		// item.setTextItem(textItem);
		appendMsg(item);
		if (item != null) {
			/* 发送出去 */
			// if((item.getUserItem().chatType == ChatType.Invite)
			// ||(item.getUserItem().chatType == ChatType.Other)){
			// /*男端发起邀请，添加到联系人列表*/
			// ContactManager.getInstance().updateOrAddContact(targetId, true);
			// }
			ContactManager.getInstance().addOrUpdateContactBySendMsg(item);
			etMessage.setText("");
		}
	}

	/**
	 * 发送privatePhoto
	 * 
	 * @param photoPath
	 *            图片本地地址
	 */
	public void sendPrivatePhoto(String photoPath) {
		// Log.i("hunter", "sendPrivatePhoto photoPath: " + photoPath);
		if (!((chatTarget.chatType == ChatType.InChatCharge) || (chatTarget.chatType == ChatType.InChatUseTryTicket))) {
			/* 发送图片必须建立会话才能发送 */
			MaterialDialogAlert dialog = new MaterialDialogAlert(this);
			dialog.setMessage(getString(R.string.livechat_can_not_send_photo_before_the_conversation_has_started));
			dialog.addButton(dialog.createButton(
					getString(R.string.common_btn_ok), null));
			dialog.show();
			return;
		}

		LCMessageItem item = mLiveChatManager.SendPhoto(chatTarget.userId,
				photoPath);
		appendMsg(item);
		if (item != null) {
			ContactManager.getInstance().addOrUpdateContactBySendMsg(item);
		}
	}

	/**
	 * 发送语音
	 */
	public void sendVoiceItem(String savePath, long recordTime) {
		// Log.i("hunter", "sendVoiceItem savePath: " + savePath);
		if (!msgList.hasLadyInvited()) {
			// 判断是否有女士发来消息，否则不让发高表
			MaterialDialogAlert dialog = new MaterialDialogAlert(this);
			dialog.setMessage(getString(R.string.livechat_can_not_send_voice_message_before_the_conversation_has_started));
			dialog.addButton(dialog.createButton(
					getString(R.string.common_btn_ok), null));
			dialog.show();
			return;
		}
		
		if(recordTime < 1){
			//录音时长小于1秒，提示不发送
			Toast.makeText(this, getString(R.string.livechat_record_voice_too_short), Toast.LENGTH_SHORT).show();
			return;
		}

		LCMessageItem item = mLiveChatManager.SendVoice(chatTarget.userId,
				savePath, "aac", (int) recordTime);
		appendMsg(item);
		if (item != null) {
			ContactManager.getInstance().addOrUpdateContactBySendMsg(item);
		}
	}

	private void sendEmotionItem(String emotionId) {
		if (!msgList.hasLadyInvited()) {
			MaterialDialogAlert dialog = new MaterialDialogAlert(this);
			dialog.setMessage(getString(R.string.livechat_can_not_send_premium_sticker_before_the_conversation_has_started));
			dialog.addButton(dialog.createButton(
					getString(R.string.common_btn_ok), null));
			dialog.show();
			return;
		}

		LCMessageItem item = mLiveChatManager.SendEmotion(chatTarget.userId,
				emotionId);
		appendMsg(item);
		if (item != null) {
			ContactManager.getInstance().addOrUpdateContactBySendMsg(item);
		}
	}
	
	/**
	 * 发送小高级表情
	 * @param magicIconId
	 */
	public void sendMagicIcon(String magicIconId) {
		// Log.i("hunter", "sendPrivatePhoto photoPath: " + photoPath);
		if (!((chatTarget.chatType == ChatType.InChatCharge) || (chatTarget.chatType == ChatType.InChatUseTryTicket))) {
			/* 发送图片必须建立会话才能发送 */
			MaterialDialogAlert dialog = new MaterialDialogAlert(this);
			dialog.setMessage(getString(R.string.livechat_can_not_send_premium_sticker_before_the_conversation_has_started));
			dialog.addButton(dialog.createButton(
					getString(R.string.common_btn_ok), null));
			dialog.show();
			return;
		}

		LCMessageItem item = mLiveChatManager.SendMagicIcon(chatTarget.userId,
				magicIconId);
		appendMsg(item);
		if (item != null) {
			ContactManager.getInstance().addOrUpdateContactBySendMsg(item);
		}
	}

	public View appendMsg(LCMessageItem msgBean) {
		// 更新视图
		if (msgBean != null) {
			View rowView = msgList.addRow(msgBean);
			msgList.scrollToBottom(true);
			return rowView;
		} else {
			MaterialDialogAlert dialog = new MaterialDialogAlert(this);
			dialog.setMessage(getString(R.string.livechat_kickoff_by_sever_update));
			dialog.addButton(dialog.createButton(getString(R.string.login),
					new OnClickListener() {

						@Override
						public void onClick(View v) {
							/* 由于未登录成功等原因，底层认为异常返回空，跳去登陆处理,首先注销php登陆 */
							LoginManager.newInstance(ChatActivity.this).LogoutAndClean(false);
							
							Intent intent = new Intent(ChatActivity.this,
									HomeActivity.class);
							intent.putExtra(HomeActivity.NEED_RELOGIN_OPERATE,
									true);
							intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
							startActivity(intent);
						}
					}));
			dialog.addButton(dialog.createButton(
					getString(R.string.common_btn_cancel), null));

			dialog.show();
			return null;
		}
	}

	/**
	 * 显示消息列表
	 * 
	 * @param msgBeans
	 * @param smooth
	 */
	private void showMsgBeans(List<LCMessageItem> msgBeans, boolean smooth) {
		// // 邀请消息条数显示控制，只显示最新的2条信息
		// if (chatTarget != null && chatTarget.chatType == ChatType.Invite
		// && msgBeans.size() > 2) {
		// try {
		// msgBeans = msgBeans.subList(msgBeans.size() - 2,
		// msgBeans.size());
		// } catch (Exception e) {
		// e.printStackTrace();
		// }
		// }
		synchronized (msgBeans)
		{
			msgList.replaceAllRow(msgBeans);
		}
		msgList.scrollToBottom(smooth);
	}

	/**
	 * 检测是否可以使用试聊券
	 * 
	 * @param userId
	 */
	private void checkTrychat(String userId) {
		boolean success = mLiveChatManager.CheckCoupon(userId);
		if (!success) {
			/* 调用接口失败 */
			chatChargeNofity(null);
		}
	}

	/**
	 * 基础控件监听设置
	 */
	private TextWatcher edtInputWatcher = new TextWatcher() {
		@Override
		public void beforeTextChanged(CharSequence s, int start, int count,
				int after) {
			if (count > 0) {
                int end = start + count;
                Editable message = etMessage.getEditableText();
                ImageSpan[] list = message.getSpans(start, end, ImageSpan.class);

                for (ImageSpan span : list) {
                    // Get only the emoticons that are inside of the changed
                    // region.
                    int spanStart = message.getSpanStart(span);
                    int spanEnd = message.getSpanEnd(span);
                    if ((spanStart < end) && (spanEnd > start)) {
                        // Add to remove list
                    	synchronized(mEmoticonsToRemove){
                    		mEmoticonsToRemove.add(span);
                    	}
                    }
                }
            }
		}

		public void afterTextChanged(android.text.Editable s) {
			Editable message = etMessage.getEditableText();
			synchronized(mEmoticonsToRemove){
	            for (ImageSpan span : mEmoticonsToRemove) {
	                int start = message.getSpanStart(span);
	                int end = message.getSpanEnd(span);
	
	                // Remove the span
	                message.removeSpan(span);
	
	                // Remove the remaining emoticon text.
	                if (start != end) {
	                    message.delete(start, end);
	                }
	            }
	            mEmoticonsToRemove.clear();
			}
		};

		@Override
		public void onTextChanged(CharSequence s, int start, int before,
				int count) {
			// btnSend.setEnabled(etMessage.getText().length() > 0 ? true :
			// false);
		}
	};

	@SuppressWarnings("unchecked")
	@Override
	protected void handleUiMessage(Message msg) {
		// TODO Auto-generated method stub
		super.handleUiMessage(msg);
		switch (msg.what) {
		case TARGET_PHOTO_UPDATE:
			LadyDetail ladyDetailitem = (LadyDetail) msg.obj;
			tvName.setText(ladyDetailitem.firstname);
			String localPath = FileCacheManager.getInstance()
					.CacheImagePathFromUrl(ladyDetailitem.photoMinURL);
			new ImageViewLoader(this).DisplayImage(ivPhoto,
					ladyDetailitem.photoMinURL, localPath, null);
			break;
		case RECEIVE_CHAT_MESSAGE:
			LCMessageItem item = (LCMessageItem) msg.obj;
			/* 不是发给当前聊天且在联系人列表，更新未读条数 */
			if (!item.fromId.equals(targetId)
					&& (mContactManager.isMyContact(item.fromId))) {
				unreadCount++;
				tvUnread.setVisibility(View.VISIBLE);
				tvUnread.setText("" + unreadCount);
			}
			appendMsg(item);
			break;
		case RECEIVE_CHECK_SEND_MESSAGE_ERROR:{
			/* 底层判断无试聊券，无钱发送邀请失败处理 */
			LiveChatErrType errType = LiveChatErrType.values()[msg.arg1];
			List<LCMessageItem> msgCallbackList = (List<LCMessageItem>) msg.obj;
			if ((errType == LiveChatErrType.NoMoney)&&(containSelf(msgCallbackList, targetId))) {
				/* 无信用点提示充值 */
				final GetMoreCreditDialog dialog = new GetMoreCreditDialog(
						this, R.style.ChoosePhotoDialog);
				if(isCurrActivityVisible){
					dialog.show();
				}
			}
			for (LCMessageItem msgItem : msgCallbackList) {
				LiveChatCallBackItem livechatCallbackItem = new LiveChatCallBackItem(
						msg.arg1, "", "", msgItem);
				msgList.updateSendMessageCallback(livechatCallbackItem);
			}
		}break;
		case PHOTO_FEE_SUCCESS:
			/* 购买图片成功update列表数据并显示图片 */
			msgList.onPhotoFeeSuccess((LCMessageItem) msg.obj);
			break;
		case PRIVATE_SHOW_PHOTO_DOWNLOADED:
			/* 私密照大图下载完成更新列表显示 */
			msgList.onPrivatePhotoDownload((LCMessageItem) msg.obj);
			break;
		case SEND_MESSAGE_CALLBACK:
			/* 发送消息成功与否回调更新界面处理 */
			msgList.updateSendMessageCallback((LiveChatCallBackItem) msg.obj);
			break;

		case GET_HISTORY_MESSAGE_UPDATE:
			showMsgBeans(((LCUserItem) msg.obj).getMsgList(), true);
			break;
		case CHECK_COUPON_UPDATE:
			/* 检测试聊券成功返回处理 */
			chatChargeNofity((Coupon) msg.obj);
			break;
		case END_CHAT: {
			finish();
		}
			break;
		case REQUEST_ADD_FAVOUR_SUCCESS: {
			showToastDone("Added!");
		}
			break;
		case REQUEST_ADD_FAVOUR_FAIL: {
			// 收藏失败
			LiveChatCallBackItem obj = (LiveChatCallBackItem) msg.obj;
			showToastFailed("Fail!");
			Toast.makeText(this, obj.errMsg, Toast.LENGTH_LONG).show();
		}
			break;
		case VIDEO_FEE_SUCCESS: {
			// 购买video 回调
			RequestBaseResponse response = (RequestBaseResponse) msg.obj;
			if(response.isSuccess){
				//购买成功立即下载视频
				mLiveChatManager.GetVideo((LCMessageItem)response.body);
			}else{
				//购买失败处理没钱提示并停止处理中状态
//				if(response.errno.equals("ERROR00003")){
//					final GetMoreCreditDialog dialog = new GetMoreCreditDialog(this, R.style.ChoosePhotoDialog);
//					if(isCurrActivityVisible){
//						dialog.show();
//					}
//				}//修改聊天界面统一以消息的形式通知，屏蔽传统九宫格弹出
				msgList.updateVideoStatus((LCMessageItem)response.body);
			}
		}
			break;
		case VIDEO_THUMB_PHOTO_DOWNLOADED: {
			// video thumb photo下载成功处理
			ArrayList<LCMessageItem> obj = (ArrayList<LCMessageItem>) msg.obj;
			msgList.updateVideoThumbPhoto(obj);
		}
			break;
		case VIDEO_DOWNLOAD_STATUS_NOTIFY: {
			// video下载状态改变界面更新
			ArrayList<LCMessageItem> obj = (ArrayList<LCMessageItem>) msg.obj;
			msgList.updateVideoStatus(obj);
		}
			break;
		case GET_TARGET_STATUS_CALLBACK: {
			// video下载状态改变界面更新
			LCUserItem userItem = (LCUserItem) msg.obj;
			if((userItem != null) && (userItem.statusType != UserStatusType.USTATUS_ONLINE)){
				if(offlineDialog == null){
					offlineDialog = new MaterialDialogAlert(this);
					offlineDialog.setMessage(getString(R.string.send_error_lady_offline));
					offlineDialog.addButton(offlineDialog.createButton(getString(R.string.common_send_email), new OnClickListener() {
							@Override
							public void onClick(View v) {
								MailEditActivity.launchMailEditActivity(ChatActivity.this, targetId, ReplyType.DEFAULT, "", "");
							}
						}));
					offlineDialog.addButton(offlineDialog.createButton(getString(R.string.common_btn_cancel), null));
				}
				if(!offlineDialog.isShowing()){
					if(isCurrActivityVisible){
						offlineDialog.show();
					}
				}
			}
		}break;
		
		case LOAD_THEME_ONFINISH: {
			//加载主题成功
			Bitmap themeBg = (Bitmap)msg.obj;
			Log.i("hunter", "LOAD_THEME_ONFINISH themeBg: " + themeBg);
			if(themeBg != null){
				bg_img.setImageBitmap(themeBg);
				contentBody.setBackgroundColor(getResources().getColor(R.color.transparent_full));
				flChatArea.setBackgroundColor(getResources().getColor(R.color.transparent_full));
			}else{
				//提示用户重新加载
				String message = String.format(mContext.getString(R.string.livechat_theme_apply_failed_notify), getCurrentThemeTitle());
				String linkTxt = getString(R.string.common_btn_tapRetry);
				mLiveChatManager.BuildTSystemLinkMsg(targetId, message, linkTxt, SystemLinkOptType.Theme_reload, mCurrentTheme);
			}
		}
			break;
		case DOWNLOAD_THEME_SOURCE_CALLBACK: {
			//下载主题成功
			RequestBaseResponse response = (RequestBaseResponse)msg.obj;
			if(response.isSuccess){
				parseThemeConfig(mCurrentTheme.themeId);
			}else{
				//提示用户重新加载
				String message = String.format(mContext.getString(R.string.livechat_theme_apply_failed_notify), getCurrentThemeTitle());
				String linkTxt = getString(R.string.common_btn_tapRetry);
				mLiveChatManager.BuildTSystemLinkMsg(targetId, message, linkTxt, SystemLinkOptType.Theme_reload, mCurrentTheme);
			}
		}
			break;
		case RENEW_THEME_CALLBACK: {
			//重新购买主题回调
			LiveChatErrType errorType = LiveChatErrType.values()[msg.arg1];
			if(errorType == LiveChatErrType.Success){
				//购买成功功
				String message = String.format(mContext.getString(R.string.livechat_theme_renew_successful_notify), getCurrentThemeTitle());
				mLiveChatManager.BuildTSystemLinkMsg(targetId, message, "", SystemLinkOptType.Unknow, mCurrentTheme);
				loadTheme();
			}else if(errorType == LiveChatErrType.NoMoney){
				//提示充值
				ThemeItem themeItem = ThemeConfigManager.newInstance().getThemeItemByThemeId(mCurrentTheme.themeId);
				mLiveChatManager.BuildAndInsertNotifyMsg(targetId, NotifyType.Theme_nomoney, themeItem);
			}else if(errorType == LiveChatErrType.SubjectException){
				String message = String.format(mContext.getString(R.string.livechat_theme_renew_failed_noconversion), getCurrentThemeTitle());
				String linkTxt = getString(R.string.common_btn_tapRetry);
				mLiveChatManager.BuildTSystemLinkMsg(targetId, message, linkTxt, SystemLinkOptType.Theme_recharge, mCurrentTheme);
			}else{
				String message = String.format(mContext.getString(R.string.livechat_theme_renew_failed_notify), getCurrentThemeTitle());
				String linkTxt = getString(R.string.common_btn_tapRetry);
				mLiveChatManager.BuildTSystemLinkMsg(targetId, message, linkTxt, SystemLinkOptType.Theme_recharge, mCurrentTheme);
			}
		}
			break;
		case RECOMMAND_THEME_CALLBACK: {
			//女士推荐男士主题
			String themeId = (String)msg.obj;
			ThemeItem themeItem = ThemeConfigManager.newInstance().getThemeItemByThemeId(themeId);
			if(themeItem != null){
				mLiveChatManager.BuildAndInsertNotifyMsg(targetId, NotifyType.Theme_recommand, themeItem);
			}
		}
			break;
		case RECIEVE_THEME_PLAY_REQUEST: {
			//女士通知男士端播放动画
			if( (mCurrentTheme != null) && isActivityVisible()){
				playCurrentThemeAnimation();
			}
		}
			break;
		default:
			break;
		}
	}
	
	/**
	 * 是否包括当前用户
	 * @param msgList
	 * @param id
	 * @return
	 */
	private boolean containSelf(List<LCMessageItem> msgList, String id){
		boolean containSelf = false;
		if((msgList != null) && (msgList.size() > 0)){
			for(LCMessageItem item : msgList){
				if((item != null)&&(item.getUserItem() != null)){
					String usrId = item.getUserItem().userId;
					if(usrId.equals(id)){
						containSelf = true;
						break;
					}
				}
			}
		}
		return containSelf;
	}

	/**
	 * 邀请或者other状态时，提示资费情况或使用试聊券
	 * 
	 * @param coupon
	 */
	private void chatChargeNofity(Coupon coupon) {
		LCMessageItem message = new LCMessageItem();
		message.sendType = SendType.Send;
		if (coupon == null) {
			/* 获取试聊券失败，提示资费信息 */
			LCSystemItem systemItem = new LCSystemItem();
			systemItem.message = getString(R.string.livechat_charge_terms);
			message.setSystemItem(systemItem);
		} else {
			if (coupon.status == CouponStatus.Yes) {
				message.msgType = MessageType.Custom;
			} else {
				/* 获取试聊券成功，但无试聊券，提示资费信息 */
				LCSystemItem systemItem = new LCSystemItem();
				systemItem.message = getString(R.string.livechat_charge_terms);
				message.setSystemItem(systemItem);
			}
		}
		appendMsg(message);
	}

	/**
	 * 选择表情
	 * 
	 * @param val
	 */
	@SuppressWarnings("deprecation")
	public void selectEmotion(int val) {
		int imgId = 0;
		try {
			imgId = R.drawable.class.getDeclaredField("e" + val).getInt(null);
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (imgId != 0) {
			int textSize = getResources().getDimensionPixelSize(
					R.dimen.expre_drawable_size);
			Drawable drawable = getResources().getDrawable(imgId);
			drawable.setBounds(0, 0, textSize, textSize);
			ImageSpan imgSpan = new ImageSpan(drawable, ImageSpan.ALIGN_BOTTOM);
			String code = "[img:" + val + "]";
			SpannableString ss = new SpannableString(code);
			ss.setSpan(imgSpan, 0, code.length(),
					Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
			etMessage.getEditableText().insert(etMessage.getSelectionStart(),
					ss);
		}
	}

	/**
	 * 获取聊天历史返回，更新消息列表
	 * 
	 * @param userItem
	 */
	private void onGetHistoryMessageCallback(LCUserItem userItem) {
		if ((userItem != null) && (userItem.userId != null)
				&& (userItem.userId.equals(targetId))) {
			/* 当前用户的消息历史返回才处理 */
			Message msg = Message.obtain();
			msg.what = GET_HISTORY_MESSAGE_UPDATE;
			msg.obj = userItem;
			sendUiMessage(msg);
		}
	}

	/**
	 * 发送消息成功与否回调
	 * 
	 * @param errType
	 * @param item
	 */
	private void onSendMessageUpdate(LiveChatCallBackItem item) {
		Message msg = Message.obtain();
		msg.what = SEND_MESSAGE_CALLBACK;
		msg.obj = item;
		sendUiMessage(msg);
	}

	/**
	 * 收到聊天信息回调主界面处理
	 * 
	 * @param item
	 */
	private void onReceiveMsgUpdate(LCMessageItem item) {
		Message msg = Message.obtain();
		msg.what = RECEIVE_CHAT_MESSAGE;
		msg.obj = item;
		sendUiMessage(msg);
	}

	/**
	 * 底层检测试聊有钱与否，返回出错信息列表处理
	 * 
	 * @param errType
	 * @param msgList
	 */
	private void onReceiveMsgList(LiveChatErrType errType,
			ArrayList<LCMessageItem> msgList) {
		Message msg = Message.obtain();
		msg.what = RECEIVE_CHECK_SEND_MESSAGE_ERROR;
		msg.arg1 = errType.ordinal();
		msg.obj = msgList;
		sendUiMessage(msg);
	}

	/**
	 * 试聊券查询
	 * 
	 * @param item
	 */
	private void onCheckCouponCallback(Coupon item) {
		Message msg = Message.obtain();
		msg.what = CHECK_COUPON_UPDATE;
		msg.obj = item;
		sendUiMessage(msg);
	}

	/**
	 * Livechat 购买私密照成功
	 * 
	 * @param item
	 */
	private void onGetPhotoFeeSuccess(LCMessageItem item) {
		Message msg = Message.obtain();
		msg.what = PHOTO_FEE_SUCCESS;
		msg.obj = item;
		sendUiMessage(msg);
	}

	/**
	 * Livechat 私密照下大图成功，更新列表
	 * 
	 * @param item
	 */
	private void onGetShowPhotoSuccess(LCMessageItem item) {
		Message msg = Message.obtain();
		msg.what = PRIVATE_SHOW_PHOTO_DOWNLOADED;
		msg.obj = item;
		sendUiMessage(msg);
	}

	/**
	 * 购买Video返回
	 * 
	 * @param success
	 * @param errno
	 * @param errmsg
	 * @param item
	 */
	private void onVideoFeeCallback(boolean success, String errno,
			String errmsg, LCMessageItem item) {
		Message msg = Message.obtain();
		msg.what = VIDEO_FEE_SUCCESS;
		RequestBaseResponse requestBaseResponse = new RequestBaseResponse(
				success, errno, errmsg, item);
		msg.obj = requestBaseResponse;
		sendUiMessage(msg);
	}

	/**
	 * 下载video thumb 图片成功回调刷新
	 * 
	 * @param msgList
	 */
	private void onGetVideoThumbPhoto(ArrayList<LCMessageItem> msgList) {
		Message msg = Message.obtain();
		msg.what = VIDEO_THUMB_PHOTO_DOWNLOADED;
		msg.obj = msgList;
		sendUiMessage(msg);
	}

	/**
	 * 下载video 回调更新
	 * 
	 * @param msgList
	 */
	private void onGetVideoCallback(ArrayList<LCMessageItem> msgList) {
		Message msg = Message.obtain();
		msg.what = VIDEO_DOWNLOAD_STATUS_NOTIFY;
		msg.obj = msgList;
		sendUiMessage(msg);
	}

	/**
	 * LiveChatManger 回调管理
	 */
	@Override
	public void OnSendMessage(LiveChatErrType errType, String errmsg,
			LCMessageItem item) {
		if ((item != null) && (item.getUserItem() != null)
				&& (item.getUserItem().userId != null)
				&& (item.getUserItem().userId.equals(targetId))) {
			LiveChatCallBackItem callBack = new LiveChatCallBackItem(
					errType.ordinal(), null, errmsg, item);
			onSendMessageUpdate(callBack);
		}
	}

	@Override
	public void OnRecvMessage(LCMessageItem item) {
		if (chatTarget != null && item != null) {
			if (item.fromId.equals(chatTarget.userId)) {
				onReceiveMsgUpdate(item);
			}
		}
	}

	@Override
	public void OnRecvWarning(LCMessageItem item) {
		/* 普通warning及以下错误（余额不足等） */
		if (item.getUserItem().userId.equals(chatTarget.userId)) {
			onReceiveMsgUpdate(item);
		}
	}

	@Override
	public void OnRecvSystemMsg(LCMessageItem item) {
		/* 系统通知，以消息的形式（试聊结束等） */
		if (item.fromId.equals(chatTarget.userId)) {
			onReceiveMsgUpdate(item);
		}
	}

	@Override
	public void OnRecvEditMsg(String fromId) {
		// TODO Auto-generated method stub

	}

	@Override
	public void OnSendVoice(LiveChatErrType errType, String errno,
			String errmsg, LCMessageItem item) {
		if ((item != null) && (item.getUserItem() != null)
				&& (item.getUserItem().userId != null)
				&& (item.getUserItem().userId.equals(targetId))) {
			LiveChatCallBackItem callBack = new LiveChatCallBackItem(
					errType.ordinal(), errno, errmsg, item);
			onSendMessageUpdate(callBack);
		}
	}

	@Override
	public void OnGetVoice(LiveChatErrType errType, String errmsg,
			LCMessageItem item) {

	}

	@Override
	public void OnRecvVoice(LCMessageItem item) {
		if (item.fromId.equals(chatTarget.userId)) {
			onReceiveMsgUpdate(item);
		}
	}

	@Override
	public void OnUseTryTicket(LiveChatErrType errType, String errno,
			String errmsg, String userId, TryTicketEventType eventType) {
		// TODO Auto-generated method stub

	}

	@Override
	public void OnRecvTryTalkBegin(LCUserItem userItem, int time) {
		// TODO Auto-generated method stub

	}

	@Override
	public void OnRecvTryTalkEnd(LCUserItem userItem) {
		// TODO Auto-generated method stub

	}

	@Override
	public void OnCheckCoupon(boolean success, String errno, String errmsg,
			Coupon item) {
		if (item != null && item.userId.equals(targetId)) {
			onCheckCouponCallback(item);
		}
	}

	@Override
	public void OnEndTalk(LiveChatErrType errType, String errmsg,
			LCUserItem userItem) {
		if ((userItem != null) && (userItem.userId != null)
				&& (userItem.userId.equals(targetId))) {
			Message msg = Message.obtain();
			msg.what = END_CHAT;
			sendUiMessage(msg);
		}
	}

	@Override
	public void OnRecvTalkEvent(LCUserItem item) {
		// TODO Auto-generated method stub

	}

	@Override
	public void OnSendPhoto(LiveChatErrType errType, String errno,
			String errmsg, LCMessageItem item) {
		if ((item != null) && (item.getUserItem() != null)
				&& (item.getUserItem().userId != null)
				&& (item.getUserItem().userId.equals(targetId))) {
			LiveChatCallBackItem callBack = new LiveChatCallBackItem(
					errType.ordinal(), errno, errmsg, item);
			onSendMessageUpdate(callBack);
		}
	}

	@Override
	public void OnPhotoFee(boolean success, String errno, String errmsg,
			LCMessageItem item) {
		/* 购买图片成功，更新item属性 */
		if (success) {
			if ((item != null) && (item.getUserItem() != null)
					&& (item.getUserItem().userId != null)
					&& (item.getUserItem().userId.equals(targetId))) {
				onGetPhotoFeeSuccess(item);
			}
		}
	}

	@Override
	public void OnGetPhoto(LiveChatErrType errType, String errno,
			String errmsg, LCMessageItem item) {
		if (errType == LiveChatErrType.Success) {
			/* 在购买私密照界面购买清晰图成功，回调更新界面 */
			if (!isCurrActivityVisible && item.getPhotoItem().charge) {
				if ((item != null) && (item.getUserItem() != null)
						&& (item.getUserItem().userId != null)
						&& (item.getUserItem().userId.equals(targetId))) {
					onGetShowPhotoSuccess(item);
				}
			}
		}
	}

	@Override
	public void OnRecvPhoto(LCMessageItem item) {
		if (item.fromId.equals(chatTarget.userId)) {
			onReceiveMsgUpdate(item);
		}
	}

	@Override
	public void OnGetEmotionConfig(boolean success, String errno,
			String errmsg, OtherEmotionConfigItem item) {
		// TODO Auto-generated method stub

	}

	@Override
	public void OnSendEmotion(LiveChatErrType errType, String errmsg,
			LCMessageItem item) {
		if ((item != null) && (item.getUserItem() != null)
				&& (item.getUserItem().userId != null)
				&& (item.getUserItem().userId.equals(targetId))) {
			LiveChatCallBackItem callBack = new LiveChatCallBackItem(
					errType.ordinal(), null, errmsg, item);
			onSendMessageUpdate(callBack);
		}
	}

	@Override
	public void OnRecvEmotion(LCMessageItem item) {
		if (item.fromId.equals(chatTarget.userId)) {
			onReceiveMsgUpdate(item);
		}
	}

	@Override
	public void OnGetEmotionImage(boolean success, LCEmotionItem emotionItem) {
	}

	@Override
	public void OnGetEmotionPlayImage(boolean success, LCEmotionItem emotionItem) {
	}

	@Override
	public void OnGetEmotion3gp(boolean success, LCEmotionItem emotionItem) {
		// TODO Auto-generated method stub

	}

	@Override
	public void OnLogin(LiveChatErrType errType, String errmsg,
			boolean isAutoLogin) {
		// TODO Auto-generated method stub

	}

	@Override
	public void OnLogout(LiveChatErrType errType, String errmsg,
			boolean isAutoLogin) {

	}

	@Override
	public void OnGetTalkList(LiveChatErrType errType, String errmsg) {
		// TODO Auto-generated method stub

	}

	@Override
	public void OnGetHistoryMessage(boolean success, String errno,
			String errmsg, LCUserItem userItem) {
		/* 拿历史消息返回，需更新消息列表 */
		if (success) {
			onGetHistoryMessageCallback(userItem);
		}
	}

	@Override
	public void OnGetUsersHistoryMessage(boolean success, String errno,
			String errmsg, LCUserItem[] userItems) {
		// TODO Auto-generated method stub
		if (success) {
			for (LCUserItem userItem : userItems) {
				onGetHistoryMessageCallback(userItem);
			}
		}
	}

	@Override
	public void OnSetStatus(LiveChatErrType errType, String errmsg) {
		// TODO Auto-generated method stub

	}

	@Override
	public void OnGetUserStatus(LiveChatErrType errType, String errmsg,
			LCUserItem[] userList) {
		if(errType == LiveChatErrType.Success && userList != null && !onlineChecked){
			for(LCUserItem item : userList){
				if(item.userId.equals(targetId)){
					onlineChecked = true;
					Message msg = Message.obtain();
					msg.what = GET_TARGET_STATUS_CALLBACK;
					msg.obj = item;
					sendUiMessage(msg);
					break;
				}
			}
		}
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

	}

	/**
	 * 默认读取配置中虚拟键盘高度，如果不等于-1，表示已记录不在更新
	 * 
	 * @return
	 */
	public int getKeyboardHeight() {
		SharedPreferences preference = getSharedPreferences("virtualKeyboard",
				MODE_PRIVATE);
		return preference.getInt("keyboardheight", -1);
	}

	/**
	 * 提交保存键盘高度
	 * 
	 * @param height
	 */
	private void saveKeyboardHeight(int height) {
		SharedPreferences preference = getSharedPreferences("virtualKeyboard",
				MODE_PRIVATE);
		Editor editor = preference.edit();
		editor.putInt("keyboardheight", height);
		editor.commit();
	}

	private enum MenuBtnType {
		NORMAL_EXPRESSION, SELECT_PHOTO, RECORD_VOICE, EMOTION_PAN, USE_CAMERA
	}

	/**
	 * 当开始录音或回收时，关掉所有录音
	 */
	public void stopAllVoicePlaying() {
		if (msgList != null) {
			msgList.stopPlaying();
		}
	}

	/**
	 * 用于Online lady 点击进入（因为无minPhotoUrl，需用Id获取显示）
	 * 
	 * @param womanid
	 */
	private void QueryLadyDetail(String womanid) {
		LadyDetailManager.getInstance().QueryLadyDetail(womanid,
				new OnLadyDetailManagerQueryLadyDetailCallback() {

					@Override
					public void OnQueryLadyDetailCallback(boolean isSuccess,
							String errno, String errmsg, LadyDetail item) {
						// TODO Auto-generated method stub
						if (isSuccess) {
							Message msg = Message.obtain();
							msg.what = TARGET_PHOTO_UPDATE;
							msg.obj = item;
							sendUiMessage(msg);
						}
					}
				});
	}

	/**
	 * 请求收藏女士
	 */
	public void AddFavour(String womanid) {
		showToastProgressing("Adding");
		RequestOperator.getInstance().AddFavouritesLady(womanid,
				new OnRequestCallback() {

					@Override
					public void OnRequest(boolean isSuccess, String errno,
							String errmsg) {
						// TODO Auto-generated method stub
						Message msg = Message.obtain();
						LiveChatCallBackItem obj = new LiveChatCallBackItem();
						obj.errMsg = errmsg;
						obj.errNo = errno;
						if (isSuccess) {
							// 获取个人信息成功
							msg.what = REQUEST_ADD_FAVOUR_SUCCESS;
						} else {
							// 获取个人信息失败
							msg.what = REQUEST_ADD_FAVOUR_FAIL;
						}
						msg.obj = obj;
						sendUiMessage(msg);
					}
				});
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && isMenuOpen) {
			/* 拦截返回键事件，菜单打开时优先关闭menu */
			closeSubMenu();
			return true;
		}
		/* 解决onDestroy调用慢，导致在聊未读状态错误问题 */
		mContactManager.mWomanId = "";
		return super.onKeyDown(keyCode, event);
	}

	private OnTouchListener onMessageListTouchListener = new OnTouchListener() {

		@Override
		public boolean onTouch(View v, MotionEvent event) {
			// TODO Auto-generated method stub
			if (event.getAction() == MotionEvent.ACTION_UP) {
				closeSubMenu();
			}
			return false;
		}
	};

	/**
	 * 关闭子菜单
	 */
	private void closeSubMenu() {
		isMenuOpen = false;
		hideSoftInput();
		LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) flBottom
				.getLayoutParams();
		params.height = 0;
		flBottom.setLayoutParams(params);

		Fragment fragment = getSupportFragmentManager().findFragmentById(
				R.id.flPane);
		if (fragment != null) {
			FragmentTransaction transaction = getSupportFragmentManager()
					.beginTransaction();
			transaction.remove(fragment);
			transaction.commitAllowingStateLoss();
		}

		/* 还原按钮未选中图标 */
		resetMenuButtonBg();
	}

	/**
	 * 功能按钮切换的过程中，还原按钮图标状态
	 */
	private void resetMenuButtonBg() {
		if (photosendEnable) {
			btnSelectPhoto.setImageResource(R.drawable.ic_photo_grey600_24dp);
			btnTakePhoto.setImageResource(R.drawable.ic_photo_camera_grey600_24dp);
		}
		btnExpression.setImageResource(R.drawable.ic_tag_faces_grey600_24dp);
		btnVoice.setImageResource(R.drawable.ic_mic_grey600_24dp);
		btnEmotion.setImageResource(R.drawable.ic_premium_emotion_24dp);
	}
	
	// 主题购物车
	@Override
	public void onShopCarClick(ButtonFloat button) {
		Intent intent = new Intent(ChatActivity.this,ThemeMainActivity.class);
		intent.putExtra("womanId", targetId);
		startActivity(intent);
	}

	// 主题播放动画
	@Override
	public void onPlayClick(ButtonFloat button) {
		// TODO Auto-generated method stub
		playCurrentThemeAnimation();
	}
	
	/**
	 * 播放当前主题动画
	 */
	@SuppressLint("NewApi")
	public void playCurrentThemeAnimation() {
		if (mConfigItem != null) {
			if(themePreviewer != null && (themePreviewer.isShowing() &&
					themePreviewer.isPlaying())){
				themePreviewer.resetPlay();
				themePreviewer.play();
			}else{
				themePreviewer = new ThemePreviewer(mContext, mConfigItem);
				themePreviewer.setRepeat(mConfigItem.mMotionRepeat);
				themePreviewer.setFrame(mConfigItem.mMotionFrame);
				themePreviewer.setContentGravity(mConfigItem.mMotionLoca);
				int height = SystemUtil.getDisplayMetrics(this).heightPixels - UnitConversion.dip2px(this, 56 + 92) - getStatusHeight(this);//状态栏高度25
				themePreviewer.setHeight(height);
				themePreviewer.showAtLocation(getCurrentFocus(), Gravity.TOP, 0,llHead.getHeight()+getStatusHeight(this));
				themePreviewer.play();
				if(mCurrentTheme != null){
					mLiveChatManager.PlayThemeMotion(targetId, mCurrentTheme.themeId);
				}
			}
		}
	}
	/**
	 * 获得状态栏的高度
	 * 
	 * @param context
	 * @return
	 */
	public static int getStatusHeight(Context context) {

		int statusHeight = UnitConversion.dip2px(context, 25);
		try {
			Class clazz = Class.forName("com.android.internal.R$dimen");
			Object object = clazz.newInstance();
			int height = Integer.parseInt(clazz.getField("status_bar_height").get(object).toString());
			statusHeight = context.getResources().getDimensionPixelSize(height);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return statusHeight;
	}

	@Override
	public void OnSendMessageListFail(LiveChatErrType errType,
			ArrayList<LCMessageItem> msgList) {
		/* 底层检测是否有试聊券，是否有钱聊天，如果没有直接此处返回错误 */
		onReceiveMsgList(errType, msgList);
	}

	@Override
	public void OnGetVideoPhoto(LiveChatErrType errType, String errno,
			String errmsg, String userId, String inviteId, String videoId,
			VideoPhotoType type, String filePath,
			ArrayList<LCMessageItem> msgList) {
		if ((errType == LiveChatErrType.Success)
				&& (!StringUtil.isEmpty(userId)) && (userId.equals(targetId))
				&& (msgList != null) && (msgList.size() > 0)) {
			onGetVideoThumbPhoto(msgList);
		}
	}

	@Override
	public void OnVideoFee(boolean success, String errno, String errmsg,
			LCMessageItem item) {
		if ((item != null) && (item.getUserItem() != null)
				&& (!StringUtil.isEmpty(item.getUserItem().userId))
				&& (item.getUserItem().userId.equals(targetId))) {
			onVideoFeeCallback(success, errno, errmsg, item);
		}
	}

	@Override
	public void OnStartGetVideo(String userId, String videoId, String inviteId,
			String videoPath, ArrayList<LCMessageItem> msgList) {
		if ((!StringUtil.isEmpty(userId)) && (userId.equals(targetId))
				&& (msgList != null) && (msgList.size() > 0)) {
			onGetVideoCallback(msgList);
		}
	}

	@Override
	public void OnGetVideo(LiveChatErrType errType, String userId,
			String videoId, String inviteId, String videoPath,
			ArrayList<LCMessageItem> msgList) {
		if ((!StringUtil.isEmpty(userId)) && (userId.equals(targetId))
				&& (msgList != null) && (msgList.size() > 0)) {
			onGetVideoCallback(msgList);
		}
	}

	@Override
	public void OnRecvVideo(LCMessageItem item) {
		if (chatTarget != null && item != null) {
			if (item.fromId.equals(chatTarget.userId)) {
				onReceiveMsgUpdate(item);
			}
		}
	}

	@Override
	public void InitView() {
		// TODO Auto-generated method stub
		
	}
	
	/*
	 * ======================= LiveChatManagerMagicIconListener
	 * ===================
	 */

	@Override
	public void OnGetMagicIconConfig(boolean success, String errno,
			String errmsg, MagicIconConfig item) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void OnSendMagicIcon(LiveChatErrType errType, String errmsg,
			LCMessageItem item) {
		if ((item != null) && (item.getUserItem() != null)
				&& (item.getUserItem().userId != null)
				&& (item.getUserItem().userId.equals(targetId))) {
			LiveChatCallBackItem callBack = new LiveChatCallBackItem(
					errType.ordinal(), null, errmsg, item);
			onSendMessageUpdate(callBack);
		}		
	}

	@Override
	public void OnRecvMagicIcon(LCMessageItem item) {
		if (item.fromId.equals(chatTarget.userId)) {
			onReceiveMsgUpdate(item);
		}		
	}

	@Override
	public void OnGetMagicIconSrcImage(boolean success,
			LCMagicIconItem magicIconItem) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void OnGetMagicIconThumbImage(boolean success,
			LCMagicIconItem magicIconItem) {
		// TODO Auto-generated method stub
		
	}

	/*************************** Theme Callback *************************************/
	@Override
	public void OnGetPaidTheme(LiveChatErrType errType, String errmsg,
			String userId, LCPaidThemeInfo[] paidThemeList) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void OnGetAllPaidTheme(boolean isSuccess, String errmsg, LCPaidThemeInfo[] paidThemeList, 
			ThemeItem[] themeList) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void OnManFeeTheme(LiveChatErrType errType, String womanId, String themeId, String errmsg,
			LCPaidThemeInfo paidThemeInfo) {
		if(targetId.equals(womanId)&&
				(mCurrentTheme!= null && (mCurrentTheme.themeId.equals(themeId)))){
			//当前购买成功
			Message msg = Message.obtain();
			msg.what = RENEW_THEME_CALLBACK;
			msg.arg1 = Integer.valueOf(errType.ordinal());
			sendUiMessage(msg);
		} 
	}

	@Override
	public void OnManApplyTheme(LiveChatErrType errType, String womanId, String themeId, String errmsg,
			LCPaidThemeInfo paidThemeInfo) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void OnPlayThemeMotion(LiveChatErrType errType, String errmsg,
			String womanId, String themeId) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void OnRecvThemeMotion(String themeId, String manId, String womanId) {
		// TODO Auto-generated method stub
		if(mCurrentTheme != null){
			if(womanId.equals(targetId)&&(mCurrentTheme.themeId.equals(themeId))){
				Message msg = Message.obtain();
				msg.what = RECIEVE_THEME_PLAY_REQUEST;
				sendUiMessage(msg);
			}
		}
		
	}

	@Override
	public void OnRecvThemeRecommend(String themeId, String manId,
			String womanId) {
		if(womanId.equals(targetId)
				&& isThemeEnable){
			Message msg = Message.obtain();
			msg.what = RECOMMAND_THEME_CALLBACK;
			msg.obj = themeId;
			sendUiMessage(msg);
		}
	}

	@Override
	public void onThemeDownloadUpdate(String themeId, int progress) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onThemeDownloadFinish(boolean isSuccess, String themeId,
			String sourceDir) {
		Log.i("hunter", "onThemeDownloadFinish isSuccess: " + isSuccess + "  themeId: " + themeId + " sourceDir:" + sourceDir);
		if(mCurrentTheme != null && mCurrentTheme.themeId.equals(themeId)){
			Message msg = Message.obtain();
			msg.what = DOWNLOAD_THEME_SOURCE_CALLBACK;
			RequestBaseResponse response = new RequestBaseResponse(isSuccess, "", "", sourceDir);
			msg.obj = response;
			sendUiMessage(msg);
		}
	}

}
