package com.qpidnetwork.dating.contacts;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.Locale;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.qpidnetwork.dating.R;
import com.qpidnetwork.dating.authorization.LoginManager;
import com.qpidnetwork.dating.bean.ContactBean;
import com.qpidnetwork.dating.emf.MailEditActivity;
import com.qpidnetwork.dating.lady.LadyDetailActivity;
import com.qpidnetwork.dating.lady.LadyDetailManager;
import com.qpidnetwork.dating.livechat.ChatActivity;
import com.qpidnetwork.dating.livechat.ExpressionImageGetter;
import com.qpidnetwork.framework.base.UpdateableAdapter;
import com.qpidnetwork.framework.util.StringUtil;
import com.qpidnetwork.framework.util.UnitConversion;
import com.qpidnetwork.framework.widget.CircleImageView;
import com.qpidnetwork.manager.FileCacheManager;
import com.qpidnetwork.request.OnRequestCallback;
import com.qpidnetwork.request.RequestJniEMF.ReplyType;
import com.qpidnetwork.tool.ImageViewLoader;
import com.qpidnetwork.view.FlatToast;
import com.qpidnetwork.view.MaterialNameCardDialog;
import com.qpidnetwork.view.MaterialNameCardDialog.NameCardButton;

public class ContactsAdapter extends UpdateableAdapter<ContactBean>{

	
	
	public Activity mContext;
	public int type = 0;//0:联系人列表 1:livechat列表 
	private OnContactListItemLongClickListener listener;
	private static String[] attachmentText;
	private ExpressionImageGetter imageGetter; 
	private MaterialNameCardDialog nameCard;
	private FlatToast flatToast;
	
	
	public ContactsAdapter(Activity context, int type){
		this.mContext = context;
		this.type = type;
		attachmentText = mContext.getResources().getStringArray(R.array.livechat_msg_in_type);
		imageGetter = new ExpressionImageGetter(context, UnitConversion.dip2px(context, 16), UnitConversion.dip2px(context, 16));
		nameCard = new MaterialNameCardDialog(mContext);
	}
	
	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		if(convertView == null){
			holder = new ViewHolder();
			convertView = LayoutInflater.from(mContext).inflate(R.layout.adapter_contact_livechat_item, null);
			holder.llContainer = (LinearLayout)convertView.findViewById(R.id.llContainer);
			holder.ivPhoto = (CircleImageView)convertView.findViewById(R.id.ivPhoto);
			holder.ivOnline = (ImageView)convertView.findViewById(R.id.ivOnline);
			holder.tvName = (TextView)convertView.findViewById(R.id.tvName);
			holder.ivFavorite = (ImageView)convertView.findViewById(R.id.ivFavorite);
			holder.ivVideos = (ImageView)convertView.findViewById(R.id.ivVideos);
			holder.ivInchat = (ImageView)convertView.findViewById(R.id.ivInchat);
			holder.tvDesc = (TextView)convertView.findViewById(R.id.tvDesc);
			holder.ivUnread = (ImageView) convertView.findViewById(R.id.ivFlat);
			holder.ivUnread.setImageBitmap(createUnreadMark());
			holder.imageDownLoader = null;
			convertView.setTag(holder);
		}else{
			holder = (ViewHolder)convertView.getTag();
		}
		
		final ContactBean item = getItem(position);
		holder.tvName.setText(item.firstname);
		
		if (item.unreadCount > 0){
			holder.ivUnread.setVisibility(View.VISIBLE);
			holder.tvDesc.setTextColor(mContext.getResources().getColor(R.color.text_color_dark));
//			convertView.setBackgroundColor(mContext.getResources().getColor(R.color.thin_grey));
			holder.llContainer.setBackgroundResource(R.drawable.selector_menu_unread);
			
		}else{
			holder.ivUnread.setVisibility(View.GONE);
			holder.tvDesc.setTextColor(mContext.getResources().getColor(R.color.text_color_grey));
//			convertView.setBackgroundColor(Color.WHITE);
			holder.llContainer.setBackgroundResource(R.drawable.touch_feedback_holo_light);
		}
		
		/*在线状态*/
		if(item.isOnline){
			holder.ivOnline.setVisibility(View.VISIBLE);
		}else{
			holder.ivOnline.setVisibility(View.GONE);
		}
		
		if(item.isInchating && item.isOnline){
			holder.ivInchat.setVisibility(View.VISIBLE);
		}else{
			holder.ivInchat.setVisibility(View.GONE);
		}
		
		if(item.isfavorite){
			holder.ivFavorite.setVisibility(View.VISIBLE);
		}else{
			holder.ivFavorite.setVisibility(View.GONE);
		}
		
		if (type == 1){
			holder.ivFavorite.setVisibility(View.GONE);
		}
		
		if(item.videoCount > 0){
			holder.ivVideos.setVisibility(View.GONE);  /**不需要Video標誌(Martin)**/
		}else{
			holder.ivVideos.setVisibility(View.GONE);
		}	

