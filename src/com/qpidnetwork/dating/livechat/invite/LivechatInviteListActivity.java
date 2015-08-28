package com.qpidnetwork.dating.livechat.invite;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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
import com.qpidnetwork.dating.lady.LadyDetailManager;
import com.qpidnetwork.dating.lady.LadyDetailManager.OnLadyDetailManagerQueryLadyDetailCallback;
import com.qpidnetwork.framework.base.BaseActionBarFragmentActivity;
import com.qpidnetwork.framework.util.StringUtil;
import com.qpidnetwork.framework.widget.CircleImageView;
import com.qpidnetwork.livechat.LCMessageItem;
import com.qpidnetwork.livechat.LCUserItem;
import com.qpidnetwork.livechat.LiveChatManager;
import com.qpidnetwork.manager.FileCacheManager;
import com.qpidnetwork.manager.WebSiteManager;
import com.qpidnetwork.request.item.LadyDetail;
import com.qpidnetwork.tool.ImageViewLoader;

public class LivechatInviteListActivity extends BaseActionBarFragmentActivity implements OnNewInviteUpdateCallback{
	
	private static final int TARGET_PHOTO_UPDATE = 0;
	private static final int NEW_INVITE_UPDATE = 1;

	private ListView lvContainer;
	private List<InviteItem> mInviteList;
	private HashMap<String, InviteItem> mCurrInviteMap;//存储所有当前出现过得邀请信息，方便找回
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
		
		mLiveChatManager = LiveChatManager.newInstance(this);
		mContactManager = ContactManager.getInstance();
		mContactManager.registerInviteUpdate(this);
		
		mInviteList = new ArrayList<InviteItem>();
		mCurrInviteMap = new HashMap<String, InviteItem>();
		mAdapter = new LivechatInviteAdapter(this, mInviteList);
		lvContainer.setAdapter(mAdapter);
		
		siteManager = WebSiteManager.newInstance(this);
		if (siteManager != null) getCustomActionBar().setAppbarBackgroundColor((this.getResources().getColor(siteManager.GetWebSite().getSiteColor())));
		
		updateListData();
	}
	
	/**
	 * 有新邀请更新数据
	 */
	private void updateListData(){
		List<LCUserItem> inviteList = mLiveChatManager.GetInviteUsers();
		mInviteList.clear();
		if(inviteList != null){
			for(LCUserItem item : inviteList){
				if(mCurrInviteMap.containsKey(item.userId)){
					mInviteList.add(mCurrInviteMap.get(item.userId));
				}else{
					InviteItem inviteItem = new InviteItem();
					inviteItem.userId = item.userId;
					inviteItem.username = item.userName;
					/*获取详细信息*/
					QueryLadyDetail(item.userId);
					mCurrInviteMap.put(item.userId, inviteItem);
					mInviteList.add(inviteItem);
				}
				
				/*提示消息内容*/
				LCMessageItem lastMsg = item.getTheOtherLastMessage();
				if (lastMsg != null){
					mCurrInviteMap.get(item.userId).msgDesc = mContactManager.generateMsgHint(lastMsg);
				} 
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
		case TARGET_PHOTO_UPDATE:
			LadyDetail item = (LadyDetail)msg.obj;
			updateListView(item);
			break;
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
	private void updateListView(LadyDetail item){
		int mPosition = -1; //当前更新数据的位置
		/*更新数据*/
		if(mInviteList!=null){
			for(int i=0; i<mInviteList.size(); i++){
				InviteItem inviteItem = mInviteList.get(i);
				if(inviteItem.userId.equals(item.womanid)){
					inviteItem.username = item.firstname;
					inviteItem.photoUrl = item.photoMinURL;
					mPosition = i;
				}
			}
		}
		
		/*更新单个Item*/
		 View childAt = lvContainer.getChildAt(mPosition - lvContainer.getFirstVisiblePosition());
         if(childAt != null){
        	 if(!StringUtil.isEmpty(item.firstname)){
        		 ((TextView) childAt.findViewById(R.id.tvName)).setText(item.firstname);
        	 }
        	 /*头像处理*/
     		if(!StringUtil.isEmpty(item.photoMinURL)){
     			String localPath = FileCacheManager.getInstance().CacheImagePathFromUrl(item.photoMinURL);
     			CircleImageView ivPhoto = (CircleImageView) childAt.findViewById(R.id.ivPhoto);
     			new ImageViewLoader(this).DisplayImage(ivPhoto, item.photoMinURL, localPath, null);
     		}
         }
	}

	
	/**
	 * 由于邀请列表无女士头像及会员名称，需调用详情获取
	 * @param womanid
	 */
	private void QueryLadyDetail(String womanid){
		LadyDetailManager.getInstance().QueryLadyDetail(womanid, new OnLadyDetailManagerQueryLadyDetailCallback() {
			
			@Override
			public void OnQueryLadyDetailCallback(boolean isSuccess, String errno,
					String errmsg, LadyDetail item) {
				// TODO Auto-generated method stub
				if(isSuccess){
					Message msg = Message.obtain();
					msg.what = TARGET_PHOTO_UPDATE;
					msg.obj = item;
					sendUiMessage(msg);
				}
			}
		});
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
	}
	
}
