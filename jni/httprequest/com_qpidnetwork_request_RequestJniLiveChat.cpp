/*
 * com_qpidnetwork_request_RequestJniLiveChat.cpp
 *
 *  Created on: 2015-4-27
 *      Author: Samson
 */
#include "com_qpidnetwork_request_RequestJniLiveChat.h"
#include "com_qpidnetwork_request_RequestJni_GobalFunc.h"
#include "RequestLiveChatController.h"

void onCheckCoupon(long requestId, bool success, Coupon item, string userId, string errnum, string errmsg);
void onUseCoupon(long requestId, bool success, string errnum, string errmsg, string userId);
void onQueryChatVirtualGift(long requestId, bool success, list<Gift> giftList, int totalCount, string path, string version,string errnum, string errmsg);
void onQueryChatRecord(long requestId, bool success, int dbTime, list<Record> recordList, string errnum, string errmsg, string inviteId);
void onQueryChatRecordMutiple(long requestId, bool success, int dbTime, list<RecordMutiple> recordMutiList, string errnum, string errmsg);
void onSendPhoto(long requestId, bool success, const string& errnum, const string& errmsg, const LCSendPhotoItem& item);
void onPhotoFee(long requestId, bool success, const string& errnum, const string& errmsg);
void onGetPhoto(long requestId, bool success, const string& errnum, const string& errmsg, const string& filePath);
void onUploadVoice(long requestId, bool success, const string& errnum, const string& errmsg, const string& voiceId);
void onPlayVoice(long requestId, bool success, const string& errnum, const string& errmsg, const string& filePath);
void onSendGift(long requestId, bool success, const string& errnum, const string& errmsg);
void onQueryRecentVideoList(long requestId, bool success, list<LCVideoItem> itemList, string errnum, string errmsg);
void onGetVideoPhoto(long requestId, bool success, const string& errnum, const string& errmsg, const string& filePath);
void onGetVideo(long requestId, bool success, const string& errnum, const string& errmsg, const string& url);
RequestLiveChatControllerCallback gRequestLiveChatControllerCallback {
	onCheckCoupon,
	onUseCoupon,
	onQueryChatVirtualGift,
	onQueryChatRecord,
	onQueryChatRecordMutiple,
	onSendPhoto,
	onPhotoFee,
	onGetPhoto,
	onUploadVoice,
	onPlayVoice,
	onSendGift,
	onQueryRecentVideoList,
	onGetVideoPhoto,
	onGetVideo,
};
RequestLiveChatController gRequestLiveChatController(&gHttpRequestManager, gRequestLiveChatControllerCallback);

/******************************************************************************/
void onCheckCoupon(long requestId, bool success, Coupon item, string userId, string errnum, string errmsg) {
	JNIEnv* env = NULL;
	bool isAttachThread = false;
	GetEnv(&env, &isAttachThread);

	FileLog("httprequest", "LiveChat.Native::onCheckCoupon( success : %s, env:%p, isAttachThread:%d )", success?"true":"false", env, isAttachThread);

	/* turn object to java object here */
	jobject jItem = NULL;
	JavaItemMap::iterator itr = gJavaItemMap.find(LIVECHAT_COUPON_ITEM_CLASS);
	if( itr != gJavaItemMap.end() ) {
		jclass cls = env->GetObjectClass(itr->second);
		if( cls != NULL) {
			jmethodID init = env->GetMethodID(cls, "<init>", "("
					"Ljava/lang/String;"
					"I"
					")V"
					);

			FileLog("httprequest", "LiveChat.Native::onCheckCoupon( GetMethodID <init> : %p )", init);

			jstring juserId = env->NewStringUTF(userId.c_str());
			if( init != NULL ) {
				jItem = env->NewObject(cls, init,
						juserId,
						item.status
						);
				FileLog("httprequest", "LiveChat.Native::onCheckCoupon( NewObject: %p , item.status:%d )", jItem, item.status);
			}
			env->DeleteLocalRef(juserId);
		}
	}

	/* real callback java */
	jobject callbackObj = gCallbackMap.Erase(requestId);

	jclass callbackCls = env->GetObjectClass(callbackObj);

	string signure = "(JZLjava/lang/String;Ljava/lang/String;";
	signure += "L";
	signure += LIVECHAT_COUPON_ITEM_CLASS;
	signure += ";";
	signure += ")V";
	jmethodID callback = env->GetMethodID(callbackCls, "OnCheckCoupon", signure.c_str());
	FileLog("httprequest", "LiveChat.Native::onCheckCoupon( callback : %p, signure : %s )",
			callback, signure.c_str());

	if( callbackObj != NULL && callback != NULL ) {
		jstring jerrno = env->NewStringUTF(errnum.c_str());
		jstring jerrmsg = env->NewStringUTF(errmsg.c_str());
		jlong jrequestId = requestId;

		FileLog("httprequest", "LiveChat.Native::onCheckCoupon( CallObjectMethod "
				"jItem : %p )", jItem);

		env->CallVoidMethod(callbackObj, callback, jrequestId, success, jerrno, jerrmsg, jItem);

		env->DeleteGlobalRef(callbackObj);

		env->DeleteLocalRef(jerrno);
		env->DeleteLocalRef(jerrmsg);
	}

	if( jItem != NULL ) {
		env->DeleteLocalRef(jItem);
	}

	ReleaseEnv(isAttachThread);
}
/*
 * Class:     com_qpidnetwork_request_RequestJniLiveChat
 * Method:    CheckCoupon
 * Signature: (Ljava/lang/String;Lcom/qpidnetwork/request/OnCheckCouponCallCallback;)J
 */
JNIEXPORT jlong JNICALL Java_com_qpidnetwork_request_RequestJniLiveChat_CheckCoupon
  (JNIEnv *env, jclass, jstring womanId, jobject callback) {
	jlong requestId = -1;

	const char *cpWomanId = env->GetStringUTFChars(womanId, 0);

	requestId = gRequestLiveChatController.CheckCoupon(cpWomanId);
	jobject obj = env->NewGlobalRef(callback);
	gCallbackMap.Insert(requestId, obj);

	env->ReleaseStringUTFChars(womanId, cpWomanId);

	return requestId;
}

/******************************************************************************/
void onUseCoupon(long requestId, bool success, string errnum, string errmsg, string userId) {
	JNIEnv* env = NULL;
	bool isAttachThread = false;
	GetEnv(&env, &isAttachThread);

	FileLog("httprequest", "LiveChat.Native::onUseCoupon( success : %s, env:%p, isAttachThread:%d )", success?"true":"false", env, isAttachThread);

	/* real callback java */
	jobject callbackObj = gCallbackMap.Erase(requestId);
	jclass callbackCls = env->GetObjectClass(callbackObj);

	string signure = "(JZLjava/lang/String;Ljava/lang/String;Ljava/lang/String;)V";
	jmethodID callback = env->GetMethodID(callbackCls, "OnLCUseCoupon", signure.c_str());
	FileLog("httprequest", "LiveChat.Native::onUseCoupon( callbackCls : %p, callback : %p, signure : %s )",
			callbackCls, callback, signure.c_str());

	if( callbackObj != NULL && callback != NULL ) {
		jstring jerrno = env->NewStringUTF(errnum.c_str());
		jstring jerrmsg = env->NewStringUTF(errmsg.c_str());
		jstring juserId = env->NewStringUTF(userId.c_str());
		jlong jrequestId = requestId;

		FileLog("httprequest", "LiveChat.Native::onUseCoupon( CallObjectMethod )");

		env->CallVoidMethod(callbackObj, callback, jrequestId, success, jerrno, jerrmsg, juserId);

		env->DeleteGlobalRef(callbackObj);

		env->DeleteLocalRef(jerrno);
		env->DeleteLocalRef(jerrmsg);
		env->DeleteLocalRef(juserId);
	}

	ReleaseEnv(isAttachThread);
}
/*
 * Class:     com_qpidnetwork_request_RequestJniLiveChat
 * Method:    UseCoupon
 * Signature: (Ljava/lang/String;Lcom/qpidnetwork/request/OnRequestCallback;)J
 */
