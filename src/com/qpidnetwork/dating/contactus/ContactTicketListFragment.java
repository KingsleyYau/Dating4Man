package com.qpidnetwork.dating.contactus;

import java.util.Arrays;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Message;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

import com.qpidnetwork.dating.R;
import com.qpidnetwork.dating.bean.PageBean;
import com.qpidnetwork.framework.base.BaseListFragment;
import com.qpidnetwork.framework.util.ToastUtil;
import com.qpidnetwork.framework.util.UnitConversion;
import com.qpidnetwork.request.OnTicketListCallback;
import com.qpidnetwork.request.RequestOperator;
import com.qpidnetwork.request.item.TicketListItem;
import com.qpidnetwork.request.item.TicketListItem.StatusType;

public class ContactTicketListFragment extends BaseListFragment implements OnItemClickListener{

	private static final int TICKET_LIST_INIT = 0;
	private static final int TICKET_LIST_REFRESH = 1;
	private static final int TICKET_LIST_MORE = 2;

	private static final int GET_TICKET_LIST_SUCCESS = 3;
	private static final int GET_TICKET_LIST_FAILED = 4;
	
	private static final int RESULT_REOLVE_NOTIFY = 5;

	private ContactTicketListAdapter mAdapter;
	
	private boolean isInited = false;//记录是否初始化成功，处理不同的加载及错误界面

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onActivityCreated(savedInstanceState);
		mAdapter = new ContactTicketListAdapter(getActivity());

		getPullToRefreshListView().setSelector(
				new ColorDrawable(Color.TRANSPARENT));
		getPullToRefreshListView().setDivider(
				new ColorDrawable(getResources().getColor(
						R.color.listview_divider_grey)));
		getPullToRefreshListView().setDividerHeight(
				UnitConversion.dip2px(getActivity(), 1));
		getPullToRefreshListView().setAdapter(mAdapter);

		hideLoadingPage();

		getPullToRefreshListView().setOnItemClickListener(this);
		
