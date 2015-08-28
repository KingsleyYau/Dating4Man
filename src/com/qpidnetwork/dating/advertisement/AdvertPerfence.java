package com.qpidnetwork.dating.advertisement;

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

import com.qpidnetwork.manager.WebSiteManager;
import com.qpidnetwork.request.item.AdPushAdvert;

/**
 * 广告本地缓存 
 * @author Max.Chiu
 *
 */
public class AdvertPerfence {
	/**
	 * 缓存主界面浮窗广告
	 * @param context	上下文
	 * @param item		主界面浮窗广告
	 */
	public static void SaveAdMainAdvertItem(Context context, AdMainAdvertItem item) {
		if( item == null ) {
			return;
		}
		
		SharedPreferences mSharedPreferences = context.getSharedPreferences("base64", Context.MODE_PRIVATE); 
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();  
        ObjectOutputStream oos;
		try {
			oos = new ObjectOutputStream(baos);
	        oos.writeObject(item);  
	        String personBase64 = new String(Base64.encode(baos.toByteArray(), Base64.DEFAULT));  
	        SharedPreferences.Editor editor = mSharedPreferences.edit();  
	        editor.putString("AdMainAdvertItem" +  WebSiteManager.getInstance().GetWebSite().getSiteId(), personBase64);  
	        editor.commit();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}  
	}
	
	/**
	 * 获取缓存主界面浮窗广告
	 * @param context	上下文
	 * @return			主界面浮窗广告
	 */
	public static AdMainAdvertItem GetAdMainAdvert(Context context) {
		AdMainAdvertItem item = null;
		
        try {  
            SharedPreferences mSharedPreferences = context.getSharedPreferences("base64", Context.MODE_PRIVATE);  
            String personBase64 = mSharedPreferences.getString("AdMainAdvertItem" + WebSiteManager.getInstance().GetWebSite().getSiteId(), "");  
            byte[] base64Bytes = Base64.decode(personBase64.getBytes(), Base64.DEFAULT);  
            ByteArrayInputStream bais = new ByteArrayInputStream(base64Bytes);  
            ObjectInputStream ois = new ObjectInputStream(bais);  
            item = (AdMainAdvertItem) ois.readObject();  
        } catch (Exception e) {  
            e.printStackTrace();  
        }  
        
		return item;
	}
	
	/**
	 * 缓存女士列表广告
	 * @param context	上下文
	 * @param item		女士列表广告
	 */
	public static void SaveAdWomanListAdvertItem(Context context, AdWomanListAdvertItem item) {
		if( item == null ) {
			return;
		}
		
		SharedPreferences mSharedPreferences = context.getSharedPreferences("base64", Context.MODE_PRIVATE); 
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();  
        ObjectOutputStream oos;
		try {
			oos = new ObjectOutputStream(baos);
	        oos.writeObject(item);  
	        String personBase64 = new String(Base64.encode(baos.toByteArray(), Base64.DEFAULT));  
	        SharedPreferences.Editor editor = mSharedPreferences.edit();  
	        editor.putString("AdWomanListAdvertItem" + WebSiteManager.getInstance().GetWebSite().getSiteId(), personBase64);  
	        editor.commit();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}  
	}
	
	/**
	 * 获取缓存女士列表广告
	 * @param context	上下文
	 * @return			女士列表广告
	 */
	public static AdWomanListAdvertItem GetAdWomanListAdvertItem(Context context) {
		AdWomanListAdvertItem item = null;
		
        try {  
            SharedPreferences mSharedPreferences = context.getSharedPreferences("base64", Context.MODE_PRIVATE);  
            String personBase64 = mSharedPreferences.getString("AdWomanListAdvertItem" + WebSiteManager.getInstance().GetWebSite().getSiteId(), "");  
            byte[] base64Bytes = Base64.decode(personBase64.getBytes(), Base64.DEFAULT);  
            ByteArrayInputStream bais = new ByteArrayInputStream(base64Bytes);  
            ObjectInputStream ois = new ObjectInputStream(bais);  
            item = (AdWomanListAdvertItem) ois.readObject();  
        } catch (Exception e) {  
            e.printStackTrace();  
        }  
        
		return item;
	}
	
	/**
	 * 缓存Push广告列表
	 * @param context	上下文
	 * @param item		Push广告列表
	 */
	public static void SaveAdPushAdvertList(Context context, List<AdPushAdvert> list) {
		if( list == null ) {
			return;
		}
		
		SharedPreferences mSharedPreferences = context.getSharedPreferences("base64", Context.MODE_PRIVATE); 
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();  
        ObjectOutputStream oos;
		try {
			oos = new ObjectOutputStream(baos);
			
			for(AdPushAdvert item : list) {
				oos.writeObject(item); 
			}

	        String personBase64 = new String(Base64.encode(baos.toByteArray(), Base64.DEFAULT));  
	        SharedPreferences.Editor editor = mSharedPreferences.edit();  
	        editor.putString("AdPushAdvertList" + WebSiteManager.getInstance().GetWebSite().getSiteId(), personBase64);  
	        editor.commit();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}  
	}
	
	/**
	 * 获取缓存Push广告列表
	 * @param context	上下文
	 * @return			Push广告列表
	 */
	public static List<AdPushAdvert> GetAdPushAdvert(Context context) {
		List<AdPushAdvert> list = new ArrayList<AdPushAdvert>();
		
        try {  
            SharedPreferences mSharedPreferences = context.getSharedPreferences("base64", Context.MODE_PRIVATE);  
            String personBase64 = mSharedPreferences.getString("AdPushAdvertList" + WebSiteManager.getInstance().GetWebSite().getSiteId(), "");  
            byte[] base64Bytes = Base64.decode(personBase64.getBytes(), Base64.DEFAULT);  
            ByteArrayInputStream bais = new ByteArrayInputStream(base64Bytes);  
            ObjectInputStream ois = new ObjectInputStream(bais);  
            
            AdPushAdvert item = (AdPushAdvert) ois.readObject();  
            while( item != null ) {
            	list.add(item);
            	item = (AdPushAdvert) ois.readObject();
            }

        } catch (Exception e) {  
            e.printStackTrace();  
        }  
        
		return list;
	}

}
