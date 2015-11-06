package com.qpidnetwork.dating.emf;

import android.os.Handler;
import android.os.Message;

import com.qpidnetwork.dating.bean.RequestBaseResponse;
import com.qpidnetwork.dating.emf.EMFVideoManager.OnEmfVideoDownloadCallback;
import com.qpidnetwork.request.OnGetVideoCallback;
import com.qpidnetwork.request.RequestOperator;
import com.qpidnetwork.tool.FileDownloader;

/**
 * EMF Video 购买和下载统一处理
 * 
 * @author Hunter 2015.9.30
 */
public class EMFVideoDownloadTask {

	private static final int GET_VIDEOURL_CALLBACK = 0;
	private static final int GET_VIDEO_CALLBACK = 1;

	private Handler mHandler;
	private EMFVideoManager mEmfVideoManager;
	private OnEmfVideoDownloadCallback mOnEmfVideoDownloadCallback;
	
	private String womanId;
	private String send_id;
	private String video_id;
	private String message_id;

	public EMFVideoDownloadTask(EMFVideoManager manager) {
		this.mEmfVideoManager = manager;
		mHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				RequestBaseResponse response = (RequestBaseResponse)msg.obj;
				if(!response.isSuccess){
					if(mOnEmfVideoDownloadCallback != null){
						mOnEmfVideoDownloadCallback.onEmfVideoDownload(response.isSuccess, response.errno, response.errmsg, "");
					}
				}else{
					switch (msg.what) {
					case GET_VIDEOURL_CALLBACK:
						String videoRemoteUrl = (String)response.body;
						if(mOnEmfVideoDownloadCallback != null){
							mOnEmfVideoDownloadCallback.onEMfFeeCallback(true);
						}
						GetVideo(videoRemoteUrl);
						break;
					case GET_VIDEO_CALLBACK:
						String videoLocalUrl = (String)response.body;
						if(mOnEmfVideoDownloadCallback != null){
							mOnEmfVideoDownloadCallback.onEmfVideoDownload(response.isSuccess, response.errno, response.errmsg, videoLocalUrl);
						}
						break;
					default:
						break;
					}
				}
			}
		};
	}

	/**
	 * 启动下载
	 * @param womanId
	 * @param send_id
	 * @param video_id
	 * @param message_id
	 * @param callback
	 */
	public void executeVideoDownload(String womanId, String send_id, String video_id, String message_id, OnEmfVideoDownloadCallback callback) {
		this.womanId = womanId;
		this.send_id = send_id;
		this.video_id = video_id;
		this.message_id = message_id;
		this.mOnEmfVideoDownloadCallback = callback;
		getVideoUrl();
	}

	/**
	 * 购买或获取视频Url
	 * 
	 * @param womanId
	 * @param send_id
	 * @param video_id
	 * @param message_id
	 */
	private void getVideoUrl() {
		RequestOperator.getInstance().GetVideoUrl(womanId, send_id, video_id,
				message_id, new OnGetVideoCallback() {

					@Override
					public void OnLCGetVideo(long requestId, boolean isSuccess,
							String errno, String errmsg, String url) {
						RequestBaseResponse response = new RequestBaseResponse(
								isSuccess, errno, errmsg, url);
						Message msg = Message.obtain();
						msg.what = GET_VIDEOURL_CALLBACK;
						msg.obj = response;
						mHandler.sendMessage(msg);
					}
				});
	}

	/**
	 * 下载指定url视频文件
	 * 
	 * @param womanId
	 * @param send_id
	 * @param video_id
	 * @param message_id
	 * @param videoUrl
	 */
	private void GetVideo(String videoUrl) {
		final String tempPath = mEmfVideoManager.getVideoTempPath(womanId,
				send_id, video_id, message_id);
		FileDownloader fileDownloader = new FileDownloader();
		fileDownloader.SetBigFile(true);
		fileDownloader.SetUseCache(false);
		fileDownloader.StartDownload(videoUrl, tempPath,
				new FileDownloader.FileDownloaderCallback() {

					@Override
					public void onUpdate(FileDownloader loader, int progress) {

					}

					@Override
					public void onSuccess(FileDownloader loader) {
						// TODO Auto-generated method stub
						boolean result = false;
						// 把临时文件复制到正式文件目录
						String videoPath = mEmfVideoManager.getVideoPath(womanId,
								send_id, video_id, message_id);
						result = mEmfVideoManager.tempFileToDesFile(tempPath,
								videoPath);
						Message msg = Message.obtain();
						msg.what = GET_VIDEO_CALLBACK;
						RequestBaseResponse response = new RequestBaseResponse(result, "", "", videoPath);
						msg.obj = response;
						mHandler.sendMessage(msg);
					}

					@Override
					public void onFail(FileDownloader loader) {
						Message msg = Message.obtain();
						msg.what = GET_VIDEO_CALLBACK;
						RequestBaseResponse response = new RequestBaseResponse(false, "", "", "");
						msg.obj = response;
						mHandler.sendMessage(msg);
					}
				});
	}
	
	
}
