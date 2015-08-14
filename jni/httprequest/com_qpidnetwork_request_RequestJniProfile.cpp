/*
 * com_qpidnetwork_request_RequestJniProfile.h
 *
 *  Created on: 2015-2-27
 *      Author: Max.Chiu
 *      Email: Kingsleyyau@gmail.com
 */
#include "com_qpidnetwork_request_RequestJniProfile.h"
#include "com_qpidnetwork_request_RequestJni_GobalFunc.h"
#include "RequestProfileController.h"

void onGetMyProfile(long requestId, bool success, ProfileItem item, string errnum, string errmsg);
void onUpdateMyProfile(long requestId, bool success, bool rsModified, string errnum, string errmsg);
void onStartEditResume(long requestId, bool success, string errnum, string errmsg);
void onSaveContact(long requestId, bool success, string errnum, string errmsg);
void onUploadHeaderPhoto(long requestId, bool success, string errnum, string errmsg);

RequestProfileControllerCallback gRequestProfileControllerCallback {
	onGetMyProfile,
	onUpdateMyProfile,
	onStartEditResume,
	onSaveContact,
	onUploadHeaderPhoto,
};
RequestProfileController gRequestProfileController(&gHttpRequestManager, gRequestProfileControllerCallback);

void onGetMyProfile(long requestId, bool success, ProfileItem item, string errnum, string errmsg) {
	FileLog("httprequest", "Profile.Native::onGetMyProfile( success : %s )", success?"true":"false");

	/* turn object to java object here */
	JNIEnv* env;
	jint iRet = JNI_ERR;
	gJavaVM->GetEnv((void**)&env, JNI_VERSION_1_4);
	if( env == NULL ) {
		iRet = gJavaVM->AttachCurrentThread((JNIEnv **)&env, NULL);
	}

	jobject jItem = NULL;
	JavaItemMap::iterator itr = gJavaItemMap.find(PROFILE_ITEM_CLASS);
	if( itr != gJavaItemMap.end() ) {
		jclass cls = env->GetObjectClass(itr->second);
		if( cls != NULL) {
			jmethodID init = env->GetMethodID(cls, "<init>", "("
					"Ljava/lang/String;"
					"I"
					"Ljava/lang/String;"
					"Ljava/lang/String;"
					"Ljava/lang/String;"
					"Ljava/lang/String;"

					"I"
					"I"
					"I"
					"I"
					"I"
					"I"
					"I"
					"I"
					"I"
					"I"
					"I"
					"I"
					"I"
					"I"

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

					"I"
					"I"
					"Ljava/lang/String;"
					"I"

					"Ljava/lang/String;"
					"I"
					"I"

					"Ljava/lang/String;"
					"I"
					"Ljava/lang/String;"
					"I"

					"Ljava/util/ArrayList;"
					")V"
					);

			FileLog("httprequest", "Profile.Native::onGetMyProfile( GetMethodID <init> : %p )", init);

			if( init != NULL ) {
				jstring manid = env->NewStringUTF(item.manid.c_str());
				jstring birthday = env->NewStringUTF(item.birthday.c_str());
				jstring firstname = env->NewStringUTF(item.firstname.c_str());
				jstring lastname = env->NewStringUTF(item.lastname.c_str());
				jstring email = env->NewStringUTF(item.email.c_str());

				jstring resume = env->NewStringUTF(item.resume.c_str());
				jstring resume_content = env->NewStringUTF(item.resume_content.c_str());

				jstring address1 = env->NewStringUTF(item.address1.c_str());
				jstring address2 = env->NewStringUTF(item.address2.c_str());
				jstring city = env->NewStringUTF(item.city.c_str());
				jstring province = env->NewStringUTF(item.province.c_str());
				jstring zipcode = env->NewStringUTF(item.zipcode.c_str());
				jstring telephone = env->NewStringUTF(item.telephone.c_str());
				jstring fax = env->NewStringUTF(item.fax.c_str());
				jstring alternate_email = env->NewStringUTF(item.alternate_email.c_str());
				jstring money = env->NewStringUTF(item.money.c_str());

				jstring photoURL = env->NewStringUTF(item.photoURL.c_str());
				jstring mobile = env->NewStringUTF(item.mobile.c_str());
				jstring landline = env->NewStringUTF(item.landline.c_str());
				jstring landline_ac = env->NewStringUTF(item.landline_ac.c_str());

				jclass jArrayList = env->FindClass("java/util/ArrayList");
				jmethodID jArrayListInit = env->GetMethodID(jArrayList, "<init>", "()V");
				jmethodID jArrayListAdd = env->GetMethodID(jArrayList, "add", "(Ljava/lang/Object;)Z");
				FileLog("httprequest", "Profile.Native::onGetMyProfile( "
										"jArrayList : %p, "
										"jArrayListInit : %p, "
										"jArrayListAdd : %p "
										")",
										jArrayList,
										jArrayListInit,
										jArrayListAdd
										);

				FileLog("httprequest", "Profile.Native::onGetMyProfile( "
						"item.interests.size() : %d "
						")",
						item.interests.size()
						);

				int i = 0;
				jobject jInterestList = NULL;
				jInterestList = env->NewObject(jArrayList, jArrayListInit);
				if( item.interests.size() > 0 ) {
					i = 0;
					for(list<string>::iterator itr = item.interests.begin(); itr != item.interests.end(); itr++, i++) {
						jstring value = env->NewStringUTF((*itr).c_str());
						env->CallBooleanMethod(jInterestList, jArrayListAdd, value);
						env->DeleteLocalRef(value);
					}
				}

				jItem = env->NewObject(cls, init,
						manid,
						item.age,
						birthday,
						firstname,
						lastname,
						email,

						item.gender,
						item.country,
						item.marry,
						item.height,
						item.weight,
						item.smoke,
						item.drink,
						item.language,
						item.religion,
						item.education,
						item.profession,
						item.ethnicity,
						item.income,
						item.children,

						resume,
						resume_content,
						item.resume_status,

						address1,
						address2,
						city,
						province,
						zipcode,
						telephone,
						fax,
						alternate_email,
						money,

						item.v_id,
						item.photo,
						photoURL,
						item.integral,

						mobile,
						item.mobile_cc,
						item.mobile_status,

						landline,
						item.landline_cc,
						landline_ac,
						item.landline_status,

						jInterestList
						);

				FileLog("httprequest", "Profile.Native::onGetMyProfile( NewObject : %p )", jItem);

				env->DeleteLocalRef(manid);
				env->DeleteLocalRef(birthday);
				env->DeleteLocalRef(firstname);
				env->DeleteLocalRef(lastname);
				env->DeleteLocalRef(email);
				env->DeleteLocalRef(resume);
				env->DeleteLocalRef(resume_content);
				env->DeleteLocalRef(address1);
				env->DeleteLocalRef(address2);
				env->DeleteLocalRef(city);
				env->DeleteLocalRef(province);
				env->DeleteLocalRef(zipcode);
				env->DeleteLocalRef(telephone);
				env->DeleteLocalRef(fax);
				env->DeleteLocalRef(alternate_email);
				env->DeleteLocalRef(money);
				env->DeleteLocalRef(photoURL);
				env->DeleteLocalRef(mobile);
				env->DeleteLocalRef(landline);
				env->DeleteLocalRef(landline_ac);

				if( jInterestList != NULL ) {
					env->DeleteLocalRef(jInterestList);
				}
			}
		}
	}

	/* real callback java */
	jobject callbackObj = gCallbackMap.Erase(requestId);
	jclass callbackCls = env->GetObjectClass(callbackObj);

	string signure = "(ZLjava/lang/String;Ljava/lang/String;";
	signure += "L";
	signure += PROFILE_ITEM_CLASS;
	signure += ";";
	signure += ")V";
	jmethodID callback = env->GetMethodID(callbackCls, "OnGetMyProfile", signure.c_str());
	FileLog("httprequest", "onGetMyProfile( callbackCls : %p, callback : %p, signure : %s )",
			callbackCls, callback, signure.c_str());

	if( callbackObj != NULL && callback != NULL ) {
		jstring jerrno = env->NewStringUTF(errnum.c_str());
		jstring jerrmsg = env->NewStringUTF(errmsg.c_str());

		FileLog("httprequest", "Profile.Native::onGetMyProfile( CallObjectMethod "
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
 * Class:     com_qpidnetwork_request_RequestJniProfile
 * Method:    GetMyProfile
 * Signature: (Lcom/qpidnetwork/request/OnGetMyProfileCallback;)J
 */
JNIEXPORT jlong JNICALL Java_com_qpidnetwork_request_RequestJniProfile_GetMyProfile
  (JNIEnv *env, jclass, jobject callback) {
	jlong requestId = -1;

	requestId = gRequestProfileController.GetMyProfile();

	jobject obj = env->NewGlobalRef(callback);
	gCallbackMap.Insert(requestId, obj);

	return requestId;
}

void onUpdateMyProfile(long requestId, bool success, bool rsModified, string errnum, string errmsg) {
	FileLog("httprequest", "Profile.Native::onUpdateMyProfile( success : %s )", success?"true":"false");

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

	string signure = "(ZLjava/lang/String;Ljava/lang/String;Z)V";
	jmethodID callback = env->GetMethodID(callbackCls, "OnUpdateMyProfile", signure.c_str());
	FileLog("httprequest", "Profile.Native::onUpdateMyProfile( callbackCls : %p, callback : %p, signure : %s )",
			callbackCls, callback, signure.c_str());

	if( callbackObj != NULL && callback != NULL ) {
		jstring jerrno = env->NewStringUTF(errnum.c_str());
		jstring jerrmsg = env->NewStringUTF(errmsg.c_str());

		FileLog("httprequest", "Profile.Native::onUpdateMyProfile( CallObjectMethod )");

		env->CallVoidMethod(callbackObj, callback, success, jerrno, jerrmsg, rsModified);

		env->DeleteGlobalRef(callbackObj);

		env->DeleteLocalRef(jerrno);
		env->DeleteLocalRef(jerrmsg);
	}

	if( iRet == JNI_OK ) {
		gJavaVM->DetachCurrentThread();
	}
}
/*
 * Class:     com_qpidnetwork_request_RequestJniProfile
 * Method:    UpdateProfile
 * Signature: (IIIIIIIIIIILjava/lang/String;Lcom/qpidnetwork/request/OnUpdateMyProfileCallback;)J
 */
JNIEXPORT jlong JNICALL Java_com_qpidnetwork_request_RequestJniProfile_UpdateProfile
  (JNIEnv *env, jclass, jint weight, jint height, jint language, jint ethnicity, jint religion,
		  jint education, jint profession, 	jint income, jint children, jint smoke,
		  jint drink, jstring resume, jobjectArray interests, jobject callback) {
	jlong requestId = -1;

	const char *cpResume = env->GetStringUTFChars(resume, 0);

	list<string> interestList;
	jint len = env->GetArrayLength(interests);
	for(int i = 0; i < len; i++) {
		jstring interest = (jstring)env->GetObjectArrayElement(interests, i);
		const char *cpInterest = env->GetStringUTFChars(interest, 0);
		interestList.push_back(cpInterest);
		env->ReleaseStringUTFChars(interest, cpInterest);
	}

	requestId = gRequestProfileController.UpdateProfile(weight, height, language, ethnicity, religion,
		  education, profession, income, children, smoke, drink, cpResume, interestList);

	jobject obj = env->NewGlobalRef(callback);
	gCallbackMap.Insert(requestId, obj);

	env->ReleaseStringUTFChars(resume, cpResume);

	return requestId;
}

void onStartEditResume(long requestId, bool success, string errnum, string errmsg) {
	FileLog("httprequest", "Profile.Native::onStartEditResume( success : %s )", success?"true":"false");

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
	FileLog("httprequest", "Profile.Native::onStartEditResume( callbackCls : %p, callback : %p, signure : %s )",
			callbackCls, callback, signure.c_str());

	if( callbackObj != NULL && callback != NULL ) {
		jstring jerrno = env->NewStringUTF(errnum.c_str());
		jstring jerrmsg = env->NewStringUTF(errmsg.c_str());

		FileLog("httprequest", "Profile.Native::onStartEditResume( CallObjectMethod )");

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
 * Class:     com_qpidnetwork_request_RequestJniProfile
 * Method:    StartEditResume
 * Signature: (Lcom/qpidnetwork/request/OnRegisterCallback;)J
 */
JNIEXPORT jlong JNICALL Java_com_qpidnetwork_request_RequestJniProfile_StartEditResume
  (JNIEnv *env, jclass, jobject callback) {
	jlong requestId = -1;

	requestId = gRequestProfileController.StartEditResume();
	jobject obj = env->NewGlobalRef(callback);
	gCallbackMap.Insert(requestId, obj);

	return requestId;
}

void onSaveContact(long requestId, bool success, string errnum, string errmsg) {
	FileLog("httprequest", "Profile.Native::onStartEditResume( success : %s )", success?"true":"false");

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
	FileLog("httprequest", "Profile.Native::onStartEditResume( callbackCls : %p, callback : %p, signure : %s )",
			callbackCls, callback, signure.c_str());

	if( callbackObj != NULL && callback != NULL ) {
		jstring jerrno = env->NewStringUTF(errnum.c_str());
		jstring jerrmsg = env->NewStringUTF(errmsg.c_str());

		FileLog("httprequest", "Profile.Native::onStartEditResume( CallObjectMethod )");

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
 * Class:     com_qpidnetwork_request_RequestJniProfile
 * Method:    SaveContact
 * Signature: (Ljava/lang/String;ILjava/lang/String;ILjava/lang/String;Lcom/qpidnetwork/request/OnRegisterCallback;)J
 */
JNIEXPORT jlong JNICALL Java_com_qpidnetwork_request_RequestJniProfile_SaveContact
  (JNIEnv *env, jclass, jstring telephone, jint telephone_cc, jstring landline, jint landline_cc,
		  jstring landline_ac, jobject callback) {
	jlong requestId = -1;

	const char *cpTelephone = env->GetStringUTFChars(telephone, 0);
	const char *cpLandline = env->GetStringUTFChars(landline, 0);
	const char *cpLandline_ac = env->GetStringUTFChars(landline_ac, 0);

	requestId = gRequestProfileController.SaveContact(cpTelephone, telephone_cc, cpLandline, landline_cc,
			cpLandline_ac);
	jobject obj = env->NewGlobalRef(callback);
	gCallbackMap.Insert(requestId, obj);

	env->ReleaseStringUTFChars(telephone, cpTelephone);
	env->ReleaseStringUTFChars(landline, cpLandline);
	env->ReleaseStringUTFChars(landline_ac, cpLandline_ac);

	return requestId;
}

void onUploadHeaderPhoto(long requestId, bool success, string errnum, string errmsg) {
	FileLog("httprequest", "Profile.Native::onUploadHeaderPhoto( success : %s )", success?"true":"false");

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
	FileLog("httprequest", "Profile.Native::onUploadHeaderPhoto( callbackCls : %p, callback : %p, signure : %s )",
			callbackCls, callback, signure.c_str());

	if( callbackObj != NULL && callback != NULL ) {
		jstring jerrno = env->NewStringUTF(errnum.c_str());
		jstring jerrmsg = env->NewStringUTF(errmsg.c_str());

		FileLog("httprequest", "Profile.Native::onUploadHeaderPhoto( CallObjectMethod )");

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
 * Class:     com_qpidnetwork_request_RequestJniProfile
 * Method:    UploadHeaderPhoto
 * Signature: (Ljava/lang/String;Lcom/qpidnetwork/request/OnRegisterCallback;)J
 */
JNIEXPORT jlong JNICALL Java_com_qpidnetwork_request_RequestJniProfile_UploadHeaderPhoto
  (JNIEnv *env, jclass, jstring fileName, jobject callback) {
	jlong requestId = -1;

	const char *cpFileName = env->GetStringUTFChars(fileName, 0);

	requestId = gRequestProfileController.UploadHeaderPhoto(cpFileName);
	jobject obj = env->NewGlobalRef(callback);
	gCallbackMap.Insert(requestId, obj);

	env->ReleaseStringUTFChars(fileName, cpFileName);

	return requestId;
}
