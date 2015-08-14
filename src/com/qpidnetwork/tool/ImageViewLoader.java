package com.qpidnetwork.tool;

import java.io.File;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.widget.ImageView;

import com.qpidnetwork.framework.util.ImageUtil;
import com.qpidnetwork.framework.util.SystemUtil;
import com.qpidnetwork.tool.FileDownloader.FileDownloaderCallback;
import com.qpidnetwork.view.ProgressImageView;

/**
 * 异步加载图片工具
 * @author Max.Chiu
 *
 */
public class ImageViewLoader {
	public interface ImageViewLoaderCallback {
		public void OnDisplayNewImageFinish();
		public void OnLoadPhotoFailed();
	}
	
	private enum DownLoadFlag {
		SET_DEFAULT_IMAGE,
		SUCCESS,
		SUCCESS_IMAGEVIEW,
		/**
		 * 处理图片缩放并裁剪圆角成功的消息
		 */
		SUCCESS_IMAGEVIEW_FILLET,
		FAIL,
	};
	
	private FileDownloader mFileDownloader;
	private Context mContext = null;
	private Handler mHandler = null;
	
	private ProgressImageView view;
	private ImageView imageView;
	
	private Drawable mDefaultImage = null;
	private boolean mbAlphaAnimation = false;
	private boolean mbBigFileDontUseCache = false;
	
	public void SetBigFileDontUseCache(boolean bBigFileDontUseCache) {
		mbBigFileDontUseCache = bBigFileDontUseCache;
	}
	
	public ImageViewLoader(Context context) {
		mContext = context;
		
		mFileDownloader = new FileDownloader(mContext);
		
		mHandler = new Handler() {
			@SuppressWarnings("deprecation")
			@Override
            public void handleMessage(final Message msg) {
				DownLoadFlag flag = DownLoadFlag.values()[msg.what];
				switch (flag) {
				case SET_DEFAULT_IMAGE: {
					if ( null != imageView && null != mDefaultImage ) {
						imageView.setImageDrawable(mDefaultImage);
					}
				}break;
				case SUCCESS: {
					if ( null != view && null != view.imageView && null != view.progressBar ) {
						view.progressBar.setVisibility(View.INVISIBLE);
						view.imageView.setVisibility(View.VISIBLE);
						
						String localPath = (String) msg.obj;
						Bitmap bitmap = null;
						File file = new File(localPath);
						if( file.exists() && file.isFile() ) {
							bitmap = ImageUtil.decodeSampledBitmapFromFile(
									localPath, 
									SystemUtil.getDisplayMetrics(mContext).widthPixels, 
									SystemUtil.getDisplayMetrics(mContext).heightPixels
									);
						}
						view.imageView.setImageBitmap(bitmap);
					}
				}break;
				case SUCCESS_IMAGEVIEW:{
					Bitmap bitmap = null;
					String localPath = (String) msg.obj;
					File file = new File(localPath);
					if( file.exists() && file.isFile() ) {
//						drawable = Drawable.createFromPath(localPath);
						bitmap = ImageUtil.decodeSampledBitmapFromFile(
								localPath, 
								SystemUtil.getDisplayMetrics(mContext).widthPixels, 
								SystemUtil.getDisplayMetrics(mContext).heightPixels
								);
					}

					// 动作开始
					AlphaAnimationStart(bitmap);
				}break;
				case SUCCESS_IMAGEVIEW_FILLET:{
					// 显示已处理的缓存
					Bitmap bitmap = (Bitmap)msg.obj;
					if ( null != bitmap && null != imageView ) {
						imageView.setImageBitmap(bitmap);
//						AlphaAnimationStart(bitmap);
					}
				}break;
				case FAIL:{
					
				}break;
				default:
					break;
				}

			}
		};
	}
	
	public void Stop() {
		mFileDownloader.Stop();
		mHandler.removeMessages(DownLoadFlag.SET_DEFAULT_IMAGE.ordinal());
		mHandler.removeMessages(DownLoadFlag.SUCCESS.ordinal());
		mHandler.removeMessages(DownLoadFlag.SUCCESS_IMAGEVIEW.ordinal());
		mHandler.removeMessages(DownLoadFlag.SUCCESS_IMAGEVIEW_FILLET.ordinal());
		mHandler.removeMessages(DownLoadFlag.FAIL.ordinal());
	}
	
