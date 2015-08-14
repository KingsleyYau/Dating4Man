package com.qpidnetwork.dating.livechat.expression;

import java.io.File;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.qpidnetwork.framework.util.StringUtil;
import com.qpidnetwork.livechat.LCEmotionItem;
import com.qpidnetwork.livechat.LCMessageItem;
import com.qpidnetwork.livechat.jni.LiveChatClientListener.LiveChatErrType;
import com.qpidnetwork.livechat.LiveChatManager;
import com.qpidnetwork.livechat.LiveChatManagerEmotionListener;
import com.qpidnetwork.request.item.OtherEmotionConfigItem;

public class EmotionImageDownloader implements LiveChatManagerEmotionListener{
	
	private LiveChatManager mLiveChatManager;
	private ImageView emotionPane;
	private ProgressBar pbDownload;
	private String emotionId;
	
	public EmotionImageDownloader(){
		mLiveChatManager = LiveChatManager.newInstance(null);
	}
	
	public void displayEmotionImage(ImageView emotionPane, ProgressBar pbDownload, String emotionId){
		this.emotionId = emotionId;
		LCEmotionItem item = mLiveChatManager.GetEmotionInfo(emotionId);
		if ((!StringUtil.isEmpty(item.imagePath))
				&& (new File(item.imagePath).exists())) {
			/* 有缩略图，直接使用 */
			Bitmap thumb = BitmapFactory.decodeFile(item.imagePath);
			emotionPane.setImageBitmap(thumb);
			if (pbDownload != null) pbDownload.setVisibility(View.GONE);
		} else {
			this.emotionPane = emotionPane;
			if (pbDownload != null) this.pbDownload = pbDownload;
			if (pbDownload != null) pbDownload.setVisibility(View.VISIBLE);
			mLiveChatManager.RegisterEmotionListener(this);
			mLiveChatManager
					.GetEmotionImage(emotionId);
		}
	}
	
	private Handler handler = new Handler(){
		public void handleMessage(android.os.Message msg) {
			LCEmotionItem item = (LCEmotionItem)msg.obj;
			if(item.emotionId.equals(emotionId)){
				mLiveChatManager.UnregisterEmotionListener(EmotionImageDownloader.this);
				if (pbDownload != null) pbDownload.setVisibility(View.GONE);
			
				if ((!StringUtil.isEmpty(item.imagePath))
						&& (new File(item.imagePath).exists())) {
					/* 有缩略图，直接使用 */
					Bitmap thumb = BitmapFactory.decodeFile(item.imagePath);
					emotionPane.setImageBitmap(thumb);
					if (pbDownload != null) pbDownload.setVisibility(View.GONE);
				}
			}
		}
	};

	@Override
	public void OnGetEmotionConfig(boolean success, String errno, String errmsg,
			OtherEmotionConfigItem item) {
		// TODO Auto-generated method stub
		
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
		Message msg = Message.obtain();
		if(success){
			msg.obj = emotionItem;
			handler.sendMessage(msg);
		}
		
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
