/*
 * com_qpidnetwork_http_request_RequestJni.cpp
 *
 *  Created on: 2015-2-27
 *      Author: Max.Chiu
 *      Email: Kingsleyyau@gmail.com
 */
#include "com_qpidnetwork_request_RequestJni_GobalFunc.h"
#include "com_qpidnetwork_request_RequestJni.h"

#include <crashhandler/CrashHandler.h>

#include <common/IPAddress.h>
#include <common/md5.h>
#include <common/command.h>
#include <common/KZip.h>

/*
 * Class:     com_qpidnetwork_request_RequestJni
 * Method:    SetLogDirectory
 * Signature: (Ljava/lang/String;)V
 */
JNIEXPORT void JNICALL Java_com_qpidnetwork_request_RequestJni_SetLogDirectory
  (JNIEnv *env, jclass, jstring directory) {
	const char *cpDirectory = env->GetStringUTFChars(directory, 0);

	KLog::SetLogDirectory(cpDirectory);
	HttpClient::SetLogDirectory(cpDirectory);
	CrashHandler::GetInstance()->SetLogDirectory(cpDirectory);

	GetPhoneInfo();

	FileLog("httprequest", "SetLogDirectory ( directory : %s ) ", cpDirectory);
	FileLog("httprequest", "SetLogDirectory ( Android CPU ABI : %s ) ", GetPhoneCpuAbi().c_str());

	env->ReleaseStringUTFChars(directory, cpDirectory);
}

/*
 * Class:     com_qpidnetwork_request_RequestJni
 * Method:    SetVersionCode
 * Signature: (Ljava/lang/String;)V
 */
JNIEXPORT void JNICALL Java_com_qpidnetwork_request_RequestJni_SetVersionCode
  (JNIEnv *env, jclass cls, jstring version) {
	const char *cpVersion = env->GetStringUTFChars(version, 0);
	gHttpRequestManager.SetVersionCode(cpVersion);
	CrashHandler::GetInstance()->SetVersion(cpVersion);
	env->ReleaseStringUTFChars(version, cpVersion);
}

/*
 * Class:     com_qpidnetwork_request_RequestJni
 * Method:    SetCookiesDirectory
 * Signature: (Ljava/lang/String;)V
 */
JNIEXPORT void JNICALL Java_com_qpidnetwork_request_RequestJni_SetCookiesDirectory
  (JNIEnv *env, jclass, jstring directory) {
	const char *cpDirectory = env->GetStringUTFChars(directory, 0);

	HttpClient::SetCookiesDirectory(cpDirectory);

	FileLog("httprequest", "SetCookiesDirectory ( directory : %s ) ", cpDirectory);

	env->ReleaseStringUTFChars(directory, cpDirectory);
}

/*
 * Class:     com_qpidnetwork_request_RequestJni
 * Method:    SetWebSite
 * Signature: (Ljava/lang/String;Ljava/lang/String;)V
 */
JNIEXPORT void JNICALL Java_com_qpidnetwork_request_RequestJni_SetWebSite
  (JNIEnv *env, jclass cls, jstring webSite, jstring appSite) {
	const char *cpWebSite = env->GetStringUTFChars(webSite, 0);
	const char *cpAppSite = env->GetStringUTFChars(appSite, 0);

	gHttpRequestHostManager.SetWebSite(cpWebSite);
	gHttpRequestHostManager.SetAppSite(cpAppSite);

	env->ReleaseStringUTFChars(webSite, cpWebSite);
	env->ReleaseStringUTFChars(appSite, cpAppSite);
}

/*
 * Class:     com_qpidnetwork_request_RequestJni
 * Method:    SetPublicWebSite
 * Signature: (Ljava/lang/String;)V
 */
JNIEXPORT void JNICALL Java_com_qpidnetwork_request_RequestJni_SetPublicWebSite
  (JNIEnv *env, jclass cls, jstring chatVoiceSite)
{
	const char *cpChatVoiceSite = env->GetStringUTFChars(chatVoiceSite, 0);
	gHttpRequestHostManager.SetChatVoiceSite(cpChatVoiceSite);
	env->ReleaseStringUTFChars(chatVoiceSite, cpChatVoiceSite);
}

/*
 * Class:     com_qpidnetwork_request_RequestJni
 * Method:    SetAuthorization
 * Signature: (Ljava/lang/String;Ljava/lang/String;)V
 */
JNIEXPORT void JNICALL Java_com_qpidnetwork_request_RequestJni_SetAuthorization
  (JNIEnv *env, jclass, jstring user, jstring password) {
	const char *cpUser = env->GetStringUTFChars(user, 0);
	const char *cpPassword = env->GetStringUTFChars(password, 0);
	gHttpRequestManager.SetAuthorization(cpUser, cpPassword);
	env->ReleaseStringUTFChars(user, cpUser);
	env->ReleaseStringUTFChars(password, cpPassword);
}

