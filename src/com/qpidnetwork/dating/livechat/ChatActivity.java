package com.qpidnetwork.dating.livechat;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
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
import android.text.SpannableString;
import android.text.Spanned;
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
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.qpidnetwork.dating.R;
import com.qpidnetwork.dating.authorization.LoginManager;
import com.qpidnetwork.dating.authorization.LoginParam;
import com.qpidnetwork.dating.authorization.LoginPerfence;
import com.qpidnetwork.dating.bean.RequestBaseResponse;
import com.qpidnetwork.dating.contacts.ContactManager;
import com.qpidnetwork.dating.home.HomeActivity;
import com.qpidnetwork.dating.lady.LadyDetailActivity;
import com.qpidnetwork.dating.lady.LadyDetailManager;
import com.qpidnetwork.dating.lady.LadyDetailManager.OnLadyDetailManagerQueryLadyDetailCallback;
import com.qpidnetwork.dating.livechat.expression.EmotionMainFragment;
import com.qpidnetwork.dating.livechat.normalexp.NormalExprssionFragment;
import com.qpidnetwork.dating.livechat.picture.PictureHelper;
import com.qpidnetwork.dating.livechat.picture.PictureSelectActivity;
import com.qpidnetwork.dating.livechat.picture.PictureSelectFragment;
import com.qpidnetwork.dating.livechat.video.VideoHistoryListActivity;
import com.qpidnetwork.dating.livechat.voice.VoiceRecordFragment;
import com.qpidnetwork.framework.base.BaseFragmentActivity;
import com.qpidnetwork.framework.util.ImageUtil;
import com.qpidnetwork.framework.util.StringUtil;
import com.qpidnetwork.framework.util.UnitConversion;
import com.qpidnetwork.framework.widget.CircleImageView;
import com.qpidnetwork.livechat.LCEmotionItem;
import com.qpidnetwork.livechat.LCMessageItem;
import com.qpidnetwork.livechat.LCMessageItem.MessageType;
import com.qpidnetwork.livechat.LCMessageItem.SendType;
import com.qpidnetwork.livechat.LCSystemItem;
import com.qpidnetwork.livechat.LCUserItem;
import com.qpidnetwork.livechat.LCUserItem.ChatType;
import com.qpidnetwork.livechat.LiveChatManager;
import com.qpidnetwork.livechat.LiveChatManagerEmotionListener;
import com.qpidnetwork.livechat.LiveChatManagerMessageListener;
import com.qpidnetwork.livechat.LiveChatManagerOtherListener;
import com.qpidnetwork.livechat.LiveChatManagerPhotoListener;
import com.qpidnetwork.livechat.LiveChatManagerTryTicketListener;
import com.qpidnetwork.livechat.LiveChatManagerVideoListener;
import com.qpidnetwork.livechat.LiveChatManagerVoiceListener;
import com.qpidnetwork.livechat.jni.LiveChatClientListener.KickOfflineType;
import com.qpidnetwork.livechat.jni.LiveChatClientListener.LiveChatErrType;
import com.qpidnetwork.livechat.jni.LiveChatClientListener.TalkEmfNoticeType;
import com.qpidnetwork.livechat.jni.LiveChatClientListener.TryTicketEventType;
import com.qpidnetwork.manager.FileCacheManager;
import com.qpidnetwork.request.OnRequestCallback;
import com.qpidnetwork.request.RequestJniLiveChat.VideoPhotoType;
import com.qpidnetwork.request.RequestOperator;
import com.qpidnetwork.request.item.Coupon;
import com.qpidnetwork.request.item.Coupon.CouponStatus;
import com.qpidnetwork.request.item.LadyDetail;
import com.qpidnetwork.request.item.OtherEmotionConfigItem;
import com.qpidnetwork.tool.ImageViewLoader;
import com.qpidnetwork.view.GetMoreCreditDialog;
import com.qpidnetwork.view.MaterialDialogAlert;
import com.qpidnetwork.view.MaterialDropDownMenu;
import com.qpidnetwork.view.MaterialThreeButtonDialog;

