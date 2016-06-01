package com.qpidnetwork.dating.livechat.expression;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.RelativeLayout;

import com.qpidnetwork.dating.R;
import com.qpidnetwork.dating.livechat.expression.NormalEmotionFragment.OnItemClickCallback;
import com.qpidnetwork.framework.base.BaseFragment;
import com.qpidnetwork.framework.util.Log;
import com.qpidnetwork.framework.util.UnitConversion;
import com.qpidnetwork.view.AbilitySwapablePageView;

@SuppressLint("InflateParams")
public class EmotionMainFragment extends BaseFragment{
	

	private ImageButton ivEmotionHistory;
	private ImageButton ivEmotion;
	
	private View indicator;
	private AbilitySwapablePageView vpEmotion; 
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_emotion_main, null);
		initViews(view);
		return view;
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onActivityCreated(savedInstanceState);
		EmotionPagerAdapter adapter = new EmotionPagerAdapter(getChildFragmentManager());
		vpEmotion.setAdapter(adapter);
		vpEmotion.setOnPageChangeListener(new OnPageChangeListener() {
			
			@Override
			public void onPageSelected(int arg0) {
				// TODO Auto-generated method stub
				if(arg0 == 0){
					onEmotionHistorySelected();
				}else{
					onEmotionSelected();
				}
			}
			
			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onPageScrollStateChanged(int arg0) {
				// TODO Auto-generated method stub
				
			}
		});
		
		vpEmotion.setCurrentItem(1);
	}
	
	private void initViews(View view){
		

		ivEmotionHistory = (ImageButton)view.findViewById(R.id.ivEmotionHistory);
		ivEmotion = (ImageButton)view.findViewById(R.id.ivEmotion);
		indicator = (View)view.findViewById(R.id.indicator);
		vpEmotion = (AbilitySwapablePageView)view.findViewById(R.id.vpEmotion);
		ivEmotionHistory.setOnClickListener(this);
		ivEmotion.setOnClickListener(this);
	}
	
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		super.onClick(v);
		switch (v.getId()) {
		case R.id.ivClose:
			break;
		case R.id.ivEmotionHistory:
			
			vpEmotion.setCurrentItem(0);
			onEmotionHistorySelected();
			break;
		case R.id.ivEmotion:
			
			vpEmotion.setCurrentItem(1);
			onEmotionSelected();
			break;
		default:
			break;
		}
	}
	
	
	private void onEmotionSelected(){
//		ivEmotion.setBackgroundColor(getResources().getColor(R.color.thin_grey));
//		ivEmotionHistory.setBackgroundColor(Color.TRANSPARENT);
		ivEmotion.setImageResource(R.drawable.ic_premium_emotion_blue_24dp);
		ivEmotionHistory.setImageResource(R.drawable.ic_history_grey600_24dp);
		RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)indicator.getLayoutParams();
		params.leftMargin = UnitConversion.dip2px(getActivity(), 48);
		indicator.setLayoutParams(params);
	}
	
	private void onEmotionHistorySelected(){
//		ivEmotionHistory.setBackgroundColor(getResources().getColor(R.color.thin_grey));
//		ivEmotion.setBackgroundColor(Color.TRANSPARENT);
		ivEmotion.setImageResource(R.drawable.ic_premium_emotion_24dp);
		ivEmotionHistory.setImageResource(R.drawable.ic_history_blue_24dp);
		
		RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)indicator.getLayoutParams();
		params.leftMargin = UnitConversion.dip2px(getActivity(), 0);
		indicator.setLayoutParams(params);
	}
	
	private OnItemClickCallback onItemClickCallback = new OnItemClickCallback(){

		@Override
		public void onItemClick() {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onItemLongClick() {
			// TODO Auto-generated method stub
			vpEmotion.setPagingEnabled(false);
			Log.v("paging", "false");
		}

		@Override
		public void onItemLongClickUp() {
			// TODO Auto-generated method stub
			vpEmotion.setPagingEnabled(true);
		}
		
	};
	
	class EmotionPagerAdapter extends FragmentPagerAdapter {

		private int count = 2;// 子页面数目
		private Fragment[] fragmentArr = new Fragment[2];

		public EmotionPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public int getCount() {
			return count;
		}

		public Fragment getFragment(int index){
			return fragmentArr[index];
		}
		
		@Override
		public Fragment getItem(int position) {
			if (fragmentArr[position] != null) {
				return fragmentArr[position];
			}
			Fragment fragment = null;
			switch (position) {
			case 0:// 我的好友
				fragment = new EmotionHistoryFragment();
				((EmotionHistoryFragment)fragment).setOnItemClickCallback(onItemClickCallback);
				break;
			case 1:// 匹配女士
				fragment = new NormalEmotionFragment();
				((NormalEmotionFragment)fragment).setOnItemClickCallback(onItemClickCallback);
				break;
			}
			fragmentArr[position] = fragment;
			return fragmentArr[position];
		}
	}


	
}
