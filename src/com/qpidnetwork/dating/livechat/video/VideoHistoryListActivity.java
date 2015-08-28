package com.qpidnetwork.dating.livechat.video;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.qpidnetwork.dating.R;
import com.qpidnetwork.dating.authorization.LoginPerfence;
import com.qpidnetwork.dating.bean.RequestBaseResponse;
import com.qpidnetwork.dating.livechat.VideoPlayActivity;
import com.qpidnetwork.framework.base.BaseActionBarFragmentActivity;
import com.qpidnetwork.framework.util.StringUtil;
import com.qpidnetwork.framework.util.ToastUtil;
import com.qpidnetwork.livechat.LCMessageItem;
import com.qpidnetwork.livechat.LiveChatManager;
import com.qpidnetwork.livechat.LiveChatManagerVideoListener;
import com.qpidnetwork.livechat.jni.LiveChatClientListener.LiveChatErrType;
import com.qpidnetwork.request.OnQueryRecentVideoListCallback;
import com.qpidnetwork.request.RequestJniLiveChat.VideoPhotoType;
import com.qpidnetwork.request.RequestOperator;
import com.qpidnetwork.request.item.LCVideoItem;
import com.qpidnetwork.request.item.LoginItem;
import com.qpidnetwork.view.MaterialAppBar;
import com.qpidnetwork.view.MaterialProgressBar;

