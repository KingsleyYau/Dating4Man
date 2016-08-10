package com.qpidnetwork.dating.credit;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.qpidnetwork.dating.R;

public class MonthlyFeeNotifyAdapter extends BaseAdapter {

	private String[] tips;
	private Context mContext;
	
	public MonthlyFeeNotifyAdapter(Context context, String[] tips) {
		super();
		this.tips = tips;
		this.mContext = context;
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return tips.length;
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return tips[position];
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		if (convertView == null) {
			convertView = LayoutInflater.from(mContext).inflate(R.layout.item_dialog_monthly_fee, null);
		}
		TextView tv_servers = (TextView)convertView.findViewById(R.id.tv_servers);
		tv_servers.setText(tips[position]);

		return convertView;
	}

}
