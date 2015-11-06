package com.qpidnetwork.dating.contactus;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RelativeLayout;

import com.qpidnetwork.dating.R;
import com.qpidnetwork.framework.base.BaseFragmentActivity;
import com.qpidnetwork.framework.util.StringUtil;
import com.qpidnetwork.framework.util.UnitConversion;
import com.qpidnetwork.manager.FileCacheManager;
import com.qpidnetwork.tool.ImageViewLoader;
import com.qpidnetwork.tool.ImageViewLoader.ImageViewLoaderCallback;
import com.qpidnetwork.view.MaterialProgressBar;
import com.qpidnetwork.view.TouchImageView;
import com.qpidnetwork.view.ViewTools;

public class AttachmentPhotoPreview extends BaseFragmentActivity implements ImageViewLoaderCallback{
	
	private static final String PHOTO_URL = "photoUrl";
	
	private static final int DOWNLOAD_PHOTO_SUCCESS = 0;
	private static final int DOWNLOAD_PHOTO_FAILED = 1;
	
	private ImageButton buttonCancel;
	private TouchImageView imageView;
	private MaterialProgressBar progress;
	
	private String photoUrl;
	
	public static void launchPhotoPreview(Context context, String photoUrl){
		Intent intent = new Intent(context, AttachmentPhotoPreview.class);
		intent.putExtra(PHOTO_URL, photoUrl);
		context.startActivity(intent);
	}
	
	@Override
	protected void onCreate(Bundle arg0) {
		// TODO Auto-generated method stub
		super.onCreate(arg0);
		setContentView(R.layout.activity_contact_us_photo_preview);
		initViews();
		initData();
	}
	
	private void initViews(){
		this.getWindow().setBackgroundDrawable(new ColorDrawable(Color.BLACK));
		
		buttonCancel = (ImageButton)findViewById(R.id.buttonCancel);
		imageView = (TouchImageView)findViewById(R.id.imageView);
		progress = (MaterialProgressBar)findViewById(R.id.progress);
		buttonCancel.setOnClickListener(this);
		
		if (Build.VERSION.SDK_INT >= 21){
			buttonCancel.getLayoutParams().height = UnitConversion.dip2px(this, 48);
			buttonCancel.getLayoutParams().width = UnitConversion.dip2px(this, 48);
			((RelativeLayout.LayoutParams)buttonCancel.getLayoutParams()).topMargin = UnitConversion.dip2px(this, 18);
		}
	}
	
	private void initData(){
		Bundle bundle = getIntent().getExtras();
		if(bundle != null && bundle.containsKey(PHOTO_URL)){
			photoUrl = bundle.getString(PHOTO_URL);
		}
		if(StringUtil.isEmpty(photoUrl)){
			finish();
		}else{
			updateView();
		}
	}
	
	private void updateView(){
		progress.setVisibility(View.VISIBLE);
		ImageViewLoader loader = new ImageViewLoader(this);
		
		loader.SetDefaultImage(new ColorDrawable(Color.TRANSPARENT));
		if( imageView != null ) {
			imageView.SetCanScale(false);
		}
		ViewTools.PreCalculateViewSize(imageView);
		String localPath = FileCacheManager.getInstance().CacheImagePathFromUrl(photoUrl);
		loader.DisplayImage(
        		imageView, 
        		photoUrl, 
        		localPath, 
        		this);
	}
	
	@Override
	protected void handleUiMessage(Message msg) {
		// TODO Auto-generated method stub
		super.handleUiMessage(msg);
		switch (msg.what) {
		case DOWNLOAD_PHOTO_SUCCESS:
		case DOWNLOAD_PHOTO_FAILED:
			imageView.SetCanScale(true);
			progress.setVisibility(View.GONE);
			break;

		default:
			break;
		}
	}
	
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		super.onClick(v);
		switch (v.getId()) {
		case R.id.buttonCancel:
			finish();
			break;

		default:
			break;
		}
	}

	@Override
	public void InitView() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void OnDisplayNewImageFinish() {
		Message msg = Message.obtain();
		msg.what = DOWNLOAD_PHOTO_SUCCESS;
		sendUiMessage(msg);		
	}

	@Override
	public void OnLoadPhotoFailed() {
		Message msg = Message.obtain();
		msg.what = DOWNLOAD_PHOTO_FAILED;
		sendUiMessage(msg);		
	}
}
