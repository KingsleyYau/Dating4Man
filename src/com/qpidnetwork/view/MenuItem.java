package com.qpidnetwork.view;

import com.qpidnetwork.dating.R;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class MenuItem extends RelativeLayout{
	
	private ImageView ivMenuIcon;
	private TextView tvMenuDesc;
	private TextView tvMenuUnread;
	
	private String menuDesc;
	private int menuIconResId;

	public MenuItem(Context context) {
		super(context);
		initLayout(context);
	}

	public MenuItem(Context context, AttributeSet attrs) {
		super(context, attrs);
		TypedArray a = context.obtainStyledAttributes(attrs,
				R.styleable.MenuItem);
		menuDesc = a.getString(R.styleable.MenuItem_menu_desc);
		menuIconResId = a.getResourceId(R.styleable.MenuItem_indicater_icon, -1);
		a.recycle();
		initLayout(context);
	}

	private void initLayout(Context context) {
		LayoutInflater inflater = LayoutInflater.from(context);
		inflater.inflate(R.layout.cell_menu_item, this, true);
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		initViews();
	}

	private void initViews() {
		ivMenuIcon = (ImageView) findViewById(R.id.ivMenuIcon);
		tvMenuDesc = (TextView) findViewById(R.id.tvMenuDesc);
		tvMenuUnread = (TextView) findViewById(R.id.tvMenuUnread);
		if(menuDesc != null){
			setMenuDescription(menuDesc);
		}
		if(menuIconResId >= 0){
			setMenuIcon(menuIconResId);
		}
		this.setClickable(true);
		this.setBackgroundResource(R.drawable.selector_menu_item);	
	}

	public void setMenuDescription(int resId){
		if(tvMenuDesc != null){
			tvMenuDesc.setText(resId);
		}
	}
	
	public void setMenuDescription(String desc){
		if(tvMenuDesc != null){
			tvMenuDesc.setText(desc);
		}
	}
	
	public void setMenuIcon(int resId){
		if(ivMenuIcon != null){
			ivMenuIcon.setImageResource(resId);
		}
	}
	
	public void setMenuIcon(Drawable drawable){
		if(ivMenuIcon != null){
			ivMenuIcon.setImageDrawable(drawable);
		}
	}
	
	public void setUnreadBackgroud(int resid){
		if(tvMenuUnread != null){
			tvMenuUnread.setBackgroundResource(resid);
		}
	}
	
	public void setUnreadText(String count){
		if(tvMenuUnread != null){
			tvMenuUnread.setVisibility(View.VISIBLE);
			tvMenuUnread.setText(count);
		}
	}
	
	public void setUnreadVisibility(int visible){
		if(tvMenuUnread != null){
			tvMenuUnread.setVisibility(visible);
		}
	}
}
