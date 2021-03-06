package com.qpidnetwork.request.item;

public class LCVideoItem {
	public LCVideoItem() {
		videoid = "";
		title = "";
		inviteid = "";	
		video_url = "";
	}
	
	/**
	 * 微视频列表结构体
	 * @param videoid			视频ID
	 * @param title				视频标题
	 * @param inviteid			邀请ID
	 * @param video_url			视频url
	 */
	public LCVideoItem(
			String videoid,
			String title,
			String inviteid,	
			String video_url
			) {
		this.videoid = videoid;
		this.title = title;
		this.inviteid = inviteid;	
		this.video_url = video_url;
	}
	
	public String videoid;
	public String title;
	public String inviteid;
	public String video_url;
	
	public String toString() {
		String result = "{ ";
		result += "videoid = " + videoid + ", ";
		result += "title = " + title + ", ";
		result += "inviteid = " + inviteid + ", ";
		result += "video_url = " + video_url;
		result += " }";
		return result;
	}
}
