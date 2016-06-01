/*
 * RequestJni_GobalFunc.h
 *
 *  Created on: 2015-3-4
 *      Author: Max.Chiu
 *      Email: Kingsleyyau@gmail.com
 */

#ifndef REQUESTJNI_GOBALFUNC_H_
#define REQUESTJNI_GOBALFUNC_H_

#include <jni.h>

#include <manrequesthandler/HttpRequestManager.h>
#include <manrequesthandler/HttpRequestHostManager.h>
#include "CallbackItemAndroidDef.h"

#include <common/KSafeMap.h>

#include <string>
#include <list>
using namespace std;

extern JavaVM* gJavaVM;

extern HttpRequestHostManager gHttpRequestHostManager;
extern HttpRequestManager gHttpRequestManager;

/* java callback object */
typedef KSafeMap<long, jobject> CallbackMap;
extern CallbackMap gCallbackMap;

/* java data item */
typedef map<string, jobject> JavaItemMap;
extern JavaItemMap gJavaItemMap;

string JString2String(JNIEnv* env, jstring str);
void InitEnumHelper(JNIEnv *env, const char *path, jobject *objptr);
void InitClassHelper(JNIEnv *env, const char *path, jobject *objptr);
jclass GetJClass(JNIEnv* env, const char* classPath);

bool GetEnv(JNIEnv** env, bool* isAttachThread);
bool ReleaseEnv(bool isAttachThread);

#endif /* REQUESTJNI_GOBALFUNC_H_ */
