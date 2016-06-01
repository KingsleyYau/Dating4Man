package com.qpidnetwork.dating.profile;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;

import com.qpidnetwork.dating.R;
import com.qpidnetwork.dating.bean.RequestBaseResponse;
import com.qpidnetwork.framework.base.BaseFragmentActivity;
import com.qpidnetwork.request.OnRequestCallback;
import com.qpidnetwork.request.RequestOperator;
import com.qpidnetwork.view.MaterialAppBar;
import com.qpidnetwork.view.MaterialDialogAlert;
import com.qpidnetwork.view.MaterialTextField;

/**
 * MyProfile模块
 * @author Max.Chiu
 */
public class MyProfileChangePasswordActivity extends BaseFragmentActivity implements OnRequestCallback, OnClickListener {
	
	private enum RequestFlag {
		REQUEST_SUCCESS,
		REQUEST_FAIL,
	}
	
	//private TextView textViewTips;
	private MaterialTextField editTextCurrentPassword; 
	private MaterialTextField editTextNewPassword; 
	private MaterialTextField editTextConfirmPassword; 
	private MaterialAppBar appbar;
	
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
	 * 点击改变密码
	 * @param v
	 */
	public void onClickChange(View v) {
		if( CheckPassword() ) {
			// 此处应有菊花
			showProgressDialog("Loading...");
			RequestOperator.getInstance().ChangePassword(
					editTextCurrentPassword.getText().toString(),
					editTextNewPassword.getText().toString(),
					this
					);
		} 
	}
	
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		if (v.getId() == R.id.common_button_back){
			onClickCancel(v);
		}
	}
	
	@Override
	public void InitView() {
		setContentView(R.layout.activity_my_profile_change_password);
		
		appbar = (MaterialAppBar)findViewById(R.id.appbar);
		appbar.setTouchFeedback(MaterialAppBar.TOUCH_FEEDBACK_HOLO_LIGHT);
		appbar.addButtonToLeft(R.id.common_button_back, "", R.drawable.ic_close_grey600_24dp);
		appbar.setTitle(getString(R.string.Change_Password), getResources().getColor(R.color.text_color_dark));
		appbar.setOnButtonClickListener(this);
		
		//textViewTips = (TextView) findViewById(R.id.textViewTips);
		//textViewTips.setVisibility(View.GONE);
		
		editTextCurrentPassword = (MaterialTextField) findViewById(R.id.editTextCurrentPassword);
		editTextCurrentPassword.setPassword();
		editTextCurrentPassword.setHint("Your current password");
		
		editTextNewPassword = (MaterialTextField) findViewById(R.id.editTextNewPassword);
		editTextNewPassword.setPassword();
		editTextNewPassword.setHint("New password");
		
		editTextConfirmPassword = (MaterialTextField) findViewById(R.id.editTextConfirmPassword);
		editTextConfirmPassword.setPassword();
		editTextConfirmPassword.setHint("Confirm Password");
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
			// 改变密码成功
			finish();
		}break;
		case REQUEST_FAIL:{
			// 请求失败
			Toast.makeText(mContext, obj.errmsg, Toast.LENGTH_LONG).show();	
//			editTextCurrentPassword.setError(Color.RED, true);
		}break;
		default:
			break;
		}
	}
	
	/**
	 * 检查新密码是否正确
	 * @return
	 */
	public boolean CheckPassword() {
		if( editTextCurrentPassword.getText().toString().length() == 0 ) {
			// 沒輸入當前密碼
			editTextCurrentPassword.setError(Color.RED, true);
			return false;
		} else if( editTextNewPassword.getText().toString().length() == 0 ) {
			editTextNewPassword.setError(Color.RED, true);
			return false;
		} else if( editTextConfirmPassword.getText().toString().length() == 0 ) {
			editTextConfirmPassword.setError(Color.RED, true);
			return false;
		} else if( editTextNewPassword.getText().toString().length() < 6 ) {
			// 新密小于6位
			MaterialDialogAlert alert = new MaterialDialogAlert(mContext);
			alert.setMessage("Password must more than 6 characters.");
			alert.addButton(alert.createButton(getString(R.string.common_btn_ok), null));
			alert.show();
			
			editTextNewPassword.setError(Color.RED, true);
			return false;
		} else if( editTextNewPassword.getText().toString().length() > 12 ) {
			// 新密大于12位
			MaterialDialogAlert alert = new MaterialDialogAlert(mContext);
			alert.setMessage("Password can't exceed 12 characters.");
			alert.addButton(alert.createButton(getString(R.string.common_btn_ok), null));
			alert.show();
			
			editTextNewPassword.setError(Color.RED, true);
			return false;
		} else if( editTextNewPassword.getText().toString().compareTo(editTextConfirmPassword.getText().toString()) != 0 ) {
			// 检查2个新密码是否一样
			MaterialDialogAlert alert = new MaterialDialogAlert(mContext);
			alert.setMessage("New passwords do not match.");
			alert.addButton(alert.createButton(getString(R.string.common_btn_ok), null));
			alert.show();

			editTextConfirmPassword.setError(Color.RED, true);
			return false;
		}
		return true;
	}

	@Override
	public void OnRequest(boolean isSuccess, String errno, String errmsg) {
		// TODO Auto-generated method stub
		Message msg = Message.obtain();
		RequestBaseResponse obj = new RequestBaseResponse(isSuccess, errno, errmsg, null);
		if( isSuccess ) {
			// 改变密码成功
			msg.what = RequestFlag.REQUEST_SUCCESS.ordinal();
		} else {
			// 失败
			msg.what = RequestFlag.REQUEST_FAIL.ordinal();
		}
		msg.obj = obj;
		sendUiMessage(msg);
	}
}
