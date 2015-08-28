package com.qpidnetwork.request;


/**
 * 女士模块接口
 * @author Samson.Fan
 *
 */
public class RequestJniEMF {
	/**
	 * 筛选条件（UNREAD：未读，READNOTREPLY：已读未回复，READ：已回复，DEFAULT：全部）
	 */
	public enum SortType {
		UNREAD,
		READNOTREPLY,
		READ,
		DEFAULT
	}
	
	/**
	 * 筛选状态（UNREAD：Unread，PENDING：Pending，DELIVERED：Delivered，DEFAULT：All）
	 */
	public enum ProgressType {
		UNREAD,
		PENDING,
		DELIVERED,
		DEFAULT
	}
	
	/**
	 * 邮件类型（INBOX：收件箱邮件，OUTBOX：发件箱邮件，ADMIRER：意向信邮件）
	 */
	public enum MailType {
		INBOX,
		OUTBOX,
		ADMIRER
	}
	
	/**
	 * 添加黑名单原因（REASON_1：原因A，REASON_2：原因B，REASON_3：原因C，UNKNOW：未定义）
	 */
	public enum BlockReasonType {
		REASON_1,
		REASON_2,
		REASON_3,
		UNKNOW
	}
	
	/**
	 * 回复状态（CONSIDERING：考虑中，REPLIED：已回复，REASON_A：拒绝原因A，REASON_B：拒绝原因B，REASON_C：拒绝原因C，UNKNOW：未定义） 
	 */
	public enum ReplyFlagType {
		CONSIDERING,
		REPLIED,
		REASON_A,
		REASON_B,
		REASON_C,
		UNKNOW
	}
	
	/**
	 * 发送邮件附件类型（IMAGE：图片）
	 */
	public enum UploadAttachType {
		IMAGE,
		UNKNOW
	}
	
	/**
	 * 私密照图片质量
	 */
	public enum PrivatePhotoType {
		LARGE,
		MIDDLE,
		SMALL,
		ORIGINAL
	}
	
	/**
	 * InboxList（查询收件箱列表：/emf/inboxlist）
	 * @param pageIndex
	 * @param pageSize
	 * @param sortType
	 * @param womanId
	 * @param callback
	 * @return -1 fails, else success
	 */
	static public long InboxList(int pageIndex, int pageSize, SortType sortType
			, String womanId, OnEMFInboxListCallback callback) {
    	return InboxList(pageIndex, pageSize, sortType.ordinal(), womanId, callback);
    }
    static protected native long InboxList(int pageIndex, int pageSize, int sortType
    		, String womanId, OnEMFInboxListCallback callback);
    
    /**
     * InboxMsg（查询已收邮件详细：/emf/inboxmsg）
     * @param messageId
     * @param callback
     * @return -1 fails, else success
     */
    static public native long InboxMsg(String messageId, OnEMFInboxMsgCallback callback);
    
    /**
     * OutboxList（查询发件箱列表：/emf/outboxlist）
     * @param pageIndex
     * @param pageSize
     * @param womanId
     * @param progressType
     * @param callback
     * @return -1 fails, else success
     */
    static public long OutboxList(int pageIndex, int pageSize, String womanId
    		, ProgressType progressType, OnEMFOutboxListCallback callback) {
    	return OutboxList(pageIndex, pageSize, womanId, progressType.ordinal(), callback);
    }
    static protected native long OutboxList(int pageIndex, int pageSize, String womanId, int progressType, OnEMFOutboxListCallback callback);
    
    /**
     * OutboxMsg（查询已发邮件详细：/emf/outboxmsg）
     * @param messageId
     * @param callback
     * @return -1 fails, else success
     */
    static public native long OutboxMsg(String messageId, OnEMFOutboxMsgCallback callback);
    
    /**
     * MsgTotal（查询收件箱某状态邮件数量：/emf/msgtotal）
     * @param sortType
     * @param callback
     * @return -1 fails, else success
     */
    static public long MsgTotal(SortType sortType, OnEMFMsgTotalCallback callback) {
    	return MsgTotal(sortType.ordinal(), callback);
    }
    static protected native long MsgTotal(int sortType, OnEMFMsgTotalCallback callback);
    
    /**
     * 回复类型
     */
    static public enum ReplyType {
    	EMF,
    	ADMIRE,
    	DEFAULT,
    }
    
