package com.qpidnetwork.dating.admirer;

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
import com.qpidnetwork.dating.emf.EMFBaseAdapter;
import com.qpidnetwork.dating.emf.EMFDetailActivity;
import com.qpidnetwork.dating.lady.LadyDetailActivity;
import com.qpidnetwork.manager.FileCacheManager;
import com.qpidnetwork.request.RequestJniEMF.ReplyFlagType;
import com.qpidnetwork.request.item.EMFAdmirerListItem;
import com.qpidnetwork.tool.ImageViewLoader;

public class AdmirerListAdapter extends EMFBaseAdapter<EMFAdmirerListItem>{
	
	public AdmirerListAdapter(Activity context){
		super(context);
	}
	
	/*邮件删除*/
	public void removeItemById(String emfId){
		for(EMFAdmirerListItem item : getDataList()){
			if(item.id.equals(emfId)){
				getDataList().remove(item);
				break;
			}
		}
		notifyDataSetChanged();
	}

	@SuppressWarnings("deprecation")
	@Override
	protected void instantiateViews(int position, final ViewHolder holder) {
		// TODO Auto-generated method stub
		
		final EMFAdmirerListItem item = getItem(position);
		
		holder.llContainer.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				mContext.startActivityForResult(EMFDetailActivity.getIntent(mContext, new EMFBean(item)), EMFDetailActivity.REQUEST_CODE);
			}
		});
		
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
		
		holder.tvName.setText(item.firstname);
		holder.tvDate.setText(item.sendTime);
		
		if (!item.readflag){
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
			
			if (item.replyflag.equals(ReplyFlagType.REPLIED)){
				holder.ivFlat.setVisibility(View.VISIBLE);
				holder.ivFlat.setImageResource(R.drawable.ic_reply_all_grey600_18dp);
			}else{
				holder.ivFlat.setVisibility(View.GONE);
			}
		}
		
		//if (item.photoURL != null && item.)
		/*是否有附件*/
		if(item.attachnum > 0){
			holder.ivAttach.setVisibility(View.VISIBLE);
		}else{
			holder.ivAttach.setVisibility(View.GONE);
		}
		holder.ivGift.setVisibility(View.GONE);
		
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
