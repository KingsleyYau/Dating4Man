package com.qpidnetwork.dating.advertisement;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.view.Display;
import android.view.View;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.qpidnetwork.dating.R;
import com.qpidnetwork.framework.base.BaseFragmentActivity;
import com.qpidnetwork.framework.util.ImageUtil;
import com.qpidnetwork.manager.FileCacheManager;

public class MainAdvertisementActivity extends BaseFragmentActivity {

	private ImageView ivAdvert;
	private ImageButton btnFinish;
	private Point windowSize = new Point();

	// data
	private AdMainAdvertItem mAdMainAdvertItem;
	

	@Override
	protected void onCreate(Bundle arg0) {
		// TODO Auto-generated method stub
		super.onCreate(arg0);
		setWidowSize();
		
		setContentView(R.layout.activity_main_pop_advertisement);

		ivAdvert = (ImageView) findViewById(R.id.ivAdvert);
		btnFinish = (ImageButton) findViewById(R.id.btnFinish);
		
		ivAdvert.setLayoutParams(new FrameLayout.LayoutParams(windowSize.x, windowSize.y));

		initData();
		
	}
	
	@SuppressWarnings("deprecation")
	private void setWidowSize(){
		
		float density = this.getResources().getDisplayMetrics().density;
		Display display = this.getWindow().getWindowManager().getDefaultDisplay();
    	Point size = new Point();
    	
    	if (Build.VERSION.SDK_INT > 12){
    		display.getSize(size);
    	}else{
    		size.y = display.getHeight();
    		size.x = display.getWidth();
    	}
    	
    	windowSize = size;
    	
    	int width_times =  Math.round((float)size.x / (56.0f * density));
    	float dialog_width = ((float)(width_times - 1) * 56.0f * density);
    	
    	int height_times = Math.round((float)size.y / (56.0f * density));
    	float dialog_height = ((float)(height_times - 3) * 56.0f * density);
    	
    	windowSize.x = (int)dialog_width;
    	windowSize.y = (int)dialog_height;
    	//this.getWindow().setLayout((int)dialog_width, (int)dialog_height);
    	this.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
    	this.requestWindowFeature(Window.FEATURE_NO_TITLE);
	}

	private void initData() {
		AdvertisementManager manager = AdvertisementManager.getInstance();
		if (manager != null) {
			mAdMainAdvertItem = manager.getAdMainAdvertItem();
		}
		if ((mAdMainAdvertItem != null)
				&& (mAdMainAdvertItem.adMainAdvert != null)) {
			// 显示次数+1
			if(!manager.hasNewMainAdvert()){
				mAdMainAdvertItem.Show(getApplicationContext());
			}

			String localPath = FileCacheManager
					.getInstance()
					.CacheImagePathFromUrl(mAdMainAdvertItem.adMainAdvert.image);
			Bitmap bitmap = ImageUtil.decodeSampledBitmapFromFile(localPath, windowSize.x, windowSize.y);//ImageUtil.get2DpRoundedImage(this, ImageUtil.decodeSampledBitmapFromFile(localPath, windowSize.x, windowSize.y), 0, Color.TRANSPARENT, true);
			ivAdvert.setImageBitmap(bitmap);
		}

		ivAdvert.setOnClickListener(this);
		btnFinish.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		super.onClick(v);
		switch (v.getId()) {
		case R.id.ivAdvert:
			/* 点击查看一次 */
			AdvertisementManager manager = AdvertisementManager.getInstance();
			if(!manager.hasNewMainAdvert()){
				mAdMainAdvertItem.Click(getApplicationContext());
			}
			AdvertisementManager.getInstance().parseAdvertisment(MainAdvertisementActivity.this,
					mAdMainAdvertItem.adMainAdvert.adurl,
					mAdMainAdvertItem.adMainAdvert.openType);
			finish();
			break;
		case R.id.btnFinish:
			finish();
			break;
		default:
			break;
		}
	}
}
