package com.qpidnetwork.request.item;

public class EMFMsgTotalItem {	
	public EMFMsgTotalItem() {
	}
	 /**
	  * 
	  * @param msgTotal		邮件数量
	  */
	public EMFMsgTotalItem(
		 int msgTotal
		 	) {
		this.msgTotal = msgTotal;
	}
	
	public int msgTotal;
}
