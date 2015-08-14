/*
 * com_qpidnetwork_request_RequestJniQuickMatch.cpp
 *
 *  Created on: 2015-2-27
 *      Author: Max.Chiu
 *      Email: Kingsleyyau@gmail.com
 */
#include "com_qpidnetwork_request_RequestJniQuickMatch.h"
#include "com_qpidnetwork_request_RequestJni_GobalFunc.h"
#include "RequestQuickMatchController.h"

void onQueryQuickMatchLadyList(long requestId, bool success, list<QuickMatchLady> itemList, string errnum, string errmsg);
void onSubmitQuickMatchMarkLadyList(long requestId, bool success, string errnum, string errmsg);
void onQueryQuickMatchLikeLadyList(long requestId, bool success, list<QuickMatchLady> itemList, int totalCount, string errnum, string errmsg);
void onRemoveQuickMatchLikeLadyList(long requestId, bool success, string errnum, string errmsg);

RequestQuickMatchControllerCallback gRequestQuickMatchControllerCallback {
	onQueryQuickMatchLadyList,
	onSubmitQuickMatchMarkLadyList,
	onQueryQuickMatchLikeLadyList,
	onRemoveQuickMatchLikeLadyList,
};
RequestQuickMatchController gRequestQuickMatchController(&gHttpRequestManager, gRequestQuickMatchControllerCallback);


/******************************************************************************/
void onQueryQuickMatchLadyList(long requestId, bool success, list<QuickMatchLady> itemList, string errnum, string errmsg) {
	FileLog("httprequest", "QuickMatch.Native::onQueryQuickMatchLadyList( success : %s )", success?"true":"false");

	/* turn object to java object here */
	JNIEnv* env;
	jint iRet = JNI_ERR;
	gJavaVM->GetEnv((void**)&env, JNI_VERSION_1_4);
	if( env == NULL ) {
		iRet = gJavaVM->AttachCurrentThread((JNIEnv **)&env, NULL);
	}

	jobjectArray jItemArray = NULL;
	JavaItemMap::iterator itr = gJavaItemMap.find(QUICKMATCH_LADY_ITEM_CLASS);
	if( itr != gJavaItemMap.end() ) {
		jclass cls = env->GetObjectClass(itr->second);
		jmethodID init = env->GetMethodID(cls, "<init>", "("
				"I"
				"Ljava/lang/String;"
				"Ljava/lang/String;"
				"Ljava/lang/String;"
				"Ljava/lang/String;"
				"Ljava/lang/String;"
				")V");

		if( itemList.size() > 0 ) {
			jItemArray = env->NewObjectArray(itemList.size(), cls, NULL);
			int i = 0;
			for(list<QuickMatchLady>::iterator itr = itemList.begin(); itr != itemList.end(); itr++, i++) {
				jstring womanid = env->NewStringUTF(itr->womanid.c_str());
				jstring firstname = env->NewStringUTF(itr->firstname.c_str());
				jstring country = env->NewStringUTF(itr->country.c_str());
				jstring image = env->NewStringUTF(itr->image.c_str());
				jstring photoURL = env->NewStringUTF(itr->photoURL.c_str());

				jobject item = env->NewObject(cls, init,
						itr->age,
						womanid,
						firstname,
						country,
						image,
						photoURL
						);

				env->SetObjectArrayElement(jItemArray, i, item);

				env->DeleteLocalRef(womanid);
				env->DeleteLocalRef(firstname);
				env->DeleteLocalRef(country);
				env->DeleteLocalRef(image);
				env->DeleteLocalRef(photoURL);

				env->DeleteLocalRef(item);
			}
		}
	}

	/* real callback java */
	jobject callbackObj = gCallbackMap.Erase(requestId);

	jclass callbackCls = env->GetObjectClass(callbackObj);

	string signure = "(ZLjava/lang/String;Ljava/lang/String;";
	signure += "[L";
	signure += QUICKMATCH_LADY_ITEM_CLASS;
	signure += ";";
	signure += ")V";
	jmethodID callback = env->GetMethodID(callbackCls, "OnQueryQuickMatchLadyList", signure.c_str());
	FileLog("httprequest", "QuickMatch.Native::onQueryQuickMatchLadyList( callback : %p, signure : %s )",
			callback, signure.c_str());

	if( callbackObj != NULL && callback != NULL ) {
		jstring jerrno = env->NewStringUTF(errnum.c_str());
		jstring jerrmsg = env->NewStringUTF(errmsg.c_str());

		FileLog("httprequest", "QuickMatch.Native::onQueryQuickMatchLadyList( CallObjectMethod "
				"jItemArray : %p )", jItemArray);

		env->CallVoidMethod(callbackObj, callback, success, jerrno, jerrmsg, jItemArray);

		env->DeleteGlobalRef(callbackObj);

		env->DeleteLocalRef(jerrno);
		env->DeleteLocalRef(jerrmsg);
	}

	if( jItemArray != NULL ) {
		env->DeleteLocalRef(jItemArray);
	}

	if( iRet == JNI_OK ) {
		gJavaVM->DetachCurrentThread();
	}
}

