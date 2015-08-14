package com.qpidnetwork.dating.contactus;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.qpidnetwork.dating.R;
import com.qpidnetwork.dating.authorization.LoginParam;
import com.qpidnetwork.dating.authorization.LoginPerfence;
import com.qpidnetwork.framework.util.StringUtil;
import com.qpidnetwork.framework.widget.CircleImageView;
import com.qpidnetwork.manager.FileCacheManager;
import com.qpidnetwork.request.item.TicketContentItem;
import com.qpidnetwork.request.item.TicketContentItem.MethodType;
import com.qpidnetwork.request.item.TicketDetailItem;
import com.qpidnetwork.tool.ImageViewLoader;
import com.qpidnetwork.view.ButtonRaised;

public class TicketDetailAdapter extends BaseAdapter{
	
	private Context mContext;
	private TicketDetailItem mTicketDetailItem;
	private OnResolveClickListener mOnResolveClickListener;
	
	public TicketDetailAdapter(Context context, TicketDetailItem ticketDeatilItem){
		mContext = context;
		this.mTicketDetailItem = ticketDeatilItem;
	}

	@Override
	public int getCount() {
		if(mTicketDetailItem != null && mTicketDetailItem.contentList != null){
			return mTicketDetailItem.contentList.length;
		}else{
			return 0;
		}
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder = null;
		if(convertView == null){
			holder = new ViewHolder();
			convertView = LayoutInflater.from(mContext).inflate(R.layout.adapter_ticket_detail_item, null);
			holder.ivPhoto = (CircleImageView)convertView.findViewById(R.id.ivPhoto);
			holder.tvName = (TextView)convertView.findViewById(R.id.tvName);
			holder.tvDesc = (TextView)convertView.findViewById(R.id.tvDesc);
			holder.tvIndex = (TextView)convertView.findViewById(R.id.tvIndex);
			holder.tvBody = (TextView)convertView.findViewById(R.id.tvBody);
			holder.tvResolve = (ButtonRaised)convertView.findViewById(R.id.tvResolve);
			holder.mAttachList.add((ImageView)convertView.findViewById(R.id.ivAttach1));
			holder.mAttachList.add((ImageView)convertView.findViewById(R.id.ivAttach2));
			holder.mAttachList.add((ImageView)convertView.findViewById(R.id.ivAttach3));
			holder.mAttachList.add((ImageView)convertView.findViewById(R.id.ivAttach4));
			holder.mAttachList.add((ImageView)convertView.findViewById(R.id.ivAttach5));
			convertView.setTag(holder);
		}else{
			holder = (ViewHolder)convertView.getTag();
		}
		
		
		
		TicketContentItem item = mTicketDetailItem.contentList[position];
		
		/*resolve*/
		/*if((mTicketDetailItem.status == StatusType.Open)&&(item.method == MethodType.Receive)){
			holder.tvResolve.setVisibility(View.VISIBLE);
		}else{
			holder.tvResolve.setVisibility(View.GONE);
		}
		holder.tvResolve.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(mOnResolveClickListener != null){
					mOnResolveClickListener.onResolveClick();
				}
			}
		});*/
		
		/*Stop all ImageViewLoader*/
		if(holder.mImageDownloaderList != null){
			int size = holder.mImageDownloaderList.size();
			for(int i=0; i<size; i++){
				if(holder.mImageDownloaderList.get(i) != null){
					holder.mImageDownloaderList.get(i).ResetImageView();
				}
			}
		}
		
		holder.tvResolve.setVisibility(View.GONE);
		if(item.method == MethodType.Receive){
			holder.ivPhoto.setImageResource(R.drawable.ic_launcher);
		}else{
			LoginParam loginParam = LoginPerfence.GetLoginParam(mContext);
			holder.ivPhoto.setImageResource(R.drawable.default_photo_64dp);
			/*头像处理*/
			if((loginParam != null)&&(loginParam.item!= null)&&(!StringUtil.isEmpty(loginParam.item.photoURL))){
				String localPath = FileCacheManager.getInstance().CacheImagePathFromUrl(loginParam.item.photoURL);
				ImageViewLoader downloader = new ImageViewLoader(mContext);
				downloader.SetDefaultImage(mContext.getResources().getDrawable(R.drawable.default_photo_64dp));
				downloader.DisplayImage(holder.ivPhoto, loginParam.item.photoURL, localPath, null);
				holder.mImageDownloaderList.add(downloader);
			}
		}
		holder.tvName.setText(item.fromName);
		holder.tvDesc.setText(new SimpleDateFormat("MMM dd, yyyy", Locale.ENGLISH).format(new Date(((long)item.sendDate) * 1000)));
		holder.tvBody.setText(item.message);
		if(mTicketDetailItem.contentList.length > position){
			holder.tvIndex.setVisibility(View.VISIBLE);
			holder.tvIndex.setText("" + (mTicketDetailItem.contentList.length - position));
		}else{
			holder.tvIndex.setVisibility(View.GONE);
		}
		
		if(item.fileList != null){
			for(int i=0; i<item.fileList.length; i++){
				/*头像处理*/
				if((item.fileList[i] != null)&&(!item.fileList[i].equals(""))){
					holder.mAttachList.get(i).setVisibility(View.VISIBLE);
					String localPath = FileCacheManager.getInstance().CacheImagePathFromUrl(item.fileList[i]);
					ImageViewLoader downloader = new ImageViewLoader(mContext);
					downloader.SetDefaultImage(new ColorDrawable(Color.TRANSPARENT));
					downloader.DisplayImage(holder.mAttachList.get(i), item.fileList[i], localPath, null);
					holder.mImageDownloaderList.add(downloader);
					
					final String photoUrl = item.fileList[i];
					holder.mAttachList.get(i).setOnClickListener(new OnClickListener() {
						
						@Override
						public void onClick(View v) {
							AttachmentPhotoPreview.launchPhotoPreview(mContext, photoUrl);
						}
					});
				}
			}
			for(int i=4; i>item.fileList.length-1; i--){
				holder.mAttachList.get(i).setVisibility(View.GONE);
			}
		}else{
			for(int i=0; i<5; i++){
				holder.mAttachList.get(i).setVisibility(View.GONE);
			}
		}
		
		return convertView;
	}
	
	private class ViewHolder{
		CircleImageView ivPhoto;
		TextView tvName;
		TextView tvDesc;
		TextView tvIndex;
		TextView tvBody;
		List<ImageView> mAttachList;
		List<ImageViewLoader> mImageDownloaderList;
		ButtonRaised tvResolve;
		
		public ViewHolder(){
			mAttachList = new ArrayList<ImageView>();
			mImageDownloaderList = new ArrayList<ImageViewLoader>();
		}
	}
	
	public void setOnResolveClickListener(OnResolveClickListener listener){
		this.mOnResolveClickListener = listener;
	}
	
	public interface OnResolveClickListener{
		public void onResolveClick();
	}
	
	public void updateData(TicketDetailItem ticketDeatilItem){
		this.mTicketDetailItem = ticketDeatilItem;
	}

}
