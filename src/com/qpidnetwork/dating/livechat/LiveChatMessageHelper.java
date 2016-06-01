package com.qpidnetwork.dating.livechat;

import android.content.Context;

import com.qpidnetwork.dating.R;
import com.qpidnetwork.livechat.LCMessageItem.MessageType;
import com.qpidnetwork.request.RequestJniLiveChat.FunctionType;

/**
 * Livechat 相关公共转换工具类
 * @author Hunter
 * @since 2016.4.13
 */
public class LiveChatMessageHelper {
	
	public static FunctionType getFunctionTypeByMsgType(MessageType type){
		FunctionType functionType = null;
		switch (type) {
		case Text:
			functionType = FunctionType.CHAT_TEXT;
			break;
		case Emotion:
			functionType = FunctionType.CHAT_EMOTION;
			break;
		case Voice:
			functionType = FunctionType.CHAT_VOICE;
			break;
		case Photo:
			functionType = FunctionType.CHAT_PRIVATEPHOTO;
			break;
		case MagicIcon:
			functionType = FunctionType.CHAT_MAGICICON;
			break;

		default:
			break;
		}
		return functionType;
	}
	
	public static String getNoSupportMessage(Context context, MessageType type){
		String message = "";
		switch (type) {
		case Text:
			message = context.getString(R.string.livechat_text_notsupported_error);
			break;
		case Emotion:
			message = context.getString(R.string.livechat_emotion_notsupported_error);
			break;
		case Voice:
			message = context.getString(R.string.livechat_voice_notsupported_error);
			break;
		case Photo:
			message = context.getString(R.string.livechat_photo_notsupported_error);
			break;
		case MagicIcon:
			message = context.getString(R.string.livechat_magicIcon_notsupported_error);
			break;

		default:
			break;
		}
		return message;
	}
}
