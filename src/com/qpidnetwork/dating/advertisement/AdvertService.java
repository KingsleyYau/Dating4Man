package com.qpidnetwork.dating.advertisement;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;

/**
 * 广告管理服务
 * @author Max.Chiu
 *
 */
public class AdvertService extends Service {
	
	/**
	 * 闹铃间隔, 2小时闹一次
	 */
	static final int INTERVAL = 2 * 60 * 60 * 1000;
	
	/**
	 * 闹铃广播
	 */
	static final String ALARM_ACTION = "com.qpidnetwork.dating.service.action.alarm";
	
	/**
	 * 消息
	 */
	private enum MESSAGE_FLAG {
		ALARM_FLAG,
	}
	
	/**
	 * 动态广播接收器
	 */
	private GroupReceiver mGroupReceiver;
	
	/**
	 * 消息处理
	 */
	private Handler mHandler;
	
	/**
	 * 服务绑定实例
	 */
	private AdvertBinder mAdvertBinder = new AdvertBinder();
	
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return mAdvertBinder;
	}

	@Override
	public void onCreate() {
		
		AdvertNotification.newInstance(this);
		
		// 初始化消息接收
		InitHandler();
		
		// 初始化广播接收
		InitReceiver();
		
		// 设置闹铃
		SetAlarmTime(this, INTERVAL);
		
//		for(int i = 0; i < 40; i++) {
//			AdvertNotification.getInstance().ShowNotification(
//					R.drawable.ic_launcher, 
//					"content" + ":" + String.valueOf(i), 
//					String.valueOf(i), 
//					"content", 
//					"http://www.baidu.com", 
//					OpenType.SYSTEMBROWER, 
//					true, 
//					true, 
//					true
//					);
//		}
	}
	
	/**
	 * 动态广播处理器
	 */
	public class GroupReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			String stringAction = intent.getAction();
    		if( stringAction.equals(ALARM_ACTION) ) {
    			// 获取Push广告闹铃
    			Message msg = Message.obtain();
    			msg.arg1 = MESSAGE_FLAG.ALARM_FLAG.ordinal();
    			mHandler.sendMessage(msg);
    		}
		}
	}

	/**
	 * 广告Bind
	 */
	public class AdvertBinder extends IAdvertBinder.Stub {

		@Override
		public void Start() throws RemoteException {
			// TODO Auto-generated method stub
			// 同步广告
//			AdvertisementManager am = AdvertisementManager.getInstance();
//			am.SyncAdvert();
		}
	}
	
	/**
	 * 设置闹铃
	 * @param context
	 * @param interval
	 */
	public void SetAlarmTime(Context context, long interval) {
		AlarmManager am = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
		Intent intent = new Intent(ALARM_ACTION);
		PendingIntent sender = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
		am.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), interval, sender);
	}
	
	/**
	 * 初始化动态广播接收器
	 */
	public void InitReceiver() {
    	mGroupReceiver = new GroupReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(ALARM_ACTION);
        registerReceiver(mGroupReceiver, filter);
    }
	
	/**
	 * 初始化消息接收
	 */
	public void InitHandler() {
		mHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				switch ( MESSAGE_FLAG.values()[msg.what] ) {
				case ALARM_FLAG:{
					// 闹铃
					AdvertisementManager am = AdvertisementManager.getInstance();
					am.PushAdvert();
				}break;
				default:break;
				}
			}
		};
	}
	
	
}
