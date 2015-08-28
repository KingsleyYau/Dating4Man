package com.qpidnetwork.request;

/**
 * 5.Live Chat
 * @author Max.Chiu
 *
 */
public class RequestJniLiveChat {
	/**
	 * 5.1.查询是否符合试聊条件
	 * @param womanId			女士ID
	 * @param callback			
	 * @return					请求唯一标识
	 */
	static public native long CheckCoupon(String womanId, OnCheckCouponCallCallback callback);
	
	/**
	 * 5.2.使用试聊券
	 * @param womanId			女士ID
	 * @param callback			
	 * @return					请求唯一标识
	 */
	static public native long UseCoupon(String womanId, OnLCUseCouponCallback callback);
	
	/**
	 * 5.3.获取虚拟礼物列表
	 * @param sessionId			登录成功返回的sessionid
	 * @param userId			登录成功返回的manid
	 * @param callback	
	 * @return					请求唯一标识
	 */
	static public native long QueryChatVirtualGift(String sessionId, String userId, 
			OnQueryChatVirtualGiftCallback callback);
	
	/**
	 * 5.4.查询聊天记录
	 * @param inviteId			邀请ID
	 * @param callback			
	 * @return					请求唯一标识
	 */
	static public native long QueryChatRecord(String inviteId, OnQueryChatRecordCallback callback);
	
	/**
	 * 5.5.批量查询聊天记录
	 * @param inviteId			邀请ID数组
	 * @param callback			
	 * @return					请求唯一标识
	 */
	static public native long QueryChatRecordMutiple(String[] inviteIds, OnQueryChatRecordMutipleCallback callback);
	
	/**
	 * 发送私密照片
	 * @param targetId	接收方ID
	 * @param inviteId	邀请ID
	 * @param userId	用户ID
	 * @param sid		sid
	 * @param filePath	待发送的文件路径	
	 * @return
	 */
	static public native long SendPhoto(String targetId, String inviteId, String userId, String sid, String filePath, OnLCSendPhotoCallback callback);
	
	/**
	 * 付费获取私密照片
	 * @param targetId	接收方ID
	 * @param inviteId	邀请ID
	 * @param userId	用户ID
	 * @param sid		sid
	 * @param photoId	照片ID
	 * @return
	 */
	static public native long PhotoFee(String targetId, String inviteId, String userId, String sid, String photoId, OnLCPhotoFeeCallback callback);
	
	/**
	 * 获取类型
	 */
	public enum ToFlagType {
		/**
		 * 女士获取男士
		 */
		WomanGetMan,
		/**
		 * 男士获取女士
		 */
		ManGetWoman,
		/**
		 * 女士获取自己
		 */
		WomanGetSelf,
		/**
		 * 男士获取自己
		 */
		ManGetSelf
	}
	
	/**
	 * 照片尺寸
	 */
	public enum PhotoSizeType {
		/**
		 * 大图
		 */
		Large,
		/**
		 * 中图
		 */
		Middle,
		/**
		 * 小图
		 */
		Small,
		/**
		 * 原图
		 */
		Original
	}
	
	/**
	 * 照片类型
	 */
	public enum PhotoModeType {
		/**
		 * 模糊
		 */
		Fuzzy,
		/**
		 * 清晰
		 */
		Clear
	}

	/**
	 * 获取照片
	 * @param toFlag	获取类型
	 * @param targetId	照片所有者ID
	 * @param userId	用户ID
	 * @param sid		sid
	 * @param photoId	照片ID
	 * @param sizeType	照片尺寸
	 * @param modeType	照片类型
	 * @param filePath	照片文件路径
	 * @return
	 */
	static public long GetPhoto(ToFlagType toFlag, String targetId, String userId, String sid, String photoId, PhotoSizeType sizeType, PhotoModeType modeType, String filePath, OnLCGetPhotoCallback callback) {
		return GetPhoto(toFlag.ordinal(), targetId, userId, sid, photoId, sizeType.ordinal(), modeType.ordinal(), filePath, callback);
	} 
	static protected native long GetPhoto(int toFlag, String targetId, String userId, String sid, String photoId, int sizeType, int modeType, String filePath, OnLCGetPhotoCallback callback);
	
