/*
 * com_qpidnetwork_request_RequestJniAdvert.cpp
 *
 *  Created on: 2015-3-9
 *      Author: Samson.Fan
 */
#include "com_qpidnetwork_request_RequestJniAdvert.h"
#include "com_qpidnetwork_request_RequestJni_GobalFunc.h"
#include "RequestAdvertController.h"

void OnMainAdvert(long requestId, bool success, const string& errnum, const string& errmsg, const AdMainAdvertItem& item);
void OnWomanListAdvert(long requestId, bool success, const string& errnum, const string& errmsg, const AdWomanListAdvertItem& item);
void OnPushAdvert(long requestId, bool success, const string& errnum, const string& errmsg, const AdPushAdvertList& pushList);
static RequestAdvertControllerCallback gRequestControllerCallback {
	OnMainAdvert,
	OnWomanListAdvert,
	OnPushAdvert
};
static RequestAdvertController gRequestController(&gHttpRequestManager, gRequestControllerCallback);

jobject GetPushAdvertJObject(JNIEnv* env, const AdPushAdvertItem& item)
{
	jobject jItem = NULL;
	jclass jItemCls = GetJClass(env, ADVERT_PUSHADVERT_ITEM_CLASS);
	if (jItemCls != NULL) {
		jmethodID init = env->GetMethodID(jItemCls, "<init>", "("
					"Ljava/lang/String;"	// pushId
					"Ljava/lang/String;"	// message
					"Ljava/lang/String;"	// adurl
					"I"						// opentype
					")V");

		jItem = env->NewObject(jItemCls, init,
					env->NewStringUTF(item.pushId.c_str()),
					env->NewStringUTF(item.message.c_str()),
					env->NewStringUTF(item.adurl.c_str()),
					item.openType
					);

		env->DeleteLocalRef(jItemCls);
	}
	return jItem;
}

// ------------------------------ MainAdvert ---------------------------------
/*
 * Class:     com_qpidnetwork_request_RequestJniAdvert
 * Method:    MainAdvert
 * Signature: (Ljava/lang/String;Ljava/lang/String;IILcom/qpidnetwork/request/OnAdMainAdvertCallback;)J
 */
JNIEXPORT jlong JNICALL Java_com_qpidnetwork_request_RequestJniAdvert_MainAdvert
  (JNIEnv *env, jclass cls, jstring deviceId, jstring advertId, jint showTimes, jint clickTimes, jobject callback)
{
	jlong requestId = -1;

	// deviceId
	string strDeviceId("");
	const char *cpDeviceId = env->GetStringUTFChars(deviceId, 0);
	strDeviceId = cpDeviceId;
	env->ReleaseStringUTFChars(deviceId, cpDeviceId);

	// advertId
	string strAdvertId("");
	if (NULL != advertId) {
		const char *cpAdvertId = env->GetStringUTFChars(advertId, 0);
		strAdvertId = cpAdvertId;
		env->ReleaseStringUTFChars(advertId, cpAdvertId);
	}

	// 发出请求
	requestId = gRequestController.MainAdvert(strDeviceId, strAdvertId, showTimes, clickTimes);
	if (requestId != -1) {
		// 保存callback
		jobject jObj = env->NewGlobalRef(callback);
		gCallbackMap.Insert(requestId, jObj);
		FileLog("httprequest", "Native::MainAdvert() requestId:%lld, callback:%p, jObj:%p", requestId, callback, jObj);
	}
	else {
		FileLog("httprequest", "Native::MainAdvert() fails. requestId:%lld, deviceId:%s, advertId:%s, showTimes:%d, clickTimes:%d"
			, requestId, strDeviceId.c_str(), strAdvertId.c_str(), showTimes, clickTimes);
	}

	return requestId;
}

