/*
 * com_qpidnetwork_livechat_jni_LiveChatClient.cpp
 *
 *  Created on: 2015-04-14
 *      Author: Samson.Fan
 * Description: 男士端LiveChat JNI接口
 */

#include "com_qpidnetwork_livechat_jni_LiveChatClient.h"
#include <jni.h>
#include <livechat/ILiveChatClient.h>
#include <map>
#include <common/CommonFunc.h>
#include <livechat/JniIntToType.h>	// 处理 jint 转  枚举type
#include <livechat/LiveChatJniCallbackItemDef.h>
#include <common/CheckMemoryLeak.h>
#include <common/KLog.h>

using namespace std;

// -------------- java --------------
static JavaVM* gJavaVM = NULL;

/* java listener object */
static jobject gListener = NULL;

/* java data item */
typedef map<string, jobject> JavaItemMap;
static JavaItemMap gJavaItemMap;

bool GetEnv(JNIEnv** env, bool* isAttachThread);
bool ReleaseEnv(bool isAttachThread);

static ILiveChatClient* g_liveChatClient = NULL;
static string gDeviceId = "";
// -------------- c++ ----------------
string GetJString(JNIEnv* env, jstring str)
{
	string result("");
	if (NULL != str) {
		const char* cpTemp = env->GetStringUTFChars(str, 0);
		result = cpTemp;
		env->ReleaseStringUTFChars(str, cpTemp);
	}
	return result;
}

void InitClassHelper(JNIEnv *env, const char *path, jobject *objptr) {
//	FileLog("httprequest.Gobal.Native", "InitClassHelper( path : %s )", path);
    jclass cls = env->FindClass(path);
    if( !cls ) {
//    	FileLog("httprequest.Gobal.Native", "InitClassHelper( !cls )");
        return;
    }

    jmethodID constr = env->GetMethodID(cls, "<init>", "()V");
    if( !constr ) {
//    	FileLog("httprequest.Gobal.Native", "InitClassHelper( !constr )");
        constr = env->GetMethodID(cls, "<init>", "(Ljava/lang/String;I)V");
        if( !constr ) {
//        	FileLog("httprequest.Gobal.Native", "InitClassHelper( !constr )");
            return;
        }
        return;
    }

    jobject obj = env->NewObject(cls, constr);
    if( !obj ) {
//    	FileLog("max.httprequest", "InitClassHelper( !obj )");
        return;
    }

    (*objptr) = env->NewGlobalRef(obj);
}


void InsertJObjectClassToMap(JNIEnv* env, const char* classPath)
{
	jobject jTempObject;
	InitClassHelper(env, classPath, &jTempObject);
	gJavaItemMap.insert(JavaItemMap::value_type(classPath, jTempObject));
}

jclass GetJObjectClassWithMap(JNIEnv* env, const char* classPath)
{
	jclass cls = NULL;
	JavaItemMap::iterator itr = gJavaItemMap.find(classPath);
	if( itr != gJavaItemMap.end() ) {
		jobject jItemObj = itr->second;
		cls = env->GetObjectClass(jItemObj);
	}
	return cls;
}

jobject GetTalkUserListItem(JNIEnv* env, const TalkUserListItem& item)
{
	jobject jItem = NULL;
	jclass jItemCls = GetJObjectClassWithMap(env, LIVECHAT_TALKUSERLISTTIME_CLASS);
	if (NULL != jItemCls) {
		jmethodID init = env->GetMethodID(jItemCls, "<init>"
				, "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;IILjava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;ZIIIIIIIILjava/lang/String;)V");

		int sexType = UserSexTypeToInt(item.sexType);
		int marryType = MarryTypeToInt(item.marryType);
		int childrenType = ChildrenTypeToInt(item.childrenType);
		int statusType = UserStatusTypeToInt(item.status);
		int userType = UserTypeToInt(item.userType);
		int deviceType = DeviceTypeToInt(item.deviceType);
		int clientType = ClientTypeToInt(item.clientType);
		jstring juserId = env->NewStringUTF(item.userId.c_str());
		jstring juserName = env->NewStringUTF(item.userName.c_str());
		jstring jserver = env->NewStringUTF(item.server.c_str());
		jstring jimgUrl = env->NewStringUTF(item.imgUrl.c_str());
		jstring jweight = env->NewStringUTF(item.weight.c_str());
		jstring jheight = env->NewStringUTF(item.height.c_str());
		jstring jcountry = env->NewStringUTF(item.country.c_str());
		jstring jprovince = env->NewStringUTF(item.province.c_str());
		jstring jclientVersion = env->NewStringUTF(item.clientVersion.c_str());
		jItem = env->NewObject(jItemCls, init,
					juserId,
					juserName,
					jserver,
					jimgUrl,
					sexType,
					item.age,
					jweight,
					jheight,
					jcountry,
					jprovince,
					item.videoChat,
					item.videoCount,
					marryType,
					childrenType,
					statusType,
					userType,
					item.orderValue,
					deviceType,
					clientType,
					jclientVersion
					);
		env->DeleteLocalRef(juserId);
		env->DeleteLocalRef(juserName);
		env->DeleteLocalRef(jserver);
		env->DeleteLocalRef(jimgUrl);
		env->DeleteLocalRef(jweight);
		env->DeleteLocalRef(jheight);
		env->DeleteLocalRef(jcountry);
		env->DeleteLocalRef(jprovince);
		env->DeleteLocalRef(jclientVersion);
	}

	return jItem;
}

jobjectArray GetTalkUserList(JNIEnv* env, const TalkUserList& list)
{
	jobjectArray array = NULL;
	jclass jItemCls = GetJObjectClassWithMap(env, LIVECHAT_TALKUSERLISTTIME_CLASS);
	if (NULL != jItemCls) {
		array = env->NewObjectArray(list.size(), jItemCls, NULL);
		if (NULL != array) {
			int i = 0;
			for(TalkUserList::const_iterator itr = list.begin();
				itr != list.end();
				itr++)
			{
				jobject jItem = GetTalkUserListItem(env, (*itr));
				if (NULL != jItem) {
					env->SetObjectArrayElement(array, i, jItem);
					i++;
				}
				env->DeleteLocalRef(jItem);
			}
		}
	}

	return array;
}

jobjectArray GetUserInfoList(JNIEnv* env, const UserInfoList& list)
{
	jobjectArray array = NULL;
	jclass jItemCls = GetJObjectClassWithMap(env, LIVECHAT_TALKUSERLISTTIME_CLASS);
	if (NULL != jItemCls) {
		array = env->NewObjectArray(list.size(), jItemCls, NULL);
		if (NULL != array) {
			int i = 0;
			for(UserInfoList::const_iterator itr = list.begin();
				itr != list.end();
				itr++)
			{
				jobject jItem = GetTalkUserListItem(env, (*itr));
				if (NULL != jItem) {
					env->SetObjectArrayElement(array, i, jItem);
					i++;
				}
				env->DeleteLocalRef(jItem);
			}
		}
	}

	return array;
}

jobject GetTalkSessionListItem(JNIEnv* env, const TalkSessionListItem& item)
{
	jobject jItem = NULL;
	jclass jItemCls = GetJObjectClassWithMap(env, LIVECHAT_TALKSESSIONLISTTIME_CLASS);
	if (NULL != jItemCls) {
		jmethodID init = env->GetMethodID(jItemCls, "<init>"
				, "(Ljava/lang/String;Ljava/lang/String;ZI)V");

		jstring jtargetId = env->NewStringUTF(item.targetId.c_str());
		jstring jinvitedId = env->NewStringUTF(item.invitedId.c_str());
		jItem = env->NewObject(jItemCls, init,
					jtargetId,
					jinvitedId,
					item.charget,
					item.chatTime
					);
		env->DeleteLocalRef(jtargetId);
		env->DeleteLocalRef(jinvitedId);
	}

	return jItem;
}

jobjectArray GetTalkSessionList(JNIEnv* env, const TalkSessionList& list)
{
	jobjectArray array = NULL;
	jclass jItemCls = GetJObjectClassWithMap(env, LIVECHAT_TALKSESSIONLISTTIME_CLASS);
	if (NULL != jItemCls) {
		array = env->NewObjectArray(list.size(), jItemCls, NULL);
		if (NULL != array) {
			int i = 0;
			for(TalkSessionList::const_iterator itr = list.begin();
				itr != list.end();
				itr++)
			{
				jobject jItem = GetTalkSessionListItem(env, (*itr));
				if (NULL != jItem) {
					env->SetObjectArrayElement(array, i, jItem);
					i++;
				}
				env->DeleteLocalRef(jItem);
			}
		}
	}

	return array;
}

