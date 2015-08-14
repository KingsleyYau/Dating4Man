package com.qpidnetwork.request.item;

public class VSPlayVideoItem {
	public VSPlayVideoItem() {
		
	}

	/**
	 * 
	 * @param videoURL		视频URL
	 * @param transcription	视频说明文字
	 * @param viewTime1		有效期起始时间
	 * @param viewTime2		有效期终止时间
	 */
	public VSPlayVideoItem(
			String videoURL,
			String transcription,
			String viewTime1,
			String viewTime2
			) {
		this.videoURL = videoURL;
		this.transcription = transcription;
		this.viewTime1 = viewTime1;
		this.viewTime2 = viewTime2;
	}
	
	public String videoURL;
	public String transcription;
	public String viewTime1;
	public String viewTime2;
}
