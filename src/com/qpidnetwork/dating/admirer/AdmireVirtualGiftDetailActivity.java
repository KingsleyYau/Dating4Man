package com.qpidnetwork.dating.admirer;

import java.io.File;
import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
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
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.qpidnetwork.dating.R;
import com.qpidnetwork.dating.admirer.ObservableScrollView.ScrollViewListener;
import com.qpidnetwork.dating.admirer.VirtualGiftView.OnVirtualGiftPlayCallback;
import com.qpidnetwork.dating.authorization.LoginManager;
import com.qpidnetwork.dating.authorization.LoginManager.LoginStatus;
import com.qpidnetwork.dating.authorization.LoginParam;
import com.qpidnetwork.dating.bean.EMFAttachmentBean;
import com.qpidnetwork.dating.bean.EMFAttachmentBean.AttachType;
import com.qpidnetwork.dating.bean.EMFBean;
import com.qpidnetwork.dating.bean.RequestBaseResponse;
import com.qpidnetwork.dating.credit.BuyCreditActivity;
import com.qpidnetwork.dating.emf.EMFAttachmentAdapter;
import com.qpidnetwork.dating.emf.EMFAttachmentPreviewActivity;
import com.qpidnetwork.dating.emf.EMFAttachmentPrivatePhotoFragment.PrivatePhotoDirection;
import com.qpidnetwork.dating.emf.EMFDetailActivity;
import com.qpidnetwork.dating.emf.MailEditActivity;
import com.qpidnetwork.dating.lady.LadyDetailActivity;
import com.qpidnetwork.framework.base.BaseFragmentActivity;
import com.qpidnetwork.framework.widget.CircleImageView;
import com.qpidnetwork.manager.FileCacheManager;
import com.qpidnetwork.manager.MonthlyFeeManager;
import com.qpidnetwork.manager.VirtualGiftManager;
import com.qpidnetwork.request.OnEMFAdmirerViewerCallback;
import com.qpidnetwork.request.OnEMFDeleteMsgCallback;
import com.qpidnetwork.request.OnEMFSendMsgCallback;
import com.qpidnetwork.request.OnOtherIntegralCheckCallback;
import com.qpidnetwork.request.RequestErrorCode;
import com.qpidnetwork.request.RequestJniEMF.MailType;
import com.qpidnetwork.request.RequestJniEMF.ReplyType;
import com.qpidnetwork.request.RequestJniMonthlyFee.MemberType;
import com.qpidnetwork.request.RequestOperator;
import com.qpidnetwork.request.item.EMFAdmirerViewerItem;
import com.qpidnetwork.request.item.EMFSendMsgErrorItem;
import com.qpidnetwork.request.item.EMFSendMsgItem;
import com.qpidnetwork.request.item.LadyDetail;
import com.qpidnetwork.request.item.MonthLyFeeTipItem;
import com.qpidnetwork.request.item.OtherIntegralCheckItem;
import com.qpidnetwork.tool.FileDownloader;
import com.qpidnetwork.tool.FileDownloader.FileDownloaderCallback;
import com.qpidnetwork.tool.ImageViewLoader;
import com.qpidnetwork.tool.ImageViewLoader.ImageViewLoaderCallback;
import com.qpidnetwork.view.ButtonRaised;
import com.qpidnetwork.view.FlatToast;
import com.qpidnetwork.view.GetMoreCreditDialog;
import com.qpidnetwork.view.MaterialAppBar;
import com.qpidnetwork.view.MaterialDialogAlert;
import com.qpidnetwork.view.MaterialDropDownMenu;
import com.qpidnetwork.view.MaterialProgressBar;
import com.qpidnetwork.view.MonthlyFeeDialog;

