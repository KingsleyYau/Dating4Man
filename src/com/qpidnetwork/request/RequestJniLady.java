package com.qpidnetwork.request;

import com.qpidnetwork.request.RequestEnum.Children;
import com.qpidnetwork.request.RequestEnum.Education;
import com.qpidnetwork.request.RequestEnum.Marry;

/**
 * 4.女士信息
 * @author Max.Chiu
 *
 */
public class RequestJniLady {
	/**
	 * 4.1.获取匹配女士条件
	 * @param callback			
	 * @return					请求唯一标识
	 */
	static public native long QueryLadyMatch(OnQueryLadyMatchCallback callback);
	
	/**
	 * 4.2.保存匹配女士条件
	 * @param ageRangeFrom		起始年龄
	 * @param ageRangeTo		结束年龄
	 * @param marry				婚姻状况,参考枚举 <RequestEnum.Marry>
	 * @param children			子女状况,参考枚举 <RequestEnum.Children>
	 * @param education			教育程度,参考枚举 <RequestEnum.Education>
	 * @param callback
	 * @return					请求唯一标识
	 */
	static public long SaveLadyMatch(int ageRangeFrom, int ageRangeTo, Children children, 
			Marry marry, Education education, OnRequestCallback callback) {
		return SaveLadyMatch(ageRangeFrom, ageRangeTo, children.ordinal(), marry.ordinal(), 
				education.ordinal(), callback);
	}
	static protected native long SaveLadyMatch(int ageRangeFrom, int ageRangeTo, int children, 
			int marry, int education, OnRequestCallback callback);
	
    /**
     * 4.3.条件查询女士列表，查询类型
     * @param DEFAULT		默认查询 
     * @param FAVOURITE		查询favorite
     * @param BYID			根据ID查询
     * @param BYCONDITION	根据匹配条件
     * @param WITHVIDEO		有视频的女士 
     * @param WITHPHONE		可以打电话（ver3.0起）
     * @param NEWEST		最新加入的女士（ver3.0起）
     */
    public enum SearchType {
    	DEFAULT,
    	FAVOURITE,	
    	BYID,
    	BYCONDITION,
    	WITHVIDEO,
    	WITHPHONE,
    	NEWEST
    }
    
    public enum OrderType {
    	DEFAULT(-1),
    	NEWST(0),
    	AGEUP(1),
    	AGEDOWN(2);
    	
    	private int value;
   	 
        private OrderType(int value) {
            this.value = value;
        }
 
        public int getValue() {
            return value;
        }
    }
    
    /**
     * 4.3.条件查询女士列表，在线类型
     * @param DEFAULT	默认
     * @param OFFLINE	离线
     * @param ONLINE	在线
     */
    public enum OnlineType {
    	DEFAULT(-1),
    	OFFLINE(0),
    	ONLINE(1);
    	
    	private int value;
    	 
        private OnlineType(int value) {
            this.value = value;
        }
 
        public int getValue() {
            return value;
        }
    }
    
    /**
     * 4.3.条件查询女士列表
     * @param pageIndex			当前页数
     * @param pageSize			每页行数
     * @param searchType		查询类型
     * @param womanId			女士ID(长度等于0：默认)
     * @param isOnline			是否在线(-1：默认，0：否，1：是)
     * @param ageRangeFrom		起始年龄(小于0：默认)
     * @param ageRangeTo		结束年龄(小于0：默认)
     * @param country			国家(长度等于0：默认)
     * @return					请求唯一标识
     */
    static public long QueryLadyList(
    		int pageIndex, 
    		int pageSize, 
    		SearchType searchType, 
    		String womanId, 
    		OnlineType isOnline, 
			int ageRangeFrom, 
			int ageRangeTo, 
			String country, 
			OrderType orderType,
			String deviceId,
			OnQueryLadyListCallback callback
			) {
    	return QueryLadyList(pageIndex, pageSize, searchType.ordinal()+1, womanId, isOnline.value,
    			ageRangeFrom, ageRangeTo, country, orderType.value, deviceId, callback);
    }
    static protected native long QueryLadyList(int pageIndex, int pageSize, int searchType, String womanId, 
			int isOnline, int ageRangeFrom, int ageRangeTo, String country, int orderType, String deviceId,
			OnQueryLadyListCallback callback);
    
    /**
     * 4.4.查询女士详细信息
     * @param womanId			女士ID
     * @param callback
     * @return					请求唯一标识
     */
    static public native long QueryLadyDetail(String womanId, OnQueryLadyDetailCallback callback);
    
    /**
     * 4.5.收藏女士
     * @param womanId			女士ID
     * @param callback
     * @return					请求唯一标识
     */
    static public native long AddFavouritesLady(String womanId, OnRequestCallback callback);
    
    /**
     * 4.6.删除收藏女士
     * @param womanId			女士ID
     * @param callback
     * @return					请求唯一标识
     */
    static public native long RemoveFavouritesLady(String womanId, OnRequestCallback callback);
    
    /**
     * 4.7.获取女士Direct Call TokenID
     * @param womanId			女士ID
     * @param callback
     * @return					请求唯一标识
     */
    static public native long QueryLadyCall(String womanId, OnQueryLadyCallCallback callback);
    
    /**
     * 获取最近联系人列表（ver3.0起）
     * @param callback
     * @return					请求唯一标识
     */
    static public native long RecentContact(OnLadyRecentContactListCallback callback);
    
    
    /**
     * 删除最近联系人列表（ver3.0.3起）
     * @param callback
     * @return					请求唯一标识
     */
    static public native long RemoveContactList(String[] womanIds,
    		OnRequestCallback callback);
    
    /**
     * 查询女士标签列表（ver3.0起）
     * @param womanId			女士ID
     * @param callback
     * @return					请求唯一标识
     */
    static public native long SignList(String womanId, OnLadySignListCallback callback);
    
    /**
     * 提交女士标签（ver3.0起）
     * @param womanId			女士ID
     * @param signIdArray		选中的标签ID列表
     * @param callback
     * @return					请求唯一标识
     */
    static public native long UploadSign(String womanId, String[] signIdArray, OnRequestCallback callback);
}
