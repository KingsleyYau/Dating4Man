/*
 * com_qpidnetwork_request_RequestJniOther.cpp
 *
 *  Created on: 2015-3-16
 *      Author: Samson.Fan
 * Description:
 */
#include "com_qpidnetwork_request_RequestJniOther.h"
#include "com_qpidnetwork_request_RequestJni_GobalFunc.h"
#include "RequestOtherController.h"

#include <common/KZip.h>
#include <common/command.h>

#define OS_TYPE "Android"

void OnEmotionConfig(long requestId, bool success, const string& errnum, const string& errmsg, const OtherEmotionConfigItem& item);
void OnGetCount(long requestId, bool success, const string& errnum, const string& errmsg, const OtherGetCountItem& item);
void OnPhoneInfo(long requestId, bool success, const string& errnum, const string& errmsg);
void OnIntegralCheck(long requestId, bool success, const string& errnum, const string& errmsg, const OtherIntegralCheckItem& item);
void OnVersionCheck(long requestId, bool success, const string& errnum, const string& errmsg, const OtherVersionCheckItem& item);
void OnSynConfig(long requestId, bool success, const string& errnum, const string& errmsg, const OtherSynConfigItem& item);
void OnOnlineCount(long requestId, bool success, const string& errnum, const string& errmsg, const OtherOnlineCountList& countList);
void OnUploadCrashLog(long requestId, bool success, const string& errnum, const string& errmsg);
void OnInstallLogs(long requestId, bool success, const string& errnum, const string& errmsg);
static RequestOtherControllerCallback gRequestControllerCallback {
	OnEmotionConfig,
	OnGetCount,
	OnPhoneInfo,
	OnIntegralCheck,
	OnVersionCheck,
	OnSynConfig,
	OnOnlineCount,
	OnUploadCrashLog,
	OnInstallLogs
};
static RequestOtherController gRequestController(&gHttpRequestManager, gRequestControllerCallback);

// ------------------------------ EmotionConfig ---------------------------------
/*
 * Class:     com_qpidnetwork_request_RequestJniOther
 * Method:    EmotionConfig
 * Signature: (Lcom/qpidnetwork/request/OnOtherEmotionConfigCallback;)J
 */
JNIEXPORT jlong JNICALL Java_com_qpidnetwork_request_RequestJniOther_EmotionConfig
  (JNIEnv *env, jclass cls, jobject callback)
{
	jlong requestId = -1;

	// 发出请求
	requestId = gRequestController.EmotionConfig();
	if (requestId != -1) {
		// 保存callback
		jobject jObj = env->NewGlobalRef(callback);
		gCallbackMap.Insert(requestId, jObj);
		FileLog("httprequest", "Other.Native::EmotionConfig() requestId:%lld, callback:%p, jObj:%p", requestId, callback, jObj);
	}
	else {
		FileLog("httprequest", "Other.Native::EmotionConfig() fails. "
				"requestId:%lld", requestId);
	}

	return requestId;
}

