package com.qpidnetwork.request.item;

public class LadyRecentContactItem {
	public LadyRecentContactItem() {
		
	}

	/**
	 * 最近联系人列表item（ver3.0起）
	 * @param womanid		女士ID
	 * @param firstname		女士firstname
	 * @param age			年龄
	 * @param photoURL		头像URL
	 * @param photoBigURL	头像URL（大图）
	 * @param isfavorite	是否收藏
	 * @param videoCount	视频数量
	 * @param lasttime		最后联系时间/单位秒
	 */
	public LadyRecentContactItem(
			String womanid,
			String firstname,
			int age,
			String photoURL,
			String photoBigURL,
			boolean isfavorite,
			int videoCount,
			int lasttime
			) 
	{
		this.womanid = womanid;
		this.firstname = firstname;
		this.age = age;
		this.photoURL = photoURL;
		this.photoBigURL = photoBigURL;
		this.isfavorite = isfavorite;
		this.videoCount = videoCount;
		this.lasttime = lasttime;
	}
	
	public String womanid;
	public String firstname;
	public int age;
	public String photoURL;
	public String photoBigURL;
	public boolean isfavorite;
	public int videoCount;
	public int lasttime;
}
