package com.qpidnetwork.dating.lovecall;

import java.util.Iterator;
import java.util.List;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;

public class DirectCallManager {
	
	private Context mContext;
	
	public DirectCallManager(Context context){
		mContext = context;
	}
	
	/**
	 * 直接呼叫传统电话
	 * @param callCenter callcenter号码
	 * @param tokenId 
	 */
	public void makeCall(String callCenter, String tokenId){
		
		Intent localIntent = new Intent("android.intent.action.CALL");
		String packedCallNumber = generatePhoneNumber(callCenter, tokenId);
		PackageManager localPackageManager = mContext.getPackageManager();
		List localList1 = phoneClients(localPackageManager, packedCallNumber);
		ResolveInfo localResolveInfo = findDefaultClient(localList1);
		if(localResolveInfo != null){
			/*找到系统默认的使用系统默认的*/
			mContext.startActivity(callIntent(packedCallNumber, localResolveInfo));
		}else{
			mContext.startActivity(callIntent(packedCallNumber, null));
		}
	}
	
	/**
	 * 生成呼叫用电话号码
	 * @param paramString
	 * @return
	 */
	private String generatePhoneNumber(String freecall, String tokenid){
	    String str1 = Uri.encode("#");
	    String str2 = Uri.encode(String.valueOf(','));
	    String str3 = Uri.encode("+");
	    return (str3 + freecall + str2 + tokenid + "#").replace("#", str1);
	}
	
	/*call default call*/
	private Intent callIntent(String paramString, ResolveInfo paramResolveInfo){
	    Intent localIntent = new Intent("android.intent.action.CALL");
	    localIntent.setData(Uri.parse("tel:" + paramString));
	    if ((paramResolveInfo != null) && (paramResolveInfo.activityInfo != null))
	      localIntent.setComponent(new ComponentName(paramResolveInfo.activityInfo.packageName, paramResolveInfo.activityInfo.name));
	    return localIntent;
	}
	
	private List<ResolveInfo> phoneClients(PackageManager paramPackageManager, String paramString){
	    return paramPackageManager.queryIntentActivities(callIntent(paramString, null), 0);
	}
	
	private ResolveInfo findDefaultClient(List<ResolveInfo> paramList){
	    if (paramList != null){
	        Iterator localIterator = paramList.iterator();
	        ResolveInfo localResolveInfo;
	        while (localIterator.hasNext()){
	        	localResolveInfo = (ResolveInfo)localIterator.next();
	        	if (isDefaultPhone(localResolveInfo))
	        		return localResolveInfo;
	        }
	    }
	    return null;
	}
	
	private boolean isDefaultPhone(ResolveInfo paramResolveInfo){
	    return (paramResolveInfo.activityInfo != null) && (paramResolveInfo.activityInfo.packageName.equals("com.android.phone"));
	 }
}