void OnEmotionConfig(long requestId, bool success, const string& errnum, const string& errmsg, const OtherEmotionConfigItem& item)
{
	JNIEnv* env = NULL;
	bool isAttachThread = false;
	GetEnv(&env, &isAttachThread);

	FileLog("httprequest", "Other.Native::OnEmotionConfig( success : %s, env:%p, isAttachThread:%d )", success?"true":"false", env, isAttachThread);

	/* turn object to java object here */
	jobject jItem = NULL;
	if (success) {
		jobject jItemObj = NULL;
		JavaItemMap::iterator itr = gJavaItemMap.find(OTHER_EMOTIONCONFIG_ITEM_CLASS);
		if( itr != gJavaItemMap.end() ) {
			jItemObj = itr->second;

			jclass jItemCls = env->GetObjectClass(jItemObj);
			if(NULL != jItemCls) {
				string strInit = "(";
				strInit += "I";										// version
				strInit += "Ljava/lang/String;";					// path
				strInit += "[L";
				strInit += OTHER_EMOTIONCONFIG_TYPE_ITEM_CLASS;		// typeList
				strInit += ";";
				strInit += "[L";
				strInit += OTHER_EMOTIONCONFIG_TAG_ITEM_CLASS;		// tagList
				strInit += ";";
				strInit += "[L";
				strInit += OTHER_EMOTIONCONFIG_EMOTION_ITEM_CLASS;	// manEmotionList
				strInit += ";";
				strInit += "[L";
				strInit += OTHER_EMOTIONCONFIG_EMOTION_ITEM_CLASS;	// ladyEmotionList
				strInit += ";";
				strInit += ")V";

				jmethodID itemInit = env->GetMethodID(jItemCls, "<init>", strInit.c_str());

				// ----- type list -----
				JavaItemMap::iterator typeItr = gJavaItemMap.find(OTHER_EMOTIONCONFIG_TYPE_ITEM_CLASS);
				jobject jTypeItemObj = typeItr->second;
				jclass jTypeItemCls = env->GetObjectClass(jTypeItemObj);

				// create init method
				jmethodID typeInit = env->GetMethodID(jTypeItemCls, "<init>", "("
											"I"						// toFlag
											"Ljava/lang/String;"	// typeId
											"Ljava/lang/String;"	// typeName
											")V");

				// create type list
				jobjectArray jTypeArray = env->NewObjectArray(item.typeList.size(), jTypeItemCls, NULL);
				int iTypeIndex;
				OtherEmotionConfigItem::TypeList::const_iterator typeItemIter;
				for (iTypeIndex = 0, typeItemIter = item.typeList.begin();
						typeItemIter != item.typeList.end();
						typeItemIter++, iTypeIndex++)
				{
					jobject jTypeItem = env->NewObject(jTypeItemCls, typeInit,
							typeItemIter->toflag,
							env->NewStringUTF(typeItemIter->typeId.c_str()),
							env->NewStringUTF(typeItemIter->typeName.c_str())
							);

					env->SetObjectArrayElement(jTypeArray, iTypeIndex, jTypeItem);
					env->DeleteLocalRef(jTypeItem);
				}
				// release
				env->DeleteLocalRef(jTypeItemCls);

				// ----- tag list -----
				JavaItemMap::iterator tagItr = gJavaItemMap.find(OTHER_EMOTIONCONFIG_TAG_ITEM_CLASS);
				jobject jTagItemObj = tagItr->second;
				jclass jTagItemCls = env->GetObjectClass(jTagItemObj);

				// create init method
				jmethodID tagInit = env->GetMethodID(jTagItemCls, "<init>", "("
											"I"						// toFlag
											"Ljava/lang/String;"	// typeId
											"Ljava/lang/String;"	// tagId
											"Ljava/lang/String;"	// tagName
											")V");

				// create tag list
				jobjectArray jTagArray = env->NewObjectArray(item.tagList.size(), jTagItemCls, NULL);
				int iTagIndex;
				OtherEmotionConfigItem::TagList::const_iterator tagItemIter;
				for (iTagIndex = 0, tagItemIter = item.tagList.begin();
						tagItemIter != item.tagList.end();
						tagItemIter++, iTagIndex++)
				{
					jobject jTagItem = env->NewObject(jTagItemCls, tagInit,
							tagItemIter->toflag,
							env->NewStringUTF(tagItemIter->typeId.c_str()),
							env->NewStringUTF(tagItemIter->tagId.c_str()),
							env->NewStringUTF(tagItemIter->tagName.c_str())
							);

					env->SetObjectArrayElement(jTagArray, iTagIndex, jTagItem);
					env->DeleteLocalRef(jTagItem);
				}
				// release
				env->DeleteLocalRef(jTagItemCls);

				// ----- emotin list -----
				JavaItemMap::iterator emotionItr = gJavaItemMap.find(OTHER_EMOTIONCONFIG_EMOTION_ITEM_CLASS);
				jobject jEmotionItemObj = emotionItr->second;
				jclass jEmotionItemCls = env->GetObjectClass(jEmotionItemObj);

				// create init method
				jmethodID emotionInit = env->GetMethodID(jEmotionItemCls, "<init>", "("
											"Ljava/lang/String;"	// fileName
											"D"						// price
											"Z"						// isNew
											"Z"						// isSale
											"I"						// sortId
											"Ljava/lang/String;"	// typeId
											"Ljava/lang/String;"	// tagId
											"Ljava/lang/String;"	// title
											")V");

				// create man emotion list
				jobjectArray jManEmotionArray = env->NewObjectArray(item.manEmotionList.size(), jEmotionItemCls, NULL);
				int iEmotionIndex;
				OtherEmotionConfigItem::EmotionList::const_iterator emotionItemIter;
				for (iEmotionIndex = 0, emotionItemIter = item.manEmotionList.begin();
						emotionItemIter != item.manEmotionList.end();
						emotionItemIter++, iEmotionIndex++)
				{
					jobject jEmotionItem = env->NewObject(jEmotionItemCls, emotionInit,
							env->NewStringUTF(emotionItemIter->fileName.c_str()),
							emotionItemIter->price,
							emotionItemIter->isNew,
							emotionItemIter->isSale,
							emotionItemIter->sortId,
							env->NewStringUTF(emotionItemIter->typeId.c_str()),
							env->NewStringUTF(emotionItemIter->tagId.c_str()),
							env->NewStringUTF(emotionItemIter->title.c_str())
							);

					env->SetObjectArrayElement(jManEmotionArray, iEmotionIndex, jEmotionItem);
					env->DeleteLocalRef(jEmotionItem);
				}

				// create lady emotion list
				jobjectArray jLadyEmotionArray = env->NewObjectArray(item.ladyEmotionList.size(), jEmotionItemCls, NULL);
				for (iEmotionIndex = 0, emotionItemIter = item.ladyEmotionList.begin();
						emotionItemIter != item.ladyEmotionList.end();
						emotionItemIter++, iEmotionIndex++)
				{
					jobject jEmotionItem = env->NewObject(jEmotionItemCls, emotionInit,
							env->NewStringUTF(emotionItemIter->fileName.c_str()),
							emotionItemIter->price,
							emotionItemIter->isNew,
							emotionItemIter->isSale,
							emotionItemIter->sortId,
							env->NewStringUTF(emotionItemIter->typeId.c_str()),
							env->NewStringUTF(emotionItemIter->tagId.c_str()),
							env->NewStringUTF(emotionItemIter->title.c_str())
							);

					env->SetObjectArrayElement(jLadyEmotionArray, iEmotionIndex, jEmotionItem);
					env->DeleteLocalRef(jEmotionItem);
				}

				// release
				env->DeleteLocalRef(jEmotionItemCls);

				jItem = env->NewObject(jItemCls, itemInit,
						item.version,
						env->NewStringUTF(item.path.c_str()),
						jTypeArray,
						jTagArray,
						jManEmotionArray,
						jLadyEmotionArray
						);

				env->DeleteLocalRef(jLadyEmotionArray);
				env->DeleteLocalRef(jTagArray);
				env->DeleteLocalRef(jTypeArray);
				env->DeleteLocalRef(jItemCls);
			}
		}
	}

	/* real callback java */
	jobject jCallbackObj = gCallbackMap.Erase(requestId);
	jclass jCallbackCls = env->GetObjectClass(jCallbackObj);

	string signure = "(ZLjava/lang/String;Ljava/lang/String;";
	signure += "L";
	signure += OTHER_EMOTIONCONFIG_ITEM_CLASS;
	signure += ";";
	signure += ")V";
	jmethodID jCallback = env->GetMethodID(jCallbackCls, "OnOtherEmotionConfig", signure.c_str());

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

	if( jItem != NULL ) {
		env->DeleteLocalRef(jItem);
	}

	ReleaseEnv(isAttachThread);
}

