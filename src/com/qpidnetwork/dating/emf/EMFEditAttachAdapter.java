package com.qpidnetwork.dating.emf;

import java.util.List;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

public class EMFEditAttachAdapter extends BaseAdapter {
	
	private Context mContext;
	
	private List<View> mAttachList;
	
	public EMFEditAttachAdapter(Context context, List<View> attachList){
		mContext = context;
		mAttachList = attachList;
	}
	
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return mAttachList.size();
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

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		
		return mAttachList.get(position);
	}
	
}
