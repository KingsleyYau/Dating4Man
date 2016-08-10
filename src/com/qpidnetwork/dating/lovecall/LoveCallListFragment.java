package com.qpidnetwork.dating.lovecall;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.qpidnetwork.dating.R;
import com.qpidnetwork.dating.bean.LoveCallBean;
import com.qpidnetwork.dating.bean.PageBean;
import com.qpidnetwork.dating.home.HomeActivity;
import com.qpidnetwork.framework.base.BaseListFragment;
import com.qpidnetwork.request.OnQueryLoveCallListCallback;
import com.qpidnetwork.request.RequestJniLoveCall.SearchType;
import com.qpidnetwork.request.RequestOperator;
import com.qpidnetwork.request.item.LoveCall;

public class LoveCallListFragment extends BaseListFragment{
	
	private static final int LOVE_CALL_LIST_INIT =0;
	private static final int LOVE_CALL_LIST_REFRESH = 1;
	private static final int LOVE_CALL_LIST_MORE = 2;
	public static final int MAX_TIMESTAMP = 15 * 60 * 1000;
	
	private static final int GET_LOVE_CALL_SUCCESS = 3;
	private static final int GET_LOVE_CALL_FAILED = 4;
	
	private LoveCallListAdapter mAdapter;	
	private boolean isInited = false;//记录是否初始化成功，处理不同的加载及错误界面
	private long lastUpdate = 0;//最后一次刷新时间
	private int mIndex = 0;/** index = 0 schedule * index = 1 request */
	
	
	public static LoveCallListFragment newInstance(int index){
		LoveCallListFragment fragment = new LoveCallListFragment();
		Bundle bundle = new Bundle();
		bundle.putInt("index", index);
		fragment.setArguments(bundle);
		return fragment;
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onActivityCreated(savedInstanceState);
		Bundle bundle = getArguments();
		if(bundle != null){
			if(bundle.containsKey("index")){
				mIndex = bundle.getInt("index");
			}
		}
		mAdapter = new LoveCallListAdapter(getActivity());
		getPullToRefreshListView().setAdapter(mAdapter);
		queryLoveCallList(LOVE_CALL_LIST_INIT);
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		if(resultCode == Activity.RESULT_OK){
			if(requestCode == 0){
				/*confirm or decline call back*/
				Bundle bundle = data.getExtras();
				String orderId = "";
				if(bundle != null){
					if(bundle.containsKey(LoveCallDetailActivity.LOVE_CALL_ORDER_ID)){
						orderId = bundle.getString(LoveCallDetailActivity.LOVE_CALL_ORDER_ID);
					}
				}
				List<LoveCallBean> list = mAdapter.getDataList();
				for(LoveCallBean item : list){
					if(item.orderid.equals(orderId)){
						list.remove(item);
						break;
					}
				}
				mAdapter.replaceList(list);
			}
		}
	}
	
	/**
	 * 用于记录页面操作，用于retry等记录使用
	 * @param operation 记录当前操作，用于retry操作
	 */
	private void queryLoveCallList(final int operation){
		PageBean pageBean = getPageBean();
		if(operation == LOVE_CALL_LIST_INIT){
			showInitLoading();
		}
		SearchType type = SearchType.SCHEDULED;
		if(mIndex == 0){
			type = SearchType.SCHEDULED;
		}else if(mIndex == 1){
			type = SearchType.REQUEST;
		}
		RequestOperator.getInstance().QueryLoveCallList(pageBean.getNextPageIndex(), pageBean.getPageSize(), type, new OnQueryLoveCallListCallback() {
			
			@Override
			public void OnQueryLoveCallList(boolean isSuccess, String errno, String errmsg,
					LoveCall[] itemList, int totalCount) {
				// TODO Auto-generated method stub
				Message msg = Message.obtain();
				msg.arg1 = operation;
				if(isSuccess){
					getPageBean().setDataCount(totalCount);
					msg.what = GET_LOVE_CALL_SUCCESS;
					msg.obj = itemList;
				}else{
					//请求失败，页数减1
					getPageBean().decreasePageIndex();
					msg.what = GET_LOVE_CALL_FAILED;
					msg.obj = errmsg;
				}
				sendUiMessage(msg);
			}
		});
	}
	
