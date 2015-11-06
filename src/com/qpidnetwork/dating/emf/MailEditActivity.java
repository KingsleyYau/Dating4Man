package com.qpidnetwork.dating.emf;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import android.provider.MediaStore;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.qpidnetwork.dating.R;
import com.qpidnetwork.dating.authorization.LoginManager;
import com.qpidnetwork.dating.authorization.LoginManager.LoginStatus;
import com.qpidnetwork.dating.authorization.LoginParam;
import com.qpidnetwork.dating.authorization.LoginPerfence;
import com.qpidnetwork.dating.bean.ContactBean;
import com.qpidnetwork.dating.bean.EMFAttachmentBean;
import com.qpidnetwork.dating.bean.EMFAttachmentBean.AttachType;
import com.qpidnetwork.dating.contacts.ContactManager;
import com.qpidnetwork.dating.emf.EMFAttachmentUploader.EMFAttachmentUploaderCallback;
import com.qpidnetwork.dating.lady.LadyDetailActivity;
import com.qpidnetwork.dating.lady.LadyDetailManager;
import com.qpidnetwork.dating.lady.LadyDetailManager.OnLadyDetailManagerQueryLadyDetailCallback;
import com.qpidnetwork.framework.base.BaseActionBarFragmentActivity;
import com.qpidnetwork.framework.util.CompatUtil;
import com.qpidnetwork.framework.util.ImageUtil;
import com.qpidnetwork.framework.util.StringUtil;
import com.qpidnetwork.framework.util.UnitConversion;
import com.qpidnetwork.framework.widget.CircleImageView;
import com.qpidnetwork.manager.FileCacheManager;
import com.qpidnetwork.manager.VirtualGiftManager;
import com.qpidnetwork.request.OnEMFSendMsgCallback;
import com.qpidnetwork.request.OnOtherIntegralCheckCallback;
import com.qpidnetwork.request.OnRequestCallback;
import com.qpidnetwork.request.RequestErrorCode;
import com.qpidnetwork.request.RequestJni;
import com.qpidnetwork.request.RequestJniEMF.ReplyType;
import com.qpidnetwork.request.RequestJniEMF.UploadAttachType;
import com.qpidnetwork.request.RequestJniLiveChat.UseType;
import com.qpidnetwork.request.RequestOperator;
import com.qpidnetwork.request.item.EMFSendMsgErrorItem;
import com.qpidnetwork.request.item.EMFSendMsgItem;
import com.qpidnetwork.request.item.LadyDetail;
import com.qpidnetwork.request.item.LoginItem;
import com.qpidnetwork.request.item.OtherIntegralCheckItem;
import com.qpidnetwork.tool.ImageViewLoader;
import com.qpidnetwork.view.ChooseRemoveAttachmentDialog;
import com.qpidnetwork.view.CustomGridView;
import com.qpidnetwork.view.FlatToast;
import com.qpidnetwork.view.GetMoreCreditDialog;
import com.qpidnetwork.view.MaterialAppBar;
import com.qpidnetwork.view.MaterialDialogAlert;
import com.qpidnetwork.view.MaterialDropDownMenu;
import com.qpidnetwork.view.MaterialProgressBar;
import com.qpidnetwork.view.MaterialThreeButtonDialog;
import com.qpidnetwork.view.ProgressImageHorizontalView;

