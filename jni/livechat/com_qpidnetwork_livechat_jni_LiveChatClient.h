/* DO NOT EDIT THIS FILE - it is machine generated */
#include <jni.h>
/* Header for class com_qpidnetwork_livechat_jni_LiveChatClient */

#ifndef _Included_com_qpidnetwork_livechat_jni_LiveChatClient
#define _Included_com_qpidnetwork_livechat_jni_LiveChatClient
#ifdef __cplusplus
extern "C" {
#endif
/*
 * Class:     com_qpidnetwork_livechat_jni_LiveChatClient
 * Method:    SetLogDirectory
 * Signature: (Ljava/lang/String;)V
 */
JNIEXPORT void JNICALL Java_com_qpidnetwork_livechat_jni_LiveChatClient_SetLogDirectory
  (JNIEnv *, jclass, jstring);

/*
 * Class:     com_qpidnetwork_livechat_jni_LiveChatClient
 * Method:    Init
 * Signature: (Lcom/qpidnetwork/livechat/LiveChatClientListener;[Ljava/lang/String;I)Z
 */
JNIEXPORT jboolean JNICALL Java_com_qpidnetwork_livechat_jni_LiveChatClient_Init
  (JNIEnv *, jclass, jobject, jobjectArray, jint);

/*
 * Class:     com_qpidnetwork_livechat_jni_LiveChatClient
 * Method:    Login
 * Signature: (Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;I)Z
 */
JNIEXPORT jboolean JNICALL Java_com_qpidnetwork_livechat_jni_LiveChatClient_Login
  (JNIEnv *, jclass, jstring, jstring, jstring, jint, jint);

/*
 * Class:     com_qpidnetwork_livechat_jni_LiveChatClient
 * Method:    Logout
 * Signature: ()Z
 */
JNIEXPORT jboolean JNICALL Java_com_qpidnetwork_livechat_jni_LiveChatClient_Logout
  (JNIEnv *, jclass);

/*
 * Class:     com_qpidnetwork_livechat_jni_LiveChatClient
 * Method:    SetStatus
 * Signature: (I)Z
 */
JNIEXPORT jboolean JNICALL Java_com_qpidnetwork_livechat_jni_LiveChatClient_SetStatus
  (JNIEnv *, jclass, jint);

/*
 * Class:     com_qpidnetwork_livechat_jni_LiveChatClient
 * Method:    EndTalk
 * Signature: (Ljava/lang/String;)Z
 */
JNIEXPORT jboolean JNICALL Java_com_qpidnetwork_livechat_jni_LiveChatClient_EndTalk
  (JNIEnv *, jclass, jstring);

/*
 * Class:     com_qpidnetwork_livechat_jni_LiveChatClient
 * Method:    GetUserStatus
 * Signature: ([Ljava/lang/String;)Z
 */
JNIEXPORT jboolean JNICALL Java_com_qpidnetwork_livechat_jni_LiveChatClient_GetUserStatus
  (JNIEnv *, jclass, jobjectArray);

/*
 * Class:     com_qpidnetwork_livechat_jni_LiveChatClient
 * Method:    GetTalkInfo
 * Signature: (Ljava/lang/String;)Z
 */
JNIEXPORT jboolean JNICALL Java_com_qpidnetwork_livechat_jni_LiveChatClient_GetTalkInfo
  (JNIEnv *, jclass, jstring);

/*
 * Class:     com_qpidnetwork_livechat_jni_LiveChatClient
 * Method:    UploadTicket
 * Signature: (Ljava/lang/String;I)Z
 */
JNIEXPORT jboolean JNICALL Java_com_qpidnetwork_livechat_jni_LiveChatClient_UploadTicket
  (JNIEnv *, jclass, jstring, jint);

/*
 * Class:     com_qpidnetwork_livechat_jni_LiveChatClient
 * Method:    SendMessage
 * Signature: (Ljava/lang/String;Ljava/lang/String;ZI)Z
 */
JNIEXPORT jboolean JNICALL Java_com_qpidnetwork_livechat_jni_LiveChatClient_SendMessage
  (JNIEnv *, jclass, jstring, jstring, jboolean, jint);

/*
 * Class:     com_qpidnetwork_livechat_jni_LiveChatClient
 * Method:    SendEmotion
 * Signature: (Ljava/lang/String;Ljava/lang/String;I)Z
 */
JNIEXPORT jboolean JNICALL Java_com_qpidnetwork_livechat_jni_LiveChatClient_SendEmotion
  (JNIEnv *, jclass, jstring, jstring, jint);

/*
 * Class:     com_qpidnetwork_livechat_jni_LiveChatClient
 * Method:    SendVGift
 * Signature: (Ljava/lang/String;Ljava/lang/String;I)Z
 */
JNIEXPORT jboolean JNICALL Java_com_qpidnetwork_livechat_jni_LiveChatClient_SendVGift
  (JNIEnv *, jclass, jstring, jstring, jint);

/*
 * Class:     com_qpidnetwork_livechat_jni_LiveChatClient
 * Method:    GetVoiceCode
 * Signature: (Ljava/lang/String;I)Z
 */
JNIEXPORT jboolean JNICALL Java_com_qpidnetwork_livechat_jni_LiveChatClient_GetVoiceCode
  (JNIEnv *, jclass, jstring, jint);

/*
 * Class:     com_qpidnetwork_livechat_jni_LiveChatClient
 * Method:    SendVoice
 * Signature: (Ljava/lang/String;Ljava/lang/String;II)Z
 */
JNIEXPORT jboolean JNICALL Java_com_qpidnetwork_livechat_jni_LiveChatClient_SendVoice
  (JNIEnv *, jclass, jstring, jstring, jint, jint);

/*
 * Class:     com_qpidnetwork_livechat_jni_LiveChatClient
 * Method:    UseTryTicket
 * Signature: (Ljava/lang/String;)Z
 */
JNIEXPORT jboolean JNICALL Java_com_qpidnetwork_livechat_jni_LiveChatClient_UseTryTicket
  (JNIEnv *, jclass, jstring);

/*
 * Class:     com_qpidnetwork_livechat_jni_LiveChatClient
 * Method:    GetTalkList
 * Signature: (I)Z
 */
JNIEXPORT jboolean JNICALL Java_com_qpidnetwork_livechat_jni_LiveChatClient_GetTalkList
  (JNIEnv *, jclass, jint);

/*
 * Class:     com_qpidnetwork_livechat_jni_LiveChatClient
 * Method:    SendPhoto
 * Signature: (Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;ZLjava/lang/String;I)Z
 */
JNIEXPORT jboolean JNICALL Java_com_qpidnetwork_livechat_jni_LiveChatClient_SendPhoto
  (JNIEnv *, jclass, jstring, jstring, jstring, jstring, jboolean, jstring, jint);

/*
 * Class:     com_qpidnetwork_livechat_jni_LiveChatClient
 * Method:    ShowPhoto
 * Signature: (Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;ZLjava/lang/String;I)Z
 */
JNIEXPORT jboolean JNICALL Java_com_qpidnetwork_livechat_jni_LiveChatClient_ShowPhoto
  (JNIEnv *, jclass, jstring, jstring, jstring, jstring, jboolean, jstring, jint);

/*
 * Class:     com_qpidnetwork_livechat_jni_LiveChatClient
 * Method:    PlayVideo
 * Signature: (Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;ZLjava/lang/String;I)Z
 */
JNIEXPORT jboolean JNICALL Java_com_qpidnetwork_livechat_jni_LiveChatClient_PlayVideo
  (JNIEnv *, jclass, jstring, jstring, jstring, jstring, jboolean, jstring, jint);

/*
 * Class:     com_qpidnetwork_livechat_jni_LiveChatClient
 * Method:    GetUserInfo
 * Signature: (Ljava/lang/String;)Z
 */
JNIEXPORT jboolean JNICALL Java_com_qpidnetwork_livechat_jni_LiveChatClient_GetUserInfo
  (JNIEnv *, jclass, jstring);

/*
 * Class:     com_qpidnetwork_livechat_jni_LiveChatClient
 * Method:    GetBlockList
 * Signature: ()Z
 */
JNIEXPORT jboolean JNICALL Java_com_qpidnetwork_livechat_jni_LiveChatClient_GetBlockList
  (JNIEnv *, jclass);

/*
 * Class:     com_qpidnetwork_livechat_jni_LiveChatClient
 * Method:    GetContactList
 * Signature: ()Z
 */
JNIEXPORT jboolean JNICALL Java_com_qpidnetwork_livechat_jni_LiveChatClient_GetContactList
  (JNIEnv *, jclass);

/*
 * Class:     com_qpidnetwork_livechat_jni_LiveChatClient
 * Method:    UploadVer
 * Signature: (Ljava/lang/String;)Z
 */
JNIEXPORT jboolean JNICALL Java_com_qpidnetwork_livechat_jni_LiveChatClient_UploadVer
  (JNIEnv *, jclass, jstring);

/*
 * Class:     com_qpidnetwork_livechat_jni_LiveChatClient
 * Method:    GetBlockUsers
 * Signature: ()Z
 */
JNIEXPORT jboolean JNICALL Java_com_qpidnetwork_livechat_jni_LiveChatClient_GetBlockUsers
  (JNIEnv *, jclass);

#ifdef __cplusplus
}
#endif
#endif
