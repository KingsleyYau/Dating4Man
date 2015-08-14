package com.qpidnetwork.view;

import com.qpidnetwork.dating.R;
import com.qpidnetwork.view.CheckButtonNormal.OnCheckLinstener;
import com.qpidnetwork.view.RangeSeekBar.OnRangeSeekBarChangeListener;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class SearchOnlineLadyView extends LinearLayout {

	public interface OnSearchOnlineLadyViewCallback {
		public void OnClickSearch(View v);
		public void OnClickGo(View v);
		public void OnClickSelf(View v);
	}
	public OnSearchOnlineLadyViewCallback callback;
	
	public void SetOnSearchOnlineLadyViewCallback(OnSearchOnlineLadyViewCallback callback) {
		this.callback = callback;
	}
	
	public CheckButtonNormal buttonNoMatter;
	public CheckButtonNormal buttonOnline;
	public Button buttonSearch;
	public Button buttonGo;
	public MaterialTextField editTextId;
	
	public LinearLayout layoutAge;
	public RangeSeekBar<Integer> rangeSeekBar;
	public TextView textViewMin;
	public TextView textViewMax;
	
	public boolean mbOnline = true;
	public Integer miMin = 0;
	public Integer miMax = 99;
	
	public SearchOnlineLadyView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
		Init(context);
	}
	
	public SearchOnlineLadyView(Context context, AttributeSet attrs){
		super(context, attrs);
		// TODO Auto-generated constructor stub
		Init(context);
	}
	
	public void Init(Context context) {
		View view = LayoutInflater.from(context).inflate(R.layout.layout_online_search, this, true);
		
		buttonNoMatter = (CheckButtonNormal) view.findViewById(R.id.buttonNoMatter);
		buttonNoMatter.SetText("No matter");
		buttonNoMatter.SetOnCheckChangeListener(new OnCheckLinstener() {
			
			@Override
			public void onCheckedChange(boolean bChecked) {
				// TODO Auto-generated method stub
				mbOnline = bChecked;
				buttonOnline.SetChecked(!bChecked);
			}
		});
		
		if( !isInEditMode() ) {
			buttonNoMatter.SetChecked(true);
		}
		
		buttonOnline = (CheckButtonNormal) view.findViewById(R.id.buttonOnline);
		buttonOnline.SetText("Yes");
		buttonOnline.SetOnCheckChangeListener(new OnCheckLinstener() {
			
			@Override
			public void onCheckedChange(boolean bChecked) {
				// TODO Auto-generated method stub
				mbOnline = bChecked;
				buttonNoMatter.SetChecked(!bChecked);
			}
		});
		
		buttonSearch = (Button) view.findViewById(R.id.buttonSearch);
		buttonSearch.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if( callback != null ) {
					callback.OnClickSearch(v);
				}
			}
		});
		
		buttonGo = (Button) view.findViewById(R.id.buttonGo);
		buttonGo.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if( callback != null ) {
					callback.OnClickGo(v);
				}
			}
		});
		
		editTextId = (MaterialTextField) view.findViewById(R.id.editTextId);
		editTextId.setFocusedStateColor(context.getResources().getColor(R.color.blue_color));
		editTextId.setHint("Enter lady's profile ID");
		
		textViewMin = (TextView) view.findViewById(R.id.textViewMin);
		textViewMax = (TextView) view.findViewById(R.id.textViewMax);
		layoutAge = (LinearLayout) view.findViewById(R.id.layoutAge);
		rangeSeekBar = new RangeSeekBar<Integer>(
				0, 
				99, 
				context, 
				context.getResources().getColor(R.color.green)
				);
		rangeSeekBar.setLayoutParams(
				new LinearLayout.LayoutParams(
						LayoutParams.MATCH_PARENT,
						LayoutParams.MATCH_PARENT
						)
				);
		rangeSeekBar.setOnRangeSeekBarChangeListener(new OnRangeSeekBarChangeListener<Integer>() {
            @Override
            public void onRangeSeekBarValuesChanged(RangeSeekBar<?> bar, Integer minValue, Integer maxValue) {
                    // handle changed range values
            	miMin = minValue;
            	miMax = maxValue;
            	textViewMin.setText(String.valueOf(minValue));
            	textViewMax.setText(String.valueOf(maxValue));
            }
		});
		layoutAge.addView(rangeSeekBar);
		
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
	
	public String GetText() {
		return editTextId.getText().toString();
	}
}
