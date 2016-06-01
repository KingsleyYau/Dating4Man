/*
 * com_qpidnetwork_request_RequestJniLady.cpp
 *
 *  Created on: 2015-2-27
 *      Author: Max.Chiu
 *      Email: Kingsleyyau@gmail.com
 */
#include "com_qpidnetwork_request_RequestJniLady.h"
#include "com_qpidnetwork_request_RequestJni_GobalFunc.h"
#include <manrequesthandler/RequestLadyController.h>

void onQueryLadyMatch(long requestId, bool success, LadyMatch item, string errnum, string errmsg);
void onSaveLadyMatch(long requestId, bool success, string errnum, string errmsg);
void onQueryLadyList(long requestId, bool success, list<Lady> ladyList, int totalCount, string errnum, string errmsg);
void onQueryLadyDetail(long requestId, bool success, LadyDetail item, string errnum, string errmsg);
void onAddFavouritesLady(long requestId, bool success, string errnum, string errmsg);
void onRemoveFavouritesLady(long requestId, bool success, string errnum, string errmsg);
void onQueryLadyCall(long requestId, bool success, LadyCall item, string errnum, string errmsg);
void onRecentContact(long requestId, bool success, const string& errnum, const string& errmsg, const list<LadyRecentContact>& list);
void onRemoveContactList(long requestId, bool success, string errnum, string errmsg);
void onSignList(long requestId, bool success, const string& errnum, const string& errmsg, const list<LadySignListItem>& list);
void onUploadSign(long requestId, bool success, const string& errnum, const string& errmsg);

RequestLadyControllerCallback gRequestLadyControllerCallback {
	onQueryLadyMatch,
	onSaveLadyMatch,
	onQueryLadyList,
	onQueryLadyDetail,
	onAddFavouritesLady,
	onRemoveFavouritesLady,
	onQueryLadyCall,
	onRecentContact,
	onRemoveContactList,
	onSignList,
	onUploadSign
};
RequestLadyController gRequestLadyController(&gHttpRequestManager, gRequestLadyControllerCallback);

/******************************************************************************/
void onQueryLadyMatch(long requestId, bool success, LadyMatch item, string errnum, string errmsg) {
	FileLog("httprequest", "Lady.Native::onQueryLadyMatch( success : %s )", success?"true":"false");

	/* turn object to java object here */
	JNIEnv* env;
	jint iRet = JNI_ERR;
	gJavaVM->GetEnv((void**)&env, JNI_VERSION_1_4);
	if( env == NULL ) {
		iRet = gJavaVM->AttachCurrentThread((JNIEnv **)&env, NULL);
	}

	jobject jItem = NULL;
	JavaItemMap::iterator itr = gJavaItemMap.find(LADY_MATCH_ITEM_CLASS);
	if( itr != gJavaItemMap.end() ) {
		jclass cls = env->GetObjectClass(itr->second);
		if( cls != NULL) {
			jmethodID init = env->GetMethodID(cls, "<init>", "("
					"I"
					"I"
					"I"
					"I"
					"I"
					")V"
					);

			FileLog("httprequest", "Lady.Native::onQueryLadyMatch( GetMethodID <init> : %p )", init);

			if( init != NULL ) {
				jItem = env->NewObject(cls, init,
						item.age1,
						item.age2,
						item.marry,
						item.children,
						item.education
						);
				FileLog("httprequest", "Lady.Native::onQueryLadyMatch( NewObject : %p )", jItem);
			}
		}
	}

	/* real callback java */
	jobject callbackObj = gCallbackMap.Erase(requestId);

	jclass callbackCls = env->GetObjectClass(callbackObj);

	string signure = "(ZLjava/lang/String;Ljava/lang/String;";
	signure += "L";
	signure += LADY_MATCH_ITEM_CLASS;
	signure += ";";
	signure += ")V";
	jmethodID callback = env->GetMethodID(callbackCls, "OnQueryLadyMatch", signure.c_str());
	FileLog("httprequest", "Lady.Native::onQueryLadyMatch( callback : %p, signure : %s )",
			callback, signure.c_str());

	if( callbackObj != NULL && callback != NULL ) {
		jstring jerrno = env->NewStringUTF(errnum.c_str());
		jstring jerrmsg = env->NewStringUTF(errmsg.c_str());

		FileLog("httprequest", "Lady.Native::onQueryLadyMatch( CallObjectMethod "
				"jItem : %p )", jItem);

		env->CallVoidMethod(callbackObj, callback, success, jerrno, jerrmsg, jItem);

		env->DeleteGlobalRef(callbackObj);

		env->DeleteLocalRef(jerrno);
		env->DeleteLocalRef(jerrmsg);
	}

	if( jItem != NULL ) {
		env->DeleteLocalRef(jItem);
	}

	if( iRet == JNI_OK ) {
		gJavaVM->DetachCurrentThread();
	}
}
/*
 * Class:     com_qpidnetwork_request_RequestJniLady
 * Method:    QueryLadyMatch
 * Signature: (Lcom/qpidnetwork/request/OnQueryLadyMatchCallback;)J
 */
JNIEXPORT jlong JNICALL Java_com_qpidnetwork_request_RequestJniLady_QueryLadyMatch
  (JNIEnv *env, jclass, jobject callback) {
	jlong requestId = -1;

	requestId = gRequestLadyController.QueryLadyMatch();
	jobject obj = env->NewGlobalRef(callback);
	gCallbackMap.Insert(requestId, obj);

	return requestId;
}

