package com.qpidnetwork.request.item;

public class VSWatchedVideoListItem {
	public VSWatchedVideoListItem() {
		
	}

	/**
	 * 
	 * @param videoId		视频ID
	 * @param time			时长
	 * @param thumbURL		拇指图URL
	 * @param womanId		女士ID
	 * @param firstname		女士first name
	 * @param age			年龄
	 * @param weight		体重
	 * @param height		身高
	 * @param country		国家
	 * @param province		省份
	 * @param viewTime1		有效期起始时间
	 * @param viewTime2		有效期终止时间
	 * @param validTime		有效时间，距离当前时间还有多少天到期
	 */
	public VSWatchedVideoListItem(
			String videoId,
			String time,
			String thumbURL,
			String womanId,
			String firstname,
			int age,
			String weight,
			String height,
			String country,
			String province,
			String viewTime1,
			String viewTime2,
			long validTime
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
		this.viewTime1 = viewTime1;
		this.viewTime2 = viewTime2;
		this.validTime = validTime;
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
	public String viewTime1;
	public String viewTime2;
	public long validTime;
}
