package com.qpidnetwork.request.item;

import com.qpidnetwork.request.item.TicketListItem.StatusType;

/**
 * Ticket详情item
 * @author Samson Fan
 *
 */
public class TicketDetailItem {

	public TicketDetailItem() 
	{
	
	}
	
	public TicketDetailItem(
			String title,
			int status,
			TicketContentItem[] contentList
			)
	{
		this.title = title;
		this.status = StatusType.values()[status];
		this.contentList = contentList;
	}
	
	/**
	 * 标题
	 */
	public String title;
	/**
	 * 状态
	 */
	public StatusType status;
	/**
	 * 内容列表
	 */
	public TicketContentItem[] contentList;
}
