package com.qpidnetwork.dating.emf;

import java.util.Arrays;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.qpidnetwork.dating.R;
import com.qpidnetwork.dating.admirer.AdmirerListAdapter;
import com.qpidnetwork.dating.bean.PageBean;
import com.qpidnetwork.framework.base.BaseFragmentActivity;
import com.qpidnetwork.framework.base.UpdateableAdapter;
import com.qpidnetwork.framework.util.ToastUtil;
import com.qpidnetwork.manager.WebSiteManager;
import com.qpidnetwork.request.OnEMFAdmirerListCallback;
import com.qpidnetwork.request.OnEMFInboxListCallback;
import com.qpidnetwork.request.OnEMFOutboxListCallback;
import com.qpidnetwork.request.RequestJniEMF.ProgressType;
import com.qpidnetwork.request.RequestJniEMF.SortType;
import com.qpidnetwork.request.RequestOperator;
import com.qpidnetwork.request.item.EMFAdmirerListItem;
import com.qpidnetwork.request.item.EMFInboxListItem;
import com.qpidnetwork.request.item.EMFOutboxListItem;
import com.qpidnetwork.view.MartinListView;
import com.qpidnetwork.view.MartinListView.OnPullRefreshListener;
import com.qpidnetwork.view.MaterialProgressBar;

public class EMFSearchActivity extends BaseFragmentActivity implements OnClickListener, OnPullRefreshListener{
	
	private static final String EMF_TYPE = "emfType";
	private static final int SEARCH_EMF_SUCCESS = 0;
	private static final int SEARCH_EMF_FAILED = 1;
	
	private static final int EMF_SEARCH_REFRESH = 1;
	private static final int EMF_SEARCH_MORE = 2;
	
	public ImageView ivCancle;
	public EditText etSearchFilter;
	
	
	private MartinListView mMLV;
	private MaterialProgressBar pbLoading;
	
	private int type = 0;//邮件类型（0.收件箱  1.发件箱  2.意向信收件箱 ）
	private BaseAdapter  adapter;
	
	
	private PageBean pageBean = new PageBean(MartinListView.PAGE_SIZE);
	
	public static Intent getIntent(Context context, int type){
		Intent intent = new Intent(context, EMFSearchActivity.class);
		intent.putExtra(EMF_TYPE, type);
		return intent;
	}
	
	@Override
	protected void onCreate(Bundle arg0) {
		// TODO Auto-generated method stub
		super.onCreate(arg0);
		this.getWindow().setBackgroundDrawable(new ColorDrawable(getResources().getColor(WebSiteManager.getInstance().GetWebSite().getSiteColor())));
		
		setContentView(R.layout.activity_emf_search);
		
		pageBean.resetPageIndex();
		
		Bundle bundle = getIntent().getExtras();
		if(bundle != null){
			if(bundle.containsKey(EMF_TYPE)){
				type = bundle.getInt(EMF_TYPE);
			}
		}
		
		initViews();
	}
	
	private void initViews(){
		ivCancle = (ImageView)findViewById(R.id.ivCancle);
		etSearchFilter = (EditText)findViewById(R.id.etSearchFilter);
		
		mMLV = (MartinListView)findViewById(R.id.martinListView);
		mMLV.setCanPullDown(false);//关闭下拉刷新
		mMLV.setEmptyMessage("");//初始化显示为空
		
		pbLoading = (MaterialProgressBar)findViewById(R.id.pbTopLoading);
		pbLoading.setBarColor(getResources().getColor(R.color.blue_color));
		pbLoading.spin();
		pbLoading.setVisibility(View.GONE);

		ivCancle.setOnClickListener(this);
		
		/*设置键盘搜索键响应*/
		etSearchFilter.setOnEditorActionListener(new OnEditorActionListener() {
			
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				// TODO Auto-generated method stub
				if(actionId == EditorInfo.IME_ACTION_SEARCH){
					onSearch(EMF_SEARCH_REFRESH);
					return true;
				}
				return false;
			}
		});
		
		switch (type) {
		case 0:
			adapter = new EMFInboxAdapter(this);
			break;

		case 1:
			adapter = new EMFOutboxAdapter(this);
			break;
		case 2:
			adapter = new AdmirerListAdapter(this);
			break;
		}

		mMLV.setAdapter(adapter);
		etSearchFilter.requestFocus();
		
