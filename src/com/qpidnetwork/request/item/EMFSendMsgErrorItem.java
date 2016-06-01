package com.qpidnetwork.request.item;

import com.qpidnetwork.request.RequestJniMonthlyFee;
import com.qpidnetwork.request.RequestJniMonthlyFee.MemberType;

public class EMFSendMsgErrorItem {	
	public EMFSendMsgErrorItem() {
	}
	 /**
	  * 
	  * @param menoy	余额
	  */
	public EMFSendMsgErrorItem(
		 String money
		 ,int memberType
		 	) {
		this.money = money;
		this.memberType = RequestJniMonthlyFee.intToMemberType(memberType);
		
	}
	
	public String money;
	public MemberType memberType;
}
