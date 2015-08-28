package com.qpidnetwork.view;

/**
 * Author: Martin Shum
 * 
 * This view is structured by google material design
 * 
 * Set icon
 * Set Title
 * Set Message
 * Add button
 * 
 * If you have any questions please read the code 
 * through to find out the answer (Bazinga!).
 * 
 */


import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

import com.qpidnetwork.dating.R;
import com.qpidnetwork.framework.base.BaseDialog;
import com.qpidnetwork.view.RangeSeekBar.OnRangeSeekBarChangeListener;

@SuppressLint("RtlHardcoded")
public class MaterialDialogNumberRangeChooser extends BaseDialog{
	
	
	public static int DIALOG_MIN_WIDTH = 280;
	
	
	private LinearLayout contentView;
	private int view_padding = 24;
	private int view_margin = 24;
	private float density = this.getContext().getResources().getDisplayMetrics().density;
	private OnClickCallback mCallback;
	private int[] range = new int[]{0, 99};
	
	

	
	public MaterialDialogNumberRangeChooser(Context context, OnClickCallback callback, int[] number_range){
		this(context, callback);
		range = number_range;
		createView();
	}
	
	public MaterialDialogNumberRangeChooser(Context context, OnClickCallback callback){
		this(context);
		mCallback = callback;
		createView();
	}
	
	public MaterialDialogNumberRangeChooser(Context context) {
		super(context);

    	
		this.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        
	}