		queryTicketList(0, TICKET_LIST_INIT);
	}
	
	

	/**
	 * 获取Ticket list
	 * 
	 * @param curCount
	 * @param operation
	 */
	private void queryTicketList(int curCount, final int operation){
		PageBean pageBean = getPageBean();
		if(operation == TICKET_LIST_INIT){
			showInitLoading();
		}
		
		RequestOperator.newInstance(getActivity()).TicketList(pageBean.getNextPager(curCount), pageBean.getPageSize(), new OnTicketListCallback() {
			
			@Override
			public void OnTicketList(boolean isSuccess, String errno, String errmsg,
					int pageIndex, int pageSize, int dataCount, TicketListItem[] list) {
				Message msg = Message.obtain();
				msg.arg1 = operation;
				if(isSuccess){
					getPageBean().setDataCount(dataCount);
					msg.what = GET_TICKET_LIST_SUCCESS;
					msg.obj = Arrays.asList(list);
				}else{
					msg.what = GET_TICKET_LIST_FAILED;
					msg.obj = errmsg;
				}
				sendUiMessage(msg);
			}
		});
	}
	
	@SuppressWarnings("unchecked")
	@Override
	protected void handleUiMessage(Message msg) {
		// TODO Auto-generated method stub
		super.handleUiMessage(msg);
		switch (msg.what) {
		case GET_TICKET_LIST_SUCCESS:
			List<TicketListItem> list = (List<TicketListItem>)msg.obj;
			if((msg.arg1 == TICKET_LIST_INIT) || (msg.arg1 == TICKET_LIST_REFRESH)){
				mAdapter.replaceList(list);
				if(msg.arg1 == TICKET_LIST_INIT){
					isInited = true;
					hideLoadingPage();
				}
			}else if(msg.arg1 == TICKET_LIST_MORE){
				mAdapter.appendList(list);
			}
			
			break;

		case GET_TICKET_LIST_FAILED:
			if(msg.arg1 == TICKET_LIST_INIT){
				showInitError();
			}else{
				String errorMsg = (String)msg.obj;
				if(getActivity() != null){
					ToastUtil.showToast(getActivity(), errorMsg);
				}
			}
			break;
		}
		
		onRefreshComplete();
	}
	
	@Override
	public void onInitRetry() {
		// TODO Auto-generated method stub
		super.onInitRetry();
		queryTicketList(0, TICKET_LIST_INIT);
	}
	
	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		isInited = false;
	}
	
	@Override
	public void onPullDownToRefresh() {
		// TODO Auto-generated method stub
		super.onPullDownToRefresh();
		queryTicketList(0, TICKET_LIST_REFRESH);
	}
	
	@Override
	public void onPullUpToRefresh() {
		// TODO Auto-generated method stub
		super.onPullUpToRefresh();
		queryTicketList(0, TICKET_LIST_MORE);
	}
	
	@Override
	public void onRefreshComplete() {
		// TODO Auto-generated method stub
		super.onRefreshComplete();
		closePullUpRefresh(mAdapter.getDataList().size() >= getPageBean().getDataCount());
	}
	
	/*创建新的Ticket成功刷新列表*/
	public void onNewCreateUpdate(){
		if(isInited){
			queryTicketList(0, TICKET_LIST_REFRESH);
		}else{
			/*未初始化成功时，每次切换都刷新*/
			queryTicketList(0, TICKET_LIST_INIT);
		}
	}
	
	/**
	 * 解决返回处理
	 */
	public void onResolveUpdate(String ticketId){
		int position = -1; //无效位置
		List<TicketListItem> dataList = mAdapter.getDataList();
		for(int i=0; i<dataList.size(); i++){
			if(dataList.get(i).ticketId.equals(ticketId)){
				dataList.get(i).status = StatusType.UserClose;
				position = i;
				break;
			}
		}
		/*更新单个Item*/
		if(position != -1){
			View childAt = getPullToRefreshListView().getChildAt(position - getPullToRefreshListView().getFirstVisiblePosition());
	        if(childAt != null){
	        	childAt.findViewById(R.id.ivProcessDone).setVisibility(View.VISIBLE);
	        	childAt.findViewById(R.id.tvUnread).setVisibility(View.GONE);
	        }
		}
	}
	
	/*
	 * 获取详情成功，更新列表未读数目
	 */
	public void onReceiveReadedNotify(String ticketId){
		int position = -1; //无效位置
		List<TicketListItem> dataList = mAdapter.getDataList();
		for(int i=0; i<dataList.size(); i++){
			if(dataList.get(i).ticketId.equals(ticketId)){
				dataList.get(i).status = StatusType.UserClose;
				position = i;
				break;
			}
		}
		/*更新单个Item*/
		if(position != -1){
			View childAt = getPullToRefreshListView().getChildAt(position - getPullToRefreshListView().getFirstVisiblePosition());
	        if(childAt != null){
	        	childAt.findViewById(R.id.tvUnread).setVisibility(View.GONE);
	        }
		}
	}
	
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if(resultCode == Activity.RESULT_OK){
			switch (requestCode) {
			case RESULT_REOLVE_NOTIFY:
				/*已解决，更新指定Item*/
				if(data != null){
					Bundle bundle = data.getExtras();
					if(bundle != null && bundle.containsKey(TicketDetailListActivity.TICKET_ID)){
						onResolveUpdate(bundle.getString(TicketDetailListActivity.TICKET_ID));
					}
				}
				break;

			default:
				break;
			}
		}
	}



	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		Intent intent = new Intent(getActivity(),
				TicketDetailListActivity.class);
		intent.putExtra(TicketDetailListActivity.TICKET_ID, mAdapter.getDataList().get(position).ticketId);
		startActivityForResult(intent, RESULT_REOLVE_NOTIFY);		
	}
	
}
