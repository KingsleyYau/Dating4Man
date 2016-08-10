package com.qpidnetwork.request.item;

import java.io.Serializable;

import com.qpidnetwork.request.RequestEnum.OnlineStatus;

public class Lady implements Serializable{

	private static final long serialVersionUID = 6361735860068550447L;
	
	public Lady() {
		
	}
	/**
	 * 获取女士列表回调
	 * @param age			年龄
	 * @param womanid		女士ID
	 * @param firstname		女士first name
	 * @param weight		重量
	 * @param height		高度 
	 * @param country		国家
	 * @param province		省份 
	 * @param photoURL		图片URL
	 * @param onlineStatus	在线状态
	 */
	public Lady(
		 int age,
		 String womanid, 
		 String firstname,
		 String weight,
		 String height,
		 String country,
		 String province,
		 String photoURL,
		 int onlineStatus
			) {
		this.age = age;
		this.womanid = womanid;
		this.firstname = firstname;
		this.weight = weight;
		this.height = height;
		this.country = country;
		this.province = province;
		this.photoURL = photoURL;
		this.onlineStatus = OnlineStatus.Offline;
		if (onlineStatus < OnlineStatus.values().length) {
			this.onlineStatus = OnlineStatus.values()[onlineStatus];
		}
	}
	
	public int age;
	public String womanid;
	public String firstname;
	public String weight;
	public String height;
	public String country;
	public String province;
	public String photoURL;
	public OnlineStatus	onlineStatus;
}