	public boolean DisplayImage(final ProgressImageView view, final String url, final String localPath, 
			final ImageViewLoaderCallback callback) {
		Stop();
		
		if( view == null || view.progressBar == null || view.imageView == null ) {
			return false;
		}
		
		this.view = view;
		
//		BitmapDrawable drawable = null;
		Bitmap bitmap = null;
		
		File file = new File(localPath);
		if( file.exists() && file.isFile() ) {
//			drawable = Drawable.createFromPath(localPath);
			bitmap = ImageUtil.decodeSampledBitmapFromFile(
					localPath, 
					SystemUtil.getDisplayMetrics(mContext).widthPixels, 
					SystemUtil.getDisplayMetrics(mContext).heightPixels
					);
		}
		
		if( bitmap != null ) {
			// 显示缓存 
//			view.imageView.setImageDrawable(drawable);
			view.imageView.setImageBitmap(bitmap);
			view.progressBar.setVisibility(View.INVISIBLE);
			view.imageView.setVisibility(View.VISIBLE);
			
			if( callback != null ) {
				callback.OnDisplayNewImageFinish();
			}
		} else {
			if( mDefaultImage != null ) {
				view.imageView.setImageDrawable(mDefaultImage);
			}
			
			view.progressBar.setVisibility(View.VISIBLE);
			view.imageView.setVisibility(View.INVISIBLE);
			
			// 下载 
			mFileDownloader.SetBigFile(mbBigFileDontUseCache);
			mFileDownloader.SetUseCache(!mbBigFileDontUseCache);
			mFileDownloader.StartDownload(url, localPath, new FileDownloaderCallback() {
				@Override
				public void onUpdate(FileDownloader loader, int progress) {
					// TODO Auto-generated method stub
					// 下载中显示小菊花 
				}
				
				@Override
				public void onSuccess(FileDownloader loader) {
					// TODO Auto-generated method stub
					// 下载成功显示 
					Message msg = Message.obtain();
					msg.what = DownLoadFlag.SUCCESS.ordinal();
					msg.obj = localPath;
					mHandler.sendMessage(msg);
					
					if( callback != null ) {
						callback.OnDisplayNewImageFinish();
					}
				}
				
				@Override
				public void onFail(FileDownloader loader) {
					// TODO Auto-generated method stub
					// 下载失败显示X
					Message msg = Message.obtain();
					msg.what = DownLoadFlag.FAIL.ordinal();
					mHandler.sendMessage(msg);
					if (callback != null) { 
						callback.OnLoadPhotoFailed();
					}
				}
			});
		}
		
		return true;
	}
	
	public boolean DisplayImage(final ImageView imageView, final String url, final String localPath, 
			final ImageViewLoaderCallback callback) {
		Stop();
		
		if( localPath == null ) {
			return false;
		}
		
		if( imageView == null ) {
			return false;
		}
		
		this.imageView = imageView;
		
//		Drawable drawable = null;
		Bitmap tmpBitmap = null;
		
		File file = new File(localPath);
		if( file.exists() && file.isFile() ) {
//			drawable = Drawable.createFromPath(localPath);
			
			tmpBitmap = ImageUtil.decodeSampledBitmapFromFile(
					localPath, 
					SystemUtil.getDisplayMetrics(mContext).widthPixels, 
					SystemUtil.getDisplayMetrics(mContext).heightPixels
					);
		}
		
		if( tmpBitmap != null ) {
			// 显示缓存 
//			imageView.setImageDrawable(drawable);
			imageView.setImageBitmap(tmpBitmap);
			imageView.setVisibility(View.VISIBLE);
			
//			if( callback != null ) {
//				callback.OnDisplayNewImageFinish();
//			}
		} else {
			// 显示默认
			if( mDefaultImage != null ) {
				imageView.setImageDrawable(mDefaultImage);
			}
		}
		
		if( url == null || url.length() == 0 ) {
			return false;
		}
		
		// 下载 
		final Bitmap bitmap = tmpBitmap;
		mFileDownloader.StartDownload(url, localPath, new FileDownloaderCallback() {
			@Override
			public void onUpdate(FileDownloader loader, int progress) {
				// TODO Auto-generated method stub
				// 下载中显示小菊花 
			}
			
			@Override
			public void onSuccess(FileDownloader loader) {
				// TODO Auto-generated method stub
				if( !loader.notModified || bitmap == null ) {
					// 下载成功显示 
					Message msg = Message.obtain();
					msg.what = DownLoadFlag.SUCCESS_IMAGEVIEW.ordinal();
					msg.obj = localPath;
					mHandler.sendMessage(msg);
				}
				
				if( callback != null ) {
					callback.OnDisplayNewImageFinish();
				}
			}
			
			@Override
			public void onFail(FileDownloader loader) {
				// TODO Auto-generated method stub
				// 下载失败显示X
				Message msg = Message.obtain();
				msg.what = DownLoadFlag.FAIL.ordinal();
				mHandler.sendMessage(msg);
			}
		});
		
		return true;
	}
	
