package com.qpidnetwork.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v7.widget.CardView;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.qpidnetwork.dating.R;

public class ButtonFloat extends CardView{
	
	private float desity = this.getContext().getResources().getDisplayMetrics().density;
	private int elevation = (int)(2.00 * desity);
	private int radius = (int)(0.00 * desity);
	
	private ImageView iconView;
	
	public ButtonFloat(Context context) {
		super(context);
	}
	
	public ButtonFloat(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		setThis(attrs);
	}
	
	public ButtonFloat(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		setThis(attrs);
	}
	
	private void setThis(AttributeSet attrs){
		
		int icon = R.drawable.ic_launcher;
		int background = 0;
		int touch_feedback = 0;
		
		
		if (attrs != null){
			TypedArray a = this.getContext().obtainStyledAttributes(attrs, R.styleable.RaisedButton);
			icon = a.getResourceId(R.styleable.RaisedButton_icon, 0);
			background = a.getColor(R.styleable.RaisedButton_background, 0);
			touch_feedback = a.getResourceId(R.styleable.RaisedButton_touch_feedback, 0);
			elevation  = (int)a.getDimension(R.styleable.RaisedButton_elevation, elevation);
			radius = (int)a.getDimension(R.styleable.RaisedButton_radius, radius);
			a.recycle();
		}
		
		this.setUseCompatPadding(true);
		this.setClickable(true);
		this.setCardElevation(elevation);
		this.setPreventCornerOverlap(false);
		this.setRadius(radius);
		this.addView(createButton(icon, touch_feedback));
		if (background != 0) {
			this.setCardBackgroundColor(background);
		}
		
		
	}

	private LinearLayout createButton(int iconResourceId, int touch){
		
		LinearLayout view = new LinearLayout(this.getContext());
		CardView.LayoutParams params = new CardView.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		params.gravity = Gravity.CENTER;
		view.setLayoutParams(params);
		view.setGravity(Gravity.CENTER);
		
		if( Build.VERSION.SDK_INT >= 17 ) {
			view.setOrientation(LinearLayout.HORIZONTAL);
		}
		
		iconView = new ImageView(this.getContext());
		LinearLayout.LayoutParams ic_params = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		iconView.setImageResource(iconResourceId);
		iconView.setLayoutParams(ic_params);
		
		

		
		view.addView(iconView);
		if (touch != 0) view.setBackgroundResource(touch);
		
		
		return view;
	}
	
	public void setIcon(int resourceId){
		if (iconView == null) return;
		iconView.setImageResource(resourceId);
	}
	
	public void setIcon(Drawable drawable){
		if (iconView == null) return;
		iconView.setImageDrawable(drawable);
	}
	
	public void setButtonBackground(int color){
		this.setCardBackgroundColor(color);
	}
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.getSize(heightMeasureSpec));
	}
	

}
