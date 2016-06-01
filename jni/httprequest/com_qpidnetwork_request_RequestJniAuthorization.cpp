/*
 * com_qpidnetwork_request_RequestJniAuthorization.cpp
 *
 *  Created on: 2015-2-27
 *      Author: Max.Chiu
 *      Email: Kingsleyyau@gmail.com
 */
#include "com_qpidnetwork_request_RequestJniAuthorization.h"
#include "com_qpidnetwork_request_RequestJni_GobalFunc.h"
#include <manrequesthandler/RequestAuthorizationController.h>

#include <crashhandler/CrashHandler.h>

void onLoginWithFacebook(long requestId, bool success, LoginFacebookItem item, string errnum, string errmsg,
		LoginErrorItem errItem);
void onRegister(long requestId, bool success, RegisterItem item, string errnum, string errmsg);
void onLogin(long requestId, bool success, LoginItem item, string errnum, string errmsg);
void onGetCheckCode(long requestId, bool success, const char* data, int len, string errnum, string errmsg);
void onFindPassword(long requestId, bool success, string tips, string errnum, string errmsg);
void onGetSms(long requestId, bool success, string errnum, string errmsg);
void onVerifySms(long requestId, bool success, string errnum, string errmsg);
void onGetFixedPhone(long requestId, bool success, string errnum, string errmsg);
void onVerifyFixedPhone(long requestId, bool success, string errnum, string errmsg);

RequestAuthorizationControllerCallback gRequestAuthorizationControllerCallback {
	onLoginWithFacebook,
	onRegister,
	onGetCheckCode,
	onLogin,
	onFindPassword,
	onGetSms,
	onVerifySms,
	onGetFixedPhone,
	onVerifyFixedPhone
};
RequestAuthorizationController gRequestAuthorizationController(&gHttpRequestManager, gRequestAuthorizationControllerCallback);

/**
 * FacebookLogin callback
 */
