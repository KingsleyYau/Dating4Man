package com.qpidnetwork.dating.googleanalytics;

import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.View.OnClickListener;

import com.qpidnetwork.dating.R;
import com.qpidnetwork.dating.contacts.ContactManager;
import com.qpidnetwork.dating.home.HomeActivity;
import com.qpidnetwork.livechat.jni.LiveChatClientListener.KickOfflineType;
import com.qpidnetwork.view.MaterialDialogAlert;

/**
 * GoogleAnalytics统计的FragmentActivity基类
 * @author Samson Fan
 *
 */
public class GAFragmentActivity extends FragmentActivity 
{
	public static final String LIVECHAT_KICKOFF_ACTION = "kickoff";
	
	private BroadcastReceiver kickoffReceiver = new BroadcastReceiver(){
		public void onReceive(android.content.Context context, android.content.Intent intent) {
			String action = intent.getAction();
			if(action.equals(LIVECHAT_KICKOFF_ACTION)){
				Bundle bundle = intent.getExtras();
				if(bundle != null && bundle.containsKey(ContactManager.LIVE_CHAT_KICK_OFF)){
					final KickOfflineType type = KickOfflineType.values()[bundle.getInt(ContactManager.LIVE_CHAT_KICK_OFF)];
					MaterialDialogAlert dialog = new MaterialDialogAlert(GAFragmentActivity.this);
					if(type == KickOfflineType.Maintain){
						dialog.setMessage(getString(R.string.livechat_kickoff_by_sever_update));
					}
					else{
						dialog.setMessage(getString(R.string.livechat_kickoff_by_other));
					}
				
					dialog.addButton(dialog.createButton(getString(R.string.common_btn_ok), new OnClickListener() {
						
						@Override
						public void onClick(View v) {
							Intent intent = new Intent(GAFragmentActivity.this, HomeActivity.class);
							intent.putExtra(ContactManager.LIVE_CHAT_KICK_OFF, type);
							intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
							startActivity(intent);
						}
					}));
					dialog.show();
				}
			}
		};
	};
	
	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		GAManager.newInstance().getReportStart(this);
	}
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		IntentFilter filter = new IntentFilter();
		filter.addAction(LIVECHAT_KICKOFF_ACTION);
		registerReceiver(kickoffReceiver, filter);
	}
	
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		unregisterReceiver(kickoffReceiver);
	}
	
	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		GAManager.newInstance().getReportStop(this);
		super.onStop();
	}
}
