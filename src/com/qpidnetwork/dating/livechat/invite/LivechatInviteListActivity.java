package com.qpidnetwork.dating.livechat.invite;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Message;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.qpidnetwork.dating.R;
import com.qpidnetwork.dating.contacts.ContactManager;
import com.qpidnetwork.dating.contacts.OnNewInviteUpdateCallback;
import com.qpidnetwork.framework.base.BaseActionBarFragmentActivity;
import com.qpidnetwork.framework.util.StringUtil;
import com.qpidnetwork.framework.widget.CircleImageView;
import com.qpidnetwork.livechat.LCMessageItem;
import com.qpidnetwork.livechat.LCUserItem;
import com.qpidnetwork.livechat.LiveChatManager;
import com.qpidnetwork.livechat.LiveChatManagerOtherListener;
import com.qpidnetwork.livechat.jni.LiveChatClientListener.KickOfflineType;
import com.qpidnetwork.livechat.jni.LiveChatClientListener.LiveChatErrType;
import com.qpidnetwork.livechat.jni.LiveChatClientListener.TalkEmfNoticeType;
import com.qpidnetwork.livechat.jni.LiveChatTalkUserListItem;
import com.qpidnetwork.manager.FileCacheManager;
import com.qpidnetwork.manager.WebSiteManager;
import com.qpidnetwork.tool.ImageViewLoader;

