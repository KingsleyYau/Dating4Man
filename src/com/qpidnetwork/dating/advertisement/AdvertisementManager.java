package com.qpidnetwork.dating.advertisement;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.telephony.TelephonyManager;

import com.qpidnetwork.dating.QpidApplication;
import com.qpidnetwork.dating.R;
import com.qpidnetwork.dating.setting.SettingPerfence;
import com.qpidnetwork.dating.setting.SettingPerfence.NotificationItem;
import com.qpidnetwork.manager.FileCacheManager;
import com.qpidnetwork.request.OnAdMainAdvertCallback;
import com.qpidnetwork.request.OnAdPushAdvertCallback;
import com.qpidnetwork.request.RequestJni;
import com.qpidnetwork.request.RequestJniAdvert;
import com.qpidnetwork.request.item.AdMainAdvert;
import com.qpidnetwork.request.item.AdMainAdvert.OpenType;
import com.qpidnetwork.request.item.AdPushAdvert;
import com.qpidnetwork.tool.FileDownloader;

/**
 * 广告管理器
 * 
 * @author Max.Chiu
 * 
 */
public class AdvertisementManager implements OnAdMainAdvertCallback, 
											 OnAdPushAdvertCallback 
{

	private static AdvertisementManager gAdvertisementManager;
	private Context mContenxt = null;
	private Handler mHandler;
	private boolean isMainNewAdvert = false; //用于处理新广告与就广告显示次数，点击次数写入冲突问题
	/**
	 * 主界面广告显示列表
	 */
	private ArrayList<AdMainAdvertItem> mAdMainList = null; 

	public static AdvertisementManager newInstance(Context context) {
		if (gAdvertisementManager == null) {
			gAdvertisementManager = new AdvertisementManager(context);
		}
		return gAdvertisementManager;
	}

	public static AdvertisementManager getInstance() {
		return gAdvertisementManager;
	}

	public AdvertisementManager(Context context) {
		mContenxt = context;

		InitHandler();

		mAdPushAdvertList = AdvertPerfence.GetAdPushAdvert(mContenxt);
		if (mAdPushAdvertList != null) {
			mAdPushAdvertList = new ArrayList<AdPushAdvert>();
		}
		GetLastPushId();
		
		// 初始化显示主界面广告列表
		mAdMainList = new ArrayList<AdMainAdvertItem>();
	}

	/**
	 * 消息
	 */
	private enum MESSAGE_FLAG {
		PUSH_ADVERT_FLAG,
	}

	private AdMainAdvertItem mAdMainAdvertItem;

	public AdMainAdvertItem getAdMainAdvertItem() {
		return mAdMainAdvertItem;
	}
	
	/**
	 * 主界面广告是否有更新
	 * @return
	 */
	public boolean hasNewMainAdvert(){
		return isMainNewAdvert;
	}
	
	/**
	 * Push广告列表
	 */
	private List<AdPushAdvert> mAdPushAdvertList;

	/**
	 * 上一次获取的最后一条Push广告ID
	 */
	private String mPushId = "";

	
	/**
	 * 同步主界面浮窗广告
	 * @param tm
	 */
	public void SyncMainAdvert(){
		isMainNewAdvert = false;
		TelephonyManager tm = (TelephonyManager) mContenxt
				.getSystemService(Context.TELEPHONY_SERVICE);
		
		/**
		 * 查询浮窗广告
		 */
		RequestJniAdvert
				.MainAdvert(
						RequestJni.GetDeviceId(tm),
						(mAdMainAdvertItem != null && mAdMainAdvertItem.adMainAdvert != null) ? mAdMainAdvertItem.adMainAdvert.id: "0", 
						mAdMainAdvertItem.showTimes,
						mAdMainAdvertItem.clickTimes,
						this);
	}

	/**
	 * 查询Push广告
	 */
	public void PushAdvert() {
		TelephonyManager tm = (TelephonyManager) mContenxt
				.getSystemService(Context.TELEPHONY_SERVICE);
		RequestJniAdvert.PushAdvert(RequestJni.GetDeviceId(tm), mPushId, this);
	}

	/**
	 * 获取最后的pushId
	 * 
	 * @return 最后的pushId
	 */
	public void GetLastPushId() {
		if (mAdPushAdvertList != null) {
			for (AdPushAdvert item : mAdPushAdvertList) {
				if (item.id.compareTo(mPushId) > 0) {
					mPushId = item.id;
				}
			}
		}
	}

	/**
	 * 初始化消息接收
	 */
	@SuppressLint("HandlerLeak")
	public void InitHandler() {
		mHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				switch (MESSAGE_FLAG.values()[msg.what]) {
				case PUSH_ADVERT_FLAG: {
					// 显示推送广告
					if (mAdPushAdvertList != null) {
						for (AdPushAdvert item : mAdPushAdvertList) {
							String appName = mContenxt.getResources().getString(R.string.app_name);
							NotificationItem ni = SettingPerfence
									.GetNotificationItem(QpidApplication.getContext());

							boolean bSound = false;
							boolean bVibrate = true;

							switch (ni.mPushNotification) {
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
								bSound = true;
								bVibrate = false;
							}
							default: {
								bSound = false;
								bVibrate = false;
							}
								break;
							}
							AdvertNotification.getInstance().ShowNotification(
									R.drawable.ic_launcher,
									item.message, appName,
									item.message, item.adurl, item.openType,
									bVibrate, bSound);
						}
					}
				}
					break;
				default:
					break;
				}
			}
		};
	}

	/**
	 * 下载广告图片
	 * 
	 * @param url
	 */
	private void downloadAdvertImage(String url) {
		String localPath = FileCacheManager.getInstance()
				.CacheImagePathFromUrl(url);
		FileDownloader fileDownLoader = new FileDownloader(mContenxt);
		fileDownLoader.StartDownload(url, localPath, null);
	}
	
	/**
	 * 初始化本次使用浮窗广告及更新最新广告数据
	 */
	public void resetMainAdvertItem(){
		// 加载本地数据
		mAdMainAdvertItem = AdvertPerfence.GetAdMainAdvert(mContenxt);
		if (mAdMainAdvertItem == null) {
			mAdMainAdvertItem = new AdMainAdvertItem();
		}
		else {
//			// 判断是否需要显示广告
//			if (isMainAdvertShow()) {
//				// 添加到广告显示列表
//				mAdMainList.add(mAdMainAdvertItem);
//			}
		}
		/*服务器获取下次广告显示数据*/
		SyncMainAdvert();
	}

	/**
	 * 主界面弹窗广告是否显示
	 * 
	 * @return
	 */
	public boolean isMainAdvertShow() {
		boolean isCanShow = false;
		int currTime = (int) (System.currentTimeMillis() / 1000);
		if ((mAdMainAdvertItem != null)
				&& (mAdMainAdvertItem.adMainAdvert != null)
				&& (mAdMainAdvertItem.adMainAdvert.validTime >= currTime)
				&& (mAdMainAdvertItem.adMainAdvert.isShow)) {
			/* 广告在有效期且是可以显示的 */
			String localPath = FileCacheManager
					.getInstance()
					.CacheImagePathFromUrl(mAdMainAdvertItem.adMainAdvert.image);
			if (new File(localPath).exists()) {
				/* 图片已下载，显示广告 */
				isCanShow = true;
			} else {
				/* 下载但是不显示广告 */
				FileDownloader fileDownLoader = new FileDownloader(mContenxt);
				fileDownLoader.StartDownload(
						mAdMainAdvertItem.adMainAdvert.image, localPath, null);
			}
		}
		return isCanShow;
	}
	
	/**
	 * 显示主界面广告
	 * @param context	上下文
	 */
	public void showMainAdvert(Activity activity)
	{
		if (!mAdMainList.isEmpty() 
				&& null != activity) 
		{
			// 显示广告
			Intent intent = new Intent(activity, MainAdvertisementActivity.class);
			activity.startActivity(intent);
			
			// 移除已显示过的广告
			mAdMainList.remove(0);
		}
	}

	/**
	 * 广告跳转解析
	 * 
	 * @param url
	 * @param openType
	 */
	public void parseAdvertisment(Context context, String url, OpenType openType) {
		switch (openType) {
		case SYSTEMBROWER:
			openSystemBrowser(context, url);
			break;
		case APPBROWER:
		case HIDE:
			openWebviewUseUrl(context, url, openType);
			break;
		default:
			break;
		}
	}
	
	
	/**
	 * 使用默认浏览器打开默认Url
	 * 
	 * @param url
	 */
	public static void openSystemBrowser(Context context, String url) {
		Intent intent = new Intent();
		intent.setAction("android.intent.action.VIEW");
		Uri content_url = Uri.parse(url);
		intent.setData(content_url);
		context.startActivity(intent);
	}

	/**
	 * 使用App webviewactivity打开指定Url
	 * 
	 * @param context
	 * @param url
	 */
	public static void openWebviewUseUrl(Context context, String url, OpenType openType) {
		Intent intent = AdvertWebviewActivity.getIntent(context, url, openType);
		context.startActivity(intent);
	}

	@Override
	public void OnAdMainAdvert(boolean isSuccess, String errno, String errmsg,
			AdMainAdvert advert) {
		// TODO Auto-generated method stub
		if (isSuccess) {
			int curTime = (int)(System.currentTimeMillis()/1000);
			if (mAdMainAdvertItem == null
					|| mAdMainAdvertItem.adMainAdvert == null
					|| mAdMainAdvertItem.adMainAdvert.id.compareTo(advert.id) != 0) 
			{
				isMainNewAdvert = true;
				// 新广告，更新本地内存
				AdMainAdvertItem item = new AdMainAdvertItem();
				item.showTimes = 0;
				item.clickTimes = 0;
				item.adMainAdvert = advert;
				/*valid 需要转换一次，服务器返回的是最后有效期和当前的时间差*/
				item.adMainAdvert.validTime = item.adMainAdvert.validTime + curTime;
				
				AdvertPerfence
						.SaveAdMainAdvertItem(
								mContenxt,
								item);
				/* 下载广告图片用于显示 */
				if (advert.image != null) {
					downloadAdvertImage(advert.image);
				}
			}else{
				/*本地当前广告,更新最后有效时间*/
				mAdMainAdvertItem.adMainAdvert.validTime = advert.validTime + curTime;
				// 判断是否需要显示广告
				if (isMainAdvertShow()) {
					// 添加到广告显示列表
					mAdMainList.add(mAdMainAdvertItem);
				}
				AdvertPerfence.SaveAdMainAdvertItem(mContenxt, mAdMainAdvertItem);
			}
		}
	}

	@Override
	public void OnAdPushAdvert(boolean isSuccess, String errno, String errmsg,
			AdPushAdvert[] advert) {
		// TODO Auto-generated method stub
		if (isSuccess) {
			// 保存列表
			if (advert != null) {
				mAdPushAdvertList = Arrays.asList(advert);
			}

			// 获取最新Id
			GetLastPushId();

			// 本地缓存
			if (mAdPushAdvertList != null) {
				AdvertPerfence.SaveAdPushAdvertList(mContenxt,
						mAdPushAdvertList);
			}

			Message msg = Message.obtain();
			msg.what = MESSAGE_FLAG.PUSH_ADVERT_FLAG.ordinal();
			mHandler.sendMessage(msg);
		}
	}
}
