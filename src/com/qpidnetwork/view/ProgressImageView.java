package com.qpidnetwork.view;

import com.qpidnetwork.dating.R;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

public class ProgressImageView extends RelativeLayout {

	public View progressBar = null;
	public ImageView imageView = null;
	
	public ProgressImageView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
		 LayoutInflater.from(context).inflate(R.layout.progress_image_layout, this, true);
		 progressBar = findViewById(R.id.progressBar);
		 imageView = (ImageView) findViewById(R.id.imageView);
	}
	
	public ProgressImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		 LayoutInflater.from(context).inflate(R.layout.progress_image_layout, this, true);
		 progressBar = findViewById(R.id.progressBar);
		 imageView = (ImageView) findViewById(R.id.imageView);
	}

}
