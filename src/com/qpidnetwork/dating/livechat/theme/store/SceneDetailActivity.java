package com.qpidnetwork.dating.livechat.theme.store;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Message;
import android.support.v7.widget.CardView;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.qpidnetwork.dating.R;
import com.qpidnetwork.dating.authorization.LoginManager;
import com.qpidnetwork.dating.bean.ContactBean;
import com.qpidnetwork.dating.contacts.ContactManager;
import com.qpidnetwork.dating.lady.LadyDetailManager;
import com.qpidnetwork.dating.lady.LadyDetailManager.OnLadyDetailManagerQueryLadyDetailCallback;
import com.qpidnetwork.dating.livechat.ChatActivity;
import com.qpidnetwork.dating.livechat.LiveChatCallBackItem;
import com.qpidnetwork.framework.base.BaseFragmentActivity;
import com.qpidnetwork.livechat.LCThemeManager.ThemeStatus;
import com.qpidnetwork.livechat.LCUserItem;
import com.qpidnetwork.livechat.LCUserItem.ChatType;
import com.qpidnetwork.livechat.LiveChatManager;
import com.qpidnetwork.livechat.LiveChatManagerThemeListener;
import com.qpidnetwork.livechat.jni.LCPaidThemeInfo;
import com.qpidnetwork.livechat.jni.LiveChatClient.UserStatusType;
import com.qpidnetwork.livechat.jni.LiveChatClientListener.LiveChatErrType;
import com.qpidnetwork.manager.FileCacheManager;
import com.qpidnetwork.manager.ThemeConfigManager;
import com.qpidnetwork.request.OnGetThemeConfigCallback;
import com.qpidnetwork.request.item.LadyDetail;
import com.qpidnetwork.request.item.ThemeConfig;
import com.qpidnetwork.request.item.ThemeItem;
import com.qpidnetwork.tool.ImageViewLoader;
import com.qpidnetwork.tool.ImageViewLoader.ImageViewLoaderCallback;
import com.qpidnetwork.view.ButtonRaised;
import com.qpidnetwork.view.GetMoreCreditDialog;
import com.qpidnetwork.view.MaterialAppBar;
import com.qpidnetwork.view.MaterialDialogAlert;
import com.qpidnetwork.view.ThemeProgressDialog;

/**
 * @author Yanni
 * 
 * @version 2016-4-22
 */
