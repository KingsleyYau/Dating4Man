package com.qpidnetwork.request.item;

public class EMFShortVideoItem {
	public EMFShortVideoItem() {
	}
	
	/**
	 * 私密照片
	 * @param sendId	发送ID
	 * @param photoId	照片ID
	 * @param photoFee	是否已扣费
	 * @param photoDesc	照片描述
	 */
	public EMFShortVideoItem(
			String sendId,	
			String videoId,
			boolean videoFee,
			String videoDesc
			) 
	{
		this.sendId = sendId;
		this.videoId = videoId;
		this.videoFee = videoFee;
		this.videoDesc = videoDesc;
	}
	
	public String sendId;
	public String videoId;
	public boolean videoFee;
	public String videoDesc;
}
