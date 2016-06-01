/*
 * com_qpidnetwork_request_RequestJniEMF.cpp
 *
 *  Created on: 2015-07-13
 *      Author: Samson.Fan
 * Description:
 */
#include "com_qpidnetwork_request_RequestJniTicket.h"
#include "com_qpidnetwork_request_RequestJni_GobalFunc.h"
#include <manrequesthandler/RequestTicketController.h>

void OnTicketList(long requestId, bool success, const string& errnum, const string& errmsg, int pageIndex, int pageSize, int dataCount, const TicketList& ticketList);
void OnTicketDetail(long requestId, bool success, const string& errnum, const string& errmsg, const TicketDetailItem& item);
void OnReplyTicket(long requestId, bool success, const string& errnum, const string& errmsg);
void OnResolvedTicket(long requestId, bool success, const string& errnum, const string& errmsg);
void OnAddTicket(long requestId, bool success, const string& errnum, const string& errmsg);
static RequestTicketControllerCallback gRequestControllerCallback {
	OnTicketList,
	OnTicketDetail,
	OnReplyTicket,
	OnResolvedTicket,
	OnAddTicket
};
static RequestTicketController gRequestController(&gHttpRequestManager, gRequestControllerCallback);

// ------------------------------ TicketList ---------------------------------
/*
 * Class:     com_qpidnetwork_request_RequestJniTicket
 * Method:    TicketList
 * Signature: (IILcom/qpidnetwork/request/OnTicketListCallback;)J
 */
JNIEXPORT jlong JNICALL Java_com_qpidnetwork_request_RequestJniTicket_TicketList
  (JNIEnv *env, jclass cls, jint pageIndex, jint pageSize, jobject callback)
{
	jlong requestId = -1;

	// 发出请求
	requestId = gRequestController.QueryTicketList(pageIndex, pageSize);
	if (requestId != -1) {
		// 保存callback
		jobject jObj = env->NewGlobalRef(callback);
		gCallbackMap.Insert(requestId, jObj);
		FileLog("httprequest", "Ticket.Native::TicketList() requestId:%lld, callback:%p, jObj:%p", requestId, callback, jObj);
	}
	else {
		FileLog("httprequest", "Ticket.Native::TicketList() fails. "
				"requestId:%lld, pageIndex:%d, pageSize:%d"
			, requestId, pageIndex, pageSize);
	}

	return requestId;
}

