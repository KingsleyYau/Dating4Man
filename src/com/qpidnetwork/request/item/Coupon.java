package com.qpidnetwork.request.item;


public class Coupon {
	public Coupon() {
		
	}

	/**
	 * 5.1.查询是否符合试聊条件回调
	 * @param status			试聊状态
	 */
	public Coupon(
			String userId,
			int status
			) 
	{
		if( status < 0 || status >= CouponStatus.values().length ) {
			this.status = CouponStatus.values()[0];
		} else {
			this.status = CouponStatus.values()[status];
		}
		this.userId = userId;
	}
	
	public enum CouponStatus {
		Used,		// 已聊过
		None,		// 不能使用
		Yes,		// 可以使用
		Started,	// 已开始使用
		Promotion,	// 促销
	}
	
	public String userId;
	public CouponStatus status;
}