void onLoginWithFacebook(long requestId, bool success, LoginFacebookItem item, string errnum, string errmsg,
		LoginErrorItem errItem) {
	FileLog("httprequest", "Authorization.Native::onLoginWithFacebook( success : %s )", success?"true":"false");

	// Add for crash dump
	if( success ) {
		CrashHandler::GetInstance()->SetUser(item.manid);
	}

	/* turn object to java object here */
	JNIEnv* env;
	jint iRet = JNI_ERR;
	gJavaVM->GetEnv((void**)&env, JNI_VERSION_1_4);
	if( env == NULL ) {
		iRet = gJavaVM->AttachCurrentThread((JNIEnv **)&env, NULL);
	}

	jobject jItem = NULL;
	JavaItemMap::iterator itr = gJavaItemMap.find(LOGIN_FACEBOOK_ITEM_CLASS);
	if( itr != gJavaItemMap.end() ) {
		jclass cls = env->GetObjectClass(itr->second);
		if( cls != NULL) {
			jmethodID init = env->GetMethodID(cls, "<init>", "("
					"Ljava/lang/String;"
					"Ljava/lang/String;"
					"Ljava/lang/String;"
					"Ljava/lang/String;"
					"Ljava/lang/String;"
					"Ljava/lang/String;"
					"Z"
					"I"
					"Ljava/lang/String;"
					"Z"
					"I"
					"Ljava/lang/String;"
					"Ljava/lang/String;"
					"Ljava/lang/String;"
					"Z"
					"Z"
					"Z"
					"Z"
					"Z"
					"Z"
					"Z"
					"Z"
					"I"
					")V"
					);

			FileLog("httprequest", "Authorization.Native::onFacebookLogin( GetMethodID <init> : %p )", init);

			if( init != NULL ) {

				jstring manid = env->NewStringUTF(item.manid.c_str());
				jstring email = env->NewStringUTF(item.email.c_str());
				jstring firstname = env->NewStringUTF(item.firstname.c_str());
				jstring lastname = env->NewStringUTF(item.lastname.c_str());
				jstring photoURL = env->NewStringUTF(item.photoURL.c_str());
				jstring reg_step = env->NewStringUTF(item.reg_step.c_str());
				jstring telephone = env->NewStringUTF(item.telephone.c_str());
				jstring sessionid = env->NewStringUTF(item.sessionid.c_str());
				jstring ga_uid = env->NewStringUTF(item.ga_uid.c_str());
				jstring ticketid = env->NewStringUTF(item.ticketid.c_str());

				jItem = env->NewObject(cls, init,
						manid,
						email,
						firstname,
						lastname,
						photoURL,
						reg_step,
						item.is_reg,
						item.country,
						telephone,
						item.telephone_verify,
						item.telephone_cc,
						sessionid,
						ga_uid,
						ticketid,

						item.photosend,
						item.photoreceived,
						item.videoreceived,

						item.premit,
						item.ladyprofile,
						item.livechat,
						item.admirer,
						item.bpemf,

						item.rechargeCredit
						);

				env->DeleteLocalRef(manid);
				env->DeleteLocalRef(email);
				env->DeleteLocalRef(firstname);
				env->DeleteLocalRef(lastname);
				env->DeleteLocalRef(photoURL);
				env->DeleteLocalRef(reg_step);
				env->DeleteLocalRef(telephone);
				env->DeleteLocalRef(sessionid);
				env->DeleteLocalRef(ga_uid);
				env->DeleteLocalRef(ticketid);
				FileLog("httprequest", "Authorization.Native::onFacebookLogin( NewObject jItem : %p )", jItem);
			}
		}
	}

	jobject jErrItem = NULL;
	JavaItemMap::iterator itrerr = gJavaItemMap.find(LOGIN_ERROR_ITEM_CLASS);
	if( itrerr != gJavaItemMap.end() ) {
		jclass cls = env->GetObjectClass(itrerr->second);
		if( cls != NULL) {
			jmethodID init = env->GetMethodID(cls, "<init>", "("
					"Ljava/lang/String;"
					"Ljava/lang/String;"
					"Ljava/lang/String;"
					"Ljava/lang/String;"
					")V"
					);

			FileLog("httprequest", "Authorization.Native::onFacebookLogin( GetMethodID <init> : %p )", init);

			if( init != NULL ) {

				jstring email = env->NewStringUTF(errItem.email.c_str());
				jstring firstname = env->NewStringUTF(errItem.firstname.c_str());
				jstring lastname = env->NewStringUTF(errItem.lastname.c_str());
				jstring photoURL = env->NewStringUTF(errItem.photoURL.c_str());

				jErrItem = env->NewObject(cls, init,
						email,
						firstname,
						lastname,
						photoURL
						);

				env->DeleteLocalRef(email);
				env->DeleteLocalRef(firstname);
				env->DeleteLocalRef(lastname);
				env->DeleteLocalRef(photoURL);
				FileLog("httprequest", "Authorization.Native::onFacebookLogin( NewObject jErrItem : %p )", jErrItem);
			}
		}
	}

	/* real callback java */
	jobject callbackObj = gCallbackMap.Erase(requestId);
	jclass callbackCls = env->GetObjectClass(callbackObj);

	string signure = "(ZLjava/lang/String;Ljava/lang/String;";
	signure += "L";
	signure += LOGIN_FACEBOOK_ITEM_CLASS;
	signure += ";";
	signure += "L";
	signure += LOGIN_ERROR_ITEM_CLASS;
	signure += ";";
	signure += ")V";
	jmethodID callback = env->GetMethodID(callbackCls, "OnLoginWithFacebook", signure.c_str());
	FileLog("httprequest", "Authorization.Native::onLoginWithFacebook( callbackCls : %p, callback : %p, signure : %s )",
			callbackCls, callback, signure.c_str());

	if( callbackObj != NULL && callback != NULL ) {
		jstring jerrno = env->NewStringUTF(errnum.c_str());
		jstring jerrmsg = env->NewStringUTF(errmsg.c_str());

		FileLog("httprequest", "Authorization.Native::onLoginWithFacebook( CallObjectMethod "
				"jItem : %p )", jItem);

		env->CallVoidMethod(callbackObj, callback, success, jerrno, jerrmsg, jItem, jErrItem);

		env->DeleteGlobalRef(callbackObj);

		env->DeleteLocalRef(jerrno);
		env->DeleteLocalRef(jerrmsg);
	}

	if( jItem != NULL ) {
		env->DeleteLocalRef(jItem);
	}

	if( jErrItem != NULL ) {
		env->DeleteLocalRef(jErrItem);
	}

	if( iRet == JNI_OK ) {
		gJavaVM->DetachCurrentThread();
	}
}
/*
 * Class:     com_qpidnetwork_request_RequestJniAuthorization
 * Method:    LoginWithFacebook
 * Signature: (Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Lcom/qpidnetwork/request/OnLoginCallback;)J
 */
JNIEXPORT jlong JNICALL Java_com_qpidnetwork_request_RequestJniAuthorization_LoginWithFacebook
  (JNIEnv *env , jclass cls, jstring token, jstring email, jstring password, jstring deviceId,
		  jstring versioncode, jstring model, jstring manufacturer, jstring prevcode,
		  jstring birthday_y, jstring birthday_m, jstring birthday_d, jstring referrer,
		  jobject callback) {
	jlong requestId = -1;

	const char *cpToken = env->GetStringUTFChars(token, 0);
	const char *cpEmail = env->GetStringUTFChars(email, 0);
	const char *cpPassword = env->GetStringUTFChars(password, 0);
	const char *cpDeviceId = env->GetStringUTFChars(deviceId, 0);
	const char *cpVersioncode = env->GetStringUTFChars(versioncode, 0);
	const char *cpModel = env->GetStringUTFChars(model, 0);
	const char *cpManufacturer= env->GetStringUTFChars(manufacturer, 0);
	const char *cpPrevcode = env->GetStringUTFChars(prevcode, 0);
	const char *cpBirthday_y = env->GetStringUTFChars(birthday_y, 0);
	const char *cpBirthday_m = env->GetStringUTFChars(birthday_m, 0);
	const char *cpBirthday_d = env->GetStringUTFChars(birthday_d, 0);
	const char *cpReferrer = env->GetStringUTFChars(referrer, 0);

	requestId = gRequestAuthorizationController.LoginWithFacebook(cpToken, cpEmail, cpPassword, cpDeviceId,
			cpVersioncode, cpModel, cpManufacturer, cpPrevcode, cpBirthday_y, cpBirthday_m, cpBirthday_d,
			cpReferrer);

	jobject obj = env->NewGlobalRef(callback);
	gCallbackMap.Insert(requestId, obj);

	env->ReleaseStringUTFChars(token, cpToken);
	env->ReleaseStringUTFChars(email, cpEmail);
	env->ReleaseStringUTFChars(password, cpPassword);
	env->ReleaseStringUTFChars(deviceId, cpDeviceId);
	env->ReleaseStringUTFChars(versioncode, cpVersioncode);
	env->ReleaseStringUTFChars(model, cpModel);
	env->ReleaseStringUTFChars(manufacturer, cpManufacturer);
	env->ReleaseStringUTFChars(prevcode, cpPrevcode);
	env->ReleaseStringUTFChars(birthday_y, cpBirthday_y);
	env->ReleaseStringUTFChars(birthday_m, cpBirthday_m);
	env->ReleaseStringUTFChars(birthday_d, cpBirthday_d);
	env->ReleaseStringUTFChars(referrer, cpReferrer);

	return requestId;
}
/**
 * Register Callback
 */
