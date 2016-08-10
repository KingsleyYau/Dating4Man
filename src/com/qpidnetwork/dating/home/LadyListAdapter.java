package com.qpidnetwork.dating.home;

import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v7.widget.CardView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.qpidnetwork.dating.R;
import com.qpidnetwork.dating.advertisement.AdWomanListAdvertItem;
import com.qpidnetwork.dating.advertisement.AdvertisementManager;
import com.qpidnetwork.dating.authorization.LoginManager;
import com.qpidnetwork.dating.contacts.ContactManager;
import com.qpidnetwork.dating.emf.MailEditActivity;
import com.qpidnetwork.dating.lady.LadyDetailActivity;
import com.qpidnetwork.dating.lady.LadyListItem;
import com.qpidnetwork.dating.lady.LadyListManager;
import com.qpidnetwork.dating.lady.VideoDetailActivity;
import com.qpidnetwork.dating.livechat.ChatActivity;
import com.qpidnetwork.dating.livechat.ExpressionImageGetter;
import com.qpidnetwork.framework.util.Log;
import com.qpidnetwork.framework.util.UnitConversion;
import com.qpidnetwork.livechat.LCMessageItem;
import com.qpidnetwork.livechat.LCUserItem;
import com.qpidnetwork.livechat.LCUserItem.ChatType;
import com.qpidnetwork.livechat.LiveChatManager;
import com.qpidnetwork.manager.FileCacheManager;
import com.qpidnetwork.manager.FileCacheManager.LadyFileType;
import com.qpidnetwork.request.RequestEnum.OnlineStatus;
import com.qpidnetwork.request.RequestJniEMF.ReplyType;
import com.qpidnetwork.tool.ImageViewLoader;
import com.qpidnetwork.view.FitTopImageView;
import com.qpidnetwork.view.MaterialDropDownMenu;

public class LadyListAdapter extends BaseAdapter {
	public interface OnLadyListAdapterCallback {
		public void OnClickAddFavour(LadyListItem item);
		public void OnClickCallLady(LadyListItem item);
		public void OnClickProfileDetail(LadyListItem item);
	}
	public void SetOnLadyListAdapterCallback(OnLadyListAdapterCallback callback) {
		this.callback = callback;
	}
	public void SetAdvert(AdWomanListAdvertItem advert) {
		this.mAdvert = advert;
	}
	
	private OnLadyListAdapterCallback callback;
	private Context mContext;
	private AdWomanListAdvertItem mAdvert;
	private View mAdvertView;
	private final int mAdvertPos = 3;
	private List<LadyListItem> mLadyList;
	private ArrayList<Drawable> mDefaultDrawableList;
	private MaterialDropDownMenu dropDown;
	public enum ChatButtonType {
		Chat,
		Call,
		Video,
		Default,
	}
	public ChatButtonType mChatButtonType = ChatButtonType.Chat;
	
	private ExpressionImageGetter imageGetter;
	
	@SuppressLint("InflateParams")
	public LadyListAdapter(Context context, List<LadyListItem> data){
		this.mContext = context;
		this.mLadyList = data;
		this.mAdvert = null;
		
		// 生成背景颜色的drawable数组
		mDefaultDrawableList = new ArrayList<Drawable>();
		final int[] backgroundColorArray = LadyListManager.getBackgroundColorArray();
		for (int bcIndex = 0; bcIndex < backgroundColorArray.length; bcIndex++) {
			Drawable drawable = new ColorDrawable(backgroundColorArray[bcIndex]);
			drawable.setBounds(0, 0, LadyListManager.getLadyListItemWidth(), LadyListManager.getMaxLadyListItemHeight());
			if (null != drawable) {
				mDefaultDrawableList.add(drawable);
			}
		}
		
		// 生成广告view
		mAdvertView = LayoutInflater.from(mContext).inflate(R.layout.adapter_lady_list_advert_item, null); 
		//邀请小表情处理
		imageGetter = new ExpressionImageGetter(context, UnitConversion.dip2px(context, 16), UnitConversion.dip2px(context, 16));
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		int advertCount = 0;
		if (isShowAdvert()) 
		{
			advertCount = 1;
		}
		return mLadyList.size() + advertCount;
	}
	
	/**
	 * 是否显示广告
	 * @return
	 */
	private boolean isShowAdvert()
	{
		return mLadyList.size() >= mAdvertPos
				&& null != this.mAdvert
				&& null != this.mAdvert.adWomanListAdvert
				&& this.mAdvert.adWomanListAdvert.height > 0
				&& this.mAdvert.adWomanListAdvert.width > 0
				&& this.mAdvert.isShow;
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

	@SuppressLint({ "DefaultLocale", "InflateParams" })
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		View view = null;
		if (isShowAdvert()
			&& position == mAdvertPos) 
		{
			// 获取广告view
			view = getAdvertView(position, convertView, parent);
		}
		else {
			// 获取女士view
			view = getLadyView(position, convertView, parent);
		}
		return view;
	}
	
