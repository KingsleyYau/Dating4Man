package com.qpidnetwork.view;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.qpidnetwork.dating.R;

public class CheckButtonNormal extends RelativeLayout implements View.OnClickListener {
	
	public interface OnCheckLinstener {
		public void onCheckedChange(boolean bChecked);
	}
	
	
	private TextView textView = null;
	
	private boolean mChecked = false;
	private OnCheckLinstener mListener = null;
	
	public CheckButtonNormal(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
		Init();
	}
	
    public CheckButtonNormal(Context context, AttributeSet attrs) {
        super(context, attrs);
        Init();
    }
    
    public void Init() {
    	LayoutInflater.from(getContext()).inflate(R.layout.checkbutton_normal, this, true);
        textView = (TextView) findViewById(R.id.textView);
		textView.setTextColor(getContext().getResources().getColor(R.color.text_color_dark));
		setOnClickListener(this);
    }
    
    public void SetOnCheckChangeListener(OnCheckLinstener listener) {
    	mListener = listener;
    }
    
    public void SetChecked(boolean bChecked) {
    	mChecked = bChecked;
    	
    	if( mChecked ) {
    		textView.setTextColor(Color.WHITE);
    		setBackgroundResource(R.drawable.round_right_rect_green);
    	} else {
    		textView.setTextColor(getContext().getResources().getColor(R.color.text_color_dark));
    		setBackgroundColor(Color.TRANSPARENT);
    	}
    	
    }
    
    private void SetCheckedAndCallback(boolean bChecked) {
    	boolean bFlag = !( mChecked == bChecked);
    	mChecked = bChecked;
    	
    	if( mChecked ) {
    		textView.setTextColor(Color.WHITE);
    		setBackgroundResource(R.drawable.round_right_rect_green);
    	} else {
    		textView.setTextColor(getContext().getResources().getColor(R.color.text_color_dark));
    		setBackgroundColor(Color.TRANSPARENT);
    	}
    	
    	if( bFlag && mListener != null ) {
    		mListener.onCheckedChange(mChecked);
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