void onRegister(long requestId, bool success, RegisterItem item, string errnum, string errmsg) {
	FileLog("httprequest", "Authorization.Native::onRegister( success : %s )", success?"true":"false");

	/* turn object to java object here */
	JNIEnv* env;
	jint iRet = JNI_ERR;
	gJavaVM->GetEnv((void**)&env, JNI_VERSION_1_4);
	if( env == NULL ) {
		iRet = gJavaVM->AttachCurrentThread((JNIEnv **)&env, NULL);
	}

	jobject jItem = NULL;
	JavaItemMap::iterator itr = gJavaItemMap.find(REGISTER_ITEM_CLASS);
	if( itr != gJavaItemMap.end() ) {
		jclass cls = env->GetObjectClass(itr->second);
		if( cls != NULL) {
			jmethodID init = env->GetMethodID(cls, "<init>", "("
					"Z"
					"Ljava/lang/String;"
					"Ljava/lang/String;"
					"Ljava/lang/String;"
					"Ljava/lang/String;"
					"Ljava/lang/String;"
					"Ljava/lang/String;"
					"Ljava/lang/String;"
					"Ljava/lang/String;"

					"Ljava/lang/String;"
					"Ljava/lang/String;"
					"Ljava/lang/String;"

					"Z"
					"Z"
					"Z"
					")V"
					);

			FileLog("httprequest", "Authorization.Native::onRegister( GetMethodID <init> : %p )", init);

			if( init != NULL ) {

				jstring manid = env->NewStringUTF(item.manid.c_str());
				jstring email = env->NewStringUTF(item.email.c_str());
				jstring firstname = env->NewStringUTF(item.firstname.c_str());
				jstring lastname = env->NewStringUTF(item.lastname.c_str());
				jstring sid = env->NewStringUTF(item.sid.c_str());
				jstring reg_step = env->NewStringUTF(item.reg_step.c_str());
				jstring errnum = env->NewStringUTF(item.errnum.c_str());
				jstring errtext = env->NewStringUTF(item.errtext.c_str());
				jstring photoURL = env->NewStringUTF(item.photoURL.c_str());
				jstring sessionid = env->NewStringUTF(item.sessionid.c_str());
				jstring ga_uid = env->NewStringUTF(item.ga_uid.c_str());

				jItem = env->NewObject(cls, init,
						item.login,
						manid,
						email,
						firstname,
						lastname,
						sid,
						reg_step,
						errnum,
						errtext,
						photoURL,
						sessionid,
						ga_uid,
						item.photosend,
						item.photoreceived,
						item.videoreceived
						);

				env->DeleteLocalRef(manid);
				env->DeleteLocalRef(email);
				env->DeleteLocalRef(firstname);
				env->DeleteLocalRef(lastname);
				env->DeleteLocalRef(sid);
				env->DeleteLocalRef(reg_step);
				env->DeleteLocalRef(errnum);
				env->DeleteLocalRef(errtext);
				env->DeleteLocalRef(photoURL);
				env->DeleteLocalRef(sessionid);
				env->DeleteLocalRef(ga_uid);
				FileLog("httprequest", "Authorization.Native::onRegister( NewObject : %p )", jItem);
			}
		}
	}

	/* real callback java */
	jobject callbackObj = gCallbackMap.Erase(requestId);
	jclass callbackCls = env->GetObjectClass(callbackObj);

	string signure = "(ZLjava/lang/String;Ljava/lang/String;";
	signure += "L";
	signure += REGISTER_ITEM_CLASS;
	signure += ";";
	signure += ")V";
	jmethodID callback = env->GetMethodID(callbackCls, "OnRegister", signure.c_str());
	FileLog("httprequest", "Authorization.Native::onRegister( callbackCls : %p, callback : %p, signure : %s )",
			callbackCls, callback, signure.c_str());

	if( callbackObj != NULL && callback != NULL ) {
		jstring jerrno = env->NewStringUTF(errnum.c_str());
		jstring jerrmsg = env->NewStringUTF(errmsg.c_str());

		FileLog("httprequest", "Authorization.Native::onRegister( CallObjectMethod "
				"jItem : %p )", jItem);

		env->CallVoidMethod(callbackObj, callback, success, jerrno, jerrmsg, jItem);

		env->DeleteGlobalRef(callbackObj);

		env->DeleteLocalRef(jerrno);
		env->DeleteLocalRef(jerrmsg);
	}

	if( jItem != NULL ) {
		env->DeleteLocalRef(jItem);
	}

	if( iRet == JNI_OK ) {
		gJavaVM->DetachCurrentThread();
	}
}

