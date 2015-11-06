package com.qpidnetwork.dating.livechat;

import java.io.File;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.MediaPlayer.OnSeekCompleteListener;
import android.media.MediaPlayer.OnVideoSizeChangedListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Display;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.qpidnetwork.framework.base.BaseFragmentActivity;
import com.qpidnetwork.dating.R;
import com.qpidnetwork.framework.util.Log;
import com.qpidnetwork.tool.ImageViewLoader;

/**
 * EMF模块
 * 显示虚拟礼物界面
 * @author Max.Chiu
 */
public class VideoPlayActivity extends BaseFragmentActivity implements Callback, OnSeekCompleteListener, OnCompletionListener, OnErrorListener, OnPreparedListener, OnVideoSizeChangedListener {
	private static final String PHOTOLOCALPATH = "photoLocalPath";
	private static final String VIDEOLOCALPATH = "videoLocalPath";
	private static final String VIDEOFINISHCLOSEACTIVITY = "videofinishcloseactivity";
	
	public static void launchVideoPlayActivity(Context context, String photoLocalPath, String videoLocalPath, boolean isClose){
		Intent intent = new Intent();
		intent.setClass(context, VideoPlayActivity.class);
		intent.putExtra(PHOTOLOCALPATH, photoLocalPath);
		intent.putExtra(VIDEOLOCALPATH, videoLocalPath);
		intent.putExtra(VIDEOFINISHCLOSEACTIVITY, isClose);
		context.startActivity(intent);
	}
	
//	private View rootView;
	/**
	 * 大图
	 */
	private ImageView imageView;
	private ImageViewLoader loader = new ImageViewLoader(this);
	private ImageButton buttonPlay;
	private String photoLocalPath = "";
	
	/**
	 * 视频
	 */
	private String videoLocalPath = "";
	
	//播放结束是否关闭播放界面
	private boolean isClose = false;
//	private Configuration mConfiguration;
	
	/**
	 * 请求消息
	 */
	private enum PlayFlag {
		PLAY_MSG_START,
		PLAY_MSG_STOP,
	}
	
	/**
	 * 播放进度
	 */
	private TextView textView;
	
	
	/**
	 * 播放控件
	 */
	private SurfaceView surfaceView;
	private MediaPlayer mediaPlayer;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
		Bundle bundle = getIntent().getExtras();
		if (bundle != null) {
			if (bundle.containsKey(PHOTOLOCALPATH)) {
				photoLocalPath = bundle.getString(PHOTOLOCALPATH);
			}
			if (bundle.containsKey(VIDEOLOCALPATH)) {
				videoLocalPath = bundle.getString(VIDEOLOCALPATH);
			}
			if (bundle.containsKey(VIDEOFINISHCLOSEACTIVITY)) {
				isClose = bundle.getBoolean(VIDEOFINISHCLOSEACTIVITY);
			}
		}
		
		if( photoLocalPath != null ) {
			loader.DisplayImage(imageView, "", photoLocalPath, null);
		}
		
//		mConfiguration = getResources().getConfiguration();
		
    	// 加载视频
		File file = new File(videoLocalPath);
		if( file.exists() && file.isFile() ) {
			// 已经缓存过, 直接播放
			Play();
		}
    }
	
    @Override
    protected void onDestroy() {
    	// TODO Auto-generated method stub
    	super.onDestroy();
    	Stop();
    	if( mediaPlayer != null ) {
    		mediaPlayer.release();
    	}
    }
    
	@SuppressWarnings("deprecation")
	@Override
	public void InitView() {
		// TODO Auto-generated method stub
		setContentView(R.layout.activity_livechat_video_play);
//		rootView = findViewById(R.id.rl_root);

		InitPlayer();
		
		surfaceView = (SurfaceView) findViewById(R.id.surfaceView);
		surfaceView.getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		surfaceView.setVisibility(View.GONE);
        surfaceView.getHolder().addCallback(this); 
        
		imageView = (ImageView) findViewById(R.id.imageView);
		
        buttonPlay = (ImageButton) findViewById(R.id.buttonPlay);
        buttonPlay.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				onClickPlay(v);
			}
		});
        
        textView = (TextView) findViewById(R.id.textView);
        textView.setVisibility(View.GONE);
        textView.setText("");
	}
	
	@Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Log.d("VideoPlayActivity", "onConfigurationChanged( " +
        		"newConfig : " + newConfig + 
        		" )");
