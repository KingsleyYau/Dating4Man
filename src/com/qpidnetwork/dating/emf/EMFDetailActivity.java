package com.qpidnetwork.dating.emf;

import java.io.File;
import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Message;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.qpidnetwork.dating.R;
import com.qpidnetwork.dating.bean.EMFAttachmentBean;
import com.qpidnetwork.dating.bean.EMFAttachmentBean.AttachType;
import com.qpidnetwork.dating.bean.EMFBean;
import com.qpidnetwork.dating.bean.PrivatePhotoBean;
import com.qpidnetwork.dating.bean.RequestFailBean;
import com.qpidnetwork.dating.bean.ShortVideoBean;
import com.qpidnetwork.dating.credit.BuyCreditActivity;
import com.qpidnetwork.dating.emf.EMFAttachmentPrivatePhotoFragment.PrivatePhotoDirection;
import com.qpidnetwork.dating.lady.LadyDetailActivity;
import com.qpidnetwork.framework.base.BaseFragmentActivity;
import com.qpidnetwork.framework.util.ToastUtil;
import com.qpidnetwork.framework.util.UnitConversion;
import com.qpidnetwork.framework.widget.CircleImageView;
import com.qpidnetwork.manager.FileCacheManager;
import com.qpidnetwork.manager.VirtualGiftManager;
import com.qpidnetwork.request.OnEMFAdmirerViewerCallback;
import com.qpidnetwork.request.OnEMFDeleteMsgCallback;
import com.qpidnetwork.request.OnEMFInboxMsgCallback;
import com.qpidnetwork.request.OnEMFOutboxMsgCallback;
import com.qpidnetwork.request.OnEMFPrivatePhotoViewCallback;
import com.qpidnetwork.request.OnRequestFileCallback;
import com.qpidnetwork.request.RequestErrorCode;
import com.qpidnetwork.request.RequestJniEMF.MailType;
import com.qpidnetwork.request.RequestJniEMF.PrivatePhotoType;
import com.qpidnetwork.request.RequestJniEMF.ReplyType;
import com.qpidnetwork.request.RequestJniLiveChat.VideoPhotoType;
import com.qpidnetwork.request.RequestOperator;
import com.qpidnetwork.request.item.EMFAdmirerViewerItem;
import com.qpidnetwork.request.item.EMFInboxMsgItem;
import com.qpidnetwork.request.item.EMFOutboxMsgItem;
import com.qpidnetwork.request.item.EMFShortVideoItem;
import com.qpidnetwork.tool.ImageViewLoader;
import com.qpidnetwork.view.ButtonRaised;
import com.qpidnetwork.view.FitTopImageView;
import com.qpidnetwork.view.MaterialAppBar;
import com.qpidnetwork.view.MaterialDialogAlert;
import com.qpidnetwork.view.MaterialDropDownMenu;

