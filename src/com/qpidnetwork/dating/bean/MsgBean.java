package com.qpidnetwork.dating.bean;

import java.io.Serializable;

public class MsgBean implements Serializable{

	private static final long serialVersionUID = -2750816340748385241L;

	public static final int STATUS_SUC = 1; // 送达成功
	public static final int STATUS_DEFAULT = 0; // 默认、未处理
	public static final int STATUS_FAIL = -1; // 送达失败

	public static final String CONTENT_SEPARATOR = "-";

	/**
	 * 邀请id
	 */
	public String inviteId;
	/**
	 * 来源者id
	 */
	public String fromUserId;
	/**
	 * 来源者名称
	 */
	public String fromUserName;
	/**
	 * 消息目标id
	 */
	public String toId;
	/**
	 * 消息目标名字
	 */
	public String toUserName;
	/**
	 * 消息类型
	 */
	public MessageType msgType;
	/**
	 * 方向
	 */
	public Forward forward;
	/**
	 * 是否付费
	 */
	public boolean charge;
	/**
	 * 票根
	 */
	public int ticket;
	
	/*一下针对发送消息*/
	/**
	 * 是否成功
	 */
	public boolean status;
	/**
	 * 消息合法性
	 */
	public boolean illegal;
	
	/**
	 * 服务器消息时间(用于排序及时间显示)
	 */
	public long adddate;


	
	public enum MessageType{
		TYPE_TEXT, 
		TYPE_EMOTION, 
		TYPE_VOICE,
		TYPE_PICTURE,
		TYPE_GIFT,
		TYPE_WARN
	}
	
	public enum Forward{
		FORWARD_IN,
		FORWARD_OUT
	}

}
