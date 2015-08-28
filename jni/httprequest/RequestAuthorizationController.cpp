/*
 * RequestAuthorizationController.cpp
 *
 *  Created on: 2015-2-27
 *      Author: Max.Chiu
 *      Email: Kingsleyyau@gmail.com
 */

#include "RequestAuthorizationController.h"

#include "RequestEnumDefine.h"

#include <amf/AmfParser.h>



/**
 * 验证类型
 * 2.默认为首次验证，3.下單驗證（可选字段）
 */
#define VerifyArrayCount 2
string VerifyArray[VerifyArrayCount] = {
		"1",
		"3",
};

RequestAuthorizationController::RequestAuthorizationController(HttpRequestManager *pHttpRequestManager, RequestAuthorizationControllerCallback callback/* CallbackManager* pCallbackManager*/) {
	// TODO Auto-generated constructor stub
	SetHttpRequestManager(pHttpRequestManager);
	mRequestAuthorizationControllerCallback = callback;
}

RequestAuthorizationController::~RequestAuthorizationController() {
	// TODO Auto-generated destructor stub
}

/* IHttpRequestManagerCallback */
void RequestAuthorizationController::onSuccess(long requestId, string url, const char* buf, int size) {
	FileLog("httprequest",
			"RequestAuthorizationController::onSuccess( url : %s, content-type : %s, buf( size : %d ) )",
			url.c_str(),
			GetContentTypeById(requestId).c_str(),
			size
			);

	if (size < MAX_LOG_BUFFER) {
		FileLog("httprequest", "RequestAuthorizationController::onSuccess(), buf: %s", buf);
	}

	/* parse base result */
	string errnum = "";
	string errmsg = "";
	Json::Value data;
	Json::Value errdata;

	bool bFlag = HandleResult(buf, size, errnum, errmsg, &data, &errdata);

	/* resopned parse ok, callback success */
	if( url.compare(FACEBOOK_LOGIN_PATH) == 0 ) {
		/* 2.1.Facebook注册及登录 */
		LoginFacebookItem item;
		item.Parse(data);
		LoginErrorItem errItem;
		errItem.Parse(errdata);
		if( mRequestAuthorizationControllerCallback.onLoginWithFacebook != NULL ) {
			mRequestAuthorizationControllerCallback.onLoginWithFacebook(requestId, bFlag, item, errnum, errmsg, errItem);
		}
	} else if( url.compare(REGISTER_PATH) == 0 ) {
		/* 2.2.注册帐号 */
		RegisterItem item;
		item.Parse(data);
		if( mRequestAuthorizationControllerCallback.onRegister != NULL ) {
			mRequestAuthorizationControllerCallback.onRegister(requestId, bFlag, item, errnum, errmsg);
		}
	} else if( url.compare(CEHCKCODE_PATH) == 0 ) {
		/* 2.3.获取验证码 */
		bool bParse = false;
		if( GetContentTypeById(requestId).compare("image/png") == 0 ) {
			bParse = true;
		}

		if( mRequestAuthorizationControllerCallback.onGetCheckCode != NULL ) {
			mRequestAuthorizationControllerCallback.onGetCheckCode(requestId, bParse, buf, size, errnum, errmsg);
		}
	} else if( url.compare(LOGIN_PATH) == 0 ) {
		/* 2.4.登录 */
		LoginItem item;
		item.Parse(data);
		if( mRequestAuthorizationControllerCallback.onLogin != NULL ) {
			mRequestAuthorizationControllerCallback.onLogin(requestId, bFlag, item, errnum, errmsg);
		}
	} else if( url.compare(FINDPASSWORD_PATH) == 0 ) {
		/* 2.5.找回密码 */
		if( mRequestAuthorizationControllerCallback.onFindPassword != NULL ) {
			if( data.isString() ) {
				mRequestAuthorizationControllerCallback.onFindPassword(requestId, bFlag,
						data.asString(), errnum, errmsg);
			} else {
				mRequestAuthorizationControllerCallback.onFindPassword(requestId, bFlag,
						"", errnum, errmsg);
			}
		}

	} else if( url.compare(GET_SMS_PATH) == 0 ) {
		/* 2.6.手机获取认证短信 */
		if( mRequestAuthorizationControllerCallback.onGetSms != NULL ) {
			mRequestAuthorizationControllerCallback.onGetSms(requestId, bFlag, errnum, errmsg);
		}
	} else if( url.compare(VERIFY_SMS_PATH) == 0 ) {
		/* 2.7.手机短信认证  */
		if( mRequestAuthorizationControllerCallback.onVerifySms != NULL ) {
			mRequestAuthorizationControllerCallback.onVerifySms(requestId, bFlag, errnum, errmsg);
		}
	} else if( url.compare(GET_FIXED_PHONE_PATH) == 0 ) {
		/* 2.8.固定电话获取认证短信 */
		if( mRequestAuthorizationControllerCallback.onGetFixedPhone != NULL ) {
			mRequestAuthorizationControllerCallback.onGetFixedPhone(requestId, bFlag, errnum, errmsg);
		}
	} else if( url.compare(VERIFY_FIXED_PHONE_PATH) == 0 ) {
		/* 2.9.固定电话短信认证 */
		if( mRequestAuthorizationControllerCallback.onVerifyFixedPhone != NULL ) {
			mRequestAuthorizationControllerCallback.onVerifyFixedPhone(requestId, bFlag, errnum, errmsg);
		}
	}

	FileLog("httprequest", "RequestAuthorizationController::onSuccess() end, url:%s", url.c_str());
}
void RequestAuthorizationController::onFail(long requestId, string url) {
	FileLog("httprequest", "RequestAuthorizationController::onFail( url : %s )", url.c_str());
	/* request fail, callback fail */
	if( url.compare(FACEBOOK_LOGIN_PATH) == 0 ) {
		/* 2.1.Facebook注册及登录 */
		LoginFacebookItem item;
		LoginErrorItem errItem;
		if( mRequestAuthorizationControllerCallback.onLoginWithFacebook != NULL ) {
			mRequestAuthorizationControllerCallback.onLoginWithFacebook(requestId, false, item,
					LOCAL_ERROR_CODE_TIMEOUT, LOCAL_ERROR_CODE_TIMEOUT_DESC, errItem);
		}
	} else if( url.compare(REGISTER_PATH) == 0 ) {
		/* 2.2.注册帐号 */
		if( mRequestAuthorizationControllerCallback.onRegister != NULL ) {
			RegisterItem item;
			mRequestAuthorizationControllerCallback.onRegister(requestId, false, item, LOCAL_ERROR_CODE_TIMEOUT, LOCAL_ERROR_CODE_TIMEOUT_DESC);
		}
	} else if( url.compare(CEHCKCODE_PATH) == 0 ) {
		/* 2.3.获取验证码 */
		if( mRequestAuthorizationControllerCallback.onGetCheckCode != NULL ) {
			mRequestAuthorizationControllerCallback.onGetCheckCode(requestId, false, NULL, 0, LOCAL_ERROR_CODE_TIMEOUT, LOCAL_ERROR_CODE_TIMEOUT_DESC);
		}
	} else if( url.compare(LOGIN_PATH) == 0 ) {
		/* 2.4.登录 */
		if( mRequestAuthorizationControllerCallback.onLogin != NULL ) {
			LoginItem item;
			mRequestAuthorizationControllerCallback.onLogin(requestId, false, item, LOCAL_ERROR_CODE_TIMEOUT, LOCAL_ERROR_CODE_TIMEOUT_DESC);
		}
	} else if( url.compare(FINDPASSWORD_PATH) == 0 ) {
		/* 2.5.找回密码 */
		if( mRequestAuthorizationControllerCallback.onFindPassword != NULL ) {
			mRequestAuthorizationControllerCallback.onFindPassword(requestId, false,
					"", LOCAL_ERROR_CODE_TIMEOUT, LOCAL_ERROR_CODE_TIMEOUT_DESC);
		}
	} else if( url.compare(GET_SMS_PATH) == 0 ) {
		/* 2.6.手机获取认证短信 */
		if( mRequestAuthorizationControllerCallback.onGetSms != NULL ) {
			mRequestAuthorizationControllerCallback.onGetSms(requestId, false, LOCAL_ERROR_CODE_TIMEOUT, LOCAL_ERROR_CODE_TIMEOUT_DESC);
		}
	} else if( url.compare(VERIFY_SMS_PATH) == 0 ) {
		/* 2.7.手机短信认证  */
		if( mRequestAuthorizationControllerCallback.onVerifySms != NULL ) {
			mRequestAuthorizationControllerCallback.onVerifySms(requestId, false, LOCAL_ERROR_CODE_TIMEOUT, LOCAL_ERROR_CODE_TIMEOUT_DESC);
		}
	} else if( url.compare(GET_FIXED_PHONE_PATH) == 0 ) {
		/* 2.8.固定电话获取认证短信 */
		if( mRequestAuthorizationControllerCallback.onGetFixedPhone != NULL ) {
			mRequestAuthorizationControllerCallback.onGetFixedPhone(requestId, false, LOCAL_ERROR_CODE_TIMEOUT, LOCAL_ERROR_CODE_TIMEOUT_DESC);
		}
	} else if( url.compare(VERIFY_FIXED_PHONE_PATH) == 0 ) {
		/* 2.9.固定电话短信认证 */
		if( mRequestAuthorizationControllerCallback.onVerifyFixedPhone != NULL ) {
			mRequestAuthorizationControllerCallback.onVerifyFixedPhone(requestId, false, LOCAL_ERROR_CODE_TIMEOUT, LOCAL_ERROR_CODE_TIMEOUT_DESC);
		}
	}
	FileLog("httprequest", "RequestAuthorizationController::onFail() end, url:%s", url.c_str());
}


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
 * @param referrer			app推广参数（安装成功app第一次运行时GooglePlay返回）
 * @return					请求唯一标识
 */
