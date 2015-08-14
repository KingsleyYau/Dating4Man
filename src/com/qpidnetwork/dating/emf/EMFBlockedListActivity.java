package com.qpidnetwork.dating.emf;


import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;

import com.qpidnetwork.dating.R;
import com.qpidnetwork.framework.base.BaseActionBarFragmentActivity;
import com.qpidnetwork.view.MaterialAppBar;



public class EMFBlockedListActivity extends BaseActionBarFragmentActivity{
	
	public static int ACTIVITY_INTENT_CODE = 201;
	
	public static void launchEMFBlockedListActivity(Context context){
		Intent intent = new Intent(context, EMFBlockedListActivity.class);
		context.startActivity(intent);
	}
	
	public EMFBlockListFragment fragment;

	@Override
	protected void onCreate(Bundle arg0) {
		// TODO Auto-generated method stub
		super.onCreate(arg0);
		setCustomContentView(R.layout.activity_emf_blocked_list);

		fragment = new EMFBlockListFragment();
		getSupportFragmentManager().beginTransaction()
				.replace(R.id.flBlockedList, fragment).commit();
		
		/*actionBar*/
		getCustomActionBar().setAppbarBackgroundColor(Color.WHITE);
		getCustomActionBar().setButtonIconById(R.id.common_button_back, R.drawable.ic_arrow_back_grey600_24dp);
		getCustomActionBar().getButtonById(R.id.common_button_back).setBackgroundResource(MaterialAppBar.TOUCH_FEEDBACK_HOLO_LIGHT);
		getCustomActionBar().setTouchFeedback(MaterialAppBar.TOUCH_FEEDBACK_HOLO_LIGHT);
		getCustomActionBar().setTitle(getString(R.string.emf_block_list), getResources().getColor(R.color.text_color_dark));
	}
	
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		super.onClick(v);
		switch (v.getId()) {
		}
	}
}
