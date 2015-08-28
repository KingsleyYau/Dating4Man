package com.qpidnetwork.dating.livechat.video;

import java.util.ArrayList;

import com.qpidnetwork.livechat.LCMessageItem;
import com.qpidnetwork.livechat.jni.LiveChatClientListener.LiveChatErrType;

public class MessageVideoDownloadObject {
	public LiveChatErrType errType;
	public String userId;
	public String videoId;
	public String inviteId;
	public ArrayList<LCMessageItem> msgList;

	public MessageVideoDownloadObject() {
		this.msgList = new ArrayList<LCMessageItem>();
	}

	public MessageVideoDownloadObject(LiveChatErrType errType, String userId,
			String videoId, String inviteId, ArrayList<LCMessageItem> msgList) {
		this.errType = errType;
		this.userId = userId;
		this.videoId = videoId;
		this.inviteId = inviteId;
		if(msgList != null){
			this.msgList = msgList;
		}else{
			this.msgList = new ArrayList<LCMessageItem>();
		}
	}
}
