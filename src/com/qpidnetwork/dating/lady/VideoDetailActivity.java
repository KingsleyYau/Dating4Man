package com.qpidnetwork.dating.lady;

import java.util.Arrays;
import java.util.List;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnInfoListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.MediaPlayer.OnSeekCompleteListener;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Display;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.qpidnetwork.dating.R;
import com.qpidnetwork.dating.authorization.LoginManager;
import com.qpidnetwork.dating.bean.RequestFailBean;
import com.qpidnetwork.framework.base.BaseFragmentActivity;
import com.qpidnetwork.framework.util.DateUtil;
import com.qpidnetwork.framework.util.Log;
import com.qpidnetwork.framework.util.ToastUtil;
import com.qpidnetwork.manager.FileCacheManager;
import com.qpidnetwork.request.OnVSPlayVideoCallback;
import com.qpidnetwork.request.OnVSSaveVideoCallback;
import com.qpidnetwork.request.OnVSVideoDetailCallback;
import com.qpidnetwork.request.RequestOperator;
import com.qpidnetwork.request.item.VSPlayVideoItem;
import com.qpidnetwork.request.item.VSVideoDetailItem;
import com.qpidnetwork.tool.ImageViewLoader;
import com.qpidnetwork.view.FlatToast;
import com.qpidnetwork.view.FlatToast.StikyToastType;
import com.qpidnetwork.view.GetMoreCreditDialog;
import com.qpidnetwork.view.MaterialDialogAlert;
import com.qpidnetwork.view.QpidGallery;
import com.qpidnetwork.view.VideoView;
import com.qpidnetwork.view.VideoView.MySizeChangeLinstener;