public class VideoHistoryListActivity extends BaseActionBarFragmentActivity
		implements LiveChatManagerVideoListener {

	private static final String VIDEO_HISTORY_TARGET_ID = "targetId";

	private static final int GET_VIDEO_THUMBPHTO_SUCCESS = 0;
	private static final int GET_VIDEO_START_NOTIFY = 1;
	private static final int GET_VIDEO_DOWNLOAD_FINISH = 2;
	private static final int GET_RECENT_WATCH_VIDEOLIST = 3;

	private String targetId = "";
	private GridView gdVideo;
	private MaterialProgressBar pbLoading;
	private TextView tvEmpty;
	//error page
	private LinearLayout llErrorPage;
	private Button btnErrorRetry;
	
	private VideoHistoryAdapter mVideoHistoryAdapter;

	private LiveChatManager mLiveChatManager;// video下载管理器

	public static void launchVideoHistoryActivity(Context context,
			String targetId) {
		if (!StringUtil.isEmpty(targetId)) {
			Intent intent = new Intent(context, VideoHistoryListActivity.class);
			intent.putExtra(VIDEO_HISTORY_TARGET_ID, targetId);
			context.startActivity(intent);
		}
	}

	@Override
	protected void onCreate(Bundle arg0) {
		// TODO Auto-generated method stub
		super.onCreate(arg0);
		setCustomContentView(R.layout.activity_livechat_video_history);

		Bundle bundle = getIntent().getExtras();
		if ((bundle != null) && (bundle.containsKey(VIDEO_HISTORY_TARGET_ID))) {
			targetId = bundle.getString(VIDEO_HISTORY_TARGET_ID);
		}

		/* init title */
		getCustomActionBar()
				.setTitle(getString(R.string.livechat_recent_watched_videos),
						getResources().getColor(R.color.text_color_dark));

		getCustomActionBar().setButtonIconById(R.id.common_button_back, R.drawable.ic_arrow_back_grey600_24dp);
		getCustomActionBar().getButtonById(R.id.common_button_back).setBackgroundResource(MaterialAppBar.TOUCH_FEEDBACK_HOLO_LIGHT);
		getCustomActionBar().setAppbarBackgroundColor(Color.WHITE);
		
		gdVideo = (GridView) findViewById(R.id.gdVideo);
		pbLoading = (MaterialProgressBar) findViewById(R.id.pbLoading);
		tvEmpty = (TextView) findViewById(R.id.tvEmpty);
		llErrorPage = (LinearLayout) findViewById(R.id.llErrorPage);
		btnErrorRetry = (Button) findViewById(R.id.btnErrorRetry);
		btnErrorRetry.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				/*获取最近看过视频列表*/
				getRecentWatchList();
			}
		});

		if (StringUtil.isEmpty(targetId)) {
			finish();
		} else {
			mLiveChatManager = LiveChatManager.newInstance(this);
			mLiveChatManager.RegisterVideoListener(this);
			mVideoHistoryAdapter = new VideoHistoryAdapter(this, targetId);
			gdVideo.setAdapter(mVideoHistoryAdapter);
			gdVideo.setOnItemClickListener(new OnItemClickListener() {

				@Override
				public void onItemClick(AdapterView<?> parent, View view,
						int position, long id) {
					//Item 点击响应
					LCVideoItem item = mVideoHistoryAdapter.getDataList().get(position);
					downloadVideo(position, item);
				}
				
			});
			
			/*获取最近看过视频列表*/
			getRecentWatchList();
		}
	}
	
	/**
	 * 开始下载
	 */
	private void refreshForDownloadStart(){
		pbLoading.setVisibility(View.VISIBLE);
		gdVideo.setVisibility(View.GONE);
		llErrorPage.setVisibility(View.GONE);
		tvEmpty.setVisibility(View.GONE);
	}
	
	/**
	 * 下载失败
	 */
	private void refreshForDownloadError(){
		pbLoading.setVisibility(View.GONE);
		gdVideo.setVisibility(View.GONE);
		llErrorPage.setVisibility(View.VISIBLE);
		tvEmpty.setVisibility(View.GONE);
	}
	
	/**
	 * 下载成功无数据
	 */
	private void refreshForNoData(){
		pbLoading.setVisibility(View.GONE);
		gdVideo.setVisibility(View.GONE);
		llErrorPage.setVisibility(View.GONE);
		tvEmpty.setVisibility(View.VISIBLE);
	}
	
	/**
	 * 下载成功，视频列表不为空
	 */
	private void refreshForDataUpdate(){
		pbLoading.setVisibility(View.GONE);
		gdVideo.setVisibility(View.VISIBLE);
		llErrorPage.setVisibility(View.GONE);
		tvEmpty.setVisibility(View.GONE);
	}
	
	/**
	 * 点击下载Video
	 * @param item
	 */
	private void downloadVideo(int position, LCVideoItem item){
		String videoThumbPath = mLiveChatManager.GetVideoPhotoPathWithExist(targetId, item.inviteid, item.videoid, VideoPhotoType.Default);
		if(StringUtil.isEmpty(videoThumbPath)){
			//无缩略图下载
			mLiveChatManager.GetVideoPhoto(targetId, item, VideoPhotoType.Default);
		}
		
		String videoLocalPath = mLiveChatManager.GetVideoPathWithExist(targetId, item.inviteid, item.videoid);
		if(StringUtil.isEmpty(videoLocalPath)){
			//无视频，下载视频
			mLiveChatManager.GetVideo(targetId, item);
			updateVideoDownloadItem(position, item.inviteid, item.videoid);
		}else{
			VideoPlayActivity.launchVideoPlayActivity(this, videoThumbPath, videoLocalPath);
		}
	}
	
	/**
	 * 获取最近视频列表
	 */
	private void getRecentWatchList(){
		LoginItem item = LoginPerfence.GetLoginParam(this).item;
		if(item != null){
			refreshForDownloadStart();
			RequestOperator.getInstance().QueryRecentVideo(item.sessionid, item.manid, targetId, new OnQueryRecentVideoListCallback() {
				
				@Override
				public void OnQueryRecentVideoList(boolean isSuccess, String errno,
						String errmsg, LCVideoItem[] itemList) {
					Message msg = Message.obtain();
					msg.what = GET_RECENT_WATCH_VIDEOLIST;
					msg.obj = new RequestBaseResponse(isSuccess, errno, errmsg, itemList);
					sendUiMessage(msg);
				}
			});
		}
	}


	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		if (mLiveChatManager != null) {
			mLiveChatManager.UnregisterVideoListener(this);
		}
	}

	@Override
	protected void handleUiMessage(Message msg) {
		super.handleUiMessage(msg);
		switch (msg.what) {
		case GET_RECENT_WATCH_VIDEOLIST: {
			RequestBaseResponse requestResponse = (RequestBaseResponse) msg.obj;
			if(requestResponse.isSuccess){
				if(requestResponse.body != null){
					LCVideoItem[] dataList = (LCVideoItem[])requestResponse.body;
					if(dataList.length > 0 ){
						refreshForDataUpdate();
						mVideoHistoryAdapter.appendList((LCVideoItem[])requestResponse.body);
					}else{
						refreshForNoData();
					}
				}else{
					/*无数据处理*/
					refreshForNoData();
				}
			}else{
				refreshForDownloadError();
				ToastUtil.showToast(this, requestResponse.errmsg);
			}
		}
		break;
		
		case GET_VIDEO_THUMBPHTO_SUCCESS: {
			MessageVideoPhotoItem item = (MessageVideoPhotoItem) msg.obj;
			updateVideoThumbPhoto(item);
		}
		break;
		
		case GET_VIDEO_START_NOTIFY:
		case GET_VIDEO_DOWNLOAD_FINISH: {
			MessageVideoDownloadObject videoDownloadItem = (MessageVideoDownloadObject) msg.obj;
			updateVideoDownloadStatus(videoDownloadItem);
		}
		break;
		
		default:
			break;
		}
	}

	/**
	 * 下载Video 状态更新
	 * 
	 * @param item
	 */
	private void updateVideoDownloadStatus(MessageVideoDownloadObject item) {
		List<LCVideoItem> dataList = mVideoHistoryAdapter.getDataList();
		for (int i = 0; i < dataList.size(); i++) {
			LCVideoItem videoItem = dataList.get(i);
			if ((videoItem != null)
					&& (videoItem.inviteid.equals(item.inviteId))
					&& (videoItem.videoid.equals(item.videoId))) {
				updateVideoDownloadItem(i, item.inviteId, item.videoId);
			}
		}
	}

	/**
	 * 更新指定Item 的video状态
	 * 
	 * @param position
	 * @param item
	 */
	private void updateVideoDownloadItem(int position, String inviteId,
			String videoId) {
		/* 更新单个Item */
		View childAt = gdVideo.getChildAt(position
				- gdVideo.getFirstVisiblePosition());
		if (childAt != null) {
			
			String videoLocalPath = mLiveChatManager.GetVideoPathWithExist(targetId, inviteId, videoId);
			LivechatVideoItem videoItem = ((LivechatVideoItem) childAt.findViewById(R.id.videoItem));
			if(StringUtil.isEmpty(videoLocalPath)){
				//本地无
				if(mLiveChatManager.isGetVideoNow(videoId)){
					//正在下载过程中
					videoItem.updateForDownloading();
				}else{
					videoItem.updateForDefault();
				}
			}else{
				//已经下载成功，本地已经存在
				videoItem.updateForPlay();
			}
		}
	}

	/**
	 * 下载Thumb photo成功更新指定Item显示
	 * 
	 * @param item
	 */
	private void updateVideoThumbPhoto(MessageVideoPhotoItem item) {
		List<LCVideoItem> dataList = mVideoHistoryAdapter.getDataList();
		for (int i = 0; i < dataList.size(); i++) {
			LCVideoItem videoItem = dataList.get(i);
			if ((videoItem != null)
					&& (videoItem.inviteid.equals(item.inviteId))
					&& (videoItem.videoid.equals(item.videoId))) {
				updateThumbPhotoItem(i, item.filePath);
			}
		}
	}

	/**
	 * 更新指定Item thumb 图片
	 * 
	 * @param position
	 * @param filePath
	 */
	private void updateThumbPhotoItem(int position, String filePath) {
		/* 更新单个Item */
		View childAt = gdVideo.getChildAt(position
				- gdVideo.getFirstVisiblePosition());
		if (childAt != null) {
			if (!StringUtil.isEmpty(filePath)) {
				((LivechatVideoItem) childAt.findViewById(R.id.videoItem))
						.setVideoThumb(filePath);
			}
		}
	}

	@Override
	public void OnGetVideoPhoto(LiveChatErrType errType, String errno,
			String errmsg, String userId, String inviteId, String videoId,
			VideoPhotoType type, String filePath,
			ArrayList<LCMessageItem> msgList) {
		if ((errType == LiveChatErrType.Success) && !StringUtil.isEmpty(userId)
				&& (userId.equals(targetId))) {
			// 是本人视频
			Message msg = Message.obtain();
			msg.what = GET_VIDEO_THUMBPHTO_SUCCESS;
			msg.obj = new MessageVideoPhotoItem(userId, inviteId, videoId,
					type, filePath, msgList);
			sendUiMessage(msg);
		}
	}

	@Override
	public void OnVideoFee(boolean success, String errno, String errmsg,
			LCMessageItem item) {

	}

	@Override
	public void OnStartGetVideo(String userId, String videoId, String inviteId,
			String videoPath, ArrayList<LCMessageItem> msgList) {
		if (!StringUtil.isEmpty(userId) && (userId.equals(targetId))) {
			// 是本人视频
			Message msg = Message.obtain();
			msg.what = GET_VIDEO_START_NOTIFY;
			msg.obj = new MessageVideoDownloadObject(LiveChatErrType.Success,
					userId, videoId, inviteId, msgList);
			sendUiMessage(msg);
		}
	}

	@Override
	public void OnGetVideo(LiveChatErrType errType, String userId,
			String videoId, String inviteId, String videoPath,
			ArrayList<LCMessageItem> msgList) {
		if (!StringUtil.isEmpty(userId) && (userId.equals(targetId))) {
			// 是本人视频
			Message msg = Message.obtain();
			msg.what = GET_VIDEO_DOWNLOAD_FINISH;
			msg.obj = new MessageVideoDownloadObject(errType, userId, videoId,
					inviteId, msgList);
			sendUiMessage(msg);
		}
	}

	@Override
	public void OnRecvVideo(LCMessageItem item) {

	}

}
