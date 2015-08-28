package com.qpidnetwork.framework.base;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.TranslateAnimation;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.qpidnetwork.dating.R;
import com.qpidnetwork.manager.WebSiteManager;
import com.qpidnetwork.view.ButtonFloat;
import com.qpidnetwork.view.MaterialAppBar;

/**
 * 添加基础ActionBar
 * @author Hunter 
 * @since 2015.4.24
 */
public class BaseActionBarFragmentActivity extends BaseFragmentActivity{
	
	private LinearLayout llContainer;
	private MaterialAppBar mActionBar;
	private ButtonFloat floatButton;
	private TextView errorMsg;
	
	@Override
	protected void onCreate(Bundle arg0) {
		// TODO Auto-generated method stub
		super.onCreate(arg0);
		setContentView(R.layout.activity_base_actionbar);
		
		llContainer = (LinearLayout)findViewById(R.id.llContainer);
		mActionBar = (MaterialAppBar)findViewById(R.id.appbar);
		floatButton = (ButtonFloat)findViewById(R.id.floatButton);
		errorMsg = (TextView)findViewById(R.id.errorMsg);
		
		mActionBar.addButtonToLeft(R.id.common_button_back, "", R.drawable.ic_arrow_back_white_24dp);
		mActionBar.setAppbarBackgroundColor(getResources().getColor(R.color.theme_actionbar_bg_cd));
		mActionBar.setOnButtonClickListener(this);
		
		floatButton.setButtonBackground(getResources().getColor(WebSiteManager.getInstance().GetWebSite().getSiteColor()));
		
	}
	
	protected View getBackButton(){
		return mActionBar.getButtonById(R.id.common_button_back);
	}
	
	protected void setCustomContentView(int layoutResId) {
		LayoutInflater.from(this).inflate(layoutResId, llContainer);
	}
	
	protected  void setCustomContentView(View view) {
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		llContainer.addView(view, params);
	}
	
	public MaterialAppBar getCustomActionBar(){
		return mActionBar;
	}
	
	public ButtonFloat getFloatButton(){
		floatButton.setVisibility(View.VISIBLE);
		return floatButton;
	}
	
	public void showErrorMssage(CharSequence msg){
		errorMsg.setText(msg);
		TranslateAnimation animation = new TranslateAnimation(TranslateAnimation.RELATIVE_TO_SELF, 0,
				TranslateAnimation.RELATIVE_TO_SELF, 0,
				TranslateAnimation.RELATIVE_TO_SELF, -2, 
				TranslateAnimation.RELATIVE_TO_SELF, 0);
		animation.setDuration(500);
		errorMsg.startAnimation(animation);
		errorMsg.setVisibility(View.VISIBLE);
	}
	
	public void clearErrorMessage(){
		errorMsg.setVisibility(View.GONE);
	}
	
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		super.onClick(v);
		switch (v.getId()) {
		case R.id.common_button_back:
			
			finish();
			break;
		}
	}
}