public class ChatActivity extends BaseFragmentActivity implements
		LiveChatManagerMessageListener, LiveChatManagerEmotionListener,
		LiveChatManagerPhotoListener, LiveChatManagerTryTicketListener,
		LiveChatManagerVoiceListener, LiveChatManagerOtherListener,
		LiveChatManagerVideoListener {

	public static final String SEND_VOICE_ACTION = "livechat.sendvoice";
	public static final String SEND_EMTOTION_ACTION = "livechat.sendemotion";
	public static final String EMOTION_ID = "emotion_id";

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

	// fragments
	private NormalExprssionFragment normalExprssionFragment;
	private PictureSelectFragment pictureSelectFragment;
	private VoiceRecordFragment voiceRecordFragment;
	private EmotionMainFragment emotionMainFragment;
	private CameraViewFragment cameraViewFragment;

	/* title */
	private LinearLayout llBack;
	private CircleImageView ivPhoto;
	private TextView tvUnread;
	private TextView tvName;
	private ImageButton btnMore;
	private MaterialDropDownMenu dropDown;

	private EditText etMessage;
	private ImageButton btnExpression;
	private ImageButton btnSend;
	private MessageListView msgList;

	private ImageButton btnTakePhoto;
	private ImageButton btnSelectPhoto;
	private ImageButton btnVoice;
	private ImageButton btnEmotion;

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

	private boolean isCurrActivityVisible = true;// 简单判断当前Activity是否可见，用于下载私密照返回处理是否更新

	private boolean photosendEnable = true; // 风控模块，判断是否可以发送私密照，以便控制按钮响应及显示

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
		
		mLiveChatManager = LiveChatManager.newInstance(null);
		initViews();
		initData();
		initLivechatConfig();
		initReceive();
		initKeyboardDetect();

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

	}

	private void initViews() {

		/* title */
		llBack = (LinearLayout) findViewById(R.id.llBack);
		llBack.setOnClickListener(this);
		tvUnread = (TextView) findViewById(R.id.tvUnread);
		ivPhoto = (CircleImageView) findViewById(R.id.ivPhoto);
		tvName = (TextView) findViewById(R.id.tvName);
		btnMore = (ImageButton) findViewById(R.id.btnMore);

		msgList = (MessageListView) findViewById(R.id.msgList);
		msgList.setOnTouchListener(onMessageListTouchListener);

		/* 文字信息编辑 */
		etMessage = (EditText) findViewById(R.id.etMessage);
		etMessage.addTextChangedListener(edtInputWatcher);
		etMessage.setOnTouchListener(edtInputTouch);

		btnExpression = (ImageButton) findViewById(R.id.btnExpression);
		btnExpression.setOnClickListener(this);
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
		btnTakePhoto = (ImageButton) findViewById(R.id.btnTakePhoto);
		btnSelectPhoto = (ImageButton) findViewById(R.id.btnSelectPhoto);
		btnVoice = (ImageButton) findViewById(R.id.btnVoice);
		btnEmotion = (ImageButton) findViewById(R.id.btnEmotion);
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
		btnVoice.setOnClickListener(this);
		btnEmotion.setOnClickListener(this);
		btnMore.setOnClickListener(this);
		ivPhoto.setOnClickListener(this);

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
				if (chatTarget.getMsgList().size() > 0) {
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
		}
	}

	/**
	 * 设置监控软键盘弹出时，计算软键盘高度，以此确定子view高度
	 */
	private void initKeyboardDetect() {
		final LinearLayout llRoot = (LinearLayout) findViewById(R.id.llRoot);
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
				}
			}
		};
		IntentFilter filter = new IntentFilter();
		filter.addAction(SEND_VOICE_ACTION);
		filter.addAction(SEND_EMTOTION_ACTION);
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
		/* 当前界面不可见时，置空当前联系人，可接收push推送 */
		mContactManager.mWomanId = "";
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

		/* 清除正在聊天对象 */
		mContactManager.mWomanId = "";
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
			break;
		case R.id.btnTakePhoto:
			
			//Android api 11 or above will use in app camera. otherwise will open system camera.
			if (Build.VERSION.SDK_INT < 11){
				takePhoto();
			}else{
				onMenuButtonClick(MenuBtnType.USE_CAMERA);
			}
			
			break;
		case R.id.btnSelectPhoto:
			onMenuButtonClick(MenuBtnType.SELECT_PHOTO);
			break;
		case R.id.btnVoice:
			onMenuButtonClick(MenuBtnType.RECORD_VOICE);
			break;
		case R.id.btnEmotion:
			onMenuButtonClick(MenuBtnType.EMOTION_PAN);
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

			if (btnExpression.getTag() == null
					|| btnExpression.getTag().equals("false")) {
				btnExpression.setImageResource(R.drawable.ic_keyboard_grey600_24dp);
				transaction.replace(R.id.flPane, new NormalExprssionFragment());
				btnExpression.setTag("true");
			} else {
				btnExpression.setImageResource(R.drawable.ic_tag_faces_grey600_24dp);
				btnExpression.setTag("false");
				showSoftInput();
				isResizeInOnLayout = true;
			}

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
			if (emotionMainFragment == null)
				emotionMainFragment = new EmotionMainFragment();
			transaction.replace(R.id.flPane, emotionMainFragment);
			btnEmotion.setImageResource(R.drawable.ic_premium_emotion_blue_24dp);
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
		transaction.commit();
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
		msgList.replaceAllRow(msgBeans);
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
		}

		public void afterTextChanged(android.text.Editable s) {
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
					&& (mContactManager.isMyContact(targetId))) {
				unreadCount++;
				tvUnread.setVisibility(View.VISIBLE);
				tvUnread.setText("" + unreadCount);
			}
			appendMsg(item);
			break;
		case RECEIVE_CHECK_SEND_MESSAGE_ERROR:
			/* 底层判断无试聊券，无钱发送邀请失败处理 */
			LiveChatErrType errType = LiveChatErrType.values()[msg.arg1];
			List<LCMessageItem> msgCallbackList = (List<LCMessageItem>) msg.obj;
			if (errType == LiveChatErrType.NoMoney) {
				/* 无信用点提示充值 */
				final GetMoreCreditDialog dialog = new GetMoreCreditDialog(
						this, R.style.ChoosePhotoDialog);
				dialog.show();
			}
			for (LCMessageItem msgItem : msgCallbackList) {
				LiveChatCallBackItem livechatCallbackItem = new LiveChatCallBackItem(
						msg.arg1, "", "", msgItem);
				msgList.updateSendMessageCallback(livechatCallbackItem);
			}
			break;
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
				if(response.errno.equals("ERROR00003")){
					final GetMoreCreditDialog dialog = new GetMoreCreditDialog(this, R.style.ChoosePhotoDialog);
			        dialog.show();
				}
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
		default:
			break;
		}
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
	private int getKeyboardHeight() {
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
			transaction.commit();
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
		btnVoice.setImageResource(R.drawable.ic_mic_grey600_24dp);
		btnEmotion.setImageResource(R.drawable.ic_premium_emotion_24dp);
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
}