/*
 * Class:     com_qpidnetwork_request_RequestJniAuthorization
 * Method:    Register
 * Signature: (Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;ILjava/lang/String;Ljava/lang/String;Ljava/lang/String;ZLjava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Lcom/qpidnetwork/request/OnRegisterCallback;)J
 */
JNIEXPORT jlong JNICALL Java_com_qpidnetwork_request_RequestJniAuthorization_Register
  (JNIEnv *env, jclass cls, jstring email, jstring password, jboolean male, jstring first_name, jstring last_name,
		  jint country, jstring birthday_y, jstring birthday_m, jstring birthday_d,
		  jboolean weeklymail, jstring model, jstring deviceId, jstring manufacturer,
		  jstring referrer, jobject callback) {
	jlong requestId = -1;

	const char *cpEmail = env->GetStringUTFChars(email, 0);
	const char *cpPassword = env->GetStringUTFChars(password, 0);
	const char *cpFirst_name = env->GetStringUTFChars(first_name, 0);
	const char *cpLast_name = env->GetStringUTFChars(last_name, 0);
	const char *cpBirthday_y = env->GetStringUTFChars(birthday_y, 0);
	const char *cpBirthday_m = env->GetStringUTFChars(birthday_m, 0);
	const char *cpBirthday_d = env->GetStringUTFChars(birthday_d, 0);
	const char *cpModel = env->GetStringUTFChars(model, 0);
	const char *cpDeviceId = env->GetStringUTFChars(deviceId, 0);
	const char *cpManufacturer = env->GetStringUTFChars(manufacturer, 0);
	const char *cpReferrer = env->GetStringUTFChars(referrer, 0);

	requestId = gRequestAuthorizationController.Register(cpEmail, cpPassword, male, cpFirst_name, cpLast_name,
			country, cpBirthday_y, cpBirthday_m, cpBirthday_d, weeklymail, cpModel, cpDeviceId, cpManufacturer,
			cpReferrer);

	jobject obj = env->NewGlobalRef(callback);
	gCallbackMap.Insert(requestId, obj);

	env->ReleaseStringUTFChars(email, cpEmail);
	env->ReleaseStringUTFChars(password, cpPassword);
	env->ReleaseStringUTFChars(first_name, cpFirst_name);
	env->ReleaseStringUTFChars(last_name, cpLast_name);
	env->ReleaseStringUTFChars(birthday_y, cpBirthday_y);
	env->ReleaseStringUTFChars(birthday_m, cpBirthday_m);
	env->ReleaseStringUTFChars(birthday_d, cpBirthday_d);
	env->ReleaseStringUTFChars(model, cpModel);
	env->ReleaseStringUTFChars(deviceId, cpDeviceId);
	env->ReleaseStringUTFChars(manufacturer, cpManufacturer);
	env->ReleaseStringUTFChars(referrer, cpReferrer);

	return requestId;
}

void onGetCheckCode(long requestId, bool success, const char* data, int len, string errnum, string errmsg) {
	FileLog("httprequest", "Authorization.Native::onGetCheckCode( success : %s )", success?"true":"false");

	/* turn object to java object here */
	JNIEnv* env;
	jint iRet = JNI_ERR;
	gJavaVM->GetEnv((void**)&env, JNI_VERSION_1_4);
	if( env == NULL ) {
		iRet = gJavaVM->AttachCurrentThread((JNIEnv **)&env, NULL);
	}

	jbyteArray byteArray = env->NewByteArray(len);
	if( byteArray != NULL ) {
		env->SetByteArrayRegion(byteArray, 0, len, (const jbyte*) data);
	}

	/* real callback java */
	jobject callbackObj = gCallbackMap.Erase(requestId);
	jclass callbackCls = env->GetObjectClass(callbackObj);

	string signure = "(ZLjava/lang/String;Ljava/lang/String;";
	signure += "[B";
	signure += ")V";
	jmethodID callback = env->GetMethodID(callbackCls, "OnRequestData", signure.c_str());
	FileLog("httprequest", "Authorization.Native::onGetCheckCode( callbackCls : %p, callback : %p, signure : %s )",
			callbackCls, callback, signure.c_str());

	if( callbackObj != NULL && callback != NULL ) {
		jstring jerrno = env->NewStringUTF(errnum.c_str());
		jstring jerrmsg = env->NewStringUTF(errmsg.c_str());

		FileLog("httprequest", "Authorization.Native::onGetCheckCode( CallObjectMethod "
				"byteArray : %p )", byteArray);

		env->CallVoidMethod(callbackObj, callback, success, jerrno, jerrmsg, byteArray);

		env->DeleteGlobalRef(callbackObj);

		env->DeleteLocalRef(jerrno);
		env->DeleteLocalRef(jerrmsg);
	}

	if( byteArray != NULL ) {
		env->DeleteLocalRef(byteArray);
	}

	if( iRet == JNI_OK ) {
		gJavaVM->DetachCurrentThread();
	}
}
/*
 * Class:     com_qpidnetwork_request_RequestJniAuthorization
 * Method:    GetCheckCode
 * Signature: (Lcom/qpidnetwork/request/OnRequestOriginalCallback;)J
 */
