package com.qpidnetwork.dating.livechat.normalexp;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;

import com.qpidnetwork.dating.R;
import com.qpidnetwork.framework.base.BaseFragment;

public class NormalExprssionFragment extends BaseFragment{
	
	private GridView gridView;
	
	@SuppressLint("InflateParams")
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.viewstub_expression, null);
		gridView = (GridView) view.findViewById(R.id.gridView);
		//dotsView = (DotsView) view.findViewById(R.id.dotsView);
		//viewPagerExpr = (ViewPager) view.findViewById(R.id.viewPagerExpr);
		return view;
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onActivityCreated(savedInstanceState);
		
		ExpressionGridAdapter dapt = new ExpressionGridAdapter(getActivity(), 0, 180);
		gridView.setAdapter(dapt);
		
	}

}