void OnMainAdvert(long requestId, bool success, const string& errnum, const string& errmsg, const AdMainAdvertItem& item)
{
	FileLog("httprequest", "Native::OnMainAdvert( success : %s )", success?"true":"false");

	/* turn object to java object here */
	JNIEnv* env;
	jint iRet = JNI_ERR;
	gJavaVM->GetEnv((void**)&env, JNI_VERSION_1_4);
	if( env == NULL ) {
		iRet = gJavaVM->AttachCurrentThread((JNIEnv **)&env, NULL);
	}

	jobject jItem = NULL;
	jclass jItemCls = GetJClass(env, ADVERT_MAINADVERT_ITEM_CLASS);
	if (jItemCls != NULL) {
		jmethodID init = env->GetMethodID(jItemCls, "<init>", "("
					"Ljava/lang/String;"	// advertId
					"Ljava/lang/String;"	// image
					"I"						// width
					"I"						// height
					"Ljava/lang/String;"	// adurl
					"I"						// opentype
					"Z"						// isShow
					"I"						// valid
					")V");

		jItem = env->NewObject(jItemCls, init,
					env->NewStringUTF(item.advertId.c_str()),
					env->NewStringUTF(item.image.c_str()),
					item.width,
					item.height,
					env->NewStringUTF(item.adurl.c_str()),
					item.openType,
					item.isShow,
					item.valid
					);

		env->DeleteLocalRef(jItemCls);
	}

	/* real callback java */
	jobject jCallbackObj = gCallbackMap.Erase(requestId);
	jclass jCallbackCls = env->GetObjectClass(jCallbackObj);

	string signure = "(ZLjava/lang/String;Ljava/lang/String;";
	signure += "L";
	signure += ADVERT_MAINADVERT_ITEM_CLASS;
	signure += ";";
	signure += ")V";
	jmethodID jCallback = env->GetMethodID(jCallbackCls, "OnAdMainAdvert", signure.c_str());

	if( jCallbackObj != NULL && jCallback != NULL ) {
		jstring jerrno = env->NewStringUTF(errnum.c_str());
		jstring jerrmsg = env->NewStringUTF(errmsg.c_str());

		env->CallVoidMethod(jCallbackObj, jCallback, success, jerrno, jerrmsg, jItem);

		env->DeleteLocalRef(jerrno);
		env->DeleteLocalRef(jerrmsg);
	}

	// delete callback object & class
	if (jCallbackObj != NULL) {
		env->DeleteGlobalRef(jCallbackObj);
	}
	if (jCallbackCls != NULL) {
		env->DeleteLocalRef(jCallbackCls);
	}

	// delete item
	if( jItem != NULL ) {
		env->DeleteLocalRef(jItem);
	}

	if( iRet == JNI_OK ) {
		gJavaVM->DetachCurrentThread();
	}
}

// ------------------------------ WomanListAdvert ---------------------------------
/*
 * Class:     com_qpidnetwork_request_RequestJniAdvert
 * Method:    WomanListAdvert
 * Signature: (Ljava/lang/String;Ljava/lang/String;IILcom/qpidnetwork/request/OnAdWomanListAdvertCallback;)J
 */
JNIEXPORT jlong JNICALL Java_com_qpidnetwork_request_RequestJniAdvert_WomanListAdvert
  (JNIEnv *env, jclass cls, jstring deviceId, jstring advertId, jint showTimes, jint clickTimes, jobject callback)
{
	jlong requestId = -1;

	// deviceId
	string strDeviceId("");
	const char *cpDeviceId = env->GetStringUTFChars(deviceId, 0);
	strDeviceId = cpDeviceId;
	env->ReleaseStringUTFChars(deviceId, cpDeviceId);

	// advertId
	string strAdvertId("");
	if (NULL != advertId) {
		const char *cpAdvertId = env->GetStringUTFChars(advertId, 0);
		strAdvertId = cpAdvertId;
		env->ReleaseStringUTFChars(advertId, cpAdvertId);
	}

	// 发出请求
	requestId = gRequestController.WomanListAdvert(strDeviceId, strAdvertId, showTimes, clickTimes);
	if (requestId != -1) {
		// 保存callback
		jobject jObj = env->NewGlobalRef(callback);
		gCallbackMap.Insert(requestId, jObj);
		FileLog("httprequest", "Native::WomanListAdvert() requestId:%lld, callback:%p, jObj:%p", requestId, callback, jObj);
	}
	else {
		FileLog("httprequest", "Native::WomanListAdvert() fails. requestId:%lld, deviceId:%s, advertId:%s, showTimes:%d, clickTimes:%d"
			, requestId, strDeviceId.c_str(), strAdvertId.c_str(), showTimes, clickTimes);
	}

	return requestId;
}