JNIEXPORT jlong JNICALL Java_com_qpidnetwork_request_RequestJniAuthorization_GetCheckCode
  (JNIEnv *env, jclass cls, jobject callback) {
	jlong requestId = -1;

	requestId = gRequestAuthorizationController.GetCheckCode();

	jobject obj = env->NewGlobalRef(callback);
	gCallbackMap.Insert(requestId, obj);


	return requestId;
}
/**
 * Login Callback
 */
void onLogin(long requestId, bool success, LoginItem item, string errnum, string errmsg) {
	FileLog("httprequest", "Authorization.Native::onLogin( success : %s )", success?"true":"false");

	// Add for crash dump
	if( success ) {
		CrashHandler::GetInstance()->SetUser(item.manid);
	}

	/* turn object to java object here */
	JNIEnv* env;
	jint iRet = JNI_ERR;
	gJavaVM->GetEnv((void**)&env, JNI_VERSION_1_4);
	if( env == NULL ) {
		iRet = gJavaVM->AttachCurrentThread((JNIEnv **)&env, NULL);
	}

	jobject jItem = NULL;
	JavaItemMap::iterator itr = gJavaItemMap.find(LOGIN_ITEM_CLASS);
	if( itr != gJavaItemMap.end() ) {
		jclass cls = env->GetObjectClass(itr->second);
		if( cls != NULL) {
			jmethodID init = env->GetMethodID(cls, "<init>", "("
					"Ljava/lang/String;"
					"Ljava/lang/String;"
					"Ljava/lang/String;"
					"Ljava/lang/String;"
					"Ljava/lang/String;"
					"Ljava/lang/String;"
					"I"
					"Ljava/lang/String;"
					"Z"
					"I"
					"Ljava/lang/String;"
					"Ljava/lang/String;"
					"Ljava/lang/String;"
					"Z"
					"Z"
					"Z"
					"Z"
					"Z"
					"Z"
					"Z"
					"Z"
					"I"
					")V"
					);

			FileLog("httprequest", "Authorization.Native::onLogin( GetMethodID <init> : %p )", init);

			if( init != NULL ) {

				jstring manid = env->NewStringUTF(item.manid.c_str());
				jstring email = env->NewStringUTF(item.email.c_str());
				jstring firstname = env->NewStringUTF(item.firstname.c_str());
				jstring lastname = env->NewStringUTF(item.lastname.c_str());
				jstring photoURL = env->NewStringUTF(item.photoURL.c_str());
				jstring reg_step = env->NewStringUTF(item.reg_step.c_str());
				jstring telephone = env->NewStringUTF(item.telephone.c_str());
				jstring sessionid = env->NewStringUTF(item.sessionid.c_str());
				jstring ga_uid = env->NewStringUTF(item.ga_uid.c_str());
				jstring ticketid = env->NewStringUTF(item.ticketid.c_str());

				jItem = env->NewObject(cls, init,
						manid,
						email,
						firstname,
						lastname,
						photoURL,
						reg_step,
						item.country,
						telephone,
						item.telephone_verify,
						item.telephone_cc,
						sessionid,
						ga_uid,
						ticketid,

						item.photosend,
						item.photoreceived,
						item.videoreceived,

						item.premit,
						item.ladyprofile,
						item.livechat,
						item.admirer,
						item.bpemf,

						item.rechargeCredit
						);

				env->DeleteLocalRef(manid);
				env->DeleteLocalRef(email);
				env->DeleteLocalRef(firstname);
				env->DeleteLocalRef(lastname);
				env->DeleteLocalRef(photoURL);
				env->DeleteLocalRef(reg_step);
				env->DeleteLocalRef(telephone);
				env->DeleteLocalRef(sessionid);
				env->DeleteLocalRef(ga_uid);
				env->DeleteLocalRef(ticketid);
				FileLog("httprequest", "Authorization.Native::onLogin( NewObject : %p )", jItem);
			}
		}
	}

	/* real callback java */
	jobject callbackObj = gCallbackMap.Erase(requestId);
	jclass callbackCls = env->GetObjectClass(callbackObj);

	string signure = "(ZLjava/lang/String;Ljava/lang/String;";
	signure += "L";
	signure += LOGIN_ITEM_CLASS;
	signure += ";";
	signure += ")V";
	jmethodID callback = env->GetMethodID(callbackCls, "OnLogin", signure.c_str());
	FileLog("httprequest", "Authorization.Native::onLogin( callbackCls : %p, callback : %p, signure : %s )",
			callbackCls, callback, signure.c_str());

	if( callbackObj != NULL && callback != NULL ) {
		jstring jerrno = env->NewStringUTF(errnum.c_str());
		jstring jerrmsg = env->NewStringUTF(errmsg.c_str());

		FileLog("httprequest", "Authorization.Native::onLogin( CallObjectMethod "
				"jItem : %p )", jItem);

		env->CallVoidMethod(callbackObj, callback, success, jerrno, jerrmsg, jItem);

		env->DeleteGlobalRef(callbackObj);

		env->DeleteLocalRef(jerrno);
		env->DeleteLocalRef(jerrmsg);
	}

	if( jItem != NULL ) {
		env->DeleteLocalRef(jItem);
	}

	if( iRet == JNI_OK ) {
		gJavaVM->DetachCurrentThread();
	}
}

