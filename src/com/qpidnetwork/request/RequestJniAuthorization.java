package com.qpidnetwork.request;

import com.qpidnetwork.request.RequestEnum.Country;

/**
 * 2.认证模块
 * @author Max.Chiu
 *
 */
public class RequestJniAuthorization {
    
    /**
     * 2.1.Facebook注册及登录
     * @param token				Facebook登录返回的token
     * @param email				电子邮箱
     * @param password			密码
     * @param deviceId			设备唯一标识
     * @param versioncode		客户端内部版本号
     * @param model				移动设备型号
     * @param manufacturer		制造厂商
     * @param prevcode			上一步操作的错误代码
     * @param birthday_y		生日的年
     * @param birthday_m		生日的月
     * @param birthday_d		生日的日
     * @param callback
     * @return					请求唯一标识
     */
    static public native long LoginWithFacebook(
    		String token, 
    		String email, 
    		String password, 
    		String deviceId, 
    		String versioncode, 
    		String model, 
    		String manufacturer, 
    		String prevcode, 
    		String birthday_y, 
    		String birthday_m, 
    		String birthday_d,
    		OnLoginWithFacebookCallback callback
    		);
    
    /**
     * 2.2.注册帐号
     * @param email				电子邮箱
     * @param password			密码
     * @param male				性别, true:男性/false:女性
     * @param first_name		用户first name
     * @param last_name			用户last name
     * @param country			国家区号,参考枚举 <RequestEnum.Country>
     * @param birthday_y		生日的年
     * @param birthday_m		生日的月
     * @param birthday_d		生日的日
     * @param weeklymail		是否接收订阅
     * @param model				移动设备型号
     * @param deviceId			设备唯一标识
     * @param manufacturer		制造厂商
     * @param callback
     * @return					请求唯一标识
     */
    static public long Register(
    		String email, 
    		String password, 
    		boolean male, 
    		String first_name, 
    		String last_name, 
    		Country country, 
    		String birthday_y, 
    		String birthday_m, 
    		String birthday_d, 
    		boolean weeklymail, 
			String model, 
			String deviceId, 
			String manufacturer, 
			OnRegisterCallback callback
			) {
		return Register(email, password, male, first_name, last_name, country.ordinal(), birthday_y, birthday_m, 
				birthday_d, weeklymail, model, deviceId, manufacturer, callback);
	}
    static protected native long Register(
    		String email, 
    		String password, 
    		boolean male, 
    		String first_name, 
    		String last_name, 
			int country, 
			String birthday_y, 
			String birthday_m, 
			String birthday_d, 
			boolean weeklymail, 
			String model, 
			String deviceId, 
			String manufacturer, 
			OnRegisterCallback callback
			);
    
    /**
     * 2.3.获取验证码
     * @param callback
     * @return					请求唯一标识
     */
    static public native long GetCheckCode(OnRequestOriginalCallback callback);
    
    /**
     * 2.4.登录
     * @param email				电子邮箱
     * @param password			密码
     * @param deviceId			设备唯一标识
     * @param versioncode		客户端内部版本号
     * @param model				移动设备型号
     * @param manufacturer		制造厂商
     * @param callback
     * @return					请求唯一标识
     */
    static public native long Login(String email, String password, String checkcode, String deviceId, 
    		String versioncode, String model, String manufacturer, OnLoginCallback callback);
    
    /**
     * 2.5.找回密码
     * @param email			用户注册的邮箱
     * @param checkcode		验证码
     * @param callback
     * @return				请求唯一标识
     */
    static public native long FindPassword(String email, String checkcode, OnFindPasswordCallback callback);
    
    /**
     * 2.6.手机获取认证短信
     * @param telephone			电话号码
     * @param telephone_cc		国家区号,参考枚举 <RequestEnum.Country>
     * @param device_id			设备唯一标识
     * @param callback
     * @return					请求唯一标识
     */
    static public long GetSms(String telephone, Country telephone_cc, String device_id,
    		OnRequestCallback callback) {
    	return GetSms(telephone, telephone_cc.ordinal(), device_id, callback);
    }
    static protected native long GetSms(String telephone, int telephone_cc, String device_id,
    		OnRequestCallback callback);
    
    /**
     * 验证类型
     */
    public enum Verify {
    	NoNeed(-1),
    	Default(0),
    	Book(1);
    	
    	private int value;
    	 
        private Verify(int value) {
            this.value = value;
        }
 
        public int getValue() {
            return value;
        }
    }
    
    /**
     * 2.7.手机短信认证
     * @param verify_code		验证码
     * @param v_type			验证类型,参考枚举<Verify>
     * @param callback
     * @return					请求唯一标识
     */
    static public long VerifySms(String verify_code, Verify v_type, OnRequestCallback callback) {
    	return VerifySms(verify_code, v_type.getValue(), callback);
    }
    static protected native long VerifySms(String verify_code, int v_type, OnRequestCallback callback);
    
    /**
     * 2.8.固定电话获取认证短信
     * @param landline			电话号码
     * @param telephone_cc		国家区号,参考枚举 <RequestEnum.Country>
     * @param landline_ac		区号
     * @param device_id			设备唯一标识
     * @param callback
     * @return					请求唯一标识
     */
    static public long GetFixedPhone(String landline, Country telephone_cc, String landline_ac, 
    		String device_id, OnRequestCallback callback) {
    	return GetFixedPhone(landline, telephone_cc.ordinal(), landline_ac, device_id, callback);
    }
    static protected native long GetFixedPhone(String landline, int telephone_cc, String landline_ac, 
    		String device_id, OnRequestCallback callback);
    
    /**
     * 2.9.固定电话短信认证
     * @param verify_code		验证码
     * @param callback
     * @return					请求唯一标识
     */
    static public native long VerifyFixedPhone(String verify_code, OnRequestCallback callback);
    
}
