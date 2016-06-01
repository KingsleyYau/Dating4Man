package com.qpidnetwork.dating.profile;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.qpidnetwork.dating.R;
import com.qpidnetwork.framework.base.BaseFragmentActivity;
import com.qpidnetwork.request.item.ProfileItem;
import com.qpidnetwork.view.ButtonRaised;
import com.qpidnetwork.view.MaterialAppBar;

/**
 * MyProfile模块
 * @author Max.Chiu
 */
public class MyProfilePhoneVerifyActivity extends BaseFragmentActivity implements OnClickListener {
	
	private ImageView imageViewMobile;
	private TextView textViewAddMobile;
	private TextView textViewMobile;
	private ImageView mobileDoneMark;
	private ImageView landDoneMark;
	private ButtonRaised layoutChangeMobile;
	
	private ImageView imageViewLandline;
	private TextView textViewAddLandline;
	private TextView textViewLandline;
	private ButtonRaised layoutChangeLandline;

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
	 * 点击增加手机
	 * @param view
	 */
	public void onClickAddMobile(View view) {
		Intent intent = new Intent(this, MyProfilePhoneVerifyMobileActivity.class);
		startActivity(intent);
	}
	
	/**
	 * 点击改变手机
	 * @param view
	 */
	public void onClickChangeMobile(View view) {
		Intent intent = new Intent(this, MyProfilePhoneVerifyMobileActivity.class);
		startActivity(intent);
	}
	
	/**
	 * 点击增加固话
	 * @param view
	 */
	public void onClickAddLandline(View view) {
		Intent intent = new Intent(this, MyProfilePhoneVerifyLandlineActivity.class);
		startActivity(intent);
	}
	
	/**
	 * 点击改变固话
	 * @param view
	 */
	public void onClickChangeLandline(View view) {
		Intent intent = new Intent(this, MyProfilePhoneVerifyLandlineActivity.class);
		startActivity(intent);
	}

	@Override
	public void InitView() {
		setContentView(R.layout.activity_my_profile_phone_verify);
		
		imageViewMobile = (ImageView) findViewById(R.id.imageViewMobile);
		textViewAddMobile = (TextView) findViewById(R.id.textViewAddMobile);
		textViewMobile = (TextView) findViewById(R.id.textViewMobile);
		layoutChangeMobile = (ButtonRaised) findViewById(R.id.layoutChangeMobile);
		
		mobileDoneMark = (ImageView)findViewById(R.id.mobileDoneMark);
		landDoneMark = (ImageView)findViewById(R.id.landDoneMark);
		
		imageViewLandline = (ImageView) findViewById(R.id.imageViewLandline);
		textViewAddLandline = (TextView) findViewById(R.id.textViewAddLandline);
		textViewLandline = (TextView) findViewById(R.id.textViewLandline);
		layoutChangeLandline = (ButtonRaised) findViewById(R.id.layoutChangeLandline);	
		
		MaterialAppBar appbar = (MaterialAppBar)findViewById(R.id.appbar); 
		appbar.setTouchFeedback(MaterialAppBar.TOUCH_FEEDBACK_HOLO_LIGHT);
		appbar.addButtonToLeft(android.R.id.button1, "back", R.drawable.ic_close_grey600_24dp);
		appbar.setTitle("Phone verification", getResources().getColor(R.color.text_color_dark));
		appbar.setOnButtonClickListener(this);
	}
	
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		finish();
	}
	
	public void ReloadData() {
		if( mProfileItem != null ) {
			// 手机认证
			if( mProfileItem.mobile != null && mProfileItem.mobile.length() > 0 ) {
				// 已经认证
				imageViewMobile.setImageResource(R.drawable.ic_phone_android_black_48dp);
				textViewAddMobile.setVisibility(View.GONE);
				mobileDoneMark.setVisibility(View.VISIBLE);
				String format = getResources().getString(R.string.Mobile);
				textViewMobile.setText(String.format(format, "+86", mProfileItem.mobile));
				textViewMobile.setVisibility(View.VISIBLE);
				layoutChangeMobile.setVisibility(View.VISIBLE);
				imageViewMobile.setClickable(false);
			} else {
				imageViewMobile.setImageResource(R.drawable.ic_add_grey600_48dp);
				textViewAddMobile.setVisibility(View.VISIBLE);
				mobileDoneMark.setVisibility(View.GONE);
				textViewMobile.setVisibility(View.GONE);
				layoutChangeMobile.setVisibility(View.GONE);
				imageViewMobile.setClickable(true);
			}
			
			// 固话认证
			if( mProfileItem.landline != null && mProfileItem.landline.length() > 0 ) {
				// 已经认证
				imageViewLandline.setImageResource(R.drawable.ic_call_black_48dp);
				textViewAddLandline.setVisibility(View.GONE);
				landDoneMark.setVisibility(View.VISIBLE);
				String format = getResources().getString(R.string.Landline);
				textViewLandline.setText(String.format(format, "+86", mProfileItem.landline));
				textViewLandline.setVisibility(View.VISIBLE);
				layoutChangeLandline.setVisibility(View.VISIBLE);
				imageViewLandline.setClickable(false);
			} else {
				imageViewLandline.setImageResource(R.drawable.ic_add_grey600_48dp);
				textViewAddLandline.setVisibility(View.VISIBLE);
				landDoneMark.setVisibility(View.GONE);
				textViewLandline.setVisibility(View.GONE);
				layoutChangeLandline.setVisibility(View.GONE);
				imageViewLandline.setClickable(true);
			}			
		}
	}
}
