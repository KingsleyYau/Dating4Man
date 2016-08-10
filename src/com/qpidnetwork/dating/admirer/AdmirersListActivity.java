package com.qpidnetwork.dating.admirer;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;

import com.qpidnetwork.dating.R;
import com.qpidnetwork.dating.emf.EMFDetailActivity;
import com.qpidnetwork.dating.emf.EMFSearchActivity;
import com.qpidnetwork.dating.emf.MailEditActivity;
import com.qpidnetwork.framework.base.BaseActionBarFragmentActivity;
import com.qpidnetwork.manager.WebSiteManager;
import com.qpidnetwork.request.RequestJniEMF.ReplyType;

public class AdmirersListActivity extends BaseActionBarFragmentActivity {
	
	public AdmireListFragment fragment;

	@Override
	protected void onCreate(Bundle arg0) {
		// TODO Auto-generated method stub
		super.onCreate(arg0);
		this.getWindow().setBackgroundDrawable(new ColorDrawable(getResources().getColor(WebSiteManager.getInstance().GetWebSite().getSiteColor())));
		
		setCustomContentView(R.layout.activity_admirer_list);

		fragment = AdmireListFragment.newInstance();
		getSupportFragmentManager().beginTransaction()
				.replace(R.id.flAdmirer, fragment).commit();
		
		/*actionBar*/
		getCustomActionBar().addButtonToRight(R.id.common_button_search, "search", R.drawable.ic_search_white_24dp);
		getCustomActionBar().setTitle(getString(R.string.menu_admirers), Color.WHITE);
		getCustomActionBar().setAppbarBackgroundColor(getResources().getColor(WebSiteManager.getInstance().GetWebSite().getSiteColor()));
		
//		getFloatButton().setId(R.id.common_button_send);
//		getFloatButton().setOnClickListener(this);
//		getFloatButton().setVisibility(View.VISIBLE);
	}
	
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		super.onClick(v);
		switch (v.getId()) {
		case R.id.common_button_search:
			onSearch();
			break;
		case R.id.common_button_send:
			MailEditActivity.launchMailEditActivity(this, "", ReplyType.DEFAULT, "", "");
			break;
		}
	}
	
	/**
	 * 搜索按钮响应
	 */
	private void onSearch(){

		startActivityForResult(EMFSearchActivity.getIntent(this, 2), 1);
		//设置切换动画，从右边进入，左边退出  
		overridePendingTransition(R.anim.anim_donot_animate, R.anim.anim_donot_animate);        
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent arg2) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, arg2);
		if(resultCode == RESULT_OK){
			if(requestCode == EMFDetailActivity.REQUEST_CODE){
				if((arg2 != null)&&(arg2.getExtras().containsKey(EMFDetailActivity.EMF_MESSAGEID))){
					String emfId = arg2.getExtras().getString(EMFDetailActivity.EMF_MESSAGEID);

					
					boolean isDelete = arg2.getExtras().getBoolean(EMFDetailActivity.EMF_DELETE);
					boolean isRead = arg2.getExtras().getBoolean(EMFDetailActivity.EMF_DETAIL_READED);
					if(emfId != null){
						if(isDelete){
							fragment.onEMFDelete(emfId);
						}else if(isRead){
							fragment.onEMFRead(emfId);
						}
				}}
			}
		}
		
	}

}