/*
 * Class:     com_qpidnetwork_request_RequestJni
 * Method:    CleanCookies
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_com_qpidnetwork_request_RequestJni_CleanCookies
  (JNIEnv *, jclass) {
	HttpClient::CleanCookies();
}

/*
 * Class:     com_qpidnetwork_request_RequestJni
 * Method:    GetCookies
 * Signature: (Ljava/lang/String;)Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_com_qpidnetwork_request_RequestJni_GetCookies
  (JNIEnv *env, jclass, jstring site) {
	const char *cpSite = env->GetStringUTFChars(site, 0);
	string cookies = HttpClient::GetCookies(cpSite);
	env->ReleaseStringUTFChars(site, cpSite);
	return env->NewStringUTF(cookies.c_str());
}


/*
 * Class:     com_qpidnetwork_request_RequestJni
 * Method:    GetCookiesInfo
 * Signature: ()[Ljava/lang/String;
 */
JNIEXPORT jobjectArray JNICALL Java_com_qpidnetwork_request_RequestJni_GetCookiesInfo
  (JNIEnv *env, jclass cls)
{
	jobjectArray jStringArray = NULL;

	list<string> cookies = HttpClient::GetCookiesInfo();

	jclass jStringCls = env->FindClass("java/lang/String");
	jStringArray = env->NewObjectArray(cookies.size(), jStringCls, NULL);

	FileLog("httprequest", "GetCookiesInfo() JNI cookies.size:%d, jStringArray:%p", cookies.size(), jStringArray);

	if (NULL != jStringArray)
	{
		int i = 0;
		for (list<string>::const_iterator iter = cookies.begin();
			 iter != cookies.end();
			 iter++, i++)
		{
			jstring item = env->NewStringUTF((*iter).c_str());

			FileLog("httprequest", "GetCookiesInfo() JNI i:%d, cookie:%s, item:%p", i, (*iter).c_str(), item);
			env->SetObjectArrayElement(jStringArray, i, item);

			env->DeleteLocalRef(item);
		}
	}

	return jStringArray;
}

/*
 * Class:     com_qpidnetwork_request_RequestJni
 * Method:    GetCookiesItem
 * Signature: ()["com/qpidnetwork/request/item/CookiesItem";
 */
JNIEXPORT jobjectArray JNICALL Java_com_qpidnetwork_request_RequestJni_GetCookiesItem
  (JNIEnv *env, jclass cls)
{
	FileLog("httprequest","GetCookiesItem() begin");
	jobjectArray jCookiesArray = NULL;
	list<CookiesItem> cookies = HttpClient::GetCookiesItem();
	jclass jItemCls = env->FindClass("com/qpidnetwork/request/item/CookiesItem");
	if(!jItemCls)
	{
		FileLog("httprequest", "GetCookiesItem() JNI jclass is NULL");
		return NULL;
	}

	jmethodID jItemMethod = env->GetMethodID(jItemCls, "<init>", "()V");
	if(!jItemMethod)
	{
		FileLog("httprequest","GetCookiesItem() <init>ID is NULL end");
		env->DeleteLocalRef(jItemCls);
		return NULL;
	}

	jmethodID jItemInitMethod = env->GetMethodID(jItemCls,"init","(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V");
	if(!jItemInitMethod)
	{
		FileLog("httprequest","GetCookiesItem() init()ID is NULL end");
		env->DeleteLocalRef(jItemCls);
		return NULL;
	}

	jCookiesArray = env->NewObjectArray(cookies.size(), jItemCls, NULL);
	FileLog("httprequest", "GetCookiesItem() JNI cookies.size:%d, jCookiesArray:%p", cookies.size(), jCookiesArray);

	if (NULL != jCookiesArray)
	{
		int i = 0;
		for (list<CookiesItem>::const_iterator iter = cookies.begin();
			 iter != cookies.end();
			 iter++, i++)
		{
			jstring domain = env->NewStringUTF((*iter).m_domain.c_str());
			jstring accessOtherWeb = env->NewStringUTF((*iter).m_accessOtherWeb.c_str());
			jstring symbol = env->NewStringUTF((*iter).m_symbol.c_str());
			jstring isSend = env->NewStringUTF((*iter).m_isSend.c_str());
			jstring expiresTime = env->NewStringUTF((*iter).m_expiresTime.c_str());
			jstring cName = env->NewStringUTF((*iter).m_cName.c_str());
			jstring value = env->NewStringUTF((*iter).m_value.c_str());
			jobject objCookiesItem       = env->NewObject(jItemCls, jItemMethod);
			if(!objCookiesItem)
			{
				FileLog("httprequest","GetCookiesItem() objCookiesItem is NULL end");
				env->DeleteLocalRef(jItemCls);
				env->DeleteLocalRef(jCookiesArray);
				return NULL;
			}
			env->CallVoidMethod(objCookiesItem, jItemInitMethod, domain, accessOtherWeb, symbol, isSend, expiresTime, cName, value);
			env->DeleteLocalRef(domain);
			env->DeleteLocalRef(accessOtherWeb);
			env->DeleteLocalRef(symbol);
			env->DeleteLocalRef(isSend);
			env->DeleteLocalRef(expiresTime);
			env->DeleteLocalRef(cName);
			env->DeleteLocalRef(value);
			env->SetObjectArrayElement(jCookiesArray, i, objCookiesItem);
		}
		env->DeleteLocalRef(jItemCls);
	}
	FileLog("httprequest", "GetCookiesItem() JNI end");
	return jCookiesArray;
}

