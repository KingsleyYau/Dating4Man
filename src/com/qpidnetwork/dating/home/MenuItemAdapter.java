package com.qpidnetwork.dating.home;

import java.util.List;

import com.qpidnetwork.dating.R;
import com.qpidnetwork.dating.home.MenuHelper.MenuItemBean;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class MenuItemAdapter extends BaseAdapter{
	
	private Context mContext;
	private List<MenuItemBean> menuList;
	private int[] colors = new int[]{
		
		R.color.brand_color_light2,
		R.color.brand_color_light3,
		R.color.brand_color_light4,
		R.color.brand_color_light1,
		R.color.brand_color_light5,
		R.color.brand_color_light6
	};
	
	public MenuItemAdapter(Context context, List<MenuItemBean> menuList){
		this.mContext = context;
		this.menuList = menuList;
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return menuList.size();
	}

	@Override
	public MenuItemBean getItem(int position) {
		// TODO Auto-generated method stub
		return menuList.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return 0;
	}

	@SuppressWarnings("deprecation")
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		ViewHolder holder;
		if(convertView == null){
			holder = new ViewHolder();
			convertView = LayoutInflater.from(mContext).inflate(R.layout.cell_menu_item, null);
			holder.ivIcon = (ImageView)convertView.findViewById(R.id.ivMenuIcon);
			holder.tvDesc = (TextView)convertView.findViewById(R.id.tvMenuDesc);
			holder.tvUnread = (TextView)convertView.findViewById(R.id.tvMenuUnread);
			convertView.setTag(holder);
		}else{
			holder = (ViewHolder)convertView.getTag();
		}
		

		if (position < 5){
			Bitmap circle = createColorfulCircle(mContext.getResources().getColor(colors[position]));
			Drawable bg;
			if (Build.VERSION.SDK_INT > 15){
				bg = new BitmapDrawable(mContext.getResources(), circle);
				holder.ivIcon.setBackground(bg);
			}else{
				bg = new BitmapDrawable(circle);
				holder.ivIcon.setBackgroundDrawable(bg);
			}
		}
		
		MenuItemBean itemBean = menuList.get(position);
		if(itemBean.iconResId >=0 ){
			holder.ivIcon.setImageResource(itemBean.iconResId);
			holder.ivIcon.setVisibility(View.VISIBLE);
		}else{
			holder.ivIcon.setVisibility(View.GONE);
		}
		holder.tvDesc.setText(itemBean.menuDesc);
		if(itemBean.unreadCount > 0 ){
			holder.tvUnread.setVisibility(View.VISIBLE);
			holder.tvUnread.setText(String.valueOf(itemBean.unreadCount));
		}else{
			holder.tvUnread.setVisibility(View.GONE);
		}
		return convertView;
	}
	
	/**
	 * 更新已有列表部分数据（未读条数）
	 * @param position
	 */
	public void update(int position, int unreadCount){
		menuList.get(position).unreadCount = unreadCount;
		notifyDataSetChanged();
	}
	
	private Bitmap createColorfulCircle(int color){
		int size = (int)(36.00 * mContext.getResources().getDisplayMetrics().density);
		Bitmap bmp = Bitmap.createBitmap(size, size, Config.ARGB_8888);
		Paint paint = new Paint();
		paint.setColor(color);
		paint.setAntiAlias(true);
		
		Canvas canvas = new Canvas(bmp);
		canvas.drawCircle(size / 2, size / 2, size / 2, paint);
		
		return bmp;
		
		
	}
	
	private class ViewHolder{
		ImageView ivIcon;
		TextView tvDesc;
		TextView tvUnread;
	}

}
