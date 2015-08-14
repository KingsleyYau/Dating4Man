package com.qpidnetwork.request;

/**
 * 11.Love Call
 * @author Max.Chiu
 *
 */
public class RequestJniLoveCall {
    /**
     * 11.1.条件查询Love士列表接口，查询类型
     * @param REQUEST		未确定
     * @param SCHEDULED		确定
     */
    public enum SearchType {
    	REQUEST,
    	SCHEDULED,	
    }
    
    /**
     * 11.1.获取Love Call列表接口
     * @param pageIndex			当前页数
     * @param pageSize			每页行数
     * @param searchType		条件查询Love士列表接口，查询类型<SearchType>
     * @param callback
     * @return					请求唯一标识
     */
    static public long QueryLoveCallList(int pageIndex, int pageSize, SearchType searchType, OnQueryLoveCallListCallback callback) {
    	return QueryLoveCallList(pageIndex, pageSize, searchType.ordinal(), callback);
    }
    static protected native long QueryLoveCallList(int pageIndex, int pageSize, int searchType,
    		OnQueryLoveCallListCallback callback);
    
    /**
     * 11.2.确定Love Call接口.确定类型
     * @param CONFIRM		接受
     * @param REJECT		拒绝
     */
    public enum ConfirmType {
    	REJECT,	
    	CONFIRM,
    }
    
    /**
     * 11.2.确定Love Call接口
     * @param orderId			订单ID
     * @param confirmType		确定类型
     * @param callback
     * @return					请求唯一标识
     */
    static public long ConfirmLoveCall(String orderId, ConfirmType confirmType, OnRequestCallback callback) {
    	return ConfirmLoveCall(orderId, confirmType.ordinal(), callback);
    }
    static protected native long ConfirmLoveCall(String orderId, int confirmType, OnRequestCallback callback);
    
    /**
     * 11.3.获取LoveCall未处理数接口
     * @param searchType		条件查询Love士列表接口，查询类型<SearchType>
     * @return
     */
    static public long QueryLoveCallRequestCount(SearchType searchType, OnQueryLoveCallRequestCountCallback callback) {
    	return QueryLoveCallRequestCount(searchType.ordinal(), callback);
    }
    static protected native long QueryLoveCallRequestCount(int searchType, OnQueryLoveCallRequestCountCallback callback);
}
