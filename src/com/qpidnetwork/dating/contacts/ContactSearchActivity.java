package com.qpidnetwork.dating.contacts;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.qpidnetwork.dating.R;
import com.qpidnetwork.dating.contacts.ContactSearchType.LabelType;
import com.qpidnetwork.framework.base.BaseFragmentActivity;
import com.qpidnetwork.framework.util.UnitConversion;
import com.qpidnetwork.manager.WebSiteManager;

public class ContactSearchActivity extends BaseFragmentActivity implements OnEditorActionListener, TextWatcher{
	
	public static final String CURRENT_CONTACT_TYPE = "contactType";
	
//	private LinearLayout llSearchContainer;
	private ImageView ivCancle;
	private EditText etSearchFilter;
	private ListView lvContainer;
	//label
	private LinearLayout llLabelContainer;
	private Button btnOnline;
	private Button btnOffline;
	private Button btnFavorite;
	private Button btnVideos;
	
	private ContactsAdapter adapter;
	
	int type = 0;//0：为联系人搜索 1：为livechat列表搜索
	
	public static void launchContactSearchActivity(Context context, int type){
		Intent intent = new Intent(context, ContactSearchActivity.class);
		intent.putExtra(CURRENT_CONTACT_TYPE, type);
		context.startActivity(intent);
	}
	
	@Override
	protected void onCreate(Bundle arg0) {
		// TODO Auto-generated method stub
		super.onCreate(arg0);
		setContentView(R.layout.activity_contacts_search);
		this.getWindow().setBackgroundDrawable(new ColorDrawable(getResources().getColor(WebSiteManager.getInstance().GetWebSite().getSiteColor())));
		
		Bundle bundle = getIntent().getExtras();
		if((bundle!=null)&&(bundle.containsKey(CURRENT_CONTACT_TYPE))){
			type = bundle.getInt(CURRENT_CONTACT_TYPE);
		}
		
		initViews();
		adapter = new ContactsAdapter(this, type);
		lvContainer.setAdapter(adapter);
		
		showSoftInput();
	}
	
	private void initViews(){
//		llSearchContainer = (LinearLayout)findViewById(R.id.llSearchContainer);
//		if(type == 0){
//			llSearchContainer.setBackgroundColor(getResources().getColor(R.color.theme_actionbar_bg_cd));
//		}else if(type == 1){
//			llSearchContainer.setBackgroundColor(Color.BLACK);
//		}
		
		ivCancle = (ImageView)findViewById(R.id.ivCancle);
		etSearchFilter = (EditText)findViewById(R.id.etSearchFilter);
		etSearchFilter.setHint(R.string.contact_search_filter_hint);
		/*设置键盘搜索键响应*/
		etSearchFilter.setOnEditorActionListener(this);
		etSearchFilter.addTextChangedListener(this);
		
		lvContainer = (ListView)findViewById(R.id.lvContainer);
		
		llLabelContainer = (LinearLayout)findViewById(R.id.llLabelContainer);
		btnOnline = (Button)findViewById(R.id.btnOnline);
		btnOffline = (Button)findViewById(R.id.btnOffline);
		btnFavorite = (Button)findViewById(R.id.btnFavorite);
		btnVideos = (Button)findViewById(R.id.btnVideos);
		
		ivCancle.setOnClickListener(this);
		btnOnline.setOnClickListener(this);
		btnOffline.setOnClickListener(this);
		btnFavorite.setOnClickListener(this);
		btnVideos.setOnClickListener(this);
		
		LinearLayout includeSearch = (LinearLayout) findViewById(R.id.includeSearch);
//		llSearchContainer = (LinearLayout)findViewById(R.id.llSearchContainer);
		if(type == 0){
			includeSearch.setBackgroundColor(getResources().getColor(WebSiteManager.getInstance().GetWebSite().getSiteColor()));
		} else if(type == 1){
			includeSearch.setBackgroundColor(Color.BLACK);
			//ivCancle.setBackgroundResource(R.drawable.touch_feedback_holo_dark);
			
		}
		
		if (Build.VERSION.SDK_INT >= 21 ) {
			ivCancle.getLayoutParams().height = UnitConversion.dip2px(this, 48);
			ivCancle.getLayoutParams().width = UnitConversion.dip2px(this, 48);
		}
	}
	
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		super.onClick(v);
		switch (v.getId()) {
		case R.id.ivCancle:
			finish();
			break;
		case R.id.btnOnline:
			onSearchLabelClick(LabelType.ONLINE_ONLY);
			break;
		case R.id.btnOffline:
			onSearchLabelClick(LabelType.OFFLINE_ONLY);
			break;
		case R.id.btnFavorite:
			onSearchLabelClick(LabelType.MY_FAVORITES);
			break;
		case R.id.btnVideos:
			onSearchLabelClick(LabelType.WITH_VIDEOS);
			break;
		default:
			break;
		}
	}
	
	private void onSearchLabelClick(LabelType type){
		startActivity(ContactLabelSearchActivity.getIntent(this, type, this.type));
		finish();
	}
	
	private void onSearchByIdOrName(){
		String key = etSearchFilter.getText().toString().trim();
		if((key == null) ||(key.equals(""))){
			return;
		}
		
		adapter.replaceList(ContactManager.getInstance().getContactsByIdOrName(key));
		lvContainer.setVisibility(View.VISIBLE);
		llLabelContainer.setVisibility(View.GONE);
	}
	
	@Override
	public void finish(){
		super.finish();
		overridePendingTransition(R.anim.anim_donot_animate, R.anim.anim_donot_animate);   
	}

	@Override
	public void InitView() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void beforeTextChanged(CharSequence s, int start, int count,
			int after) {
		
	}

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {
		if (s.toString().length() == 0){
			lvContainer.setVisibility(View.GONE);
			llLabelContainer.setVisibility(View.VISIBLE);
		}else{
			adapter.replaceList(ContactManager.getInstance().getContactsByIdOrName(s.toString()));
			lvContainer.setVisibility(View.VISIBLE);
			llLabelContainer.setVisibility(View.GONE);
		}
	}

	@Override
	public void afterTextChanged(Editable s) {
		
	}

	@Override
	public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
		if(actionId == EditorInfo.IME_ACTION_SEARCH){
			onSearchByIdOrName();
			return true;
		}
		return false;	
	}
}
