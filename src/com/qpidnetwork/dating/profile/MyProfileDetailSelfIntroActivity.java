package com.qpidnetwork.dating.profile;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;

import com.qpidnetwork.dating.BaseActivity;
import com.qpidnetwork.dating.R;
import com.qpidnetwork.view.MaterialAppBar;

/**
 * MyProfile模块
 * @author Max.Chiu
 */
public class MyProfileDetailSelfIntroActivity extends BaseActivity {

	public static final String SELF_INTRO = "self_intro";
	
	/**
	 * 个人简介
	 */
	private EditText editTextSelfIntro;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		mContext = this;
	}
	
	

	
	/**
	 * 初始化界面
	 */
	@Override
	public void InitView() {
		setContentView(R.layout.activity_my_profile_detail_selfintro);
		
		MaterialAppBar appbar = (MaterialAppBar)findViewById(R.id.appbar);
		appbar.setTouchFeedback(MaterialAppBar.TOUCH_FEEDBACK_HOLO_LIGHT);
		appbar.setAppbarBackgroundColor(getResources().getColor(R.color.theme_actionbar_secoundary));
		appbar.addButtonToLeft(R.id.common_button_back, "", R.drawable.ic_close_grey600_24dp);
		appbar.addButtonToRight(R.id.common_button_ok, "", R.drawable.ic_done_grey600_24dp);
		appbar.setTitle(getString(R.string.My_selfintro), getResources().getColor(R.color.text_color_dark));
		
		appbar.setOnButtonClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				switch (v.getId()){
				case R.id.common_button_back:
					setResult(RESULT_CANCELED, null);
					finish();
					break;
				case R.id.common_button_ok:
					Intent intent = new Intent();
					intent.putExtra(SELF_INTRO, editTextSelfIntro.getText().toString());
					setResult(RESULT_OK, intent);
					finish();
					break;
				}
			}
			
		});
		
		/**
		 * 个人简介
		 */
		editTextSelfIntro = (EditText) findViewById(R.id.editTextSelfIntro);
		String text = getIntent().getExtras().getString(SELF_INTRO);
		if( text != null ) {
			editTextSelfIntro.setText(text);
		}
	}

	@Override
	public void InitHandler() {
		// TODO Auto-generated method stub
		
	}
	
}
