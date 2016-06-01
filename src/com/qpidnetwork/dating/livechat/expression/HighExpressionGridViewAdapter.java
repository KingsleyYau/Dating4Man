package com.qpidnetwork.dating.livechat.expression;

import java.io.File;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.os.Build;
import android.text.TextUtils;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.qpidnetwork.dating.R;
import com.qpidnetwork.livechat.LCMagicIconItem;
import com.qpidnetwork.livechat.LiveChatManager;
import com.qpidnetwork.request.item.MagicIconItem;

/**
 * @author Yanni
 * 
 * @version 2016-4-13
 */
public class HighExpressionGridViewAdapter extends BaseAdapter implements OnLongClickListener {

	private List<MagicIconItem> mIconItemList;
	private Context mContext;
	private LiveChatManager mLiveChatManager;

	public HighExpressionGridViewAdapter(Context context,
			List<MagicIconItem> iconItemList) {
		super();
		// TODO Auto-generated constructor stub
		this.mContext = context;
		this.mIconItemList = iconItemList;
		mLiveChatManager = LiveChatManager.getInstance();
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return mIconItemList.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return mIconItemList.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		ViewHolder viewHolder;
		if (convertView == null) {
			viewHolder = new ViewHolder();
			
			float density = mContext.getResources().getDisplayMetrics().density;
			Display display = ((Activity) mContext).getWindowManager().getDefaultDisplay();
			Point size = new Point();
			
			if(Build.VERSION.SDK_INT > 12){
				display.getSize(size);
			}else{
				size.x = display.getWidth();
				size.y = display.getHeight();
			}
			
			int item_size = (int)(((float)size.x - (int)(3.0f * density)) / 4);
			
			convertView = LayoutInflater.from(mContext).inflate(R.layout.item_preminum_emotion, null);
			viewHolder.ivImg = (ImageView) convertView.findViewById(R.id.icon);
			viewHolder.tvPrice = (TextView) convertView.findViewById(R.id.price);
			
			RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(item_size, item_size);
			viewHolder.ivImg.setLayoutParams(params);
			
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}
		viewHolder.tvPrice.setText(mIconItemList.get(position).price+"");
		LCMagicIconItem item = mLiveChatManager.GetMagicIconInfo(mIconItemList.get(position).id);
		String localPath = item.getThumbPath();
		if(!TextUtils.isEmpty(localPath) && (new File(localPath).exists())){
//			new ImageViewLoader(mContext).DisplayImage(viewHolder.ivImg, null, localPath, null);
			viewHolder.ivImg.setImageBitmap(BitmapFactory.decodeFile(localPath));
		}else{
			mLiveChatManager.GetMagicIconThumbImage(item.getMagicIconId());
		}
//		convertView.setOnLongClickListener(this);
//		convertView.setOnClickListener(this);
		return convertView;
	}

	public static class ViewHolder {
		ImageView ivImg;
		TextView tvPrice;
	}

	@Override
	public boolean onLongClick(View v) {
		// TODO Auto-generated method stub
//		if (!preview.isShowing()){
//			preview.showAtLocation(v, Gravity.CENTER, 0, 0);
//			canScroll = false;
//			previewPosition = position;
//			playEmotion();
//			if (onItemClickCallback != null ) onItemClickCallback.onItemLongClick();
//		}
		return false;
	}

}
