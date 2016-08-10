package com.qpidnetwork.dating.emf;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentPagerAdapter;
import android.view.View;

import com.qpidnetwork.dating.R;
import com.qpidnetwork.framework.base.BaseTabbarTitleFragmentActivity;
import com.qpidnetwork.framework.util.Log;
import com.qpidnetwork.livechat.LCUserItem;
import com.qpidnetwork.livechat.LiveChatManager;
import com.qpidnetwork.livechat.LiveChatManagerOtherListener;
import com.qpidnetwork.livechat.jni.LiveChatClientListener.KickOfflineType;
import com.qpidnetwork.livechat.jni.LiveChatClientListener.LiveChatErrType;
import com.qpidnetwork.livechat.jni.LiveChatClientListener.TalkEmfNoticeType;
import com.qpidnetwork.livechat.jni.LiveChatTalkUserListItem;
import com.qpidnetwork.manager.WebSiteManager;
import com.qpidnetwork.request.RequestJniEMF.ReplyType;
import com.qpidnetwork.view.TitleTabBar;

public class EMFListActivity extends BaseTabbarTitleFragmentActivity implements LiveChatManagerOtherListener{
	
	private EMFPagerAdapter pageAdapter;
	
	private boolean newInboxUpdate = false;
	
	/**
	 * 请求消息
	 */
	private enum RequestFlag {
		REQUEST_LIVECHAT_SET_STATUS_SUCCESS,
	}
	
	/**
	 * 界面消息
	 */
	@SuppressWarnings("unused")
	private class MessageCallbackItem {
		/**
		 * 
		 * @param errno				接口错误码
		 * @param errmsg			错误提示
		 */
		public MessageCallbackItem(
				) {
		}
		public String errno;
		public String errmsg;
		public TalkEmfNoticeType noticeType;
	}
	
	@SuppressWarnings("deprecation")
	protected void onCreate(Bundle arg0) {
		// TODO Auto-generated method stub
		super.onCreate(arg0);
		
		// 统计设置为page activity
		SetPageActivity(true);
		
		this.getWindow().setBackgroundDrawable(new ColorDrawable(getResources().getColor(WebSiteManager.getInstance().GetWebSite().getSiteColor())));
		//Log.d("EMFLisActivity", "onCreate( TaskId : " + getTaskId() +" )");

		TitleTabBar tabBar = getTitleTabBar();
		tabBar.AddTab(0, "Inbox", getResources().getDrawable(R.drawable.ic_inbox_white_24dp));
		tabBar.AddTab(1, "Outbox", getResources().getDrawable(R.drawable.ic_outbox_white_24dp));
		pageAdapter = new EMFPagerAdapter(this);
		getViewPagerContainer().setAdapter(pageAdapter);
		
		
		//getCustomActionBar().addButtonToRight(R.id.common_button_send, "send", R.drawable.ic_send_white_24dp);
		getCustomActionBar().addButtonToRight(R.id.common_button_search, "search", R.drawable.ic_search_white_24dp);
		getCustomActionBar().setAppbarBackgroundColor(getResources().getColor(WebSiteManager.getInstance().GetWebSite().getSiteColor()));
		/*暂时屏蔽Emf黑名单功能*/
//		getCustomActionBar().addOverflowButton(new String[]{getString(R.string.emf_block_list)}, new OnClickCallback(){
//
//			@Override
//			public void onClick(AdapterView<?> adptView, View v, int which) {
//				// TODO Auto-generated method stub
//				EMFBlockedListActivity.launchEMFBlockedListActivity(EMFListActivity.this);
//			}
//			
//		}, R.drawable.ic_more_vert_white_24dp);
		tabBar.SelectTab(0);
		
//		getFloatButton().setId(R.id.common_button_send);
//		getFloatButton().setOnClickListener(this);
//		getFloatButton().setVisibility(View.VISIBLE);
		
		LiveChatManager.getInstance().RegisterOtherListener(this);
		// 清空emf消息中心
		EMFNotification.newInstance(this.getApplicationContext()).Cancel();
	}
	
	@Override
	protected void onDestroy()
	{
		super.onDestroy();
		LiveChatManager.getInstance().UnregisterOtherListener(this);
	}
	
	@Override
	protected void onNewIntent(Intent intent) {
		// TODO Auto-generated method stub
		super.onNewIntent(intent);
		Log.d("EMFLisActivity", "onNewIntent( TaskId : " + getTaskId() +" )");
		
		// 清空emf消息中心
		EMFNotification.newInstance(this).Cancel();
	}
	
	public class EMFPagerAdapter extends FragmentPagerAdapter {

		private int count = 2;// 子页面数目
		private Fragment[] fragmentArr = new Fragment[2];

		public EMFPagerAdapter(FragmentActivity activity) {
			super(activity.getSupportFragmentManager());
		}

		@Override
		public int getCount() {
			return count;
		}

		public Fragment getFragment(int index){
			return fragmentArr[index];
		}
		
		@Override
		public Fragment getItem(int position) {
			if (fragmentArr[position] != null) {
				return fragmentArr[position];
			}
			Fragment fragment = null;
			switch (position) {
			case 0:// 我的好友
				fragment = EMFInboxFragment.newInstance();
				break;
			case 1:// 匹配女士
				fragment = EMFOutboxFragment.newInstance();
				break;
			}
			fragmentArr[position] = fragment;
			return fragmentArr[position];
		}
	}
	
	@Override
	public void onPageSelected(int arg0) {
		// TODO Auto-generated method stub
		super.onPageSelected(arg0);
		onTabSelectedRefresh();
	}
	
