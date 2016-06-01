package com.qpidnetwork.view;

import java.io.File;
import java.util.ArrayList;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.View;

import com.qpidnetwork.framework.util.ImageUtil;
import com.qpidnetwork.framework.util.UnitConversion;

public class EmotionPlayer extends View{
	
	private static final int MIN_FRAME_SPACE = 1000/6;//1秒钟6帧 
	private ArrayList<String> imgUrls = null;
	private int currentFrame = 0;//当前要画的帧在数组中的位置
	private Bitmap curBitmap = null;//当前要画的Bitmap预加载
	private Context mConetext;
	private boolean isPlaying = false;
	
	private int mRepeat = -1;//重复播放次数，默认循环播放
	private int mCurrentIndex = 0; //当前已经播放次数
	private OnAnimationListener mAnimationListener;
	
	public EmotionPlayer(Context context){
		super(context);
		mConetext = context;
		init();
	}
	
	public EmotionPlayer(Context context, AttributeSet attrs){
		super(context, attrs);
		mConetext = context;
		init();
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		// TODO Auto-generated method stub
		super.onDraw(canvas);
		synchronized (this) {		
			if(curBitmap != null){
				int width = getMeasuredWidth();
				Matrix matrix = new Matrix();
				matrix.setScale(((float)width)/curBitmap.getWidth(), ((float)width)/curBitmap.getWidth());
				canvas.drawBitmap(curBitmap, matrix, null);
				curBitmap.recycle();
				curBitmap = null;
			}
		}
	}
	
	private Handler mHandler = new Handler();
	
	private Runnable animationTimer = new Runnable(){
		public void run() {
			refreshFrames();
		};
	};
	
	private void refreshFrames(){
		if((imgUrls != null)){
			if(imgUrls.size() > currentFrame + 1){
				//动画未画到最后一帧，继续画
				currentFrame++;	
			}else{
				//动画播放到最后一帧，重头播放
				mCurrentIndex ++;
				currentFrame = 0;
				if(mRepeat != -1){
					if(mRepeat ==  mCurrentIndex){
						stop();
						return;
					}
				}
			}
			long start = System.currentTimeMillis();
			if(reInvalidate()){
				/*图片无问题，刷新动画成功，不跳帧直接处理下一帧*/
				long timestamp = 0;//保证1秒6帧
				if(System.currentTimeMillis() - start > MIN_FRAME_SPACE){
					timestamp = 0;
				}else{
					timestamp = MIN_FRAME_SPACE - (System.currentTimeMillis() - start);
				}
				mHandler.postDelayed(animationTimer, timestamp);
			}else{
				/*跳帧时，删除改帧*/
				imgUrls.remove(currentFrame);
				if(imgUrls.size() > 0){
					currentFrame--;
				}else{
					imgUrls = null;
				}
				refreshFrames();
			}
		}
	}
	
	/**
	 * 设置循环播放次数
	 * @param repeat
	 */
	public void setRepeat(int repeat){
//		Log.i("hunter", "EmotionPlayer setRepeat: " + repeat);
		mRepeat = repeat;
	}
	
	private void init(){
		currentFrame = 0;
		reInvalidate();
	}
	
	/**
	 * 设置即将播放的动画数组
	 * @param playBigImages
	 */
	public void setImageList(ArrayList<String> playBigImages){
		if(imgUrls == null){
			imgUrls = new ArrayList<String>();
		}else{
			imgUrls.clear();
		}
		this.imgUrls.addAll(playBigImages);
		init();
	}
	
	public void play(){
		isPlaying = true;
		mCurrentIndex = 0;
		if(mAnimationListener != null){
			mAnimationListener.onAnimationStart();
		}
		refreshFrames();
	}
	
	/**
	 * 是否正在播放
	 * @return
	 */
	public boolean isPlaying(){
		return isPlaying;
	}
	
	/**
	 * 是否能播放
	 */
	public boolean canPlay(){
		return (imgUrls != null &&(imgUrls.size()>0));
	}
	
	public void stop(){
		isPlaying = false;
		mCurrentIndex = 0;
		mHandler.removeCallbacks(animationTimer);
		currentFrame = 0;
		reInvalidate();
		if(mAnimationListener != null){
			mAnimationListener.onAnimationStop();
		}
	}
	
	private boolean reInvalidate(){
		boolean isInvalidate = false;
		synchronized (this) {
			if((imgUrls != null)&&(imgUrls.size() > currentFrame)){
				File file = new File(imgUrls.get(currentFrame));
				if(file.exists()){
//					curBitmap = BitmapFactory.decodeFile(imgUrls.get(currentFrame));
					curBitmap = ImageUtil.decodeHeightDependedBitmapFromFile(imgUrls.get(currentFrame), UnitConversion.dip2px(mConetext, 120));
					if(curBitmap != null){
						isInvalidate = true;
						invalidate();
					}
				}
			}
		}
		return isInvalidate;
	}
	
	/**
	 * 设置动画监听器
	 * @param listener
	 */
	public void setAnimationListener(OnAnimationListener listener){
		mAnimationListener = listener;
	}
	
	public interface OnAnimationListener{
		public void onAnimationStart();
		public void onAnimationStop();
	}
}
