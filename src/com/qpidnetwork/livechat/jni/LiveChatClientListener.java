package com.qpidnetwork.livechat.jni;


/**
 * LiveChat回调接口类
 * @author Samson Fan
 */
public abstract class LiveChatClientListener {
	
	/**
	 * 处理结果类型
	 */
	public enum LiveChatErrType {
		Fail,		// 服务器返回失败结果
		Success,	// 成功
		
		// 服务器返回错误
		UnbindInterpreter,		// 女士的翻译未将其绑定
		SideOffile,				// 对方不在线（聊天）
		NoMoney,				// 帐号余额不足
		InvalidUser,			// 用户不存在（登录）
		InvalidPassword,		// 密码错误（登录）
		BlockUser,				// 对方为黑名单用户（聊天）
		EmotionError,			// 高级表情异常（聊天）
		VoiceError,				// 语音异常（聊天）
		
		// 客户端定义的错误
		ProtocolError,			// 协议解析失败（服务器返回的格式不正确）
		ConnectFail,			// 连接服务器失败/断开连接
		CheckVerFail,			// 检测版本号失败（可能由于版本过低导致）
		LoginFail,				// 登录失败
		ServerBreak,			// 服务器踢下线
		CanNotSetOffline,		// 不能把在线状态设为"离线"，"离线"请使用Logout()
		
		
	};
	
	/**
	 * 登录回调
	 * @param errType	处理结果类型
	 * @param errmsg	处理结果描述
	 */
	public abstract void OnLogin(LiveChatErrType errType, String errmsg);
	public void OnLogin(int errType, String errmsg) {
		OnLogin(LiveChatErrType.values()[errType], errmsg);
	}
	
	/**
	 * 注销/断线回调
	 * @param errType	处理结果类型
	 * @param errmsg	处理结果描述
	 */
	public abstract void OnLogout(LiveChatErrType errType, String errmsg);
	public void OnLogout(int errType, String errmsg) {
		OnLogout(LiveChatErrType.values()[errType], errmsg);
	}
	
	/**
	 * 设置在线状态回调
	 * @param errType	处理结果类型
	 * @param errmsg	处理结果描述
	 */
	public abstract void OnSetStatus(LiveChatErrType errType, String errmsg);
	public void OnSetStatus(int errType, String errmsg) {
		OnSetStatus(LiveChatErrType.values()[errType], errmsg);
	}
	
	/**
	 * 结束聊天会话回调
	 * @param errType	处理结果类型
	 * @param errmsg	处理结果描述
	 * @param userId	用户ID
	 */
	public abstract void OnEndTalk(LiveChatErrType errType, String errmsg, String userId);
	public void OnEndTalk(int errType, String errmsg, String userId) {
		OnEndTalk(LiveChatErrType.values()[errType], errmsg, userId);
	}
	
	/**
	 * 获取用户在线状态回调
	 * @param errType			处理结果类型
	 * @param errmsg			处理结果描述
	 * @param userStatusArray	用户在线状态数组
	 */
	public abstract void OnGetUserStatus(LiveChatErrType errType, String errmsg, LiveChatUserStatus[] userStatusArray);
	public void OnGetUserStatus(int errType, String errmsg, LiveChatUserStatus[] userStatusArray) {
		OnGetUserStatus(LiveChatErrType.values()[errType], errmsg, userStatusArray);
	}
	
	/**
	 * 获取聊天会话信息回调
	 * @param errType	处理结果类型
	 * @param errmsg	处理结果描述
	 * @param userId	用户ID
	 * @param invitedId	邀请ID
	 * @param charget	是否已付费
	 * @param chatTime	聊天时长
	 */
	public abstract void OnGetTalkInfo(LiveChatErrType errType, String errmsg, String userId, String invitedId, boolean charget, int chatTime);
	public void OnGetTalkInfo(int errType, String errmsg, String userId, String invitedId, boolean charget, int chatTime) {
		OnGetTalkInfo(LiveChatErrType.values()[errType], errmsg, userId, invitedId, charget, chatTime);
	}
	
	/**
	 * 发送聊天文本消息回调
	 * @param errType	处理结果类型
	 * @param errmsg	处理结果描述
	 * @param userId	用户ID
	 * @param message	消息内容
	 * @param ticket	票根
	 */
	public abstract void OnSendMessage(LiveChatErrType errType, String errmsg, String userId, String message, int ticket);
	public void OnSendMessage(int errType, String errmsg, String userId, String message, int ticket) {
		OnSendMessage(LiveChatErrType.values()[errType], errmsg, userId, message, ticket);
	}