class LiveChatClientListener : public ILiveChatClientListener {
public:
	LiveChatClientListener() {};
	virtual ~LiveChatClientListener() {};

public:
	// 客户端主动请求
	// 回调函数的参数在 err 之前的为请求参数，在 errmsg 之后为返回参数
	virtual void OnLogin(LCC_ERR_TYPE err, const string& errmsg) {
		JNIEnv* env = NULL;
		bool isAttachThread = false;
		GetEnv(&env, &isAttachThread);

		FileLog("LiveChatClient", "OnLogin() callback, env:%p, isAttachThread:%d", env, isAttachThread);

		jclass jCallbackCls = env->GetObjectClass(gListener);

		string signure = "(ILjava/lang/String;)V";
		jmethodID jCallback = env->GetMethodID(jCallbackCls, "OnLogin", signure.c_str());
		if (NULL != gListener && NULL != jCallback)
		{
			FileLog("LiveChatClient", "OnLogin() callback now");

			int errType = LccErrTypeToInt(err);
			jstring jerrmsg = env->NewStringUTF(errmsg.c_str());
			env->CallVoidMethod(gListener, jCallback, errType, jerrmsg);
			env->DeleteLocalRef(jerrmsg);

			FileLog("LiveChatClient", "OnLogin() callback ok");
		}

		ReleaseEnv(isAttachThread);
	}
	virtual void OnLogout(LCC_ERR_TYPE err, const string& errmsg) {
		JNIEnv* env = NULL;
		bool isAttachThread = false;
		GetEnv(&env, &isAttachThread);

		FileLog("LiveChatClient", "OnLogout() callback begin, gListener:%p, env:%p, isAttachThread:%d", gListener, env, isAttachThread);

		jclass jCallbackCls = env->GetObjectClass(gListener);

		FileLog("LiveChatClient", "OnLogout() callback jCallbackCls:%p", jCallbackCls);

		string signure = "(ILjava/lang/String;)V";
		jmethodID jCallback = env->GetMethodID(jCallbackCls, "OnLogout", signure.c_str());

		FileLog("LiveChatClient", "OnLogout() callback jCallback:%p, signure:%s", jCallbackCls, signure.c_str());

		if (NULL != gListener && NULL != jCallback)
		{
			FileLog("LiveChatClient", "OnLogout() callback now");

			int errType = LccErrTypeToInt(err);
			jstring jerrmsg = env->NewStringUTF(errmsg.c_str());
			env->CallVoidMethod(gListener, jCallback, errType, jerrmsg);
			env->DeleteLocalRef(jerrmsg);

			FileLog("LiveChatClient", "OnLogout() callback ok");
		}

		ReleaseEnv(isAttachThread);

		FileLog("LiveChatClient", "OnLogout() callback end");
	}
	virtual void OnSetStatus(LCC_ERR_TYPE err, const string& errmsg) {
		JNIEnv* env = NULL;
		bool isAttachThread = false;
		GetEnv(&env, &isAttachThread);

		FileLog("LiveChatClient", "OnSetStatus() callback, env:%p, isAttachThread:%d", env, isAttachThread);

		jclass jCallbackCls = env->GetObjectClass(gListener);

		string signure = "(ILjava/lang/String;)V";
		jmethodID jCallback = env->GetMethodID(jCallbackCls, "OnSetStatus", signure.c_str());
		if (NULL != gListener && NULL != jCallback)
		{
			FileLog("LiveChatClient", "OnSetStatus() callback now");

			int errType = LccErrTypeToInt(err);
			jstring jerrmsg = env->NewStringUTF(errmsg.c_str());
			env->CallVoidMethod(gListener, jCallback, errType, jerrmsg);
			env->DeleteLocalRef(jerrmsg);

			FileLog("LiveChatClient", "OnSetStatus() callback ok");
		}

		ReleaseEnv(isAttachThread);
	}
	virtual void OnEndTalk(const string& inUserId, LCC_ERR_TYPE err, const string& errmsg) {
		JNIEnv* env = NULL;
		bool isAttachThread = false;
		GetEnv(&env, &isAttachThread);

		FileLog("LiveChatClient", "OnEndTalk() callback, env:%p, isAttachThread:%d", env, isAttachThread);

		jclass jCallbackCls = env->GetObjectClass(gListener);

		string signure = "(ILjava/lang/String;Ljava/lang/String;)V";
		jmethodID jCallback = env->GetMethodID(jCallbackCls, "OnEndTalk", signure.c_str());
		if (NULL != gListener && NULL != jCallback)
		{
			FileLog("LiveChatClient", "OnEndTalk() callback now");

			int errType = LccErrTypeToInt(err);
			jstring jerrmsg = env->NewStringUTF(errmsg.c_str());
			jstring juserId = env->NewStringUTF(inUserId.c_str());
			env->CallVoidMethod(gListener, jCallback, errType, jerrmsg, juserId);
			env->DeleteLocalRef(juserId);
			env->DeleteLocalRef(jerrmsg);

			FileLog("LiveChatClient", "OnEndTalk() callback ok");
		}

		ReleaseEnv(isAttachThread);
	}
	virtual void OnGetUserStatus(const UserIdList& inList, LCC_ERR_TYPE err, const string& errmsg, const UserStatusList& list) {
		JNIEnv* env = NULL;
		bool isAttachThread = false;
		GetEnv(&env, &isAttachThread);

		FileLog("LiveChatClient", "OnGetUserStatus() callback, env:%p, isAttachThread:%d", env, isAttachThread);

		jobjectArray jItemArray = NULL;
		jclass jItemCls = GetJObjectClassWithMap(env, LIVECHAT_USERSTATUS_CLASS);
		if (NULL != jItemCls) {
			jItemArray = env->NewObjectArray(list.size(), jItemCls, NULL);
			if (NULL != jItemArray) {
				int i = 0;
				for(UserStatusList::const_iterator itr = list.begin();
					itr != list.end();
					itr++)
				{
					jmethodID init = env->GetMethodID(jItemCls, "<init>", "("
							"Ljava/lang/String;"	// statusType
							"I"						// userId
							")V");

					int statusType = UserStatusTypeToInt(itr->statusType);
					jstring juserId = env->NewStringUTF(itr->userId.c_str());
					jobject jItem = env->NewObject(jItemCls, init,
							juserId,
							statusType
							);
					env->DeleteLocalRef(juserId);
					env->SetObjectArrayElement(jItemArray, i, jItem);
					env->DeleteLocalRef(jItem);

					i++;
				}
			}
		}
		FileLog("LiveChatClient", "OnGetUserStatus() create array ok");

		jclass jCallbackCls = env->GetObjectClass(gListener);
		string signure = "(ILjava/lang/String;";
		signure += "[L";
		signure += LIVECHAT_USERSTATUS_CLASS;
		signure += ";)V";
		jmethodID jCallback = env->GetMethodID(jCallbackCls, "OnGetUserStatus", signure.c_str());
		if (NULL != gListener && NULL != jCallback)
		{
			FileLog("LiveChatClient", "OnGetUserStatus() callback now");

			int errType = LccErrTypeToInt(err);
			jstring jerrmsg = env->NewStringUTF(errmsg.c_str());
			env->CallVoidMethod(gListener, jCallback, errType, jerrmsg, jItemArray);
			env->DeleteLocalRef(jerrmsg);

			FileLog("LiveChatClient", "OnGetUserStatus() callback ok");
		}

		if (NULL != jItemArray) {
			env->DeleteLocalRef(jItemArray);
		}

		ReleaseEnv(isAttachThread);
	}
	virtual void OnGetTalkInfo(const string& inUserId, LCC_ERR_TYPE err, const string& errmsg, const string& userId, const string& invitedId, bool charge, unsigned int chatTime) {
		JNIEnv* env = NULL;
		bool isAttachThread = false;
		GetEnv(&env, &isAttachThread);

		FileLog("LiveChatClient", "OnGetTalkInfo() callback, env:%p, isAttachThread:%d", env, isAttachThread);

		jclass jCallbackCls = env->GetObjectClass(gListener);
		string signure = "(ILjava/lang/String;Ljava/lang/String;Ljava/lang/String;ZI)V";
		jmethodID jCallback = env->GetMethodID(jCallbackCls, "OnGetTalkInfo", signure.c_str());
		if (NULL != gListener && NULL != jCallback)
		{
			FileLog("LiveChatClient", "OnGetTalkInfo() callback now");

			int errType = LccErrTypeToInt(err);
			jstring jerrmsg = env->NewStringUTF(errmsg.c_str());
			jstring juserId = env->NewStringUTF(userId.c_str());
			jstring jinvitedId = env->NewStringUTF(invitedId.c_str());

			env->CallVoidMethod(gListener, jCallback, errType, jerrmsg, juserId, jinvitedId, charge, chatTime);

			env->DeleteLocalRef(jinvitedId);
			env->DeleteLocalRef(juserId);
			env->DeleteLocalRef(jerrmsg);

			FileLog("LiveChatClient", "OnGetTalkInfo() callback ok");
		}

		ReleaseEnv(isAttachThread);
	}
	virtual void OnSendTextMessage(const string& inUserId, const string& inMessage, int inTicket, LCC_ERR_TYPE err, const string& errmsg) {
		JNIEnv* env = NULL;
		bool isAttachThread = false;
		GetEnv(&env, &isAttachThread);

		FileLog("LiveChatClient", "OnSendTextMessage() callback, env:%p, isAttachThread:%d", env, isAttachThread);

		jclass jCallbackCls = env->GetObjectClass(gListener);
		string signure = "(ILjava/lang/String;Ljava/lang/String;Ljava/lang/String;I)V";
		jmethodID jCallback = env->GetMethodID(jCallbackCls, "OnSendMessage", signure.c_str());
		if (NULL != gListener && NULL != jCallback)
		{
			FileLog("LiveChatClient", "OnSendTextMessage() callback now");

			int errType = LccErrTypeToInt(err);
			jstring jerrmsg = env->NewStringUTF(errmsg.c_str());
			jstring juserId = env->NewStringUTF(inUserId.c_str());
			jstring jmessage = env->NewStringUTF(inMessage.c_str());

			env->CallVoidMethod(gListener, jCallback, errType, jerrmsg, juserId, jmessage, inTicket);

			env->DeleteLocalRef(jmessage);
			env->DeleteLocalRef(juserId);
			env->DeleteLocalRef(jerrmsg);

			FileLog("LiveChatClient", "OnSendTextMessage() callback ok");
		}

		ReleaseEnv(isAttachThread);
	}
	virtual void OnSendEmotion(const string& inUserId, const string& inEmotionId, int inTicket, LCC_ERR_TYPE err, const string& errmsg) {
		JNIEnv* env = NULL;
		bool isAttachThread = false;
		GetEnv(&env, &isAttachThread);

		FileLog("LiveChatClient", "OnSendEmotion() callback, env:%p, isAttachThread:%d", env, isAttachThread);

		jclass jCallbackCls = env->GetObjectClass(gListener);
		string signure = "(ILjava/lang/String;Ljava/lang/String;Ljava/lang/String;I)V";
		jmethodID jCallback = env->GetMethodID(jCallbackCls, "OnSendEmotion", signure.c_str());
		if (NULL != gListener && NULL != jCallback)
		{
			FileLog("LiveChatClient", "OnSendEmotion() callback now");

			int errType = LccErrTypeToInt(err);
			jstring jerrmsg = env->NewStringUTF(errmsg.c_str());
			jstring juserId = env->NewStringUTF(inUserId.c_str());
			jstring jemotionId = env->NewStringUTF(inEmotionId.c_str());

			env->CallVoidMethod(gListener, jCallback, errType, jerrmsg, juserId, jemotionId, inTicket);

			env->DeleteLocalRef(jemotionId);
			env->DeleteLocalRef(juserId);
			env->DeleteLocalRef(jerrmsg);

			FileLog("LiveChatClient", "OnSendEmotion() callback ok");
		}

		ReleaseEnv(isAttachThread);
	}
	virtual void OnSendVGift(const string& inUserId, const string& inGiftId, int ticket, LCC_ERR_TYPE err, const string& errmsg) {
		JNIEnv* env = NULL;
		bool isAttachThread = false;
		GetEnv(&env, &isAttachThread);

		FileLog("LiveChatClient", "OnSendVGift() callback, env:%p, isAttachThread:%d", env, isAttachThread);

		jclass jCallbackCls = env->GetObjectClass(gListener);
		string signure = "(ILjava/lang/String;Ljava/lang/String;Ljava/lang/String;I)V";
		jmethodID jCallback = env->GetMethodID(jCallbackCls, "OnSendVGift", signure.c_str());
		if (NULL != gListener && NULL != jCallback)
		{
			FileLog("LiveChatClient", "OnSendVGift() callback now");

			int errType = LccErrTypeToInt(err);
			jstring jerrmsg = env->NewStringUTF(errmsg.c_str());
			jstring juserId = env->NewStringUTF(inUserId.c_str());
			jstring jgiftId = env->NewStringUTF(inGiftId.c_str());

			env->CallVoidMethod(gListener, jCallback, errType, jerrmsg, juserId, jgiftId, ticket);

			env->DeleteLocalRef(jgiftId);
			env->DeleteLocalRef(juserId);
			env->DeleteLocalRef(jerrmsg);

			FileLog("LiveChatClient", "OnSendVGift() callback ok");
		}

		ReleaseEnv(isAttachThread);
	}
	virtual void OnGetVoiceCode(const string& inUserId, int ticket, LCC_ERR_TYPE err, const string& errmsg, const string& voiceCode) {
		JNIEnv* env = NULL;
		bool isAttachThread = false;
		GetEnv(&env, &isAttachThread);

		FileLog("LiveChatClient", "OnGetVoiceCode() callback, env:%p, isAttachThread:%d", env, isAttachThread);

		jclass jCallbackCls = env->GetObjectClass(gListener);
		string signure = "(ILjava/lang/String;Ljava/lang/String;ILjava/lang/String;)V";
		jmethodID jCallback = env->GetMethodID(jCallbackCls, "OnGetVoiceCode", signure.c_str());
		if (NULL != gListener && NULL != jCallback)
		{
			FileLog("LiveChatClient", "OnGetVoiceCode() callback now");

			int errType = LccErrTypeToInt(err);
			jstring jerrmsg = env->NewStringUTF(errmsg.c_str());
			jstring juserId = env->NewStringUTF(inUserId.c_str());
			jstring jvoiceCode = env->NewStringUTF(voiceCode.c_str());

			env->CallVoidMethod(gListener, jCallback, errType, jerrmsg, juserId, ticket, jvoiceCode);

			env->DeleteLocalRef(jvoiceCode);
			env->DeleteLocalRef(juserId);
			env->DeleteLocalRef(jerrmsg);

			FileLog("LiveChatClient", "OnGetVoiceCode() callback ok");
		}

		ReleaseEnv(isAttachThread);
	}
	virtual void OnSendVoice(const string& inUserId, const string& inVoiceId, int ticket, LCC_ERR_TYPE err, const string& errmsg) {
		JNIEnv* env = NULL;
		bool isAttachThread = false;
		GetEnv(&env, &isAttachThread);

		FileLog("LiveChatClient", "OnSendVoice() callback, env:%p, isAttachThread:%d", env, isAttachThread);

		jclass jCallbackCls = env->GetObjectClass(gListener);
		string signure = "(ILjava/lang/String;Ljava/lang/String;Ljava/lang/String;I)V";
		jmethodID jCallback = env->GetMethodID(jCallbackCls, "OnSendVoice", signure.c_str());
		if (NULL != gListener && NULL != jCallback)
		{
			FileLog("LiveChatClient", "OnSendVoice() callback now");

			int errType = LccErrTypeToInt(err);
			jstring jerrmsg = env->NewStringUTF(errmsg.c_str());
			jstring juserId = env->NewStringUTF(inUserId.c_str());
			jstring jvoiceId = env->NewStringUTF(inVoiceId.c_str());

			env->CallVoidMethod(gListener, jCallback, errType, jerrmsg, juserId, jvoiceId, ticket);

			env->DeleteLocalRef(jvoiceId);
			env->DeleteLocalRef(juserId);
			env->DeleteLocalRef(jerrmsg);

			FileLog("LiveChatClient", "OnSendVoice() callback ok");
		}

		ReleaseEnv(isAttachThread);
	}
	virtual void OnUseTryTicket(const string& inUserId, LCC_ERR_TYPE err, const string& errmsg, const string& userId, TRY_TICKET_EVENT tickEvent) {
		JNIEnv* env = NULL;
		bool isAttachThread = false;
		GetEnv(&env, &isAttachThread);

		FileLog("LiveChatClient", "OnUseTryTicket() callback, env:%p, isAttachThread:%d", env, isAttachThread);

		jclass jCallbackCls = env->GetObjectClass(gListener);
		string signure = "(ILjava/lang/String;Ljava/lang/String;I)V";
		jmethodID jCallback = env->GetMethodID(jCallbackCls, "OnUseTryTicket", signure.c_str());
		if (NULL != gListener && NULL != jCallback)
		{
			FileLog("LiveChatClient", "OnUseTryTicket() callback now");

			int errType = LccErrTypeToInt(err);
			jstring jerrmsg = env->NewStringUTF(errmsg.c_str());
			jstring juserId = env->NewStringUTF(inUserId.c_str());
			int eventType = TryTicketEventTypeToInt(tickEvent);

			env->CallVoidMethod(gListener, jCallback, errType, jerrmsg, juserId, eventType);

			env->DeleteLocalRef(juserId);
			env->DeleteLocalRef(jerrmsg);

			FileLog("LiveChatClient", "OnUseTryTicket() callback ok");
		}

		ReleaseEnv(isAttachThread);
	}
	virtual void OnGetTalkList(int inListType, LCC_ERR_TYPE err, const string& errmsg, const TalkListInfo& talkListInfo) {
		JNIEnv* env = NULL;
		bool isAttachThread = false;
		GetEnv(&env, &isAttachThread);

		FileLog("LiveChatClient", "OnGetTalkList() err:%d, errmsg:%s, env:%p, isAttachThread:%d", err, errmsg.c_str(), env, isAttachThread);

		// invite列表
		jobjectArray jInviteArray = GetTalkUserList(env, talkListInfo.invite);
		// inviteSession列表
		jobjectArray jInviteSessionArray = GetTalkSessionList(env, talkListInfo.inviteSession);
		// invited列表
		jobjectArray jInvitedArray = GetTalkUserList(env, talkListInfo.invited);
		// inivtedSession列表
		jobjectArray jInvitedSessionArray = GetTalkSessionList(env, talkListInfo.invitedSession);
		// chating列表
		jobjectArray jChatingArray = GetTalkUserList(env, talkListInfo.chating);
		// chatingSession列表
		jobjectArray jChatingSessionArray = GetTalkSessionList(env, talkListInfo.chatingSession);
		// pause列表
		jobjectArray jPauseArray = GetTalkUserList(env, talkListInfo.pause);
		// pauseSession列表
		jobjectArray jPauseSessionArray = GetTalkSessionList(env, talkListInfo.pauseSession);

		FileLog("LiveChatClient", "OnGetTalkList() create array ok");

		// Create info
		jobject jTalkListInfoItem = NULL;
		jclass jTalkListInfoCls = GetJObjectClassWithMap(env, LIVECHAT_TALKLISTINFO_CLASS);
		if (NULL != jTalkListInfoCls) {
			string initSig = "(";
			// Invite
			initSig += "[L";
			initSig += LIVECHAT_TALKUSERLISTTIME_CLASS;
			initSig += ";";
			initSig += "[L";
			initSig += LIVECHAT_TALKSESSIONLISTTIME_CLASS;
			initSig += ";";
			// Invited
			initSig += "[L";
			initSig += LIVECHAT_TALKUSERLISTTIME_CLASS;
			initSig += ";";
			initSig += "[L";
			initSig += LIVECHAT_TALKSESSIONLISTTIME_CLASS;
			initSig += ";";
			// chating
			initSig += "[L";
			initSig += LIVECHAT_TALKUSERLISTTIME_CLASS;
			initSig += ";";
			initSig += "[L";
			initSig += LIVECHAT_TALKSESSIONLISTTIME_CLASS;
			initSig += ";";
			// pause
			initSig += "[L";
			initSig += LIVECHAT_TALKUSERLISTTIME_CLASS;
			initSig += ";";
			initSig += "[L";
			initSig += LIVECHAT_TALKSESSIONLISTTIME_CLASS;
			initSig += ";";
			// end
			initSig += ")V";

			jmethodID init = env->GetMethodID(jTalkListInfoCls, "<init>"
							, initSig.c_str());

			jTalkListInfoItem = env->NewObject(jTalkListInfoCls, init,
									jInviteArray,
									jInviteSessionArray,
									jInvitedArray,
									jInvitedSessionArray,
									jChatingArray,
									jChatingSessionArray,
									jPauseArray,
									jPauseSessionArray
									);
		}

		FileLog("LiveChatClient", "OnGetTalkList() create info ok");

		// 释放列表
		if (NULL != jInviteArray) {
			env->DeleteLocalRef(jInviteArray);
		}
		if (NULL != jInviteSessionArray) {
			env->DeleteLocalRef(jInviteSessionArray);
		}
		if (NULL != jInvitedArray) {
			env->DeleteLocalRef(jInvitedArray);
		}
		if (NULL != jInvitedSessionArray) {
			env->DeleteLocalRef(jInvitedSessionArray);
		}
		if (NULL != jChatingArray) {
			env->DeleteLocalRef(jChatingArray);
		}
		if (NULL != jChatingSessionArray) {
			env->DeleteLocalRef(jChatingSessionArray);
		}
		if (NULL != jPauseArray) {
			env->DeleteLocalRef(jPauseArray);
		}
		if (NULL != jPauseSessionArray) {
			env->DeleteLocalRef(jPauseSessionArray);
		}

		jclass jCallbackCls = env->GetObjectClass(gListener);
		string signure = "(ILjava/lang/String;I";
		signure += "L";
		signure += LIVECHAT_TALKLISTINFO_CLASS;
		signure += ";)V";
		jmethodID jCallback = env->GetMethodID(jCallbackCls, "OnGetTalkList", signure.c_str());
		if (NULL != gListener && NULL != jCallback)
		{
			FileLog("LiveChatClient", "OnGetTalkList() callback now");

			int errType = LccErrTypeToInt(err);
			jstring jerrmsg = env->NewStringUTF(errmsg.c_str());
			env->CallVoidMethod(gListener, jCallback, errType, jerrmsg, inListType, jTalkListInfoItem);
			env->DeleteLocalRef(jerrmsg);

			FileLog("LiveChatClient", "OnGetTalkList() callback ok");
		}

		if (NULL != jTalkListInfoItem) {
			env->DeleteLocalRef(jTalkListInfoItem);
		}

		ReleaseEnv(isAttachThread);
	}
	virtual void OnSendPhoto(LCC_ERR_TYPE err, const string& errmsg, int ticket) {
		JNIEnv* env = NULL;
		bool isAttachThread = false;
		GetEnv(&env, &isAttachThread);

		FileLog("LiveChatClient", "OnSendPhoto() callback, env:%p, isAttachThread:%d", env, isAttachThread);

		jclass jCallbackCls = env->GetObjectClass(gListener);
		string signure = "(ILjava/lang/String;I)V";
		jmethodID jCallback = env->GetMethodID(jCallbackCls, "OnSendPhoto", signure.c_str());
		if (NULL != gListener && NULL != jCallback)
		{
			FileLog("LiveChatClient", "OnSendPhoto() callback now");

			int errType = LccErrTypeToInt(err);
			jstring jerrmsg = env->NewStringUTF(errmsg.c_str());

			env->CallVoidMethod(gListener, jCallback, errType, jerrmsg, ticket);

			env->DeleteLocalRef(jerrmsg);

			FileLog("LiveChatClient", "OnSendPhoto() callback ok");
		}

		ReleaseEnv(isAttachThread);
	}
	virtual void OnSendLadyPhoto(LCC_ERR_TYPE err, const string& errmsg, int ticket) {

	}
	virtual void OnShowPhoto(LCC_ERR_TYPE err, const string& errmsg, int ticket) 
	{
		JNIEnv* env = NULL;
		bool isAttachThread = false;
		GetEnv(&env, &isAttachThread);

		FileLog("LiveChatClient", "OnShowPhoto() callback, env:%p, isAttachThread:%d", env, isAttachThread);

		jclass jCallbackCls = env->GetObjectClass(gListener);
		string signure = "(ILjava/lang/String;I)V";
		jmethodID jCallback = env->GetMethodID(jCallbackCls, "OnShowPhoto", signure.c_str());
		if (NULL != gListener && NULL != jCallback)
		{
			FileLog("LiveChatClient", "OnShowPhoto() callback now");

			int errType = LccErrTypeToInt(err);
			jstring jerrmsg = env->NewStringUTF(errmsg.c_str());

			env->CallVoidMethod(gListener, jCallback, errType, jerrmsg, ticket);

			env->DeleteLocalRef(jerrmsg);

			FileLog("LiveChatClient", "OnShowPhoto() callback ok");
		}

		ReleaseEnv(isAttachThread);
	}
	virtual void OnPlayVideo(LCC_ERR_TYPE err, const string& errmsg, int ticket)
	{
		JNIEnv* env = NULL;
		bool isAttachThread = false;
		GetEnv(&env, &isAttachThread);

		FileLog("LiveChatClient", "OnPlayVideo() callback, env:%p, isAttachThread:%d", env, isAttachThread);

		jclass jCallbackCls = env->GetObjectClass(gListener);
		string signure = "(ILjava/lang/String;I)V";
		jmethodID jCallback = env->GetMethodID(jCallbackCls, "OnPlayVideo", signure.c_str());
		if (NULL != gListener && NULL != jCallback)
		{
			FileLog("LiveChatClient", "OnPlayVideo() callback now");

			int errType = LccErrTypeToInt(err);
			jstring jerrmsg = env->NewStringUTF(errmsg.c_str());

			env->CallVoidMethod(gListener, jCallback, errType, jerrmsg, ticket);

			env->DeleteLocalRef(jerrmsg);

			FileLog("LiveChatClient", "OnPlayVideo() callback ok");
		}

		ReleaseEnv(isAttachThread);
	}
	virtual void OnSendLadyVideo(LCC_ERR_TYPE err, const string& errmsg, int ticket)
	{

	}
	virtual void OnGetUserInfo(const string& inUserId, LCC_ERR_TYPE err, const string& errmsg, const UserInfoItem& item)
	{
		JNIEnv* env = NULL;
		bool isAttachThread = false;
		GetEnv(&env, &isAttachThread);

		FileLog("LiveChatClient", "OnGetUserInfo() callback, inUserId:%s", inUserId.c_str());
		jobject jItem = GetTalkUserListItem(env, item);

		jclass jCallbackCls = env->GetObjectClass(gListener);
		string signure = "(ILjava/lang/String;Ljava/lang/String;";
		signure += "L";
		signure += LIVECHAT_TALKUSERLISTTIME_CLASS;
		signure += ";";
		signure += ")V";
		jmethodID jCallback = env->GetMethodID(jCallbackCls, "OnGetUserInfo", signure.c_str());
		if (NULL != gListener && NULL != jCallback)
		{
			FileLog("LiveChatClient", "OnGetUserInfo() callback now");

			jstring jInUserId = env->NewStringUTF(inUserId.c_str());
			int errType = LccErrTypeToInt(err);
			jstring jerrmsg = env->NewStringUTF(errmsg.c_str());

			env->CallVoidMethod(gListener, jCallback, errType, jerrmsg, jInUserId, jItem);

			env->DeleteLocalRef(jInUserId);
			env->DeleteLocalRef(jerrmsg);

			FileLog("LiveChatClient", "OnGetUserInfo() callback ok");
		}

		if (NULL != jItem) {
			env->DeleteLocalRef(jItem);
		}

		ReleaseEnv(isAttachThread);
	}
	virtual void OnGetUsersInfo(LCC_ERR_TYPE err, const string& errmsg, int seq, const UserInfoList& userList)
	{
		JNIEnv* env = NULL;
		bool isAttachThread = false;
		GetEnv(&env, &isAttachThread);

		/*UserInfo list*/
		FileLog("LiveChatClient", "OnGetUsersInfo() callback, begin");
		jobjectArray juserInfoList = GetUserInfoList(env, userList);

		/*callback*/
		jclass jCallbackCls = env->GetObjectClass(gListener);
		string signure = "(ILjava/lang/String;";
		signure += "[L";
		signure += LIVECHAT_TALKUSERLISTTIME_CLASS;
		signure += ";";
		signure += ")V";
		jmethodID jCallback = env->GetMethodID(jCallbackCls, "OnGetUsersInfo", signure.c_str());
		if (NULL != gListener && NULL != jCallback)
		{
			FileLog("LiveChatClient", "OnGetUsersInfo() callback now");

			int errType = LccErrTypeToInt(err);
			jstring jerrmsg = env->NewStringUTF(errmsg.c_str());

			env->CallVoidMethod(gListener, jCallback, errType, jerrmsg, juserInfoList);

			env->DeleteLocalRef(jerrmsg);

			FileLog("LiveChatClient", "OnGetUsersInfo() callback ok");
		}

		if (NULL != juserInfoList) {
			env->DeleteLocalRef(juserInfoList);
		}

		ReleaseEnv(isAttachThread);
	}
	// 获取联系人/黑名单列表
	virtual void OnGetContactList(CONTACT_LIST_TYPE inListType, LCC_ERR_TYPE err, const string& errmsg, const TalkUserList& list)
	{
		JNIEnv* env = NULL;
		bool isAttachThread = false;
		GetEnv(&env, &isAttachThread);

		FileLog("LiveChatClient", "OnGetContactList() err:%d, errmsg:%s, env:%p, isAttachThread:%d", err, errmsg.c_str(), env, isAttachThread);

		jobjectArray jListArray = GetTalkUserList(env, list);

		jclass jCallbackCls = env->GetObjectClass(gListener);
		string signure = "(ILjava/lang/String;";
		signure += "[L";
		signure += LIVECHAT_TALKUSERLISTTIME_CLASS;
		signure += ";";
		signure += ")V";

		string strMethod("");
		if (inListType == CONTACT_LIST_CONTACT) {
			strMethod = "OnGetContactList";
		}
		else if (inListType == CONTACT_LIST_BLOCK) {
			strMethod = "OnGetBlockList";
		}

		jmethodID jCallback = env->GetMethodID(jCallbackCls, strMethod.c_str(), signure.c_str());
		if (NULL != gListener && NULL != jCallback)
		{
			FileLog("LiveChatClient", "OnGetContactList() callback now");

			int errType = LccErrTypeToInt(err);
			jstring jerrmsg = env->NewStringUTF(errmsg.c_str());

			env->CallVoidMethod(gListener, jCallback, errType, jerrmsg, jListArray);

			env->DeleteLocalRef(jerrmsg);

			FileLog("LiveChatClient", "OnGetContactList() callback ok");
		}

		if (NULL != jListArray) {
			env->DeleteLocalRef(jListArray);
		}

		ReleaseEnv(isAttachThread);
	}
	// 获取被屏蔽女士列表
	virtual void OnGetBlockUsers(LCC_ERR_TYPE err, const string& errmsg, const list<string>& users)
	{
		JNIEnv* env = NULL;
		bool isAttachThread = false;
		GetEnv(&env, &isAttachThread);

		FileLog("LiveChatClient", "OnGetBlockUsers() err:%d, errmsg:%s, env:%p, isAttachThread:%d", err, errmsg.c_str(), env, isAttachThread);

		jclass jStringCls = env->FindClass("java/lang/String");
		jobjectArray jArray = env->NewObjectArray(users.size(), jStringCls, NULL);
		if (NULL != jArray) {
			int i = 0;
			for (list<string>::const_iterator itr = users.begin()
				; itr != users.end()
				; itr++, i++)
			{
				jstring userId = env->NewStringUTF((*itr).c_str());
				env->SetObjectArrayElement(jArray, i, userId);
				env->DeleteLocalRef(userId);
			}
		}

		jclass jCallbackCls = env->GetObjectClass(gListener);
		string signure = "(ILjava/lang/String;[Ljava/lang/String;)V";

		jmethodID jCallback = env->GetMethodID(jCallbackCls, "OnGetBlockUsers", signure.c_str());
		if (NULL != gListener && NULL != jCallback)
		{
			FileLog("LiveChatClient", "OnGetBlockUsers() callback now");

			int errType = LccErrTypeToInt(err);
			jstring jerrmsg = env->NewStringUTF(errmsg.c_str());

			env->CallVoidMethod(gListener, jCallback, errType, jerrmsg, jArray);

			env->DeleteLocalRef(jerrmsg);

			FileLog("LiveChatClient", "OnGetBlockUsers() callback ok");
		}

		if (NULL != jArray) {
			env->DeleteLocalRef(jArray);
		}

		ReleaseEnv(isAttachThread);
	}

