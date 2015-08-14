/*
 * com_qpidnetwork_request_RequestJniSetting.cpp
 *
 *  Created on: 2015-2-27
 *      Author: Max.Chiu
 *      Email: Kingsleyyau@gmail.com
 */
#include "com_qpidnetwork_request_RequestJniSetting.h"
#include "com_qpidnetwork_request_RequestJni_GobalFunc.h"
#include "RequestSettingController.h"

void onChangePassword(long requestId, bool success, string errnum, string errmsg);

RequestSettingControllerCallback gRequestSettingControllerCallback {
	onChangePassword,
};
RequestSettingController gRequestSettingController(&gHttpRequestManager, gRequestSettingControllerCallback);

void onChangePassword(long requestId, bool success, string errnum, string errmsg) {
	FileLog("httprequest", "Setting.Native::onChangePassword( success : %s )", success?"true":"false");

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
	FileLog("httprequest", "Setting.Native::onChangePassword( callbackCls : %p, callback : %p, signure : %s )",
			callbackCls, callback, signure.c_str());

	if( callbackObj != NULL && callback != NULL ) {
		jstring jerrno = env->NewStringUTF(errnum.c_str());
		jstring jerrmsg = env->NewStringUTF(errmsg.c_str());

		FileLog("httprequest", "Setting.Native::onChangePassword( CallObjectMethod )");

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
 * Class:     com_qpidnetwork_request_RequestJniSetting
 * Method:    ChangePassword
 * Signature: (Ljava/lang/String;Ljava/lang/String;Lcom/qpidnetwork/request/OnRequestCallback;)J
 */
JNIEXPORT jlong JNICALL Java_com_qpidnetwork_request_RequestJniSetting_ChangePassword
  (JNIEnv *env, jclass, jstring oldPassword, jstring newPassword, jobject callback) {
	jlong requestId = -1;

	const char *cpOldPassword = env->GetStringUTFChars(oldPassword, 0);
	const char *cpNewPassword = env->GetStringUTFChars(newPassword, 0);

	requestId = gRequestSettingController.ChangePassword(cpOldPassword, cpNewPassword);
	jobject obj = env->NewGlobalRef(callback);
	gCallbackMap.Insert(requestId, obj);

	env->ReleaseStringUTFChars(oldPassword, cpOldPassword);
	env->ReleaseStringUTFChars(newPassword, cpNewPassword);

	return requestId;
}

