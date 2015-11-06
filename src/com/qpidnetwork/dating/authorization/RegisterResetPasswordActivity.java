package com.qpidnetwork.dating.authorization;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Message;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.qpidnetwork.framework.base.BaseFragmentActivity;
import com.qpidnetwork.dating.R;
import com.qpidnetwork.dating.bean.RequestBaseResponse;
import com.qpidnetwork.framework.util.Log;
import com.qpidnetwork.request.OnFindPasswordCallback;
import com.qpidnetwork.request.OnRequestOriginalCallback;
import com.qpidnetwork.request.RequestErrorCode;
import com.qpidnetwork.request.RequestJniAuthorization;
import com.qpidnetwork.view.MaterialAppBar;
import com.qpidnetwork.view.MaterialTextField;

public class RegisterResetPasswordActivity extends BaseFragmentActivity
										   implements OnRequestOriginalCallback,
										   			  OnFindPasswordCallback
{
	
	private enum RequestFlag {
		REQUEST_SUCCESS,
		REQUEST_FAIL,
		REQUEST_CHECKCODE_SUCCESS,
		REQUEST_CHECKCODE_FAIL,
	}
	
	
	private MaterialTextField editTextEmail;
	
	private RelativeLayout layoutCheckCode;
	private MaterialTextField editTextCheckcode;
	private MaterialAppBar appbar;
	
	private ImageView imageViewCheckCode;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// 调用刷新注册码接口
		GetCheckCode();
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
	 * 点击验证码码
	 * @param view
	 */
	public void onClickCheckCode(View view) {
		// 调用刷新注册码接口
		GetCheckCode();
	}
	
	/**
	 * 验证码码
	 */
	public void GetCheckCode() {
		RequestJniAuthorization.GetCheckCode(this);
	}
	
	/**
	 * 点击改变密码
	 * @param v
	 */
	public void onClickReset(View v) {
		// 此处该弹菊花
		
		if (editTextEmail.getText().length() < 10){
			editTextEmail.setError(Color.RED, true);
			return;
		}
		
		if (editTextCheckcode.getText().length() < 4){
			editTextCheckcode.setError(Color.RED, true);
			return;
		}
		
		
		showProgressDialog("Loading...");
		RequestJniAuthorization.FindPassword(
				editTextEmail.getText().toString(), 
				editTextCheckcode.getText().toString(),
				this);
	}
	
	@Override
	public void InitView() {
		setContentView(R.layout.activity_register_forget_password);
		
		appbar = (MaterialAppBar)findViewById(R.id.appbar);
		appbar.setTouchFeedback(MaterialAppBar.TOUCH_FEEDBACK_HOLO_LIGHT);
		appbar.setTitle(getString(R.string.Reset_Password), getResources().getColor(R.color.text_color_dark));
		appbar.addButtonToLeft(R.id.common_button_back, "", R.drawable.ic_close_grey600_24dp);
		appbar.setAppbarBackgroundColor(getResources().getColor(R.color.white));
		appbar.setOnButtonClickListener(this);
		editTextEmail = (MaterialTextField) findViewById(R.id.editTextEmail);
		editTextEmail.setHint(getResources().getString(R.string.Enter_your_eamil));
		editTextEmail.setEmail();
		layoutCheckCode = (RelativeLayout) findViewById(R.id.layoutCheckCode);
		layoutCheckCode.setVisibility(View.GONE);
		
		editTextCheckcode = (MaterialTextField) findViewById(R.id.editTextCheckCode);
		editTextCheckcode.setHint(getResources().getString(R.string.Enter_verification_code));
		editTextCheckcode.setNoPredition();
		
//		textViewSep3 = (TextView) findViewById(R.id.textViewSep3);
//		textViewSep3.setVisibility(View.GONE);
		
		imageViewCheckCode = (ImageView) findViewById(R.id.imageViewCheckCode);
		imageViewCheckCode.setImageDrawable(null);
		
		
	}
	
	@Override
	protected void handleUiMessage(Message msg) {
		// TODO Auto-generated method stub
		super.handleUiMessage(msg);
		// 收起菊花
		hideProgressDialog();
		RequestBaseResponse obj = (RequestBaseResponse) msg.obj;
		switch ( RequestFlag.values()[msg.what] ) {
		case REQUEST_SUCCESS:{
			// 收起菊花
			Intent intent = new Intent(mContext, RegisterResetPasswordSuccessfulAcitiviy.class);
			intent.putExtra(RegisterResetPasswordSuccessfulAcitiviy.INPUT_EMAIL_KEY, editTextEmail.getText().toString());
			startActivity(intent);
			
			Log.v("email address", editTextEmail.getText().toString());
			
			// 改变密码成功
			finish();
		}break;
		case REQUEST_FAIL:{
			// 收起菊花
			Toast.makeText(mContext, obj.errmsg, Toast.LENGTH_LONG).show();
			
			// 改变密码失败
			switch (obj.errno) {
			case RequestErrorCode.MBCE1012: {
				// 验证码无效
			}
			case RequestErrorCode.MBCE1013:{
				// 验证码无效
				layoutCheckCode.setVisibility(View.VISIBLE);
			}break;
			default:
				break;
			}
		}break;
		case REQUEST_CHECKCODE_SUCCESS:{
			// 获取验证码成功
			if( obj != null && obj.body != null ) {
				Bitmap bitmap = (Bitmap)obj.body;
				imageViewCheckCode.setImageBitmap(bitmap);
				layoutCheckCode.setVisibility(View.VISIBLE);
//				textViewSep3.setVisibility(View.VISIBLE);
			} else {
				layoutCheckCode.setVisibility(View.GONE);
//				textViewSep3.setVisibility(View.GONE);
			}
		}break;
		default:
			break;
		}
	}

	@Override
	public void OnRequestData(boolean isSuccess, String errno, String errmsg,
			byte[] data) 
	{
		// TODO Auto-generated method stub
		if( isSuccess ) {
			Message msg = Message.obtain();
			RequestBaseResponse obj = new RequestBaseResponse(isSuccess, errno, errmsg, null);
			if( isSuccess ) {
				// 获取验证码成功
				msg.what = RequestFlag.REQUEST_CHECKCODE_SUCCESS.ordinal();
				if( data.length != 0 ) {
					obj.body = BitmapFactory.decodeByteArray(data, 0, data.length);
				}
			} else {
				// 获取验证码失败
				msg.what = RequestFlag.REQUEST_CHECKCODE_FAIL.ordinal();
			}
			msg.obj = obj;
			sendUiMessage(msg);
		}
	}

	@Override
	public void OnFindPassword(boolean isSuccess, String errno, String errmsg,
			String tips) 
	{
		// TODO Auto-generated method stub
		Message msg = Message.obtain();
		RequestBaseResponse obj = new RequestBaseResponse(isSuccess, errno, errmsg, null);
		if( isSuccess ) {
			// 成功
			msg.what = RequestFlag.REQUEST_SUCCESS.ordinal();
		} else {
			// 失败
			msg.what = RequestFlag.REQUEST_FAIL.ordinal();;
		}
		msg.obj = obj;
		sendUiMessage(msg);
	}
	
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		if (v.getId() == R.id.common_button_back){
			onClickCancel(v);
		}
	}
}