JNIEXPORT jlong JNICALL Java_com_qpidnetwork_request_RequestJniLiveChat_UseCoupon
  (JNIEnv *env, jclass, jstring womanId, jobject callback) {
	jlong requestId = -1;

	const char *cpWomanId = env->GetStringUTFChars(womanId, 0);

	requestId = gRequestLiveChatController.UseCoupon(cpWomanId);
	jobject obj = env->NewGlobalRef(callback);
	gCallbackMap.Insert(requestId, obj);

	env->ReleaseStringUTFChars(womanId, cpWomanId);

	return requestId;
}

/******************************************************************************/
void onQueryChatVirtualGift(long requestId, bool success, list<Gift> giftList, int totalCount,
		string path, string version, string errnum, string errmsg)
{
	JNIEnv* env = NULL;
	bool isAttachThread = false;
	GetEnv(&env, &isAttachThread);

	FileLog("httprequest", "LiveChat.Native::onQueryChatVirtualGift( success : %s, env:%p, isAttachThread:%d )", success?"true":"false", env, isAttachThread);

	/* turn object to java object here */
	jobjectArray jItemArray = NULL;
	JavaItemMap::iterator itr = gJavaItemMap.find(LIVECHAT_GIFT_ITEM_CLASS);
	if( itr != gJavaItemMap.end() ) {
		jclass cls = env->GetObjectClass(itr->second);
		jmethodID init = env->GetMethodID(cls, "<init>", "("
				"Ljava/lang/String;"
				"Ljava/lang/String;"
				"Ljava/lang/String;"
				")V");

		if( giftList.size() > 0 ) {
			jItemArray = env->NewObjectArray(giftList.size(), cls, NULL);
			int i = 0;
			for(list<Gift>::iterator itr = giftList.begin(); itr != giftList.end(); itr++, i++) {
				jstring vgid = env->NewStringUTF(itr->vgid.c_str());
				jstring title = env->NewStringUTF(itr->title.c_str());
				jstring price = env->NewStringUTF(itr->price.c_str());

				jobject item = env->NewObject(cls, init,
						vgid,
						title,
						price
						);

				env->SetObjectArrayElement(jItemArray, i, item);

				env->DeleteLocalRef(vgid);
				env->DeleteLocalRef(title);
				env->DeleteLocalRef(price);

				env->DeleteLocalRef(item);
			}
		}
	}

	/* real callback java */
	jobject callbackObj = gCallbackMap.Erase(requestId);

	jclass callbackCls = env->GetObjectClass(callbackObj);

	string signure = "(ZLjava/lang/String;Ljava/lang/String;";
	signure += "[L";
	signure += LIVECHAT_GIFT_ITEM_CLASS;
	signure += ";";
	signure += "I";
	signure += "Ljava/lang/String;";
	signure	+= "Ljava/lang/String;";
	signure += ")V";
	jmethodID callback = env->GetMethodID(callbackCls, "OnQueryChatVirtualGift", signure.c_str());
	FileLog("httprequest", "LiveChat.Native::onQueryChatVirtualGift( callback : %p, signure : %s )",
			callback, signure.c_str());

	if( callbackObj != NULL && callback != NULL ) {
		jstring jerrno = env->NewStringUTF(errnum.c_str());
		jstring jerrmsg = env->NewStringUTF(errmsg.c_str());
		jstring jpath = env->NewStringUTF(path.c_str());
		jstring jversion = env->NewStringUTF(version.c_str());


		FileLog("httprequest", "LiveChat.Native::onQueryChatVirtualGift( CallObjectMethod "
				"jItemArray : %p )", jItemArray);

		env->CallVoidMethod(callbackObj, callback, success, jerrno, jerrmsg,
				jItemArray, totalCount, jpath, jversion);

		env->DeleteGlobalRef(callbackObj);

		env->DeleteLocalRef(jerrno);
		env->DeleteLocalRef(jerrmsg);
		env->DeleteLocalRef(jpath);
		env->DeleteLocalRef(jversion);
	}

	if( jItemArray != NULL ) {
		env->DeleteLocalRef(jItemArray);
	}

	ReleaseEnv(isAttachThread);
}
/*
 * Class:     com_qpidnetwork_request_RequestJniLiveChat
 * Method:    QueryChatVirtualGift
 * Signature: (Ljava/lang/String;Ljava/lang/String;Lcom/qpidnetwork/request/OnQueryChatVirtualGiftCallback;)J
 */
JNIEXPORT jlong JNICALL Java_com_qpidnetwork_request_RequestJniLiveChat_QueryChatVirtualGift
  (JNIEnv *env, jclass, jstring sessionId, jstring userId, jobject callback) {
	jlong requestId = -1;

	const char *cpSessionId = env->GetStringUTFChars(sessionId, 0);
	const char *cpUserId = env->GetStringUTFChars(userId, 0);

	requestId = gRequestLiveChatController.QueryChatVirtualGift(cpSessionId, cpUserId);
	jobject obj = env->NewGlobalRef(callback);
	gCallbackMap.Insert(requestId, obj);

	env->ReleaseStringUTFChars(sessionId, cpSessionId);
	env->ReleaseStringUTFChars(userId, cpUserId);

	return requestId;
}