	public View getAdvertView(int position, View convertView, ViewGroup parent)
	{
		FitTopImageView imageView = (FitTopImageView)mAdvertView.findViewById(R.id.ivLadyPhoto);
		
		// 计算高度
		float scale = (float)mAdvert.adWomanListAdvert.height / (float)mAdvert.adWomanListAdvert.width; 
		int advertWidth = LadyListManager.getLadyListItemWidth();
		int advertHeight = (int)(advertWidth * scale); 
		
		// 下载图片，并转为上下圆角
		ImageViewLoader loader = new ImageViewLoader(mContext);
		loader.SetDefaultImage(mDefaultDrawableList.get(0));
		loader.DisplayImage(
			imageView, 
			true,
			mAdvert.adWomanListAdvert.image,
			advertWidth,
			advertHeight,
			2,
			2,
			FileCacheManager.getInstance().CacheImagePathFromUrl(mAdvert.adWomanListAdvert.image) + "_advert", 
			null
		);
		
		// 设置显示高度
		FrameLayout.LayoutParams params = (FrameLayout.LayoutParams)imageView.getLayoutParams();
		params.height = advertHeight;
		
		// 设置点击
		mAdvertView.setClickable(true);
		mAdvertView.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				// 处理点击事件
				mAdvert.Click(mContext);
				AdvertisementManager.getInstance().parseAdvertisment(mContext, mAdvert.adWomanListAdvert.adurl, mAdvert.adWomanListAdvert.openType);
			}
		});
		
		return mAdvertView;
	}
	
	@SuppressLint({ "InflateParams", "DefaultLocale" })
	public View getLadyView(int position, View convertView, ViewGroup parent)
	{
		ViewHolder holder = null;
		
		boolean isNewConvertView = false;
		if(convertView == null) {
			isNewConvertView = true;
		}else{
			if (convertView.getTag() instanceof ViewHolder) {
				holder = (ViewHolder)convertView.getTag();
				if (null != holder.loader) {
					// 重置 loader 的 imageView，防止callback失败
					holder.loader.ResetImageView();
				}
			}
			else {
				isNewConvertView = true;
			}
		}
		
		if (isNewConvertView) {
			convertView = LayoutInflater.from(mContext).inflate(R.layout.adapter_lady_list_item, null);
			holder = new ViewHolder(convertView);
			
		}
		
		long startTime = System.currentTimeMillis();
			
		if (position < 2){
			LinearLayout.LayoutParams params = (LayoutParams)holder.cvCard.getLayoutParams();
			params.topMargin = (int)(4.00 * mContext.getResources().getDisplayMetrics().density);
		}else{
			LinearLayout.LayoutParams params = (LayoutParams)holder.cvCard.getLayoutParams();
			params.topMargin = (int)(0.00 * mContext.getResources().getDisplayMetrics().density);
		}
		
		// 计算item位置偏移
		int tempPosition = position;
		if (isShowAdvert()) {
			if (position > mAdvertPos) {
				tempPosition--;
			}
		}
		final LadyListItem item = mLadyList.get(tempPosition);
		
		convertView.setClickable(true);
		convertView.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				LadyDetailActivity.launchLadyDetailActivity(mContext, item.lady.womanid, true);
			}
		});
		
		holder.tvLadyName.setText(item.lady.firstname);
		String tips = String.format("%d yrs old, %s", item.lady.age, item.lady.country);
		holder.tvLadyAge.setText(tips);
		
		// 是否在线
		if(item.lady.onlineStatus.equals(OnlineStatus.Online)){
			holder.onlineIndicator.setBackgroundResource(R.drawable.green_rounded_rect);
			//添加邀请功能
			String inviteMessage = getInviteMessage(item.lady.womanid);
			if(!TextUtils.isEmpty(inviteMessage)){
				holder.tvInivte.setVisibility(View.VISIBLE);
				holder.tvInivte.setText(imageGetter.getExpressMsgHTML(inviteMessage));
				holder.ivCanChat.setVisibility(View.VISIBLE);
			}else{
				holder.tvInivte.setVisibility(View.GONE);
				holder.ivCanChat.setVisibility(View.GONE);
			}
			holder.buttonChat.setImageResource(R.drawable.ic_chat_grey600_24dp);
			holder.buttonChat.setEnabled(true);
		}else{
			holder.onlineIndicator.setBackgroundResource(R.drawable.grey_rounded_rect);
			holder.tvInivte.setVisibility(View.GONE);
			holder.ivCanChat.setVisibility(View.GONE);
			holder.buttonChat.setImageResource(R.drawable.ic_chat_greyc8c8c8_24dp);
			holder.buttonChat.setEnabled(false);
		}
		
		LinearLayout.LayoutParams params = (LinearLayout.LayoutParams)holder.flPhoto.getLayoutParams();