/*
 * Class:     com_qpidnetwork_request_RequestJniAuthorization
 * Method:    Login
 * Signature: (Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Lcom/qpidnetwork/request/OnRegisterCallback;)J
 */
JNIEXPORT jlong JNICALL Java_com_qpidnetwork_request_RequestJniAuthorization_Login
  (JNIEnv *env, jclass, jstring email, jstring password, jstring checkcode, jstring deviceId, jstring versioncode,
		  jstring model, jstring manufacturer, jobject callback) {
	jlong requestId = -1;

	const char *cpEmail = env->GetStringUTFChars(email, 0);
	const char *cpPassword = env->GetStringUTFChars(password, 0);
	const char *cpCheckcode = env->GetStringUTFChars(checkcode, 0);
	const char *cpDeviceId = env->GetStringUTFChars(deviceId, 0);
	const char *cpVersioncode = env->GetStringUTFChars(versioncode, 0);
	const char *cpModel = env->GetStringUTFChars(model, 0);
	const char *cpManufacturer= env->GetStringUTFChars(manufacturer, 0);

	requestId = gRequestAuthorizationController.Login(cpEmail, cpPassword, cpCheckcode, cpDeviceId,
			cpVersioncode, cpModel, cpManufacturer);

	jobject obj = env->NewGlobalRef(callback);
	gCallbackMap.Insert(requestId, obj);

	env->ReleaseStringUTFChars(email, cpEmail);
	env->ReleaseStringUTFChars(password, cpPassword);
	env->ReleaseStringUTFChars(checkcode, cpCheckcode);
	env->ReleaseStringUTFChars(deviceId, cpDeviceId);
	env->ReleaseStringUTFChars(versioncode, cpVersioncode);
	env->ReleaseStringUTFChars(model, cpModel);
	env->ReleaseStringUTFChars(manufacturer, cpManufacturer);

	return requestId;
}

void onFindPassword(long requestId, bool success, string tips, string errnum, string errmsg) {
	FileLog("httprequest", "Authorization.Native::onFindPassword( success : %s )", success?"true":"false");

	/* turn object to java object here */
	JNIEnv* env;
	jint iRet = JNI_ERR;
	gJavaVM->GetEnv((void**)&env, JNI_VERSION_1_4);
	if( env == NULL ) {
		iRet = gJavaVM->AttachCurrentThread((JNIEnv **)&env, NULL);
	}

	/* real callback java */
	jobject callbackObj = gCallbackMap.Erase(requestId);
	jclass callbackCls = env->GetObjectClass(callbackObj);

	string signure = "(ZLjava/lang/String;Ljava/lang/String;Ljava/lang/String;)V";
	jmethodID callback = env->GetMethodID(callbackCls, "OnFindPassword", signure.c_str());
	FileLog("httprequest", "Authorization.Native::onFindPassword( callbackCls : %p, callback : %p, signure : %s )",
			callbackCls, callback, signure.c_str());

	if( callbackObj != NULL && callback != NULL ) {
		jstring jerrno = env->NewStringUTF(errnum.c_str());
		jstring jerrmsg = env->NewStringUTF(errmsg.c_str());
		jstring jtips = env->NewStringUTF(tips.c_str());

		FileLog("httprequest", "Authorization.Native::onFindPassword( CallObjectMethod )");

		env->CallVoidMethod(callbackObj, callback, success, jerrno, jerrmsg, jtips);

		env->DeleteGlobalRef(callbackObj);

		env->DeleteLocalRef(jerrno);
		env->DeleteLocalRef(jerrmsg);
		env->DeleteLocalRef(jtips);
	}

	if( iRet == JNI_OK ) {
		gJavaVM->DetachCurrentThread();
	}
}
/*
 * Class:     com_qpidnetwork_request_RequestJniAuthorization
 * Method:    FindPassword
 * Signature: (Ljava/lang/String;Lcom/qpidnetwork/request/OnFindPasswordCallback;)J
 */
JNIEXPORT jlong JNICALL Java_com_qpidnetwork_request_RequestJniAuthorization_FindPassword
  (JNIEnv *env, jclass, jstring email, jstring checkcode, jobject callback) {
	jlong requestId = -1;

	const char *cpEmail = env->GetStringUTFChars(email, 0);
	const char *cpCheckcode = env->GetStringUTFChars(checkcode, 0);

	requestId = gRequestAuthorizationController.FindPassword(cpEmail, cpCheckcode);

	jobject obj = env->NewGlobalRef(callback);
	gCallbackMap.Insert(requestId, obj);

	env->ReleaseStringUTFChars(email, cpEmail);
	env->ReleaseStringUTFChars(checkcode, cpCheckcode);

	return requestId;
}