// ------------------------------ GetCount ---------------------------------
/*
 * Class:     com_qpidnetwork_request_RequestJniOther
 * Method:    GetCount
 * Signature: (ZZZZZLcom/qpidnetwork/request/OnOtherGetCountCallback;)J
 */
JNIEXPORT jlong JNICALL Java_com_qpidnetwork_request_RequestJniOther_GetCount
  (JNIEnv *env, jclass cls, jboolean money, jboolean coupon, jboolean regstep, jboolean allowAlbum, jboolean admirerUr, jboolean integral, jobject callback)
{
	jlong requestId = -1;

	// 发出请求
	requestId = gRequestController.GetCount(money, coupon, regstep, allowAlbum, admirerUr, integral);
	if (requestId != -1) {
		// 保存callback
		jobject jObj = env->NewGlobalRef(callback);
		gCallbackMap.Insert(requestId, jObj);
		FileLog("httprequest", "Other.Native::GetCount() requestId:%lld, callback:%p, jObj:%p", requestId, callback, jObj);
	}
	else {
		FileLog("httprequest", "Other.Native::GetCount() fails. "
				"requestId:%lld, money:%d, coupon:%d, regstep:%d, allowAlbum:%d, admirerUr:%d"
				, requestId, money, coupon, regstep, allowAlbum, admirerUr);
	}

	return requestId;
}

void OnGetCount(long requestId, bool success, const string& errnum, const string& errmsg, const OtherGetCountItem& item)
{
	JNIEnv* env = NULL;
	bool isAttachThread = false;
	GetEnv(&env, &isAttachThread);

	FileLog("httprequest", "Other.Native::OnGetCount( success : %s, env:%p, isAttachThread:%d )", success?"true":"false", env, isAttachThread);

	/* turn object to java object here */
	jobject jItem = NULL;
	if (success) {
		jobject jItemObj = NULL;
		JavaItemMap::iterator itr = gJavaItemMap.find(OTHER_GETCOUNT_ITEM_CLASS);
		if( itr != gJavaItemMap.end() ) {
			jItemObj = itr->second;

			jclass jItemCls = env->GetObjectClass(jItemObj);

			if(NULL != jItemCls) {
				jmethodID init = env->GetMethodID(jItemCls, "<init>", "("
							"D"		// money
							"I"		// coupon
							"I"		// integral
							"I"		// regstep
							"Z"		// allowAlbum
							"I"		// admirerUr
							")V");

				jItem = env->NewObject(jItemCls, init,
						item.money,
						item.coupon,
						item.integral,
						item.regstep,
						item.allowAlbum,
						item.admirerUr
						);

				env->DeleteLocalRef(jItemCls);
			}
		}
	}

	/* real callback java */
	jobject jCallbackObj = gCallbackMap.Erase(requestId);

	jclass jCallbackCls = env->GetObjectClass(jCallbackObj);

	string signure = "(ZLjava/lang/String;Ljava/lang/String;";
	signure += "L";
	signure += OTHER_GETCOUNT_ITEM_CLASS;
	signure += ";";
	signure += ")V";
	jmethodID jCallback = env->GetMethodID(jCallbackCls, "OnOtherGetCount", signure.c_str());

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

	if( jItem != NULL ) {
		env->DeleteLocalRef(jItem);
	}

	ReleaseEnv(isAttachThread);
}

// ------------------------------ PhoneInfo ---------------------------------
/*
 * Class:     com_qpidnetwork_request_RequestJniOther
 * Method:    PhoneInfo
 * Signature: (Ljava/lang/String;ILjava/lang/String;ZIDIILjava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;IILjava/lang/String;)J
 */
JNIEXPORT jlong JNICALL Java_com_qpidnetwork_request_RequestJniOther_PhoneInfo
  (JNIEnv *env, jclass cls, jstring manId, jint verCode, jstring verName, jint action, jint siteId
   , jdouble density, jint width, jint height, jstring lineNumber
   , jstring simOptName, jstring simOpt, jstring simCountryIso, jstring simState
   , jint phoneType, jint networkType, jstring deviceId
   , jobject callback)
{
	jlong requestId = -1;
	const char *cpTemp = NULL;

	// manId
	string strManId("");
	if (NULL != manId) {
		cpTemp = env->GetStringUTFChars(manId, 0);
		strManId = cpTemp;
		env->ReleaseStringUTFChars(manId, cpTemp);
	}

	// verName
	string strVerName("");
	if (NULL != verName) {
		cpTemp = env->GetStringUTFChars(verName, 0);
		strVerName = cpTemp;
		env->ReleaseStringUTFChars(verName, cpTemp);
	}

	// lineNumber
	string strLineNumber("");
	if (NULL != lineNumber) {
		cpTemp = env->GetStringUTFChars(lineNumber, 0);
		strLineNumber = cpTemp;
		env->ReleaseStringUTFChars(lineNumber, cpTemp);
	}

	// simOptName
	string strSimOptName("");
	if (NULL != simOptName) {
		cpTemp = env->GetStringUTFChars(simOptName, 0);
		strSimOptName = cpTemp;
		env->ReleaseStringUTFChars(simOptName, cpTemp);
	}

	// simOpt
	string strSimOpt("");
	if (simOpt) {
		cpTemp = env->GetStringUTFChars(simOpt, 0);
		strSimOpt = cpTemp;
		env->ReleaseStringUTFChars(simOpt, cpTemp);
	}

	// simCountryIso
	string strSimCountryIso("");
	if (NULL != simCountryIso) {
		cpTemp = env->GetStringUTFChars(simCountryIso, 0);
		strSimCountryIso = cpTemp;
		env->ReleaseStringUTFChars(simCountryIso, cpTemp);
	}

	// simState
	string strSimState("");
	if (NULL != simState) {
		cpTemp = env->GetStringUTFChars(simState, 0);
		strSimState = cpTemp;
		env->ReleaseStringUTFChars(simState, cpTemp);
	}

	// deviceId
	string strDeviceId("");
	if (NULL != deviceId) {
		cpTemp = env->GetStringUTFChars(deviceId, 0);
		strDeviceId = cpTemp;
		env->ReleaseStringUTFChars(deviceId, cpTemp);
	}

	string strDensityDpi = GetPhoneDensityDPI();
	string strModel = GetPhoneModel();
	string strManufacturer = GetPhoneManufacturer();
	string strOS = OS_TYPE;
	string strRelease = GetPhoneBuildVersion();
	string strSDK = GetPhoneBuildSDKVersion();
	string strLanguage = GetPhoneLocalLanguage();
	string strCountry = GetPhoneLocalRegion();

	// 发出请求
	requestId = gRequestController.PhoneInfo(strManId, verCode, strVerName, action, siteId
			, density, width, height, strDensityDpi, strModel, strManufacturer, strOS, strRelease, strSDK, strLanguage, strCountry
			, strLineNumber , strSimOptName, strSimOpt, strSimCountryIso, strSimState
			, phoneType, networkType, strDeviceId);

	if (requestId != -1) {
		// 保存callback
		jobject jObj = env->NewGlobalRef(callback);
		gCallbackMap.Insert(requestId, jObj);
		FileLog("httprequest", "Other.Native::PhoneInfo() requestId:%lld, callback:%p, jObj:%p", requestId, callback, jObj);
	}
	else {
		FileLog("httprequest", "Other.Native::PhoneInfo() fails. "
				"requestId:%lld", requestId);
	}

	return requestId;
}