/******************************************************************************/
jobjectArray GetArrayWithListRecord(JNIEnv* env, const list<Record>& recordList)
{
	FileLog("httprequest", "LiveChat.Native::GetArrayWithListRecord() begin, recordList.size:%d", recordList.size());
	jobjectArray jItemArray = NULL;
	JavaItemMap::iterator itr = gJavaItemMap.find(LIVECHAT_RECORD_ITEM_CLASS);
	if( itr != gJavaItemMap.end() ) {
		jclass cls = env->GetObjectClass(itr->second);
		FileLog("httprequest", "LiveChat.Native::GetArrayWithListRecord() cls:%p", cls);
		if( NULL != cls && recordList.size() > 0 )
		{
			jItemArray = env->NewObjectArray(recordList.size(), cls, NULL);
			int i = 0;
			for(list<Record>::const_iterator itr = recordList.begin(); itr != recordList.end(); itr++, i++) {
				jmethodID init = env->GetMethodID(cls, "<init>", "("
						"I"						// toflag
						"I"						// adddate
						"I"						// messageType
						"Ljava/lang/String;"	// textMsg
						"Ljava/lang/String;"	// inviteMsg
						"Ljava/lang/String;"	// warningMsg
						"Ljava/lang/String;"	// emotionId
						"Ljava/lang/String;"	// photoId
						"Ljava/lang/String;"	// photoSendId
						"Ljava/lang/String;"	// photoDesc
						"Z"						// photoCharge
						"Ljava/lang/String;"	// voiceId
						"Ljava/lang/String;"	// voiceType
						"I"						// voiceTime
						")V");

				jstring textMsg = env->NewStringUTF(itr->textMsg.c_str());
				jstring inviteMsg = env->NewStringUTF(itr->inviteMsg.c_str());
				jstring warningMsg = env->NewStringUTF(itr->warningMsg.c_str());
				jstring emotionId = env->NewStringUTF(itr->emotionId.c_str());
				jstring photoId = env->NewStringUTF(itr->photoId.c_str());
				jstring photoSendId = env->NewStringUTF(itr->photoSendId.c_str());
				jstring photoDesc = env->NewStringUTF(itr->photoDesc.c_str());
				jstring voiceId = env->NewStringUTF(itr->voiceId.c_str());
				jstring voiceType = env->NewStringUTF(itr->voiceType.c_str());

				jobject item = env->NewObject(cls, init,
						itr->toflag,
						itr->adddate,
						itr->messageType,
						textMsg,
						inviteMsg,
						warningMsg,
						emotionId,
						photoId,
						photoSendId,
						photoDesc,
						itr->photoCharge,
						voiceId,
						voiceType,
						itr->voiceTime
						);

				env->SetObjectArrayElement(jItemArray, i, item);

				env->DeleteLocalRef(textMsg);
				env->DeleteLocalRef(inviteMsg);
				env->DeleteLocalRef(warningMsg);
				env->DeleteLocalRef(emotionId);
				env->DeleteLocalRef(photoId);
				env->DeleteLocalRef(photoSendId);
				env->DeleteLocalRef(photoDesc);
				env->DeleteLocalRef(voiceId);
				env->DeleteLocalRef(voiceType);

				env->DeleteLocalRef(item);
			}
		}
	}

	FileLog("httprequest", "LiveChat.Native::GetArrayWithListRecord() end");
	return jItemArray;
}

void onQueryChatRecord(long requestId, bool success, int dbTime, list<Record> recordList, string errnum, string errmsg, string inviteId)
{
	JNIEnv* env = NULL;
	bool isAttachThread = false;
	GetEnv(&env, &isAttachThread);

	FileLog("httprequest", "LiveChat.Native::onQueryChatRecord( success : %s, env:%p, isAttachThread:%d )", success?"true":"false", env, isAttachThread);
	FileLog("httprequest", "LiveChat.Native::onQueryChatRecord() recordList.size:%d", recordList.size());

	/* turn object to java object here */
	jobjectArray jItemArray = GetArrayWithListRecord(env, recordList);

	/* real callback java */
	jobject callbackObj = gCallbackMap.Erase(requestId);

	jclass callbackCls = env->GetObjectClass(callbackObj);

	string signure = "(ZLjava/lang/String;Ljava/lang/String;I";
	signure += "[L";
	signure += LIVECHAT_RECORD_ITEM_CLASS;
	signure += ";";
	signure += "Ljava/lang/String;";
	signure += ")V";
	jmethodID callback = env->GetMethodID(callbackCls, "OnQueryChatRecord", signure.c_str());
	FileLog("httprequest", "LiveChat.Native::onQueryChatRecord( callback : %p, signure : %s )",
			callback, signure.c_str());

	if( callbackObj != NULL && callback != NULL ) {
		jstring jerrno = env->NewStringUTF(errnum.c_str());
		jstring jerrmsg = env->NewStringUTF(errmsg.c_str());
		jstring jinviteId = env->NewStringUTF(inviteId.c_str());

		FileLog("httprequest", "LiveChat.Native::onQueryChatRecord( CallObjectMethod "
				"jItemArray : %p )", jItemArray);

		env->CallVoidMethod(callbackObj, callback, success, jerrno, jerrmsg, dbTime, jItemArray, jinviteId);

		env->DeleteGlobalRef(callbackObj);

		env->DeleteLocalRef(jerrno);
		env->DeleteLocalRef(jerrmsg);
		env->DeleteLocalRef(jinviteId);
	}

	if( jItemArray != NULL ) {
		env->DeleteLocalRef(jItemArray);
	}

	ReleaseEnv(isAttachThread);
}

/*
 * Class:     com_qpidnetwork_request_RequestJniLiveChat
 * Method:    QueryChatRecord
 * Signature: (Ljava/lang/String;Lcom/qpidnetwork/request/OnQueryChatRecordCallback;)J
 */
JNIEXPORT jlong JNICALL Java_com_qpidnetwork_request_RequestJniLiveChat_QueryChatRecord
  (JNIEnv *env, jclass cls, jstring inviteId, jobject callback)
{
	jlong requestId = -1;

	const char *cpInviteId = env->GetStringUTFChars(inviteId, 0);

	requestId = gRequestLiveChatController.QueryChatRecord(cpInviteId);
	jobject obj = env->NewGlobalRef(callback);
	gCallbackMap.Insert(requestId, obj);

	env->ReleaseStringUTFChars(inviteId, cpInviteId);

	return requestId;
}

/******************************************************************************/
void onQueryChatRecordMutiple(long requestId, bool success, int dbTime, list<RecordMutiple> recordMutiList, string errnum, string errmsg)
{
	JNIEnv* env = NULL;
	bool isAttachThread = false;
	GetEnv(&env, &isAttachThread);

	FileLog("httprequest", "LiveChat.Native::onQueryChatRecordMutiple( success : %s, env:%p, isAttachThread:%d )", success?"true":"false", env, isAttachThread);

	/* turn object to java object here */
	jobjectArray jItemArray = NULL;
	JavaItemMap::iterator itr = gJavaItemMap.find(LIVECHAT_RECORD_MUTIPLE_ITEM_CLASS);
	if( itr != gJavaItemMap.end() ) {
		jclass cls = env->GetObjectClass(itr->second);

		if( recordMutiList.size() > 0 ) {
			FileLog("httprequest", "LiveChat.Native::onQueryChatRecordMutiple() recordMutiList.size():%d", recordMutiList.size());
			jItemArray = env->NewObjectArray(recordMutiList.size(), cls, NULL);
			int i = 0;
			for(list<RecordMutiple>::iterator itr = recordMutiList.begin(); itr != recordMutiList.end(); itr++, i++) {
				// recordList
				list<Record> recordList = itr->recordList;
				jobjectArray jRecordList = GetArrayWithListRecord(env, recordList);
				// inviteId
				jstring inviteId = env->NewStringUTF(itr->inviteId.c_str());

				/* RecordMutiple item */
				string signure = "(L";
				signure += "java/lang/String;";
				signure += "[L";
				signure += LIVECHAT_RECORD_ITEM_CLASS;
				signure += ";)V";
				jmethodID init = env->GetMethodID(cls, "<init>", signure.c_str());
				// creaet item
				jobject item = env->NewObject(cls, init,
						inviteId,
						jRecordList
						);

				env->SetObjectArrayElement(jItemArray, i, item);

				// release item
				env->DeleteLocalRef(item);
				// release inviteId
				env->DeleteLocalRef(inviteId);
				// release recordList
				if( jRecordList != NULL ) {
					env->DeleteLocalRef(jRecordList);
				}
			}
		}
	}

		/* real callback java */
	jobject callbackObj = gCallbackMap.Erase(requestId);
	jclass callbackCls = env->GetObjectClass(callbackObj);

	string signure = "(ZLjava/lang/String;Ljava/lang/String;I";
	signure += "[L";
	signure += LIVECHAT_RECORD_MUTIPLE_ITEM_CLASS;
	signure += ";";
	signure += ")V";
	jmethodID callback = env->GetMethodID(callbackCls, "OnQueryChatRecordMutiple", signure.c_str());
	FileLog("httprequest", "LiveChat.Native::onQueryChatRecordMutiple() callback:%p, signure:%s, callbackObj:%p, callbackCls:%p",
			callback, signure.c_str(), callbackObj, callbackCls);

	if( callbackObj != NULL && callback != NULL ) {
		jstring jerrno = env->NewStringUTF(errnum.c_str());
		jstring jerrmsg = env->NewStringUTF(errmsg.c_str());

		FileLog("httprequest", "LiveChat.Native::onQueryChatRecordMutiple( CallObjectMethod "
				"jItemArray : %p )", jItemArray);

		env->CallVoidMethod(callbackObj, callback, success, jerrno, jerrmsg, dbTime, jItemArray);

		env->DeleteGlobalRef(callbackObj);

		env->DeleteLocalRef(jerrno);
		env->DeleteLocalRef(jerrmsg);
	}

	if( jItemArray != NULL ) {
		env->DeleteLocalRef(jItemArray);
	}

	ReleaseEnv(isAttachThread);
}

