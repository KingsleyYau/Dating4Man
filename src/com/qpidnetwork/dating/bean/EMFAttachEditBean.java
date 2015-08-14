package com.qpidnetwork.dating.bean;

public class EMFAttachEditBean {
	
	public String vg_id = "";
	/*图片*/
	public String attachmentId = "";
	public String localImgPath = "";//用于上传的源文件地址
	public AttachStatus status = AttachStatus.DEFAULT;
	public String errorCode = "";
	
	public String fileName = ""; //文件名
	public float rate = 0; //上传进度记录
	
	
	public enum AttachStatus{
		DEFAULT,
		ATTACHMENT_UPLOADING,
		ATTACH_SUCCESS,
		ATTACH_FAILED,
	}
}
