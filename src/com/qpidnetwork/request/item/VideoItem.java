package com.qpidnetwork.request.item;

import java.io.Serializable;

public class VideoItem implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 7788548955355612335L;
	public VideoItem() {
		
	}

	/**
	 * 视频列表成功回调
	 * @param id			视频ID
	 * @param thumb			视频拇指图URL
	 * @param time			视频时长
	 * @param photo			视频图片URL
	 */
	public VideoItem(
			String id,
			String thumb,
			String time,
			String photo
			) {
		this.id = id;
		this.thumb = thumb;
		this.time = time;
		this.photo = photo;	
	}
	
	public String id;
	public String thumb;
	public String time;
	public String photo;	
}
