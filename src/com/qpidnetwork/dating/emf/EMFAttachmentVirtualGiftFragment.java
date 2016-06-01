package com.qpidnetwork.dating.emf;


import java.io.File;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.MediaPlayer.OnSeekCompleteListener;
import android.os.Bundle;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.qpidnetwork.dating.R;
import com.qpidnetwork.dating.googleanalytics.AnalyticsFragmentActivity;
import com.qpidnetwork.framework.util.Log;
import com.qpidnetwork.tool.FileDownloader;
import com.qpidnetwork.tool.FileDownloader.FileDownloaderCallback;
import com.qpidnetwork.tool.ImageViewLoader;
import com.qpidnetwork.view.IndexFragment;
import com.qpidnetwork.view.MaterialProgressBar;

/**
 * EMF模块
 * 显示虚拟礼物界面
 * @author Max.Chiu
 */
public class EMFAttachmentVirtualGiftFragment extends IndexFragment 
											  implements Callback, 
											  			 OnSeekCompleteListener, 
											  			 OnCompletionListener, 
											  			 OnErrorListener, 
											  			 OnPreparedListener,
											  			 OnClickListener,
											  			 FileDownloaderCallback
{
	
	private enum DownLoadFlag {
		FAIL,
		SUCCESS,
	};
	
	private boolean bCanStop = false;
	
	private View rootView;
	
	private boolean bShowInsert = false;
	private boolean bShowTips = true;
	
	/**
	 * 描述
	 */
//	private RelativeLayout layoutInsert;
	private TextView textViewDescription;
	private Button buttonInsert;
	private String name = "";
	
	/**
	 * 大图
	 */
	private ImageView imageView;
	private ImageViewLoader loader;
	private ImageButton buttonPlay;
	private String photoUrl = "";
	private String photoLocalPath = "";
	
	/**
	 * 视频
	 */
	private String videoUrl = "";
	private String videoLocalPath = "";
	private FileDownloader mFileDownloader;
	
	/**
	 * 播放控件
	 */
	private SurfaceView surfaceView;
	private MediaPlayer mediaPlayer;
	
	/**
	 * 菊花
	 */
	private MaterialProgressBar progressBar;
	private boolean mNeedPlay = false;
	
	public EMFAttachmentVirtualGiftFragment() {
		super();
		photoUrl = "";
		photoLocalPath = "";
		videoUrl = "";
		videoLocalPath = "";
	}
	
	public EMFAttachmentVirtualGiftFragment(int index) {
		super(index);
		// TODO Auto-generated constructor stub
		photoUrl = "";
		photoLocalPath = "";
		videoUrl = "";
		videoLocalPath = "";
		
	}
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
    
    @SuppressLint("InflateParams")
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
                                        
    	
        View view = inflater.inflate(R.layout.fragment_emf_attachment_virtual_gift, null);
        imageView = (ImageView) view.findViewById(R.id.imageView);
        loader = new ImageViewLoader(getActivity());
        
//        layoutInsert = (RelativeLayout) view.findViewById(R.id.layoutInsert);
        textViewDescription = (TextView) view.findViewById(R.id.textViewDescription);
        buttonInsert = (Button) view.findViewById(R.id.buttonInsert);
        buttonInsert.setOnClickListener(this);
        
        buttonPlay = (ImageButton) view.findViewById(R.id.buttonPlay);
        buttonPlay.setOnClickListener(this);
        
        mediaPlayer = new MediaPlayer();
        surfaceView = (SurfaceView) view.findViewById(R.id.surfaceView); 
        surfaceView.getHolder().addCallback(this);   
        
        progressBar = (MaterialProgressBar) view.findViewById(R.id.progressBar);
        progressBar.setVisibility(View.GONE);
        
        rootView = view;
        UpdateView();
        return view;
    }
    
    @Override
    public void onAttach(Activity activity) {
    	// TODO Auto-generated method stub
    	super.onAttach(activity);
    	mFileDownloader = new FileDownloader(mContext);
    	ReloadData();
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
		} else {
			if( mFileDownloader == null ) {
				return;
			}
	    	
			if( !mFileDownloader.IsDownloading() ) {
				// 未缓存过, 开始下载 
				mNeedPlay = true;
				mFileDownloader.StartDownload(videoUrl, videoLocalPath, this);
			}
			
			// 刷新界面
	    	UpdateView();
		}
    }
    
    @Override
    public void onDestroyView() {
    	// TODO Auto-generated method stub
    	super.onDestroyView();
    	Stop();
    }
    
    /**
     * 重新加载数据
     * @param photoUrl
     * @param photoLocalPath
     * @param videoUrl
     * @param videoLocalPath
     */
    public void ReloadData(String photoUrl, String photoLocalPath, String videoUrl, final String videoLocalPath, String name) {
    	// 描述
    	this.name = name;
    	
    	// 加载图片
    	this.photoUrl = photoUrl;
    	this.photoLocalPath = photoLocalPath;
    	
		// 加载视频
    	this.videoUrl = videoUrl;
    	this.videoLocalPath = videoLocalPath;
    	
    	ReloadData();
    }

    public void ReloadData() {
       	// 加载视频
		File file = new File(videoLocalPath);
		if( file.exists() && file.isFile() ) {
			// 已经缓存过
		} else {
			if( mFileDownloader == null ) {
				return;
			}
			if( !mFileDownloader.IsDownloading() ) {
				// 未缓存过, 开始下载 
				mFileDownloader.StartDownload(videoUrl, videoLocalPath, new FileDownloaderCallback() {
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
						msg.obj = videoLocalPath;
						sendUiMessage(msg);
					}
					
					@Override
					public void onFail(FileDownloader loader) {
						// TODO Auto-generated method stub
						// 下载失败显示X
						Message msg = Message.obtain();
						msg.what = DownLoadFlag.FAIL.ordinal();
						sendUiMessage(msg);
					}
				});
			}
		}
    	
		// 刷新界面
    	UpdateView();
    }
    
    /**
     * 停止播放
     */
    public void Stop() {
    	if( mediaPlayer.isPlaying() ) {
    		mediaPlayer.stop();
    	}
    }
    
    /**
     * 开始播放
     */
    @SuppressWarnings("deprecation")
	public void Play() {  
        try {  
        	
    		mediaPlayer.reset(); 
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            surfaceView.getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS); 
            mediaPlayer.setLooping(true);

            mediaPlayer.setOnPreparedListener(this);
            mediaPlayer.setOnCompletionListener(this);  
            mediaPlayer.setOnSeekCompleteListener(this);
            mediaPlayer.setOnErrorListener(this);    
            
            // 把视频画面输出到SurfaceView  
//            mediaPlayer.setDisplay(surfaceView.getHolder());
            
            // 设置需要播放的视频  
            mediaPlayer.setDataSource(videoLocalPath);
            
            
        	buttonPlay.setVisibility(View.GONE);
        	imageView.setVisibility(View.GONE);
        	// 引起sufaceCreated
        	surfaceView.setVisibility(View.VISIBLE);
//            mediaPlayer.prepare();
//
//            // 自适应视频大小
//            int width = 0;
//            int height = 0;
//            int videoWidth = mediaPlayer.getVideoWidth();
//            int videoHeight = mediaPlayer.getVideoHeight();
//            
//            if( videoWidth != 0 && videoHeight != 0 ) {
//            	
//            	// 按照长边缩放
//            	if( rootView.getWidth() < rootView.getHeight() ) {
//            		width = rootView.getWidth();
//            		height = (int) (1.0f * videoHeight * rootView.getWidth() / videoWidth );
//            	} else {
//            		width = (int) (1.0f * videoWidth * rootView.getHeight() / videoHeight );
//            		height = rootView.getHeight();
//            	}
//            	
//            	
//    			surfaceView.setVisibility(View.INVISIBLE);
//            	surfaceView.getLayoutParams().width = width;
//            	surfaceView.getLayoutParams().height = height;
//            	surfaceView.getHolder().setFixedSize(width, height);
//            }
            
//            // 播放  
//            mediaPlayer.start();

        } catch (Exception e) {  
            // TODO: handle exception  
        	Log.d("EMFAttachmentVirtualGiftFragment", "Play( Exception :" + e.getMessage() + " )");
        }
    }   
    
	/**
	 * 刷新界面
	 */
	private void UpdateView() {
		if( photoUrl != null && photoUrl.length() > 0 
				&& photoLocalPath != null && photoLocalPath.length() > 0 
				&& loader != null ) {
			loader.DisplayImage(imageView, photoUrl, photoLocalPath, null);
		}
		
		if( mediaPlayer != null && mediaPlayer.isPlaying() ) {
			if( buttonPlay != null )
				buttonPlay.setVisibility(View.GONE);
			if( imageView != null )
				imageView.setVisibility(View.GONE);
			if( surfaceView != null )
				surfaceView.setVisibility(View.VISIBLE);
		} else {
			if( buttonPlay != null )
				buttonPlay.setVisibility(View.VISIBLE);
			if( imageView != null )
				imageView.setVisibility(View.VISIBLE);
			if( surfaceView != null )
				surfaceView.setVisibility(View.GONE);
		}
		
		if( buttonInsert != null ) {
			if( bShowInsert ) {
				buttonInsert.setVisibility(View.VISIBLE);
			} else {
				buttonInsert.setVisibility(View.GONE);
			}
		}
		
		if( textViewDescription != null ) {
			if( bShowTips == true )
				textViewDescription.setText(this.name);
			else {
				textViewDescription.setText("");
			}
		} 
		
		if( progressBar != null && buttonPlay != null ) {
			if( mFileDownloader.IsDownloading() ) {
				progressBar.setVisibility(View.VISIBLE);
				buttonPlay.setVisibility(View.GONE);
			} else {
				progressBar.setVisibility(View.GONE);
			}
		}
		
		
	}
	
	public void SetInsert(boolean bShowInsert) {
		this.bShowInsert = bShowInsert;
	}
	
	public void SetTips(boolean bShowTips) {
		this.bShowTips = bShowTips;
	}
	
	/**
	 * 点击插入
	 * @param v
	 */
	public void onClickInsert(View v) {
		Intent intent = new Intent();
		intent.putExtra(EMFAttachmentPreviewActivity.INDEX, getIndex());
		getActivity().setResult(Activity.RESULT_OK, intent);
		getActivity().finish();
	}
	
	@Override
	protected void handleUiMessage(Message msg) {
		super.handleUiMessage(msg);
		DownLoadFlag flag = DownLoadFlag.values()[msg.what];
		switch (flag) {
		case SUCCESS: {
			// 下载成功播放
			if( progressBar != null ) {
				progressBar.setVisibility(View.GONE);
			} 
			
			if( mNeedPlay ) {
				Play();
			} else {
				if( buttonPlay != null ) {
					buttonPlay.setVisibility(View.VISIBLE);
				}
			}
		}break;
		case FAIL:{
			if( progressBar != null ) {
				progressBar.setVisibility(View.GONE);
			} 
			
			if( buttonPlay != null ) {
				buttonPlay.setVisibility(View.VISIBLE);
			}
		}break;
		default:break;
		}
	}

	@Override
	public void onPageScrollStateChanged(int arg0) {
		// TODO Auto-generated method stub
		if( arg0 == 0 && bCanStop ) {
			Stop();
			UpdateView();
		}
	}

	@Override
	public void onPageScrolled(int arg0, float arg1, int arg2) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onPageSelected(int arg0) {
		// TODO Auto-generated method stub
		if( arg0 != getIndex() ) {
			bCanStop = true;
		} else {
			bCanStop = false;
		}
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		// TODO Auto-generated method stub
		Log.d("EMFAttachmentVirtualGiftFragment", "surfaceCreated()");
        
		try {
			// 把视频画面输出到SurfaceView
			mediaPlayer.setDisplay(holder);
	        mediaPlayer.prepareAsync();
		} catch (Exception e) {
			// TODO: handle exception
			Log.d("EMFAttachmentVirtualGiftFragment", "surfaceCreated( Exception : " + e.getMessage() + ")");
		}
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		// TODO Auto-generated method stub
		Log.d("EMFAttachmentVirtualGiftFragment", "surfaceChanged( " +
					"format : " + format + ", " + 
					"width : " + width + ", " +
					"height : " + height +
					" )");
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		// TODO Auto-generated method stub
		Log.d("EMFAttachmentVirtualGiftFragment", "surfaceDestroyed()");   
	}
	
	@Override
	public void onSeekComplete(MediaPlayer mp) {
		// TODO Auto-generated method stub
//		Log.d("EMFAttachmentVirtualGiftFragment", "onSeekComplete( " + mp.getCurrentPosition() + " )");    
	}

	@Override
	public void onCompletion(MediaPlayer mp) {
		// TODO Auto-generated method stub
		Log.d("EMFAttachmentVirtualGiftFragment", "onCompletion()");
	}

	@Override
	public boolean onError(MediaPlayer mp, int what, int extra) {
		// TODO Auto-generated method stub
		Log.d("EMFAttachmentVirtualGiftFragment", "onError()");
		mp.reset();
		return false;
	}

	@Override
	public void onPrepared(MediaPlayer mp) {
		// TODO Auto-generated method stub
		Log.d("EMFAttachmentVirtualGiftFragment", "onPrepared()");
        
        // 自适应视频大小
        int width = 0;
        int height = 0;
        int videoWidth = mediaPlayer.getVideoWidth();
        int videoHeight = mediaPlayer.getVideoHeight();
        
        if( videoWidth != 0 && videoHeight != 0 ) {
        	
        	// 按照长边缩放
        	if( rootView.getWidth() < rootView.getHeight() ) {
        		width = rootView.getWidth();
        		height = (int) (1.0f * videoHeight * rootView.getWidth() / videoWidth );
        	} else {
        		width = (int) (1.0f * videoWidth * rootView.getHeight() / videoHeight );
        		height = rootView.getHeight();
        	}
        	
        	surfaceView.getLayoutParams().width = width;
        	surfaceView.getLayoutParams().height = height;
			surfaceView.getHolder().setFixedSize(width, height);
        }
        
		// 播放  
        mediaPlayer.start();
	}
	
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		if (v.getId() == R.id.buttonInsert) {
			onClickInsert(v);
		}
		else if (v.getId() == R.id.buttonPlay) {
			onClickPlay(v);
		}
	}

	@Override
	public void onSuccess(FileDownloader loader) {
		// TODO Auto-generated method stub
		// 下载成功显示 
		Message msg = Message.obtain();
		msg.what = DownLoadFlag.SUCCESS.ordinal();
		msg.obj = videoLocalPath;
		sendUiMessage(msg);
	}

	@Override
	public void onFail(FileDownloader loader) {
		// TODO Auto-generated method stub
		// 下载失败
		Message msg = Message.obtain();
		msg.what = DownLoadFlag.FAIL.ordinal();
		sendUiMessage(msg);
	}

	@Override
	public void onUpdate(FileDownloader loader, int progress) {
		// TODO Auto-generated method stub
		// 下载中 
	}
	
	@Override
	public void onFragmentSelected(int page) 
	{
		// 判断是否本页
		if (getIndex() == page)
		{
			// 统计
			AnalyticsFragmentActivity activity = getAnalyticsFragmentActivity();
			if (null != activity) {
				activity.onAnalyticsPageSelected(this, page);
			}
		}
	}
	
	private AnalyticsFragmentActivity getAnalyticsFragmentActivity()
	{
		AnalyticsFragmentActivity activity = null;
		if (getActivity() instanceof AnalyticsFragmentActivity)
		{
			activity = (AnalyticsFragmentActivity)getActivity();
		}
		return activity;
	}
}
