package com.qpidnetwork.request.item;

public class VSVideoDetailItem {
	public VSVideoDetailItem() {
		
	}

	/**
	 * 
	 * @param id			视频ID
	 * @param title			视频标题
	 * @param womanId		女士ID
	 * @param thumbURL		视频拇指图URL
	 * @param time			视频时长
	 * @param photoURL		视频图片URL
	 * @param videoFav		是否收藏
	 * @param videoSize		视频大小（MB）
	 * @param transcription	视频说明文字
	 * @param viewTime1		有效期起始时间
	 * @param viewTime2		有效期终止时间
	 */
	public VSVideoDetailItem(
			String id,
			String title,
			String womanId,
			String thumbURL,
			String time,
			String photoURL,
			boolean videoFav,
			String videoSize,
			String transcription,
			String viewTime1,
			String viewTime2
			) {
		this.id = id;
		this.title = title;
		this.womanId = womanId;
		this.thumbURL = thumbURL;
		this.time = time;
		this.photoURL = photoURL;
		this.videoFav = videoFav;
		this.videoSize = videoSize;
		this.transcription = transcription;
		this.viewTime1 = viewTime1;
		this.viewTime2 = viewTime2;
	}
	
	public String id;
	public String title;
	public String womanId;
	public String thumbURL;
	public String time;
	public String photoURL;
	public boolean videoFav;
	public String videoSize;
	public String transcription;
	public String viewTime1;
	public String viewTime2;
}
