package com.qpidnetwork.manager;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;

import com.qpidnetwork.request.item.Gift;

public class VirtualGiftPerfence {
	/**
	 * 获取虚拟礼物列表缓存key(分站点)
	 * @param context
	 * @return
	 */
	private static String GetGiftListKey(Context context)
	{
		String key = "GiftList_" + WebSiteManager.getInstance().GetWebSite().getSiteId();
		return key;
	}
	
	/**
	 * 缓存虚拟礼物列表
	 * @param context	上下文
	 * @param item		个人资料
	 */
	public static void SaveGiftList(Context context, List<Gift> list) {
		SharedPreferences mSharedPreferences = context.getSharedPreferences("base64", Context.MODE_PRIVATE); 
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();  
        ObjectOutputStream oos;
		try {
			oos = new ObjectOutputStream(baos);
			
			if( list != null ) {
				for(Gift item : list) {
					oos.writeObject(item); 
				}
			}
			String key = GetGiftListKey(context);
			String personBase64 = new String(Base64.encode(baos.toByteArray(), Base64.DEFAULT));  
	        SharedPreferences.Editor editor = mSharedPreferences.edit();  
	        editor.putString(key, personBase64);  
	        editor.commit();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}  
	}
	
	/**
	 * 获取缓存本虚拟礼物列表
	 * @param context	上下文
	 * @return			个人资料
	 */
	public static List<Gift> GetGiftList(Context context) {
		List<Gift> list = new ArrayList<Gift>();
		
        try {  
        	String key = GetGiftListKey(context);
            SharedPreferences mSharedPreferences = context.getSharedPreferences("base64", Context.MODE_PRIVATE);  
            String personBase64 = mSharedPreferences.getString(key, "");  
            byte[] base64Bytes = Base64.decode(personBase64.getBytes(), Base64.DEFAULT);  
            ByteArrayInputStream bais = new ByteArrayInputStream(base64Bytes);  
            ObjectInputStream ois = new ObjectInputStream(bais);  
            
            Gift item = (Gift) ois.readObject();  
            while( item != null ) {
            	list.add(item);
            	item = (Gift) ois.readObject();
            }

        } catch (Exception e) {  
            e.printStackTrace();  
        }  
        
		return list;
	}
	
	/**
	 * 获取虚拟礼物版本号key(分站点)
	 * @param context
	 * @return
	 */
	private static String GetGiftVersionKey(Context context)
	{
		String key = "GiftVersion_" + WebSiteManager.getInstance().GetWebSite().getSiteId();
		return key;
	}
	
	/**
	 * 缓存虚拟礼物版本号
	 * @param context	上下文
	 * @param item		个人资料
	 */
	public static void SaveGiftVersion(Context context, String version) {
		SharedPreferences mSharedPreferences = context.getSharedPreferences("base64", Context.MODE_PRIVATE); 
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();  
        ObjectOutputStream oos;
		try {
			oos = new ObjectOutputStream(baos);
			
			if( version != null ) {
				oos.writeObject(version);
			}

			String key = GetGiftVersionKey(context);
	        String personBase64 = new String(Base64.encode(baos.toByteArray(), Base64.DEFAULT));  
	        SharedPreferences.Editor editor = mSharedPreferences.edit();  
	        editor.putString(key, personBase64);  
	        editor.commit();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}  
	}
	/**
	 * 获取缓存本虚拟礼物版本号
	 * @param context	上下文
	 * @return			版本号
	 */
	public static String GetGiftVersion(Context context) {
		String version = "";
		
        try {  
        	String key = GetGiftVersionKey(context);
            SharedPreferences mSharedPreferences = context.getSharedPreferences("base64", Context.MODE_PRIVATE);  
            String personBase64 = mSharedPreferences.getString(key, "");  
            byte[] base64Bytes = Base64.decode(personBase64.getBytes(), Base64.DEFAULT);  
            ByteArrayInputStream bais = new ByteArrayInputStream(base64Bytes);  
            ObjectInputStream ois = new ObjectInputStream(bais);  
            
            version = (String) ois.readObject();

        } catch (Exception e) {  
            e.printStackTrace();  
        }  
        
		return version;
	}
}
