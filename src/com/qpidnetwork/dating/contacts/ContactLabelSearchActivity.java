package com.qpidnetwork.dating.contacts;

import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.qpidnetwork.dating.R;
import com.qpidnetwork.dating.bean.ContactBean;
import com.qpidnetwork.dating.contacts.ContactSearchType.LabelType;
import com.qpidnetwork.framework.base.BaseActionBarFragmentActivity;
import com.qpidnetwork.manager.WebSiteManager;

/**
 * 根据标签显示指定的标签列表
 * @author Hunter
 * 2015.5.23
 */
public class ContactLabelSearchActivity extends BaseActionBarFragmentActivity{
	
	private static final String CONTACT_SEARCH_TYPE = "searchType";
	
	private ListView lvContainer;
	TextView emptyView;
	private LabelType type;
	private int moduleType = 0;//0：为联系人搜索 1：为livechat列表搜索
	
	private ContactsAdapter adapter;
	
	public static Intent getIntent(Context context, LabelType type, int moduleType){
		Intent intent = new Intent(context, ContactLabelSearchActivity.class);
		intent.putExtra(ContactSearchActivity.CURRENT_CONTACT_TYPE, moduleType);
		intent.putExtra(CONTACT_SEARCH_TYPE, type.ordinal());
		return intent;
	}
	
	@Override
	protected void onCreate(Bundle arg0) {
		// TODO Auto-generated method stub
		super.onCreate(arg0);
		setCustomContentView(R.layout.activity_contact_label_search);
		this.getWindow().setBackgroundDrawable(new ColorDrawable(getResources().getColor(WebSiteManager.getInstance().GetWebSite().getSiteColor())));
		
		lvContainer = (ListView)findViewById(R.id.lvContainer);
		emptyView = (TextView)findViewById(R.id.emptyView);
		lvContainer.setEmptyView(emptyView);
		
		Bundle bundle = getIntent().getExtras();
		if(bundle != null){
			if(bundle.containsKey(CONTACT_SEARCH_TYPE)){
				type = LabelType.values()[bundle.getInt(CONTACT_SEARCH_TYPE)];
			}
			if(bundle.containsKey(ContactSearchActivity.CURRENT_CONTACT_TYPE)){
				moduleType = bundle.getInt(ContactSearchActivity.CURRENT_CONTACT_TYPE);
			}
		}
	
		initTitle();
		initData();
	}
	
	
	private void initTitle(){
		String title = "";
		String empptyTip = "";
		switch (type) {
		case ONLINE_ONLY:
			title = getResources().getString(R.string.contact_label_online_only);
			empptyTip = getResources().getString(R.string.contact_no_online_contacts);
			break;
		case OFFLINE_ONLY:
			title = getResources().getString(R.string.contact_label_offline_only);
			empptyTip = getResources().getString(R.string.contact_no_offline_contacts);
			break;
		case MY_FAVORITES:
			title = getResources().getString(R.string.contact_label_my_favorites);
			empptyTip = getResources().getString(R.string.contact_no_favorites);
			break;
		case WITH_VIDEOS:
			title = getResources().getString(R.string.contact_label_with_videos);
			empptyTip = getResources().getString(R.string.contact_no_video_ladies);
			break;
		default:
			break;
		}
		
		emptyView.setText(empptyTip);
		getCustomActionBar().setTitle(title, Color.WHITE);
		if(moduleType == 0){
			/*联系人*/
			getCustomActionBar().setAppbarBackgroundColor(getResources().getColor(WebSiteManager.getInstance().GetWebSite().getSiteColor()));
		}else if(moduleType == 1){
			/*livechat*/
			getCustomActionBar().setAppbarBackgroundColor(Color.BLACK);
		}
	}
	
	private void initData(){
		List<ContactBean> contacts = ContactManager.getInstance().getContactsByType(type);
		adapter = new ContactsAdapter(this, moduleType);
		adapter.replaceList(contacts);
		lvContainer.setAdapter(adapter);
	}
}
