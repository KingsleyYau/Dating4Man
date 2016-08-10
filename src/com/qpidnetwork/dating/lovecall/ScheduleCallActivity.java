package com.qpidnetwork.dating.lovecall;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Message;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.StyleSpan;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.TextView;

import com.qpidnetwork.dating.R;
import com.qpidnetwork.dating.bean.RequestBaseResponse;
import com.qpidnetwork.dating.lady.LadyDetailManager;
import com.qpidnetwork.dating.lady.LadyDetailManager.OnLadyDetailManagerQueryLadyDetailCallback;
import com.qpidnetwork.framework.base.BaseActionBarFragmentActivity;
import com.qpidnetwork.framework.widget.CircleImageView;
import com.qpidnetwork.manager.FileCacheManager;
import com.qpidnetwork.manager.MonthlyFeeManager;
import com.qpidnetwork.request.OnEMFSendMsgCallback;
import com.qpidnetwork.request.RequestErrorCode;
import com.qpidnetwork.request.RequestJniEMF.ReplyType;
import com.qpidnetwork.request.RequestJniMonthlyFee.MemberType;
import com.qpidnetwork.request.RequestOperator;
import com.qpidnetwork.request.item.EMFSendMsgErrorItem;
import com.qpidnetwork.request.item.EMFSendMsgItem;
import com.qpidnetwork.request.item.LadyDetail;
import com.qpidnetwork.request.item.MonthLyFeeTipItem;
import com.qpidnetwork.tool.ImageViewLoader;
import com.qpidnetwork.view.ButtonRaised;
import com.qpidnetwork.view.GetMoreCreditDialog;
import com.qpidnetwork.view.MaterialAppBar;
import com.qpidnetwork.view.MaterialDatePickerDialog;
import com.qpidnetwork.view.MaterialDatePickerDialog.DateSelectCallback;
import com.qpidnetwork.view.MaterialDialogAlert;
import com.qpidnetwork.view.MaterialDialogSingleChoice;
import com.qpidnetwork.view.MaterialTimePickerDialog;
import com.qpidnetwork.view.MaterialTimePickerDialog.TimeSelectCallback;
import com.qpidnetwork.view.MonthlyFeeDialog;

/**
 * 邮件方式通知女士预约电话（拨号失败提供预约服务）
 * @author Hunter
 * @since 2016.6.2
 */
public class ScheduleCallActivity extends BaseActionBarFragmentActivity implements DateSelectCallback, TimeSelectCallback{
	
	private static final String LADY_INFO = "ladyInfo";
	
	private static final int SEND_EMF_CALLBACK = 1;
	private static final int QUERY_LADY_DETAIL =2;
	
	private CircleImageView ivPhoto;
	private TextView tvName;
	private TextView tvLadyInfo;
	
	private TextView tvSelectCountry;
	private TextView tvSelectTimezone;
	private TextView tvSelectDate;
	private TextView tvSelectTime;
	private EditText etMessage;
	
	private TextView tvTerms;
	
	private int countryIndex = -1;
	private int cityIndex = -1;
	private int mYear = -1;
	private int mMonth = -1;
	private int mDay = -1;
	private int mHour = -1;
	private int mMinute = -1;
	private LadyDetail ladyDetail = null;

	public static void launchScheduleCallActivity(Context context, LadyDetail ladyDetail){
		Intent intent = new Intent(context, ScheduleCallActivity.class);
		intent.putExtra(LADY_INFO, ladyDetail);
		context.startActivity(intent);
	}
	
	@Override
	protected void onCreate(Bundle arg0) {
		// TODO Auto-generated method stub
		super.onCreate(arg0);
		setCustomContentView(R.layout.activity_schedule_call);
		
		/* 初始化头部 */
		getCustomActionBar().setTouchFeedback(MaterialAppBar.TOUCH_FEEDBACK_HOLO_LIGHT);
		getCustomActionBar().setTitle(getString(R.string.lovecall_mail_schedule_title),getResources().getColor(R.color.text_color_dark));
		getCustomActionBar().changeIconById(R.id.common_button_back,R.drawable.ic_arrow_back_grey600_24dp);
		getCustomActionBar().getButtonById(R.id.common_button_back).setBackgroundResource(MaterialAppBar.TOUCH_FEEDBACK_HOLO_LIGHT);
		getCustomActionBar().setAppbarBackgroundDrawable(new ColorDrawable(Color.WHITE));
		
		initViews();
		initData();
	}
	