public class LivechatInviteListActivity extends BaseActionBarFragmentActivity implements OnNewInviteUpdateCallback,
							LiveChatManagerOtherListener{
	
	private static final int TARGET_PHOTO_UPDATE = 0;
	private static final int NEW_INVITE_UPDATE = 1;

	private ListView lvContainer;
	private List<InviteItem> mInviteList;
	private HashMap<String, InviteItem> mCurrInviteMap;//存储所有当前邀请列表需要获取详情更新的对象列表
	private LivechatInviteAdapter mAdapter;
	
	private LiveChatManager mLiveChatManager;
	private ContactManager mContactManager;
	private WebSiteManager siteManager;
	
	
	@Override
	protected void onCreate(Bundle arg0) {
		// TODO Auto-generated method stub
		super.onCreate(arg0);
		this.getWindow().setBackgroundDrawable(new ColorDrawable(getResources().getColor(WebSiteManager.getInstance().GetWebSite().getSiteColor())));
		
		setCustomContentView(R.layout.activity_livechat_invite);
		
		lvContainer = (ListView)findViewById(R.id.lvContainer);
		TextView emptyView = (TextView)findViewById(R.id.emptyView);
		lvContainer.setEmptyView(emptyView);
		
		/*title*/
		getCustomActionBar().setTitle(getResources().getString(R.string.livechat_lady_invite_title), Color.WHITE);
		
		mLiveChatManager = LiveChatManager.getInstance();
		mLiveChatManager.RegisterOtherListener(this);
		mContactManager = ContactManager.getInstance();
		mContactManager.registerInviteUpdate(this);
		
		mInviteList = new ArrayList<InviteItem>();
		mCurrInviteMap = new HashMap<String, InviteItem>();
		mAdapter = new LivechatInviteAdapter(this, mInviteList);
		lvContainer.setAdapter(mAdapter);
		
		siteManager = WebSiteManager.getInstance();
		if (siteManager != null) getCustomActionBar().setAppbarBackgroundColor((this.getResources().getColor(siteManager.GetWebSite().getSiteColor())));
		
		updateListData();
	}
	
	/**
	 * 有新邀请更新数据
	 */
	private void updateListData(){
		List<LCUserItem> allInviteList = mLiveChatManager.GetInviteUsers();
		//处理最多只显示最近99条
		List<LCUserItem> inviteList = new ArrayList<LCUserItem>();
		int inviteLength = allInviteList.size() >=100 ? 99:allInviteList.size();
		for(int i=0; i<inviteLength; i++){
			inviteList.add(allInviteList.get(i));
		}
		mInviteList.clear();
		mCurrInviteMap.clear();
		if(inviteList != null){
			for(LCUserItem item : inviteList){
				LiveChatTalkUserListItem ladyInfo = mLiveChatManager.GetLadyInfoById(item.userId);
				InviteItem inviteItem = new InviteItem();
				inviteItem.userId = item.userId;
				inviteItem.username = item.userName;
				/*提示消息内容*/
				LCMessageItem lastMsg = item.getTheOtherLastMessage();
				inviteItem.msgDesc = mContactManager.generateMsgHint(lastMsg);
				if(ladyInfo != null){
					inviteItem.photoUrl = ladyInfo.imgUrl;
				}else{
					mCurrInviteMap.put(item.userId, inviteItem);
				}
				mInviteList.add(inviteItem); 
			}
			/*10个分组获取女士详情更新*/
			List<String> userIdList = new ArrayList<String>();
			Set<String> set = mCurrInviteMap.keySet();
			int i = 0;
			for(Iterator<String> iter = set.iterator(); iter.hasNext();)
			{
				String key = (String)iter.next();
				userIdList.add(key);
				i++;
				if(i>=10){
					mLiveChatManager.GetUsersInfo((String[])userIdList.toArray(new String[userIdList.size()]));
					userIdList.clear();
					i=0;
				}
			}
			if(userIdList.size() > 0 ){
				mLiveChatManager.GetUsersInfo((String[])userIdList.toArray(new String[userIdList.size()]));
			}
			
		}
		/*更新界面*/
		mAdapter.notifyDataSetChanged();
	}
	
	
	@Override
	protected void handleUiMessage(Message msg) {
		// TODO Auto-generated method stub
		super.handleUiMessage(msg);
		switch (msg.what) {
		case TARGET_PHOTO_UPDATE:{
			LiveChatTalkUserListItem[] itemArray = (LiveChatTalkUserListItem[])msg.obj;
			if(itemArray != null){
				for(LiveChatTalkUserListItem item : itemArray){
					updateListView(item);
				}
			}
		}break;
		case NEW_INVITE_UPDATE:
			updateListData();
			break;
		default:
			break;
		}
	}
	
	/**
	 * 获取女士详情返回
	 * @param item
	 */
	private void updateListView(LiveChatTalkUserListItem item){
		int mPosition = -1; //当前更新数据的位置
		/*更新数据*/
		if(mInviteList!=null){
			for(int i=0; i<mInviteList.size(); i++){
				InviteItem inviteItem = mInviteList.get(i);
				if(inviteItem.userId.equals(item.userId)){
					//更新用户名称，防止自动邀请时用户名未更新
					mLiveChatManager.GetUserWithId(inviteItem.userId).userName = item.userName;
					
					inviteItem.username = item.userName;
					inviteItem.photoUrl = item.imgUrl;
					mPosition = i;
				}
			}
		}
		
		/*更新单个Item*/
		 View childAt = lvContainer.getChildAt(mPosition - lvContainer.getFirstVisiblePosition());
         if(childAt != null){
        	 if(!StringUtil.isEmpty(item.userName)){
        		 ((TextView) childAt.findViewById(R.id.tvName)).setText(item.userName);
        	 }
        	 /*头像处理*/
     		if(!StringUtil.isEmpty(item.imgUrl)){
     			String localPath = FileCacheManager.getInstance().CacheImagePathFromUrl(item.imgUrl);
     			CircleImageView ivPhoto = (CircleImageView) childAt.findViewById(R.id.ivPhoto);
     			new ImageViewLoader(this).DisplayImage(ivPhoto, item.imgUrl, localPath, null);
     		}
         }
	}


	@Override
	public void onNewInviteUpdate() {
		Message msg = Message.obtain();
		msg.what = NEW_INVITE_UPDATE;
		sendUiMessage(msg);
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		if(mContactManager != null){
			mContactManager.unregisterInviteUpdata(this);
		}
		mLiveChatManager.UnregisterOtherListener(this);
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
	public void OnGetHistoryMessage(boolean success, String errno,
			String errmsg, LCUserItem userItem) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void OnGetUsersHistoryMessage(boolean success, String errno,
			String errmsg, LCUserItem[] userItems) {
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
		if(errType == LiveChatErrType.Success){
			Message msg = Message.obtain();
			msg.what = TARGET_PHOTO_UPDATE;
			msg.obj = itemList;
			sendUiMessage(msg);
		}		
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
		
	}
	
}
