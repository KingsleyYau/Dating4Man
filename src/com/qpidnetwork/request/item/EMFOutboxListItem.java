package com.qpidnetwork.request.item;

import com.qpidnetwork.request.RequestJniEMF.ProgressType;

public class EMFOutboxListItem{
	public EMFOutboxListItem() {
	}
	 /**
	  * 
	  * @param id 			邮件ID
	  * @param attachnum	附件数量
	  * @param virtualGifts	虚拟礼物
	  * @param womanid		女士ID
	  * @param firstName	女士first name
	  * @param lastName		女士last name
	  * @param weight		体重
	  * @param height		身高
	  * @param country		国家
	  * @param province		省份
	  * @param age			年龄
	  * @param sendTime		发送时间
	  * @param photoURL		女士头像
	  * @param intro		邮件简介(ver3.0起)
	  */
	public EMFOutboxListItem(
		 String id,
		 int attachnum, 
		 boolean virtualGifts,
		 int progress,
		 String womanid,
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
		this.progress = ProgressType.values()[progress];
		this.womanid = womanid;
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
	public ProgressType progress;
	public String womanid;
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
		if((o != null)&&(o instanceof EMFOutboxListItem)){
			EMFOutboxListItem object = (EMFOutboxListItem)o;
			return (object.id.equals(id));
		}
		return super.equals(o);
	}
}