//videoUrlList(视频列表,数组形式{id/视频ID, time/播放时长，thumb/小截图，photo/大截图})
public class VideoDetailActivity extends BaseFragmentActivity implements
		OnClickListener, OnPreparedListener, OnCompletionListener,
		OnErrorListener, OnInfoListener, OnBufferingUpdateListener,
		OnSeekCompleteListener, MySizeChangeLinstener { 
	
	/**
	 * 其他界面进入参数
	 */
	public static final String WOMANID = "womanId";
	
	private final static String TAG = "VideoDetailActivity";
	
	private static final int GET_VEDIO_LIST_SUCCESS = 0;
	private static final int GET_VEDIO_LIST_FAILED = 1;
	private static final int PLAY_VEDIO_SUCCESS = 2;
	private static final int PLAY_VEDIO_FAILED = 3;
	private static final int ADD_FAVORITE_SUCCESS = 4;
	private static final int ADD_FAVORITE_FAILED = 5;
	
	private static final String INPUT_LADY_ID = "inputLadyId";
	private static final String INPUT_LADY_NAME = "inputLadyName";
	
//	private LinearLayout bottomArea;

//	private RelativeLayout videoDetailRoot;
	private RelativeLayout videoViewLayout;
	private RelativeLayout noPlayLayout;
	private ImageView background_img;
	private ImageButton btnPlay;
	private TextView time_tips;
//	private TextView replay_tips;
	private RelativeLayout prepareProgressLayout;
	private RelativeLayout videoViewTop;
	private ImageButton btnBack;
	private ImageButton btnLadyProfile;
	private RelativeLayout videoViewButtom;
	private ImageView playImageBtn;
	private TextView currentTime;
	private TextView totalTime;
	private ImageView fullImageBtn;
	private LinearLayout video_detail_progress_layout;
//	private TextView progress_layout_sudu;
	private TextView progress_layout_buff;
	private RelativeLayout titleLayout;
	private TextView tv_video_title;
	private TextView tv_video_des;
	private TextView tv_video_interest;
	private LinearLayout ll_gallery_lable;
	private TextView galleryLable;
	private SeekBar seekBar;
	private VideoView videoView;
//	private Handler mainHandler;

//	private AudioManager mAudioManager;
//	private int mMaxVolume;
//	private int mVolume = -1;
//	private float mBrightness = -1f;
	private boolean isPlay;
	private boolean isPlayComplete = true;
	private upDateSeekBar update;
	private boolean isFinish;
	private long pauseSize;
//	private long size;
//	private ArrayList<String> urls;
	private boolean istouched;
	private boolean isCanPlay;
	private boolean isbuffering;
	private boolean isstop;
	public static boolean hasCancel;
	public static int screenWidth = 0;
	public static int screenHeight = 0;
	public static int videoLayoutHeight = 0;
	public static int orientation = 0;

	// private Thread loadingThread;

	private final static int SCREEN_FULL = 0;
	private final static int SCREEN_DEFAULT = 1;

	
	
	class LadyInfo{
		public String ladyId;
		public String ladyName;
		
		public LadyInfo(String id, String name){
			this.ladyId = id;
			this.ladyName = name;
		}
	}
	
	/*data*/
	private LadyInfo mLadyDetail;
	private List<VSVideoDetailItem> mVideoDetailList;
	private int mCurrentPosition = 0; //当前播放的视频索引
	private String mCurrentVideoUrl; //用于记录当前正在播放的视频的url
	private QpidGallery galleryLadyVideos; //视频列表
	private VideoGalleryAdapter videoGalleryAdapter;
	

	
	public static void launchLadyVideoDetailActivity(Context context, String ladyId, String ladyName){
		
		if(LoginManager.getInstance().CheckLogin(context)){
			Intent intent = new Intent(context, VideoDetailActivity.class);
			intent.putExtra(INPUT_LADY_ID, ladyId);
			intent.putExtra(INPUT_LADY_NAME, ladyName);
			context.startActivity(intent);
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		getWindow().setFormat(PixelFormat.TRANSLUCENT);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		getScreenSize();
		setContentView(R.layout.activity_video_detail);
		initView();
		setListener();
		initData();
	}

	private void initView() {

		
		
//		videoDetailRoot = (RelativeLayout) findViewById(R.id.video_detail_root);
		videoViewLayout = (RelativeLayout) findViewById(R.id.video_detail_video_view_layout);
		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
				RelativeLayout.LayoutParams.MATCH_PARENT, videoLayoutHeight);
		videoViewLayout.setLayoutParams(params);
		
		/*未开始播放时，界面展现*/
		noPlayLayout = (RelativeLayout) findViewById(R.id.no_play_layout);
		noPlayLayout.setVisibility(View.VISIBLE);
		background_img = (ImageView) findViewById(R.id.background_img);
		background_img.setVisibility(View.VISIBLE);
		btnPlay = (ImageButton) findViewById(R.id.btnPlay);
		time_tips = (TextView) findViewById(R.id.time_tips);
//		replay_tips = (TextView) findViewById(R.id.replay_tips);
		
		/*加载控制*/
		prepareProgressLayout = (RelativeLayout) findViewById(R.id.video_prepare_progress_layout);
		prepareProgressLayout.setVisibility(View.GONE);
		
		/*播放盘顶部*/
		videoViewTop = (RelativeLayout) findViewById(R.id.video_detail_video_view_top_rl);
		videoViewTop.setVisibility(View.VISIBLE);
		btnBack = (ImageButton) findViewById(R.id.btnBack);
		btnLadyProfile = (ImageButton) findViewById(R.id.btnLadyProfile);
		
		/*播放盘底部*/
		videoViewButtom = (RelativeLayout) findViewById(R.id.video_detail_video_view_buttom_lin);
		videoViewButtom.setVisibility(View.GONE);
		playImageBtn = (ImageView) findViewById(R.id.video_detail_playcontrol_imageview);
		playImageBtn.setClickable(false);
		currentTime = (TextView) findViewById(R.id.video_detail_play_current_time_textview);
		totalTime = (TextView) findViewById(R.id.video_detail_play_total_time_textview);
		fullImageBtn = (ImageView) findViewById(R.id.video_detail_fullscreen_imageview);
		seekBar = (SeekBar) findViewById(R.id.video_detail_seekbar);
		videoView = (VideoView) findViewById(R.id.surface_view);


		/*加载缓存提示*/
		video_detail_progress_layout = (LinearLayout) findViewById(R.id.video_detail_progress_layout);
//		progress_layout_sudu = (TextView) findViewById(R.id.progress_layout_sudu);
		progress_layout_buff = (TextView) findViewById(R.id.progress_layout_buff);
		
		
		titleLayout = (RelativeLayout) findViewById(R.id.video_detail_title_layout);
		tv_video_title = (TextView) findViewById(R.id.tv_video_title);
		tv_video_des = (TextView) findViewById(R.id.tv_video_des);
		tv_video_interest = (TextView) findViewById(R.id.tv_video_interest);
		ll_gallery_lable = (LinearLayout) findViewById(R.id.ll_gallery_lable);
		galleryLable = (TextView) findViewById(R.id.tv_video_gallery_lable);
//		bottomArea = (LinearLayout) findViewById(R.id.bottomArea);


		galleryLadyVideos = (QpidGallery) findViewById(R.id.galleryLadyVideos);
//		mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

		videoGalleryAdapter = new VideoGalleryAdapter(this);
//		mMaxVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
//		mainHandler = new Handler();
		
	}

	private void setListener() {
		/*快进进度监控*/
		seekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				/*快进结束*/
				int value = (int) (seekBar.getProgress()
						* videoView.getDuration() / seekBar.getMax());
				videoView.seekTo(value);
				videoView.start();
				isPlay = true;
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				/*快进开始*/
				isPlay = false;
				videoView.pause();
			}

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {

			}
		});
		/*视频列表点击切换控制*/
		galleryLadyVideos.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				mCurrentVideoUrl = null;
				hasCancel = true;
				isPlayComplete = true;
				isPlay = false;
				seekBar.setProgress(0);
				seekBar.setEnabled(false);
				videoView.releaseMedia();
				video_detail_progress_layout.setVisibility(View.GONE);
				prepareProgressLayout.setVisibility(View.GONE);
				VSVideoDetailItem currVideo = mVideoDetailList.get(position);
				mCurrentPosition = position;
				background_img.setImageDrawable(null);
				background_img.setScaleType(ScaleType.CENTER_CROP);
				/*hunter 下载图片*/
				String localPath = FileCacheManager.getInstance().CacheImagePathFromUrl(currVideo.photoURL);
				new ImageViewLoader(VideoDetailActivity.this).DisplayImage(background_img, currVideo.photoURL, localPath, null);
				
				String videoTitle = currVideo.title;
				if (videoTitle == null || "".equals(videoTitle.trim())) {
					videoTitle = mLadyDetail.ladyName + "\'s video";
				}
				galleryLable.setText(mLadyDetail.ladyName + "\'s videos");
				time_tips.setText(currVideo.time);
				tv_video_title.setText(videoTitle);
				tv_video_des.setText(currVideo.transcription);
				noPlayLayout.setVisibility(View.VISIBLE);
				background_img.setVisibility(View.VISIBLE);
				videoViewButtom.setVisibility(View.GONE);
				playImageBtn.setImageResource(R.drawable.ic_play_circle_outline_white_24dp);
				// 按是否是在播放有效期内切换提示

				String beginTime = currVideo.viewTime1;
				String endTime = currVideo.viewTime2;
				if (beginTime != null && endTime != null) {
					if (endTime.trim().equals("")|| beginTime.trim().equals("")) {// 不在免费期的返回""
						isCanPlay = false;
					} else {
						isCanPlay = true;
					}
				}

				if (currVideo.videoFav) {
				} else {

				}

			}
		});

		
		videoViewLayout.setOnClickListener(this);
		playImageBtn.setOnClickListener(this);
		fullImageBtn.setOnClickListener(this);
		btnPlay.setOnClickListener(this);
		btnBack.setOnClickListener(this);
		btnLadyProfile.setOnClickListener(this);
		tv_video_interest.setOnClickListener(this);
		update = new upDateSeekBar();
		new Thread(update).start();

	}

	private void initData() {
		
		Bundle bundle = getIntent().getExtras();
		if(bundle == null || !bundle.containsKey(INPUT_LADY_ID) || !bundle.containsKey(INPUT_LADY_NAME)){
			finish();
			return;
		}
		
		mLadyDetail = new LadyInfo(bundle.getString(INPUT_LADY_ID), bundle.getString(INPUT_LADY_NAME));

		if(mLadyDetail == null){
			showInitLoadError();
		}else{
			queryVideoDetailList();
		}
	}

	private void play(String url) {
		Log.i("play", url);
		hasCancel = false;
		try {
			videoView.setVideoPath(url);
			videoView.setOnPreparedListener(this);
			videoView.setOnCompletionListener(this);
			videoView.setOnErrorListener(this);
			videoView.setOnInfoListener(this);
			videoView.setOnBufferingUpdateListener(this);
			videoView.setOnSeekCompleteListener(this);
			videoView.setMySizeChangeLinstener(this);
			videoView.start();

		} catch (Exception e) {
			Log.i("hck", "PlayActivity " + e.toString());
		}
	}

	@SuppressLint("HandlerLeak")
	@Override
	public void onClick(View v) {
		// Log.i("onClick", "R.id.video_detail_playcontrol_imageview");
		switch (v.getId()) {
		case R.id.video_detail_playcontrol_imageview:
			if (isPlayComplete) {
				if (mCurrentVideoUrl != null && !"".equals(mCurrentVideoUrl.trim())) {
					play(mCurrentVideoUrl);
					prepareProgressLayout.setVisibility(View.VISIBLE);
					playImageBtn.setImageResource(R.drawable.ic_pause_circle_outline_white_24dp);
					videoView.start();
					isPlay = true;
					seekBar.setEnabled(true);
					videoViewTop.setVisibility(View.VISIBLE);
					videoViewButtom.setVisibility(View.VISIBLE);
					noPlayLayout.setVisibility(View.GONE);
				}

			} else {
				if (isPlay) {
					videoView.pause();
					playImageBtn.setImageResource(R.drawable.ic_play_circle_outline_white_24dp);
					isPlay = false;
					seekBar.setEnabled(false);
					videoViewTop.setVisibility(View.VISIBLE);
					videoViewButtom.setVisibility(View.GONE);
					noPlayLayout.setVisibility(View.VISIBLE);
					background_img.setVisibility(View.INVISIBLE);
					progress_layout_buff.setVisibility(View.GONE);
					video_detail_progress_layout.setVisibility(View.GONE);
				} else {
					playImageBtn.setImageResource(R.drawable.ic_pause_circle_outline_white_24dp);
					videoView.start();
					isPlay = true;
					seekBar.setEnabled(true);
					videoViewTop.setVisibility(View.VISIBLE);
					videoViewButtom.setVisibility(View.VISIBLE);
					noPlayLayout.setVisibility(View.GONE);
				}
			}

			break;
		case R.id.btnPlay:
			
			if (mVideoDetailList == null || mVideoDetailList.size() == 0 ) return;
			
			if (isCanPlay) {
				if (isPlayComplete) {
					if (mCurrentVideoUrl != null && !"".equals(mCurrentVideoUrl.trim())) {
						play(mCurrentVideoUrl);
						prepareProgressLayout.setVisibility(View.VISIBLE);
						playImageBtn
								.setImageResource(R.drawable.ic_pause_circle_outline_white_24dp);
						isPlay = true;
						seekBar.setEnabled(true);
						videoViewTop.setVisibility(View.VISIBLE);
						videoViewButtom.setVisibility(View.VISIBLE);
						noPlayLayout.setVisibility(View.GONE);
						background_img.setVisibility(View.INVISIBLE);
					} else {
						playVideo();
					}

				} else {
					playImageBtn.setImageResource(R.drawable.ic_pause_circle_outline_white_24dp);
					videoView.start();
					isPlay = true;
					seekBar.setEnabled(true);
					videoViewTop.setVisibility(View.VISIBLE);
					videoViewButtom.setVisibility(View.VISIBLE);
					noPlayLayout.setVisibility(View.GONE);
					
				}
			} else {
				MaterialDialogAlert dialog = new MaterialDialogAlert(VideoDetailActivity.this);
				dialog.setMessage(getString(R.string.video_cost_desc));
				dialog.addButton(dialog.createButton(getString(R.string.btn_play), new OnClickListener(){

					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						playVideo();
					}
					
				}));
				
				dialog.addButton(dialog.createButton(getString(R.string.common_btn_cancel), null));
				dialog.show();

			} 
			break;
		case R.id.video_detail_fullscreen_imageview:
			if (getRequestedOrientation() != ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
				setVideoScale(SCREEN_FULL);
			} else {
				setVideoScale(SCREEN_DEFAULT);
			}

			break;
		case R.id.btnBack:
			if (getRequestedOrientation() == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
				setVideoScale(SCREEN_DEFAULT);
			}else{
				finish();
			}
			break;
		case R.id.tv_video_interest:
			VSVideoDetailItem currVideo = mVideoDetailList.get(mCurrentPosition);
			if (currVideo.videoFav) {// 如果收藏过的就删除，没收藏过的就收藏
				ToastUtil.showToast(VideoDetailActivity.this, R.string.msg_saved_video);
			} else {
				storeFavoriteVideo(currVideo.id);
			}

			break;
		case R.id.video_detail_video_view_layout:
			if (istouched) {
				videoViewTop.setVisibility(View.VISIBLE);
				videoViewButtom.setVisibility(View.VISIBLE);
				istouched = false;
			} else {
				//operation_volume_brightness.setVisibility(View.GONE);
				videoViewTop.setVisibility(View.GONE);
				videoViewButtom.setVisibility(View.GONE);
				istouched = true;
			}
			break;
		case R.id.btnLadyProfile:
			if (mLadyDetail.ladyId != null && mLadyDetail.ladyId.length() > 0)
			LadyDetailActivity.launchLadyDetailActivity(this, mLadyDetail.ladyId, true);
			break;
			
		}
	}

	/**
	 * 解析视频Url
	 * @param videourl
	 * @return
	 */
	private static String parseVideoUrl(String videourl) {
		if (videourl == null || videourl.trim().length() == 0 ) return "";
		
		String[] arrStr = videourl.split("\\|\\|\\|");
		if (arrStr.length < 10) return "";
		String source = arrStr[0] + "/videofiles/processed/" + arrStr[6] + "/"
				+ (int) Math.ceil(Double.parseDouble((arrStr[3])) / 200) + "/"
				+ arrStr[1] + "-" + arrStr[9] + ".mp4";
		
		return source;
	}

	@SuppressWarnings("deprecation")
	private void getScreenSize() {


		Point size = new Point();
		Display display = this.getWindowManager().getDefaultDisplay();
		if (Build.VERSION.SDK_INT > 12){
			display.getSize(size);
		}else{
			size.y = display.getHeight();
			size.x = display.getWidth();
		}
		
		screenHeight = size.y;
		screenWidth = size.x;
		videoLayoutHeight = (12 * (screenWidth / 16));
		// 屏幕宽度减去左右间距再以相应比例设置
	}

	public int dip2px(Context context, float dipValue) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (dipValue * scale + 0.5f);
	}

	private void setVideoScale(int flag) {

		LayoutParams lp = videoViewLayout.getLayoutParams();

		if (flag == SCREEN_FULL){
			titleLayout.setVisibility(View.GONE);
			ll_gallery_lable.setVisibility(View.GONE);
			fullImageBtn.setImageResource(R.drawable.ic_launch_back_white_24dp);
			lp.height = screenWidth;
			lp.width = LayoutParams.MATCH_PARENT;
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
			getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
			videoView.setVideoScale(screenHeight, screenWidth);
		}else{
			titleLayout.setVisibility(View.VISIBLE);
			ll_gallery_lable.setVisibility(View.VISIBLE);
			fullImageBtn.setImageResource(R.drawable.ic_launch_white_24dp);
			lp.height = videoLayoutHeight;
			lp.width = screenWidth;
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
			getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
			videoView.setVideoScale(screenWidth, videoLayoutHeight);
		}
		
		videoViewLayout.setLayoutParams(lp);
		
		

	}

	class upDateSeekBar implements Runnable {

		@Override
		public void run() {
			if (!isFinish) {
				mHandler.sendMessage(Message.obtain());
				mHandler.postDelayed(update, 1000);
			}

		}
	}

	@SuppressLint("HandlerLeak")
	Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			/*播放进度跟踪*/
			if (videoView == null) {
				return;
			}
			currentTime.setText(DateUtil.generateTime(videoView
					.getCurrentPosition()));
			seekBar(videoView.getCurrentPosition());
		};
	};
	
	/*更新进度条界面*/
	private void seekBar(long size) {
		if (videoView.isPlaying()) {
			long mMax = videoView.getDuration();
			int sMax = seekBar.getMax();
			if (mMax != 0) {
				seekBar.setProgress((int) (size * sMax / mMax));
			}
		}
	}



	private Handler disHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			if (msg.what == 0) {
				//operation_volume_brightness.setVisibility(View.GONE);
			} else {
				istouched = true;
				videoViewButtom.setVisibility(View.GONE);
				if (isPlay) {
					videoViewTop.setVisibility(View.GONE);
				}

			}

		}
	};

	@Override
	protected void onPause() {
		super.onPause();
		Log.i(TAG, "onPause");
		pauseSize = videoView.getCurrentPosition();
		if (isPlay) {
			videoView.pause();
			playImageBtn.setImageResource(R.drawable.ic_play_circle_outline_white_24dp);
			seekBar.setEnabled(false);
			noPlayLayout.setVisibility(View.VISIBLE);
			videoViewTop.setVisibility(View.VISIBLE);
			videoViewButtom.setVisibility(View.GONE);

		}

	}

	@Override
	protected void onResume() {
		super.onResume();
		Log.i(TAG, "onResume");
		if (pauseSize > 0) {
			videoView.seekTo(pauseSize);
			pauseSize = 0;
			if (isstop) {
				prepareProgressLayout.setVisibility(View.VISIBLE);
			}
		}
		if (isPlay) {
			videoView.start();
			playImageBtn.setImageResource(R.drawable.ic_pause_circle_outline_white_24dp);
			seekBar.setEnabled(true);
			noPlayLayout.setVisibility(View.GONE);
			videoViewTop.setVisibility(View.VISIBLE);
			videoViewButtom.setVisibility(View.VISIBLE);
		}
		isstop = false;
	}

	@Override
	protected void onStop() {
		super.onStop();
		Log.i(TAG, "onStop");
		isstop = true;
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		
		
		hideProgressDialog();
		cancelToastImmediately();
		
		if (videoView != null) {
			videoView.stopPlayback();
			videoView = null;
		}
		isPlay = false;
		isFinish = true;
		System.gc();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		orientation = this.getResources().getConfiguration().orientation;
	}

	@Override
	public void onPrepared(MediaPlayer arg0) {
		if (hasCancel) {
			videoView.releaseMedia();
			return;
		}
		video_detail_progress_layout.setVisibility(View.GONE);
		prepareProgressLayout.setVisibility(View.GONE);
		disHandler.removeMessages(1);
		disHandler.sendEmptyMessageDelayed(1, 500);
		isPlayComplete = false;
		totalTime.setText(DateUtil.generateTime(videoView.getDuration()));
		time_tips.setText(DateUtil.generateTime(videoView.getDuration()));
		if (pauseSize > 0) {
			videoView.seekTo(pauseSize);
			pauseSize = 0;
		}
		if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		} else if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		}
	}

	@Override
	public void onCompletion(MediaPlayer arg0) {
		videoView.stopPlayback();
		// videoView.releaseMedia();
		isPlayComplete = true;
		isPlay = false;
		videoViewButtom.setVisibility(View.GONE);
		videoViewTop.setVisibility(View.VISIBLE);
		seekBar.setProgress(0);
		seekBar.setEnabled(false);
		noPlayLayout.setVisibility(View.VISIBLE);
	}

	@Override
	public boolean onError(MediaPlayer arg0, int arg1, int arg2) {
		return false;
	}

	@Override
	public boolean onInfo(MediaPlayer mp, int what, int extra) {
		// TODO Auto-generated method stub
		// Well apparently it's a known bug in Android. When playing HLS stream
		// it's just never calls OnInfoListener or OnBuffering.
		switch (what) {
		case MediaPlayer.MEDIA_INFO_BUFFERING_START:
			isbuffering = true;
			progress_layout_buff.setVisibility(View.VISIBLE);
			video_detail_progress_layout.setVisibility(View.VISIBLE);
			break;
		case MediaPlayer.MEDIA_INFO_BUFFERING_END:
			isbuffering = false;
			progress_layout_buff.setVisibility(View.GONE);
			video_detail_progress_layout.setVisibility(View.GONE);
			break;
		case MediaPlayer.MEDIA_INFO_BAD_INTERLEAVING:// 当文件中的音频和视频数据不正确的交错时，将触发如下操作。
			break;
		case MediaPlayer.MEDIA_INFO_METADATA_UPDATE:// 当心的元数据可用时，将触发它，android
			break;
		case MediaPlayer.MEDIA_INFO_NOT_SEEKABLE:// 媒体不能正确定位，意味着它可能是一个在线流
			break;
		case MediaPlayer.MEDIA_INFO_VIDEO_TRACK_LAGGING:// 当无法播放视频时，可能是将要播放视频，但是视频太复杂
			break;
		case MediaPlayer.MEDIA_INFO_UNKNOWN:
			break;
		}
		return true;
	}

	public void onBufferInfo(int what) {
		switch (what) {
		case MediaPlayer.MEDIA_INFO_BUFFERING_START:
			Log.i("onBufferInfo", "MediaPlayer.MEDIA_INFO_BUFFERING_START---"
					+ what);
			isbuffering = true;
			if (isPlay) {
				seekBar.setEnabled(false);
				videoView.pause();
				isPlay = false;
				playImageBtn.setImageResource(R.drawable.ic_play_circle_outline_white_24dp);
				progress_layout_buff.setVisibility(View.VISIBLE);
				video_detail_progress_layout.setVisibility(View.VISIBLE);
			}
			break;
		case MediaPlayer.MEDIA_INFO_BUFFERING_END:
			isbuffering = false;
			if (!isPlay) {
				seekBar.setEnabled(true);
				isPlay = true;
				videoView.start();
				playImageBtn.setImageResource(R.drawable.ic_pause_circle_outline_white_24dp);
				progress_layout_buff.setVisibility(View.GONE);
				video_detail_progress_layout.setVisibility(View.GONE);
			}
			break;
		}
	}

	@Override
	public void onBufferingUpdate(MediaPlayer arg0, int arg1) {
//		Log.i("onBufferingUpdate",
//				videoView.getBufferPercentage()
//						+ "%"
//						+ "---"
//						+ arg1
//						+ "%-------"
//						+ videoView.getCurrentPosition()
//						+ "----"
//						+ videoView.getDuration()
//						+ "---"
//						+ (int) (100 * ((float) videoView.getCurrentPosition() / (float) videoView
//								.getDuration())));
		if (isbuffering) {
			progress_layout_buff.setText(videoView.getBufferPercentage() + "%");
		}
	}

	@Override
	public void onSeekComplete(MediaPlayer arg0) {
		
	}

	@Override
	public void doMyThings() {
		// TODO Auto-generated method stub
		if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
			orientation = Configuration.ORIENTATION_LANDSCAPE;
		} else if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
			orientation = Configuration.ORIENTATION_PORTRAIT;
		}

	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK
				&& event.getAction() == KeyEvent.ACTION_DOWN
				&& event.getRepeatCount() == 0) {
			if (getRequestedOrientation() == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
				setVideoScale(SCREEN_DEFAULT);
				return false;
			}

		}
		
		return super.onKeyDown(keyCode, event);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	protected void handleUiMessage(Message msg) {
		// TODO Auto-generated method stub
		super.handleUiMessage(msg);
		switch (msg.what) {
		case GET_VEDIO_LIST_SUCCESS:
			mVideoDetailList = (List<VSVideoDetailItem>)msg.obj;
			
			if (mVideoDetailList == null || mVideoDetailList.size() == 0){
				showInitLoadError();
				break;
			}
			
			videoGalleryAdapter.setVideoList(mVideoDetailList);
			galleryLadyVideos.setAdapter(videoGalleryAdapter);
			hideInitLoading();
			showAll();
			break;
		case GET_VEDIO_LIST_FAILED:
			showInitLoadError();		
			break;
		case PLAY_VEDIO_SUCCESS:
			
			cancelToastImmediately();
			
			VSPlayVideoItem playVideoItem = (VSPlayVideoItem)msg.obj;
			mCurrentVideoUrl = parseVideoUrl(playVideoItem.videoURL);
			mVideoDetailList.get(mCurrentPosition).viewTime1 = playVideoItem.viewTime1;
			mVideoDetailList.get(mCurrentPosition).viewTime2 = playVideoItem.viewTime2;
			isCanPlay = true;
			playImageBtn.setClickable(true);
			play(mCurrentVideoUrl);
			
			if (mCurrentVideoUrl.trim().length() == 0 ){
				FlatToast.showStickToast(VideoDetailActivity.this, "Failed", StikyToastType.FAILED);
				return;
			}
			
			prepareProgressLayout.setVisibility(View.VISIBLE);
			playImageBtn.setImageResource(R.drawable.ic_pause_circle_outline_white_24dp);
			videoView.start();
			isPlay = true;
			seekBar.setEnabled(true);
			videoViewTop.setVisibility(View.VISIBLE);
			videoViewButtom.setVisibility(View.VISIBLE);
			noPlayLayout.setVisibility(View.GONE);
			background_img.setVisibility(View.INVISIBLE);
			break;
		case PLAY_VEDIO_FAILED:
			
			cancelToastImmediately();
			
			RequestFailBean bean = (RequestFailBean)msg.obj;
			if ("MBCE45003".equals(bean.errno.trim())) {
				GetMoreCreditDialog dialog = new GetMoreCreditDialog(VideoDetailActivity.this);
				dialog.show();
				break;
			}
			
			if (bean.errmsg != null) {
				FlatToast.showStickToast(VideoDetailActivity.this, "Failed", StikyToastType.FAILED);
			}
			
			break;
		case ADD_FAVORITE_SUCCESS:
			
			break;
		case ADD_FAVORITE_FAILED:
			
			break;
		}
	}

	/**
	 * 收藏视频
	 * 
	 * @param ladyId
	 */
	private void storeFavoriteVideo(String videoid) {
		RequestOperator.getInstance().SaveVideo(videoid, new OnVSSaveVideoCallback() {
			
			@Override
			public void OnVSSaveVideo(boolean isSuccess, String errno, String errmsg) {
				// TODO Auto-generated method stub
				
			}
		});
	}

	/**
	 * 查询指定女士视频信息详情
	 * 
	 * @param ladyId
	 */
	private void queryVideoDetailList() {
		
		showProgressDialog("Loading");
		
		RequestOperator.getInstance().VideoDetail(mLadyDetail.ladyId, new OnVSVideoDetailCallback() {
			
			@Override
			public void OnVSVideoDetail(boolean isSuccess, String errno, String errmsg,
					VSVideoDetailItem[] item) {
				Message msg = Message.obtain();
				if( isSuccess && (item != null)){
					msg.what = GET_VEDIO_LIST_SUCCESS;
					msg.obj = Arrays.asList(item);
				}else{
					RequestFailBean bean = new RequestFailBean(errno, errmsg);
					msg.what = GET_VEDIO_LIST_FAILED;
					msg.obj = bean;
				}
				sendUiMessage(msg);
			}
		});
	}

	/**
	 * 播放视频
	 * 
	 * @param ladyId
	 */
	private void playVideo() {
		
		showToastProgressing("Loading");
		
		RequestOperator.getInstance().PlayVideo(mLadyDetail.ladyId, mVideoDetailList.get(mCurrentPosition).id, new OnVSPlayVideoCallback() {
			
			@Override
			public void OnVSPlayVideo(boolean isSuccess, String errno, String errmsg,
					VSPlayVideoItem item) {
				Message msg = Message.obtain();
				if( isSuccess && (item != null)){
					msg.what = PLAY_VEDIO_SUCCESS;
					msg.obj = item;
				}else{
					RequestFailBean bean = new RequestFailBean(errno, errmsg);
					msg.what = PLAY_VEDIO_FAILED;
					msg.obj = bean;
				}
				sendUiMessage(msg);
			}
		});
	}

	private void showAll() {
		if( mVideoDetailList == null || mVideoDetailList.size() == 0 ) {
			return;
		}
		mCurrentVideoUrl = null;
		hasCancel = true;
		isPlayComplete = true;
		isPlay = false;
		seekBar.setProgress(0);
		seekBar.setEnabled(false);
		
		VSVideoDetailItem video = mVideoDetailList.get(0);
		
		background_img.setImageDrawable(null);
		background_img.setScaleType(ScaleType.CENTER_CROP);
		/*hunter 下载缩略图*/
		String localPath = FileCacheManager.getInstance().CacheImagePathFromUrl(video.photoURL);
		new ImageViewLoader(VideoDetailActivity.this).DisplayImage(background_img, video.photoURL, localPath, null);
		
		String videoTitle = video.title;
		if (videoTitle == null || "".equals(videoTitle.trim())) {
			videoTitle = mLadyDetail.ladyName + "\'s video";
		}
		noPlayLayout.setVisibility(View.VISIBLE);
		background_img.setVisibility(View.VISIBLE);
		videoViewButtom.setVisibility(View.GONE);
		playImageBtn.setImageResource(R.drawable.ic_play_circle_outline_white_24dp);

		time_tips.setText(video.time);
		tv_video_title.setText(videoTitle);
		tv_video_des.setText(video.transcription);
		galleryLable.setText(mLadyDetail.ladyName + "'s videos");
		
		String beginTime = video.viewTime1;
		String endTime = video.viewTime2;
		
		if (beginTime != null && endTime != null) {
			if (endTime.trim().equals("") || beginTime.trim().equals("")) {// 不在免费期的返回""
				isCanPlay = false;
			} else {
				isCanPlay = true;
			}
		}
		
		
		if (video.videoFav) {
		} else {
		}
		
		galleryLadyVideos.setItemSelected(0);
	}


	
	/*初始化加载异常提示*/
	private void showInitLoadError(){
		hideProgressDialog();
		MaterialDialogAlert dialog = new MaterialDialogAlert(this);
		dialog.setCancelable(false);
		dialog.setMessage(getString(R.string.can_not_load_video_please_try_again));
		dialog.addButton(dialog.createButton(getString(R.string.common_btn_retry), new OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				initData();
			}
			
		}));
		
		dialog.addButton(dialog.createButton(getString(R.string.common_btn_cancel), new OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				finish();
			}
			
		}));
		if(isActivityVisible()){
			dialog.show();
		}
	}
	
	/*加载成功收起加载进度*/
	private void hideInitLoading(){
		hideProgressDialog();
	}
	
}