package com.qpidnetwork.livechat;

import java.io.Serializable;

/**
 * 自定义一些本地消息，用于消息列表显示（如试聊提醒等特殊显示）
 * @author Hunter
 *
 */
public class LCLocalCustomItem implements Serializable{

	private static final long serialVersionUID = -6893137215841862065L;
	public enum CustomType{
		FREE_CHAT_CAN_USE,
		DEFAULT
	}
	public CustomType type = CustomType.DEFAULT;
}
