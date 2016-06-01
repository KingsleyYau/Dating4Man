package com.qpidnetwork.livechat;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;

import com.qpidnetwork.livechat.LCMessageItem.SendType;
import com.qpidnetwork.livechat.LCMessageItem.StatusType;
import com.qpidnetwork.livechat.jni.LiveChatClient.ClientType;
import com.qpidnetwork.livechat.jni.LiveChatClient.UserSexType;
import com.qpidnetwork.livechat.jni.LiveChatClient.UserStatusType;
import com.qpidnetwork.livechat.jni.LiveChatClientListener.TalkEventType;
import com.qpidnetwork.livechat.jni.LiveChatClientListener.TalkMsgType;

/**
 * LiveChat的 User 对象
 * @author Samson Fan
 */
public class LCUserItem implements Serializable{

	private static final long serialVersionUID = -2059118989300697623L;
	/**
	 * 用户ID
	 */
	public String userId;
	/**
	 * 用户名称
	 */
	public String userName;
	/**
	 * 用户性别
	 */
	public UserSexType sexType;
	/**
	 * 用户使用的客户端类型
	 */
	public ClientType clientType;
	/**
	 * 用户在线状态
	 */
	public UserStatusType statusType;
	/**
	 * 与用户的当前聊天状态
	 */
	public ChatType chatType;
	/**
	 * 邀请ID（仅InChat状态）
	 */
	public String inviteId;
	/**
	 * 是否正在尝试发送（可能正在检测是否可聊天，包括检测是否有试聊券，是否可使用，是否有足够点数等)
	 */
	public boolean tryingSend;
	/**
	 * 排序分值（用于邀请排序）
	 */
	public int order;
	/**
	 * 待发送消息
	 */
	public ArrayList<LCMessageItem> sendMsgList;
	/**
	 * 聊天记录列表
	 */
	protected ArrayList<LCMessageItem> msgList;
	
	/**
	 * 聊天状态
	 */
	public enum ChatType {
		InChatCharge, 		// 收费
		InChatUseTryTicket, // 试聊券
		Invite, 		// 邀请
		ManInvite,		// 男士已经发出邀请 
		Other, 			// 其它
	}

	public LCUserItem() {
		userId = "";
		userName = "";
		sexType = UserSexType.USER_SEX_FEMALE;
		clientType = ClientType.CLIENT_ANDROID;
		statusType = UserStatusType.USTATUS_OFFLINE_OR_HIDDEN;
		chatType = ChatType.Other;
		inviteId = "";
		tryingSend = false;
		order = 0;
		msgList = new ArrayList<LCMessageItem>();
		sendMsgList = new ArrayList<LCMessageItem>();
	}

	/**
	 * 获取聊天记录列表（已按时间排序）
	 */
	public final ArrayList<LCMessageItem> getMsgList() {
		return msgList;
	}

	/**
	 * 排序插入聊天记录
	 * @param item
	 * @return
	 */
	public boolean insertSortMsgList(LCMessageItem item) {
		boolean result = false;
		synchronized (msgList) {
			result = msgList.add(item);
			if (result) {
				Collections.sort(msgList, LCMessageItem.getComparator());
			}
		}
		
		if (result) {
			item.setUserItem(this);
		}
		return result;
	}
	
	/**
	 * 删除聊天记录
	 * @param item	消息item
	 * @return
	 */
	public boolean removeSortMsgList(LCMessageItem item) {
		boolean result = false;
		if (null != item) {
			synchronized (msgList) {
				result = msgList.remove(item);
				if (result) {
					item.setUserItem(null);
					Collections.sort(msgList, LCMessageItem.getComparator());
				}
			}
		}
		return result;
	}
	
	/**
	 * 清除所有聊天记录
	 */
	public void clearMsgList() {
		synchronized (msgList) {
			for (Iterator<LCMessageItem> iter = msgList.iterator(); iter.hasNext(); ) {
				LCMessageItem item = iter.next();
				item.clear();
			}
			msgList.clear();
		}
	}
	
	/**
	 * 清除所有已完成的聊天记录
	 */
	public void clearFinishedMsgList() {
		ArrayList<LCMessageItem> tempList = new ArrayList<LCMessageItem>();
		synchronized (msgList) {
			for (Iterator<LCMessageItem> iter = msgList.iterator(); iter.hasNext(); ) {
				LCMessageItem item = iter.next();
				if (item.statusType == StatusType.Finish) {
					tempList.add(item);
					item.clear();
				}
			}
			msgList.removeAll(tempList);
		}
	}
	
	/**
	 * 根据消息ID获取LCMessageItem
	 * @param msgId	消息ID
	 * @return
	 */
	public LCMessageItem getMsgItemWithId(int msgId) {
		LCMessageItem item = null;
		synchronized (msgList) {
			for (Iterator<LCMessageItem> iter = msgList.iterator(); iter.hasNext(); ) {
				LCMessageItem msgItem = iter.next();
				if (msgItem.msgId == msgId) {
					item = msgItem;
					break;
				}
			}
		}
		return item;
	}
	
