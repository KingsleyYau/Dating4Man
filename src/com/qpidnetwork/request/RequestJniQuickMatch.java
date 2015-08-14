package com.qpidnetwork.request;

/**
 * 4.女士信息
 * @author Max.Chiu
 *
 */
public class RequestJniQuickMatch {
	/**
	 * 10.1.查询女士图片列表
	 * @param deviceId		设备唯一标识
	 * @param callback		
	 * @return				请求唯一标识
	 */
    static public native long QueryQuickMatchLadyList(
    		String deviceId, 
    		OnQueryQuickMatchLadyListCallback callback
    		);
    
    /**
     * 10.2.提交已标记的女士
     * @param likeList		喜爱的女士列表
     * @param unlikeList	不喜爱女士列表
     * @param callback
     * @return				请求唯一标识
     */
    static public native long SubmitQuickMatchMarkLadyList(
    		String[] likeListId,
    		String[] unlikeListId,
    		OnRequestCallback callback
    		);
    
	/**
	 * 10.3.查询已标记like的女士列表
	 * @param callback		
	 * @return				请求唯一标识
	 */
    static public native long QueryQuickMatchLikeLadyList(
    		int pageIndex, 
    		int pageSize, 
    		OnQueryQuickMatchLikeLadyListCallback callback
    		);
    
    /**
     * 10.4.删除已标记like的女士
     * @param likeListId	喜爱的女士列表
     * @param callback
     * @return				请求唯一标识
     */
    static public native long RemoveQuickMatchLikeLadyList(
    		String[] likeListId,
    		OnRequestCallback callback
    		);
}
