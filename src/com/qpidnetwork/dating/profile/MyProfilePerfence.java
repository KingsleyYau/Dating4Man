package com.qpidnetwork.dating.profile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;

import com.qpidnetwork.request.item.LadyMatch;
import com.qpidnetwork.request.item.OtherGetCountItem;
import com.qpidnetwork.request.item.ProfileItem;

/**
 * MyProfile模块
 * @author Max.Chiu
 */
public class MyProfilePerfence {
	/**
	 * 缓存个人资料
	 * @param context	上下文
	 * @param item		个人资料
	 */
	public static void SaveProfileItem(Context context, ProfileItem item) {
		SharedPreferences mSharedPreferences = context.getSharedPreferences("base64", Context.MODE_PRIVATE); 
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();  
        ObjectOutputStream oos;
		try {
			oos = new ObjectOutputStream(baos);
			if( item != null)
				oos.writeObject(item);  
			else {
				
			}
	        String personBase64 = new String(Base64.encode(baos.toByteArray(), Base64.DEFAULT));  
	        SharedPreferences.Editor editor = mSharedPreferences.edit();  
	        editor.putString("ProfileItem", personBase64);  
	        editor.commit();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}  
	}
	
	/**
	 * 获取缓存个人资料
	 * @param context	上下文
	 * @return			个人资料
	 */
	public static ProfileItem GetProfileItem(Context context) {
		ProfileItem item = null;
		
        try {  
            SharedPreferences mSharedPreferences = context.getSharedPreferences("base64", Context.MODE_PRIVATE);  
            String personBase64 = mSharedPreferences.getString("ProfileItem", "");  
            byte[] base64Bytes = Base64.decode(personBase64.getBytes(), Base64.DEFAULT);  
            ByteArrayInputStream bais = new ByteArrayInputStream(base64Bytes);  
            ObjectInputStream ois = new ObjectInputStream(bais);  
            item = (ProfileItem) ois.readObject();  
        } catch (Exception e) {  
            e.printStackTrace();  
        }  
        
		return item;
	}
	
	/**
	 * 缓存信用点
	 * @param context	上下文
	 * @param item		个人资料
	 */
	public static void SaveOtherGetCountItem(Context context, OtherGetCountItem item) {
		SharedPreferences mSharedPreferences = context.getSharedPreferences("base64", Context.MODE_PRIVATE); 
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();  
        ObjectOutputStream oos;
		try {
			oos = new ObjectOutputStream(baos);
			if( item != null )
				oos.writeObject(item);  
	        String personBase64 = new String(Base64.encode(baos.toByteArray(), Base64.DEFAULT));  
	        SharedPreferences.Editor editor = mSharedPreferences.edit();  
	        editor.putString("OtherGetCountItem", personBase64);  
	        editor.commit();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}  
	}
	
	/**
	 * 获取缓存信用点
	 * @param context	上下文
	 * @return			个人资料
	 */
	public static OtherGetCountItem GetOtherGetCountItem(Context context) {
		OtherGetCountItem item = null;
		
        try {  
            SharedPreferences mSharedPreferences = context.getSharedPreferences("base64", Context.MODE_PRIVATE);  
            String personBase64 = mSharedPreferences.getString("OtherGetCountItem", "");  
            byte[] base64Bytes = Base64.decode(personBase64.getBytes(), Base64.DEFAULT);  
            ByteArrayInputStream bais = new ByteArrayInputStream(base64Bytes);  
            ObjectInputStream ois = new ObjectInputStream(bais);  
            item = (OtherGetCountItem) ois.readObject();  
        } catch (Exception e) {  
            e.printStackTrace();  
        }  
        
		return item;
	}
	
	/**
	 * 缓存匹配女士条件
	 * @param context		上下文
	 * @param item			匹配女士item
	 */
	public static void SaveLadyMatch(Context context, LadyMatch item) {
		SharedPreferences mSharedPreferences = context.getSharedPreferences("base64", Context.MODE_PRIVATE); 
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();  
        ObjectOutputStream oos;
		try {
			oos = new ObjectOutputStream(baos);
			if( item != null )
				oos.writeObject(item);  
	        String personBase64 = new String(Base64.encode(baos.toByteArray(), Base64.DEFAULT));  
	        SharedPreferences.Editor editor = mSharedPreferences.edit();  
	        editor.putString("LadyMatch", personBase64);  
	        editor.commit();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}  
	}
	
	/**
	 * 获取缓存女士匹配条件
	 * @param context		上下文
	 * @return				匹配女士item
	 */
	public static LadyMatch GetLadyMatch(Context context) {
		LadyMatch item = null;
		
        try {  
            SharedPreferences mSharedPreferences = context.getSharedPreferences("base64", Context.MODE_PRIVATE);  
            String personBase64 = mSharedPreferences.getString("LadyMatch", "");  
            byte[] base64Bytes = Base64.decode(personBase64.getBytes(), Base64.DEFAULT);  
            ByteArrayInputStream bais = new ByteArrayInputStream(base64Bytes);  
            ObjectInputStream ois = new ObjectInputStream(bais);  
            item = (LadyMatch) ois.readObject();  
        } catch (Exception e) {  
            e.printStackTrace();  
        }  
        
		return item;
	}
}
