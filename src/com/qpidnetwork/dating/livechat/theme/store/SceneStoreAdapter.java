package com.qpidnetwork.dating.livechat.theme.store;

import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.database.DataSetObserver;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.qpidnetwork.dating.R;
import com.qpidnetwork.framework.util.SystemUtil;
import com.qpidnetwork.framework.util.UnitConversion;
import com.qpidnetwork.framework.widget.stickygridheaders.StickyGridHeadersBaseAdapter;
import com.qpidnetwork.manager.FileCacheManager;
import com.qpidnetwork.manager.ThemeConfigManager;
import com.qpidnetwork.request.item.ThemeItem;
import com.qpidnetwork.request.item.ThemeTagItem;
import com.qpidnetwork.tool.ImageViewLoader;

/**
 * @author Yanni
 * 
 * @version 2016-4-25
 */
public class SceneStoreAdapter implements StickyGridHeadersBaseAdapter {

	private Context mContext;// 上下文对象
	private List<ThemeTagItem> tagItemList;// 主题标签信息
	private List<List<ThemeItem>> mTagList;// 主题分类集合
	private List<ThemeItem> mThemeList;// 所有主题排序后的集合
	private String mWomanId;

	public SceneStoreAdapter(Context context, List<ThemeTagItem> tagItemList,
			List<List<ThemeItem>> tagList, List<ThemeItem> themeList,
			String womanId) {
		super();
		this.tagItemList = tagItemList;
		this.mContext = context;
		this.mTagList = tagList;
		this.mThemeList = themeList;
		this.mWomanId = womanId;
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
		if (convertView == null) {
			holder = new HeadViewHolder();
			convertView = View.inflate(mContext, R.layout.adapter_theme_head,null);
			holder.tvTag = (TextView) convertView.findViewById(R.id.tvTag);
			convertView.setTag(holder);
		} else {
			holder = (HeadViewHolder) convertView.getTag();
		}
		convertView.setLayoutParams(new GridView.LayoutParams(  
				GridView.LayoutParams.WRAP_CONTENT,  
				GridView.LayoutParams.WRAP_CONTENT));
		holder.tvTag.setText(tagItemList.get(position).tagName);
		return convertView;
	}

	@SuppressWarnings("unused")
	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		ItemViewHolder holder = null;
		if (convertView == null) {
			convertView = LayoutInflater.from(parent.getContext()).inflate( R.layout.adapter_theme_item, null);
			holder = new ItemViewHolder(convertView);
			holder.imageDownLoader = null;
		} else {
			holder = (ItemViewHolder) convertView.getTag();
		}

		holder.setPosition(position);
		
		// 判断最热或最新
		if (mThemeList.get(position).isSale) {
			holder.btnMost.setVisibility(View.VISIBLE);
			holder.btnMost.setText("Sale");
		} else if (mThemeList.get(position).isNew) {
			holder.btnMost.setVisibility(View.VISIBLE);
			holder.btnMost.setText("New");
		} else {
			holder.btnMost.setVisibility(View.GONE);
		}
		holder.tvDes.setText(mThemeList.get(position).title);
		if ( null != holder.imageDownLoader ) {
			// 停止回收旧Downloader
			holder.imageDownLoader.ResetImageView();
		}
		String imgUrl = ThemeConfigManager.newInstance().getThemeThumbUrl(mThemeList.get(position).themeId);
		if((imgUrl != null)&&(!imgUrl.equals(""))){
			int width = (SystemUtil.getDisplayMetrics(mContext).widthPixels - UnitConversion.dip2px(mContext, 4 + 4 + 4 + 4))/2;
			int height = UnitConversion.dip2px(mContext, 160);
			String localPath = FileCacheManager.getInstance().CacheImagePathFromUrl(imgUrl);
			holder.imageDownLoader = new ImageViewLoader(mContext);
			Drawable drawable = new ColorDrawable(Color.WHITE);
			drawable.setBounds(0, 0, width, height);
			holder.imageDownLoader.SetDefaultImage(drawable);
			holder.imageDownLoader.DisplayImage(holder.ivImg, true, imgUrl, width, height, 2, 0, localPath, null);
		}

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
		TextView tvTag;// 分类标题
	}

	public class ItemViewHolder {
		
		TextView btnMost;// 最新/最火等等
		ImageView ivImg;// 主题图片
		TextView tvDes;// 主题描述
		int position;
		ImageViewLoader imageDownLoader;
		
		public ItemViewHolder(View itemView){
			ivImg = (ImageView) itemView.findViewById(R.id.ivImg);
			btnMost = (TextView) itemView.findViewById(R.id.btnMost);
			tvDes = (TextView) itemView.findViewById(R.id.tvDes);
			
			itemView.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					Intent intent = new Intent(mContext, SceneDetailActivity.class);
					intent.putExtra("themeItem", mThemeList.get(position));
					intent.putExtra("womanId", mWomanId);
					mContext.startActivity(intent);
				}
			});

			
			itemView.setTag(this);
		}
		
		public void setPosition(int position){
			this.position = position;
		}

	}
}
