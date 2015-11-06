package com.qpidnetwork.dating.emf;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.content.Context;

import com.qpidnetwork.manager.FileCacheManager;
import com.qpidnetwork.request.OnRequestFileCallback;
import com.qpidnetwork.request.RequestJni;
import com.qpidnetwork.request.RequestJniLiveChat.VideoPhotoType;
import com.qpidnetwork.request.RequestOperator;
import com.qpidnetwork.tool.Arithmetic;

/**
 * 主要用于处理EMF Video相关的逻辑调用
 * 
 * @author Hunter 2015.9.29
 */
public class EMFVideoManager {

	private Context mContext;

	private static EMFVideoManager mEMFVideoManager;

	/**
	 * 正在下载视频map表(videoId, RequestId)
	 */
	private HashMap<String, Long> mVideoPhotoRequestMap;
	/**
	 * 多个video thumb photo同时下载用于存放回调列表
	 */
	private HashMap<String, List<OnRequestFileCallback>> mVideoPhotoCallBackMap;
	/**
	 * 正在下载视频map表(videoId, EMFVideoDownloadTask)
	 */
	private HashMap<String, EMFVideoDownloadTask> mVideoUrlRequestMap;
	/**
	 * 多个video 同时下载时存放回调列表
	 */
	private HashMap<String, List<OnEmfVideoDownloadCallback>> mVideoUrlCallBackMap;

	/**
	 * 本地缓存文件目录
	 */
	private String mDirPath = "";

	private EMFVideoManager(Context context) {
		this.mContext = context;
		// 保存videoId 和 requestId串，防止重复下载
		mVideoPhotoRequestMap = new HashMap<String, Long>();
		mVideoPhotoCallBackMap = new HashMap<String, List<OnRequestFileCallback>>();
		//视频下载存储
		mVideoUrlRequestMap = new HashMap<String, EMFVideoDownloadTask>();
		mVideoUrlCallBackMap = new HashMap<String, List<OnEmfVideoDownloadCallback>>();

		initDirPath();
	}

	public static EMFVideoManager newInstance(Context context) {
		if (mEMFVideoManager == null) {
			mEMFVideoManager = new EMFVideoManager(context);
		}
		return mEMFVideoManager;
	}

	/**
	 * 初始化
	 * 
	 * @param dirPath
	 *            文件存放目录
	 * @return
	 */
	private boolean initDirPath() {
		mDirPath = FileCacheManager.getInstance().GetEMFVideoPath();
		;
		if (!mDirPath.isEmpty()
				&& !mDirPath.regionMatches(mDirPath.length() - 1, "/", 0, 1)) {
			mDirPath += "/";
		}
		return !mDirPath.isEmpty();
	}

	public EMFVideoManager getInstance() {
		return mEMFVideoManager;
	}

	/**
	 * 获取指定视频的ThumbPhoto
	 * 
	 * @param womanid
	 * @param send_id
	 * @param video_id
	 * @param messageId
	 * @param type
	 * @param filePath
	 * @param callback
	 */
	public void GetVideoThumbPhoto(final String womanid, final String send_id,
			final String video_id, final String messageId, final VideoPhotoType type,
			OnRequestFileCallback callback) {
		// callback 插入回调列表，统一处理
		insertIntoVideoPhotoCallbackList(video_id, callback);
		if (!isVideoPhotoRequest(video_id)) {
			String filePath = getVideoThumbPhotoTempPath(womanid, send_id,
					video_id, messageId, type);
			long requestId = RequestOperator.getInstance().GetVideoThumbPhoto(
					womanid, send_id, video_id, messageId, type, filePath,
					new OnRequestFileCallback() {

						@Override
						public void OnRequestFile(long requestId,
								boolean isSuccess, String errno, String errmsg,
								String filePath) {
							String desPath = "";
							boolean result = false;
							// 下载成功
							if (isSuccess) {
								// 把临时文件复制到正式文件目录 
								desPath = getVideoThumbPhotoPath(womanid, send_id, video_id, messageId, type);
								result = tempFileToDesFile(filePath, desPath);
							}
							//修改下载中状态为默认状态
							removeVideoFromPhotoRequestMap(video_id);
							
							//callback
							synchronized (mVideoPhotoCallBackMap) {
								if(mVideoPhotoCallBackMap.containsKey(video_id)){
									List<OnRequestFileCallback> callbackList = mVideoPhotoCallBackMap.get(video_id);
									for(OnRequestFileCallback callback : callbackList){
										callback.OnRequestFile(requestId, result, errno, errmsg, desPath);
									}
									callbackList.clear();
									mVideoPhotoCallBackMap.remove(video_id);
								}
							}
							
						}
					});
			if (requestId != RequestJni.InvalidRequestId) {
				addToVideoPhotoRequestMap(requestId, video_id);
			}
		}
	}
	