    /**
     * SendMsg（发送邮件：/emf/sendmsg）
     * @param womanid
     * @param body
     * @param useIntegral
     * @param gifts
     * @param attachs
     * @return -1 fails, else success
     */
    static public long SendMsg(
    		String womanid, 
    		String body, 
    		boolean useIntegral, 
    		ReplyType replyType, 
    		String mtab, 
    		String[] gifts, 
    		String[] attachs, 
    		OnEMFSendMsgCallback callback
    		) {
    	return SendMsg(womanid, body, useIntegral, replyType.ordinal(), mtab, gifts, attachs, callback);
    }
    static protected native long SendMsg(String womanid, String body, boolean useIntegral, int replyType, String mtab, String[] gifts, String[] attachs, OnEMFSendMsgCallback callback);
    
    /**
     * UploadImage（追加邮件附件：/emf/uploadimage）
     * @param messageId
     * @param filename
     * @return -1 fails, else success
     */
    static public native long UploadImage(String messageId, String[] filename, OnEMFUploadImageCallback callback);
    
    /**
     * UploadAttach（上传邮件附件：/emf/uploadattach）
     * @param attachType
     * @param filePath
     * @param callback
     * @return -1 fails, else success
     */
    static public long UploadAttach(UploadAttachType attachType, String filePath, OnEMFUploadAttachCallback callback) {
    	return UploadAttach(attachType.ordinal(), filePath, callback);
    }
    static protected native long UploadAttach(int attachType, String filePath, OnEMFUploadAttachCallback callback);
    
    /**
     * DeleteMsg（删除邮件：/emf/deletemsg）
     * @param messageId
     * @param mailType
     * @return -1 fails, else success
     */
    static public long DeleteMsg(String messageId, MailType mailType, OnEMFDeleteMsgCallback callback) {
    	return DeleteMsg(messageId, mailType.ordinal(), callback);
    }
    static protected native long DeleteMsg(String messageId, int mailType, OnEMFDeleteMsgCallback callback);
    
    /**
     * AdmirerList（查询意向信收件箱列表：/emf/admirerlist）
     * @param pageIndex
     * @param pageSize
     * @param sortType
     * @param womanid
     * @return -1 fails, else success
     */
    static public long AdmirerList(int pageIndex, int pageSize, SortType sortType, String womanid, OnEMFAdmirerListCallback callback) {
    	return AdmirerList(pageIndex, pageSize, sortType.ordinal(), womanid, callback);
    }
    static protected native long AdmirerList(int pageIndex, int pageSize, int sortType, String womanid, OnEMFAdmirerListCallback callback);
    
    /**
     * AdmirerViewer（查询意向信详细信息：/emf/admirerviewer）
     * @param messageId
     * @return -1 fails, else success
     */
    static public native long AdmirerViewer(String messageId, OnEMFAdmirerViewerCallback callback);
    
    /**
     * BlockList（查询黑名单列表：/emf/blocklist）
     * @param pageIndex
     * @param pageSize
     * @param womanid
     * @return -1 fails, else success
     */
    static public native long BlockList(int pageIndex, int pageSize, String womanid, OnEMFBlockListCallback callback);
    
    /***
     * Block（添加黑名单：/emf/block）
     * @param womanid
     * @param blockReasonType
     * @return -1 fails, else success 
     */
    static public long Block(String womanid, BlockReasonType blockReasonType, OnEMFBlockCallback callback) {
    	return Block(womanid, blockReasonType.ordinal(), callback);
    }
    static protected native long Block(String womanid, int blockReasonType, OnEMFBlockCallback callback);
    
    /**
     * Unblock（删除黑名单：/emf/unblock）
     * @param womanid
     * @return -1 fails, else success
     */
    static public native long Unblock(String[] womanid, OnEMFUnblockCallback callback);
    
    /**
     * InboxPhotoFee（男士付费获取EMF私密照片：/emf/inbox_photo_fee）
     * @param womanId
     * @param photoId
     * @param sendId
     * @param messageId
     * @return -1 fails, else success
     */
    static public native long InboxPhotoFee(String womanId, String photoId, String sendId, String messageId, OnEMFInboxPhotoFeeCallback callback);
    
    /**
     * PrivatePhotoView（获取对方或自己的EMF私密照片：/emf/private_photo_view）
     * @param womanId
     * @param photoId
     * @param sendId
     * @param messageId
     * @return -1 fails, else success
     */
    static public long PrivatePhotoView(String womanId, String photoId, String sendId, String messageId, String filePath, PrivatePhotoType type, OnEMFPrivatePhotoViewCallback callback) {
    	return PrivatePhotoView(womanId, photoId, sendId, messageId, filePath, type.ordinal(), callback);
    }
    static protected native long PrivatePhotoView(String womanId, String photoId, String sendId, String messageId, String filePath, int type, OnEMFPrivatePhotoViewCallback callback);
}
