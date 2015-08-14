package com.qpidnetwork.request.item;

public class EMFInboxListItem {
	public EMFInboxListItem() {
	}
	 /**
	  * 
	  * @param id 			邮件ID
	  * @param attachnum	附件数量
	  * @param virtualGifts	虚拟礼物
	  * @param womanid		女士ID
	  * @param readFlag		是否已读
	  * @param rFlag		是否回复
	  * @param fFlag		是否转发
	  * @param pFlag		是否打印
	  * @param firstName	女士first name
	  * @param lastName		女士last name
	  * @param weight		体重
	  * @param height		身高
	  * @param country		国家
	  * @param province		省份
	  * @param age			年龄
	  * @param sendTime		发送时间
	  * @param photoURL		头像URL
	  * @param intro		邮件简介(ver3.0起)
	  */
	public EMFInboxListItem(
		 String id,
		 int attachnum, 
		 boolean virtualGifts,
		 String womanid,
		 boolean readFlag,
		 boolean rFlag,
		 boolean fFlag,
		 boolean pFlag,
		 String firstName,
		 String lastName,
		 String weight,
		 String height,
		 String country,
		 String province,
		 int age,
		 String sendTime,
		 String photoURL,
		 String intro
			) {
		this.id = id;
		this.attachnum = attachnum;
		this.virtualGifts = virtualGifts;
		this.womanid = womanid;
		this.readFlag = readFlag;
		this.rFlag = rFlag;
		this.fFlag = fFlag;
		this.pFlag = pFlag;
		this.firstName = firstName;
		this.lastName = lastName;
		this.weight = weight;
		this.height = height;
		this.country = country;
		this.province = province;
		this.age = age;
		this.sendTime = sendTime;
		this.photoURL = photoURL;
		this.intro = intro;
	}
	
	public String id;
	public int attachnum; 
	public boolean virtualGifts;
	public String womanid;
	public boolean readFlag;
	public boolean rFlag;
	public boolean fFlag;
	public boolean pFlag;
	public String firstName;
	public String lastName;
	public String weight;
	public String height;
	public String country;
	public String province;
	public int age;
	public String sendTime;
	public String photoURL;
	public String intro;
	
	@Override
	public boolean equals(Object o) {
		if((o != null)&&(o instanceof EMFInboxListItem)){
			EMFInboxListItem object = (EMFInboxListItem)o;
			return (object.id.equals(id));
		}
		return super.equals(o);
	}
}
