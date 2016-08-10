package com.qpidnetwork.dating.emf;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Message;
import android.text.Html;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Base64;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.qpidnetwork.dating.R;
import com.qpidnetwork.dating.authorization.LoginManager;
import com.qpidnetwork.dating.authorization.LoginManager.LoginStatus;
import com.qpidnetwork.dating.authorization.LoginParam;
import com.qpidnetwork.dating.bean.EMFAttachmentBean;
import com.qpidnetwork.dating.bean.EMFAttachmentBean.AttachType;
import com.qpidnetwork.dating.bean.EMFBean;
import com.qpidnetwork.dating.bean.PrivatePhotoBean;
import com.qpidnetwork.dating.bean.RequestFailBean;
import com.qpidnetwork.dating.bean.ShortVideoBean;
import com.qpidnetwork.dating.credit.BuyCreditActivity;
import com.qpidnetwork.dating.credit.MonthlyFeeNotifyAdapter;
import com.qpidnetwork.dating.emf.EMFAttachmentPrivatePhotoFragment.PrivatePhotoDirection;
import com.qpidnetwork.dating.lady.LadyDetailActivity;
import com.qpidnetwork.framework.base.BaseFragmentActivity;
import com.qpidnetwork.framework.util.ToastUtil;
import com.qpidnetwork.framework.widget.CircleImageView;
import com.qpidnetwork.manager.FileCacheManager;
import com.qpidnetwork.manager.MonthlyFeeManager;
import com.qpidnetwork.request.OnEMFAdmirerViewerCallback;
import com.qpidnetwork.request.OnEMFDeleteMsgCallback;
import com.qpidnetwork.request.OnEMFInboxMsgCallback;
import com.qpidnetwork.request.OnEMFOutboxMsgCallback;
import com.qpidnetwork.request.OnEMFPrivatePhotoViewCallback;
import com.qpidnetwork.request.OnEMFSendMsgCallback;
import com.qpidnetwork.request.OnOtherIntegralCheckCallback;
import com.qpidnetwork.request.OnRequestFileCallback;
import com.qpidnetwork.request.RequestErrorCode;
import com.qpidnetwork.request.RequestJniEMF.MailType;
import com.qpidnetwork.request.RequestJniEMF.PrivatePhotoMode;
import com.qpidnetwork.request.RequestJniEMF.PrivatePhotoType;
import com.qpidnetwork.request.RequestJniEMF.ReplyType;
import com.qpidnetwork.request.RequestJniLiveChat.VideoPhotoType;
import com.qpidnetwork.request.RequestJniMonthlyFee;
import com.qpidnetwork.request.RequestJniMonthlyFee.MemberType;
import com.qpidnetwork.request.RequestOperator;
import com.qpidnetwork.request.item.EMFAdmirerViewerItem;
import com.qpidnetwork.request.item.EMFInboxMsgItem;
import com.qpidnetwork.request.item.EMFOutboxMsgItem;
import com.qpidnetwork.request.item.EMFSendMsgErrorItem;
import com.qpidnetwork.request.item.EMFSendMsgItem;
import com.qpidnetwork.request.item.EMFShortVideoItem;
import com.qpidnetwork.request.item.LadyDetail;
import com.qpidnetwork.request.item.MonthLyFeeTipItem;
import com.qpidnetwork.request.item.OtherIntegralCheckItem;
import com.qpidnetwork.tool.ImageViewLoader;
import com.qpidnetwork.view.ButtonRaised;
import com.qpidnetwork.view.FlatToast;
import com.qpidnetwork.view.GetMoreCreditDialog;
import com.qpidnetwork.view.MaterialAppBar;
import com.qpidnetwork.view.MaterialDialogAlert;
import com.qpidnetwork.view.MaterialDropDownMenu;
import com.qpidnetwork.view.MonthlyFeeDialog;