public class MailEditActivity extends BaseActionBarFragmentActivity
							  implements OnItemClickListener,
							  			 EMFAttachmentUploaderCallback
{

	/**
	 * 选择虚拟礼物界面返回
	 */
	private static final int RESULT_VIRTUAL_GIFT = 0;

	/**
	 * 其他界面传入参数
	 * 
	 * @param WOMANID
	 *            女士Id
	 * @param REPLYTYPE
	 *            回复类型
	 * @param MTAB
	 *            表后缀
	 */
	public static final String WOMANID = "WOMAINID";
	public static final String REPLYTYPE = "REPLYTYPE";
	public static final String MTAB = "MTAB";

	public static final int MAX_ATTACHMENT = 6;
	/**
	 * 拍照
	 */
	private static final int RESULT_LOAD_IMAGE_CAPTURE = 1;
	/**
	 * 相册
	 */
	private static final int RESULT_LOAD_IMAGE_ALBUMN = 2;

	private enum RequestFlag {
		REQUEST_SUCCESS, REQUEST_FAIL, REQUEST_SENDGIFT_SUCCESS, REQUEST_SENDGIFT_FAIL, REQUEST_BOUNDS_SUCCESS, REQUEST_BOUNDS_FAIL, REQUEST_LADY_DETAIL_SUCCESS, REQUEST_LADY_DETAIL_FAIL,
	}

	private Context mContext;

	private EditText etLadyId;
	private MaterialProgressBar progressBarId;
	private int progressCount = 0;

	private LinearLayout layoutId;
	private EditText editTextId;
	private CircleImageView imageViewId;
	private ImageViewLoader mLoader;
	private ImageView imageViewIdCancel;

	private ImageView ivContacts;
	private TextView tvBonusPoint;
	private EditText tvEMFBody;
	private CustomGridView gvAttachments;

	private String takePhotoTempPath = "";
	private EMFEditAttachAdapter mAdapter;

	private boolean isRequestingLadyDetail = false;

	private ReplyType mReplyType = ReplyType.DEFAULT;
	private String mMtab = "";

	/**
	 * Dialogs
	 */
	private ChooseRemoveAttachmentDialog mChooseRemoveAttachmentDialog;
	private MaterialDialogAlert mErrorDialog;

	/* 下载控制 */
	private List<EMFAttachmentUploader> mUploadList;
	private List<View> mViewsList;

	/**
	 * 联系人弹窗
	 */
	private MaterialDropDownMenu contactsListPopwindow;

	/**
	 * 已经选择的虚拟礼物Id
	 */
	private List<String> mVgIdList = new LinkedList<String>();

	/**
	 * 已经选择的附件Id
	 */
	private List<String> mAttachmentIdList = new LinkedList<String>();

	/**
	 * 保存当前发信女士详情，用于添加联系人及发信读取
	 */
	private LadyDetail mCurrentLady;

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
	 * 邮件编辑功能界面统一入口，方便风控等处理
	 * @param context
	 * @param womanId
	 * @param replayType
	 * @param mTab
	 */
	public static void launchMailEditActivity(Context context, String womanId,
			ReplyType replayType, String mTab) {
		boolean isCanEnter = true;
		if(LoginManager.getInstance().GetLoginStatus() == LoginStatus.LOGINED){
			LoginParam loginParam = LoginManager.getInstance().GetLoginParam();
			if(loginParam.item.premit){
				if(((replayType == ReplyType.ADMIRE)&&loginParam.item.admirer)||
							loginParam.item.ladyprofile){
					isCanEnter = false;
				}
			}else{
				isCanEnter = false;
			}
			if(!isCanEnter){
				/*被风控，不能进入*/
				MaterialDialogAlert dialog = new MaterialDialogAlert(context);
				dialog.setMessage(context.getString(R.string.common_risk_control_notify));
				dialog.addButton(dialog.createButton(context.getString(R.string.common_btn_ok), null));
				dialog.show();
			}else{
				/*未被风控，可以进入*/
				Intent intent = new Intent(context, MailEditActivity.class);
				if(!StringUtil.isEmpty(womanId)){
					intent.putExtra(WOMANID, womanId);
				}
				intent.putExtra(MailEditActivity.REPLYTYPE, replayType);
				if(!StringUtil.isEmpty(mTab)){
					intent.putExtra(MailEditActivity.MTAB, mTab);
				}
				context.startActivity(intent);
			}
		}
	}

	@Override
	protected void onCreate(Bundle arg0) {
		// TODO Auto-generated method stub
		super.onCreate(arg0);
		mContext = this;
		setCustomContentView(R.layout.activity_mail_edit);
		/* 初始化头部 */
		getCustomActionBar().setTouchFeedback(MaterialAppBar.TOUCH_FEEDBACK_HOLO_LIGHT);

		getCustomActionBar().addButtonToRight(R.id.common_button_gift, "",
				R.drawable.ic_wallet_giftcard_grey600_24dp);
		LoginItem item = LoginManager.getInstance().GetLoginParam().item;
		if((item != null)&& (item.photosend)){
			/*私密照发送风控，当允许发送时才显示添加附件按钮，否则直接隐藏*/
			getCustomActionBar().addButtonToRight(R.id.common_button_attachments,
					"", R.drawable.ic_attachment_grey600_24dp);
		}
		getCustomActionBar().addButtonToRight(R.id.common_button_send, "",
				R.drawable.ic_send_grey600_24dp);
		getCustomActionBar().setTitle(getString(R.string.emf_edit_title),
				getResources().getColor(R.color.text_color_dark));
		getCustomActionBar().changeIconById(R.id.common_button_back,
				R.drawable.ic_arrow_back_grey600_24dp);
		getCustomActionBar().getButtonById(R.id.common_button_back).setBackgroundResource(MaterialAppBar.TOUCH_FEEDBACK_HOLO_LIGHT);
		getCustomActionBar().setAppbarBackgroundDrawable(
				new ColorDrawable(Color.WHITE));

		initViews();

//		// 清空临时附件目录
//		ClearCaceh();

	}

	private void initViews() {

		mChooseRemoveAttachmentDialog = new ChooseRemoveAttachmentDialog(this);
		
		//初始化dialog
		mErrorDialog = new MaterialDialogAlert(this);
		mErrorDialog.setMessage(getString(R.string.emf_send_fail));
		mErrorDialog.addButton(mErrorDialog.createButton(
				getString(R.string.common_btn_cancel), null));
		mErrorDialog.addButton(mErrorDialog.createButton(
				getString(R.string.common_btn_ok),
				new OnClickListener() {

					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						SendEMFMessage();
					}
				}));

		/* 使用积分按钮 */
		tvBonusPoint = (TextView) findViewById(R.id.tvBonusPoint);
		tvBonusPoint.setOnClickListener(this);
		tvBonusPoint.setVisibility(View.GONE);

		/* 女士Id */
		etLadyId = (EditText) findViewById(R.id.etLadyId);
		etLadyId.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				// TODO Auto-generated method stub

			}

			@Override
			public void afterTextChanged(Editable s) {
				// TODO Auto-generated method stub
				if (s.length() < 4) {

					progressBarId.setVisibility(View.GONE);
					return;
				}

				layoutId.setVisibility(View.GONE);
				etLadyId.setTextColor(Color.BLACK);

				// 隐藏可用积分
				tvBonusPoint.setVisibility(View.GONE);

				// 查询女士Id是否正确, 就是查询女士详情
				isRequestingLadyDetail = true;
				QueryLadyDetail();
			}
		});

		progressBarId = (MaterialProgressBar) findViewById(R.id.progressBarId);
		progressBarId.setVisibility(View.GONE);

		layoutId = (LinearLayout) findViewById(R.id.layoutId);
		layoutId.setVisibility(View.GONE);
		editTextId = (EditText) layoutId.findViewById(R.id.editTextId);
		editTextId.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				// TODO Auto-generated method stub

				if (etLadyId.getText().toString()
						.compareTo(editTextId.getText().toString()) != 0) {
					layoutId.setVisibility(View.GONE);
					etLadyId.setVisibility(View.VISIBLE);
					etLadyId.setText(s);
				}
			}

			@Override
			public void afterTextChanged(Editable s) {
				// TODO Auto-generated method stub
			}
		});

		imageViewId = (CircleImageView) layoutId.findViewById(R.id.imageViewId);
		imageViewIdCancel = (ImageView) layoutId
				.findViewById(R.id.imageViewIdCancel);
		imageViewIdCancel.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				layoutId.setVisibility(View.GONE);
				etLadyId.setVisibility(View.VISIBLE);
				etLadyId.setText("");
				etLadyId.requestFocus();
			}
		});

		/* 下拉联系人选择 */
		ivContacts = (ImageView) findViewById(R.id.ivContacts);
		ivContacts.setOnClickListener(this);

		/* 邮件主题 */
		tvEMFBody = (EditText) findViewById(R.id.tvEMFBody);

		Bundle bundle = getIntent().getExtras();
		if (bundle != null) {
			String womanId = bundle.getString(WOMANID);
			if (womanId != null) {
				// 隐藏联系人
				ivContacts.setVisibility(View.INVISIBLE);

				// 女士不可编辑
				etLadyId.setText(womanId);
				editTextId.setEnabled(false);
				imageViewIdCancel.setVisibility(View.GONE);

				/* 默认有女士Id光标置于EMF内容编辑处 */
				tvEMFBody.requestFocus();

				tvBonusPoint.setVisibility(View.GONE);
				integralCheck();
			}

			int replyType = bundle.getInt(REPLYTYPE);
			if (replyType > -1 && replyType < ReplyType.values().length) {
				mReplyType = ReplyType.values()[replyType];
			}

			String mtab = bundle.getString(MTAB);
			if (mtab != null) {
				mMtab = mtab;
			}
		}

		tvEMFBody.setText(GetMailBody());
		gvAttachments = (CustomGridView) findViewById(R.id.gvAttachments);

		mUploadList = new ArrayList<EMFAttachmentUploader>();
		mViewsList = new ArrayList<View>();
		mAdapter = new EMFEditAttachAdapter(this, mViewsList);
		gvAttachments.setAdapter(mAdapter);

		// 联系人弹窗
		final List<ContactBean> cbList = ContactManager.getInstance()
				.getContactList();
		Point size = new Point((int) (240.0f * getResources()
				.getDisplayMetrics().density), LayoutParams.WRAP_CONTENT);
		contactsListPopwindow = new MaterialDropDownMenu(this,
				new EMFContactChipsAdapter(this, cbList),
				new MaterialDropDownMenu.OnClickCallback() {

					@Override
					public void onClick(AdapterView<?> adptView, View v,
							int which) {
						// TODO Auto-generated method stub
						ContactBean contact = cbList.get(which);
						etLadyId.setText(contact.womanid);

						// 读取草稿
						ReloadDraft();

						tvEMFBody.requestFocus();
					}
				}, size);

		// 附件
		gvAttachments.setOnItemClickListener(this);

		if (cbList.size() == 0) {
			// 没有最近联系人, 隐藏下拉
			ivContacts.setVisibility(View.INVISIBLE);
		}

		// 读取草稿
		ReloadDraft();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		if (keyCode == KeyEvent.KEYCODE_BACK
				&& event.getAction() == KeyEvent.ACTION_DOWN) {
			if (CheckNeedSaveDraft()) {

				MaterialThreeButtonDialog dialog = new MaterialThreeButtonDialog(
						this, new MaterialThreeButtonDialog.OnClickCallback() {

							@Override
							public void OnSecondButtonClick(View v) {
								// TODO Auto-generated method stub
								DeleteDraft();
								finish();
							}

							@Override
							public void OnFirstButtonClick(View v) {
								// TODO Auto-generated method stub
								SaveMailBody();
								finish();
							}

							@Override
							public void OnCancelButtonClick(View v) {
								// TODO Auto-generated method stub

							}
						});

				dialog.setMessage(getString(R.string.emf_save_draft_tips));
				dialog.getTitle().setVisibility(View.GONE);
				dialog.hideImageView();
				dialog.getFirstButton().setText(
						getString(R.string.emf_save_draft));
				dialog.getSecondButton().setText(
						getString(R.string.emf_delete_draft));
				dialog.show();

			} else {
				finish();
			}
			return false;
		} else {

		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		// super.onClick(v);
		switch (v.getId()) {
		case R.id.common_button_back: {
			// 弹出保存草稿
			if (CheckNeedSaveDraft()) {

				MaterialThreeButtonDialog dialog = new MaterialThreeButtonDialog(
						this, new MaterialThreeButtonDialog.OnClickCallback() {

							@Override
							public void OnSecondButtonClick(View v) {
								// TODO Auto-generated method stub
								DeleteDraft();
								finish();
							}

							@Override
							public void OnFirstButtonClick(View v) {
								// TODO Auto-generated method stub
								SaveMailBody();
								finish();
							}

							@Override
							public void OnCancelButtonClick(View v) {
								// TODO Auto-generated method stub

							}
						});

				dialog.setMessage(getString(R.string.emf_save_draft_tips));
				dialog.getTitle().setVisibility(View.GONE);
				dialog.hideImageView();
				dialog.getFirstButton().setText(
						getString(R.string.emf_save_draft));
				dialog.getSecondButton().setText(
						getString(R.string.emf_delete_draft));
				dialog.show();

				/*
				 * if( !mChooseSaveDraftDialog.isShowing() )
				 * 
				 * { mChooseSaveDraftDialog.show();
				 * mChooseSaveDraftDialog.textViewSave.setOnClickListener(new
				 * OnClickListener() {
				 * 
				 * @Override public void onClick(View v) { // TODO
				 * Auto-generated method stub // 保存草稿
				 * 
				 * mChooseSaveDraftDialog.dismiss(); } });
				 * mChooseSaveDraftDialog.textViewDelete.setOnClickListener(new
				 * OnClickListener() {
				 * 
				 * @Override public void onClick(View v) { // TODO
				 * Auto-generated method stub // 直接退出
				 * 
				 * mChooseSaveDraftDialog.dismiss(); } });
				 * mChooseSaveDraftDialog.textViewCancel.setOnClickListener(new
				 * OnClickListener() {
				 * 
				 * @Override public void onClick(View v) { // TODO
				 * Auto-generated method stub // 不退出, 继续编辑
				 * mChooseSaveDraftDialog.dismiss(); } }); }
				 */
			} else {
				finish();
			}
		}
			break;
		case R.id.common_button_gift:
			// 点击虚拟礼物
			if (CheckSelecedVirtualGift()) {
				// 已经选择过, 弹出对话框
				MaterialDialogAlert dialog = new MaterialDialogAlert(this);

				dialog.addButton(dialog.createButton(
						getString(R.string.common_btn_ok), null));
				dialog.show();
				dialog.setMessage(getString(R.string.emf_remove_virtual_gift));

				// CustomNotifyDialogFragment fragment =
				// CustomNotifyDialogFragment.newInstance(getString(R.string.common_btn_ok),
				// getString(R.string.emf_remove_virtual_gift));
				// fragment.show(getSupportFragmentManager(), "");
			} else {
				// 打开选择界面
				Intent intent = new Intent(this, EmotionChooseActivity.class);
				startActivityForResult(intent, RESULT_VIRTUAL_GIFT);
			}
			break;
		case R.id.common_button_attachments:
			if (CheckMaxAttachment()) {
				// 超过最大附件数, 弹出对话框

				MaterialDialogAlert dialog = new MaterialDialogAlert(this);
				dialog.addButton(dialog.createButton(
						getString(R.string.common_btn_ok), null));
				dialog.show();
				dialog.setMessage(getString(R.string.emf_maximum_attachments));

				// CustomNotifyDialogFragment fragment =
				// CustomNotifyDialogFragment.newInstance(getString(R.string.common_btn_ok),
				// getString(R.string.emf_maximum_attachments));
				// fragment.show(getSupportFragmentManager(), "");
			} else {
				// 打开选择图片
				doChoosePhoto();
			}
			break;
		case R.id.common_button_send:
			// 点击发送邮件
			if (CheckAttachmentAllUpload()) {
				// 不是全部附件都已经上传, 弹出对话框

				MaterialDialogAlert dialog = new MaterialDialogAlert(this);
				dialog.setMessage(getString(R.string.emf_send_without_attachment));
				dialog.addButton(dialog.createButton(
						getString(R.string.common_btn_send),
						new OnClickListener() {

							@Override
							public void onClick(View v) {
								// TODO Auto-generated method stub
								SendEMFMessage();
							}

						}));
				dialog.addButton(dialog.createButton(
						getString(R.string.common_btn_edit), null));
				dialog.show();

				/*
				 * CustomInfoDialogFragment fragment =
				 * CustomInfoDialogFragment.newInstance(
				 * getString(R.string.common_btn_send),
				 * getString(R.string.common_btn_edit),
				 * getString(R.string.emf_send_without_attachment) );
				 * fragment.show(getSupportFragmentManager(),
				 * DIALOG_SEND_WITHOUT_ATTACHMENT_TAG);
				 */
			} else {
				// 发送消息
				SendEMFMessage();
			}
			break;
		case R.id.ivContacts:
			// 弹出联系人列表
			contactsListPopwindow.showAsDropDown(ivContacts);
			break;
		case R.id.tvBonusPoint:
			// 弹出使用积分

			MaterialDialogAlert dialog = new MaterialDialogAlert(this);
			dialog.setMessage(getString(R.string.emf_bp_tips));
			dialog.addButton(dialog.createButton(
					getString(R.string.common_btn_ok), null));
			dialog.show();

			/*
			 * if( !mChooseConfirmWithTitleDialogBP.isShowing() ) {
			 * mChooseConfirmWithTitleDialogBP.show();
			 * mChooseConfirmWithTitleDialogBP
			 * .buttonConfirm.setOnClickListener(new OnClickListener() {
			 * 
			 * @Override public void onClick(View v) { // TODO Auto-generated
			 * method stub mChooseConfirmWithTitleDialogBP.dismiss(); } }); }
			 */
			break;
		}
	}

	private void doChoosePhoto() {

		MaterialThreeButtonDialog dialog = new MaterialThreeButtonDialog(this,
				new MaterialThreeButtonDialog.OnClickCallback() {

					@Override
					public void OnSecondButtonClick(View v) {
						// TODO Auto-generated method stub
						Intent intent = CompatUtil.getSelectPhotoFromAlumIntent();
//						intent.setType("image/*");
//						intent.setAction(Intent.ACTION_GET_CONTENT);
						startActivityForResult(intent, RESULT_LOAD_IMAGE_ALBUMN);
					}

					@Override
					public void OnFirstButtonClick(View v) {
						// TODO Auto-generated method stub

						Intent intent = new Intent(
								MediaStore.ACTION_IMAGE_CAPTURE);
						takePhotoTempPath = FileCacheManager.getInstance().GetEMFCameraUrl();
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

		dialog.hideImageView();
		dialog.setTitle(getString(R.string.emf_do_photo_add_tips));
		dialog.setMessage(getString(R.string.emf_do_photo_add));
		dialog.show();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode) {
		case RESULT_LOAD_IMAGE_CAPTURE:
			if (resultCode == RESULT_OK) {
				if ((takePhotoTempPath != null)
						&& (!takePhotoTempPath.equals(""))) {
					// 保存到相册
					String fileName = "mail" + "_" + System.currentTimeMillis() + ".jpg";
					ImageUtil.SaveImageToGallery(this, null, takePhotoTempPath,
							fileName, null);
					
					// 增加附件
					addAttachments(takePhotoTempPath);
				}
			}
			takePhotoTempPath = "";
			break;
		case RESULT_LOAD_IMAGE_ALBUMN:
			if (resultCode == RESULT_OK && null != data) {
				Uri selectedImage = data.getData();
//				String[] filePathColumn = { MediaStore.Images.Media.DATA };
//
//				String picturePath = "";
//				Cursor cursor = getContentResolver().query(selectedImage,
//						filePathColumn, null, null, null);
//				if (cursor != null) {
//					cursor.moveToFirst();
//
//					int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
//					picturePath = cursor.getString(columnIndex);
//					cursor.close();
//
//					/* 更新附件列表界面 */
//					addAttachments(picturePath);
//				}
				String picturePath = CompatUtil.getSelectedPhotoPath(this, selectedImage);
	    		if(!StringUtil.isEmpty(picturePath)){
	    			/* 更新附件列表界面 */
					addAttachments(picturePath);
	    		}

			}
			break;
		case RESULT_VIRTUAL_GIFT:
			// 选择虚拟礼物返回
			if (resultCode == RESULT_OK) {
				String vgId = data.getExtras().getString(
						EmotionChooseActivity.VIRTUAL_GIFT_ID);
				addVirtualGift(vgId);
			}
			break;
		}
	}

	private void addAttachments(String localPath) {
		ProgressImageHorizontalView view = new ProgressImageHorizontalView(this);
		view.setLayoutParams(new AbsListView.LayoutParams(
				(gvAttachments.getWidth() - UnitConversion.dip2px(this, 16)) / 2,
				(gvAttachments.getWidth() - UnitConversion.dip2px(this, 16)) / 2));
		EMFAttachmentUploader uploader = new EMFAttachmentUploader();
		uploader.Upload(view, UploadAttachType.IMAGE, localPath, this);

		/* 绑定附件信息到View，用于附件预览时取出 */
		EMFAttachmentBean item = new EMFAttachmentBean();
		item.type = AttachType.NORAML_PICTURE;
		item.photoLocalUrl = localPath;
		view.setTag(item);

		mViewsList.add(view);
		mUploadList.add(uploader);
		mAdapter.notifyDataSetChanged();
	}

	/**
	 * 增加虚拟礼物到发送列表
	 * 
	 * @author Max.Chiu
	 * @param vgId
	 *            虚拟礼物Id
	 */
	private void addVirtualGift(final String vgId) {
		// 带进度条的上传图像控件
		final ProgressImageHorizontalView view = new ProgressImageHorizontalView(
				this);
		view.setLayoutParams(new AbsListView.LayoutParams(
				(gvAttachments.getWidth() - UnitConversion.dip2px(this, 16)) / 2,
				(gvAttachments.getWidth() - UnitConversion.dip2px(this, 16)) / 2));

		String name = VirtualGiftManager.getInstance().GetVirtualGiftName(vgId);
		view.textView.setText(name);

		view.imageView.setScaleType(ScaleType.CENTER_CROP);
		view.imageViewPlay.setVisibility(View.VISIBLE);
		view.progressBar.setVisibility(View.GONE);

		view.buttonCancel.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				// 删除虚拟礼物
				mVgIdList.remove(vgId);
				mViewsList.remove(view);
				mAdapter.notifyDataSetChanged();
			}
		});

		String photoUrl = VirtualGiftManager.getInstance().GetVirtualGiftImage(
				vgId);
		String localPhotoPath = VirtualGiftManager.getInstance()
				.CacheVirtualGiftImagePath(vgId);
		new ImageViewLoader(this).DisplayImage(view.imageView, photoUrl,
				localPhotoPath, null);

		// 增加虚拟礼物
		mVgIdList.add(vgId);

		/* 绑定附件信息到View，用于附件预览时取出 */
		EMFAttachmentBean item = new EMFAttachmentBean();
		item.type = AttachType.VIRTUAL_GIFT;
		item.vgId = vgId;
		view.setTag(item);

		mViewsList.add(view);
		mAdapter.notifyDataSetChanged();
	}

	/**
	 * 查询是否可以使用积分
	 * 
	 * @param womanId
	 *            女士Id
	 */
	private void integralCheck() {
		final String womanId = etLadyId.getText().toString().toUpperCase();
		RequestOperator.getInstance().IntegralCheck(womanId,
				new OnOtherIntegralCheckCallback() {

					@Override
					public void OnOtherIntegralCheck(boolean isSuccess,
							String errno, String errmsg,
							OtherIntegralCheckItem item) {
						// TODO Auto-generated method stub
						Message msg = Message.obtain();
						if (isSuccess) {
							// 成功
							msg.what = RequestFlag.REQUEST_BOUNDS_SUCCESS
									.ordinal();
						} else {
							msg.what = RequestFlag.REQUEST_BOUNDS_FAIL
									.ordinal();
						}
						MessageCallbackItem callbackItem = new MessageCallbackItem(
								errno, errmsg);
						callbackItem.womanId = womanId;
						callbackItem.otItem = item;
						msg.obj = callbackItem;
						sendUiMessage(msg);

					}
				});
	}

	/**
	 * 查询女士详情
	 */
	public void QueryLadyDetail() {
		progressCount++;
		progressBarId.setVisibility(View.VISIBLE);
		final String womanId = etLadyId.getText().toString().toUpperCase();
		LadyDetailManager.getInstance().QueryLadyDetail(womanId,
				new OnLadyDetailManagerQueryLadyDetailCallback() {

					@Override
					public void OnQueryLadyDetailCallback(boolean isSuccess,
							String errno, String errmsg, LadyDetail item) {
						// TODO Auto-generated method stub
						Message msg = Message.obtain();
						if (isSuccess) {
							msg.what = RequestFlag.REQUEST_LADY_DETAIL_SUCCESS
									.ordinal();
						} else {
							msg.what = RequestFlag.REQUEST_LADY_DETAIL_FAIL
									.ordinal();
						}
						MessageCallbackItem callbackItem = new MessageCallbackItem(
								errno, errmsg);
						callbackItem.womanId = womanId;
						callbackItem.ladyDetail = item;
						msg.obj = callbackItem;
						sendUiMessage(msg);
					}
				});
	}

	/**
	 * 发送邮件
	 */
	private void SendEMFMessage() {

		final String womanId = etLadyId.getText().toString().toUpperCase();
		final String emfBody = tvEMFBody.getText().toString();

		if ((womanId == null) || (womanId.length() <= 4)) {
			// womanId为空不能发送
			shakeView(etLadyId, true);
			showSoftInput();
			return;
		}

		if (isRequestingLadyDetail) {
			shakeView(etLadyId, true);
			return;
		}

		if (etLadyId.getTag() == null || (Boolean) etLadyId.getTag() != true) {
			shakeView(etLadyId, true);
			return;
		}

		if ((emfBody == null) || (emfBody.length() <= 0)) {
			// 邮件内容为空不能发送
			shakeView(tvEMFBody, true);
			showSoftInput();
			return;
		}

		// 此处应有菊花
		showProgressDialog("Loading...");

		RequestOperator.getInstance().SendMsg(
				womanId,
				emfBody,
				tvBonusPoint.getVisibility() == View.VISIBLE,
				mReplyType,
				mMtab,
				(String[]) mVgIdList.toArray(new String[mVgIdList.size()]),
				(String[]) mAttachmentIdList
						.toArray(new String[mAttachmentIdList.size()]),
				new OnEMFSendMsgCallback() {

					@Override
					public void OnEMFSendMsg(boolean isSuccess, String errno,
							String errmsg, EMFSendMsgItem item,
							EMFSendMsgErrorItem errItem) {
						// TODO Auto-generated method stub
						Message msg = Message.obtain();
						if (isSuccess) {
							// 成功
							msg.what = RequestFlag.REQUEST_SUCCESS.ordinal();
						} else {
							msg.what = RequestFlag.REQUEST_FAIL.ordinal();
						}
						MessageCallbackItem callbackItem = new MessageCallbackItem(
								errno, errmsg);
						callbackItem.womanId = womanId;
						callbackItem.sendItem = item;
						callbackItem.errItem = errItem;
						msg.obj = callbackItem;
						sendUiMessage(msg);
					}
				});
	}

	/**
	 * 发送虚拟礼物
	 */
	public boolean SendGift(String womanId, String mailId) {
		if (mVgIdList != null && mVgIdList.size() > 0) {
			// 此处应有菊花
			showProgressDialog("Loading...");

			TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);

			String user_sid = "";
			String user_id = "";
			LoginParam param = LoginManager.getInstance().GetLoginParam();
			if (param != null && param.item != null) {
				user_sid = param.item.sessionid;
				user_id = param.item.manid;
			}

			RequestOperator.getInstance().SendGift(womanId, mVgIdList.get(0),
					RequestJni.GetDeviceId(tm), mailId, UseType.EMF, user_sid,
					user_id, new OnRequestCallback() {

						@Override
						public void OnRequest(boolean isSuccess, String errno,
								String errmsg) {
							// TODO Auto-generated method stub
							Message msg = Message.obtain();
							if (isSuccess) {
								// 成功
								msg.what = RequestFlag.REQUEST_SENDGIFT_SUCCESS
										.ordinal();
							} else {
								msg.what = RequestFlag.REQUEST_SENDGIFT_FAIL
										.ordinal();
							}
							MessageCallbackItem callbackItem = new MessageCallbackItem(
									errno, errmsg);
							msg.obj = callbackItem;
							sendUiMessage(msg);
						}
					});
			return true;
		}
		return false;
	}

	@SuppressWarnings("deprecation")
	@Override
	protected void handleUiMessage(Message msg) {
		// TODO Auto-generated method stub
		super.handleUiMessage(msg);

		// 收起菊花
		hideProgressDialog();

		// 获取数据
		MessageCallbackItem callbackItem = (MessageCallbackItem) msg.obj;

		switch (RequestFlag.values()[msg.what]) {
		case REQUEST_SUCCESS: {
			/* 发送成功，添加到现有联系人 */
			FlatToast.showStickToast(mContext, "Sent!", FlatToast.StikyToastType.DONE);
			ContactManager.getInstance().updateBySendEMF(mCurrentLady);

			// 发送成功
			if (callbackItem.sendItem != null
					&& SendGift(callbackItem.womanId, callbackItem.sendItem.id)) {
				// 需要发送虚拟礼物

			} else {
				// 不需要发送虚拟礼物
				// 清空草稿
				tvEMFBody.setText("");
				SaveMailBody();
				finish();
			}
		}
			break;
		case REQUEST_FAIL: {
			// 请求失败, 弹出对话框
			// 根据错误码处理
			switch (callbackItem.errno) {
			case RequestErrorCode.MBCE10003: {
				// 信用点 >1 但不足够发送本邮件
				if (!mChooseRemoveAttachmentDialog.isShowing()
						&& mAttachmentIdList.size() > 0) {
					if(isActivityVisible()){
						mChooseRemoveAttachmentDialog.show();
					}
					mChooseRemoveAttachmentDialog.textViewBounds
							.setText(callbackItem.errItem.money);
					mChooseRemoveAttachmentDialog.textViewRemoveAttachment
							.setOnClickListener(new OnClickListener() {

								@Override
								public void onClick(View v) {
									// TODO Auto-generated method stub
									mChooseRemoveAttachmentDialog.dismiss();
								}
							});
					mChooseRemoveAttachmentDialog.textViewAddCredit
							.setOnClickListener(new OnClickListener() {

								@Override
								public void onClick(View v) {
									// TODO Auto-generated method stub
									mChooseRemoveAttachmentDialog.dismiss();
								}
							});
				} else {
					// 弹出充值页面
					final GetMoreCreditDialog dialog = new GetMoreCreditDialog(
							mContext, R.style.ChoosePhotoDialog);
					if(isActivityVisible()){
						dialog.show();
					}
				}
			}
				break;
			default: {
				// 网络超时, 或者其他错误
				if (!mErrorDialog.isShowing()) {
					if(isActivityVisible()){
						mErrorDialog.show();
					}
				}
			}
				break;
			}
		}
			break;
		case REQUEST_BOUNDS_SUCCESS: {
			// 请求积分成功
			String ladyId = etLadyId.getText().toString().toUpperCase();
			if (ladyId.compareTo(callbackItem.womanId.toUpperCase()) == 0) {
				if(LoginManager.getInstance().GetLoginStatus() == LoginStatus.LOGINED){
					LoginParam loginParam = LoginManager.getInstance().GetLoginParam();
					if(loginParam.item.bpemf){
						/*风控使用积分发邮件*/
						tvBonusPoint.setVisibility(View.GONE);
					}else{
						tvBonusPoint.setVisibility(View.VISIBLE);
					}
				}else{
					/*无法风控，走旧流程*/
					tvBonusPoint.setVisibility(View.VISIBLE);
				}
			}
		}
			break;
		case REQUEST_BOUNDS_FAIL: {
			// 请求积分失败
		}
			break;
		case REQUEST_LADY_DETAIL_SUCCESS: {
			// 请求女士详情成功

			// 没有请求详细
			progressCount--;
			if (progressCount == 0) {
				progressBarId.setVisibility(View.GONE);
			}

			mCurrentLady = callbackItem.ladyDetail;

			// 接口返回女士是否当前女士
			String ladyId = etLadyId.getText().toString().toUpperCase();
			if (ladyId.compareTo(callbackItem.womanId.toUpperCase()) == 0) {
				// 刷新数据
				isRequestingLadyDetail = false;
				etLadyId.setTag(true);
				progressBarId.setVisibility(View.GONE);
				layoutId.setVisibility(View.VISIBLE);
				etLadyId.setVisibility(View.INVISIBLE);

				editTextId.setText(mCurrentLady.womanid);
				tvEMFBody.requestFocus();
				String localPath = FileCacheManager.getInstance()
						.CacheImagePathFromUrl(mCurrentLady.photoMinURL);

				if (mLoader != null) {
					mLoader.ResetImageView();
				}
				mLoader = new ImageViewLoader(this);
				mLoader.SetDefaultImage(getResources().getDrawable(
						R.drawable.female_default_profile_photo_40dp));
				mLoader.DisplayImage(imageViewId, mCurrentLady.photoMinURL,
						localPath, null);
				imageViewId.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						LadyDetailActivity.launchLadyDetailActivity(mContext,
								mCurrentLady.womanid, true);
					}
				});

				// 查询是否有可用积分
				integralCheck();
			}
		}
			break;
		case REQUEST_LADY_DETAIL_FAIL: {
			// 请求女士详情失败

			// 没有请求详细
			progressCount--;
			if (progressCount == 0) {
				progressBarId.setVisibility(View.GONE);
			}

			// 如果错误码不是网络超时, 显示女士Id不正确
			if (callbackItem.errno
					.compareTo(RequestErrorCode.LOCAL_ERROR_CODE_TIMEOUT) != 0) {
				// 接口返回女士是否当前女士
				String ladyId = etLadyId.getText().toString().toUpperCase();
				if (ladyId.compareTo(callbackItem.womanId.toUpperCase()) == 0) {
					// 刷新数据
					isRequestingLadyDetail = false;
					progressBarId.setVisibility(View.GONE);
					layoutId.setVisibility(View.GONE);
					etLadyId.setVisibility(View.VISIBLE);
					etLadyId.setTextColor(Color.RED);
					etLadyId.setTag(false);
				}

			} else {

			}
		}
			break;
		case REQUEST_SENDGIFT_SUCCESS: {
			// 发送虚拟礼物成功
			// 清空草稿
			tvEMFBody.setText("");
			SaveMailBody();
			finish();
		}
			break;
		case REQUEST_SENDGIFT_FAIL: {
			// 发送虚拟礼物失败
			// 清空草稿
			tvEMFBody.setText("");
			SaveMailBody();
			finish();
		}
			break;
		default:
			break;
		}
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		if (mUploadList != null) {
			for (EMFAttachmentUploader item : mUploadList) {
				item.onDestroy();
			}
			mUploadList.clear();
		}
	}

	/**
	 * 根据女士Id显示邮件内容
	 */
	public void ReloadDraft() {
		if (etLadyId.getText().toString().length() > 0
				&& tvEMFBody.getText().toString().length() == 0) {
			try {
				SharedPreferences mSharedPreferences = mContext
						.getSharedPreferences("base64", Context.MODE_PRIVATE);
				String personBase64 = mSharedPreferences.getString(etLadyId
						.getText().toString(), "");
				byte[] base64Bytes = Base64.decode(personBase64.getBytes(),
						Base64.DEFAULT);
				ByteArrayInputStream bais = new ByteArrayInputStream(
						base64Bytes);
				ObjectInputStream ois = new ObjectInputStream(bais);
				String body = (String) ois.readObject();
				tvEMFBody.setText(body);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 根据女士Id保存邮件内容
	 */
	private void SaveMailBody() {
		if (etLadyId.getText().toString().length() > 0
				&& tvEMFBody.getText().toString() != null) {
			SharedPreferences mSharedPreferences = getSharedPreferences(
					"base64", Context.MODE_PRIVATE);

			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ObjectOutputStream oos;
			try {
				oos = new ObjectOutputStream(baos);
				oos.writeObject(tvEMFBody.getText().toString());
				String personBase64 = new String(Base64.encode(
						baos.toByteArray(), Base64.DEFAULT));
				SharedPreferences.Editor editor = mSharedPreferences.edit();
				editor.putString(etLadyId.getText().toString(), personBase64);
				editor.commit();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	/**
	 * 删除草稿
	 */
	private void DeleteDraft() {
		SharedPreferences mSharedPreferences = getSharedPreferences("base64",
				Context.MODE_PRIVATE);

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		@SuppressWarnings("unused")
		ObjectOutputStream oos = null;
		try {
			oos = new ObjectOutputStream(baos);
			String personBase64 = new String(Base64.encode(baos.toByteArray(),
					Base64.DEFAULT));
			SharedPreferences.Editor editor = mSharedPreferences.edit();
			editor.putString(etLadyId.getText().toString(), personBase64);
			editor.commit();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * 根据女士Id获取邮件内容
	 * 
	 * @return
	 */
	private String GetMailBody() {
		String item = "";

		if (etLadyId.getText().toString().length() > 0) {
			try {
				SharedPreferences mSharedPreferences = getSharedPreferences(
						"base64", Context.MODE_PRIVATE);
				String personBase64 = mSharedPreferences.getString(etLadyId
						.getText().toString(), "");
				byte[] base64Bytes = Base64.decode(personBase64.getBytes(),
						Base64.DEFAULT);
				ByteArrayInputStream bais = new ByteArrayInputStream(
						base64Bytes);
				ObjectInputStream ois = new ObjectInputStream(bais);
				item = (String) ois.readObject();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return item;
	}

	/**
	 * 清空临时附件目录
	 */
//	private void ClearCaceh() {
//		String cmd = "rm -rf " + EMFHelper.getEMFPath();
//		try {
//			Runtime.getRuntime().exec(cmd);
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	}

	/**
	 * #########################################################################
	 * ################ 以下是检查发送逻辑
	 */

	/**
	 * 是否需要保存草稿
	 * 
	 * @return
	 */
	public boolean CheckNeedSaveDraft() {
		if (etLadyId.getText().toString().length() > 0
				&& tvEMFBody.getText().toString().length() > 0) {
			return true;
		}
		return false;
	}

	/**
	 * 检查是否全部附件上传完毕
	 * 
	 * @return true:不是全部已经上传, false:全部已经上传
	 */
	public boolean CheckAttachmentAllUpload() {
		for (EMFAttachmentUploader item : mUploadList) {
			if (!item.IsUploadSuccess()) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 检查是否已经选择虚拟礼物
	 * 
	 * @return
	 */
	public boolean CheckSelecedVirtualGift() {
		if (mVgIdList.size() != 0) {
			return true;
		}
		return false;
	}

	/**
	 * 检查是否超过最大附件数
	 * 
	 * @return
	 */
	public boolean CheckMaxAttachment() {
		if (mUploadList.size() >= MAX_ATTACHMENT) {
			return true;
		}
		return false;
	}

	/**
	 * 检查女士Id和内容是否为空
	 * 
	 * @return
	 */
	public boolean CheckIdAndBody() {
		if (etLadyId.getText().toString().length() == 0
				|| tvEMFBody.getText().toString().length() == 0) {
			return true;
		}
		return false;
	}

	/**
	 * #########################################################################
	 * ################
	 */

	@Override
	/**
	* OnItemClickListener callback
	*/
	public void onItemClick(AdapterView<?> parent, View view,
			int position, long id) {
		// TODO Auto-generated method stub
		ArrayList<EMFAttachmentBean> attachList = new ArrayList<EMFAttachmentBean>();
		for (int i = 0; i < mViewsList.size(); i++) {
			attachList.add((EMFAttachmentBean) mViewsList.get(i)
					.getTag());
		}
		startActivity(EMFAttachmentPreviewActivity.getIntent(
				MailEditActivity.this, attachList, position));
	}

	@Override
	public void OnUploadFinish(boolean isSuccess, String errno, String errmsg,
			String attachId) {
		// TODO Auto-generated method stub
		if (isSuccess) {
			// 上传附件成功
			synchronized (mAttachmentIdList) {
				mAttachmentIdList.add(attachId);
			}
		} else {
			// 上传附件失败
		}
	}

	@Override
	public void OnClickCancel(EMFAttachmentUploader uploader) {
		// TODO Auto-generated method stub
		// 点击取消
		synchronized (mAttachmentIdList) {
			mAttachmentIdList.remove(uploader.GetAttachmentId());
		}

		mViewsList.remove(uploader.GetView());
		mUploadList.remove(uploader);
		mAdapter.notifyDataSetChanged();
	}

	@Override
	public void OnClickWarning(final EMFAttachmentUploader uploader) {
		// TODO Auto-generated method stub
		// 点击警告, 弹出对话框
		final MaterialDialogAlert dialog = new MaterialDialogAlert(
				mContext);
		dialog.setMessage(uploader.GetErrmsg());

		switch (uploader.GetErrno()) {
		case RequestErrorCode.MBCE65001: {
			// 附件大小不合法(5m)
		}
			break;
		case RequestErrorCode.MBCE65004: {
			// 附件格式不正确(非jpg)
		}
			break;
		default: {
			// 增加重试按钮
			dialog.addButton(dialog.createButton(
					getString(R.string.common_btn_retry),
					new OnClickListener() {

						@Override
						public void onClick(View v) {
							// TODO Auto-generated method stub
							// 附件上传失败, 重试
							uploader.Reload();
							dialog.dismiss();
						}
					}));
		}
			break;
		}

		dialog.addButton(dialog.createButton(
				getString(R.string.common_btn_remove),
				new OnClickListener() {

					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						// 附件上传失败, 删除
						synchronized (mAttachmentIdList) {
							mAttachmentIdList.remove(uploader
									.GetAttachmentId());
						}

						mViewsList.remove(uploader.GetView());
						mUploadList.remove(uploader);
						mAdapter.notifyDataSetChanged();
						dialog.dismiss();
					}
				}));
		dialog.show();
		//
		// CustomInfoDialogFragment fragment =
		// CustomInfoDialogFragment.newInstance(
		// getString(R.string.common_btn_retry),
		// getString(R.string.common_btn_remove),
		// getString(R.string.emf_attachment_fail)
		// );
		// fragment.mParam = uploader;
		// fragment.show(getSupportFragmentManager(),
		// DIALOG_REUPLOAD_ATTACHMENT_TAG);
	}
}