	private void initViews(){
		ivPhoto = (CircleImageView)findViewById(R.id.ivPhoto);
		tvName = (TextView)findViewById(R.id.tvName);
		tvLadyInfo = (TextView)findViewById(R.id.tvLadyInfo);
		
		tvSelectCountry = (TextView)findViewById(R.id.tvSelectCountry);
		tvSelectTimezone = (TextView)findViewById(R.id.tvSelectTimezone);
		tvSelectDate = (TextView)findViewById(R.id.tvSelectDate);
		tvSelectTime = (TextView)findViewById(R.id.tvSelectTime);
		etMessage = (EditText)findViewById(R.id.etMessage);
		ButtonRaised btnSend = (ButtonRaised)findViewById(R.id.btnSend);
		tvSelectCountry.setOnClickListener(this);
		tvSelectTimezone.setOnClickListener(this);
		tvSelectDate.setOnClickListener(this);
		tvSelectTime.setOnClickListener(this);
		btnSend.setOnClickListener(this);
		tvSelectTimezone.setClickable(false);
		
		//SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy");
		mYear = Calendar.getInstance().get(Calendar.YEAR);
		mMonth = Calendar.getInstance().get(Calendar.MONTH);
		mDay = Calendar.getInstance().get(Calendar.DATE);
		tvSelectDate.setText(mDay + "/" + mMonth + "/" + mYear);
		
		tvTerms = (TextView)findViewById(R.id.tvTerms);
		String tips = getString(R.string.lovecall_mail_schedule_tips_terms2);
		SpannableString sp = new SpannableString(tips);
		ClickableSpan clickableSpan = new ClickableSpan() {

			@Override
			public void onClick(View widget) {
				//open lovecall request
				Intent intent = new Intent(ScheduleCallActivity.this, LoveCallListActivity.class);
				startActivity(intent);
			}
		};
		sp.setSpan(new StyleSpan(Typeface.BOLD),
				tips.indexOf("Open"), tips.indexOf("Open") + "Open".length(),
				Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
		sp.setSpan(clickableSpan,
				tips.indexOf("Open"), tips.indexOf("Open") + "Open".length(),
				Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
		tvTerms.setText(sp);
		tvTerms.setLinkTextColor(mContext.getResources().getColor(
				R.color.blue_color));
		tvTerms.setMovementMethod(LinkMovementMethod.getInstance());
	}
	
	private void initData(){
		Bundle extra = getIntent().getExtras();
		if(extra != null){
			if(extra.containsKey(LADY_INFO)){
				ladyDetail = (LadyDetail)extra.getSerializable(LADY_INFO);
			}
		}
		if(ladyDetail == null){
			finish();
		}else{
			if(TextUtils.isEmpty(ladyDetail.firstname)
					||TextUtils.isEmpty(ladyDetail.country)
					||ladyDetail.age <= 0
					||TextUtils.isEmpty(ladyDetail.photoMinURL)){
				//需要重新刷新女士详情
				queryLadyDetail(ladyDetail.womanid);
			}
			if(!TextUtils.isEmpty(ladyDetail.firstname)){
				tvName.setText(ladyDetail.firstname);
			}else if(!TextUtils.isEmpty(ladyDetail.womanid)){
				tvName.setText(ladyDetail.womanid);
			}
			if(!TextUtils.isEmpty(ladyDetail.country)&&ladyDetail.age > 0){
				tvLadyInfo.setText(ladyDetail.age + "Years old," + ladyDetail.country);
			}
			if(!TextUtils.isEmpty(ladyDetail.photoMinURL)){
				String localPath = FileCacheManager.getInstance().CacheImagePathFromUrl(ladyDetail.photoMinURL);
				ImageViewLoader imageDownLoader = new ImageViewLoader(mContext);
				imageDownLoader.SetDefaultImage(mContext.getResources().getDrawable(R.drawable.female_default_profile_photo_40dp));
				imageDownLoader.DisplayImage(ivPhoto, ladyDetail.photoMinURL, localPath, null);
			}
		}
	}
	
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		super.onClick(v);
		switch (v.getId()) {
		case R.id.tvSelectCountry:{
			final String[] countryArray = getResources().getStringArray(R.array.timezone_country);
			MaterialDialogSingleChoice dialog = new MaterialDialogSingleChoice(
					ScheduleCallActivity.this, 
					countryArray,
					new MaterialDialogSingleChoice.OnClickCallback() {
						
						@Override
						public void onClick(AdapterView<?> adptView, View v, int which) {
							// TODO Auto-generated method stub
							if( which > -1 && which < countryArray.length) {
								String countryName = countryArray[which];
								tvSelectCountry.setText(countryName);
								tvSelectTimezone.setClickable(true);
								if(countryIndex != which){
									countryIndex = which;
									cityIndex = -1;
									tvSelectTimezone.setText(getString(R.string.lovecall_mail_schedule_edit_selectcity));
								}
								
							}
						}
					},
					countryIndex>=0?countryIndex:0);
			
			dialog.setTitle(getResources().getString(R.string.lovecall_mail_schedule_dailog_country_title));
			dialog.show();
			dialog.setCanceledOnTouchOutside(true);
		}break;
		
		case R.id.tvSelectTimezone:{
			String[] countryCode = getResources().getStringArray(R.array.timezone_country_short);
			if(countryIndex >= 0 && countryIndex < countryCode.length){
				String timezoneCityArrayKey = countryCode[countryIndex] + "_cities";
				final String[] timezoneCityArray = getResources().getStringArray(getResources().getIdentifier(timezoneCityArrayKey, "array", getPackageName()));
				if(timezoneCityArray != null){
					MaterialDialogSingleChoice dialog = new MaterialDialogSingleChoice(
							ScheduleCallActivity.this, 
							timezoneCityArray,
							new MaterialDialogSingleChoice.OnClickCallback() {
								
								@Override
								public void onClick(AdapterView<?> adptView, View v, int which) {
									// TODO Auto-generated method stub
									if( which > -1 && which < timezoneCityArray.length) {
										cityIndex = which;
										String timezoneCity = timezoneCityArray[which];
										tvSelectTimezone.setText(timezoneCity);
									}
								}
							},
							cityIndex>=0?cityIndex:0);
					
					dialog.setTitle(getResources().getString(R.string.lovecall_mail_schedule_dialog_city_title));
					dialog.show();
					dialog.setCanceledOnTouchOutside(true);
				}
			}
		}break;
		
		case R.id.tvSelectDate:{
	        MaterialDatePickerDialog datePicker = new MaterialDatePickerDialog(this, this,  mYear,  mMonth, mDay);
	        datePicker.show();
		}break;
		
		case R.id.tvSelectTime:{
			int hour = (mHour >= 0) ? mHour : 12;
			int minute = (mMinute >= 0) ? mMinute: 00;
			
	        MaterialTimePickerDialog datePicker = new MaterialTimePickerDialog(this, this,  hour,  minute);
	        datePicker.show();
		}break;
		
		case R.id.btnSend:{
			sendScheduleCallEmail();
		}break;

		default:
			break;
		}
	}
	
	private void sendScheduleCallEmail(){
		boolean checkedOK = true;
		String errMsg = "";
		if(countryIndex<0 || TextUtils.isEmpty(tvSelectCountry.getText().toString())){
			//errMsg = getString(R.string.lovecall_mail_schedule_country_empty);
			//checkedOK = false;
			this.shakeView(tvSelectCountry, true);
			return;
		}
		if(cityIndex<0 || TextUtils.isEmpty(tvSelectTimezone.getText().toString())){
			this.shakeView(tvSelectTimezone, true);
			return;
		}
		if(mYear<0 || mMonth <0 || mDay < 0 
				|| TextUtils.isEmpty(tvSelectDate.getText().toString())){
			this.shakeView(tvSelectDate, true);
			return;
		}
		if(mHour<0 || mMinute<0 
				|| TextUtils.isEmpty(tvSelectTime.getText().toString())){
			this.shakeView(tvSelectTime, true);
			return;
		}
		if(TextUtils.isEmpty(etMessage.getText().toString())){
			this.shakeView(etMessage, true);
			return;
		}
		
		String body = CreateMessgaeBody();
		showToastProgressing(getResources().getString(R.string.common_toast_sending));
		RequestOperator.getInstance().SendLoveCallMsg(ladyDetail.womanid, body, false, ReplyType.DEFAULT, "", new String[0], new String[0], new OnEMFSendMsgCallback() {
			
			@Override
			public void OnEMFSendMsg(boolean isSuccess, String errno, String errmsg,
					EMFSendMsgItem item, EMFSendMsgErrorItem errItem) {
				Message msg = Message.obtain();
				msg.what = SEND_EMF_CALLBACK;
				RequestBaseResponse response = new RequestBaseResponse(isSuccess, errno, errmsg, errItem);
				msg.obj = response;
				sendUiMessage(msg);
			}
		});
		
	}
	
	/**
	 * 生成消息体格式
	 * @return
	 */
	private String CreateMessgaeBody(){
		String body = "";
		String timeZone = tvSelectCountry.getText().toString() + ", " + tvSelectTimezone.getText().toString();
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MMM/dd HH:mm", Locale.ENGLISH);
		Date date = new Date(mYear-1900, mMonth-1, mDay, mHour, mMinute); 
		String time = formatter.format(date);
		String message = etMessage.getText().toString();
		body += getString(R.string.lovecall_mail_schedule_body_title);
		body += String.format(getString(R.string.lovecall_mail_schedule_body_timezone), timeZone);
		body += String.format(getString(R.string.lovecall_mail_schedule_body_datetime), time);
		body += String.format(getString(R.string.lovecall_mail_schedule_body_body), message);
		return body;
	}
	
	private void queryLadyDetail(String womanId){
		LadyDetailManager.getInstance().QueryLadyDetail(womanId,
				new OnLadyDetailManagerQueryLadyDetailCallback() {

					@Override
					public void OnQueryLadyDetailCallback(
							boolean isSuccess, String errno,
							String errmsg, LadyDetail item) {
						// TODO Auto-generated method stub
						if (isSuccess) {
							Message msg = Message.obtain();
							msg.what = QUERY_LADY_DETAIL;
							msg.obj = item;
							sendUiMessage(msg);
						}
					}
				});
	}
	
	@Override
	protected void handleUiMessage(Message msg) {
		// TODO Auto-generated method stub
		super.handleUiMessage(msg);
		switch (msg.what) {
		case SEND_EMF_CALLBACK:{
			RequestBaseResponse response = (RequestBaseResponse)msg.obj;
			if(response.isSuccess){
				showToastDone(getResources().getString(R.string.theme_store_toast_success));
				finish();
			}else{
				cancelToastImmediately();
				EMFSendMsgErrorItem errorItem = (EMFSendMsgErrorItem)response.body;
				if(errorItem != null){
					// 首先判断月费类型
					MemberType type = errorItem.memberType;
					if (type == MemberType.NO_FEED_FIRST_MONTHLY_MEMBER|| type == MemberType.NO_FEED_MONTHLY_MEMBER) {
						MonthlyFeeManager.getInstance().onMemberTypeUpdate(type);
						MonthLyFeeTipItem mMonthLyFeeTipItem = MonthlyFeeManager.getInstance().getMonthLyFeeTipItem(errorItem.memberType);
						if (mMonthLyFeeTipItem != null) {
							MonthlyFeeDialog dialog = new MonthlyFeeDialog(this,R.style.ChoosePhotoDialog);
							dialog.setData(mMonthLyFeeTipItem);// 设置数据对象
							if(isActivityVisible()){
								dialog.show();						
							}
						}
					} else {
						// 根据错误码处理
						switch (response.errno) {
						case RequestErrorCode.MBCE10003: {
							// 弹出充值页面
							final GetMoreCreditDialog dialog = new GetMoreCreditDialog(
									mContext, R.style.ChoosePhotoDialog);
							if (isActivityVisible()) {
								dialog.show();
							}
						}break;
						default: {
							// 网络超时, 或者其他错误
							MaterialDialogAlert dialog = new MaterialDialogAlert(mContext);
							dialog.setMessage(mContext.getString(R.string.lovecall_mail_schedule_send_fail));
							dialog.addButton(dialog.createButton(
									mContext.getString(R.string.common_btn_yes),
									new OnClickListener() {

										@Override
										public void onClick(View v) {
											sendScheduleCallEmail();
										}
									}));
							dialog.addButton(dialog.createButton(
									mContext.getString(R.string.common_btn_no),
									new OnClickListener() {

										@Override
										public void onClick(View v) {

										}

									}));
							if (isActivityVisible()) {
								dialog.show();
							}
						}
							break;
						}
					}

				}
			}
		}break;
		
		case QUERY_LADY_DETAIL:{
			LadyDetail item = (LadyDetail)msg.obj;
			if(item != null){
				ladyDetail = item;
				if(!TextUtils.isEmpty(ladyDetail.firstname)){
					tvName.setText(ladyDetail.firstname);
				}
				if(!TextUtils.isEmpty(ladyDetail.country)&&ladyDetail.age > 0){
					tvLadyInfo.setText(ladyDetail.age + "Years old," + ladyDetail.country);
				}
				if(!TextUtils.isEmpty(ladyDetail.photoMinURL)){
					String localPath = FileCacheManager.getInstance().CacheImagePathFromUrl(ladyDetail.photoMinURL);
					ImageViewLoader imageDownLoader = new ImageViewLoader(mContext);
					imageDownLoader.SetDefaultImage(mContext.getResources().getDrawable(R.drawable.female_default_profile_photo_40dp));
					imageDownLoader.DisplayImage(ivPhoto, ladyDetail.photoMinURL, localPath, null);
				}
			}
		}break;
		default:
			break;
		}
	}

	@Override
	public void onDateSelected(int year, int month, int day) {
		// TODO Auto-generated method stub
		mYear = year;
		mMonth = month+1;
		mDay = day;
	    tvSelectDate.setText(mDay + "/" + mMonth + "/" + mYear);
	}

	@Override
	public void onTimeSelected(int hour, int minute) {
		// TODO Auto-generated method stub
		mHour = hour;
		mMinute = minute;
		String minute1 = "";
		String hour1 = "";
		if(minute < 10){
			minute1 = "0" + minute;
		}else{
			minute1 = String.valueOf(minute);
		}
		if(hour < 10){
			hour1 = "0" + hour;
		}else{
			hour1 = String.valueOf(hour);
		}
	    tvSelectTime.setText(hour1 + "：" + minute1);
	}

}