long RequestAuthorizationController::LoginWithFacebook(
		string token,
		string email,
		string password,
		string deviceId,
		string versioncode,
		string model,
		string manufacturer,
		string prevcode,
		string birthday_y,
		string birthday_m,
		string birthday_d,
		string referrer
		) {
	char temp[16];

	HttpEntiy entiy;

	if( token.length() > 0 ) {
		entiy.AddContent(AUTHORIZATION_FACEBOOK_TOKEN, token.c_str());
	}

	if( email.length() > 0 ) {
		entiy.AddContent(AUTHORIZATION_EMAIL, email.c_str());
	}

	if( password.length() > 0 ) {
		entiy.AddContent(AUTHORIZATION_PASSWORD2, password.c_str());
	}

	if( versioncode.length() > 0 ) {
		entiy.AddContent(AUTHORIZATION_VERSIONCODE, versioncode.c_str());
	}

	if( model.length() > 0 ) {
		entiy.AddContent(AUTHORIZATION_MODEL, model.c_str());
	}

	if( deviceId.length() > 0 ) {
		entiy.AddContent(AUTHORIZATION_DEVICEID, deviceId.c_str());
	}

	if( manufacturer.length() > 0 ) {
		entiy.AddContent(AUTHORIZATION_MANUFACTURER, manufacturer.c_str());
	}

	if (prevcode.length() > 0) {
		entiy.AddContent(AUTHORIZATION_PREVCODE, prevcode.c_str());
	}

	if( birthday_y.length() > 0 && birthday_m.length() > 0 && birthday_d.length() > 0 ) {
		string birthday = birthday_y + "-" + birthday_m + "-" + birthday_d;
		entiy.AddContent(AUTHORIZATION_BIRTHDAY, birthday.c_str());
	}

	if (referrer.length() > 0) {
		entiy.AddContent(AUTHORIZATION_UTMREFERRER, referrer.c_str());
	}

	string url = FACEBOOK_LOGIN_PATH;
	FileLog("httprequest", "RequestAuthorizationController::LoginWithFacebook( "
			"url : %s, "
			"token : %s, "
			"email : %s, "
			"password : %s, "
			"versioncode : %s, "
			"model : %s, "
			"deviceId : %s, "
			"manufacturer : %s "
			"prevcode : %s, "
			"birthday_y : %s, "
			"birthday_m : %s, "
			"birthday_d : %s"
			"referrer : %s"
			" )",
			url.c_str(),
			token.c_str(),
			email.c_str(),
			password.c_str(),
			versioncode.c_str(),
			model.c_str(),
			deviceId.c_str(),
			manufacturer.c_str(),
			prevcode.c_str(),
			birthday_y.c_str(),
			birthday_m.c_str(),
			birthday_d.c_str(),
			referrer.c_str()
			);

	return StartRequest(url, entiy, this);
}

