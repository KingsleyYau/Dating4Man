package com.qpidnetwork.dating.emf;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.os.Bundle;
import android.os.Message;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;

import com.qpidnetwork.dating.R;
import com.qpidnetwork.dating.bean.PageBean;
import com.qpidnetwork.dating.lady.LadyDetailActivity;
import com.qpidnetwork.framework.base.BaseFragmentActivity;
import com.qpidnetwork.framework.base.BaseListFragment;
import com.qpidnetwork.framework.util.ToastUtil;
import com.qpidnetwork.request.OnEMFBlockListCallback;
import com.qpidnetwork.request.OnEMFUnblockCallback;
import com.qpidnetwork.request.RequestOperator;
import com.qpidnetwork.request.item.EMFBlockListItem;
import com.qpidnetwork.view.MaterialDialogSingleChoice;

public class EMFBlockListFragment extends BaseListFragment
								  implements OnItemClickListener,
								  			 OnItemLongClickListener
{

	
	private static final int EMF_BLOCKLIST_INIT =0;
	private static final int EMF_BLOCKLIST_REFRESH = 1;
	private static final int EMF_BLOCKLIST_MORE = 2;
	
	private static final int GET_BLOCK_LIST_SUCCESS = 3;
	private static final int GET_BLOCK_LIST_FAILED = 4;
	private static final int UNBLOCK_LIST_SUCCESS = 5;
	private static final int UNBLOCK_LIST_FAILED = 6;
			
	EMFBlockedListAdapter mAdapter;
	List<EMFBlockListItem> people = new ArrayList<EMFBlockListItem>();
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		
		// TODO Auto-generated method stub
		super.onActivityCreated(savedInstanceState);
		
		mAdapter = new EMFBlockedListAdapter(getActivity(), people);
		getPullToRefreshListView().setAdapter(mAdapter);
		queryEMFBlockedList(0, EMF_BLOCKLIST_INIT, "");
		getPullToRefreshListView().setOnItemClickListener(this);
		getPullToRefreshListView().setOnItemLongClickListener(this);
	}
	
	/**
	 * 获取黑名单列表
	 * @param begin
	 */
	private void queryEMFBlockedList(int curCount, final int operation, String womanId){
		PageBean pageBean = getPageBean();
		if(operation == EMF_BLOCKLIST_INIT){
			showInitLoading();
		}
		RequestOperator.getInstance().BlockList(pageBean.getNextPager(curCount), pageBean.getPageSize(), womanId, new OnEMFBlockListCallback() {
			
			@Override
			public void OnEMFBlockList(boolean isSuccess, String errno, String errmsg,
					int pageIndex, int pageSize, int dataCount,
					EMFBlockListItem[] listArray) {
				Message msg = Message.obtain();
				msg.arg1 = operation;
				if(isSuccess){
					getPageBean().setDataCount(dataCount);
					msg.what = GET_BLOCK_LIST_SUCCESS;
					msg.obj = Arrays.asList(listArray);
				}else{
					msg.what = GET_BLOCK_LIST_FAILED;
					msg.obj = errmsg;
				}
				sendUiMessage(msg);
			}
		});
	}
	
	/**
	 * 移除黑名单
	 * @param womanid
	 */
	private void unBlock(final String[] womanid){
		RequestOperator.getInstance().Unblock(womanid, new OnEMFUnblockCallback() {
			
			@Override
			public void OnEMFUnblock(boolean isSuccess, String errno, String errmsg) {
				/*移除黑名单*/
				Message msg = Message.obtain();
				if(isSuccess){
					msg.what = UNBLOCK_LIST_SUCCESS;
					msg.obj = womanid;
				}else{
					msg.what = UNBLOCK_LIST_FAILED;
					msg.obj = errmsg;
				}
				sendUiMessage(msg);
			}
		});
	}
	
	
	@SuppressWarnings("unchecked")
	@Override
	protected void handleUiMessage(Message msg) {
		super.handleUiMessage(msg);
		switch (msg.what) {
		case GET_BLOCK_LIST_SUCCESS:
			List<EMFBlockListItem> list = (List<EMFBlockListItem>)msg.obj;
			if((msg.arg1 == EMF_BLOCKLIST_INIT) || (msg.arg1 == EMF_BLOCKLIST_REFRESH)){
				people.clear();
				people.addAll(list);
				mAdapter.notifyDataSetChanged();
				if(msg.arg1 == EMF_BLOCKLIST_INIT){
					hideLoadingPage();
				}
			}else if(msg.arg1 == EMF_BLOCKLIST_MORE){
				/**
				 * 加入排重操作
				 */
				for(int i=0; i<people.size();i++){
					if(!this.people.contains(list.get(i))){
						this.people.add(list.get(i));
					}
				}
				mAdapter.notifyDataSetChanged();
			}
			onRefreshComplete();
			break;

		case GET_BLOCK_LIST_FAILED:
			if(msg.arg1 == EMF_BLOCKLIST_INIT){
				showInitError();
			}else{
				String errorMsg = (String)msg.obj;
				if(getActivity() != null){
					ToastUtil.showToast(getActivity(), errorMsg);
				}
			}
			onRefreshComplete();
			break;
		case UNBLOCK_LIST_SUCCESS:
			/*移除黑名单成功*/
			if(getActivity() != null){
				((BaseFragmentActivity)getActivity()).showToastDone("Done!");
			}
			String[] womanIds = (String[])msg.obj;
			mAdapter.removeItems(womanIds);
			break;
		case UNBLOCK_LIST_FAILED:
			((BaseFragmentActivity)getActivity()).showToastFailed("Failed!");
			break;
		}
		
	}
	
	
	
	@Override
	public void onInitRetry() {
		// TODO Auto-generated method stub
		super.onInitRetry();
		queryEMFBlockedList(0, EMF_BLOCKLIST_INIT, "");

	}
	
	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		((BaseFragmentActivity)getActivity()).cancelToastImmediately();
		super.onDestroy();
	}
	
	@Override
	public void onPullDownToRefresh() {
		// TODO Auto-generated method stub
		super.onPullDownToRefresh();
		queryEMFBlockedList(0, EMF_BLOCKLIST_REFRESH, "");
	}
	
	@Override
	public void onPullUpToRefresh() {
		// TODO Auto-generated method stub
		super.onPullUpToRefresh();
		queryEMFBlockedList(people.size(), EMF_BLOCKLIST_MORE, "");
	}
	
	
	/*邮件删除*/
	public void onEMFDelete(int which){
		mAdapter.removeItem(which);
	}

	
	@Override
	public void onRefreshComplete() {
		// TODO Auto-generated method stub
		super.onRefreshComplete();
		closePullUpRefresh(mAdapter.getDataList().size() >= getPageBean().getDataCount());
	}

	@Override
	/**
	* OnItemClickListener
	*/
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		// TODO Auto-generated method stub
		LadyDetailActivity.launchLadyDetailActivity(getActivity(), mAdapter.getDataList().get(arg2).womanid, true);
	}

	@Override
	/**
	* OnItemLongClickListener callback
	*/
	public boolean onItemLongClick(AdapterView<?> arg0, View arg1, final int arg2,
			long arg3) 
	{
		// TODO Auto-generated method stub
		MaterialDialogSingleChoice dialog = new MaterialDialogSingleChoice(getActivity(), 
				new String[]{getString(R.string.common_btn_delete), getString(R.string.view_profile), getString(R.string.common_btn_cancel)},
				new MaterialDialogSingleChoice.OnClickCallback() {
					
					@Override
					public void onClick(AdapterView<?> adptView, View v, int which) {
						// TODO Auto-generated method stub
						if(which == 0){
							((BaseFragmentActivity)getActivity()).showToastProgressing("Deleting");
							String[] womanid = new String[1];
							womanid[0] = mAdapter.getDataList().get(arg2).womanid;
							unBlock(womanid);
						}else if (which == 1){
							LadyDetailActivity.launchLadyDetailActivity(getActivity(), mAdapter.getDataList().get(arg2).womanid, true);
						}
					}
				}
				, -1
			);
		
		dialog.show();
		return true;
	}

}
