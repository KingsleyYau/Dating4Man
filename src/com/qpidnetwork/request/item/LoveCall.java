package com.qpidnetwork.request.item;

import com.qpidnetwork.framework.util.UnitConversion;

public class LoveCall {
	public LoveCall() {
		
	}

	/**
	 * 11.1.获取Love Call列表接口回调
	 * @param orderid			订单ID
	 * @param womanid			女士ID
	 * @param image				图片URL
	 * @param firstname			女士first name
	 * @param country			国家
	 * @param age				年龄
	 * @param begintime			打电话的起始时间（Unix Timestamp）
	 * @param endtime			打电话的结束时间（Unix Timestamp）
	 * @param needtr			是否需要翻译
	 * @param isconfirm			是否已确定
	 * @param confirmmsg		男士发给女士的确定消息
	 * @param callid			Love Call ID
	 * @param centerid			Call Ceneter ID
	 */
	public LoveCall(
		 String orderid,
		 String womanid, 
		 String image,
		 String firstname,
		 String country,
		 int age,
		 int begintime,
		 int endtime,
		 boolean needtr,
		 boolean isconfirm,
		 String confirmmsg,
		 String callid,
		 String centerid
			) {

		this.orderid = orderid;
		this.womanid = womanid;
		this.image = image;
		this.firstname = firstname;
		this.country = country;
		this.age = age;
		this.begintime = begintime;
		this.endtime = endtime;
		this.needtr = needtr;
		this.isconfirm = isconfirm;
		this.confirmmsg = confirmmsg;
		this.callid = callid;
		this.centerid = centerid;
		this.longbegintime = (long)this.begintime * (long)1000;
		this.longendtime = (long)this.endtime * (long)1000;
	}
	
	public boolean isCallActive(){
		return (System.currentTimeMillis() >= this.longbegintime && System.currentTimeMillis() <= this.longendtime);
	}
	
	public String getWhenCallActive(){
		long diff = this.longbegintime - System.currentTimeMillis();
		if (diff < 1) return "" + 0;
		return UnitConversion.getDurationStringToSecond((int)diff / 1000);
	}
	
	public String orderid;
	public String womanid;
	public String image;
	public String firstname;
	public String country;
	public int age;
	public int begintime;
	public int endtime;
	public boolean needtr;
	public boolean isconfirm;
	public String confirmmsg;
	public String callid;
	public String centerid;
	public long longbegintime;
	public long longendtime;
}