@SuppressLint("InflateParams")
public class EMFDetailActivity extends BaseFragmentActivity implements
		OnClickListener, MaterialDropDownMenu.OnClickCallback,
		OnEMFInboxMsgCallback, OnEMFOutboxMsgCallback,
		OnEMFAdmirerViewerCallback, OnEMFDeleteMsgCallback,
		OnEMFPrivatePhotoViewCallback {
	
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
	 * 编辑
	 */
	private static final int RESULT_ATTACHMENT = 1;

	private static final String EMF_BASE_BEAN = "emfBean";
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

	private CircleImageView ivPhoto;
	private TextView tvName;
	private LinearLayout llAgeCountry;
	private TextView tvAge;
	private TextView tvCountry;
	private TextView tvDesc;
	private TextView tvDate;

	private TextView tvEMFdetail;
	private LinearLayout llAttachment;
	private LinearLayout llTranslator;
	private TextView tvMessage;
	private ButtonRaised btnReply;

	private View includeError;
	private TextView tvErrorMessage;
	private ButtonRaised btnErrorOperate;

	private EMFBean emfBean;
	private Object emfDeail;
	private MaterialAppBar appbar;

	/* 存放附件列表 */
	private ArrayList<EMFAttachmentBean> mAttachList;
	private EMFVideoManager mEMFVideoManager;

	private EMFInboxMsgItem mEMFInboxMsgItem = null;
	private EMFOutboxMsgItem mEMFOutboxMsgItem = null;
	
	/* 广播用于activity间数据传递 */
	private BroadcastReceiver mBroadcastReceiver;//处理购买成功状态更新

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
		/* title */
		appbar = (MaterialAppBar) findViewById(R.id.appbar);
		appbar.setAppbarBackgroundColor(getResources().getColor(
				R.color.theme_actionbar_secoundary));
		appbar.setOnButtonClickListener(this);
		appbar.setTouchFeedback(MaterialAppBar.TOUCH_FEEDBACK_HOLO_LIGHT);
		// appbar.addButtonToLeft(R.id.common_button_forward, "forward",
		// R.drawable.ic_forward_grey600_24dp);
		appbar.addButtonToLeft(R.id.common_button_reply, "reply",
				R.drawable.ic_reply_all_grey600_24dp);
		appbar.addButtonToLeft(R.id.common_button_back, "back",
				R.drawable.ic_arrow_back_grey600_24dp);
		// LinearLayout.LayoutParams params =
		// (LinearLayout.LayoutParams)appbar.findViewWithTag("reply").getLayoutParams();
		// params.setMargins((int)(72.00 *
		// getResources().getDisplayMetrics().density) - params.width, 0, 0, 0);
		// appbar.setButtonLayoutParams("reply", params);

		appbar.addOverflowButton(
				new String[] { getString(R.string.emf_menu_delete) }, this,
				R.drawable.ic_more_vert_grey600_24dp);

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
		llAttachment = (LinearLayout) findViewById(R.id.llAttachment);
		llTranslator = (LinearLayout) findViewById(R.id.llTranslator);
		tvMessage = (TextView) findViewById(R.id.tvMessage);
		btnReply = (ButtonRaised) findViewById(R.id.btnReply);
		btnReply.setOnClickListener(this);

		/* Error */
		includeError = (View) findViewById(R.id.includeError);
		tvErrorMessage = (TextView) findViewById(R.id.tvErrorMessage);
		btnErrorOperate = (ButtonRaised) findViewById(R.id.btnErrorOperate);
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
		case R.id.common_button_reply:
		case R.id.btnReply: {
			String mTab = "";
			ReplyType type = ReplyType.DEFAULT;
			if (emfBean.type == 2) {
				// 回复意向信
				type = ReplyType.ADMIRE;
				mTab = emfBean.mtab;
			} else {
				// 回复emf
				type = ReplyType.EMF;
			}
			MailEditActivity.launchMailEditActivity(this, emfBean.womanid,
					type, mTab);
		}
			break;
		case R.id.ivPhoto: {
			LadyDetailActivity.launchLadyDetailActivity(EMFDetailActivity.this,
					emfBean.womanid, true);
		}
			break;
		case R.id.image: {
			Intent intent = EMFAttachmentPreviewActivity.getIntent(
					EMFDetailActivity.this, mAttachList, (Integer) v.getTag());
			intent.putExtra(EMFAttachmentPreviewActivity.VGTIPS, false);
			intent.putExtra(EMFAttachmentPreviewActivity.ATTACH_DIRECTION,
					(emfBean.type == 1) ? PrivatePhotoDirection.MW.name()
							: PrivatePhotoDirection.WM.name());
			startActivityForResult(intent, RESULT_ATTACHMENT);
		}
			break;
		default:
			break;
		}
	}

	@Override
	protected void handleUiMessage(Message msg) {
		// TODO Auto-generated method stub
		super.handleUiMessage(msg);

		switch (msg.what) {
		case GET_EMF_DETAIL_SUCCESS:
			hideProgressDialog();
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
		case GET_EMF_DETAIL_FAILED:
			hideProgressDialog();
			includeError.setVisibility(View.VISIBLE);
			onGetDeatilFailed(msg.obj);
			break;
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
		}
	}

	/**
	 * 收件箱详情数据更新
	 * 
	 * @param item
	 */
	private void onGetInboxDetailSuccess() {
		if (mEMFInboxMsgItem != null) {
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

					// 是否已经购买的私密照
					if (privateItem.privatePhoto.photoFee) {
						// 生成缓存路径
						String localPhotoPath = FileCacheManager.getInstance()
								.CachePrivatePhotoImagePath(
										privateItem.privatePhoto.sendId,
										privateItem.privatePhoto.photoId,
										PrivatePhotoType.SMALL);

						// 是否已经缓存
						File file = new File(localPhotoPath);
						if (file.exists() && file.isFile()) {
							// 已经下载过, 直接显示
						} else {
							// 请求接口获取私密照
							PrivatePhotoView(privateItem.privatePhoto);
						}
					}
				}
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
					if (privateItem.privatePhoto.photoFee) {
						// 生成缓存路径
						String localPhotoPath = FileCacheManager.getInstance()
								.CachePrivatePhotoImagePath(
										privateItem.privatePhoto.sendId,
										privateItem.privatePhoto.photoId,
										PrivatePhotoType.SMALL);

						// 是否已经缓存
						File file = new File(localPhotoPath);
						if (file.exists() && file.isFile()) {
							// 已经下载过, 直接显示
						} else {
							// 请求接口获取私密照
							PrivatePhotoView(privateItem.privatePhoto);
						}
					}
				}
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
	private void onGetDeatilFailed(Object object) {
		RequestFailBean error = (RequestFailBean) object;
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
		if (mAttachList != null) {
			llAttachment.setVisibility(View.VISIBLE);
			llAttachment.removeAllViews();
			for (int i = 0; i < mAttachList.size(); i++) {

				View v = LayoutInflater.from(this).inflate(
						R.layout.item_emf_attachment_preview, null);
				FitTopImageView iv = (FitTopImageView) v
						.findViewById(R.id.image);
				ImageButton play = (ImageButton) v
						.findViewById(R.id.play_button);
				ImageView gift_mark = (ImageView) v
						.findViewById(R.id.gift_mark);

				iv.setOnClickListener(this);
				iv.setOnClickListener(this);
				iv.setTag(i);
				play.setTag(i);

				String url = "";
				String localPath = "";// FileCacheManager.getInstance().CacheImagePathFromUrl(mAttachList.get(i).photoUrl);

				EMFAttachmentBean item = mAttachList.get(i);

				if (item.type.equals(AttachType.VIRTUAL_GIFT)) {
					iv.setImageResource(R.drawable.attachment_gift_unloaded_110_150dp);
					gift_mark.setVisibility(View.VISIBLE);
					url = VirtualGiftManager.getInstance().GetVirtualGiftImage(
							item.vgId);
					localPath = VirtualGiftManager.getInstance()
							.CacheVirtualGiftImagePath(item.vgId);
				} else if (item.type.equals(AttachType.PRIVATE_PHOTO)) {

					if (emfBean.type == 1) {
						// 發送
						iv.setImageResource(R.drawable.attachment_photo_unloaded_110_150dp);
					} else {
						// 收到
						iv.setImageResource(R.drawable.private_photo_unviewed_110_150dp);
					}

					localPath = FileCacheManager.getInstance()
							.CachePrivatePhotoImagePath(
									item.privatePhoto.sendId,
									item.privatePhoto.photoId,
									PrivatePhotoType.LARGE);
				} else if (item.type.equals(AttachType.SHORT_VIDEO)) {
					iv.setImageResource(R.drawable.attachment_photo_unloaded_110_150dp);
					play.setVisibility(View.VISIBLE);
					localPath = mEMFVideoManager.getVideoThumbPhotoPath(
							item.shortVideo.womanid, item.shortVideo.sendId,
							item.shortVideo.videoId, item.shortVideo.messageid,
							VideoPhotoType.Big);
				} else {
					iv.setImageResource(R.drawable.attachment_photo_unloaded_110_150dp);
					url = item.photoUrl;
					localPath = FileCacheManager.getInstance()
							.CacheImagePathFromUrl(url);
				}

				llAttachment.addView(v);

				new ImageViewLoader(this).DisplayImage(iv, url, localPath,
						UnitConversion.dip2px(this, 150),
						UnitConversion.dip2px(this, 110), null);
			}
		}
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
		// 生成缓存路径
		String localPhotoPath = FileCacheManager.getInstance()
				.CachePrivatePhotoImagePath(item.sendId, item.photoId,
						PrivatePhotoType.LARGE);

		RequestOperator.getInstance().PrivatePhotoView(item.womanid,
				item.photoId, item.sendId, item.messageid, localPhotoPath,
				PrivatePhotoType.LARGE, this);
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

	@Override
	public void OnEMFInboxMsg(boolean isSuccess, String errno, String errmsg,
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
}
