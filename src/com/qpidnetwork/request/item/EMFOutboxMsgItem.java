package com.qpidnetwork.request.item;

public class EMFOutboxMsgItem {	
	public EMFOutboxMsgItem() {
	}
	 /**
	  * 
	  * @param id			邮件ID
	  * @param vgid			虚拟礼物ID
	  * @param content		邮件内容
	  * @param sendTime		发送时间
	  * @param photosURL	图片附件文件URL
	  * @param photoURL		女士头像URL
	  * @param womanid		女士ID
	  * @param firstname	女士first name
	  * @param lastname		女士last name
	  * @param weight		体重
	  * @param height		身高
	  * @param country		国家
	  * @param province		省份
	  * @param age			年龄
	  * @param privatePhotos	私密照列表
	  */
	public EMFOutboxMsgItem(
		 String id,
		 String vgid,
		 String content,
		 String sendTime,
		 String[] photosURL,
		 String photoURL,
		 String womanid,
		 String firstname,
		 String lastname,
		 String weight,
		 String height,
		 String country,
		 String province,
		 int age,
		 EMFPrivatePhotoItem[] privatePhotos
		 	) {
		this.id = id;
		this.vgid = vgid;
		this.content = content;
		this.sendTime = sendTime;
		this.photosURL = photosURL;
		this.photoURL = photoURL;
		this.womanid = womanid;
		this.firstname = firstname;
		this.lastname = lastname;
		this.weight = weight;
		this.height = height;
		this.country = country;
		this.province = province;
		this.age = age;
		this.privatePhotos = privatePhotos;
	}
	
	public String id;
	public String vgid;
	public String content;
	public String sendTime;
	public String[] photosURL;
	public String photoURL;
	public String womanid;
	public String firstname;
	public String lastname;
	public String weight;
	public String height;
	public String country;
	public String province;
	public int age;
	public EMFPrivatePhotoItem[] privatePhotos;
}
