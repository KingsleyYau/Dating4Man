package com.qpidnetwork.dating.lovecall;

import java.text.SimpleDateFormat;
import java.util.Locale;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Message;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.qpidnetwork.dating.R;
import com.qpidnetwork.dating.authorization.LoginPerfence;
import com.qpidnetwork.dating.bean.LoveCallBean;
import com.qpidnetwork.dating.bean.RequestBaseResponse;
import com.qpidnetwork.dating.contacts.ContactManager;
import com.qpidnetwork.framework.base.BaseActionBarFragmentActivity;
import com.qpidnetwork.framework.util.Log;
import com.qpidnetwork.framework.util.SystemUtil;
import com.qpidnetwork.manager.FileCacheManager;
import com.qpidnetwork.manager.MonthlyFeeManager;
import com.qpidnetwork.request.OnConfirmLovecallCallback;
import com.qpidnetwork.request.RequestJniLoveCall.ConfirmType;
import com.qpidnetwork.request.RequestJniMonthlyFee;
import com.qpidnetwork.request.RequestJniMonthlyFee.MemberType;
import com.qpidnetwork.request.RequestOperator;
import com.qpidnetwork.request.item.MonthLyFeeTipItem;
import com.qpidnetwork.tool.ImageViewLoader;
import com.qpidnetwork.view.ButtonRaised;
import com.qpidnetwork.view.GetMoreCreditDialog;
import com.qpidnetwork.view.MaterialAppBar;
import com.qpidnetwork.view.MaterialDialogAlert;
import com.qpidnetwork.view.MaterialThreeButtonDialog;
import com.qpidnetwork.view.MonthlyFeeDialog;

public class LoveCallDetailActivity extends BaseActionBarFragmentActivity {
	private static final String LOVE_CALL_DETAIL_ITEM = "lovecallitem";
	public static final String LOVE_CALL_ORDER_ID = "orderId";

	private static final int LOVECALL_CONFIRM_SUCCESS = 0;
	private static final int LOVECALL_CONFIRM_FAILED = 1;
	private static final int LOVECALL_DECLINE_SUCCESS = 2;
	private static final int LOVECALL_DECLINE_FAILED = 3;
	private static final String DIALOG_TERMS_CALL_TAG = "decline_retry";

	private ImageView ivPhoto;
	private TextView tvName;
	private TextView tvAge;
	private TextView tvCountry;
	private TextView tvDesc;

	private TextView tvSubTitle;
	private TextView tvLoveCallDate;
	private TextView tvLoveCallPeriod;
	private FrameLayout flCallId;
	private TextView tvCallId;
	private TextView tvThirdCall;

	private LinearLayout llMessage;
	private TextView tvMessage;

	private LoveCallBean lovecallBean;
	
	private ButtonRaised btnConfirm;
	private ButtonRaised btnDecline;
	private ButtonRaised btnCall;
	private Button btnNoWork;

	public static Intent getIntent(Context context, LoveCallBean bean) {
		Intent intent = new Intent(context, LoveCallDetailActivity.class);
		intent.putExtra(LOVE_CALL_DETAIL_ITEM, bean);
		return intent;
	}

	@Override
	protected void onCreate(Bundle arg0) {
		// TODO Auto-generated method stub
		super.onCreate(arg0);

		setCustomContentView(R.layout.activity_lovecall_detail);

		Bundle bundle = getIntent().getExtras();
		if (bundle != null) {
			if (bundle.containsKey(LOVE_CALL_DETAIL_ITEM)) {
				lovecallBean = bundle.getParcelable(LOVE_CALL_DETAIL_ITEM);
			}
		}
		if (lovecallBean != null) {
			initViews();
			initData();
		}
	}

