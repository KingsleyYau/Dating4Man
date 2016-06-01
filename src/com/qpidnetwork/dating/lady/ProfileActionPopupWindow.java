package com.qpidnetwork.dating.lady;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
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
import android.widget.PopupWindow;

import com.qpidnetwork.dating.R;
import com.qpidnetwork.framework.util.UnitConversion;
import com.qpidnetwork.view.ButtonFloat;

public class ProfileActionPopupWindow extends PopupWindow implements OnTouchListener, View.OnClickListener{

	private Context context;
	private View contentView;
	private ButtonFloat chatButton;
	private ButtonFloat mailButton;
	private ButtonFloat favoriteButton;
	
	private boolean isFavoirte;
	private boolean isOnline;
	private OnItemClickListener itemClickListener;
	
	public interface OnItemClickListener{
		public void onFavoriteClick(ButtonFloat button);
		public void onMailClick(ButtonFloat button);
		public void onChatClick(ButtonFloat button);
	}
	
	
	
	public ProfileActionPopupWindow(Context context){
		super(context);
		this.context = context;
		setContentView(createContentView(context));
		this.setFocusable(true);
		this.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
		this.setHeight(LayoutParams.WRAP_CONTENT);
		this.setWidth(LayoutParams.WRAP_CONTENT);
		if (Build.VERSION.SDK_INT < 21){
			this.setTouchable(true);
			this.setAnimationStyle(R.style.PopupListAnimation);	
		}else{
			this.setOutsideTouchable(true);
			this.setTouchInterceptor(this);
		}
	}
	
	
	private View createContentView(Context context){
		contentView = LayoutInflater.from(context).inflate(R.layout.layout_lady_profile_action_popup_windown, null);
		chatButton = (ButtonFloat) contentView.findViewById(R.id.chatButton);
		mailButton = (ButtonFloat) contentView.findViewById(R.id.mailButton);
		favoriteButton = (ButtonFloat) contentView.findViewById(R.id.favoriteButton);
		
		chatButton.setOnClickListener(this);
		mailButton.setOnClickListener(this);
		favoriteButton.setOnClickListener(this);
		
		return contentView;
	}
	
	public void setOnItemClickListener(OnItemClickListener listener){
		itemClickListener = listener;
	}
	
	public void setFavorite(boolean isFavorite, boolean clickable){
		this.isFavoirte = isFavorite;
		chatButton.setEnabled(clickable);
		if(clickable){
			if (isFavorite){
				favoriteButton.setIcon(R.drawable.ic_favorite_grey600_24dp);
			}else{
				favoriteButton.setIcon(R.drawable.ic_favorite_outline_grey600_24dp);
			}
		}else{
			favoriteButton.setIcon(R.drawable.ic_favorite_outline_greyc8c8c8_24dp);
		}
	}
	
	public void setIsOnline(boolean isOnline){
		this.isOnline = isOnline;
		if (isOnline){
			chatButton.setIcon(R.drawable.ic_chat_grey600_24dp);
			chatButton.setEnabled(isOnline);
		}else{
			chatButton.setIcon(R.drawable.ic_chat_greyc8c8c8_24dp);
			chatButton.setEnabled(isOnline);
		}
	}
	
	
	@Override
	public boolean onTouch(View v, MotionEvent event) {
		return false;
	}
	
	public void show(View anchor){
		showAtLocation(anchor, 
				Gravity.RIGHT|Gravity.END|Gravity.BOTTOM, 
				UnitConversion.dip2px(context, 24), 
				UnitConversion.dip2px(context, (Build.VERSION.SDK_INT >= 21) ? 136.0f : 90.0f));
		if (Build.VERSION.SDK_INT < 21) return;
		
		contentView.setVisibility(View.INVISIBLE);
		contentView.postDelayed(new Runnable(){

			@Override
			public void run() {
				// TODO Auto-generated method stub
				setCircularRevealAnimation(contentView);
			}
			
		}, 100);
	}
	
	
	private void setCircularRevealAnimation(final View view){

		int initialRadius = view.getWidth();
		Animator anim = ViewAnimationUtils.createCircularReveal(view, view.getWidth() / 2, UnitConversion.dip2px(context, 152), 0, initialRadius);
		anim.setDuration(80);
		contentView.setVisibility(View.VISIBLE);
		anim.start();
	}
	
	
	@Override
	public void dismiss(){
		if (Build.VERSION.SDK_INT < 21){
			superDismiss();
			return;
		}
		
		int initialRadius = contentView.getWidth();
		Animator anim = ViewAnimationUtils.createCircularReveal(contentView, contentView.getWidth() / 2, UnitConversion.dip2px(context, 152), initialRadius, 0);
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
	
	public void superDismiss(){
		super.dismiss();
	}


	private void callActionOnDelay(final int viewId){
		if (itemClickListener == null) 
			return;
		contentView.postDelayed(new Runnable(){

			@Override
			public void run() {
				// TODO Auto-generated method stub
				switch(viewId){
				case R.id.chatButton:
					itemClickListener.onChatClick(chatButton);
					break;
				case R.id.mailButton:
					itemClickListener.onMailClick(mailButton);
					break;
				case R.id.favoriteButton:
					itemClickListener.onFavoriteClick(favoriteButton);
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

}