	virtual void OnSearchOnlineMan(LCC_ERR_TYPE err, const string& errmsg, const list<string>& userList)
	{
		// 仅女士端
	}

	virtual void OnReplyIdentifyCode(LCC_ERR_TYPE err, const string& errmsg)
	{
		// 仅女士端
	}

	virtual void OnGetRecentContactList(LCC_ERR_TYPE err, const string& errmsg, const list<string>& userList)
	{
		// 仅女士端
	}

	virtual void OnGetFeeRecentContactList(LCC_ERR_TYPE err, const string& errmsg, const list<string>& userList)
	{
		// 仅女士端
	}

	virtual void OnGetLadyChatInfo(LCC_ERR_TYPE err, const string& errmsg, const list<string>& chattingList, const list<string>& chattingInviteIdList, const list<string>& missingList, const list<string>& missingInviteIdList)
	{
		// 仅女士端
	}
	virtual void OnGetLadyCondition(const string& inUserId, LCC_ERR_TYPE err, const string& errmsg, const LadyConditionItem& item)
	{
		JNIEnv* env = NULL;
		bool isAttachThread = false;
		GetEnv(&env, &isAttachThread);

		FileLog("LiveChatClient", "OnGetLadyCondition() err:%d, errmsg:%s, env:%p, isAttachThread:%d", err, errmsg.c_str(), env, isAttachThread);

		jobject jLadyCondition = NULL;
		jclass jLadyConditionCls = GetJObjectClassWithMap(env, LIVECHAT_LADYCONDITION_CLASS);
		if (NULL != jLadyConditionCls) {
			jmethodID init = env->GetMethodID(jLadyConditionCls, "<init>"
					, "(Ljava/lang/String;ZZZZZZZZZZZZZZZZII)V");

			jstring jwomanId = env->NewStringUTF(item.womanId.c_str());
			jLadyCondition = env->NewObject(jLadyConditionCls, init
						, jwomanId
						, item.marriageCondition	// 是否判断婚姻状况
						, item.neverMarried				// 是否从未结婚
						, item.divorced					// 是否离婚
						, item.widowed					// 是否丧偶
						, item.separated					// 是否分居
						, item.married					// 是否已婚
						, item.childCondition			// 是否判断子女状况
						, item.noChild					// 是否没有子女
						, item.countryCondition		// 是否判断国籍
						, item.unitedstates				// 是否美国国籍
						, item.canada						// 是否加拿大国籍
						, item.newzealand					// 是否新西兰国籍
						, item.australia					// 是否澳大利亚国籍
						, item.unitedkingdom				// 是否英国国籍
						, item.germany					// 是否德国国籍
						, item.othercountries				// 是否其它国籍
						, item.startAge					// 起始年龄
						, item.endAge						// 结束年龄
						);
			env->DeleteLocalRef(jwomanId);
		}

		jclass jCallbackCls = env->GetObjectClass(gListener);
		string signure = "(ILjava/lang/String;Ljava/lang/String;";
		signure += "L";
		signure += LIVECHAT_LADYCONDITION_CLASS;
		signure += ";";
		signure += ")V";

		jmethodID jCallback = env->GetMethodID(jCallbackCls, "OnGetLadyCondition", signure.c_str());
		if (NULL != gListener && NULL != jCallback)
		{
			FileLog("LiveChatClient", "OnGetLadyCondition() callback now");

			int errType = LccErrTypeToInt(err);
			jstring jerrmsg = env->NewStringUTF(errmsg.c_str());
			jstring jwomanId = env->NewStringUTF(inUserId.c_str());

			env->CallVoidMethod(gListener, jCallback, errType, jerrmsg, jwomanId, jLadyCondition);

			env->DeleteLocalRef(jwomanId);
			env->DeleteLocalRef(jerrmsg);

			FileLog("LiveChatClient", "OnGetLadyCondition() callback ok");
		}

		if (NULL != jLadyCondition) {
			env->DeleteLocalRef(jLadyCondition);
		}

		ReleaseEnv(isAttachThread);
	}
	virtual void OnGetLadyCustomTemplate(const string& inUserId, LCC_ERR_TYPE err, const string& errmsg, const vector<string>& contents, const vector<bool>& flags)
	{
		JNIEnv* env = NULL;
		bool isAttachThread = false;
		GetEnv(&env, &isAttachThread);

		FileLog("LiveChatClient", "OnGetLadyCustomTemplate() err:%d, errmsg:%s, env:%p, isAttachThread:%d", err, errmsg.c_str(), env, isAttachThread);

		jclass jStringCls = env->FindClass("java/lang/String");
		jobjectArray jContentArray = env->NewObjectArray(contents.size(), jStringCls, NULL);
		if (NULL != jContentArray) {
			for (int i = 0; i < contents.size(); i++)
			{
				jstring content = env->NewStringUTF(contents[i].c_str());
				env->SetObjectArrayElement(jContentArray, i, content);
				env->DeleteLocalRef(content);
			}
		}

		jbooleanArray jFlagArray = env->NewBooleanArray(flags.size());
		if (NULL != jFlagArray) {
			for (int i = 0; i < flags.size(); i++)
			{
				jboolean flag = flags[i];
				env->SetBooleanArrayRegion(jFlagArray, i, 1, &flag);
			}
		}

		jclass jCallbackCls = env->GetObjectClass(gListener);
		string signure = "(ILjava/lang/String;Ljava/lang/String;[Ljava/lang/String;[Z)V";

		jmethodID jCallback = env->GetMethodID(jCallbackCls, "OnGetLadyCustomTemplate", signure.c_str());
		if (NULL != gListener && NULL != jCallback)
		{
			FileLog("LiveChatClient", "OnGetLadyCustomTemplate() callback now");

			int errType = LccErrTypeToInt(err);
			jstring jerrmsg = env->NewStringUTF(errmsg.c_str());
			jstring jwomanId = env->NewStringUTF(inUserId.c_str());

			env->CallVoidMethod(gListener, jCallback, errType, jerrmsg, jwomanId, jContentArray, jFlagArray);

			env->DeleteLocalRef(jwomanId);
			env->DeleteLocalRef(jerrmsg);

			FileLog("LiveChatClient", "OnGetLadyCustomTemplate() callback ok");
		}

		if (NULL != jContentArray) {
			env->DeleteLocalRef(jContentArray);
		}

		if (NULL != jFlagArray) {
			env->DeleteLocalRef(jFlagArray);
		}

		ReleaseEnv(isAttachThread);
	}
	virtual void OnSendMagicIcon(const string& inUserId, const string& inIconId, int inTicket, LCC_ERR_TYPE err, const string& errmsg)
	{
		JNIEnv* env = NULL;
		bool isAttachThread = false;
		GetEnv(&env, &isAttachThread);

		FileLog("LiveChatClient", "OnSendMagicIcon() callback, env:%p, isAttachThread:%d", env, isAttachThread);

		jclass jCallbackCls = env->GetObjectClass(gListener);
		string signure = "(ILjava/lang/String;Ljava/lang/String;Ljava/lang/String;I)V";
		jmethodID jCallback = env->GetMethodID(jCallbackCls, "OnSendMagicIcon", signure.c_str());
		if (NULL != gListener && NULL != jCallback)
		{
			FileLog("LiveChatClient", "OnSendMagicIcon() callback now");

			int errType = LccErrTypeToInt(err);
			jstring jerrmsg = env->NewStringUTF(errmsg.c_str());
			jstring juserId = env->NewStringUTF(inUserId.c_str());
			jstring jmagicIconId = env->NewStringUTF(inIconId.c_str());

			env->CallVoidMethod(gListener, jCallback, errType, jerrmsg, juserId, jmagicIconId, inTicket);

			env->DeleteLocalRef(jmagicIconId);
			env->DeleteLocalRef(juserId);
			env->DeleteLocalRef(jerrmsg);

			FileLog("LiveChatClient", "OnSendMagicIcon() callback ok");
		}

		ReleaseEnv(isAttachThread);
	}

