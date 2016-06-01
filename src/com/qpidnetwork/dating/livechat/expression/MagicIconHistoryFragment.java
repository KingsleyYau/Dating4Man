package com.qpidnetwork.dating.livechat.expression;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Message;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.ImageView;

import com.qpidnetwork.dating.R;
import com.qpidnetwork.dating.authorization.LoginManager;
import com.qpidnetwork.dating.livechat.ChatActivity;
import com.qpidnetwork.framework.base.BaseFragment;
import com.qpidnetwork.livechat.LCMagicIconItem;
import com.qpidnetwork.livechat.LCMessageItem;
import com.qpidnetwork.livechat.LiveChatManager;
import com.qpidnetwork.livechat.LiveChatManagerMagicIconListener;
import com.qpidnetwork.livechat.jni.LiveChatClientListener.LiveChatErrType;
import com.qpidnetwork.manager.WebSiteManager;
import com.qpidnetwork.request.item.MagicIconConfig;
import com.qpidnetwork.request.item.MagicIconItem;
import com.qpidnetwork.tool.ImageViewLoader;

public class MagicIconHistoryFragment extends BaseFragment implements LiveChatManagerMagicIconListener{
	
	private static final int GET_MAGICICON_THUNMB_CALLBACK = 1;
	
	private GridView gvEmotion;
	
	private ArrayList<String> mMagicIconHistoryIds;
	private List<MagicIconItem> mIconItemList;
	private MagicIconConfig mMagicIconConfig;
	private HighExpressionGridViewAdapter mAdapter;
	private LiveChatManager mLiveChatManager;
	
	/*广播用于activity间数据传递*/
	private BroadcastReceiver mBroadcastReceiver;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_emotion_gridview, null);
		gvEmotion = (GridView) view.findViewById(R.id.gvEmotion);
		return view;
	}
	
	@Override
	public void onActivityCreated(@Nullable Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onActivityCreated(savedInstanceState);
		mIconItemList = new ArrayList<MagicIconItem>();
		mLiveChatManager = LiveChatManager.getInstance();
		mMagicIconConfig = mLiveChatManager.GetMagicIconConfigItem();
		mLiveChatManager.RegisterMagicIconListener(this);
		
		initData();
		
		initReceive();
	}
	
	/**
	 * 初始化历史数据
	 */
	private void initData(){
		String curSite = WebSiteManager.getInstance().GetWebSite().getSiteName();
		String userId = LoginManager.getInstance().GetLoginParam().item.manid;
		mMagicIconHistoryIds =  EmotionHistoryUtil.getItemStringIds(mContext, userId, curSite, "MagicIcon");
		for(String magicIconId : mMagicIconHistoryIds){
			if(!TextUtils.isEmpty(magicIconId)){
				MagicIconItem item = getMagicItemByIconId(magicIconId);
				if(item != null){
					mIconItemList.add(item);
				}
			}
		}
		mAdapter = new HighExpressionGridViewAdapter(getActivity(),mIconItemList);
		gvEmotion.setAdapter(mAdapter);
		gvEmotion.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				MagicIconItem item =  mIconItemList.get(position);
				Intent intent = new Intent(ChatActivity.SEND_MAGICICON_ACTION);
				intent.putExtra(ChatActivity.MAGICICON_ID, item.id);
				mContext.sendBroadcast(intent);	
			}
		});
	}
	
	private MagicIconItem getMagicItemByIconId(String magicIconId){
		MagicIconItem magicItem = null;
		if(mMagicIconConfig != null){
			if(!TextUtils.isEmpty(magicIconId)){
				for(MagicIconItem item : mMagicIconConfig.magicIconArray){
					if(item.id.equals(magicIconId)){
						magicItem = item;
						break;
					}
				}
			}
		}
		return magicItem;
	}
	
	@Override
	protected void handleUiMessage(Message msg) {
		// TODO Auto-generated method stub
		super.handleUiMessage(msg);
		switch (msg.what) {
		case GET_MAGICICON_THUNMB_CALLBACK:{
			LCMagicIconItem item = (LCMagicIconItem)msg.obj;
			if(item != null){
				String localPath = item.getThumbPath();
				if(!TextUtils.isEmpty(localPath) && (new File(localPath).exists())){
					updateMagicThumbImage(item);
				}
			}
		}break;

		default:
			break;
		}
	}
	
	private void updateMagicThumbImage(LCMagicIconItem item){
		if(item != null){
			int position = -1;
			if(mIconItemList != null){
				for(int i=0; i<mIconItemList.size(); i++){
					if(mIconItemList.get(i).id.equals(item.getMagicIconId())){
						position = i;
						break;
					}
				}
			}
			
			if(position >= 0){
				/*更新单个Item*/
				 View childAt = gvEmotion.getChildAt(position - gvEmotion.getFirstVisiblePosition());
				 if(childAt != null){
					 ImageView magicIconImage = ((ImageView) childAt.findViewById(R.id.icon));
					 new ImageViewLoader(mContext).DisplayImage(magicIconImage, null, item.getThumbPath(), null);
				 }
			}
		}
	}
	
	@Override
	public void onDetach() {
		// TODO Auto-generated method stub
		super.onDetach();
		mLiveChatManager.UnregisterMagicIconListener(this);
	}
