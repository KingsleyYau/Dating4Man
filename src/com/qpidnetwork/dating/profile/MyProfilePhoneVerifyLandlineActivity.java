package com.qpidnetwork.dating.profile;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.telephony.TelephonyManager;
import android.text.TextUtils.TruncateAt;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;

import com.qpidnetwork.dating.BaseActivity;
import com.qpidnetwork.dating.R;
import com.qpidnetwork.framework.util.Log;
import com.qpidnetwork.request.OnRequestCallback;
import com.qpidnetwork.request.RequestEnum.Country;
import com.qpidnetwork.request.RequestJni;
import com.qpidnetwork.request.RequestJniAuthorization;
import com.qpidnetwork.request.item.ProfileItem;
import com.qpidnetwork.view.ButtonRaised;
import com.qpidnetwork.view.MaterialAppBar;
import com.qpidnetwork.view.MaterialTextField;

/**
 * MyProfile模块
 * @author Max.Chiu
 */
public class MyProfilePhoneVerifyLandlineActivity extends BaseActivity {
	/**
	 * 编辑国家
	 */
	private static final int RESULT_COUNTRY = 0;
	
	private enum RequestFlag {
		REQUEST_PROFILE_SUCCESS,
		REQUEST_GET_SMS_SUCCESS,
		REQUEST_FAIL,
	}

	/**
	 * 界面消息
	 */
	private class MessageCallbackItem {
		/**
		 * 
		 * @param errno				接口错误码
		 * @param errmsg			错误提示
		 */
		public MessageCallbackItem(
				String errno, 
				String errmsg
				) {
			this.errno = errno;
			this.errmsg = errmsg;
		}
		public String errno;
		public String errmsg;
	}
	
	private MaterialTextField editTextUnitedStates;
	private MaterialTextField editTextAreaCode;
	private MaterialTextField editTextPhoneNumber;
	private ButtonRaised btnContinue;