	protected void createView() {

    	//setup content view
		DIALOG_MIN_WIDTH = (int)(280.0f * density);
		view_padding = (int)(24.0f * density);
		view_margin = (int)(20.0f * density);
		
		contentView = new LinearLayout(this.getContext());
		contentView.setMinimumWidth(DIALOG_MIN_WIDTH);
		contentView.setOrientation(LinearLayout.VERTICAL);
		contentView.setBackgroundResource(R.drawable.rectangle_rounded_angle_white_bg);
		this.setContentView(contentView);
		
		LinearLayout holderView = new LinearLayout(this.getContext());
		LayoutParams holderParams = new LayoutParams(this.getDialogSize(), LayoutParams.WRAP_CONTENT);
		holderView.setLayoutParams(holderParams);
		holderView.setGravity(Gravity.TOP | Gravity.CENTER);
		holderView.setOrientation(LinearLayout.VERTICAL);
		holderView.setPadding(view_padding, view_padding, view_padding, view_padding);
		

        //title
        LayoutParams titleParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        
        titleParams.setMargins(0, 0, 0, view_margin);
        TextView title = new TextView(this.getContext());
        title.setLayoutParams(titleParams);
        title.setTextColor(this.getContext().getResources().getColor(R.color.text_color_dark));
        title.setTextSize(18);
        title.setTypeface(null, Typeface.BOLD);
        title.setId(android.R.id.title);
        title.setVisibility(View.GONE);
    
        
        //button area
        LayoutParams btnsParams = new LayoutParams(LayoutParams.MATCH_PARENT, (int)(52.0f * density));
        LinearLayout buttons = new LinearLayout(this.getContext());
        buttons.setLayoutParams(btnsParams);
        buttons.setGravity(Gravity.RIGHT | Gravity.CENTER);
        buttons.setId(android.R.id.extractArea);
        buttons.setPadding(view_padding, 0, (int)(8.0f * density), 0);
        buttons.addView(createButton(this.getContext().getString(R.string.common_btn_ok), new View.OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (mCallback != null) mCallback.onClick(v, range);
				dismiss();
			}
        	
        }));
        
        
        LinearLayout rangebar = createRangeView(density);
        
        holderView.addView(title);
        holderView.addView(rangebar);
        
        contentView.addView(holderView);
        contentView.addView(buttons);
        
    }
    
    private LinearLayout createRangeView(float dst){
    	
    	int H = (int)(48.0f * dst);
    	int TW = (int)(32 * dst);
    	LinearLayout.LayoutParams vp = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, H);
    	vp.bottomMargin = (int)(20.0f * dst);
    	LinearLayout v = new LinearLayout(this.getContext());
    	v.setLayoutParams(vp);
    	v.setOrientation(LinearLayout.HORIZONTAL);
    	v.setGravity(Gravity.LEFT | Gravity.CENTER);
    	
    	final TextView leftValue = new TextView(this.getContext());
    	LinearLayout.LayoutParams ltp = new LinearLayout.LayoutParams(TW, LayoutParams.WRAP_CONTENT);
    	ltp.gravity = Gravity.LEFT | Gravity.CENTER;
    	leftValue.setTextSize(16);
    	leftValue.setTextColor(this.getContext().getResources().getColor(R.color.text_color_grey));
    	leftValue.setText("0");
    	leftValue.setLayoutParams(ltp);
    	leftValue.setText(range[0] + "");
    	leftValue.setId(android.R.id.text1);
    	
    	final TextView rightValue = new TextView(this.getContext());
    	LinearLayout.LayoutParams rtp = new LinearLayout.LayoutParams(TW, LayoutParams.WRAP_CONTENT);
    	rtp.gravity = Gravity.RIGHT | Gravity.CENTER;
    	rightValue.setLayoutParams(rtp);
    	rightValue.setTextSize(16);
    	rightValue.setTextColor(this.getContext().getResources().getColor(R.color.text_color_grey));
    	rightValue.setText("0");
    	rightValue.setGravity(Gravity.RIGHT | Gravity.CENTER);
    	rightValue.setText(range[1] + "");
    	rightValue.setId(android.R.id.text2);
    	
    	LinearLayout.LayoutParams stp = new LinearLayout.LayoutParams( LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
    	stp.weight = 1;
    	RangeSeekBar<Integer> seekbar = new RangeSeekBar<Integer>(range[0], range[1], this.getContext(), 
    			this.getContext().getResources().getColor(R.color.green));
    	seekbar.setId(android.R.id.custom);
    	seekbar.setLayoutParams(stp);
        
        seekbar.setOnRangeSeekBarChangeListener(new OnRangeSeekBarChangeListener<Integer>() {
                @Override
                public void onRangeSeekBarValuesChanged(RangeSeekBar<?> bar, Integer minValue, Integer maxValue) {
                        // handle changed range values
                	range[0] = minValue;
                	range[1] = maxValue;
                	leftValue.setText(String.valueOf(minValue));
                	rightValue.setText(String.valueOf(maxValue));
                }
        });
        
        v.addView(leftValue);
        v.addView(seekbar);
        v.addView(rightValue);
        
        return v;
    	
    }
    
    
    public Button createButton(CharSequence text, View.OnClickListener click){
    	
    	float density = this.getContext().getResources().getDisplayMetrics().density;
    	int button_height = (int)(36.0f * density);
    	int button_margin = (int)(4.0f * density);
    	int button_padding = (int)(8.0f * density);
    	int min_width = (int)(64.0f * density);
    	
    	LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT, button_height);
    	params.setMargins(button_margin, 0, 0, 0);
    	
    	Button button = new Button(this.getContext());
    	button.setLayoutParams(params);
    	button.setPadding(button_padding, 0, button_padding, 0);
    	button.setBackgroundResource(R.drawable.touch_feedback_holo_light_round_rectangle);
    	button.setTextColor(this.getContext().getResources().getColor(R.color.blue_color));
    	button.setTextSize(16);
    	button.setMinWidth(min_width);
    	button.setOnClickListener(click);
    	button.setText(text);
    	button.setTypeface(null, Typeface.BOLD);
    	
    	return button;
    	
    	
    }
    
    public interface OnClickCallback{
    	public void onClick (View v, int[] range);
    }
    
    
    public LinearLayout getContentView(){
    	return contentView;
    }
    
    public void setTitle(CharSequence title){
    	getTitle().setText(title);
    	getTitle().setVisibility(View.VISIBLE);
    }
    
    public TextView getTitle(){
    	return (TextView)contentView.findViewById(android.R.id.title);
    }
    
    public void setSelectRange(int[] rge){
    	getSeekBar().setSelectedMinValue(rge[0]);
    	getSeekBar().setSelectedMaxValue(rge[1]);
    	getLeftText().setText(rge[0] + "");
    	getRightText().setText(rge[1] + "");
    }
    
    public TextView getLeftText(){
    	return (TextView)contentView.findViewById(android.R.id.text1);
    }
    
    public TextView getRightText(){
    	return (TextView)contentView.findViewById(android.R.id.text2);
    }
    
    @SuppressWarnings("unchecked")
	public RangeSeekBar<Integer> getSeekBar(){
    	return (RangeSeekBar<Integer>)contentView.findViewById(android.R.id.custom);
    }
    

    public void addButton(Button button){
    	LinearLayout v = (LinearLayout)contentView.findViewById(android.R.id.extractArea);
    	v.addView(button);
    }
    

}

	
	
	
