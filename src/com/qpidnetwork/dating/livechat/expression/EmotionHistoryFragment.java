package com.qpidnetwork.dating.livechat.expression;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;

import com.qpidnetwork.dating.R;
import com.qpidnetwork.dating.authorization.LoginPerfence;
import com.qpidnetwork.dating.livechat.ChatActivity;
import com.qpidnetwork.dating.livechat.expression.NormalEmotionFragment.OnItemClickCallback;
import com.qpidnetwork.framework.base.BaseFragment;
import com.qpidnetwork.livechat.LCEmotionItem;
import com.qpidnetwork.livechat.LCMessageItem;
import com.qpidnetwork.livechat.LiveChatManager;
import com.qpidnetwork.livechat.LiveChatManagerEmotionListener;
import com.qpidnetwork.livechat.jni.LiveChatClientListener.LiveChatErrType;
import com.qpidnetwork.manager.WebSiteManager;
import com.qpidnetwork.request.item.OtherEmotionConfigEmotionItem;
import com.qpidnetwork.request.item.OtherEmotionConfigItem;
import com.qpidnetwork.view.MaterialProgressBar;

public class EmotionHistoryFragment extends BaseFragment implements LiveChatManagerEmotionListener{
	
	private GridView gvEmotion;
	private MaterialProgressBar pbDownload;
	
	/*广播用于activity间数据传递*/
	private BroadcastReceiver mBroadcastReceiver;
	
	/*data*/
	private ArrayList<String> premiumIds;
	private List<OtherEmotionConfigEmotionItem> premiumdataList;
	private LiveChatManager mLiveChatManager;
	private EmotionGridviewAdapter mAdapter;
	public OnItemClickCallback itemClickCallback;
	
	
	public void setOnItemClickCallback(OnItemClickCallback callback){
		this.itemClickCallback = callback;
	};
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_emotion_gridview, null);
		gvEmotion = (GridView)view.findViewById(R.id.gvEmotion);
		pbDownload = (MaterialProgressBar)view.findViewById(R.id.progress);
		return view;
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onActivityCreated(savedInstanceState);
		
		mLiveChatManager = LiveChatManager.newInstance(null);
		OtherEmotionConfigItem item = mLiveChatManager.GetEmotionConfigItem();
		if(item != null){
			updateEmotionGridview(Arrays.asList(item.manEmotionList));
		}else{
			pbDownload.setVisibility(View.VISIBLE);
			mLiveChatManager.RegisterEmotionListener(this);
			boolean isSuccess = LiveChatManager.newInstance(null).GetEmotionConfig();
			if(!isSuccess){
				pbDownload.setVisibility(View.GONE);
				mLiveChatManager.UnregisterEmotionListener(this);
			}
		}
		
	}
	
	private void updateEmotionGridview(List<OtherEmotionConfigEmotionItem> emotionList){
		String curSite = WebSiteManager.newInstance(getActivity()).GetWebSite().getSiteName();
		String userId = LoginPerfence.GetLoginParam(getActivity()).item.manid;
		premiumIds = EmotionHistoryUtil.getItemStringIds(getActivity(), userId, curSite, "premium");
		premiumdataList = new ArrayList<OtherEmotionConfigEmotionItem>();
		for (String idstr : premiumIds) {
			for (OtherEmotionConfigEmotionItem emoBean : emotionList) {
				if (emoBean.fileName.equals(idstr)) {
					premiumdataList.add(emoBean);
				}
			}
		}
		mAdapter = new EmotionGridviewAdapter(getActivity(), premiumdataList, gvEmotion, itemClickCallback);
		gvEmotion.setAdapter(mAdapter);
		initReceive();
	}
	
	private void initReceive(){
		mBroadcastReceiver = new BroadcastReceiver() {
			
			@Override
			public void onReceive(Context context, Intent intent) {
				// TODO Auto-generated method stub
				String action = intent.getAction();
				if(action.equals(ChatActivity.SEND_EMTOTION_ACTION)){
					String emotionId = intent.getExtras().getString(ChatActivity.EMOTION_ID);
					updateEmotionHistoryList(emotionId);
				}
			}
		};
		IntentFilter filter = new IntentFilter();
		filter.addAction(ChatActivity.SEND_EMTOTION_ACTION);
		getActivity().registerReceiver(mBroadcastReceiver, filter);
	}
	
	private void updateEmotionHistoryList(String emotionId){
		if (premiumIds.contains(emotionId)) {
			OtherEmotionConfigEmotionItem item = getEmotionItemById(emotionId, premiumdataList);
			premiumIds.remove(emotionId);
			premiumdataList.remove(item);
			premiumIds.add(0, emotionId);
			premiumdataList.add(0,item);
		}else{
			while (premiumIds.size()>=10) {
				premiumIds.remove(premiumIds.size()-1);
				premiumdataList.remove(premiumdataList.size()-1);
			}
			premiumIds.add(0, emotionId);
			premiumdataList.add(0,getEmotionItemById(emotionId, Arrays.asList(mLiveChatManager.GetEmotionConfigItem().manEmotionList)));
		} 
		
		String curSite = WebSiteManager.newInstance(getActivity()).GetWebSite().getSiteName();
		String userId = LoginPerfence.GetLoginParam(getActivity()).item.manid;
		
		EmotionHistoryUtil.saveItemStringIds(premiumIds, getActivity(), userId, curSite, "premium");
		mAdapter.notifyDataSetChanged();
	}
	
	/**
	 * 根据Id获取Item
	 * @param emotionId
	 * @return
	 */
	private OtherEmotionConfigEmotionItem getEmotionItemById(String emotionId, List<OtherEmotionConfigEmotionItem> emotionList){
		if(emotionList != null){
			for(OtherEmotionConfigEmotionItem item : emotionList){
				if(item.fileName.equals(emotionId)){
					return item;
				}
			}
		}
		return null;
	}
	
	@Override
	protected void handleUiMessage(Message msg) {
		// TODO Auto-generated method stub
		super.handleUiMessage(msg);
		pbDownload.setVisibility(View.GONE);
		mLiveChatManager.UnregisterEmotionListener(this);
		if(msg.arg1 == 1){
			OtherEmotionConfigItem item = (OtherEmotionConfigItem)msg.obj;
			updateEmotionGridview(Arrays.asList(item.manEmotionList));
		}
	}
	
	@Override
	public void onDetach() {
		// TODO Auto-generated method stub
		super.onDetach();
		mLiveChatManager.UnregisterEmotionListener(this);
		if(mBroadcastReceiver != null){
			getActivity().unregisterReceiver(mBroadcastReceiver);
		}
	}

	@Override
	public void OnGetEmotionConfig(boolean success, String errno, String errmsg,
			OtherEmotionConfigItem item) {
		// TODO Auto-generated method stub
		Message msg = Message.obtain();
		msg.arg1 = success?1:0;
		msg.obj = item;
		sendUiMessage(msg);
	}

	@Override
	public void OnSendEmotion(LiveChatErrType errType, String errmsg,
			LCMessageItem item) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void OnRecvEmotion(LCMessageItem item) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void OnGetEmotionImage(boolean success,
			LCEmotionItem emotionItem) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void OnGetEmotionPlayImage(boolean success,
			LCEmotionItem emotionItem) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void OnGetEmotion3gp(boolean success, LCEmotionItem emotionItem) {
		// TODO Auto-generated method stub
		
	}
}