@SuppressLint("InflateParams")
public class EMFDetailActivity extends BaseFragmentActivity implements
		OnClickListener, MaterialDropDownMenu.OnClickCallback,
		OnEMFInboxMsgCallback, OnEMFOutboxMsgCallback,
		OnEMFAdmirerViewerCallback, OnEMFDeleteMsgCallback,
		OnEMFPrivatePhotoViewCallback, OnOtherIntegralCheckCallback,
		OnEMFSendMsgCallback {

	public static final String ACTION_SHORT_VIDEO_FEE = "videoFee";
	public static final String SHORT_VIDEO_VIDEOID = "videoId";

	public static final int REQUEST_CODE = 0;
	public static final String EMF_MESSAGEID = "emfId";

	public static final String EMF_DELETE = "emfdelete";
	public static final String EMF_DETAIL_READED = "emfreaded";

	public static enum BlockReason {
		REASON_A, REASON_B, REASON_C
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
		 * @param womanId
		 *            女士Id
		 * @param item
		 *            emf返回
		 * @param errItem
		 *            emf错误返回
		 * @param otItem
		 *            积分返回
		 * @param ladyDetail
		 *            女士详情
		 */
		public MessageCallbackItem(String errno, String errmsg) {
			this.errno = errno;
			this.errmsg = errmsg;
		}

		public String errno;
		public String errmsg;
		public String womanId;
		public EMFSendMsgItem sendItem;
		public EMFSendMsgErrorItem errItem;
		public OtherIntegralCheckItem otItem;
		public LadyDetail ladyDetail;
	}

	/**
	 * 编辑
	 */
	public static final int RESULT_ATTACHMENT = 1;

	public static final String EMF_BASE_BEAN = "emfBean";
	private static final int GET_EMF_DETAIL_SUCCESS = 0;
	private static final int GET_EMF_DETAIL_FAILED = 1;
	private static final int DELETE_EMF_SUCCESS = 2;
	private static final int DELETE_EMF_FAILED = 3;
	private static final int REQUEST_GET_PHOTO_SUCCESS = 4;
	private static final int REQUEST_GET_PHOTO_FAIL = 5;
	private static final int BLOCK_WOMAN_SUCCESS = 6;
	private static final int BLOCK_WOMAN_FAILED = 7;
	private static final int REQUEST_GET_VIDEOPHOTO_SUCCESS = 8;
	private static final int REQUEST_GET_VIDEOPHOTO_FAIL = 9;
	private static final int REQUEST_GET_BOUNDS = 10;
	private static final int REQUEST_SEND_EMF = 11;

	private CircleImageView ivPhoto;
	private TextView tvName;
	private LinearLayout llAgeCountry;
	private TextView tvAge;
	private TextView tvCountry;
	private TextView tvDesc;
	private TextView tvDate;

	private TextView tvEMFdetail;
	private LinearLayout llAttachments;
	private GridView gvAttachment;// 附件列表
	private LinearLayout llTranslator;
	private TextView tvMessage;
	private ButtonRaised btnReply;

	// ------Quick Replay-------
	private View includeQuickReply;
	private ImageView ivQuickReply;
	private EditText etReply;
	private ButtonRaised btnSend;
	private TextView tvBonusPoint;
//	private TextView tvLearnMore;
	
	private View includeMonthlyError;
	private TextView tvPrice;
	private ListView listView;
	private ButtonRaised btnSubscribe;

	private View includeError;
	private TextView tvErrorMessage;
	private ButtonRaised btnErrorOperate;

	private EMFBean emfBean;
	private Object emfDeail;
	private MaterialAppBar appbar;

	private boolean useIntegral = false;// 是否使用积分
	private MaterialDialogAlert mErrorDialog;

	/* 存放附件列表 */
	private ArrayList<EMFAttachmentBean> mAttachList;
	private EMFVideoManager mEMFVideoManager;

	private EMFInboxMsgItem mEMFInboxMsgItem = null;
	private EMFOutboxMsgItem mEMFOutboxMsgItem = null;

	/* 广播用于activity间数据传递 */
	private BroadcastReceiver mBroadcastReceiver;// 处理购买成功状态更新

	public static Intent getIntent(Context context, EMFBean emfBean) {
		Intent intent = new Intent(context, EMFDetailActivity.class);
		intent.putExtra(EMF_BASE_BEAN, emfBean);
		return intent;
	}

	@Override
	protected void onCreate(Bundle arg0) {
		// TODO Auto-generated method stub
		super.onCreate(arg0);
		setContentView(R.layout.activity_emf_detail);
		mEMFVideoManager = EMFVideoManager.newInstance(this);

		initReceiver();
		initViews();
		initData();
	}
	
	private void initReceiver(){
		mBroadcastReceiver = new BroadcastReceiver() {

			@Override
			public void onReceive(Context context, Intent intent) {
				// TODO Auto-generated method stub
				String action = intent.getAction();
				if (action.equals(ACTION_SHORT_VIDEO_FEE)) {
					String videoId = intent.getExtras().getString(SHORT_VIDEO_VIDEOID);
					updateEmFShortVideo(videoId);
				}
			}
		};
		
		IntentFilter filter = new IntentFilter();
		filter.addAction(ACTION_SHORT_VIDEO_FEE);
		registerReceiver(mBroadcastReceiver, filter);
	}
	
	/**
	 * 更新指定视频为已付费状态，保证activity间数据一致
	 * @param isFee
	 */
	private void updateEmFShortVideo(String shortVideoId){
		if((mEMFInboxMsgItem != null) && (mEMFInboxMsgItem.shortVideos != null) && (mEMFInboxMsgItem.shortVideos.length > 0)){
			for(EMFShortVideoItem item : mEMFInboxMsgItem.shortVideos){
				if(item.videoId.equals(shortVideoId)){
					item.videoFee = true;
				}
			}
		}
		if((mAttachList != null) && (mAttachList.size() > 0)){
			for(EMFAttachmentBean item : mAttachList){
				if((item.type == AttachType.SHORT_VIDEO)&&
						(item.shortVideo != null)&&
						(item.shortVideo.videoId.equals(shortVideoId))){
					item.shortVideo.videoFee = true;
				}
			}
		}
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		// onGetInboxDetailSuccess();
		// onGetOutboxDetailSuccess();
		initAttachment();
	}

	@Override
	protected void onDestroy() {
		cancelToastImmediately();
		super.onDestroy();
		unregisterReceiver(mBroadcastReceiver);
	}

	private void initViews() {
		// 初始化dialog
		mErrorDialog = new MaterialDialogAlert(this);
		mErrorDialog.setMessage(getString(R.string.emf_send_fail));
		mErrorDialog.addButton(mErrorDialog.createButton(
				getString(R.string.common_btn_cancel), null));
		mErrorDialog.addButton(mErrorDialog.createButton(
				getString(R.string.common_btn_ok), new OnClickListener() {
					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						sendEMFTextMessage();
					}
				}));
		
		/* title */
		appbar = (MaterialAppBar) findViewById(R.id.appbar);
		appbar.setAppbarBackgroundColor(getResources().getColor(
				R.color.theme_actionbar_secoundary));
		appbar.setOnButtonClickListener(this);
		appbar.setTouchFeedback(MaterialAppBar.TOUCH_FEEDBACK_HOLO_LIGHT);
		// appbar.addButtonToLeft(R.id.common_button_forward, "forward",
		// R.drawable.ic_forward_grey600_24dp);
		//appbar.addButtonToLeft(R.id.common_button_reply, "reply",
		//		R.drawable.ic_reply_all_grey600_24dp);
		appbar.addButtonToLeft(R.id.common_button_back, "back",
				R.drawable.ic_arrow_back_grey600_24dp);
		// LinearLayout.LayoutParams params =
		// (LinearLayout.LayoutParams)appbar.findViewWithTag("reply").getLayoutParams();
		// params.setMargins((int)(72.00 *
		// getResources().getDisplayMetrics().density) - params.width, 0, 0, 0);
		// appbar.setButtonLayoutParams("reply", params);
		appbar.setTitle(getString(R.string.emf_mail_viewer), getResources().getColor(R.color.text_color_dark));
		appbar.addOverflowButton(new String[] { getString(R.string.emf_menu_delete) }, this,R.drawable.ic_more_vert_grey600_24dp);

		/* base header */
		ivPhoto = (CircleImageView) findViewById(R.id.ivPhoto);
		tvName = (TextView) findViewById(R.id.tvName);
		llAgeCountry = (LinearLayout) findViewById(R.id.llAgeCountry);
		tvAge = (TextView) findViewById(R.id.tvAge);
		tvCountry = (TextView) findViewById(R.id.tvCountry);
		tvDesc = (TextView) findViewById(R.id.tvDesc);
		tvDate = (TextView) findViewById(R.id.tvDate);

		/* detail */
		tvEMFdetail = (TextView) findViewById(R.id.tvEMFdetail);
		llAttachments = (LinearLayout) findViewById(R.id.llAttachments);
		gvAttachment = (GridView) findViewById(R.id.gvAttachment);
		llTranslator = (LinearLayout) findViewById(R.id.llTranslator);
		tvMessage = (TextView) findViewById(R.id.tvMessage);
		btnReply = (ButtonRaised) findViewById(R.id.btnReply);
		btnReply.setOnClickListener(this);

		/* Quick Reply */
		includeQuickReply = (View) findViewById(R.id.llQuickReplyBody);
		ivQuickReply = (ImageView) findViewById(R.id.ivQuickReply);
		etReply = (EditText) findViewById(R.id.etReply);
		btnSend = (ButtonRaised) findViewById(R.id.btnSend);
		tvBonusPoint = (TextView) findViewById(R.id.tvBonusPoint);
		ivQuickReply.setOnClickListener(this);
		btnSend.setOnClickListener(this);

		/*Monthly Fee Error*/
		includeMonthlyError = (View) findViewById(R.id.includeMonthlyError);
		tvPrice = (TextView) findViewById(R.id.tvPrice);
		listView = (ListView)findViewById(R.id.listView);
		btnSubscribe = (ButtonRaised) findViewById(R.id.btnSubscribe);
		
		/* Error */
		includeError = (View) findViewById(R.id.includeError);
		tvErrorMessage = (TextView) findViewById(R.id.tvErrorMessage);
		btnErrorOperate = (ButtonRaised) findViewById(R.id.btnErrorOperate);
		includeQuickReply.setVisibility(View.GONE);
	}

	private void initData() {
		Bundle bundle = getIntent().getExtras();
		if ((bundle != null) && (bundle.containsKey(EMF_BASE_BEAN))) {
			emfBean = bundle.getParcelable(EMF_BASE_BEAN);
		}

		if (emfBean != null) {

			if (emfBean.type == 1) {
				// 已发详情
				llAgeCountry.setVisibility(View.GONE);
				tvDesc.setVisibility(View.GONE);
				tvName.setText("TO: " + emfBean.firstname);
			} else {
				llAgeCountry.setVisibility(View.VISIBLE);
				tvDesc.setVisibility(View.VISIBLE);
				tvName.setText(emfBean.firstname);
				tvAge.setText(emfBean.age + "");
				tvCountry.setText(emfBean.country);
				tvDesc.setText(R.string.emf_to_me);
			}
			tvDate.setText(emfBean.sendTime);

			/* 头像处理 */
			if ((emfBean.photoURL != null) && (!emfBean.photoURL.equals(""))) {
				String localPath = FileCacheManager.getInstance()
						.CacheImagePathFromUrl(emfBean.photoURL);
				new ImageViewLoader(this).DisplayImage(ivPhoto,
						emfBean.photoURL, localPath, null);
			}

			ivPhoto.setClickable(true);
			ivPhoto.setOnClickListener(this);

			/* 请求积分 */
			if (!TextUtils.isEmpty(emfBean.womanid)) {
				integralCheck(emfBean.womanid);// 获取积分
			}

			/* 获取邮件详情 */
			getEmfDetail(emfBean.id);
		}

		mAttachList = new ArrayList<EMFAttachmentBean>();
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		super.onClick(v);
		switch (v.getId()) {
		case R.id.common_button_back:
			finish();
			break;
		case R.id.ivPhoto: {
			LadyDetailActivity.launchLadyDetailActivity(EMFDetailActivity.this,emfBean.womanid, true);
		}
			break;
		case R.id.image: {
			Intent intent = EMFAttachmentPreviewActivity.getIntent(EMFDetailActivity.this, mAttachList, (Integer) v.getTag());
			intent.putExtra(EMFAttachmentPreviewActivity.VGTIPS, false);
			intent.putExtra(EMFAttachmentPreviewActivity.ATTACH_DIRECTION,(emfBean.type == 1) ? PrivatePhotoDirection.MW.name(): PrivatePhotoDirection.WM.name());
			startActivityForResult(intent, RESULT_ATTACHMENT);
		}
			break;
		case R.id.common_button_reply:
		case R.id.btnReply:
		case R.id.ivQuickReply:// 快捷回复带输入内容
			String mTab = "";
			String mTempMessage = etReply.getText().toString();// 保存临时输入文本
			ReplyType type = ReplyType.DEFAULT;
			if (emfBean.type == 2) {
				// 回复意向信
				type = ReplyType.ADMIRE;
				mTab = emfBean.mtab;
			} else {
				// 回复emf
				type = ReplyType.EMF;
			}
			MailEditActivity.launchMailEditActivity(this, emfBean.womanid,type, mTab, mTempMessage);
			break;
		case R.id.btnSend:// 回复文本
			sendEMFTextMessage();// 发送文本信件
			break;
		case R.id.tvLearnMore:// 了解更多
			MaterialDialogAlert dialog = new MaterialDialogAlert(this);
			dialog.setMessage(getString(R.string.emf_bp_tips));
			dialog.addButton(dialog.createButton(getString(R.string.common_btn_ok), null));
			dialog.show();
			break;
		case R.id.btnSubscribe:{
			//月费点击充值
			Intent intent = new Intent(mContext, BuyCreditActivity.class);
			mContext.startActivity(intent);
			finish();
		}break;
		default:
			break;
		}
	}

	/**
	 * 发送文本信件
	 */
	private void sendEMFTextMessage() {
		// TODO Auto-generated method stub
		if (TextUtils.isEmpty(emfBean.womanid)) {
			return;
		}
		String etContent = etReply.getText().toString();
		if (TextUtils.isEmpty(etContent)) {
			shakeView(etReply, true);
			showSoftInput();
			return;
		}
		showProgressDialog("Loading...");

		String mTab = "";
		ReplyType type = ReplyType.DEFAULT;
		if (emfBean.type == 2) {
			// 回复意向信
			type = ReplyType.ADMIRE;
			if(emfBean.mtab!=null)//可能为空
				mTab = emfBean.mtab;
		} else {
			// 回复emf
			type = ReplyType.EMF;
		}

		RequestOperator.getInstance().SendMsg(emfBean.womanid, etContent, useIntegral, type, mTab,new String[] {}, new String[] {}, this);
	}

	@Override
	protected void handleUiMessage(Message msg) {
		// TODO Auto-generated method stub
		super.handleUiMessage(msg);

		switch (msg.what) {
		case GET_EMF_DETAIL_SUCCESS:
			hideProgressDialog();
			includeMonthlyError.setVisibility(View.GONE);
			includeError.setVisibility(View.GONE);
			switch (emfBean.type) {
			case 0:
				mEMFInboxMsgItem = (EMFInboxMsgItem) msg.obj;
				onGetInboxDetailSuccess();
				break;
			case 1:
				mEMFOutboxMsgItem = (EMFOutboxMsgItem) msg.obj;
				onGetOutboxDetailSuccess();
				break;
			case 2:
				emfDeail = (EMFAdmirerViewerItem) msg.obj;
				onGetAdmirerDetailSuccess((EMFAdmirerViewerItem) emfDeail);
				break;
			}
			initAttachment();
			/* 已读，通知列表刷新状态 */
			notifyEmfList(false, true);
			break;
		case GET_EMF_DETAIL_FAILED:{
			hideProgressDialog();
			onGetDeatilFailed(msg.obj, msg.arg1);
		}break;
		case DELETE_EMF_SUCCESS:
			hideProgressDialog();
			notifyEmfList(true, false);
			finish();
			break;
		case DELETE_EMF_FAILED:
			hideProgressDialog();

			MaterialDialogAlert dialog = new MaterialDialogAlert(
					EMFDetailActivity.this);
			dialog.setMessage(getString(R.string.emf_delete_error));
			dialog.addButton(dialog.createButton(
					getString(R.string.common_btn_cancel), null));
			dialog.addButton(dialog.createButton(
					getString(R.string.common_btn_retry),
					new OnClickListener() {

						@Override
						public void onClick(View v) {
							// TODO Auto-generated method stub
							onEmfDelete();
						}

					}));
			if (isActivityVisible()) {
				dialog.show();
			}
			break;
		case REQUEST_GET_PHOTO_SUCCESS: 
		case REQUEST_GET_VIDEOPHOTO_SUCCESS:{
			initAttachment();
		}
			break;

		case BLOCK_WOMAN_SUCCESS: {
			showToastDone("Done!");
		}
			break;
		case BLOCK_WOMAN_FAILED: {
			RequestFailBean bean = (RequestFailBean) msg.obj;
			if (bean.errno.equals("MBCE35003")) {
				/* 已存在，提示 */
				cancelToastImmediately();
				ToastUtil.showToast(this, bean.errmsg);
			} else {
				showToastFailed("Failed!");
			}
		}
			break;
		case REQUEST_GET_BOUNDS:
			if (msg.arg1 == 1) {// 请求积分成功
				if (LoginManager.getInstance().GetLoginStatus() == LoginStatus.LOGINED) {
					LoginParam loginParam = LoginManager.getInstance().GetLoginParam();
					if (!loginParam.item.bpemf) {
						/* 风控使用积分发邮件 */
						useIntegral = true;
						updateTvBonusPoint();
					}
				}
			}
			break;
		case REQUEST_SEND_EMF:
			MessageCallbackItem callbackItem = (MessageCallbackItem) msg.obj;
			hideProgressDialog();
			if (msg.arg1 == 1) {// EMF发送成功
				/* 发送成功，添加到现有联系人 */
				FlatToast.showStickToast(mContext, "Sent!",FlatToast.StikyToastType.DONE);
				finish();
			} else {// 发送失败
				sendFail(callbackItem);// 发送失败
			}
			break;
		}
	}
	
	/**
	 * 更新显示积分提示
	 */
	private void updateTvBonusPoint() {
		// TODO Auto-generated method stub
		String learnMore = getResources().getString(R.string.emf_quick_reply_learn_more);
		tvBonusPoint.setText(getResources().getString(R.string.emf_quick_reply_use_integral));
		SpannableString spStr = new SpannableString(learnMore);
		spStr.setSpan(new ClickableSpan() {
            @Override
            public void updateDrawState(TextPaint ds) {
                super.updateDrawState(ds);
                ds.setColor(Color.GRAY);       //设置文件颜色
                ds.setUnderlineText(true);      //设置下划线
            }

            @Override
            public void onClick(View widget) {
	            MaterialDialogAlert dialog = new MaterialDialogAlert(EMFDetailActivity.this);
    			dialog.setMessage(getString(R.string.emf_bp_tips));
    			dialog.addButton(dialog.createButton(getString(R.string.common_btn_ok), null));
    			dialog.show();
            }
	    }, 0, learnMore.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        tvBonusPoint.append(spStr);
        tvBonusPoint.setMovementMethod(LinkMovementMethod.getInstance());//开始响应点击事件
	}

	/**
	 * 发送失败
	 * @param callbackItem 
	 */
	private void sendFail(MessageCallbackItem callbackItem) {
		// TODO Auto-generated method stub
		// 首先判断月费类型
		MemberType type = callbackItem.errItem.memberType;
		if (type == MemberType.NO_FEED_FIRST_MONTHLY_MEMBER|| type == MemberType.NO_FEED_MONTHLY_MEMBER) {
			MonthLyFeeTipItem mMonthLyFeeTipItem = MonthlyFeeManager.getInstance().getMonthLyFeeTipItem(callbackItem.errItem.memberType);
			if (mMonthLyFeeTipItem != null) {
				MonthlyFeeDialog dialog = new MonthlyFeeDialog(this,R.style.ChoosePhotoDialog);
				dialog.setData(mMonthLyFeeTipItem);// 设置数据对象
				if (isActivityVisible()) {
					dialog.show();
				}
			}
		} else {
			// 根据错误码处理
			switch (callbackItem.errno) {
			case RequestErrorCode.MBCE10003: {
				// 弹出充值页面
				final GetMoreCreditDialog dialog = new GetMoreCreditDialog(mContext, R.style.ChoosePhotoDialog);
				if (isActivityVisible()) {
					dialog.show();
				}
			}
				break;
			default:{//其他错误
				// 网络超时, 或者其他错误
				if (!mErrorDialog.isShowing()) {
					if (isActivityVisible()) {
						mErrorDialog.show();
					}
				}
				break;
			}
			}
		}
	}

	/**
	 * 收件箱详情数据更新
	 * 
	 * @param item
	 */
	private void onGetInboxDetailSuccess() {
		if (mEMFInboxMsgItem != null) {
			includeQuickReply.setVisibility(View.VISIBLE);
			tvEMFdetail.setText(Html.fromHtml(mEMFInboxMsgItem.body));
			tvEMFdetail.setVisibility(View.VISIBLE);
			if ((mEMFInboxMsgItem.notetoman != null)
					&& (!mEMFInboxMsgItem.notetoman.equals(""))) {
				llTranslator.setVisibility(View.VISIBLE);
				tvMessage.setText(mEMFInboxMsgItem.notetoman);
			}

			btnReply.setVisibility(View.VISIBLE);

			/* 初始化附件列表 */
			mAttachList.clear();
			if (mEMFInboxMsgItem.photosURL != null) {
				for (int i = 0; i < mEMFInboxMsgItem.photosURL.length; i++) {
					EMFAttachmentBean normalItem = new EMFAttachmentBean();
					normalItem.type = AttachType.NORAML_PICTURE;
					normalItem.photoUrl = mEMFInboxMsgItem.photosURL[i];
					mAttachList.add(normalItem);
				}
			}

			if (mEMFInboxMsgItem.privatePhotos != null) {
				for (int i = 0; i < mEMFInboxMsgItem.privatePhotos.length; i++) {
					EMFAttachmentBean privateItem = new EMFAttachmentBean();
					privateItem.type = AttachType.PRIVATE_PHOTO;
					privateItem.privatePhoto.messageid = mEMFInboxMsgItem.id;
					privateItem.privatePhoto.womanid = mEMFInboxMsgItem.womanid;
					privateItem.privatePhoto.sendId = mEMFInboxMsgItem.privatePhotos[i].sendId;
					privateItem.privatePhoto.photoId = mEMFInboxMsgItem.privatePhotos[i].photoId;
					privateItem.privatePhoto.photoFee = mEMFInboxMsgItem.privatePhotos[i].photoFee;
					privateItem.privatePhoto.photoDesc = mEMFInboxMsgItem.privatePhotos[i].photoDesc;
					mAttachList.add(privateItem);

					PrivatePhotoMode mode = PrivatePhotoMode.MODE_DISTINCT;
					if(!privateItem.privatePhoto.photoFee){
						mode = PrivatePhotoMode.MODE_BLUR;
					}
					// 是否已经购买的私密照
//					if (privateItem.privatePhoto.photoFee) {
						// 生成缓存路径
						String localPhotoPath = FileCacheManager.getInstance()
								.CachePrivatePhotoImagePath(
										privateItem.privatePhoto.sendId,
										privateItem.privatePhoto.photoId,
										PrivatePhotoType.LARGE, mode);

						// 是否已经缓存
						File file = new File(localPhotoPath);
						if (file.exists() && file.isFile()) {
							// 已经下载过, 直接显示
						} else {
							// 请求接口获取私密照
							PrivatePhotoView(privateItem.privatePhoto);
						}
					}
//				}
			}

			// 小视屏添加
			if (mEMFInboxMsgItem.shortVideos != null) {
				for (int i = 0; i < mEMFInboxMsgItem.shortVideos.length; i++) {
					EMFAttachmentBean shortVideoItem = new EMFAttachmentBean();
					shortVideoItem.type = AttachType.SHORT_VIDEO;
					shortVideoItem.shortVideo.messageid = mEMFInboxMsgItem.id;
					shortVideoItem.shortVideo.womanid = mEMFInboxMsgItem.womanid;
					shortVideoItem.shortVideo.sendId = mEMFInboxMsgItem.shortVideos[i].sendId;
					shortVideoItem.shortVideo.videoId = mEMFInboxMsgItem.shortVideos[i].videoId;
					shortVideoItem.shortVideo.videoFee = mEMFInboxMsgItem.shortVideos[i].videoFee;
					shortVideoItem.shortVideo.videoDesc = mEMFInboxMsgItem.shortVideos[i].videoDesc;
					mAttachList.add(shortVideoItem);

					/* 下载视频图片 */
					String localPhotoPath = mEMFVideoManager
							.getVideoThumbPhotoPath(
									shortVideoItem.shortVideo.womanid,
									shortVideoItem.shortVideo.sendId,
									shortVideoItem.shortVideo.videoId,
									shortVideoItem.shortVideo.messageid,
									VideoPhotoType.Big);

					// 是否已经缓存
					File file = new File(localPhotoPath);
					if (file.exists() && file.isFile()) {
						// 已经下载过, 直接显示
					} else {
						// 请求接口获取私密照
						GetVideoThumbPhoto(shortVideoItem.shortVideo);
					}
				}
			}

			if ((mEMFInboxMsgItem.vgId != null)
					&& (!mEMFInboxMsgItem.vgId.equals(""))) {
				EMFAttachmentBean virtualItem = new EMFAttachmentBean();
				virtualItem.type = AttachType.VIRTUAL_GIFT;
				virtualItem.vgId = mEMFInboxMsgItem.vgId;
				mAttachList.add(virtualItem);
			}
		}

	}

	/**
	 * 收件箱详情数据更新
	 * 
	 * @param item
	 */
	private void onGetOutboxDetailSuccess() {
		if (mEMFOutboxMsgItem != null) {
			tvEMFdetail.setText(Html.fromHtml(mEMFOutboxMsgItem.content));
			tvEMFdetail.setVisibility(View.VISIBLE);
			btnReply.setVisibility(View.VISIBLE);
			/* 初始化附件列表 */
			mAttachList.clear();
			if (mEMFOutboxMsgItem.photosURL != null) {
				for (int i = 0; i < mEMFOutboxMsgItem.photosURL.length; i++) {
					EMFAttachmentBean normalItem = new EMFAttachmentBean();
					normalItem.type = AttachType.NORAML_PICTURE;
					normalItem.photoUrl = mEMFOutboxMsgItem.photosURL[i];
					mAttachList.add(normalItem);
				}
			}

			if (mEMFOutboxMsgItem.privatePhotos != null) {
				for (int i = 0; i < mEMFOutboxMsgItem.privatePhotos.length; i++) {
					EMFAttachmentBean privateItem = new EMFAttachmentBean();
					privateItem.type = AttachType.PRIVATE_PHOTO;
					privateItem.privatePhoto.messageid = mEMFOutboxMsgItem.id;
					privateItem.privatePhoto.womanid = mEMFOutboxMsgItem.womanid;
					privateItem.privatePhoto.sendId = mEMFOutboxMsgItem.privatePhotos[i].sendId;
					privateItem.privatePhoto.photoId = mEMFOutboxMsgItem.privatePhotos[i].photoId;
					privateItem.privatePhoto.photoFee = mEMFOutboxMsgItem.privatePhotos[i].photoFee;
					privateItem.privatePhoto.photoDesc = mEMFOutboxMsgItem.privatePhotos[i].photoDesc;
					mAttachList.add(privateItem);

					// 是否已经购买的私密照
					PrivatePhotoMode mode = PrivatePhotoMode.MODE_DISTINCT;
					if(!privateItem.privatePhoto.photoFee){
						mode = PrivatePhotoMode.MODE_BLUR;
					}
//					if (privateItem.privatePhoto.photoFee) {
						// 生成缓存路径
						String localPhotoPath = FileCacheManager.getInstance()
								.CachePrivatePhotoImagePath(
										privateItem.privatePhoto.sendId,
										privateItem.privatePhoto.photoId,
										PrivatePhotoType.LARGE, mode);

						// 是否已经缓存
						File file = new File(localPhotoPath);
						if (file.exists() && file.isFile()) {
							// 已经下载过, 直接显示
						} else {
							// 请求接口获取私密照
							PrivatePhotoView(privateItem.privatePhoto);
						}
					}
//				}
			}

			if ((mEMFOutboxMsgItem.vgid != null)
					&& (!mEMFOutboxMsgItem.vgid.equals(""))) {
				EMFAttachmentBean virtualItem = new EMFAttachmentBean();
				virtualItem.type = AttachType.VIRTUAL_GIFT;
				virtualItem.vgId = mEMFOutboxMsgItem.vgid;
				mAttachList.add(virtualItem);
			}
		}
	}

	/**
	 * 收件箱详情数据更新
	 * 
	 * @param item
	 */
	private void onGetAdmirerDetailSuccess(EMFAdmirerViewerItem item) {
		tvEMFdetail.setText(Html.fromHtml(item.body));
		tvEMFdetail.setVisibility(View.VISIBLE);
		btnReply.setVisibility(View.VISIBLE);
		/* 初始化附件列表 */
		mAttachList.clear();
		if (item.photosURL != null) {
			for (int i = 0; i < item.photosURL.length; i++) {
				EMFAttachmentBean normalItem = new EMFAttachmentBean();
				normalItem.type = AttachType.NORAML_PICTURE;
				normalItem.photoUrl = item.photosURL[i];
				mAttachList.add(normalItem);
			}
		}
		if ((item.vgId != null)
				&& (!item.vgId.equals(""))) {
			EMFAttachmentBean virtualItem = new EMFAttachmentBean();
			virtualItem.type = AttachType.VIRTUAL_GIFT;
			virtualItem.vgId = item.vgId;
			mAttachList.add(virtualItem);
		}
	}

	/**
	 * 获取邮件详情失败统一处理
	 */
	private void onGetDeatilFailed(Object object, int memberType) {
		RequestFailBean error = (RequestFailBean) object;
		//先判断月费类型
		MemberType type = RequestJniMonthlyFee.intToMemberType(memberType);
		if(type == MemberType.NO_FEED_FIRST_MONTHLY_MEMBER|| type == MemberType.NO_FEED_MONTHLY_MEMBER){
			includeError.setVisibility(View.GONE);
			includeMonthlyError.setVisibility(View.VISIBLE);
			MonthlyFeeManager.getInstance().onMemberTypeUpdate(type);
			MonthLyFeeTipItem monthLyFeeTipItem = MonthlyFeeManager.getInstance().getMonthLyFeeTipItem(type);
			tvPrice.setText(Html.fromHtml(monthLyFeeTipItem.priceDescribe));
			listView.setAdapter(new MonthlyFeeNotifyAdapter(this, monthLyFeeTipItem.tips));
			btnSubscribe.setOnClickListener(this);
		}else{ 
			includeError.setVisibility(View.VISIBLE);
			includeMonthlyError.setVisibility(View.GONE);
			if (error.errno.equals(RequestErrorCode.MBCE8012)) {
				// 信用点不足
				tvErrorMessage.setText(getString(R.string.emf_credit_not_enough));
				btnErrorOperate
						.setButtonTitle(getString(R.string.emf_purshase_credits));
				btnErrorOperate.setOnClickListener(new OnClickListener() {
	
					@Override
					public void onClick(View v) {
						// 跳去充值模块
						Intent intent = new Intent(EMFDetailActivity.this,
								BuyCreditActivity.class);
						startActivity(intent);
					}
				});
			} else if (error.errno
					.equals(RequestErrorCode.LOCAL_ERROR_CODE_TIMEOUT)) {
				// 网络出错
				tvErrorMessage.setText(getString(R.string.common_network_error));
				btnErrorOperate
						.setButtonTitle(getString(R.string.common_btn_retry));
				btnErrorOperate.setOnClickListener(new OnClickListener() {
	
					@Override
					public void onClick(View v) {
						// 重试
						getEmfDetail(emfBean.id);
					}
				});
			} else {
				// 其他错误算信件错误
				tvErrorMessage.setText(getString(R.string.emf_all_other_error));
				btnErrorOperate
						.setButtonTitle(getString(R.string.common_btn_close));
				btnErrorOperate.setOnClickListener(new OnClickListener() {
	
					@Override
					public void onClick(View v) {
						finish();
					}
				});
			}
		}
	}

	/**
	 * 获取邮箱详情
	 * 
	 * @param messageId
	 */
	private void getEmfDetail(String messageId) {
		showProgressDialog(getString(R.string.common_loading_tips));
		switch (emfBean.type) {
		case 0:
			getEmfInboxDetail(messageId);
			break;
		case 1:
			getEmfOutBoxDetail(messageId);
			break;
		case 2:
			getAdmirerDetail(messageId);
			break;
		}
	}

	/**
	 * 获取收件箱邮件详情
	 * 
	 * @param messageId
	 */
	private void getEmfInboxDetail(String messageId) {
		RequestOperator.getInstance().InboxMsg(messageId, this);
	}

	/**
	 * 获取发件箱邮件详情
	 * 
	 * @param messageId
	 */
	private void getEmfOutBoxDetail(String messageId) {
		RequestOperator.getInstance().OutboxMsg(messageId, this);
	}

	/**
	 * 获取意向信详情
	 * 
	 * @param messageId
	 */
	private void getAdmirerDetail(String messageId) {
		RequestOperator.getInstance().AdmirerViewer(messageId, this);
	}

	private void onEmfDelete() {
		showProgressDialog(getString(R.string.common_loading_tips));
		switch (emfBean.type) {
		case 0:
			deleteEMF(emfBean.id, MailType.INBOX);
			break;
		case 1:
			deleteEMF(emfBean.id, MailType.OUTBOX);
			break;
		case 2:
			deleteEMF(emfBean.id, MailType.ADMIRER);
			break;
		}
	}

	/**
	 * 查询是否可以使用积分
	 * 
	 * @param womanId
	 *            女士Id
	 */
	private void integralCheck(String womanId) {
		RequestOperator.getInstance().IntegralCheck(womanId, this);
	}

	// /**
	// * 添加到黑名单
	// */
	// private void addToBlock(String womanid, BlockReasonType blockReasonType){
	// showToastProgressing("Loading");
	// RequestOperator.getInstance().Block(womanid, blockReasonType, new
	// OnEMFBlockCallback() {
	//
	// @Override
	// public void OnEMFBlock(boolean isSuccess, String errno, String errmsg) {
	// /*添加女士到黑名单*/
	// Message msg = Message.obtain();
	// if(isSuccess){
	// msg.what = BLOCK_WOMAN_SUCCESS;
	// }else{
	// msg.what = BLOCK_WOMAN_FAILED;
	// RequestFailBean bean = new RequestFailBean(errno,
	// errmsg);
	// msg.obj = bean;
	// }
	// sendUiMessage(msg);
	// }
	// });
	// }

	/**
	 * 删除邮件
	 * 
	 * @param emfId
	 *            邮件Id
	 * @param type
	 *            邮件类型
	 */
	private void deleteEMF(String emfId, MailType type) {
		RequestOperator.getInstance().DeleteMsg(emfId, type, this);
	}

	/* attchment 处理 */
	private void initAttachment() {

		if (mAttachList != null && mAttachList.size() > 0 && emfBean!=null) {
			EMFAttachmentAdapter mAdapter = new EMFAttachmentAdapter(this,mAttachList,emfBean);
			gvAttachment.setAdapter(mAdapter);
			llAttachments.setVisibility(View.VISIBLE);
		} 

		// if (mAttachList != null) {
		// llAttachment.setVisibility(View.VISIBLE);
		// llAttachment.removeAllViews();
		// for (int i = 0; i < mAttachList.size(); i++) {
		//
		// View v =
		// LayoutInflater.from(this).inflate(R.layout.item_emf_attachment_preview,
		// null);
		// FitTopImageView iv = (FitTopImageView) v.findViewById(R.id.image);
		// ImageButton play = (ImageButton) v.findViewById(R.id.play_button);
		// ImageView gift_mark = (ImageView) v.findViewById(R.id.gift_mark);
		//
		// iv.setOnClickListener(this);
		// iv.setOnClickListener(this);
		// iv.setTag(i);
		// play.setTag(i);
		//
		// String url = "";
		// String localPath = "";//
		// FileCacheManager.getInstance().CacheImagePathFromUrl(mAttachList.get(i).photoUrl);
		//
		// EMFAttachmentBean item = mAttachList.get(i);
		//
		// if (item.type.equals(AttachType.VIRTUAL_GIFT)) {
		// iv.setImageResource(R.drawable.attachment_gift_unloaded_110_150dp);
		// gift_mark.setVisibility(View.VISIBLE);
		// url =
		// VirtualGiftManager.getInstance().GetVirtualGiftImage(item.vgId);
		// localPath =
		// VirtualGiftManager.getInstance().CacheVirtualGiftImagePath(item.vgId);
		// } else if (item.type.equals(AttachType.PRIVATE_PHOTO)) {
		//
		// if (emfBean.type == 1) {
		// // 發送
		// iv.setImageResource(R.drawable.attachment_photo_unloaded_110_150dp);
		// } else {
		// // 收到
		// iv.setImageResource(R.drawable.private_photo_unviewed_110_150dp);
		// }
		//
		// localPath =
		// FileCacheManager.getInstance().CachePrivatePhotoImagePath(
		// item.privatePhoto.sendId,
		// item.privatePhoto.photoId,
		// PrivatePhotoType.LARGE);
		// } else if (item.type.equals(AttachType.SHORT_VIDEO)) {
		// iv.setImageResource(R.drawable.attachment_photo_unloaded_110_150dp);
		// play.setVisibility(View.VISIBLE);
		// localPath = mEMFVideoManager.getVideoThumbPhotoPath(
		// item.shortVideo.womanid, item.shortVideo.sendId,
		// item.shortVideo.videoId, item.shortVideo.messageid,
		// VideoPhotoType.Big);
		// } else {
		// iv.setImageResource(R.drawable.attachment_photo_unloaded_110_150dp);
		// url = item.photoUrl;
		// localPath =
		// FileCacheManager.getInstance().CacheImagePathFromUrl(url);
		// }
		//
		// llAttachment.addView(v);
		//
//		new ImageViewLoader(this).DisplayImage(iv, url, localPath,
//				UnitConversion.dip2px(this, 150),
//				UnitConversion.dip2px(this, 110), null);
		// }
		// }
	}

	private void notifyEmfList(boolean isDelete, boolean isRead) {
		Intent intent = new Intent();
		intent.putExtra(EMF_MESSAGEID, emfBean.id);
		intent.putExtra(EMF_DELETE, isDelete);
		intent.putExtra(EMF_DETAIL_READED, isRead);
		setResult(RESULT_OK, intent);
	}

	// private void removeAttachments() {
	// if (llAttachment != null) {
	// llAttachment.removeAllViews();
	// }
	// }

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		switch (requestCode) {
		case RESULT_ATTACHMENT: {
			// 编辑个人简介返回
			if (resultCode == RESULT_OK) {
				boolean needReload = data.getExtras().getBoolean(
						EMFAttachmentPreviewActivity.NEED_RELOAD);
				if (needReload) {
					/* 获取邮件详情 */
					getEmfDetail(emfBean.id);
				}
			}
		}
			break;
		default:
			break;
		}
	}

	/**
	 * 获取已购买的私密照
	 */
	public void PrivatePhotoView(PrivatePhotoBean item) {
		
		PrivatePhotoMode mode = PrivatePhotoMode.MODE_DISTINCT;
		if(!item.photoFee){
			mode = PrivatePhotoMode.MODE_BLUR;
		}
		// 生成缓存路径
		String localPhotoPath = FileCacheManager.getInstance()
				.CachePrivatePhotoImagePath(item.sendId, item.photoId,
						PrivatePhotoType.LARGE, mode);
		
		RequestOperator.getInstance().PrivatePhotoView(item.womanid,
				item.photoId, item.sendId, item.messageid, localPhotoPath,
				PrivatePhotoType.LARGE, mode, this);
	}

	/**
	 * 获取收件箱详情小视频缩略图
	 * 
	 * @param item
	 */
	private void GetVideoThumbPhoto(ShortVideoBean item) {
		if(item != null){
			mEMFVideoManager.GetVideoThumbPhoto(item.womanid, item.sendId,
					item.videoId, item.messageid, VideoPhotoType.Big, new OnRequestFileCallback() {
						
						@Override
						public void OnRequestFile(long requestId, boolean isSuccess, String errno,
								String errmsg, String filePath) {
							Message msg = Message.obtain();
							if (isSuccess) {
								// 成功
								msg.what = REQUEST_GET_VIDEOPHOTO_SUCCESS;
							} else {
								// 失败
								msg.what = REQUEST_GET_VIDEOPHOTO_FAIL;
							}
							sendUiMessage(msg);
						}
					});
		}
	}

	@Override
	/**
	 * MaterialDropDownMenu.OnClickCallback callback
	 */
	public void onClick(AdapterView<?> adptView, View v, int which) {
		// TODO Auto-generated method stub
		MaterialDialogAlert dialog = new MaterialDialogAlert(
				EMFDetailActivity.this);
		dialog.setMessage(getString(R.string.emf_delete_confirm));
		dialog.addButton(dialog.createButton(
				getString(R.string.common_btn_cancel), null));
		dialog.addButton(dialog.createButton(
				getString(R.string.common_btn_yes), new OnClickListener() {

					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						onEmfDelete();
					}

				}));

		dialog.show();
	}

	/**
	 * 根据女士Id保存邮件内容
	 */
	private void SaveMailBody() {
		if (TextUtils.isEmpty(emfBean.womanid)
				&& etReply.getText().toString() != null) {
			SharedPreferences mSharedPreferences = getSharedPreferences(
					"base64", Context.MODE_PRIVATE);

			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ObjectOutputStream oos;
			try {
				oos = new ObjectOutputStream(baos);
				oos.writeObject(etReply.getText().toString());
				String personBase64 = new String(Base64.encode(
						baos.toByteArray(), Base64.DEFAULT));
				SharedPreferences.Editor editor = mSharedPreferences.edit();
				editor.putString(emfBean.womanid, personBase64);
				editor.commit();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	@Override
	public void OnEMFInboxMsg(boolean isSuccess, String errno, String errmsg, int memberType,
			EMFInboxMsgItem item) {
		// TODO Auto-generated method stub
		Message msg = Message.obtain();
		if (isSuccess) {
			msg.what = GET_EMF_DETAIL_SUCCESS;
			msg.obj = item;
		} else {
			msg.what = GET_EMF_DETAIL_FAILED;
			RequestFailBean bean = new RequestFailBean(errno, errmsg);
			msg.obj = bean;
			msg.arg1 = memberType;
		}
		sendUiMessage(msg);
	}

	@Override
	public void OnEMFOutboxMsg(boolean isSuccess, String errno, String errmsg,
			EMFOutboxMsgItem item) {
		// TODO Auto-generated method stub
		Message msg = Message.obtain();
		if (isSuccess) {
			msg.what = GET_EMF_DETAIL_SUCCESS;
			msg.obj = item;
		} else {
			msg.what = GET_EMF_DETAIL_FAILED;
			RequestFailBean bean = new RequestFailBean(errno, errmsg);
			msg.obj = bean;
			msg.arg1 = MemberType.NORMAL_MEMBER.ordinal();
		}
		sendUiMessage(msg);
	}

	@Override
	public void OnEMFAdmirerViewer(boolean isSuccess, String errno,
			String errmsg, EMFAdmirerViewerItem item) {
		// TODO Auto-generated method stub
		Message msg = Message.obtain();
		if (isSuccess) {
			msg.what = GET_EMF_DETAIL_SUCCESS;
			msg.obj = item;
		} else {
			msg.what = GET_EMF_DETAIL_FAILED;
			RequestFailBean bean = new RequestFailBean(errno, errmsg);
			msg.obj = bean;
			msg.arg1 = MemberType.NORMAL_MEMBER.ordinal();
		}
		sendUiMessage(msg);
	}

	@Override
	public void OnEMFDeleteMsg(boolean isSuccess, String errno, String errmsg) {
		// TODO Auto-generated method stub
		Message msg = Message.obtain();
		if (isSuccess) {
			msg.what = DELETE_EMF_SUCCESS;
		} else {
			msg.what = DELETE_EMF_FAILED;
			RequestFailBean bean = new RequestFailBean(errno, errmsg);
			msg.obj = bean;
		}
		sendUiMessage(msg);
	}

	@Override
	public void OnEMFPrivatePhotoView(boolean isSuccess, String errno,
			String errmsg, String filePath) {
		// TODO Auto-generated method stub
		Message msg = Message.obtain();
		if (isSuccess) {
			// 成功
			msg.what = REQUEST_GET_PHOTO_SUCCESS;
		} else {
			// 失败
			msg.what = REQUEST_GET_PHOTO_FAIL;
		}
		sendUiMessage(msg);
	}

	@Override
	public void InitView() {
		// TODO Auto-generated method stub

	}

	@Override
	public void OnOtherIntegralCheck(boolean isSuccess, String errno,
			String errmsg, OtherIntegralCheckItem item) {
		// TODO Auto-generated method stub
		Message msg = Message.obtain();
		msg.what = REQUEST_GET_BOUNDS;
		msg.arg1 = isSuccess ? 1 : 0;
		sendUiMessage(msg);
	}

	@Override
	public void OnEMFSendMsg(boolean isSuccess, String errno, String errmsg,
			EMFSendMsgItem item, EMFSendMsgErrorItem errItem) {
		// TODO Auto-generated method stub
		Message msg = Message.obtain();
		msg.what = REQUEST_SEND_EMF;
		msg.arg1 = isSuccess ? 1 : 0;
		MessageCallbackItem callbackItem = new MessageCallbackItem(errno,
				errmsg);
		callbackItem.sendItem = item;
		callbackItem.errItem = errItem;
		msg.obj = callbackItem;
		sendUiMessage(msg);
	}

}
