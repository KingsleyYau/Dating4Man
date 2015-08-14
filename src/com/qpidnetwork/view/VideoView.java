package com.qpidnetwork.view;

import java.io.IOException;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnInfoListener;
import android.media.MediaPlayer.OnSeekCompleteListener;
import android.net.Uri;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.MediaController;
import android.widget.MediaController.MediaPlayerControl;

import com.qpidnetwork.dating.lady.VideoDetailActivity;
import com.qpidnetwork.framework.util.Log;

/**
 * Displays a video file. The VideoView class can load images from various
 * sources (such as resources or content providers), takes care of computing its
 * measurement from the video so that it can be used in any layout manager, and
 * provides various display options such as scaling and tinting.
 */
public class VideoView extends SurfaceView implements MediaPlayerControl {
	private String TAG = "VideoView";
	/**
	 * 视频URI路径
	 */
	private Uri mUri;
	private String mPath;

	/**
	 * 音频、视频文件播放时间总长度
	 */
	private int mDuration;
	/**
	 * 上下文，或者说坏境变量
	 */
	private Context mContext;
	
	// all possible internal states
	private static final int STATE_ERROR = -1;
	/**
	 * MediaPlayer的空闲状态
	 */
	private static final int STATE_IDLE = 0;
	/**
	 * MediaPlayer正在准备的状态
	 */
	private static final int STATE_PREPARING = 1;
	/**
	 * MediaPlayer准备好了的状态
	 */
	private static final int STATE_PREPARED = 2;
	/**
	 * MediaPlayer播放中的状态
	 */
	private static final int STATE_PLAYING = 3;
	/**
	 * MediaPlayer暂停的状态
	 */
	private static final int STATE_PAUSED = 4;
	/**
	 * MediaPlayer播放完成的状态
	 */
	private static final int STATE_PLAYBACK_COMPLETED = 5;

	// mCurrentState is a VideoView object's current state.
	// mTargetState is the state that a method caller intends to reach.
	// For instance, regardless the VideoView object's current state,
	// calling pause() intends to bring the object to a target state
	// of STATE_PAUSED.
	/**
	 * 默认是空闲状态
	 */
	private int mCurrentState = STATE_IDLE;
	/**
	 * 默认是空闲状态
	 */
	private int mTargetState = STATE_IDLE;

	// All the stuff we need for playing and showing a video
	/**
	 * 播放和显示视频需要,是显示视频的帮助类。
	 */
	private SurfaceHolder mSurfaceHolder = null;

	private Canvas canvas;
	/**
	 * 播放视频、音乐其实就是靠这个类。
	 */
	private MediaPlayer mMediaPlayer = null;
	/**
	 * 视频文件播放时显示宽度
	 */
	private int mVideoWidth;
	/**
	 * 视频文件播放时显示高度
	 */
	private int mVideoHeight;
	/**
	 * 视频文件播放时显示高度
	 */
	private int mSurfaceWidth;
	private int mSurfaceHeight;
	/**
	 * 媒体控制面板，上面有暂停、快进、快退、拖动条等按钮和控件。
	 */
	private MediaController mMediaController;
	/**
	 * 
	 * 加载视频缓冲区的百分比，用于提示用户，视频加载多少了，好不让用户等着急。
	 */
	private int mCurrentBufferPercentage;

	private int mSeekWhenPrepared; // recording the seek position while
	/**
	 * 是否能暂停
	 */
	private boolean mCanPause;
	/**
	 * 是否能快退
	 */
	private boolean mCanSeekBack;
	/**
	 * 是否能快进
	 */
	private boolean mCanSeekForward;

	/**
	 * 监听器，当视频、音频、视频播放完后MediaPlayer会告诉监听器-“嘿我播放完了”
	 */
	private OnCompletionListener mOnCompletionListener;
	/**
	 * add by yangguangfu 监听器，当视频、音频、视频播放前得缓冲
	 */
	private OnBufferingUpdateListener mOnBufferingUpdateListener;
	/**
	 * 监听器，当视频、音频、视频播放前准备好了MediaPlayer会告诉该监听器-“嘿准备好可以播放了调用我开始播放了”
	 */
	private MediaPlayer.OnPreparedListener mOnPreparedListener;
	/**
	 * 
	 * 监听器，MediaPlayer加载视频时，或者播放视频时，如果出错。MediaPlayer会告诉该监听器-
	 * "哥们你让我播放的视频格式是AVI啊，我不支持，我不干了"
	 */
	private OnErrorListener mOnErrorListener;
	/**
	 * 自定义监听器，用于监听屏蔽大小是否改变
	 */
	private MySizeChangeLinstener mMyChangeLinstener;

