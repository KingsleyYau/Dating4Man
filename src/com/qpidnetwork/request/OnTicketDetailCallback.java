package com.qpidnetwork.request;

import com.qpidnetwork.request.item.TicketDetailItem;

/**
 * Ticket模块的获取ticket详情回调
 * @author Samson Fan
 *
 */
public interface OnTicketDetailCallback {
	public void OnTicketDetail(boolean isSuccess, String errno, String errmsg, TicketDetailItem item);
}
