package com.qpidnetwork.dating.livechat;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.annotation.SuppressLint;
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
import com.qpidnetwork.livechat.LCMessageItem;
import com.qpidnetwork.view.ViewPagerFixed;

public class LivechatPrivatePhotoPreviewActivity extends BaseFragmentActivity implements OnPageChangeListener{

	private static final String LIVECHAT_PRIVATEPHOTO = "privatephoto";

	private List<LCMessageItem> mMessageList;
	private int currPosition;
	
	private ViewPagerFixed mViewPager;
	private PrivatePhotoAdapter mAdapter;
	private boolean mInitPage = true;
//	private LiveChatManager mLiveChatManager;


	public static Intent getIntent(Context context, PrivatePhotoPriviewBean bean) {
		Intent intent = new Intent(context, LivechatPrivatePhotoPreviewActivity.class);
		intent.putExtra(LIVECHAT_PRIVATEPHOTO, bean);
		return intent;
	}

	@Override
	protected void onCreate(Bundle arg0) {
		// TODO Auto-generated method stub
		super.onCreate(arg0);
		
		// 统计设置为page activity
		SetPageActivity(true);
		
		setContentView(R.layout.activity_privatephoto_preview);
		
		this.getWindow().setBackgroundDrawable(new ColorDrawable(Color.BLACK));
		
		mMessageList = new ArrayList<LCMessageItem>();
//		mLiveChatManager = LiveChatManager.getInstance();
		Bundle bundle = getIntent().getExtras();
		if ((bundle != null) && (bundle.containsKey(LIVECHAT_PRIVATEPHOTO))) {
			PrivatePhotoPriviewBean bean = (PrivatePhotoPriviewBean) bundle
					.getSerializable(LIVECHAT_PRIVATEPHOTO);
			if(bean.msgList != null && bean.msgList.size() > 0){
				mMessageList = bean.msgList;
			}
			currPosition = bean.currPosition;
		}else{
			currPosition = 0;
		}
		initViews();
	}
	
	@SuppressWarnings("deprecation")
	private void initViews(){
		/*cancel*/
		ImageButton buttonCancel = (ImageButton)findViewById(R.id.buttonCancel);
		buttonCancel.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				finish();
			}
		});
		 // 分页控件
		mAdapter = new PrivatePhotoAdapter(this, mMessageList);
		mViewPager = (ViewPagerFixed) findViewById(R.id.viewPager);
		mViewPager.setOnPageChangeListener(this);
		mViewPager.setOffscreenPageLimit(1);
		mViewPager.setAdapter(mAdapter);
		mViewPager.setCurrentItem(currPosition);
		
		if (Build.VERSION.SDK_INT >= 21){
			buttonCancel.getLayoutParams().height = UnitConversion.dip2px(this, 48);
			buttonCancel.getLayoutParams().width = UnitConversion.dip2px(this, 48);
			((RelativeLayout.LayoutParams)buttonCancel.getLayoutParams()).topMargin = UnitConversion.dip2px(this, 18);
		}
	}

	@Override
	public void onPageScrollStateChanged(int arg0) 
	{
		// TODO Auto-generated method stub
		
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
		/*页面切换，reset之前的ImageView放大设置*/
		if(mAdapter != null){
			if(mAdapter.getFragment(arg0 - 1) != null){
				((PrivatePhotoPreviewFragment)mAdapter.getFragment(arg0 - 1)).reset();
			}
			if(mAdapter.getFragment(arg0 + 1) != null){
				((PrivatePhotoPreviewFragment)mAdapter.getFragment(arg0 + 1)).reset();
			}
		}
		
		// 回调Fragment被选中
		if (FragmentSelected(arg0)) {
			// page初始化完成
			mInitPage = false;
		}
	}
	
	@SuppressLint("UseSparseArrays")
	private class PrivatePhotoAdapter extends FragmentPagerAdapter{

		private List<LCMessageItem> msgList;
		private HashMap<Integer, WeakReference<Fragment>> mPageReference;
		
		
		public PrivatePhotoAdapter(FragmentActivity activity, List<LCMessageItem> msgList) {
			super(activity.getSupportFragmentManager());
			this.msgList = msgList;
			mPageReference = new HashMap<Integer, WeakReference<Fragment>>();
		}
		
		public Fragment getFragment(int position){
			Fragment fragment = null;
			if(mPageReference.containsKey(position)){
				fragment = mPageReference.get(position).get();
			}
			return fragment;
		}

		@Override
		public int getCount() {
			int count = 0;
			if(msgList != null){
				count = msgList.size();
			}
			return count;
		}
		
		@Override
		public Fragment getItem(int position) {
			Fragment fragment= PrivatePhotoPreviewFragment.getFragment(msgList.get(position));
			mPageReference.put(position, new WeakReference<Fragment>(fragment));
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
