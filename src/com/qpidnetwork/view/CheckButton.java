package com.qpidnetwork.view;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.qpidnetwork.dating.R;

public class CheckButton extends RelativeLayout implements View.OnClickListener {
	
	public interface OnCheckLinstener {
		public void onCheckedChange(View v, boolean bChecked);
	}
	
	
	private TextView textView = null;
	private ImageView imageView = null;
	
	private boolean mChecked = false;
	private OnCheckLinstener mListener = null;
	
	public CheckButton(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
		
        LayoutInflater.from(context).inflate(R.layout.checkbutton, this, true);
        textView = (TextView) findViewById(R.id.textView);
        imageView = (ImageView) findViewById(R.id.imageView);
        SetChecked(false);
        setOnClickListener(this);
	}
	
    public CheckButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        
        LayoutInflater.from(context).inflate(R.layout.checkbutton, this, true);
        textView = (TextView) findViewById(R.id.textView);
        imageView = (ImageView) findViewById(R.id.imageView);
        SetChecked(false);
        setOnClickListener(this);
    }
    
    public void SetOnCheckChangeListener(OnCheckLinstener listener) {
    	mListener = listener;
    }
    
    public void SetChecked(boolean bChecked) {
    	mChecked = bChecked;
    	
    	if( mChecked ) {
    		textView.setTextColor(Color.WHITE);
    		imageView.setVisibility(View.VISIBLE);
    		setBackgroundResource(R.drawable.radius_angle_grey_stroke_grey_bg_2dp);
    	} else {
    		textView.setTextColor(getContext().getResources().getColor(R.color.text_color_dark));
    		setBackgroundResource(R.drawable.radius_angle_grey_stroke_white_bg_2dp);
    		imageView.setVisibility(View.INVISIBLE);
    	}
    	
    }
    
    private void SetCheckedAndCallback(boolean bChecked) {
    	boolean bFlag = !( mChecked == bChecked);
    	mChecked = bChecked;
    	
    	if( mChecked ) {
    		textView.setTextColor(Color.WHITE);
    		imageView.setVisibility(View.VISIBLE);
    		setBackgroundResource(R.drawable.radius_angle_grey_stroke_grey_bg_2dp);
    	} else {
    		textView.setTextColor(getContext().getResources().getColor(R.color.text_color_dark));
    		setBackgroundResource(R.drawable.radius_angle_grey_stroke_white_bg_2dp);
    		imageView.setVisibility(View.INVISIBLE);
    	}
    	
    	if( bFlag && mListener != null ) {
    		mListener.onCheckedChange(this, mChecked);
    	}
    	
    }
    
    public boolean IsChecked() {
    	return mChecked;
    }
    
    public void SetText(String text) {
    	textView.setText(text);
    }

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		SetCheckedAndCallback(!mChecked);
	}
}