	virtual void OnGetPaidTheme(const string& inUserId, LCC_ERR_TYPE err, const string& errmsg, const ThemeInfoList& themeList){
		JNIEnv* env = NULL;
		bool isAttachThread = false;
		GetEnv(&env, &isAttachThread);

		FileLog("LiveChatClient", "OnGetPaidTheme() callback, env:%p, isAttachThread:%d", env, isAttachThread);

		/*create paid theme list*/
		jobjectArray jpaidThemeArray = NULL;
		jclass jItemCls = GetJObjectClassWithMap(env, LIVECHAT_PAID_THEME_INFO);
		if(NULL != jItemCls){
			jpaidThemeArray = env->NewObjectArray(themeList.size(), jItemCls, NULL);
			jmethodID jitemInit = env->GetMethodID(jItemCls, "<init>", "("
					"Ljava/lang/String;"
					"Ljava/lang/String;"
					"Ljava/lang/String;"
					"IIII"
					")V");
			ThemeInfoList::const_iterator themeInfoIter;
			int iIndex;
			for(iIndex = 0, themeInfoIter = themeList.begin();
					themeInfoIter != themeList.end();
					iIndex++, themeInfoIter++){
				jstring jthemeId = env->NewStringUTF(themeInfoIter->themeId.c_str());
				jstring jmanId = env->NewStringUTF(themeInfoIter->manId.c_str());
				jstring jwomanId = env->NewStringUTF(themeInfoIter->womanId.c_str());

				jobject jpaidThemeInfo = env->NewObject(jItemCls, jitemInit, jthemeId, jmanId, jwomanId,
						themeInfoIter->startTime, themeInfoIter->endTime, themeInfoIter->nowTime, themeInfoIter->updateTime);
				env->SetObjectArrayElement(jpaidThemeArray, iIndex, jpaidThemeInfo);
				env->DeleteLocalRef(jpaidThemeInfo);

				env->DeleteLocalRef(jthemeId);
				env->DeleteLocalRef(jmanId);
				env->DeleteLocalRef(jwomanId);
			}
		}


		jclass jCallbackCls = env->GetObjectClass(gListener);
		string signure = "(ILjava/lang/String;Ljava/lang/String;"
				"[L"
				LIVECHAT_PAID_THEME_INFO
				";"
				")V";
		jmethodID jCallback = env->GetMethodID(jCallbackCls, "OnGetPaidTheme", signure.c_str());
		if (NULL != gListener && NULL != jCallback)
		{
			FileLog("LiveChatClient", "OnGetPaidTheme() callback now");

			int errType = LccErrTypeToInt(err);
			jstring jerrmsg = env->NewStringUTF(errmsg.c_str());
			jstring juserId = env->NewStringUTF(inUserId.c_str());

			env->CallVoidMethod(gListener, jCallback, errType, jerrmsg, juserId, jpaidThemeArray);

			env->DeleteLocalRef(juserId);
			env->DeleteLocalRef(jerrmsg);

			FileLog("LiveChatClient", "OnGetPaidTheme() callback ok");
		}

		if(NULL != jpaidThemeArray){
			env->DeleteLocalRef(jpaidThemeArray);
		}

		ReleaseEnv(isAttachThread);
	}

	virtual void OnGetAllPaidTheme(LCC_ERR_TYPE err, const string& errmsg, const ThemeInfoList& themeInfoList){
		JNIEnv* env = NULL;
		bool isAttachThread = false;
		GetEnv(&env, &isAttachThread);

		FileLog("LiveChatClient", "OnGetAllPaidTheme() callback, env:%p, isAttachThread:%d", env, isAttachThread);

		/*create paid theme list*/
		jobjectArray jpaidThemeArray = NULL;
		jclass jItemCls = GetJObjectClassWithMap(env, LIVECHAT_PAID_THEME_INFO);
		if(NULL != jItemCls){
			jpaidThemeArray = env->NewObjectArray(themeInfoList.size(), jItemCls, NULL);
			jmethodID jitemInit = env->GetMethodID(jItemCls, "<init>", "("
					"Ljava/lang/String;"
					"Ljava/lang/String;"
					"Ljava/lang/String;"
					"IIII"
					")V");
			ThemeInfoList::const_iterator themeInfoIter;
			int iIndex;
			for(iIndex = 0, themeInfoIter = themeInfoList.begin();
					themeInfoIter != themeInfoList.end();
					iIndex++, themeInfoIter++){
				jstring jthemeId = env->NewStringUTF(themeInfoIter->themeId.c_str());
				jstring jmanId = env->NewStringUTF(themeInfoIter->manId.c_str());
				jstring jwomanId = env->NewStringUTF(themeInfoIter->womanId.c_str());

				jobject jpaidThemeInfo = env->NewObject(jItemCls, jitemInit, jthemeId, jmanId, jwomanId,
						themeInfoIter->startTime, themeInfoIter->endTime, themeInfoIter->nowTime, themeInfoIter->updateTime);
				env->SetObjectArrayElement(jpaidThemeArray, iIndex, jpaidThemeInfo);
				env->DeleteLocalRef(jpaidThemeInfo);

				env->DeleteLocalRef(jthemeId);
				env->DeleteLocalRef(jmanId);
				env->DeleteLocalRef(jwomanId);
			}
		}


		jclass jCallbackCls = env->GetObjectClass(gListener);
		string signure = "(ILjava/lang/String;"
				"[L"
				LIVECHAT_PAID_THEME_INFO
				";"
				")V";
		jmethodID jCallback = env->GetMethodID(jCallbackCls, "OnGetAllPaidTheme", signure.c_str());
		if (NULL != gListener && NULL != jCallback)
		{
			FileLog("LiveChatClient", "OnGetAllPaidTheme() callback now");

			int errType = LccErrTypeToInt(err);
			jstring jerrmsg = env->NewStringUTF(errmsg.c_str());

			env->CallVoidMethod(gListener, jCallback, errType, jerrmsg, jpaidThemeArray);

			env->DeleteLocalRef(jerrmsg);

			FileLog("LiveChatClient", "OnGetAllPaidTheme() callback ok");
		}

		if(NULL != jpaidThemeArray){
			env->DeleteLocalRef(jpaidThemeArray);
		}

		ReleaseEnv(isAttachThread);
	}

	virtual void OnManFeeTheme(const string& inUserId, const string& inThemeId, LCC_ERR_TYPE err, const string& errmsg, const ThemeInfoItem& item){
		JNIEnv* env = NULL;
		bool isAttachThread = false;
		GetEnv(&env, &isAttachThread);

		FileLog("LiveChatClient", "OnManFeeTheme() callback, env:%p, isAttachThread:%d", env, isAttachThread);

		/*create paid themeInfo*/
		jobject jpaidThemeInfo = NULL;
		jclass jItemCls = GetJObjectClassWithMap(env, LIVECHAT_PAID_THEME_INFO);
		if(NULL != jItemCls){
			jmethodID jitemInit = env->GetMethodID(jItemCls, "<init>", "("
					"Ljava/lang/String;"
					"Ljava/lang/String;"
					"Ljava/lang/String;"
					"IIII"
					")V");
			jstring jthemeId = env->NewStringUTF(item.themeId.c_str());
			jstring jmanId = env->NewStringUTF(item.manId.c_str());
			jstring jwomanId = env->NewStringUTF(item.womanId.c_str());

			jpaidThemeInfo = env->NewObject(jItemCls, jitemInit, jthemeId, jmanId, jwomanId,
					item.startTime, item.endTime, item.nowTime, item.updateTime);

			env->DeleteLocalRef(jthemeId);
			env->DeleteLocalRef(jmanId);
			env->DeleteLocalRef(jwomanId);
		}


		jclass jCallbackCls = env->GetObjectClass(gListener);
		string signure = "(ILjava/lang/String;Ljava/lang/String;Ljava/lang/String;"
				"L"
				LIVECHAT_PAID_THEME_INFO
				";"
				")V";
		jmethodID jCallback = env->GetMethodID(jCallbackCls, "OnManFeeTheme", signure.c_str());
		if (NULL != gListener && NULL != jCallback)
		{
			FileLog("LiveChatClient", "OnManFeeTheme() callback now");

			int errType = LccErrTypeToInt(err);
			jstring jcallbackWomanId = env->NewStringUTF(inUserId.c_str());
			jstring jcallbackThemeId = env->NewStringUTF(inThemeId.c_str());
			jstring jerrmsg = env->NewStringUTF(errmsg.c_str());

			env->CallVoidMethod(gListener, jCallback, errType, jcallbackWomanId, jcallbackThemeId, jerrmsg, jpaidThemeInfo);

			env->DeleteLocalRef(jcallbackWomanId);
			env->DeleteLocalRef(jcallbackThemeId);
			env->DeleteLocalRef(jerrmsg);

			FileLog("LiveChatClient", "OnManFeeTheme() callback ok");
		}

		if(NULL != jpaidThemeInfo){
			env->DeleteLocalRef(jpaidThemeInfo);
		}

		ReleaseEnv(isAttachThread);
	}

