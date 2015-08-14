package com.qpidnetwork.dating.contactus;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.qpidnetwork.dating.R;
import com.qpidnetwork.framework.base.UpdateableAdapter;
import com.qpidnetwork.request.item.TicketListItem;
import com.qpidnetwork.request.item.TicketListItem.StatusType;

public class ContactTicketListAdapter extends UpdateableAdapter<TicketListItem>{
	
	private Activity mContext;
	
	public ContactTicketListAdapter(Activity context){
		this.mContext = context;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder = null;
		if(convertView == null){
			holder = new ViewHolder();
			convertView = LayoutInflater.from(mContext).inflate(R.layout.adapter_ticket_list_item, null);
			holder.tvTicketSubject = (TextView)convertView.findViewById(R.id.tvTicketSubject);
			holder.tvTime = (TextView)convertView.findViewById(R.id.tvTime);
			holder.tvUnread = (TextView)convertView.findViewById(R.id.tvUnread);
			holder.ivProcessDone = (ImageView)convertView.findViewById(R.id.ivProcessDone);
			convertView.setTag(holder);
		}else{
			holder = (ViewHolder)convertView.getTag();
		}
		
		TicketListItem item = getItem(position);
		holder.tvTicketSubject.setText(item.title);
		holder.tvTime.setText(new SimpleDateFormat("MMM - dd", Locale.ENGLISH).format(new Date(((long)item.addDate) * 1000)));
		holder.tvUnread.setVisibility(View.GONE);
		if(item.status != StatusType.Open){
			/*已结束*/
			holder.ivProcessDone.setVisibility(View.VISIBLE);
		}else{
			holder.ivProcessDone.setVisibility(View.GONE);
			if(item.unreadNum > 0){
				holder.tvUnread.setVisibility(View.VISIBLE);
				holder.tvUnread.setText("" + item.unreadNum);
			}
		}
		return convertView;
	}

	private class ViewHolder{
		TextView tvTicketSubject;
		TextView tvTime;
		TextView tvUnread;
		ImageView ivProcessDone;
	}
	
}