	/**
	 * 下载图片、等比缩放并把图片变圆，再把set到ImageView
	 * @param imageView		ImageView
	 * @param url			图片下载URL
	 * @param desWidth		等 比缩放后的宽度
	 * @param desHeight		等 比缩放后的高度
	 * @param topRadius		顶部圆角的半径(0：不变圆)	
	 * @param bottomRadius	底部圆角的半径(0：不变圆)
	 * @param localPath		本地路径
	 * @param callback
	 * @return
	 */
	public boolean DisplayImage(final ImageView imageView, final String url, final int desWidth, final int desHeight, 
			final int topRadius, final int bottomRadius, final String localPath, 
			final ImageViewLoaderCallback callback) 
	{
		Stop();
		
		if( localPath == null ) {
			return false;
		}
		
		if( imageView == null ) {
			return false;
		}
		
		this.imageView = imageView;
		
		Bitmap bitmap = getBitmapWithFile(localPath);
		if ( null != bitmap ) {
			// 显示图片
			imageView.setImageBitmap(bitmap);
		} else {
			// 需要下载，先画默认图
			if( mDefaultImage != null ) {
				imageView.setImageDrawable(mDefaultImage);
			}
		}
		
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				if( null == url
					|| url.length() == 0 ) 
				{
					return;
				}
				
				// 下载 
				final String srcFileName = "_srcfile";
				final String srcFilePath = localPath + srcFileName; 
				
				mFileDownloader.SetBigFile(mbBigFileDontUseCache);
				mFileDownloader.SetUseCache(!mbBigFileDontUseCache);
				mFileDownloader.StartDownload(url, srcFilePath, new FileDownloaderCallback() {
					@Override
					public void onUpdate(FileDownloader loader, int progress) {
						// TODO Auto-generated method stub
						// 下载中显示小菊花 
					}
					
					@Override
					public void onSuccess(FileDownloader loader) {
						// TODO Auto-generated method stub
						Bitmap bitmap = getBitmapWithFile(localPath);
						if( !loader.notModified || bitmap == null ) {
							// 重新裁剪
							boolean result = false;
//							long startTime = System.currentTimeMillis();
							// 处理下载图片
							Bitmap bitmapBig = ImageUtil.decodeSampledBitmapFromFile(srcFilePath, desWidth, desHeight);
							if ( null != bitmapBig ) {
								// 处理图片为圆角
								bitmapBig = ImageUtil.filletBitmap(bitmapBig, topRadius, bottomRadius, mContext.getResources().getDisplayMetrics().density);
								// 保存本地图片
								ImageUtil.saveBitmapToFile(localPath, bitmapBig, Bitmap.CompressFormat.PNG, 100);
								
								result = null != bitmapBig;
							}
							
							if ( result ) 
							{
								// 显示图片 
								Message msg = Message.obtain();
								msg.what = DownLoadFlag.SUCCESS_IMAGEVIEW_FILLET.ordinal();
								msg.obj = bitmapBig;
								mHandler.sendMessage(msg);
							}
						} 
						
						// 回调
						if( callback != null ) {
							callback.OnDisplayNewImageFinish();
						}
					}
					
					@Override
					public void onFail(FileDownloader loader) {
						// TODO Auto-generated method stub
						// 下载失败显示X
						Message msg = Message.obtain();
						msg.what = DownLoadFlag.FAIL.ordinal();
						mHandler.sendMessage(msg);
					}
				});
			}
		}).start();
		
		return true;
	}
	
	/**
	 * 加载图片文件
	 * @param filePath		图片路径
	 * @param topRadius		顶部圆角半径(0:不变)
	 * @param bottomRadius	底部圆角半径(0:不变)
	 * @return
	 */
	private Bitmap getBitmapWithFile(String filePath)
	{
		Bitmap bitmap = null;
		File file = new File(filePath);
		if( file.exists() && file.isFile() ) {
			// 加载文件
			final BitmapFactory.Options options = new BitmapFactory.Options();
			options.inJustDecodeBounds = false;
		    bitmap = BitmapFactory.decodeFile(filePath, options);
		}
		return bitmap;
	}

	public void SetDefaultImage(Drawable drawable) {
		mDefaultImage = drawable;
	}
	
	public void SetAlphaAnimation(boolean bAlphaAnimation) {
		mbAlphaAnimation = bAlphaAnimation;
	}

	public void ResetImageView() {
		// TODO Auto-generated method stub
		this.imageView = null;
		if (null != mFileDownloader) {
			mFileDownloader.StopDonotWait();
		}
	}
	
	private void AlphaAnimationStart(final Bitmap bitmap) {
		if( mbAlphaAnimation ) {
			AlphaAnimation alphaAnim = new AlphaAnimation(1.0f, 0.0f);  
	        alphaAnim.setDuration(250);
	        alphaAnim.setAnimationListener(new AnimationListener() {
				
				@Override
				public void onAnimationStart(Animation animation) {
					// TODO Auto-generated method stub
					
				}
				
				@Override
				public void onAnimationRepeat(Animation animation) {
					// TODO Auto-generated method stub
					
				}
				
				@Override
				public void onAnimationEnd(Animation animation) {
					// TODO Auto-generated method stub
					if (null != imageView) {
						imageView.setImageBitmap(bitmap);
					}
					AlphaAnimation alphaAnim = new AlphaAnimation(0.0f, 1.0f);  
					alphaAnim.setDuration(250);
					imageView.startAnimation(alphaAnim);
				}
			});
			if (null != imageView) {
		        imageView.startAnimation(alphaAnim);
			}
		} else {
			if (null != imageView) {
				imageView.setImageBitmap(bitmap);
			}
		}
	}
}
