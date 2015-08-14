package com.qpidnetwork.dating.contacts;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;

import com.qpidnetwork.dating.R;
import com.qpidnetwork.framework.base.BaseActionBarFragmentActivity;
import com.qpidnetwork.manager.WebSiteManager;

public class ContactsListActivity extends BaseActionBarFragmentActivity {
	
	public ContactsListFragment fragment;

	@Override
	protected void onCreate(Bundle arg0) {
		// TODO Auto-generated method stub
		super.onCreate(arg0);
		this.getWindow().setBackgroundDrawable(new ColorDrawable(getResources().getColor(WebSiteManager.getInstance().GetWebSite().getSiteColor())));
		
		setCustomContentView(R.layout.activity_admirer_list);
	
		ContactsListFragment fragment = ContactsListFragment.newInstance();
		getSupportFragmentManager().beginTransaction()
				.replace(R.id.flAdmirer, fragment).commit();
		
		/*actionBar*/
		getCustomActionBar().addButtonToRight(R.id.common_button_search, "", R.drawable.ic_search_white_24dp);
		getCustomActionBar().setTitle(getString(R.string.contact_title), Color.WHITE);
		getCustomActionBar().setAppbarBackgroundColor(getResources().getColor(WebSiteManager.getInstance().GetWebSite().getSiteColor()));
	}
	
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		
		super.onClick(v);
		switch (v.getId()) {
		case R.id.common_button_search:
			onSearch();
			overridePendingTransition(R.anim.anim_donot_animate, R.anim.anim_donot_animate);  
			break;
		}
	}
	
	/**
	 * 搜索按钮响应
	 */
	private void onSearch(){
		Intent intent = new Intent(this, ContactSearchActivity.class);
		startActivity(intent);
	}

}