/*
 * Class:     com_qpidnetwork_request_RequestJniLiveChat
 * Method:    QueryChatRecordMutiple
 * Signature: ([Ljava/lang/String;Lcom/qpidnetwork/request/OnQueryChatRecordMutipleCallback;)J
 */
JNIEXPORT jlong JNICALL Java_com_qpidnetwork_request_RequestJniLiveChat_QueryChatRecordMutiple
  (JNIEnv *env, jclass cls, jobjectArray inviteIds, jobject callback)
{
	jlong requestId = -1;

	list<string> inviteIdList;
	if( inviteIds != NULL ) {
		jsize len = env->GetArrayLength(inviteIds);
		for(int i = 0; i < len; i++) {
			jstring inviteId = (jstring)env->GetObjectArrayElement(inviteIds, i);
			const char *cpInviteId = env->GetStringUTFChars(inviteId, 0);
			inviteIdList.push_back(cpInviteId);
			env->ReleaseStringUTFChars(inviteId, cpInviteId);
		}
	}

	requestId = gRequestLiveChatController.QueryChatRecordMutiple(inviteIdList);
	jobject obj = env->NewGlobalRef(callback);
	gCallbackMap.Insert(requestId, obj);

	return requestId;
}

// -------------------------- SendPhoto -------------------------
/*
 * Class:     com_qpidnetwork_request_RequestJniLiveChat
 * Method:    SendPhoto
 * Signature: (Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Lcom/qpidnetwork/request/OnLCSendPhotoCallback;)J
 */
JNIEXPORT jlong JNICALL Java_com_qpidnetwork_request_RequestJniLiveChat_SendPhoto
  (JNIEnv *env, jclass cls, jstring targetId, jstring inviteId, jstring userId, jstring sid, jstring filePath, jobject callback)
{
	jlong requestId = -1;
	const char *cpTemp = NULL;

	// targetId
	string strTargetId("");
	cpTemp = env->GetStringUTFChars(targetId, 0);
	strTargetId = cpTemp;
	env->ReleaseStringUTFChars(targetId, cpTemp);
	// inviteId
	string strInviteId("");
	cpTemp = env->GetStringUTFChars(inviteId, 0);
	strInviteId = cpTemp;
	env->ReleaseStringUTFChars(inviteId, cpTemp);
	// userId
	string strUserId("");
	cpTemp = env->GetStringUTFChars(userId, 0);
	strUserId = cpTemp;
	env->ReleaseStringUTFChars(userId, cpTemp);
	// sid
	string strSid("");
	cpTemp = env->GetStringUTFChars(sid, 0);
	strSid = cpTemp;
	env->ReleaseStringUTFChars(sid, cpTemp);
	// filePath
	string strFilePath("");
	cpTemp = env->GetStringUTFChars(filePath, 0);
	strFilePath = cpTemp;
	env->ReleaseStringUTFChars(filePath, cpTemp);

	requestId = gRequestLiveChatController.SendPhoto(strTargetId, strInviteId, strUserId, strSid, SMT_FROMPHOTOFILE, "", strFilePath);
	if (requestId != -1) {
		jobject obj = env->NewGlobalRef(callback);
		gCallbackMap.Insert(requestId, obj);
	}
	else {
		FileLog("httprequest", "LiveChat.Native::SendPhoto() fails. "
			"requestId:%lld, targetId:%s, inviteId:%s, userId:%s, sid:%s, filePath:%s"
			, requestId, strTargetId.c_str(), strInviteId.c_str(), strUserId.c_str(), strSid.c_str(), strFilePath.c_str());
	}

	return requestId;
}