	/**
	 * 发送高级表情消息回调
	 * @param errType	处理结果类型
	 * @param errmsg	处理结果描述
	 * @param userId	用户ID
	 * @param emotionId	高级表情ID
	 * @param ticket	票根
	 */
	public abstract void OnSendEmotion(LiveChatErrType errType, String errmsg, String userId, String emotionId, int ticket);
	public void OnSendEmotion(int errType, String errmsg, String userId, String emotionId, int ticket) {
		OnSendEmotion(LiveChatErrType.values()[errType], errmsg, userId, emotionId, ticket);
	}

	/**
	 * 发送虚拟礼物回调
	 * @param errType	处理结果类型
	 * @param errmsg	处理结果描述
	 * @param userId	用户ID
	 * @param giftId	虚拟礼物ID
	 */
	public abstract void OnSendVGift(LiveChatErrType errType, String errmsg, String userId, String giftId, int ticket);
	public void OnSendVGift(int errType, String errmsg, String userId, String giftId, int ticket) {
		OnSendVGift(LiveChatErrType.values()[errType], errmsg, userId, giftId, ticket);
	}
	
	/**
	 * 获取发送语音验证码回调
	 * @param errType	处理结果类型
	 * @param errmsg	处理结果描述
	 * @param userId	用户ID
	 * @param voiceCode	语音ID
	 */
	public abstract void OnGetVoiceCode(LiveChatErrType errType, String errmsg, String userId, int ticket, String voiceCode);
	public void OnGetVoiceCode(int errType, String errmsg, String userId, int ticket, String voiceCode) {
		OnGetVoiceCode(LiveChatErrType.values()[errType], errmsg, userId, ticket, voiceCode);
	}

	/**
	 * 发送语音回调 
	 * @param errType	处理结果类型
	 * @param errmsg	处理结果描述
	 * @param userId	用户ID
	 * @param voiceId	语音ID
	 */
	public abstract void OnSendVoice(LiveChatErrType errType, String errmsg, String userId, String voiceId, int ticket);
	public void OnSendVoice(int errType, String errmsg, String userId, String voiceId, int ticket) {
		OnSendVoice(LiveChatErrType.values()[errType], errmsg, userId, voiceId, ticket);
	}

	/**
	 * 试聊事件定义
	 */
	public enum TryTicketEventType {
		Unknow,		// 未知
		Normal,		// 正常使用
		Used,		// 已使用券
		Paid,		// 已付费
		NoTicket,	// 没有券
		Offline,	// 对方已离线
	}
	
	/**
	 * 使用试聊券回调
	 * @param errType	处理结果类型
	 * @param errmsg	处理结果描述
	 * @param userId	用户ID
	 * @param eventType	试聊券使用情况
	 */
	public abstract void OnUseTryTicket(LiveChatErrType errType, String errmsg, String userId, TryTicketEventType eventType);
	public void OnUseTryTicket(int errType, String errmsg, String userId, int eventType) {
		OnUseTryTicket(LiveChatErrType.values()[errType], errmsg, userId, TryTicketEventType.values()[eventType]);
	}
	
	/**
	 * 获取邀请/在聊列表回调
	 * @param errType	处理结果类型
	 * @param errmsg	处理结果描述
	 * @param listType	请求列表类型
	 * @param info		请求结果
	 */
	public abstract void OnGetTalkList(LiveChatErrType errType, String errmsg, int listType, LiveChatTalkListInfo info);
	public void OnGetTalkList(int errType, String errmsg, int listType, LiveChatTalkListInfo info) {
		OnGetTalkList(LiveChatErrType.values()[errType], errmsg, listType, info);
	}

	/**
	 * 发送图片回调
	 * @param errType	处理结果类型
	 * @param errmsg	处理结果描述
	 */
	public abstract void OnSendPhoto(LiveChatErrType errType, String errmsg, int ticket);
	public void OnSendPhoto(int errType, String errmsg, int ticket) {
		OnSendPhoto(LiveChatErrType.values()[errType], errmsg, ticket);
	}
	
	/**
	 * 显示图片回调
	 * @param errType	处理结果类型
	 * @param errmsg	处理结果描述
	 * @param ticket	票根
	 */
	public abstract void OnShowPhoto(LiveChatErrType errType, String errmsg, int ticket);
	public void OnShowPhoto(int errType, String errmsg, int ticket) {
		OnShowPhoto(LiveChatErrType.values()[errType], errmsg, ticket);
	}
	
	/**
	 * 播放微视频
	 * @param errType	处理结果类型
	 * @param errmsg	处理结果描述
	 * @param ticket	票根
	 */
	public abstract void OnPlayVideo(LiveChatErrType errType, String errmsg, int ticket);
	public void OnPlayVideo(int errType, String errmsg, int ticket) {
		OnPlayVideo(LiveChatErrType.values()[errType], errmsg, ticket);
	}
	
