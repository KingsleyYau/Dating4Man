package com.qpidnetwork.view;

import com.qpidnetwork.dating.R;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ChooseWebSiteDialog extends Dialog {
	
	private TextView textViewTitle;
	private LinearLayout layoutContent;
	private float density = this.getContext().getResources().getDisplayMetrics().density;
	
	public ChooseWebSiteDialog(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        this.setContentView(R.layout.dialog_choose_website);
        textViewTitle = (TextView)findViewById(R.id.textViewTitle);
        layoutContent = (LinearLayout) findViewById(R.id.layoutContent);
        
        Display display = this.getWindow().getWindowManager().getDefaultDisplay();
    	Point size = new Point();
    	
    	if (Build.VERSION.SDK_INT > 12){
    		display.getSize(size);
    	}else{
    		size.y = display.getHeight();
    		size.x = display.getWidth();
    	}
    	
    	
    	int width_times =  Math.round((float)size.x / (56.0f * density));
    	float dialog_width = ((float)(width_times - 1) * 56.0f * density);
    	
    	this.getWindow().setLayout((int)dialog_width, LayoutParams.WRAP_CONTENT);
        
	}
    
    public ChooseWebSiteDialog(Context context, int theme) {
        super(context, theme);
    }
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
    }
    
    public View AddItem(int imageId, String tips, String desc, boolean selected) {
    	LayoutInflater layoutInfalter = LayoutInflater.from(getContext());
    	View view = layoutInfalter.inflate(R.layout.layout_website_item, null);
    	layoutContent.addView(view, 
    			new LinearLayout.LayoutParams(
    					LinearLayout.LayoutParams.MATCH_PARENT,
    					LinearLayout.LayoutParams.MATCH_PARENT
    					)
    	);
    	
    	ImageView imageView = (ImageView) view.findViewById(R.id.imageView);
    	imageView.setImageResource(imageId);
    	TextView textViewName = (TextView) view.findViewById(R.id.textViewName);
    	textViewName.setText(tips);
    	TextView textViewDesc = (TextView) view.findViewById(R.id.textViewDesc);
    	textViewDesc.setText(desc);
    	ImageView location = (ImageView) view.findViewById(R.id.select_indicator);
    	
    	if (selected) {
    		location.setVisibility(View.VISIBLE);
    	}else{
    		location.setVisibility(View.GONE);
    	}
    	
    	
    	return view;
    }
    
}