/******************************************************************************/
void onSaveLadyMatch(long requestId, bool success, string errnum, string errmsg) {
	FileLog("httprequest", "Lady.Native::onSaveLadyMatch( success : %s )", success?"true":"false");

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
	FileLog("httprequest", "onSaveLadyMatch( callbackCls : %p, callback : %p, signure : %s )",
			callbackCls, callback, signure.c_str());

	if( callbackObj != NULL && callback != NULL ) {
		jstring jerrno = env->NewStringUTF(errnum.c_str());
		jstring jerrmsg = env->NewStringUTF(errmsg.c_str());

		FileLog("httprequest", "Lady.Native::onSaveLadyMatch( CallObjectMethod )");

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
 * Class:     com_qpidnetwork_request_RequestJniLady
 * Method:    SaveLadyMatch
 * Signature: (IIIIILcom/qpidnetwork/request/OnRequestCallback;)J
 */
JNIEXPORT jlong JNICALL Java_com_qpidnetwork_request_RequestJniLady_SaveLadyMatch
  (JNIEnv *env, jclass, jint ageRangeFrom, jint ageRangeTo, jint children,
			jint marry, jint education, jobject callback) {
	jlong requestId = -1;

	requestId = gRequestLadyController.SaveLadyMatch(ageRangeFrom, ageRangeTo, children, marry, education);
	jobject obj = env->NewGlobalRef(callback);
	gCallbackMap.Insert(requestId, obj);

	return requestId;
}

/******************************************************************************/
void onQueryLadyList(long requestId, bool success, list<Lady> ladyList, int totalCount, string errnum, string errmsg) {
	FileLog("httprequest", "Lady.Native::onQueryLadyList( success : %s )", success?"true":"false");

	/* turn object to java object here */
	JNIEnv* env;
	jint iRet = JNI_ERR;
	gJavaVM->GetEnv((void**)&env, JNI_VERSION_1_4);
	if( env == NULL ) {
		iRet = gJavaVM->AttachCurrentThread((JNIEnv **)&env, NULL);
	}

	jobjectArray jItemArray = NULL;
	JavaItemMap::iterator itr = gJavaItemMap.find(LADY_ITEM_CLASS);
	if( itr != gJavaItemMap.end() ) {
		jclass cls = env->GetObjectClass(itr->second);
		jmethodID init = env->GetMethodID(cls, "<init>", "("
				"I"
				"Ljava/lang/String;"
				"Ljava/lang/String;"
				"Ljava/lang/String;"
				"Ljava/lang/String;"
				"Ljava/lang/String;"
				"Ljava/lang/String;"
				"Ljava/lang/String;"
				"I"
				")V");

		if( ladyList.size() > 0 ) {
			jItemArray = env->NewObjectArray(ladyList.size(), cls, NULL);
			int i = 0;
			for(list<Lady>::iterator itr = ladyList.begin(); itr != ladyList.end(); itr++, i++) {
				jstring womanid = env->NewStringUTF(itr->womanid.c_str());
				jstring firstname = env->NewStringUTF(itr->firstname.c_str());
				jstring weight = env->NewStringUTF(itr->weight.c_str());
				jstring height = env->NewStringUTF(itr->height.c_str());
				jstring country = env->NewStringUTF(itr->country.c_str());
				jstring province = env->NewStringUTF(itr->province.c_str());
				jstring photoURL = env->NewStringUTF(itr->photoURL.c_str());

				jobject item = env->NewObject(cls, init,
						itr->age,
						womanid,
						firstname,
						weight,
						height,
						country,
						province,
						photoURL,
						itr->onlineStatus-1		// 处理数值转换
						);

				env->SetObjectArrayElement(jItemArray, i, item);

				env->DeleteLocalRef(womanid);
				env->DeleteLocalRef(firstname);
				env->DeleteLocalRef(weight);
				env->DeleteLocalRef(height);
				env->DeleteLocalRef(country);
				env->DeleteLocalRef(province);
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
	signure += LADY_ITEM_CLASS;
	signure += ";";
	signure += "I";
	signure += ")V";
	jmethodID callback = env->GetMethodID(callbackCls, "OnQueryLadyList", signure.c_str());
	FileLog("httprequest", "Lady.Native::onQueryLadyList( callback : %p, signure : %s )",
			callback, signure.c_str());

	if( callbackObj != NULL && callback != NULL ) {
		jstring jerrno = env->NewStringUTF(errnum.c_str());
		jstring jerrmsg = env->NewStringUTF(errmsg.c_str());

		FileLog("httprequest", "Lady.Native::onQueryLadyList( CallObjectMethod "
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
 * Class:     com_qpidnetwork_request_RequestJniLady
 * Method:    QueryLadyList
 * Signature: (IIILjava/lang/String;IIILjava/lang/String;ILjava/lang/String;Lcom/qpidnetwork/request/OnQueryLadyListCallback;)J
 */
JNIEXPORT jlong JNICALL Java_com_qpidnetwork_request_RequestJniLady_QueryLadyList
  (JNIEnv *env, jclass cls, jint pageIndex, jint pageSize, jint searchType, jstring womanId,
		  jint isOnline, jint ageRangeFrom, jint ageRangeTo, jstring country, jint orderBy, jstring deviceId, jobject callback) {
	jlong requestId = -1;

	string strWomanId("");
	if ( NULL != womanId ) {
		const char *cpWomanId = env->GetStringUTFChars(womanId, 0);
		strWomanId = cpWomanId;
		env->ReleaseStringUTFChars(womanId, cpWomanId);
	}

	string strCountry("");
	if ( NULL != country ) {
		const char *cpCountry = env->GetStringUTFChars(country, 0);
		strCountry = cpCountry;
		env->ReleaseStringUTFChars(country, cpCountry);
	}

	string strDeviceId("");
	if ( NULL != deviceId ) {
		const char *cpDeviceId = env->GetStringUTFChars(deviceId, 0);
		strDeviceId = cpDeviceId;
		env->ReleaseStringUTFChars(deviceId, cpDeviceId);
	}

	requestId = gRequestLadyController.QueryLadyList(pageIndex, pageSize, searchType, strWomanId, isOnline,
				ageRangeFrom, ageRangeTo, strCountry, orderBy, strDeviceId);

	jobject obj = env->NewGlobalRef(callback);
	gCallbackMap.Insert(requestId, obj);

	return requestId;
}

/******************************************************************************/
void onQueryLadyDetail(long requestId, bool success, LadyDetail item, string errnum, string errmsg) {
	FileLog("httprequest", "Lady.Native::onQueryLadyDetail( success : %s )", success?"true":"false");

	/* turn object to java object here */
	JNIEnv* env;
	jint iRet = JNI_ERR;
	gJavaVM->GetEnv((void**)&env, JNI_VERSION_1_4);
	if( env == NULL ) {
		iRet = gJavaVM->AttachCurrentThread((JNIEnv **)&env, NULL);
	}

	jobject jItem = NULL;
	JavaItemMap::iterator itr = gJavaItemMap.find(LADY_DETAIL_ITEM_CLASS);
	if( itr != gJavaItemMap.end() ) {
		jclass cls = env->GetObjectClass(itr->second);
		if( cls != NULL) {
			jmethodID init = env->GetMethodID(cls, "<init>", "("
					"Ljava/lang/String;"
					"Ljava/lang/String;"
					"Ljava/lang/String;"
					"Ljava/lang/String;"
					"Ljava/lang/String;"
					"I"
					"Ljava/lang/String;"
					"Ljava/lang/String;"
					"Ljava/lang/String;"
					"Ljava/lang/String;"
					"Ljava/lang/String;"
					"Ljava/lang/String;"
					"Ljava/lang/String;"
					"Ljava/lang/String;"
					"Ljava/lang/String;"
					"Ljava/lang/String;"
					"Ljava/lang/String;"
					"Ljava/lang/String;"
					"I"
					"I"
					"Z"
					"Z"
					"Ljava/lang/String;"
					"I"

					"Ljava/lang/String;"
					"Ljava/lang/String;"

					"Ljava/util/ArrayList;"
					"Ljava/util/ArrayList;"
					"Ljava/util/ArrayList;"
					"I"
					")V"
					);

			FileLog("httprequest", "Lady.Native::onQueryLadyDetail( GetMethodID <init> : %p )", init);

			if( init != NULL ) {
				jstring womanid = env->NewStringUTF(item.womanid.c_str());
				jstring firstname = env->NewStringUTF(item.firstname.c_str());
				jstring country = env->NewStringUTF(item.country.c_str());
				jstring province = env->NewStringUTF(item.province.c_str());
				jstring birthday = env->NewStringUTF(item.birthday.c_str());
				jstring zodiac = env->NewStringUTF(item.zodiac.c_str());
				jstring weight = env->NewStringUTF(item.weight.c_str());
				jstring height = env->NewStringUTF(item.height.c_str());
				jstring smoke = env->NewStringUTF(item.smoke.c_str());
				jstring drink = env->NewStringUTF(item.drink.c_str());
				jstring english = env->NewStringUTF(item.english.c_str());
				jstring religion = env->NewStringUTF(item.religion.c_str());
				jstring education = env->NewStringUTF(item.education.c_str());
				jstring profession = env->NewStringUTF(item.profession.c_str());
				jstring children = env->NewStringUTF(item.children.c_str());
				jstring marry = env->NewStringUTF(item.marry.c_str());
				jstring resume = env->NewStringUTF(item.resume.c_str());
				jstring last_update = env->NewStringUTF(item.last_update.c_str());

				jstring photoURL = env->NewStringUTF(item.photoURL.c_str());
				jstring photoMinURL = env->NewStringUTF(item.photoMinURL.c_str());

				jclass jArrayList = env->FindClass("java/util/ArrayList");
				jmethodID jArrayListInit = env->GetMethodID(jArrayList, "<init>", "()V");
				jmethodID jArrayListAdd = env->GetMethodID(jArrayList, "add", "(Ljava/lang/Object;)Z");
				FileLog("httprequest", "Lady.Native::onQueryLadyDetail( "
										"jArrayList : %p, "
										"jArrayListInit : %p, "
										"jArrayListAdd : %p "
										")",
										jArrayList,
										jArrayListInit,
										jArrayListAdd
										);

				FileLog("httprequest", "Lady.Native::onQueryLadyDetail( "
						"item.thumbList.size() : %d "
						")",
						item.thumbList.size()
						);

				int i = 0;
				jobject jThumbList = NULL;
				if( item.thumbList.size() > 0 ) {
					i = 0;
					jThumbList = env->NewObject(jArrayList, jArrayListInit);
					for(list<string>::iterator itr = item.thumbList.begin(); itr != item.thumbList.end(); itr++, i++) {
						jstring value = env->NewStringUTF((*itr).c_str());
						env->CallBooleanMethod(jThumbList, jArrayListAdd, value);
						env->DeleteLocalRef(value);
					}
				}

				FileLog("httprequest", "Lady.Native::onQueryLadyDetail( "
						"item.photoList.size() : %d "
						")",
						item.photoList.size()
						);

				jobject jPhotoList = NULL;
				if( item.photoList.size() > 0 ) {
					i = 0;
					jPhotoList = env->NewObject(jArrayList, jArrayListInit);
					for(list<string>::iterator itr = item.photoList.begin(); itr != item.photoList.end(); itr++, i++) {
						jstring value = env->NewStringUTF((*itr).c_str());
						env->CallBooleanMethod(jPhotoList, jArrayListAdd, value);
						env->DeleteLocalRef(value);
					}
				}

				FileLog("httprequest", "Lady.Native::onQueryLadyDetail( "
						"item.videoList.size() : %d "
						")",
						item.videoList.size());

				jobject jVideoList = NULL;
				if( item.videoList.size() > 0 ) {

					jclass jVideoItemCls = NULL;
					jmethodID jVideoItemInit = NULL;
					JavaItemMap::iterator itr = gJavaItemMap.find(LADY_VIDEO_ITEM_CLASS);
					if( itr != gJavaItemMap.end() ) {
						jVideoItemCls = env->GetObjectClass(itr->second);

						jVideoItemInit = env->GetMethodID(jVideoItemCls, "<init>", "("
											"Ljava/lang/String;"
											"Ljava/lang/String;"
											"Ljava/lang/String;"
											"Ljava/lang/String;"
											")V"
											);
					}

					FileLog("httprequest", "Lady.Native::onQueryLadyDetail( "
											"jVideoItemCls : %p, "
											"jVideoItemInit : %p "
											")",
											jVideoItemCls,
											jVideoItemInit
											);

					i = 0;
					jVideoList = env->NewObject(jArrayList, jArrayListInit);
					for(list<VideoItem>::iterator itr = item.videoList.begin(); itr != item.videoList.end(); itr++, i++) {
						if( jVideoItemCls != NULL && jVideoItemInit != NULL ) {
							jstring id = env->NewStringUTF((itr->id).c_str());
							jstring thumb = env->NewStringUTF((itr->thumb).c_str());
							jstring time = env->NewStringUTF((itr->time).c_str());
							jstring photo = env->NewStringUTF((itr->photo).c_str());

							jobject jVideoItem = env->NewObject(jVideoItemCls, jVideoItemInit,
									id,
									thumb,
									time,
									photo
									);

							env->CallBooleanMethod(jVideoList, jArrayListAdd, jVideoItem);

							env->DeleteLocalRef(id);
							env->DeleteLocalRef(thumb);
							env->DeleteLocalRef(time);
							env->DeleteLocalRef(photo);

							env->DeleteLocalRef(jVideoItem);
						}
					}
				}

				jItem = env->NewObject(cls, init,
						womanid,
						firstname,
						country,
						province,
						birthday,
						item.age,
						zodiac,
						weight,
						height,
						smoke,
						drink,
						english,
						religion,
						education,
						profession,
						children,
						marry,
						resume,
						item.age1,
						item.age2,
						item.isonline,
						item.isfavorite,
						last_update,
						item.show_lovecall,

						photoURL,
						photoMinURL,

						jThumbList,
						jPhotoList,
						jVideoList,
						item.photoLockNum
						);

				env->DeleteLocalRef(womanid);
				env->DeleteLocalRef(firstname);
				env->DeleteLocalRef(country);
				env->DeleteLocalRef(province);
				env->DeleteLocalRef(birthday);
				env->DeleteLocalRef(zodiac);
				env->DeleteLocalRef(weight);
				env->DeleteLocalRef(height);
				env->DeleteLocalRef(smoke);
				env->DeleteLocalRef(drink);
				env->DeleteLocalRef(english);
				env->DeleteLocalRef(religion);
				env->DeleteLocalRef(education);
				env->DeleteLocalRef(profession);
				env->DeleteLocalRef(children);
				env->DeleteLocalRef(marry);
				env->DeleteLocalRef(resume);
				env->DeleteLocalRef(last_update);

				env->DeleteLocalRef(photoURL);
				env->DeleteLocalRef(photoMinURL);

				if( jThumbList != NULL ) {
					env->DeleteLocalRef(jThumbList);
				}

				if( jPhotoList != NULL ) {
					env->DeleteLocalRef(jPhotoList);
				}

				if( jVideoList != NULL ) {
					env->DeleteLocalRef(jVideoList);
				}

				FileLog("httprequest", "Lady.Native::onQueryLadyDetail( NewObject : %p )", jItem);
			}
		}
	}

	/* real callback java */
	jobject callbackObj = gCallbackMap.Erase(requestId);

	jclass callbackCls = env->GetObjectClass(callbackObj);

	string signure = "(ZLjava/lang/String;Ljava/lang/String;";
	signure += "L";
	signure += LADY_DETAIL_ITEM_CLASS;
	signure += ";";
	signure += ")V";
	jmethodID callback = env->GetMethodID(callbackCls, "OnQueryLadyDetail", signure.c_str());
	FileLog("httprequest", "Lady.Native::onQueryLadyDetail( callback : %p, signure : %s )",
			callback, signure.c_str());

	if( callbackObj != NULL && callback != NULL ) {
		jstring jerrno = env->NewStringUTF(errnum.c_str());
		jstring jerrmsg = env->NewStringUTF(errmsg.c_str());

		FileLog("httprequest", "Lady.Native::OnQueryLadyDetail( CallObjectMethod "
				"jItem : %p )", jItem);

		env->CallVoidMethod(callbackObj, callback, success, jerrno, jerrmsg, jItem);

		env->DeleteGlobalRef(callbackObj);

		env->DeleteLocalRef(jerrno);
		env->DeleteLocalRef(jerrmsg);
	}

	if( jItem != NULL ) {
		env->DeleteLocalRef(jItem);
	}

	if( iRet == JNI_OK ) {
		gJavaVM->DetachCurrentThread();
	}
}
/*
 * Class:     com_qpidnetwork_request_RequestJniLady
 * Method:    QueryLadyDetail
 * Signature: (Ljava/lang/String;Lcom/qpidnetwork/request/OnQueryLadyDetailCallback;)J
 */
JNIEXPORT jlong JNICALL Java_com_qpidnetwork_request_RequestJniLady_QueryLadyDetail
  (JNIEnv *env, jclass, jstring womanId, jobject callback) {
	jlong requestId = -1;

	const char *cpWomanId = env->GetStringUTFChars(womanId, 0);

	requestId = gRequestLadyController.QueryLadyDetail(cpWomanId);
	jobject obj = env->NewGlobalRef(callback);
	gCallbackMap.Insert(requestId, obj);

	env->ReleaseStringUTFChars(womanId, cpWomanId);

	return requestId;
}

/******************************************************************************/
void onAddFavouritesLady(long requestId, bool success, string errnum, string errmsg) {
	FileLog("httprequest", "Lady.Native::onAddFavouritesLady( success : %s )", success?"true":"false");

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
	FileLog("httprequest", "Lady.Native::onAddFavouritesLady( callbackCls : %p, callback : %p, signure : %s )",
			callbackCls, callback, signure.c_str());

	if( callbackObj != NULL && callback != NULL ) {
		jstring jerrno = env->NewStringUTF(errnum.c_str());
		jstring jerrmsg = env->NewStringUTF(errmsg.c_str());

		FileLog("httprequest", "Lady.Native::onAddFavouritesLady( CallObjectMethod )");

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
 * Class:     com_qpidnetwork_request_RequestJniLady
 * Method:    AddFavouritesLady
 * Signature: (Ljava/lang/String;Lcom/qpidnetwork/request/OnRequestCallback;)J
 */
JNIEXPORT jlong JNICALL Java_com_qpidnetwork_request_RequestJniLady_AddFavouritesLady
  (JNIEnv *env, jclass, jstring womanId, jobject callback) {
	jlong requestId = -1;

	const char *cpWomanId = env->GetStringUTFChars(womanId, 0);

	requestId = gRequestLadyController.AddFavouritesLady(cpWomanId);
	jobject obj = env->NewGlobalRef(callback);
	gCallbackMap.Insert(requestId, obj);

	env->ReleaseStringUTFChars(womanId, cpWomanId);

	return requestId;
}

/******************************************************************************/
void onRemoveFavouritesLady(long requestId, bool success, string errnum, string errmsg) {
	FileLog("httprequest", "Lady.Native::onRemoveFavouritesLady( success : %s )", success?"true":"false");

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
	FileLog("httprequest", "Lady.Native::onRemoveFavouritesLady( callbackCls : %p, callback : %p, signure : %s )",
			callbackCls, callback, signure.c_str());

	if( callbackObj != NULL && callback != NULL ) {
		jstring jerrno = env->NewStringUTF(errnum.c_str());
		jstring jerrmsg = env->NewStringUTF(errmsg.c_str());

		FileLog("httprequest", "Lady.Native::onRemoveFavouritesLady( CallObjectMethod )");

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
 * Class:     com_qpidnetwork_request_RequestJniLady
 * Method:    RemoveFavouritesLady
 * Signature: (Ljava/lang/String;Lcom/qpidnetwork/request/OnRequestCallback;)J
 */
JNIEXPORT jlong JNICALL Java_com_qpidnetwork_request_RequestJniLady_RemoveFavouritesLady
  (JNIEnv *env, jclass, jstring womanId, jobject callback) {
	jlong requestId = -1;

	const char *cpWomanId = env->GetStringUTFChars(womanId, 0);

	requestId = gRequestLadyController.RemoveFavouritesLady(cpWomanId);
	jobject obj = env->NewGlobalRef(callback);
	gCallbackMap.Insert(requestId, obj);

	env->ReleaseStringUTFChars(womanId, cpWomanId);

	return requestId;
}

/******************************************************************************/
void onQueryLadyCall(long requestId, bool success, LadyCall item, string errnum, string errmsg) {
	FileLog("httprequest", "Lady.Native::onQueryLadyCall( success : %s )", success?"true":"false");

	/* turn object to java object here */
	JNIEnv* env;
	jint iRet = JNI_ERR;
	gJavaVM->GetEnv((void**)&env, JNI_VERSION_1_4);
	if( env == NULL ) {
		iRet = gJavaVM->AttachCurrentThread((JNIEnv **)&env, NULL);
	}

	jobject jItem = NULL;
	JavaItemMap::iterator itr = gJavaItemMap.find(LADY_CALL_ITEM_CLASS);
	if( itr != gJavaItemMap.end() ) {
		jclass cls = env->GetObjectClass(itr->second);
		if( cls != NULL) {
			jmethodID init = env->GetMethodID(cls, "<init>", "("
					"Ljava/lang/String;"
					"Ljava/lang/String;"
					"Ljava/lang/String;"
					")V"
					);

			FileLog("httprequest", "Lady.Native::onQueryLadyCall( GetMethodID <init> : %p )", init);

			if( init != NULL ) {
				jstring womanid = env->NewStringUTF(item.womanid.c_str());
				jstring lovecallid = env->NewStringUTF(item.lovecallid.c_str());
				jstring lc_centernumber = env->NewStringUTF(item.lc_centernumber.c_str());

				jItem = env->NewObject(cls, init,
						womanid,
						lovecallid,
						lc_centernumber
						);

				env->DeleteLocalRef(womanid);
				env->DeleteLocalRef(lovecallid);
				env->DeleteLocalRef(lc_centernumber);

				FileLog("httprequest", "Lady.Native::onQueryLadyCall( NewObject : %p )", jItem);
			}
		}
	}

	/* real callback java */
	jobject callbackObj = gCallbackMap.Erase(requestId);

	jclass callbackCls = env->GetObjectClass(callbackObj);

	string signure = "(ZLjava/lang/String;Ljava/lang/String;";
	signure += "L";
	signure += LADY_CALL_ITEM_CLASS;
	signure += ";";
	signure += ")V";
	jmethodID callback = env->GetMethodID(callbackCls, "OnQueryLadyCall", signure.c_str());
	FileLog("httprequest", "Lady.Native::onQueryLadyCall( callback : %p, signure : %s )",
			callback, signure.c_str());

	if( callbackObj != NULL && callback != NULL ) {
		jstring jerrno = env->NewStringUTF(errnum.c_str());
		jstring jerrmsg = env->NewStringUTF(errmsg.c_str());

		FileLog("httprequest", "Lady.Native::onQueryLadyCall( CallObjectMethod "
				"jItem : %p )", jItem);

		env->CallVoidMethod(callbackObj, callback, success, jerrno, jerrmsg, jItem);

		env->DeleteGlobalRef(callbackObj);

		env->DeleteLocalRef(jerrno);
		env->DeleteLocalRef(jerrmsg);
	}

	if( jItem != NULL ) {
		env->DeleteLocalRef(jItem);
	}

	if( iRet == JNI_OK ) {
		gJavaVM->DetachCurrentThread();
	}
}
/*
 * Class:     com_qpidnetwork_request_RequestJniLady
 * Method:    QueryLadyCall
 * Signature: (Ljava/lang/String;Lcom/qpidnetwork/request/OnQueryLadyCallCallback;)J
 */
JNIEXPORT jlong JNICALL Java_com_qpidnetwork_request_RequestJniLady_QueryLadyCall
  (JNIEnv *env, jclass, jstring womanId, jobject callback) {
	jlong requestId = -1;

	const char *cpWomanId = env->GetStringUTFChars(womanId, 0);

	requestId = gRequestLadyController.QueryLadyCall(cpWomanId);
	jobject obj = env->NewGlobalRef(callback);
	gCallbackMap.Insert(requestId, obj);

	env->ReleaseStringUTFChars(womanId, cpWomanId);

	return requestId;
}

/******************************************************************************/
void onRecentContact(long requestId, bool success, const string& errnum, const string& errmsg, const list<LadyRecentContact>& contactList)
{
	FileLog("httprequest", "Lady.Native::onRecentContact( success : %s )", success?"true":"false");

	/* turn object to java object here */
	JNIEnv* env;
	jint iRet = JNI_ERR;
	iRet = gJavaVM->GetEnv((void**)&env, JNI_VERSION_1_4);
	if( iRet == JNI_EDETACHED ) {
		iRet = gJavaVM->AttachCurrentThread((JNIEnv **)&env, NULL);
	}

	jobjectArray jItemArray = NULL;
	JavaItemMap::iterator itr = gJavaItemMap.find(LADY_RECENTCONTACT_ITEM_CLASS);
	if( itr != gJavaItemMap.end() ) {
		jclass cls = env->GetObjectClass(itr->second);
		jmethodID init = env->GetMethodID(cls, "<init>", "("
				"Ljava/lang/String;"
				"Ljava/lang/String;"
				"I"
				"Ljava/lang/String;"
				"Ljava/lang/String;"
				"Z"
				"I"
				"I"
				")V");

		if( contactList.size() > 0 ) {
			jItemArray = env->NewObjectArray(contactList.size(), cls, NULL);
			int i = 0;
			for(list<LadyRecentContact>::const_iterator itr = contactList.begin(); itr != contactList.end(); itr++, i++) {
				jstring womanId = env->NewStringUTF(itr->womanId.c_str());
				jstring firstname = env->NewStringUTF(itr->firstname.c_str());
				jstring photoURL = env->NewStringUTF(itr->photoURL.c_str());
				jstring photoBigURL = env->NewStringUTF(itr->photoBigURL.c_str());

				jobject item = env->NewObject(cls, init,
						womanId,
						firstname,
						itr->age,
						photoURL,
						photoBigURL,
						itr->isFavorite,
						itr->videoCount,
						itr->lasttime
						);

				env->DeleteLocalRef(womanId);
				env->DeleteLocalRef(firstname);
				env->DeleteLocalRef(photoURL);
				env->DeleteLocalRef(photoBigURL);

				env->SetObjectArrayElement(jItemArray, i, item);

				env->DeleteLocalRef(item);

				FileLog("httprequest", "Lady.Native::onRecentContact() itr.lasttime:%ld",
						itr->lasttime);
			}
		}
	}

	/* real callback java */
	jobject callbackObj = gCallbackMap.Erase(requestId);

	jclass callbackCls = env->GetObjectClass(callbackObj);

	string signure = "(ZLjava/lang/String;Ljava/lang/String;";
	signure += "[L";
	signure += LADY_RECENTCONTACT_ITEM_CLASS;
	signure += ";";
	signure += ")V";
	jmethodID callback = env->GetMethodID(callbackCls, "OnLadyRecentContactList", signure.c_str());
	FileLog("httprequest", "Lady.Native::onRecentContact( object:%p, callback:%p, signure:%s )",
			callbackObj, callback, signure.c_str());

	if( callbackObj != NULL && callback != NULL ) {
		jstring jerrno = env->NewStringUTF(errnum.c_str());
		jstring jerrmsg = env->NewStringUTF(errmsg.c_str());

		FileLog("httprequest", "Lady.Native::onRecentContact( CallObjectMethod "
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
 * Class:     com_qpidnetwork_request_RequestJniLady
 * Method:    RecentContact
 * Signature: (Lcom/qpidnetwork/request/OnLadyRecentContactListCallback;)J
 */
JNIEXPORT jlong JNICALL Java_com_qpidnetwork_request_RequestJniLady_RecentContact
  (JNIEnv *env, jclass cls, jobject callback)
{
	jlong requestId = -1;

	// 请求
	requestId = gRequestLadyController.RecentContactList();
	if (requestId != -1) {
		jobject obj = env->NewGlobalRef(callback);
		gCallbackMap.Insert(requestId, obj);

		FileLog("httprequest", "Lady.Native::RecentContact() request success, requestId:%lld, callback: %p", requestId, callback);
	}
	else {
		FileLog("httprequest", "Lady.Native::RecentContact() request fail, requestId:%lld, callback: %p", requestId, callback);
	}

	return requestId;
}

/******************************************************************************/
void onRemoveContactList(long requestId, bool success, string errnum, string errmsg)
{
	FileLog("httprequest", "Lady.Native::onRemoveContactList( success : %s )", success?"true":"false");

	/* turn object to java object here */
	JNIEnv* env;
	jint iRet = JNI_ERR;
	iRet = gJavaVM->GetEnv((void**)&env, JNI_VERSION_1_4);
	if( iRet == JNI_EDETACHED ) {
		iRet = gJavaVM->AttachCurrentThread((JNIEnv **)&env, NULL);
	}

	/* real callback java */
	jobject callbackObj = gCallbackMap.Erase(requestId);

	jclass callbackCls = env->GetObjectClass(callbackObj);

	string signure = "(ZLjava/lang/String;Ljava/lang/String;)V";
	jmethodID callback = env->GetMethodID(callbackCls, "OnRequest", signure.c_str());
	FileLog("httprequest", "Lady.Native::onRemoveContactList( object:%p, callback:%p, signure:%s )",
			callbackObj, callback, signure.c_str());

	if( callbackObj != NULL && callback != NULL ) {
		jstring jerrno = env->NewStringUTF(errnum.c_str());
		jstring jerrmsg = env->NewStringUTF(errmsg.c_str());

		FileLog("httprequest", "Lady.Native::onRemoveContactList( CallObjectMethod )");

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
 * Class:     com_qpidnetwork_request_RequestJniLady
 * Method:    RemoveContactList
 * Signature: ([Ljava/lang/String;Lcom/qpidnetwork/request/OnRequestCallback;)J
 */
JNIEXPORT jlong JNICALL Java_com_qpidnetwork_request_RequestJniLady_RemoveContactList
  (JNIEnv *env, jclass cls, jobjectArray womanListId, jobject callback)
{
	jlong requestId = -1;

	jstring value;
	jint length = env->GetArrayLength(womanListId);
	list<string> womanList;
	for(int i = 0; i < length; i++) {
		value = (jstring) env->GetObjectArrayElement(womanListId, i);

		const char *cpValue = env->GetStringUTFChars(value, 0);
		womanList.push_back(cpValue);
		env->ReleaseStringUTFChars(value, cpValue);
	}

	requestId = gRequestLadyController.RemoveContactList(womanList);

	jobject obj = env->NewGlobalRef(callback);
	gCallbackMap.Insert(requestId, obj);

	return requestId;
}

/******************************************************************************/
void onSignList(long requestId, bool success, const string& errnum, const string& errmsg, const list<LadySignListItem>& signList)
{
	FileLog("httprequest", "Lady.Native::onSignList( success : %s )", success?"true":"false");

	/* turn object to java object here */
	JNIEnv* env;
	jint iRet = JNI_ERR;
	gJavaVM->GetEnv((void**)&env, JNI_VERSION_1_4);
	if( env == NULL ) {
		iRet = gJavaVM->AttachCurrentThread((JNIEnv **)&env, NULL);
	}

	jobjectArray jItemArray = NULL;
	JavaItemMap::iterator itr = gJavaItemMap.find(LADY_SIGN_ITEM_CLASS);
	if( itr != gJavaItemMap.end() ) {
		jclass cls = env->GetObjectClass(itr->second);
		jmethodID init = env->GetMethodID(cls, "<init>", "("
				"Ljava/lang/String;"
				"Ljava/lang/String;"
				"Ljava/lang/String;"
				"Z"
				")V");

		if( signList.size() > 0 ) {
			jItemArray = env->NewObjectArray(signList.size(), cls, NULL);
			int i = 0;
			for(list<LadySignListItem>::const_iterator itr = signList.begin(); itr != signList.end(); itr++, i++) {
				jstring signId = env->NewStringUTF(itr->signId.c_str());
				jstring name = env->NewStringUTF(itr->name.c_str());
				jstring color = env->NewStringUTF(itr->color.c_str());

				jobject item = env->NewObject(cls, init,
						signId,
						name,
						color,
						itr->isSigned
						);

				env->SetObjectArrayElement(jItemArray, i, item);

				env->DeleteLocalRef(signId);
				env->DeleteLocalRef(name);
				env->DeleteLocalRef(color);

				env->DeleteLocalRef(item);
			}
		}
	}

	/* real callback java */
	jobject callbackObj = gCallbackMap.Erase(requestId);

	jclass callbackCls = env->GetObjectClass(callbackObj);

	string signure = "(ZLjava/lang/String;Ljava/lang/String;";
	signure += "[L";
	signure += LADY_SIGN_ITEM_CLASS;
	signure += ";";
	signure += ")V";
	jmethodID callback = env->GetMethodID(callbackCls, "OnLadySignList", signure.c_str());
	FileLog("httprequest", "Lady.Native::onSignList( callback : %p, signure : %s )",
			callback, signure.c_str());

	if( callbackObj != NULL && callback != NULL ) {
		jstring jerrno = env->NewStringUTF(errnum.c_str());
		jstring jerrmsg = env->NewStringUTF(errmsg.c_str());

		FileLog("httprequest", "Lady.Native::onSignList( CallObjectMethod "
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
 * Class:     com_qpidnetwork_request_RequestJniLady
 * Method:    SignList
 * Signature: (Ljava/lang/String;Lcom/qpidnetwork/request/OnLadySignListCallback;)J
 */
JNIEXPORT jlong JNICALL Java_com_qpidnetwork_request_RequestJniLady_SignList
  (JNIEnv *env, jclass cls, jstring womanId, jobject callback)
{
	jlong requestId = -1;

	// womanId
	const char *cpWomanId = env->GetStringUTFChars(womanId, 0);
	string strWomanId = cpWomanId;
	env->ReleaseStringUTFChars(womanId, cpWomanId);

	// 请求
	requestId = gRequestLadyController.QuerySignList(strWomanId);
	if (requestId != -1) {
		jobject obj = env->NewGlobalRef(callback);
		gCallbackMap.Insert(requestId, obj);
	}
	return requestId;
}

/******************************************************************************/
void onUploadSign(long requestId, bool success, const string& errnum, const string& errmsg)
{
	FileLog("httprequest", "Lady.Native::onUploadSign( success : %s )", success?"true":"false");

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
	FileLog("httprequest", "Lady.Native::onUploadSign( callbackCls : %p, callback : %p, signure : %s )",
			callbackCls, callback, signure.c_str());

	if( callbackObj != NULL && callback != NULL ) {
		jstring jerrno = env->NewStringUTF(errnum.c_str());
		jstring jerrmsg = env->NewStringUTF(errmsg.c_str());

		FileLog("httprequest", "Lady.Native::onUploadSign( CallObjectMethod )");

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
 * Class:     com_qpidnetwork_request_RequestJniLady
 * Method:    UploadSign
 * Signature: (Ljava/lang/String;[Ljava/lang/String;Lcom/qpidnetwork/request/OnRequestCallback;)J
 */
JNIEXPORT jlong JNICALL Java_com_qpidnetwork_request_RequestJniLady_UploadSign
  (JNIEnv *env, jclass cls, jstring womanId, jobjectArray signArray, jobject callback)
{
	jlong requestId = -1;

	// signArray
	list<string> signList;
	for (int i = 0; i < env->GetArrayLength(signArray); i++) {
		jstring signId = (jstring)env->GetObjectArrayElement(signArray, i);
		if (signId != NULL) {
			const char* cpTemp = env->GetStringUTFChars(signId, 0);
			if (NULL != cpTemp && strlen(cpTemp) > 0) {
				signList.push_back(cpTemp);
			}
			env->ReleaseStringUTFChars(signId, cpTemp);
		}
	}

	// womanId
	const char *cpWomanId = env->GetStringUTFChars(womanId, 0);
	string strWomanId = cpWomanId;
	env->ReleaseStringUTFChars(womanId, cpWomanId);

	// 请求
	requestId = gRequestLadyController.UploadSigned(strWomanId, signList);
	if (requestId != -1) {
		jobject obj = env->NewGlobalRef(callback);
		gCallbackMap.Insert(requestId, obj);
	}
	return requestId;
}
