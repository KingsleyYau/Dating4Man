package com.qpidnetwork.dating.livechat.theme;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Build.VERSION;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.PopupWindow;

import com.qpidnetwork.dating.R;
import com.qpidnetwork.framework.util.UnitConversion;
import com.qpidnetwork.view.ButtonFloat;

/**
 * @author Yanni
 * 
 * @version 2016-4-20
 */
public class ThemePopupWindow extends PopupWindow implements OnTouchListener,
		View.OnClickListener {

	private Context context;
	private View contentView;
	private ButtonFloat shopCarButton;
	private ButtonFloat playButton;
	private LinearLayout llScenePlayBody;

	private boolean isUse;
	private OnItemClickListener itemClickListener;
	
	private boolean playShowFlags = true;

	public interface OnItemClickListener {
		public void onShopCarClick(ButtonFloat button);

		public void onPlayClick(ButtonFloat button);
	}

	public ThemePopupWindow(Context context) {
		super(context);
		this.context = context;
		setContentView(createContentView(context));
		this.setFocusable(true);
		this.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
		this.setHeight(LayoutParams.MATCH_PARENT);
		this.setWidth(LayoutParams.MATCH_PARENT);
		if (Build.VERSION.SDK_INT < 21) {
			this.setTouchable(true);
			this.setAnimationStyle(R.style.PopupListAnimation);
		} else {
			this.setOutsideTouchable(true);
			this.setTouchInterceptor(this);
		}
	}

	private View createContentView(Context context) {
		contentView = LayoutInflater.from(context).inflate(
				R.layout.layout_theme_popup_windown, null);
		shopCarButton = (ButtonFloat) contentView.findViewById(R.id.shopCarButton);
		playButton = (ButtonFloat) contentView.findViewById(R.id.playButton);
		llScenePlayBody = (LinearLayout)contentView.findViewById(R.id.llScenePlayBody);

		shopCarButton.setOnClickListener(this);
		playButton.setOnClickListener(this);
		contentView.setOnClickListener(this);

		return contentView;
	}

	public void setOnItemClickListener(OnItemClickListener listener) {
		itemClickListener = listener;
	}

	
	@Override
	public boolean onTouch(View v, MotionEvent event) {
		return false;
	}

	public void show(View anchor, int top) {
		showAtLocation(anchor, Gravity.TOP,
				UnitConversion.dip2px(context, 0),
				top);
		if (Build.VERSION.SDK_INT < 21)
			return;

		contentView.setVisibility(View.INVISIBLE);
		if(!playShowFlags && llScenePlayBody != null){
			llScenePlayBody.setVisibility(View.GONE);
		}else{
			llScenePlayBody.setVisibility(View.VISIBLE);
		}
		contentView.postDelayed(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				setCircularRevealAnimation(contentView);
			}

		}, 100);
	}

	@SuppressLint("NewApi")
	private void setCircularRevealAnimation(final View view) {

		int initialRadius = view.getWidth();
		Animator anim = ViewAnimationUtils.createCircularReveal(view,
				view.getWidth() - UnitConversion.dip2px(context, 42), 
				view.getHeight() - UnitConversion.dip2px(context, 34), 
				0,
				initialRadius);
		anim.setDuration(80);
		contentView.setVisibility(View.VISIBLE);
		anim.start();
	}

	@SuppressLint("NewApi")
	@Override
	public void dismiss() {
		if (Build.VERSION.SDK_INT < 21) {
			superDismiss();
			return;
		}
		

		int initialRadius = contentView.getWidth();
		Animator anim = ViewAnimationUtils.createCircularReveal(
				contentView,
				contentView.getWidth() - UnitConversion.dip2px(context, 42), 
				contentView.getHeight() - UnitConversion.dip2px(context, 34), 
				initialRadius, 
				0);
		anim.setDuration(80);
		anim.addListener(new AnimatorListenerAdapter() {
			@Override
			public void onAnimationEnd(Animator animation) {
				super.onAnimationEnd(animation);
				superDismiss();
			}
		});

		anim.start();
	}

	public void superDismiss() {
		super.dismiss();
	}

	private void callActionOnDelay(final int viewId) {
		if (itemClickListener == null)
			return;
		contentView.postDelayed(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				switch (viewId) {
				case R.id.shopCarButton:
					itemClickListener.onShopCarClick(shopCarButton);
					break;
				case R.id.playButton:
					itemClickListener.onPlayClick(playButton);
					break;
				}
			}

		}, (VERSION.SDK_INT < 21) ? 160 : 80);
	}

	@Override
	public void onClick(View v) {
		callActionOnDelay(v.getId());
		dismiss();
	}
	
	/**
	 * 设置是否隐藏播放控制区域
	 * @param isShow
	 */
	public void setPlayShowFlags(boolean isShow){
		playShowFlags = isShow;
	}
}
