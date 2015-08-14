/*
 * com_qpidnetwork_request_RequestJniEMF.cpp
 *
 *  Created on: 2015-3-9
 *      Author: Samson.Fan
 */
#include "com_qpidnetwork_request_RequestJniEMF.h"
#include "com_qpidnetwork_request_RequestJni_GobalFunc.h"
#include "RequestEMFController.h"

void OnInboxList(long requestId, bool success, const string& errnum, const string& errmsg, int pageIndex, int pageSize, int dataCount, const EMFInboxList& inboxList);
void OnInboxMsg(long requestId, bool success, const string& errnum, const string& errmsg, const EMFInboxMsgItem& item);
void OnOutboxList(long requestId, bool success, const string& errnum, const string& errmsg, int pageIndex, int pageSize, int dataCount, const EMFOutboxList& outboxList);
void OnOutboxMsg(long requestId, bool success, const string& errnum, const string& errmsg, const EMFOutboxMsgItem& item);
void OnMsgTotal(long requestId, bool success, const string& errnum, const string& errmsg, const EMFMsgTotalItem& item);
void OnSendMsg(long requestId, bool success, const string& errnum, const string& errmsg, const EMFSendMsgItem& item, const EMFSendMsgErrorItem& errItem);
void OnUploadImage(long requestId, bool success, const string& errnum, const string& errmsg);
void OnUploadAttach(long requestId, bool success, const string& errnum, const string& errmsg, const string& attachId);
void OnDeleteMsg(long requestId, bool success, const string& errnum, const string& errmsg);
void OnAdmirerList(long requestId, bool success, const string& errnum, const string& errmsg, int pageIndex, int pageSize, int dataCount, const EMFAdmirerList& admirerList);
void OnAdmirerViewer(long requestId, bool success, const string& errnum, const string& errmsg, const EMFAdmirerViewerItem& item);
void OnBlockList(long requestId, bool success, const string& errnum, const string& errmsg, int pageIndex, int pageSize, int dataCount, const EMFBlockList& blockList);
void OnBlock(long requestId, bool success, const string& errnum, const string& errmsg);
void OnUnblock(long requestId, bool success, const string& errnum, const string& errmsg);
void OnInboxPhotoFee(long requestId, bool success, const string& errnum, const string& errmsg);
void OnPrivatePhotoView(long requestId, bool success, const string& errnum, const string& errmsg, const string& filePath);
static RequestEMFControllerCallback gRequestControllerCallback {
	OnInboxList,
	OnInboxMsg,
	OnOutboxList,
	OnOutboxMsg,
	OnMsgTotal,
	OnSendMsg,
	OnUploadImage,
	OnUploadAttach,
	OnDeleteMsg,
	OnAdmirerList,
	OnAdmirerViewer,
	OnBlockList,
	OnBlock,
	OnUnblock,
	OnInboxPhotoFee,
	OnPrivatePhotoView
};
static RequestEMFController gRequestController(&gHttpRequestManager, gRequestControllerCallback);

jobjectArray GetPrivatePhotoJArray(JNIEnv* env, const EMFPrivatePhotoList& list)
{
	jobjectArray photoArray = NULL;

	jclass jCls = GetJClass(env, EMF_PRIVATEPHOTO_ITEM_CLASS);
	if (NULL != jCls) {
		photoArray = env->NewObjectArray(list.size(), jCls, NULL);
		EMFPrivatePhotoList::const_iterator iter;
		int iIndex = 0;
		for (iter = list.begin(); iter != list.end(); iter++, iIndex++) {
			jmethodID init = env->GetMethodID(jCls, "<init>", "("
					"Ljava/lang/String;"	// sendId
					"Ljava/lang/String;"	// photoId
					"Z"						// photoFee
					"Ljava/lang/String;"	// photoDesc
					")V");

			jobject jItem = env->NewObject(jCls, init,
					env->NewStringUTF((*iter).sendId.c_str()),
					env->NewStringUTF((*iter).photoId.c_str()),
					(*iter).photoFee,
					env->NewStringUTF((*iter).photoDesc.c_str())
					);

			env->SetObjectArrayElement(photoArray, iIndex, jItem);
			env->DeleteLocalRef(jItem);
		}
	}

	return photoArray;
}

// ------------------------------ InboxList ---------------------------------
/*
 * Class:     com_qpidnetwork_request_RequestJniEMF
 * Method:    InboxList
 * Signature: (IIILjava/lang/String;Lcom/qpidnetwork/request/OnEMFInboxListCallback;)J
 */
JNIEXPORT jlong JNICALL Java_com_qpidnetwork_request_RequestJniEMF_InboxList
  (JNIEnv *env, jclass cls, jint pageIndex, jint pageSize, jint sortType, jstring womanId, jobject callback)
{
	jlong requestId = -1;

	// 生成转换的womanid字符串
	string strWomanId("");
	const char *cpWomanId = env->GetStringUTFChars(womanId, 0);
	strWomanId = cpWomanId;
	env->ReleaseStringUTFChars(womanId, cpWomanId);

	// 发出请求
	requestId = gRequestController.InboxList(pageIndex, pageSize, sortType, strWomanId);
	if (requestId != -1) {
		// 保存callback
		jobject jObj = env->NewGlobalRef(callback);
		gCallbackMap.Insert(requestId, jObj);
		FileLog("httprequest", "EMF.Native::InboxList() requestId:%lld, callback:%p, jObj:%p", requestId, callback, jObj);
	}
	else {
		FileLog("httprequest", "EMF.Native::InboxList() fails. requestId:%lld, pageIndex:%d, pageSize:%d, sortType:%d, womanid:%s"
			, requestId, pageIndex, pageSize, sortType, strWomanId.c_str());
	}

	return requestId;
}