	/**
	 * 上传语音文件
	 * @param voiceCode		语音验证码
	 * @param inviteId		邀请ID
	 * @param mineId		自己的用户ID
	 * @param isMan			是否男士
	 * @param userId		对方的用户ID
	 * @param siteType		站点ID		
	 * @param fileType		文件类型(mp3, aac...)
	 * @param voiceLength	语音时长
	 * @param filePath		语音文件路径
	 * @param callback
	 * @return
	 */
	static public native long UploadVoice(
			String voiceCode
			, String inviteId
			, String mineId
			, boolean isMan
			, String userId
			, int siteType
			, String fileType
			, int voiceLength
			, String filePath
			, OnLCUploadVoiceCallback callback);
	
	/**
	 * 下载语音文件
	 * @param voiceId	语音ID
	 * @param siteType	站点ID
	 * @param filePath	文件路径
	 * @param callback
	 * @return
	 */
	static public native long PlayVoice(String voiceId, int siteType, String filePath, OnLCPlayVoiceCallback callback);
	
	/**
	 * 模块类型
	 */
	public enum UseType {
		/**
		 * 邮件
		 */
		EMF,
		/**
		 * LiveChat
		 */
		CHAT
	}
	
	/**
	 * 6.11.发送虚拟礼物
	 * @param womanId		女士ID
	 * @param vg_id			虚拟礼物ID
	 * @param device_id		设备唯一标识
	 * @param chat_id		livechat邀请ID或EMF邮件ID
	 * @param use_type		模块类型<UseType>
	 * @param user_sid		登录成功返回的sessionid
	 * @param user_id		登录成功返回的manid
	 * @param callback
	 * @return				请求唯一Id
	 */
	static public long SendGift(
			String womanId, 
			String vg_id, 
			String device_id, 
			String chat_id, 
			UseType use_type, 
			String user_sid,
			String user_id,
			OnRequestCallback callback) {
		return SendGift(womanId, vg_id, device_id, chat_id, use_type.ordinal(), user_sid, user_id, callback);
	}
	static protected native long SendGift(
			String womanId, 
			String vg_id, 
			String device_id, 
			String chat_id, 
			int use_type, 
			String user_sid,
			String user_id,
			OnRequestCallback callback);
	
	/**
	 * 6.12.获取最近已看微视频列表（http post）（New）
	 * @param womanId		女士ID
	 * @param callback
	 * @return				请求唯一Id
	 */
	static public native long QueryRecentVideo(
			String user_sid,
			String user_id,
			String womanId, 
			OnQueryRecentVideoListCallback callback);
	
	/**
	 * 获取类型
	 */
	public enum VideoPhotoType {
		Default,
		Big,
	}
	/**
	 * 6.13.获取微视频图片（http get）（New）
	 * @param womanId		女士ID
	 * @param videoid		视频ID
	 * @param type			图片尺寸<VideoPhotoType>
	 * @param filePath		文件路径
	 * @param callback
	 * @return				请求唯一Id
	 */
	static public long GetVideoPhoto(
			String user_sid,
			String user_id,
			String womanId, 
			String videoid,
			VideoPhotoType type,
			String filePath,
			OnRequestFileCallback callback) {
		return GetVideoPhoto(user_sid, user_id, womanId, videoid, type.ordinal(), filePath, callback);
	}
	static protected native long GetVideoPhoto(
			String user_sid,
			String user_id,
			String womanId, 
			String videoid,
			int type,
			String filePath,
			OnRequestFileCallback callback);
	
	/**
	 * 获取类型
	 */
	public enum VideoToFlagType {
		Woman,
		Man,
	}
	
	/**
	 * 6.14.获取微视频文件URL（http post）（New）
	 * @param womanId		女士ID
	 * @param videoid		视频ID
	 * @param inviteid		邀请ID
	 * @param toflag		客户端类型<VideoToFlagType>
	 * @param sendid		发送ID，在LiveChat收到女士端发出的消息中
	 * @param callback
	 * @return				请求唯一Id
	 */
	static public long GetVideo(
			String user_sid,
			String user_id,
			String womanId, 
			String videoid,
			String inviteid,
			VideoToFlagType toflag,
			String sendid,
			OnGetVideoCallback callback) {
		return GetVideo(user_sid, user_id, womanId, videoid, inviteid, toflag.ordinal(), sendid, callback);
	}
	static protected native long GetVideo(
			String user_sid,
			String user_id,
			String womanId, 
			String videoid,
			String inviteid,
			int toflag,
			String sendid,
			OnGetVideoCallback callback);
}
