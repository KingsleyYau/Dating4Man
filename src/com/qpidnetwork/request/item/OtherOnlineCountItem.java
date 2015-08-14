package com.qpidnetwork.request.item;

public class OtherOnlineCountItem {
	public OtherOnlineCountItem() {
		
	}

	/**
	 * 查询站点当前在线人数
	 * @param siteId		站点ID
	 * @param onlineCount	在线人数统计
	 */
	public OtherOnlineCountItem(
			int siteId,
			int onlineCount
			) 
	{
		this.site = siteId;
		this.onlineCount = onlineCount;
	}
	
	public int site;
	public int onlineCount;
}
