/*
 * com_qpidnetwork_request_RequestJniEMF.cpp
 *
 *  Created on: 2015-3-12
 *      Author: Samson.Fan
 * Description:
 */
#include "com_qpidnetwork_request_RequestJniVideoShow.h"
#include "com_qpidnetwork_request_RequestJni_GobalFunc.h"
#include "RequestVideoShowController.h"

void OnVideoList(long requestId, bool success, const string& errnum, const string& errmsg, int pageIndex, int pageSize, int dataCount, const VSVideoList& list);
void OnVideoDetail(long requestId, bool success, const string& errnum, const string& errmsg, const VSVideoDetailList& list);
void OnPlayVideo(long requestId, bool success, const string& errnum, const string& errmsg, const VSPlayVideoItem& item);
void OnWatchedVideoList(long requestId, bool success, const string& errnum, const string& errmsg, int pageIndex, int pageSize, int dataCount, const VSWatchedVideoList& list);
void OnSaveVideo(long requestId, bool success, const string& errnum, const string& errmsg);
void OnRemoveVideo(long requestId, bool success, const string& errnum, const string& errmsg);
void OnSavedVideoList(long requestId, bool success, const string& errnum, const string& errmsg, int pageIndex, int pageSize, int dataCount, const VSSavedVideoList& list);
static RequestVideoShowControllerCallback gRequestControllerCallback {
	OnVideoList,
	OnVideoDetail,
	OnPlayVideo,
	OnWatchedVideoList,
	OnSaveVideo,
	OnRemoveVideo,
	OnSavedVideoList
};
static RequestVideoShowController gRequestController(&gHttpRequestManager, gRequestControllerCallback);

// ------------------------------ VideoList ---------------------------------
/*
 * Class:     com_qpidnetwork_request_RequestJniVideoShow
 * Method:    VideoList
 * Signature: (IIIILcom/qpidnetwork/request/RequestJniVideoShow/OrderByType;Lcom/qpidnetwork/request/OnVSVideoListCallback;)J
 */
JNIEXPORT jlong JNICALL Java_com_qpidnetwork_request_RequestJniVideoShow_VideoList
  (JNIEnv *env, jclass cls, jint pageIndex, jint pageSize, jint age1, jint age2, jint orderBy, jobject callback)
{
	jlong requestId = -1;

	// 发出请求
	requestId = gRequestController.VideoList(pageIndex, pageSize, age1, age2, orderBy);
	if (requestId != -1) {
		// 保存callback
		jobject jObj = env->NewGlobalRef(callback);
		gCallbackMap.Insert(requestId, jObj);
		FileLog("httprequest", "VideoShow.VideoShow.Native::VideoList() requestId:%lld, callback:%p, jObj:%p", requestId, callback, jObj);
	}
	else {
		FileLog("httprequest", "VideoShow.Native::VideoList() fails. "
				"requestId:%lld, pageIndex:%d, pageSize:%d, age1:%d, age2:%d, orderBy:%d"
			, requestId, pageIndex, pageSize, age1, age2, orderBy);
	}

	return requestId;
}

