/*
 * CallbackItemAndroidDef.h
 *
 *  Created on: 2015-3-3
 *      Author: Max.Chiu
 *      Email: Kingsleyyau@gmail.com
 */

#ifndef CALLBACKITEMANDROIDDEF_H_
#define CALLBACKITEMANDROIDDEF_H_

/* 附录 */
//#define COUNTRY_ITEM_CLASS 		"com/qpidnetwork/request/RequestEnum$Country"

/* 1.认证模块 */
#define LOGIN_FACEBOOK_ITEM_CLASS 				"com/qpidnetwork/request/item/LoginFacebookItem"
#define REGISTER_ITEM_CLASS 					"com/qpidnetwork/request/item/RegisterItem"
#define LOGIN_ITEM_CLASS 						"com/qpidnetwork/request/item/LoginItem"
#define LOGIN_ERROR_ITEM_CLASS 					"com/qpidnetwork/request/item/LoginErrorItem"

/* 2.个人信息模块 */
#define PROFILE_ITEM_CLASS 						"com/qpidnetwork/request/item/ProfileItem"

/* 4.女士模块 */
#define LADY_MATCH_ITEM_CLASS					"com/qpidnetwork/request/item/LadyMatch"
#define LADY_ITEM_CLASS		 					"com/qpidnetwork/request/item/Lady"
#define LADY_DETAIL_ITEM_CLASS					"com/qpidnetwork/request/item/LadyDetail"
#define LADY_VIDEO_ITEM_CLASS					"com/qpidnetwork/request/item/VideoItem"
#define LADY_CALL_ITEM_CLASS					"com/qpidnetwork/request/item/LadyCall"
#define LADY_RECENTCONTACT_ITEM_CLASS			"com/qpidnetwork/request/item/LadyRecentContactItem"
#define LADY_SIGN_ITEM_CLASS					"com/qpidnetwork/request/item/LadySignItem"

/* 5.live chat 模块 */
#define LIVECHAT_COUPON_ITEM_CLASS				"com/qpidnetwork/request/item/Coupon"
#define LIVECHAT_GIFT_ITEM_CLASS				"com/qpidnetwork/request/item/Gift"
#define LIVECHAT_RECORD_ITEM_CLASS				"com/qpidnetwork/request/item/Record"
#define LIVECHAT_RECORD_MUTIPLE_ITEM_CLASS		"com/qpidnetwork/request/item/RecordMutiple"
#define LIVECHAT_SENDPHOTO_TIME_CLASS			"com/qpidnetwork/request/item/LCSendPhotoItem"
#define LIVECHAT_LCVIDEO_TIME_CLASS				"com/qpidnetwork/request/item/LCVideoItem"

/* 6.EMF模块 */
#define EMF_PRIVATEPHOTO_ITEM_CLASS		"com/qpidnetwork/request/item/EMFPrivatePhotoItem"
#define EMF_INBOXLIST_ITEM_CLASS		"com/qpidnetwork/request/item/EMFInboxListItem"
#define EMF_INBOXMSG_ITEM_CLASS			"com/qpidnetwork/request/item/EMFInboxMsgItem"
#define EMF_OUTBOXLIST_ITEM_CLASS		"com/qpidnetwork/request/item/EMFOutboxListItem"
#define EMF_OUTBOXMSG_ITEM_CLASS		"com/qpidnetwork/request/item/EMFOutboxMsgItem"
#define EMF_MSGTOTAL_ITEM_CLASS			"com/qpidnetwork/request/item/EMFMsgTotalItem"
#define EMF_SENDMSG_ITEM_CLASS			"com/qpidnetwork/request/item/EMFSendMsgItem"
#define EMF_SENDMSG_ERROR_ITEM_CLASS	"com/qpidnetwork/request/item/EMFSendMsgErrorItem"
#define EMF_ADMIRERLIST_ITEM_CLASS		"com/qpidnetwork/request/item/EMFAdmirerListItem"
#define EMF_ADMIRERVIEWER_ITEM_CLASS	"com/qpidnetwork/request/item/EMFAdmirerViewerItem"
#define EMF_BLOCKLIST_ITEM_CLASS		"com/qpidnetwork/request/item/EMFBlockListItem"

