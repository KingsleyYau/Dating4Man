package com.qpidnetwork.framework.base;

import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.qpidnetwork.dating.R;
import com.qpidnetwork.framework.util.UnitConversion;
import com.qpidnetwork.framework.widget.stickygridheaders.StickyGridHeadersGridView;
import com.qpidnetwork.view.ButtonRaised;

/**
 * 封装列表空及错误页面逻辑
 * @author Hunter
 * @since 2016.5.18
 */
public class BaseSortedGridViewFragment extends BaseFragment implements OnRefreshListener{
	
	/*列表Views*/
	private SwipeRefreshLayout swipeRefreshLayout;
	private StickyGridHeadersGridView sortedGridView;
	private SwipeRefreshLayout swipeLayoutEmpty;
	private TextView emptyView;
	
	/*错误页面*/
	private LinearLayout llInitContainer;
	private ButtonRaised btnRetry;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_base_sorted_gridview, null);
		
		swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipeRefresh);
		sortedGridView = (StickyGridHeadersGridView) view.findViewById(R.id.sortedGridView);
		sortedGridView.setAreHeadersSticky(false);
		swipeLayoutEmpty = (SwipeRefreshLayout) view.findViewById(R.id.swipeLayoutEmpty);
		emptyView = (TextView) view.findViewById(R.id.emptyView);
		
		llInitContainer = (LinearLayout)view.findViewById(R.id.llInitContainer);
		btnRetry = (ButtonRaised) view.findViewById(R.id.btnRetry);
	
		btnRetry.setOnClickListener(this);
		swipeRefreshLayout.setOnRefreshListener(this);
		swipeLayoutEmpty.setOnRefreshListener(this);
		swipeRefreshLayout.setProgressViewOffset(false, 0, UnitConversion.dip2px(mContext, 24));
		swipeRefreshLayout.setProgressViewOffset(false, 0, UnitConversion.dip2px(mContext, 24));
		sortedGridView.setEmptyView(swipeLayoutEmpty);
		
		initDownloadData();
		return view;
	}

	@Override
	public void onRefresh() {
		onDataNeedRefresh();
	}
	
	public void onDataNeedRefresh(){
		
	}
	
	private void initDownloadData(){
		swipeRefreshLayout.setRefreshing(true);
		onDataNeedRefresh();
	}
	
	/**
	 * 刷新完成
	 * @param isSuccess 是否成功
	 */
	public void onRefreshComplete(boolean isSuccess){
		swipeRefreshLayout.setRefreshing(false);
		swipeLayoutEmpty.setRefreshing(false);
		if(!isSuccess){
			llInitContainer.setVisibility(View.VISIBLE);
			swipeRefreshLayout.setVisibility(View.GONE);
			swipeLayoutEmpty.setVisibility(View.GONE);
		}
	}
	
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		super.onClick(v);
		switch (v.getId()) {
		case R.id.btnRetry:{
			llInitContainer.setVisibility(View.GONE);
			swipeRefreshLayout.setVisibility(View.VISIBLE);
			swipeLayoutEmpty.setVisibility(View.VISIBLE);
			swipeRefreshLayout.setRefreshing(true);
			onDataNeedRefresh();
		}break;
		}
	}
	
	/**
	 * 设置列表空时显示
	 * @param emptyText
	 */
	public void setEmptyMessage(String emptyText){
		if(emptyView != null){
			emptyView.setText(emptyText);
		}
	}
	
	/**
	 * 获取当前SortedGridView
	 * @return
	 */
	public StickyGridHeadersGridView getSortedGridView(){
		return sortedGridView;
	}
}