		if(StringUtil.isEmpty(item.msgHint)){
			holder.tvDesc.setText(mContext.getString(R.string.contact_list_desc, new SimpleDateFormat("dd MMM, yyyy", Locale.ENGLISH).format(new Date(((long)item.getLastUpdateTime()) * 1000))));
		}else{
			holder.tvDesc.setText(imageGetter.getExpressMsgHTML(item.msgHint));
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
		
		holder.llContainer.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if(type == 0){
					
					nameCard.setCallback(new MaterialNameCardDialog.Callback() {
						
						@Override
						public void OnButtonClick(NameCardButton buttton) {
							// TODO Auto-generated method stub
							switch (buttton){
							case CHAT:
								ChatActivity.launchChatActivity(mContext, item.womanid, item.firstname, item.photoURL);
								break;
							case MAIL:
								MailEditActivity.launchMailEditActivity(mContext, item.womanid, ReplyType.DEFAULT, "");
								break;
							case ADDFAVORITE:
								
								if( !LoginManager.getInstance().CheckLogin(mContext, false) ) {
									return;
								}
								
								if (flatToast == null) flatToast = new FlatToast(mContext);
								if (flatToast.isShowing()) flatToast.cancelImmediately();
								flatToast.setProgressing("Adding");
								flatToast.show();
								
//								RequestOperator.getInstance().AddFavouritesLady(item.womanid, new OnRequestCallback() {
								LadyDetailManager.getInstance().AddFavour(item.womanid, new OnRequestCallback() {	
									@Override
									public void OnRequest(boolean isSuccess, String errno, String errmsg) {
										// TODO Auto-generated method stub
										if (isSuccess){
											mContext.runOnUiThread(new Runnable(){

												@Override
												public void run() {
													// TODO Auto-generated method stub
													try{
														flatToast.setDone("Done!");//避免activity 関了它還調toast導致崩潰, 所以try了一下.
													}catch(Exception e){};
													
													item.isfavorite = true;
//													LadyDetailManager.getInstance().SetLocalLadyFavour(item.womanid, true);
													notifyDataSetChanged(false);
												}
												
											});
										}else{
											mContext.runOnUiThread(new Runnable(){

												@Override
												public void run() {
													// TODO Auto-generated method stub
													try{
														flatToast.setFailed("Failed!");//避免activity 関了它還調toast導致崩潰.
													}catch(Exception e){};
													
												}
												
											});
										}
									}
								});
								
								
								break;
							case UNFAVORITE:
								
								if (flatToast == null) flatToast = new FlatToast(mContext);
								if (flatToast.isShowing()) flatToast.cancelImmediately();
								flatToast.setProgressing("Removing");
								flatToast.show();
		
//								RequestOperator.getInstance().RemoveFavouritesLady(item.womanid, new OnRequestCallback() {
								LadyDetailManager.getInstance().RemoveFavour(item.womanid, new OnRequestCallback() {
									@Override
									public void OnRequest(boolean isSuccess, String errno, String errmsg) {
										// TODO Auto-generated method stub
										if (isSuccess){
											mContext.runOnUiThread(new Runnable(){

												@Override
												public void run() {
													// TODO Auto-generated method stub
													try{
														flatToast.setDone("Done!");
													}catch(Exception e){};  //避免activity 関了它還調toast導致崩潰.
													item.isfavorite = false;
//													LadyDetailManager.getInstance().SetLocalLadyFavour(item.womanid, false);
													notifyDataSetChanged(false);
												}
												
											});
										}else{
											mContext.runOnUiThread(new Runnable(){

												@Override
												public void run() {
													// TODO Auto-generated method stub
													try{
														flatToast.setFailed("Failed!");
													}catch(Exception e){};
												}
												
											});
										}
									}
								});
								
								
								break;
							case PHOTO:
								LadyDetailActivity.launchLadyDetailActivity(mContext, item.womanid, true);
								break;
							default:
								break;
							}
						}
					});
					
					nameCard.setContactItem(item);
					nameCard.show();
					
					
					
					//LadyDetailActivity.launchLadyDetailActivity(mContext, item.womanid, true);
				}else if(type == 1){
					ChatActivity.launchChatActivity(mContext, item.womanid, item.firstname, item.photoURL);
				}
			}
		});
		
		holder.llContainer.setOnLongClickListener(new OnLongClickListener() {
			
			@Override
			public boolean onLongClick(View v) {
				// TODO Auto-generated method stub
				if(listener != null){
					listener.onContactListItemLongClick(position);
				}
				return false;
			}
		});
		
			
		return convertView;
	}
	
	/**
	 * 长按回调
	 * @param listener
	 */
	public void setOnContactListItemLongClickListener(OnContactListItemLongClickListener listener){
		this.listener = listener;
	}
	
	public interface OnContactListItemLongClickListener{
		public void onContactListItemLongClick(int position);
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
	
	protected class ViewHolder{
		public LinearLayout llContainer;
		public ImageView ivPhoto;
		public ImageView ivOnline;
		public TextView tvName;
		public ImageView ivFavorite;
		public ImageView ivVideos;
		public ImageView ivInchat;
		public TextView tvDesc;
		public ImageView ivUnread;
		public ImageViewLoader imageDownLoader;
	}
}