/* 7.VideoShow模块*/
#define VS_VIDEOLIST_ITEM_CLASS			"com/qpidnetwork/request/item/VSVideoListItem"
#define VS_VIDEODETAIL_ITEM_CLASS		"com/qpidnetwork/request/item/VSVideoDetailItem"
#define VS_PLAYVIDEO_ITEM_CLASS			"com/qpidnetwork/request/item/VSPlayVideoItem"
#define VS_WATCHEDVIDEOLIST_ITEM_CLASS	"com/qpidnetwork/request/item/VSWatchedVideoListItem"
#define VS_SAVEDVIDEOLIST_ITEM_CLASS	"com/qpidnetwork/request/item/VSSavedVideoListItem"

/* 8.Other模块*/
#define OTHER_EMOTIONCONFIG_ITEM_CLASS	"com/qpidnetwork/request/item/OtherEmotionConfigItem"
#define OTHER_EMOTIONCONFIG_TYPE_ITEM_CLASS		"com/qpidnetwork/request/item/OtherEmotionConfigTypeItem"
#define OTHER_EMOTIONCONFIG_TAG_ITEM_CLASS		"com/qpidnetwork/request/item/OtherEmotionConfigTagItem"
#define OTHER_EMOTIONCONFIG_EMOTION_ITEM_CLASS	"com/qpidnetwork/request/item/OtherEmotionConfigEmotionItem"
#define OTHER_GETCOUNT_ITEM_CLASS		"com/qpidnetwork/request/item/OtherGetCountItem"
#define OTHER_INTEGRALCHECK_ITEM_CLASS	"com/qpidnetwork/request/item/OtherIntegralCheckItem"
#define OTHER_VERSIONCHECK_ITEM_CLASS	"com/qpidnetwork/request/item/OtherVersionCheckItem"
#define OTHER_SYNCONFIG_ITEM_CLASS		"com/qpidnetwork/request/item/OtherSynConfigItem"
#define OTHER_SYNCONFIG_SITE_ITEM_CLASS			"com/qpidnetwork/request/item/OtherSynConfigSiteItem"
#define OTHER_SYNCONFIG_PUBLIC_ITEM_CLASS		"com/qpidnetwork/request/item/OtherSynConfigPublicItem"
#define OTHER_ONLINECOUNT_ITEM_CLASS		"com/qpidnetwork/request/item/OtherOnlineCountItem"

/* 9.广告模块 */
#define ADVERT_MAINADVERT_ITEM_CLASS		"com/qpidnetwork/request/item/AdMainAdvert"
#define ADVERT_WOMANLISTADVERT_ITEM_CLASS	"com/qpidnetwork/request/item/AdWomanListAdvert"
#define ADVERT_PUSHADVERT_ITEM_CLASS		"com/qpidnetwork/request/item/AdPushAdvert"

/* 10.Quick Match模块 */
#define QUICKMATCH_LADY_ITEM_CLASS				"com/qpidnetwork/request/item/QuickMatchLady"

/* 11.Love Call模块 */
#define LOVECALL_ITEM_CLASS				"com/qpidnetwork/request/item/LoveCall"

/* 12.Contact Us模块*/
#define TICKET_TICKETLISTITEM_CLASS			"com/qpidnetwork/request/item/TicketListItem"
#define TICKET_TICKETDETAILITEM_CLASS		"com/qpidnetwork/request/item/TicketDetailItem"
#define TICKET_TICKETCONTENTITEM_CLASS		"com/qpidnetwork/request/item/TicketContentItem"

#endif /* CALLBACKITEMANDROIDDEF_H_ */
