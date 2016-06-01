package com.qpidnetwork.dating.livechat.theme.store;

import java.lang.ref.WeakReference;
import java.util.HashMap;

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
 * 主题主要展示界面
 * 
 * @author Hunter
 * @since 2016.4.21
 */
public class ThemeMainActivity extends BaseTabbarTitleFragmentActivity {

	private String mWomanId;
	private LivechatThemePageAdapter mLivechatThemePageAdapter;

	@Override
	protected void onCreate(Bundle arg0) {
		// TODO Auto-generated method stub
		super.onCreate(arg0);
		this.getWindow().setBackgroundDrawable(
				new ColorDrawable(getResources().getColor(
						WebSiteManager.getInstance().GetWebSite()
								.getSiteColor())));
		mWomanId = getIntent().getStringExtra("womanId");

		TitleTabBar tabBar = getTitleTabBar();
		tabBar.AddTab(0, getString(R.string.livechat_theme_sceme_store),
				getResources().getDrawable(R.drawable.ic_add_shopping_cart_white_24dp));
		tabBar.AddTab(1, getString(R.string.livechat_theme_my_scene),
				getResources().getDrawable(R.drawable.ic_effect_white_24dp));
		mLivechatThemePageAdapter = new LivechatThemePageAdapter(
				getSupportFragmentManager());
		getViewPagerContainer().setAdapter(mLivechatThemePageAdapter);
		getViewPagerContainer().setOnPageChangeListener(this);
		getCustomActionBar().setAppbarBackgroundColor(
				getResources().getColor(
						WebSiteManager.getInstance().GetWebSite()
								.getSiteColor()));
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

		switch (v.getId()) {
		case R.id.common_button_back:
			finish();
			break;
		}
	}

	private class LivechatThemePageAdapter extends FragmentPagerAdapter {

		private HashMap<Integer, WeakReference<Fragment>> mPageReference;

		public LivechatThemePageAdapter(FragmentManager fm) {
			super(fm);
			mPageReference = new HashMap<Integer, WeakReference<Fragment>>();
		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return 2;
		}

		@Override
		public Fragment getItem(int position) {
			Fragment fragment = null;
			if (mPageReference.containsKey(position)) {
				fragment = mPageReference.get(position).get();
			}
			if (fragment == null) {
				if (position == 0) {
					fragment = new SceneStoreFragment(mWomanId);// 购买主题
				} else if (position == 1) {
					fragment = new MyScenesFragment();// 已买主题
				}
				mPageReference.put(position, new WeakReference<Fragment>(
						fragment));
			}
			return fragment;
		}

	}
}