void onSendPhoto(long requestId, bool success, const string& errnum, const string& errmsg, const LCSendPhotoItem& item)
{
	JNIEnv* env = NULL;
	bool isAttachThread = false;
	GetEnv(&env, &isAttachThread);

	FileLog("httprequest", "LiveChat.Native::onSendPhoto( success : %s, env:%p, isAttachThread:%d )", success?"true":"false", env, isAttachThread);

	/* create java item */
	jobject jItem = NULL;
	if (success) {
		jclass jItemCls = GetJClass(env, LIVECHAT_SENDPHOTO_TIME_CLASS);
		if(NULL != jItemCls) {
			jmethodID init = env->GetMethodID(jItemCls, "<init>", "("
						"Ljava/lang/String;"	// photoId
						"Ljava/lang/String;"	// sendId
						")V");

			jItem = env->NewObject(jItemCls, init,
						env->NewStringUTF(item.photoId.c_str()),
						env->NewStringUTF(item.sendId.c_str())
						);
		}
	}

	/* real callback java */
	jobject jCallbackObj = gCallbackMap.Erase(requestId);
	jclass jCallbackCls = env->GetObjectClass(jCallbackObj);

	string signure = "(JZLjava/lang/String;Ljava/lang/String;";
	signure += "L";
	signure += LIVECHAT_SENDPHOTO_TIME_CLASS;
	signure += ";";
	signure += ")V";
	jmethodID jCallback = env->GetMethodID(jCallbackCls, "OnLCSendPhoto", signure.c_str());

	if( jCallbackObj != NULL && jCallback != NULL ) {
		jstring jerrno = env->NewStringUTF(errnum.c_str());
		jstring jerrmsg = env->NewStringUTF(errmsg.c_str());
		jlong jlrequestId = requestId;

		env->CallVoidMethod(jCallbackObj, jCallback, jlrequestId, success, jerrno, jerrmsg, jItem);

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

// -------------------------- PhotoFee -------------------------
/*
 * Class:     com_qpidnetwork_request_RequestJniLiveChat
 * Method:    PhotoFee
 * Signature: (Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Lcom/qpidnetwork/request/OnLCPhotoFeeCallback;)J
 */
JNIEXPORT jlong JNICALL Java_com_qpidnetwork_request_RequestJniLiveChat_PhotoFee
  (JNIEnv *env, jclass cls, jstring targetId, jstring inviteId, jstring userId, jstring sid, jstring photoId, jobject callback)
{
	jlong requestId = -1;
	const char *cpTemp = NULL;

	// targetId
	string strTargetId("");
	cpTemp = env->GetStringUTFChars(targetId, 0);
	strTargetId = cpTemp;
	env->ReleaseStringUTFChars(targetId, cpTemp);
	// inviteId
	string strInviteId("");
	cpTemp = env->GetStringUTFChars(inviteId, 0);
	strInviteId = cpTemp;
	env->ReleaseStringUTFChars(inviteId, cpTemp);
	// userId
	string strUserId("");
	cpTemp = env->GetStringUTFChars(userId, 0);
	strUserId = cpTemp;
	env->ReleaseStringUTFChars(userId, cpTemp);
	// sid
	string strSid("");
	cpTemp = env->GetStringUTFChars(sid, 0);
	strSid = cpTemp;
	env->ReleaseStringUTFChars(sid, cpTemp);
	// photoId
	string strPhotoId("");
	cpTemp = env->GetStringUTFChars(photoId, 0);
	strPhotoId = cpTemp;
	env->ReleaseStringUTFChars(photoId, cpTemp);

	requestId = gRequestLiveChatController.PhotoFee(strTargetId, strInviteId, strUserId, strSid, strPhotoId);
	if (requestId != -1) {
		jobject obj = env->NewGlobalRef(callback);
		gCallbackMap.Insert(requestId, obj);
	}
	else {
		FileLog("httprequest", "LiveChat.Native::PhotoFee() fails. "
			"requestId:%lld, targetId:%s, inviteId:%s, userId:%s, sid:%s, photoId:%s"
			, requestId, strTargetId.c_str(), strInviteId.c_str(), strUserId.c_str(), strSid.c_str(), strPhotoId.c_str());
	}

	return requestId;
}

void onPhotoFee(long requestId, bool success, const string& errnum, const string& errmsg)
{
	JNIEnv* env = NULL;
	bool isAttachThread = false;
	GetEnv(&env, &isAttachThread);

	FileLog("httprequest", "LiveChat.Native::onPhotoFee( success : %s, env:%p, isAttachThread:%d )", success?"true":"false", env, isAttachThread);

	/* real callback java */
	jobject jCallbackObj = gCallbackMap.Erase(requestId);
	jclass jCallbackCls = env->GetObjectClass(jCallbackObj);

	string signure = "(JZLjava/lang/String;Ljava/lang/String;)V";
	jmethodID jCallback = env->GetMethodID(jCallbackCls, "OnLCPhotoFee", signure.c_str());

	if( jCallbackObj != NULL && jCallback != NULL ) {
		jstring jerrno = env->NewStringUTF(errnum.c_str());
		jstring jerrmsg = env->NewStringUTF(errmsg.c_str());
		jlong jlrequestId = requestId;

		env->CallVoidMethod(jCallbackObj, jCallback, jlrequestId, success, jerrno, jerrmsg);

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

	ReleaseEnv(isAttachThread);
}

// -------------------------- GetPhoto -------------------------
/*
 * Class:     com_qpidnetwork_request_RequestJniLiveChat
 * Method:    GetPhoto
 * Signature: (ILjava/lang/String;Ljava/lang/String;IILjava/lang/String;Lcom/qpidnetwork/request/OnLCGetPhotoCallback;)J
 */
JNIEXPORT jlong JNICALL Java_com_qpidnetwork_request_RequestJniLiveChat_GetPhoto
  (JNIEnv *env, jclass cls, int toFlag, jstring targetId, jstring userId, jstring sid, jstring photoId, int size, int mode, jstring filePath, jobject callback)
{
	jlong requestId = -1;
	const char *cpTemp = NULL;

	// targetId
	string strTargetId("");
	cpTemp = env->GetStringUTFChars(targetId, 0);
	strTargetId = cpTemp;
	env->ReleaseStringUTFChars(targetId, cpTemp);
	// userId
	string strUserId("");
	cpTemp = env->GetStringUTFChars(userId, 0);
	strUserId = cpTemp;
	env->ReleaseStringUTFChars(userId, cpTemp);
	// sid
	string strSid("");
	cpTemp = env->GetStringUTFChars(sid, 0);
	strSid = cpTemp;
	env->ReleaseStringUTFChars(sid, cpTemp);
	// photoId
	string strPhotoId("");
	cpTemp = env->GetStringUTFChars(photoId, 0);
	strPhotoId = cpTemp;
	env->ReleaseStringUTFChars(photoId, cpTemp);
	// filePath
	string strFilePath("");
	cpTemp = env->GetStringUTFChars(filePath, 0);
	strFilePath = cpTemp;
	env->ReleaseStringUTFChars(filePath, cpTemp);

	requestId = gRequestLiveChatController.GetPhoto(
			(GETPHOTO_TOFLAG_TYPE)toFlag
			, strTargetId
			, strUserId
			, strSid
			, strPhotoId
			, (GETPHOTO_PHOTOSIZE_TYPE)size
			, (GETPHOTO_PHOTOMODE_TYPE)mode
			, strFilePath);
	if (requestId != -1) {
		jobject obj = env->NewGlobalRef(callback);
		gCallbackMap.Insert(requestId, obj);
	}
	else {
		FileLog("httprequest", "LiveChat.Native::GetPhoto() fails. "
			"requestId:%lld, toFlag:%d, targetId:%s, userId:%s, sid:%s, photoId:%s, size:%d, mode:%d, filePath:%s"
			, requestId, toFlag, strTargetId.c_str(), strUserId.c_str(), strSid.c_str(), strPhotoId.c_str(), size, mode, strFilePath.c_str());
	}
	return requestId;
}

void onGetPhoto(long requestId, bool success, const string& errnum, const string& errmsg, const string& filePath)
{
	JNIEnv* env = NULL;
	bool isAttachThread = false;
	GetEnv(&env, &isAttachThread);

	FileLog("httprequest", "LiveChat.Native::onGetPhoto( success : %s, env:%p, isAttachThread:%d )", success?"true":"false", env, isAttachThread);

	/* real callback java */
	jobject jCallbackObj = gCallbackMap.Erase(requestId);
	jclass jCallbackCls = env->GetObjectClass(jCallbackObj);

	string signure = "(JZLjava/lang/String;Ljava/lang/String;Ljava/lang/String;)V";
	jmethodID jCallback = env->GetMethodID(jCallbackCls, "OnLCGetPhoto", signure.c_str());

	if( jCallbackObj != NULL && jCallback != NULL ) {
		jstring jerrno = env->NewStringUTF(errnum.c_str());
		jstring jerrmsg = env->NewStringUTF(errmsg.c_str());
		jstring jfilePath = env->NewStringUTF(filePath.c_str());
		jlong jlrequestId = requestId;

		env->CallVoidMethod(jCallbackObj, jCallback, jlrequestId, success, jerrno, jerrmsg, jfilePath);

		env->DeleteLocalRef(jerrno);
		env->DeleteLocalRef(jerrmsg);
		env->DeleteLocalRef(jfilePath);
	}

	// delete callback object & class
	if (jCallbackCls != NULL) {
		env->DeleteLocalRef(jCallbackCls);
	}
	if (jCallbackObj != NULL) {
		env->DeleteGlobalRef(jCallbackObj);
	}

	ReleaseEnv(isAttachThread);
}


// -------------------------- UploadVoice -------------------------
/*
 * Class:     com_qpidnetwork_request_RequestJniLiveChat
 * Method:    UploadVoice
 * Signature: (Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;ZLjava/lang/String;ILjava/lang/String;ILjava/lang/String;Lcom/qpidnetwork/request/OnLCUploadVoiceCallback;)J
 */
JNIEXPORT jlong JNICALL Java_com_qpidnetwork_request_RequestJniLiveChat_UploadVoice
  (JNIEnv *env, jclass cls, jstring voiceCode, jstring inviteId, jstring mineId, jboolean isMan, jstring userId, jint siteId, jstring fileType, jint voiceLength, jstring filePath, jobject callback)
{
	FileLog("httprequest", "LiveChat.Native::UploadVoice() begin");

	jlong requestId = -1;
	const char *cpTemp = NULL;

	// voiceCode
	string strVoiceCode("");
	cpTemp = env->GetStringUTFChars(voiceCode, 0);
	strVoiceCode = cpTemp;
	env->ReleaseStringUTFChars(voiceCode, cpTemp);
	// inviteId
	string strInviteId("");
	cpTemp = env->GetStringUTFChars(inviteId, 0);
	strInviteId = cpTemp;
	env->ReleaseStringUTFChars(inviteId, cpTemp);
	// mineId
	string strMineId("");
	cpTemp = env->GetStringUTFChars(mineId, 0);
	strMineId = cpTemp;
	env->ReleaseStringUTFChars(mineId, cpTemp);
	// userId
	string strUserId("");
	cpTemp = env->GetStringUTFChars(userId, 0);
	strUserId = cpTemp;
	env->ReleaseStringUTFChars(userId, cpTemp);
	// fileType
	string strFileType("");
	cpTemp = env->GetStringUTFChars(fileType, 0);
	strFileType = cpTemp;
	env->ReleaseStringUTFChars(fileType, cpTemp);
	// filePath
	string strFilePath("");
	cpTemp = env->GetStringUTFChars(filePath, 0);
	strFilePath = cpTemp;
	env->ReleaseStringUTFChars(filePath, cpTemp);

	requestId = gRequestLiveChatController.UploadVoice(
					strVoiceCode
					, strInviteId
					, strMineId
					, isMan
					, strUserId
					, (OTHER_SITE_TYPE)siteId
					, strFileType
					, voiceLength
					, strFilePath);
	if (requestId != -1) {
		jobject obj = env->NewGlobalRef(callback);
		gCallbackMap.Insert(requestId, obj);
	}
	else {
		FileLog("httprequest", "LiveChat.Native::UploadVoice() fails. "
			"requestId:%lld"
			", voiceCode:%s"
			", inviteId:%s"
			", mineId:%s"
			", isMan:%d"
			", userId:%s"
			", siteId:%d"
			", fileType:%s"
			", voiceLength:%d"
			", filePath:%s"
			, requestId
			, strVoiceCode.c_str()
			, strInviteId.c_str()
			, strMineId.c_str()
			, isMan
			, strUserId.c_str()
			, siteId
			, strFileType.c_str()
			, voiceLength
			, strFilePath.c_str());
	}

	FileLog("httprequest", "LiveChat.Native::UploadVoice() requestId:%lld", requestId);

	return requestId;
}

void onUploadVoice(long requestId, bool success, const string& errnum, const string& errmsg, const string& voiceId)
{
	JNIEnv* env = NULL;
	bool isAttachThread = false;
	GetEnv(&env, &isAttachThread);

	FileLog("httprequest", "LiveChat.Native::onUploadVoice( success : %s, env:%p, isAttachThread:%d )", success?"true":"false", env, isAttachThread);

	/* real callback java */
	jobject jCallbackObj = gCallbackMap.Erase(requestId);
	jclass jCallbackCls = env->GetObjectClass(jCallbackObj);

	string signure = "(JZLjava/lang/String;Ljava/lang/String;Ljava/lang/String;)V";
	jmethodID jCallback = env->GetMethodID(jCallbackCls, "OnLCUploadVoice", signure.c_str());

	if( jCallbackObj != NULL && jCallback != NULL ) {
		jstring jerrno = env->NewStringUTF(errnum.c_str());
		jstring jerrmsg = env->NewStringUTF(errmsg.c_str());
		jstring jvoiceId = env->NewStringUTF(voiceId.c_str());
		jlong jlrequestId = requestId;

		env->CallVoidMethod(jCallbackObj, jCallback, jlrequestId, success, jerrno, jerrmsg, jvoiceId);

		env->DeleteLocalRef(jerrno);
		env->DeleteLocalRef(jerrmsg);
		env->DeleteLocalRef(jvoiceId);
	}

	// delete callback object & class
	if (jCallbackCls != NULL) {
		env->DeleteLocalRef(jCallbackCls);
	}
	if (jCallbackObj != NULL) {
		env->DeleteGlobalRef(jCallbackObj);
	}

	ReleaseEnv(isAttachThread);
}

// -------------------------- PlayVoice -------------------------
/*
 * Class:     com_qpidnetwork_request_RequestJniLiveChat
 * Method:    PlayVoice
 * Signature: (Ljava/lang/String;ILjava/lang/String;Lcom/qpidnetwork/request/OnLCPlayVoiceCallback;)J
 */
JNIEXPORT jlong JNICALL Java_com_qpidnetwork_request_RequestJniLiveChat_PlayVoice
  (JNIEnv *env, jclass cls, jstring voiceId, jint siteType, jstring filePath, jobject callback)
{
	jlong requestId = -1;
	const char *cpTemp = NULL;

	// voiceId
	string strVoiceId("");
	cpTemp = env->GetStringUTFChars(voiceId, 0);
	strVoiceId = cpTemp;
	env->ReleaseStringUTFChars(voiceId, cpTemp);
	// filePath
	string strFilePath("");
	cpTemp = env->GetStringUTFChars(filePath, 0);
	strFilePath = cpTemp;
	env->ReleaseStringUTFChars(filePath, cpTemp);

	requestId = gRequestLiveChatController.PlayVoice(strVoiceId, (OTHER_SITE_TYPE)siteType, strFilePath);
	if (requestId != -1) {
		jobject obj = env->NewGlobalRef(callback);
		gCallbackMap.Insert(requestId, obj);
	}
	else {
		FileLog("httprequest", "LiveChat.Native::PlayVoice() fails. "
			"requestId:%lld, voiceId:%s, siteType:%d, filePath:%s"
			, requestId, strVoiceId.c_str(), siteType, strFilePath.c_str());
	}
	return requestId;
}

void onPlayVoice(long requestId, bool success, const string& errnum, const string& errmsg, const string& filePath)
{
	JNIEnv* env = NULL;
	bool isAttachThread = false;
	GetEnv(&env, &isAttachThread);

	FileLog("httprequest", "LiveChat.Native::onPlayVoice( success : %s, env:%p, isAttachThread:%d )", success?"true":"false", env, isAttachThread);

	/* real callback java */
	jobject jCallbackObj = gCallbackMap.Erase(requestId);
	jclass jCallbackCls = env->GetObjectClass(jCallbackObj);

	string signure = "(JZLjava/lang/String;Ljava/lang/String;Ljava/lang/String;)V";
	jmethodID jCallback = env->GetMethodID(jCallbackCls, "OnLCPlayVoice", signure.c_str());

	FileLog("httprequest", "LiveChat.Native::onPlayVoice(), errnum:%s, errmsg:%s, filePath:%s", errnum.c_str(), errmsg.c_str(), filePath.c_str());
	if( jCallbackObj != NULL && jCallback != NULL ) {
		jstring jerrno = env->NewStringUTF(errnum.c_str());
		jstring jerrmsg = env->NewStringUTF(errmsg.c_str());
		jstring jfilePath = env->NewStringUTF(filePath.c_str());
		jlong jlrequestId = requestId;

		FileLog("httprequest", "LiveChat.Native::onPlayVoice() jCallbackObj:%p, jCallback:%p, requestId:%ld, jerrno:%p, jerrmsg:%p, jfilePath:%p", jCallbackObj, jCallback, requestId, jerrno, jerrmsg, jfilePath);
		env->CallVoidMethod(jCallbackObj, jCallback, jlrequestId, success, jerrno, jerrmsg, jfilePath);

		env->DeleteLocalRef(jerrno);
		env->DeleteLocalRef(jerrmsg);
		env->DeleteLocalRef(jfilePath);
	}

	// delete callback object & class
	if (jCallbackCls != NULL) {
		env->DeleteLocalRef(jCallbackCls);
	}
	if (jCallbackObj != NULL) {
		env->DeleteGlobalRef(jCallbackObj);
	}

	ReleaseEnv(isAttachThread);
}

/******************************************************************************/
void onSendGift(long requestId, bool success, const string& errnum, const string& errmsg)
{
	JNIEnv* env = NULL;
	bool isAttachThread = false;
	GetEnv(&env, &isAttachThread);

	FileLog("httprequest", "LiveChat.Native::onSendGift( success : %s, env:%p, isAttachThread:%d )", success?"true":"false", env, isAttachThread);

	/* real callback java */
	jobject callbackObj = gCallbackMap.Erase(requestId);
	jclass callbackCls = env->GetObjectClass(callbackObj);

	string signure = "(ZLjava/lang/String;Ljava/lang/String;)V";
	jmethodID callback = env->GetMethodID(callbackCls, "OnRequest", signure.c_str());
	FileLog("httprequest", "LiveChat.Native::onSendGift( callbackCls : %p, callback : %p, signure : %s )",
			callbackCls, callback, signure.c_str());

	if( callbackObj != NULL && callback != NULL ) {
		jstring jerrno = env->NewStringUTF(errnum.c_str());
		jstring jerrmsg = env->NewStringUTF(errmsg.c_str());

		FileLog("httprequest", "LiveChat.Native::onSendGift( CallObjectMethod )");

		env->CallVoidMethod(callbackObj, callback, success, jerrno, jerrmsg);

		env->DeleteGlobalRef(callbackObj);

		env->DeleteLocalRef(jerrno);
		env->DeleteLocalRef(jerrmsg);
	}

	ReleaseEnv(isAttachThread);
}

/*
 * Class:     com_qpidnetwork_request_RequestJniLiveChat
 * Method:    SendGift
 * Signature: (Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;ILjava/lang/String;Ljava/lang/String;Lcom/qpidnetwork/request/OnRequestCallback;)J
 */
JNIEXPORT jlong JNICALL Java_com_qpidnetwork_request_RequestJniLiveChat_SendGift
  (JNIEnv *env, jclass cls, jstring womanId, jstring vg_id, jstring device_id, jstring chat_id, jint use_type, jstring user_sid, jstring user_id, jobject callback) {
	jlong requestId = -1;

	const char *cpWomanId = env->GetStringUTFChars(womanId, 0);
	const char *cpVg_id = env->GetStringUTFChars(vg_id, 0);
	const char *cpDevice_id = env->GetStringUTFChars(device_id, 0);
	const char *cpChat_id = env->GetStringUTFChars(chat_id, 0);
	const char *cpUser_sid = env->GetStringUTFChars(user_sid, 0);
	const char *cpUser_id = env->GetStringUTFChars(user_id, 0);

	requestId = gRequestLiveChatController.SendGift(
			cpWomanId,
			cpVg_id,
			cpDevice_id,
			cpChat_id,
			use_type,
			cpUser_sid,
			cpUser_id
			);

	jobject obj = env->NewGlobalRef(callback);
	gCallbackMap.Insert(requestId, obj);

	env->ReleaseStringUTFChars(womanId, cpWomanId);
	env->ReleaseStringUTFChars(vg_id, cpVg_id);
	env->ReleaseStringUTFChars(device_id, cpDevice_id);
	env->ReleaseStringUTFChars(chat_id, cpChat_id);
	env->ReleaseStringUTFChars(user_sid, cpUser_sid);
	env->ReleaseStringUTFChars(user_id, cpUser_id);

	return requestId;
}

/**************************** QueryRecentVideo **************************/
void onQueryRecentVideoList(long requestId, bool success, list<LCVideoItem> itemList, string errnum, string errmsg)
{
	JNIEnv* env = NULL;
	bool isAttachThread = false;
	GetEnv(&env, &isAttachThread);

	FileLog("httprequest", "LiveChat.Native::onQueryRecentVideoList( success : %s, env:%p, isAttachThread:%d )", success?"true":"false", env, isAttachThread);

	/* turn object to java object here */
	jobjectArray jItemArray = NULL;
	JavaItemMap::iterator itr = gJavaItemMap.find(LIVECHAT_LCVIDEO_TIME_CLASS);
	if( itr != gJavaItemMap.end() ) {
		jclass cls = env->GetObjectClass(itr->second);
		jmethodID init = env->GetMethodID(cls, "<init>", "("
				"Ljava/lang/String;"
				"Ljava/lang/String;"
				"Ljava/lang/String;"
				")V");

		if( itemList.size() > 0 ) {
			int i = 0;
			jItemArray = env->NewObjectArray(itemList.size(), cls, NULL);
			for(list<LCVideoItem>::iterator itr = itemList.begin(); itr != itemList.end(); itr++, i++) {
				jstring videoid = env->NewStringUTF(itr->videoid.c_str());
				jstring title = env->NewStringUTF(itr->title.c_str());
				jstring inviteid = env->NewStringUTF(itr->inviteid.c_str());
				jstring video_url = env->NewStringUTF(itr->video_url.c_str());


				jobject item = env->NewObject(cls, init,
						videoid,
						title,
						inviteid,
						video_url
						);

				env->SetObjectArrayElement(jItemArray, i, item);

				env->DeleteLocalRef(videoid);
				env->DeleteLocalRef(title);
				env->DeleteLocalRef(inviteid);
				env->DeleteLocalRef(video_url);

				env->DeleteLocalRef(item);
			}
		}
	}

	/* real callback java */
	jobject callbackObj = gCallbackMap.Erase(requestId);

	jclass callbackCls = env->GetObjectClass(callbackObj);

	string signure = "(ZLjava/lang/String;Ljava/lang/String;";
	signure += "[L";
	signure += LIVECHAT_LCVIDEO_TIME_CLASS;
	signure += ";";
	signure += ")V";
	jmethodID callback = env->GetMethodID(callbackCls, "OnQueryRecentVideoList", signure.c_str());
	FileLog("httprequest", "LiveChat.Native::onQueryRecentVideoList( callback : %p, signure : %s )",
			callback, signure.c_str());

	if( callbackObj != NULL && callback != NULL ) {
		jstring jerrno = env->NewStringUTF(errnum.c_str());
		jstring jerrmsg = env->NewStringUTF(errmsg.c_str());

		FileLog("httprequest", "LiveChat.Native::onQueryRecentVideoList( CallObjectMethod "
				"jItemArray : %p )", jItemArray);

		env->CallVoidMethod(callbackObj, callback, success, jerrno, jerrmsg, jItemArray);

		env->DeleteGlobalRef(callbackObj);

		env->DeleteLocalRef(jerrno);
		env->DeleteLocalRef(jerrmsg);
	}

	if( jItemArray != NULL ) {
		env->DeleteLocalRef(jItemArray);
	}

	ReleaseEnv(isAttachThread);
}
/*
 * Class:     com_qpidnetwork_request_RequestJniLiveChat
 * Method:    QueryRecentVideo
 * Signature: (Ljava/lang/String;Lcom/qpidnetwork/request/OnQueryRecentVideoListCallback;)J
 */
JNIEXPORT jlong JNICALL Java_com_qpidnetwork_request_RequestJniLiveChat_QueryRecentVideo
  (JNIEnv *env, jclass, jstring womanId, jobject callback) {
	jlong requestId = -1;

	requestId = gRequestLiveChatController.QueryRecentVideo(JString2String(env, womanId));

	jobject obj = env->NewGlobalRef(callback);
	gCallbackMap.Insert(requestId, obj);

	return requestId;
}

/**************************** GetVideoPhoto **************************/
/*
 * Class:     com_qpidnetwork_request_RequestJniLiveChat
 * Method:    GetVideoPhoto
 * Signature: (Ljava/lang/String;Ljava/lang/String;ILjava/lang/String;Lcom/qpidnetwork/request/OnRequestFileCallback;)J
 */
JNIEXPORT jlong JNICALL Java_com_qpidnetwork_request_RequestJniLiveChat_GetVideoPhoto
  (JNIEnv *env, jclass, jstring womanId, jstring videoid, jint size, jstring filePath, jobject callback) {
	jlong requestId = -1;

	requestId = gRequestLiveChatController.GetVideoPhoto(
			JString2String(env, womanId),
			JString2String(env, videoid),
			size,
			JString2String(env, filePath)
			);

	jobject obj = env->NewGlobalRef(callback);
	gCallbackMap.Insert(requestId, obj);

	return requestId;
}

void onGetVideoPhoto(long requestId, bool success, const string& errnum, const string& errmsg, const string& filePath)
{
	JNIEnv* env = NULL;
	bool isAttachThread = false;
	GetEnv(&env, &isAttachThread);

	FileLog("httprequest", "LiveChat.Native::onGetVideoPhoto( success : %s, env:%p, isAttachThread:%d )", success?"true":"false", env, isAttachThread);

	/* real callback java */
	jobject jCallbackObj = gCallbackMap.Erase(requestId);
	jclass jCallbackCls = env->GetObjectClass(jCallbackObj);

	string signure = "(JZLjava/lang/String;Ljava/lang/String;Ljava/lang/String;)V";
	jmethodID jCallback = env->GetMethodID(jCallbackCls, "OnRequestFile", signure.c_str());

	FileLog("httprequest", "LiveChat.Native::onGetVideoPhoto(), errnum:%s, errmsg:%s, filePath:%s", errnum.c_str(), errmsg.c_str(), filePath.c_str());
	if( jCallbackObj != NULL && jCallback != NULL ) {
		jstring jerrno = env->NewStringUTF(errnum.c_str());
		jstring jerrmsg = env->NewStringUTF(errmsg.c_str());
		jstring jfilePath = env->NewStringUTF(filePath.c_str());
		jlong jlrequestId = requestId;

		FileLog("httprequest", "LiveChat.Native::onGetVideoPhoto() jCallbackObj:%p, jCallback:%p, requestId:%ld, jerrno:%p, jerrmsg:%p, jfilePath:%p", jCallbackObj, jCallback, requestId, jerrno, jerrmsg, jfilePath);
		env->CallVoidMethod(jCallbackObj, jCallback, jlrequestId, success, jerrno, jerrmsg, jfilePath);

		env->DeleteLocalRef(jerrno);
		env->DeleteLocalRef(jerrmsg);
		env->DeleteLocalRef(jfilePath);
	}

	// delete callback object & class
	if (jCallbackCls != NULL) {
		env->DeleteLocalRef(jCallbackCls);
	}
	if (jCallbackObj != NULL) {
		env->DeleteGlobalRef(jCallbackObj);
	}

	ReleaseEnv(isAttachThread);
}
/**************************** GetVideo **************************/
/*
 * Class:     com_qpidnetwork_request_RequestJniLiveChat
 * Method:    GetVideo
 * Signature: (Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;ILjava/lang/String;Lcom/qpidnetwork/request/OnGetVideoCallback;)J
 */
JNIEXPORT jlong JNICALL Java_com_qpidnetwork_request_RequestJniLiveChat_GetVideo
  (JNIEnv *env, jclass, jstring womanId, jstring videoid, jstring inviteid, jint toflag, jstring sendid, jobject callback) {
	jlong requestId = -1;

	requestId = gRequestLiveChatController.GetVideo(
			JString2String(env, womanId),
			JString2String(env, videoid),
			JString2String(env, inviteid),
			toflag,
			JString2String(env, sendid)
			);

	jobject obj = env->NewGlobalRef(callback);
	gCallbackMap.Insert(requestId, obj);

	return requestId;
}
void onGetVideo(long requestId, bool success, const string& errnum, const string& errmsg, const string& url)
{
	JNIEnv* env = NULL;
	bool isAttachThread = false;
	GetEnv(&env, &isAttachThread);

	FileLog("httprequest", "LiveChat.Native::onGetVideo( success : %s, env:%p, isAttachThread:%d )", success?"true":"false", env, isAttachThread);

	/* real callback java */
	jobject jCallbackObj = gCallbackMap.Erase(requestId);
	jclass jCallbackCls = env->GetObjectClass(jCallbackObj);

	string signure = "(JZLjava/lang/String;Ljava/lang/String;Ljava/lang/String;)V";
	jmethodID jCallback = env->GetMethodID(jCallbackCls, "OnGetVideo", signure.c_str());

	FileLog("httprequest", "LiveChat.Native::onGetVideo(), errnum:%s, errmsg:%s, filePath:%s", errnum.c_str(), errmsg.c_str(), url.c_str());
	if( jCallbackObj != NULL && jCallback != NULL ) {
		jstring jerrno = env->NewStringUTF(errnum.c_str());
		jstring jerrmsg = env->NewStringUTF(errmsg.c_str());
		jstring jUrl = env->NewStringUTF(url.c_str());
		jlong jlrequestId = requestId;

		FileLog("httprequest", "LiveChat.Native::onGetVideo() jCallbackObj:%p, jCallback:%p, requestId:%ld, jerrno:%p, jerrmsg:%p, jUrl:%p", jCallbackObj, jCallback, requestId, jerrno, jerrmsg, jUrl);
		env->CallVoidMethod(jCallbackObj, jCallback, jlrequestId, success, jerrno, jerrmsg, jUrl);

		env->DeleteLocalRef(jerrno);
		env->DeleteLocalRef(jerrmsg);
		env->DeleteLocalRef(jUrl);
	}

	// delete callback object & class
	if (jCallbackCls != NULL) {
		env->DeleteLocalRef(jCallbackCls);
	}
	if (jCallbackObj != NULL) {
		env->DeleteGlobalRef(jCallbackObj);
	}

	ReleaseEnv(isAttachThread);
}