		LinearLayout includeSearch = (LinearLayout) findViewById(R.id.includeSearch);
		includeSearch.setBackgroundColor(getResources().getColor(WebSiteManager.getInstance().GetWebSite().getSiteColor()));
		//showSoftInput();
	}
	
	private void showLoading(){
		mMLV.setVisibility(View.GONE);
		pbLoading.setVisibility(View.VISIBLE);
	}
	
	private void hideLoading(){
		mMLV.setVisibility(View.VISIBLE);
		pbLoading.setVisibility(View.GONE);
	}
	
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		//super.onClick(v);
		switch (v.getId()) {
		case R.id.ivCancle:
			finish();
			break;
		}
	}
	
	@Override
	protected void handleUiMessage(Message msg) {
		// TODO Auto-generated method stub
		super.handleUiMessage(msg);
		switch (msg.what){	
		case SEARCH_EMF_SUCCESS:
			onSearchSuccess(msg);
			break;

		case SEARCH_EMF_FAILED:
			String errorMsg = (String)msg.obj;
			ToastUtil.showToast(EMFSearchActivity.this, errorMsg);
			break;
		}
		onRefreshFinish();
	}

	
	private void onSearch(int operate){
		String filter = etSearchFilter.getText().toString();
		if((filter == null)||(filter.length() < 4)){
			shakeView(etSearchFilter, true);
		}else{
			if(pageBean.getPageIndex() == 0){
				//第一次为搜索，其余为更多
				if (adapter == null || adapter.getCount() == 0) 
					showLoading();
			}	
			switch (type) {
			case 0:
				onEMFInboxSearch(filter, operate);
				break;
			case 1:
				onEMFOutboxSearch(filter, operate);
				break;
			case 2:
				onAdmirerSearch(filter, operate);
				break;
			}
		}
	}
	
	/**
	 * 收件箱条件搜索(womanId)
	 * @param filter 过滤条件
	 */
	private void onEMFInboxSearch(String filter, final int operate){
		
		RequestOperator.getInstance().InboxList(pageBean.getNextPager(((EMFInboxAdapter)adapter).getDataList().size()), pageBean.getPageSize(), SortType.DEFAULT, filter, new OnEMFInboxListCallback() {
			
			@Override
			public void OnEMFInboxList(boolean isSuccess, String errno, String errmsg,
					int pageIndex, int pageSize, int dataCount,
					EMFInboxListItem[] listArray) {
				// TODO Auto-generated method stub
				Message msg = Message.obtain();
				if(isSuccess){
					pageBean.setDataCount(dataCount);
					msg.what = SEARCH_EMF_SUCCESS;
					msg.arg1 = operate;
					msg.obj = Arrays.asList(listArray);
				}else{
					//请求失败，页数减1
					pageBean.decreasePageIndex();
					msg.what = SEARCH_EMF_FAILED;
					msg.obj = errmsg;
				}
				sendUiMessage(msg);
			}
		});
	}
	
	/**
	 * 发件箱条件搜索(womanId)
	 * @param filter 过滤条件
	 */
	private void onEMFOutboxSearch(String filter, final int operate){
		RequestOperator.getInstance().OutboxList(pageBean.getNextPager(((EMFOutboxAdapter)adapter).getDataList().size()), pageBean.getPageSize(), filter, ProgressType.DEFAULT, new OnEMFOutboxListCallback() {
			
			@Override
			public void OnEMFOutboxList(boolean isSuccess, String errno, String errmsg,
					int pageIndex, int pageSize, int dataCount,
					EMFOutboxListItem[] listArray) {
				// TODO Auto-generated method stub
				Message msg = Message.obtain();
				if(isSuccess){
					pageBean.setDataCount(dataCount);
					msg.what = SEARCH_EMF_SUCCESS;
					msg.arg1 = operate;
					msg.obj = Arrays.asList(listArray);
				}else{
					//请求失败，页数减1
					pageBean.decreasePageIndex();
					msg.what = SEARCH_EMF_FAILED;
					msg.obj = errmsg;
				}
				sendUiMessage(msg);
			}
		});
	}
	
	/**
	 * 意向信条件搜索(womanId)
	 * @param filter 过滤条件
	 */
	private void onAdmirerSearch(String filter, final int operate){
		RequestOperator.getInstance().AdmirerList(pageBean.getNextPager(((AdmirerListAdapter)adapter).getDataList().size()), pageBean.getPageSize(), SortType.DEFAULT, filter, new OnEMFAdmirerListCallback() {
			
			@Override
			public void OnEMFAdmirerList(boolean isSuccess, String errno, String errmsg,
					int pageIndex, int pageSize, int dataCount,
					EMFAdmirerListItem[] listArray) {
				// TODO Auto-generated method stub
				Message msg = Message.obtain();
				if(isSuccess){
					pageBean.setDataCount(dataCount);
					msg.what = SEARCH_EMF_SUCCESS;
					msg.arg1 = operate;
					msg.obj = Arrays.asList(listArray);
				}else{
					//请求失败，页数减1
					pageBean.decreasePageIndex();
					msg.what = SEARCH_EMF_FAILED;
					msg.obj = errmsg;
				}
				sendUiMessage(msg);
			}
		});
	}
	
	/**
	 * 搜索成功统一返回处理
	 * @param obj 数据对象
	 */
	@SuppressWarnings("unchecked")
	private void onSearchSuccess(Message msg){
		String filter = etSearchFilter.getText().toString();
		filter = String.format(getString(R.string.common_no_result), filter);
		mMLV.setEmptyMessage(filter);
		switch (type) {
		case 0:
			
			List<EMFInboxListItem> inboxList = (List<EMFInboxListItem>)msg.obj;
			if (inboxList.size() > 0){
				hideSoftInput();
			}
			
			if(msg.arg1 == EMF_SEARCH_REFRESH){
				((EMFInboxAdapter)adapter).replaceList(inboxList);
			}else if(msg.arg1 == EMF_SEARCH_MORE){
				((EMFInboxAdapter)adapter).appendList(inboxList);
			}
			break;
		case 1:
			List<EMFOutboxListItem> outboxList = (List<EMFOutboxListItem>)msg.obj;
			if (outboxList.size() > 0){
				hideSoftInput();
			}
			
			if(msg.arg1 == EMF_SEARCH_REFRESH){
				((EMFOutboxAdapter)adapter).replaceList(outboxList);
			}else if(msg.arg1 == EMF_SEARCH_MORE){
				((EMFOutboxAdapter)adapter).appendList(outboxList);
			}
			break;
		case 2:
			List<EMFAdmirerListItem> admireList = (List<EMFAdmirerListItem>)msg.obj;
			if (admireList.size() > 0){
				hideSoftInput();
			}
			if(msg.arg1 == EMF_SEARCH_REFRESH){
				((AdmirerListAdapter)adapter).replaceList(admireList);
			}else if(msg.arg1 == EMF_SEARCH_MORE){
				((AdmirerListAdapter)adapter).appendList(admireList);
			}
			break;
		}
	}
	
	@SuppressWarnings("rawtypes")
	private void onRefreshFinish(){
		hideLoading();
		mMLV.onRefreshComplete();
		UpdateableAdapter updateableAdapter = (UpdateableAdapter)adapter;
		mMLV.setCanPullUp((updateableAdapter).getDataList().size() >= pageBean.getDataCount());
	}
	
	/*进入详情删除操作*/
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent arg2) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, arg2);
		if(resultCode == RESULT_OK){
			if(requestCode == EMFDetailActivity.REQUEST_CODE){
				if((arg2 != null)&&(arg2.getExtras().containsKey(EMFDetailActivity.EMF_MESSAGEID))){
					String emfId = arg2.getExtras().getString(EMFDetailActivity.EMF_MESSAGEID);
					if(emfId != null){
						Intent intent = new Intent();
						intent.putExtra(EMFDetailActivity.EMF_MESSAGEID, emfId);
						setResult(RESULT_OK, intent);
						finish();
					}
				}
			}
		}
		
	}
	

	@Override
	public void onPullDownToRefresh() {
		// TODO Auto-generated method stub
		onSearch(EMF_SEARCH_REFRESH);
	}

	@Override
	public void onPullUpToRefresh() {
		// TODO Auto-generated method stub
		onSearch(EMF_SEARCH_MORE);
	}
	
	@Override
	public void finish(){
		super.finish();
		overridePendingTransition(R.anim.anim_donot_animate, R.anim.anim_donot_animate);   
	}
	
}	
