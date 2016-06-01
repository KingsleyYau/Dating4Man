package com.qpidnetwork.dating.lady;

import java.lang.ref.WeakReference;
import java.util.HashMap;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.RelativeLayout;

import com.qpidnetwork.dating.R;
import com.qpidnetwork.framework.base.BaseFragment;
import com.qpidnetwork.framework.base.BaseFragmentActivity;
import com.qpidnetwork.framework.util.UnitConversion;
import com.qpidnetwork.request.item.LadyDetail;
import com.qpidnetwork.view.ViewPagerFixed;

public class NormalPhotoPreviewActivity extends BaseFragmentActivity implements
		OnPageChangeListener {

	public static final String LADY_DETAIL = "ladyDetail";

	private LadyDetail mLadyDetail;
	private ViewPagerFixed mViewPager;
	private NormalPhotoAdapter mAdapter;
	private boolean mInitPage = true;

	public static void launchNoramlPhotoActivity(Context context, LadyDetail ladyDetail) {
		Intent intent = new Intent(context, NormalPhotoPreviewActivity.class);
		intent.putExtra(LADY_DETAIL, ladyDetail);
		context.startActivity(intent);
	}

	@Override
	protected void onCreate(Bundle arg0) {
		// TODO Auto-generated method stub
		super.onCreate(arg0);
		
		// 统计设置为page activity
		SetPageActivity(true);
		
		setContentView(R.layout.activity_privatephoto_preview);

		this.getWindow().setBackgroundDrawable(new ColorDrawable(Color.BLACK));

		Bundle bundle = getIntent().getExtras();
		if (bundle != null) {
			if (bundle.containsKey(LADY_DETAIL)) {
				mLadyDetail = (LadyDetail)bundle.getSerializable(LADY_DETAIL);
			}
		}
		if(mLadyDetail == null){
			return;
		}
		initViews();
	}

	@SuppressWarnings("deprecation")
	private void initViews() {
		/* cancel */
		ImageButton buttonCancel = (ImageButton) findViewById(R.id.buttonCancel);
		buttonCancel.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				finish();
			}
		});
		// 分页控件
		mAdapter = new NormalPhotoAdapter(this, mLadyDetail);
		mViewPager = (ViewPagerFixed) findViewById(R.id.viewPager);
		mViewPager.setOnPageChangeListener(this);
		mViewPager.setOffscreenPageLimit(1);
		mViewPager.setAdapter(mAdapter);
//		mViewPager.setCurrentItem(1);

		if (Build.VERSION.SDK_INT >= 21) {
			buttonCancel.getLayoutParams().height = UnitConversion.dip2px(this,
					48);
			buttonCancel.getLayoutParams().width = UnitConversion.dip2px(this,
					48);
			((RelativeLayout.LayoutParams) buttonCancel.getLayoutParams()).topMargin = UnitConversion
					.dip2px(this, 18);
		}
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		int pageId = mViewPager.getCurrentItem();
		if(pageId >= 0){
			BaseFragment baseFragment = GetBaseFragment(pageId);
			if (null != baseFragment) {
				baseFragment.onFragmentPause(pageId);
			}
		}
	}

	@Override
	public void onPageScrollStateChanged(int arg0) {
		
	}

	@Override
	public void onPageScrolled(int arg0, float arg1, int arg2) 
	{
		// 若page未初始化
		if (mInitPage) {
			// 回调Fragment被选中
			if (FragmentSelected(arg0)) {
				// page初始化完成
				mInitPage = false;
			}
		}
	}

	@Override
	public void onPageSelected(int arg0) {
		/* 页面切换，reset之前的ImageView放大设置 */
		Fragment fragment = null;
		if (mAdapter != null) {
			if (mAdapter.getFragment(arg0 - 1) != null) {
				fragment = mAdapter.getFragment(arg0 - 1);
				if(fragment instanceof NormalPhotoFragment){
					((NormalPhotoFragment)fragment).reset();
				}
			}
			if (mAdapter.getFragment(arg0 + 1) != null) {
				fragment = mAdapter.getFragment(arg0 + 1);
				if(fragment instanceof NormalPhotoFragment){
					((NormalPhotoFragment)fragment).reset();
				}
			}
		}
		
		// 回调Fragment被选中
		if (FragmentSelected(arg0)) {
			// page初始化完成
			mInitPage = false;
		}
	}

	private class NormalPhotoAdapter extends FragmentPagerAdapter {

		private LadyDetail ladyDetail;
		private HashMap<Integer, WeakReference<Fragment>> mPageReference;

		public NormalPhotoAdapter(FragmentActivity activity, LadyDetail ladyDetail) {
			super(activity.getSupportFragmentManager());
			this.ladyDetail = ladyDetail;
			mPageReference = new HashMap<Integer, WeakReference<Fragment>>();
		}

		public Fragment getFragment(int position) {
			Fragment fragment = null;
			if (mPageReference.containsKey(position)) {
				fragment = mPageReference.get(position).get();
			}
			return fragment;
		}

		@Override
		public int getCount() {
			int count = 0;
			if (ladyDetail != null) {
				if(ladyDetail.photoList!=null){
					count = ladyDetail.photoList.size();
				}
				count += ladyDetail.photoLockNum;
			}
			
			return count;
		}

		@Override
		public Fragment getItem(int position) {
			Fragment fragment = null;
			if (mPageReference.containsKey(position)) {
				fragment = mPageReference.get(position).get();
			}
			if (fragment == null) {
				if((ladyDetail.photoList != null)&&(position >= 0) &&(position < ladyDetail.photoList.size())){
					fragment = NormalPhotoFragment.getInstance(ladyDetail.photoList.get(position));
				}else{
					fragment = LockPhotoFragment.getInstance(ladyDetail);
				}
				
				mPageReference.put(position, new WeakReference<Fragment>(
						fragment));
			}
			return fragment;
		}
	}

	@Override
	public void InitView() {
		// TODO Auto-generated method stub
		
	}

	/**
	 * 获取BaseFragment
	 * @param arg0
	 * @return
	 */
	private BaseFragment GetBaseFragment(int page)
	{
		BaseFragment baseFragment = null;
		if (null != mAdapter && mAdapter.getCount() > 0)
		{
			if (mAdapter.getFragment(page) != null) {
				Fragment fragment = mAdapter.getFragment(page);
				if(fragment instanceof BaseFragment){
					baseFragment = (BaseFragment)fragment;
				}
			}
		}
		return baseFragment;
	}
	
	/**
	 * 回调Fragment被选中的函数
	 * @param page
	 */
	private boolean FragmentSelected(int page)
	{
		boolean result = false;
		BaseFragment baseFragment = GetBaseFragment(page);
		if (null != baseFragment) {
			baseFragment.onFragmentSelected(page);
			result = true;
		}
		return result;
	}
}
