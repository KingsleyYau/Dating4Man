package com.qpidnetwork.dating.authorization;

import java.io.Serializable;

import com.qpidnetwork.request.item.LoginItem;

public class LoginParam implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -3519711941929090295L;
	/**
	 * 登录方式
	 *
	 */
	public enum LoginType {
		Default,
		Facebook,
	}
	
	public LoginParam() {
		this.email = "";
		this.password = "";
		this.accessToken = "";
		this.type = LoginType.Default;
		this.item = null;
	}
	
	/**
	 * 登录成功回调
	 * @param email				电子邮箱
	 * @param password			密码
	 * @param type				登录方式(0:普通/1:facebook)
	 */
	public LoginParam(
			String email,
			String password,
			String accessToken,
			LoginType type,
			LoginItem item
			) {
		this.email = email;
		this.password = password;
		this.accessToken = accessToken;
		this.type = type;
		this.item = item;
	}
	public String email;
	public String password;
	public String accessToken;
	public LoginType type;
	public LoginItem item;

}
