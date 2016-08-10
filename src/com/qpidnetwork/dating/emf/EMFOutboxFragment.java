package com.qpidnetwork.dating.emf;

import java.util.Arrays;
import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.qpidnetwork.dating.R;
import com.qpidnetwork.dating.bean.PageBean;
import com.qpidnetwork.dating.home.HomeActivity;
import com.qpidnetwork.framework.base.BaseListFragment;
import com.qpidnetwork.framework.util.ToastUtil;
import com.qpidnetwork.request.OnEMFOutboxListCallback;
import com.qpidnetwork.request.RequestJniEMF.ProgressType;
import com.qpidnetwork.request.RequestJniEMF.ReplyType;
import com.qpidnetwork.request.RequestOperator;
import com.qpidnetwork.request.item.EMFOutboxListItem;


public class EMFOutboxFragment extends BaseListFragment{
	
	private static final int EMF_OUTBOX_INIT =0;
	private static final int EMF_OUTBOX_REFRESH = 1;
	private static final int EMF_OUTBOX_MORE = 2;
	
	private static final int GET_OUTBOX_LIST_SUCCESS = 3;
	private static final int GET_OUTBOX_LIST_FAILED = 4;
	
	private EMFOutboxAdapter mAdapter;
	private boolean isInited = false;//记录是否初始化成功，处理不同的加载及错误界面
	
	public static EMFOutboxFragment newInstance(){
		EMFOutboxFragment fragment = new EMFOutboxFragment();
		return fragment;
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onActivityCreated(savedInstanceState);
		mAdapter = new EMFOutboxAdapter(getActivity());
		getPullToRefreshListView().setAdapter(mAdapter);
		getFloatButton().setId(R.id.common_button_send);
		getFloatButton().setOnClickListener(this);
		queryENFOutboxList(0, EMF_OUTBOX_INIT, "", ProgressType.DEFAULT);
	}

	/**
	 * @return 设置emptyView
	 */
	private View getEmptyView() {
		// TODO Auto-generated method stub
		View view = LayoutInflater.from(mContext).inflate(R.layout.view_emf_empty, null);
		((TextView)view.findViewById(R.id.tvEmfType)).setText(R.string.emf_outbox_empty);
		view.findViewById(R.id.btnSearch).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				getActivity().sendBroadcast(new Intent(HomeActivity.REFRESH_NEWEST_LADY));
				getActivity().finish();
			}
		});
		return view;
	}
	
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		super.onClick(v);
		switch (v.getId()) {
		case R.id.common_button_send:{
			MailEditActivity.launchMailEditActivity(mContext, "", ReplyType.DEFAULT, "", "");
		}break;
		}
	}

	/**
	 * 获取EMF列表
	 * @param operation 用于记录页面操作，用于retry等记录使用
	 * @param type
	 * @param womanId
	 */
	private void queryENFOutboxList(int curCount, final int operation, String womanId, ProgressType type){
		PageBean pageBean = getPageBean();
		if(operation == EMF_OUTBOX_INIT){
			showInitLoading();
		}
		RequestOperator.getInstance().OutboxList(pageBean.getNextPager(curCount), pageBean.getPageSize(), womanId, type, new OnEMFOutboxListCallback() {
			
			@Override
			public void OnEMFOutboxList(boolean isSuccess, String errno, String errmsg,
					int pageIndex, int pageSize, int dataCount,
					EMFOutboxListItem[] listArray) {
				// TODO Auto-generated method stub
				Message msg = Message.obtain();
				msg.arg1 = operation;
				if(isSuccess){
					getPageBean().setDataCount(dataCount);
					msg.what = GET_OUTBOX_LIST_SUCCESS;
					msg.obj = Arrays.asList(listArray);
				}else{
					msg.what = GET_OUTBOX_LIST_FAILED;
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
		case GET_OUTBOX_LIST_SUCCESS:
			List<EMFOutboxListItem> list = (List<EMFOutboxListItem>) msg.obj;

			if ((msg.arg1 == EMF_OUTBOX_INIT)
					|| (msg.arg1 == EMF_OUTBOX_REFRESH)) {
				if (list != null && list.size() > 0) {// init和refresh成功加判断
					mAdapter.replaceList(list);
					if (msg.arg1 == EMF_OUTBOX_INIT) {
						isInited = true;
						hideLoadingPage();
					}
					//列表不为空，显示编辑信件按钮
					getFloatButton().setVisibility(View.VISIBLE);
				} else {
					isInited = false;// 重置init
					showInitEmpty(getEmptyView());// 显示空界面
					//列表为空时，隐藏
					getFloatButton().setVisibility(View.GONE);
				}
			}else if(msg.arg1 == EMF_OUTBOX_MORE){
				mAdapter.appendList(list);
				//列表不为空，显示编辑信件按钮
				getFloatButton().setVisibility(View.VISIBLE);
			}
			
			break;

		case GET_OUTBOX_LIST_FAILED:{
			//列表为空时，隐藏
			getFloatButton().setVisibility(View.GONE);
			if(msg.arg1 == EMF_OUTBOX_INIT){
				showInitError();
			}else{
				String errorMsg = (String)msg.obj;
				if(getActivity() != null){
					ToastUtil.showToast(getActivity(), errorMsg);
				}
			}
		}break;
		}
		
		onRefreshComplete();
		
	}
	
	/**
	 * tab 切换时刷新列表接口
	 * @param forceRefresh
	 */
	public void TabSelectRefresh(boolean forceRefresh){
		if(isInited){
			if(forceRefresh){
				queryENFOutboxList(0, EMF_OUTBOX_REFRESH, "", ProgressType.DEFAULT);
			}
		}else{
			/*未初始化成功时，每次切换都刷新*/
			queryENFOutboxList(0, EMF_OUTBOX_INIT, "", ProgressType.DEFAULT);
		}
	}
	
	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		isInited = false;
	}
	
	@Override
	public void onInitRetry() {
		// TODO Auto-generated method stub
		super.onInitRetry();
		TabSelectRefresh(false);
	}
	
	@Override
	public void onPullDownToRefresh() {
		// TODO Auto-generated method stub
		super.onPullDownToRefresh();
		queryENFOutboxList(0, EMF_OUTBOX_REFRESH, "", ProgressType.DEFAULT);
	}
	
	@Override
	public void onPullUpToRefresh() {
		// TODO Auto-generated method stub
		super.onPullUpToRefresh();
		queryENFOutboxList(mAdapter.getCount(), EMF_OUTBOX_MORE, "", ProgressType.DEFAULT);
	}
	
	
	/*邮件删除*/
	public void onEMFDelete(String emfId){
		mAdapter.removeItemById(emfId);
	}
	
	
	@Override
	public void onRefreshComplete() {
		// TODO Auto-generated method stub
		super.onRefreshComplete();
		closePullUpRefresh(mAdapter.getDataList().size() >= getPageBean().getDataCount());
	}

}