	private String[] countries;
	private ProfileItem mProfileItem;
	
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
		GetFixedPhone();
	}

	@Override
	public void InitView() {
		setContentView(R.layout.activity_my_profile_phone_verify_landline);
		countries = mContext.getResources().getStringArray(R.array.country);
		
		MaterialAppBar appbar = (MaterialAppBar)findViewById(R.id.appbar);
		appbar.setTouchFeedback(MaterialAppBar.TOUCH_FEEDBACK_HOLO_LIGHT);
		appbar.addButtonToLeft(android.R.id.button1, "back", R.drawable.ic_close_grey600_24dp);
		appbar.setTitle("Verify landline number", getResources().getColor(R.color.text_color_dark));
		appbar.setOnButtonClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				finish();
			}
			
		});
		
		btnContinue = (ButtonRaised)findViewById(R.id.btn_continue);
		editTextAreaCode = (MaterialTextField) findViewById(R.id.editTextAreaCode);
		editTextUnitedStates = (MaterialTextField) findViewById(R.id.editTextUnitedStates);
		editTextPhoneNumber = (MaterialTextField) findViewById(R.id.editTextPhoneNumber);
		
		editTextAreaCode.getEditor().setTextSize(20);
		editTextUnitedStates.getEditor().setTextSize(20);
		editTextPhoneNumber.getEditor().setTextSize(20);
		editTextAreaCode.getEditor().setSingleLine();
		editTextUnitedStates.getEditor().setSingleLine();
		editTextPhoneNumber.getEditor().setSingleLine();
		editTextAreaCode.setNumber();
		editTextPhoneNumber.setNumber();
		
		editTextAreaCode.requestFocus();
		editTextAreaCode.setHint("Area code");
		editTextPhoneNumber.setHint("Your phone number");
		editTextUnitedStates.setText("United States (+1)");
		editTextUnitedStates.getEditor().setEllipsize(TruncateAt.END);
		editTextUnitedStates.setOnFocusChangedCallback(new MaterialTextField.OnFocuseChangedCallback() {
			
			@Override
			public void onFocuseChanged(View v, boolean hasFocus) {
				// TODO Auto-generated method stub
				if (hasFocus){
					onClickUnitedStates(v);
					if (editTextAreaCode.getEditor().getText().length() == 0){
						editTextAreaCode.requestFocus();
					}else{
						editTextPhoneNumber.requestFocus();
					}
				}
			}
		});
		
		editTextUnitedStates.getEditor().setText(countries[222]);
		editTextUnitedStates.setTag(222);
		
	}
	
	/**
	 * 固定电话获取认证短信
	 */
	public void GetFixedPhone() {
		
		if (editTextPhoneNumber.getText().toString().length() < 5){
			editTextPhoneNumber.setError(Color.RED, true);
			return;
		}
		
		showProgressDialog("Loading...");
		TelephonyManager tm = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);

		Log.v("log", editTextPhoneNumber.getText() + "  " +  Country.values()[(int)editTextUnitedStates.getTag()] + "   " +editTextAreaCode.getText() + " " );
		RequestJniAuthorization.GetFixedPhone(
				editTextPhoneNumber.getText().toString(),  //telephone number
				Country.values()[(int)editTextUnitedStates.getTag()], //country code
				editTextAreaCode.getText().toString(),  //area code
				RequestJni.GetDeviceId(tm), 
				new OnRequestCallback() {
					
					@Override
					public void OnRequest(boolean isSuccess, String errno, String errmsg) {
						// TODO Auto-generated method stub
						Message msg = Message.obtain();
						MessageCallbackItem obj = new MessageCallbackItem(errno, errmsg);
						if( isSuccess ) {
							// 获取个人信息成功
							msg.what = RequestFlag.REQUEST_GET_SMS_SUCCESS.ordinal();
						} else {
							// 获取个人信息失败
							msg.what = RequestFlag.REQUEST_FAIL.ordinal();
						}
						msg.obj = obj;
						mHandler.sendMessage(msg);
					}
				});
	}
	
	@Override
	public void InitHandler() {
		// TODO Auto-generated method stub
		mHandler = new Handler() {
			@Override
			public void handleMessage(android.os.Message msg) {
				MessageCallbackItem obj = (MessageCallbackItem) msg.obj;
				// 收起菊花
				hideProgressDialog();
				switch ( RequestFlag.values()[msg.what] ) {
				case REQUEST_PROFILE_SUCCESS:{
					// 获取个人信息成功
					ReloadData();
				}break;
				case REQUEST_GET_SMS_SUCCESS:{
					// 固定电话获取认证短信成功
					// 跳转界面
					//Intent intent = new Intent(mContext, MyProfilePhoneVerifyMobileCodeActivity.class);
					//startActivity(intent);
					String phoneNumber = editTextPhoneNumber.getText().toString();
					MyProfilePhoneVerifyMobileCodeActivity.LaunchActivity(MyProfilePhoneVerifyMobileCodeActivity.LaunchType.LAND, phoneNumber, mContext);
				}break;
				case REQUEST_FAIL:{
					// 请求失败
					Toast.makeText(mContext, obj.errmsg, Toast.LENGTH_LONG).show();	
				}break;
				default:
					break;
				}
			};
		};
	}
	
	public void ReloadData() {
		if( mProfileItem != null ) {
			// 手机认证
			if( mProfileItem.mobile != null && mProfileItem.mobile.length() > 0 ) {
				// 已经认证
				editTextUnitedStates.getEditor().setText(countries[mProfileItem.landline_cc.ordinal()]);
				editTextUnitedStates.setTag(mProfileItem.landline_cc.ordinal());
				editTextPhoneNumber.setText(mProfileItem.landline);
			} else {
				//editTextUnitedStates.getEditor().setText("");
				//editTextPhoneNumber.setText("");
			}
		}
	}
}