/*
 * Class:     com_qpidnetwork_request_RequestJni
 * Method:    StopRequest
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_com_qpidnetwork_request_RequestJni_StopRequest
  (JNIEnv *env, jclass, jlong requestId) {
	gHttpRequestManager.StopRequest(requestId);
}

/*
 * Class:     com_qpidnetwork_request_RequestJni
 * Method:    StopAllRequest
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_com_qpidnetwork_request_RequestJni_StopAllRequest
  (JNIEnv *env, jclass) {
	gHttpRequestManager.StopAllRequest();
}

/*
 * Class:     com_qpidnetwork_request_RequestJni
 * Method:    GetDeviceId
 * Signature: ()Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_com_qpidnetwork_request_RequestJni_GetDeviceId
  (JNIEnv *env, jclass) {
	string macAddress = "";

	list<IpAddressNetworkInfo> infoList = IPAddress::GetNetworkInfoList();
	if( infoList.size() > 0 && infoList.begin() != infoList.end() ) {
		IpAddressNetworkInfo info = *(infoList.begin());
		macAddress = info.mac;
	}

	char deviceId[128] = {'\0'};
	memset(deviceId, '\0', sizeof(deviceId));
	GetMD5String(macAddress.c_str(), deviceId);

	return env->NewStringUTF(deviceId);
}

/*
 * Class:     com_qpidnetwork_request_RequestJni
 * Method:    SetDeviceId
 * Signature: (Ljava/lang/String;)V
 */
JNIEXPORT void JNICALL Java_com_qpidnetwork_request_RequestJni_SetDeviceId
  (JNIEnv *env, jclass, jstring deviceId) {
	const char *cpDeviceId = env->GetStringUTFChars(deviceId, 0);
	CrashHandler::GetInstance()->SetDeviceId(cpDeviceId);
	env->ReleaseStringUTFChars(deviceId, cpDeviceId);
}

/*
 * Class:     com_qpidnetwork_request_RequestJni
 * Method:    GetDownloadContentLength
 * Signature: (J)IV
 */
JNIEXPORT jint JNICALL Java_com_qpidnetwork_request_RequestJni_GetDownloadContentLength
  (JNIEnv *env, jclass cls, jlong requestId)
{
	jint jiContentLength = 0;
	const HttpRequest* request = gHttpRequestManager.GetRequestById(requestId);
	if (NULL != request) {
		int iContentLength = 0;
		int iRecvLength = 0;
		request->GetRecvDataCount(iContentLength, iRecvLength);
		jiContentLength = iContentLength;
	}
	return jiContentLength;
}

/*
 * Class:     com_qpidnetwork_request_RequestJni
 * Method:    GetRecvLength
 * Signature: (J)IV
 */
JNIEXPORT jint JNICALL Java_com_qpidnetwork_request_RequestJni_GetRecvLength
  (JNIEnv *env, jclass cls, jlong requestId)
{
	jint jiRecvLength = 0;
	const HttpRequest* request = gHttpRequestManager.GetRequestById(requestId);
	if (NULL != request) {
		int iContentLength = 0;
		int iRecvLength = 0;
		request->GetRecvDataCount(iContentLength, iRecvLength);
		jiRecvLength = iRecvLength;
	}
	return jiRecvLength;
}

/*
 * Class:     com_qpidnetwork_request_RequestJni
 * Method:    GetUploadContentLength
 * Signature: (J)IV
 */
JNIEXPORT jint JNICALL Java_com_qpidnetwork_request_RequestJni_GetUploadContentLength
  (JNIEnv *env, jclass cls, jlong requestId)
{
	jint jiContentLength = 0;
	const HttpRequest* request = gHttpRequestManager.GetRequestById(requestId);
	if (NULL != request) {
		int iContentLength = 0;
		int iSendLength = 0;
		request->GetSendDataCount(iContentLength, iSendLength);
		jiContentLength = iContentLength;
	}
	return jiContentLength;
}

/*
 * Class:     com_qpidnetwork_request_RequestJni
 * Method:    GetSendLength
 * Signature: (J)IV
 */
JNIEXPORT jint JNICALL Java_com_qpidnetwork_request_RequestJni_GetSendLength
  (JNIEnv *env, jclass cls, jlong requestId)
{
	jint jiSendLength = 0;
	const HttpRequest* request = gHttpRequestManager.GetRequestById(requestId);
	if (NULL != request) {
		int iContentLength = 0;
		int iSendLength = 0;
		request->GetSendDataCount(iContentLength, iSendLength);
		jiSendLength = iSendLength;
	}
	return jiSendLength;
}
