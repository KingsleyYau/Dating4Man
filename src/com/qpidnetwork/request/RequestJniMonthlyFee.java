package com.qpidnetwork.request;

public class RequestJniMonthlyFee {
	
	public enum MemberType {
		MEMBER_TYPE_START,
    	NORMAL_MEMBER,
    	FEED_MONTHLY_MEMBER,
    	NO_FEED_FIRST_MONTHLY_MEMBER,
    	NO_FEED_MONTHLY_MEMBER,
    	MEMBER_TYPE_END,
    }
	
	public static MemberType intToMemberType(int memberType){
		MemberType type = MemberType.NORMAL_MEMBER;
		if(memberType > MemberType.MEMBER_TYPE_START.ordinal() 
				&& memberType < MemberType.MEMBER_TYPE_END.ordinal()){
			type = MemberType.values()[memberType];
		}
		return type;
	}

	/**
	 * 13.1. 获取月费会员类型
	 * @param callback
	 * @return
	 */
	static protected native long QueryMemberType(OnQueryMemberTypeCallback callback);
	
	/**
	 * 13.2.获取月费提示层数据
	 * @param callback
	 * @return
	 */
	static public native long GetMonthlyFeeTips(OnGetMonthlyFeeTipsCallback callback);
}
