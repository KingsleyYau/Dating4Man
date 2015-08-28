package com.qpidnetwork.dating.advertisement;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.text.format.Time;
import android.widget.RemoteViews;

import com.qpidnetwork.dating.R;
import com.qpidnetwork.dating.home.HomeActivity;
import com.qpidnetwork.request.item.AdMainAdvert.OpenType;

/**
 * 广告消息中心
 * @author Max.Chiu
 *
 */
@SuppressWarnings("deprecation")
public class AdvertNotification {

	private static final int NOTIFICATION_BASE_ID = 10000;
	private static final int NOTIFICATION_MAX_COUNT = 10;
	private static int mCurNotificationId = NOTIFICATION_BASE_ID;

	
	private static AdvertNotification gAdvertNotification;
	private Context mContext = null;
	private NotificationManager mNotification;
	
	public static AdvertNotification newInstance(Context context) {
		if( gAdvertNotification == null ) {
			gAdvertNotification = new AdvertNotification(context);
		} 
		return gAdvertNotification;
	}
	
	public static AdvertNotification getInstance() {
		return gAdvertNotification;
	}
	
	public AdvertNotification(Context context)  {
		mContext = context;
		mNotification = (NotificationManager)mContext.getSystemService(Context.NOTIFICATION_SERVICE);
		mCurNotificationId = NOTIFICATION_BASE_ID;
	}
	
	/**
	 * 通知栏显示通知
	 * @param icon
	 * @param text
	 * @param title
	 * @param content
	 * @param isVibrate
	 * @param isSound
	 * @param isAutoCancel
	 */
	public void ShowNotification(
			int icon, 
			String tickerText, 
			String title, 
			String content,
			String adurl,
			OpenType openType,
    		boolean isVibrate, 
    		boolean isSound, 
    		boolean isAutoCancel
    		) {
		
		// 去除旧的通知栏消息
		mCurNotificationId = NOTIFICATION_BASE_ID + ++mCurNotificationId %  NOTIFICATION_MAX_COUNT;
		mNotification.cancel(mCurNotificationId);
		
		// 创建新的通知
    	Notification notification = new Notification();
    	
    	// 点击关闭
    	notification.flags = Notification.FLAG_AUTO_CANCEL;	
    	
    	// 振动
    	if( isVibrate ) {
    		notification.defaults |= Notification.DEFAULT_VIBRATE;
    		long[] vibrate = {0, 100, 200, 300};
    		notification.vibrate = vibrate;
    	}
    	
    	// 声音
    	if( isSound ) {
    		notification.defaults |= Notification.DEFAULT_SOUND;
    	}
    	
        // 状态栏
    	notification.icon = icon;
    	notification.tickerText = tickerText;
    	
    	// 自定义通知栏
    	RemoteViews contentView = new RemoteViews(mContext.getPackageName(), R.layout.notification_advert_push);  
        contentView.setImageViewResource(R.id.imageView, icon);  
        contentView.setTextViewText(R.id.textViewTitle, title); 
        contentView.setTextViewText(R.id.textViewContent, content);
        
        Time time = new Time();
		time.setToNow();
        contentView.setTextViewText(R.id.textViewTime, 
        		String.format(
        				"%d-%d %d:%d:%d", 
        				time.month + 1, 
        				time.monthDay, 
        				time.hour, 
        				time.minute, 
        				time.second)
        				);
        
        notification.contentView = contentView; 
    	
        // 点击事件
    	Intent intent = new Intent();
    	
    	intent.setClass(mContext, HomeActivity.class);
    	intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
    	intent.putExtra(HomeActivity.START_ADVERT, true);
    	intent.putExtra(HomeActivity.OPENTYPE, openType.ordinal());
    	intent.putExtra(HomeActivity.URL, adurl);
		
        PendingIntent pt = PendingIntent.getActivity(mContext, 0, intent, 0);
    	notification.contentIntent = pt;
    	
    	mNotification.notify(mCurNotificationId, notification);
    }
}
