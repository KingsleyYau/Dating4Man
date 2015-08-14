package com.qpidnetwork.request;


/**
 * VideoShow模块接口
 * @author Samson.Fan
 *
 */
public class RequestJniVideoShow {
	/**
	 * 排序规则（NEWEST：按最新，HOTTEST：按最高点击率，UNKNOW：未定义类型）
	 */
	public enum OrderByType {
		NEWEST,
		HOTTEST,
		UNKNOW
	}
	
	/**
	 * VideoList（查询视频列表：/member/videoshow）
	 * @param pageIndex
	 * @param pageSize
	 * @param age1
	 * @param age2
	 * @param callback
	 * @return -1 fails, else success
	 */
	static public long VideoList(int pageIndex, int pageSize, int age1
			, int age2, OrderByType orderBy, OnVSVideoListCallback callback) {
		return VideoList(pageIndex, pageSize, age1, age2, orderBy.ordinal(), callback);
	}
	static protected native long VideoList(int pageIndex, int pageSize, int age1
			, int age2, int orderBy, OnVSVideoListCallback callback);
	
	/**
	 * VideoDetail（查询指定女士的视频信息：/member/video_detail）
	 * @param womanId
	 * @return -1 fails, else success
	 */
	static public native long VideoDetail(String womanId, OnVSVideoDetailCallback callback);
	
	/**
	 * PlayVideo（查询视频详细信息：/member/play_video）
	 * @param womanId
	 * @param videoId
	 * @return -1 fails, else success
	 */
	static public native long PlayVideo(String womanId, String videoId, OnVSPlayVideoCallback callback);
	
	/**
	 * WatchedVideoList（查询已看过的视频列表：/member/watched_video）
	 * @param pageIndex
	 * @param pageSize
	 * @return -1 fails, else success
	 */
	static public native long WatchedVideoList(int pageIndex, int pageSize, OnVSWatchedVideoListCallback callback);
	
	/**
	 * SaveVideo（收藏视频：/member/save_video）
	 * @param videoId
	 * @return -1 fails, else success
	 */
	static public native long SaveVideo(String videoId, OnVSSaveVideoCallback callback);
	
	/**
	 * RemoveVideo（删除收藏视频：/member/remove_video）
	 * @param videoId
	 * @return -1 fails, else success
	 */
	static public native long RemoveVideo(String videoId, OnVSRemoveVideoCallback callback);
	
	/**
	 * SavedVideoList（查询已收藏的视频列表：/member/saved_video）
	 * @param pageIndex
	 * @param pageSize
	 * @return -1 fails, else success
	 */
	static public native long SavedVideoList(int pageIndex, int pageSize, OnVSSavedVideoListCallback callback);
}