	@Override
	public void onTabSelected(int index) {
		super.onTabSelected(index);
		// 统计切换页
		onAnalyticsPageSelected(index);
	}
	
	private void onTabSelectedRefresh(){
		int index = getCurrentIndex();
		EMFInboxFragment fragment;
		if(index == 0){
			fragment = (EMFInboxFragment)pageAdapter.getFragment(0);
			if( fragment != null ) {
				fragment.TabSelectRefresh(newInboxUpdate);
				// 强制刷新完成
				newInboxUpdate = false;
			}
		}else if(index == 1){
			fragment = (EMFInboxFragment)pageAdapter.getFragment(0);
			if( fragment != null ) {
				fragment.TabSelectRefresh(false);
			}
		}

	}
	
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		super.onClick(v);
		switch (v.getId()) {
		case R.id.common_button_send:
			MailEditActivity.launchMailEditActivity(this, "", ReplyType.DEFAULT, "", "");
			break;
		case R.id.common_button_search:
			onSearch();
			break;
		}
	}
	
	/**
	 * 搜索按钮响应
	 */
	private void onSearch(){
		int index = getCurrentIndex();
		if(index == 0){
			startActivityForResult(EMFSearchActivity.getIntent(this, 0),EMFDetailActivity.REQUEST_CODE);
		}else if(index == 1){
			startActivityForResult(EMFSearchActivity.getIntent(this, 1),EMFDetailActivity.REQUEST_CODE);
		}
		//设置切换动画，从右边进入，左边退出  
        overridePendingTransition(R.anim.anim_donot_animate, R.anim.anim_donot_animate);         
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent arg2) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, arg2);
		if(resultCode == RESULT_OK){
			if(requestCode == EMFDetailActivity.REQUEST_CODE){
				if((arg2 != null)&&(arg2.getExtras().containsKey(EMFDetailActivity.EMF_MESSAGEID))){
					String emfId = arg2.getExtras().getString(EMFDetailActivity.EMF_MESSAGEID);
					boolean isDelete = arg2.getExtras().getBoolean(EMFDetailActivity.EMF_DELETE);
					boolean isRead = arg2.getExtras().getBoolean(EMFDetailActivity.EMF_DETAIL_READED);
					if(emfId != null){
						if(isDelete){
							onEMFDelete(emfId);
						}else if(isRead){
							onEMFRead(emfId);
						}
					}
				}
			}
		}
		
	}
	
	private void onEMFRead(String emfId){
		int index = getCurrentIndex();
		if(index == 0){
			EMFInboxFragment fragment = (EMFInboxFragment)pageAdapter.getFragment(0);
			if(fragment != null){
				fragment.onEMFRead(emfId);
			}
		}
	}
	
	/**
	 * 删除邮件回调
	 * @param emfId
	 */
	private void onEMFDelete(String emfId){
		showToastProgressing("");
		showToastDone("Deleted!");
		int index = getCurrentIndex();
		if(index == 0){
			EMFInboxFragment fragment = (EMFInboxFragment)pageAdapter.getFragment(0);
			if(fragment != null){
				fragment.onEMFDelete(emfId);
			}
		}else if(index == 1){
			EMFOutboxFragment fragment = (EMFOutboxFragment)pageAdapter.getFragment(1);
			if(fragment != null){
				fragment.onEMFDelete(emfId);
			}
		}
	}

	@Override
	public void OnLogin(LiveChatErrType errType, String errmsg,
			boolean isAutoLogin) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void OnLogout(LiveChatErrType errType, String errmsg,
			boolean isAutoLogin) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void OnGetTalkList(LiveChatErrType errType, String errmsg) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void OnGetHistoryMessage(boolean success, String errno, String errmsg,
			LCUserItem userItem) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void OnGetUsersHistoryMessage(boolean success, String errno, String errmsg,
			LCUserItem[] userItems) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void OnSetStatus(LiveChatErrType errType, String errmsg) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void OnGetUserStatus(LiveChatErrType errType, String errmsg,
			LCUserItem[] userList) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void OnGetUsersInfo(LiveChatErrType errType, String errmsg,
			LiveChatTalkUserListItem[] itemList) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void OnUpdateStatus(LCUserItem userItem) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void OnChangeOnlineStatus(LCUserItem userItem) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void OnRecvKickOffline(KickOfflineType kickType) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void OnRecvEMFNotice(String fromId, TalkEmfNoticeType noticeType) {
		// TODO Auto-generated method stub
		// 收到EMF更新通知
		Message msg = Message.obtain();
		MessageCallbackItem obj = new MessageCallbackItem();
		msg.what = RequestFlag.REQUEST_LIVECHAT_SET_STATUS_SUCCESS.ordinal();
		msg.obj = obj;
		obj.noticeType = noticeType;
		sendUiMessage(msg);
	}
	
	@Override
	protected void handleUiMessage(Message msg) {
		// TODO Auto-generated method stub
		super.handleUiMessage(msg);

		MessageCallbackItem obj = (MessageCallbackItem) msg.obj;
		switch ( RequestFlag.values()[msg.what] ) {
		case REQUEST_LIVECHAT_SET_STATUS_SUCCESS: {
			// 收到EMF更新通知
			if( obj.noticeType == TalkEmfNoticeType.EMF ) {
				// 在EMF列表需要显示红点
				getTitleTabBar().SetTabBadge(0, " ");
				
				// 标记收件箱为需要强制刷新
				newInboxUpdate = true;
			}
		}break;
		default:
			break;
		}
	}
}