void OnInboxList(long requestId, bool success, const string& errnum, const string& errmsg, int pageIndex, int pageSize, int dataCount, const EMFInboxList& inboxList)
{
	FileLog("httprequest", "EMF.Native::OnInboxList( success : %s )", success?"true":"false");
	FileLog("httprequest", "EMF.Native::OnInboxList( pageIndex:%d, pageSize:%d, dataCount:%d, inboxList.size():%d )"
			, pageIndex, pageSize, dataCount, inboxList.size());

	/* turn object to java object here */
	JNIEnv* env;
	jint iRet = JNI_ERR;
	gJavaVM->GetEnv((void**)&env, JNI_VERSION_1_4);
	if( env == NULL ) {
		iRet = gJavaVM->AttachCurrentThread((JNIEnv **)&env, NULL);
	}

	jobjectArray jItemArray = NULL;
	if (success) {
		JavaItemMap::iterator itr = gJavaItemMap.find(EMF_INBOXLIST_ITEM_CLASS);
		if( itr != gJavaItemMap.end() ) {
			jobject jItemObj = itr->second;
			jclass jItemCls = env->GetObjectClass(jItemObj);
			if(NULL != jItemCls) {
				jItemArray = env->NewObjectArray(inboxList.size(), jItemCls, NULL);

				int i = 0;
				for(EMFInboxList::const_iterator itr = inboxList.begin();
					itr != inboxList.end();
					itr++, i++)
				{
					jmethodID init = env->GetMethodID(jItemCls, "<init>", "("
							"Ljava/lang/String;"	// id
							"I"						// attachnum
							"Z"						// virtual_gifts
							"Ljava/lang/String;"	// womanid
							"Z"						// readflag
							"Z"						// rflag
							"Z"						// fflag
							"Z"						// pflag
							"Ljava/lang/String;"	// firstname
							"Ljava/lang/String;"	// lastname
							"Ljava/lang/String;"	// weight
							"Ljava/lang/String;"	// height
							"Ljava/lang/String;"	// country
							"Ljava/lang/String;"	// province
							"I"						// age
							"Ljava/lang/String;"	// sendTime
							"Ljava/lang/String;"	// photoURL
							"Ljava/lang/String;"	// intro
							")V");

					jobject jItem = env->NewObject(jItemCls, init,
							env->NewStringUTF(itr->id.c_str()),
							itr->attachnum,
							itr->virtual_gifts,
							env->NewStringUTF(itr->womanid.c_str()),
							itr->readflag,
							itr->rflag,
							itr->fflag,
							itr->pflag,
							env->NewStringUTF(itr->firstname.c_str()),
							env->NewStringUTF(itr->lastname.c_str()),
							env->NewStringUTF(itr->weight.c_str()),
							env->NewStringUTF(itr->height.c_str()),
							env->NewStringUTF(itr->country.c_str()),
							env->NewStringUTF(itr->province.c_str()),
							itr->age,
							env->NewStringUTF(itr->sendTime.c_str()),
							env->NewStringUTF(itr->photoURL.c_str()),
							env->NewStringUTF(itr->intro.c_str())
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
	signure += EMF_INBOXLIST_ITEM_CLASS;
	signure += ";";
	signure += ")V";
	jmethodID jCallback = env->GetMethodID(jCallbackCls, "OnEMFInboxList", signure.c_str());

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

// ------------------------------ InboxMsg ---------------------------------
/*
 * Class:     com_qpidnetwork_request_RequestJniEMF
 * Method:    InboxMsg
 * Signature: (Ljava/lang/String;Lcom/qpidnetwork/request/OnEMFInboxMsgCallback;)J
 */
JNIEXPORT jlong JNICALL Java_com_qpidnetwork_request_RequestJniEMF_InboxMsg
  (JNIEnv *env, jclass cls, jstring messageId, jobject callback)
{
	jlong requestId = -1;

	// 生成转换的messageid字符串
	string strMessageId("");
	const char *cpMessageId = env->GetStringUTFChars(messageId, 0);
	strMessageId = cpMessageId;
	env->ReleaseStringUTFChars(messageId, cpMessageId);

	// 发出请求
	requestId = gRequestController.InboxMsg(strMessageId);
	if (requestId != -1) {
		// 保存callback
		jobject jObj = env->NewGlobalRef(callback);
		gCallbackMap.Insert(requestId, jObj);
		FileLog("httprequest", "EMF.Native::InboxMsg() requestId:%lld, callback:%p, jObj:%p", requestId, callback, jObj);
	}
	else {
		FileLog("httprequest", "EMF.Native::InboxMsg() fails. requestId:%lld, messageId:%s"
			, requestId, strMessageId.c_str());
	}

	return requestId;
}

void OnInboxMsg(long requestId, bool success, const string& errnum, const string& errmsg, const EMFInboxMsgItem& item)
{
	FileLog("httprequest", "EMF.Native::OnInboxMsg( success : %s )", success?"true":"false");
	FileLog("httprequest", "EMF.Native::OnInboxMsg( item.privatePhotoList.size:%d )", item.privatePhotoList.size());

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
		JavaItemMap::iterator itr = gJavaItemMap.find(EMF_INBOXMSG_ITEM_CLASS);
		if( itr != gJavaItemMap.end() ) {
			jItemObj = itr->second;
			jclass jItemCls = env->GetObjectClass(jItemObj);
			if(NULL != jItemCls) {
				jmethodID init = env->GetMethodID(jItemCls, "<init>", "("
							"Ljava/lang/String;"	// id
							"Ljava/lang/String;"	// womanid
							"Ljava/lang/String;"	// firstname
							"Ljava/lang/String;"	// lastname
							"Ljava/lang/String;"	// weight
							"Ljava/lang/String;"	// height
							"Ljava/lang/String;"	// country
							"Ljava/lang/String;"	// province
							"I"						// age
							"Ljava/lang/String;"	// photoURL
							"Ljava/lang/String;"	// body
							"Ljava/lang/String;"	// notetoman
							"[Ljava/lang/String;"	// photosURL
							"Ljava/lang/String;"	// sendTime
							"[L"
							EMF_PRIVATEPHOTO_ITEM_CLASS	// privatePhotos
							";"
							"Ljava/lang/String;"	// vgId
							")V");

				jclass jStringCls = env->FindClass("java/lang/String");
				jobjectArray jPhotosURLArray = env->NewObjectArray(item.photosURL.size(), jStringCls, NULL);
				EMFPhotoUrlList::const_iterator iter;
				int iPhotosURLIndex = 0;
				for (iter = item.photosURL.begin(); iter != item.photosURL.end(); iter++, iPhotosURLIndex++) {
					jstring url = env->NewStringUTF((*iter).c_str());
					env->SetObjectArrayElement(jPhotosURLArray, iPhotosURLIndex, url);
					env->DeleteLocalRef(url);
				}

				jobjectArray privatePhotoArray = GetPrivatePhotoJArray(env, item.privatePhotoList);

				jItem = env->NewObject(jItemCls, init,
							env->NewStringUTF(item.id.c_str()),
							env->NewStringUTF(item.womanid.c_str()),
							env->NewStringUTF(item.firstname.c_str()),
							env->NewStringUTF(item.lastname.c_str()),
							env->NewStringUTF(item.weight.c_str()),
							env->NewStringUTF(item.height.c_str()),
							env->NewStringUTF(item.country.c_str()),
							env->NewStringUTF(item.province.c_str()),
							item.age,
							env->NewStringUTF(item.photoURL.c_str()),
							env->NewStringUTF(item.body.c_str()),
							env->NewStringUTF(item.notetoman.c_str()),
							jPhotosURLArray,
							env->NewStringUTF(item.sendTime.c_str()),
							privatePhotoArray,
							env->NewStringUTF(item.vgId.c_str())
							);

				FileLog("httprequest", "EMF.Native::OnInboxMsg() NewObject() OK, jItem:%p", jItem);

				env->DeleteLocalRef(privatePhotoArray);
				env->DeleteLocalRef(jPhotosURLArray);
				env->DeleteLocalRef(jItemCls);
			}
		}
	}

	/* real callback java */
	jobject jCallbackObj = gCallbackMap.Erase(requestId);

	jclass jCallbackCls = env->GetObjectClass(jCallbackObj);

	string signure = "(ZLjava/lang/String;Ljava/lang/String;";
	signure += "L";
	signure += EMF_INBOXMSG_ITEM_CLASS;
	signure += ";";
	signure += ")V";
	jmethodID jCallback = env->GetMethodID(jCallbackCls, "OnEMFInboxMsg", signure.c_str());

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

// ------------------------------ OutboxList ---------------------------------
/*
 * Class:     com_qpidnetwork_request_RequestJniEMF
 * Method:    OutboxList
 * Signature: (IILjava/lang/String;ILcom/qpidnetwork/request/OnEMFOutboxListCallback;)J
 */
JNIEXPORT jlong JNICALL Java_com_qpidnetwork_request_RequestJniEMF_OutboxList
  (JNIEnv *env, jclass cls, jint pageIndex, jint pageSize, jstring womanId, jint progressType, jobject callback)
{
	jlong requestId = -1;

	// 生成转换的womanid字符串
	string strWomanId("");
	const char *cpWomanId = env->GetStringUTFChars(womanId, 0);
	strWomanId = cpWomanId;
	env->ReleaseStringUTFChars(womanId, cpWomanId);

	// 发出请求
	requestId = gRequestController.OutboxList(pageIndex, pageSize, progressType, strWomanId);
	if (requestId != -1) {
		// 保存callback
		jobject jObj = env->NewGlobalRef(callback);
		gCallbackMap.Insert(requestId, jObj);
		FileLog("httprequest", "EMF.Native::OutboxList() requestId:%lld, callback:%p, jObj:%p", requestId, callback, jObj);
	}
	else {
		FileLog("httprequest", "EMF.Native::OutboxList() fails. requestId:%lld, pageIndex:%d, pageSize:%d, progressType:%d, womanId:%s"
			, requestId, pageIndex, pageSize, progressType, strWomanId.c_str());
	}


	return requestId;
}

void OnOutboxList(long requestId, bool success, const string& errnum, const string& errmsg, int pageIndex, int pageSize, int dataCount, const EMFOutboxList& outboxList)
{
	FileLog("httprequest", "EMF.Native::OnOutboxList( success : %s )", success?"true":"false");

	/* turn object to java object here */
	JNIEnv* env;
	jint iRet = JNI_ERR;
	gJavaVM->GetEnv((void**)&env, JNI_VERSION_1_4);
	if( env == NULL ) {
		iRet = gJavaVM->AttachCurrentThread((JNIEnv **)&env, NULL);
	}


	jobjectArray jItemArray = NULL;
	if (success) {
		JavaItemMap::iterator itr = gJavaItemMap.find(EMF_OUTBOXLIST_ITEM_CLASS);
		if( itr != gJavaItemMap.end() ) {
			jobject jItemObj = itr->second;
			jclass jItemCls = env->GetObjectClass(jItemObj);

			if(NULL != jItemCls) {
				jItemArray = env->NewObjectArray(outboxList.size(), jItemCls, NULL);

				int i = 0;
				for(EMFOutboxList::const_iterator itr = outboxList.begin();
					itr != outboxList.end();
					itr++, i++)
				{
					jmethodID init = env->GetMethodID(jItemCls, "<init>", "("
							"Ljava/lang/String;"	// id
							"I"						// attachnum
							"Z"						// virtual_gifts
							"I"						// progress
							"Ljava/lang/String;"	// womanid
							"Ljava/lang/String;"	// firstname
							"Ljava/lang/String;"	// lastname
							"Ljava/lang/String;"	// weight
							"Ljava/lang/String;"	// height
							"Ljava/lang/String;"	// country
							"Ljava/lang/String;"	// province
							"I"						// age
							"Ljava/lang/String;"	// sendTime
							"Ljava/lang/String;"	// photoURL
							"Ljava/lang/String;"	// intro
							")V");

					jobject jItem = env->NewObject(jItemCls, init,
							env->NewStringUTF(itr->id.c_str()),
							itr->attachnum,
							itr->virtual_gifts,
							itr->progress,
							env->NewStringUTF(itr->womanid.c_str()),
							env->NewStringUTF(itr->firstname.c_str()),
							env->NewStringUTF(itr->lastname.c_str()),
							env->NewStringUTF(itr->weight.c_str()),
							env->NewStringUTF(itr->height.c_str()),
							env->NewStringUTF(itr->country.c_str()),
							env->NewStringUTF(itr->province.c_str()),
							itr->age,
							env->NewStringUTF(itr->sendTime.c_str()),
							env->NewStringUTF(itr->photoURL.c_str()),
							env->NewStringUTF(itr->intro.c_str())
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
	signure += EMF_OUTBOXLIST_ITEM_CLASS;
	signure += ";";
	signure += ")V";
	jmethodID jCallback = env->GetMethodID(jCallbackCls, "OnEMFOutboxList", signure.c_str());

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

// ------------------------------ OutboxMsg ---------------------------------
/*
 * Class:     com_qpidnetwork_request_RequestJniEMF
 * Method:    OutboxMsg
 * Signature: (Ljava/lang/String;Lcom/qpidnetwork/request/OnEMFOutboxMsgCallback;)J
 */
JNIEXPORT jlong JNICALL Java_com_qpidnetwork_request_RequestJniEMF_OutboxMsg
  (JNIEnv *env, jclass cls, jstring messageId, jobject callback)
{
	jlong requestId = -1;

	// 生成转换的messageid字符串
	string strMessageId("");
	const char *cpMessageId = env->GetStringUTFChars(messageId, 0);
	strMessageId = cpMessageId;
	env->ReleaseStringUTFChars(messageId, cpMessageId);

	// 发出请求
	requestId = gRequestController.OutboxMsg(strMessageId);
	if (requestId != -1) {
		// 保存callback
		jobject jObj = env->NewGlobalRef(callback);
		gCallbackMap.Insert(requestId, jObj);
		FileLog("httprequest", "EMF.Native::OutboxMsg() requestId:%lld, callback:%p, jObj:%p", requestId, callback, jObj);
	}
	else {
		FileLog("httprequest", "EMF.Native::OutboxMsg() fails. requestId:%lld, messageId:%s"
			, requestId, strMessageId.c_str());
	}

	return requestId;
}

void OnOutboxMsg(long requestId, bool success, const string& errnum, const string& errmsg, const EMFOutboxMsgItem& item)
{
	FileLog("httprequest", "EMF.Native::OnOutboxMsg( success : %s )", success?"true":"false");

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
		JavaItemMap::iterator itr = gJavaItemMap.find(EMF_OUTBOXMSG_ITEM_CLASS);
		if( itr != gJavaItemMap.end() ) {
			jItemObj = itr->second;
			jclass jItemCls = env->GetObjectClass(jItemObj);

			if(NULL != jItemCls) {
				jmethodID init = env->GetMethodID(jItemCls, "<init>", "("
							"Ljava/lang/String;"	// id
							"Ljava/lang/String;"	// vgid
							"Ljava/lang/String;"	// content
							"Ljava/lang/String;"	// sendTime
							"[Ljava/lang/String;"	// photosURL
							"Ljava/lang/String;"	// photoURL
							"Ljava/lang/String;"	// womanid
							"Ljava/lang/String;"	// firstname
							"Ljava/lang/String;"	// lastname
							"Ljava/lang/String;"	// weight
							"Ljava/lang/String;"	// height
							"Ljava/lang/String;"	// country
							"Ljava/lang/String;"	// province
							"I"						// age
							"[L"
							EMF_PRIVATEPHOTO_ITEM_CLASS	// privatePhotos
							";"
							")V");

				jclass jStringCls = env->FindClass("java/lang/String");
				jobjectArray jPhotosURLArray = env->NewObjectArray(item.photosURL.size(), jStringCls, NULL);
				EMFPhotoUrlList::const_iterator iter;
				int iPhotosURLIndex = 0;
				for (iter = item.photosURL.begin(); iter != item.photosURL.end(); iter++, iPhotosURLIndex++) {
					jstring url = env->NewStringUTF((*iter).c_str());
					env->SetObjectArrayElement(jPhotosURLArray, iPhotosURLIndex, url);
					env->DeleteLocalRef(url);
				}

				jobjectArray privatePhotoArray = GetPrivatePhotoJArray(env, item.privatePhotoList);

				jItem = env->NewObject(jItemCls, init,
							env->NewStringUTF(item.id.c_str()),
							env->NewStringUTF(item.vgId.c_str()),
							env->NewStringUTF(item.content.c_str()),
							env->NewStringUTF(item.sendTime.c_str()),
							jPhotosURLArray,
							env->NewStringUTF(item.photoURL.c_str()),
							env->NewStringUTF(item.womanid.c_str()),
							env->NewStringUTF(item.firstname.c_str()),
							env->NewStringUTF(item.lastname.c_str()),
							env->NewStringUTF(item.weight.c_str()),
							env->NewStringUTF(item.height.c_str()),
							env->NewStringUTF(item.country.c_str()),
							env->NewStringUTF(item.province.c_str()),
							item.age,
							privatePhotoArray
							);

				env->DeleteLocalRef(privatePhotoArray);
				env->DeleteLocalRef(jPhotosURLArray);
				env->DeleteLocalRef(jItemCls);
			}
		}
	}

	/* real callback java */
	jobject jCallbackObj = gCallbackMap.Erase(requestId);
	jclass jCallbackCls = env->GetObjectClass(jCallbackObj);

	string signure = "(ZLjava/lang/String;Ljava/lang/String;";
	signure += "L";
	signure += EMF_OUTBOXMSG_ITEM_CLASS;
	signure += ";";
	signure += ")V";
	jmethodID jCallback = env->GetMethodID(jCallbackCls, "OnEMFOutboxMsg", signure.c_str());

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

	FileLog("httprequest", "EMF.Native::OnOutboxMsg() end");
}

// ------------------------------ MsgTotal ---------------------------------
/*
 * Class:     com_qpidnetwork_request_RequestJniEMF
 * Method:    MsgTotal
 * Signature: (ILcom/qpidnetwork/request/OnEMFMsgTotalCallback;)J
 */
JNIEXPORT jlong JNICALL Java_com_qpidnetwork_request_RequestJniEMF_MsgTotal
  (JNIEnv *env, jclass cls, jint sortType, jobject callback)
{
	jlong requestId = -1;

	// 发出请求
	requestId = gRequestController.MsgTotal(sortType);
	if (requestId != -1) {
		// 保存callback
		jobject jObj = env->NewGlobalRef(callback);
		gCallbackMap.Insert(requestId, jObj);
		FileLog("httprequest", "EMF.Native::MsgTotal() requestId:%lld, callback:%p, jObj:%p", requestId, callback, jObj);
	}
	else {
		FileLog("httprequest", "EMF.Native::MsgTotal() fails. requestId:%lld, sortType:%d"
			, requestId, sortType);
	}

	return requestId;
}

void OnMsgTotal(long requestId, bool success, const string& errnum, const string& errmsg, const EMFMsgTotalItem& item)
{
	FileLog("httprequest", "EMF.Native::OnMsgTotal( success : %s )", success?"true":"false");

	/* turn object to java object here */
	JNIEnv* env;
	jint iRet = JNI_ERR;
	gJavaVM->GetEnv((void**)&env, JNI_VERSION_1_4);
	if( env == NULL ) {
		iRet = gJavaVM->AttachCurrentThread((JNIEnv **)&env, NULL);
	}

	jobject jItem = NULL;
	jobject jItemObj = NULL;
	JavaItemMap::iterator itr = gJavaItemMap.find(EMF_MSGTOTAL_ITEM_CLASS);
	if( itr != gJavaItemMap.end() ) {
		jItemObj = itr->second;

		jclass jItemCls = env->GetObjectClass(jItemObj);

		if(NULL != jItemCls) {
			jmethodID init = env->GetMethodID(jItemCls, "<init>", "("
						"I"						// msgTotal
						")V");

			jItem = env->NewObject(jItemCls, init,
					item.msgTotal
					);

			env->DeleteLocalRef(jItemCls);
		}
	}

	/* real callback java */
	jobject jCallbackObj = gCallbackMap.Erase(requestId);
	jclass jCallbackCls = env->GetObjectClass(jCallbackObj);

	string signure = "(ZLjava/lang/String;Ljava/lang/String;";
	signure += "L";
	signure += EMF_MSGTOTAL_ITEM_CLASS;
	signure += ";";
	signure += ")V";
	jmethodID jCallback = env->GetMethodID(jCallbackCls, "OnEMFMsgTotal", signure.c_str());

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

	FileLog("httprequest", "EMF.Native::OnMsgTotal() end");
}

// ------------------------------ SendMsg ---------------------------------
/*
 * Class:     com_qpidnetwork_request_RequestJniEMF
 * Method:    SendMsg
 * Signature: (Ljava/lang/String;Ljava/lang/String;ZILjava/lang/String;[Ljava/lang/String;[Ljava/lang/String;Lcom/qpidnetwork/request/OnEMFSendMsgCallback;)J
 */
JNIEXPORT jlong JNICALL Java_com_qpidnetwork_request_RequestJniEMF_SendMsg
  (JNIEnv *env, jclass cls, jstring womanid, jstring body, jboolean useIntegral, jint replyType, jstring mtab, jobjectArray gifts, jobjectArray attachs, jobject callback) {
	jlong requestId = -1;

	// 生成转换的字符串
	// womanid
	string strWomanId("");
	const char *cpWomanId = env->GetStringUTFChars(womanid, 0);
	strWomanId = cpWomanId;
	env->ReleaseStringUTFChars(womanid, cpWomanId);

	// body
	string strBody("");
	const char *cpBody = env->GetStringUTFChars(body, 0);
	strBody = cpBody;
	env->ReleaseStringUTFChars(body, cpBody);

	string strMtab("");
	const char *cpWtab = env->GetStringUTFChars(mtab, 0);
	strMtab = cpWtab;
	env->ReleaseStringUTFChars(mtab, cpWtab);

	// gifts
	SendMsgGifts giftList;
	for (int i = 0; i < env->GetArrayLength(gifts); i++) {
		jstring giftId = (jstring)env->GetObjectArrayElement(gifts, i);
		if (giftId != NULL) {
			const char* cpTemp = env->GetStringUTFChars(giftId, 0);
			if (NULL != cpTemp && strlen(cpTemp) > 0) {
				giftList.push_back(cpTemp);
			}
			env->ReleaseStringUTFChars(giftId, cpTemp);
		}
	}
	// attachs
	SendMsgAttachs attachList;
	for (int i = 0; i < env->GetArrayLength(attachs); i++) {
		jstring attachId = (jstring)env->GetObjectArrayElement(attachs, i);
		if (attachId != NULL) {
			const char* cpTemp = env->GetStringUTFChars(attachId, 0);
			if (NULL != cpTemp && strlen(cpTemp) > 0) {
				attachList.push_back(cpTemp);
			}
			env->ReleaseStringUTFChars(attachId, cpTemp);
		}
	}

	// 发出请求
	requestId = gRequestController.SendMsg(strWomanId, strBody, useIntegral, replyType, strMtab, giftList, attachList);
	if (requestId != -1) {
		// 保存callback
		jobject jObj = env->NewGlobalRef(callback);
		gCallbackMap.Insert(requestId, jObj);
		FileLog("httprequest", "EMF.Native::SendMsg() requestId:%lld, callback:%p, jObj:%p", requestId, callback, jObj);
	}
	else {
		FileLog("httprequest", "EMF.Native::SendMsg() fails. requestId:%lld, womanId:%s, useIntegral:%d, body:%s"
			, requestId, strWomanId.c_str(), useIntegral, strBody.c_str());
	}

	return requestId;
}

void OnSendMsg(long requestId, bool success, const string& errnum, const string& errmsg, const EMFSendMsgItem& item, const EMFSendMsgErrorItem& errItem)
{
	FileLog("httprequest", "EMF.Native::OnSendMsg( success : %s )", success?"true":"false");

	/* turn object to java object here */
	JNIEnv* env;
	jint iRet = JNI_ERR;
	gJavaVM->GetEnv((void**)&env, JNI_VERSION_1_4);
	if( env == NULL ) {
		iRet = gJavaVM->AttachCurrentThread((JNIEnv **)&env, NULL);
	}

	jobject jItem = NULL;
	JavaItemMap::iterator itr = gJavaItemMap.find(EMF_SENDMSG_ITEM_CLASS);
	if( itr != gJavaItemMap.end() ) {
		jobject jItemObj = itr->second;
		jclass jItemCls = env->GetObjectClass(jItemObj);

		if(NULL != jItemCls) {
			jmethodID init = env->GetMethodID(jItemCls, "<init>", "("
						"Ljava/lang/String;"	// messageid
						"I"						// sendTime
						")V");

			jItem = env->NewObject(jItemCls, init,
						env->NewStringUTF(item.messageId.c_str()),
						item.sendTime
						);

			env->DeleteLocalRef(jItemCls);
		}
	}
	jobject jErrItem = NULL;
	JavaItemMap::iterator errItr = gJavaItemMap.find(EMF_SENDMSG_ERROR_ITEM_CLASS);
	if( errItr != gJavaItemMap.end() ) {
		jobject jItemObj = errItr->second;
		jclass jItemCls = env->GetObjectClass(jItemObj);

		if(NULL != jItemCls) {
			jmethodID init = env->GetMethodID(jItemCls, "<init>", "("
						"Ljava/lang/String;"	// money
						")V");

			jErrItem = env->NewObject(jItemCls, init,
						env->NewStringUTF(errItem.money.c_str())
						);

			env->DeleteLocalRef(jItemCls);
		}
	}

	/* real callback java */
	jobject jCallbackObj = gCallbackMap.Erase(requestId);

	jclass jCallbackCls = env->GetObjectClass(jCallbackObj);

	string signure = "(ZLjava/lang/String;Ljava/lang/String;";
	signure += "L";
	signure += EMF_SENDMSG_ITEM_CLASS;
	signure += ";";
	signure += "L";
	signure += EMF_SENDMSG_ERROR_ITEM_CLASS;
	signure += ";";
	signure += ")V";
	jmethodID jCallback = env->GetMethodID(jCallbackCls, "OnEMFSendMsg", signure.c_str());

	if( jCallbackObj != NULL && jCallback != NULL ) {
		jstring jerrno = env->NewStringUTF(errnum.c_str());
		jstring jerrmsg = env->NewStringUTF(errmsg.c_str());

		env->CallVoidMethod(jCallbackObj, jCallback, success, jerrno, jerrmsg, jItem, jErrItem);

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
	if ( jErrItem != NULL ) {
		env->DeleteLocalRef(jErrItem);
	}

	if( iRet == JNI_OK ) {
		gJavaVM->DetachCurrentThread();
	}

	FileLog("httprequest", "EMF.Native::OnSendMsg() end");
}

// ------------------------------ UploadImage ---------------------------------
/*
 * Class:     com_qpidnetwork_request_RequestJniEMF
 * Method:    UploadImage
 * Signature: (Ljava/lang/String;[Ljava/lang/String;)J
 */
JNIEXPORT jlong JNICALL Java_com_qpidnetwork_request_RequestJniEMF_UploadImage
  (JNIEnv *env, jclass cls, jstring messageId, jobjectArray fileArray, jobject callback)
{
	jlong requestId = -1;

	// 生成转换的messageid字符串
	string strMessageId("");
	const char *cpMessageId = env->GetStringUTFChars(messageId, 0);
	strMessageId = cpMessageId;
	env->ReleaseStringUTFChars(messageId, cpMessageId);
	// 生成文件列表
	EMFFileNameList filenameList;
	for (int i = 0; i < env->GetArrayLength(fileArray); i++) {
		jstring filepath = (jstring)env->GetObjectArrayElement(fileArray, i);
		if (filepath != NULL) {
			const char* cpFilePath = env->GetStringUTFChars(filepath, 0);
			string strFilePath = cpFilePath;
			if (!strFilePath.empty()) {
				filenameList.push_back(strFilePath);
			}
			env->ReleaseStringUTFChars(filepath, cpFilePath);
		}
	}

	// 发出请求
	requestId = gRequestController.UploadImage(strMessageId, filenameList);
	if (requestId != -1) {
		// 保存callback
		jobject jObj = env->NewGlobalRef(callback);
		gCallbackMap.Insert(requestId, jObj);
	}
	else {
		FileLog("httprequest", "EMF.Native::UploadImage() fails. requestId:%lld, messageId:%s"
			, requestId, strMessageId.c_str());
	}

	return requestId;
}

void OnUploadImage(long requestId, bool success, const string& errnum, const string& errmsg)
{
	FileLog("httprequest", "EMF.Native::OnUploadImage( success : %s )", success?"true":"false");

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

	string signure = "(ZLjava/lang/String;Ljava/lang/String;)V";
	jmethodID jCallback = env->GetMethodID(jCallbackCls, "OnEMFUploadImage", signure.c_str());
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

	FileLog("httprequest", "EMF.Native::OnUploadImage() end");
}

// ------------------------------ UploadAttach ---------------------------------
JNIEXPORT jlong JNICALL Java_com_qpidnetwork_request_RequestJniEMF_UploadAttach
  (JNIEnv *env, jclass cls, jint attachType, jstring filePath, jobject callback)
{
	jlong requestId = -1;

		// 生成转换的filePath字符串
	string strFilePath("");
	const char *cpFilePath = env->GetStringUTFChars(filePath, 0);
	strFilePath = cpFilePath;
	env->ReleaseStringUTFChars(filePath, cpFilePath);

	// 发出请求
	requestId = gRequestController.UploadAttach(strFilePath, attachType);
	if (requestId != -1) {
		// 保存callback
		jobject jObj = env->NewGlobalRef(callback);
		gCallbackMap.Insert(requestId, jObj);
	}
	else {
		FileLog("httprequest", "EMF.Native::UploadAttach() fails. requestId:%lld, filePath:%s"
			, requestId, strFilePath.c_str());
	}

	return requestId;
}

void OnUploadAttach(long requestId, bool success, const string& errnum, const string& errmsg, const string& attachId)
{
	FileLog("httprequest", "EMF.Native::OnUploadAttach( success : %s )", success?"true":"false");

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

	string signure = "(ZLjava/lang/String;Ljava/lang/String;Ljava/lang/String;)V";
	jmethodID jCallback = env->GetMethodID(jCallbackCls, "OnEMFUploadAttach", signure.c_str());
	if( jCallbackObj != NULL && jCallback != NULL ) {
		jstring jerrno = env->NewStringUTF(errnum.c_str());
		jstring jerrmsg = env->NewStringUTF(errmsg.c_str());
		jstring jattachId = env->NewStringUTF(attachId.c_str());

		env->CallVoidMethod(jCallbackObj, jCallback, success, jerrno, jerrmsg, jattachId);

		env->DeleteLocalRef(jerrno);
		env->DeleteLocalRef(jerrmsg);
		env->DeleteLocalRef(jattachId);
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

	FileLog("httprequest", "EMF.Native::OnUploadAttach() end");
}

// ------------------------------ DeleteMsg ---------------------------------
/*
 * Class:     com_qpidnetwork_request_RequestJniEMF
 * Method:    DeleteMsg
 * Signature: (Ljava/lang/String;I)J
 */
JNIEXPORT jlong JNICALL Java_com_qpidnetwork_request_RequestJniEMF_DeleteMsg
  (JNIEnv *env, jclass cls, jstring messageid, jint mailType, jobject callback)
{
	jlong requestId = -1;

	// 生成转换的messageid字符串
	string strMessageId("");
	const char *cpMessageId = env->GetStringUTFChars(messageid, 0);
	strMessageId = cpMessageId;
	env->ReleaseStringUTFChars(messageid, cpMessageId);

	// 发出请求
	requestId = gRequestController.DeleteMsg(strMessageId, mailType);
	if (requestId != -1) {
		// 保存callback
		jobject jObj = env->NewGlobalRef(callback);
		gCallbackMap.Insert(requestId, jObj);
	}
	else {
		FileLog("httprequest", "EMF.Native::DeleteMsg() fails. requestId:%lld, messageId:%s, mailType:%d"
			, requestId, strMessageId.c_str(), mailType);
	}

	return requestId;
}

void OnDeleteMsg(long requestId, bool success, const string& errnum, const string& errmsg)
{
	FileLog("httprequest", "EMF.Native::OnDeleteMsg( success : %s )", success?"true":"false");

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

	string signure = "(ZLjava/lang/String;Ljava/lang/String;)V";
	jmethodID jCallback = env->GetMethodID(jCallbackCls, "OnEMFDeleteMsg", signure.c_str());
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

	FileLog("httprequest", "EMF.Native::OnDeleteMsg() end");
}

// ------------------------------ AdmirerList ---------------------------------
/*
 * Class:     com_qpidnetwork_request_RequestJniEMF
 * Method:    AdmirerList
 * Signature: (IIILjava/lang/String;)J
 */
JNIEXPORT jlong JNICALL Java_com_qpidnetwork_request_RequestJniEMF_AdmirerList
  (JNIEnv *env, jclass cls, jint pageIndex, jint pageSize, jint sortType, jstring womanid, jobject callback)
{
	jlong requestId = -1;

	// 生成转换的messageid字符串
	string strWomanId("");
	const char *cpWomanId = env->GetStringUTFChars(womanid, 0);
	strWomanId = cpWomanId;
	env->ReleaseStringUTFChars(womanid, cpWomanId);

	// 发出请求
	requestId = gRequestController.AdmirerList(pageIndex, pageSize, sortType, strWomanId);
	if (requestId != -1) {
		// 保存callback
		jobject jObj = env->NewGlobalRef(callback);
		gCallbackMap.Insert(requestId, jObj);
	}
	else {
		FileLog("httprequest", "EMF.Native::AdmirerList() fails. "
			"requestId:%lld, pageIndex:%d, pageSize:%d, sortType:%d, womanid:%s"
			, requestId, pageIndex, pageSize, sortType, strWomanId.c_str());
	}

	return requestId;
}

void OnAdmirerList(long requestId, bool success, const string& errnum, const string& errmsg, int pageIndex, int pageSize, int dataCount, const EMFAdmirerList& admirerList)
{
	FileLog("httprequest", "EMF.Native::OnAdmirerList( success : %s )", success?"true":"false");

	/* turn object to java object here */
	JNIEnv* env;
	jint iRet = JNI_ERR;
	gJavaVM->GetEnv((void**)&env, JNI_VERSION_1_4);
	if( env == NULL ) {
		iRet = gJavaVM->AttachCurrentThread((JNIEnv **)&env, NULL);
	}


	jobjectArray jItemArray = NULL;
	if (success) {
		JavaItemMap::iterator itr = gJavaItemMap.find(EMF_ADMIRERLIST_ITEM_CLASS);
		if( itr != gJavaItemMap.end() ) {
			jobject jItemObj = itr->second;
			jclass jItemCls = env->GetObjectClass(jItemObj);

			if(NULL != jItemCls) {
				jItemArray = env->NewObjectArray(admirerList.size(), jItemCls, NULL);

				int i = 0;
				for(EMFAdmirerList::const_iterator itr = admirerList.begin();
					itr != admirerList.end();
					itr++, i++)
				{
					jmethodID init = env->GetMethodID(jItemCls, "<init>", "("
							"Ljava/lang/String;"	// id
							"Ljava/lang/String;"	// idcode
							"Z"						// readflag
							"I"						// replyflag
							"Ljava/lang/String;"	// womanid
							"Ljava/lang/String;"	// firstname
							"Ljava/lang/String;"	// weight
							"Ljava/lang/String;"	// height
							"Ljava/lang/String;"	// country
							"Ljava/lang/String;"	// province
							"Ljava/lang/String;"	// mtab
							"I"						// age
							"Ljava/lang/String;"	// photoURL
							"Ljava/lang/String;"	// sendTime
							")V");

					jobject jItem = env->NewObject(jItemCls, init,
							env->NewStringUTF(itr->id.c_str()),
							env->NewStringUTF(itr->idcode.c_str()),
							itr->readflag,
							itr->replyflag,
							env->NewStringUTF(itr->womanid.c_str()),
							env->NewStringUTF(itr->firstname.c_str()),
							env->NewStringUTF(itr->weight.c_str()),
							env->NewStringUTF(itr->height.c_str()),
							env->NewStringUTF(itr->country.c_str()),
							env->NewStringUTF(itr->province.c_str()),
							env->NewStringUTF(itr->mtab.c_str()),
							itr->age,
							env->NewStringUTF(itr->photoURL.c_str()),
							env->NewStringUTF(itr->sendTime.c_str())
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
	signure += EMF_ADMIRERLIST_ITEM_CLASS;
	signure += ";";
	signure += ")V";
	jmethodID jCallback = env->GetMethodID(jCallbackCls, "OnEMFAdmirerList", signure.c_str());

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

	FileLog("httprequest", "EMF.Native::OnAdmirerList() end");
}

// ------------------------------ AdmirerViewer ---------------------------------
/*
 * Class:     com_qpidnetwork_request_RequestJniEMF
 * Method:    AdmirerViewer
 * Signature: (Ljava/lang/String;)J
 */
JNIEXPORT jlong JNICALL Java_com_qpidnetwork_request_RequestJniEMF_AdmirerViewer
  (JNIEnv *env, jclass cls, jstring messageid, jobject callback)
{
	jlong requestId = -1;

	// 生成转换的messageid字符串
	string strMessageId("");
	const char *cpMessageId = env->GetStringUTFChars(messageid, 0);
	strMessageId = cpMessageId;
	env->ReleaseStringUTFChars(messageid, cpMessageId);

	// 发出请求
	requestId = gRequestController.AdmirerViewer(strMessageId);
	if (requestId != -1) {
		// 保存callback
		jobject jObj = env->NewGlobalRef(callback);
		gCallbackMap.Insert(requestId, jObj);
	}
	else {
		FileLog("httprequest", "EMF.Native::AdmirerViewer() fails. "
				"requestId:%lld, messageId:%s"
			, requestId, strMessageId.c_str());
	}

	return requestId;
}

void OnAdmirerViewer(long requestId, bool success, const string& errnum, const string& errmsg, const EMFAdmirerViewerItem& item)
{
	FileLog("httprequest", "EMF.Native::OnAdmirerViewer( success : %s )", success?"true":"false");

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
		JavaItemMap::iterator itr = gJavaItemMap.find(EMF_ADMIRERVIEWER_ITEM_CLASS);
		if( itr != gJavaItemMap.end() ) {
			jItemObj = itr->second;
			jclass jItemCls = env->GetObjectClass(jItemObj);

			if(NULL != jItemCls) {
				jmethodID init = env->GetMethodID(jItemCls, "<init>", "("
							"Ljava/lang/String;"	// id
							"Ljava/lang/String;"	// body
							"[Ljava/lang/String;"	// photosURL
							"Ljava/lang/String;"	// womanid
							"Ljava/lang/String;"	// firstname
							"Ljava/lang/String;"	// weight
							"Ljava/lang/String;"	// height
							"Ljava/lang/String;"	// country
							"Ljava/lang/String;"	// province
							"Ljava/lang/String;"	// mtab
							"I"						// age
							"Ljava/lang/String;"	// photoURL
							"Ljava/lang/String;"	// sendTime
							")V");

				jclass jStringCls = env->FindClass("java/lang/String");
				jobjectArray jPhotosURLArray = env->NewObjectArray(item.photosURL.size(), jStringCls, NULL);
				EMFPhotoUrlList::const_iterator iter;
				int iPhotosURLIndex = 0;
				for (iter = item.photosURL.begin(); iter != item.photosURL.end(); iter++, iPhotosURLIndex++) {
					jstring url = env->NewStringUTF((*iter).c_str());
					env->SetObjectArrayElement(jPhotosURLArray, iPhotosURLIndex, url);
					env->DeleteLocalRef(url);
				}

				jItem = env->NewObject(jItemCls, init,
						env->NewStringUTF(item.id.c_str()),
						env->NewStringUTF(item.body.c_str()),
						jPhotosURLArray,
						env->NewStringUTF(item.womanid.c_str()),
						env->NewStringUTF(item.firstname.c_str()),
						env->NewStringUTF(item.weight.c_str()),
						env->NewStringUTF(item.height.c_str()),
						env->NewStringUTF(item.country.c_str()),
						env->NewStringUTF(item.province.c_str()),
						env->NewStringUTF(item.mtab.c_str()),
						item.age,
						env->NewStringUTF(item.sendTime.c_str()),
						env->NewStringUTF(item.photoURL.c_str())
						);

				env->DeleteLocalRef(jPhotosURLArray);
				env->DeleteLocalRef(jItemCls);
			}
		}
	}

	/* real callback java */
	jobject jCallbackObj = gCallbackMap.Erase(requestId);
	jclass jCallbackCls = env->GetObjectClass(jCallbackObj);

	string signure = "(ZLjava/lang/String;Ljava/lang/String;";
	signure += "L";
	signure += EMF_ADMIRERVIEWER_ITEM_CLASS;
	signure += ";";
	signure += ")V";
	jmethodID jCallback = env->GetMethodID(jCallbackCls, "OnEMFAdmirerViewer", signure.c_str());

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

	FileLog("httprequest", "EMF.Native::OnAdmirerViewer() end");
}

// ------------------------------ BlockList ---------------------------------
/*
 * Class:     com_qpidnetwork_request_RequestJniEMF
 * Method:    BlockList
 * Signature: (IILjava/lang/String;)J
 */
JNIEXPORT jlong JNICALL Java_com_qpidnetwork_request_RequestJniEMF_BlockList
  (JNIEnv *env, jclass cls, jint pageIndex, jint pageSize, jstring womanid, jobject callback)
{
	jlong requestId = -1;

	// 生成转换的womanid字符串
	string strWomanId("");
	const char *cpWomanId = env->GetStringUTFChars(womanid, 0);
	strWomanId = cpWomanId;
	env->ReleaseStringUTFChars(womanid, cpWomanId);

	// 发出请求
	requestId = gRequestController.BlockList(pageIndex, pageSize, strWomanId);
	if (requestId != -1) {
		// 保存callback
		jobject jObj = env->NewGlobalRef(callback);
		gCallbackMap.Insert(requestId, jObj);
	}
	else {
		FileLog("httprequest", "EMF.Native::BlockList() fails. "
			"requestId:%lld, pageIndex:%d, pageSize:%d, womanid:%s"
			, requestId, pageIndex, pageSize, strWomanId.c_str());
	}

	return requestId;
}

void OnBlockList(long requestId, bool success, const string& errnum, const string& errmsg, int pageIndex, int pageSize, int dataCount, const EMFBlockList& blockList)
{
	FileLog("httprequest", "EMF.Native::OnBlockList( success : %s )", success?"true":"false");

	/* turn object to java object here */
	JNIEnv* env;
	jint iRet = JNI_ERR;
	gJavaVM->GetEnv((void**)&env, JNI_VERSION_1_4);
	if( env == NULL ) {
		iRet = gJavaVM->AttachCurrentThread((JNIEnv **)&env, NULL);
	}


	jobjectArray jItemArray = NULL;
	if (success) {
		JavaItemMap::iterator itr = gJavaItemMap.find(EMF_BLOCKLIST_ITEM_CLASS);
		if( itr != gJavaItemMap.end() ) {
			jobject jItemObj = itr->second;
			jclass jItemCls = env->GetObjectClass(jItemObj);

			if(NULL != jItemCls) {
				jItemArray = env->NewObjectArray(blockList.size(), jItemCls, NULL);

				int i = 0;
				for(EMFBlockList::const_iterator itr = blockList.begin();
					itr != blockList.end();
					itr++, i++)
				{
					jmethodID init = env->GetMethodID(jItemCls, "<init>", "("
							"Ljava/lang/String;"	// womanid
							"Ljava/lang/String;"	// firstname
							"I"						// age
							"Ljava/lang/String;"	// weight
							"Ljava/lang/String;"	// height
							"Ljava/lang/String;"	// country
							"Ljava/lang/String;"	// province
							"Ljava/lang/String;"	// city
							"Ljava/lang/String;"	// photoURL
							"I"						// blockreason
							")V");

					jobject jItem = env->NewObject(jItemCls, init,
							env->NewStringUTF(itr->womanid.c_str()),
							env->NewStringUTF(itr->firstname.c_str()),
							itr->age,
							env->NewStringUTF(itr->weight.c_str()),
							env->NewStringUTF(itr->height.c_str()),
							env->NewStringUTF(itr->country.c_str()),
							env->NewStringUTF(itr->province.c_str()),
							env->NewStringUTF(itr->city.c_str()),
							env->NewStringUTF(itr->photoURL.c_str()),
							itr->blockreason
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
	signure += EMF_BLOCKLIST_ITEM_CLASS;
	signure += ";";
	signure += ")V";
	jmethodID jCallback = env->GetMethodID(jCallbackCls, "OnEMFBlockList", signure.c_str());

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

	FileLog("httprequest", "EMF.Native::OnBlockList() end");
}

// ------------------------------ Block ---------------------------------
/*
 * Class:     com_qpidnetwork_request_RequestJniEMF
 * Method:    Block
 * Signature: (Ljava/lang/String;I)J
 */
JNIEXPORT jlong JNICALL Java_com_qpidnetwork_request_RequestJniEMF_Block
  (JNIEnv *env, jclass cls, jstring womanid, jint blockreason, jobject callback)
{
	jlong requestId = -1;

	// 生成转换的womanid字符串
	string strWomanId("");
	const char *cpWomanId = env->GetStringUTFChars(womanid, 0);
	strWomanId = cpWomanId;
	env->ReleaseStringUTFChars(womanid, cpWomanId);

	// 发出请求
	requestId = gRequestController.Block(strWomanId, blockreason);
	if (requestId != -1) {
		// 保存callback
		jobject jObj = env->NewGlobalRef(callback);
		gCallbackMap.Insert(requestId, jObj);
	}
	else {
		FileLog("httprequest", "EMF.Native::Block() fails. "
			"requestId:%lld, womanid:%s, blockreason:%d"
			, requestId, strWomanId.c_str(), blockreason);
	}

	return requestId;
}

void OnBlock(long requestId, bool success, const string& errnum, const string& errmsg)
{
	FileLog("httprequest", "EMF.Native::OnBlock( success : %s )", success?"true":"false");

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

	string signure = "(ZLjava/lang/String;Ljava/lang/String;)V";
	jmethodID jCallback = env->GetMethodID(jCallbackCls, "OnEMFBlock", signure.c_str());
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

	FileLog("httprequest", "EMF.Native::OnBlock() end");
}

// ------------------------------ Unblock ---------------------------------
/*
 * Class:     com_qpidnetwork_request_RequestJniEMF
 * Method:    Unblock
 * Signature: ([Ljava/lang/String;)J
 */
JNIEXPORT jlong JNICALL Java_com_qpidnetwork_request_RequestJniEMF_Unblock
  (JNIEnv *env, jclass cls, jobjectArray womanidArray, jobject callback)
{
	jlong requestId = -1;

	// 生成womanid列表
	string womanidLog("");
	EMFWomanidList womanidList;
	for (int i = 0; i < env->GetArrayLength(womanidArray); i++) {
		jstring womanId = (jstring)env->GetObjectArrayElement(womanidArray, i);
		if (womanId != NULL) {
			const char* cpWomanId = env->GetStringUTFChars(womanId, 0);
			string strWomanId = cpWomanId;
			if (!strWomanId.empty()) {
				womanidList.push_back(strWomanId);

				// log
				if (!womanidLog.empty()) {
					womanidLog += ",";
				}
				womanidLog += strWomanId;
			}
			env->ReleaseStringUTFChars(womanId, cpWomanId);
		}
	}

	// 发出请求
	requestId = gRequestController.Unblock(womanidList);
	if (requestId != -1) {
		// 保存callback
		jobject jObj = env->NewGlobalRef(callback);
		gCallbackMap.Insert(requestId, jObj);
	}
	else {
		FileLog("httprequest", "EMF.Native::Unblock() fails. requestId:%lld, womanid:%s"
			, requestId, womanidLog.c_str());
	}

	return requestId;
}

void OnUnblock(long requestId, bool success, const string& errnum, const string& errmsg)
{
	FileLog("httprequest", "EMF.Native::OnUnblock( success : %s )", success?"true":"false");

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

	string signure = "(ZLjava/lang/String;Ljava/lang/String;)V";
	jmethodID jCallback = env->GetMethodID(jCallbackCls, "OnEMFUnblock", signure.c_str());
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

	FileLog("httprequest", "EMF.Native::OnUnblock() end");
}

// ------------------------------ InboxPhotoFee ---------------------------------
/*
 * Class:     com_qpidnetwork_request_RequestJniEMF
 * Method:    InboxPhotoFee
 * Signature: (Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Lcom/qpidnetwork/request/OnEMFInboxPhotoFeeCallback;)J
 */
JNIEXPORT jlong JNICALL Java_com_qpidnetwork_request_RequestJniEMF_InboxPhotoFee
  (JNIEnv *env, jclass cls, jstring womanId, jstring photoId, jstring sendId, jstring messageId, jobject callback)
{
	jlong requestId = -1;

	// womanid
	string strWomanId("");
	const char *cpWomanId = env->GetStringUTFChars(womanId, 0);
	strWomanId = cpWomanId;
	env->ReleaseStringUTFChars(womanId, cpWomanId);
	// photoid
	string strPhotoId("");
	const char *cpPhotoId = env->GetStringUTFChars(photoId, 0);
	strPhotoId = cpPhotoId;
	env->ReleaseStringUTFChars(photoId, cpPhotoId);
	// sendid
	string strSendId("");
	const char *cpSendId = env->GetStringUTFChars(sendId, 0);
	strSendId = cpSendId;
	env->ReleaseStringUTFChars(sendId, cpSendId);
	// messageid
	string strMessageId("");
	const char *cpMessageId = env->GetStringUTFChars(messageId, 0);
	strMessageId = cpMessageId;
	env->ReleaseStringUTFChars(messageId, cpMessageId);

	// 发出请求
	requestId = gRequestController.InboxPhotoFee(strWomanId, strPhotoId, strSendId, strMessageId);
	if (requestId != -1) {
		// 保存callback
		jobject jObj = env->NewGlobalRef(callback);
		gCallbackMap.Insert(requestId, jObj);
	}
	else {
		FileLog("httprequest", "EMF.Native::InboxPhotoFee() fails. "
			"requestId:%lld, womanid:%s, photoid:%s, sendid:%s, messageid:%s"
			, requestId, strWomanId.c_str(), strPhotoId.c_str(), strSendId.c_str(), strMessageId.c_str());
	}

	return requestId;
}

void OnInboxPhotoFee(long requestId, bool success, const string& errnum, const string& errmsg)
{
	FileLog("httprequest", "EMF.Native::OnInboxPhotoFee( success : %s )", success?"true":"false");

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

	string signure = "(ZLjava/lang/String;Ljava/lang/String;)V";
	jmethodID jCallback = env->GetMethodID(jCallbackCls, "OnEMFInboxPhotoFee", signure.c_str());
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

	FileLog("httprequest", "EMF.Native::OnInboxPhotoFee() end");
}

// ------------------------------ PrivatePhotoView ---------------------------------
/*
 * Class:     com_qpidnetwork_request_RequestJniEMF
 * Method:    PrivatePhotoView
 * Signature: (Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Lcom/qpidnetwork/request/OnEMFPrivatePhotoViewCallback;)J
 */
JNIEXPORT jlong JNICALL Java_com_qpidnetwork_request_RequestJniEMF_PrivatePhotoView
  (JNIEnv *env, jclass cls, jstring womanId, jstring photoId, jstring sendId, jstring messageId, jstring filePath, jint type, jobject callback)
{
	jlong requestId = -1;

	// womanid
	string strWomanId("");
	const char *cpWomanId = env->GetStringUTFChars(womanId, 0);
	strWomanId = cpWomanId;
	env->ReleaseStringUTFChars(womanId, cpWomanId);
	// photoid
	string strPhotoId("");
	const char *cpPhotoId = env->GetStringUTFChars(photoId, 0);
	strPhotoId = cpPhotoId;
	env->ReleaseStringUTFChars(photoId, cpPhotoId);
	// sendid
	string strSendId("");
	const char *cpSendId = env->GetStringUTFChars(sendId, 0);
	strSendId = cpSendId;
	env->ReleaseStringUTFChars(sendId, cpSendId);
	// messageid
	string strMessageId("");
	const char *cpMessageId = env->GetStringUTFChars(messageId, 0);
	strMessageId = cpMessageId;
	env->ReleaseStringUTFChars(messageId, cpMessageId);
	// filePath
	string strFilePath("");
	const char *cpFilePath = env->GetStringUTFChars(filePath, 0);
	strFilePath = cpFilePath;
	env->ReleaseStringUTFChars(filePath, cpFilePath);

	// 发出请求
	requestId = gRequestController.PrivatePhotoView(strWomanId, strPhotoId, strSendId, strMessageId, strFilePath, type);
	if (requestId != -1) {
		// 保存callback
		jobject jObj = env->NewGlobalRef(callback);
		gCallbackMap.Insert(requestId, jObj);
	}
	else {
		FileLog("httprequest", "EMF.Native::PrivatePhotoView() fails. "
			"requestId:%lld, womanid:%s, photoid:%s, sendid:%s, messageid:%s, filePath:%s"
			, requestId, strWomanId.c_str(), strPhotoId.c_str(), strSendId.c_str(), strMessageId.c_str(), strFilePath.c_str());
	}

	return requestId;
}

void OnPrivatePhotoView(long requestId, bool success, const string& errnum, const string& errmsg, const string& filePath)
{
	FileLog("httprequest", "EMF.Native::OnPrivatePhotoView( success : %s )", success?"true":"false");

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

	string signure = "(ZLjava/lang/String;Ljava/lang/String;Ljava/lang/String;)V";
	jmethodID jCallback = env->GetMethodID(jCallbackCls, "OnEMFPrivatePhotoView", signure.c_str());
	if( jCallbackObj != NULL && jCallback != NULL ) {
		jstring jerrno = env->NewStringUTF(errnum.c_str());
		jstring jerrmsg = env->NewStringUTF(errmsg.c_str());
		jstring jfilePath = env->NewStringUTF(filePath.c_str());

		env->CallVoidMethod(jCallbackObj, jCallback, success, jerrno, jerrmsg, jfilePath);

		env->DeleteLocalRef(jfilePath);
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

	FileLog("httprequest", "EMF.Native::OnPrivatePhotoView() end");
}

