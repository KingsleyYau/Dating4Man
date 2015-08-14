package com.qpidnetwork.dating.emf;

import java.util.ArrayList;
import java.util.List;

import com.qpidnetwork.dating.R;
import com.qpidnetwork.framework.widget.CircleImageView;
import com.qpidnetwork.manager.FileCacheManager;
import com.qpidnetwork.request.item.EMFBlockListItem;
import com.qpidnetwork.tool.ImageViewLoader;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class EMFBlockedListAdapter extends BaseAdapter{
	
	Context mContext;
	List<EMFBlockListItem> mBlockPeople;
	
	public EMFBlockedListAdapter(Context context, List<EMFBlockListItem> blockPeople){
		
		mContext = context;
		mBlockPeople = blockPeople;
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return mBlockPeople.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return 0;
	}
	
	public List<EMFBlockListItem> getDataList(){
		return mBlockPeople;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		if (convertView == null){
			convertView = LayoutInflater.from(mContext).inflate(R.layout.adapter_emf_blocked_list_item, null);
			convertView.setTag(new ViewHolder(convertView));
		}
		
		EMFBlockListItem person = mBlockPeople.get(position);
		ViewHolder h = (ViewHolder)convertView.getTag();
		
		h.womanName.setText(person.firstname);
		h.womanId.setText(person.womanid);
		
		h.photo.setImageResource(R.drawable.female_default_profile_photo_40dp);
		/*头像处理*/
		if((person.photoURL != null)&&(!person.photoURL.equals(""))){
			String localPath = FileCacheManager.getInstance().CacheImagePathFromUrl(person.photoURL);
			new ImageViewLoader(mContext).DisplayImage(h.photo, person.photoURL, localPath, null);
		}
		
		return convertView;
	}
	
	
	public void removeItem(int which){
		mBlockPeople.remove(which);
		notifyDataSetChanged();
	}
	
	/*移除指定Id队列*/
	public void removeItems(String[] womanIds){
		if(womanIds != null){
			for(int i=0; i<womanIds.length; i++){
				for(EMFBlockListItem item : mBlockPeople){
					if(item.womanid.equals(womanIds[i])){
						mBlockPeople.remove(item);
						break;
					}
				}
			}
			notifyDataSetChanged();
		}
	}
	
	
	class ViewHolder{
		
		public CircleImageView photo;
		public TextView womanName;
		public TextView womanId;
		
		public ViewHolder(View v){
			this.photo = (CircleImageView)v.findViewById(R.id.ivPhoto);
			this.womanName = (TextView)v.findViewById(R.id.tvName);
			this.womanId = (TextView)v.findViewById(R.id.tvDesc);
		}
	}
	
}