	virtual void OnManApplyTheme(const string& inUserId, const string& inThemeId, LCC_ERR_TYPE err, const string& errmsg, const ThemeInfoItem& item){
		JNIEnv* env = NULL;
		bool isAttachThread = false;
		GetEnv(&env, &isAttachThread);

		FileLog("LiveChatClient", "OnManApplyTheme() callback, env:%p, isAttachThread:%d", env, isAttachThread);

		/*create paid themeInfo*/
		jobject jpaidThemeInfo = NULL;
		jclass jItemCls = GetJObjectClassWithMap(env, LIVECHAT_PAID_THEME_INFO);
		if(NULL != jItemCls){
			jmethodID jitemInit = env->GetMethodID(jItemCls, "<init>", "("
					"Ljava/lang/String;"
					"Ljava/lang/String;"
					"Ljava/lang/String;"
					"IIII"
					")V");
			jstring jthemeId = env->NewStringUTF(item.themeId.c_str());
			jstring jmanId = env->NewStringUTF(item.manId.c_str());
			jstring jwomanId = env->NewStringUTF(item.womanId.c_str());

			jpaidThemeInfo = env->NewObject(jItemCls, jitemInit, jthemeId, jmanId, jwomanId,
					item.startTime, item.endTime, item.nowTime, item.updateTime);

			env->DeleteLocalRef(jthemeId);
			env->DeleteLocalRef(jmanId);
			env->DeleteLocalRef(jwomanId);
		}


		jclass jCallbackCls = env->GetObjectClass(gListener);
		string signure = "(ILjava/lang/String;Ljava/lang/String;Ljava/lang/String;"
				"L"
				LIVECHAT_PAID_THEME_INFO
				";"
				")V";
		jmethodID jCallback = env->GetMethodID(jCallbackCls, "OnManApplyTheme", signure.c_str());
		if (NULL != gListener && NULL != jCallback)
		{
			FileLog("LiveChatClient", "OnManApplyTheme() callback now");

			int errType = LccErrTypeToInt(err);
			jstring jcallbackWomanId = env->NewStringUTF(inUserId.c_str());
			jstring jcallbackThemeId = env->NewStringUTF(inThemeId.c_str());
			jstring jerrmsg = env->NewStringUTF(errmsg.c_str());

			env->CallVoidMethod(gListener, jCallback, errType, jcallbackWomanId, jcallbackThemeId, jerrmsg, jpaidThemeInfo);

			env->DeleteLocalRef(jcallbackWomanId);
			env->DeleteLocalRef(jcallbackThemeId);
			env->DeleteLocalRef(jerrmsg);

			FileLog("LiveChatClient", "OnManApplyTheme() callback ok");
		}

		if(NULL != jpaidThemeInfo){
			env->DeleteLocalRef(jpaidThemeInfo);
		}

		ReleaseEnv(isAttachThread);
	}

	virtual void OnPlayThemeMotion(const string& inUserId, const string& inThemeId, LCC_ERR_TYPE err, const string& errmsg, bool success){
		JNIEnv* env = NULL;
		bool isAttachThread = false;
		GetEnv(&env, &isAttachThread);

		FileLog("LiveChatClient", "OnPlayThemeMotion() callback, env:%p, isAttachThread:%d", env, isAttachThread);

		jclass jCallbackCls = env->GetObjectClass(gListener);
		string signure = "(ILjava/lang/String;Ljava/lang/String;Ljava/lang/String;)V";
		jmethodID jCallback = env->GetMethodID(jCallbackCls, "OnPlayThemeMotion", signure.c_str());
		if (NULL != gListener && NULL != jCallback)
		{
			FileLog("LiveChatClient", "OnManApplyTheme() callback now");

			int errType = LccErrTypeToInt(err);
			jstring juserId = env->NewStringUTF(inUserId.c_str());
			jstring jthemeId = env->NewStringUTF(inThemeId.c_str());
			jstring jerrmsg = env->NewStringUTF(errmsg.c_str());

			env->CallVoidMethod(gListener, jCallback, errType, jerrmsg, juserId, jthemeId);

			env->DeleteLocalRef(jerrmsg);
			env->DeleteLocalRef(jthemeId);
			env->DeleteLocalRef(juserId);

			FileLog("LiveChatClient", "OnPlayThemeMotion() callback ok");
		}

		ReleaseEnv(isAttachThread);
	}

	virtual void OnRecvThemeMotion(const string& themeId, const string& manId, const string& womanId){
		JNIEnv* env = NULL;
		bool isAttachThread = false;
		GetEnv(&env, &isAttachThread);

		FileLog("LiveChatClient", "OnRecvThemeMotion() callback, env:%p, isAttachThread:%d", env, isAttachThread);

		jclass jCallbackCls = env->GetObjectClass(gListener);
		string signure = "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V";
		jmethodID jCallback = env->GetMethodID(jCallbackCls, "OnRecvThemeMotion", signure.c_str());
		if (NULL != gListener && NULL != jCallback)
		{
			FileLog("LiveChatClient", "OnRecvThemeMotion() callback now");

			jstring jthemeId = env->NewStringUTF(themeId.c_str());
			jstring jmanId = env->NewStringUTF(manId.c_str());
			jstring jwomanId = env->NewStringUTF(womanId.c_str());

			env->CallVoidMethod(gListener, jCallback, jthemeId, jmanId, jwomanId);

			env->DeleteLocalRef(jthemeId);
			env->DeleteLocalRef(jmanId);
			env->DeleteLocalRef(jwomanId);

			FileLog("LiveChatClient", "OnRecvThemeMotion() callback ok");
		}

		ReleaseEnv(isAttachThread);
	}

	virtual void OnRecvThemeRecommend(const string& themeId, const string& manId, const string& womanId){
		JNIEnv* env = NULL;
		bool isAttachThread = false;
		GetEnv(&env, &isAttachThread);

		FileLog("LiveChatClient", "OnRecvThemeRecommend() callback, env:%p, isAttachThread:%d", env, isAttachThread);

		jclass jCallbackCls = env->GetObjectClass(gListener);
		string signure = "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V";
		jmethodID jCallback = env->GetMethodID(jCallbackCls, "OnRecvThemeRecommend", signure.c_str());
		if (NULL != gListener && NULL != jCallback)
		{
			FileLog("LiveChatClient", "OnRecvThemeRecommend() callback now");

			jstring jthemeId = env->NewStringUTF(themeId.c_str());
			jstring jmanId = env->NewStringUTF(manId.c_str());
			jstring jwomanId = env->NewStringUTF(womanId.c_str());

			env->CallVoidMethod(gListener, jCallback, jthemeId, jmanId, jwomanId);

			env->DeleteLocalRef(jthemeId);
			env->DeleteLocalRef(jmanId);
			env->DeleteLocalRef(jwomanId);

			FileLog("LiveChatClient", "OnRecvThemeRecommend() callback ok");
		}

		ReleaseEnv(isAttachThread);
	}

