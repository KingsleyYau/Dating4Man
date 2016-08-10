package com.qpidnetwork.dating.livechat.theme;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import com.qpidnetwork.dating.R;
import com.qpidnetwork.dating.livechat.theme.ThemeConfigItem.MotionLocaType;
import com.qpidnetwork.view.ThemeEmotionPlayer;
import com.qpidnetwork.view.ThemeEmotionPlayer.OnAnimationListener;

/**
 * @author Yanni
 * 
 * @version 2016-4-20
 */
public class ThemePreviewer extends PopupWindow {

	private Context context;
	public LinearLayout contentView;

	public ThemeEmotionPlayer emotionPlayer;
	private ThemeConfigItem mConfigItem;

	private int mRepeat = -1;// 设置repeat次数
	private int mFrame = -1;// 设置每秒帧数

	public ThemePreviewer(Context context, ThemeConfigItem configItem) {
		super(context);
		this.context = context;
		this.mConfigItem = configItem;
		this.setHeight(LayoutParams.WRAP_CONTENT);
		this.setWidth(LayoutParams.WRAP_CONTENT);
		this.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
		this.setTouchable(false);
		// this.setFocusable(true);
		// this.setOutsideTouchable(true);
		setContentView(createWindow());
	}

	@SuppressLint("InflateParams")
	private View createWindow() {
		contentView = (LinearLayout) LayoutInflater.from(context).inflate(R.layout.view_theme_previewer, null);
		emotionPlayer = (ThemeEmotionPlayer) contentView.findViewById(R.id.emotionPlayer);
		return contentView;
	}

	/**
	 * 设置动画重复播放次数
	 * 
	 * @param repeat
	 */
	public void setRepeat(int repeat) {
		// Log.i("hunter", "ThemePreViewer setRepeat: " + repeat);
		mRepeat = repeat;
	}

	/**
	 * 设置每秒播放帧数
	 * 
	 * @param frame
	 */
	public void setFrame(int frame) {
		if(frame != 0){
			mFrame = frame;
		}
	}

	/**
	 * 设置popupwindow高度
	 * 
	 * @param height
	 */
	public void setHeigth(int height) {
		this.setHeight(height);
	}

	/**
	 * @param gravity
	 *            设置对齐方式
	 */
	public void setContentGravity(MotionLocaType type) {
		// contentView.setGravity(gravity);
		switch (type) {
		case UNKNOW:
			contentView.setGravity(Gravity.CENTER);// 默认居中
			break;
		case TOP:
			contentView.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL);
			break;
		case CENTER:
			contentView.setGravity(Gravity.CENTER);
			break;
		case BOTTOM:
			contentView.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL);
			break;

		default:
			break;
		}
	}

	@Override
	public void dismiss() {
		super.dismiss();
		if (emotionPlayer.isPlaying())
			emotionPlayer.stop();
	}

	/**
	 * 播放动画
	 */
	public void play() {
		if (mRepeat != -1) {
			emotionPlayer.setRepeat(mRepeat);
		}
		if (mFrame != -1) {
			emotionPlayer.setFrame(mFrame);
		}
		emotionPlayer.setAnimationListener(new OnAnimationListener() {
			
			@Override
			public void onAnimationStop() {
				// TODO Auto-generated method stub
				dismiss();
			}
			
			@Override
			public void onAnimationStart() {
				// TODO Auto-generated method stub
				
			}
		});
		emotionPlayer.setImageList(mConfigItem.mMotionFiles);
		emotionPlayer.play();
	}
	
	/**
	 * 动画正在播放中
	 * @return
	 */
	public boolean isPlaying(){
		boolean isPlaying = false;
		if(emotionPlayer != null){
			isPlaying = emotionPlayer.isPlaying();
		}
		return isPlaying;
	}

	/**
	 * 停止播放动画
	 */
	public void resetPlay(){
		if(emotionPlayer != null){
			emotionPlayer.reset();
		}
	}
}
