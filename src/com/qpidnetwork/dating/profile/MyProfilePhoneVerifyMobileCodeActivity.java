package com.qpidnetwork.dating.profile;

import java.util.regex.Pattern;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Message;
import android.view.Gravity;
import android.view.View;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import com.qpidnetwork.dating.R;
import com.qpidnetwork.framework.base.BaseFragmentActivity;
import com.qpidnetwork.request.OnRequestCallback;
import com.qpidnetwork.request.RequestJniAuthorization.Verify;
import com.qpidnetwork.request.RequestOperator;
import com.qpidnetwork.view.MaterialAppBar;
import com.qpidnetwork.view.MaterialTextField;

/**
 * MyProfile模块
 * @author Max.Chiu
 */
public class MyProfilePhoneVerifyMobileCodeActivity extends BaseFragmentActivity implements OnRequestCallback{
	
	public static String INPUT_LAUNCH_TYPE = "INPUT_LAUNCH_TYPE";
	public static String INPUT_PHONE_NUMBER = "INPUT_PHONE_NUMBER";
	
	private enum RequestFlag {
		REQUEST_VERIFY_PHONE_SUCCESS,
		REQUEST_FAIL,
	}

	private TextView textViewVerifyTip;
	private ImageView imageViewVerifyTypeIcon;
	private MaterialTextField editTextVerifyCode;
	private TextView errorMsg;
	
	
	
	private LaunchType launchType = LaunchType.CELL;
	private String phoneNumber = "";
	
	public static enum LaunchType{
		LAND,  //landline
		CELL   //cellphone
	}
	
	
	public static void LaunchActivity(LaunchType launchType, String phone_number, Context context){
		Intent intent = new Intent(context, MyProfilePhoneVerifyMobileCodeActivity.class);
		intent.putExtra(INPUT_LAUNCH_TYPE, launchType.name());
		intent.putExtra(INPUT_PHONE_NUMBER, phone_number);
		context.startActivity(intent);
	}
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		
	}
	
	/**
	 * 点击取消
	 * @param v
	 */
	public void onClickCancel(View v) {
		finish();
	}
	
	/**
	 * 点击验证
	 * @param view
	 */
	public void onClickVerify(View view) {
		
		String t = editTextVerifyCode.getText().toString();
		
		Pattern pattern = Pattern.compile("[0-9]{4,8}");
		if (!pattern.matcher(t).matches()){
			editTextVerifyCode.setError(Color.RED, true);
			return;
		}
		
		//showErrorMssage("test error no");
		VerifySms();
	}

	
	private void showErrorMssage(CharSequence msg){
		errorMsg.setText(msg);
		TranslateAnimation animation = new TranslateAnimation(TranslateAnimation.RELATIVE_TO_SELF, 0,
				TranslateAnimation.RELATIVE_TO_SELF, 0,
				TranslateAnimation.RELATIVE_TO_SELF, -2, 
				TranslateAnimation.RELATIVE_TO_SELF, 0);
		animation.setDuration(500);
		errorMsg.startAnimation(animation);
		errorMsg.setVisibility(View.VISIBLE);
		
	}
	
	private void clearErrorMessage(){
		errorMsg.setVisibility(View.GONE);
	}
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		finish();
	}
	
	@Override
	public void InitView() {
		
		Bundle bundle = getIntent().getExtras();
		if (!bundle.containsKey(INPUT_LAUNCH_TYPE) || !bundle.containsKey(INPUT_PHONE_NUMBER)) {
			finish();
			return;
		}
		
		launchType = LaunchType.valueOf(bundle.getString(INPUT_LAUNCH_TYPE));
		phoneNumber = bundle.getString(INPUT_PHONE_NUMBER);
		
		setContentView(R.layout.activity_my_profile_phone_verify_mobile_code);
		
		MaterialAppBar appbar = (MaterialAppBar)findViewById(R.id.appbar);
		appbar.setTouchFeedback(MaterialAppBar.TOUCH_FEEDBACK_HOLO_LIGHT);
		appbar.addButtonToLeft(android.R.id.button1, "back", R.drawable.ic_close_grey600_24dp);
		
		appbar.setOnButtonClickListener(this);
		
		
		textViewVerifyTip = (TextView)findViewById(R.id.textViewVerifyTip);
		imageViewVerifyTypeIcon = (ImageView)findViewById(R.id.imageViewVerifyTypeIcon);
		errorMsg = (TextView)findViewById(R.id.errorMsg);
		editTextVerifyCode = (MaterialTextField) findViewById(R.id.editTextVerifyCode);
		
		if (launchType == LaunchType.CELL){
			appbar.setTitle("Verify mobile number", getResources().getColor(R.color.text_color_dark));
			textViewVerifyTip.setText(getString(R.string.verify_mobile_code_tips, phoneNumber));
			imageViewVerifyTypeIcon.setImageResource(R.drawable.ic_message_green_48dp);
		}else{
			textViewVerifyTip.setText(getString(R.string.verify_landline_code_tips, phoneNumber));
			imageViewVerifyTypeIcon.setImageResource(R.drawable.ic_call_green_48dp);
			appbar.setTitle("Verify landline number", getResources().getColor(R.color.text_color_dark));
		}
		
		editTextVerifyCode.setHint("Verification code");
		editTextVerifyCode.getEditor().setGravity(Gravity.CENTER);
		editTextVerifyCode.getEditor().setTextSize(22);
		editTextVerifyCode.getEditor().setSingleLine();
		editTextVerifyCode.setNumber();
	}
	
	/**
	 * 手机短信认证
	 */
	public void VerifySms() {
		// 此处应有菊花
		clearErrorMessage();
		showProgressDialog("Loading...");
		RequestOperator.getInstance().VerifySms(
				editTextVerifyCode.getText().toString(),
				Verify.Default,
				this);
	}
	@Override
	public void OnRequest(boolean isSuccess, String errno, String errmsg) {
		// TODO Auto-generated method stub
		Message msg = Message.obtain();
		if( isSuccess ) {
			// 手机短信认证成功
			msg.what = RequestFlag.REQUEST_VERIFY_PHONE_SUCCESS.ordinal();
		} else {
			// 失败
			msg.what = RequestFlag.REQUEST_FAIL.ordinal();
			msg.obj = errmsg;
		}
		sendUiMessage(msg);
	}
	@Override
	protected void handleUiMessage(Message msg) {
		// TODO Auto-generated method stub
		super.handleUiMessage(msg);
		// 收起菊花
		hideProgressDialog();
		switch ( RequestFlag.values()[msg.what] ) {
		case REQUEST_VERIFY_PHONE_SUCCESS:{
			// 手机短信认证成功
			// 跳转到手机绑定界面
			Intent intent = new Intent(mContext, MyProfileDetailActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
		}break;
		case REQUEST_FAIL:{
			// 请求失败
			editTextVerifyCode.setError(Color.RED, true);
			showErrorMssage(msg.obj.toString());
		}break;
		default:
			break;
		}
	}
	
}
