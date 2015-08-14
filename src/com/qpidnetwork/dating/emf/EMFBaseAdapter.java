package com.qpidnetwork.dating.emf;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.qpidnetwork.dating.R;
import com.qpidnetwork.framework.base.UpdateableAdapter;
import com.qpidnetwork.framework.widget.CircleImageView;
import com.qpidnetwork.tool.ImageViewLoader;

/**
 * EMF, Admirers 公共容器基础组件
 * @author Hunter
 * @since 2015.5.13
 */
public abstract class EMFBaseAdapter<T> extends UpdateableAdapter<T>{
	
	public Activity mContext;
	
	public EMFBaseAdapter(Activity context){
		this.mContext = context;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		ViewHolder holder;
		if(convertView == null){
			holder = new ViewHolder();
			convertView = LayoutInflater.from(mContext).inflate(R.layout.adapter_email_item, null);
			holder.llContainer = (LinearLayout)convertView.findViewById(R.id.llContainer);
			holder.ivPhoto = (CircleImageView)convertView.findViewById(R.id.ivPhoto);
			holder.tvName = (TextView)convertView.findViewById(R.id.tvName);
			holder.ivAttach = (ImageView)convertView.findViewById(R.id.ivAttach);
			holder.ivGift = (ImageView)convertView.findViewById(R.id.ivGift);
			holder.tvDate = (TextView)convertView.findViewById(R.id.tvDate);
			holder.tvDesc = (TextView)convertView.findViewById(R.id.tvDesc);
			holder.ivFlat = (ImageView)convertView.findViewById(R.id.ivFlat);
			holder.this_item = (FrameLayout)convertView.findViewById(R.id.this_item);
			holder.imageDownLoader = null;
			convertView.setTag(holder);
		}else{
			holder = (ViewHolder)convertView.getTag();
		}
		
		instantiateViews(position, holder);
		
		return convertView;
	}
	
	protected abstract void instantiateViews(int position, ViewHolder holder);
	
	public class ViewHolder{
		public FrameLayout this_item;
	    public LinearLayout	llContainer;
		public CircleImageView ivPhoto;
		public TextView tvName;
		public ImageView ivAttach;
		public ImageView ivGift;
		public TextView tvDate;
		public TextView tvDesc;
		public ImageView ivFlat;
		public ImageViewLoader imageDownLoader;
	}

}