	/**
	 * 设置聊天状态
	 * @param eventType		聊天事件	
	 */
	public void setChatTypeWithEventType(TalkEventType eventType) {
		switch (eventType) {
		case EndTalk:
			this.chatType = ChatType.Other;
			break;
		case StartCharge:
			if (this.chatType != ChatType.InChatCharge
				&& this.chatType != ChatType.InChatUseTryTicket)
			{
				this.chatType = ChatType.InChatCharge;
			}
			break;
		case StopCharge:
			this.chatType = ChatType.Other;
			break;
		case NoMoney:
		case VideoNoMoney:
			this.chatType = ChatType.Other;
			break;
		case TargetNotFound:
			this.chatType = ChatType.Other;
			break;
		default:
			break;
		}
	}
	
	/**
	 * 设置聊天状态 
	 * @param msgType	聊天消息类型
	 */
	public void setChatTypeWithTalkMsgType(boolean charge, TalkMsgType msgType) {
		this.chatType = getChatTypeWithTalkMsgType(charge, msgType);
	}
	
	/**
	 * 根据 TalkMsgType 获取聊天状态
	 * @param charge	是否已付费
	 * @param msgType	消息收费类型
	 * @return
	 */
	public static ChatType getChatTypeWithTalkMsgType(boolean charge, TalkMsgType msgType) 
	{
		ChatType chatType = ChatType.Other; 
		switch(msgType) {
		case TMT_FREE:
			if (!charge) {
				// TMT_FREE 及 charge=false，则为邀请
				chatType = ChatType.Invite;
			}
			else {
				// charge=true，则为InChatCharge
				chatType = ChatType.InChatCharge;
			}
			break;
		case TMT_CHARGE:
			chatType = ChatType.InChatCharge;
			break;
		case TMT_CHARGE_FREE:
			chatType = ChatType.InChatUseTryTicket;
			break;
		default:
			chatType = ChatType.Other;
			break;
		}
		return chatType;
	}
	
	/**
	 * 获取对方发出的最后一条聊天消息
	 * @return
	 */
	public LCMessageItem getTheOtherLastMessage()
	{
		LCMessageItem item = null;
		synchronized(msgList) {
			if (msgList.size() > 0) 
			{
				for (int i = msgList.size() - 1; i >= 0 ; i--) 
				{
					LCMessageItem tempItem = msgList.get(i);
					if (tempItem.sendType == SendType.Recv) {
						item = tempItem;
						break;
					}
				}
			}
		}
		return item;
	}
	
	/**
	 * 结束聊天处理
	 */
	public void endTalk() {
		chatType = ChatType.Other;
		clearMsgList();
	}
	
	/**
	 * 添加到等发送队列
	 */
	public void addToSendMsgList(LCMessageItem message)
	{
		synchronized (sendMsgList)
		{
			sendMsgList.add(message);
		}
	}
	
	/**
	 * 获取比较器
	 * @return
	 */
	static public Comparator<LCUserItem> getComparator() {
		Comparator<LCUserItem> comparator = new Comparator<LCUserItem>() {
			@Override
			public int compare(LCUserItem lhs, LCUserItem rhs) {
				// TODO Auto-generated method stub
				int result = 0;
				if (lhs != rhs) 
				{
					synchronized (lhs.msgList) 
					{
						synchronized (rhs.msgList) 
						{
							if (lhs.msgList.size() == 0 && rhs.msgList.size() == 0) {
								// 两个都没有消息，按名字排序
								int nameCompareValue = lhs.userName.compareTo(rhs.userName);
								if (nameCompareValue > 0) {
									result = 1;
								}
								else if (nameCompareValue < 0) {
									result = -1;
								}
							}
							else if (lhs.msgList.size() > 0 && rhs.msgList.size() > 0) {
								// 两个都有消息
								LCMessageItem lMsgItem = lhs.msgList.get(lhs.msgList.size()-1);
								LCMessageItem rMsgItem = rhs.msgList.get(rhs.msgList.size()-1);
								
								// 以最后一条消息的聊天时间倒序排序
								if (null != lMsgItem && null != rMsgItem) {
									if (lMsgItem.createTime != rMsgItem.createTime) {
										result = (lMsgItem.createTime > rMsgItem.createTime ? -1 : 1);
									}
								}
								else {
									if (null == lMsgItem && null != rMsgItem) {
										result = 1;
									}
									else if (null != lMsgItem && null == rMsgItem) {
										result = -1;
									}
								}
							}
							else {
								// 其中一个有消息
								result = (lhs.msgList.size() > rhs.msgList.size() ? -1 : 1);
							}
						}
					}
				}
				return result;
			}
		};
		return comparator;
	}
}