void OnTicketList(long requestId, bool success, const string& errnum, const string& errmsg, int pageIndex, int pageSize, int dataCount, const TicketList& ticketList)
{
	FileLog("httprequest", "Ticket.Native::OnTicketList( success : %s )", success?"true":"false");

	/* turn object to java object here */
	JNIEnv* env;
	jint iRet = JNI_ERR;
	gJavaVM->GetEnv((void**)&env, JNI_VERSION_1_4);
	if( env == NULL ) {
		iRet = gJavaVM->AttachCurrentThread((JNIEnv **)&env, NULL);
	}

	jobjectArray jItemArray = NULL;
	if (success) {
		JavaItemMap::iterator itr = gJavaItemMap.find(TICKET_TICKETLISTITEM_CLASS);
		if( itr != gJavaItemMap.end() ) {
			jobject jItemObj = itr->second;
			jclass jItemCls = env->GetObjectClass(jItemObj);
			if(NULL != jItemCls) {
				jItemArray = env->NewObjectArray(ticketList.size(), jItemCls, NULL);

				int i = 0;
				TicketList::const_iterator itr;
				for(itr = ticketList.begin();
					itr != ticketList.end();
					itr++, i++)
				{
					jmethodID init = env->GetMethodID(jItemCls, "<init>", "("
							"Ljava/lang/String;"	// ticketId
							"Ljava/lang/String;"	// title
							"I"						// unreadNum
							"I"						// status
							"I"						// addDate
							")V");

					jstring jticketId = env->NewStringUTF(itr->ticketId.c_str());
					jstring jtitle = env->NewStringUTF(itr->title.c_str());
					jobject jItem = env->NewObject(jItemCls, init,
							jticketId,
							jtitle,
							itr->unreadNum,
							itr->status,
							itr->addDate
							);
					env->DeleteLocalRef(jticketId);
					env->DeleteLocalRef(jtitle);

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
	signure += TICKET_TICKETLISTITEM_CLASS;
	signure += ";";
	signure += ")V";
	jmethodID jCallback = env->GetMethodID(jCallbackCls, "OnTicketList", signure.c_str());

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

// ------------------------------ TicketDetail ---------------------------------
/*
 * Class:     com_qpidnetwork_request_RequestJniTicket
 * Method:    TicketDetail
 * Signature: (Ljava/lang/String;Lcom/qpidnetwork/request/OnTicketDetailCallback;)J
 */
JNIEXPORT jlong JNICALL Java_com_qpidnetwork_request_RequestJniTicket_TicketDetail
  (JNIEnv *env, jclass cls, jstring ticketId, jobject callback)
{
	jlong requestId = -1;

	// 生成转换的ticketId字符串
	string strTicketId("");
	const char *cpTicketId = env->GetStringUTFChars(ticketId, 0);
	strTicketId = cpTicketId;
	env->ReleaseStringUTFChars(ticketId, cpTicketId);

	// 发出请求
	requestId = gRequestController.TicketDetail(strTicketId);
	if (requestId != -1) {
		// 保存callback
		jobject jObj = env->NewGlobalRef(callback);
		gCallbackMap.Insert(requestId, jObj);
		FileLog("httprequest", "Ticket.Native::TicketDetail() requestId:%lld, callback:%p, jObj:%p", requestId, callback, jObj);
	}
	else {
		FileLog("httprequest", "TicketShow.Native::TicketDetail() fails. "
				"requestId:%lld, ticketId:%s"
			, requestId, strTicketId.c_str());
	}

	return requestId;
}

void OnTicketDetail(long requestId, bool success, const string& errnum, const string& errmsg, const TicketDetailItem& item)
{
	FileLog("httprequest", "Ticket.Native::OnTicketDetail( success : %s )", success?"true":"false");

	/* turn object to java object here */
	JNIEnv* env;
	jint iRet = JNI_ERR;
	gJavaVM->GetEnv((void**)&env, JNI_VERSION_1_4);
	if( env == NULL ) {
		iRet = gJavaVM->AttachCurrentThread((JNIEnv **)&env, NULL);
	}

	jobject jItem = NULL;
	if (success) {
		jobjectArray jContentItemArray = NULL;
		JavaItemMap::iterator contentClsItr = gJavaItemMap.find(TICKET_TICKETCONTENTITEM_CLASS);
		if( contentClsItr != gJavaItemMap.end() ) {
			jobject jContentItemObj = contentClsItr->second;
			jclass jContentItemCls = env->GetObjectClass(jContentItemObj);
			if(NULL != jContentItemCls) {
				// create contentList array
				jContentItemArray = env->NewObjectArray(item.contentList.size(), jContentItemCls, NULL);
				int iContentIndex = 0;
				TicketContentList::const_iterator contentItr;
				for(contentItr = item.contentList.begin();
					contentItr != item.contentList.end();
					contentItr++, iContentIndex++)
				{
					// create fileList array
					jclass jStringCls = env->FindClass("java/lang/String");
					jobjectArray jFileArray = env->NewObjectArray(contentItr->fileList.size(), jStringCls, NULL);
					TicketFileList::const_iterator fileIter;
					int iFileIndex = 0;
					for (fileIter = contentItr->fileList.begin();
						fileIter != contentItr->fileList.end();
						fileIter++, iFileIndex++)
					{
						jstring url = env->NewStringUTF((*fileIter).c_str());
						env->SetObjectArrayElement(jFileArray, iFileIndex, url);
						env->DeleteLocalRef(url);
					}

					jmethodID init = env->GetMethodID(jContentItemCls, "<init>", "("
							"I"						// method
							"Ljava/lang/String;"	// fromName
							"Ljava/lang/String;"	// toName
							"I"						// sendDate
							"Ljava/lang/String;"	// message
							"[Ljava/lang/String;"	// fileList
							")V");

					jstring jfromName = env->NewStringUTF(contentItr->fromName.c_str());
					jstring jtoName = env->NewStringUTF(contentItr->toName.c_str());
					jstring jmessage = env->NewStringUTF(contentItr->message.c_str());
					jobject jContentItem = env->NewObject(jContentItemCls, init,
							contentItr->method,
							jfromName,
							jtoName,
							contentItr->sendDate,
							jmessage,
							jFileArray
							);
					env->DeleteLocalRef(jfromName);
					env->DeleteLocalRef(jtoName);
					env->DeleteLocalRef(jmessage);

					env->SetObjectArrayElement(jContentItemArray, iContentIndex, jContentItem);

					env->DeleteLocalRef(jContentItem);
					env->DeleteLocalRef(jFileArray);
				}
				env->DeleteLocalRef(jContentItemCls);
			}
		}

		JavaItemMap::iterator detailClsItr = gJavaItemMap.find(TICKET_TICKETDETAILITEM_CLASS);
		if( detailClsItr != gJavaItemMap.end() ) {
			jobject jDetailItemObj = detailClsItr->second;
			jclass jDetailItemCls = env->GetObjectClass(jDetailItemObj);
			if (NULL != jDetailItemCls) {
				jmethodID init = env->GetMethodID(jDetailItemCls, "<init>", "("
						"Ljava/lang/String;"	// title
						"I"						// status
						"[L"
						TICKET_TICKETCONTENTITEM_CLASS	// contentList
						";"
						")V");

				jstring jtitle = env->NewStringUTF(item.title.c_str());
				jItem = env->NewObject(jDetailItemCls, init,
							jtitle,
							item.status,
							jContentItemArray
							);
				env->DeleteLocalRef(jtitle);
			}
		}

		if (NULL != jContentItemArray) {
			env->DeleteLocalRef(jContentItemArray);
		}
	}

	/* real callback java */
	jobject jCallbackObj = gCallbackMap.Erase(requestId);
	jclass jCallbackCls = env->GetObjectClass(jCallbackObj);

	string signure = "(ZLjava/lang/String;Ljava/lang/String;";
	signure += "L";
	signure += TICKET_TICKETDETAILITEM_CLASS;
	signure += ";";
	signure += ")V";
	jmethodID jCallback = env->GetMethodID(jCallbackCls, "OnTicketDetail", signure.c_str());

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

// ------------------------------ ReplyTicket ---------------------------------
/*
 * Class:     com_qpidnetwork_request_RequestJniTicket
 * Method:    ReplyTicket
 * Signature: (Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Lcom/qpidnetwork/request/OnRequestCallback;)J
 */
JNIEXPORT jlong JNICALL Java_com_qpidnetwork_request_RequestJniTicket_ReplyTicket
  (JNIEnv *env, jclass cls, jstring ticketId, jstring message, jstring filePath, jobject callback)
{
	jlong requestId = -1;

	// 生成转换的ticketId字符串
	string strTicketId("");
	const char *cpTicketId = env->GetStringUTFChars(ticketId, 0);
	strTicketId = cpTicketId;
	env->ReleaseStringUTFChars(ticketId, cpTicketId);
	// 生成转换的message字符串
	string strMessage("");
	const char *cpMessage = env->GetStringUTFChars(message, 0);
	strMessage = cpMessage;
	env->ReleaseStringUTFChars(message, cpMessage);
	// 生成转换的filePath字符串
	string strFilePath("");
	const char *cpFilePath = env->GetStringUTFChars(filePath, 0);
	strFilePath = cpFilePath;
	env->ReleaseStringUTFChars(filePath, cpFilePath);

	// 发出请求
	requestId = gRequestController.ReplyTicket(strTicketId, strMessage, strFilePath);
	if (requestId != -1) {
		// 保存callback
		jobject jObj = env->NewGlobalRef(callback);
		gCallbackMap.Insert(requestId, jObj);
		FileLog("httprequest", "Ticket.Native::ReplyTicket() requestId:%lld, callback:%p, jObj:%p", requestId, callback, jObj);
	}
	else {
		FileLog("httprequest", "Ticket.Native::ReplyTicket() fails. "
				"requestId:%lld, ticketId:%s, message:%s, filePath:%s"
			, requestId, strTicketId.c_str(), strMessage.c_str(), strFilePath.c_str());
	}

	return requestId;
}

void OnReplyTicket(long requestId, bool success, const string& errnum, const string& errmsg)
{
	FileLog("httprequest", "Ticket.Native::OnReplyTicket( success : %s )", success?"true":"false");

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
	jmethodID jCallback = env->GetMethodID(jCallbackCls, "OnRequest", signure.c_str());

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

// ------------------------------ ResolvedTicket ---------------------------------
/*
 * Class:     com_qpidnetwork_request_RequestJniTicket
 * Method:    ResolvedTicket
 * Signature: (Ljava/lang/String;Lcom/qpidnetwork/request/OnRequestCallback;)J
 */
JNIEXPORT jlong JNICALL Java_com_qpidnetwork_request_RequestJniTicket_ResolvedTicket
  (JNIEnv *env, jclass cls, jstring ticketId, jobject callback)
{
	jlong requestId = -1;

	// 生成转换的ticketId字符串
	string strTicketId("");
	const char *cpTicketId = env->GetStringUTFChars(ticketId, 0);
	strTicketId = cpTicketId;
	env->ReleaseStringUTFChars(ticketId, cpTicketId);

	// 发出请求
	requestId = gRequestController.ResolvedTicket(strTicketId);
	if (requestId != -1) {
		// 保存callback
		jobject jObj = env->NewGlobalRef(callback);
		gCallbackMap.Insert(requestId, jObj);
		FileLog("httprequest", "Ticket.Native::ResolvedTicket() requestId:%lld, callback:%p, jObj:%p", requestId, callback, jObj);
	}
	else {
		FileLog("httprequest", "Ticket.Native::ResolvedTicket() fails. "
				"requestId:%lld, ticketId:%s"
			, requestId, strTicketId.c_str());
	}

	return requestId;
}

void OnResolvedTicket(long requestId, bool success, const string& errnum, const string& errmsg)
{
	FileLog("httprequest", "Ticket.Native::OnReplyTicket( success : %s )", success?"true":"false");

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
	jmethodID jCallback = env->GetMethodID(jCallbackCls, "OnRequest", signure.c_str());

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

// ------------------------------ AddTicket ---------------------------------
/*
 * Class:     com_qpidnetwork_request_RequestJniTicket
 * Method:    AddTicket
 * Signature: (ILjava/lang/String;Ljava/lang/String;Ljava/lang/String;Lcom/qpidnetwork/request/OnRequestCallback;)J
 */
JNIEXPORT jlong JNICALL Java_com_qpidnetwork_request_RequestJniTicket_AddTicket
  (JNIEnv *env, jclass cls, jint typeId, jstring title, jstring message, jstring filePath, jobject callback)
{
	jlong requestId = -1;

	// 生成转换的title字符串
	string strTitle("");
	const char *cpTitle = env->GetStringUTFChars(title, 0);
	strTitle = cpTitle;
	env->ReleaseStringUTFChars(title, cpTitle);
	// 生成转换的message字符串
	string strMessage("");
	const char *cpMessage = env->GetStringUTFChars(message, 0);
	strMessage = cpMessage;
	env->ReleaseStringUTFChars(message, cpMessage);
	// 生成转换的filePath字符串
	string strFilePath("");
	const char *cpFilePath = env->GetStringUTFChars(filePath, 0);
	strFilePath = cpFilePath;
	env->ReleaseStringUTFChars(filePath, cpFilePath);

	// 发出请求
	requestId = gRequestController.AddTicket(typeId, strTitle, strMessage, strFilePath);
	if (requestId != -1) {
		// 保存callback
		jobject jObj = env->NewGlobalRef(callback);
		gCallbackMap.Insert(requestId, jObj);
		FileLog("httprequest", "Ticket.Native::AddTicket() requestId:%lld, callback:%p, jObj:%p", requestId, callback, jObj);
	}
	else {
		FileLog("httprequest", "Ticket.Native::AddTicket() fails. "
				"requestId:%lld, typeId:%d, title:%s, message:%s, filePath:%s"
			, requestId, typeId, strTitle.c_str(), strMessage.c_str(), strFilePath.c_str());
	}

	return requestId;
}

void OnAddTicket(long requestId, bool success, const string& errnum, const string& errmsg)
{
	FileLog("httprequest", "Ticket.Native::OnReplyTicket( success : %s )", success?"true":"false");

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
	jmethodID jCallback = env->GetMethodID(jCallbackCls, "OnRequest", signure.c_str());

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