	/**
	 * 获取用户信息
	 * @param errType	处理结果类型
	 * @param errmsg	处理结果描述
	 * @param item		用户信息item
	 */
	public abstract void OnGetUserInfo(LiveChatErrType errType, String errmsg, LiveChatTalkUserListItem item);
	public void OnGetUserInfo(int errType, String errmsg, LiveChatTalkUserListItem item) {
		OnGetUserInfo(LiveChatErrType.values()[errType], errmsg, item);
	}
	
	/**
	 * 获取黑名单列表
	 * @param errType	处理结果类型
	 * @param errmsg	处理结果描述
	 * @param list		黑名单列表
	 */
	public abstract void OnGetBlockList(LiveChatErrType errType, String errmsg, LiveChatTalkUserListItem[] list);
	public void OnGetBlockList(int errType, String errmsg, LiveChatTalkUserListItem[] list) {
		OnGetBlockList(LiveChatErrType.values()[errType], errmsg, list);
	}
	
	/**
	 * 获取LiveChat联系人列表
	 * @param errType	处理结果类型
	 * @param errmsg	处理结果描述
	 * @param list		联系人列表
	 */
	public abstract void OnGetContactList(LiveChatErrType errType, String errmsg, LiveChatTalkUserListItem[] list);
	public void OnGetContactList(int errType, String errmsg, LiveChatTalkUserListItem[] list) {
		OnGetContactList(LiveChatErrType.values()[errType], errmsg, list);
	}
	
	/**
	 * 获取被屏蔽女士列表
	 * @param errType	处理结果类型
	 * @param errmsg	处理结果描述
	 * @param usersId	被屏蔽女士列表
	 */
	public abstract void OnGetBlockUsers(LiveChatErrType errType, String errmsg, String[] usersId);
	public void OnGetBlockUsers(int errType, String errmsg, String[] usersId) {
		OnGetBlockUsers(LiveChatErrType.values()[errType], errmsg, usersId);
	}
	
	/**
	 * 聊天消息类型
	 */
	public enum TalkMsgType {
		TMT_UNKNOW,			// 未知
		TMT_FREE,			// 免费
		TMT_CHARGE,			// 收费
		TMT_CHARGE_FREE,	// 试聊券
	}
	
	/**
	 * 接收聊天文本消息回调
	 * @param toId		接收者ID
	 * @param fromId	发送者ID
	 * @param fromName	发送者用户名
	 * @param inviteId	邀请ID
	 * @param charget	是否已付费
	 * @param ticket	票根
	 * @param msgType	聊天消息类型
	 * @param message	消息内容
	 */
	public abstract void OnRecvMessage(String toId, String fromId, String fromName, String inviteId, boolean charget, int ticket, TalkMsgType msgType, String message);
	public void OnRecvMessage(String toId, String fromId, String fromName, String inviteId, boolean charget, int ticket, int msgType, String message) {
		OnRecvMessage(toId, fromId, fromName, inviteId, charget, ticket, TalkMsgType.values()[msgType], message);
	}
	
	
	/**
	 * 接收高级表情消息回调
	 * @param toId		接收者ID
	 * @param fromId	发送者ID
	 * @param fromName	发送者用户名
	 * @param inviteId	邀请ID
	 * @param charget	是否已付费
	 * @param ticket	票根
	 * @param msgType	聊天消息类型
	 * @param emotionId	高级表情ID
	 */
	public abstract void OnRecvEmotion(String toId, String fromId, String fromName, String inviteId, boolean charget, int ticket, TalkMsgType msgType, String emotionId);
	public void OnRecvEmotion(String toId, String fromId, String fromName, String inviteId, boolean charget, int ticket, int msgType, String emotionId) {
		OnRecvEmotion(toId, fromId, fromName, inviteId, charget, ticket, TalkMsgType.values()[msgType], emotionId);
	}
	
	/**
	 * 接收语音消息回调
	 * @param toId		接收者ID
	 * @param fromId	发送者ID
	 * @param fromName	发送者用户名
	 * @param inviteId	邀请ID
	 * @param charget	是否已付费
	 * @param msgType	聊天消息类型
	 * @param voiceId	语音ID
	 */
	public abstract void OnRecvVoice(String toId, String fromId, String fromName, String inviteId, boolean charget, TalkMsgType msgType, String voiceId, String fileType, int timeLen);
	public void OnRecvVoice(String toId, String fromId, String fromName, String inviteId, boolean charget, int msgType, String voiceId, String fileType, int timeLen) {
		OnRecvVoice(toId, fromId, fromName, inviteId, charget, TalkMsgType.values()[msgType], voiceId, fileType, timeLen);
	}
	