//		FrameLayout.LayoutParams params = (FrameLayout.LayoutParams)holder.ivLadyPhoto.getLayoutParams();
		params.height = LadyListManager.getLadyListItemHeight(item.radio);
		
		String localPath = FileCacheManager.getInstance().CacheLadyPathFromUrl(item.lady.womanid, LadyFileType.LADY_PHOTO);
		holder.loader = new ImageViewLoader(mContext);
		holder.loader.SetDefaultImage(mDefaultDrawableList.get(item.backgroundColorType));
		holder.loader.DisplayImage(
				holder.ivLadyPhoto, 
				holder.womanId.compareTo(item.lady.womanid) != 0,
				item.lady.photoURL,
				LadyListManager.getLadyListItemWidth(),
				LadyListManager.getMaxLadyListItemHeight(),
				2,
				0,
				localPath, 
				null
		);
		holder.womanId = item.lady.womanid;
		
		// 第一个按钮
		switch (mChatButtonType) {
		case Default: 
		case Chat:{
			// 不显示
			holder.buttonCall.setVisibility(View.GONE);
			holder.buttonVideo.setVisibility(View.GONE);
		}break;
		case Call:{
			// 打电话
			holder.buttonCall.setVisibility(View.VISIBLE);
			holder.buttonVideo.setVisibility(View.GONE);
		}break;
		case Video:{
			// 视频
			holder.buttonCall.setVisibility(View.GONE);
			holder.buttonVideo.setVisibility(View.VISIBLE);
		}break;
		default:
			break;
		}

		//点击进入Livechat
		holder.buttonChat.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if(item.lady.onlineStatus.equals(OnlineStatus.Online)){
					//在线打开聊天界面
					ChatActivity.launchChatActivity(mContext, item.lady.womanid, item.lady.firstname, "");
				}
			}
		});
		
		// 点击进入发送emf
		holder.buttonMail.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				// 检测是否登录
				boolean bFlag = LoginManager.getInstance().CheckLogin(mContext);
				if( bFlag ) {
					// 跳进发送emf
					MailEditActivity.launchMailEditActivity(mContext, item.lady.womanid, ReplyType.DEFAULT, "", "");
				}
			}
		});
		
		//点击拨打电话
		holder.buttonCall.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if( callback != null ) {
					callback.OnClickCallLady(item);
				}
			}
		});
		
		//点击查看视频
		holder.buttonVideo.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				VideoDetailActivity.launchLadyVideoDetailActivity(mContext, item.lady.womanid, item.lady.firstname, "");
			}
		});
		
		
		holder.imageViewOverFlow.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				dropDown = new MaterialDropDownMenu(mContext, new String[]{mContext.getString(R.string.view_profile)}, new MaterialDropDownMenu.OnClickCallback() {
					@Override
					public void onClick(AdapterView<?> adptView, View v, int which) {
						// TODO Auto-generated method stub
						if( callback != null ) {
							//callback.OnClickAddFavour(item);
							callback.OnClickProfileDetail(item);
						}
					}
				}, new Point((int)(196.0f * mContext.getResources().getDisplayMetrics().density), LayoutParams.WRAP_CONTENT));
				dropDown.showAsDropDown(v);
			}
		});
			
		long endTime = System.currentTimeMillis();
		long diffTime = endTime - startTime;
		Log.d("LadyListAdapter", "getView() name:%s, photoUrl:%s, radio:%f, ivHeight:%d, diffTime:%d, holder:%s"
				, item.lady.firstname
				, item.lady.photoURL
				, item.radio
				, params.height
				, diffTime
				, String.valueOf(holder));
		

		return convertView;
	}
	
	private class ViewHolder{
		public FrameLayout flPhoto;
		public FitTopImageView ivLadyPhoto;
		public TextView tvLadyName;
		public CardView cvCard;
		public TextView tvLadyAge;
		public ImageViewLoader loader;
		public String womanId;
		public TextView tvInivte;
		
		public ImageView ivCanChat;
		public ImageButton buttonChat;
		public ImageButton buttonMail;
		public ImageButton buttonCall;
		public ImageButton buttonVideo;
		public ImageButton imageViewOverFlow;
		public View onlineIndicator;
		
		public ViewHolder(View convertView){
			
			flPhoto = (FrameLayout)convertView.findViewById(R.id.flPhoto);
			ivLadyPhoto = (FitTopImageView)convertView.findViewById(R.id.ivLadyPhoto);
			tvLadyName = (TextView)convertView.findViewById(R.id.tvLadyName);
			cvCard = (CardView)convertView.findViewById(R.id.cardView);
			tvLadyAge = (TextView)convertView.findViewById(R.id.tvLadyAge);
			tvInivte = (TextView)convertView.findViewById(R.id.tvInivte);
			
			ivCanChat = (ImageView)convertView.findViewById(R.id.ivCanChat);
			buttonChat = (ImageButton)convertView.findViewById(R.id.buttonChat);
			buttonMail = (ImageButton)convertView.findViewById(R.id.buttonMail);
			buttonCall = (ImageButton)convertView.findViewById(R.id.buttonCall);
			buttonVideo = (ImageButton)convertView.findViewById(R.id.buttonVideo);
			imageViewOverFlow = (ImageButton)convertView.findViewById(R.id.imageViewOverFlow);
			onlineIndicator = (View)convertView.findViewById(R.id.online_indicator);
			
			loader = null;
			womanId = "";
			
			if (Build.VERSION.SDK_INT < 21){
				buttonChat.getLayoutParams().height = UnitConversion.dip2px(mContext, 48);
				buttonChat.getLayoutParams().width = UnitConversion.dip2px(mContext, 48);
				((RelativeLayout.LayoutParams)buttonChat.getLayoutParams()).leftMargin = 0;
				
				buttonMail.getLayoutParams().height = UnitConversion.dip2px(mContext, 48);
				buttonMail.getLayoutParams().width = UnitConversion.dip2px(mContext, 48);
				((RelativeLayout.LayoutParams)buttonMail.getLayoutParams()).leftMargin = 0;
				
				imageViewOverFlow.getLayoutParams().height = UnitConversion.dip2px(mContext, 48);
				imageViewOverFlow.getLayoutParams().width = UnitConversion.dip2px(mContext, 48);
				((RelativeLayout.LayoutParams)imageViewOverFlow.getLayoutParams()).rightMargin = UnitConversion.dip2px(mContext, -6);
			}
			
//			if (mChatButtonType != ChatButtonType.Default){
//				((RelativeLayout.LayoutParams)buttonMail.getLayoutParams()).leftMargin = 0;
//			}
			
			convertView.setTag(this);
		}
	}

	
	//decode image efficiently
	public static Bitmap decodeSampledBitmapFromResource(Resources res, int resId,
	        int reqWidth, int reqHeight) {

	    // First decode with inJustDecodeBounds=true to check dimensions
	    final BitmapFactory.Options options = new BitmapFactory.Options();
	    options.inJustDecodeBounds = true;
	    BitmapFactory.decodeResource(res, resId, options);

	    // Calculate inSampleSize
	    options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

	    // Decode bitmap with inSampleSize set
	    options.inJustDecodeBounds = false;
	    Bitmap src = BitmapFactory.decodeResource(res, resId, options);
	    return createScaleBitmap(src, reqWidth, reqHeight);
	}
	
	public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
	    // Raw height and width of image
	    final int height = options.outHeight;
	    final int width = options.outWidth;
	    int inSampleSize = 1;
	
	    if (height > reqHeight || width > reqWidth) {
	
	        final int halfHeight = height / 2;
	        final int halfWidth = width / 2;
	
	        // Calculate the largest inSampleSize value that is a power of 2 and keeps both
	        // height and width larger than the requested height and width.
	        while ((halfHeight / inSampleSize) > reqHeight
	                && (halfWidth / inSampleSize) > reqWidth) {
	            inSampleSize *= 2;
	        }
	    }
	    
	    return inSampleSize;
	}
	
	  // 如果是放大图片，filter决定是否平滑，如果是缩小图片，filter无影响
    private static Bitmap createScaleBitmap(Bitmap src, int dstWidth,
            int dstHeight) {
        Bitmap dst = Bitmap.createScaledBitmap(src, dstWidth, dstHeight, true);
        if (src != dst) { // 如果没有缩放，那么不回收
            src.recycle(); // 释放Bitmap的native像素数组
        }
        return dst;
    }
    
    /*
     * 获取这个女士发来的最后一条消息
     */
    private String getInviteMessage(String womanId){
    	String inviteMessage = "";
    	LiveChatManager livechatManager = LiveChatManager.getInstance();
    	LCUserItem userItem = livechatManager.GetUserWithId(womanId);
    	if((userItem.chatType == ChatType.Invite)
    			&&(userItem.getMsgList()!= null)
    			&&(userItem.getMsgList().size()>0)){
    		LCMessageItem lastMsg = userItem.getTheOtherLastMessage();
    		if(lastMsg != null){
    			inviteMessage = ContactManager.getInstance().generateMsgHint(lastMsg);
    		}
    	}
    	return inviteMessage;
    }
}
