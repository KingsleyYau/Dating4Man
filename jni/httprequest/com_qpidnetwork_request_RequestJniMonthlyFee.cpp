/*
 * com_qpidnetwork_request_RequestJniMonthlyFee.cpp
 *
 *  Created on: 2016-5-11
 *      Author: Hunter
 */
#include "com_qpidnetwork_request_RequestJniMonthlyFee.h"
#include "com_qpidnetwork_request_RequestJni_GobalFunc.h"
#include <manrequesthandler/RequestMonthlyFeeController.h>

void onQueryMemberType(long requestId, bool success, string errnum, string errmsg, int memberType);
void onGetMonthlyFeeTips(long requestId, bool success, string errnum, string errmsg, list<MonthlyFeeTip> tipsList);

RequestMonthlyFeeControllerCallback gRequestMonthlyFeeControllerCallback{
	onQueryMemberType,
	onGetMonthlyFeeTips
};
RequestMonthlyFeeController gRequestMonthlyFeeController(&gHttpRequestManager, gRequestMonthlyFeeControllerCallback);


/******************************************************************************/
void onQueryMemberType(long requestId, bool success, string errnum, string errmsg, int memberType) {
	FileLog("httprequest", "MonthlyFee.Native::onQueryMemberType( success : %s )", success?"true":"false");

	/* turn object to java object here */
	JNIEnv* env = NULL;
	bool isAttachThread = false;
	GetEnv(&env, &isAttachThread);

	/* real callback java */
	jobject callbackObj = gCallbackMap.Erase(requestId);
	jclass callbackCls = env->GetObjectClass(callbackObj);

	string signure = "(ZLjava/lang/String;Ljava/lang/String;I)V";
	jmethodID callback = env->GetMethodID(callbackCls, "OnQueryMemberType", signure.c_str());
	FileLog("httprequest", "MonthlyFee.Native::onQueryMemberType( callback : %p, signure : %s )",
				callback, signure.c_str());
	if( callbackObj != NULL && callback != NULL ) {
		jstring jerrno = env->NewStringUTF(errnum.c_str());
		jstring jerrmsg = env->NewStringUTF(errmsg.c_str());

		env->CallVoidMethod(callbackObj, callback, success, jerrno, jerrmsg, memberType);

		env->DeleteLocalRef(jerrno);
		env->DeleteLocalRef(jerrmsg);
	}

	if(NULL != callbackCls){
		env->DeleteLocalRef(callbackCls);
	}

	if(NULL != callbackObj){
		env->DeleteGlobalRef(callbackObj);
	}

	ReleaseEnv(isAttachThread);
}

/*
 * Class:     com_qpidnetwork_request_RequestJniMonthlyFee
 * Method:    QueryMemberType
 * Signature: (Lcom/qpidnetwork/request/OnQueryMemberTypeCallback;)J
 */
JNIEXPORT jlong JNICALL Java_com_qpidnetwork_request_RequestJniMonthlyFee_QueryMemberType
  (JNIEnv *env, jclass cls, jobject callback){
	jlong requestId = -1;
	requestId = gRequestMonthlyFeeController.QueryMemberType();
	jobject obj = env->NewGlobalRef(callback);
	gCallbackMap.Insert(requestId, obj);

	return requestId;
}

/******************************************************************************/
void onGetMonthlyFeeTips(long requestId, bool success, string errnum, string errmsg, list<MonthlyFeeTip> tipsList) {
	FileLog("httprequest", "MonthlyFee.Native::onGetMonthlyFeeTips( success : %s )", success?"true":"false");

	/* turn object to java object here */
	JNIEnv* env = NULL;
	bool isAttachThread = false;
	GetEnv(&env, &isAttachThread);

	/* create the data array */
	jobjectArray jItemArray = NULL;
	JavaItemMap::iterator itr = gJavaItemMap.find(MONTHLY_FEE_TIP_CLASS);
	if( itr != gJavaItemMap.end() ) {
		jclass cls = env->GetObjectClass(itr->second);
		jmethodID init = env->GetMethodID(cls, "<init>", "("
						"I"
						"Ljava/lang/String;"
						"[Ljava/lang/String;"
						")V");
		if( tipsList.size() > 0 ) {
			jItemArray = env->NewObjectArray(tipsList.size(), cls, NULL);
			int i = 0;
			for(list<MonthlyFeeTip>::iterator itr = tipsList.begin(); itr != tipsList.end(); itr++, i++) {
				jstring priceTitle = env->NewStringUTF(itr->priceTilte.c_str());

				jclass jStringCls = env->FindClass("java/lang/String");
				jobjectArray jTipsArray = env->NewObjectArray(itr->tipList.size(), jStringCls, NULL);
				if (NULL != jTipsArray) {
					int j = 0;
					for (list<string>::const_iterator stringItr = itr->tipList.begin()
						; stringItr != itr->tipList.end()
						; stringItr++, j++)
					{
						jstring tip = env->NewStringUTF((*stringItr).c_str());
						env->SetObjectArrayElement(jTipsArray, j, tip);
						env->DeleteLocalRef(tip);
					}
				}

				jobject item = env->NewObject(cls, init,
						itr->memberType,
						priceTitle,
						jTipsArray
						);

				env->SetObjectArrayElement(jItemArray, i, item);

				env->DeleteLocalRef(jTipsArray);
				env->DeleteLocalRef(priceTitle);
				env->DeleteLocalRef(item);
			}
		}
	}

	/* real callback java */
	jobject callbackObj = gCallbackMap.Erase(requestId);
	jclass callbackCls = env->GetObjectClass(callbackObj);

	string signure = "(ZLjava/lang/String;Ljava/lang/String;"
			"[L"
			MONTHLY_FEE_TIP_CLASS
			";"
			")V";
	jmethodID callback = env->GetMethodID(callbackCls, "OnGetMonthlyFeeTips", signure.c_str());
	FileLog("httprequest", "MonthlyFee.Native::onGetMonthlyFeeTips( callback : %p, signure : %s )",
				callback, signure.c_str());
	if( callbackObj != NULL && callback != NULL ) {
		jstring jerrno = env->NewStringUTF(errnum.c_str());
		jstring jerrmsg = env->NewStringUTF(errmsg.c_str());

		FileLog("httprequest", "MonthlyFee.Native::onGetMonthlyFeeTips( CallObjectMethod "
				"jItemArray : %p )", jItemArray);

		env->CallVoidMethod(callbackObj, callback, success, jerrno, jerrmsg, jItemArray);

		env->DeleteLocalRef(jerrno);
		env->DeleteLocalRef(jerrmsg);
	}

	if(NULL != jItemArray){
		env->DeleteLocalRef(jItemArray);
	}

	if(NULL != callbackCls){
		env->DeleteLocalRef(callbackCls);
	}

	if(NULL != callbackObj){
		env->DeleteGlobalRef(callbackObj);
	}

	ReleaseEnv(isAttachThread);
}

/*
 * Class:     com_qpidnetwork_request_RequestJniMonthlyFee
 * Method:    GetMonthlyFeeTips
 * Signature: (Lcom/qpidnetwork/request/OnGetMonthlyFeeTipsCallback;)J
 */
JNIEXPORT jlong JNICALL Java_com_qpidnetwork_request_RequestJniMonthlyFee_GetMonthlyFeeTips
  (JNIEnv *env, jclass cls, jobject callback){
	jlong requestId = -1;

	requestId = gRequestMonthlyFeeController.GetMonthlyFeeTips();
	jobject obj = env->NewGlobalRef(callback);
	gCallbackMap.Insert(requestId, obj);

	return requestId;
}