public class SceneDetailActivity extends BaseFragmentActivity implements
		OnClickListener, LiveChatManagerThemeListener,
		OnGetThemeConfigCallback, OnLadyDetailManagerQueryLadyDetailCallback,ImageViewLoaderCallback {

	private static final int MAN_FEE_THEME = 1;
	private static final int MAN_APPLY_THEME = 2;
	private static final int THEME_DOWNLOAD_PROGRESS = 3;
	private static final int THEME_DOWNLOAD_FINISH = 4;
	private static final int GET_LADY_DETAIL = 5;
	private static final int IMAGE_DOWNLOAD_FINISH = 6;

	private MaterialAppBar appbar;
	private ImageView ivImg;// 主题图片
	private CardView cvLady;// 主题所应用女士信息
	private ImageView ivLadyPhoto;// 女士头像
	private TextView tvLadyName;// 女士名称
	private TextView tvSceneName;// 主题名称
	private TextView tvDes;// 主题描述
	private TextView tvPrice;// 主题价格
	private ButtonRaised btnCommit;// 提交按钮 购买/应用
	private CardView cvWarn;// no inchat提示
	private TextView tvLadyId;// 提示框中女士id

	private ThemeItem mThemeItem = null;// 主题信息
	private String mWomanId = null;
	private String mWomanName = "";
	private String mPhotoUrl = "";

	private LCUserItem chatTarget; // 存储当前聊天对象
	private LiveChatManager mLiveChatManager;
	private ThemeConfigManager mThemeConfigManager;
	private ContactManager mContactManager;

	private MaterialDialogAlert mMaterialDialog;
	private ThemeProgressDialog mThemeProgressDialog;
	private ThemeStatus themeStatus;// 当前主题的状态
	private boolean inChat;// 是否inChat状态
	
	private Runnable mProgressRunnable;

	/**
	 * 数据初始化
	 */
	private void initData() {
		// TODO Auto-generated method stub
		// 获取传递的主题信息与女士id
		Intent intent = getIntent();
		mThemeItem = (ThemeItem) intent.getSerializableExtra("themeItem");
		mWomanId = intent.getStringExtra("womanId");
		initWomanInfoByLocal(mWomanId);
		
		mMaterialDialog = new MaterialDialogAlert(this);
		mThemeProgressDialog = new ThemeProgressDialog(this);

		mLiveChatManager = LiveChatManager.getInstance();
		mContactManager = ContactManager.getInstance();
		mLiveChatManager.RegisterThemeListener(this);

		themeStatus = mLiveChatManager.getThemeStatus(mWomanId,mThemeItem.themeId);// 获取当前主题状态
		inChat = checkInChat(mWomanId);// 判断与当前女士的inchat状态

		// 获取主题配置
		String user_sid = LoginManager.getInstance().GetLoginParam().item.sessionid;
		String user_id = LoginManager.getInstance().GetLoginParam().item.manid;
		mThemeConfigManager = ThemeConfigManager.getInstance();
		mThemeConfigManager.GetThemeConfig(user_sid, user_id, this);
		
		mProgressRunnable = new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				if(mThemeItem != null){
					int progress =  mLiveChatManager.GetThemeDownloadProgress(mThemeItem.themeId);
					if (themeStatus == ThemeStatus.PAID_NO_USED){
						Message msg = Message.obtain();
						msg.what = THEME_DOWNLOAD_PROGRESS;
						msg.arg1 = progress;
						msg.obj = mThemeItem.themeId;
						sendUiMessage(msg);
					}
				}
			}
		};
		
	}

	/*
	 * 初始化控件
	 */
	public void InitView() {
		initData();// 初始化数据
		setContentView(R.layout.activity_scene_detail);
		ivImg = (ImageView) this.findViewById(R.id.ivImg);
		cvLady = (CardView) this.findViewById(R.id.cvLady);
		ivLadyPhoto = (ImageView) this.findViewById(R.id.ivLadyPhoto);
		tvLadyName = (TextView) this.findViewById(R.id.tvLadyName);
		tvSceneName = (TextView) this.findViewById(R.id.tvSceneName);
		tvDes = (TextView) this.findViewById(R.id.tvDesc);
		tvPrice = (TextView) this.findViewById(R.id.tvPrice);
		btnCommit = (ButtonRaised) this.findViewById(R.id.btnCommit);
		cvWarn = (CardView) this.findViewById(R.id.cvWarn);
		tvLadyId = (TextView) this.findViewById(R.id.tvLadyId);
		btnCommit.setOnClickListener(this);
		
		
		appbar = (MaterialAppBar)findViewById(R.id.appbar);
		appbar.setAppbarBackgroundColor(getResources().getColor(R.color.theme_actionbar_secoundary));
		appbar.setTouchFeedback(MaterialAppBar.TOUCH_FEEDBACK_HOLO_LIGHT);
		appbar.addButtonToLeft(R.id.common_button_back, "", R.drawable.ic_arrow_back_grey600_24dp);
		appbar.setTitle(getString(R.string.theme_store_details), getResources().getColor(R.color.text_color_dark));
		appbar.setOnButtonClickListener(this);

		updateView();// 填充数据更新UI
	}

	/**
	 * 填充数据
	 */
	private void updateView() {
		
		float WHR = 840.0f / 780.0f;
		float W = getResources().getDisplayMetrics().widthPixels;
		float H = W * WHR;
		RelativeLayout.LayoutParams imageParams = new RelativeLayout.LayoutParams((int)W, (int)H);
		ivImg.setLayoutParams(imageParams);
		
		// 女士信息填充
		if (mWomanId != null) {
			if(!TextUtils.isEmpty(mWomanName)){
				tvLadyName.setText(mWomanName);
			}else{
				tvLadyName.setText(mWomanId);
			}
			if(!TextUtils.isEmpty(mPhotoUrl)){
				String localPath = FileCacheManager.getInstance().CacheImagePathFromUrl(mPhotoUrl);// 获取本地缓存路径
				new ImageViewLoader(mContext).DisplayImage(ivLadyPhoto, mPhotoUrl,localPath, null);
			}
			if(TextUtils.isEmpty(mWomanName) ||
					TextUtils.isEmpty(mPhotoUrl)){
				LadyDetailManager.getInstance().QueryLadyDetail(mWomanId, this);// 联系人中不存在时获取女士详情
			}
		}
				
		switch (themeStatus) {
		case NO_PAID:
			//btnCommit.setText(mContext.);
			btnCommit.setButtonTitle(getString(R.string.theme_store_btn_buy));
			tvPrice.setVisibility(View.VISIBLE);
			break;
		case PAID_NO_USED:
			//btnCommit.setText(mContext.getString(R.string.theme_store_btn_activate));
			btnCommit.setButtonTitle(getString(R.string.theme_store_btn_activate));
			tvPrice.setVisibility(View.GONE);// 隐藏价格信息
			if (!inChat) {// 非inchar状态显示警告提示
				btnCommit.setClickable(false);
				btnCommit.setButtonBackground(getResources().getColor(R.color.them_btn_enable));
				cvWarn.setVisibility(View.VISIBLE);// 显示提示
				String dialogMsg = "";
				if(TextUtils.isEmpty(mWomanName)){
					dialogMsg = String.format(mContext.getString(R.string.theme_store_dialog_inchat), mWomanId);
				}else{
					dialogMsg = String.format(mContext.getString(R.string.theme_store_dialog_inchat), mWomanName);
				}
				tvLadyId.setText(dialogMsg);
			}
			break;
		case USED:
			tvPrice.setVisibility(View.GONE);
			//btnCommit.setText(mContext.getString(R.string.theme_store_btn_in_use));
			btnCommit.setButtonBackground(Color.GRAY);
			btnCommit.setClickable(false);
			btnCommit.setButtonTitle("In Use");
			//btnCommit.setBackgroundColor(Color.GRAY);
			break;

		default:
			break;
		}
		// 根据传递的themeItem填充到UI界面中
		if (mThemeItem != null) {
			String imgUrl = ThemeConfigManager.newInstance().getThemePreImgUrl(mThemeItem.themeId);
			String localPath = FileCacheManager.getInstance().CacheImagePathFromUrl(imgUrl);// 获取本地缓存路径
			new ImageViewLoader(mContext).DisplayImage(ivImg, imgUrl,localPath, this);

			tvSceneName.setText(mThemeItem.title);
			tvDes.setText(mThemeItem.description);//主题描述 为空
			tvPrice.setText(mThemeItem.price + " " + mContext.getString(R.string.theme_store_credits));
		}
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		super.onClick(v);
		switch (v.getId()) {
		
		case R.id.common_button_back:{
			finish();
		}break;
		case R.id.btnCommit:
			switch (themeStatus) {
			case NO_PAID:
				sceneBuy();// 购买主题
				break;
			case PAID_NO_USED:
				sceneActivate();// 应用主题
				break;
			case USED:

				break;

			default:
				break;
			}
			break;
			
		//case R.id.ivAsk:
		//	mMaterialDialog.removeAllButton();
		//	mMaterialDialog.setMessage("This is Null.");
		//	mMaterialDialog.show();
		//	break;

		default:
			break;
		}
	}

	/**
	 * 应用主题
	 */
	private void sceneActivate() {
		// TODO Auto-generated method stub
		if (inChat) {// 判断是否inchat状态
			// showToastProgressing("Processing...");
			mLiveChatManager.GetThemeResource(mThemeItem.themeId);
			startProgressTimer();
		} else {
			mMaterialDialog.removeAllButton();
			mMaterialDialog.setMessage(mContext.getString(R.string.theme_store_activate_inchat));
			mMaterialDialog.addButton(mMaterialDialog.createButton(mContext.getString(R.string.common_btn_ok), null));
			mMaterialDialog.show();
		}
	}

	/**
	 * 购买主题
	 */
	private void sceneBuy() {
		// TODO Auto-generated method stub
		if (inChat) {// 判断是否inchat状态
			showToastProgressing("Processing...");
			mLiveChatManager.ManFeeTheme(mWomanId, mThemeItem.themeId);
		} else {
			mMaterialDialog.removeAllButton();
			mMaterialDialog.setMessage(mContext.getString(R.string.theme_store_activate_inchat));
			mMaterialDialog.addButton(mMaterialDialog.createButton("ok", null));
			mMaterialDialog.show();
		}
	}

	/**
	 * 判断是否处于inChat状态
	 */
	private boolean checkInChat(String womanId) {
		// TODO Auto-generated method stub
		Boolean inChat = false;
		chatTarget = mLiveChatManager.GetUserWithId(womanId);
		ChatType chatType = chatTarget.chatType;
		if ((chatTarget.statusType == UserStatusType.USTATUS_ONLINE)
				&&(chatType == ChatType.InChatCharge|| chatType == ChatType.InChatUseTryTicket)) {
			inChat = true;
		}
		return inChat;
	}

	@Override
	protected void handleUiMessage(Message msg) {
		// TODO Auto-generated method stub
		super.handleUiMessage(msg);
		switch (msg.what) {
		case MAN_FEE_THEME:// 购买主题
			LiveChatCallBackItem feeCallBack = (LiveChatCallBackItem) msg.obj;
			LiveChatErrType feeErrType = LiveChatErrType.values()[feeCallBack.errType];
			LCPaidThemeInfo themeInfo = (LCPaidThemeInfo) feeCallBack.body;
			if (feeErrType == LiveChatErrType.Success) {
				feeThemeSuccess(themeInfo);// 购买成功
				
				//通知聊天界面刷新主题配置
				Intent intent = new Intent(ChatActivity.REFRESH_THEME_CONFIG);
				intent.putExtra(ChatActivity.WOMAN_ID, mWomanId);
				sendBroadcast(intent);
				
			} else if (feeErrType == LiveChatErrType.NoMoney) {// 余额不足
				cancelToast();
				GetMoreCreditDialog dialog = new GetMoreCreditDialog(SceneDetailActivity.this,R.style.ChoosePhotoDialog);
				if (isActivityVisible()) {
					dialog.show();
				}
			} else {
				feeThemeFail();// 购买失败
			}
			break;
		case MAN_APPLY_THEME:// 应用主题
			LiveChatCallBackItem applyCallBack = (LiveChatCallBackItem) msg.obj;
			LiveChatErrType applyErrType = LiveChatErrType.values()[applyCallBack.errType];
			if (applyErrType == LiveChatErrType.Success) {// 应用成功
				showToastDone("Success");
				btnCommit.setButtonTitle("In Use");
				btnCommit.setBackgroundColor(Color.GRAY);
				themeStatus = ThemeStatus.USED;// 设置状态为应用中
				
				//通知聊天界面刷新主题配置
				Intent intent = new Intent(ChatActivity.REFRESH_THEME_CONFIG);
				intent.putExtra(ChatActivity.WOMAN_ID, mWomanId);
				sendBroadcast(intent);
			} else if(applyErrType==LiveChatErrType.SideOffile){
				mMaterialDialog.removeAllButton();
				mMaterialDialog.setMessage(mContext.getString(R.string.send_error_lady_offline));
				mMaterialDialog.addButton(mMaterialDialog.createButton("ok", null));
				mMaterialDialog.show();
			}else{
				showToastFailed("Failed");
			}
			break;

		case THEME_DOWNLOAD_PROGRESS:// 更新下载进度
			cancelToast();
			mThemeProgressDialog.setCancelable(false);
			mThemeProgressDialog.show();
			mThemeProgressDialog.setProgress(msg.arg1);
			startProgressTimer();
			break;
		case THEME_DOWNLOAD_FINISH:// 下载完成
			cancelToast();
			mHandler.removeCallbacks(mProgressRunnable);
			mThemeProgressDialog.dismiss();
			mLiveChatManager.ManApplyTheme(mWomanId, mThemeItem.themeId);
			showToastProgressing(mContext.getString(R.string.theme_store_toast_activating));
			break;
			
		case GET_LADY_DETAIL://获取女士详情
			if (msg.arg1 == 1) {// 请求成功
				LadyDetail ladyDetail = (LadyDetail) msg.obj;
				mWomanName = ladyDetail.firstname;
				mPhotoUrl = ladyDetail.photoMinURL;
				String localPath = FileCacheManager.getInstance().CacheImagePathFromUrl(mPhotoUrl);// 获取本地缓存路径
				new ImageViewLoader(mContext).DisplayImage(ivLadyPhoto, mPhotoUrl,localPath, null); 
				tvLadyName.setText(mWomanName);
				String dialogMsg = String.format(mContext.getString(R.string.theme_store_dialog_inchat), mWomanName);
				tvLadyId.setText(dialogMsg);
			}
			break;
			
		case IMAGE_DOWNLOAD_FINISH://图片加载完成
			
			/*int w = ivImg.getWidth();
			int h = ivImg.getHeight();
			
			System.out.println(w+"------"+h);
			
			int width = this.getWindow().getWindowManager().getDefaultDisplay().getWidth();
			if(w==0||h==0){
				String imgUrl = ThemeConfigManager.newInstance().getThemePreImgUrl(mThemeItem.themeId);
				String localPath = FileCacheManager.getInstance().CacheImagePathFromUrl(imgUrl);// 获取本地缓存路径
				new ImageViewLoader(mContext).DisplayImage(ivImg, imgUrl,localPath, this);
			}else{
				ivImg.setLayoutParams(new RelativeLayout.LayoutParams(width, width*(h/w)));
			}*/
			
			Bitmap bmp = (Bitmap)msg.obj;
			if (bmp == null) return;
			int w = bmp.getWidth();
			int h = bmp.getHeight();
			
			float t = this.getResources().getDisplayMetrics().widthPixels / (float)w;
			float newW = w * t;
			float newH = h * t;
		
			ivImg.setLayoutParams(new RelativeLayout.LayoutParams((int)newW, (int)newH));
			
			break;

		default:
			break;
		}
	}

	/**
	 * 购买主题成功
	 */
	private void feeThemeSuccess(LCPaidThemeInfo themeInfo) {
		// TODO Auto-generated method stub
		showToastDone(mContext.getString(R.string.theme_store_toast_success));
		cvLady.setVisibility(View.VISIBLE);
		tvLadyName.setText(themeInfo.womanId);
		btnCommit.setButtonTitle(mContext.getString(R.string.theme_store_btn_in_use));
		btnCommit.setBackgroundColor(Color.GRAY);
		themeStatus = ThemeStatus.USED;// 设置状态为应用中

		mLiveChatManager.GetThemeResource(themeInfo.themeId);// 购买成功后下载主题资料
	}
	
	/**
	 * 获取指定主题进度
	 * @param themeId
	 */
	private void startProgressTimer(){
		mHandler.postDelayed(mProgressRunnable, 200);
	}

	/**
	 * 购买主题失败
	 */
	private void feeThemeFail() {
		cancelToast();
		mMaterialDialog.removeAllButton();
		mMaterialDialog.setMessage(mContext.getString(R.string.theme_store_buy_faile));
		mMaterialDialog.addButton(mMaterialDialog.createButton("Retry",
				new OnClickListener() {
					@Override
					public void onClick(View v) {
						mMaterialDialog.dismiss();
						sceneBuy();// 重新触发购买
					}
				}));
		mMaterialDialog.addButton(mMaterialDialog.createButton("Cancel", null));
		mMaterialDialog.show();
	}
	
	private void initWomanInfoByLocal(String womanId){
		ContactBean contact = ContactManager.getInstance().getContactById(mWomanId);
		if(contact != null){
			mWomanName = contact.firstname;
			mPhotoUrl = contact.photoURL;
		}
		if(TextUtils.isEmpty(mWomanName)||(TextUtils.isEmpty(mPhotoUrl))){
			LadyDetail ladyDetail = LadyDetailManager.getInstance().getLadyDetailById(womanId);
			if(ladyDetail != null){
				mWomanName = ladyDetail.firstname;
				mPhotoUrl = ladyDetail.photoMinURL;
			}
		}
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		mHandler.removeCallbacks(mProgressRunnable);
		if(mThemeProgressDialog != null){
			mThemeProgressDialog.dismiss();
		}
		mLiveChatManager.UnregisterThemeListener(this);
	}

	/**
	 * @param callBack
	 * 
	 *            购买回调成功发送message回传UI线程
	 */
	private void onSendMessageFeeTheme(LiveChatCallBackItem callBack) {
		// TODO Auto-generated method stub
		Message msg = Message.obtain();
		msg.what = MAN_FEE_THEME;
		msg.obj = callBack;
		sendUiMessage(msg);
	}

	/**
	 * @param callBack
	 * 
	 *            应用回调成功发送message回传UI线程
	 */
	private void onSendMessageApplyTheme(LiveChatCallBackItem callBack) {
		// TODO Auto-generated method stub
		Message msg = Message.obtain();
		msg.what = MAN_APPLY_THEME;
		msg.obj = callBack;
		sendUiMessage(msg);
	}

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
		// TODO Auto-generated method stub
		LiveChatCallBackItem callBack = new LiveChatCallBackItem(errType.ordinal(), errmsg, null, paidThemeInfo);
		onSendMessageFeeTheme(callBack);
	}

	@Override
	public void OnManApplyTheme(LiveChatErrType errType, String womanId, String themeId, String errmsg,
			LCPaidThemeInfo paidThemeInfo) {
		// TODO Auto-generated method stub
		LiveChatCallBackItem callBack = new LiveChatCallBackItem(errType.ordinal(), errmsg, null, paidThemeInfo);
		onSendMessageApplyTheme(callBack);
	}

	@Override
	public void OnPlayThemeMotion(LiveChatErrType errType, String errmsg,
			String womanId, String themeId) {
		// TODO Auto-generated method stub

	}

	@Override
	public void OnRecvThemeMotion(String themeId, String manId, String womanId) {
		// TODO Auto-generated method stub

	}

	@Override
	public void OnRecvThemeRecommend(String themeId, String manId,
			String womanId) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onThemeDownloadUpdate(String themeId, int progress) {
		// TODO Auto-generated method stub
		if (themeStatus == ThemeStatus.PAID_NO_USED) {// 仅当主题已购买未应用时显示进度
			Message msg = Message.obtain();
			msg.what = THEME_DOWNLOAD_PROGRESS;
			msg.arg1 = progress;
			msg.obj = themeId;
			sendUiMessage(msg);
		}
	}

	@Override
	public void onThemeDownloadFinish(boolean isSuccess, String themeId,
			String sourceDir) {
		// TODO Auto-generated method stub
		if (themeStatus == ThemeStatus.PAID_NO_USED) {// 仅当主题已购买未应用时通知完成
			Message msg = Message.obtain();
			msg.what = THEME_DOWNLOAD_FINISH;
			sendUiMessage(msg);
		}
	}

	@Override
	public void OnGetThemeConfig(boolean isSuccess, String errno,
			String errmsg, ThemeConfig config) {
		// TODO Auto-generated method stub

	}

	/**
	 * 获取女士详情
	 */
	@Override
	public void OnQueryLadyDetailCallback(boolean isSuccess, String errno,
			String errmsg, LadyDetail item) {
		// TODO Auto-generated method stub
		if (item != null) {
			Message msg = Message.obtain();
			msg.what = GET_LADY_DETAIL;
			msg.arg1 = isSuccess ? 1 : 0;
			msg.obj = item;
			sendUiMessage(msg);
		}

	}



	@Override
	public void OnLoadPhotoFailed() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void OnDisplayNewImageFinish(Bitmap bmp) {
		// TODO Auto-generated method stub
		Message msg = Message.obtain();
		msg.what = IMAGE_DOWNLOAD_FINISH;
		msg.obj = bmp;
		sendUiMessage(msg);
		
		
		
		
	}
}
