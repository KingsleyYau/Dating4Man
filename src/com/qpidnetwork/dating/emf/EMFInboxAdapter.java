package com.qpidnetwork.dating.emf;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.View;
import android.view.View.OnClickListener;

import com.qpidnetwork.dating.R;
import com.qpidnetwork.dating.bean.EMFBean;
import com.qpidnetwork.dating.lady.LadyDetailActivity;
import com.qpidnetwork.manager.FileCacheManager;
import com.qpidnetwork.request.item.EMFInboxListItem;
import com.qpidnetwork.tool.ImageViewLoader;

public class EMFInboxAdapter extends EMFBaseAdapter<EMFInboxListItem>{
	
	public EMFInboxAdapter(Activity context){
		super(context);
	}
	
	/*邮件删除*/
	public void removeItemById(String emfId){
		for(EMFInboxListItem item : getDataList()){
			if(item.id.equals(emfId)){
				getDataList().remove(item);
				break;
			}
		}
		notifyDataSetChanged();
	}

	@SuppressWarnings("deprecation")
	@Override
	protected void instantiateViews(int position, ViewHolder holder) {
		// TODO Auto-generated method stub
		final EMFInboxListItem item = getItem(position);
		
		holder.llContainer.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				mContext.startActivityForResult(EMFDetailActivity.getIntent(mContext, new EMFBean(item)),EMFDetailActivity.REQUEST_CODE);
			}
		});
		
		holder.tvName.setText(item.firstName);
		holder.tvDesc.setText(item.intro);
		if (item.sendTime.length() < 8){
			holder.tvDate.setText(item.sendTime);
		}else{
			holder.tvDate.setText(item.sendTime.subSequence(0, item.sendTime.length() - 6));
		}
		
		
		if (!item.readFlag){
			holder.ivFlat.setVisibility(View.VISIBLE);
			holder.ivFlat.setImageBitmap(createUnreadMark());
			holder.this_item.setBackgroundColor(mContext.getResources().getColor(R.color.unread_item_grey));
			holder.tvName.setTextColor(Color.BLACK);
			holder.tvDate.setTextColor(Color.BLACK);
			holder.tvDesc.setTextColor(Color.BLACK);
		}else{
			holder.ivFlat.setVisibility(View.GONE);
			holder.ivFlat.setImageBitmap(createUnreadMark());
			holder.this_item.setBackgroundColor(Color.WHITE);
			holder.tvName.setTextColor(mContext.getResources().getColor(R.color.text_color_grey));
			holder.tvDate.setTextColor(mContext.getResources().getColor(R.color.text_color_grey));
			holder.tvDesc.setTextColor(mContext.getResources().getColor(R.color.text_color_grey));
			
			if (item.rFlag){
				holder.ivFlat.setVisibility(View.VISIBLE);
				holder.ivFlat.setImageResource(R.drawable.ic_reply_all_grey600_18dp);
			}else{
				holder.ivFlat.setVisibility(View.GONE);
			}
			
		}
		

		
		/*是否有附件*/
		if(item.attachnum > 0){
			holder.ivAttach.setVisibility(View.VISIBLE);
		}else{
			holder.ivAttach.setVisibility(View.GONE);
		}
		
		/*是否包含虚拟礼物*/
		if(item.virtualGifts){
			holder.ivGift.setVisibility(View.VISIBLE);
		}else{
			holder.ivGift.setVisibility(View.GONE);
		}
		
		holder.ivPhoto.setImageResource(R.drawable.female_default_profile_photo_40dp);
		/*头像处理*/
		if ( null != holder.imageDownLoader ) {
			// 停止回收旧Downloader
			holder.imageDownLoader.ResetImageView();
		}
		if((item.photoURL != null)&&(!item.photoURL.equals(""))){
			String localPath = FileCacheManager.getInstance().CacheImagePathFromUrl(item.photoURL);
			holder.imageDownLoader = new ImageViewLoader(mContext);
			holder.imageDownLoader.SetDefaultImage(mContext.getResources().getDrawable(R.drawable.female_default_profile_photo_40dp));
			holder.imageDownLoader.DisplayImage(holder.ivPhoto, item.photoURL, localPath, null);
		}
		holder.ivPhoto.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				LadyDetailActivity.launchLadyDetailActivity(mContext, item.womanid, true);
			}
		});
	}
	
	private Bitmap createUnreadMark(){
		
		int size = (int)(12.00 * mContext.getResources().getDisplayMetrics().density);
		
		Bitmap bmp = Bitmap.createBitmap(size, size, Config.ARGB_8888);
		Paint paint = new Paint();
		paint.setAntiAlias(true);
		paint.setColor(mContext.getResources().getColor(R.color.blue_color));
		
		Canvas canvas = new Canvas(bmp);
		canvas.drawCircle(size / 3, size / 3, size / 3, paint);
		
		return bmp;
		
	}
	
}