	private OnInfoListener mOnInfoListener;

	private OnSeekCompleteListener mOnSeekCompleteListener;

	private MediaPlayer.OnCompletionListener mCompletionListener = new MediaPlayer.OnCompletionListener() {
		public void onCompletion(MediaPlayer mp) {
			Log.i("onCompletion", "onCompletion");
			mCurrentState = STATE_PLAYBACK_COMPLETED;
			mTargetState = STATE_PLAYBACK_COMPLETED;
			if (mMediaController != null) {
				mMediaController.hide();
			}
			if (mOnCompletionListener != null) {
				mOnCompletionListener.onCompletion(mMediaPlayer);
			}
		}
	};

	private MediaPlayer.OnBufferingUpdateListener mBufferingUpdateListener = new MediaPlayer.OnBufferingUpdateListener() {
		public void onBufferingUpdate(MediaPlayer mp, int percent) {
			Log.i("onBufferingUpdate", "onBufferingUpdate");
			mCurrentBufferPercentage = percent;
			// Log.i(TAG, "videoView"+String.valueOf(percent));
			if (mOnBufferingUpdateListener != null) {
				mOnBufferingUpdateListener.onBufferingUpdate(mMediaPlayer,
						percent);
			}

		}
	};

	MediaPlayer.OnVideoSizeChangedListener mSizeChangedListener = new MediaPlayer.OnVideoSizeChangedListener() {
		public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
			Log.i("onVideoSizeChanged", "onVideoSizeChanged");
			mVideoWidth = mp.getVideoWidth();
			mVideoHeight = mp.getVideoHeight();
			int mWidth1 = 0;
			int mHeight1 = 0;
			Log.i("mSurfaceWidth---mSurfaceHeight", mSurfaceWidth + "---"
					+ mSurfaceHeight);
			Log.i("mVideoWidth---mVideoHeight", mVideoWidth + "---"
					+ mVideoHeight);
			Log.i("width---height", width + "---"
					+ height);
			if (mMyChangeLinstener != null) {
				mMyChangeLinstener.doMyThings();
			}
			if (mVideoWidth != 0 && mVideoHeight != 0) {
				if (VideoDetailActivity.orientation == Configuration.ORIENTATION_LANDSCAPE) {
					mWidth1 = VideoDetailActivity.screenWidth;
					mHeight1 = VideoDetailActivity.screenHeight;
				} else if (VideoDetailActivity.orientation == Configuration.ORIENTATION_PORTRAIT) {
					mWidth1 = VideoDetailActivity.screenWidth;
					mHeight1 = VideoDetailActivity.videoLayoutHeight;
				}
				if (mVideoWidth > 0 && mVideoHeight > 0) {
					if (mVideoWidth * mHeight1 > mWidth1 * mVideoHeight) {
						mHeight1 = mWidth1 * mVideoHeight / mVideoWidth;
					} else if (mVideoWidth * mHeight1 < mWidth1 * mVideoHeight) {
						mWidth1 = mHeight1 * mVideoWidth / mVideoHeight;
					}
				}
				Log.i("screenWidth---mHeight1", VideoDetailActivity.screenWidth
						+ "---" + VideoDetailActivity.videoLayoutHeight);
				Log.i("mVideoWidth---mVideoHeight", mVideoWidth + "---"
						+ mVideoHeight);
				Log.i("mWidth1---mHeight1", mWidth1 + "---" + mHeight1);
				setVideoScale(mWidth1, mHeight1);
			}
		}
	};

	MediaPlayer.OnPreparedListener mPreparedListener = new MediaPlayer.OnPreparedListener() {
		public void onPrepared(MediaPlayer mp) {
			Log.i("onPrepared", "onPrepared");
			if (VideoDetailActivity.hasCancel) {
				releaseMedia();
				return;
			}
			mCurrentState = STATE_PREPARED;
			//mTargetState = STATE_PLAYING;
			// // Get the capabilities of the player for this stream
			// Metadata data =
			// mp.getMetadata(MediaPlayer.METADATA_ALL,MediaPlayer.BYPASS_METADATA_FILTER);
			// if (data != null) {
			// mCanPause = !data.has(Metadata.PAUSE_AVAILABLE) ||
			// data.getBoolean(Metadata.PAUSE_AVAILABLE);
			// mCanSeekBack = !data.has(Metadata.SEEK_BACKWARD_AVAILABLE) ||
			// data.getBoolean(Metadata.SEEK_BACKWARD_AVAILABLE);
			// mCanSeekForward = !data.has(Metadata.SEEK_FORWARD_AVAILABLE) ||
			// data.getBoolean(Metadata.SEEK_FORWARD_AVAILABLE);
			// } else {
			// mCanPause = mCanSeekForward = mCanSeekForward = true;
			// }

			mCanPause = mCanSeekBack = mCanSeekForward = true;
			if (mMediaController != null) {
				mMediaController.setEnabled(true);
			}
			
			int seekToPosition = mSeekWhenPrepared; // mSeekWhenPrepared may be
			// changed after seekTo()
			// call
			if (seekToPosition != 0) {
				seekTo(seekToPosition);
			}
			mVideoWidth = mp.getVideoWidth();
			mVideoHeight = mp.getVideoHeight();
			if (mVideoWidth != 0 && mVideoHeight != 0) {
				// Log.i("@@@@", "video size: " + mVideoWidth +"/"+
				// mVideoHeight);
				int mWidth1 = 0;
				int mHeight1 = 0;
				if (VideoDetailActivity.orientation == Configuration.ORIENTATION_LANDSCAPE) {
					mWidth1 = VideoDetailActivity.screenWidth;
					mHeight1 = VideoDetailActivity.screenHeight;
				} else if (VideoDetailActivity.orientation == Configuration.ORIENTATION_PORTRAIT) {
					mWidth1 = VideoDetailActivity.screenWidth;
					mHeight1 = VideoDetailActivity.videoLayoutHeight;
				}
				if (mVideoWidth > 0 && mVideoHeight > 0) {
					if (mVideoWidth * mHeight1 > mWidth1 * mVideoHeight) {
						mHeight1 = mWidth1 * mVideoHeight / mVideoWidth;
					} else if (mVideoWidth * mHeight1 < mWidth1 * mVideoHeight) {
						mWidth1 = mHeight1 * mVideoWidth / mVideoHeight;
					}
				}
				Log.i("screenWidth---mHeight1", VideoDetailActivity.screenWidth
						+ "---" + VideoDetailActivity.videoLayoutHeight);
				Log.i("mSurfaceWidth---mSurfaceHeight", mSurfaceWidth + "---" + mSurfaceHeight);
				Log.i("mVideoWidth---mVideoHeight", mVideoWidth + "---"
						+ mVideoHeight);
				Log.i("mWidth1---mHeight1", mWidth1 + "---" + mHeight1);
				Log.i("mTargetState",""+ mTargetState);
				setVideoScale(mWidth1, mHeight1);
				if (mSurfaceWidth == mVideoWidth
						&& mSurfaceHeight == mVideoHeight) {
					// We didn't actually change the size (it was already at the
					// size we need), so we won't get a "surface changed"
					// callback,
					// so start the video here instead of in the callback.
					if (mTargetState == STATE_PLAYING) {
						start();
						if (mMediaController != null) {
							mMediaController.show();
						}
					} else if (!isPlaying()
							&& (seekToPosition != 0 || getCurrentPosition() > 0)) {
						if (mMediaController != null) {
							// Show the media controls when we're paused into a
							// video and make 'em stick.
							mMediaController.show(0);
						}
					}
				}
			} else {
				// We don't know the video size yet, but should start anyway.
				// The video size might be reported to us later.
				if (mTargetState == STATE_PLAYING) {
					start();
				}
			}
			if (mOnPreparedListener != null) {
				mOnPreparedListener.onPrepared(mMediaPlayer);
			}
		}
	};

	private MediaPlayer.OnErrorListener mErrorListener = new MediaPlayer.OnErrorListener() {
		public boolean onError(MediaPlayer mp, int framework_err, int impl_err) {
			Log.i("onError", "onError");
			Log.d(TAG, "Error: " + framework_err + "," + impl_err);
			mCurrentState = STATE_ERROR;
			mTargetState = STATE_ERROR;
			if (mMediaController != null) {
				mMediaController.hide();
			}
			/* If an error handler has been supplied, use it and finish. */
			if (mOnErrorListener != null) {
				if (mOnErrorListener.onError(mMediaPlayer, framework_err,
						impl_err)) {
					return true;
				}
			}
			/*
			 * Otherwise, pop up an error dialog so the user knows that
			 * something bad has happened. Only try and pop up the dialog if
			 * we're attached to a window. When we're going away and no longer
			 * have a window, don't bother showing the user an error.
			 */
			if (getWindowToken() != null) {
				// Resources r = mContext.getResources();
				// int messageId;
				//
				// if (framework_err
				// ==MediaPlayer.MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK)
				// {
				// messageId
				// =com.android.internal.R.string.VideoView_error_text_invalid_progressive_playback;
				// } else {
				// messageId
				// =com.android.internal.R.string.VideoView_error_text_unknown;
				// }
				// new
				// AlertDialog.Builder(mContext).setTitle(com.android.internal.R.string.VideoView_error_title)
				// .setMessage(messageId)
				// .setPositiveButton(com.android.internal.R.string.VideoView_error_button,
				// new DialogInterface.OnClickListener(){
				// public void onClick(DialogInterface dialog,int whichButton) {
				// If we get here, there is no onErrorlistener, so at least
				// inform them
				// that the video is over.
				// if (mOnCompletionListener !=null) {
				// mOnCompletionListener.onCompletion(mMediaPlayer); } } })
				// .setCancelable(false).show();

			}
			return true;
		}
	};

	private OnSeekCompleteListener mSeekCompleteListener = new MediaPlayer.OnSeekCompleteListener() {

		@Override
		public void onSeekComplete(MediaPlayer mp) {
			// TODO Auto-generated method stub
			if (mOnSeekCompleteListener != null) {
				mOnSeekCompleteListener.onSeekComplete(mp);
			}
		}
	};

	SurfaceHolder.Callback mSHCallback = new SurfaceHolder.Callback() {
		public void surfaceChanged(SurfaceHolder holder, int format, int w,
				int h) {
			Log.i("surfaceChanged", (holder != null) + "");
			Log.i("mSurfaceWidth---mSurfaceHeight", mSurfaceWidth + "---"
					+ mSurfaceHeight);
			Log.i("mVideoWidth---mVideoHeight", mVideoWidth + "---"
					+ mVideoHeight);
			Log.i("w---h", w + "---" + h);
			mSurfaceWidth = w;
			mSurfaceHeight = h;
			boolean isValidState = (mTargetState == STATE_PLAYING);
			boolean hasValidSize = (mVideoWidth == w && mVideoHeight == h);
			Log.i("mTargetState",""+ mTargetState);
			Log.i("isValidState---hasValidSize", isValidState + "---" + hasValidSize);
			if (mMediaPlayer != null && isValidState && hasValidSize) {
				if (mSeekWhenPrepared != 0) {
					seekTo(mSeekWhenPrepared);
				}
				start();
				if (mMediaController != null) {
					mMediaController.show();
				}
			}
		}

		public void surfaceCreated(SurfaceHolder holder) {
			Log.i("surfaceCreated", (holder != null) + "");
			mSurfaceHolder = holder;
			openVideo();
		}

		public void surfaceDestroyed(SurfaceHolder holder) {
			// after we return from this we can't use the surface any more
			Log.i("surfaceDestroyed", (holder != null) + "surfaceDestroyed");
			mSurfaceHolder = null;
			if (mMediaController != null)
				mMediaController.hide();
			release(true);
		}
	};
	
	/**
	 * 设置视频规模,是否全屏
	 * 
	 * @param width
	 * @param height
	 */
	public void setVideoScale(float width, float height) {
		
		float videoWidth = this.getVideoWidth();
		float videoHeight = this.getVideoHeight();
		if (videoWidth > 0 && videoHeight > 0) {
			if (videoWidth * height > width * videoHeight) {
				height = width * videoHeight / videoWidth;
			} else if (videoWidth * height < width * videoHeight) {
				width = height * videoWidth / videoHeight;
			}
		}

		LayoutParams lp = getLayoutParams();
		lp.height = (int)height;
		lp.width = (int)width;
		setLayoutParams(lp);
		
		getHolder().setFixedSize(mVideoWidth, mVideoHeight);//这个是设置分辨率！！！！
	}


	/**
	 * 自定义监听器，用于监听屏蔽大小是否改变
	 */
	public interface MySizeChangeLinstener {
		void doMyThings();
	}

	/**
	 * 自定义监听器，用于监听屏蔽大小是否改变,用于提供给其他类调用。
	 */
	public void setMySizeChangeLinstener(MySizeChangeLinstener l) {
		mMyChangeLinstener = l;
	}

	/**
	 * 
	 * context 视图运行的应用程序上下文，通过它可以访问当前主题、资源等等。
	 */
	public VideoView(Context context) {
		super(context);
		initVideoView();
		this.mContext = context;  
	}

	/**
	 * 
	 * context 视图运行的应用程序上下文，通过它可以访问当前主题、资源等等。 attrs 用于视
	 * 
	 * 图的 XML 标签属性集合。
	 */
	public VideoView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
		initVideoView();
		this.mContext = context;
	}

	/**
	 * 
	 * context 视图运行的应用程序上下文，通过它可以访问当前主题、资源等等。
	 * 
	 * attrs 用于视图的 XML 标签属性集合。
	 * 
	 * defStyle 应用到视图的默认风格。如果为 0 则不应用（包括当前主题中的）风格。 该值可
	 * 
	 * 以是当前主题中的属性资源，或者是明确的风格资源ID。
	 */
	public VideoView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initVideoView();
		this.mContext = context;  
	}

	/**
	 * 初始化VideoView，设置相关参数。
	 */
	private void initVideoView() {
		Log.i("initVideoView", "initVideoView");
		mVideoWidth = 0;
		mVideoHeight = 0;
		mCurrentState = STATE_IDLE;
		mTargetState = STATE_IDLE;
		// 给SurfaceView当前的持有者一个回调对象。如果没有这个，表现是黑屏
		getHolder().addCallback(mSHCallback);
		// 下面设置SurfaceView不维护自己的缓冲区,而是等待屏幕的渲染引擎将内容推送到用户面前
		getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		// 通过setFocusable().来设置SurfaceView接受焦点的资格,
		setFocusable(true);
		// 对应在触摸模式下，你可以调用isFocusableInTouchMode().来获知是否有焦点来响应点触，
		// 也可以通过setFocusableInTouchMode().来设置是否有焦点来响应点触的资格.
		setFocusableInTouchMode(true);
		// 当用户请求在某个界面聚集焦点时，会调用requestFocus().这个方法。
		requestFocus();
	}

	/**
	 * 是使用 View 前需要调用的方法. 通知View进行自身尺寸测量.
	 * 如果自己重写的话测量完自身大小注意需要调用setMeasuredDimension(int, int);这个方法
	 * 
	 * 设置控 件大小.
	 */
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

		int width = getDefaultSize(mVideoWidth, widthMeasureSpec);
		int height = getDefaultSize(mVideoHeight, heightMeasureSpec);
		setMeasuredDimension(width, height);

	}

	public int resolveAdjustedSize(int desiredSize, int measureSpec) {
		int result = desiredSize;
		int specMode = MeasureSpec.getMode(measureSpec);
		int specSize = MeasureSpec.getSize(measureSpec);

		switch (specMode) {
		case MeasureSpec.UNSPECIFIED:
			/*
			 * Parent says we can be as big as we want. Just don't be larger
			 * than max size imposed on ourselves.
			 */
			result = desiredSize;
			break;

		case MeasureSpec.AT_MOST:
			/*
			 * Parent says we can be as big as we want, up to specSize. Don't be
			 * larger than specSize, and don't be larger than the max size
			 * imposed on ourselves.
			 */
			result = Math.min(desiredSize, specSize);
			break;

		case MeasureSpec.EXACTLY:
			// No choice. Do what we are told.
			result = specSize;
			break;
		}
		return result;
	}

	/**
	 * Register a callback to be invoked when the media file is loaded and ready
	 * to go.
	 * 
	 * @param l
	 *            The callback that will be run
	 */
	public void setOnPreparedListener(MediaPlayer.OnPreparedListener l) {
		mOnPreparedListener = l;
	}

	/**
	 * Register a callback to be invoked when the end of a media file has been
	 * reached during playback.
	 * 
	 * @param l
	 *            The callback that will be run
	 */
	public void setOnBufferingUpdateListener(OnBufferingUpdateListener l) {
		mOnBufferingUpdateListener = l;
	}

	/**
	 * Register a callback to be invoked when an error occurs during playback or
	 * setup. If no listener is specified, or if the listener returned false,
	 * VideoView will inform the user of any errors.
	 * 
	 * @param l
	 *            The callback that will be run
	 */
	public void setOnErrorListener(OnErrorListener l) {
		mOnErrorListener = l;
	}

	public void setOnCompletionListener(OnCompletionListener l) {
		mOnCompletionListener = l;
	}

	public void setOnInfoListener(OnInfoListener l) {
		mOnInfoListener = l;
	}

	public void setOnSeekCompleteListener(OnSeekCompleteListener l) {
		mOnSeekCompleteListener = l;
	}

	/**
	 * 是否MediaPlayer能否暂停
	 */
	public boolean canPause() {
		return mCanPause;
	}

	/**
	 * 是否MediaPlayer能否快退
	 */
	public boolean canSeekBackward() {
		return mCanSeekBack;
	}

	/**
	 * 是否MediaPlayer能否快进
	 */
	public boolean canSeekForward() {
		return mCanSeekForward;
	}

	/**
	 * 停止播放，并释放资源，让MediaPlayer处于空闲状态。
	 */
	public void stopPlayback() {
		Log.i("stopPlayback", "stopPlayback");
		if (mMediaPlayer != null) {
			mMediaPlayer.stop();
			mMediaPlayer.release();
			mMediaPlayer = null;
			mCurrentState = STATE_IDLE;
			mTargetState = STATE_IDLE;
		}
	}

	/**
	 * release the media player in any state
	 * 不管MediaPlayer是处于正在播放，还是暂停状态，只要你MediaPlayer存在，我就要MediaPlayer播放结束并处于空闲状态。
	 */
	public void release(boolean cleartargetstate) {
		Log.i("release", "release");
		if (mMediaPlayer != null) {
			Log.i("release", "releaseMediaPlayer");
			mMediaPlayer.reset();
			mMediaPlayer.release();
			mMediaPlayer = null;
			mCurrentState = STATE_IDLE;
			if (cleartargetstate) {
				mTargetState = STATE_IDLE;
			}
		}
	}

	public void releaseMedia() {
		Log.i("release", "release");
		if (mMediaPlayer != null) {
			mMediaPlayer.stop();
			mMediaPlayer.reset();
			mMediaPlayer.release();
			mMediaPlayer = null;
			mCurrentState = STATE_IDLE;
			mTargetState = STATE_IDLE;
			setVisibility(View.INVISIBLE);
			setVisibility(View.VISIBLE);
		}
	}

	/**
	 * 是否可以播放情况一切可好
	 * 
	 * @return
	 */
	public boolean isInPlaybackState() {
		Log.i("start", "isInPlaybackState "+mCurrentState);
		return (mMediaPlayer != null && mCurrentState != STATE_ERROR
				&& mCurrentState != STATE_IDLE && mCurrentState != STATE_PREPARING);
	}

	/**
	 * MediaPlayer是否是处于播放状态
	 */
	public boolean isPlaying() {
		return isInPlaybackState() && mMediaPlayer.isPlaying();
	}

	/**
	 * 显示、隐藏控制面板
	 */
	private void toggleMediaControlsVisiblity() {
		Log.i("toggleMediaControlsVisiblity", "toggleMediaControlsVisiblity");
		if (mMediaController.isShowing()) {
			mMediaController.hide();
		} else {
			mMediaController.show();
		}
	}

	/**
	 * 开始播放
	 */
	public void start() {
		Log.i("start", "start");
		if (isInPlaybackState()) {
			mMediaPlayer.start();
			Log.i("start", "startPlay");
			mCurrentState = STATE_PLAYING;
		} 
		mTargetState = STATE_PLAYING;
	}

	/**
	 * 暂停播放
	 */
	public void pause() {
		Log.i("pause", "pause");
		if (isInPlaybackState()) {
			if (mMediaPlayer.isPlaying()) {
				mMediaPlayer.pause();
				mCurrentState = STATE_PAUSED;
			}
		}
		mTargetState = STATE_PAUSED;
	}

	/**
	 * 得到视频的宽
	 */
	public int getVideoWidth() {
		return mVideoWidth;
	}

	/**
	 * 
	 * @return：视频高度
	 */
	public int getVideoHeight() {
		return mVideoHeight;
	}

	// cache duration as mDuration for faster access
	/**
	 * 得到音乐、视频文件播放时时间的总长度
	 */
	public int getDuration() {
		if (isInPlaybackState()) {
			if (mDuration > 0) {
				return mDuration;
			}
			mDuration = mMediaPlayer.getDuration();
			return mDuration;
		}
		mDuration = -1;
		return mDuration;
	}

	/**
	 * 得到音乐、视频文件播放到的当前时间位置
	 */
	public int getCurrentPosition() {
		if (isInPlaybackState()) {
			return mMediaPlayer.getCurrentPosition();
		}
		return 0;
	}

	/**
	 * 播放设置指定的位置
	 */
	public void seekTo(int msec) {
		if (isInPlaybackState()) {
			mMediaPlayer.seekTo(msec);
			mSeekWhenPrepared = 0;
		} else {
			mSeekWhenPrepared = msec;
		}
	}

	/**
	 * 播放设置指定的位置
	 */
	public void seekTo(long msec) {
		if (isInPlaybackState()) {
			mMediaPlayer.seekTo((int) msec);
			mSeekWhenPrepared = 0;
		} else {
			mSeekWhenPrepared = (int) msec;
		}
	}

	/**
	 * 得到当前加载视频的百分比
	 */
	public int getBufferPercentage() {
		if (mMediaPlayer != null) {
			return mCurrentBufferPercentage;
		}
		return 0;
	}

	/**
	 * 用于处理触屏时间
	 */
	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		if (isInPlaybackState() && mMediaController != null) {
			toggleMediaControlsVisiblity();
		}
		return false;
	}

	/**
	 * 处理一个跟踪球事件。 默认情况下，不做任何动作， ..
	 */
	@Override
	public boolean onTrackballEvent(MotionEvent ev) {
		if (isInPlaybackState() && mMediaController != null) {
			toggleMediaControlsVisiblity();
		}
		return false;
	}

	/**
	 * 设置系统默认控制面板
	 * 
	 * @param controller
	 */
	public void setMediaController(MediaController controller) {
		if (mMediaController != null) {
			mMediaController.hide();
		}
		mMediaController = controller;
		attachMediaController();
	}

	/**
	 * 显示系统默认控制面板
	 */
	private void attachMediaController() {
		if (mMediaPlayer != null && mMediaController != null) {
			mMediaController.setMediaPlayer(this);
			View anchorView = this.getParent() instanceof View ? (View) this
					.getParent() : this;
			mMediaController.setAnchorView(anchorView);
			mMediaController.setEnabled(isInPlaybackState());
		}
	}

	/**
	 * 设置要播放音频、视频的路径。
	 * 
	 * @param path
	 *            视频、音频路径
	 */
	public void setVideoPath(String path) {
		//setVideoURI(Uri.parse(path));
//		setVisibility(View.INVISIBLE);
//		setVisibility(View.VISIBLE);
		mPath = path;
		mSeekWhenPrepared = 0;
		openVideo();
		// 当某些变更导致视图的布局失效时调用该方法。该方法按照视图树的顺序调用。
		requestLayout();
		// 更新视图
		invalidate();
	}

	/**
	 * 设置要播放音频、视频的路径。
	 * 
	 * @param uri
	 *            视频、音频路径
	 */
	public void setVideoURI(Uri uri) {
//		setVisibility(View.INVISIBLE);
//		setVisibility(View.VISIBLE);
		mUri = uri;
		mSeekWhenPrepared = 0;
		openVideo();
		// 当某些变更导致视图的布局失效时调用该方法。该方法按照视图树的顺序调用。
		requestLayout();
		// 更新视图
		invalidate();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		boolean isKeyCodeSupported = keyCode != KeyEvent.KEYCODE_BACK
				&& keyCode != KeyEvent.KEYCODE_VOLUME_UP
				&& keyCode != KeyEvent.KEYCODE_VOLUME_DOWN
				&& keyCode != KeyEvent.KEYCODE_MENU
				&& keyCode != KeyEvent.KEYCODE_CALL
				&& keyCode != KeyEvent.KEYCODE_ENDCALL;
		if (isInPlaybackState() && isKeyCodeSupported
				&& mMediaController != null) {
			if (keyCode == KeyEvent.KEYCODE_HEADSETHOOK
					|| keyCode == KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE) {
				if (mMediaPlayer.isPlaying()) {
					pause();
					mMediaController.show();
				} else {
					start();
					mMediaController.hide();
				}
				return true;
			} else if (keyCode == KeyEvent.KEYCODE_MEDIA_STOP
					&& mMediaPlayer.isPlaying()) {
				pause();
				mMediaController.show();
			} else {
				toggleMediaControlsVisiblity();
			}
		}

		return super.onKeyDown(keyCode, event);
	}

	/**
	 * 打开视频路径，并设置相关参数-该方法重要啊
	 */
	private void openVideo() {
		Log.i("openVideo", (mSurfaceHolder != null) + "");
		if (mPath == null || mSurfaceHolder == null) {
			// not ready for playback just yet, will try again later
			Log.i("openVideo_back", (mSurfaceHolder != null) + "");
			return;
		}
		release(false);
		try {
			mMediaPlayer = new MediaPlayer();
			mMediaPlayer.setOnInfoListener(mOnInfoListener);
			mMediaPlayer.setOnPreparedListener(mPreparedListener);
			mMediaPlayer.setOnCompletionListener(mCompletionListener);
			mMediaPlayer.setOnErrorListener(mErrorListener);
			mMediaPlayer.setOnBufferingUpdateListener(mBufferingUpdateListener);
			mMediaPlayer.setOnSeekCompleteListener(mSeekCompleteListener);
			mMediaPlayer.setOnVideoSizeChangedListener(mSizeChangedListener);
			mDuration = -1;
			mCurrentBufferPercentage = 0;
			//mMediaPlayer.setDataSource(mContext, mUri);
			mMediaPlayer.setDataSource(mPath);
			Log.i("openVideo",mPath + "");
			mMediaPlayer.setDisplay(mSurfaceHolder);
			mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
			mMediaPlayer.setScreenOnWhilePlaying(true);
			mMediaPlayer.prepareAsync();
			//mMediaPlayer.prepare();
			// we don't set the target state here either, but preserve the
			// target state that was there before.
			mCurrentState = STATE_PREPARING;
			attachMediaController();
		} catch (IOException ex) {
			Log.w(TAG, "Unable to open content: " + mUri, ex);
			mCurrentState = STATE_ERROR;
			mTargetState = STATE_ERROR;
			mErrorListener.onError(mMediaPlayer,
					MediaPlayer.MEDIA_ERROR_UNKNOWN, 0);
			return;
		} catch (IllegalArgumentException ex) {
			Log.w(TAG, "Unable to open content: " + mUri, ex);
			mCurrentState = STATE_ERROR;
			mTargetState = STATE_ERROR;
			mErrorListener.onError(mMediaPlayer,
					MediaPlayer.MEDIA_ERROR_UNKNOWN, 0);
			return;
		}
	}

	@Override
	public int getAudioSessionId() {
		// TODO Auto-generated method stub
		return 0;
	}

}
