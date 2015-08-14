package com.qpidnetwork.dating.contactus;

import java.util.Arrays;
import java.util.Comparator;

import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.qpidnetwork.dating.R;
import com.qpidnetwork.dating.bean.RequestFailBean;
import com.qpidnetwork.dating.contactus.TicketDetailAdapter.OnResolveClickListener;
import com.qpidnetwork.framework.base.BaseActionBarFragmentActivity;
import com.qpidnetwork.framework.util.StringUtil;
import com.qpidnetwork.framework.util.ToastUtil;
import com.qpidnetwork.request.OnRequestCallback;
import com.qpidnetwork.request.OnTicketDetailCallback;
import com.qpidnetwork.request.RequestOperator;
import com.qpidnetwork.request.item.TicketContentItem;
import com.qpidnetwork.request.item.TicketDetailItem;
import com.qpidnetwork.request.item.TicketListItem.StatusType;
import com.qpidnetwork.view.MaterialAppBar;
import com.qpidnetwork.view.MaterialDialogAlert;
import com.qpidnetwork.view.MaterialDropDownMenu.OnClickCallback;

public class TicketDetailListActivity extends BaseActionBarFragmentActivity{
	
	public static final String TICKET_ID = "ticket_id";
	public static final String TICKET_TITLE = "ticket_title";
	
	public static final int RESULT_REPLY_SUCCESS = 1;
	
	private static final int GET_TICKET_DETAIL_SUCCESS = 0;
	private static final int GET_TICKET_DETAIL_FAILED = 1;
	private static final int TICKET_RESOLVE_SUCCESS = 2;
	private static final int TICKET_RESOLVE_FAILED = 3;
	
	private ListView lvContent;
	private TicketDetailAdapter mAdapter;
	private String ticketId = "";
	private TicketDetailItem ticketDetailItem = null;
	private View headerResolve;
	
	/*empty view*/
	private TextView emptyView;
	
	private boolean isReplyBtnVisible = false;
		
	@Override
	protected void onCreate(Bundle arg0) {
		// TODO Auto-generated method stub
		super.onCreate(arg0);
		setCustomContentView(R.layout.activity_ticket_detail);
		
		Bundle bundle = getIntent().getExtras();
		if(bundle != null && bundle.containsKey(TICKET_ID)){
			ticketId = bundle.getString(TICKET_ID);
		}
		
		initTitle();
		initViews();
		if(StringUtil.isEmpty(ticketId)){
			/*ticketId 不能为空*/
			finish();
		}else{
			queryTicketDetail(ticketId);
		}
	}
	
	private void initTitle(){
		
		getCustomActionBar().setAppbarBackgroundColor(getResources().getColor(R.color.theme_actionbar_secoundary));
		getCustomActionBar().setTouchFeedback(MaterialAppBar.TOUCH_FEEDBACK_HOLO_LIGHT);
		
		getCustomActionBar().addButtonToLeft(R.id.common_button_back, "back", R.drawable.ic_close_grey600_24dp);
		
	}
	
	private void initViews(){
		lvContent = (ListView)findViewById(R.id.lvContent);
		emptyView = (TextView)findViewById(R.id.emptyView);
		lvContent.setEmptyView(emptyView);
		headerResolve = LayoutInflater.from(this).inflate(R.layout.header_ticket_detail_list, null);
		headerResolve.setVisibility(View.GONE);
		
		mAdapter = new TicketDetailAdapter(this, ticketDetailItem);
		mAdapter.setOnResolveClickListener(new OnResolveClickListener() {
			
			@Override
			public void onResolveClick() {
				resolveTicket(ticketId);
			}
		});
		
		lvContent.addHeaderView(headerResolve);
		lvContent.setAdapter(mAdapter);
		
	}
	
	
	@Override public void onClick(View v){
		super.onClick(v);
		switch(v.getId()){
		case R.id.llResolveButton:
			MaterialDialogAlert dialog = new MaterialDialogAlert(TicketDetailListActivity.this);
			dialog.setMessage(getString(R.string.this_tecket_will_be_closed));
			dialog.addButton(dialog.createButton(getString(R.string.common_btn_ok), this, R.id.common_button_ok));
			dialog.show();
			break;
		case R.id.common_button_ok:
			resolveTicket(ticketId);
			break;
		default:
			break;
		}
	}
	/**
	 * 获取详情成功数据刷新
	 */
	private void onTicketDetailSuccess(){
		if(ticketDetailItem != null){
			getCustomActionBar().setTitle(ticketDetailItem.title, getResources().getColor(R.color.text_color_dark));
			
			headerResolve.setVisibility(View.VISIBLE);
			if (ticketDetailItem.status == StatusType.Open){
				headerResolve.findViewById(R.id.llResolveButton).setOnClickListener(this);
				((TextView)headerResolve.findViewById(R.id.textTicketStatus)).setText(R.string.set_as_resolved);
			}else{
				headerResolve.findViewById(R.id.llResolveButton).setOnClickListener(null);
				headerResolve.findViewById(R.id.llResolveButton).setEnabled(false);
				((TextView)headerResolve.findViewById(R.id.textTicketStatus)).setText(R.string.this_ticket_is_resolved);
			}
			
			if(!isReplyBtnVisible){
				//防止按钮重复添加
				isReplyBtnVisible = true;
				if(ticketDetailItem.status == StatusType.Open){
					getCustomActionBar().addOverflowButton(new String[]{getString(R.string.add_reply)}, new OnClickCallback() {
						
						@Override
						public void onClick(AdapterView<?> adptView, View v, int which) {
							Intent intent = new Intent(TicketDetailListActivity.this, TicketReplyActivity.class);
							intent.putExtra(TICKET_ID, ticketId);
							intent.putExtra(TICKET_TITLE, ticketDetailItem.title);
							startActivityForResult(intent, RESULT_REPLY_SUCCESS);
						}
					}, R.drawable.ic_more_vert_grey600_24dp);
					
					
					
				}else{
					
				}
			}
			mAdapter.updateData(ticketDetailItem);
			mAdapter.notifyDataSetChanged();
			
			/*通知列表更新未读数目*/
			Intent intent = new Intent(ContactTicketListActivity.ACTION_GET_DETAIL_SUCCESS);
			intent.putExtra(TICKET_ID, ticketId);
			sendBroadcast(intent);
		}else{
			headerResolve.setVisibility(View.GONE);
		}
	}
	
