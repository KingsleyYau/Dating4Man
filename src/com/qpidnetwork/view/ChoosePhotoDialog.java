package com.qpidnetwork.view;

import com.qpidnetwork.dating.R;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

public class ChoosePhotoDialog extends Dialog implements View.OnClickListener{
	
	private ImageView ivPhoto;
	private TextView tvTitle;
	private TextView tvTitleTips;
	private TextView textViewTakePhoto;
	private TextView textViewSelectExisting;
	private TextView textViewCancel;
	
	private View.OnClickListener mTakePhotoListener;
	private View.OnClickListener mSelectPhotoListener;
	private View.OnClickListener mCancelListener;
	
	
	public ChoosePhotoDialog(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}
    
    public ChoosePhotoDialog(Context context, int theme) {
        super(context, theme);
    }
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.dialog_choose_photo);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        ivPhoto = (ImageView)findViewById(R.id.imageView);
        tvTitle = (TextView)findViewById(R.id.textView);
        tvTitleTips = (TextView)findViewById(R.id.textViewTips);
        
    	textViewTakePhoto = (TextView) findViewById(R.id.textViewTakePhoto);
    	textViewSelectExisting = (TextView) findViewById(R.id.textViewSelectExisting);;
    	textViewCancel = (TextView) findViewById(R.id.textViewCancel);
    	textViewTakePhoto.setOnClickListener(this);
    	textViewSelectExisting.setOnClickListener(this);
    	textViewCancel.setOnClickListener(this);
    }
    
    public void setOnTakePhotoClickListerner(View.OnClickListener listener){
    	mTakePhotoListener = listener;
    }
    
    public void setOnSelectPhotoClickListerner(View.OnClickListener listener){
    	mSelectPhotoListener = listener;
    }
    
    public void setOnCancelClickListerner(View.OnClickListener listener){
    	mCancelListener = listener;
    }

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		dismiss();
		switch (v.getId()) {
		case R.id.textViewTakePhoto:
			if(mTakePhotoListener != null){
				mTakePhotoListener.onClick(v);
			}
			break;
		case R.id.textViewSelectExisting:
			if(mSelectPhotoListener != null){
				mSelectPhotoListener.onClick(v);
			}
			break;
		case R.id.textViewCancel:
			if(mCancelListener != null){
				mCancelListener.onClick(v);
			}
			break;
		}
	}
}
