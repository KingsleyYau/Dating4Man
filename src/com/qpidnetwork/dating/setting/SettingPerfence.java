package com.qpidnetwork.dating.setting;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;

import com.qpidnetwork.request.item.OtherVersionCheckItem;

public class SettingPerfence {
	public enum Notification {
		SoundWithVibrate,
		Vibrate,
		Silent,
		None,
	}
	
	public static class NotificationItem implements Serializable {
		/**
		 * 
		 */
		private static final long serialVersionUID = 4453217563778925265L;
		public NotificationItem() {
			mChatNotification = Notification.Vibrate;
			mMailNotification = Notification.Vibrate;
			mPushNotification = Notification.Vibrate;
		}
		
		public Notification mChatNotification;
		public Notification mMailNotification;
		public Notification mPushNotification;
	}
	
	/**
	 * 缓存Notification配置
	 * @param context	上下文
	 * @param item		
	 */
	public static void SaveNotificationItem(Context context, NotificationItem item) {
		SharedPreferences mSharedPreferences = context.getSharedPreferences("base64", Context.MODE_PRIVATE); 
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();  
        ObjectOutputStream oos;
		try {
			oos = new ObjectOutputStream(baos);
	        oos.writeObject(item);  
	        String personBase64 = new String(Base64.encode(baos.toByteArray(), Base64.DEFAULT));  
	        SharedPreferences.Editor editor = mSharedPreferences.edit();  
	        editor.putString("NotificationItem", personBase64);  
	        editor.commit();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	} 
	
	/**
	 * 获取缓存Notification
	 * @param context	上下文
	 * @return			
	 */
	public static NotificationItem GetNotificationItem(Context context) {
		NotificationItem item = new NotificationItem();
		
        try {  
            SharedPreferences mSharedPreferences = context.getSharedPreferences("base64", Context.MODE_PRIVATE);  
            String personBase64 = mSharedPreferences.getString("NotificationItem", "");  
            byte[] base64Bytes = Base64.decode(personBase64.getBytes(), Base64.DEFAULT);  
            ByteArrayInputStream bais = new ByteArrayInputStream(base64Bytes);  
            ObjectInputStream ois = new ObjectInputStream(bais);  
            item = (NotificationItem) ois.readObject();  
        } catch (Exception e) {  
            e.printStackTrace();  
        }  
        
		return item;
	}
	
	/**
	 * 缓存版本信息
	 * @param context	上下文
	 * @param item		个人资料
	 */
	public static void SaveOtherVersionCheckItem(Context context, OtherVersionCheckItem item) {
		SharedPreferences mSharedPreferences = context.getSharedPreferences("base64", Context.MODE_PRIVATE); 
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();  
        ObjectOutputStream oos;
		try {
			oos = new ObjectOutputStream(baos);
	        oos.writeObject(item);  
	        String personBase64 = new String(Base64.encode(baos.toByteArray(), Base64.DEFAULT));  
	        SharedPreferences.Editor editor = mSharedPreferences.edit();  
	        editor.putString("OtherVersionCheckItem", personBase64);  
	        editor.commit();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}  
	}
	
	/**
	 * 获取缓存版本信息
	 * @param context	上下文
	 * @return			个人资料
	 */
	public static OtherVersionCheckItem GetOtherVersionCheckItem(Context context) {
		OtherVersionCheckItem item = null;
		
        try {  
            SharedPreferences mSharedPreferences = context.getSharedPreferences("base64", Context.MODE_PRIVATE);  
            String personBase64 = mSharedPreferences.getString("OtherVersionCheckItem", "");  
            byte[] base64Bytes = Base64.decode(personBase64.getBytes(), Base64.DEFAULT);  
            ByteArrayInputStream bais = new ByteArrayInputStream(base64Bytes);  
            ObjectInputStream ois = new ObjectInputStream(bais);  
            item = (OtherVersionCheckItem) ois.readObject();  
        } catch (Exception e) {  
            e.printStackTrace();  
        }  
        
		return item;
	}
}
