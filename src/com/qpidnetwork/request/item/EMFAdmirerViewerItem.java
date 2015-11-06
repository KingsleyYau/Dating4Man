package com.qpidnetwork.request.item;

public class EMFAdmirerViewerItem {	
	public EMFAdmirerViewerItem() {
	}
	 /**
	  * 
	  * @param id			意向信ID
	  * @param body			内容
	  * @param photosURL	图片附件URL
	  * @param womanid		女士ID
	  * @param firstname	女士first name
	  * @param weight		体重
	  * @param height		身高
	  * @param country		国家
	  * @param province		省份
	  * @param mtab			表的后缀
	  * @param age			年龄
	  * @param photoURL		头像URL
	  * @param sendTime		发送时间
	  * @param templateType 主题类型（A:信件主题，B:虚拟礼物主题）
	  * @param vgId		            虚拟礼物ID
	  */
	public EMFAdmirerViewerItem(
		 String id,
		 String body,
		 String[] photosURL,
		 String womanid,
		 String firstname,
		 String weight,
		 String height,
		 String country,
		 String province,
		 String mtab,
		 int age,
		 String photoURL,
		 String sendTime,
		 String templateType,
		 String vgId
		 	) {
		this.id = id;
		this.body = body;
		this.photosURL = photosURL;
		this.womanid = womanid;
		this.firstname = firstname;
		this.weight = weight;
		this.height = height;
		this.country = country;
		this.province = province;
		this.mtab = mtab;
		this.age = age;
		this.photoURL = photoURL;
		this.sendTime = sendTime;
		this.templateType = templateType;
		this.vgId = vgId;
	}
	
	public String id;
	public String body;
	public String[] photosURL;
	public String womanid;
	public String firstname;
	public String weight;
	public String height;
	public String country;
	public String province;
	public String mtab;
	public int age;
	public String photoURL;
	public String sendTime;
	public String templateType;
	public String vgId;
}
