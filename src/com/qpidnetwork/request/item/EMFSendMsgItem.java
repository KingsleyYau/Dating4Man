package com.qpidnetwork.request.item;

public class EMFSendMsgItem {	
	public EMFSendMsgItem() {
	}
	 /**
	  * 
	  * @param id		邮件ID
	  * @param sendTime	发送时间
	  */
	public EMFSendMsgItem(
		 String id,
		 int sendTime
		 	) {
		this.id = id;
		this.sendTime = sendTime;
	}
	
	public String id;
	public int sendTime;
}
