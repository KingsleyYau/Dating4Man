package com.qpidnetwork.dating.authorization;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Message;
import android.view.View;

import com.qpidnetwork.dating.R;
import com.qpidnetwork.dating.authorization.LoginManager.OnLoginManagerCallback;
import com.qpidnetwork.dating.bean.RequestBaseResponse;
import com.qpidnetwork.framework.base.BaseFragmentActivity;
import com.qpidnetwork.request.RequestErrorCode;
import com.qpidnetwork.request.item.LoginErrorItem;
import com.qpidnetwork.request.item.LoginItem;
import com.qpidnetwork.view.MaterialAppBar;
import com.qpidnetwork.view.MaterialDatePickerDialog;
import com.qpidnetwork.view.MaterialTextField;
import com.qpidnetwork.view.MaterialTextField.OnFocuseChangedCallback;

/**
 * 认证模块
 * Facebook绑定邮箱界面
 * @author Max.Chiu
 *
 */
public class RegisterByFacebookActivity extends BaseFragmentActivity 
										implements OnLoginManagerCallback,
												   MaterialDatePickerDialog.DateSelectCallback,
												   OnFocuseChangedCallback
{
	public static final int REQUEST_SUCCESS = 0;
	public static final int REQUEST_FAIL = 1;
	
	private View lastFocusedView;
	private MaterialTextField editTextEmail;
	private MaterialTextField editTextViewBirthday;
	private MaterialAppBar appbar;
		
	private String year = "1970";
	private String month = "1";
	private String day = "1";
	
	private enum RequestFlag {
		REQUEST_SUCCESS,
		REQUEST_FAIL,
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// 初始化界面
		InitView();
		
		// 增加登录状态改变监听
		LoginManager.getInstance().AddListenner(this);
	}
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		
		// 删除登录状态改变监听
		LoginManager.getInstance().RemoveListenner(this);
	}
	
	/**
	 * 点击取消
	 * @param v
	 */
	public void onClickCancel(View v) {
		finish();
	}
	
	/**
	 * 点击Complete
	 * @param v
	 */
	public void onClickComplete(View v) {
		// 此处该有菊花
		// 调用facebook绑定接口
		String email_compiler = "[a-z0-9A-Z\\.\\-_]{3,36}@[a-z0-9A-Z\\.\\-]{3,36}";
		Pattern _pattern = Pattern.compile(email_compiler);
		Matcher _matcher = _pattern.matcher(editTextEmail.getText());
		if (!_matcher.matches()){
			editTextEmail.setError(Color.RED, true);
			return;
		}
		showProgressDialog("Requesting...");
		LoginManager.getInstance().LoginWithFacebook(editTextEmail.getText().toString().toLowerCase(Locale.ENGLISH), "", RequestErrorCode.MBCE64001, year, month, day);
	}
	
	/**
	 * 点击出生日期
	 * @param v
	 */
	public void onClickBirthday(View v) {
		int year = 1970;
		int month = 1;
		int day = 1;
		
		String birthday = LoginManager.getInstance().GetFacebookUserInfo().getBirthday();
		if( birthday != null && birthday.length() > 0 ) {
			String[] ss = birthday.split("/");
			
			if( ss.length > 2 ) {
				day = Integer.parseInt(ss[0]);
				month = Integer.parseInt(ss[1]);
				year = Integer.parseInt(ss[2]);
			}
		}

        MaterialDatePickerDialog datePicker = new MaterialDatePickerDialog(this, this, year, month, day);
        datePicker.show();
	}

	@Override
	public void OnLogin(boolean isSuccess, String errno, String errmsg,
			LoginItem item, LoginErrorItem errItem) {
		// TODO Auto-generated method stub
		// facebook登录状态改变
		Message msg = Message.obtain();
		RequestBaseResponse obj = new RequestBaseResponse(isSuccess, errno, errmsg, null);
		if( isSuccess ) {
			// 登录成功
			msg.what = RequestFlag.REQUEST_SUCCESS.ordinal();
			obj.body = item;
		} else {
			// 登录失败
			msg.what = RequestFlag.REQUEST_FAIL.ordinal();
			obj.body = errItem;
		}
		msg.obj = obj;
		sendUiMessage(msg);
	}

	@Override
	public void OnLogout(boolean bActive) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void InitView() {
		setContentView(R.layout.activity_register_by_facebook);
		
		appbar = (MaterialAppBar) findViewById(R.id.appbar);
		appbar.setTouchFeedback(MaterialAppBar.TOUCH_FEEDBACK_HOLO_LIGHT);
		appbar.addButtonToLeft(android.R.id.button1, "", R.drawable.ic_arrow_back_grey600_24dp);
		appbar.setTitle(getString(R.string.facebook_connect), getResources().getColor(R.color.text_color_dark));
		appbar.setOnButtonClickListener(this);
		
		editTextEmail = (MaterialTextField) findViewById(R.id.editTextEmail);
		
		editTextViewBirthday = (MaterialTextField) findViewById(R.id.editTextViewBirthday);
		editTextViewBirthday.setHint(getString(R.string.your_birthday));
		editTextViewBirthday.setNoPredition();
		editTextViewBirthday.setText("01/01/1970");
		
		editTextEmail.requestFocus();
		editTextEmail.setEmail();
		lastFocusedView = editTextEmail.getEditor();
		editTextEmail.setOnFocusChangedCallback(this);
		editTextViewBirthday.setOnFocusChangedCallback(this);
	}
	
	@Override
	protected void handleUiMessage(Message msg) {
		// TODO Auto-generated method stub
		super.handleUiMessage(msg);
		// 收起菊花
		hideProgressDialog();
//		MessageCallbackItem obj = (MessageCallbackItem) msg.obj;
		switch ( RequestFlag.values()[msg.what] ) {
		case REQUEST_SUCCESS:{
			// 绑定成功
			finish();
		}break;
		case REQUEST_FAIL:{
			// 绑定失败
//			switch (obj.errno) {
//			case RequestErrorCode.MBCE64002:{
//				// facebook有邮箱，并且已经被qpidnetwork注册，显示输入密码，重新绑定
//				Intent intent = new Intent(mContext, RegisterFacebookPasswordActivity.class);
//				intent.putExtra(
//						RegisterFacebookPasswordActivity.REGISTER_FACEBOOK_LOGINERRORITEM_KEY,
//						obj.loginErrorItem
//						);
//				startActivity(intent);
//			}break;
//			default:{
//				// 绑定失败
//				Toast.makeText(mContext, obj.errmsg, Toast.LENGTH_LONG).show();
//				break;
//			}
//			}
		}break;
		default:
			break;
		}
	}

	@Override
	public void onDateSelected(int year, int month, int day) {
		// TODO Auto-generated method stub
		this.year = String.valueOf(year);
	    this.month = String.valueOf(month + 1);
	    this.day = String.valueOf(day);
	    
	    editTextViewBirthday.setText(day + "/" + month + "/" + year);
	}

	@Override
	public void onFocuseChanged(View v, boolean hasFocus) {
		// TODO Auto-generated method stub
		if(v.equals(editTextViewBirthday.getEditor())){
			if (hasFocus){
				onClickBirthday(v);
				lastFocusedView.requestFocus();
			} else {
				if (hasFocus) lastFocusedView = v;
			}
		}
	}
	
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		if (v.getId() == android.R.id.button1) {
			onClickCancel(v);
		}
	}
}
