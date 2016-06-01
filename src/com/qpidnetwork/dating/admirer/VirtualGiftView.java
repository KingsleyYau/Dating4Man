package com.qpidnetwork.dating.admirer;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.MediaPlayer.OnSeekCompleteListener;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;

import com.qpidnetwork.framework.util.Log;

public class VirtualGiftView extends SurfaceView implements
		OnSeekCompleteListener, OnCompletionListener, OnPreparedListener,
		OnErrorListener, Callback{
	
	private static final String TAG = "VirtualGiftView";

	private boolean isInited = false;
	private String virtualGiftPath = "";
	private MediaPlayer mediaPlayer;
	
	private OnVirtualGiftPlayCallback mCallback;

	public VirtualGiftView(Context context) {
		this(context, null);
	}

	public VirtualGiftView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mediaPlayer = new MediaPlayer();
		
		getHolder().addCallback(this);
		getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
	}
	
	public void init(String filePath){
		this.isInited = true;
		this.virtualGiftPath = filePath;
	}
	
	public void setOnVirtualGiftPlayCallback(OnVirtualGiftPlayCallback callback){
		this.mCallback = callback;
	}

	/**
	 * 开始播放
	 */
	@SuppressWarnings("deprecation")
	public void Play() {
		try {
			if(isInited && (!TextUtils.isEmpty(virtualGiftPath))){
				mediaPlayer.reset();
				try {
					mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
					mediaPlayer.setLooping(true);
					mediaPlayer.setOnPreparedListener(this);
					mediaPlayer.setOnCompletionListener(this);
					mediaPlayer.setOnSeekCompleteListener(this);
					mediaPlayer.setOnErrorListener(this);
					try{
						mediaPlayer.setDataSource(virtualGiftPath);
					}catch(Exception e){
						Log.d(TAG, "mediaPlayer setDataSource e: " + e.getMessage());
					}
					mediaPlayer.prepare();
				} catch (IllegalStateException e) {
					e.printStackTrace();
					Log.d(TAG, "mediaPlayer prepare e: " + e.getMessage());
				}

				mediaPlayer.start();
				postDelayed(new Runnable() {
					
					@Override
					public void run() {
						if(mCallback != null){
				        	mCallback.onVideoStart();
				        }
					}
				}, 300);
			}
		} catch (Exception e) {
			// TODO: handle exception
			Log.d(TAG, "Play( Exception :" + e.getMessage() + " )");
		}
	}

	@Override
	public boolean onError(MediaPlayer mp, int what, int extra) {
		Log.d(TAG, "onError() what: " + what + " extra: " + extra);
		mp.reset();
		if(mCallback != null){
        	mCallback.onVideoError();
        }
		return false;
	}

	@Override
	public void onPrepared(MediaPlayer mp) {
		Log.d(TAG, "onPrepared()");
        
        // 自适应视频大小
        int width = 0;
        int height = 0;
        int videoWidth = mediaPlayer.getVideoWidth();
        int videoHeight = mediaPlayer.getVideoHeight();
        
        if( videoWidth != 0 && videoHeight != 0 ) {
        	
        	// 按照长边缩放
        	if( getWidth() < getHeight() ) {
        		width = getWidth();
        		height = (int) (1.0f * videoHeight * getWidth() / videoWidth );
        	} else {
        		width = (int) (1.0f * videoWidth * getHeight() / videoHeight );
        		height = getHeight();
        	}
        	
        	getLayoutParams().width = width;
        	getLayoutParams().height = height;
			getHolder().setFixedSize(width, height);
        }
	}

	@Override
	public void onCompletion(MediaPlayer mp) {
		Log.d(TAG, "onCompletion()");
		Play();
		if(mCallback != null){
        	mCallback.onVideoCompletion();
        }
	}

	@Override
	public void onSeekComplete(MediaPlayer mp) {
		Log.d(TAG, "onSeekComplete()");
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		Log.d(TAG, "surfaceCreated()");
        
		try {
			// 把视频画面输出到SurfaceView
			mediaPlayer.setDisplay(holder);
			Play();//放到后台后会出现mediaplayer播放错误异常停止，切换回前台后需要重现启动播放
		} catch (Exception e) {
			// TODO: handle exception
			Log.d(TAG, "surfaceCreated( Exception : " + e.getMessage() + ")");
		}		
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		Log.d(TAG, "surfaceChanged( " + "format : " + format + ", " + 
				"width : " + width + ", " +
				"height : " + height +
				" )");	
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		Log.d(TAG, "surfaceDestroyed()");
	}
	
	 /**
     * 停止播放
     */
    public void Stop() {
    	if( mediaPlayer.isPlaying() ) {
    		mediaPlayer.stop();
    		mediaPlayer.reset();
    		mediaPlayer.release();
    		Log.d(TAG, "mediaPlayer stop and release ");
    		mediaPlayer = null;
    		if(mCallback != null){
            	mCallback.onVideoStop();
            }
    	}
    }
    
    public interface OnVirtualGiftPlayCallback{
    	public void onVideoStart();
    	public void onVideoStop();
    	public void onVideoCompletion();
    	public void onVideoError();
    }

}