void onGetSms(long requestId, bool success, string errnum, string errmsg) {
	FileLog("httprequest", "Authorization.Native::onGetSms( success : %s )", success?"true":"false");

	/* turn object to java object here */
	JNIEnv* env;
	jint iRet = JNI_ERR;
	gJavaVM->GetEnv((void**)&env, JNI_VERSION_1_4);
	if( env == NULL ) {
		iRet = gJavaVM->AttachCurrentThread((JNIEnv **)&env, NULL);
	}

	/* real callback java */
	jobject callbackObj = gCallbackMap.Erase(requestId);
	jclass callbackCls = env->GetObjectClass(callbackObj);

	string signure = "(ZLjava/lang/String;Ljava/lang/String;)V";
	jmethodID callback = env->GetMethodID(callbackCls, "OnRequest", signure.c_str());
	FileLog("httprequest", "Authorization.Native::onGetSms( callbackCls : %p, callback : %p, signure : %s )",
			callbackCls, callback, signure.c_str());

	if( callbackObj != NULL && callback != NULL ) {
		jstring jerrno = env->NewStringUTF(errnum.c_str());
		jstring jerrmsg = env->NewStringUTF(errmsg.c_str());

		FileLog("httprequest", "Authorization.Native::onGetSms( CallObjectMethod )");

		env->CallVoidMethod(callbackObj, callback, success, jerrno, jerrmsg);

		env->DeleteGlobalRef(callbackObj);

		env->DeleteLocalRef(jerrno);
		env->DeleteLocalRef(jerrmsg);
	}

	if( iRet == JNI_OK ) {
		gJavaVM->DetachCurrentThread();
	}
}
/*
 * Class:     com_qpidnetwork_request_RequestJniAuthorization
 * Method:    GetSms
 * Signature: (Ljava/lang/String;ILjava/lang/String;Lcom/qpidnetwork/request/OnRequestCallback;)J
 */
JNIEXPORT jlong JNICALL Java_com_qpidnetwork_request_RequestJniAuthorization_GetSms
  (JNIEnv *env, jclass, jstring telephone, jint telephone_cc, jstring device_id, jobject callback) {
	jlong requestId = -1;

	const char *cpTelephone = env->GetStringUTFChars(telephone, 0);
	const char *cpDeviceId = env->GetStringUTFChars(device_id, 0);

	requestId = gRequestAuthorizationController.GetSms(cpTelephone, telephone_cc, cpDeviceId);

	jobject obj = env->NewGlobalRef(callback);
	gCallbackMap.Insert(requestId, obj);

	env->ReleaseStringUTFChars(telephone, cpTelephone);
	env->ReleaseStringUTFChars(device_id, cpDeviceId);

	return requestId;
}

void onVerifySms(long requestId, bool success, string errnum, string errmsg) {
	FileLog("httprequest", "Authorization.Native::onVerifySms( success : %s )", success?"true":"false");

	/* turn object to java object here */
	JNIEnv* env;
	jint iRet = JNI_ERR;
	gJavaVM->GetEnv((void**)&env, JNI_VERSION_1_4);
	if( env == NULL ) {
		iRet = gJavaVM->AttachCurrentThread((JNIEnv **)&env, NULL);
	}

	/* real callback java */
	jobject callbackObj = gCallbackMap.Erase(requestId);
	jclass callbackCls = env->GetObjectClass(callbackObj);

	string signure = "(ZLjava/lang/String;Ljava/lang/String;)V";
	jmethodID callback = env->GetMethodID(callbackCls, "OnRequest", signure.c_str());
	FileLog("httprequest", "Authorization.Native::onVerifySms( callbackCls : %p, callback : %p, signure : %s )",
			callbackCls, callback, signure.c_str());

	if( callbackObj != NULL && callback != NULL ) {
		jstring jerrno = env->NewStringUTF(errnum.c_str());
		jstring jerrmsg = env->NewStringUTF(errmsg.c_str());

		FileLog("httprequest", "Authorization.Native::onVerifySms( CallObjectMethod )");

		env->CallVoidMethod(callbackObj, callback, success, jerrno, jerrmsg);

		env->DeleteGlobalRef(callbackObj);

		env->DeleteLocalRef(jerrno);
		env->DeleteLocalRef(jerrmsg);
	}

	if( iRet == JNI_OK ) {
		gJavaVM->DetachCurrentThread();
	}
}
/*
 * Class:     com_qpidnetwork_request_RequestJniAuthorization
 * Method:    VerifySms
 * Signature: (Ljava/lang/String;ILcom/qpidnetwork/request/OnRequestCallback;)J
 */
JNIEXPORT jlong JNICALL Java_com_qpidnetwork_request_RequestJniAuthorization_VerifySms
  (JNIEnv *env, jclass, jstring verify_code, jint v_type, jobject callback) {
	jlong requestId = -1;

	const char *cpVerify_code = env->GetStringUTFChars(verify_code, 0);

	requestId = gRequestAuthorizationController.VerifySms(cpVerify_code, v_type);

	jobject obj = env->NewGlobalRef(callback);
	gCallbackMap.Insert(requestId, obj);

	env->ReleaseStringUTFChars(verify_code, cpVerify_code);

	return requestId;
}

