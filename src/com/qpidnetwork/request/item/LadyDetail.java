package com.qpidnetwork.request.item;

import java.io.Serializable;
import java.util.ArrayList;

public class LadyDetail implements Serializable{
	
	private static final long serialVersionUID = 585399136851344089L;

	public LadyDetail() {
		
	}

	/**
	 * 查询女士详细信息回调
	 * @param womanid			女士ID
	 * @param firstname			女士first name
	 * @param country			国家
	 * @param province			省份
	 * @param birthday			出生日期
	 * @param age				年龄
	 * @param zodiac			星座
	 * @param weight			体重
	 * @param height			身高
	 * @param smoke				吸烟情况
	 * @param drink				喝酒情况
	 * @param english			英语能力
	 * @param religion			宗教情况
	 * @param education			教育情况
	 * @param profession		职业
	 * @param children			子女状况
	 * @param marry				婚姻状况
	 * @param resume			个人简介
	 * @param age1				期望的起始年龄
	 * @param age2				期望的结束年龄
	 * @param isonline			是否在线
	 * @param isfavorite		是否收藏
	 * @param last_update		最后更新时间
	 * @param show_lovecall		是否显示Love Call功能
	 * @param photoURL			女士头像URL
	 * @param photoMinURL		女士小头像URL(100*133)
	 * @param thumbList			拇指图URL列表
	 * @param photoList			图片URL列表
	 * @param videoList			视频列表
	 * @param photoLockNum     	锁定的相片数量
	 */
	public LadyDetail(
			String womanid,
			String firstname,
			String country,
			String province,
			String birthday,
			int age,
			String zodiac,
			String weight,
			String height,
			String smoke,
			String drink,
			String english,
			String religion,
			String education,
			String profession,
			String children,
			String marry,
			String resume,
			int age1,
			int age2,
			boolean isonline,
			boolean isfavorite,
			String last_update,
			int show_lovecall,
			String photoURL,
			String photoMinURL,
			
			ArrayList<String> thumbList,
			ArrayList<String> photoList,
			ArrayList<VideoItem> videoList,
			int photoLockNum
			) {
		this.womanid = womanid;
		this.firstname = firstname;
		this.country = country;
		this.province = province;
		this.birthday = birthday;
		this.age = age;
		this.zodiac = zodiac;
		this.weight = weight;
		this.height = height;
		this.smoke = smoke;
		this.drink = drink;
		this.english = english;
		this.religion = religion;
		this.education = education;
		this.profession = profession;
		this.children = children;
		this.marry = marry;
		this.resume = resume;
		this.age1 = age1;
		this.age2 = age2;
		this.isonline = isonline;
		this.isfavorite = isfavorite;
		this.last_update = last_update;
		if ( show_lovecall < 0 || show_lovecall >= ShowLoveCall.values().length ) {
			this.show_lovecall = ShowLoveCall.values()[0];
		} else {
			this.show_lovecall = ShowLoveCall.values()[show_lovecall];
		}
		this.photoURL = photoURL;
		this.photoMinURL = photoMinURL;
		this.thumbList = thumbList;
		this.photoList = photoList;
		this.videoList = videoList;
		this.photoLockNum = photoLockNum;
	}
	
	public String womanid;
	public String firstname;
	public String country;
	public String province;
	public String birthday;
	public int age;
	public String zodiac;
	public String weight;
	public String height;
	public String smoke;
	public String drink;
	public String english;
	public String religion;
	public String education;
	public String profession;
	public String children;
	public String marry;
	public String resume;
	public int age1;
	public int age2;
	public boolean isonline;
	public boolean isfavorite;
	public String last_update;
	
	/**
	 * @see 只有CallMeNow才能拨号
	 */
	public enum ShowLoveCall {
		None,
		CallMe,
		CallMeNow,
	}
	public ShowLoveCall show_lovecall;
	
	public String photoURL;
	public String photoMinURL;
	
	public ArrayList<String> thumbList;
	public ArrayList<String> photoList;
	
	public ArrayList<VideoItem> videoList;
	
	public int photoLockNum;
	
}