/**
 * 2.2.注册帐号
 * @param email				电子邮箱
 * @param password			密码
 * @param male				性别, true:男性/false:女性
 * @param first_name		用户first name
 * @param last_name			用户last name
 * @param country			国家区号,参考数组<CountryArray>
 * @param birthday_y		生日的年
 * @param birthday_m		生日的月
 * @param birthday_d		生日的日
 * @param weeklymail		是否接收订阅
 * @param model				移动设备型号
 * @param deviceId			设备唯一标识
 * @param manufacturer		制造厂商
 * @param referrer			app推广参数（安装成功app第一次运行时GooglePlay返回）
 * @return					请求唯一标识
 */
long RequestAuthorizationController::Register(
		string email,
		string password,
		bool male,
		string first_name,
		string last_name,
		int country,
		string birthday_y,
		string birthday_m,
		string birthday_d,
		bool weeklymail,
		string model,
		string deviceId,
		string manufacturer,
		string referrer
		) {
	char temp[16];

	HttpEntiy entiy;
	entiy.SetSaveCookie(true);

	if( email.length() > 0 ) {
		entiy.AddContent(AUTHORIZATION_EMAIL, email.c_str());
	}

	if( password.length() > 0 ) {
		entiy.AddContent(AUTHORIZATION_PASSWORD, password.c_str());
	}

	if( male ) {
		// 男人
		entiy.AddContent(AUTHORIZATION_GENDER, "M");
	} else {
		// 女人
		entiy.AddContent(AUTHORIZATION_GENDER, "F");
	}

	if( first_name.length() > 0 ) {
		entiy.AddContent(AUTHORIZATION_FIRST_NAME, first_name.c_str());
	}

	if( last_name.length() > 0 ) {
		entiy.AddContent(AUTHORIZATION_LAST_NAME, last_name.c_str());
	}

	if( country > -1 && country < CountryArrayCount ) {
		entiy.AddContent(AUTHORIZATION_COUNTRY, CountryArray[country]);
	}

	if( birthday_y.length() > 0 ) {
		entiy.AddContent(AUTHORIZATION_BIRTHDAY_Y, birthday_y.c_str());
	}

	if( birthday_m.length() > 0 ) {
		entiy.AddContent(AUTHORIZATION_BIRTHDAY_M, birthday_m.c_str());
	}

	if( birthday_d.length() > 0 ) {
		entiy.AddContent(AUTHORIZATION_BIRTHDAY_D, birthday_d.c_str());
	}

	sprintf(temp, "%s", weeklymail?"true":"false");
	entiy.AddContent(AUTHORIZATION_WEEKLY_MAIL, temp);

	if( model.length() > 0 ) {
		entiy.AddContent(AUTHORIZATION_MODEL, model.c_str());
	}

	if( deviceId.length() > 0 ) {
		entiy.AddContent(AUTHORIZATION_DEVICEID, deviceId.c_str());
	}

	if( manufacturer.length() > 0 ) {
		entiy.AddContent(AUTHORIZATION_MANUFACTURER, manufacturer.c_str());
	}

	if (referrer.length() > 0) {
		entiy.AddContent(AUTHORIZATION_UTMREFERRER, referrer.c_str());
	}

	string url = REGISTER_PATH;
	FileLog("httprequest", "RequestAuthorizationController::Register( "
			"url : %s, "
			"email : %s, "
			"password : %s, "
			"first_name : %s, "
			"last_name : %s, "
			"country : %s, "
			"birthday_y : %s, "
			"birthday_m : %s, "
			"birthday_d : %s, "
			"weeklymail : %s, "
			"model : %s, "
			"deviceId : %s, "
			"manufacturer : %s "
			"referrer : %s "
			")",
			url.c_str(),
			email.c_str(),
			password.c_str(),
			first_name.c_str(),
			last_name.c_str(),
			CountryArray[country],
			birthday_y.c_str(),
			birthday_m.c_str(),
			birthday_d.c_str(),
			weeklymail?"true":"false",
			model.c_str(),
			deviceId.c_str(),
			manufacturer.c_str(),
			referrer.c_str()
			);

	return StartRequest(url, entiy, this);
}

