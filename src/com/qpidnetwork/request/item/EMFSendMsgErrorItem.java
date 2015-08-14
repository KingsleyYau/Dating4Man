package com.qpidnetwork.request.item;

public class EMFSendMsgErrorItem {	
	public EMFSendMsgErrorItem() {
	}
	 /**
	  * 
	  * @param menoy	余额
	  */
	public EMFSendMsgErrorItem(
		 String money
		 	) {
		this.money = money;
	}
	
	public String money;
}
