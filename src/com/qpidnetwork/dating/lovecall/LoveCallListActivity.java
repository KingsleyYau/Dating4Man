package com.qpidnetwork.dating.lovecall;

import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentPagerAdapter;

import com.qpidnetwork.dating.R;
import com.qpidnetwork.framework.base.BaseTabbarTitleFragmentActivity;
import com.qpidnetwork.manager.WebSiteManager;
import com.qpidnetwork.view.TitleTabBar;

public class LoveCallListActivity extends BaseTabbarTitleFragmentActivity{
	
	public static final String LOVECALL_NEW_REQUEST = "newRequest";

	private LoveCallPagerAdapter pageAdapter;
	
	private boolean newScheduleUpdate = false;
	private boolean newRequesrUpdate = false;
	
	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle arg0) {
		// TODO Auto-generated method stub
		super.onCreate(arg0);
		
		// 统计设置为page activity
		SetPageActivity(true);
		
		this.getWindow().setBackgroundDrawable(new ColorDrawable(getResources().getColor(WebSiteManager.getInstance().GetWebSite().getSiteColor())));
		
		TitleTabBar tabBar = getTitleTabBar();
		tabBar.AddTab(0, "Scheduled", getResources().getDrawable(R.drawable.ic_today_white_24dp));
		tabBar.AddTab(1, "Request", getResources().getDrawable(R.drawable.ic_perm_phone_msg_white_24dp));
		pageAdapter = new LoveCallPagerAdapter(this);
		getViewPagerContainer().setAdapter(pageAdapter);
		
		boolean hasNewRequest = false;
		Bundle bundle = getIntent().getExtras();
		if(bundle != null && bundle.containsKey(LOVECALL_NEW_REQUEST)){
			hasNewRequest = bundle.getBoolean(LOVECALL_NEW_REQUEST);
		}
		if(hasNewRequest){
			tabBar.SelectTab(1);
		}else{
			tabBar.SelectTab(0);
		}
		
		getCustomActionBar().setAppbarBackgroundColor(getResources().getColor(WebSiteManager.getInstance().GetWebSite().getSiteColor()));
	}
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		
		LoveCallListFragment fragment = ((LoveCallListFragment)pageAdapter.getFragment(0));
		if( fragment != null ) {
			fragment.TabSelectTimeoutRefresh(true);
		}
		
		fragment = ((LoveCallListFragment)pageAdapter.getFragment(1));
		if( fragment != null ) {
			fragment.TabSelectTimeoutRefresh(true);
		}
	}
	public class LoveCallPagerAdapter extends FragmentPagerAdapter {

		private int count = 2;// 子页面数目
		private Fragment[] fragmentArr = new Fragment[2];

		public LoveCallPagerAdapter(FragmentActivity activity) {
			super(activity.getSupportFragmentManager());
		}
		
		public Fragment getFragment(int index){
			return fragmentArr[index];
		}

		@Override
		public int getCount() {
			return count;
		}

		@Override
		public Fragment getItem(int position) {
			if (fragmentArr[position] != null) {
				return fragmentArr[position];
			}
			Fragment fragment = null;
			switch (position) {
			case 0:// 我的好友
				fragment = LoveCallListFragment.newInstance(0);
				break;
			case 1:// 匹配女士
				fragment = LoveCallListFragment.newInstance(1);
				break;
			}
			fragmentArr[position] = fragment;
			return fragmentArr[position];
		}

	}
	
	@Override
	public void onPageSelected(int arg0) {
		// TODO Auto-generated method stub
		super.onPageSelected(arg0);
		int index = getCurrentIndex();
		LoveCallListFragment fragment = null;
		if(index == 0){
			fragment = ((LoveCallListFragment)pageAdapter.getFragment(0));
			if(fragment != null){
				fragment.TabSelectTimeoutRefresh(newScheduleUpdate);
			}
		}else if(index == 1){
			fragment = ((LoveCallListFragment)pageAdapter.getFragment(1));
			if(fragment != null){
				fragment.TabSelectTimeoutRefresh(newRequesrUpdate);
			}
		}
	}
	
	@Override
	public void onTabSelected(int index) {
		super.onTabSelected(index);
		// 统计切换页
		onAnalyticsPageSelected(index);
	}
}