void onGetFixedPhone(long requestId, bool success, string errnum, string errmsg) {
	FileLog("httprequest", "Authorization.Native::onGetFixedPhone( success : %s )", success?"true":"false");

	/* turn object to java object here */
	JNIEnv* env;
	jint iRet = JNI_ERR;
	gJavaVM->GetEnv((void**)&env, JNI_VERSION_1_4);
	if( env == NULL ) {
		iRet = gJavaVM->AttachCurrentThread((JNIEnv **)&env, NULL);
	}

	/* real callback java */
	jobject callbackObj = gCallbackMap.Erase(requestId);
	jclass callbackCls = env->GetObjectClass(callbackObj);

	string signure = "(ZLjava/lang/String;Ljava/lang/String;)V";
	jmethodID callback = env->GetMethodID(callbackCls, "OnRequest", signure.c_str());
	FileLog("httprequest", "Authorization.Native::onGetFixedPhone( callbackCls : %p, callback : %p, signure : %s )",
			callbackCls, callback, signure.c_str());

	if( callbackObj != NULL && callback != NULL ) {
		jstring jerrno = env->NewStringUTF(errnum.c_str());
		jstring jerrmsg = env->NewStringUTF(errmsg.c_str());

		FileLog("httprequest", "Authorization.Native::onGetFixedPhone( CallObjectMethod )");

		env->CallVoidMethod(callbackObj, callback, success, jerrno, jerrmsg);

		env->DeleteGlobalRef(callbackObj);

		env->DeleteLocalRef(jerrno);
		env->DeleteLocalRef(jerrmsg);
	}

	if( iRet == JNI_OK ) {
		gJavaVM->DetachCurrentThread();
	}
}
/*
 * Class:     com_qpidnetwork_request_RequestJniAuthorization
 * Method:    GetFixedPhone
 * Signature: (Ljava/lang/String;ILjava/lang/String;Ljava/lang/String;Lcom/qpidnetwork/request/OnRequestCallback;)J
 */
JNIEXPORT jlong JNICALL Java_com_qpidnetwork_request_RequestJniAuthorization_GetFixedPhone
  (JNIEnv *env, jclass, jstring landline, jint telephone_cc, jstring landline_ac, jstring device_id, jobject callback) {
	jlong requestId = -1;

	const char *cpLandline = env->GetStringUTFChars(landline, 0);
	const char *cpLandline_ac = env->GetStringUTFChars(landline_ac, 0);
	const char *cpDevice_id = env->GetStringUTFChars(device_id, 0);

	requestId = gRequestAuthorizationController.GetFixedPhone(cpLandline, telephone_cc, cpLandline_ac, cpDevice_id);

	jobject obj = env->NewGlobalRef(callback);
	gCallbackMap.Insert(requestId, obj);

	env->ReleaseStringUTFChars(landline, cpLandline);
	env->ReleaseStringUTFChars(landline_ac, cpLandline_ac);
	env->ReleaseStringUTFChars(device_id, cpDevice_id);

	return requestId;
}

void onVerifyFixedPhone(long requestId, bool success, string errnum, string errmsg) {
	FileLog("httprequest", "Authorization.Native::onVerifyFixedPhone( success : %s )", success?"true":"false");

	/* turn object to java object here */
	JNIEnv* env;
	jint iRet = JNI_ERR;
	gJavaVM->GetEnv((void**)&env, JNI_VERSION_1_4);
	if( env == NULL ) {
		iRet = gJavaVM->AttachCurrentThread((JNIEnv **)&env, NULL);
	}

	/* real callback java */
	jobject callbackObj = gCallbackMap.Erase(requestId);
	jclass callbackCls = env->GetObjectClass(callbackObj);

	string signure = "(ZLjava/lang/String;Ljava/lang/String;)V";
	jmethodID callback = env->GetMethodID(callbackCls, "OnRequest", signure.c_str());
	FileLog("httprequest", "Authorization.Native::onGetFixedPhone( callbackCls : %p, callback : %p, signure : %s )",
			callbackCls, callback, signure.c_str());

	if( callbackObj != NULL && callback != NULL ) {
		jstring jerrno = env->NewStringUTF(errnum.c_str());
		jstring jerrmsg = env->NewStringUTF(errmsg.c_str());

		FileLog("httprequest", "Authorization.Native::onVerifyFixedPhone( CallObjectMethod )");

		env->CallVoidMethod(callbackObj, callback, success, jerrno, jerrmsg);

		env->DeleteGlobalRef(callbackObj);

		env->DeleteLocalRef(jerrno);
		env->DeleteLocalRef(jerrmsg);
	}

	if( iRet == JNI_OK ) {
		gJavaVM->DetachCurrentThread();
	}
}
/*
 * Class:     com_qpidnetwork_request_RequestJniAuthorization
 * Method:    VerifyFixedPhone
 * Signature: (Ljava/lang/String;Lcom/qpidnetwork/request/OnRequestCallback;)J
 */
JNIEXPORT jlong JNICALL Java_com_qpidnetwork_request_RequestJniAuthorization_VerifyFixedPhone
  (JNIEnv *env, jclass, jstring verify_code, jobject callback) {
	jlong requestId = -1;

	const char *cpVerify_code = env->GetStringUTFChars(verify_code, 0);

	requestId = gRequestAuthorizationController.VerifyFixedPhone(cpVerify_code);

	jobject obj = env->NewGlobalRef(callback);
	gCallbackMap.Insert(requestId, obj);

	env->ReleaseStringUTFChars(verify_code, cpVerify_code);

	return requestId;
}
