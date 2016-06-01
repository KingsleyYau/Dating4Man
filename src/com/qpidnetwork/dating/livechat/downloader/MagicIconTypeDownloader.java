package com.qpidnetwork.dating.livechat.downloader;

import java.io.File;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.ImageView;

import com.qpidnetwork.framework.util.ImageUtil;
import com.qpidnetwork.tool.FileDownloader;
import com.qpidnetwork.tool.FileDownloader.FileDownloaderCallback;
import com.qpidnetwork.tool.ImageViewLoader.ImageViewLoaderCallback;

public class MagicIconTypeDownloader {
	private static final int IMAGEDOWNLOADER_SUCCESS = 1;
	private static final int IMAGEDOWNLOADER_FAIL = 2;
	
	private FileDownloader mFileDownloader;
	private Context mContext = null;
	private Handler mHandler = null;
	private ImageView imageView;
	private Drawable mDefaultImage = null;
	
	public MagicIconTypeDownloader(Context context){
		
		mContext = context;
		mFileDownloader = new FileDownloader(mContext);

		mHandler = new Handler() {
			@Override
			public void handleMessage(final Message msg) {
				switch (msg.what) {

				case IMAGEDOWNLOADER_SUCCESS: {
					if (msg.obj instanceof Bitmap) {
						// 动作开始
						Bitmap bitmap = (Bitmap) msg.obj;
						LocalSetImageBitmap(bitmap);
					}
				}
				break;
				case IMAGEDOWNLOADER_FAIL: {

				}
					break;
				default:
					break;
				}

			}
		};
	}
	
	public void SetDefaultImage(Drawable drawable) {
		mDefaultImage = drawable;
	}
	
	/**
	 * 尝试加载文件，若不成功则下载图片，等比缩放至目标大小并显示
	 * 
	 * @param imageView
	 * @param url
	 * @param localPath
	 * @param width
	 * @param height
	 * @param callback
	 * @return
	 */
	public boolean DisplayImage(final ImageView imageView, final String url,
			final String localPath, final ImageViewLoaderCallback callback) 
	{			
		Stop();
		if (localPath == null) {
			return false;
		}

		if (imageView == null) {
			return false;
		}

		this.imageView = imageView;

		Bitmap tmpBitmap = null;

		File file = new File(localPath);
		if (file.exists() && file.isFile()) {
			tmpBitmap = BitmapFactory.decodeFile(localPath);
		}
		if (tmpBitmap != null) {
			LocalSetImageBitmap(tmpBitmap);
			imageView.setVisibility(View.VISIBLE);

		} else {
			if (mDefaultImage != null) {
				imageView.setImageDrawable(mDefaultImage);
			}
		}

		if (url == null || url.length() == 0) {
			return false;
		}
		
		// 下载
		final Bitmap bitmap = tmpBitmap;
		mFileDownloader.StartDownload(url, localPath,
				new FileDownloaderCallback() {
					@Override
					public void onUpdate(FileDownloader loader, int progress) {
						// TODO Auto-generated method stub
					}

					@Override
					public void onSuccess(FileDownloader loader) {
						// TODO Auto-generated method stub
						if (!loader.notModified || bitmap == null) {
							// 下载成功
							Bitmap tempBitmap = BitmapFactory.decodeFile(localPath);
							if (null != tempBitmap) {
								// 显示图片
								Message msg = Message.obtain();
								msg.what = IMAGEDOWNLOADER_SUCCESS;
								msg.obj = tempBitmap;
								mHandler.sendMessage(msg);
							}
						}
					}

					@Override
					public void onFail(FileDownloader loader) {
						// TODO Auto-generated method stub
						// 下载失败显示X
						Message msg = Message.obtain();
						msg.what = IMAGEDOWNLOADER_FAIL;
						mHandler.sendMessage(msg);
					}
				});

		return true;
	}
	
	public void Stop() {
		mFileDownloader.Stop();
		mHandler.removeMessages(IMAGEDOWNLOADER_SUCCESS);
		mHandler.removeMessages(IMAGEDOWNLOADER_FAIL);
	}
	
	/**
	 * 设置图片
	 * @param bitmap
	 */
	private void LocalSetImageBitmap(Bitmap bitmap){
		imageView.setImageBitmap(ImageUtil.getGreyImage(bitmap));
	}
}