	/**
	 * 获取反馈来往详情
	 * @param ticketId
	 */
	private void queryTicketDetail(String ticketId){
		showProgressDialog(getString(R.string.common_loading_tips));
		RequestOperator.newInstance(this).TicketDetail(ticketId, new OnTicketDetailCallback() {
			
			@Override
			public void OnTicketDetail(boolean isSuccess, String errno, String errmsg,
					TicketDetailItem item) {
				Message msg = Message.obtain();
				if(isSuccess){
					msg.what = GET_TICKET_DETAIL_SUCCESS;
					if(item.contentList != null){
						Arrays.sort(item.contentList, new Comparator<TicketContentItem>() {

							@Override
							public int compare(TicketContentItem lhs,
									TicketContentItem rhs) {
								if(lhs.sendDate != rhs.sendDate){
									return lhs.sendDate > rhs.sendDate ? -1:1;
								}else{
									return 0;
								}
							}
						});
					}
					msg.obj = item;
				}else{
					msg.what = GET_TICKET_DETAIL_FAILED;
					msg.obj = new RequestFailBean(errno, errmsg);
				}
				sendUiMessage(msg);
			}
		});
	}
	
	/**
	 * 结束反馈主题
	 * @param ticketId
	 */
	private void resolveTicket(String ticketId){
		showProgressDialog(getString(R.string.common_loading_tips));
		RequestOperator.newInstance(this).ResolvedTicket(ticketId, new OnRequestCallback() {
			
			@Override
			public void OnRequest(boolean isSuccess, String errno, String errmsg) {
				Message msg = Message.obtain();
				if(isSuccess){
					msg.what = TICKET_RESOLVE_SUCCESS;
				}else{
					msg.what = TICKET_RESOLVE_FAILED;
					msg.obj = new RequestFailBean(errno, errmsg);
				}
				sendUiMessage(msg);
			}
		});
	}
	
	@Override
	protected void handleUiMessage(Message msg) {
		// TODO Auto-generated method stub
		super.handleUiMessage(msg);
		hideProgressDialog();
		switch (msg.what) {
		case GET_TICKET_DETAIL_SUCCESS:
			ticketDetailItem = (TicketDetailItem)msg.obj;
			onTicketDetailSuccess();
			break;
		case GET_TICKET_DETAIL_FAILED:{
			emptyView.setText(getString(R.string.ticket_detail_error));
		}
			break;
		case TICKET_RESOLVE_SUCCESS:
			Intent intent = new Intent();
			intent.putExtra(TICKET_ID, ticketId);
			setResult(RESULT_OK, intent);
			finish();
			break;
		case TICKET_RESOLVE_FAILED:{
			RequestFailBean errorBean = (RequestFailBean)msg.obj;
			ToastUtil.showToast(this, errorBean.errmsg);
		}
			break;
		default:
			break;
		}
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if(resultCode == RESULT_OK){
			switch (requestCode) {
			case RESULT_REPLY_SUCCESS:
				/*回复成功，刷新详情列表*/
				queryTicketDetail(ticketId);
				break;

			default:
				break;
			}
		}
	}
	
}
