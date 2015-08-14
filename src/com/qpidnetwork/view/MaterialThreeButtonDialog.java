package com.qpidnetwork.view;

import com.qpidnetwork.dating.R;
import com.qpidnetwork.framework.base.BaseDialog;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class MaterialThreeButtonDialog extends BaseDialog implements View.OnClickListener{
	
	private View contentView;
	private ImageView ivPhoto;
	private TextView tvTitle;
	private TextView tvTitleTips;
	private TextView textViewTakePhoto;
	private TextView textViewSelectExisting;
	private TextView textViewCancel;
	private OnClickCallback callback;
    
    public MaterialThreeButtonDialog(Context context, OnClickCallback callback) {
        super(context);
        contentView = LayoutInflater.from(context).inflate(R.layout.dialog_choose_photo, null);
        contentView.findViewById(R.id.masterView).getLayoutParams().width = this.getDialogSize();
        
        this.setContentView(contentView);
        
        this.callback = callback;
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
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);

    }
    

	
	public void setFirstButtonText(CharSequence text){
		getFirstButton().setText(text);
	}
	
	public void setSecondButtonText(CharSequence text){
		getSecondButton().setText(text);
	}
	
	public TextView getFirstButton(){
		return textViewTakePhoto;
	} 
	
	public TextView getSecondButton(){
		return textViewSelectExisting;
	}
	
	public TextView getCancelButton(){
		return textViewCancel;
	}
	
	
	public void hideImageView(){
		getImageView().setVisibility(View.GONE);
	}
	
	public ImageView getImageView(){
		return ivPhoto;
	}
	
	public void setTitle(CharSequence text){
		getTitle().setText(text);
	}
	
	public void setMessage(CharSequence text){
		getMessage().setText(text);
	}
	
	public TextView getTitle(){
		return tvTitle;
	}
	
	public TextView getMessage(){
		return tvTitleTips;
	}
	
	
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		dismiss();
		
		switch (v.getId()){
		case R.id.textViewTakePhoto:
			if (callback != null) callback.OnFirstButtonClick(v);
			break;
		case R.id.textViewSelectExisting:
			if (callback != null) callback.OnSecondButtonClick(v);
			break;
		case R.id.textViewCancel:
			if (callback != null) callback.OnCancelButtonClick(v);
			break;
			
		}
		
	}
	
	public interface OnClickCallback{
		public void OnFirstButtonClick(View v);
		public void OnSecondButtonClick(View v);
		public void OnCancelButtonClick(View v);
	}
	
	
}