void OnPhoneInfo(long requestId, bool success, const string& errnum, const string& errmsg)
{
	JNIEnv* env = NULL;
	bool isAttachThread = false;
	GetEnv(&env, &isAttachThread);

	FileLog("httprequest", "Other.Native::OnPhoneInfo( success : %s, env:%p, isAttachThread:%d )", success?"true":"false", env, isAttachThread);

	/* real callback java */
	jobject jCallbackObj = gCallbackMap.Erase(requestId);

	jclass jCallbackCls = env->GetObjectClass(jCallbackObj);

	string signure = "(ZLjava/lang/String;Ljava/lang/String;)V";
	jmethodID jCallback = env->GetMethodID(jCallbackCls, "OnOtherPhoneInfo", signure.c_str());

	if( jCallbackObj != NULL && jCallback != NULL ) {
		jstring jerrno = env->NewStringUTF(errnum.c_str());
		jstring jerrmsg = env->NewStringUTF(errmsg.c_str());

		env->CallVoidMethod(jCallbackObj, jCallback, success, jerrno, jerrmsg);

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

	ReleaseEnv(isAttachThread);
}


// ------------------------------ IntegralCheck ---------------------------------
/*
 * Class:     com_qpidnetwork_request_RequestJniOther
 * Method:    IntegralCheck
 * Signature: (Ljava/lang/String;Lcom/qpidnetwork/request/OnOtherIntegralCheckCallback;)J
 */
JNIEXPORT jlong JNICALL Java_com_qpidnetwork_request_RequestJniOther_IntegralCheck
  (JNIEnv *env, jclass cls, jstring womanId, jobject callback)
{
	jlong requestId = -1;
	const char *cpTemp = NULL;

	// womanId
	string strWomanId;
	cpTemp = env->GetStringUTFChars(womanId, 0);
	strWomanId = cpTemp;
	env->ReleaseStringUTFChars(womanId, cpTemp);

	// 发出请求
	requestId = gRequestController.IntegralCheck(strWomanId);
	if (requestId != -1) {
		// 保存callback
		jobject jObj = env->NewGlobalRef(callback);
		gCallbackMap.Insert(requestId, jObj);
		FileLog("httprequest", "Other.Native::IntegralCheck() requestId:%lld, callback:%p, jObj:%p", requestId, callback, jObj);
	}
	else {
		FileLog("httprequest", "Other.Native::IntegralCheck() fails. "
				"requestId:%lld, womanId:%s"
				, requestId, strWomanId.c_str());
	}

	return requestId;
}

void OnIntegralCheck(long requestId, bool success, const string& errnum, const string& errmsg, const OtherIntegralCheckItem& item)
{
	JNIEnv* env = NULL;
	bool isAttachThread = false;
	GetEnv(&env, &isAttachThread);

	FileLog("httprequest", "Other.Native::OnIntegralCheck( success : %s, env:%p, isAttachThread:%d )", success?"true":"false", env, isAttachThread);

	/* create java item */
	jobject jItem = NULL;
	if (success) {
		jobject jItemObj = NULL;
		JavaItemMap::iterator itr = gJavaItemMap.find(OTHER_INTEGRALCHECK_ITEM_CLASS);
		if( itr != gJavaItemMap.end() ) {
			jItemObj = itr->second;

			jclass jItemCls = env->GetObjectClass(jItemObj);

			if(NULL != jItemCls) {
				jmethodID init = env->GetMethodID(jItemCls, "<init>", "("
							"I"		// integral
							")V");

				jItem = env->NewObject(jItemCls, init, item.integral);

				env->DeleteLocalRef(jItemCls);
			}
		}
	}

	/* real callback java */
	jobject jCallbackObj = gCallbackMap.Erase(requestId);

	jclass jCallbackCls = env->GetObjectClass(jCallbackObj);

	string signure = "(ZLjava/lang/String;Ljava/lang/String;";
	signure += "L";
	signure += OTHER_INTEGRALCHECK_ITEM_CLASS;
	signure += ";";
	signure += ")V";
	jmethodID jCallback = env->GetMethodID(jCallbackCls, "OnOtherIntegralCheck", signure.c_str());

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

	// delete jItem
	if (jItem != NULL) {
		env->DeleteLocalRef(jItem);
	}

	ReleaseEnv(isAttachThread);
}

// ------------------------------ VersionCheck ---------------------------------
/*
 * Class:     com_qpidnetwork_request_RequestJniOther
 * Method:    VersionCheck
 * Signature: (ILcom/qpidnetwork/request/OnOtherVersionCheckCallback;)J
 */
JNIEXPORT jlong JNICALL Java_com_qpidnetwork_request_RequestJniOther_VersionCheck
  (JNIEnv *env, jclass cls, jint currVer, jobject callback)
{
	jlong requestId = -1;

	// 发出请求
	requestId = gRequestController.VersionCheck(currVer);
	if (requestId != -1) {
		// 保存callback
		jobject jObj = env->NewGlobalRef(callback);
		gCallbackMap.Insert(requestId, jObj);
		FileLog("httprequest", "Other.Native::VersionCheck() requestId:%lld, callback:%p, jObj:%p", requestId, callback, jObj);
	}
	else {
		FileLog("httprequest", "Other.Native::VersionCheck() fails. "
				"requestId:%lld, currVer:%d"
				, requestId, currVer);
	}

	return requestId;
}

void OnVersionCheck(long requestId, bool success, const string& errnum, const string& errmsg, const OtherVersionCheckItem& item)
{
	JNIEnv* env = NULL;
	bool isAttachThread = false;
	GetEnv(&env, &isAttachThread);

	FileLog("httprequest", "Other.Native::OnVersionCheck( success : %s, env:%p, isAttachThread:%d )", success?"true":"false", env, isAttachThread);

	/* create java item */
	jobject jItem = NULL;
	if (success) {
		jobject jItemObj = NULL;
		JavaItemMap::iterator itr = gJavaItemMap.find(OTHER_VERSIONCHECK_ITEM_CLASS);
		if( itr != gJavaItemMap.end() ) {
			jItemObj = itr->second;

			jclass jItemCls = env->GetObjectClass(jItemObj);

			if(NULL != jItemCls) {
				jmethodID init = env->GetMethodID(jItemCls, "<init>", "("
							"I"						// verCode
							"Ljava/lang/String;"	// verName
							"Ljava/lang/String;"	// verDesc
							"Ljava/lang/String;"	// url
							"Ljava/lang/String;"	// storeUrl
							"Ljava/lang/String;"	// pubTime
							"I"						// checkTime
							")V");

				jstring jstrVersionName = env->NewStringUTF(item.versionName.c_str());
				jstring jstrVersionDesc = env->NewStringUTF(item.versionDesc.c_str());
				jstring jstrUrl = env->NewStringUTF(item.url.c_str());
				jstring jstrStoreUrl = env->NewStringUTF(item.storeUrl.c_str());
				jstring jstrPubTime = env->NewStringUTF(item.pubTime.c_str());
				jItem = env->NewObject(jItemCls, init,
							item.versionCode,
							jstrVersionName,
							jstrVersionDesc,
							jstrUrl,
							jstrStoreUrl,
							jstrPubTime,
							item.checkTime
							);

				env->DeleteLocalRef(jstrVersionName);
				env->DeleteLocalRef(jstrVersionDesc);
				env->DeleteLocalRef(jstrUrl);
				env->DeleteLocalRef(jstrStoreUrl);
				env->DeleteLocalRef(jstrPubTime);
				env->DeleteLocalRef(jItemCls);
			}
		}
	}

	/* real callback java */
	jobject jCallbackObj = gCallbackMap.Erase(requestId);

	jclass jCallbackCls = env->GetObjectClass(jCallbackObj);

	string signure = "(ZLjava/lang/String;Ljava/lang/String;";
	signure += "L";
	signure += OTHER_VERSIONCHECK_ITEM_CLASS;
	signure += ";";
	signure += ")V";
	jmethodID jCallback = env->GetMethodID(jCallbackCls, "OnOtherVersionCheck", signure.c_str());

	if( jCallbackObj != NULL && jCallback != NULL ) {
		jstring jerrno = env->NewStringUTF(errnum.c_str());
		jstring jerrmsg = env->NewStringUTF(errmsg.c_str());

		env->CallVoidMethod(jCallbackObj, jCallback, success, jerrno, jerrmsg, jItem);

		env->DeleteLocalRef(jerrno);
		env->DeleteLocalRef(jerrmsg);
	}

	// delete callback object & class
	if (jCallbackCls != NULL) {
		env->DeleteLocalRef(jCallbackCls);
	}
	if (jCallbackObj != NULL) {
		env->DeleteGlobalRef(jCallbackObj);
	}

	// delete jItem
	if (jItem != NULL) {
		env->DeleteLocalRef(jItem);
	}

	ReleaseEnv(isAttachThread);
}

// ------------------------------ SynConfig ---------------------------------
/*
 * Class:     com_qpidnetwork_request_RequestJniOther
 * Method:    SynConfig
 * Signature: (Lcom/qpidnetwork/request/OnOtherSynConfigCallback;)J
 */
JNIEXPORT jlong JNICALL Java_com_qpidnetwork_request_RequestJniOther_SynConfig
  (JNIEnv *env, jclass cls, jobject callback)
{
	jlong requestId = -1;

	// 发出请求
	requestId = gRequestController.SynConfig();
	if (requestId != -1) {
		// 保存callback
		jobject jObj = env->NewGlobalRef(callback);
		gCallbackMap.Insert(requestId, jObj);
		FileLog("httprequest", "Other.Native::SynConfig() requestId:%lld, callback:%p, jObj:%p", requestId, callback, jObj);
	}
	else {
		FileLog("httprequest", "Other.Native::SynConfig() fails. "
				"requestId:%lld", requestId);
	}

	return requestId;
}

void CreateSynConfigSiteJItem(JNIEnv* env, const OtherSynConfigItem::SiteItem& item, jobject& jItem)
{
	jItem = NULL;
	jobject jItemObj = NULL;
	JavaItemMap::iterator itr = gJavaItemMap.find(OTHER_SYNCONFIG_SITE_ITEM_CLASS);
	if( itr != gJavaItemMap.end() ) {
		jItemObj = itr->second;
		jclass jItemCls = env->GetObjectClass(jItemObj);
		if(NULL != jItemCls) {
			// get method
			jmethodID itemInit = env->GetMethodID(jItemCls, "<init>", "("
					"Ljava/lang/String;"	// host
					"[Ljava/lang/String;"	// proxy host
					"I"						// port
					"D"						// minChat
					"D"						// minEmf
					"[Ljava/lang/String;"	// countryList
					")V");

			// create proxy host list
			jclass jStringCls = env->FindClass("java/lang/String");
			jobjectArray jProxyHostArray = env->NewObjectArray(item.proxyHostList.size(), jStringCls, NULL);
			OtherSynConfigItem::ProxyHostList::const_iterator proxyHostIter;
			int iProxyHostIndex = 0;
			for (proxyHostIter = item.proxyHostList.begin();
				 proxyHostIter != item.proxyHostList.end();
				 proxyHostIter++, iProxyHostIndex++)
			{
				jstring proxyHost = env->NewStringUTF((*proxyHostIter).c_str());
				env->SetObjectArrayElement(jProxyHostArray, iProxyHostIndex, proxyHost);
				env->DeleteLocalRef(proxyHost);
			}

			// create country list
			jobjectArray jCountryArray = env->NewObjectArray(item.countryList.size(), jStringCls, NULL);
			OtherSynConfigItem::CountryList::const_iterator countryIter;
			int iCountryIndex = 0;
			for (countryIter = item.countryList.begin();
				 countryIter != item.countryList.end();
				 countryIter++, iCountryIndex++)
			{
				jstring country = env->NewStringUTF((*countryIter).c_str());
				env->SetObjectArrayElement(jCountryArray, iCountryIndex, country);
				env->DeleteLocalRef(country);
			}

			jstring host = env->NewStringUTF(item.host.c_str());

			// create jItem
			jItem = env->NewObject(jItemCls, itemInit,
						host,
						jProxyHostArray,
						item.port,
						item.minChat,
						item.minEmf,
						jCountryArray
						);

			// delete proxy host list
			env->DeleteLocalRef(jProxyHostArray);
			// delete country list
			env->DeleteLocalRef(jCountryArray);
			// delete jStringCls
			env->DeleteLocalRef(host);
			// delete jItemCls
//			env->DeleteLocalRef(jItemCls);
		}
	}
}

void CreateSynConfigPublicJItem(JNIEnv* env, const OtherSynConfigItem::PublicItem& item, jobject& jItem)
{
	jItem = NULL;
	jobject jItemObj = NULL;
	JavaItemMap::iterator itr = gJavaItemMap.find(OTHER_SYNCONFIG_PUBLIC_ITEM_CLASS);
	if( itr != gJavaItemMap.end() ) {
		jItemObj = itr->second;
		jclass jItemCls = env->GetObjectClass(jItemObj);
		if(NULL != jItemCls) {
			// get method
			jmethodID itemInit = env->GetMethodID(jItemCls, "<init>", "("
					"I"						// vgVer
					"I"						// apkVerCode
					"Ljava/lang/String;"	// apkVerName
					"Z"						// apkForceUpdate
					"Z"						// facebook_enable
					"Ljava/lang/String;"	// apkFileVerify
					"Ljava/lang/String;"	// url
					"Ljava/lang/String;"	// storeUrl
					"Ljava/lang/String;"	// chatVoiceUrl
					"Ljava/lang/String;"	// addCreditsUrl
					"Ljava/lang/String;"	// addCredits2Url
					")V");

			jstring apkVerName = env->NewStringUTF(item.apkVerName.c_str());
			jstring apkFileVerify = env->NewStringUTF(item.apkFileVerify.c_str());
			jstring apkVerURL = env->NewStringUTF(item.apkVerURL.c_str());
			jstring apkStoreURL = env->NewStringUTF(item.apkStoreURL.c_str());
			jstring chatVoiceHostUrl = env->NewStringUTF(item.chatVoiceHostUrl.c_str());
			jstring addCreditsUrl = env->NewStringUTF(item.addCreditsUrl.c_str());
			jstring addCredits2Url = env->NewStringUTF(item.addCredits2Url.c_str());

			// create jItem
			jItem = env->NewObject(jItemCls, itemInit,
						item.vgVer,
						item.apkVerCode,
						apkVerName,
						item.apkForceUpdate,
						item.facebook_enable,
						apkFileVerify,
						apkVerURL,
						apkStoreURL,
						chatVoiceHostUrl,
						addCreditsUrl,
						addCredits2Url
						);

			// delete jItemCls
			env->DeleteLocalRef(apkVerName);
			env->DeleteLocalRef(apkFileVerify);
			env->DeleteLocalRef(apkVerURL);
			env->DeleteLocalRef(apkStoreURL);
			env->DeleteLocalRef(chatVoiceHostUrl);
			env->DeleteLocalRef(addCreditsUrl);
			env->DeleteLocalRef(addCredits2Url);
		}
	}
}

void OnSynConfig(long requestId, bool success, const string& errnum, const string& errmsg, const OtherSynConfigItem& item)
{
	JNIEnv* env = NULL;
	bool isAttachThread = false;
	GetEnv(&env, &isAttachThread);

	FileLog("httprequest", "Other.Native::OnSynConfig( success : %s, env:%p, isAttachThread:%d )", success?"true":"false", env, isAttachThread);

	/* turn object to java object here */
	jobject jItem = NULL;
	if (success) {
		jobject jItemObj = NULL;
		JavaItemMap::iterator itr = gJavaItemMap.find(OTHER_SYNCONFIG_ITEM_CLASS);
		if( itr != gJavaItemMap.end() ) {
			jItemObj = itr->second;

			jclass jItemCls = env->GetObjectClass(jItemObj);
			if(NULL != jItemCls) {
				string strInit = "(";
				strInit += "L";
				strInit += OTHER_SYNCONFIG_SITE_ITEM_CLASS;		// CL
				strInit += ";";
				strInit += "L";
				strInit += OTHER_SYNCONFIG_SITE_ITEM_CLASS;		// IDA
				strInit += ";";
				strInit += "L";
				strInit += OTHER_SYNCONFIG_SITE_ITEM_CLASS;		// CH
				strInit += ";";
				strInit += "L";
				strInit += OTHER_SYNCONFIG_SITE_ITEM_CLASS;		// LA
				strInit += ";";
				strInit += "L";
				strInit += OTHER_SYNCONFIG_PUBLIC_ITEM_CLASS;	// public
				strInit += ";";
				strInit += ")V";

				jmethodID itemInit = env->GetMethodID(jItemCls, "<init>", strInit.c_str());

				FileLog("httprequest", "Other.Native::OnSynConfig() create site item");
				// Create site item
				// cl
				jobject jClItem = NULL;
				CreateSynConfigSiteJItem(env, item.cl, jClItem);
				// ida
				jobject jIdaItem = NULL;
				CreateSynConfigSiteJItem(env, item.ida, jIdaItem);
				// ch
				jobject jChItem = NULL;
				CreateSynConfigSiteJItem(env, item.ch, jChItem);
				// la
				jobject jLaItem = NULL;
				CreateSynConfigSiteJItem(env, item.la, jLaItem);

				FileLog("httprequest", "Other.Native::OnSynConfig() create public item");
				// create public item
				jobject jPublicItem = NULL;
				CreateSynConfigPublicJItem(env, item.pub, jPublicItem);

				FileLog("httprequest", "Other.Native::OnSynConfig() create item");
				jItem = env->NewObject(jItemCls, itemInit,
						jClItem,
						jIdaItem,
						jChItem,
						jLaItem,
						jPublicItem
						);

				env->DeleteLocalRef(jLaItem);
				env->DeleteLocalRef(jChItem);
				env->DeleteLocalRef(jIdaItem);
				env->DeleteLocalRef(jClItem);
				env->DeleteLocalRef(jPublicItem);
//				env->DeleteLocalRef(jItemCls);
			}
		}
	}

	/* real callback java */
	jobject jCallbackObj = gCallbackMap.Erase(requestId);
	jclass jCallbackCls = env->GetObjectClass(jCallbackObj);

	string signure = "(ZLjava/lang/String;Ljava/lang/String;";
	signure += "L";
	signure += OTHER_SYNCONFIG_ITEM_CLASS;
	signure += ";";
	signure += ")V";
	jmethodID jCallback = env->GetMethodID(jCallbackCls, "OnOtherSynConfig", signure.c_str());
	FileLog("httprequest", "Other.Native::OnSynConfig() callback begin");
	if( jCallbackObj != NULL && jCallback != NULL ) {
		jstring jerrno = env->NewStringUTF(errnum.c_str());
		jstring jerrmsg = env->NewStringUTF(errmsg.c_str());

		FileLog("httprequest", "Other.Native::OnSynConfig() callback here, jCallbackObj:%p, jCallback:%p, jItem:%p"
				, jCallbackObj, jCallback, jItem);
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

	// delete jItem
	if( jItem != NULL ) {
		env->DeleteLocalRef(jItem);
	}

	ReleaseEnv(isAttachThread);
}

// ------------------------------ OnlineCount ---------------------------------
/*
 * Class:     com_qpidnetwork_request_RequestJniOther
 * Method:    OnlineCount
 * Signature: (ILcom/qpidnetwork/request/OnOtherOnlineCountCallback;)J
 */
JNIEXPORT jlong JNICALL Java_com_qpidnetwork_request_RequestJniOther_OnlineCount
  (JNIEnv *env, jclass cls, jint site, jobject callback)
{
	jlong requestId = -1;

	// 发出请求
	requestId = gRequestController.OnlineCount(site);
	if (requestId != -1) {
		// 保存callback
		jobject jObj = env->NewGlobalRef(callback);
		gCallbackMap.Insert(requestId, jObj);
		FileLog("httprequest", "Other.Native::OnlineCount() requestId:%lld, callback:%p, jObj:%p, site:%d", requestId, callback, jObj, site);
	}
	else {
		FileLog("httprequest", "Other.Native::OnlineCount() fails. "
				"requestId:%lld", requestId);
	}

	return requestId;
}

void OnOnlineCount(long requestId, bool success, const string& errnum, const string& errmsg, const OtherOnlineCountList& countList)
{
	JNIEnv* env = NULL;
	bool isAttachThread = false;
	GetEnv(&env, &isAttachThread);

	FileLog("httprequest", "Other.Native::OnOnlineCount( success : %s, env:%p, isAttachThread:d )", success?"true":"false", env, isAttachThread);

	/* turn object to java object here */
	jobjectArray jCountArray = NULL;
	jclass jItemCls = GetJClass(env, OTHER_ONLINECOUNT_ITEM_CLASS);
	if(NULL != jItemCls) {
		// get method
		jmethodID itemInit = env->GetMethodID(jItemCls, "<init>", "("
				"I"						// siteId
				"I"						// onlineCount
				")V");


		jCountArray = env->NewObjectArray(countList.size(), jItemCls, NULL);
		if (NULL != jCountArray) {
			int iIndex;
			OtherOnlineCountList::const_iterator iter;
			for (iIndex = 0, iter = countList.begin();
					iter != countList.end();
					iter++)
			{
				// create jItem
				jobject jItem = env->NewObject(jItemCls, itemInit,
							iter->siteId,
							iter->onlineCount
							);
				if (jItem != NULL) {
					env->SetObjectArrayElement(jCountArray, iIndex, jItem);
					iIndex++;
				}
				env->DeleteLocalRef(jItem);
			}
		}
	}

	/* real callback java */
	jobject jCallbackObj = gCallbackMap.Erase(requestId);
	jclass jCallbackCls = env->GetObjectClass(jCallbackObj);

	string signure = "(ZLjava/lang/String;Ljava/lang/String;";
	signure += "[L";
	signure += OTHER_ONLINECOUNT_ITEM_CLASS;
	signure += ";";
	signure += ")V";
	jmethodID jCallback = env->GetMethodID(jCallbackCls, "OnOtherOnlineCount", signure.c_str());

	FileLog("httprequest", "Other.Native::OnOtherOnlineCount() callback");
	if( jCallbackObj != NULL && jCallback != NULL ) {
		jstring jerrno = env->NewStringUTF(errnum.c_str());
		jstring jerrmsg = env->NewStringUTF(errmsg.c_str());

		env->CallVoidMethod(jCallbackObj, jCallback, success, jerrno, jerrmsg, jCountArray);

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

	// delete jItem
	if( jCountArray != NULL ) {
		env->DeleteLocalRef(jCountArray);
	}

	ReleaseEnv(isAttachThread);
}

// ------------------------------ UploadCrashLog ---------------------------------

void OnUploadCrashLog(long requestId, bool success, const string& errnum, const string& errmsg) {
	FileLog("httprequest", "Other.Native::OnUploadCrashLog( success : %s )", success?"true":"false");

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
	FileLog("httprequest", "Other.Native::OnUploadCrashLog( callbackCls : %p, callback : %p, signure : %s )",
			callbackCls, callback, signure.c_str());

	if( callbackObj != NULL && callback != NULL ) {
		jstring jerrno = env->NewStringUTF(errnum.c_str());
		jstring jerrmsg = env->NewStringUTF(errmsg.c_str());

		FileLog("httprequest", "Other.Native::OnUploadCrashLog( CallObjectMethod )");

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
 * Class:     com_qpidnetwork_request_RequestJniOther
 * Method:    UploadCrashLog
 * Signature: (Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Lcom/qpidnetwork/request/OnRequestCallback;)J
 */
JNIEXPORT jlong JNICALL Java_com_qpidnetwork_request_RequestJniOther_UploadCrashLog
  (JNIEnv *env, jclass, jstring deviceId, jstring directory, jstring tmpDirectory, jobject callback) {
	jlong requestId = -1;

	const char *cpDeviceId = env->GetStringUTFChars(deviceId, 0);
	const char *cpDirectory = env->GetStringUTFChars(directory, 0);
	const char *cpTmpDirectory = env->GetStringUTFChars(tmpDirectory, 0);

	FileLog("httprequest", "UploadCrashLog ( directory : %s, tmpDirectory : %s ) ", cpDirectory, cpTmpDirectory);

	time_t stm = time(NULL);
	struct tm tTime;
	localtime_r(&stm, &tTime);
    char pZipFileName[1024] = {'\0'};
    snprintf(pZipFileName, sizeof(pZipFileName), "%s/crash-%d-%02d-%02d_[%02d-%02d-%02d].zip", \
    		cpTmpDirectory, tTime.tm_year + 1900, tTime.tm_mon + 1, \
    		tTime.tm_mday, tTime.tm_hour, tTime.tm_min, tTime.tm_sec);

    // create zip
    KZip zip;
	string comment = "";
	const char password[] = {
			0x51, 0x70, 0x69, 0x64, 0x5F, 0x44, 0x61, 0x74, 0x69, 0x6E, 0x67, 0x00
	}; // Qpid_Dating

	bool bFlag = zip.CreateZipFromDir(cpDirectory, pZipFileName, password, comment);

	FileLog("httprequest", "UploadCrashLog ( pZipFileName : %s  zip  : %s ) ", pZipFileName, bFlag?"ok":"fail");

	if( bFlag ) {
		// upload to server
		requestId = gRequestController.UploadCrashLog(cpDeviceId, pZipFileName);
		if (requestId != -1) {
			// callback
			jobject jObj = env->NewGlobalRef(callback);
			gCallbackMap.Insert(requestId, jObj);
		}
	}

	env->ReleaseStringUTFChars(deviceId, cpDeviceId);
	env->ReleaseStringUTFChars(directory, cpDirectory);
	env->ReleaseStringUTFChars(tmpDirectory, cpTmpDirectory);

	return requestId;
}

// ------------------------------ InstallLogs ---------------------------------
void OnInstallLogs(long requestId, bool success, const string& errnum, const string& errmsg) {
	FileLog("httprequest", "Other.Native::OnInstallLogs( success : %s )", success?"true":"false");

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
	FileLog("httprequest", "Other.Native::OnInstallLogs( callbackCls : %p, callback : %p, signure : %s )",
			callbackCls, callback, signure.c_str());

	if( callbackObj != NULL && callback != NULL ) {
		jstring jerrno = env->NewStringUTF(errnum.c_str());
		jstring jerrmsg = env->NewStringUTF(errmsg.c_str());

		FileLog("httprequest", "Other.Native::OnInstallLogs( CallObjectMethod )");

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
 * Class:     com_qpidnetwork_request_RequestJniOther
 * Method:    InstallLogs
 * Signature: (Ljava/lang/String;IIJJILjava/lang/String;Lcom/qpidnetwork/request/OnRequestCallback;)J
 */
JNIEXPORT jlong JNICALL Java_com_qpidnetwork_request_RequestJniOther_InstallLogs
  (JNIEnv *env, jclass cls, jstring deviceId, jint width, jint height, jlong installTime, jlong submitTime, jint verCode, jstring referrer, jobject callback)
{
	jlong requestId = -1;

	const char *cpDeviceId = env->GetStringUTFChars(deviceId, 0);
	const char *cpReferrer = env->GetStringUTFChars(referrer, 0);

	string model = GetPhoneModel();
	string manufacturer = GetPhoneManufacturer();
	string os = OS_TYPE;
	string release = GetPhoneBuildVersion();
	string sdk = GetPhoneBuildSDKVersion();

	requestId = gRequestController.InstallLogs(
					cpDeviceId, installTime, submitTime, verCode
					, model, manufacturer, os, release, sdk, width, height, cpReferrer);
	if (requestId != -1) {
		// callback
		jobject jObj = env->NewGlobalRef(callback);
		gCallbackMap.Insert(requestId, jObj);
	}

	env->ReleaseStringUTFChars(deviceId, cpDeviceId);
	env->ReleaseStringUTFChars(referrer, cpReferrer);

	return requestId;
}