public class AdmireVirtualGiftDetailActivity extends BaseFragmentActivity
		implements MaterialDropDownMenu.OnClickCallback,
		ImageViewLoaderCallback, FileDownloaderCallback,
		OnVirtualGiftPlayCallback, ScrollViewListener,
		OnOtherIntegralCheckCallback, OnEMFSendMsgCallback {

	private static final int GET_EMF_DETAIL_CALLBACK = 1;
	private static final int DELETE_EMF_CALLBACK = 2;
	private static final int DOWNLOAD_GIFT_THUMB_CALLBACK = 3;
	private static final int DOWNLOAD_GIFT_3GP_CALLBACK = 4;
	private static final int REQUEST_GET_BOUNDS = 5;
	private static final int REQUEST_SEND_EMF = 6;

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

	// title
	private MaterialAppBar appbar;

	// EMF 主要内容
	private ObservableScrollView scrollView;
	private VirtualGiftView vgPlayer;
	private ImageView vgBg;
	private MaterialProgressBar progressBar;
	private TextView tvRetry;
	private ImageButton buttonPlay;
	private TextView tvEMFdetail;
	private LinearLayout llAttachments;
	private GridView gvAttachment;// 附件列表
	private ButtonRaised btnReply;

	// ------Quick Replay-------
	private ImageView ivQuickReply;
	private EditText etReply;
	private ButtonRaised btnSend;
	private TextView tvBonusPoint;
	private TextView tvLearnMore;

	// error page
	private View includeError;
	private TextView tvErrorMessage;
	private ButtonRaised btnErrorOperate;

	// 虚拟礼物处理下载相关
	private ImageViewLoader loader; // 虚拟礼物图片下载器
	private FileDownloader mFileDownloader;// 3GP视频下载器

	// data
	private EMFBean emfBean;

	private boolean useIntegral = false;// 是否使用积分
	private MaterialDialogAlert mErrorDialog;

	/* 存放附件列表 */
	private ArrayList<EMFAttachmentBean> mAttachList;
	
	private EMFAdmirerViewerItem admireDetail = null;
	
	private boolean isDownThumbSuccess = false;
	private boolean isDown3gpSuccess = false;
	
	public static Intent getIntent(Context context, EMFBean emfBean) {
		Intent intent = new Intent(context, AdmireVirtualGiftDetailActivity.class);
		intent.putExtra(EMFDetailActivity.EMF_BASE_BEAN, emfBean);
		return intent;
	}
	
	@Override
	protected void onCreate(Bundle arg0) {
		// TODO Auto-generated method stub
		super.onCreate(arg0);
	}

	@Override
	public void InitView() {
		setContentView(R.layout.activity_admire_detail_virtualgift);
		
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
						sendAdmireTextMessage();
					}
				}));

		appbar = (MaterialAppBar) findViewById(R.id.appbar);
		appbar.setAppbarBackgroundColor(getResources().getColor(
				R.color.theme_actionbar_secoundary));
		appbar.setOnButtonClickListener(this);
		appbar.setTouchFeedback(MaterialAppBar.TOUCH_FEEDBACK_HOLO_LIGHT);
		appbar.addButtonToLeft(R.id.common_button_reply, "reply",
				R.drawable.ic_reply_all_grey600_24dp);
		appbar.addButtonToLeft(R.id.common_button_back, "back",
				R.drawable.ic_arrow_back_grey600_24dp);

		appbar.addOverflowButton(
				new String[] { getString(R.string.emf_menu_delete) }, this,
				R.drawable.ic_more_vert_grey600_24dp);

		/* EMF 主要内容 */
		scrollView = (ObservableScrollView) findViewById(R.id.scrollView);
		if (android.os.Build.VERSION.SDK_INT <= android.os.Build.VERSION_CODES.HONEYCOMB) {
			scrollView.setScrollViewListener(this);
		}
		vgPlayer = (VirtualGiftView) findViewById(R.id.vgPlayer);
		vgBg = (ImageView) findViewById(R.id.vgBg);
		progressBar = (MaterialProgressBar) findViewById(R.id.progressBar);
		tvRetry = (TextView) findViewById(R.id.tvRetry);
		buttonPlay = (ImageButton) findViewById(R.id.buttonPlay);
		tvEMFdetail = (TextView) findViewById(R.id.tvEMFdetail);
		llAttachments = (LinearLayout) findViewById(R.id.llAttachments);
		gvAttachment = (GridView) findViewById(R.id.gvAttachment);
		btnReply = (ButtonRaised) findViewById(R.id.btnReply);
		btnReply.setOnClickListener(this);
		
		/* btnReply获取焦点避免scrollView滚动到gridView位置 */
		btnReply.setFocusable(true);
		btnReply.setFocusableInTouchMode(true);
		btnReply.requestFocus();
		
		vgPlayer.setOnVirtualGiftPlayCallback(this);
		tvRetry.setOnClickListener(this);
		buttonPlay.setOnClickListener(this);

		/* Quick Reply */
		ivQuickReply = (ImageView) findViewById(R.id.ivQuickReply);
		etReply = (EditText) findViewById(R.id.etReply);
		btnSend = (ButtonRaised) findViewById(R.id.btnSend);
		tvBonusPoint = (TextView) findViewById(R.id.tvBonusPoint);
		tvLearnMore = (TextView) findViewById(R.id.tvLearnMore);
		ivQuickReply.setOnClickListener(this);
		btnSend.setOnClickListener(this);
		tvLearnMore.setOnClickListener(this);

		/* Error */
		includeError = (View) findViewById(R.id.includeError);
		tvErrorMessage = (TextView) findViewById(R.id.tvErrorMessage);
		btnErrorOperate = (ButtonRaised) findViewById(R.id.btnErrorOperate);
		
		//初始化下载器
		mFileDownloader = new FileDownloader(this);
		loader = new ImageViewLoader(this);
		
		initData();
	}
	
	private void initData(){
		mAttachList = new ArrayList<EMFAttachmentBean>();
	
		Bundle bundle = getIntent().getExtras();
		if ((bundle != null) && (bundle.containsKey(EMFDetailActivity.EMF_BASE_BEAN))) {
			emfBean = bundle.getParcelable(EMFDetailActivity.EMF_BASE_BEAN);
		}
		if(emfBean != null){
			//处理信件头部
			((TextView) findViewById(R.id.tvName)).setText(emfBean.firstname);
			((TextView) findViewById(R.id.tvAge)).setText(emfBean.age + "");
			((TextView) findViewById(R.id.tvCountry)).setText(emfBean.country);
			((TextView) findViewById(R.id.tvDesc)).setText(R.string.emf_to_me);
			((TextView) findViewById(R.id.tvDate)).setText(emfBean.sendTime);
			
			CircleImageView ivPhoto = (CircleImageView) findViewById(R.id.ivPhoto);
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
			getAdmireDetail();
		}
	}
	
	private void getAdmireDetail(){
		showProgressDialog(getString(R.string.common_loading_tips));
		RequestOperator.getInstance().AdmirerViewer(emfBean.id, new OnEMFAdmirerViewerCallback() {
			
			@Override
			public void OnEMFAdmirerViewer(boolean isSuccess, String errno,
					String errmsg, EMFAdmirerViewerItem item) {
				Message msg = Message.obtain();
				msg.what = GET_EMF_DETAIL_CALLBACK;
				RequestBaseResponse response = new RequestBaseResponse(isSuccess, errno, errmsg, item);
				msg.obj = response;
				sendUiMessage(msg);
			}
		});
	}
	
	@Override
	protected void handleUiMessage(Message msg) {
		// TODO Auto-generated method stub
		super.handleUiMessage(msg);
		hideProgressDialog();
		switch (msg.what) {
		case GET_EMF_DETAIL_CALLBACK: {
			RequestBaseResponse response = (RequestBaseResponse) msg.obj;
			if (response.isSuccess) {
				includeError.setVisibility(View.GONE);
				admireDetail = (EMFAdmirerViewerItem) response.body;
				onGetAdmirerDetailSuccess(admireDetail);
				initAttachment();
				/* 已读，通知列表刷新状态 */
				notifyEmfList(false, true);
			} else {
				includeError.setVisibility(View.VISIBLE);
				onGetDeatilFailed(response);
			}
		}
			break;

		case DELETE_EMF_CALLBACK: {
			RequestBaseResponse response = (RequestBaseResponse) msg.obj;
			if (response.isSuccess) {
				notifyEmfList(true, false);
				finish();
			} else {
				includeError.setVisibility(View.VISIBLE);
				onGetDeatilFailed(response);
			}
		}
			break;

		case DOWNLOAD_GIFT_THUMB_CALLBACK: {
			RequestBaseResponse response = (RequestBaseResponse) msg.obj;
			isDownThumbSuccess = response.isSuccess;
			progressBar.setVisibility(View.GONE);
			if (response.isSuccess) {
				download3gp();
			} else {
				tvRetry.setVisibility(View.VISIBLE);
			}
		}
			break;

		case DOWNLOAD_GIFT_3GP_CALLBACK: {
			RequestBaseResponse response = (RequestBaseResponse) msg.obj;
			if (response.isSuccess) {
				isDown3gpSuccess = true;
				playVirtualGift();
			} else {
				tvRetry.setVisibility(View.VISIBLE);
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
			if (msg.arg1 == 1) {// EMF发送成功
				/* 发送成功，添加到现有联系人 */
				FlatToast.showStickToast(mContext, "Sent!",FlatToast.StikyToastType.DONE);
				finish();
			} else {
				sendFail(callbackItem);// 发送失败
			}
			break;

		default:
			
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
	            MaterialDialogAlert dialog = new MaterialDialogAlert(AdmireVirtualGiftDetailActivity.this);
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
		if(callbackItem.errItem!=null){
			MemberType type = callbackItem.errItem.memberType;
			if (type == MemberType.NO_FEED_FIRST_MONTHLY_MEMBER|| type == MemberType.NO_FEED_MONTHLY_MEMBER) {
				MonthLyFeeTipItem mMonthLyFeeTipItem = MonthlyFeeManager.getInstance().getMonthLyFeeTipItem(callbackItem.errItem.memberType);
				if (mMonthLyFeeTipItem != null) {
					//弹出月费充值页面
					MonthlyFeeDialog dialog = new MonthlyFeeDialog(this,R.style.ChoosePhotoDialog);
					dialog.setData(mMonthLyFeeTipItem);// 设置数据对象
					if (isActivityVisible()) {
						dialog.show();
					}
				}else{
					showErrorDialog();
				}
			}else if(callbackItem.errno!=null){
				if(callbackItem.errno.equals(RequestErrorCode.MBCE10003)){
					// 弹出充值页面
					final GetMoreCreditDialog dialog = new GetMoreCreditDialog(mContext, R.style.ChoosePhotoDialog);
					if (isActivityVisible()) {
						dialog.show();
					}
				}else{
					showErrorDialog();
				}
			}else{
				showErrorDialog();
			}
		}else {
			showErrorDialog();
		}
//		MemberType type = callbackItem.errItem.memberType;
//		if (type == MemberType.NO_FEED_FIRST_MONTHLY_MEMBER|| type == MemberType.NO_FEED_MONTHLY_MEMBER) {
//			MonthLyFeeTipItem mMonthLyFeeTipItem = MonthlyFeeManager.getInstance().getMonthLyFeeTipItem(callbackItem.errItem.memberType);
//			if (mMonthLyFeeTipItem != null) {
//				MonthlyFeeDialog dialog = new MonthlyFeeDialog(this,R.style.ChoosePhotoDialog);
//				dialog.setData(mMonthLyFeeTipItem);// 设置数据对象
//				if (isActivityVisible()) {
//					dialog.show();
//				}
//			}
//		} else {
//			// 根据错误码处理
//			switch (callbackItem.errno) {
//			case RequestErrorCode.MBCE10003: {
//				// 弹出充值页面
//				final GetMoreCreditDialog dialog = new GetMoreCreditDialog(mContext, R.style.ChoosePhotoDialog);
//				if (isActivityVisible()) {
//					dialog.show();
//				}
//			}
//				break;
//			default:{//其他错误
//				// 网络超时, 或者其他错误
//				if (!mErrorDialog.isShowing()) {
//					if (isActivityVisible()) {
//						mErrorDialog.show();
//					}
//				}
//				break;
//			}
//			}
//		}
	}

	/**
	 * 显示错误dialog
	 */
	private void showErrorDialog() {
		// TODO Auto-generated method stub
		// 网络超时, 或者其他错误
		if (!mErrorDialog.isShowing()) {
			if (isActivityVisible()) {
				mErrorDialog.show();
			}
		}
	}



	/**
	 * 下载成功或点击重新播放
	 */
	private void playVirtualGift(){
		String localVideoPath = VirtualGiftManager.getInstance().CacheVirtualGiftVideoPath(admireDetail.vgId);
		if(!TextUtils.isEmpty(localVideoPath) && (new File(localVideoPath).exists())){
			buttonPlay.setVisibility(View.GONE);
			vgPlayer.init(localVideoPath);
			vgPlayer.Play();
		}
	}
	
	/**
	 * 收件箱详情数据更新
	 * 
	 * @param item
	 */
	private void onGetAdmirerDetailSuccess(EMFAdmirerViewerItem item) {
		//处理虚拟礼物模块
		if(!TextUtils.isEmpty(item.vgId)){
			//有虚拟礼物，先下载缩略图
			downloadVirtualGiftThumb();
		}else{
			//无虚拟礼物
			progressBar.setVisibility(View.GONE);
		}
		
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
	}

	/* attchment 处理 */
	private void initAttachment() {
		if (mAttachList != null && mAttachList.size() > 0 && emfBean != null) {
			EMFAttachmentAdapter mAdapter = new EMFAttachmentAdapter(this,mAttachList, emfBean);
			gvAttachment.setAdapter(mAdapter);
			llAttachments.setVisibility(View.VISIBLE);
		}
		// if (mAttachList != null) {
		// llAttachment.setVisibility(View.VISIBLE);
		// llAttachment.removeAllViews();
		// for (int i = 0; i < mAttachList.size(); i++) {
		//
		// View v = LayoutInflater.from(this).inflate(
		// R.layout.item_emf_attachment_preview, null);
		// FitTopImageView iv = (FitTopImageView) v
		// .findViewById(R.id.image);
		// iv.setTag(i);
		// iv.setOnClickListener(this);
		//
		// EMFAttachmentBean item = mAttachList.get(i);
		// iv.setImageResource(R.drawable.attachment_photo_unloaded_110_150dp);
		// String localPath = FileCacheManager.getInstance()
		// .CacheImagePathFromUrl(item.photoUrl);
		// llAttachment.addView(v);
		// new ImageViewLoader(this).DisplayImage(iv, item.photoUrl, localPath,
		// UnitConversion.dip2px(this, 150),
		// UnitConversion.dip2px(this, 110), null);
		// }
		// }
	}

	@Override
	public void onClick(View v) {
		super.onClick(v);
		switch (v.getId()) {
		case R.id.common_button_back:
			finish();
			break;
		// case R.id.common_button_reply:
		// case R.id.btnReply: {
		// String mTab = emfBean.mtab;
		// ReplyType type = ReplyType.ADMIRE;
		// MailEditActivity.launchMailEditActivity(this, emfBean.womanid,type,
		// mTab, null);
		// }
		// break;
		case R.id.ivPhoto: {
			LadyDetailActivity.launchLadyDetailActivity(this, emfBean.womanid, true);
		}break;
		case R.id.image: {
			Intent intent = EMFAttachmentPreviewActivity.getIntent(this, mAttachList, (Integer) v.getTag());
			intent.putExtra(EMFAttachmentPreviewActivity.VGTIPS, false);
			intent.putExtra(EMFAttachmentPreviewActivity.ATTACH_DIRECTION, 
					(emfBean.type == 1) ? PrivatePhotoDirection.MW.name(): PrivatePhotoDirection.WM.name());
			startActivity(intent);
		}break;
		case R.id.tvRetry: {
			//下载失败
			if(isDownThumbSuccess){
				download3gp();
			}else{
				downloadVirtualGiftThumb();
			}
		}break;
		case R.id.buttonPlay: {
			// 点击播放按钮
		}
			break;
		case R.id.common_button_reply:
		case R.id.btnReply:
		case R.id.ivQuickReply:// 快捷回复带输入内容
			if(TextUtils.isEmpty(emfBean.womanid)){
				break;
			}
			String mTempMessage = etReply.getText().toString();// 保存临时输入文本
			String mTab = emfBean.mtab;
			ReplyType type = ReplyType.ADMIRE;
			MailEditActivity.launchMailEditActivity(this, emfBean.womanid,type, mTab, mTempMessage);
			break;
		case R.id.tvLearnMore:// 了解更多
			MaterialDialogAlert dialog = new MaterialDialogAlert(this);
			dialog.setMessage(getString(R.string.emf_bp_tips));
			dialog.addButton(dialog.createButton(getString(R.string.common_btn_ok), null));
			dialog.show();
			break;
		case R.id.btnSend:// 回复文本
			sendAdmireTextMessage();// 发送文本信件
		default:
			break;
		}
	}

	/**
	 * 发送文本信件
	 */
	private void sendAdmireTextMessage() {
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

		String mTab = "";;
		if(emfBean.mtab!=null){
			mTab = emfBean.mtab;
		}
		ReplyType type = ReplyType.ADMIRE;
		RequestOperator.getInstance().SendMsg(emfBean.womanid, etContent, useIntegral, type, mTab,new String[] {}, new String[] {}, this);
	}

	/**
	 * 通知列表已读
	 * @param isDelete
	 * @param isRead
	 */
	private void notifyEmfList(boolean isDelete, boolean isRead) {
		Intent intent = new Intent();
		intent.putExtra(EMFDetailActivity.EMF_MESSAGEID, emfBean.id);
		intent.putExtra(EMFDetailActivity.EMF_DELETE, isDelete);
		intent.putExtra(EMFDetailActivity.EMF_DETAIL_READED, isRead);
		setResult(RESULT_OK, intent);
	}

	@Override
	public void onClick(AdapterView<?> adptView, View v, int which) {
		MaterialDialogAlert dialog = new MaterialDialogAlert(this);
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
	 * 查询是否可以使用积分
	 * 
	 * @param womanId
	 *            女士Id
	 */
	private void integralCheck(String womanId) {
		RequestOperator.getInstance().IntegralCheck(womanId, this);
	}

	/**
	 * 删除邮件
	 */
	private void onEmfDelete(){
		showProgressDialog(getString(R.string.common_loading_tips));
		RequestOperator.getInstance().DeleteMsg(emfBean.id, MailType.ADMIRER, new OnEMFDeleteMsgCallback() {
			
			@Override
			public void OnEMFDeleteMsg(boolean isSuccess, String errno, String errmsg) {
				Message msg = Message.obtain();
				msg.what = DELETE_EMF_CALLBACK;
				RequestBaseResponse response = new RequestBaseResponse(isSuccess, errno, errmsg, null);
				msg.obj = response;
				sendUiMessage(msg);
			}
		});
	}
	
	/**
	 * 获取邮件详情失败统一处理
	 */
	private void onGetDeatilFailed(RequestBaseResponse response) {
		if (response.errno.equals(RequestErrorCode.MBCE8012)) {
			// 信用点不足
			tvErrorMessage.setText(getString(R.string.emf_credit_not_enough));
			btnErrorOperate
					.setButtonTitle(getString(R.string.emf_purshase_credits));
			btnErrorOperate.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					// 跳去充值模块
					Intent intent = new Intent(AdmireVirtualGiftDetailActivity.this,
							BuyCreditActivity.class);
					startActivity(intent);
				}
			});
		} else if (response.errno
				.equals(RequestErrorCode.LOCAL_ERROR_CODE_TIMEOUT)) {
			// 网络出错
			tvErrorMessage.setText(getString(R.string.common_network_error));
			btnErrorOperate
					.setButtonTitle(getString(R.string.common_btn_retry));
			btnErrorOperate.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					// 重试
					getAdmireDetail();
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
	
	/**
	 * 下载虚拟礼物thumb图片
	 */
	private void downloadVirtualGiftThumb(){
		tvRetry.setVisibility(View.GONE);
		progressBar.setVisibility(View.VISIBLE);
		String photoUrl = VirtualGiftManager.getInstance().GetVirtualGiftImage(admireDetail.vgId);
		String localPhotoPath = VirtualGiftManager.getInstance().CacheVirtualGiftImagePath(admireDetail.vgId);
		if( photoUrl != null && photoUrl.length() > 0 && localPhotoPath != null && localPhotoPath.length() > 0 
				&& loader != null ) {
			loader.DisplayImage(vgBg, photoUrl, localPhotoPath, this);
		}else{
			progressBar.setVisibility(View.GONE);
		}
	}
	
	/**
	 * 下载虚拟礼物视频
	 */
	private void download3gp(){
		tvRetry.setVisibility(View.GONE);
		String videoUrl = VirtualGiftManager.getInstance().GetVirtualGiftVideo(admireDetail.vgId);
		String localVideoPath = VirtualGiftManager.getInstance().CacheVirtualGiftVideoPath(admireDetail.vgId);
		mFileDownloader.StartDownload(videoUrl, localVideoPath, this);
	}
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		if(vgPlayer != null){
			//退出界面回收资源
			vgPlayer.Stop();
		}
	}

	/*虚拟礼物缩略图下载回调*/
	@Override
	public void OnDisplayNewImageFinish(Bitmap bmp) {
		//下载缩略图成功
		Message msg = Message.obtain();
		msg.what = DOWNLOAD_GIFT_THUMB_CALLBACK;
		RequestBaseResponse response = new RequestBaseResponse(true, "", "", null);
		msg.obj = response;
		sendUiMessage(msg);
	}

	@Override
	public void OnLoadPhotoFailed() {
		//下载缩略图失败
		Message msg = Message.obtain();
		msg.what = DOWNLOAD_GIFT_THUMB_CALLBACK;
		RequestBaseResponse response = new RequestBaseResponse(false, "", "", null);
		msg.obj = response;
		sendUiMessage(msg);
	}

	/*下载3GP是否成功*/
	@Override
	public void onSuccess(FileDownloader loader) {
		Message msg = Message.obtain();
		msg.what = DOWNLOAD_GIFT_3GP_CALLBACK;
		RequestBaseResponse response = new RequestBaseResponse(true, "", "", null);
		msg.obj = response;
		sendUiMessage(msg);
	}

	@Override
	public void onFail(FileDownloader loader) {
		Message msg = Message.obtain();
		msg.what = DOWNLOAD_GIFT_3GP_CALLBACK;
		RequestBaseResponse response = new RequestBaseResponse(false, "", "", null);
		msg.obj = response;
		sendUiMessage(msg);
	}

	@Override
	public void onUpdate(FileDownloader loader, int progress) {
		
	}

	/*Video 播放状态反馈*/
	@Override
	public void onVideoStart() {
		vgBg.setVisibility(View.GONE);
		buttonPlay.setVisibility(View.GONE);
	}

	@Override
	public void onVideoStop() {
		vgBg.setVisibility(View.VISIBLE);
	}

	@Override
	public void onVideoCompletion() {
//		vgBg.setVisibility(View.VISIBLE);
		buttonPlay.setVisibility(View.GONE);
	}

	@Override
	public void onVideoError() {
		vgBg.setVisibility(View.VISIBLE);		
	}

	@Override
	public void onScrollChanged() {
		if(isDown3gpSuccess){
			vgBg.setVisibility(View.VISIBLE);
			vgPlayer.setVisibility(View.INVISIBLE);
		}
	}

	@Override
	public void onScrollComplete() {
		if(isDown3gpSuccess){
			vgBg.setVisibility(View.GONE);
			vgPlayer.setVisibility(View.VISIBLE);
		}
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
		MessageCallbackItem callbackItem = new MessageCallbackItem(errno,errmsg);
		callbackItem.sendItem = item;
		callbackItem.errItem = errItem;
		msg.obj = callbackItem;
		sendUiMessage(msg);
	}

}
