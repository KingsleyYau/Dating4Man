/*
 * RequestJni_GobalFunc.cpp
 *
 *  Created on: 2015-3-4
 *      Author: Max.Chiu
 *      Email: Kingsleyyau@gmail.com
 */

#include "com_qpidnetwork_request_RequestJni_GobalFunc.h"

JavaVM* gJavaVM;

CallbackMap gCallbackMap;

JavaItemMap gJavaItemMap;

HttpRequestManager gHttpRequestManager;

HttpRequestHostManager gHttpRequestHostManager;

string JString2String(JNIEnv* env, jstring str) {
	string result("");
	if (NULL != str) {
		const char* cpTemp = env->GetStringUTFChars(str, 0);
		result = cpTemp;
		env->ReleaseStringUTFChars(str, cpTemp);
	}
	return result;
}

void InitEnumHelper(JNIEnv *env, const char *path, jobject *objptr) {
	FileLog("httprequest", "InitEnumHelper( path : %s )", path);
    jclass cls = env->FindClass(path);
    if( !cls ) {
    	FileLog("httprequest", "InitEnumHelper( !cls )");
        return;
    }

    jmethodID constr = env->GetMethodID(cls, "<init>", "(Ljava/lang/String;I)V");
    if( !constr ) {
    	FileLog("httprequest", "InitEnumHelper( !constr )");
        return;
    }

    jobject obj = env->NewObject(cls, constr, NULL, 0);
    if( !obj ) {
    	FileLog("httprequest", "InitEnumHelper( !obj )");
        return;
    }

    (*objptr) = env->NewGlobalRef(obj);
}

void InitClassHelper(JNIEnv *env, const char *path, jobject *objptr) {
	FileLog("httprequest", "InitClassHelper( path : %s )", path);
    jclass cls = env->FindClass(path);
    if( !cls ) {
    	FileLog("httprequest", "InitClassHelper( !cls )");
        return;
    }

    jmethodID constr = env->GetMethodID(cls, "<init>", "()V");
    if( !constr ) {
    	FileLog("httprequest", "InitClassHelper( !constr )");
        constr = env->GetMethodID(cls, "<init>", "(Ljava/lang/String;I)V");
        if( !constr ) {
        	FileLog("httprequest", "InitClassHelper( !constr )");
            return;
        }
        return;
    }

    jobject obj = env->NewObject(cls, constr);
    if( !obj ) {
    	FileLog("httprequest", "InitClassHelper( !obj )");
        return;
    }

    (*objptr) = env->NewGlobalRef(obj);
}

jclass GetJClass(JNIEnv* env, const char* classPath)
{
	jclass jCls = NULL;
	JavaItemMap::iterator itr = gJavaItemMap.find(classPath);
	if( itr != gJavaItemMap.end() ) {
		jobject jItemObj = itr->second;
		jCls = env->GetObjectClass(jItemObj);
	}
	return jCls;
}

