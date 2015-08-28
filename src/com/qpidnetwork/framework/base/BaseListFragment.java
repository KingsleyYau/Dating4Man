package com.qpidnetwork.framework.base;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.qpidnetwork.dating.R;
import com.qpidnetwork.dating.bean.PageBean;
import com.qpidnetwork.view.ButtonRaised;
import com.qpidnetwork.view.MartinListView;
import com.qpidnetwork.view.MartinListView.OnPullRefreshListener;
import com.qpidnetwork.view.MaterialProgressBar;

/**
 * 带上拉和下拉刷新的ListFragment（设置默认无数据显示)
 * 
 * @author Hunter
 * @since 2015.5.16
 */
@SuppressLint("InflateParams")
public class BaseListFragment extends BaseFragment implements OnPullRefreshListener{

	
	
	public int mPageSize = 30;
	private PageBean pageBean = new PageBean(mPageSize);
	
	/*第一次初始化特殊加载及错误逻辑处理*/
	private LinearLayout llInitContainer;
	private MaterialProgressBar pbLoading;
	private View includeError;
//	private ImageView ivError;
//	private TextView tvErrorMsg;
	// private Button btnRetry;
	private ButtonRaised btnRetry;

	private MartinListView refreshListview;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		View view = inflater.inflate(
				R.layout.fragment_base_pulltorefreshlistview, null);
		refreshListview = (MartinListView) view.findViewById(R.id.refreshListview);
		
		llInitContainer = (LinearLayout)view.findViewById(R.id.llInitContainer);
		pbLoading = (MaterialProgressBar) view.findViewById(R.id.pbLoading11);
		pbLoading.setBarColor(getResources().getColor(R.color.blue_color));
		pbLoading.spin();
		// pbLoading.se
		includeError = (View) view.findViewById(R.id.includeError);
//		ivError = (ImageView) view.findViewById(R.id.ivError);
//		tvErrorMsg = (TextView) view.findViewById(R.id.tvErrorMsg);
		btnRetry = (ButtonRaised) view.findViewById(R.id.btnRetry);

		setRefreshView();
		return view;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onActivityCreated(savedInstanceState);
	}

	public void setRefreshView() {
		refreshListview.setOnPullRefreshListener(this);
	}


	public ListView getPullToRefreshListView() {
		return refreshListview.getListView();
	}

	/**
	 * 设置无数据时提示
	 * 
	 * @param empty
	 */
	public void setEmptyText(String empty) {
		refreshListview.setEmptyMessage(empty);
	}

	/**
	 * 显示初始化无数据提示
	 */
	public void showInitLoading() {
		llInitContainer.setVisibility(View.VISIBLE);
		refreshListview.setVisibility(View.GONE);
		pbLoading.setVisibility(View.VISIBLE);
		includeError.setVisibility(View.GONE);
	}

	/**
	 * 初始化出错显示
	 */
	public void showInitError() {
		llInitContainer.setVisibility(View.VISIBLE);
		refreshListview.setVisibility(View.GONE);
		pbLoading.setVisibility(View.GONE);
		includeError.setVisibility(View.VISIBLE);
		btnRetry.setOnClickListener(this);
	}

	/**
	 * 初始化成功处理
	 */
	public void hideLoadingPage() {
		llInitContainer.setVisibility(View.GONE);
		refreshListview.setVisibility(View.VISIBLE);
	}
	
	/*下拉刷新*/
	public void onPullDownToRefresh() {
		// TODO Auto-generated method stub
	}
	
	/*上拉刷新*/
	public void onPullUpToRefresh() {
		// TODO Auto-generated method stub
	}
	
	/*刷新成功*/
	public void onRefreshComplete(){
		refreshListview.onRefreshComplete();
	}

	protected PageBean getPageBean() {
		return pageBean;
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		super.onClick(v);
		switch (v.getId()) {
		case R.id.btnRetry:
			onInitRetry();
			break;
		}
	}

	public void onInitRetry() {

	}

	/**
	 * 无更多关闭下拉刷新
	 */
	public void closePullUpRefresh(){
		refreshListview.setCanPullUp(false);
	}
	
	/**
	 * 关闭下拉刷新功能
	 */
	public void closePullDownRefresh(){
		refreshListview.setCanPullDown(false);
	}
}