	/**
	 * 接收警告消息回调
	 * @param toId		接收者ID
	 * @param fromId	发送者ID
	 * @param fromName	发送者用户名
	 * @param inviteId	邀请ID
	 * @param charget	是否已付费
	 * @param ticket	票根
	 * @param msgType	聊天消息类型
	 * @param message	消息内容
	 */
	public abstract void OnRecvWarning(String toId, String fromId, String fromName, String inviteId, boolean charget, int ticket, TalkMsgType msgType, String message);
	public void OnRecvWarning(String toId, String fromId, String fromName, String inviteId, boolean charget, int ticket, int msgType, String message) {
		OnRecvWarning(toId, fromId, fromName, inviteId, charget, ticket, TalkMsgType.values()[msgType], message);
	}
	
	/**
	 * 接收图片消息回调
	 * @param toId		接收者ID
	 * @param fromId	发送者ID
	 * @param fromName	发送者用户名
	 * @param inviteId	邀请ID
	 * @param photoId	图片ID
	 * @param sendId	图片发送ID
	 * @param charget	是否已付费
	 * @param photoDesc	图片描述
	 * @param ticket	票根
	 */
	public abstract void OnRecvPhoto(String toId, String fromId, String fromName, String inviteId, String photoId, String sendId, boolean charget, String photoDesc, int ticket);
	
	/**
	 * 接收微视频消息回调
	 * @param toId		接收者ID
	 * @param fromId	发送者ID
	 * @param fromName	发送者用户名
	 * @param inviteId	邀请ID
	 * @param videoId	视频ID
	 * @param sendId	发送ID
	 * @param charget	是否已付费
	 * @param videoDesc	视频描述
	 * @param ticket	票根
	 */
	public abstract void OnRecvVideo(String toId, String fromId, String fromName, String inviteId, String videoId, String sendId, boolean charget, String videoDesc, int ticket);

	/**
	 * 接收更新在线状态消息回调
	 * @param userId
	 * @param server
	 * @param clientType
	 * @param statusType
	 */
	public abstract void OnUpdateStatus(String userId, String server, LiveChatClient.ClientType clientType, LiveChatClient.UserStatusType statusType);
	public void OnUpdateStatus(String userId, String server, int clientType, int statusType) {
		OnUpdateStatus(userId, server, LiveChatClient.ClientType.values()[clientType], LiveChatClient.UserStatusType.values()[statusType]);
	}

	/**
	 * 接收更新票根消息回调
	 * @param fromId	发送者ID
	 * @param ticket	票根
	 */
	public abstract void OnUpdateTicket(String fromId, int ticket);

	/**
	 * 接收用户正在编辑消息回调 
	 * @param fromId	用户ID
	 */
	public abstract void OnRecvEditMsg(String fromId);

	/**
	 * 聊天事件类型
	 */
	public enum TalkEventType {
		Unknow,			// 未知
		EndTalk,		// 结束聊天
		StartCharge,	// 开始收费
		StopCharge,		// 暂停收费
		NoMoney,		// 余额不足
		VideoNoMoney,	// 视频余额不足
		TargetNotFound,	// 目标不存在
	}
	
	/**
	 * 接收聊天事件消息回调
	 * @param userId	聊天对象ID
	 * @param eventType	聊天事件
	 */
	public abstract void OnRecvTalkEvent(String userId, TalkEventType eventType);
	public void OnRecvTalkEvent(String userId, int eventType) {
		OnRecvTalkEvent(userId, TalkEventType.values()[eventType]);
	}
	
	/**
	 * 接收试聊开始消息回调
	 * @param toId		接收者ID
	 * @param fromId	发起者ID
	 * @param time		试聊时长
	 */
	public abstract void OnRecvTryTalkBegin(String toId, String fromId, int time);
	
	/**
	 * 接收试聊结束消息回调
	 * @param userId	聊天对象ID
	 */
	public abstract void OnRecvTryTalkEnd(String userId);
	
	/**
	 * 邮件类型
	 */
	public enum TalkEmfNoticeType {
		Unknow,		// 未知
		EMF,		// EMF
		Admirer,	// 意向信
	}
	
	/**
	 * 接收邮件更新消息回调
	 * @param fromId		发送者ID
	 * @param noticeType	邮件类型
	 */
	public abstract void OnRecvEMFNotice(String fromId, TalkEmfNoticeType noticeType);
	public void OnRecvEMFNotice(String fromId, int noticeType) {
		OnRecvEMFNotice(fromId, TalkEmfNoticeType.values()[noticeType]);
	}

	/**
	 * 被踢下线类型
	 */
	public enum KickOfflineType {
		Unknow,		// 未知
		Maintain,	// 服务器维护退出通知
		Timeout,	// 心跳包超时
		OtherLogin,	// 用户在其它地方登录
	}
	
	/**
	 * 接收被踢下线消息回调
	 * @param kickType	被踢下线原因
	 */
	public abstract void OnRecvKickOffline(KickOfflineType kickType);
	public void OnRecvKickOffline(int kickType) {
		OnRecvKickOffline(KickOfflineType.values()[kickType]);
	}
}
