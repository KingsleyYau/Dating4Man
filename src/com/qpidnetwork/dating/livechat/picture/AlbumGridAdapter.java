package com.qpidnetwork.dating.livechat.picture;

import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.os.Build;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.qpidnetwork.dating.R;
import com.qpidnetwork.dating.livechat.picture.AlbumPictureCashe.ImageCallback;
import com.qpidnetwork.framework.util.Log;


public class AlbumGridAdapter extends BaseAdapter {
	
	final String TAG = getClass().getSimpleName();
	
	Activity mActivity;
	AlbumPictureCashe cache;
	private List<ImageBean> mAlbumList;
	
	ImageCallback callback = new ImageCallback() {
		@Override
		public void imageLoad(ImageView imageView, Bitmap bitmap,
				Object... params) {
			if (imageView != null && bitmap != null) {
				String url = (String) params[0];
				if (url != null && url.equals((String) imageView.getTag())) {
					((ImageView) imageView).setImageBitmap(bitmap);
				} else {
					Log.e(TAG, "callback, bmp not match");
				}
			} else {
				Log.e(TAG, "callback, bmp null");
			}
		}
	};

	public AlbumGridAdapter(Activity act, List<ImageBean> list) {
		this.mActivity = act;
		mAlbumList = list;
		cache = new AlbumPictureCashe();
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		int count = 0;
		if (mAlbumList != null) {
			count = mAlbumList.size();
		}
		return count;
	}

	@Override
	public ImageBean getItem(int position) {
		// TODO Auto-generated method stub
		return mAlbumList.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@SuppressLint("NewApi") @Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		Holder holder;
		if (convertView == null) {
			
			float density = mActivity.getResources().getDisplayMetrics().density;
			Display display = mActivity.getWindowManager().getDefaultDisplay();
			Point size = new Point();
			
			if(Build.VERSION.SDK_INT > 12){
				display.getSize(size);
			}else{
				size.x = display.getWidth();
				size.y = display.getHeight();
			}
			
			int item_size = (int)(((float)size.x - (int)(1.0f * density)) / 2);
			
			holder = new Holder();
			convertView = LayoutInflater.from(mActivity).inflate(R.layout.adapter_album_item, null);
			holder.ivAlbum = (ImageView) convertView.findViewById(R.id.ivAlbum);
			holder.touch_feedback_region = (View)convertView.findViewById(R.id.touch_region);
			holder.ivAlbum.setLayoutParams(new RelativeLayout.LayoutParams(item_size, item_size));
			holder.touch_feedback_region.setLayoutParams(new RelativeLayout.LayoutParams(item_size, item_size));
			
			convertView.setTag(holder);
		} else {
			holder = (Holder) convertView.getTag();
		}
		final ImageBean item = mAlbumList.get(position);
		

		holder.ivAlbum.setTag(item.imagePath);
		cache.displayBmp(holder.ivAlbum, item.thumbnailPath, item.imagePath, callback);
		return convertView;
	}
	
	class Holder {
		private ImageView ivAlbum;
		private View touch_feedback_region;
	}
}