//        mConfiguration = newConfig;
        
        AutoResetVideoViewSize();
    }
	@Override
	protected void handleUiMessage(Message msg) {
		// TODO Auto-generated method stub
		super.handleUiMessage(msg);
		switch (PlayFlag.values()[msg.what]) {
		case PLAY_MSG_START: {
			if( mediaPlayer != null && mediaPlayer.isPlaying() ) {
				textView.setVisibility(View.VISIBLE);
				String text = String.valueOf((mediaPlayer.getDuration() - mediaPlayer.getCurrentPosition()) / 1000);
				textView.setText(text);
				Message newMsg = Message.obtain();
				newMsg.what = msg.what;
				mHandler.sendMessageDelayed(newMsg, 1000);
			}
		}break;
		case PLAY_MSG_STOP: {
			mHandler.removeMessages(PlayFlag.PLAY_MSG_START.ordinal());
			textView.setVisibility(View.GONE);
		}break;
		default:
			break;
		}
	}
	
	public void InitPlayer() {
		try {
			mediaPlayer = new MediaPlayer();
	        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
	        mediaPlayer.setLooping(false);

	        mediaPlayer.setOnPreparedListener(this);
	        mediaPlayer.setOnCompletionListener(this);  
	        mediaPlayer.setOnSeekCompleteListener(this);
	        mediaPlayer.setOnErrorListener(this);   
	        mediaPlayer.setOnVideoSizeChangedListener(this);

		} catch (Exception e) {
            // TODO: handle exception  
        	Log.d("VideoPlayActivity", "InitPlayer( Exception :" + e.getMessage() + " )");
        }
	}
	
	 /**
     * 点击播放
     * @param v
     */
    public void onClickPlay(View v) {
    	// 加载视频
		File file = new File(videoLocalPath);
		if( file.exists() && file.isFile() ) {
			// 已经缓存过, 直接播放
			Play();
		}
    }
    
    /**
     * 停止播放
     */
    public void Stop() {
		Message msg = Message.obtain();
        msg.what = PlayFlag.PLAY_MSG_STOP.ordinal();
        sendUiMessage(msg);
        
    	if( mediaPlayer != null && mediaPlayer.isPlaying() ) {
    		mediaPlayer.stop();
    	}
    }
    
    /**
     * 开始播放
     */
    public void Play() { 
    	mediaPlayer.reset(); 
    	
		if( videoLocalPath != null ) {
			// 设置需要播放的视频  
			try {
				mediaPlayer.setDataSource(videoLocalPath);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
    	buttonPlay.setVisibility(View.GONE);
    	imageView.setVisibility(View.GONE);
    	
    	// 引起sufaceCreated
    	surfaceView.setVisibility(View.VISIBLE);
    }   
    
    /**
     * 自适应视频大小
     */
    @SuppressWarnings("deprecation")
	public void AutoResetVideoViewSize() {
        int width = 0;
        int height = 0;
        int videoWidth = 0;
        int videoHeight = 0;
        if( mediaPlayer != null ) {
            videoWidth = mediaPlayer.getVideoWidth();
            videoHeight = mediaPlayer.getVideoHeight();
        }
        
        if( videoWidth != 0 && videoHeight != 0 ) {
        	// 按照视频长边按比例缩放
        	
        	Display display = getWindowManager().getDefaultDisplay();  

        	int dpWidth = display.getWidth();  
        	int dpHeight = display.getHeight();  
        	
            if ( dpWidth > dpHeight ) {  
            	// 横屏
            	height = dpHeight;
        		width = (int) (1.0f * videoWidth * dpHeight / videoHeight );
            } else {
            	// 竖屏
            	width = dpWidth;
        		height = (int) (1.0f * videoHeight * width / videoWidth );
            }
        	
        	Log.d("VideoPlayActivity", "AutoResetVideoViewSize( " +
    				"videoWidth : " + videoWidth + ", " +
    				"videoHeight : " + videoHeight + ", " +
    				"dpWidth : " + dpWidth + ", " +
    				"dpHeight : " + dpHeight + 
    				" )");
        	
        	Log.d("VideoPlayActivity", "AutoResetVideoViewSize( " +
					"width : " + width + ", " +
					"height : " + height +
					" )");
        	if( surfaceView != null ) {
            	surfaceView.getLayoutParams().width = width;
            	surfaceView.getLayoutParams().height = height;
    			surfaceView.getHolder().setFixedSize(width, height);
        	}
        }
    }
    
	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		// TODO Auto-generated method stub
		Log.d("VideoPlayActivity", "surfaceCreated()");
        
		try {
			// 把视频画面输出到SurfaceView
			mediaPlayer.setDisplay(holder);
	        mediaPlayer.prepareAsync();
		} catch (Exception e) {
			// TODO: handle exception
			Log.d("VideoPlayActivity", "surfaceCreated( Exception : " + e.getMessage() + ")");
		}
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		// TODO Auto-generated method stub
		Log.d("VideoPlayActivity", "surfaceChanged( " +
					"format : " + format + ", " + 
					"width : " + width + ", " +
					"height : " + height +
					" )");
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		// TODO Auto-generated method stub
		Log.d("VideoPlayActivity", "surfaceDestroyed()");   
	}
	
	@Override
	public void onSeekComplete(MediaPlayer mp) {
		// TODO Auto-generated method stub
		Log.d("VideoPlayActivity", "onSeekComplete( " + mp.getCurrentPosition() + " )"); 
	}

	@Override
	public void onCompletion(MediaPlayer mp) {
		// TODO Auto-generated method stub
		Log.d("VideoPlayActivity", "onCompletion()");
		
		Stop();
		buttonPlay.setVisibility(View.VISIBLE);
		imageView.setVisibility(View.VISIBLE);
		surfaceView.setVisibility(View.GONE);
		if(isClose){
			finish();
		}
	}

	@Override
	public boolean onError(MediaPlayer mp, int what, int extra) {
		// TODO Auto-generated method stub
		Log.d("VideoPlayActivity", "onError()");
		mediaPlayer.reset();
		
		Message msg = Message.obtain();
        msg.what = PlayFlag.PLAY_MSG_STOP.ordinal();
        sendUiMessage(msg);
        
		return false;
	}

	@Override
	public void onPrepared(MediaPlayer mp) {
		// TODO Auto-generated method stub
		Log.d("VideoPlayActivity", "onPrepared()");
        
		AutoResetVideoViewSize();
		
		// 播放  
        mediaPlayer.start();
        
        Message msg = Message.obtain();
        msg.what = PlayFlag.PLAY_MSG_START.ordinal();
        sendUiMessage(msg);
	}

	@Override
	public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
		// TODO Auto-generated method stub
		Log.d("VideoPlayActivity", "onVideoSizeChanged( " +
				"width : " + width + ", " +
				"height : " + height +
				" )");
	}
}
