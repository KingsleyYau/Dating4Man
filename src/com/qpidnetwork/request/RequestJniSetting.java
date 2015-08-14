package com.qpidnetwork.request;


/**
 * 3.设置
 * @author Max.Chiu
 *
 */
public class RequestJniSetting {
	/**
	 * 3.1.修改密码
	 * @param oldPassword	新密码
	 * @param newPassword	旧密码
	 * @param callback
	 * @return				请求唯一标识
	 */
	static public native long ChangePassword(String oldPassword, String newPassword, OnRequestCallback callback);
}
