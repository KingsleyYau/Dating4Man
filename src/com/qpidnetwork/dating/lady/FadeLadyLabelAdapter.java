package com.qpidnetwork.dating.lady;

import java.util.ArrayList;
import java.util.List;

import com.qpidnetwork.dating.R;
import com.qpidnetwork.request.item.LadySignItem;
import com.qpidnetwork.view.FlowLayout;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;

public class FadeLadyLabelAdapter {
	
	private FlowLayout mFlowLayout;
	private List<LadySignItem> mLabelList;
	private Context mContext;
	
	public FadeLadyLabelAdapter(Context context, FlowLayout flowLayout, List<LadySignItem> data){
		mContext = context;
		mLabelList = data;
		mFlowLayout = flowLayout;
	}
	
	
	public void adaptView(){
		for (int i = 0; i < mLabelList.size(); i++){
			mFlowLayout.addView(getView(null, i));
		}
	};
	
	private View getView(View convertView, final int position){
		ViewHolder holder;
		if(convertView == null){
			holder = new ViewHolder();
			convertView = LayoutInflater.from(mContext).inflate(R.layout.adapter_label_item, null);
			holder.cvLabel = (CardView)convertView.findViewById(R.id.cvLabel);
			holder.ivLabelCheck = (ImageView)convertView.findViewById(R.id.ivLabelCheck);
			holder.tvLabelDesc = (TextView)convertView.findViewById(R.id.tvLabelDesc);
			convertView.setTag(holder);
		}else{
			holder = (ViewHolder)convertView.getTag();
		}
		
		LadySignItem item = mLabelList.get(position);
		holder.cvLabel.setCardBackgroundColor(Color.WHITE);
		
		if(item.isSigned){
			holder.ivLabelCheck.setImageResource(R.drawable.ic_done_green_18dp);
			holder.cvLabel.setCardBackgroundColor(Color.parseColor(item.color));
			holder.tvLabelDesc.setTextColor(mContext.getResources().getColor(R.color.green));
		}else{
//			holder.cvLabel.setCardBackgroundColor(mContext.getResources().getColor(R.color.brown));
			holder.ivLabelCheck.setImageResource(R.drawable.ic_add_grey600_18dp);
			holder.tvLabelDesc.setTextColor(mContext.getResources().getColor(R.color.text_color_dark));
		}
		holder.tvLabelDesc.setText(item.name);
		holder.tvLabelDesc.setTextSize(20);
		convertView.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				ViewHolder holder = (ViewHolder)v.getTag();
				LadySignItem item = mLabelList.get(position);
				
				if(mLabelList.get(position).isSigned){
					mLabelList.get(position).isSigned = false;
				}else{
					mLabelList.get(position).isSigned = true;
				}
				
				if(item.isSigned){
					holder.ivLabelCheck.setImageResource(R.drawable.ic_done_green_18dp);
					holder.cvLabel.setCardBackgroundColor(Color.parseColor(item.color));
					holder.tvLabelDesc.setTextColor(mContext.getResources().getColor(R.color.green));
				}else{
					holder.cvLabel.setCardBackgroundColor(mContext.getResources().getColor(R.color.white));
					holder.ivLabelCheck.setImageResource(R.drawable.ic_add_grey600_18dp);
					holder.tvLabelDesc.setTextColor(mContext.getResources().getColor(R.color.text_color_dark));
				}
			}
		});
		
		return convertView;
	}
	
	
	public List<String> getChoosedLabelsId(){
		List<String> ids = new ArrayList<String>();
		if(mLabelList != null){
			for(LadySignItem item : mLabelList){
				if(item.isSigned){
					ids.add(item.signId);
				}
			}
		}
		return ids;
	}
	
	
	private class ViewHolder{
		CardView cvLabel;
		ImageView ivLabelCheck;
		TextView tvLabelDesc;
	}

}