/**
 * 2.3.获取验证码
 * @param callback
 * @return					请求唯一标识
 */
long RequestAuthorizationController::GetCheckCode() {
	HttpEntiy entiy;
	entiy.SetSaveCookie(true);

	string url = CEHCKCODE_PATH;
	FileLog("httprequest", "RequestAuthorizationController::GetCheckCode( "
			"url : %s, "
			")",
			url.c_str()
			);

	return StartRequest(url, entiy, this);
}
/**
 * 2.4.登录
 * @param email				电子邮箱
 * @param password			密码
 * @param deviceId			设备唯一标识
 * @param versioncode		客户端内部版本号
 * @param model				移动设备型号
 * @param manufacturer		制造厂商
 * @return
 */
long RequestAuthorizationController::Login(
		string email,
		string password,
		string checkcode,
		string deviceId,
		string versioncode,
		string model,
		string manufacturer
		) {

	char temp[16];

	HttpEntiy entiy;

	entiy.SetSaveCookie(true);

	if( email.length() > 0 ) {
		entiy.AddContent(AUTHORIZATION_EMAIL, email.c_str());
	}

	if( password.length() > 0 ) {
		entiy.AddContent(AUTHORIZATION_PASSWORD2, password.c_str());
	}

	if( checkcode.length() > 0 ) {
		entiy.AddContent(AUTHORIZATION_CHECKCODE, checkcode.c_str());
	}

	if( deviceId.length() > 0 ) {
		entiy.AddContent(AUTHORIZATION_DEVICEID, deviceId.c_str());
	}

	if( versioncode.length() > 0 ) {
		entiy.AddContent(AUTHORIZATION_VERSIONCODE, versioncode.c_str());
	}

	if( model.length() > 0 ) {
		entiy.AddContent(AUTHORIZATION_MODEL, model.c_str());
	}

	if( manufacturer.length() > 0 ) {
		entiy.AddContent(AUTHORIZATION_MANUFACTURER, manufacturer.c_str());
	}

	string url = LOGIN_PATH;
	FileLog("httprequest", "RequestAuthorizationController::Login( "
			"url : %s, "
			"email : %s, "
			"password : %s, "
			"checkcode : %s, "
			"deviceId : %s, "
			"versioncode : %s, "
			"model : %s, "
			"manufacturer : %s "
			")",
			url.c_str(),
			email.c_str(),
			password.c_str(),
			checkcode.c_str(),
			deviceId.c_str(),
			versioncode.c_str(),
			model.c_str(),
			manufacturer.c_str());

	return StartRequest(url, entiy, this);
}

