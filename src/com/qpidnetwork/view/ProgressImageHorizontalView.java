package com.qpidnetwork.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.qpidnetwork.dating.R;

public class ProgressImageHorizontalView extends RelativeLayout{

	public MaterialProgressBar progressBar = null;
	public ImageView imageView = null;
	public ImageView imageViewPlay = null;
	
	public LinearLayout layoutTips = null;
	public TextView textView = null;
	public ImageButton buttonCancel = null;

	public ProgressImageHorizontalView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
		LayoutInflater.from(context).inflate(
				R.layout.progress_image_horizontal_layout, this, true);
		init();
	}

	public ProgressImageHorizontalView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		LayoutInflater.from(context).inflate(
				R.layout.progress_image_horizontal_layout, this, true);
		init();
	}

	private void init() {
		progressBar = (MaterialProgressBar) findViewById(R.id.progressBar);
		
		
		imageView = (ImageView) findViewById(R.id.imageView);
		imageViewPlay = (ImageView) findViewById(R.id.imageViewPlay);
		layoutTips = (LinearLayout) findViewById(R.id.layoutTips);
		textView = (TextView) findViewById(R.id.textView);
		buttonCancel = (ImageButton) findViewById(R.id.buttonCancel);
//		buttonCancel.setBackgroundResource(R.drawable.u211);
	}
}