	// --------------------- 服务器主动请求 -------------------------------
	virtual void OnRecvMessage(const string& toId, const string& fromId, const string& fromName, const string& inviteId, bool charge, int ticket, TALK_MSG_TYPE msgType, const string& message) {
		JNIEnv* env = NULL;
		bool isAttachThread = false;
		GetEnv(&env, &isAttachThread);

		FileLog("LiveChatClient", "OnRecvMessage() callback, toId:%s, fromId:%s, fromName:%s"
				", inviteId:%s, charge:%d, ticket:%d, msgType:%d, message:%s, env:%p, isAttachThread:%d"
				, toId.c_str(), fromId.c_str(), fromName.c_str()
				, inviteId.c_str(), charge, ticket, msgType, message.c_str(), env, isAttachThread);

		jclass jCallbackCls = env->GetObjectClass(gListener);
		string signure = "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;ZIILjava/lang/String;)V";
		jmethodID jCallback = env->GetMethodID(jCallbackCls, "OnRecvMessage", signure.c_str());
		if (NULL != gListener && NULL != jCallback)
		{
			FileLog("LiveChatClient", "OnRecvMessage() callback now");

			jstring jtoId = env->NewStringUTF(toId.c_str());
			jstring jfromId = env->NewStringUTF(fromId.c_str());
			jstring jfromName = env->NewStringUTF(fromName.c_str());
			jstring jinviteId = env->NewStringUTF(inviteId.c_str());
			int iMsgType = TalkMsgTypeToInt(msgType);
			jstring jmessage = env->NewStringUTF(message.c_str());

			env->CallVoidMethod(gListener, jCallback, jtoId, jfromId, jfromName, jinviteId, charge, ticket, iMsgType, jmessage);

			env->DeleteLocalRef(jtoId);
			env->DeleteLocalRef(jfromId);
			env->DeleteLocalRef(jfromName);
			env->DeleteLocalRef(jinviteId);
			env->DeleteLocalRef(jmessage);

			FileLog("LiveChatClient", "OnRecvMessage() callback ok");
		}

		ReleaseEnv(isAttachThread);
	}
	virtual void OnRecvEmotion(const string& toId, const string& fromId, const string& fromName, const string& inviteId, bool charge, int ticket, TALK_MSG_TYPE msgType, const string& emotionId) {
		JNIEnv* env = NULL;
		bool isAttachThread = false;
		GetEnv(&env, &isAttachThread);

		FileLog("LiveChatClient", "OnRecvEmotion() callback, toId:%s, fromId:%s, fromName:%s, inviteId:%s"
					", charge:%d, ticket:%d, msgType:%d, emotionId:%s, env:%p, isAttachThread:%d"
					, toId.c_str(), fromId.c_str(), fromName.c_str(), inviteId.c_str()
					, charge, ticket, msgType, emotionId.c_str(), env, isAttachThread);

		jclass jCallbackCls = env->GetObjectClass(gListener);
		string signure = "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;ZIILjava/lang/String;)V";
		jmethodID jCallback = env->GetMethodID(jCallbackCls, "OnRecvEmotion", signure.c_str());
		if (NULL != gListener && NULL != jCallback)
		{
			FileLog("LiveChatClient", "OnRecvEmotion() callback now");

			jstring jtoId = env->NewStringUTF(toId.c_str());
			jstring jfromId = env->NewStringUTF(fromId.c_str());
			jstring jfromName = env->NewStringUTF(fromName.c_str());
			jstring jinviteId = env->NewStringUTF(inviteId.c_str());
			int iMsgType = TalkMsgTypeToInt(msgType);
			jstring jemotionId = env->NewStringUTF(emotionId.c_str());

			env->CallVoidMethod(gListener, jCallback, jtoId, jfromId, jfromName, jinviteId, charge, ticket, iMsgType, jemotionId);

			env->DeleteLocalRef(jtoId);
			env->DeleteLocalRef(jfromId);
			env->DeleteLocalRef(jfromName);
			env->DeleteLocalRef(jinviteId);
			env->DeleteLocalRef(jemotionId);

			FileLog("LiveChatClient", "OnRecvEmotion() callback ok");
		}

		ReleaseEnv(isAttachThread);
	}
	virtual void OnRecvVoice(const string& toId, const string& fromId, const string& fromName, const string& inviteId, bool charge, TALK_MSG_TYPE msgType, const string& voiceId, const string& fileType, int timeLen) {
		JNIEnv* env = NULL;
		bool isAttachThread = false;
		GetEnv(&env, &isAttachThread);

		FileLog("LiveChatClient", "OnRecvVoice() callback, toId:%s, fromId:%s, fromName:%s"
				", inviteId:%s, charge:%d, msgType:%d, voiceId:%s, env:%p, isAttachThread:%d"
				, toId.c_str(), fromId.c_str(), fromName.c_str()
				, inviteId.c_str(), charge, msgType, voiceId.c_str(), env, isAttachThread);

		jclass jCallbackCls = env->GetObjectClass(gListener);
		string signure = "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;ZILjava/lang/String;Ljava/lang/String;I)V";
		jmethodID jCallback = env->GetMethodID(jCallbackCls, "OnRecvVoice", signure.c_str());
		if (NULL != gListener && NULL != jCallback)
		{
			FileLog("LiveChatClient", "OnRecvVoice() callback now");

			jstring jtoId = env->NewStringUTF(toId.c_str());
			jstring jfromId = env->NewStringUTF(fromId.c_str());
			jstring jfromName = env->NewStringUTF(fromName.c_str());
			jstring jinviteId = env->NewStringUTF(inviteId.c_str());
			int iMsgType = TalkMsgTypeToInt(msgType);
			jstring jvoiceId = env->NewStringUTF(voiceId.c_str());
			jstring jfileType = env->NewStringUTF(fileType.c_str());

			env->CallVoidMethod(gListener, jCallback, jtoId, jfromId, jfromName, jinviteId, charge, iMsgType, jvoiceId, jfileType, timeLen);

			env->DeleteLocalRef(jtoId);
			env->DeleteLocalRef(jfromId);
			env->DeleteLocalRef(jfromName);
			env->DeleteLocalRef(jinviteId);
			env->DeleteLocalRef(jvoiceId);
			env->DeleteLocalRef(jfileType);

			FileLog("LiveChatClient", "OnRecvVoice() callback ok");
		}

		ReleaseEnv(isAttachThread);
	}
	virtual void OnRecvWarning(const string& toId, const string& fromId, const string& fromName, const string& inviteId, bool charge, int ticket, TALK_MSG_TYPE msgType, const string& message) {
		JNIEnv* env = NULL;
		bool isAttachThread = false;
		GetEnv(&env, &isAttachThread);

		FileLog("LiveChatClient", "OnRecvWarning() callback, toId:%s, fromId:%s, fromName:%s"
				", inviteId:%s, charge:%d, ticket:%d, msgType:%d, message:%s, env:%p, isAttachThread:%d"
				, toId.c_str(), fromId.c_str(), fromName.c_str()
				, inviteId.c_str(), charge, ticket, msgType, message.c_str(), env, isAttachThread);

		jclass jCallbackCls = env->GetObjectClass(gListener);
		string signure = "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;ZIILjava/lang/String;)V";
		jmethodID jCallback = env->GetMethodID(jCallbackCls, "OnRecvWarning", signure.c_str());
		if (NULL != gListener && NULL != jCallback)
		{
			FileLog("LiveChatClient", "OnRecvWarning() callback now");

			jstring jtoId = env->NewStringUTF(toId.c_str());
			jstring jfromId = env->NewStringUTF(fromId.c_str());
			jstring jfromName = env->NewStringUTF(fromName.c_str());
			jstring jinviteId = env->NewStringUTF(inviteId.c_str());
			int iMsgType = TalkMsgTypeToInt(msgType);
			jstring jmessage = env->NewStringUTF(message.c_str());

			env->CallVoidMethod(gListener, jCallback, jtoId, jfromId, jfromName, jinviteId, charge, ticket, iMsgType, jmessage);

			env->DeleteLocalRef(jtoId);
			env->DeleteLocalRef(jfromId);
			env->DeleteLocalRef(jfromName);
			env->DeleteLocalRef(jinviteId);
			env->DeleteLocalRef(jmessage);

			FileLog("LiveChatClient", "OnRecvWarning() callback ok");
		}

		ReleaseEnv(isAttachThread);
	}
	virtual void OnUpdateStatus(const string& userId, const string& server, CLIENT_TYPE clientType, USER_STATUS_TYPE statusType) {
		JNIEnv* env = NULL;
		bool isAttachThread = false;
		GetEnv(&env, &isAttachThread);

		FileLog("LiveChatClient", "OnUpdateStatus() callback, env:%p, isAttachThread:%d", env, isAttachThread);

		jclass jCallbackCls = env->GetObjectClass(gListener);
		string signure = "(Ljava/lang/String;Ljava/lang/String;II)V";
		jmethodID jCallback = env->GetMethodID(jCallbackCls, "OnUpdateStatus", signure.c_str());
		if (NULL != gListener && NULL != jCallback)
		{
			FileLog("LiveChatClient", "OnUpdateStatus() callback now");

			jstring juserId = env->NewStringUTF(userId.c_str());
			jstring jserver = env->NewStringUTF(server.c_str());
			int iClientType = ClientTypeToInt(clientType);
			int iStatusType = UserStatusTypeToInt(statusType);

			env->CallVoidMethod(gListener, jCallback, juserId, jserver, iClientType, iStatusType);

			env->DeleteLocalRef(juserId);
			env->DeleteLocalRef(jserver);

			FileLog("LiveChatClient", "OnUpdateStatus() callback ok");
		}

		ReleaseEnv(isAttachThread);
	}
	virtual void OnUpdateTicket(const string& fromId, int ticket) {
		JNIEnv* env = NULL;
		bool isAttachThread = false;
		GetEnv(&env, &isAttachThread);

		FileLog("LiveChatClient", "OnUpdateTicket() callback, env:%p, isAttachThread:%d", env, isAttachThread);

		jclass jCallbackCls = env->GetObjectClass(gListener);
		string signure = "(Ljava/lang/String;I)V";
		jmethodID jCallback = env->GetMethodID(jCallbackCls, "OnUpdateTicket", signure.c_str());
		if (NULL != gListener && NULL != jCallback)
		{
			FileLog("LiveChatClient", "OnUpdateTicket() callback now");

			jstring jfromId = env->NewStringUTF(fromId.c_str());

			env->CallVoidMethod(gListener, jCallback, jfromId, ticket);

			env->DeleteLocalRef(jfromId);

			FileLog("LiveChatClient", "OnUpdateTicket() callback ok");
		}

		ReleaseEnv(isAttachThread);
	}
	virtual void OnRecvEditMsg(const string& fromId) {
		JNIEnv* env = NULL;
		bool isAttachThread = false;
		GetEnv(&env, &isAttachThread);

		FileLog("LiveChatClient", "OnRecvEditMsg() callback, env:%p, isAttachThread:%d", env, isAttachThread);

		jclass jCallbackCls = env->GetObjectClass(gListener);
		string signure = "(Ljava/lang/String;)V";
		jmethodID jCallback = env->GetMethodID(jCallbackCls, "OnRecvEditMsg", signure.c_str());
		if (NULL != gListener && NULL != jCallback)
		{
			FileLog("LiveChatClient", "OnRecvEditMsg() callback now");

			jstring jfromId = env->NewStringUTF(fromId.c_str());

			env->CallVoidMethod(gListener, jCallback, jfromId);

			env->DeleteLocalRef(jfromId);

			FileLog("LiveChatClient", "OnRecvEditMsg() callback ok");
		}

		ReleaseEnv(isAttachThread);
	}
	virtual void OnRecvTalkEvent(const string& userId, TALK_EVENT_TYPE eventType) {
		JNIEnv* env = NULL;
		bool isAttachThread = false;
		GetEnv(&env, &isAttachThread);

		FileLog("LiveChatClient", "OnRecvTalkEvent() callback, env%p, isAttachThread:%d", env, isAttachThread);

		jclass jCallbackCls = env->GetObjectClass(gListener);
		string signure = "(Ljava/lang/String;I)V";
		jmethodID jCallback = env->GetMethodID(jCallbackCls, "OnRecvTalkEvent", signure.c_str());
		if (NULL != gListener && NULL != jCallback)
		{
			FileLog("LiveChatClient", "OnRecvTalkEvent() callback now");

			jstring juserId = env->NewStringUTF(userId.c_str());
			int iEventType = TalkEventTypeToInt(eventType);

			env->CallVoidMethod(gListener, jCallback, juserId, iEventType);

			env->DeleteLocalRef(juserId);

			FileLog("LiveChatClient", "OnRecvTalkEvent() callback ok");
		}

		ReleaseEnv(isAttachThread);
	}
	virtual void OnRecvTryTalkBegin(const string& toId, const string& fromId, int time) {
		JNIEnv* env = NULL;
		bool isAttachThread = false;
		GetEnv(&env, &isAttachThread);

		FileLog("LiveChatClient", "OnRecvTryTalkBegin() callback, env:%p, isAttachThread:%d", env, isAttachThread);

		jclass jCallbackCls = env->GetObjectClass(gListener);
		string signure = "(Ljava/lang/String;Ljava/lang/String;I)V";
		jmethodID jCallback = env->GetMethodID(jCallbackCls, "OnRecvTryTalkBegin", signure.c_str());
		if (NULL != gListener && NULL != jCallback)
		{
			FileLog("LiveChatClient", "OnRecvTryTalkBegin() callback now");

			jstring jtoId = env->NewStringUTF(toId.c_str());
			jstring jfromId = env->NewStringUTF(fromId.c_str());

			env->CallVoidMethod(gListener, jCallback, jtoId, jfromId, time);

			env->DeleteLocalRef(jtoId);
			env->DeleteLocalRef(jfromId);

			FileLog("LiveChatClient", "OnRecvTryTalkBegin() callback ok");
		}

		ReleaseEnv(isAttachThread);
	}
	virtual void OnRecvTryTalkEnd(const string& userId) {
		JNIEnv* env = NULL;
		bool isAttachThread = false;
		GetEnv(&env, &isAttachThread);

		FileLog("LiveChatClient", "OnRecvTryTalkEnd() callback, env:%p, isAttachThread:%d", env, isAttachThread);

		jclass jCallbackCls = env->GetObjectClass(gListener);
		string signure = "(Ljava/lang/String;)V";
		jmethodID jCallback = env->GetMethodID(jCallbackCls, "OnRecvTryTalkEnd", signure.c_str());
		if (NULL != gListener && NULL != jCallback)
		{
			FileLog("LiveChatClient", "OnRecvTryTalkEnd() callback now");

			jstring juserId = env->NewStringUTF(userId.c_str());

			env->CallVoidMethod(gListener, jCallback, juserId);

			env->DeleteLocalRef(juserId);

			FileLog("LiveChatClient", "OnRecvTryTalkEnd() callback ok");
		}

		ReleaseEnv(isAttachThread);
	}
	virtual void OnRecvEMFNotice(const string& fromId, TALK_EMF_NOTICE_TYPE noticeType) {
		JNIEnv* env = NULL;
		bool isAttachThread = false;
		GetEnv(&env, &isAttachThread);

		FileLog("LiveChatClient", "OnRecvEMFNotice() callback, env:%p, isAttachThread:%d", env, isAttachThread);

		jclass jCallbackCls = env->GetObjectClass(gListener);
		string signure = "(Ljava/lang/String;I)V";
		jmethodID jCallback = env->GetMethodID(jCallbackCls, "OnRecvEMFNotice", signure.c_str());
		if (NULL != gListener && NULL != jCallback)
		{
			FileLog("LiveChatClient", "OnRecvEMFNotice() callback now");

			jstring jfromId = env->NewStringUTF(fromId.c_str());
			int iNoticeType = TalkEmfNoticeTypeToInt(noticeType);

			env->CallVoidMethod(gListener, jCallback, jfromId, iNoticeType);

			env->DeleteLocalRef(jfromId);

			FileLog("LiveChatClient", "OnRecvEMFNotice() callback ok");
		}

		ReleaseEnv(isAttachThread);
	}
	virtual void OnRecvKickOffline(KICK_OFFLINE_TYPE kickType) {
		JNIEnv* env = NULL;
		bool isAttachThread = false;
		GetEnv(&env, &isAttachThread);

		FileLog("LiveChatClient", "OnRecvKickOffline() callback, env:%p, isAttachThread:%d", env, isAttachThread);

		jclass jCallbackCls = env->GetObjectClass(gListener);
		string signure = "(I)V";
		jmethodID jCallback = env->GetMethodID(jCallbackCls, "OnRecvKickOffline", signure.c_str());
		if (NULL != gListener && NULL != jCallback)
		{
			FileLog("LiveChatClient", "OnRecvKickOffline() callback now");

			int iKickType = KickOfflineTypeToInt(kickType);
			env->CallVoidMethod(gListener, jCallback, iKickType);

			FileLog("LiveChatClient", "OnRecvKickOffline() callback ok");
		}

		ReleaseEnv(isAttachThread);
	}
	virtual void OnRecvPhoto(const string& toId, const string& fromId, const string& fromName, const string& inviteId, const string& photoId, const string& sendId, bool charge, const string& photoDesc, int ticket)
	{
		JNIEnv* env = NULL;
		bool isAttachThread = false;
		GetEnv(&env, &isAttachThread);

		FileLog("LiveChatClient", "OnRecvPhoto() callback, env:%p, isAttachThread:%d", env, isAttachThread);

		jclass jCallbackCls = env->GetObjectClass(gListener);
		string signure = "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;ZLjava/lang/String;I)V";
		jmethodID jCallback = env->GetMethodID(jCallbackCls, "OnRecvPhoto", signure.c_str());
		if (NULL != gListener && NULL != jCallback)
		{
			FileLog("LiveChatClient", "OnRecvPhoto() callback now");

			jstring jtoId = env->NewStringUTF(toId.c_str());
			jstring jfromId = env->NewStringUTF(fromId.c_str());
			jstring jfromName = env->NewStringUTF(fromName.c_str());
			jstring jinviteId = env->NewStringUTF(inviteId.c_str());
			jstring jphotoId = env->NewStringUTF(photoId.c_str());
			jstring jsendId = env->NewStringUTF(sendId.c_str());
			jstring jphotoDesc = env->NewStringUTF(photoDesc.c_str());

			env->CallVoidMethod(gListener, jCallback, jtoId, jfromId, jfromName, jinviteId, jphotoId, jsendId, charge, jphotoDesc, ticket);

			env->DeleteLocalRef(jtoId);
			env->DeleteLocalRef(jfromId);
			env->DeleteLocalRef(jfromName);
			env->DeleteLocalRef(jinviteId);
			env->DeleteLocalRef(jphotoId);
			env->DeleteLocalRef(jsendId);
			env->DeleteLocalRef(jphotoDesc);

			FileLog("LiveChatClient", "OnRecvPhoto() callback ok");
		}

		ReleaseEnv(isAttachThread);
	}
	virtual void OnRecvShowPhoto(const string& toId, const string& fromId, const string& fromName, const string& inviteId, const string& photoId, const string& sendId, bool charge, const string& photoDesc, int ticket)
	{

	}
	virtual void OnRecvVideo(const string& toId, const string& fromId, const string& fromName, const string& inviteId, const string& videoId, const string& sendId, bool charge, const string& videoDesc, int ticket)
	{
		JNIEnv* env = NULL;
		bool isAttachThread = false;
		GetEnv(&env, &isAttachThread);

		FileLog("LiveChatClient", "OnRecvVideo() callback, env:%p, isAttachThread:%d", env, isAttachThread);

		jclass jCallbackCls = env->GetObjectClass(gListener);
		string signure = "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;ZLjava/lang/String;I)V";
		jmethodID jCallback = env->GetMethodID(jCallbackCls, "OnRecvVideo", signure.c_str());
		if (NULL != gListener && NULL != jCallback)
		{
			FileLog("LiveChatClient", "OnRecvVideo() callback now");

			jstring jtoId = env->NewStringUTF(toId.c_str());
			jstring jfromId = env->NewStringUTF(fromId.c_str());
			jstring jfromName = env->NewStringUTF(fromName.c_str());
			jstring jinviteId = env->NewStringUTF(inviteId.c_str());
			jstring jvideoId = env->NewStringUTF(videoId.c_str());
			jstring jsendId = env->NewStringUTF(sendId.c_str());
			jstring jvideoDesc = env->NewStringUTF(videoDesc.c_str());

			env->CallVoidMethod(gListener, jCallback, jtoId, jfromId, jfromName, jinviteId, jvideoId, jsendId, charge, jvideoDesc, ticket);

			env->DeleteLocalRef(jtoId);
			env->DeleteLocalRef(jfromId);
			env->DeleteLocalRef(jfromName);
			env->DeleteLocalRef(jinviteId);
			env->DeleteLocalRef(jvideoId);
			env->DeleteLocalRef(jsendId);
			env->DeleteLocalRef(jvideoDesc);

			FileLog("LiveChatClient", "OnRecvVideo() callback ok");
		}

		ReleaseEnv(isAttachThread);
	}
	virtual void OnRecvShowVideo(const string& toId, const string& fromId, const string& fromName, const string& inviteId, const string& videoId, const string& sendId, bool charge, const string& videoDesc, int ticket)
	{

	}
	virtual void OnRecvLadyVoiceCode(const string& voiceCode)
	{
		// 仅女士端
	}
	virtual void OnRecvIdentifyCode(const unsigned char* data, long dataLen)
	{
		// 仅女士端
	}
	virtual void OnRecvAutoInviteMsg(const string& womanId, const string& manId, const string& key)
	{
		JNIEnv* env = NULL;
		bool isAttachThread = false;
		bool isGetEnvSuccess = GetEnv(&env, &isAttachThread);

		FileLog("LiveChatClient", "OnRecvAutoInviteMsg() callback, env:%p, isAttachThread:%d, isGetEnvSuccess:%d"
				, env, isAttachThread, isGetEnvSuccess);

		jclass jCallbackCls = env->GetObjectClass(gListener);
		string signure = "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V";
		jmethodID jCallback = env->GetMethodID(jCallbackCls, "OnRecvAutoInviteMsg", signure.c_str());
		if (NULL != gListener && NULL != jCallback)
		{
			FileLog("LiveChatClient", "OnRecvAutoInviteMsg() callback now");

			jstring jwomanId = env->NewStringUTF(womanId.c_str());
			jstring jmanId = env->NewStringUTF(manId.c_str());
			jstring jkey = env->NewStringUTF(key.c_str());

			env->CallVoidMethod(gListener, jCallback, jwomanId, jmanId, jkey);

			env->DeleteLocalRef(jwomanId);
			env->DeleteLocalRef(jmanId);
			env->DeleteLocalRef(jkey);

			FileLog("LiveChatClient", "OnRecvAutoInviteMsg() callback ok");
		}

		ReleaseEnv(isAttachThread);
	}
	virtual void OnRecvAutoChargeResult(const string& manId, double money, TAUTO_CHARGE_TYPE type, bool result, const string& code, const string& msg)
	{
		JNIEnv* env = NULL;
		bool isAttachThread = false;
		GetEnv(&env, &isAttachThread);

		FileLog("LiveChatClient", "OnRecvAutoChargeResult() callback, env:%p, isAttachThread:%d", env, isAttachThread);

		jclass jCallbackCls = env->GetObjectClass(gListener);
		string signure = "(Ljava/lang/String;DIZLjava/lang/String;Ljava/lang/String;)V";
		jmethodID jCallback = env->GetMethodID(jCallbackCls, "OnRecvAutoChargeResult", signure.c_str());
		if (NULL != gListener && NULL != jCallback)
		{
			FileLog("LiveChatClient", "OnRecvAutoChargeResult() callback now");

			jstring jmanId = env->NewStringUTF(manId.c_str());
			jdouble jmoney = money;
			jint jtype = AutoChargeTypeToInt(type);
			jstring jcode = env->NewStringUTF(code.c_str());
			jstring jmsg = env->NewStringUTF(msg.c_str());

			env->CallVoidMethod(gListener, jCallback, jmanId, jmoney, jtype, result, jcode, jmsg);

			env->DeleteLocalRef(jmanId);
			env->DeleteLocalRef(jcode);
			env->DeleteLocalRef(jmsg);

			FileLog("LiveChatClient", "OnRecvAutoChargeResult() callback ok");
		}

		ReleaseEnv(isAttachThread);
	}
	virtual void OnRecvMagicIcon(const string& toId, const string& fromId, const string& fromName, const string& inviteId, bool charge, int ticket, TALK_MSG_TYPE msgType, const string& iconId)
	{
		JNIEnv* env = NULL;
		bool isAttachThread = false;
		GetEnv(&env, &isAttachThread);

		FileLog("LiveChatClient", "OnRecvMagicIcon() callback, toId:%s, fromId:%s, fromName:%s, inviteId:%s"
					", charge:%d, ticket:%d, msgType:%d, iconId:%s, env:%p, isAttachThread:%d"
					, toId.c_str(), fromId.c_str(), fromName.c_str(), inviteId.c_str()
					, charge, ticket, msgType, iconId.c_str(), env, isAttachThread);

		jclass jCallbackCls = env->GetObjectClass(gListener);
		string signure = "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;ZIILjava/lang/String;)V";
		jmethodID jCallback = env->GetMethodID(jCallbackCls, "OnRecvMagicIcon", signure.c_str());
		if (NULL != gListener && NULL != jCallback)
		{
			FileLog("LiveChatClient", "OnRecvMagicIcon() callback now");

			jstring jtoId = env->NewStringUTF(toId.c_str());
			jstring jfromId = env->NewStringUTF(fromId.c_str());
			jstring jfromName = env->NewStringUTF(fromName.c_str());
			jstring jinviteId = env->NewStringUTF(inviteId.c_str());
			int iMsgType = TalkMsgTypeToInt(msgType);
			jstring jmagicIconId = env->NewStringUTF(iconId.c_str());

			env->CallVoidMethod(gListener, jCallback, jtoId, jfromId, jfromName, jinviteId, charge, ticket, iMsgType, jmagicIconId);

			env->DeleteLocalRef(jtoId);
			env->DeleteLocalRef(jfromId);
			env->DeleteLocalRef(jfromName);
			env->DeleteLocalRef(jinviteId);
			env->DeleteLocalRef(jmagicIconId);

			FileLog("LiveChatClient", "OnRecvMagicIcon() callback ok");
		}

		ReleaseEnv(isAttachThread);
	}
};
static LiveChatClientListener g_listener;