	private void initViews() {

		/* 公共部分 */
		ivPhoto = (ImageView) findViewById(R.id.ivPhoto);
		tvName = (TextView) findViewById(R.id.tvName);
		tvAge = (TextView) findViewById(R.id.tvAge);
		tvCountry = (TextView) findViewById(R.id.tvCountry);
		tvDesc = (TextView) findViewById(R.id.tvDesc);

		tvSubTitle = (TextView) findViewById(R.id.tvSubTitle);
		tvLoveCallDate = (TextView) findViewById(R.id.tvLoveCallDate);
		tvLoveCallPeriod = (TextView) findViewById(R.id.tvLoveCallPeriod);
		flCallId = (FrameLayout) findViewById(R.id.flCallId);
		tvCallId = (TextView) findViewById(R.id.tvCallId);
		tvThirdCall = (TextView) findViewById(R.id.tvThirdCall);

		llMessage = (LinearLayout) findViewById(R.id.llMessage);
		tvMessage = (TextView) findViewById(R.id.tvMessage);
		
		View vsSchedule = (View)findViewById(R.id.vsSchedule);
		View vsRequest = (View) findViewById(R.id.vsRequest);

		if (lovecallBean.isconfirm) {
			// schedule detail
//			ViewStub stub = (ViewStub) findViewById(R.id.vsSchedule);
//			stub.inflate();
			vsSchedule.setVisibility(View.VISIBLE);
			vsRequest.setVisibility(View.GONE);
			btnCall = (ButtonRaised) findViewById(R.id.btnCall);
			btnNoWork = (Button) findViewById(R.id.btnNoWork);
			btnCall.setOnClickListener(this);
			btnNoWork.setOnClickListener(this);
		} else {
			// request detail
//			ViewStub stub = (ViewStub) findViewById(R.id.vsRequest);
//			stub.inflate();
//			stub.setVisibility(View.VISIBLE);
			vsRequest.setVisibility(View.VISIBLE);
			vsSchedule.setVisibility(View.GONE);
			btnConfirm = (ButtonRaised) findViewById(R.id.btnConfirm);
			btnDecline = (ButtonRaised) findViewById(R.id.btnDecline);
			btnConfirm.setOnClickListener(this);
			btnDecline.setOnClickListener(this);
		}
		
		getCustomActionBar().setTitle(getString(R.string.lovecall_schedule_details_title), getResources().getColor(R.color.text_color_dark));
		getCustomActionBar().setAppbarBackgroundColor(Color.WHITE);
		getBackButton().setBackgroundResource(MaterialAppBar.TOUCH_FEEDBACK_HOLO_LIGHT);
		getCustomActionBar().changeIconById(R.id.common_button_back, R.drawable.ic_arrow_back_grey600_24dp);
		

	}