//-----------------  magic icon history -------------------

	private void initReceive(){
		mBroadcastReceiver = new BroadcastReceiver() {
			
			@Override
			public void onReceive(Context context, Intent intent) {
				// TODO Auto-generated method stub
				String action = intent.getAction();
				if(action.equals(ChatActivity.SEND_MAGICICON_ACTION)){
					String magicIconId = intent.getExtras().getString(ChatActivity.MAGICICON_ID);
					updateMagicIconHistoryList(magicIconId);
				}
			}
		};
		IntentFilter filter = new IntentFilter();
		filter.addAction(ChatActivity.SEND_MAGICICON_ACTION);
		getActivity().registerReceiver(mBroadcastReceiver, filter);
	}
	
	/**
	 * 更新MagicIcon历史
	 * @param magicIconId
	 */
	private void updateMagicIconHistoryList(String magicIconId){
		MagicIconItem item = getMagicItemByIconId(magicIconId);
		if (mMagicIconHistoryIds.contains(magicIconId)) {
			mMagicIconHistoryIds.remove(magicIconId);
			mIconItemList.remove(item);
			mMagicIconHistoryIds.add(0, magicIconId);
			mIconItemList.add(0,item);
		}else{
			while (mMagicIconHistoryIds.size()>=10) {
				mMagicIconHistoryIds.remove(mMagicIconHistoryIds.size()-1);
				mIconItemList.remove(mIconItemList.size()-1);
			}
			mMagicIconHistoryIds.add(0, magicIconId);
			mIconItemList.add(0, item);
		} 
		
		String curSite = WebSiteManager.getInstance().GetWebSite().getSiteName();
		String userId = LoginManager.getInstance().GetLoginParam().item.manid;
		
		EmotionHistoryUtil.saveItemStringIds(mMagicIconHistoryIds, mContext, userId, curSite, "MagicIcon");
		mAdapter.notifyDataSetChanged();
	}
	
//------------------ MagicIcon relative callback --------------------------
	@Override
	public void OnGetMagicIconConfig(boolean success, String errno,
			String errmsg, MagicIconConfig item) {
		
	}

	@Override
	public void OnSendMagicIcon(LiveChatErrType errType, String errmsg,
			LCMessageItem item) {
		
	}

	@Override
	public void OnRecvMagicIcon(LCMessageItem item) {
		
	}

	@Override
	public void OnGetMagicIconSrcImage(boolean success,
			LCMagicIconItem magicIconItem) {
		
	}

	@Override
	public void OnGetMagicIconThumbImage(boolean success,
			LCMagicIconItem magicIconItem) {
		if(success){
			Message msg = Message.obtain();
			msg.what = GET_MAGICICON_THUNMB_CALLBACK;
			msg.obj = magicIconItem;
			sendUiMessage(msg);
		}
	}
}
