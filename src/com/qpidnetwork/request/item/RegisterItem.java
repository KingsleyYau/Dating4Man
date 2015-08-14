package com.qpidnetwork.request.item;

public class RegisterItem {
	public RegisterItem() {
		
	}

	/**
	 * 注册成功回调
	 * @param login				是否登录成功
	 * @param manid				用户id
	 * @param email				电子邮箱
	 * @param firstname			用户first name
	 * @param lastname			用户last name
	 * @param sid				跨服务器唯一标识
	 * @param reg_step			已进行的注册步骤数
	 * @param errno
	 * @param errtext
	 * @param photoURL			头像URL
	 * @param sessionid			跨服务器的唯一标识
	 * @param ga_uid			Google Analytics UserID参数
	 * @param photosend			私密照片发送权限
	 * @param photoreceived		私密照片接收权限
	 */
	public RegisterItem(
		 boolean login,
		 String manid, 
		 String email,
		 String firstname,
		 String lastname,
		 String sid,
		 String reg_step,
		 String errno,
		 String errtext,
		 String photoURL,
		 String sessionid,
		 String ga_uid,
		 boolean photosend,
		 boolean photoreceived
			) {
		this.login = login;
		this.manid = manid;
		this.email = email;
		this.firstname = firstname;
		this.lastname = lastname;
		this.sid = sid;
		this.reg_step = reg_step;
		this.errno = errno;
		this.errtext = errtext;
		this.photoURL = photoURL;
		this.sessionid = sessionid;
		this.ga_uid = ga_uid;
		this.photosend = photosend;
		this.photoreceived = photoreceived;
	}
	
	public boolean login;
	public String manid;
	public String email;
	public String firstname;
	public String lastname;	
	public String sid;
	public String reg_step;
	public String errno;
	public String errtext;
	public String photoURL;
	public String sessionid;
	public String ga_uid;
	public boolean photosend;
	public boolean photoreceived;
}