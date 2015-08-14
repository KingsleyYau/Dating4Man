package com.qpidnetwork.request.item;

/**
 * Ticket列表item
 * @author Samson Fan
 *
 */
public class TicketListItem {
	/**
	 * ticket状态
	 * @author Samson Fan
	 *
	 */
	public enum StatusType {
		/**
		 * 打开中
		 */
		Open,
		/**
		 * 系统关闭（已完成）
		 */
		SystemClose,
		/**
		 * 男士关闭（已完成）
		 */
		UserClose,
	}
	
	public TicketListItem()
	{
		
	}
	
	public TicketListItem(
			String ticketId,
			String title,
			int unreadNum,
			int status,
			int addDate
			)
	{
		this.ticketId = ticketId;
		this.title = title;
		this.unreadNum = unreadNum;
		this.status = StatusType.values()[status];
		this.addDate = addDate;
	}
	
	/**
	 * ticket ID
	 */
	public String ticketId;
	/**
	 * ticket标题
	 */
	public String title;
	/**
	 * ticket的回复未读数
	 */
	public int unreadNum;
	/**
	 * ticket状态
	 */
	public StatusType status;
	/**
	 * ticket提交时间
	 */
	public int addDate;
	
	
	@Override
	public boolean equals(Object o) {
		if((o != null)&&(o instanceof TicketListItem)){
			TicketListItem object = (TicketListItem)o;
			return (object.ticketId.equals(ticketId));
		}
		return super.equals(o);
	}
}
