package com.qpidnetwork.dating.profile;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.qpidnetwork.dating.R;
import com.qpidnetwork.framework.base.BaseFragmentActivity;
import com.qpidnetwork.framework.widget.wrap.WrapBaseAdapter;
import com.qpidnetwork.framework.widget.wrap.WrapListView;
import com.qpidnetwork.view.MaterialAppBar;

/**
 * MyProfile模块
 * @author Max.Chiu
 */
public class MyProfileDetailInterestActivity extends BaseFragmentActivity implements OnClickListener {

	public static final String INTEREST = "interest";
	
	public class CheckItem {
		public CheckItem() {
			
		}
		
		public CheckItem(
				String id,
				String name,
				boolean isSigned
				) 
		{
			this.id = id;
			this.name = name;
			this.isSigned = isSigned;
		}
		
		public String id;
		public String name;
		public boolean isSigned;
	}
	
	private class InterestLabelAdapter extends WrapBaseAdapter implements OnClickListener {
		
		private Context mContext;
		private List<CheckItem> mList;
		
		public InterestLabelAdapter(Context context, List<CheckItem> list){
			this.mContext = context;
			this.mList = list;
		}

		@Override
		protected int getCount() {
			// TODO Auto-generated method stub
			return mList.size();
		}

		@Override
		protected CheckItem getItem(int position) {
			// TODO Auto-generated method stub
			return mList.get(position);
		}

		@Override
		protected int getItemId(int position) {
			// TODO Auto-generated method stub
			return position;
		}

		@Override
		protected View getView(final int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			ViewHolder holder;
			if(convertView == null) {
				holder = new ViewHolder();
				convertView = LayoutInflater.from(mContext).inflate(R.layout.adapter_label_item, null);
				holder.cvLabel = (CardView)convertView.findViewById(R.id.cvLabel);
				holder.ivLabelCheck = (ImageView)convertView.findViewById(R.id.ivLabelCheck);
				holder.tvLabelDesc = (TextView)convertView.findViewById(R.id.tvLabelDesc);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder)convertView.getTag();
			}
			
			CheckItem item = getItem(position);
			
			if(item.isSigned){
				holder.cvLabel.setCardBackgroundColor(mContext.getResources().getColor(R.color.green));
				holder.ivLabelCheck.setImageResource(R.drawable.ic_done_white_18dp);
				holder.tvLabelDesc.setTextColor(Color.WHITE);
			}else{
				holder.cvLabel.setCardBackgroundColor(mContext.getResources().getColor(R.color.standard_grey));
				holder.ivLabelCheck.setImageResource(R.drawable.ic_add_grey600_18dp);
				holder.tvLabelDesc.setTextColor(mContext.getResources().getColor(R.color.text_color_dark));
			}
			holder.tvLabelDesc.setText(item.name);
			holder.cvLabel.setCardElevation(0f);
			holder.cvLabel.setTag(position);
			holder.cvLabel.setOnClickListener(this);
			
			return convertView;
		}
		
		/**
		 * 获取已选标签Id列表
		 * @return
		 */
		public ArrayList<String> getChoosedLabelsId() {
			ArrayList<String> ids = new ArrayList<String>();
			if( mList != null ){
				for(CheckItem item : mList){
					if(item.isSigned){
						ids.add(item.id);
					}
				}
			}
			return ids;
		}
		
		private class ViewHolder {
			CardView cvLabel;
			ImageView ivLabelCheck;
			TextView tvLabelDesc;
		}

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			int position = (int)v.getTag();
			if(mList.get(position).isSigned){
				mList.get(position).isSigned = false;
			}else{
				mList.get(position).isSigned = true;
			}
			notifyDataSetChanged();
		}

	}

	/**
	 * 兴趣爱好
	 */
	private WrapListView wrapListView;
	private List<CheckItem> mList = new ArrayList<CheckItem>();
	private InterestLabelAdapter intrestLabelAdapter = new InterestLabelAdapter(this, mList);
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		mContext = this;
		
		// 刷新界面
		ReloadData();
	}
	
	/**
	 * 初始化界面
	 */
	@Override
	public void InitView() {
		setContentView(R.layout.activity_my_profile_detail_intrest);

		wrapListView = (WrapListView) findViewById(R.id.wrapListView);
		wrapListView.setDividerWidth(10);
		wrapListView.setDividerHeight(20);
		wrapListView.setAdapter(intrestLabelAdapter);
		
		MaterialAppBar appbar = (MaterialAppBar)findViewById(R.id.appbar);
		appbar.setTouchFeedback(MaterialAppBar.TOUCH_FEEDBACK_HOLO_LIGHT);
		appbar.addButtonToLeft(android.R.id.button1, "", R.drawable.ic_arrow_back_grey600_24dp);
		appbar.addButtonToRight(android.R.id.button2, "", R.drawable.ic_done_grey600_24dp);
		appbar.setTitle(getString(R.string.my_interest), getResources().getColor(R.color.text_color_dark));
		appbar.setOnButtonClickListener(this);
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()){
		case android.R.id.button1:
			setResult(RESULT_CANCELED, null);
			finish();
			break;
		case android.R.id.button2:
			Intent intent = new Intent();
			intent.putExtra(INTEREST, intrestLabelAdapter.getChoosedLabelsId());
			setResult(RESULT_OK, intent);
			finish();
			break;
		}
	}
	
	/**
	 * 刷新界面
	 */
	public void ReloadData() {
		ArrayList<String> intrestList = getIntent().getExtras().getStringArrayList(INTEREST);
		
		String[] array = getResources().getStringArray(R.array.interest);
		
		for(String id : array) {
			boolean bFlag = false;
			if( intrestList != null ) {
				for(String checkName : intrestList) {
					if( id.compareTo(checkName) == 0 ) {
						bFlag = true;
						break;
					}
				}
			}
			CheckItem item = new CheckItem(id, InterestToString(id), bFlag);
			mList.add(item);
		}
		
		intrestLabelAdapter.notifyDataSetChanged();
	}
	
	/**
	 * 获取兴趣爱好字符串
	 * @param name	兴趣爱好Id
	 * @return
	 */
	private String InterestToString(String name) {
		String result = "";
		switch (name) {
		case "1":{
			result = getResources().getString(R.string.my_profile_going_to_restaurants);
		}break;
		case "2":{
			result = getResources().getString(R.string.my_profile_cooking);
		}break;
		case "3":{
			result = getResources().getString(R.string.my_profile_travel);
		}break;
		case "4":{
			result = getResources().getString(R.string.my_profile_hiking_outdoor_activities);
		}break;
		case "5":{
			result = getResources().getString(R.string.my_profile_dancing);
		}break;
		case "6":{
			result = getResources().getString(R.string.my_profile_watching_movies);
		}break;
		case "7":{
			result = getResources().getString(R.string.my_profile_shopping);
		}break;
		case "8":{
			result = getResources().getString(R.string.my_profile_having_pets);
		}break;
		case "9":{
			result = getResources().getString(R.string.my_profile_reading);
		}break;
		case "10":{
			result = getResources().getString(R.string.my_profile_sports_exercise);
		}break;
		case "11":{
			result = getResources().getString(R.string.my_profile_playing_cards_chess);
		}break;
		case "12":{
			result = getResources().getString(R.string.my_profile_Music_play_instruments);
		}break;
		default:
			break;
		}
		return result;
	}
}
