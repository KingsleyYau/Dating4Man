package com.qpidnetwork.dating.quickmatch;

import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.view.View;

import com.qpidnetwork.dating.R;
import com.qpidnetwork.framework.base.BaseTabbarTitleFragmentActivity;
import com.qpidnetwork.manager.WebSiteManager;
import com.qpidnetwork.view.TitleTabBar;


/**
 * QuickMatch模块
 * 主界面
 * @author Max.Chiu
 */
public class QuickMatchActivity extends BaseTabbarTitleFragmentActivity {

	/**
	 * 分页适配器
	 */
	private class QuickMatchActivityPagerAdapter extends FragmentPagerAdapter {

		public QuickMatchActivityPagerAdapter(FragmentManager fm) {
			super(fm);
			// TODO Auto-generated constructor stub
		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return 2;
		}

		@Override
		public Fragment getItem(int arg0) {
			// TODO Auto-generated method stub
			Fragment fragment = null;
			switch (arg0) {
			case 0:{
				fragment = QuickMatchMatchesFragment.newInstance();
			}break;
			case 1:{
				fragment = QuickMatchLikeFragment.newInstance();
			}break;
			default:
				break;
			}
			return fragment;
		} 

	}
	
	//private ViewPager mViewPager;
	//private TitleTabBar mTitleTabBar = null;
	
	private QuickMatchActivityPagerAdapter pageAdapter;
	
	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// 统计设置为page activity
		SetPageActivity(true);
		
		this.getWindow().setBackgroundDrawable(new ColorDrawable(getResources().getColor(WebSiteManager.getInstance().GetWebSite().getSiteColor())));
		
		TitleTabBar tabBar = getTitleTabBar();
		tabBar.AddTab(0, getString(R.string.matches), getResources().getDrawable(R.drawable.ic_female_contact_white_24dp));
		tabBar.AddTab(1, getString(R.string.i_like), getResources().getDrawable(R.drawable.ic_favorite_white_24dp));
		pageAdapter = new QuickMatchActivityPagerAdapter(getSupportFragmentManager());
		getViewPagerContainer().setAdapter(pageAdapter);
		getViewPagerContainer().setOnPageChangeListener(this);
		getCustomActionBar().setAppbarBackgroundColor(getResources().getColor(WebSiteManager.getInstance().GetWebSite().getSiteColor()));
//		getCustomActionBar().addButtonToRight(R.id.common_button_search, "", R.drawable.ic_settings_white_24dp, "");
//		getCustomActionBar().getButtonById(R.id.common_button_search).setOnClickListener(new OnClickListener(){
//
//			@Override
//			public void onClick(View v) {
//				// TODO Auto-generated method stub
//				Intent intent = new Intent(QuickMatchActivity.this, MyProfileMatchCriteriaActivity.class);
//				startActivity(intent);
//			}
//			
//		});

		
		//getCustomActionBar().addButtonToRight(R.id.common_button_send, "send", R.drawable.ic_send_white_24dp);
		//getCustomActionBar().addButtonToRight(R.id.common_button_search, "search", R.drawable.ic_search_white_24dp);
		
		tabBar.SelectTab(0);
	}

	@Override
	public void onPageScrollStateChanged(int arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onPageScrolled(int arg0, float arg1, int arg2) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onPageSelected(int arg0) {
		// TODO Auto-generated method stub
		super.onPageSelected(arg0);
		if( arg0 == 1 ) {
			QuickMatchLikeFragment.newInstance().LoadData(false);
		}
	}

	@Override
	public void onTabSelected(int index) {
		// TODO Auto-generated method stub
		super.onTabSelected(index);
		
		// 统计切换页
		onAnalyticsPageSelected(index);
	}
	
	/**
	 * 点击取消
	 */
	public void onClickCancel(View v) {
		
		switch(v.getId()){
		case R.id.common_button_back:
			finish();
			break;
		}
	}

	/*@Override
	public void InitView() {
		// TODO Auto-generated method stub
		setContentView(R.layout.activity_quick_match);
		
		// TabBar控件
		mTitleTabBar = (TitleTabBar) findViewById(R.id.titleTabBar);
		mTitleTabBar.AddTab(0, "Matches", getResources().getDrawable(R.drawable.u482));
		mTitleTabBar.AddTab(1, "You Like", getResources().getDrawable(R.drawable.u478));
		mTitleTabBar.SetTabBadge(1, "  ");
//		mTitleTabBar.AddTab(2, "Like Each Other", Drawable.createFromPath("/sdcard/u213.png"));
		mTitleTabBar.SetListener(this);
		
        // 分页控件
		mViewPager = (ViewPager) findViewById(R.id.viewPager);
		mViewPager.setAdapter();
		mViewPager.setOnPageChangeListener(this);
		
		// 选择第一项
		mTitleTabBar.SelectTab(0);
	}

	
	
	@Override
	public void InitHandler() {
		// TODO Auto-generated method stub
		
	}*/
}
