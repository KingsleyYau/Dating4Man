package com.qpidnetwork.dating.admirer;

import java.util.Arrays;
import java.util.List;

import android.os.Bundle;
import android.os.Message;

import com.qpidnetwork.dating.bean.PageBean;
import com.qpidnetwork.framework.base.BaseListFragment;
import com.qpidnetwork.framework.util.ToastUtil;
import com.qpidnetwork.request.OnEMFAdmirerListCallback;
import com.qpidnetwork.request.RequestJniEMF;
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
				mAdapter.replaceList(list);
				if(msg.arg1 == EMF_ADMIRER_INIT){
					isInited = true;
					hideLoadingPage();
				}
			}else if(msg.arg1 == EMF_ADMIRER_MORE){
				mAdapter.appendList(list);
			}
			
			break;

		case GET_ADMIRER_FAILED:
			if(msg.arg1 == EMF_ADMIRER_INIT){
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
		if(mAdapter.getDataList().size() >= getPageBean().getDataCount()){
			closePullUpRefresh();
		}
	}

}
