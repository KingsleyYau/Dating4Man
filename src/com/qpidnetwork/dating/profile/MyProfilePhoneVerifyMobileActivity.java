package com.qpidnetwork.dating.profile;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Message;
import android.telephony.TelephonyManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;

import com.qpidnetwork.dating.R;
import com.qpidnetwork.dating.bean.RequestBaseResponse;
import com.qpidnetwork.framework.base.BaseFragmentActivity;
import com.qpidnetwork.request.OnRequestCallback;
import com.qpidnetwork.request.RequestEnum.Country;
import com.qpidnetwork.request.RequestJni;
import com.qpidnetwork.request.RequestJniAuthorization;
import com.qpidnetwork.request.item.ProfileItem;
import com.qpidnetwork.view.MaterialAppBar;
import com.qpidnetwork.view.MaterialTextField;

/**
 * MyProfile模块
 * @author Max.Chiu
 */
public class MyProfilePhoneVerifyMobileActivity extends BaseFragmentActivity implements OnClickListener, MaterialTextField.OnFocuseChangedCallback, OnRequestCallback {
	/**
	 * 编辑国家
	 */
	private static final int RESULT_COUNTRY = 0;
	
	private enum RequestFlag {
		REQUEST_GET_SMS_SUCCESS,
		REQUEST_FAIL,
	}



	private MaterialTextField editTextUnitedStates;
	private MaterialTextField editTextPhoneNumber;

	private ProfileItem mProfileItem;
	private String[] countries;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// 创建界面时候，获取缓存数据
		mProfileItem = MyProfilePerfence.GetProfileItem(mContext);
		
		// 刷新界面
		ReloadData();
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
	 * 点击选择国家码
	 * @param view
	 */
	public void onClickUnitedStates(View view) {
		startActivityForResult(new Intent(this, MyProfileSelectCountryActivity.class), RESULT_COUNTRY);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	    super.onActivityResult(requestCode, resultCode, data);
	 
	    switch(requestCode) {
	    case RESULT_COUNTRY:{
	    	// 选择国家码返回
	    	if( resultCode == RESULT_OK ) {
	    		int postion = data.getExtras().getInt(MyProfileSelectCountryActivity.RESULT_COUNTRY_INDEX);
	    		editTextUnitedStates.getEditor().setText(countries[postion]);
	    		editTextUnitedStates.setTag(postion);
	    	}
	    }break;
	    default:break;
	    }
	}
	
	/**
	 * 点击继续
	 * @param view
	 */
	public void onClickContinue(View view) {
		if (editTextPhoneNumber.getEditor().getText().length() < 5){
			editTextPhoneNumber.setError(Color.RED, true);
			return;
		}
		GetSms();
	}
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		finish();
	}
	@Override
	public void InitView() {
		setContentView(R.layout.activity_my_profile_phone_verify_mobile);
		countries = mContext.getResources().getStringArray(R.array.country);
		
		MaterialAppBar appbar = (MaterialAppBar)findViewById(R.id.appbar);
		appbar.setTouchFeedback(MaterialAppBar.TOUCH_FEEDBACK_HOLO_LIGHT);
		appbar.addButtonToLeft(android.R.id.button1, "back", R.drawable.ic_close_grey600_24dp);
		appbar.setTitle("Verify mobile number", getResources().getColor(R.color.text_color_dark));
		appbar.setOnButtonClickListener(this);
		
		
		editTextUnitedStates = (MaterialTextField) findViewById(R.id.editTextUnitedStates);
		editTextPhoneNumber = (MaterialTextField) findViewById(R.id.editTextPhoneNumber);
		
		editTextPhoneNumber.requestFocus();
		editTextPhoneNumber.getEditor().setTextSize(20);
		editTextUnitedStates.getEditor().setTextSize(20);
		
		editTextPhoneNumber.getEditor().setSingleLine();
		editTextPhoneNumber.setNumber();
		editTextPhoneNumber.setHint("Your mobile number");
		
		editTextUnitedStates.getEditor().setSingleLine();
		
		editTextUnitedStates.setText("United States (+1)");
		editTextUnitedStates.setOnFocusChangedCallback(this);
		
		editTextUnitedStates.getEditor().setText(countries[222]);
		editTextUnitedStates.setTag(222);
	}
	@Override
	public void onFocuseChanged(View v, boolean hasFocus) {
		// TODO Auto-generated method stub
		if (hasFocus) {
			onClickUnitedStates(v);
			editTextPhoneNumber.requestFocus();
		}
		
	}
	/**
	 * 手机获取认证短信
	 */
	public void GetSms() {
		showProgressDialog("Loading...");
		TelephonyManager tm = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
		RequestJniAuthorization.GetSms(
				editTextPhoneNumber.getText().toString(), 
				Country.values()[(int)editTextUnitedStates.getTag()], 
				RequestJni.GetDeviceId(tm), 
				this);
	}
	@Override
	public void OnRequest(boolean isSuccess, String errno, String errmsg) {
		// TODO Auto-generated method stub
		Message msg = Message.obtain();
		RequestBaseResponse response = new RequestBaseResponse(isSuccess, errno, errmsg, null);
		if( isSuccess ) {
			// 获取个人信息成功
			msg.what = RequestFlag.REQUEST_GET_SMS_SUCCESS.ordinal();
		} else {
			// 获取个人信息失败
			msg.what = RequestFlag.REQUEST_FAIL.ordinal();
		}
		msg.obj = response;
		sendUiMessage(msg);
	}
	@Override
	protected void handleUiMessage(Message msg) {
		// TODO Auto-generated method stub
		super.handleUiMessage(msg);
		RequestBaseResponse obj = (RequestBaseResponse) msg.obj;
		// 收起菊花
		hideProgressDialog();
		switch ( RequestFlag.values()[msg.what] ) {
		case REQUEST_GET_SMS_SUCCESS:{
			// 手机获取认证短信成功
			// 跳转界面
			//Intent intent = new Intent(mContext, MyProfilePhoneVerifyMobileCodeActivity.class);
			//startActivity(intent);
			String phoneNumber = editTextPhoneNumber.getText().toString();
			MyProfilePhoneVerifyMobileCodeActivity.LaunchActivity(MyProfilePhoneVerifyMobileCodeActivity.LaunchType.CELL, phoneNumber, mContext);
		}break;
		case REQUEST_FAIL:{
			// 请求失败
			Toast.makeText(mContext, obj.errmsg, Toast.LENGTH_LONG).show();	
		}break;
		default:
			break;
		}
	}
	
	public void ReloadData() {
		if( mProfileItem != null ) {
			// 手机认证
			if( mProfileItem.mobile != null && mProfileItem.mobile.length() > 0 ) {
				// 已经认证
				editTextPhoneNumber.setText(mProfileItem.mobile);
				editTextUnitedStates.getEditor().setText(countries[mProfileItem.mobile_cc.ordinal()]);
				editTextUnitedStates.setTag(mProfileItem.mobile_cc.ordinal());
			} else {
				//editTextUnitedStates.setText("");
				//editTextPhoneNumber.setText("");
			}
		}
	}
}
