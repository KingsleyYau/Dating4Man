package com.qpidnetwork.request.item;

import java.io.Serializable;

public class QuickMatchLady implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -581335775373396803L;
	public QuickMatchLady() {
		this.age = 0;
		this.womanid = "";
		this.firstname = "";
		this.country = "";
		this.image = "";
		this.photoURL = "";
	}
	/**
	 * 获取女士列表回调
	 * @param age			年龄
	 * @param womanid		女士ID
	 * @param firstname		女士first name
	 * @param country		国家
	 * @param image			图片URL
	 * @param photoURL      头像URL 
	 */
	public QuickMatchLady(
		 int age,
		 String womanid, 
		 String firstname,
		 String country,
		 String image,
		 String photoURL
			) {
		this.age = age;
		this.womanid = womanid;
		this.firstname = firstname;
		this.country = country;
		this.image = image;
		this.photoURL = photoURL;
	}
	
	public int age;
	public String womanid;
	public String firstname;
	public String country;
	public String image;
	public String photoURL;
}
