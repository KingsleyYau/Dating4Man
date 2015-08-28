package com.qpidnetwork.dating.authorization;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.qpidnetwork.dating.BaseActivity;
import com.qpidnetwork.dating.R;
import com.qpidnetwork.view.MaterialAppBar;

public class RegisterResetPasswordSuccessfulAcitiviy extends BaseActivity {
	

	
	public static String INPUT_EMAIL_KEY;
	private MaterialAppBar appbar;
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		if (this.getCallingPackage() != null){
			finish();
			return;
		}
		
		
		Bundle extras = this.getIntent().getExtras();
		if (extras == null){
			finish();
			return;
		}
		
		if (!extras.containsKey(INPUT_EMAIL_KEY)){
			finish();
			return;
		}
		
		
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
	

	

	

	
	@Override
	public void InitView() {
		setContentView(R.layout.activity_register_forget_password_successful);
		
		appbar = (MaterialAppBar)findViewById(R.id.appbar);
		appbar.setTouchFeedback(MaterialAppBar.TOUCH_FEEDBACK_HOLO_LIGHT);
		appbar.setTitle(getString(R.string.Reset_Password), getResources().getColor(R.color.text_color_dark));
		appbar.addButtonToLeft(R.id.common_button_back, "", R.drawable.ic_close_grey600_24dp);
		appbar.setAppbarBackgroundColor(getResources().getColor(R.color.white));
		appbar.setOnButtonClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (v.getId() == R.id.common_button_back){
					onClickCancel(v);
				}
			}
			
		});
		
		TextView textView = (TextView)findViewById(R.id.email_address);
		textView.setText(this.getIntent().getExtras().getString(INPUT_EMAIL_KEY));
		
		
	}
	
	@Override
	public void InitHandler() {
		// TODO Auto-generated method stub
		mHandler = new Handler() {
			@Override
			public void handleMessage(android.os.Message msg) {
				// 收起菊花
				
			};
		};
	}
}