// -------------- jni function ----------------
bool GetEnv(JNIEnv** env, bool* isAttachThread)
{
	bool result = false;

	*isAttachThread = false;

	FileLog("LiveChatClient", "GetEnv() begin, env:%p", env);
	jint iRet = JNI_ERR;
	iRet = gJavaVM->GetEnv((void**)env, JNI_VERSION_1_4);
	FileLog("LiveChatClient", "GetEnv() gJavaVM->GetEnv(&gEnv, JNI_VERSION_1_4), iRet:%d", iRet);
	if( iRet == JNI_EDETACHED ) {
		iRet = gJavaVM->AttachCurrentThread(env, NULL);
		*isAttachThread = (iRet == JNI_OK);
	}

	result = (iRet == JNI_OK);
	FileLog("LiveChatClient", "GetEnv() end, env:%p, gIsAttachThread:%d, iRet:%d", *env, *isAttachThread, iRet);

	return result;
}

bool ReleaseEnv(bool isAttachThread)
{
	FileLog("LiveChatClient", "ReleaseEnv(bool) begin, isAttachThread:%d", isAttachThread);
	if (isAttachThread) {
		gJavaVM->DetachCurrentThread();
	}
	FileLog("LiveChatClient", "ReleaseEnv(bool) end");
	return true;
}

/* JNI_OnLoad */
jint JNI_OnLoad(JavaVM* vm, void* reserved) {
//	FileLog("httprequest.Gobal.Native", "JNI_OnLoad( httprequest.so JNI_OnLoad )");
	gJavaVM = vm;

	// Get JNI
	JNIEnv* env;
	if (JNI_OK != vm->GetEnv(reinterpret_cast<void**> (&env),
                           JNI_VERSION_1_4)) {
//		FileLog("httprequest.Gobal.Native", "JNI_OnLoad ( could not get JNI env )");
		return -1;
	}

	FileLog("LiveChatClient", "JNI_OnLoad()");

	// 回调Object索引表
	InsertJObjectClassToMap(env, LIVECHAT_USERSTATUS_CLASS);
	InsertJObjectClassToMap(env, LIVECHAT_TALKSESSIONLISTTIME_CLASS);
	InsertJObjectClassToMap(env, LIVECHAT_TALKUSERLISTTIME_CLASS);
	InsertJObjectClassToMap(env, LIVECHAT_TALKLISTINFO_CLASS);
	InsertJObjectClassToMap(env, LIVECHAT_LADYCONDITION_CLASS);
	InsertJObjectClassToMap(env, LIVECHAT_PAID_THEME_INFO);
	return JNI_VERSION_1_4;
}

/*
 * Class:     com_qpidnetwork_livechat_jni_LiveChatClient
 * Method:    SetLogDirectory
 * Signature: (Ljava/lang/String;)V
 */
JNIEXPORT void JNICALL Java_com_qpidnetwork_livechat_jni_LiveChatClient_SetLogDirectory
  (JNIEnv *env, jclass cls, jstring directory)
{
	string strDirectory = GetJString(env, directory);
	KLog::SetLogDirectory(strDirectory);
}

/*
 * Class:     com_qpidnetwork_livechat_jni_LiveChatClient
 * Method:    Init
 * Signature: (Lcom/qpidnetwork/livechat/LiveChatClientListener;[Ljava/lang/String;I)Z
 */
JNIEXPORT jboolean JNICALL Java_com_qpidnetwork_livechat_jni_LiveChatClient_Init
  (JNIEnv *env, jclass cls, jobject listener, jobjectArray ipArray, jint port)
{
	bool result  = false;

	FileLog("LiveChatClient", "Init() listener:%p, ipArray:%p, port:%d", listener, ipArray, port);

	FileLog("LiveChatClient", "Init() ILiveChatClient::ReleaseClient(g_liveChatClient) g_liveChatClient:%p", g_liveChatClient);

	// 释放旧的LiveChatClient
	ILiveChatClient::ReleaseClient(g_liveChatClient);
	g_liveChatClient = NULL;

	FileLog("LiveChatClient", "Init() env->DeleteGlobalRef(gListener) gListener:%p", gListener);

	// 释放旧的listener
	if (NULL != gListener) {
		env->DeleteGlobalRef(gListener);
		gListener = NULL;
	}

	FileLog("LiveChatClient", "Init() get ip array");

	// 获取IP列表
	list<string> svrIPs;
	if (NULL != ipArray) {
		for (int i = 0; i < env->GetArrayLength(ipArray); i++) {
			jstring ip = (jstring)env->GetObjectArrayElement(ipArray, i);
			string strIP = GetJString(env, ip);
			if (!strIP.empty()) {
				svrIPs.push_back(strIP);
			}
		}
	}

	FileLog("LiveChatClient", "Init() create client");

	if (NULL != listener
		&& !svrIPs.empty()
		&& port > 0)
	{
		if (NULL == g_liveChatClient) {
			g_liveChatClient = ILiveChatClient::CreateClient();
			FileLog("LiveChatClient", "Init() ILiveChatClient::CreateClient() g_liveChatClient:%p", g_liveChatClient);
		}

		if (NULL != g_liveChatClient) {
			gListener = env->NewGlobalRef(listener);
			FileLog("LiveChatClient", "Init() gListener:%p", gListener);
			result = g_liveChatClient->Init(svrIPs, (unsigned int)port, &g_listener);
		}
	}

	FileLog("LiveChatClient", "Init() result: %d", result);
	return result;
}

/*
 * Class:     com_qpidnetwork_livechat_jni_LiveChatClient
 * Method:    Login
 * Signature: (Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;I)Z
 */
JNIEXPORT jboolean JNICALL Java_com_qpidnetwork_livechat_jni_LiveChatClient_Login
  (JNIEnv *env, jclass cls, jstring user, jstring password, jstring deviceId, jint clientType, jint sexType)
{
	bool result = false;

	FileLog("LiveChatClient", "Login()");

	if (NULL != g_liveChatClient) {
		string strUser = GetJString(env, user);
		string strPassword = GetJString(env, password);
		string strDeviceId = GetJString(env, deviceId);
		gDeviceId = strDeviceId;
		CLIENT_TYPE eClientType = IntToClientType(clientType);
		USER_SEX_TYPE eSexType = IntToUserSexType(sexType);

		FileLog("LiveChatClient", "Login() user:%s, password:%s, deviceId:%s, clientType:%d, sexType:%d"
				, strUser.c_str(), strPassword.c_str(), strDeviceId.c_str(), eClientType, eSexType);

		result = g_liveChatClient->Login(strUser, strPassword, strDeviceId, eClientType, eSexType);
	}

	FileLog("LiveChatClient", "Login() result:%d", result);

	return result;
}

/*
 * Class:     com_qpidnetwork_livechat_jni_LiveChatClient
 * Method:    Logout
 * Signature: ()Z
 */
JNIEXPORT jboolean JNICALL Java_com_qpidnetwork_livechat_jni_LiveChatClient_Logout
  (JNIEnv *env, jclass cls)
{
	bool result = false;

	FileLog("LiveChatClient", "Logout() begin");

	if (NULL != g_liveChatClient) {
		result =  g_liveChatClient->Logout();
	}

	FileLog("LiveChatClient", "Logout() end");

	return result;
}

/*
 * Class:     com_qpidnetwork_livechat_jni_LiveChatClient
 * Method:    SetStatus
 * Signature: (I)Z
 */
JNIEXPORT jboolean JNICALL Java_com_qpidnetwork_livechat_jni_LiveChatClient_SetStatus
  (JNIEnv *env, jclass cls, jint status)
{
	if (NULL == g_liveChatClient) {
		return false;
	}

	USER_STATUS_TYPE eStatus = IntToUserStatusType(status);
	return g_liveChatClient->SetStatus(eStatus);
}

/*
 * Class:     com_qpidnetwork_livechat_jni_LiveChatClient
 * Method:    EndTalk
 * Signature: (Ljava/lang/String;)Z
 */
JNIEXPORT jboolean JNICALL Java_com_qpidnetwork_livechat_jni_LiveChatClient_EndTalk
  (JNIEnv *env, jclass cls, jstring userId)
{
	if (NULL == g_liveChatClient) {
		return false;
	}

	string strUserId = GetJString(env, userId);
	return g_liveChatClient->EndTalk(strUserId);
}

/*
 * Class:     com_qpidnetwork_livechat_jni_LiveChatClient
 * Method:    GetUserStatus
 * Signature: ([Ljava/lang/String;)Z
 */
JNIEXPORT jboolean JNICALL Java_com_qpidnetwork_livechat_jni_LiveChatClient_GetUserStatus
  (JNIEnv *env, jclass cls, jobjectArray userIdArray)
{
	if (NULL == g_liveChatClient) {
		return false;
	}

	UserIdList userIdList;
	if (NULL != userIdArray) {
		for (int i = 0; i < env->GetArrayLength(userIdArray); i++) {
			jstring userId = (jstring)env->GetObjectArrayElement(userIdArray, i);
			string strUserId = GetJString(env, userId);
			if (!strUserId.empty()) {
				userIdList.push_back(strUserId);
			}
		}
	}

	return g_liveChatClient->GetUserStatus(userIdList);
}

/*
 * Class:     com_qpidnetwork_livechat_jni_LiveChatClient
 * Method:    GetTalkInfo
 * Signature: (Ljava/lang/String;)Z
 */
JNIEXPORT jboolean JNICALL Java_com_qpidnetwork_livechat_jni_LiveChatClient_GetTalkInfo
  (JNIEnv *env, jclass cls, jstring userId)
{
	if (NULL == g_liveChatClient) {
		return false;
	}

	string strUserId = GetJString(env, userId);
	return g_liveChatClient->GetTalkInfo(strUserId);
}

/*
 * Class:     com_qpidnetwork_livechat_jni_LiveChatClient
 * Method:    UploadTicket
 * Signature: (Ljava/lang/String;I)Z
 */
JNIEXPORT jboolean JNICALL Java_com_qpidnetwork_livechat_jni_LiveChatClient_UploadTicket
  (JNIEnv *env, jclass cls, jstring userId, jint ticket)
{
	if (NULL == g_liveChatClient) {
		return false;
	}

	string strUserId = GetJString(env, userId);
	return g_liveChatClient->UploadTicket(strUserId, ticket);
}

/*
 * Class:     com_qpidnetwork_livechat_jni_LiveChatClient
 * Method:    SendMessage
 * Signature: (Ljava/lang/String;Ljava/lang/String;ZI)Z
 */
JNIEXPORT jboolean JNICALL Java_com_qpidnetwork_livechat_jni_LiveChatClient_SendMessage
  (JNIEnv *env, jclass cls, jstring userId, jstring message, jboolean illegal, jint ticket)
{
	if (NULL == g_liveChatClient) {
		return false;
	}

	string strUserId = GetJString(env, userId);
	string strMessage = GetJString(env, message);
	return g_liveChatClient->SendTextMessage(strUserId, strMessage, illegal, ticket);
}

/*
 * Class:     com_qpidnetwork_livechat_jni_LiveChatClient
 * Method:    SendEmotion
 * Signature: (Ljava/lang/String;Ljava/lang/String;I)Z
 */
JNIEXPORT jboolean JNICALL Java_com_qpidnetwork_livechat_jni_LiveChatClient_SendEmotion
  (JNIEnv *env, jclass cls, jstring userId, jstring emotionId, jint ticket)
{
	if (NULL == g_liveChatClient) {
		return false;
	}

	string strUserId = GetJString(env, userId);
	string strEmotionId = GetJString(env, emotionId);
	return g_liveChatClient->SendEmotion(strUserId, strEmotionId, ticket);
}

/*
 * Class:     com_qpidnetwork_livechat_jni_LiveChatClient
 * Method:    SendVGift
 * Signature: (Ljava/lang/String;Ljava/lang/String;)Z
 */
JNIEXPORT jboolean JNICALL Java_com_qpidnetwork_livechat_jni_LiveChatClient_SendVGift
  (JNIEnv *env, jclass cls, jstring userId, jstring giftId, jint ticket)
{
	if (NULL == g_liveChatClient) {
		return false;
	}

	string strUserId = GetJString(env, userId);
	string strGiftId = GetJString(env, giftId);
	return g_liveChatClient->SendVGift(strUserId, strGiftId, ticket);
}

/*
 * Class:     com_qpidnetwork_livechat_jni_LiveChatClient
 * Method:    GetVoiceCode
 * Signature: (Ljava/lang/String;I)Z
 */
