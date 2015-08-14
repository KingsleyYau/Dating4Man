package com.qpidnetwork.request.item;

public class EMFInboxMsgItem {	
	public EMFInboxMsgItem() {
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
	  * @param photoURL		头像URL
	  * @param body			邮件内容
	  * @param notetoman	翻译内容
	  * @param photosURL	图片附件数组
	  * @param sendTime		发送时间
	  * @param privatePhotos	私密照列表
	  */
	public EMFInboxMsgItem(
		 String id,
		 String womanid,
		 String firstName,
		 String lastName,
		 String weight,
		 String height,
		 String country,
		 String province,
		 int age,
		 String photoURL,
		 String body,
		 String notetoman,
		 String[] photosURL,
		 String sendTime,
		 EMFPrivatePhotoItem[] privatePhotos,
		 String vgId
			) {
		this.id = id;
		this.womanid = womanid;
		this.firstName = firstName;
		this.lastName = lastName;
		this.weight = weight;
		this.height = height;
		this.country = country;
		this.province = province;
		this.age = age;
		this.photoURL = photoURL;
		this.body = body;
		this.notetoman = notetoman;
		this.photosURL = photosURL;
		this.sendTime = sendTime;
		this.privatePhotos = privatePhotos;
		this.vgId = vgId;
	}
	
	public String id;
	public String womanid;
	public String firstName;
	public String lastName;
	public String weight;
	public String height;
	public String country;
	public String province;
	public int age;
	public String photoURL;
	public String body;
	public String notetoman;
	public String[] photosURL;
	public String sendTime;
	public EMFPrivatePhotoItem[] privatePhotos;
	public String vgId;
}
