package com.qpidnetwork.dating.profile;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.qpidnetwork.dating.R;

/**
 * MyProfile模块
 * @author Max.Chiu
 */
public class MyProfileDetailEditItemView extends RelativeLayout {

	public TextView textViewLeft;
	public TextView textViewValue;
	public ImageButton imageViewRight;
	
	public MyProfileDetailEditItemView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		LayoutInflater.from(context).inflate(R.layout.my_profile_info_detail_edit_item, this, true);
		textViewLeft = (TextView) findViewById(R.id.textViewLeft);
		textViewValue = (TextView) findViewById(R.id.textViewValue);
		imageViewRight = (ImageButton) findViewById(R.id.imageViewRight);
	}

}