JNIEXPORT jboolean JNICALL Java_com_qpidnetwork_livechat_jni_LiveChatClient_GetVoiceCode
  (JNIEnv *env, jclass cls, jstring userId, jint ticket)
{
	if (NULL == g_liveChatClient) {
		return false;
	}

	string strUserId = GetJString(env, userId);
	return g_liveChatClient->GetVoiceCode(strUserId, ticket);
}

/*
 * Class:     com_qpidnetwork_livechat_jni_LiveChatClient
 * Method:    SendVoice
 * Signature: (Ljava/lang/String;Ljava/lang/String;II)Z
 */
JNIEXPORT jboolean JNICALL Java_com_qpidnetwork_livechat_jni_LiveChatClient_SendVoice
  (JNIEnv *env, jclass cls, jstring userId, jstring voiceId, jint voiceLength, jint ticket)
{
	if (NULL == g_liveChatClient) {
		return false;
	}

	string strUserId = GetJString(env, userId);
	string strVoiceId = GetJString(env, voiceId);
	return g_liveChatClient->SendVoice(strUserId, strVoiceId, voiceLength, ticket);
}

/*
 * Class:     com_qpidnetwork_livechat_jni_LiveChatClient
 * Method:    SendMagicIcon
 * Signature: (Ljava/lang/String;Ljava/lang/String;I)Z
 */
JNIEXPORT jboolean JNICALL Java_com_qpidnetwork_livechat_jni_LiveChatClient_SendMagicIcon
  (JNIEnv *env, jclass cls, jstring userId, jstring magicIconId, jint ticket){
	if (NULL == g_liveChatClient) {
		return false;
	}

	string strUserId = GetJString(env, userId);
	string strMagicIconId = GetJString(env, magicIconId);
	return g_liveChatClient->SendMagicIcon(strUserId, strMagicIconId, ticket);
}

/*
 * Class:     com_qpidnetwork_livechat_jni_LiveChatClient
 * Method:    UseTryTicket
 * Signature: (Ljava/lang/String;)Z
 */
JNIEXPORT jboolean JNICALL Java_com_qpidnetwork_livechat_jni_LiveChatClient_UseTryTicket
  (JNIEnv *env, jclass cls, jstring userId)
{
	if (NULL == g_liveChatClient) {
		return false;
	}

	string strUserId = GetJString(env, userId);
	return g_liveChatClient->UseTryTicket(strUserId);
}

/*
 * Class:     com_qpidnetwork_livechat_jni_LiveChatClient
 * Method:    GetTalkList
 * Signature: (I)Z
 */
JNIEXPORT jboolean JNICALL Java_com_qpidnetwork_livechat_jni_LiveChatClient_GetTalkList
  (JNIEnv *env, jclass cls, jint listType)
{
	if (NULL == g_liveChatClient) {
		return false;
	}

	return g_liveChatClient->GetTalkList(listType);
}

/*
 * Class:     com_qpidnetwork_livechat_jni_LiveChatClient
 * Method:    SendPhoto
 * Signature: (Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;ZLjava/lang/String;I)Z
 */
JNIEXPORT jboolean JNICALL Java_com_qpidnetwork_livechat_jni_LiveChatClient_SendPhoto
  (JNIEnv *env, jclass cls, jstring userId, jstring inviteId, jstring photoId, jstring sendId, jboolean charge, jstring photoDesc, jint ticket)
{
	if (NULL == g_liveChatClient) {
		return false;
	}

	string strUserId = GetJString(env, userId);
	string strInviteId = GetJString(env, inviteId);
	string strPhotoId = GetJString(env, photoId);
	string strSendId = GetJString(env, sendId);
	string strPhotoDesc = GetJString(env, photoDesc);
	return g_liveChatClient->SendPhoto(strUserId, strInviteId, strPhotoId, strSendId, charge, strPhotoDesc, ticket);
}

/*
 * Class:     com_qpidnetwork_livechat_jni_LiveChatClient
 * Method:    ShowPhoto
 * Signature: (Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;ZLjava/lang/String;I)Z
 */
JNIEXPORT jboolean JNICALL Java_com_qpidnetwork_livechat_jni_LiveChatClient_ShowPhoto
  (JNIEnv *env, jclass cls, jstring userId, jstring inviteId, jstring photoId, jstring sendId, jboolean charge, jstring photoDesc, jint ticket)
{
	if (NULL == g_liveChatClient) {
		return false;
	}

	string strUserId = GetJString(env, userId);
	string strInviteId = GetJString(env, inviteId);
	string strPhotoId = GetJString(env, photoId);
	string strSendId = GetJString(env, sendId);
	string strPhotoDesc = GetJString(env, photoDesc);
	return g_liveChatClient->ShowPhoto(strUserId, strInviteId, strPhotoId, strSendId, charge, strPhotoDesc, ticket);
}

/*
 * Class:     com_qpidnetwork_livechat_jni_LiveChatClient
 * Method:    PlayVideo
 * Signature: (Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;ZLjava/lang/String;I)Z
 */
JNIEXPORT jboolean JNICALL Java_com_qpidnetwork_livechat_jni_LiveChatClient_PlayVideo
  (JNIEnv *env, jclass cls, jstring userId, jstring inviteId, jstring videoId, jstring sendId, jboolean charge, jstring videoDesc, jint ticket)
{
	if (NULL == g_liveChatClient) {
		return false;
	}

	string strUserId = GetJString(env, userId);
	string strInviteId = GetJString(env, inviteId);
	string strVideoId = GetJString(env, videoId);
	string strSendId = GetJString(env, sendId);
	string strVideoDesc = GetJString(env, videoDesc);
	return g_liveChatClient->PlayVideo(strUserId, strInviteId, strVideoId, strSendId, charge, strVideoDesc, ticket);
}

/*
 * Class:     com_qpidnetwork_livechat_jni_LiveChatClient
 * Method:    GetUserInfo
 * Signature: (Ljava/lang/String;)Z
 */
JNIEXPORT jboolean JNICALL Java_com_qpidnetwork_livechat_jni_LiveChatClient_GetUserInfo
  (JNIEnv *env, jclass cls, jstring userId)
{
	if (NULL == g_liveChatClient) {
		return false;
	}

	string strUserId = GetJString(env, userId);
	return g_liveChatClient->GetUserInfo(strUserId);
}

/*
 * Class:     com_qpidnetwork_livechat_jni_LiveChatClient
 * Method:    GetUsersInfo
 * Signature: ([Ljava/lang/String;)Z
 */
JNIEXPORT jboolean JNICALL Java_com_qpidnetwork_livechat_jni_LiveChatClient_GetUsersInfo
  (JNIEnv *env, jclass cls, jobjectArray userIds)
{
	if (NULL == g_liveChatClient) {
		return false;
	}

	// 获取user Id列表
	list<string> userIdList;
	if (NULL != userIds) {
		for (int i = 0; i < env->GetArrayLength(userIds); i++) {
			jstring userId = (jstring)env->GetObjectArrayElement(userIds, i);
			string strUserId = GetJString(env, userId);
			if (!strUserId.empty()) {
				userIdList.push_back(strUserId);
			}
		}
	}

	return g_liveChatClient->GetUsersInfo(userIdList);
}

/*
 * Class:     com_qpidnetwork_livechat_jni_LiveChatClient
 * Method:    GetBlockList
 * Signature: ()Z
 */
JNIEXPORT jboolean JNICALL Java_com_qpidnetwork_livechat_jni_LiveChatClient_GetBlockList
  (JNIEnv *env, jclass cls)
{
	if (NULL == g_liveChatClient) {
		return false;
	}

	return g_liveChatClient->GetContactList(CONTACT_LIST_BLOCK);
}

/*
 * Class:     com_qpidnetwork_livechat_jni_LiveChatClient
 * Method:    GetContactList
 * Signature: ()Z
 */
JNIEXPORT jboolean JNICALL Java_com_qpidnetwork_livechat_jni_LiveChatClient_GetContactList
  (JNIEnv *env, jclass cls)
{
	if (NULL == g_liveChatClient) {
		return false;
	}

	return g_liveChatClient->GetContactList(CONTACT_LIST_CONTACT);
}

/*
 * Class:     com_qpidnetwork_livechat_jni_LiveChatClient
 * Method:    UploadVer
 * Signature: (Ljava/lang/String;)Z
 */
JNIEXPORT jboolean JNICALL Java_com_qpidnetwork_livechat_jni_LiveChatClient_UploadVer
  (JNIEnv *env, jclass cls, jstring version)
{
	if (NULL == g_liveChatClient) {
		return false;
	}

	string strVersion = GetJString(env, version);
	return g_liveChatClient->UploadVer(strVersion);
}

/*
 * Class:     com_qpidnetwork_livechat_jni_LiveChatClient
 * Method:    GetBlockUsers
 * Signature: ()Z
 */
JNIEXPORT jboolean JNICALL Java_com_qpidnetwork_livechat_jni_LiveChatClient_GetBlockUsers
  (JNIEnv *env, jclass cls)
{
	if (NULL == g_liveChatClient) {
		return false;
	}

	return g_liveChatClient->GetBlockUsers();
}

/*
 * Class:     com_qpidnetwork_livechat_jni_LiveChatClient
 * Method:    GetLadyCondition
 * Signature: (Ljava/lang/String;)Z
 */
JNIEXPORT jboolean JNICALL Java_com_qpidnetwork_livechat_jni_LiveChatClient_GetLadyCondition
  (JNIEnv *env, jclass cls, jstring userId)
{
	if (NULL == g_liveChatClient) {
		return false;
	}

	string strUserId = GetJString(env, userId);
	return g_liveChatClient->GetLadyCondition(strUserId);
}

/*
 * Class:     com_qpidnetwork_livechat_jni_LiveChatClient
 * Method:    GetLadyCustomTemplate
 * Signature: (Ljava/lang/String;)Z
 */
JNIEXPORT jboolean JNICALL Java_com_qpidnetwork_livechat_jni_LiveChatClient_GetLadyCustomTemplate
  (JNIEnv *env, jclass cls, jstring userId)
{
	if (NULL == g_liveChatClient) {
		return false;
	}

	string strUserId = GetJString(env, userId);
	return g_liveChatClient->GetLadyCustomTemplate(strUserId);
}

/*
 * Class:     com_qpidnetwork_livechat_jni_LiveChatClient
 * Method:    UploadPopLadyAutoInvite
 * Signature: (Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)Z
 */
JNIEXPORT jboolean JNICALL Java_com_qpidnetwork_livechat_jni_LiveChatClient_UploadPopLadyAutoInvite
  (JNIEnv *env, jclass cls, jstring userId, jstring msg, jstring key)
{
	if (NULL == g_liveChatClient) {
		return false;
	}

	string strUserId = GetJString(env, userId);
	string strMsgId = GetJString(env, msg);
	string strKey = GetJString(env, key);
	return g_liveChatClient->UploadPopLadyAutoInvite(strUserId, strMsgId, strKey);
}

/*
 * Class:     com_qpidnetwork_livechat_jni_LiveChatClient
 * Method:    UploadAutoChargeStatus
 * Signature: (Z)Z
 */
JNIEXPORT jboolean JNICALL Java_com_qpidnetwork_livechat_jni_LiveChatClient_UploadAutoChargeStatus
  (JNIEnv *env, jclass cls, jboolean isCharge)
{
	if (NULL == g_liveChatClient) {
		return false;
	}

	return g_liveChatClient->UploadAutoChargeStatus(isCharge);
}

/*
 * Class:     com_qpidnetwork_livechat_jni_LiveChatClient
 * Method:    GetPaidTheme
 * Signature: (Ljava/lang/String;)Z
 */
JNIEXPORT jboolean JNICALL Java_com_qpidnetwork_livechat_jni_LiveChatClient_GetPaidTheme
  (JNIEnv *env, jclass cls, jstring userId){
	if (NULL == g_liveChatClient) {
		return false;
	}
	string strUserId = GetJString(env, userId);
	return g_liveChatClient->GetPaidTheme(strUserId);
}

/*
 * Class:     com_qpidnetwork_livechat_jni_LiveChatClient
 * Method:    GetAllPaidTheme
 * Signature: ()Z
 */
JNIEXPORT jboolean JNICALL Java_com_qpidnetwork_livechat_jni_LiveChatClient_GetAllPaidTheme
  (JNIEnv *env, jclass cls){
	if (NULL == g_liveChatClient) {
		return false;
	}
	return g_liveChatClient->GetAllPaidTheme();
}

/*
 * Class:     com_qpidnetwork_livechat_jni_LiveChatClient
 * Method:    UploadThemeListVer
 * Signature: (I)Z
 */
JNIEXPORT jboolean JNICALL Java_com_qpidnetwork_livechat_jni_LiveChatClient_UploadThemeListVer
  (JNIEnv *env, jclass cls, jint themeVer){
	if (NULL == g_liveChatClient) {
		return false;
	}
	return g_liveChatClient->UploadThemeListVer(themeVer);
}

/*
 * Class:     com_qpidnetwork_livechat_jni_LiveChatClient
 * Method:    ManFeeTheme
 * Signature: (Ljava/lang/String;Ljava/lang/String;)Z
 */
JNIEXPORT jboolean JNICALL Java_com_qpidnetwork_livechat_jni_LiveChatClient_ManFeeTheme
  (JNIEnv *env, jclass cls, jstring userId, jstring themeId){
	if (NULL == g_liveChatClient) {
		return false;
	}
	string strUserId = GetJString(env, userId);
	string strThemeId = GetJString(env, themeId);
	return g_liveChatClient->ManFeeTheme(strUserId, strThemeId);
}

/*
 * Class:     com_qpidnetwork_livechat_jni_LiveChatClient
 * Method:    ManApplyTheme
 * Signature: (Ljava/lang/String;Ljava/lang/String;)Z
 */
JNIEXPORT jboolean JNICALL Java_com_qpidnetwork_livechat_jni_LiveChatClient_ManApplyTheme
  (JNIEnv *env, jclass cls, jstring userId, jstring themeId){
	if (NULL == g_liveChatClient) {
		return false;
	}
	string strUserId = GetJString(env, userId);
	string strThemeId = GetJString(env, themeId);
	return g_liveChatClient->ManApplyTheme(strUserId, strThemeId);
}

/*
 * Class:     com_qpidnetwork_livechat_jni_LiveChatClient
 * Method:    PlayThemeMotion
 * Signature: (Ljava/lang/String;Ljava/lang/String;)Z
 */
JNIEXPORT jboolean JNICALL Java_com_qpidnetwork_livechat_jni_LiveChatClient_PlayThemeMotion
  (JNIEnv *env, jclass cls, jstring userId, jstring themeId){
	if (NULL == g_liveChatClient) {
		return false;
	}
	string strUserId = GetJString(env, userId);
	string strThemeId = GetJString(env, themeId);
	return g_liveChatClient->PlayThemeMotion(strUserId, strThemeId);
}