/*
 * Class:     com_qpidnetwork_request_RequestJniQuickMatch
 * Method:    QueryQuickMatchLadyList
 * Signature: (Ljava/lang/String;Lcom/qpidnetwork/request/OnQueryQuickMatchLadyListCallback;)J
 */
JNIEXPORT jlong JNICALL Java_com_qpidnetwork_request_RequestJniQuickMatch_QueryQuickMatchLadyList
  (JNIEnv *env, jclass cls, jstring deviceId, jobject callback) {
	jlong requestId = -1;

	const char *cpDeviceId = env->GetStringUTFChars(deviceId, 0);

	requestId = gRequestQuickMatchController.QueryQuickMatchLadyList(cpDeviceId);

	jobject obj = env->NewGlobalRef(callback);
	gCallbackMap.Insert(requestId, obj);

	env->ReleaseStringUTFChars(deviceId, cpDeviceId);

	return requestId;
}

/******************************************************************************/
void onSubmitQuickMatchMarkLadyList(long requestId, bool success, string errnum, string errmsg) {
	FileLog("httprequest", "QuickMatch.Native::onSubmitQuickMatchMarkLadyList( success : %s )", success?"true":"false");

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
	FileLog("httprequest", "QuickMatch.Native::onSubmitQuickMatchMarkLadyList( callbackCls : %p, callback : %p, signure : %s )",
			callbackCls, callback, signure.c_str());

	if( callbackObj != NULL && callback != NULL ) {
		jstring jerrno = env->NewStringUTF(errnum.c_str());
		jstring jerrmsg = env->NewStringUTF(errmsg.c_str());

		FileLog("httprequest", "QuickMatch.Native::onSubmitQuickMatchMarkLadyList( CallObjectMethod )");

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
 * Class:     com_qpidnetwork_request_RequestJniQuickMatch
 * Method:    SubmitQuickMatchMarkLadyList
 * Signature: ([Ljava/lang/String;[Ljava/lang/String;Lcom/qpidnetwork/request/OnRequestCallback;)J
 */
JNIEXPORT jlong JNICALL Java_com_qpidnetwork_request_RequestJniQuickMatch_SubmitQuickMatchMarkLadyList
  (JNIEnv *env, jclass cls, jobjectArray likeListId, jobjectArray unlikeListId, jobject callback) {
	jlong requestId = -1;

	jstring value;
	jint length = env->GetArrayLength(likeListId);
	list<string> likeList;
	for(int i = 0; i < length; i++) {
		value = (jstring) env->GetObjectArrayElement(likeListId, i);

		const char *cpValue = env->GetStringUTFChars(value, 0);
		likeList.push_back(cpValue);
		env->ReleaseStringUTFChars(value, cpValue);
	}

	list<string> unlikeList;
	for(int i = 0; i < length; i++) {
		value = (jstring) env->GetObjectArrayElement(likeListId, i);

		const char *cpValue = env->GetStringUTFChars(value, 0);
		unlikeList.push_back(cpValue);
		env->ReleaseStringUTFChars(value, cpValue);
	}

	requestId = gRequestQuickMatchController.SubmitQuickMatchMarkLadyList(likeList, unlikeList);

	jobject obj = env->NewGlobalRef(callback);
	gCallbackMap.Insert(requestId, obj);

	return requestId;
}

/******************************************************************************/
void onQueryQuickMatchLikeLadyList(long requestId, bool success, list<QuickMatchLady> itemList, int totalCount, string errnum, string errmsg) {
	FileLog("httprequest", "QuickMatch.Native::onQueryQuickMatchLikeLadyList( success : %s )", success?"true":"false");

	/* turn object to java object here */
	JNIEnv* env;
	jint iRet = JNI_ERR;
	gJavaVM->GetEnv((void**)&env, JNI_VERSION_1_4);
	if( env == NULL ) {
		iRet = gJavaVM->AttachCurrentThread((JNIEnv **)&env, NULL);
	}

	jobjectArray jItemArray = NULL;
	JavaItemMap::iterator itr = gJavaItemMap.find(QUICKMATCH_LADY_ITEM_CLASS);
	if( itr != gJavaItemMap.end() ) {
		jclass cls = env->GetObjectClass(itr->second);
		jmethodID init = env->GetMethodID(cls, "<init>", "("
				"I"
				"Ljava/lang/String;"
				"Ljava/lang/String;"
				"Ljava/lang/String;"
				"Ljava/lang/String;"
				"Ljava/lang/String;"
				")V");

		if( itemList.size() > 0 ) {
			jItemArray = env->NewObjectArray(itemList.size(), cls, NULL);
			int i = 0;
			for(list<QuickMatchLady>::iterator itr = itemList.begin(); itr != itemList.end(); itr++, i++) {
				jstring womanid = env->NewStringUTF(itr->womanid.c_str());
				jstring firstname = env->NewStringUTF(itr->firstname.c_str());
				jstring country = env->NewStringUTF(itr->country.c_str());
				jstring image = env->NewStringUTF(itr->image.c_str());
				jstring photoURL = env->NewStringUTF(itr->photoURL.c_str());

				jobject item = env->NewObject(cls, init,
						itr->age,
						womanid,
						firstname,
						country,
						image,
						photoURL
						);

				env->SetObjectArrayElement(jItemArray, i, item);

				env->DeleteLocalRef(womanid);
				env->DeleteLocalRef(firstname);
				env->DeleteLocalRef(country);
				env->DeleteLocalRef(image);
				env->DeleteLocalRef(photoURL);

				env->DeleteLocalRef(item);
			}
		}
	}

	/* real callback java */
	jobject callbackObj = gCallbackMap.Erase(requestId);

	jclass callbackCls = env->GetObjectClass(callbackObj);

	string signure = "(ZLjava/lang/String;Ljava/lang/String;";
	signure += "[L";
	signure += QUICKMATCH_LADY_ITEM_CLASS;
	signure += ";";
	signure += "I";
	signure += ")V";
	jmethodID callback = env->GetMethodID(callbackCls, "OnQueryQuickMatchLikeLadyList", signure.c_str());
	FileLog("httprequest", "QuickMatch.Native::onQueryQuickMatchLikeLadyList( callback : %p, signure : %s )",
			callback, signure.c_str());

	if( callbackObj != NULL && callback != NULL ) {
		jstring jerrno = env->NewStringUTF(errnum.c_str());
		jstring jerrmsg = env->NewStringUTF(errmsg.c_str());

		FileLog("httprequest", "QuickMatch.Native::onQueryQuickMatchLikeLadyList( CallObjectMethod "
				"jItemArray : %p )", jItemArray);

		env->CallVoidMethod(callbackObj, callback, success, jerrno, jerrmsg, jItemArray, totalCount);

		env->DeleteGlobalRef(callbackObj);

		env->DeleteLocalRef(jerrno);
		env->DeleteLocalRef(jerrmsg);
	}

	if( jItemArray != NULL ) {
		env->DeleteLocalRef(jItemArray);
	}

	if( iRet == JNI_OK ) {
		gJavaVM->DetachCurrentThread();
	}
}

/*
 * Class:     com_qpidnetwork_request_RequestJniQuickMatch
 * Method:    QueryQuickMatchLikeLadyList
 * Signature: (IILcom/qpidnetwork/request/OnQueryQuickMatchLadyLikeListCallback;)J
 */
JNIEXPORT jlong JNICALL Java_com_qpidnetwork_request_RequestJniQuickMatch_QueryQuickMatchLikeLadyList
  (JNIEnv *env, jclass cls, jint pageIndex, jint pageSize, jobject callback) {
	jlong requestId = -1;

	requestId = gRequestQuickMatchController.QueryQuickMatchLikeLadyList(pageIndex, pageSize);

	jobject obj = env->NewGlobalRef(callback);
	gCallbackMap.Insert(requestId, obj);

	return requestId;
}

/******************************************************************************/
void onRemoveQuickMatchLikeLadyList(long requestId, bool success, string errnum, string errmsg) {
	FileLog("httprequest", "QuickMatch.Native::onRemoveQuickMatchLikeLadyList( success : %s )", success?"true":"false");

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
	FileLog("httprequest", "QuickMatch.Native::onRemoveQuickMatchLikeLadyList( callbackCls : %p, callback : %p, signure : %s )",
			callbackCls, callback, signure.c_str());

	if( callbackObj != NULL && callback != NULL ) {
		jstring jerrno = env->NewStringUTF(errnum.c_str());
		jstring jerrmsg = env->NewStringUTF(errmsg.c_str());

		FileLog("httprequest", "QuickMatch.Native::onRemoveQuickMatchLikeLadyList( CallObjectMethod )");

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
 * Class:     com_qpidnetwork_request_RequestJniQuickMatch
 * Method:    RemoveQuickMatchLikeLadyList
 * Signature: ([Ljava/lang/String;Lcom/qpidnetwork/request/OnRequestCallback;)J
 */
JNIEXPORT jlong JNICALL Java_com_qpidnetwork_request_RequestJniQuickMatch_RemoveQuickMatchLikeLadyList
  (JNIEnv *env, jclass cls, jobjectArray likeListId, jobject callback) {

	jlong requestId = -1;

	jstring value;
	jint length = env->GetArrayLength(likeListId);
	list<string> likeList;
	for(int i = 0; i < length; i++) {
		value = (jstring) env->GetObjectArrayElement(likeListId, i);

		const char *cpValue = env->GetStringUTFChars(value, 0);
		likeList.push_back(cpValue);
		env->ReleaseStringUTFChars(value, cpValue);
	}

	requestId = gRequestQuickMatchController.RemoveQuickMatchLikeLadyList(likeList);

	jobject obj = env->NewGlobalRef(callback);
	gCallbackMap.Insert(requestId, obj);

	return requestId;

}
