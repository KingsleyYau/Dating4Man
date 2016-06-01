package com.qpidnetwork.dating.livechat.theme.store;

import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.database.DataSetObserver;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.qpidnetwork.dating.R;
import com.qpidnetwork.dating.bean.ContactBean;
import com.qpidnetwork.dating.contacts.ContactManager;
import com.qpidnetwork.framework.widget.stickygridheaders.StickyGridHeadersBaseAdapter;
import com.qpidnetwork.manager.FileCacheManager;
import com.qpidnetwork.manager.ThemeConfigManager;
import com.qpidnetwork.request.item.ThemeItem;
import com.qpidnetwork.tool.ImageViewLoader;

/**
 * @author Yanni
 * 
 * @version 2016-4-25
 */
public class MyScenesAdapter implements StickyGridHeadersBaseAdapter {

	private Context mContext;// 上下文对象
	private List<String> mWomanId;// 主题标签信息
	private List<List<ThemeItem>> mTagList;// 主题分类集合
	private List<ThemeItem> mThemeList;// 所有主题排序后的集合
	private ContactManager mContactManager;

	public MyScenesAdapter(Context context, List<String> womanId,List<List<ThemeItem>> tagList, List<ThemeItem> themeList) {
		super();
		this.mWomanId = womanId;
		this.mContext = context;
		this.mTagList = tagList;
		this.mThemeList = themeList;
		mContactManager = ContactManager.getInstance();
	}

	@Override
	public boolean isEmpty() {
		return getNumHeaders()==0;
	}

	@Override
	public boolean hasStableIds() {
		return false;
	}

	@Override
	public int getViewTypeCount() {
		return 1;
	}

	@Override
	public int getItemViewType(int position) {
		return 0;
	}

	@Override
	public long getItemId(int arg0) {
		return 0;
	}

	@Override
	public boolean isEnabled(int position) {
		return false;
	}

	@Override
	public boolean areAllItemsEnabled() {
		return false;
	}

	@Override
	public int getNumHeaders() {
		return mTagList.size();
	}

	@Override
	public int getCountForHeader(int header) {
		return mTagList.get(header).size();
	}

	@Override
	public int getCount() {
		return 0;
	}

	@SuppressWarnings("unused")
	@Override
	public View getHeaderView(int position, View convertView, ViewGroup parent) {
		// b.setText(mTagItem[position].tagName);
		HeadViewHolder holder = null;
		if (holder == null) {
			holder = new HeadViewHolder();
			convertView = View.inflate(mContext,R.layout.adapter_myscenes_head, null);
			holder.ivPhoto = (ImageView) convertView.findViewById(R.id.ivPhoto);
			holder.tvTag = (TextView) convertView.findViewById(R.id.tvTag);
			convertView.setTag(holder);
		} else {
			holder = (HeadViewHolder) convertView.getTag();
		}
		String imgUrl = getWomanUrlByPosition(position);//获取女士头像
		String localPath = FileCacheManager.getInstance().CacheImagePathFromUrl(imgUrl);// 获取本地缓存路径
		new ImageViewLoader(mContext).DisplayImage(holder.ivPhoto, imgUrl,localPath, null);
		
		holder.tvTag.setText(getWomanNameById(mWomanId.get(position)));
		return convertView;
	}

	@SuppressWarnings("unused")
	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		ItemViewHolder holder = null;
		
		
		if (convertView == null) {
			convertView = LayoutInflater.from(parent.getContext()).inflate( R.layout.adapter_theme_item, null);
			holder = new ItemViewHolder(convertView);
		} else {
			holder = (ItemViewHolder) convertView.getTag();
		}

		// 判断最热或最新
		holder.setPosition(position);
		
		if (mThemeList.get(position).isSale) {
			holder.btnMost.setVisibility(View.VISIBLE);
			holder.btnMost.setText(mContext.getString(R.string.theme_store_sale));
		} else if (mThemeList.get(position).isNew) {
			holder.btnMost.setVisibility(View.VISIBLE);
			holder.btnMost.setText(mContext.getString(R.string.theme_store_new));
		} else {
			holder.btnMost.setVisibility(View.GONE);
		}
		holder.tvDes.setText(mThemeList.get(position).title);

		String imgUrl = ThemeConfigManager.newInstance().getThemeThumbUrl(mThemeList.get(position).themeId);
		String localPath = FileCacheManager.getInstance().CacheImagePathFromUrl(imgUrl);// 获取本地缓存路径
		new ImageViewLoader(mContext).DisplayImage(holder.ivImg, imgUrl,localPath, null);

		return convertView;
	}

	@Override
	public void registerDataSetObserver(DataSetObserver arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void unregisterDataSetObserver(DataSetObserver arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public Object getItem(int arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	public class HeadViewHolder {
		ImageView ivPhoto;//女士头像
		TextView tvTag;// 分类标题
	}

	public class ItemViewHolder {
		
		TextView btnMost;// 最新/最火等等
		ImageView ivImg;// 主题图片
		TextView tvDes;// 主题描述
		int position;
		
		public ItemViewHolder(View itemView){
			ivImg = (ImageView) itemView.findViewById(R.id.ivImg);
			btnMost = (TextView) itemView.findViewById(R.id.btnMost);
			tvDes = (TextView) itemView.findViewById(R.id.tvDes);
			
			itemView.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					Intent intent = new Intent(mContext, SceneDetailActivity.class);
					intent.putExtra("themeItem", mThemeList.get(position));
					intent.putExtra("womanId",getWomanIdByPosition(position));
					mContext.startActivity(intent);
				}
			});

			
			itemView.setTag(this);
		}
		
		public void setPosition(int position){
			this.position = position;
		}

	}

	/**
	 * @param position
	 * 
	 * 根据item的position获取head womanid
	 * @return
	 */
	public String getWomanIdByPosition(int position) {
		int count = 0;
		String womanId = null;
		for (int i = 0; i < mTagList.size(); i++) {
			count += mTagList.get(i).size();
			if (count >= position + 1) {
				womanId = mWomanId.get(i);
				break;
			}
		}
		return womanId;
	}
	
	/**
	 * @return
	 * 获取女士头像url
	 */
	public String getWomanUrlByPosition(int position){
		ContactBean mContactBean = mContactManager.getContactById(mWomanId.get(position));
		return mContactBean.photoURL;
	}
	
	/**
	 * 获取女士姓名
	 * @param womanId
	 * @return
	 */
	public String getWomanNameById(String womanId){
		String womanName = womanId;
		//由于当前女士肯定为联系人，故从联系人获取即可
		ContactBean contact = ContactManager.getInstance().getContactById(womanId);
		if(contact != null ){
			womanName = contact.firstname;
		}
		return womanName;
	}
}