void OnWomanListAdvert(long requestId, bool success, const string& errnum, const string& errmsg, const AdWomanListAdvertItem& item)
{
	FileLog("httprequest", "Native::OnWomanListAdvert( success : %s )", success?"true":"false");

	/* turn object to java object here */
	JNIEnv* env;
	jint iRet = JNI_ERR;
	gJavaVM->GetEnv((void**)&env, JNI_VERSION_1_4);
	if( env == NULL ) {
		iRet = gJavaVM->AttachCurrentThread((JNIEnv **)&env, NULL);
	}

	jobject jItem = NULL;
	jclass jItemCls = GetJClass(env, ADVERT_WOMANLISTADVERT_ITEM_CLASS);
	if (jItemCls != NULL) {
		jmethodID init = env->GetMethodID(jItemCls, "<init>", "("
					"Ljava/lang/String;"	// advertId
					"Ljava/lang/String;"	// image
					"I"						// width
					"I"						// height
					"Ljava/lang/String;"	// adurl
					"I"						// opentype
					")V");

		jItem = env->NewObject(jItemCls, init,
					env->NewStringUTF(item.advertId.c_str()),
					env->NewStringUTF(item.image.c_str()),
					item.width,
					item.height,
					env->NewStringUTF(item.adurl.c_str()),
					item.openType
					);

		env->DeleteLocalRef(jItemCls);
	}

	/* real callback java */
	jobject jCallbackObj = gCallbackMap.Erase(requestId);
	jclass jCallbackCls = env->GetObjectClass(jCallbackObj);

	string signure = "(ZLjava/lang/String;Ljava/lang/String;";
	signure += "L";
	signure += ADVERT_WOMANLISTADVERT_ITEM_CLASS;
	signure += ";";
	signure += ")V";
	jmethodID jCallback = env->GetMethodID(jCallbackCls, "OnAdWomanListAdvert", signure.c_str());

	if( jCallbackObj != NULL && jCallback != NULL ) {
		jstring jerrno = env->NewStringUTF(errnum.c_str());
		jstring jerrmsg = env->NewStringUTF(errmsg.c_str());

		env->CallVoidMethod(jCallbackObj, jCallback, success, jerrno, jerrmsg, jItem);

		env->DeleteLocalRef(jerrno);
		env->DeleteLocalRef(jerrmsg);
	}

	// delete callback object & class
	if (jCallbackObj != NULL) {
		env->DeleteGlobalRef(jCallbackObj);
	}
	if (jCallbackCls != NULL) {
		env->DeleteLocalRef(jCallbackCls);
	}

	// delete item
	if( jItem != NULL ) {
		env->DeleteLocalRef(jItem);
	}

	if( iRet == JNI_OK ) {
		gJavaVM->DetachCurrentThread();
	}
}
// ------------------------------ PushAdvert ---------------------------------
/*
 * Class:     com_qpidnetwork_request_RequestJniAdvert
 * Method:    PushAdvert
 * Signature: (Ljava/lang/String;Ljava/lang/String;Lcom/qpidnetwork/request/OnAdPushAdvertCallback;)J
 */
