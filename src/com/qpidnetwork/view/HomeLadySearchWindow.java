package com.qpidnetwork.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.qpidnetwork.dating.R;
import com.qpidnetwork.framework.util.UnitConversion;
import com.qpidnetwork.view.CheckButtonNormal.OnCheckLinstener;
import com.qpidnetwork.view.RangeSeekBar.OnRangeSeekBarChangeListener;

public class HomeLadySearchWindow extends PopupWindow implements OnTouchListener{
	
	private static int defaultHeight = LayoutParams.WRAP_CONTENT;
	private static int defaultWidth = LayoutParams.MATCH_PARENT;
	
	
	private Context context;
	public View contentView;
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

		this.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
		this.setHeight(defaultHeight);
		this.setWidth(defaultWidth);

		if (Build.VERSION.SDK_INT < 21){
			this.setTouchable(true);
			this.setAnimationStyle(R.style.DropDownListAnimation);	
		}else{
			this.setOutsideTouchable(true);
			this.setTouchInterceptor(this);
		}
		
	}
	
	
	
	
	public View createContentView(){
		contentView = LayoutInflater.from(context).inflate(R.layout.layout_online_search, null);
		
		buttonNoMatter = (CheckButtonNormal) contentView.findViewById(R.id.buttonNoMatter);
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
		
		
		
		buttonOnline = (CheckButtonNormal) contentView.findViewById(R.id.buttonOnline);
		buttonOnline.SetOnCheckChangeListener(new OnCheckLinstener() {
			
			@Override
			public void onCheckedChange(boolean bChecked) {
				// TODO Auto-generated method stub
				mbOnline = bChecked;
				buttonNoMatter.SetChecked(!bChecked);
				//buttonOnline.setBackgroundResource(R.drawable.round_right_rect_green);
			}
		});
		

		buttonSearch = (ButtonRaised) contentView.findViewById(R.id.buttonSearch);
		buttonSearch.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				doAnimationalDismiss();
				if( callback != null ) {
					callback.OnClickSearch(v, miMin, miMax, mbOnline);
				}
			}
		});
		
		buttonNoMatter.SetChecked(true);
		buttonOnline.SetText(context.getResources().getString(R.string.common_btn_yes));
		buttonNoMatter.setBackgroundResource(R.drawable.round_left_rect_green);
		
		buttonGo = (ButtonRaised) contentView.findViewById(R.id.buttonGo);
		buttonGo.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				
				if (editTextId.getText().toString().length() == 0){
					editTextId.setError(Color.RED, true);
					return;
				}
				
				doAnimationalDismiss();
				if( callback != null ) {
					callback.OnClickGo(v, editTextId.getText().toString());
				}
				
				editTextId.setText("");
			}
		});
		
		editTextId = (MaterialTextField) contentView.findViewById(R.id.editTextId);
		editTextId.setHint(context.getString(R.string.ladys_id));
		
		textViewMin = (TextView) contentView.findViewById(R.id.textViewMin);
		textViewMax = (TextView) contentView.findViewById(R.id.textViewMax);
		layoutAge = (LinearLayout) contentView.findViewById(R.id.layoutAge);
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
		
		return contentView;
	}
	
	
	private void setCircularRevealAnimation(final View view){

		int initialRadius = view.getWidth();
		Animator anim = ViewAnimationUtils.createCircularReveal(view, view.getRight() - UnitConversion.dip2px(context, 72), 0, 0, initialRadius);
		anim.setDuration(220);
		contentView.setVisibility(View.VISIBLE);
		anim.start();
	}
	
	public void setCallback(Callback callback){
		this.callback = callback;
	}
	
	@Override
	public void showAsDropDown(View anchor){
		super.showAsDropDown(anchor);
		if (Build.VERSION.SDK_INT < 21 ) return;
		
		contentView.setVisibility(View.INVISIBLE);
		contentView.postDelayed(new Runnable(){

			@Override
			public void run() {
				// TODO Auto-generated method stub
				setCircularRevealAnimation(contentView);
			}
			
		}, 100);
		
	
	}

	public void doAnimationalDismiss() {
		// TODO Auto-generated method stub
		if (Build.VERSION.SDK_INT < 21){
			dismiss();
			return;
		}
		
		int initialRadius = contentView.getWidth();
		Animator anim = ViewAnimationUtils.createCircularReveal(contentView, contentView.getRight() - UnitConversion.dip2px(context, 72), 0, initialRadius, 0);
		anim.setDuration(220);
		anim.addListener(new AnimatorListenerAdapter() {
		    @Override
		    public void onAnimationEnd(Animator animation) {
		        super.onAnimationEnd(animation);
		        dismiss();
		    }
		});
		
		anim.start();
	}



	@SuppressLint("ClickableViewAccessibility")
	@Override
	public boolean onTouch(View v, MotionEvent event) {
		// TODO Auto-generated method stub
		if (event.getAction() == MotionEvent.ACTION_DOWN){
			if (event.getX() < 0 || event.getY() < 0 || event.getY() > contentView.getHeight()){
				doAnimationalDismiss();
				return true;
			}
		}
		
		return false;
	}

}