	private void initData() {
		
		/*头像处理*/
		if((lovecallBean.image != null)&&(!lovecallBean.image.equals(""))){
			String localPath = FileCacheManager.getInstance().CacheImagePathFromUrl(lovecallBean.image);
			new ImageViewLoader(this).DisplayImage(ivPhoto, lovecallBean.image, localPath, null);
		}
		
		tvName.setText(lovecallBean.firstname);
		tvAge.setText(lovecallBean.age + "");
		tvCountry.setText(lovecallBean.country);

		tvLoveCallDate.setText(new SimpleDateFormat("dd MMM, yyyy", Locale.getDefault()).format(lovecallBean.longbegintime));

		//Log.v("koa", lovecallBean.begintime + "   "+ (long)lovecallBean.begintime * (long)1000);
		
		tvLoveCallPeriod.setText(new SimpleDateFormat("HH:mm", Locale.getDefault())
				.format(lovecallBean.longbegintime)
				+ " - "
				+ new SimpleDateFormat("HH:mm", Locale.getDefault()).format(lovecallBean.longendtime));

		if (lovecallBean.needtr) {
			tvThirdCall.setVisibility(View.VISIBLE);
		} else {
			tvThirdCall.setVisibility(View.GONE);
		}

		if (lovecallBean.isconfirm) {
			setTitle(R.string.lovecall_schedule_details_title);
			tvSubTitle.setText(R.string.lovecall_schedule_details_subtitle);
			tvDesc.setText(R.string.lovecall_schedule_detail_desc);
			flCallId.setVisibility(View.VISIBLE);
			if (lovecallBean.callid.length() < 5){
				tvCallId.setText(lovecallBean.callid);
			}else{
				tvCallId.setText(lovecallBean.callid.subSequence(0, 4) + " " + lovecallBean.callid.subSequence(4, lovecallBean.callid.length()));
			}
			
			if (lovecallBean.confirmmsg.trim() == null || lovecallBean.confirmmsg.trim().length() == 0){
				llMessage.setVisibility(View.GONE);
			}else{
				llMessage.setVisibility(View.VISIBLE);
			}
			
			
			if (!lovecallBean.isCallActive()){
				btnCall.setButtonTitle(getString(R.string.lovecall_active_after_x, lovecallBean.getWhenCallActive()));
				btnCall.setEnabled(false);
				btnCall.setButtonBackground(getResources().getColor(R.color.standard_grey));
				btnCall.setCardElevation(getResources().getDisplayMetrics().density);
			}
			Log.v("is call activite", lovecallBean.isCallActive() + "");
			
			tvMessage.setText(lovecallBean.confirmmsg);
		} else {
			setTitle(R.string.lovecall_request_details_title);
			tvSubTitle.setText(R.string.lovecall_request_details_subtitle);
			tvDesc.setText(getString(R.string.lovecall_request_time,
					new SimpleDateFormat("dd MMM", Locale.getDefault()).format(lovecallBean.longbegintime)));
			flCallId.setVisibility(View.GONE);

			llMessage.setVisibility(View.GONE);
		}
	}

	@Override
	protected void handleUiMessage(Message msg) {
		// TODO Auto-generated method stub
		super.handleUiMessage(msg);

		switch (msg.what) {
		case LOVECALL_CONFIRM_SUCCESS:
			showToastDone("Confirmed!");
			onConfirmOrDeclineSuccess();
			break;
		case LOVECALL_CONFIRM_FAILED:{
			RequestBaseResponse response = (RequestBaseResponse)msg.obj;
			//先判断月费类型
			MemberType type = RequestJniMonthlyFee.intToMemberType(msg.arg1);
			if (type == MemberType.NO_FEED_FIRST_MONTHLY_MEMBER|| type == MemberType.NO_FEED_MONTHLY_MEMBER) {
				cancelToastImmediately();
				MonthlyFeeManager.getInstance().onMemberTypeUpdate(type);
				MonthLyFeeTipItem monthLyFeeTipItem = MonthlyFeeManager.getInstance().getMonthLyFeeTipItem(type);
				MonthlyFeeDialog dialog = new MonthlyFeeDialog(this,R.style.ChoosePhotoDialog);
				dialog.setData(monthLyFeeTipItem);// 设置数据对象
				if(isActivityVisible()){
					dialog.show();						
				}
			}else{
				if(response.errno.equals("MBCE67007")){
					/*余额不足处理*/
					cancelToastImmediately();
					final GetMoreCreditDialog dialog = new GetMoreCreditDialog(LoveCallDetailActivity.this, R.style.ChoosePhotoDialog);
					if(isActivityVisible()){
						dialog.show();
					}
				}else if(response.errno.equals("MBCE67003")){
					/*过期，无法确认*/
					cancelToastImmediately();
					showLovecallOuttime(response.errmsg);
				}else if(response.errno.equals("MBCE67002")){
					/*重复请求, 算成功*/
					showToastDone("Confirmed!");
					onConfirmOrDeclineSuccess();
				}else{
					/*普通错误提示*/
					showConfirmFailedTips();
				}
			}
		}break;
		case LOVECALL_DECLINE_SUCCESS:
			showToastDone("Declined!");
			onConfirmOrDeclineSuccess();
			break;
		case LOVECALL_DECLINE_FAILED:
			showDeclineFailedTips();
			break;
		}
	}

