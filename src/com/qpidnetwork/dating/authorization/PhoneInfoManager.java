package com.qpidnetwork.dating.authorization;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import android.content.Context;
import android.content.SharedPreferences;
import android.telephony.TelephonyManager;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.view.WindowManager;

import com.qpidnetwork.dating.QpidApplication;
import com.qpidnetwork.manager.WebSiteManager;
import com.qpidnetwork.request.RequestJni;
import com.qpidnetwork.request.RequestJniOther;
import com.qpidnetwork.request.RequestJniOther.ActionType;
import com.qpidnetwork.request.item.LoginItem;

/**
 * 请求收集手机硬件信息管理器
 * @author Samson Fan
 *
 */
public class PhoneInfoManager {
	
	/**
	 * 请求收集手机硬件信息
	 * @param item
	 */
	public static void RequestPhoneInfo(Context context, LoginItem item)
	{
		// 是否发送请求
		boolean isRequest = false;
		// 是否新用户(非新安装)
		boolean isNewUser = false;
		
		// 获取缓存信息
		PhoneInfoParam param = GetPhoneInfoParam(context);
		if (null != param) {
			// 设置请求默认为true
			isRequest = true;
			// 查找用户是否已经登录过
			for (String userId : param.preLoginUserList) {
				if (userId.compareTo(item.manid) == 0) {
					// 不是新用户，不发送请求
					isRequest = false;
					break;
				}
			}
			
			if (isRequest) {
				// 是新用户
				isNewUser = true;
				param.preLoginUserList.add(item.manid);
			}
		}
		else {
			// 从未登录过，需要发送新安装请求
			isRequest = true;
			param = new PhoneInfoParam();
			param.preLoginUserList.add(item.manid);
		}
		
		// 保存信息
		if (null != param && isRequest) {
			SavePhoneInfoParam(context, param);
		}
		
		// 发送请求
		if (isRequest) {
			ActionType actionType = isNewUser ? ActionType.NEWUSER : ActionType.SETUP;
			TelephonyManager tm = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
			int siteId = WebSiteManager.getInstance().GetWebSite().getSiteId(); 
			DisplayMetrics dm = new DisplayMetrics();
			WindowManager wm = (WindowManager)context.getSystemService(Context.WINDOW_SERVICE);
			wm.getDefaultDisplay().getMetrics(dm);
			RequestJniOther.PhoneInfo(
				item.manid,
				QpidApplication.versionCode, 
				QpidApplication.versionName, 
				actionType, 
				siteId, 
				dm.density, 
				dm.widthPixels, 
				dm.heightPixels, 
				tm.getLine1Number(), 
				tm.getSimOperatorName(), 
				tm.getSimOperator(), 
				tm.getSimCountryIso(), 
				String.valueOf(tm.getSimState()), 
				tm.getPhoneType(), 
				tm.getNetworkType(), 
				RequestJni.GetDeviceId(tm), 
				null);
		}
	}
	
	/**
	 * 缓存PhoneInfo参数
	 * @param context
	 * @param item		
	 */
	public static void SavePhoneInfoParam(Context context, PhoneInfoParam item) {
		SharedPreferences mSharedPreferences = context.getSharedPreferences("base64", Context.MODE_PRIVATE); 
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();  
        ObjectOutputStream oos;
		try {
			oos = new ObjectOutputStream(baos);
			if( item != null ) {
		        oos.writeObject(item);  
			}
	        String personBase64 = new String(Base64.encode(baos.toByteArray(), Base64.DEFAULT));  
	        SharedPreferences.Editor editor = mSharedPreferences.edit();  
	        editor.putString("PhoneInfoParam", personBase64);  
	        editor.commit();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}  
	}
	
	/**
	 * 获取缓存PhoneInfo参数
	 * @param context
	 * @return		
	 */
	public static PhoneInfoParam GetPhoneInfoParam(Context context) {
		PhoneInfoParam item = null;
		
        try {  
            SharedPreferences mSharedPreferences = context.getSharedPreferences("base64", Context.MODE_PRIVATE);  
            String personBase64 = mSharedPreferences.getString("PhoneInfoParam", "");  
            byte[] base64Bytes = Base64.decode(personBase64.getBytes(), Base64.DEFAULT);  
            ByteArrayInputStream bais = new ByteArrayInputStream(base64Bytes);  
            ObjectInputStream ois = new ObjectInputStream(bais);  
            item = (PhoneInfoParam) ois.readObject();  
        } catch (Exception e) {  
            e.printStackTrace();  
        }  
        
		return item;
	}
}
