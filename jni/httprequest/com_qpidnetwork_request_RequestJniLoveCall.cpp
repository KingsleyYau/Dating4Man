/*
 * com_qpidnetwork_request_RequestJniLoveCall.cpp
 *
 *  Created on: 2015-2-27
 *      Author: Max.Chiu
 *      Email: Kingsleyyau@gmail.com
 */
#include "com_qpidnetwork_request_RequestJniLoveCall.h"
#include "com_qpidnetwork_request_RequestJni_GobalFunc.h"
#include "RequestLoveCallController.h"

void onQueryLoveCallList(long requestId, bool success, list<LoveCall> itemList, int totalCount, string errnum, string errmsg);
void onConfirmLoveCall(long requestId, bool success, string errnum, string errmsg);
void onQueryLoveCallRequestCount(long requestId, bool success, string errnum, string errmsg, int num);

RequestLoveCallControllerCallback gRequestLoveCallControllerCallback {
	onQueryLoveCallList,
	onConfirmLoveCall,
	onQueryLoveCallRequestCount
};
RequestLoveCallController gRequestLoveCallController(&gHttpRequestManager, gRequestLoveCallControllerCallback);


/******************************************************************************/
void onQueryLoveCallList(long requestId, bool success, list<LoveCall> itemList, int totalCount, string errnum, string errmsg) {
	FileLog("httprequest", "LoveCall.Native::onQueryLoveCallList( success : %s )", success?"true":"false");

	/* turn object to java object here */
	JNIEnv* env = NULL;
	bool isAttachThread = false;
	GetEnv(&env, &isAttachThread);

	jobjectArray jItemArray = NULL;
	JavaItemMap::iterator itr = gJavaItemMap.find(LOVECALL_ITEM_CLASS);
	if( itr != gJavaItemMap.end() ) {
		jclass cls = env->GetObjectClass(itr->second);
		jmethodID init = env->GetMethodID(cls, "<init>", "("
				"Ljava/lang/String;"
				"Ljava/lang/String;"
				"Ljava/lang/String;"
				"Ljava/lang/String;"
				"Ljava/lang/String;"

				"I"
				"I"
				"I"

				"Z"
				"Z"

				"Ljava/lang/String;"
				"Ljava/lang/String;"
				"Ljava/lang/String;"
				")V");

		if( itemList.size() > 0 ) {
			jItemArray = env->NewObjectArray(itemList.size(), cls, NULL);
			int i = 0;
			for(list<LoveCall>::iterator itr = itemList.begin(); itr != itemList.end(); itr++, i++) {
				jstring orderid = env->NewStringUTF(itr->orderid.c_str());
				jstring womanid = env->NewStringUTF(itr->womanid.c_str());
				jstring image = env->NewStringUTF(itr->image.c_str());
				jstring firstname = env->NewStringUTF(itr->firstname.c_str());
				jstring country = env->NewStringUTF(itr->country.c_str());

				jstring confirmmsg = env->NewStringUTF(itr->confirmmsg.c_str());
				jstring callid = env->NewStringUTF(itr->callid.c_str());
				jstring centerid = env->NewStringUTF(itr->centerid.c_str());

				jobject item = env->NewObject(cls, init,
						orderid,
						womanid,
						image,
						firstname,
						country,

						itr->age,
						itr->begintime,
						itr->endtime,

						itr->needtr,
						itr->isconfirm,

						confirmmsg,
						callid,
						centerid
						);

				env->SetObjectArrayElement(jItemArray, i, item);

				env->DeleteLocalRef(orderid);
				env->DeleteLocalRef(womanid);
				env->DeleteLocalRef(image);
				env->DeleteLocalRef(firstname);
				env->DeleteLocalRef(country);
				env->DeleteLocalRef(confirmmsg);
				env->DeleteLocalRef(callid);
				env->DeleteLocalRef(centerid);

				env->DeleteLocalRef(item);
			}
		}
	}

	/* real callback java */
	jobject callbackObj = gCallbackMap.Erase(requestId);

	jclass callbackCls = env->GetObjectClass(callbackObj);

	string signure = "(ZLjava/lang/String;Ljava/lang/String;";
	signure += "[L";
	signure += LOVECALL_ITEM_CLASS;
	signure += ";";
	signure += "I";
	signure += ")V";
	jmethodID callback = env->GetMethodID(callbackCls, "OnQueryLoveCallList", signure.c_str());
	FileLog("httprequest", "LoveCall.Native::onQueryLoveCallList( callback : %p, signure : %s )",
			callback, signure.c_str());

	if( callbackObj != NULL && callback != NULL ) {
		jstring jerrno = env->NewStringUTF(errnum.c_str());
		jstring jerrmsg = env->NewStringUTF(errmsg.c_str());

		FileLog("httprequest", "LoveCall.Native::onQueryLoveCallList( CallObjectMethod "
				"jItemArray : %p )", jItemArray);

		env->CallVoidMethod(callbackObj, callback, success, jerrno, jerrmsg, jItemArray, totalCount);

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
 * Class:     com_qpidnetwork_request_RequestJniLoveCall
 * Method:    QueryLoveCallList
 * Signature: (IIILcom/qpidnetwork/request/OnQueryLoveCallListCallback;)J
 */
JNIEXPORT jlong JNICALL Java_com_qpidnetwork_request_RequestJniLoveCall_QueryLoveCallList
  (JNIEnv *env, jclass cls, jint pageIndex, jint pageSize, jint searchType, jobject callback) {
	jlong requestId = -1;

	requestId = gRequestLoveCallController.QueryLoveCallList(pageIndex, pageSize, searchType);

	jobject obj = env->NewGlobalRef(callback);
	gCallbackMap.Insert(requestId, obj);

	return requestId;
}

/******************************************************************************/
void onConfirmLoveCall(long requestId, bool success, string errnum, string errmsg) {
	FileLog("httprequest", "LoveCall.Native::onConfirmLoveCall( success : %s )", success?"true":"false");

	/* turn object to java object here */
	JNIEnv* env = NULL;
	bool isAttachThread = false;
	GetEnv(&env, &isAttachThread);

	/* real callback java */
	jobject callbackObj = gCallbackMap.Erase(requestId);
	jclass callbackCls = env->GetObjectClass(callbackObj);

	string signure = "(ZLjava/lang/String;Ljava/lang/String;)V";
	jmethodID callback = env->GetMethodID(callbackCls, "OnRequest", signure.c_str());
	FileLog("httprequest", "LoveCall.Native::onConfirmLoveCall( callbackCls : %p, callback : %p, signure : %s )",
			callbackCls, callback, signure.c_str());

	if( callbackObj != NULL && callback != NULL ) {
		jstring jerrno = env->NewStringUTF(errnum.c_str());
		jstring jerrmsg = env->NewStringUTF(errmsg.c_str());

		FileLog("httprequest", "LoveCall.Native::onConfirmLoveCall( CallObjectMethod )");

		env->CallVoidMethod(callbackObj, callback, success, jerrno, jerrmsg);

		env->DeleteGlobalRef(callbackObj);

		env->DeleteLocalRef(jerrno);
		env->DeleteLocalRef(jerrmsg);
	}

	ReleaseEnv(isAttachThread);
}
/*
 * Class:     com_qpidnetwork_request_RequestJniLoveCall
 * Method:    ConfirmLoveCall
 * Signature: (Ljava/lang/String;ILcom/qpidnetwork/request/OnRequestCallback;)J
 */
JNIEXPORT jlong JNICALL Java_com_qpidnetwork_request_RequestJniLoveCall_ConfirmLoveCall
  (JNIEnv *env, jclass cls, jstring orderId, jint confirmType, jobject callback) {
	jlong requestId = -1;

	const char *cpOrderId = env->GetStringUTFChars(orderId, 0);

	requestId = gRequestLoveCallController.ConfirmLoveCall(cpOrderId, confirmType);

	jobject obj = env->NewGlobalRef(callback);
	gCallbackMap.Insert(requestId, obj);

	env->ReleaseStringUTFChars(orderId, cpOrderId);

	return requestId;
}

/******************************************************************************/
void onQueryLoveCallRequestCount(long requestId, bool success, string errnum, string errmsg, int num)
{
	FileLog("httprequest", "LoveCall.Native::onQueryLoveCallRequestCount( success : %s )", success?"true":"false");

	/* turn object to java object here */
	JNIEnv* env = NULL;
	bool isAttachThread = false;
	GetEnv(&env, &isAttachThread);

	/* real callback java */
	jobject callbackObj = gCallbackMap.Erase(requestId);
	jclass callbackCls = env->GetObjectClass(callbackObj);

	string signure = "(ZLjava/lang/String;Ljava/lang/String;I)V";
	jmethodID callback = env->GetMethodID(callbackCls, "OnQueryLoveCallRequestCount", signure.c_str());
	FileLog("httprequest", "LoveCall.Native::onQueryLoveCallRequestCount( callbackCls : %p, callback : %p, signure : %s )",
			callbackCls, callback, signure.c_str());

	if( callbackObj != NULL && callback != NULL ) {
		jstring jerrno = env->NewStringUTF(errnum.c_str());
		jstring jerrmsg = env->NewStringUTF(errmsg.c_str());

		FileLog("httprequest", "LoveCall.Native::onQueryLoveCallRequestCount( CallObjectMethod )");

		env->CallVoidMethod(callbackObj, callback, success, jerrno, jerrmsg, num);

		env->DeleteGlobalRef(callbackObj);

		env->DeleteLocalRef(jerrno);
		env->DeleteLocalRef(jerrmsg);
	}

	ReleaseEnv(isAttachThread);
}

/*
 * Class:     com_qpidnetwork_request_RequestJniLoveCall
 * Method:    QueryLoveCallRequestCount
 * Signature: (ILcom/qpidnetwork/request/OnQueryLoveCallRequestCountCallback;)J
 */
JNIEXPORT jlong JNICALL Java_com_qpidnetwork_request_RequestJniLoveCall_QueryLoveCallRequestCount
  (JNIEnv *env, jclass cls, jint searchType, jobject callback)
{
	jlong requestId = -1;

	requestId = gRequestLoveCallController.QueryLoveCallRequestCount(searchType);

	jobject obj = env->NewGlobalRef(callback);
	gCallbackMap.Insert(requestId, obj);

	return requestId;
}
