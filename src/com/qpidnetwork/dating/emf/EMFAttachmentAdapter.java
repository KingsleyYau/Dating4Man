package com.qpidnetwork.dating.emf;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;

import com.qpidnetwork.dating.R;
import com.qpidnetwork.dating.bean.EMFAttachmentBean;
import com.qpidnetwork.dating.bean.EMFAttachmentBean.AttachType;
import com.qpidnetwork.dating.bean.EMFBean;
import com.qpidnetwork.dating.emf.EMFAttachmentPrivatePhotoFragment.PrivatePhotoDirection;
import com.qpidnetwork.framework.util.UnitConversion;
import com.qpidnetwork.manager.FileCacheManager;
import com.qpidnetwork.manager.VirtualGiftManager;
import com.qpidnetwork.request.RequestJniEMF.PrivatePhotoMode;
import com.qpidnetwork.request.RequestJniEMF.PrivatePhotoType;
import com.qpidnetwork.request.RequestJniLiveChat.VideoPhotoType;
import com.qpidnetwork.tool.ImageViewLoader;

/**
 * @author Yanni
 * 
 * @version 2016-5-24
 * 
 *          EMF附件列表
 */
public class EMFAttachmentAdapter extends BaseAdapter {

	private Context mContext;
	private ArrayList<EMFAttachmentBean> mAttachList;// 附件列表
	private EMFBean mEmfBean;
	private EMFVideoManager mEMFVideoManager;

	public EMFAttachmentAdapter(Context context,
			ArrayList<EMFAttachmentBean> attachList,EMFBean emfBean) {
		this.mContext = context;
		this.mAttachList = attachList;
		this.mEmfBean = emfBean;
		this.mEMFVideoManager = EMFVideoManager.newInstance(mContext);
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return mAttachList.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return mAttachList.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@SuppressWarnings("unused")
	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		ViewHolder holder = null;
		if (holder == null) {
			holder = new ViewHolder();
			convertView = View.inflate(mContext,R.layout.adapter_emf_attachment, null);
			holder.ivPreview = (ImageView) convertView.findViewById(R.id.ivPreview);
			holder.llPrivate = (LinearLayout) convertView.findViewById(R.id.llPrivate);
			holder.ivPrivate = (ImageView) convertView.findViewById(R.id.ivPrivate);
			holder.ivPlay = (ImageView) convertView.findViewById(R.id.ivPlay);
			holder.ivVirtual = (ImageView) convertView.findViewById(R.id.ivVirtual);
			
			/* 设置item固定宽高 */
			DisplayMetrics dm = new DisplayMetrics();
            ((Activity) mContext).getWindowManager().getDefaultDisplay().getMetrics(dm);
            int height = dm.heightPixels;//高度
            int width = dm.widthPixels - UnitConversion.dip2px(parent.getContext(), 16*2 + 6*3);
            int itemWidth = width / 2;
            int itemHeight = (int)((float)itemWidth / 16.0f * 12.0f);
            
            convertView.setLayoutParams(new AbsListView.LayoutParams(itemWidth, itemHeight));
            
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		
		EMFAttachmentBean item = mAttachList.get(position);

		String url = "";
		String localPath = "";
		FileCacheManager.getInstance().CacheImagePathFromUrl(item.photoUrl);
		
		/* 正常图片处理 */
		if(item.type.equals(AttachType.NORAML_PICTURE)){
			url = item.photoUrl;
			localPath = FileCacheManager.getInstance().CacheImagePathFromUrl(url);
		}
		
		/* 私密照处理 */
		if (item.type.equals(AttachType.PRIVATE_PHOTO)) {
			holder.llPrivate.setVisibility(View.VISIBLE);
			localPath = "";
			if(item.privatePhoto.photoFee){//已经扣费
				localPath = FileCacheManager.getInstance().CachePrivatePhotoImagePath(item.privatePhoto.sendId,item.privatePhoto.photoId,PrivatePhotoType.LARGE, PrivatePhotoMode.MODE_DISTINCT);
				holder.ivPrivate.setImageResource(R.drawable.ic_lock_open_white_18dp);
			}else{//未扣费
				localPath = FileCacheManager.getInstance().CachePrivatePhotoImagePath(item.privatePhoto.sendId,item.privatePhoto.photoId,PrivatePhotoType.LARGE, PrivatePhotoMode.MODE_DISTINCT);
				holder.ivPrivate.setImageResource(R.drawable.ic_lock_outline_white_18dp);
			}
		} else {
			holder.llPrivate.setVisibility(View.GONE);
		}
		/* 短视频处理 */
		if (item.type.equals(AttachType.SHORT_VIDEO)) {
			holder.ivPlay.setVisibility(View.VISIBLE);
			holder.ivPreview.setScaleType(ScaleType.CENTER_CROP);
			localPath = mEMFVideoManager.getVideoThumbPhotoPath(item.shortVideo.womanid, item.shortVideo.sendId,item.shortVideo.videoId, item.shortVideo.messageid,VideoPhotoType.Big);
		} else {
			holder.ivPlay.setVisibility(View.GONE);
		}
		/* 虚拟礼物处理 */
		if (item.type.equals(AttachType.VIRTUAL_GIFT)) {
			holder.ivVirtual.setVisibility(View.VISIBLE);
			url = VirtualGiftManager.getInstance().GetVirtualGiftImage(item.vgId);
			localPath = VirtualGiftManager.getInstance().CacheVirtualGiftImagePath(item.vgId);
		} else {
			holder.ivVirtual.setVisibility(View.GONE);
		}
		
//		holder.ivPreview.setImageResource(R.drawable.test251);
		
		new ImageViewLoader(mContext).DisplayImage(holder.ivPreview, url,localPath, null);

		convertView.setClickable(true);
		convertView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = EMFAttachmentPreviewActivity.getIntent(mContext, mAttachList, position);
				intent.putExtra(EMFAttachmentPreviewActivity.VGTIPS, false);
				intent.putExtra(EMFAttachmentPreviewActivity.ATTACH_DIRECTION,(mEmfBean.type == 1) ? PrivatePhotoDirection.MW.name(): PrivatePhotoDirection.WM.name());
				((Activity)mContext).startActivityForResult(intent, EMFDetailActivity.RESULT_ATTACHMENT);
			}
		});
		return convertView;
	}

	public class ViewHolder {
		ImageView ivPreview;// 主预览图
		LinearLayout llPrivate;// 私密照标记
		ImageView ivPrivate;// 私密照icon
		ImageView ivPlay;// 短视频
		ImageView ivVirtual;// 虚拟礼物
	}

}
