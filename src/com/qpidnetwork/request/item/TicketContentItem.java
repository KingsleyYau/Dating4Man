package com.qpidnetwork.request.item;

/**
 * Ticket详情的内容item
 * @author Samson Fan
 *
 */
public class TicketContentItem {
	/**
	 * 信件类型定义
	 * @author Samson Fan
	 *
	 */
	public enum MethodType {
		/**
		 * 发送
		 */
		Send,
		/**
		 * 回复
		 */
		Receive,
	}
	
	public TicketContentItem()
	{
		
	}
	
	public TicketContentItem(
			int method,
			String fromName,
			String toName,
			int sendDate,
			String message,
			String[] fileList
			)
	{
		this.method = MethodType.values()[method];
		this.fromName = fromName;
		this.toName = toName;
		this.sendDate = sendDate;
		this.message = message;
		this.fileList = fileList;
	}
	
	/**
	 * 信件类型
	 */
	public MethodType method;
	/**
	 * 发送人名称
	 */
	public String fromName;
	/**
	 * 接收人名称
	 */
	public String toName;
	/**
	 * 发送时间
	 */
	public int sendDate;
	/**
	 * 内容
	 */
	public String message;
	/**
	 * 附件URL列表
	 */
	public String[] fileList;
}
