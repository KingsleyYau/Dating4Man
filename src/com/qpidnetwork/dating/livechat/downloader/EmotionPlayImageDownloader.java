package com.qpidnetwork.dating.livechat.downloader;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.qpidnetwork.dating.R;
import com.qpidnetwork.livechat.LCEmotionItem;
import com.qpidnetwork.livechat.LCMessageItem;
import com.qpidnetwork.livechat.jni.LiveChatClientListener.LiveChatErrType;
import com.qpidnetwork.livechat.LiveChatManager;
import com.qpidnetwork.livechat.LiveChatManagerEmotionListener;
import com.qpidnetwork.request.item.OtherEmotionConfigItem;
import com.qpidnetwork.view.EmotionPlayer;
import com.qpidnetwork.view.MaterialDialogAlert;
import com.qpidnetwork.view.MaterialProgressBar;

/**
 * 高级表情3gp加载器
 * 
 * @author Hunter
 * 
 */
public class EmotionPlayImageDownloader implements
		LiveChatManagerEmotionListener {

	private LiveChatManager mLiveChatManager;
	private ImageView emotionDefault;
	private MaterialProgressBar pbDownload;
	private String emotionId;
	private EmotionPlayer emotionPlayer;
	private ImageButton btnError;
	private Context mContext;

	public EmotionPlayImageDownloader(Context context) {
		mLiveChatManager = LiveChatManager.newInstance(null);
		mContext = context;
	}

	public void displayEmotionPlayImage(EmotionPlayer emotionPlayer,
			ImageView emotionDef, MaterialProgressBar pbDownload,
			String emotionId, ImageButton btnError) {
		this.emotionDefault = emotionDef;
		this.pbDownload = pbDownload;
		this.emotionId = emotionId;
		this.emotionPlayer = emotionPlayer;
		this.btnError = btnError;

//		LCEmotionItem item = mLiveChatManager.GetEmotionInfo(emotionId);
		
		if(btnError != null){
			btnError.setVisibility(View.GONE);
		}
//		if ((item.playBigImages != null) && (item.playBigImages.size() > 0)) {
//			/*图片本地有，已拆分好，直接显示*/
//			emotionDef.setVisibility(View.GONE);
//			if(pbDownload != null){
//				pbDownload.setVisibility(View.GONE);
//			}
//			emotionPlayer.setVisibility(View.VISIBLE);
//			emotionPlayer.setImageList(item.playBigImages);
//			emotionPlayer.play();
//		} else {
			if(pbDownload != null){
				pbDownload.setVisibility(View.VISIBLE);
			}
			emotionDef.setVisibility(View.VISIBLE);
			emotionPlayer.setVisibility(View.GONE);
			mLiveChatManager.RegisterEmotionListener(this);
			boolean success = mLiveChatManager.GetEmotionPlayImage(emotionId);
			if(!success){
				if(pbDownload != null){
					pbDownload.setVisibility(View.GONE);
				}
				mLiveChatManager.UnregisterEmotionListener(this);
				onPlayImageDownloaderFailed();
			}
//		}
	}
	
	/**
	 * 下载失败按钮事件处理
	 */
	private void onPlayImageDownloaderFailed(){
		if(btnError != null){
			btnError.setVisibility(View.VISIBLE);
			btnError.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					/*下载失败，提示及重新下载*/
					MaterialDialogAlert dialog = new MaterialDialogAlert(mContext);
					dialog.setMessage(mContext.getString(R.string.livechat_download_photo_fail));
					dialog.addButton(dialog.createButton(mContext.getString(R.string.common_btn_retry), new OnClickListener() {
						
						@Override
						public void onClick(View v) {
							/* 文件不存在，下载文件 */
							if(pbDownload != null){
								pbDownload.setVisibility(View.VISIBLE);
							}
							btnError.setVisibility(View.GONE);
							emotionDefault.setVisibility(View.VISIBLE);
							emotionPlayer.setVisibility(View.GONE);
							mLiveChatManager.RegisterEmotionListener(EmotionPlayImageDownloader.this);
							boolean success = mLiveChatManager.GetEmotionPlayImage(emotionId);
//							if(!success){
//								if(pbDownload != null){
//									pbDownload.setVisibility(View.GONE);
//								}
//								mLiveChatManager.UnregisterEmotionListener(EmotionPlayImageDownloader.this);
//								onPlayImageDownloaderFailed();
//							}
						}
					}));
					dialog.addButton(dialog.createButton(mContext.getString(R.string.common_btn_cancel), null));
					
					dialog.show();
				}
			});
		}
	}
	
	private Handler handler = new Handler(){
		public void handleMessage(android.os.Message msg) {
			LCEmotionItem item = (LCEmotionItem)msg.obj;
			if(item.emotionId.equals(emotionId)){
				/*解除监听*/
				mLiveChatManager.UnregisterEmotionListener(EmotionPlayImageDownloader.this);
				
				/*是当前高表下载回调*/
				if(pbDownload != null){
					pbDownload.setVisibility(View.GONE);
				}
				if(msg.arg1 == 1){
					/*下载成功*/
					if ((item.playBigImages != null) && (item.playBigImages.size() > 0)) {
						/* 有缩略图，直接使用 */
						emotionDefault.setVisibility(View.GONE);
						emotionPlayer.setVisibility(View.VISIBLE);
						emotionPlayer.setImageList(item.playBigImages);
						emotionPlayer.play();
						return;
					}
				}
				onPlayImageDownloaderFailed();
			}
		}
	};

	@Override
	public void OnGetEmotionConfig(boolean success, String errno,
			String errmsg, OtherEmotionConfigItem item) {
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
	public void OnGetEmotionImage(boolean success, LCEmotionItem emotionItem) {
		// TODO Auto-generated method stub

	}

	@Override
	public void OnGetEmotionPlayImage(boolean success, LCEmotionItem emotionItem) {
		// TODO Auto-generated method stub
		Message msg = Message.obtain();
		msg.arg1 = success?1:0;
		msg.obj = emotionItem;
		handler.sendMessage(msg);
	}

	@Override
	public void OnGetEmotion3gp(boolean success, LCEmotionItem emotionItem) {
		// TODO Auto-generated method stub

	}

}
