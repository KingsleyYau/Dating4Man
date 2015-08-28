package com.qpidnetwork.dating.livechat.video;

import java.io.File;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.qpidnetwork.dating.R;
import com.qpidnetwork.dating.livechat.picture.PictureHelper;
import com.qpidnetwork.framework.util.ImageUtil;
import com.qpidnetwork.framework.util.StringUtil;
import com.qpidnetwork.framework.util.UnitConversion;
import com.qpidnetwork.view.MaterialProgressBar;

public class LivechatVideoItem extends FrameLayout{
	
	private static final int ASYN_PROCESS_PICTURE_SUCCESS = 1;

	public  ImageView ivThumb;
	private MaterialProgressBar progress;
	private ImageButton ivDownloadStatus;
	
	private Context mContext;
	
	public LivechatVideoItem(Context context) {
		super(context);
		initLayout(context);
	}

	public LivechatVideoItem(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
		initLayout(context);
	}
	
	private void initLayout(Context context) {
		LayoutInflater inflater = LayoutInflater.from(context);
		inflater.inflate(R.layout.view_livechat_video_item, this, true);
		
		ivThumb = (ImageView)findViewById(R.id.ivThumb);
		progress = (MaterialProgressBar)findViewById(R.id.progress);
		ivDownloadStatus = (ImageButton)findViewById(R.id.ivDownloadStatus);
	}
	
	/**
	 * 下载中
	 */
	public void updateForDownloading(){
		progress.setVisibility(View.VISIBLE);
		ivDownloadStatus.setVisibility(View.GONE);
	}
	
	/**
	 * 下载成功或本地已存在，处于可播放状态
	 */
	public void updateForPlay(){
		progress.setVisibility(View.GONE);
		ivDownloadStatus.setVisibility(View.VISIBLE);
		ivDownloadStatus.setImageResource(R.drawable.ic_play_circle_outline_white_24dp);
	}
	
	/**
	 * 下载失败或初始化为待下载状态
	 */
	public void updateForDefault(){
		progress.setVisibility(View.GONE);
		ivDownloadStatus.setVisibility(View.VISIBLE);
		ivDownloadStatus.setImageResource(R.drawable.ic_file_download_white_24dp);
	}

	/**
	 * 下载视频缩略图成功，设置缩略图
	 * @param path
	 */
	public void setVideoThumbWithSimpleSize(String path){
		if(!StringUtil.isEmpty(path) && (new File(path).exists())){
			AsynProcessPictureWithSimpleSize(path);
		}
	}
	
	/**
	 * 下载视频缩略图成功，设置缩略图
	 * @param path
	 */
	public void setVideoThumb(String path){
		if(!StringUtil.isEmpty(path) && (new File(path).exists())){
			AsynProcessPicture(path);
		}
	}

	
	/**
	 * 异步线程处理图片，防止界面卡住
	 * @param filePath
	 */
	private void AsynProcessPictureWithSimpleSize(final String filePath){
		PictureHelper.THREAD_POOL_EXECUTOR.execute(new Runnable() {
			
			@Override
			public void run() {
				Bitmap tempBitmap = ImageUtil.decodeSampledBitmapFromFile(filePath, UnitConversion.dip2px(mContext, 200), UnitConversion.dip2px(mContext, 200));
				Bitmap newBitmap = ImageUtil.get2DpRoundedImage(mContext, tempBitmap);
				Message msg = Message.obtain();
				msg.what = ASYN_PROCESS_PICTURE_SUCCESS;
				msg.obj = newBitmap;
				handler.sendMessage(msg);
			}
		});
	}
	
	/**
	 * 异步线程处理图片，防止界面卡住
	 * @param filePath
	 */
	private void AsynProcessPicture(final String filePath){
		PictureHelper.THREAD_POOL_EXECUTOR.execute(new Runnable() {
			
			@Override
			public void run() {
				Bitmap tempBitmap = ImageUtil.decodeHeightDependedBitmapFromFile(filePath, UnitConversion.dip2px(mContext, 112));
				Bitmap newBitmap = ImageUtil.get2DpRoundedImage(mContext, tempBitmap);
				Message msg = Message.obtain();
				msg.what = ASYN_PROCESS_PICTURE_SUCCESS;
				msg.obj = newBitmap;
				handler.sendMessage(msg);
			}
		});
	}
	
	private Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case ASYN_PROCESS_PICTURE_SUCCESS:
				Bitmap bitmap = (Bitmap)msg.obj;
				if( bitmap != null ) {
					ivThumb.setImageBitmap(bitmap);
				}
				break;

			default:
				break;
			}
		}
	};
}
