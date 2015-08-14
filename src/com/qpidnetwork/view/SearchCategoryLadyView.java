package com.qpidnetwork.view;

import com.qpidnetwork.dating.R;
import com.qpidnetwork.view.CheckButtonNormal.OnCheckLinstener;
import com.qpidnetwork.view.RangeSeekBar.OnRangeSeekBarChangeListener;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class SearchCategoryLadyView extends LinearLayout {

	public interface OnSearchCategoryLadyViewCallback {
		public void OnClickOnlineLady(View v);
		public void OnClickVideo(View v);
		public void OnClickCall(View v);
		public void OnClickNewest(View v);
		public void OnClickSelf(View v);
	}
	public OnSearchCategoryLadyViewCallback callback;
	
	public void SetOnSearchCategoryLadyViewCallback(OnSearchCategoryLadyViewCallback callback) {
		this.callback = callback;
	}
	
	public Button buttonOnlineLady;
	public Button buttonVideo;
	public Button buttonCall;
	public Button buttonNewest;
	
	public SearchCategoryLadyView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
		Init(context);
	}
	
	public SearchCategoryLadyView(Context context, AttributeSet attrs){
		super(context, attrs);
		// TODO Auto-generated constructor stub
		Init(context);
	}
	
	public void Init(Context context) {
		View view = LayoutInflater.from(context).inflate(R.layout.layout_online_category, this, true);
		
		buttonOnlineLady = (Button) view.findViewById(R.id.buttonOnlineLady);
		buttonOnlineLady.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if( callback != null ) {
					callback.OnClickOnlineLady(v);
				}
			}
		});
		
		buttonVideo = (Button) view.findViewById(R.id.buttonVideo);
		buttonVideo.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if( callback != null ) {
					callback.OnClickVideo(v);
				}
			}
		});
		
		buttonCall = (Button) view.findViewById(R.id.buttonCall);
		buttonCall.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if( callback != null ) {
					callback.OnClickCall(v);
				}
			}
		});
		
		buttonNewest = (Button) view.findViewById(R.id.buttonNewest);
		buttonNewest.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if( callback != null ) {
					callback.OnClickNewest(v);
				}
			}
		});
		
		setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if( callback != null ) {
					callback.OnClickSelf(v);
				}
			}
		});
	}
}
