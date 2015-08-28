package com.qpidnetwork.framework.base;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;

import com.qpidnetwork.dating.R;
import com.qpidnetwork.view.TitleTabBar;
import com.qpidnetwork.view.TitleTabBar.TitleTabBarListener;
import com.qpidnetwork.view.ViewPagerFixed;

/**
 * TabBar title activty(注意FragmentPagerAdapter 与 tab数目匹配问题，否则会异常)
 * @author Hunter
 * @since 2015.4.24
 */
public class BaseTabbarTitleFragmentActivity extends BaseActionBarFragmentActivity implements OnPageChangeListener, TitleTabBarListener{

	private TitleTabBar mTitleTabBar;
	private ViewPagerFixed mViewpager;
	private int index = 0;
	
	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle arg0) {
		// TODO Auto-generated method stub
		super.onCreate(arg0);
		mTitleTabBar = new TitleTabBar(this);
		getCustomActionBar().setCustomTitle(mTitleTabBar);
		mTitleTabBar.SetListener(this);
		mViewpager = new ViewPagerFixed(this);
		mViewpager.setId(R.id.activity_content_viewpager);
		mViewpager.setBackgroundColor(Color.WHITE);
		setCustomContentView(mViewpager);
		mViewpager.setOnPageChangeListener(this);
	}
	
	public TitleTabBar getTitleTabBar(){
		return mTitleTabBar; 
	}
	
	public ViewPager getViewPagerContainer(){
		return mViewpager;
	}
	
	@Override
	public void onTabSelected(int index) {
		// TODO Auto-generated method stub
		mViewpager.setCurrentItem(index, true);
		this.index = index;
	}

	@Override
	public void onPageScrollStateChanged(int arg0) {
		// TODO Auto-generated method stub
//		
//		index = arg0;
	}

	@Override
	public void onPageScrolled(int arg0, float arg1, int arg2) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onPageSelected(int arg0) {
		// TODO Auto-generated method stub
		mTitleTabBar.SelectTab(arg0);
		index = arg0;
	}
	
	/**
	 * 返回当前选中的Tab
	 */
	public int getCurrentIndex(){
		return index;
	}

}
