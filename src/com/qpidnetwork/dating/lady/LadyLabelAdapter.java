package com.qpidnetwork.dating.lady;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.qpidnetwork.dating.R;
import com.qpidnetwork.framework.widget.wrap.WrapBaseAdapter;
import com.qpidnetwork.request.item.LadySignItem;

public class LadyLabelAdapter extends WrapBaseAdapter{
	
	private Context mContext;
	private List<LadySignItem> mLabelList;
	
	public LadyLabelAdapter(Context context, List<LadySignItem> labelList){
		this.mContext = context;
		this.mLabelList = labelList;
	}

	@Override
	protected int getCount() {
		// TODO Auto-generated method stub
		return mLabelList.size();
	}

	@Override
	protected LadySignItem getItem(int position) {
		// TODO Auto-generated method stub
		return mLabelList.get(position);
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
		
		LadySignItem item = getItem(position);
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
		holder.cvLabel.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if(mLabelList.get(position).isSigned){
					mLabelList.get(position).isSigned = false;
				}else{
					mLabelList.get(position).isSigned = true;
				}
				notifyDataSetChanged();
			}
		});
		
		return convertView;
	}
	
	/**
	 * 获取已选标签Id列表
	 * @return
	 */
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
