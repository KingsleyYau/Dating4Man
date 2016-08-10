package com.qpidnetwork.livechat.jni;

import com.qpidnetwork.livechat.jni.LiveChatClient.UserStatusType;


/**
 * 获取邀请/在聊用户列表user item
 * @author Samson Fan
 *
 */
public class LiveChatTalkUserListItem {
	public LiveChatTalkUserListItem() {
		
	}
	
	public LiveChatTalkUserListItem(
			String userId, 
			String userName, 
			String server, 
			String imgUrl, 
			int sexType,
			int age,
			String weight,
			String height,
			String country,
			String province,
			boolean videoChat,
			int videoCount,
			int marryType,
			int childrenType,
			int statusType,
			int userType,
			int orderValue,
			int deviceType,
			int clientType,
			String clientVersion) 
	{
		this.userId = userId;
		this.userName = userName;
		this.server = server;
		this.imgUrl = imgUrl;
		this.sexType = LiveChatClient.UserSexType.values()[sexType];
		this.age = age;
		this.weight = weight;
		this.height = height;
		this.country = country;
		this.province = province;
		this.videoChat = videoChat;
		this.marryType = MarryType.values()[marryType];
		this.childrenType = ChildrenType.values()[childrenType];
		if(statusType>= UserStatusType.USTATUS_UNKNOW.ordinal() 
				&& statusType <= UserStatusType.USTATUS_HIDDEN.ordinal()){
			this.statusType = LiveChatClient.UserStatusType.values()[statusType];
		}else{
			this.statusType = UserStatusType.USTATUS_OFFLINE_OR_HIDDEN;
		}
		this.userType = UserType.values()[userType];
		this.orderValue = orderValue;
		this.deviceType = DeviceType.values()[deviceType];
		this.clientType = LiveChatClient.ClientType.values()[clientType];
		this.clientVersion = clientVersion; 
	}
	
	/**
	 * 婚姻状况类型
	 */
	public enum MarryType {
		Unknow,			// 未知
		NeverMarried,	// 未婚
		Disvorced,		// 离婚
		Widowed,		// 丧偶
		Separated,		// 分居
		Married,		// 已婚
	}
	
	/**
	 * 子女状况类型
	 */
	public enum ChildrenType {
		Unknow,		// 未知
		NoChildren,	// 没有子女
		Yes,		// 有子女
	}
	
	/**
	 *  用户类型
	 */
	public enum UserType {
		Unknow,			// 未知
		Woman,			// 女士
		Man,			// 男士
		Interpreter,	// 翻译
	}
	
	/**
	 * 设备类型
	 */
	public enum DeviceType {
		Unknow,		// 未知
		Web,		// Web
		Wap, 		// WAP
		WapAndroid,	// WAP Android
		WapiPhone,	// WAP iPhone
		AppAndroid,	// App Android
		AppiPhone,	// App iPhone
		AppiPad,	// App iPad
		AppPC,		// App PC
	}
	
	/**
	 * 用户ID
	 */
	public String userId;
	
	/**
	 * 用户名
	 */
	public String userName;
	
	/**
	 * 服务器名
	 */
	public String server;
	
	/**
	 * 头像URL
	 */
	public String imgUrl;
	
	/**
	 * 性别
	 */
	public LiveChatClient.UserSexType sexType;
	
	/**
	 * 年龄
	 */
	public int age;
	
	/**
	 * 体重
	 */
	public String weight;
	
	/**
	 * 身高
	 */
	public String height;
	
	/**
	 * 国家
	 */
	public String country;
	
	/**
	 * 省份
	 */
	public String province;
	
	/**
	 * 是否能视频聊天
	 */
	public boolean videoChat;
	
	/**
	 * 视频数量
	 */
	public int videoCount;
	
	/**
	 * 婚姻状况
	 */
	public MarryType marryType;
	
	/**
	 * 子女状况
	 */
	public ChildrenType childrenType;
	
	/**
	 * 在线状态类型
	 */
	public LiveChatClient.UserStatusType statusType;
	
	/**
	 * 用户类型
	 */
	public UserType userType;
	
	/**
	 * 排序分值
	 */
	public int orderValue;
	
	/**
	 * 设备类型
	 */
	public DeviceType deviceType;
	
	/**
	 * 客户端类型
	 */
	public LiveChatClient.ClientType clientType;
	
	/**
	 * 客户端版本号
	 */
	public String clientVersion;
}