	/**
	 * 购买并下载视频
	 * @param womanId
	 * @param send_id
	 * @param video_id
	 * @param message_id
	 * @param callback
	 */
	public void getVideo(String womanId, String send_id, final String video_id, String message_id, OnEmfVideoDownloadCallback callback){
		boolean isDownloading = false;
		synchronized (mVideoUrlRequestMap) {
			if(mVideoUrlRequestMap.containsKey(video_id)){
				isDownloading = true;
			}
		}
		insertIntoVideoDownloadCallbackList(video_id, callback);
		if(!isDownloading){
			EMFVideoDownloadTask task = new EMFVideoDownloadTask(this);
			addToVideoUrlRequestMap(video_id, task);
			task.executeVideoDownload(womanId, send_id, video_id, message_id, new OnEmfVideoDownloadCallback() {
				
				@Override
				public void onEmfVideoDownload(boolean isSuccess, String errno,
						String errmsg, String videoUrl) {
					//下载完成，清除正在下载标志
					removeVideoFromUrlRequestMap(video_id);
					//callback
					synchronized (mVideoUrlCallBackMap) {
						if(mVideoUrlCallBackMap.containsKey(video_id)){
							List<OnEmfVideoDownloadCallback> callbackList = mVideoUrlCallBackMap.get(video_id);
							for(OnEmfVideoDownloadCallback callback : callbackList){
								callback.onEmfVideoDownload(isSuccess, errno, errmsg, videoUrl);
							}
							callbackList.clear();
							mVideoUrlCallBackMap.remove(video_id);
						}
					}
				}
				
				@Override
				public void onEMfFeeCallback(boolean isFeed) {
					synchronized (mVideoUrlCallBackMap) {
						if(mVideoUrlCallBackMap.containsKey(video_id)){
							List<OnEmfVideoDownloadCallback> callbackList = mVideoUrlCallBackMap.get(video_id);
							for(OnEmfVideoDownloadCallback callback : callbackList){
								callback.onEMfFeeCallback(true);
							}
						}
					}
				}
			});
		}
	}

	/**
	 * 把获取video thumb photo 回调插入列表，统一请处理请求回调
	 * 
	 * @param video_id
	 * @param callback
	 */
	private void insertIntoVideoPhotoCallbackList(String video_id,
			OnRequestFileCallback callback) {
		synchronized (mVideoPhotoCallBackMap) {
			List<OnRequestFileCallback> callbackList = null;
			if (mVideoPhotoCallBackMap.containsKey(video_id)) {
				callbackList = mVideoPhotoCallBackMap.get(video_id);
				callbackList.add(callback);
			} else {
				callbackList = new ArrayList<OnRequestFileCallback>();
				callbackList.add(callback);
				mVideoPhotoCallBackMap.put(video_id, callbackList);
			}
		}
	}

	/**
	 * 添加video到 thumb photo下载列表
	 * 
	 * @param requestId
	 * @param videoId
	 */
	private void addToVideoPhotoRequestMap(long requestId, String videoId) {
		synchronized (mVideoPhotoRequestMap) {
			mVideoPhotoRequestMap.put(videoId, requestId);
		}
	}

	/**
	 * 下载Thumb photo返回，清除正在下载标志
	 * 
	 * @param videoId
	 */
	private void removeVideoFromPhotoRequestMap(String videoId) {
		synchronized (mVideoPhotoRequestMap) {
			mVideoPhotoRequestMap.remove(videoId);
		}
	}
	
	/**
	 * 把获取video download回调插入列表，统一请处理请求回调
	 * @param video_id
	 * @param callback
	 */
	private void insertIntoVideoDownloadCallbackList(String video_id,
			OnEmfVideoDownloadCallback callback) {
		synchronized (mVideoUrlCallBackMap) {
			List<OnEmfVideoDownloadCallback> callbackList = null;
			if (mVideoUrlCallBackMap.containsKey(video_id)) {
				callbackList = mVideoUrlCallBackMap.get(video_id);
				callbackList.add(callback);
			} else {
				callbackList = new ArrayList<OnEmfVideoDownloadCallback>();
				callbackList.add(callback);
				mVideoUrlCallBackMap.put(video_id, callbackList);
			}
		}
	}

	/**
	 * 添加video到 video视频下载列表
	 * 
	 * @param requestId
	 * @param videoId
	 */
	private void addToVideoUrlRequestMap(String videoId, EMFVideoDownloadTask downloadTask) {
		synchronized (mVideoUrlRequestMap) {
			mVideoUrlRequestMap.put(videoId, downloadTask);
		}
	}

