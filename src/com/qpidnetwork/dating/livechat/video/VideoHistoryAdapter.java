package com.qpidnetwork.dating.livechat.video;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView.ScaleType;
import android.widget.TextView;

import com.qpidnetwork.dating.R;
import com.qpidnetwork.framework.base.UpdateableAdapter;
import com.qpidnetwork.framework.util.StringUtil;
import com.qpidnetwork.framework.util.UnitConversion;
import com.qpidnetwork.livechat.LiveChatManager;
import com.qpidnetwork.request.RequestJniLiveChat.VideoPhotoType;
import com.qpidnetwork.request.item.LCVideoItem;

public class VideoHistoryAdapter extends UpdateableAdapter<LCVideoItem>{
	
	private Context mContext;
	private String videoOwnerId;//视频发布者Id
	private LiveChatManager mLivechatManager;
	
	public VideoHistoryAdapter(Context context, String videoOwnerId){
		this.mContext = context;
		mLivechatManager = LiveChatManager.getInstance();
		this.videoOwnerId = videoOwnerId;
	}

	@SuppressLint("InflateParams")
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		if(convertView == null){
			holder = new ViewHolder();
			convertView = LayoutInflater.from(mContext).inflate(R.layout.adapter_livechat_video_item, null);
			holder.tvVideoTitle = (TextView)convertView.findViewById(R.id.tvVideoTitle);
			holder.videoItem = (LivechatVideoItem)convertView.findViewById(R.id.videoItem);
			holder.videoItem.ivThumb.setBackgroundColor(Color.TRANSPARENT);
			holder.videoItem.ivThumb.setScaleType(ScaleType.CENTER_CROP);
			calculateImageViewSize(holder.videoItem);
			convertView.setTag(holder);
		}else{
			holder = (ViewHolder)convertView.getTag();
		}
		
		LCVideoItem item = getItem(position);
		
		String videoThumbPath = mLivechatManager.GetVideoPhotoPathWithExist(videoOwnerId, item.inviteid, item.videoid, VideoPhotoType.Default);
		if(StringUtil.isEmpty(videoThumbPath)){
			//本地没有，调用下载接口下载
			mLivechatManager.GetVideoPhoto(videoOwnerId, item, VideoPhotoType.Default);
		}else{
			//本地已存在设置thumb
			holder.videoItem.setVideoThumbWithSimpleSize(videoThumbPath);
		}
		
		//更新video状态
		String videoLocalPath = mLivechatManager.GetVideoPathWithExist(videoOwnerId, item.inviteid, item.videoid);
		if(StringUtil.isEmpty(videoLocalPath)){
			//本地无
			if(mLivechatManager.isGetVideoNow(item.videoid)){
				//正在下载过程中
				holder.videoItem.updateForDownloading();
			}else{
				holder.videoItem.updateForDefault();
			}
		}else{
			//已经下载成功，本地已经存在
			holder.videoItem.updateForPlay();
		}
		
		holder.tvVideoTitle.setText(item.title);
		
		return convertView;
	}
	
	private void calculateImageViewSize(LivechatVideoItem v){
		int padding = UnitConversion.dip2px(mContext, 8);
		int screenWith = mContext.getResources().getDisplayMetrics().widthPixels;
		int width = (screenWith - UnitConversion.dip2px(mContext, 2)) / 2;
		int height = width / 16 * 12;
		v.ivThumb.getLayoutParams().height = height;
		v.ivThumb.getLayoutParams().width = width;
		v.setPadding(padding, padding, padding, 0);
	}
	
	private class ViewHolder{
		public LivechatVideoItem videoItem;
		public TextView tvVideoTitle;
	}

}