/* JNI_OnLoad */
jint JNI_OnLoad(JavaVM* vm, void* reserved) {
	FileLog("httprequest", "JNI_OnLoad( httprequest.so JNI_OnLoad )");
	gJavaVM = vm;

	// Get JNI
	JNIEnv* env;
	if (JNI_OK != vm->GetEnv(reinterpret_cast<void**> (&env),
                           JNI_VERSION_1_4)) {
		FileLog("httprequest", "JNI_OnLoad ( could not get JNI env )");
		return -1;
	}

	HttpClient::Init();

	gHttpRequestManager.SetHostManager(&gHttpRequestHostManager);

	//	InitEnumHelper(env, COUNTRY_ITEM_CLASS, &gCountryItem);

	/* 1.认证模块 */
	jobject jLoginFacebookItem;
	InitClassHelper(env, LOGIN_FACEBOOK_ITEM_CLASS, &jLoginFacebookItem);
	gJavaItemMap.insert(JavaItemMap::value_type(LOGIN_FACEBOOK_ITEM_CLASS, jLoginFacebookItem));

	jobject jRegisterItem;
	InitClassHelper(env, REGISTER_ITEM_CLASS, &jRegisterItem);
	gJavaItemMap.insert(JavaItemMap::value_type(REGISTER_ITEM_CLASS, jRegisterItem));

	jobject jLoginItem;
	InitClassHelper(env, LOGIN_ITEM_CLASS, &jLoginItem);
	gJavaItemMap.insert(JavaItemMap::value_type(LOGIN_ITEM_CLASS, jLoginItem));

	jobject jLoginErrItem;
	InitClassHelper(env, LOGIN_ERROR_ITEM_CLASS, &jLoginErrItem);
	gJavaItemMap.insert(JavaItemMap::value_type(LOGIN_ERROR_ITEM_CLASS, jLoginErrItem));

	/* 2.个人信息模块 */
	jobject jProfileItem;
	InitClassHelper(env, PROFILE_ITEM_CLASS, &jProfileItem);
	gJavaItemMap.insert(JavaItemMap::value_type(PROFILE_ITEM_CLASS, jProfileItem));

	/* 4.女士模块 */
	jobject jLadyMatch;
	InitClassHelper(env, LADY_MATCH_ITEM_CLASS, &jLadyMatch);
	gJavaItemMap.insert(JavaItemMap::value_type(LADY_MATCH_ITEM_CLASS, jLadyMatch));

	jobject jLady;
	InitClassHelper(env, LADY_ITEM_CLASS, &jLady);
	gJavaItemMap.insert(JavaItemMap::value_type(LADY_ITEM_CLASS, jLady));

	jobject jLadyDetail;
	InitClassHelper(env, LADY_DETAIL_ITEM_CLASS, &jLadyDetail);
	gJavaItemMap.insert(JavaItemMap::value_type(LADY_DETAIL_ITEM_CLASS, jLadyDetail));

	jobject jLadyVideo;
	InitClassHelper(env, LADY_VIDEO_ITEM_CLASS, &jLadyVideo);
	gJavaItemMap.insert(JavaItemMap::value_type(LADY_VIDEO_ITEM_CLASS, jLadyVideo));

	jobject jLadyCall;
	InitClassHelper(env, LADY_CALL_ITEM_CLASS, &jLadyCall);
	gJavaItemMap.insert(JavaItemMap::value_type(LADY_CALL_ITEM_CLASS, jLadyCall));

	jobject jLadyRecentContact;
	InitClassHelper(env, LADY_RECENTCONTACT_ITEM_CLASS, &jLadyRecentContact);
	gJavaItemMap.insert(JavaItemMap::value_type(LADY_RECENTCONTACT_ITEM_CLASS, jLadyRecentContact));

	jobject jLadySignItem;
	InitClassHelper(env, LADY_SIGN_ITEM_CLASS, &jLadySignItem);
	gJavaItemMap.insert(JavaItemMap::value_type(LADY_SIGN_ITEM_CLASS, jLadySignItem));


	/* 5.live chat 模块 */
	jobject jCoupon;
	InitClassHelper(env, LIVECHAT_COUPON_ITEM_CLASS, &jCoupon);
	gJavaItemMap.insert(JavaItemMap::value_type(LIVECHAT_COUPON_ITEM_CLASS, jCoupon));

	jobject jGift;
	InitClassHelper(env, LIVECHAT_GIFT_ITEM_CLASS, &jGift);
	gJavaItemMap.insert(JavaItemMap::value_type(LIVECHAT_GIFT_ITEM_CLASS, jGift));

	jobject jRecord;
	InitClassHelper(env, LIVECHAT_RECORD_ITEM_CLASS, &jRecord);
	gJavaItemMap.insert(JavaItemMap::value_type(LIVECHAT_RECORD_ITEM_CLASS, jRecord));

	jobject jRecordMutiple;
	InitClassHelper(env, LIVECHAT_RECORD_MUTIPLE_ITEM_CLASS, &jRecordMutiple);
	gJavaItemMap.insert(JavaItemMap::value_type(LIVECHAT_RECORD_MUTIPLE_ITEM_CLASS, jRecordMutiple));

	jobject jSendPhoto;
	InitClassHelper(env, LIVECHAT_SENDPHOTO_TIME_CLASS, &jSendPhoto);
	gJavaItemMap.insert(JavaItemMap::value_type(LIVECHAT_SENDPHOTO_TIME_CLASS, jSendPhoto));

	jobject jLCVideoItem;
	InitClassHelper(env, LIVECHAT_LCVIDEO_TIME_CLASS, &jLCVideoItem);
	gJavaItemMap.insert(JavaItemMap::value_type(LIVECHAT_LCVIDEO_TIME_CLASS, jLCVideoItem));

	jobject jLCMagicConfigItem;
	InitClassHelper(env, LIVECHAT_MAGIC_CONFIG_ITEM_CLASS, &jLCMagicConfigItem);
	gJavaItemMap.insert(JavaItemMap::value_type(LIVECHAT_MAGIC_CONFIG_ITEM_CLASS, jLCMagicConfigItem));

	jobject jLCMagicIconItem;
	InitClassHelper(env, LIVECHAT_MAGIC_ICON_TIME_CLASS, &jLCMagicIconItem);
	gJavaItemMap.insert(JavaItemMap::value_type(LIVECHAT_MAGIC_ICON_TIME_CLASS, jLCMagicIconItem));

	jobject jLCMagicTypeItem;
	InitClassHelper(env, LIVECHAT_MAGIC_TYPE_TIME_CLASS, &jLCMagicTypeItem);
	gJavaItemMap.insert(JavaItemMap::value_type(LIVECHAT_MAGIC_TYPE_TIME_CLASS, jLCMagicTypeItem));

	jobject jLCThemeConfigItem;
	InitClassHelper(env, LIVECHAT_THEME_CONFIG_CLASS, &jLCThemeConfigItem);
	gJavaItemMap.insert(JavaItemMap::value_type(LIVECHAT_THEME_CONFIG_CLASS, jLCThemeConfigItem));

	jobject jLCThemeTypeItem;
	InitClassHelper(env, LIVECHAT_THEME_TYPE_CLASS, &jLCThemeTypeItem);
	gJavaItemMap.insert(JavaItemMap::value_type(LIVECHAT_THEME_TYPE_CLASS, jLCThemeTypeItem));

	jobject jLCThemeTagItem;
	InitClassHelper(env, LIVECHAT_THEME_TAG_CLASS, &jLCThemeTagItem);
	gJavaItemMap.insert(JavaItemMap::value_type(LIVECHAT_THEME_TAG_CLASS, jLCThemeTagItem));

	jobject jLCThemeItem;
	InitClassHelper(env, LIVECHAT_THEME_ITEM_CLASS, &jLCThemeItem);
	gJavaItemMap.insert(JavaItemMap::value_type(LIVECHAT_THEME_ITEM_CLASS, jLCThemeItem));

	/* 6.EMF模块*/
	jobject jEMFPrivatePhotoItem;
	InitClassHelper(env, EMF_PRIVATEPHOTO_ITEM_CLASS, &jEMFPrivatePhotoItem);
	gJavaItemMap.insert(JavaItemMap::value_type(EMF_PRIVATEPHOTO_ITEM_CLASS, jEMFPrivatePhotoItem));

	jobject jEMFShortVideoItem;
	InitClassHelper(env, EMF_SHORTVIDEO_ITEM_CLASS, &jEMFShortVideoItem);
	gJavaItemMap.insert(JavaItemMap::value_type(EMF_SHORTVIDEO_ITEM_CLASS, jEMFShortVideoItem));

	jobject jEMFInboxListItem;
	InitClassHelper(env, EMF_INBOXLIST_ITEM_CLASS, &jEMFInboxListItem);
	gJavaItemMap.insert(JavaItemMap::value_type(EMF_INBOXLIST_ITEM_CLASS, jEMFInboxListItem));

	jobject jEMFInboxMsgItem;
	InitClassHelper(env, EMF_INBOXMSG_ITEM_CLASS, &jEMFInboxMsgItem);
	gJavaItemMap.insert(JavaItemMap::value_type(EMF_INBOXMSG_ITEM_CLASS, jEMFInboxMsgItem));

	jobject jEMFOutboxListItem;
	InitClassHelper(env, EMF_OUTBOXLIST_ITEM_CLASS, &jEMFOutboxListItem);
	gJavaItemMap.insert(JavaItemMap::value_type(EMF_OUTBOXLIST_ITEM_CLASS, jEMFOutboxListItem));

	jobject jEMFOutboxMsgItem;
	InitClassHelper(env, EMF_OUTBOXMSG_ITEM_CLASS, &jEMFOutboxMsgItem);
	gJavaItemMap.insert(JavaItemMap::value_type(EMF_OUTBOXMSG_ITEM_CLASS, jEMFOutboxMsgItem));

	jobject jEMFMsgTotalItem;
	InitClassHelper(env, EMF_MSGTOTAL_ITEM_CLASS, &jEMFMsgTotalItem);
	gJavaItemMap.insert(JavaItemMap::value_type(EMF_MSGTOTAL_ITEM_CLASS, jEMFMsgTotalItem));

	jobject jEMFSendMsgItem;
	InitClassHelper(env, EMF_SENDMSG_ITEM_CLASS, &jEMFSendMsgItem);
	gJavaItemMap.insert(JavaItemMap::value_type(EMF_SENDMSG_ITEM_CLASS, jEMFSendMsgItem));
	jobject jEMFSendMsgErrorItem;
	InitClassHelper(env, EMF_SENDMSG_ERROR_ITEM_CLASS, &jEMFSendMsgErrorItem);
	gJavaItemMap.insert(JavaItemMap::value_type(EMF_SENDMSG_ERROR_ITEM_CLASS, jEMFSendMsgErrorItem));

	jobject jEMFAdmirerListItem;
	InitClassHelper(env, EMF_ADMIRERLIST_ITEM_CLASS, &jEMFAdmirerListItem);
	gJavaItemMap.insert(JavaItemMap::value_type(EMF_ADMIRERLIST_ITEM_CLASS, jEMFAdmirerListItem));

	jobject jEMFAdmirerViewerItem;
	InitClassHelper(env, EMF_ADMIRERVIEWER_ITEM_CLASS, &jEMFAdmirerViewerItem);
	gJavaItemMap.insert(JavaItemMap::value_type(EMF_ADMIRERVIEWER_ITEM_CLASS, jEMFAdmirerViewerItem));

	jobject jEMFBlockListItem;
	InitClassHelper(env, EMF_BLOCKLIST_ITEM_CLASS, &jEMFBlockListItem);
	gJavaItemMap.insert(JavaItemMap::value_type(EMF_BLOCKLIST_ITEM_CLASS, jEMFBlockListItem));

	/* 7.VideoShow模块*/
	jobject jVSVideoListItem;
	InitClassHelper(env, VS_VIDEOLIST_ITEM_CLASS, &jVSVideoListItem);
	gJavaItemMap.insert(JavaItemMap::value_type(VS_VIDEOLIST_ITEM_CLASS, jVSVideoListItem));

	jobject jVSVideoDetailItem;
	InitClassHelper(env, VS_VIDEODETAIL_ITEM_CLASS, &jVSVideoDetailItem);
	gJavaItemMap.insert(JavaItemMap::value_type(VS_VIDEODETAIL_ITEM_CLASS, jVSVideoDetailItem));

	jobject jVSPlayVideoItem;
	InitClassHelper(env, VS_PLAYVIDEO_ITEM_CLASS, &jVSPlayVideoItem);
	gJavaItemMap.insert(JavaItemMap::value_type(VS_PLAYVIDEO_ITEM_CLASS, jVSPlayVideoItem));

	jobject jVSWatchedVideoListItem;
	InitClassHelper(env, VS_WATCHEDVIDEOLIST_ITEM_CLASS, &jVSWatchedVideoListItem);
	gJavaItemMap.insert(JavaItemMap::value_type(VS_WATCHEDVIDEOLIST_ITEM_CLASS, jVSWatchedVideoListItem));

	jobject jVSSavedVideoListItem;
	InitClassHelper(env, VS_SAVEDVIDEOLIST_ITEM_CLASS, &jVSSavedVideoListItem);
	gJavaItemMap.insert(JavaItemMap::value_type(VS_SAVEDVIDEOLIST_ITEM_CLASS, jVSSavedVideoListItem));

	/* 8.Other模块*/
	jobject jOtherEmotionConfigItem;
	InitClassHelper(env, OTHER_EMOTIONCONFIG_ITEM_CLASS, &jOtherEmotionConfigItem);
	gJavaItemMap.insert(JavaItemMap::value_type(OTHER_EMOTIONCONFIG_ITEM_CLASS, jOtherEmotionConfigItem));
	jobject jOtherEmotionConfigTypeItem;
	InitClassHelper(env, OTHER_EMOTIONCONFIG_TYPE_ITEM_CLASS, &jOtherEmotionConfigTypeItem);
	gJavaItemMap.insert(JavaItemMap::value_type(OTHER_EMOTIONCONFIG_TYPE_ITEM_CLASS, jOtherEmotionConfigTypeItem));
	jobject jOtherEmotionConfigTagItem;
	InitClassHelper(env, OTHER_EMOTIONCONFIG_TAG_ITEM_CLASS, &jOtherEmotionConfigTagItem);
	gJavaItemMap.insert(JavaItemMap::value_type(OTHER_EMOTIONCONFIG_TAG_ITEM_CLASS, jOtherEmotionConfigTagItem));
	jobject jOtherEmotionConfigEmotionItem;
	InitClassHelper(env, OTHER_EMOTIONCONFIG_EMOTION_ITEM_CLASS, &jOtherEmotionConfigEmotionItem);
	gJavaItemMap.insert(JavaItemMap::value_type(OTHER_EMOTIONCONFIG_EMOTION_ITEM_CLASS, jOtherEmotionConfigEmotionItem));

	jobject jOtherGetCountItem;
	InitClassHelper(env, OTHER_GETCOUNT_ITEM_CLASS, &jOtherGetCountItem);
	gJavaItemMap.insert(JavaItemMap::value_type(OTHER_GETCOUNT_ITEM_CLASS, jOtherGetCountItem));

	jobject jOtherIntegralCheckItem;
	InitClassHelper(env, OTHER_INTEGRALCHECK_ITEM_CLASS, &jOtherIntegralCheckItem);
	gJavaItemMap.insert(JavaItemMap::value_type(OTHER_INTEGRALCHECK_ITEM_CLASS, jOtherIntegralCheckItem));

	jobject jOtherVersionCheckItem;
	InitClassHelper(env, OTHER_VERSIONCHECK_ITEM_CLASS, &jOtherVersionCheckItem);
	gJavaItemMap.insert(JavaItemMap::value_type(OTHER_VERSIONCHECK_ITEM_CLASS, jOtherVersionCheckItem));

	jobject jOtherSynConfigItem;
	InitClassHelper(env, OTHER_SYNCONFIG_ITEM_CLASS, &jOtherSynConfigItem);
	gJavaItemMap.insert(JavaItemMap::value_type(OTHER_SYNCONFIG_ITEM_CLASS, jOtherSynConfigItem));
	jobject jOtherSynConfigSiteItem;
	InitClassHelper(env, OTHER_SYNCONFIG_SITE_ITEM_CLASS, &jOtherSynConfigSiteItem);
	gJavaItemMap.insert(JavaItemMap::value_type(OTHER_SYNCONFIG_SITE_ITEM_CLASS, jOtherSynConfigSiteItem));
	jobject jOtherSynConfigPublicItem;
	InitClassHelper(env, OTHER_SYNCONFIG_PUBLIC_ITEM_CLASS, &jOtherSynConfigPublicItem);
	gJavaItemMap.insert(JavaItemMap::value_type(OTHER_SYNCONFIG_PUBLIC_ITEM_CLASS, jOtherSynConfigPublicItem));

	jobject jOtherOnlineCountItem;
	InitClassHelper(env, OTHER_ONLINECOUNT_ITEM_CLASS, &jOtherOnlineCountItem);
	gJavaItemMap.insert(JavaItemMap::value_type(OTHER_ONLINECOUNT_ITEM_CLASS, jOtherOnlineCountItem));

	/* 9.广告模块 */
	jobject jAdMainAdvertItem;
	InitClassHelper(env, ADVERT_MAINADVERT_ITEM_CLASS, &jAdMainAdvertItem);
	gJavaItemMap.insert(JavaItemMap::value_type(ADVERT_MAINADVERT_ITEM_CLASS, jAdMainAdvertItem));

	jobject jAdWomanListAdvertItem;
	InitClassHelper(env, ADVERT_WOMANLISTADVERT_ITEM_CLASS, &jAdWomanListAdvertItem);
	gJavaItemMap.insert(JavaItemMap::value_type(ADVERT_WOMANLISTADVERT_ITEM_CLASS, jAdWomanListAdvertItem));

	jobject jAdPushAdvertItem;
	InitClassHelper(env, ADVERT_PUSHADVERT_ITEM_CLASS, &jAdPushAdvertItem);
	gJavaItemMap.insert(JavaItemMap::value_type(ADVERT_PUSHADVERT_ITEM_CLASS, jAdPushAdvertItem));

	/* 10.Quick Match模块 */
	jobject jQuickMatchLadyItem;
	InitClassHelper(env, QUICKMATCH_LADY_ITEM_CLASS, &jQuickMatchLadyItem);
	gJavaItemMap.insert(JavaItemMap::value_type(QUICKMATCH_LADY_ITEM_CLASS, jQuickMatchLadyItem));

	/* 11.Love Call模块 */
	jobject jLoveItem;
	InitClassHelper(env, LOVECALL_ITEM_CLASS, &jLoveItem);
	gJavaItemMap.insert(JavaItemMap::value_type(LOVECALL_ITEM_CLASS, jLoveItem));

	/* 12.Contact Us模块 */
	jobject jTicketListItem;
	InitClassHelper(env, TICKET_TICKETLISTITEM_CLASS, &jTicketListItem);
	gJavaItemMap.insert(JavaItemMap::value_type(TICKET_TICKETLISTITEM_CLASS, jTicketListItem));

	jobject jTicketDetailItem;
	InitClassHelper(env, TICKET_TICKETDETAILITEM_CLASS, &jTicketDetailItem);
	gJavaItemMap.insert(JavaItemMap::value_type(TICKET_TICKETDETAILITEM_CLASS, jTicketDetailItem));

	jobject jTicketContentItem;
	InitClassHelper(env, TICKET_TICKETCONTENTITEM_CLASS, &jTicketContentItem);
	gJavaItemMap.insert(JavaItemMap::value_type(TICKET_TICKETCONTENTITEM_CLASS, jTicketContentItem));

	/* 13. 月费模块 */
	jobject jMonthlyFeeTipItem;
	InitClassHelper(env, MONTHLY_FEE_TIP_CLASS, &jMonthlyFeeTipItem);
	gJavaItemMap.insert(JavaItemMap::value_type(MONTHLY_FEE_TIP_CLASS, jMonthlyFeeTipItem));

	return JNI_VERSION_1_4;
}

bool GetEnv(JNIEnv** env, bool* isAttachThread)
{
	*isAttachThread = false;
	jint iRet = JNI_ERR;
	iRet = gJavaVM->GetEnv((void**) env, JNI_VERSION_1_4);
	if( iRet == JNI_EDETACHED ) {
		iRet = gJavaVM->AttachCurrentThread(env, NULL);
		*isAttachThread = (iRet == JNI_OK);
	}

	return (iRet == JNI_OK);
}

bool ReleaseEnv(bool isAttachThread)
{
	if (isAttachThread) {
		gJavaVM->DetachCurrentThread();
	}
	return true;
}
