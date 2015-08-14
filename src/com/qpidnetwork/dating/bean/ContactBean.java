package com.qpidnetwork.dating.bean;

import java.util.Comparator;

import com.qpidnetwork.framework.util.StringUtil;
import com.qpidnetwork.livechat.LCMessageItem;
import com.qpidnetwork.request.item.LadyRecentContactItem;

public class ContactBean extends LadyRecentContactItem {
	
	/*在线判断*/
	public boolean isOnline;
	
	/*是否在聊*/
	public boolean isInchating;
	
	/*最后一条消息内容*/
	public String msgHint;
	
	/*未读条数*/
	public int unreadCount;
	
	/*邀请Id*/
	public String invitedId;
	
	public boolean needSynSystemTime = false;
	
	/*比较器*/
	static private Comparator<ContactBean> comparator;
	
	public ContactBean(){
		isOnline = false;
		isInchating = false;
		unreadCount = 0;
		lasttime = 0;
		msgHint = "";
		invitedId = "";
	}
	
	public ContactBean(LadyRecentContactItem item){
		this.womanid = item.womanid;
		this.firstname = item.firstname;
		this.age = item.age;
		this.photoURL = item.photoURL;
		this.photoBigURL = item.photoBigURL;
		this.isfavorite = item.isfavorite;
		this.videoCount = item.videoCount;
		this.lasttime = item.lasttime;
		isOnline = false;
		isInchating = false;
		unreadCount = 0;
		msgHint = "";
		invitedId = "";
		/*服务器获取时间，需要同步*/
		needSynSystemTime = true;
	}
	
	/**
	 * 获取最后更新时间，和服务器同步后的时间
	 * @return
	 */
	public int getLastUpdateTime(){
		int lastupdate = 0;
		if(needSynSystemTime){
			lastupdate = LCMessageItem.GetLocalTimeWithServerTime(lasttime);
		}else{
			lastupdate = lasttime; 
		}
		return lastupdate;
	}
	
	@Override
	public boolean equals(Object o) {
		if((o != null)&&(o instanceof ContactBean)){
			ContactBean object = (ContactBean)o;
			return (object.womanid.equals(womanid));
		}
		return super.equals(o);
	}
	
	static public Comparator<ContactBean> getComparator() {
		if (null == comparator) {
			comparator = new Comparator<ContactBean>() {
				@Override
				public int compare(ContactBean lhs, ContactBean rhs) {
					// TODO Auto-generated method stub
					int result = -1;
					// 1. 比较在线状态
					if (lhs.isOnline == rhs.isOnline) {
						// 2. 比较聊天状态是否 in chat
						if (lhs.isInchating == rhs.isInchating) {
							// 3. 比较是否有 LiveChat 消息
							if (StringUtil.isEmpty(lhs.msgHint) == StringUtil.isEmpty(rhs.msgHint)) {
								boolean lasttimeCompare = true;
								
								// 4. 都没有消息，则比较favorite
								if (StringUtil.isEmpty(lhs.msgHint)) {
									if (lhs.isfavorite != rhs.isfavorite) {
										result = (lhs.isfavorite == true ? -1 : 1);
										lasttimeCompare = false;
									}
								}
								
								// 5. 都有LiveChat消息 或 都没有LiveChat消息且favorite相等，则比较最后联系时间
								if (lasttimeCompare) {
									if (lhs.getLastUpdateTime() == rhs.getLastUpdateTime()) {
										result = 0;
									}
									else {
										result = (lhs.lasttime > rhs.lasttime ? -1 : 1);
									}
								}
							}
							else {
								result = (!StringUtil.isEmpty(lhs.msgHint) ? -1 : 1);
							}
						}
						else {
							result = (lhs.isInchating == true ? -1 : 1);
						}
					}
					else {
						result = (lhs.isOnline == true ? -1 : 1);
					}

					return result;
				}
			};
		}
		return comparator;
	}
}
