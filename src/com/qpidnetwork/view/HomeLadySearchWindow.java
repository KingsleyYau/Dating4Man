package com.qpidnetwork.view;

import com.qpidnetwork.dating.R;
import com.qpidnetwork.request.RequestJniLady.OnlineType;
import com.qpidnetwork.view.CheckButtonNormal.OnCheckLinstener;
import com.qpidnetwork.view.RangeSeekBar.OnRangeSeekBarChangeListener;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Vibrator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

public class HomeLadySearchWindow extends PopupWindow{
	
	private static int defaultHeight = LayoutParams.WRAP_CONTENT;
	private static int defaultWidth = LayoutParams.MATCH_PARENT;
	
	private Context context;
	public CheckButtonNormal buttonNoMatter;
	public CheckButtonNormal buttonOnline;
	public ButtonRaised buttonSearch;
	public ButtonRaised buttonGo;
	public MaterialTextField editTextId;
	public Callback callback;
	
	public LinearLayout layoutAge;
	public RangeSeekBar<Integer> rangeSeekBar;
	public TextView textViewMin;
	public TextView textViewMax;
	
	public boolean mbOnline = false;
	public Integer miMin = 0;
	public Integer miMax = 99;
	
	public interface Callback {
		public void OnClickSearch(View v, int minAge, int maxAge, boolean isOnline);
		public void OnClickGo(View v, String ladyId);
	}
	
	
	public HomeLadySearchWindow(Context context){
		super(context);
		this.context = context;
		this.setContentView(createContentView());
		this.setFocusable(true);
		this.setTouchable(true);
		this.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
		this.setHeight(defaultHeight);
		this.setWidth(defaultWidth);
		this.setAnimationStyle(R.style.DropDownListAnimation);
		
	}
	
	
	
	
	public View createContentView(){
		View view = LayoutInflater.from(context).inflate(R.layout.layout_online_search, null);
		
		buttonNoMatter = (CheckButtonNormal) view.findViewById(R.id.buttonNoMatter);
		buttonNoMatter.SetText("No matter");
		buttonNoMatter.SetOnCheckChangeListener(new OnCheckLinstener() {
			
			@Override
			public void onCheckedChange(boolean bChecked) {
				// TODO Auto-generated method stub
				mbOnline = !bChecked;
				buttonOnline.SetChecked(!bChecked);
				//buttonNoMatter.setBackgroundResource(R.drawable.round_left_rect_green);
			}
		});
		
		
		
		buttonOnline = (CheckButtonNormal) view.findViewById(R.id.buttonOnline);
		buttonOnline.SetOnCheckChangeListener(new OnCheckLinstener() {
			
			@Override
			public void onCheckedChange(boolean bChecked) {
				// TODO Auto-generated method stub
				mbOnline = bChecked;
				buttonNoMatter.SetChecked(!bChecked);
				//buttonOnline.setBackgroundResource(R.drawable.round_right_rect_green);
			}
		});
		

		buttonSearch = (ButtonRaised) view.findViewById(R.id.buttonSearch);
		buttonSearch.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				dismiss();
				if( callback != null ) {
					callback.OnClickSearch(v, miMin, miMax, mbOnline);
				}
			}
		});
		
		buttonNoMatter.SetChecked(true);
		buttonOnline.SetText(context.getResources().getString(R.string.common_btn_yes));
		buttonNoMatter.setBackgroundResource(R.drawable.round_left_rect_green);
		
		buttonGo = (ButtonRaised) view.findViewById(R.id.buttonGo);
		buttonGo.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				
				if (editTextId.getText().toString().length() == 0){
					editTextId.setError(Color.RED, true);
					return;
				}
				
				dismiss();
				if( callback != null ) {
					callback.OnClickGo(v, editTextId.getText().toString());
				}
				
				editTextId.setText("");
			}
		});
		
		editTextId = (MaterialTextField) view.findViewById(R.id.editTextId);
		editTextId.setHint(context.getString(R.string.ladys_id));
		
		textViewMin = (TextView) view.findViewById(R.id.textViewMin);
		textViewMax = (TextView) view.findViewById(R.id.textViewMax);
		layoutAge = (LinearLayout) view.findViewById(R.id.layoutAge);
		rangeSeekBar = new RangeSeekBar<Integer>(
				18, 
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
		
		
		return view;
	}
	
	public void setCallback(Callback callback){
		this.callback = callback;
	}
	

	

}
