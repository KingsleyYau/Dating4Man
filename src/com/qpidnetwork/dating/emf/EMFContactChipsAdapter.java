package com.qpidnetwork.dating.emf;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.qpidnetwork.dating.R;
import com.qpidnetwork.dating.bean.ContactBean;
import com.qpidnetwork.framework.widget.CircleImageView;
import com.qpidnetwork.manager.FileCacheManager;
import com.qpidnetwork.tool.ImageViewLoader;



public class EMFContactChipsAdapter extends BaseAdapter{
	
	private Context mContext;
	private List<ContactBean> mDataList;
	
	public EMFContactChipsAdapter(Context context, List<ContactBean> dataList){
		mContext = context;
		mDataList = dataList;
	}
	
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return mDataList.size();
	}

	@Override
	public ContactBean getItem(int position) {
		// TODO Auto-generated method stub
		return mDataList.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		ViewHolder holder;
		if (convertView == null) {
			holder = new ViewHolder();
			holder.loader = null;
			convertView = LayoutInflater.from(mContext).inflate(R.layout.adapter_contact_chip_item, parent, false);
			holder.ivPhoto = (CircleImageView)convertView.findViewById(R.id.ivPhoto);
			holder.tvName = (TextView)convertView.findViewById(R.id.tvName);
			holder.tvDesc = (TextView)convertView.findViewById(R.id.tvDesc);
			convertView.setTag(holder);
		}else{
			holder = (ViewHolder)convertView.getTag();
		}
		
		ContactBean bean = getItem(position);
		holder.tvName.setText(bean.firstname);
		holder.tvDesc.setText(bean.womanid);
		
		/*头像处理*/
		holder.ivPhoto.setImageResource(R.drawable.female_default_profile_photo_40dp);
		if ( null != holder.loader ) {
			// 停止回收旧Downloader
			holder.loader.ResetImageView();
		}
		if((bean.photoURL != null)&&(!bean.photoURL.equals(""))){
			String localPath = FileCacheManager.getInstance().CacheImagePathFromUrl(bean.photoURL);
			holder.loader = new ImageViewLoader(mContext);
			holder.loader.DisplayImage(holder.ivPhoto, bean.photoURL, localPath, null);
		}
		
		return convertView;
	}
	
	private class ViewHolder{
		public CircleImageView ivPhoto;
		public TextView tvName;
		public TextView tvDesc;
		public ImageViewLoader loader;
	}
	
}