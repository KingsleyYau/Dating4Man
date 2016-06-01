package com.qpidnetwork.dating.livechat.expression;

import java.io.File;
import java.util.List;

import android.content.Intent;
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
import com.qpidnetwork.dating.livechat.ChatActivity;
import com.qpidnetwork.framework.base.BaseFragment;
import com.qpidnetwork.livechat.LCMagicIconItem;
import com.qpidnetwork.livechat.LCMessageItem;
import com.qpidnetwork.livechat.LiveChatManager;
import com.qpidnetwork.livechat.LiveChatManagerMagicIconListener;
import com.qpidnetwork.livechat.jni.LiveChatClientListener.LiveChatErrType;
import com.qpidnetwork.request.item.MagicIconConfig;
import com.qpidnetwork.request.item.MagicIconItem;
import com.qpidnetwork.tool.ImageViewLoader;

public class HighExpressionFragment extends BaseFragment implements LiveChatManagerMagicIconListener{
	
	private static final int GET_MAGICICON_THUNMB_CALLBACK = 1;
	
	private GridView gvEmotion;
	public OnItemClickCallback itemClickCallback;

	private List<MagicIconItem> mIconItemList;
	private HighExpressionGridViewAdapter mAdapter;
	private LiveChatManager mLiveChatManager;

	public HighExpressionFragment(List<MagicIconItem> iconItemList) {
		super();
		this.mIconItemList = iconItemList;
	}

	public interface OnItemClickCallback {
		public void onItemClick();

		public void onItemLongClick();

		public void onItemLongClickUp();
	}

	public void setOnItemClickCallback(OnItemClickCallback callback) {
		this.itemClickCallback = callback;
	};

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
		mLiveChatManager = LiveChatManager.getInstance();
		if(mIconItemList.size()>0){
			mLiveChatManager.RegisterMagicIconListener(this);
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
