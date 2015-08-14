package com.qpidnetwork.dating.contactus;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;

import com.qpidnetwork.dating.R;
import com.qpidnetwork.framework.base.BaseActionBarFragmentActivity;
import com.qpidnetwork.view.MaterialAppBar;
import com.qpidnetwork.view.MaterialDropDownMenu.OnClickCallback;

public class ContactTicketListActivity extends BaseActionBarFragmentActivity {
	
	public static final String ACTION_GET_DETAIL_SUCCESS = "getDetailSuccess";
	
	private static final int RESULT_CREATE_NEW_TICKET = 1;
	
	public ContactTicketListFragment fragment;
	
	private BroadcastReceiver mBroadcastReceiver; //用于处理已读未读问题

	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		setCustomContentView(R.layout.activity_ticket_list_activity);

		fragment = new ContactTicketListFragment();
		getSupportFragmentManager().beginTransaction().replace(R.id.flTicketList, fragment).commit();
		
		/*actionBar*/
		getCustomActionBar().setTitle(getString(R.string.title_open_tickets), getResources().getColor(R.color.text_color_dark));
		getCustomActionBar().setAppbarBackgroundColor(getResources().getColor(R.color.theme_actionbar_secoundary));
		getCustomActionBar().setTouchFeedback(MaterialAppBar.TOUCH_FEEDBACK_HOLO_LIGHT);
		getCustomActionBar().setButtonIconById(R.id.common_button_back, R.drawable.ic_arrow_back_grey600_24dp);
		getCustomActionBar().getButtonById(R.id.common_button_back).setBackgroundResource(MaterialAppBar.TOUCH_FEEDBACK_HOLO_LIGHT);
		
		getCustomActionBar().addOverflowButton(new String[]{getString(R.string.menu_more_add_new_ticket)}, new OnClickCallback() {
			
			@Override
			public void onClick(AdapterView<?> adptView, View v, int which) {
				Intent intent = new Intent(ContactTicketListActivity.this, TicketCreateActivity.class);
				startActivityForResult(intent, RESULT_CREATE_NEW_TICKET);
			}
		}, R.drawable.ic_more_vert_grey600_24dp);
		
		initReceive();
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if(resultCode == RESULT_OK){
			switch (requestCode) {
			case RESULT_CREATE_NEW_TICKET:
				/*创建新Ticket成功刷新列表*/
				fragment.onNewCreateUpdate();
				break;

			default:
				break;
			}
		}
	}
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		unregisterReceiver(mBroadcastReceiver);
	}

	private void initReceive(){
		mBroadcastReceiver = new BroadcastReceiver() {
			
			@Override
			public void onReceive(Context context, Intent intent) {
				// TODO Auto-generated method stub
				String action = intent.getAction();
				if(action.equals(ACTION_GET_DETAIL_SUCCESS)){
					String ticketId = intent.getExtras().getString(TicketDetailListActivity.TICKET_ID);
					fragment.onReceiveReadedNotify(ticketId);
				}
			}
		};
		IntentFilter filter = new IntentFilter();
		filter.addAction(ACTION_GET_DETAIL_SUCCESS);
		registerReceiver(mBroadcastReceiver, filter);
	}
}