	@Override
	protected void handleUiMessage(Message msg) {
		// TODO Auto-generated method stub
		super.handleUiMessage(msg);
		switch (msg.what) {
		case GET_LOVE_CALL_SUCCESS:
			LoveCall[] itemList = (LoveCall[])msg.obj;
			if((msg.arg1 == LOVE_CALL_LIST_INIT) || (msg.arg1 == LOVE_CALL_LIST_REFRESH)){
				if(itemList != null && itemList.length > 0){//加判断
					mAdapter.replaceList(convertLoveCallBean(itemList));
					lastUpdate = System.currentTimeMillis();
					if(msg.arg1 == LOVE_CALL_LIST_INIT){
						isInited = true;
						hideLoadingPage();
					}
				}else{
					isInited = false;//重置init
					showInitEmpty(getEmptyView());// 显示空界面
				}
				
				
			}else if(msg.arg1 == LOVE_CALL_LIST_MORE){
				mAdapter.appendList(convertLoveCallBean(itemList));
			}
			
			break;

		case GET_LOVE_CALL_FAILED:
			if(msg.arg1 == LOVE_CALL_LIST_INIT){
				showInitError();
			}
			break;
		}
		
		onRefreshComplete();
	}
	/**
	 * @return 设置emptyView
	 */
	private View getEmptyView() {
		// TODO Auto-generated method stub
		View view  = LayoutInflater.from(mContext).inflate(R.layout.view_love_call_empty, null);
		if(mIndex==0){
			((TextView)view.findViewById(R.id.tvLoveCallType)).setText(R.string.love_call_scheduled_empty);
		}else if(mIndex==1){
			((TextView)view.findViewById(R.id.tvLoveCallType)).setText(R.string.love_call_request_empty);
		}
		view.findViewById(R.id.btnCall).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				getActivity().sendBroadcast(new Intent(HomeActivity.REFRESH_AVAIABLE_CALL_LADY));
				getActivity().finish();
			}
		});
		return view;
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		isInited = false;
		lastUpdate = 0;
	}
	
	/**
	 * tab 切换时刷新列表接口
	 * @param forceRefresh
	 */
	public void TabSelectTimeoutRefresh(boolean forceRefresh){
		if(isInited){
			if(forceRefresh){
				queryLoveCallList(LOVE_CALL_LIST_REFRESH);
			}else{
				if(System.currentTimeMillis() - lastUpdate >= MAX_TIMESTAMP){
					queryLoveCallList(LOVE_CALL_LIST_REFRESH);
				}
			}
		}else{
			/*未初始化成功时，每次切换都刷新*/
			queryLoveCallList(LOVE_CALL_LIST_INIT);
		}
	}
	
	@Override
	public void onInitRetry() {
		// TODO Auto-generated method stub
		super.onInitRetry();
		TabSelectTimeoutRefresh(false);
	}
	
	/**
	 * 回调数据的再次封装
	 * @param itemList
	 * @return
	 */
	private List<LoveCallBean> convertLoveCallBean(LoveCall[] itemList){
		ArrayList<LoveCallBean> list = new ArrayList<LoveCallBean>();
		if(itemList != null){
			for(int i =0; i<itemList.length; i++){
				list.add(new LoveCallBean(itemList[i]));
			}
		}
		return list;
	}
	
	@Override
	public void onPullDownToRefresh() {
		// TODO Auto-generated method stub
		super.onPullDownToRefresh();
		queryLoveCallList(LOVE_CALL_LIST_REFRESH);
	}
	
	@Override
	public void onPullUpToRefresh() {
		// TODO Auto-generated method stub
		super.onPullUpToRefresh();
		queryLoveCallList(LOVE_CALL_LIST_MORE);
	}

}