/**
 * 2.5.找回密码
 * @param email			用户注册的邮箱
 * @return				请求唯一标识
 */
long RequestAuthorizationController::FindPassword(string email, string checkcode) {
	HttpEntiy entiy;

	if( email.length() > 0 ) {
		entiy.AddContent(AUTHORIZATION_SENDMAIL, email.c_str());
	}

	if( checkcode.length() > 0 ) {
		entiy.AddContent(AUTHORIZATION_CHECKCODE, checkcode.c_str());
	}

	string url = FINDPASSWORD_PATH;
	FileLog("httprequest", "RequestAuthorizationController::FindPassword( "
			"url : %s, "
			"email : %s, "
			"checkcode : %s "
			")",
			url.c_str(),
			email.c_str(),
			checkcode.c_str()
			);

	return StartRequest(url, entiy, this);
}

/**
 * 2.6.手机获取认证短信
 * @param telephone			电话号码
 * @param telephone_cc		国家区号,参考参考数组<CountryArray>
 * @param device_id			设备唯一标识
 * @return					请求唯一标识
 */
long RequestAuthorizationController::GetSms(string telephone, int telephone_cc, string device_id) {
	HttpEntiy entiy;

	if( telephone.length() > 0 ) {
		entiy.AddContent(AUTHORIZATION_TELEPHONE, telephone.c_str());
	}

	if( telephone_cc > -1 && telephone_cc < CountryArrayCount ) {
		entiy.AddContent(AUTHORIZATION_TELEPHONE_CC, CountryArray[telephone_cc]);
	}

	if( device_id.length() > 0 ) {
		entiy.AddContent(AUTHORIZATION_DEVICE_ID, device_id.c_str());
	}

	string url = GET_SMS_PATH;
	FileLog("httprequest", "RequestAuthorizationController::GetSms( "
			"url : %s, "
			"telephone : %s, "
			"telephone_cc : %d, "
			"device_id : %s, "
			")",
			url.c_str(),
			telephone.c_str(),
			telephone_cc,
			device_id.c_str()
			);

	return StartRequest(url, entiy, this);
}