	/**
	 * 下载微视频返回，清除正在下载标志
	 * 
	 * @param videoId
	 */
	private void removeVideoFromUrlRequestMap(String videoId) {
		synchronized (mVideoUrlRequestMap) {
			mVideoUrlRequestMap.remove(videoId);
		}
	}

	/**
	 * 判断视频图片是否在下载
	 * 
	 * @param videoId
	 *            视频ID
	 * @return
	 */
	private boolean isVideoPhotoRequest(String videoId) {
		return getRequestIdWithVideoPhotoId(videoId) != RequestJni.InvalidRequestId;
	}

	/**
	 * 获取正在下载的视频图片RequestId
	 * 
	 * @param videoId
	 *            视频ID
	 * @return 请求ID
	 */
	private long getRequestIdWithVideoPhotoId(String videoId) {
		long requestId = RequestJni.InvalidRequestId;
		synchronized (mVideoPhotoRequestMap) {
			Long result = mVideoPhotoRequestMap.get(videoId);
			if (null != result) {
				requestId = result;
			}
		}
		return requestId;
	}

	// --------------------------- 获取视频本地缓存路径 -------------------------
	
	/**
	 * 获取视频图片临时文件路径
	 * @param userId	用户ID
	 * @param videoId	视频ID
	 * @param inviteId	邀请ID
	 * @param type		视频图片类型
	 * @return
	 */
	public String getVideoThumbPhotoTempPath(String womanid, String send_id,
			String video_id, String messageId, VideoPhotoType type) 
	{
		String tempPath = "";
		String path = getVideoThumbPhotoPath(womanid, send_id, video_id, messageId, type);
		if (!path.isEmpty()) 
		{
			tempPath = path + "_temp";
		}
		return tempPath;
	}

	/**
	 * 获取Video thumb photo 临时缓存路径
	 * @param womanid
	 * @param send_id
	 * @param video_id
	 * @param messageId
	 * @param type
	 * @return
	 */
	public String getVideoThumbPhotoPath(String womanid, String send_id,
			String video_id, String messageId, VideoPhotoType type) {
		String path = "";
		if (!womanid.isEmpty() && !send_id.isEmpty() && !video_id.isEmpty()
				&& !messageId.isEmpty()) {
			// 生成文件名
			String temp = womanid + send_id + video_id + messageId;
			String fileName = Arithmetic.MD5(temp.getBytes(),
					temp.getBytes().length);

			// 生成文件全路径
			path = mDirPath + fileName + "_" + "img" + "_" + type.name();
		}
		return path;
	}
	
	/**
	 * 获取视频临时文件路径
	 * @param item		消息item
	 * @return
	 */
	public String getVideoTempPath(String womanid, String send_id, String video_id, String messageId) {
		String tempPath = "";
		String path = getVideoPath(womanid, send_id, video_id, messageId);
		if (!path.isEmpty()) {
			tempPath = path + "_temp";
		}
		return tempPath;
	}
	
	/**
	 * 获取视频本地缓存文件路径(全路径)
	 * @param userId	用户ID
	 * @param videoId	视频ID
	 * @param inviteId	邀请ID
	 * @return
	 */
	public String getVideoPath(String womanid, String send_id, String video_id, String messageId) {
		String path = "";
		if (!womanid.isEmpty()
			&& !send_id.isEmpty()
			&& !video_id.isEmpty()
			&& !messageId.isEmpty()) {
			// 生成文件名
			String temp = womanid + send_id + video_id + messageId;
			String fileName = Arithmetic.MD5(temp.getBytes(), temp.getBytes().length);
			
			// 生成文件全路径 
			path = mDirPath + fileName;
		}
		return path;
	}
	
	/**
	 * 下载完成的临时文件转换成正式文件
	 * @param tempFile	临时文件路径
	 * @param desFile		正式文件路径  
	 * @return
	 */
	public boolean tempFileToDesFile(String tempPath, String desPath) 
	{
		boolean result = false;
		if (null != tempPath && !tempPath.isEmpty()
				&& null != desPath && !desPath.isEmpty())
		{
			File tempFile = new File(tempPath);
			File newFile = new File(desPath);
			if (tempFile.exists() 
				&& tempFile.isFile()
				&& tempFile.renameTo(newFile)) 
			{
				result = true;
			}
		}
		return result;
	}
	
	/**
	 * 视频是否正在下载中
	 * @param videoId
	 */
	public boolean isGetVideoNow(String videoId){
		boolean isDownloading = false;
		synchronized (mVideoUrlRequestMap) {
			if(mVideoUrlRequestMap.containsKey(videoId)){
				isDownloading = true;
			}
		}
		return isDownloading;
	}
	
	public interface OnEmfVideoDownloadCallback{
		public void onEmfVideoDownload(boolean isSuccess, String errno, String errmsg, String videoUrl);
		public void onEMfFeeCallback(boolean isFeed);
	}
}
