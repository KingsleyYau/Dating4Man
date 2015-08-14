package com.qpidnetwork.view;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.qpidnetwork.dating.R;
import com.qpidnetwork.framework.util.Log;

/**
 * 可伸缩tabbar
 * @author Max.Chiu
 *
 */
public class TitleTabBar extends LinearLayout implements View.OnClickListener {
	
	public interface TitleTabBarListener {
		void onTabSelected(int index);
	}
	
	private LayoutInflater mInflater = null;
	private ArrayList<View> mTabs = new ArrayList<View>();
	private int mIndex = 0;
	private LinearLayout mContainer;

	private TitleTabBarListener mListener = null;
	
	public TitleTabBar(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
		mInflater = LayoutInflater.from(context);
		init(context);
	}
	
    public TitleTabBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        mInflater = LayoutInflater.from(context);
        init(context);
    }
    
    private void init(Context context){
    	
    	
    	
    	setOrientation(LinearLayout.VERTICAL);
        
        mContainer = new LinearLayout(context);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
        		LinearLayout.LayoutParams.WRAP_CONTENT,
        		(int)getResources().getDimension(R.dimen.actionbar_height));
        
		addView(mContainer, lp);
		

    }

    /**
     * 设置回调
     * @param listener
     */
    public void SetListener(TitleTabBarListener listener) {
    	mListener = listener;
    }
    
    /**
     * 增加一个选项
     * @param index		选项下标，不能重复
     * @param title		选项标题
     * @param image		选项图片
     */
	public void AddTab(int index, String title, Drawable image) {
		Log.d("TitleTabBar", "AddTab( index : " + index + " )");
		if( index < 0 ) {
			return;
		}
		
		
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, (int)getResources().getDimension(R.dimen.actionbar_height));
		View view = mInflater.inflate(R.layout.tabbar_item, null);
		view.setTag(index);
		view.setOnClickListener(this);
		view.setLayoutParams(params);
		
		ImageView imageView = (ImageView) view.findViewById(R.id.imageView);
		imageView.setImageDrawable(image);
		
		TextView textViewBadge = (TextView) view.findViewById(R.id.textViewBadge);
		textViewBadge.setVisibility(View.INVISIBLE);

		TextView textView = (TextView) view.findViewById(R.id.textView);
		textView.setText(title);

		mContainer.addView(view);
		mTabs.add(view);
	}
	
	/**
	 * 选择指定选项
	 * @param index		选项下标
	 */
	public void SelectTab(int index) {
		Log.d("TitleTabBar", "SelectTab( index : " + index + " )");
		
		if( index < 0 ) {
			return;
		}
		
		boolean bFlag = (mIndex != index);
		
		View view = null;
		TextView textView = null;
		View indicator = null;
//		TextView textViewBadge = null;
		
		for(int i = 0; i < mTabs.size(); i++) {
			view = mTabs.get(i);
			textView = (TextView) view.findViewById(R.id.textView);
			indicator = view.findViewById(R.id.indicator);
//			textViewBadge = (TextView) view.findViewById(R.id.textViewBadge);
			if( index != i ) {
				textView.setVisibility(View.GONE);
				indicator.setVisibility(View.INVISIBLE);
			} else {
				textView.setVisibility(View.VISIBLE);
				indicator.setVisibility(View.VISIBLE);
				mIndex = index;
				Log.d("TitleTabBar", "SelectTab( index : " + index + " ok )");
			}
		}
		
		if( mListener != null && bFlag ) {
			mListener.onTabSelected(mIndex);
		}
		


	}
	
	public void SetTabBadge(int index, String tips) {
		Log.d("TitleTabBar", "SetTabBadge( index : " + index + " )");
		if( index < 0 ) {
			return;
		}
		
		View view = mTabs.get(index);
		TextView textViewBadge = (TextView) view.findViewById(R.id.textViewBadge);
		
		if( tips.length() > 0 ) {
			textViewBadge.setVisibility(View.VISIBLE);
		} else {
			textViewBadge.setVisibility(View.INVISIBLE);
		}
		textViewBadge.setText(tips);
		
	}
	

	

	@Override
	public void onLayout(boolean changed, int l, int t, int r, int b){
		super.onLayout(changed, l, t, r, b);
	}
	
	@Override
	public void onClick(View v) {
		SelectTab(Integer.parseInt(v.getTag().toString()));
	}

}
