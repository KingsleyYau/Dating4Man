package com.qpidnetwork.dating.livechat.invite;

import java.util.List;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.qpidnetwork.dating.R;
import com.qpidnetwork.dating.livechat.ChatActivity;
import com.qpidnetwork.dating.livechat.ExpressionImageGetter;
import com.qpidnetwork.framework.util.StringUtil;
import com.qpidnetwork.framework.util.UnitConversion;
import com.qpidnetwork.framework.widget.CircleImageView;
import com.qpidnetwork.manager.FileCacheManager;
import com.qpidnetwork.tool.ImageViewLoader;

public class LivechatInviteAdapter extends BaseAdapter{
	
	private Activity mContext;
	private ExpressionImageGetter imageGetter;
	private List<InviteItem> inviteList; 
	
	public LivechatInviteAdapter(Activity context, List<InviteItem> inviteList){
		this.mContext = context;
		this.inviteList = inviteList;
		imageGetter = new ExpressionImageGetter(context, UnitConversion.dip2px(context, 16), UnitConversion.dip2px(context, 16));
	}
	
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return inviteList.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return 0;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		if(convertView == null){
			holder = new ViewHolder();
			convertView = LayoutInflater.from(mContext).inflate(R.layout.adapter_invite_item, null);
			holder.llContainer = (LinearLayout)convertView.findViewById(R.id.llContainer);
			holder.ivPhoto = (CircleImageView)convertView.findViewById(R.id.ivPhoto);
			holder.ivOnline = (ImageView)convertView.findViewById(R.id.ivOnline);
			holder.tvName = (TextView)convertView.findViewById(R.id.tvName);
			holder.tvDesc = (TextView)convertView.findViewById(R.id.tvDesc);
			holder.imageDownLoader = null;
			convertView.setTag(holder);
		}else{
			holder = (ViewHolder)convertView.getTag();
		}
		final InviteItem item = inviteList.get(position);
		
		if(!StringUtil.isEmpty(item.username)){
			holder.tvName.setText(item.username);
		}else{
			holder.tvName.setText(item.userId);
		}
		
		if(!StringUtil.isEmpty(item.msgDesc)){
			holder.tvDesc.setText(imageGetter.getExpressMsgHTML(item.msgDesc));
		}else{
			holder.tvDesc.setText(null);
		}
		
		/*头像处理*/
		holder.ivPhoto.setImageResource(R.drawable.female_default_profile_photo_40dp);
		if ( null != holder.imageDownLoader ) {
			// 停止回收旧Downloader
			holder.imageDownLoader.ResetImageView();
		}
		if(!StringUtil.isEmpty(item.photoUrl)){
			String localPath = FileCacheManager.getInstance().CacheImagePathFromUrl(item.photoUrl);
			holder.imageDownLoader = new ImageViewLoader(mContext);
			holder.imageDownLoader.SetDefaultImage(mContext.getResources().getDrawable(R.drawable.female_default_profile_photo_40dp));
			holder.imageDownLoader.DisplayImage(holder.ivPhoto, item.photoUrl, localPath, null);
		}
		
		holder.llContainer.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				ChatActivity.launchChatActivity(mContext, item.userId, "", "");
			}
		});
	
		return convertView;
	}

	
	protected class ViewHolder{
		public LinearLayout llContainer;
		public CircleImageView ivPhoto;
		public ImageView ivOnline;
		public TextView tvName;
		public TextView tvDesc;
		public ImageViewLoader imageDownLoader;
	}
}
