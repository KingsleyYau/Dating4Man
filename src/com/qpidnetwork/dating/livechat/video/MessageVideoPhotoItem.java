package com.qpidnetwork.dating.livechat.video;

import java.util.ArrayList;

import com.qpidnetwork.livechat.LCMessageItem;
import com.qpidnetwork.request.RequestJniLiveChat.VideoPhotoType;

public class MessageVideoPhotoItem {
	public String userId;
	public String inviteId;
	public String videoId;
	public VideoPhotoType type;
	public String filePath;
	public ArrayList<LCMessageItem> msgList;

	public MessageVideoPhotoItem() {
		msgList = new ArrayList<LCMessageItem>();
	}

	public MessageVideoPhotoItem(String userId, String inviteId,
			String videoId, VideoPhotoType type, String filePath,
			ArrayList<LCMessageItem> msgList) {
		this.userId = userId;
		this.inviteId = inviteId;
		this.videoId = videoId;
		this.type = type;
		this.filePath = filePath;
		if(msgList != null){
			this.msgList = msgList;
		}else{
			this.msgList = new ArrayList<LCMessageItem>();
		}
	}
}
