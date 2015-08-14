package com.qpidnetwork.request;

/**
 * Contact Us模块接口
 * @author Samson Fan
 *
 */
public class RequestJniTicket {
	/**
	 * 获取ticket列表
	 * @param pageIndex	当前页数
	 * @param pageSize	每页行数
	 * @param callback	回调
	 * @return -1 fails, else success
	 */
	static public native long TicketList(int pageIndex, int pageSize, OnTicketListCallback callback);
	
	/**
	 * 获取ticket详情
	 * @param ticketId	ticket的id
	 * @param callback	回调
	 * @return -1 fails, else success
	 */
	static public native long TicketDetail(String ticketId, OnTicketDetailCallback callback);
	
	/**
	 * 回复ticket
	 * @param ticketId	ticket的id
	 * @param message	回复内容
	 * @param filePath	附件路径
	 * @return -1 fails, else success
	 */
	static public native long ReplyTicket(String ticketId, String message, String filePath, OnRequestCallback callback);
	
	/**
	 * 设置ticket已经解决
	 * @param ticketId	ticket的id
	 * @return
	 */
	static public native long ResolvedTicket(String ticketId, OnRequestCallback callback);
	
	// ticket 类型定义
	static public final int TicketTypeConsultation = 0;
	static public final int TicketTypeComplaint = 1;
	static public final int TicketTypeSuggestion = 2;
	static public final int TicketTypeTechnicalSupport = 3;
	static public final int TicketTypeBillingIssue = 5;
	static public final int TicketTypeOther = 20;
	/**
	 * 新建ticket
	 * @param typeId	类型ID
	 * @param title		标题
	 * @param message	内容
	 * @param filePath	附件路径
	 * @return
	 */
	static public native long AddTicket(int typeId, String title, String message, String filePath, OnRequestCallback callback);
}
