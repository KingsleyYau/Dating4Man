package com.qpidnetwork.request.item;

public class EMFPrivatePhotoItem {
	public EMFPrivatePhotoItem() {
	}
	
	/**
	 * 私密照片
	 * @param sendId	发送ID
	 * @param photoId	照片ID
	 * @param photoFee	是否已扣费
	 * @param photoDesc	照片描述
	 */
	public EMFPrivatePhotoItem(
			String sendId,	
			String photoId,
			boolean photoFee,
			String photoDesc
			) 
	{
		this.sendId = sendId;
		this.photoId = photoId;
		this.photoFee = photoFee;
		this.photoDesc = photoDesc;
	}
	
	public String sendId;
	public String photoId;
	public boolean photoFee;
	public String photoDesc;
}