JNIEXPORT jlong JNICALL Java_com_qpidnetwork_request_RequestJniAdvert_PushAdvert
  (JNIEnv *env, jclass cls, jstring deviceId, jstring pushId, jobject callback)
{
	jlong requestId = -1;

	// deviceId
	string strDeviceId("");
	const char *cpDeviceId = env->GetStringUTFChars(deviceId, 0);
	strDeviceId = cpDeviceId;
	env->ReleaseStringUTFChars(deviceId, cpDeviceId);

	// pushId
	string strPushId("");
	if (NULL != pushId) {
		const char *cpPushId = env->GetStringUTFChars(pushId, 0);
		strPushId = cpPushId;
		env->ReleaseStringUTFChars(pushId, cpPushId);
	}

	// 发出请求
	requestId = gRequestController.PushAdvert(strDeviceId, strPushId);
	if (requestId != -1) {
		// 保存callback
		jobject jObj = env->NewGlobalRef(callback);
		gCallbackMap.Insert(requestId, jObj);
		FileLog("httprequest", "Native::PushAdvert() requestId:%lld, callback:%p, jObj:%p", requestId, callback, jObj);
	}
	else {
		FileLog("httprequest", "Native::PushAdvert() fails. requestId:%lld, deviceId:%s, pushId:%s"
			, requestId, strDeviceId.c_str(), strPushId.c_str());
	}

	return requestId;
}

void OnPushAdvert(long requestId, bool success, const string& errnum, const string& errmsg, const AdPushAdvertList& pushList)
{
	FileLog("httprequest", "Native::OnPushAdvert( success : %s )", success?"true":"false");

	/* turn object to java object here */
	JNIEnv* env;
	jint iRet = JNI_ERR;
	gJavaVM->GetEnv((void**)&env, JNI_VERSION_1_4);
	if( env == NULL ) {
		iRet = gJavaVM->AttachCurrentThread((JNIEnv **)&env, NULL);
	}

	jobjectArray jItemArray = NULL;
	jclass jItemCls = GetJClass(env, ADVERT_PUSHADVERT_ITEM_CLASS);
	if (NULL != jItemCls) {
		jItemArray = env->NewObjectArray(pushList.size(), jItemCls, NULL);
		AdPushAdvertList::const_iterator iter;
		int i = 0;
		for (iter = pushList.begin(); iter != pushList.end(); iter++) {
			jobject jItem = GetPushAdvertJObject(env ,*iter);
			if (jItem != NULL) {
				env->SetObjectArrayElement(jItemArray, i, jItem);

				env->DeleteLocalRef(jItem);
				i++;
			}
		}
	}

	/* real callback java */
	jobject jCallbackObj = gCallbackMap.Erase(requestId);
	jclass jCallbackCls = env->GetObjectClass(jCallbackObj);

	string signure = "(ZLjava/lang/String;Ljava/lang/String;";
	signure += "[L";
	signure += ADVERT_PUSHADVERT_ITEM_CLASS;
	signure += ";";
	signure += ")V";
	jmethodID jCallback = env->GetMethodID(jCallbackCls, "OnAdPushAdvert", signure.c_str());

	if( jCallbackObj != NULL && jCallback != NULL ) {
		jstring jerrno = env->NewStringUTF(errnum.c_str());
		jstring jerrmsg = env->NewStringUTF(errmsg.c_str());

		env->CallVoidMethod(jCallbackObj, jCallback, success, jerrno, jerrmsg, jItemArray);

		env->DeleteLocalRef(jerrno);
		env->DeleteLocalRef(jerrmsg);
	}

	// delete callback object & class
	if (jCallbackObj != NULL) {
		env->DeleteGlobalRef(jCallbackObj);
	}
	if (jCallbackCls != NULL) {
		env->DeleteLocalRef(jCallbackCls);
	}

	// delete itemArray
	if( jItemArray != NULL ) {
		env->DeleteLocalRef(jItemArray);
	}

	if( iRet == JNI_OK ) {
		gJavaVM->DetachCurrentThread();
	}
}
