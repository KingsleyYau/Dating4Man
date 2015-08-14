package com.qpidnetwork.request.item;

public class VSVideoListItem {	
	public VSVideoListItem() {
	}
	 /**
	  * 
	  * @param videoId		视频ID
	  * @param time			视频时长
	  * @param thumbURL		拇指图URL
	  * @param womanId		女士ID
	  * @param firstname	女士first name
	  * @param age			年龄
	  * @param weight		体重
	  * @param height		身高
	  * @param country		国家
	  * @param province		省份
	  */
	public VSVideoListItem(
		 String videoId,
		 String time,
		 String thumbURL,
		 String womanId,
		 String firstname,
		 int age,
		 String weight,
		 String height,
		 String country,
		 String province
		 	) {
		this.videoId = videoId;
		this.time = time;
		this.thumbURL = thumbURL;
		this.womanId = womanId;
		this.firstname = firstname;
		this.age = age;
		this.weight = weight;
		this.height = height;
		this.country = country;
		this.province = province;
	}
	
	public String videoId;
	public String time;
	public String thumbURL;
	public String womanId;
	public String firstname;
	public int age;
	public String weight;
	public String height;
	public String country;
	public String province;
}
