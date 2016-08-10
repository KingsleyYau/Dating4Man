package com.qpidnetwork.dating.admirer;

import java.util.Arrays;
import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;

import com.qpidnetwork.dating.R;
import com.qpidnetwork.dating.bean.PageBean;
import com.qpidnetwork.dating.emf.MailEditActivity;
import com.qpidnetwork.dating.home.HomeActivity;
import com.qpidnetwork.framework.base.BaseListFragment;
import com.qpidnetwork.framework.util.ToastUtil;
import com.qpidnetwork.request.OnEMFAdmirerListCallback;
import com.qpidnetwork.request.RequestJniEMF;
import com.qpidnetwork.request.RequestJniEMF.ReplyType;
import com.qpidnetwork.request.RequestJniEMF.SortType;
import com.qpidnetwork.request.item.EMFAdmirerListItem;

public class AdmireListFragment extends BaseListFragment{
	
	private static final int EMF_ADMIRER_INIT =0;
	private static final int EMF_ADMIRER_REFRESH = 1;
	private static final int EMF_ADMIRER_MORE = 2;
	
	private static final int GET_ADMIRER_SUCCESS = 3;
	private static final int GET_ADMIRER_FAILED = 4;
	
	private AdmirerListAdapter mAdapter;
	private boolean isInited = false;//记录是否初始化成功，处理不同的加载及错误界面
	
	public static AdmireListFragment newInstance(){
		AdmireListFragment fragment = new AdmireListFragment();
		return fragment;
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onActivityCreated(savedInstanceState);
		mAdapter = new AdmirerListAdapter(getActivity());
		getPullToRefreshListView().setAdapter(mAdapter);
		getFloatButton().setId(R.id.common_button_send);
		getFloatButton().setOnClickListener(this);
		queryAdmirerList(0, EMF_ADMIRER_INIT, SortType.DEFAULT, "");
	}
	
	/**
	 * 获取EMF列表
	 * @param operation 用于记录页面操作，用于retry等记录使用
	 * @param type
	 * @param womanId
	 */
	private void queryAdmirerList(int curCount, final int operation, SortType type, String womanId){
		PageBean pageBean = getPageBean();
		if(operation == EMF_ADMIRER_INIT){
			showInitLoading();
		}
		RequestJniEMF.AdmirerList(pageBean.getNextPager(curCount), pageBean.getPageSize(), type, womanId, new OnEMFAdmirerListCallback() {
			
			@Override
			public void OnEMFAdmirerList(boolean isSuccess, String errno, String errmsg,
					int pageIndex, int pageSize, int dataCount,
					EMFAdmirerListItem[] listArray) {
				// TODO Auto-generated method stub
				Message msg = Message.obtain();
				msg.arg1 = operation;
				if(isSuccess){
					getPageBean().setDataCount(dataCount);
					msg.what = GET_ADMIRER_SUCCESS;
					msg.obj = Arrays.asList(listArray);
				}else{
					msg.what = GET_ADMIRER_FAILED;
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
		case GET_ADMIRER_SUCCESS:
			List<EMFAdmirerListItem> list = (List<EMFAdmirerListItem>)msg.obj;
			if((msg.arg1 == EMF_ADMIRER_INIT) || (msg.arg1 == EMF_ADMIRER_REFRESH)){
				if(list != null && list.size() > 0){//加判断
					mAdapter.replaceList(list);
					if(msg.arg1 == EMF_ADMIRER_INIT){
						isInited = true;
						hideLoadingPage();
					}
					//列表不为空，显示编辑信件按钮
					getFloatButton().setVisibility(View.VISIBLE);
				}else{
					isInited = false;
					showInitEmpty(getEmptyView());// 显示空界面
					//除非列表不为空，否则都隐藏
					getFloatButton().setVisibility(View.GONE);
				}
			}else if(msg.arg1 == EMF_ADMIRER_MORE){
				//列表不为空，显示编辑信件按钮
				getFloatButton().setVisibility(View.VISIBLE);
				mAdapter.appendList(list);
			}
			
			break;

		case GET_ADMIRER_FAILED:{
			//除非列表不为空，否则都隐藏
			getFloatButton().setVisibility(View.GONE);
			if(msg.arg1 == EMF_ADMIRER_INIT){
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
	 * @return 设置emptyView
	 */
	private View getEmptyView() {
		// TODO Auto-generated method stub
		View view  = LayoutInflater.from(getActivity()).inflate(R.layout.view_admirer_mail_empty, null);
		view.findViewById(R.id.btnSearch).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				getActivity().sendBroadcast(new Intent(HomeActivity.REFRESH_ONLINE_LADY));
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
	 * tab 切换时刷新列表接口
	 * @param forceRefresh
	 */
	public void TabSelectRefresh(boolean forceRefresh){
		if(isInited){
			if(forceRefresh){
				queryAdmirerList(0, EMF_ADMIRER_REFRESH, SortType.DEFAULT, "");
			}
		}else{
			/*未初始化成功时，每次切换都刷新*/
			queryAdmirerList(0, EMF_ADMIRER_INIT, SortType.DEFAULT, "");
		}
	}
	
	@Override
	public void onInitRetry() {
		// TODO Auto-generated method stub
		super.onInitRetry();
		TabSelectRefresh(false);
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
		queryAdmirerList(0, EMF_ADMIRER_REFRESH, SortType.DEFAULT, "");
	}
	
	@Override
	public void onPullUpToRefresh() {
		// TODO Auto-generated method stub
		super.onPullUpToRefresh();
		queryAdmirerList(mAdapter.getCount(), EMF_ADMIRER_MORE, SortType.DEFAULT, "");
	}
	
	
	/*邮件删除*/
	public void onEMFDelete(String emfId){
		mAdapter.removeItemById(emfId);
	}
	
	/*邮件未读转已读*/
	public void onEMFRead(String emfId){
		List<EMFAdmirerListItem>dataList = mAdapter.getDataList();
		for(int i = 0; i< dataList.size(); i++){
			if((dataList.get(i).id.equals(emfId)) && (!dataList.get(i).readflag)){
				dataList.get(i).readflag = true;
				mAdapter.notifyDataSetChanged();
				
			}
		}
	}
	
	@Override
	public void onRefreshComplete() {
		// TODO Auto-generated method stub
		super.onRefreshComplete();
		closePullUpRefresh(mAdapter.getDataList().size() >= getPageBean().getDataCount());
	}

}
