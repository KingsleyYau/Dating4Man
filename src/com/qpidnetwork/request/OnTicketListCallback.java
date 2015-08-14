package com.qpidnetwork.request;

import com.qpidnetwork.request.item.TicketListItem;

/**
 * Ticket模块的GetTicketList回调
 * @author Samson Fan
 *
 */
public interface OnTicketListCallback {
	public void OnTicketList(boolean isSuccess, String errno, String errmsg, int pageIndex, int pageSize, int dataCount, TicketListItem[] list);
}
