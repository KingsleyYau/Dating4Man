package com.qpidnetwork.dating.gcm;

import java.util.concurrent.atomic.AtomicInteger;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.text.TextUtils;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.qpidnetwork.dating.DefaultActivity;
import com.qpidnetwork.dating.QpidApplication;
import com.qpidnetwork.dating.R;
import com.qpidnetwork.dating.authorization.LoginManager;
import com.qpidnetwork.dating.authorization.LoginParam;
import com.qpidnetwork.dating.home.HomeActivity;
import com.qpidnetwork.dating.setting.SettingPerfence;
import com.qpidnetwork.dating.setting.SettingPerfence.NotificationItem;
import com.qpidnetwork.dating.setting.SettingPerfence.NotificationSetting;
import com.qpidnetwork.framework.util.NotificationGenerator;
import com.qpidnetwork.framework.util.StringUtil;

public class GcmIntentService extends IntentService{
	
	public static final int NOTIFICATION_BASE_ID = 60000;
	private static final int NOTIFICATION_MAX_COUNT = 10;
	public static final String NOTIFICATION_JUMP_URL = "jumpUrl";
	public static final String NOTIFICATION_SITE_ID = "siteId";
	private NotificationManager mNotificationManager;
	private static int mCurNotificationId = NOTIFICATION_BASE_ID;
	public static final String TAG = "hunter";
	
	
	public GcmIntentService(){
		super("GcmIntentService");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		// TODO Auto-generated method stub
		Bundle extra = intent.getExtras();
		GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
	
		if(extra.containsKey("notification")){
			Log.i(TAG, "onHandleIntent contain notification");
		}
		
		String messageType = gcm.getMessageType(intent);
		if(!extra.isEmpty()){
			if(GoogleCloudMessaging.MESSAGE_TYPE_SEND_ERROR.equals(messageType)){
//				sendNotification("Send Error: " + extra.toString());
				Log.i(TAG, "Send Error: " + extra.toString());
			}else if (GoogleCloudMessaging.MESSAGE_TYPE_DELETED.equals(messageType)){
//				sendNotification("Delete messages on server: " + extra.toString());
				Log.i(TAG, "Delete messages on server: " + extra.toString());
			}else if(GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE.equals(messageType)){
//				sendNotification("Received: " + extra.toString());
				Log.i(TAG, "Recieved: " + extra.toString());
				if(extra != null){
					parsePushMessgae(extra);
				}
			}
		}
	}
	
	private void parsePushMessgae(Bundle extra){
		String title = "";
		String body = "";
		String url = "";
		String toId = "";
		String siteId = "";
		if(extra.containsKey("title")){
			title = extra.getString("title");
		}
		if(extra.containsKey("body")){
			body = extra.getString("body");
		}
		if(extra.containsKey("jumpurl")){
			url = extra.getString("jumpurl");
		}
		if(extra.containsKey("toId")){
			toId = extra.getString("toId");
		}
		if(extra.containsKey("siteId")){
			siteId = extra.getString("siteId");
		}
		Log.i(TAG, "Receive Notification toId: " + toId + " title: " + title + " body: " + body + " jumpUlr: " + url + " siteId: " + siteId);
		LoginParam param = LoginManager.getInstance().GetLoginParam();
		if(param != null && param.item != null
				&& !TextUtils.isEmpty(param.item.manid)){
			if(param.item.manid.equals(toId)){
				sendNotification(title, body, url, siteId);
			}
		}
		
	}
	
	private void sendNotification(String title, String body, String url, String siteId){
		// 显示到消息中心
		NotificationItem ni = SettingPerfence.GetNotificationItem(QpidApplication.getContext());
		
		//Push开关打开
		if(ni.mMailNotification != NotificationSetting.None){
			boolean bSound = false;
			boolean bVibrate = true;
			switch (ni.mMailNotification) {
			case SoundWithVibrate: {
				bSound = true;
				bVibrate = true;
			}
				break;
			case Vibrate: {
				bSound = false;
				bVibrate = true;
			}
				break;
			case Silent: {
				bSound = false;
				bVibrate = false;
			}
			default: {
				bSound = false;
				bVibrate = false;
			}
				break;
			}
			
			mCurNotificationId = NOTIFICATION_BASE_ID + ++mCurNotificationId
					% NOTIFICATION_MAX_COUNT;
			
			mNotificationManager = (NotificationManager)this.getSystemService(Context.NOTIFICATION_SERVICE);
			
			Intent intent = new Intent(this, DefaultActivity.class);
			intent.putExtra(NOTIFICATION_JUMP_URL, url);
			intent.putExtra(NOTIFICATION_SITE_ID, Integer.valueOf(siteId));
//			PendingIntent contentIntent = PendingIntent.getActivity(this, 0, intent,  0);
			
			TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
			stackBuilder.addParentStack(DefaultActivity.class);
			stackBuilder.addNextIntent(intent);
			PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(NotificationGenerator.getNotificationRequestId(),
					PendingIntent.FLAG_UPDATE_CURRENT);
			
			NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
													.setSmallIcon(R.drawable.ic_launcher)
													.setContentTitle(title)
													.setStyle(new NotificationCompat.BigTextStyle().bigText(body))
													.setContentText(body);
	    	// 振动
	    	if( bVibrate ) {
	    		long[] vibrate = {0, 100, 200, 300};
	    		builder.setVibrate(vibrate);
	    	}
	    	
	    	// 声音
	    	if( bSound ) {
	    		builder.setDefaults(Notification.DEFAULT_SOUND);
	    	}
	    	
			// 点击关闭
//			builder.setAutoCancel(true);
	    	
			builder.setContentIntent(resultPendingIntent);
			mNotificationManager.notify(mCurNotificationId, builder.build());
		}
	}
}