/**
 * 2.7.手机短信认证
 * @param verify_code		验证码
 * @param v_type			验证类型
 * @return					请求唯一标识
 */
long RequestAuthorizationController::VerifySms(string verify_code, int v_type) {
	HttpEntiy entiy;

	if( verify_code.length() > 0 ) {
		entiy.AddContent(AUTHORIZATION_VERIFY_CODE, verify_code.c_str());
	}

	if( v_type > -1 && v_type < VerifyArrayCount ) {
		entiy.AddContent(AUTHORIZATION_V_TYPE, VerifyArray[v_type]);
	}

	string url = VERIFY_SMS_PATH;
	FileLog("httprequest", "RequestAuthorizationController::VerifySms( "
			"url : %s, "
			"verify_code : %s, "
			"v_type : %d, "
			")",
			url.c_str(),
			verify_code.c_str(),
			v_type
			);

	return StartRequest(url, entiy, this);
}

/**
 * 2.8.固定电话获取认证短信
 * @param landline			电话号码
 * @param telephone_cc		国家区号,参考数组<CountryArray>
 * @param landline_ac		区号
 * @param device_id			设备唯一标识
 * @return					请求唯一标识
 */
long RequestAuthorizationController::GetFixedPhone(string landline, int telephone_cc, string landline_ac,
		string device_id) {
	HttpEntiy entiy;

	if( landline.length() > 0 ) {
		entiy.AddContent(AUTHORIZATION_LANDLINE, landline.c_str());
	}

	if( telephone_cc > -1 && telephone_cc < CountryArrayCount ) {
		entiy.AddContent(AUTHORIZATION_LANDLINE_CC, CountryArray[telephone_cc]);
	}

	if( landline_ac.length() > 0 ) {
		entiy.AddContent(AUTHORIZATION_LANDLINE_AC, landline_ac.c_str());
	}

	string url = GET_FIXED_PHONE_PATH;
	FileLog("httprequest", "RequestAuthorizationController::GetFixedPhone( "
			"url : %s, "
			"landline : %s, "
			"telephone_cc : %d, "
			"landline_ac : %s, "
			")",
			url.c_str(),
			landline.c_str(),
			telephone_cc,
			landline_ac.c_str()
			);

	return StartRequest(url, entiy, this);
}

/**
 * 2.9.固定电话短信认证
 * @param verify_code		验证码
 * @return					请求唯一标识
 */
long RequestAuthorizationController::VerifyFixedPhone(string verify_code) {
	HttpEntiy entiy;

	if( verify_code.length() > 0 ) {
		entiy.AddContent(AUTHORIZATION_VERIFY_CODE, verify_code.c_str());
	}

	string url = VERIFY_FIXED_PHONE_PATH;
	FileLog("httprequest", "RequestAuthorizationController::VerifyFixedPhone( "
			"url : %s, "
			"verify_code : %s, "
			")",
			url.c_str(),
			verify_code.c_str()
			);

	return StartRequest(url, entiy, this);
}
