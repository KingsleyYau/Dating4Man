package com.qpidnetwork.dating.lady;

import java.util.List;
import java.util.Random;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.qpidnetwork.dating.R;
import com.qpidnetwork.manager.FileCacheManager;
import com.qpidnetwork.request.item.VSVideoDetailItem;
import com.qpidnetwork.tool.ImageViewLoader;

public class VideoGalleryAdapter extends BaseAdapter{
	
	private Context mContext;
	private List<VSVideoDetailItem> mVideoList;
	private ImageView[] imageViews;
	private int imageviewHeight;
	private int imageviewwidth;
	
	private int[] brandColors = new int[]{
			R.color.brand_color_light11,
			R.color.brand_color_light12,
			R.color.brand_color_light13,
			R.color.brand_color_light14,
			R.color.brand_color_light15
		};
	
	public VideoGalleryAdapter(Context context) {
		this.mContext = context;
		imageviewHeight = context.getResources().getDimensionPixelSize(R.dimen.video_height);
		imageviewwidth = context.getResources().getDimensionPixelSize(R.dimen.video_width);
	}
	
	public VideoGalleryAdapter(Context context, List<VSVideoDetailItem> videoList) {
		this.mContext = context;
		this.mVideoList = videoList;
		imageViews = new ImageView[videoList.size()];
		imageviewHeight = context.getResources().getDimensionPixelSize(R.dimen.video_height);
		imageviewwidth = context.getResources().getDimensionPixelSize(R.dimen.video_width);
	}
	
	public List<VSVideoDetailItem> getVideoList() {
		return mVideoList;
	}

	public void setVideoList(List<VSVideoDetailItem> videoList) {
		this.mVideoList = videoList;
		this.imageViews = new ImageView[videoList.size()];
	}
	
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return mVideoList.size();
	}
	
	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return mVideoList.get(position);
	}
	
	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return 0;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		ViewHolder holder = new ViewHolder();
		if (convertView == null) {
			LayoutInflater layoutInflater = LayoutInflater.from(mContext);
			convertView = layoutInflater.inflate(R.layout.item_video_gallery, null);
			
			LinearLayout.LayoutParams prm = new LinearLayout.LayoutParams(imageviewwidth, imageviewHeight);
			convertView.setLayoutParams(prm);
			
			holder.mPhoto = (ImageView)convertView.findViewById(R.id.item_photo);
			holder.mTime = (TextView)convertView.findViewById(R.id.item_video_time);
			holder.imageDownLoader = null;
			convertView.setTag(holder);
		}else{
			holder = (ViewHolder)convertView.getTag();
		}
		VSVideoDetailItem video = mVideoList.get(position);
		imageViews[position] = holder.mPhoto;
		/*头像处理*/
		if ( null != holder.imageDownLoader ) {
			// 停止回收旧Downloader
			holder.imageDownLoader.ResetImageView();
		}
		if((video.thumbURL != null)&&(!video.thumbURL.equals(""))){
			String localPath = FileCacheManager.getInstance().CacheImagePathFromUrl(video.thumbURL);
			holder.imageDownLoader = new ImageViewLoader(mContext);
			holder.imageDownLoader.SetDefaultImage(new ColorDrawable(mContext.getResources().getColor(brandColors[new Random().nextInt(brandColors.length -1)])));
			holder.imageDownLoader.DisplayImage(holder.mPhoto, video.thumbURL, localPath, null);
		}
		
		holder.mTime.setText(video.time);
		
		return convertView;
	}
	
	public void recycle() {
		if (imageViews == null) {
			return;
		}
		for (int i = 0; i < imageViews.length; i++) {
			Drawable drawable = imageViews[i].getDrawable();
			if (drawable instanceof TransitionDrawable) {
				TransitionDrawable tranDrawable = (TransitionDrawable) drawable;
				drawable = tranDrawable.getDrawable(1);
			}
			if (drawable instanceof BitmapDrawable) {
				BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
				Bitmap bitmap = bitmapDrawable.getBitmap();
				if (bitmap != null && !bitmap.isRecycled()) {
					//Log.v("PhotoGalleryAdapter", "recycled bitmap: " + i);
					bitmap.recycle();
				}
			}
		}
	}
	
	private class ViewHolder{
		public ImageView mPhoto;
		public TextView mTime;
		public ImageViewLoader imageDownLoader;
	}
}