	private void onConfirmOrDeclineSuccess() {
		Intent intent = new Intent();
		intent.putExtra(LOVE_CALL_ORDER_ID, lovecallBean.orderid);
		setResult(RESULT_OK, intent);
		/*确认订单成功，添加订单女士到联系人列表*/
		if(lovecallBean != null ){
			ContactManager.getInstance().addOrUpdateContact(lovecallBean);
		}
		finish();
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		super.onClick(v);
		switch (v.getId()) {
		case R.id.btnCall:
			makeCall();
			break;
		case R.id.btnNoWork:
			/*无网络按钮及错误弹窗提示*/
			
			break;
		case R.id.btnConfirm:
			showConfirmNotify();
			break;
		case R.id.btnDecline:
			showDeclineNotify();
			break;
		}
	}

	private void confirmLoveCall(final ConfirmType type) {
		showToastProgressing("Loading");
		RequestOperator.getInstance().ConfirmLoveCall(lovecallBean.orderid, type,
				new OnConfirmLovecallCallback() {

					@Override
					public void OnConfirmLovecall(boolean isSuccess, String errno,
							String errmsg, int memberType) {
						// TODO Auto-generated method stub
						Message msg = Message.obtain();
						if (isSuccess) {
							if (type == ConfirmType.CONFIRM) {
								msg.what = LOVECALL_CONFIRM_SUCCESS;
							} else {
								msg.what = LOVECALL_DECLINE_SUCCESS;
							}
						} else {
							if (type == ConfirmType.CONFIRM) {
								msg.what = LOVECALL_CONFIRM_FAILED;
							} else {
								msg.what = LOVECALL_DECLINE_FAILED;
							}
						}
						msg.obj = new RequestBaseResponse(isSuccess, errno, errmsg, null);
						msg.arg1 = memberType;
						sendUiMessage(msg);
					}
				});
	}
	