void OnVideoList(long requestId, bool success, const string& errnum, const string& errmsg, int pageIndex, int pageSize, int dataCount, const VSVideoList& list)
{
	FileLog("httprequest", "VideoShow.Native::OnVideoList( success : %s )", success?"true":"false");

	/* turn object to java object here */
	JNIEnv* env;
	jint iRet = JNI_ERR;
	gJavaVM->GetEnv((void**)&env, JNI_VERSION_1_4);
	if( env == NULL ) {
		iRet = gJavaVM->AttachCurrentThread((JNIEnv **)&env, NULL);
	}

	jobjectArray jItemArray = NULL;
	if (success) {
		JavaItemMap::iterator itr = gJavaItemMap.find(VS_VIDEOLIST_ITEM_CLASS);
		if( itr != gJavaItemMap.end() ) {
			jobject jItemObj = itr->second;
			jclass jItemCls = env->GetObjectClass(jItemObj);
			if(NULL != jItemCls) {
				jItemArray = env->NewObjectArray(list.size(), jItemCls, NULL);

				int i = 0;
				for(VSVideoList::const_iterator itr = list.begin();
					itr != list.end();
					itr++, i++)
				{
					jmethodID init = env->GetMethodID(jItemCls, "<init>", "("
							"Ljava/lang/String;"	// videoid
							"Ljava/lang/String;"	// time
							"Ljava/lang/String;"	// thumbURL
							"Ljava/lang/String;"	// womanid
							"Ljava/lang/String;"	// firstname
							"I"						// age
							"Ljava/lang/String;"	// weight
							"Ljava/lang/String;"	// height
							"Ljava/lang/String;"	// country
							"Ljava/lang/String;"	// province
							")V");

					jobject jItem = env->NewObject(jItemCls, init,
							env->NewStringUTF(itr->videoId.c_str()),
							env->NewStringUTF(itr->time.c_str()),
							env->NewStringUTF(itr->thumbURL.c_str()),
							env->NewStringUTF(itr->womanId.c_str()),
							env->NewStringUTF(itr->firstname.c_str()),
							itr->age,
							env->NewStringUTF(itr->weight.c_str()),
							env->NewStringUTF(itr->height.c_str()),
							env->NewStringUTF(itr->country.c_str()),
							env->NewStringUTF(itr->province.c_str())
							);

					env->SetObjectArrayElement(jItemArray, i, jItem);

					env->DeleteLocalRef(jItem);
				}
				env->DeleteLocalRef(jItemCls);
			}
		}
	}

	/* real callback java */
	jobject jCallbackObj = gCallbackMap.Erase(requestId);
	jclass jCallbackCls = env->GetObjectClass(jCallbackObj);

	string signure = "(ZLjava/lang/String;Ljava/lang/String;III";
	signure += "[L";
	signure += VS_VIDEOLIST_ITEM_CLASS;
	signure += ";";
	signure += ")V";
	jmethodID jCallback = env->GetMethodID(jCallbackCls, "OnVSVideoList", signure.c_str());

	if( jCallbackObj != NULL && jCallback != NULL ) {
		jstring jerrno = env->NewStringUTF(errnum.c_str());
		jstring jerrmsg = env->NewStringUTF(errmsg.c_str());

		env->CallVoidMethod(jCallbackObj, jCallback, success, jerrno, jerrmsg, pageIndex, pageSize, dataCount, jItemArray);

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

	if( jItemArray != NULL ) {
		env->DeleteLocalRef(jItemArray);
	}

	if( iRet == JNI_OK ) {
		gJavaVM->DetachCurrentThread();
	}
}

// ------------------------------ VideoDetail ---------------------------------
/*
 * Class:     com_qpidnetwork_request_RequestJniVideoShow
 * Method:    VideoDetail
 * Signature: (Ljava/lang/String;)J
 */
JNIEXPORT jlong JNICALL Java_com_qpidnetwork_request_RequestJniVideoShow_VideoDetail
  (JNIEnv *env, jclass cls, jstring womanId, jobject callback)
{
	jlong requestId = -1;

	// 生成转换的womanId字符串
	string strWomanId("");
	const char *cpWomanId = env->GetStringUTFChars(womanId, 0);
	strWomanId = cpWomanId;
	env->ReleaseStringUTFChars(womanId, cpWomanId);

	// 发出请求
	requestId = gRequestController.VideoDetail(strWomanId);
	if (requestId != -1) {
		// 保存callback
		jobject jObj = env->NewGlobalRef(callback);
		gCallbackMap.Insert(requestId, jObj);
		FileLog("httprequest", "VideoShow.Native::VideoDetail() requestId:%lld, callback:%p, jObj:%p", requestId, callback, jObj);
	}
	else {
		FileLog("httprequest", "VideoShow.Native::VideoDetail() fails. "
				"requestId:%lld, womanId:%s"
			, requestId, strWomanId.c_str());
	}

	return requestId;
}

void OnVideoDetail(long requestId, bool success, const string& errnum, const string& errmsg, const VSVideoDetailList& list)
{
	FileLog("httprequest", "VideoShow.Native::OnVideoDetail( success : %s )", success?"true":"false");

	/* turn object to java object here */
	JNIEnv* env;
	jint iRet = JNI_ERR;
	gJavaVM->GetEnv((void**)&env, JNI_VERSION_1_4);
	if( env == NULL ) {
		iRet = gJavaVM->AttachCurrentThread((JNIEnv **)&env, NULL);
	}

	jobjectArray jItemArray = NULL;
	if (success) {
		JavaItemMap::iterator itr = gJavaItemMap.find(VS_VIDEODETAIL_ITEM_CLASS);
		if( itr != gJavaItemMap.end() ) {
			jobject jItemObj = itr->second;
			jclass jItemCls = env->GetObjectClass(jItemObj);
			if(NULL != jItemCls) {
				jItemArray = env->NewObjectArray(list.size(), jItemCls, NULL);

				int i = 0;
				for(VSVideoDetailList::const_iterator itr = list.begin();
					itr != list.end();
					itr++, i++)
				{
					jmethodID init = env->GetMethodID(jItemCls, "<init>", "("
							"Ljava/lang/String;"	// id
							"Ljava/lang/String;"	// title
							"Ljava/lang/String;"	// womanid
							"Ljava/lang/String;"	// thumbURL
							"Ljava/lang/String;"	// time
							"Ljava/lang/String;"	// photoURL
							"Z"						// videoFav
							"Ljava/lang/String;"	// videoSize
							"Ljava/lang/String;"	// transcription
							"Ljava/lang/String;"	// viewtime1
							"Ljava/lang/String;"	// viewtime2
							")V");

					jobject jItem = env->NewObject(jItemCls, init,
							env->NewStringUTF(itr->id.c_str()),
							env->NewStringUTF(itr->title.c_str()),
							env->NewStringUTF(itr->womanId.c_str()),
							env->NewStringUTF(itr->thumbURL.c_str()),
							env->NewStringUTF(itr->time.c_str()),
							env->NewStringUTF(itr->photoURL.c_str()),
							itr->videoFav,
							env->NewStringUTF(itr->videoSize.c_str()),
							env->NewStringUTF(itr->transcription.c_str()),
							env->NewStringUTF(itr->viewTime1.c_str()),
							env->NewStringUTF(itr->viewTime2.c_str())
							);

					env->SetObjectArrayElement(jItemArray, i, jItem);

					env->DeleteLocalRef(jItem);
				}
				env->DeleteLocalRef(jItemCls);
			}
		}
	}

	/* real callback java */
	jobject jCallbackObj = gCallbackMap.Erase(requestId);
	jclass jCallbackCls = env->GetObjectClass(jCallbackObj);

	string signure = "(ZLjava/lang/String;Ljava/lang/String;";
	signure += "[L";
	signure += VS_VIDEODETAIL_ITEM_CLASS;
	signure += ";";
	signure += ")V";
	jmethodID jCallback = env->GetMethodID(jCallbackCls, "OnVSVideoDetail", signure.c_str());

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

	if( jItemArray != NULL ) {
		env->DeleteLocalRef(jItemArray);
	}

	if( iRet == JNI_OK ) {
		gJavaVM->DetachCurrentThread();
	}
}

// ------------------------------ PlayVideo ---------------------------------
/*
 * Class:     com_qpidnetwork_request_RequestJniVideoShow
 * Method:    PlayVideo
 * Signature: (Ljava/lang/String;Ljava/lang/String;)J
 */
JNIEXPORT jlong JNICALL Java_com_qpidnetwork_request_RequestJniVideoShow_PlayVideo
  (JNIEnv *env, jclass cls, jstring womanId, jstring videoId, jobject callback)
{
	jlong requestId = -1;

	// 生成转换的womanId字符串
	string strWomanId("");
	const char *cpWomanId = env->GetStringUTFChars(womanId, 0);
	strWomanId = cpWomanId;
	env->ReleaseStringUTFChars(womanId, cpWomanId);
	// 生成转换的videoId字符串
	string strVideoId("");
	const char *cpVideoId = env->GetStringUTFChars(videoId, 0);
	strVideoId = cpVideoId;
	env->ReleaseStringUTFChars(videoId, cpVideoId);

	// 发出请求
	requestId = gRequestController.PlayVideo(strWomanId, strVideoId);
	if (requestId != -1) {
		// 保存callback
		jobject jObj = env->NewGlobalRef(callback);
		gCallbackMap.Insert(requestId, jObj);
		FileLog("httprequest", "VideoShow.Native::PlayVideo() requestId:%lld, callback:%p, jObj:%p", requestId, callback, jObj);
	}
	else {
		FileLog("httprequest", "VideoShow.Native::PlayVideo() fails. "
				"requestId:%lld, womanId:%s, videoId:%s"
			, requestId, strWomanId.c_str(), strVideoId.c_str());
	}

	return requestId;
}

void OnPlayVideo(long requestId, bool success, const string& errnum, const string& errmsg, const VSPlayVideoItem& item)
{
	FileLog("httprequest", "VideoShow.Native::OnPlayVideo( success : %s )", success?"true":"false");

	/* turn object to java object here */
	JNIEnv* env;
	jint iRet = JNI_ERR;
	gJavaVM->GetEnv((void**)&env, JNI_VERSION_1_4);
	if( env == NULL ) {
		iRet = gJavaVM->AttachCurrentThread((JNIEnv **)&env, NULL);
	}

	jobject jItem = NULL;
	if (success) {
		jobject jItemObj = NULL;
		JavaItemMap::iterator itr = gJavaItemMap.find(VS_PLAYVIDEO_ITEM_CLASS);
		if( itr != gJavaItemMap.end() ) {
			jItemObj = itr->second;

			jclass jItemCls = env->GetObjectClass(jItemObj);

			if(NULL != jItemCls) {
				jmethodID init = env->GetMethodID(jItemCls, "<init>", "("
							"Ljava/lang/String;"	// videoURL
							"Ljava/lang/String;"	// transcription
							"Ljava/lang/String;"	// viewTime1
							"Ljava/lang/String;"	// viewTime2
							")V");

				jItem = env->NewObject(jItemCls, init,
						env->NewStringUTF(item.videoURL.c_str()),
						env->NewStringUTF(item.transcription.c_str()),
						env->NewStringUTF(item.viewTime1.c_str()),
						env->NewStringUTF(item.viewTime2.c_str())
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
	signure += VS_PLAYVIDEO_ITEM_CLASS;
	signure += ";";
	signure += ")V";
	jmethodID jCallback = env->GetMethodID(jCallbackCls, "OnVSPlayVideo", signure.c_str());

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

	if( iRet == JNI_OK ) {
		gJavaVM->DetachCurrentThread();
	}
}

// ------------------------------ WatchedVideoList ---------------------------------
/*
 * Class:     com_qpidnetwork_request_RequestJniVideoShow
 * Method:    WatchedVideoList
 * Signature: (II)J
 */
JNIEXPORT jlong JNICALL Java_com_qpidnetwork_request_RequestJniVideoShow_WatchedVideoList
  (JNIEnv *env, jclass cls, jint pageIndex, jint pageSize, jobject callback)
{
	jlong requestId = -1;

	// 发出请求
	requestId = gRequestController.WatchedVideoList(pageIndex, pageSize);
	if (requestId != -1) {
		// 保存callback
		jobject jObj = env->NewGlobalRef(callback);
		gCallbackMap.Insert(requestId, jObj);
		FileLog("httprequest", "VideoShow.Native::WatchedVideoList() requestId:%lld, callback:%p, jObj:%p", requestId, callback, jObj);
	}
	else {
		FileLog("httprequest", "VideoShow.Native::WatchedVideoList() fails. "
				"requestId:%lld, pageIndex:%d, pageSize:%d"
			, requestId, pageIndex, pageSize);
	}

	return requestId;
}

void OnWatchedVideoList(long requestId, bool success, const string& errnum, const string& errmsg, int pageIndex, int pageSize, int dataCount, const VSWatchedVideoList& list)
{
	FileLog("httprequest", "VideoShow.Native::OnWatchedVideoList( success : %s )", success?"true":"false");

	/* turn object to java object here */
	JNIEnv* env;
	jint iRet = JNI_ERR;
	gJavaVM->GetEnv((void**)&env, JNI_VERSION_1_4);
	if( env == NULL ) {
		iRet = gJavaVM->AttachCurrentThread((JNIEnv **)&env, NULL);
	}

	jobjectArray jItemArray = NULL;
	if (success) {
		JavaItemMap::iterator itr = gJavaItemMap.find(VS_WATCHEDVIDEOLIST_ITEM_CLASS);
		if( itr != gJavaItemMap.end() ) {
			jobject jItemObj = itr->second;
			jclass jItemCls = env->GetObjectClass(jItemObj);
			if(NULL != jItemCls) {
				jItemArray = env->NewObjectArray(list.size(), jItemCls, NULL);

				int i = 0;
				for(VSWatchedVideoList::const_iterator itr = list.begin();
					itr != list.end();
					itr++, i++)
				{
					jmethodID init = env->GetMethodID(jItemCls, "<init>", "("
							"Ljava/lang/String;"	// videoid
							"Ljava/lang/String;"	// time
							"Ljava/lang/String;"	// thumbURL
							"Ljava/lang/String;"	// womanid
							"Ljava/lang/String;"	// firstname
							"I"						// age
							"Ljava/lang/String;"	// weight
							"Ljava/lang/String;"	// height
							"Ljava/lang/String;"	// country
							"Ljava/lang/String;"	// province
							"Ljava/lang/String;"	// viewTime1
							"Ljava/lang/String;"	// viewTime2
							"J"						// validTime
							")V");

					jobject jItem = env->NewObject(jItemCls, init,
							env->NewStringUTF(itr->videoId.c_str()),
							env->NewStringUTF(itr->time.c_str()),
							env->NewStringUTF(itr->thumbURL.c_str()),
							env->NewStringUTF(itr->womanId.c_str()),
							env->NewStringUTF(itr->firstname.c_str()),
							itr->age,
							env->NewStringUTF(itr->weight.c_str()),
							env->NewStringUTF(itr->height.c_str()),
							env->NewStringUTF(itr->country.c_str()),
							env->NewStringUTF(itr->province.c_str()),
							env->NewStringUTF(itr->viewTime1.c_str()),
							env->NewStringUTF(itr->viewTime2.c_str()),
							itr->validTime
							);

					env->SetObjectArrayElement(jItemArray, i, jItem);

					env->DeleteLocalRef(jItem);
				}
				env->DeleteLocalRef(jItemCls);
			}
		}
	}

	/* real callback java */
	jobject jCallbackObj = gCallbackMap.Erase(requestId);
	jclass jCallbackCls = env->GetObjectClass(jCallbackObj);

	string signure = "(ZLjava/lang/String;Ljava/lang/String;III";
	signure += "[L";
	signure += VS_WATCHEDVIDEOLIST_ITEM_CLASS;
	signure += ";";
	signure += ")V";
	jmethodID jCallback = env->GetMethodID(jCallbackCls, "OnVSWatchedVideoList", signure.c_str());

	if( jCallbackObj != NULL && jCallback != NULL ) {
		jstring jerrno = env->NewStringUTF(errnum.c_str());
		jstring jerrmsg = env->NewStringUTF(errmsg.c_str());

		env->CallVoidMethod(jCallbackObj, jCallback, success, jerrno, jerrmsg, pageIndex, pageSize, dataCount, jItemArray);

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

	if( jItemArray != NULL ) {
		env->DeleteLocalRef(jItemArray);
	}

	if( iRet == JNI_OK ) {
		gJavaVM->DetachCurrentThread();
	}
}

// ------------------------------ SaveVideo ---------------------------------
/*
 * Class:     com_qpidnetwork_request_RequestJniVideoShow
 * Method:    SaveVideo
 * Signature: (Ljava/lang/String;)J
 */
JNIEXPORT jlong JNICALL Java_com_qpidnetwork_request_RequestJniVideoShow_SaveVideo
  (JNIEnv *env, jclass cls, jstring videoId, jobject callback)
{
	jlong requestId = -1;

	// 生成转换的videoId字符串
	string strVideoId("");
	const char *cpVideoId = env->GetStringUTFChars(videoId, 0);
	strVideoId = cpVideoId;
	env->ReleaseStringUTFChars(videoId, cpVideoId);

	// 发出请求
	requestId = gRequestController.SaveVideo(strVideoId);
	if (requestId != -1) {
		// 保存callback
		jobject jObj = env->NewGlobalRef(callback);
		gCallbackMap.Insert(requestId, jObj);
		FileLog("httprequest", "VideoShow.Native::SaveVideo() requestId:%lld, callback:%p, jObj:%p", requestId, callback, jObj);
	}
	else {
		FileLog("httprequest", "VideoShow.Native::SaveVideo() fails. "
				"requestId:%lld, videoId:%s"
			, requestId, strVideoId.c_str());
	}

	return requestId;
}

void OnSaveVideo(long requestId, bool success, const string& errnum, const string& errmsg)
{
	FileLog("httprequest", "VideoShow.Native::OnSaveVideo( success : %s )", success?"true":"false");

	/* turn object to java object here */
	JNIEnv* env;
	jint iRet = JNI_ERR;
	gJavaVM->GetEnv((void**)&env, JNI_VERSION_1_4);
	if( env == NULL ) {
		iRet = gJavaVM->AttachCurrentThread((JNIEnv **)&env, NULL);
	}

	/* real callback java */
	jobject jCallbackObj = gCallbackMap.Erase(requestId);

	jclass jCallbackCls = env->GetObjectClass(jCallbackObj);

	string signure = "(ZLjava/lang/String;Ljava/lang/String;";
	signure += ")V";
	jmethodID jCallback = env->GetMethodID(jCallbackCls, "OnVSSaveVideo", signure.c_str());

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

	if( iRet == JNI_OK ) {
		gJavaVM->DetachCurrentThread();
	}
}

// ------------------------------ RemoveVideo ---------------------------------
/*
 * Class:     com_qpidnetwork_request_RequestJniVideoShow
 * Method:    RemoveVideo
 * Signature: (Ljava/lang/String;)J
 */
JNIEXPORT jlong JNICALL Java_com_qpidnetwork_request_RequestJniVideoShow_RemoveVideo
  (JNIEnv *env, jclass cls, jstring videoId, jobject callback)
{
	jlong requestId = -1;

	// 生成转换的videoId字符串
	string strVideoId("");
	const char *cpVideoId = env->GetStringUTFChars(videoId, 0);
	strVideoId = cpVideoId;
	env->ReleaseStringUTFChars(videoId, cpVideoId);

	// 发出请求
	requestId = gRequestController.RemoveVideo(strVideoId);
	if (requestId != -1) {
		// 保存callback
		jobject jObj = env->NewGlobalRef(callback);
		gCallbackMap.Insert(requestId, jObj);
		FileLog("httprequest", "VideoShow.Native::RemoveVideo() requestId:%lld, callback:%p, jObj:%p", requestId, callback, jObj);
	}
	else {
		FileLog("httprequest", "VideoShow.Native::RemoveVideo() fails. "
				"requestId:%lld, videoId:%s"
			, requestId, strVideoId.c_str());
	}

	return requestId;
}

void OnRemoveVideo(long requestId, bool success, const string& errnum, const string& errmsg)
{
	FileLog("httprequest", "VideoShow.Native::OnRemoveVideo( success : %s )", success?"true":"false");

	/* turn object to java object here */
	JNIEnv* env;
	jint iRet = JNI_ERR;
	gJavaVM->GetEnv((void**)&env, JNI_VERSION_1_4);
	if( env == NULL ) {
		iRet = gJavaVM->AttachCurrentThread((JNIEnv **)&env, NULL);
	}

	/* real callback java */
	jobject jCallbackObj = gCallbackMap.Erase(requestId);

	jclass jCallbackCls = env->GetObjectClass(jCallbackObj);

	string signure = "(ZLjava/lang/String;Ljava/lang/String;";
	signure += ")V";
	jmethodID jCallback = env->GetMethodID(jCallbackCls, "OnVSRemoveVideo", signure.c_str());

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

	if( iRet == JNI_OK ) {
		gJavaVM->DetachCurrentThread();
	}
}

// ------------------------------ SavedVideoList ---------------------------------
/*
 * Class:     com_qpidnetwork_request_RequestJniVideoShow
 * Method:    SavedVideoList
 * Signature: (II)J
 */
JNIEXPORT jlong JNICALL Java_com_qpidnetwork_request_RequestJniVideoShow_SavedVideoList
  (JNIEnv *env, jclass cls, jint pageIndex, jint pageSize, jobject callback)
{
	jlong requestId = -1;

	// 发出请求
	requestId = gRequestController.SavedVideoList(pageIndex, pageSize);
	if (requestId != -1) {
		// 保存callback
		jobject jObj = env->NewGlobalRef(callback);
		gCallbackMap.Insert(requestId, jObj);
		FileLog("httprequest", "VideoShow.Native::SavedVideoList() requestId:%lld, callback:%p, jObj:%p", requestId, callback, jObj);
	}
	else {
		FileLog("httprequest", "VideoShow.Native::SavedVideoList() fails. "
				"requestId:%lld, pageIndex:%d, pageSize:%d"
			, requestId, pageIndex, pageSize);
	}

	return requestId;
}

void OnSavedVideoList(long requestId, bool success, const string& errnum, const string& errmsg, int pageIndex, int pageSize, int dataCount, const VSSavedVideoList& list)
{
	FileLog("httprequest", "VideoShow.Native::OnSavedVideoList( success : %s )", success?"true":"false");

	/* turn object to java object here */
	JNIEnv* env;
	jint iRet = JNI_ERR;
	gJavaVM->GetEnv((void**)&env, JNI_VERSION_1_4);
	if( env == NULL ) {
		iRet = gJavaVM->AttachCurrentThread((JNIEnv **)&env, NULL);
	}

	jobjectArray jItemArray = NULL;
	if (success) {
		JavaItemMap::iterator itr = gJavaItemMap.find(VS_SAVEDVIDEOLIST_ITEM_CLASS);
		if( itr != gJavaItemMap.end() ) {
			jobject jItemObj = itr->second;
			jclass jItemCls = env->GetObjectClass(jItemObj);
			if(NULL != jItemCls) {
				jItemArray = env->NewObjectArray(list.size(), jItemCls, NULL);

				int i = 0;
				for(VSSavedVideoList::const_iterator itr = list.begin();
					itr != list.end();
					itr++, i++)
				{
					jmethodID init = env->GetMethodID(jItemCls, "<init>", "("
							"Ljava/lang/String;"	// videoid
							"Ljava/lang/String;"	// time
							"Ljava/lang/String;"	// thumbURL
							"Ljava/lang/String;"	// womanid
							"Ljava/lang/String;"	// firstname
							"I"						// age
							"Ljava/lang/String;"	// weight
							"Ljava/lang/String;"	// height
							"Ljava/lang/String;"	// country
							"Ljava/lang/String;"	// province
							")V");

					jobject jItem = env->NewObject(jItemCls, init,
							env->NewStringUTF(itr->videoId.c_str()),
							env->NewStringUTF(itr->time.c_str()),
							env->NewStringUTF(itr->thumbURL.c_str()),
							env->NewStringUTF(itr->womanId.c_str()),
							env->NewStringUTF(itr->firstname.c_str()),
							itr->age,
							env->NewStringUTF(itr->weight.c_str()),
							env->NewStringUTF(itr->height.c_str()),
							env->NewStringUTF(itr->country.c_str()),
							env->NewStringUTF(itr->province.c_str())
							);

					env->SetObjectArrayElement(jItemArray, i, jItem);

					env->DeleteLocalRef(jItem);
				}
				env->DeleteLocalRef(jItemCls);
			}
		}
	}

	/* real callback java */
	jobject jCallbackObj = gCallbackMap.Erase(requestId);
	jclass jCallbackCls = env->GetObjectClass(jCallbackObj);

	string signure = "(ZLjava/lang/String;Ljava/lang/String;III";
	signure += "[L";
	signure += VS_SAVEDVIDEOLIST_ITEM_CLASS;
	signure += ";";
	signure += ")V";
	jmethodID jCallback = env->GetMethodID(jCallbackCls, "OnVSSavedVideoList", signure.c_str());

	if( jCallbackObj != NULL && jCallback != NULL ) {
		jstring jerrno = env->NewStringUTF(errnum.c_str());
		jstring jerrmsg = env->NewStringUTF(errmsg.c_str());

		env->CallVoidMethod(jCallbackObj, jCallback, success, jerrno, jerrmsg, pageIndex, pageSize, dataCount, jItemArray);

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

	if( jItemArray != NULL ) {
		env->DeleteLocalRef(jItemArray);
	}

	if( iRet == JNI_OK ) {
		gJavaVM->DetachCurrentThread();
	}
}