	/**
	 * 点击confirm时弹出提示框
	 */
	private void showConfirmNotify(){
		MaterialDialogAlert dialog = new MaterialDialogAlert(this);
		dialog.setMessage(getString(R.string.lovecall_confirm_tips));
		dialog.addButton(dialog.createButton(getString(R.string.common_btn_confirm), new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				confirmLoveCall(ConfirmType.CONFIRM);
			}
		}));
		dialog.addButton(dialog.createButton(getString(R.string.common_btn_cancel), null));
		
		dialog.show();
	}
	
	/**
	 * lovecall 过期，无法confirm错误
	 */
	private void showLovecallOuttime(String errMs){
		MaterialDialogAlert dialog = new MaterialDialogAlert(this);
		dialog.setMessage(errMs);
		dialog.addButton(dialog.createButton(getString(R.string.common_btn_ok), null));
		if(isActivityVisible()){
			dialog.show();
		}
	}
	
	/**
	 * confirm 普通错误提示
	 */
	private void showConfirmFailedTips(){
		cancelToastImmediately();
		MaterialDialogAlert dialog = new MaterialDialogAlert(this);
		dialog.setMessage(getString(R.string.lovecall_confirm_fail_tips));
		dialog.addButton(dialog.createButton(getString(R.string.common_btn_retry), new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				confirmLoveCall(ConfirmType.CONFIRM);
			}
		}));
		dialog.addButton(dialog.createButton(getString(R.string.common_btn_cancel), null));
		if(isActivityVisible()){
			dialog.show();
		}
	}
	
	/**
	 * decline 点击弹出提示
	 */
	private void showDeclineNotify(){
		MaterialDialogAlert dialog = new MaterialDialogAlert(this);
		dialog.setMessage(getString(R.string.lovecall_decline_tips));
		dialog.addButton(dialog.createButton(getString(R.string.common_btn_yes), new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				confirmLoveCall(ConfirmType.REJECT);
			}
		}));
		dialog.addButton(dialog.createButton(getString(R.string.common_btn_no), null));
		
		dialog.show();
	}
	
	/**
	 * decline 失败错误弹窗提示
	 */
	private void showDeclineFailedTips(){
		
		cancelToastImmediately();
		MaterialDialogAlert dialog = new MaterialDialogAlert(this);
		dialog.setMessage(getString(R.string.lovecall_decline_fail_tips));
		dialog.addButton(dialog.createButton(getString(R.string.common_btn_retry), new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				confirmLoveCall(ConfirmType.REJECT);
			}
		}));
		dialog.addButton(dialog.createButton(getString(R.string.common_btn_cancel), null));
		if(isActivityVisible()){
			dialog.show();
		}
	}
	
	private void makeCall() {
		/* 检测有无Sim卡 */
		if (SystemUtil.isSimCanUse(this)) {
			/*资费提示*/
			
			if (LoginPerfence.GetStringPreference(LoveCallDetailActivity.this, "donnot_show_love_call_fee").equals("true")){
				new DirectCallManager(LoveCallDetailActivity.this).makeCall(lovecallBean.centerid, lovecallBean.callid);
				/*获取token成功，去拨号，添加到现有联系人*/
				if(lovecallBean != null){
					ContactManager.getInstance().addOrUpdateContact(lovecallBean);
				}
				return;
			}
			
			MaterialThreeButtonDialog dialog = new MaterialThreeButtonDialog(this, new MaterialThreeButtonDialog.OnClickCallback() {
				
				@Override
				public void OnSecondButtonClick(View v) {
					// TODO Auto-generated method stub
					new DirectCallManager(LoveCallDetailActivity.this).makeCall(lovecallBean.centerid, lovecallBean.callid);
					/*获取token成功，去拨号，添加到现有联系人*/
					if(lovecallBean != null){
						ContactManager.getInstance().addOrUpdateContact(lovecallBean);
					}
					LoginPerfence.SaveStringPreference(LoveCallDetailActivity.this, "donnot_show_love_call_fee", "true");
				}
				
				@Override
				public void OnFirstButtonClick(View v) {
					// TODO Auto-generated method stub
					new DirectCallManager(LoveCallDetailActivity.this).makeCall(lovecallBean.centerid, lovecallBean.callid);
					/*获取token成功，去拨号，添加到现有联系人*/
					if(lovecallBean != null){
						ContactManager.getInstance().addOrUpdateContact(lovecallBean);
					}
					Log.v(lovecallBean.centerid, lovecallBean.callid);
				}
				
				@Override
				public void OnCancelButtonClick(View v) {
					// TODO Auto-generated method stub
					
				}
			});
			
			dialog.hideImageView();
			dialog.setTitle(getString(R.string.lovecall_terms_title));
			dialog.setMessage(getString(R.string.lovecall_terms_detail));
			dialog.setFirstButtonText(getString(R.string.lovecall_call_now));
			dialog.setSecondButtonText(getString(R.string.love_call_dont_tell_again));
			dialog.getMessage().setGravity(Gravity.LEFT);
			dialog.getTitle().setGravity(Gravity.LEFT);
			dialog.show();

			
		} else {
			MaterialDialogAlert dialog = new MaterialDialogAlert(this);
			dialog.setTitle(getString(R.string.lovecall_no_sim_tips));
			dialog.setMessage(getString(R.string.lovecall_instruction, lovecallBean.centerid));
			dialog.addButton(dialog.createButton(getString(R.string.common_btn_ok), null));
			dialog.show();
		}
	}
	
	public void onPositiveClick(View v, boolean isChecked, String tag) {
		// TODO Auto-generated method stub
		if (tag.equals(DIALOG_TERMS_CALL_TAG)) {
			
		}
	}
}
