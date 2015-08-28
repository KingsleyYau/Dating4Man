package com.qpidnetwork.dating.contacts;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;

import com.qpidnetwork.dating.QpidApplication;
import com.qpidnetwork.dating.R;
import com.qpidnetwork.dating.authorization.LoginManager.OnLoginManagerCallback;
import com.qpidnetwork.dating.authorization.LoginParam;
import com.qpidnetwork.dating.authorization.LoginPerfence;
import com.qpidnetwork.dating.bean.ContactBean;
import com.qpidnetwork.dating.bean.LoveCallBean;
import com.qpidnetwork.dating.bean.RequestBaseResponse;
import com.qpidnetwork.dating.contacts.ContactSearchType.LabelType;
import com.qpidnetwork.dating.googleanalytics.GAFragmentActivity;
import com.qpidnetwork.dating.lady.LadyDetailManager;
import com.qpidnetwork.dating.lady.LadyDetailManager.OnLadyDetailManagerQueryLadyDetailCallback;
import com.qpidnetwork.dating.livechat.LiveChatNotification;
import com.qpidnetwork.dating.setting.SettingPerfence;
import com.qpidnetwork.dating.setting.SettingPerfence.NotificationItem;
import com.qpidnetwork.framework.util.Log;
import com.qpidnetwork.framework.util.StringUtil;
import com.qpidnetwork.livechat.LCEmotionItem;
import com.qpidnetwork.livechat.LCMessageItem;
import com.qpidnetwork.livechat.LCMessageItem.MessageType;
import com.qpidnetwork.livechat.LCMessageItem.SendType;
import com.qpidnetwork.livechat.LCUserItem;
import com.qpidnetwork.livechat.LCUserItem.ChatType;
import com.qpidnetwork.livechat.LiveChatManager;
import com.qpidnetwork.livechat.LiveChatManagerEmotionListener;
import com.qpidnetwork.livechat.LiveChatManagerMessageListener;
import com.qpidnetwork.livechat.LiveChatManagerOtherListener;
import com.qpidnetwork.livechat.LiveChatManagerPhotoListener;
import com.qpidnetwork.livechat.LiveChatManagerTryTicketListener;
import com.qpidnetwork.livechat.LiveChatManagerVideoListener;
import com.qpidnetwork.livechat.LiveChatManagerVoiceListener;
import com.qpidnetwork.livechat.jni.LiveChatClient.UserStatusType;
import com.qpidnetwork.livechat.jni.LiveChatClientListener.KickOfflineType;
import com.qpidnetwork.livechat.jni.LiveChatClientListener.LiveChatErrType;
import com.qpidnetwork.livechat.jni.LiveChatClientListener.TalkEmfNoticeType;
import com.qpidnetwork.livechat.jni.LiveChatClientListener.TryTicketEventType;
import com.qpidnetwork.manager.WebSiteManager;
import com.qpidnetwork.request.OnLadyRecentContactListCallback;
import com.qpidnetwork.request.RequestJniLiveChat.VideoPhotoType;
import com.qpidnetwork.request.RequestOperator;
import com.qpidnetwork.request.item.Coupon;
import com.qpidnetwork.request.item.LadyDetail;
import com.qpidnetwork.request.item.LadyRecentContactItem;
import com.qpidnetwork.request.item.LoginErrorItem;
import com.qpidnetwork.request.item.LoginItem;
import com.qpidnetwork.request.item.OtherEmotionConfigItem;

public class ContactManager implements OnLoginManagerCallback {
	
	public static final String LIVE_CHAT_KICK_OFF = "kickoff";

	/**
	 * 请求消息
	 */
	private enum RequestFlag {
		REQUEST_LIVECHAT_NOTIFICATION_SUCCESS,
		REQUEST_LIVECHAT_EMF_CONATCT_UPDATE,
		REQUEST_LIVECHAT_MESSAGE_CONATCT_UPDATE,
		REQUEST_LIVECHAT_KICK_OFF
	}
	
	
	protected final String tag = getClass().getName();

	private static final long CONTACTLIST_MIN_INTERVAL = 30 * 60 * 1000; // 请求联系人列表最小时间间隔
	private static final long CONTACTLIST_STATUS_INTERVAL = 5 * 60 * 1000; // 获取联系人在线状态，5分钟一次
	private long mListLastUpdate; // 上次接口请求列表刷新timestamp

	private static ContactManager mContactManager;
	private Context mContext;
	private List<OnContactUpdateCallback> mCallbackList;
	private List<ContactBean> mContactList;
	private Map<String, ContactBean> mContactsMap;// 简历联系人索引方便读取及查找

	private Map<String, List<ContactBean>> mLocalContactMap; // 存储本地联系人，防止重登陆及换站时清空（目前主要存储男端发起请求添加，添加四站分站处理）

	private LiveChatManager mLiveChatManager;
	private HandlerThread mHandlerThread;
	
	public Handler mHandler;
	public String mWomanId = "";// 当前正在聊天的女士Id，方便未读统计

	/* invite 更新监控 */
	private List<OnNewInviteUpdateCallback> mInviteCallbackList;

	@SuppressLint("HandlerLeak")
	public void InitHandler() {
		// TODO Auto-generated method stub
		mHandler = new Handler() {
			public void handleMessage(android.os.Message msg) {
				switch ( RequestFlag.values()[msg.what] ) {
				case REQUEST_LIVECHAT_NOTIFICATION_SUCCESS:{
					// 非当前在聊女士
					RequestBaseResponse obj = (RequestBaseResponse) msg.obj;
					LCMessageItem item = (LCMessageItem) obj.body;
					LiveChatNotification nt = LiveChatNotification.newInstance(QpidApplication.getContext());
					NotificationItem ni = SettingPerfence.GetNotificationItem(QpidApplication.getContext());
					
					boolean bSound = false;
					boolean bVibrate = true;
					
					switch (ni.mPushNotification) {
					case SoundWithVibrate:{
						bSound = true;
						bVibrate = true;
					}break;
					case Vibrate:{
						bSound = false;
						bVibrate = true;
					}break;
					case Silent:{
						bSound = true;
						bVibrate = false;
					}
					default:{
						bSound = false;
						bVibrate = false;	
					}break;
					}
					
//					ExpressionImageGetter imageGetter = new ExpressionImageGetter(
//							QpidApplication.getContext(), 
//							UnitConversion.dip2px(QpidApplication.getContext(), 28),
//							UnitConversion.dip2px(QpidApplication.getContext(), 28)
//							);
					String tips = "";
					if(item.msgType != MessageType.Text){
						tips = item.getUserItem().userName + ": " + generateMsgHint(item);
					}else{
						String msgTemp = (item.getTextItem().message != null) ? item
								.getTextItem().message : "";
						msgTemp = msgTemp.replaceAll("\\[\\w*:[0-9]*\\]", "[smiley]");
						tips = item.getUserItem().userName + ": " + msgTemp;
					}
					nt.ShowNotification(
							R.drawable.logo_40dp, 
							tips,
							bVibrate,
							bSound
							);
				}break;
				case REQUEST_LIVECHAT_KICK_OFF:{
					/*Livechat 被踢处理,发送广播通知界面*/
					Intent intent = new Intent();
					intent.setAction(GAFragmentActivity.LIVECHAT_KICKOFF_ACTION);
					intent.putExtra(LIVE_CHAT_KICK_OFF, msg.arg1);
					QpidApplication.getContext().sendBroadcast(intent);
				}break;
				case REQUEST_LIVECHAT_EMF_CONATCT_UPDATE:{
					/*EMF 等更新联系人*/
					String usrId = (String)msg.obj;
					LadyDetailManager.getInstance().QueryLadyDetail(usrId, new OnLadyDetailManagerQueryLadyDetailCallback() {
						
						@Override
						public void OnQueryLadyDetailCallback(boolean isSuccess, String errno,
								String errmsg, LadyDetail item) {
							// TODO Auto-generated method stub
							if(isSuccess){
								updateBySendEMF(item);
							}
						}
					});
				}break;
				case REQUEST_LIVECHAT_MESSAGE_CONATCT_UPDATE:{
					/*EMF 等更新联系人*/
					LCUserItem userItem = (LCUserItem)msg.obj;
					boolean isInvite = msg.arg1 == 1? true: false;
					updateByMessage(userItem, isInvite);
				}break;
				default:
					break;
				}
			};
		};
	}
	
	/**
	 * 实现任务线程，实现同步操作
	 */
	private Handler handler = new Handler(getLooper()) {
		public void handleMessage(android.os.Message msg) {
			if (msg.obj instanceof Runnable) {
				Runnable task = (Runnable) (msg.obj);
				task.run();
			}
		};
	};

	private Looper getLooper() {
		mHandlerThread = new HandlerThread("Contacts");
		mHandlerThread.start();
		return mHandlerThread.getLooper();
	}
	
	public static ContactManager newInstance(Context context) {
		if (mContactManager == null) {
			mContactManager = new ContactManager(context);
		}
		return mContactManager;
	}

	public static ContactManager getInstance() {
		return mContactManager;
	}

	public ContactManager(Context context) {
		this.mContext = context;
		mCallbackList = new ArrayList<OnContactUpdateCallback>();
		mContactList = new ArrayList<ContactBean>();
		mContactsMap = new HashMap<String, ContactBean>();
		mLiveChatManager = LiveChatManager.newInstance(null);
		mLocalContactMap = new HashMap<String, List<ContactBean>>();
		mInviteCallbackList = new ArrayList<OnNewInviteUpdateCallback>();
		resetContact();
		addLiveChatListener();
		
		// 初始化事件监听
		InitHandler();
	}

	/**
	 * 资源回收
	 */
	public void onDestroy() {
		if (mHandlerThread != null) {
			mHandlerThread.interrupt();
			mHandlerThread = null;
		}
		Log.d("contact", "ContactManager::onDestroy() synchronized mContactList begin");
		synchronized (mContactList) {
			mListLastUpdate = 0;
			if (mContactList != null) {
				mContactList.clear();
			}
			if (mContactsMap != null) {
				mContactsMap.clear();
			}
		}
		Log.d("contact", "ContactManager::onDestroy() synchronized mContactList end");
	}

	/**
	 * 注册联系人状态更新回调
	 * 
	 * @param callback
	 */
	public void registerContactUpdate(OnContactUpdateCallback callback) {
		Log.d("contact", "ContactManager::registerContactUpdate() synchronized mCallbackList begin");
		synchronized (mCallbackList) {
			if (mCallbackList != null) {
				mCallbackList.add(callback);
			}
		}
		Log.d("contact", "ContactManager::registerContactUpdate() synchronized mCallbackList end");
	}

	/**
	 * 注销联系人状态更新回调
	 * 
	 * @param callback
	 */
	public void unregisterContactUpdata(OnContactUpdateCallback callback) {
		Log.d("contact", "ContactManager::unregisterContactUpdata() synchronized mCallbackList begin");
		synchronized (mCallbackList) {
			if (callback != null) {
				if (mCallbackList.contains(callback)) {
					mCallbackList.remove(callback);
				}
			}
		}
		Log.d("contact", "ContactManager::unregisterContactUpdata() synchronized mCallbackList end");
	}

	/**
	 * 联系人更新回调
	 */
	private void onContactListUpdateCallback() {
		Log.d("contact", "ContactManager::onContactListUpdateCallback() synchronized mCallbackList begin");
		synchronized (mCallbackList) {
			for (OnContactUpdateCallback callback : mCallbackList) {
				callback.onContactUpdate(mContactList);
			}
		}
		Log.d("contact", "ContactManager::onContactListUpdateCallback() synchronized mCallbackList end");
	}

	/**
	 * 注册新邀请消息更新监听
	 * 
	 * @param callback
	 */
	public void registerInviteUpdate(OnNewInviteUpdateCallback callback) {
		Log.d("contact", "ContactManager::registerInviteUpdate() synchronized mInviteCallbackList begin");
		synchronized (mInviteCallbackList) {
			if (mInviteCallbackList != null) {
				mInviteCallbackList.add(callback);
			}
		}
		Log.d("contact", "ContactManager::registerInviteUpdate() synchronized mInviteCallbackList end");
	}

	/**
	 * 注销新邀请消息更新监听
	 * 
	 * @param callback
	 */
	public void unregisterInviteUpdata(OnNewInviteUpdateCallback callback) {
		Log.d("contact", "ContactManager::unregisterInviteUpdata() synchronized mInviteCallbackList begin");
		synchronized (mInviteCallbackList) {
			if (mInviteCallbackList != null) {
				if (mInviteCallbackList.contains(callback)) {
					mInviteCallbackList.remove(callback);
				}
			}
		}
		Log.d("contact", "ContactManager::unregisterInviteUpdata() synchronized mInviteCallbackList end");
	}

	/**
	 * 来新邀请，通知界面
	 */
	private void onNewInviteUpdateCallback() {
		Log.d("contact", "ContactManager::onNewInviteUpdateCallback() synchronized mInviteCallbackList begin");
		synchronized (mInviteCallbackList) {
			for (OnNewInviteUpdateCallback callback : mInviteCallbackList) {
				callback.onNewInviteUpdate();
			}
		}
		Log.d("contact", "ContactManager::onNewInviteUpdateCallback() synchronized mInviteCallbackList end");
	}

	/**
	 * 获取联系人列表
	 * 
	 * @return
	 */
	public List<ContactBean> getContactList() {
		Log.d("contact", "ContactManager::getContactList() synchronized mContactList begin");
		List<ContactBean> clone = null;
		synchronized (mContactList) {
			/*返回浅拷贝列表，防止列表数目变化异常*/
			clone = new ArrayList<ContactBean>(mContactList);
		}
		Log.d("contact", "ContactManager::getContactList() synchronized mContactList end");
		
		return clone;
	}

	/**
	 * 根据Id索引获取指定联系人
	 * 
	 * @param womanId
	 * @return
	 */
	public ContactBean getContactById(String womanId) {
		if (mContactsMap != null) {
			if (mContactsMap.containsKey(womanId)) {
				return mContactsMap.get(womanId);
			}
		}
		return null;
	}

	/**
	 * 点击进入聊天界面清除当前正在聊天的女士的未读条数显示
	 * 
	 * @param womanid
	 */
	public void clearContactUnreadCount(String womanid) {
		Log.d("contact", "ContactManager::clearContactUnreadCount() synchronized mContactList begin");
		synchronized (mContactList) {
			if (mContactsMap.containsKey(womanid)) {
				mContactsMap.get(womanid).unreadCount = 0;
				/* 需要刷新列表状态显示 */
				onContactListUpdateCallback();
			}
		}
		Log.d("contact", "ContactManager::clearContactUnreadCount() synchronized mContactList end");
	}

	/**
	 * 获取当前联系人未读消息条数
	 * 
	 * @return
	 */
	public int getAllUnreadCount() {
		int unreadCount = 0;
		Log.d("contact", "ContactManager::getAllUnreadCount() synchronized mContactList begin");
		synchronized (mContactList) {
			for (ContactBean bean : mContactList) {
				unreadCount += bean.unreadCount;
			}
		}
		Log.d("contact", "ContactManager::getAllUnreadCount() synchronized mContactList end");
		return unreadCount;
	}

	/**
	 * 判断是否是联系人
	 * 
	 * @param womanId
	 * @return
	 */
	public boolean isMyContact(String womanId) {
		boolean isContact = false;
		Log.d("contact", "ContactManager::isMyContact() synchronized mContactList begin");
		synchronized (mContactList) {
			isContact = mContactsMap.containsKey(womanId);
		}
		Log.d("contact", "ContactManager::isMyContact() synchronized mContactList end");
		return isContact;
	}

	/**
	 * 获取联系人列表
	 * 
	 * @param callback
	 */
	public void getContacts(final OnGetContactListCallBack callback) {
		long currTime = System.currentTimeMillis();
		if ((currTime - mListLastUpdate <= CONTACTLIST_MIN_INTERVAL)) {
			// 时间过短，直接从缓存取
			if (callback != null) {
				callback.onContactListCallback(true, "", "");
			}
		} else {
			RequestOperator.getInstance().RecentContact(
					new OnLadyRecentContactListCallback() {

						@Override
						public void OnLadyRecentContactList(boolean isSuccess,
								String errno, String errmsg,
								LadyRecentContactItem[] listArray) {
							// TODO Auto-generated method stub
							if (isSuccess) {
								mListLastUpdate = System.currentTimeMillis();
								Log.d("contact", "ContactManager::getContacts() synchronized mContactList begin");
								synchronized (mContactList) {
									mContactList.clear();
									mContactsMap.clear();
									if (listArray != null) {
										for (int i = 0; i < listArray.length; i++) {
											ContactBean item = new ContactBean(
													listArray[i]);
											addOrUpdateContact(item);
										}
										/* 把本地联系人加入列表 */
										List<ContactBean> localConList = mLocalContactMap
												.get(getLocalContactKey());
										if (localConList != null) {
											for (ContactBean bean : localConList) {
												addOrUpdateContact(bean);
											}
										}
									}
								}
								Log.d("contact", "ContactManager::getContacts() synchronized mContactList end");
								/* 联系人下载成功开始更新在线状态 */
								handler.removeCallbacks(statusUpdate);
								handler.post(statusUpdate);
								onContactListUpdateCallback();
								if (callback != null) {
									callback.onContactListCallback(isSuccess,
											errno, errmsg);
								}
							} else {
								if (callback != null) {
									callback.onContactListCallback(isSuccess,
											errno, errmsg);
								}
							}
						}
					});
		}
	}

	private Runnable statusUpdate = new Runnable() {

		@Override
		public void run() {
			synchronized (mContactList) {
				String[] userIds = new String[mContactList.size()];
				for (int i = 0; i < mContactList.size(); i++) {
					userIds[i] = mContactList.get(i).womanid;
				}
				if(mLiveChatManager.IsLogin()){
					/*登陆成功才调用Peter获取状态接口*/
					mLiveChatManager.GetUserStatus(userIds);
				}
				handler.postDelayed(statusUpdate, CONTACTLIST_STATUS_INTERVAL);
			}

		}
	};

	/**
	 * 添加或更新联系人列表
	 * 
	 * @param contactBean
	 */
	private void addOrUpdateContact(ContactBean contactBean) {
		if (!mContactsMap.containsKey(contactBean.womanid)) {
			mContactList.add(contactBean);
			mContactsMap.put(contactBean.womanid, contactBean);
		}
	}

	/**
	 * ========================搜索相关==========================
	 */

	/**
	 * 根据标签类型获取指定标签的联系人列表
	 * 
	 * @param type
	 * @return
	 */
	public List<ContactBean> getContactsByType(LabelType type) {
		List<ContactBean> tempList = new ArrayList<ContactBean>();
		switch (type) {
		case ONLINE_ONLY:
			tempList.addAll(getOnlieOnly());
			break;
		case OFFLINE_ONLY:
			tempList.addAll(getOfflineOnly());
			break;
		case MY_FAVORITES:
			tempList.addAll(getFavoritesList());
			break;
		case WITH_VIDEOS:
			tempList.addAll(getWithVideoList());
			break;
		default:
			break;
		}
		return tempList;
	}

	/**
	 * 获取在线联系人列表
	 * 
	 * @return
	 */
	private List<ContactBean> getOnlieOnly() {
		List<ContactBean> tempList = new ArrayList<ContactBean>();
		Log.d("contact", "ContactManager::getOnlieOnly() synchronized mContactList begin");
		synchronized (mContactList) {
			if (mContactList != null) {
				for (ContactBean bean : mContactList) {
					if (bean.isOnline) {
						tempList.add(bean);
					}
				}
			}
		}
		Log.d("contact", "ContactManager::getOnlieOnly() synchronized mContactList end");
		return tempList;
	}

	/**
	 * 获取所有不在线联系人列表
	 * 
	 * @return
	 */
	private List<ContactBean> getOfflineOnly() {
		List<ContactBean> tempList = new ArrayList<ContactBean>();
		Log.d("contact", "ContactManager::getOfflineOnly() synchronized mContactList begin");
		synchronized (mContactList) {
			if (mContactList != null) {
				for (ContactBean bean : mContactList) {
					if (!bean.isOnline) {
						tempList.add(bean);
					}
				}
			}
		}
		Log.d("contact", "ContactManager::getOfflineOnly() synchronized mContactList end");
		return tempList;
	}

	/**
	 * 获取收藏联系人列表
	 * 
	 * @return
	 */
	private List<ContactBean> getFavoritesList() {
		List<ContactBean> tempList = new ArrayList<ContactBean>();
		Log.d("contact", "ContactManager::getFavoritesList() synchronized mContactList begin");
		synchronized (mContactList) {
			if (mContactList != null) {
				for (ContactBean bean : mContactList) {
					if (bean.isfavorite) {
						tempList.add(bean);
					}
				}
			}
		}
		Log.d("contact", "ContactManager::getFavoritesList() synchronized mContactList end");
		return tempList;
	}

	/**
	 * 获取有Video的联系人列表
	 * 
	 * @return
	 */
	private List<ContactBean> getWithVideoList() {
		List<ContactBean> tempList = new ArrayList<ContactBean>();
		Log.d("contact", "ContactManager::getWithVideoList() synchronized mContactList begin");
		synchronized (mContactList) {
			if (mContactList != null) {
				for (ContactBean bean : mContactList) {
					if (bean.videoCount > 0) {
						tempList.add(bean);
					}
				}
			}
		}
		Log.d("contact", "ContactManager::getWithVideoList() synchronized mContactList end");
		return tempList;
	}

	/**
	 * 模糊查找联系人列表（ID或用户名）
	 * 
	 * @param key
	 * @return
	 */
	public List<ContactBean> getContactsByIdOrName(String key) {
		key = key.toUpperCase(Locale.ENGLISH);
		List<ContactBean> tempList = new ArrayList<ContactBean>();
		Pattern p = Pattern.compile("^(.*" + key + ".*)$");
		Log.d("contact", "ContactManager::getContactsByIdOrName() synchronized mContactList begin");
		synchronized (mContactList) {
			for (ContactBean bean : mContactList) {
				if ((p.matcher(bean.womanid.toUpperCase(Locale.ENGLISH)).find())
						|| (p.matcher(bean.firstname.toUpperCase(Locale.ENGLISH)).find())) {
					tempList.add(bean);
				}
			}
		}
		Log.d("contact", "ContactManager::getContactsByIdOrName() synchronized mContactList end");
		return tempList;
	}
	
	/**
	 * 删除指定联系人
	 * @param userId
	 */
	public void deleteContactByUserId(String[] userId){
		synchronized (mContactList) {
			for(int i=0; i<userId.length; i++){
				if(mContactsMap.containsKey(userId[i])){
					ContactBean bean = mContactsMap.get(userId[i]);
					mContactList.remove(bean);
					mContactsMap.remove(userId[i]);
				}
			}
			/*通知界面更新*/
			onContactListUpdateCallback();
		}
	}

	/**
	 * ========================= 更新处理 =========================
	 */
	
	/**
	 * 更新联系人收藏状态（取消收藏触发）
	 * @param womanId
	 * @param isFavorite
	 */
	public void updateFavoriteStatus(String womanId, boolean isFavorite){
		Log.d("contact", "ContactManager::updateFavoriteStatus() synchronized mContactList begin");
		synchronized (mContactList){
			if(mContactsMap.containsKey(womanId)){
				mContactsMap.get(womanId).isfavorite = isFavorite;
				onContactListUpdateCallback();
			}
		}
		Log.d("contact", "ContactManager::updateFavoriteStatus() synchronized mContactList end");
	}
	
	/**
	 * 发送消息，添加到联系人列表或者更新消息状态，即列表显示及排序
	 * @param msgItem
	 */
	public void addOrUpdateContactBySendMsg(LCMessageItem msgItem){
		updateOrAddContact(msgItem.getUserItem(), true);
		onContactListUpdateCallback();
	}
	
	/**
	 * Lovecall confirm 及 make call 时添加到现有联系人
	 * @param bean
	 */
	public void addOrUpdateContact(LoveCallBean lovecallItem){

		ContactBean bean = null;
		Log.d("contact", "ContactManager::addOrUpdateContact() synchronized mContactList begin");
		synchronized (mContactList) {
			if (mContactsMap.containsKey(lovecallItem.womanid)) {
				bean = mContactsMap.get(lovecallItem.womanid);
			} else {
				bean = new ContactBean();
				bean.womanid = lovecallItem.womanid;
			}
			bean.firstname = lovecallItem.firstname;
			bean.age = lovecallItem.age;
			bean.photoURL = lovecallItem.image;
			bean.lasttime = (int) (System.currentTimeMillis() / 1000);
			if (!mContactsMap.containsKey(lovecallItem.womanid)) {
				mContactList.add(bean);
				mContactsMap.put(lovecallItem.womanid, bean);
			}
		}
		Log.d("contact", "ContactManager::addOrUpdateContact() synchronized mContactList end");
		onContactListUpdateCallback();
	
	}

	/**
	 * 发送邮件成功，添加到现有联系人列表
	 * 
	 * @param lady
	 * @param localFlag 是否添加到本地存储（男士邀请联系人需要，防止刷新后聊天对象找不到）
	 */
	public void updateBySendEMF(LadyDetail lady) {
		ContactBean bean = null;
		Log.d("contact", "ContactManager::updateBySendEMF() synchronized mContactList begin");
		synchronized (mContactList) {
			if (mContactsMap.containsKey(lady.womanid)) {
				bean = mContactsMap.get(lady.womanid);
			} else {
				bean = new ContactBean();
				bean.womanid = lady.womanid;
			}
			bean.firstname = lady.firstname;
			bean.age = lady.age;
			bean.photoURL = lady.photoMinURL;
			bean.isfavorite = lady.isfavorite;
			bean.isOnline = lady.isonline;
			if (lady.videoList != null) {
				bean.videoCount = lady.videoList.size();
			}
			bean.lasttime = (int) (System.currentTimeMillis() / 1000);
			if (!mContactsMap.containsKey(lady.womanid)) {
				mContactList.add(bean);
				mContactsMap.put(lady.womanid, bean);
			}
		}
		Log.d("contact", "ContactManager::updateBySendEMF() synchronized mContactList end");
		onContactListUpdateCallback();
	}
	
	/**
	 * 收到消息，不在好友列表，根据女士详情获取信息后，添加到联系人
	 * @param userItem
	 * @param isInvite 是否男士主动邀请，需保存到本地联系人
	 */
	public void updateByMessage(final LCUserItem userItem, final boolean isInvite){

		/* 在聊或者试聊时，添加到联系人列表,没有女士头像及firstname，需获取后添加 */
		LadyDetailManager.getInstance().QueryLadyDetail(userItem.userId, new OnLadyDetailManagerQueryLadyDetailCallback() {
			
			@Override
			public void OnQueryLadyDetailCallback(boolean isSuccess, String errno,
					String errmsg, LadyDetail item) {
				// TODO Auto-generated method stub
				if(isSuccess){
					ContactBean bean = new ContactBean();
					bean.womanid = userItem.userId;
					bean.firstname = item.firstname;
					bean.age = item.age;
					bean.photoURL = item.photoMinURL;
					bean.isfavorite = item.isfavorite;
					bean.isOnline = item.isonline;
					if (item.videoList != null) {
						bean.videoCount = item.videoList.size();
					}
					bean.isInchating = true;
					if (userItem.getMsgList().size() > 0) {
						/* 生成联系人列表最后一条提示消息 */
						for (int i = userItem.getMsgList().size() - 1; i >= 0; i--) {
							LCMessageItem msgItem = userItem.getMsgList().get(i);
							if (msgItem.msgType == MessageType.Text
									|| msgItem.msgType == MessageType.Emotion
									|| msgItem.msgType == MessageType.Photo
									|| msgItem.msgType == MessageType.Voice
									|| msgItem.msgType == MessageType.Video) {
								/* 通过最后一条正常通讯消息生成提示，否则界面使用默认最后更新时间提示 */
								if (msgItem.createTime > bean.lasttime) {
									bean.lasttime = msgItem.createTime;
									bean.msgHint = generateMsgHint(msgItem);
								}
								break;
							}
						}
					} else {
						bean.lasttime = (int) (System.currentTimeMillis() / 1000);
						Log.w(tag, "Something wrong womanid: "
								+ userItem.userId);
					}
					/*添加到现有联系人*/
					synchronized (mContactList) {
						/*防止异步回调多次导致添加联系人重复*/
						if(!mContactsMap.containsKey(bean.womanid)){
							mContactList.add(bean);
							mContactsMap.put(bean.womanid, bean);
							if(isInvite){
								String localKey = getLocalContactKey();
								if (mLocalContactMap.containsKey(localKey)) {
									mLocalContactMap.get(localKey).add(bean);
								} else {
									List<ContactBean> localList = new ArrayList<ContactBean>();
									localList.add(bean);
									mLocalContactMap.put(localKey, localList);
								}
							}
						}
					}
				}
			}
		});
	}
	

	/**
	 * livechat聊天信息与列表提示转换
	 * 
	 * @param msgItem
	 * @return
	 */
	public String generateMsgHint(LCMessageItem msgItem) {
		String msg = "";
		/**
		 * <string-array name="livechat_msg_in_type"> <item
		 * name="received_system_message">You have a system messge</item> <item
		 * name="send_a_photo">You sent a photo</item> <item
		 * name="send_a_premium_sticker">You sent a premium sticker</item> <item
		 * name="send_a_voice_message">You sent a voice message</item> <item
		 * name="receive_a_photo">%s sent a photo</item> <item
		 * name="receive_a_premium_sticker">$s sent a premium sticker</item>
		 * <item name="receive_a_voice_message">%s sent a voice message</item>
		 * </string-array>
		 */
		String[] msgs_ = QpidApplication.getContext().getResources()
				.getStringArray(R.array.livechat_msg_in_type);
		switch (msgItem.msgType) {
		case Text:
			msg = (msgItem.getTextItem().message != null) ? msgItem
					.getTextItem().message : "";
			break;
		case Warning: // 警告消息
			msg = msgs_[0];
			break;
		case Emotion: // 高级表情
			if (msgItem.sendType == SendType.Send) {
				msg = msgs_[2];
			} else {
				msg = String.format(msgs_[5], msgItem.getUserItem().userName);
			}
			break;
		case Voice: // 语音
			if (msgItem.sendType == SendType.Send) {
				msg = msgs_[3];
			} else {
				msg = String.format(msgs_[6], msgItem.getUserItem().userName);
			}
			break;
		case Photo: // 私密照
			if (msgItem.sendType == SendType.Send) {
				msg = msgs_[1];
			} else {
				msg = String.format(msgs_[4], msgItem.getUserItem().userName);
			}
			break;
		case Video:
			if (msgItem.sendType == SendType.Send) {
				msg = msgs_[7];
			} else {
				msg = String.format(msgs_[8], msgItem.getUserItem().userName);
			}
			break;
		case System: // 系统消息
		case Custom: // 自定义消息
		case Unknow:
		default:
			// do nothing

		}

		return msg;
	}

	/**
	 * EMF 推送， onlinelady makecall， inchat 男士发起试聊（只有女士Id，无其他需先获取女士详情，成功再添加）
	 * 
	 * @param userId
	 */
	public void updateOrAddContact(String userId) {
		Message msg = Message.obtain();
		msg.what = RequestFlag.REQUEST_LIVECHAT_EMF_CONATCT_UPDATE.ordinal();
		msg.obj = userId;
		mHandler.sendMessage(msg);
	}

	/**
	 * LiveChat 返回列表更新联系人（online， inchat， msgHint， startTime）
	 * 
	 * @param userItems
	 */
	private void updateOrAddContact(ArrayList<LCUserItem> userItemList) {
		for (LCUserItem item : userItemList) {
			updateOrAddContact(item, false);
		}
	}

	/**
	 * LiveChat 返回列表更新联系人（online， inchat， msgHint， startTime）
	 * 
	 * @param userItems
	 */
	private void updateOrAddContact(LCUserItem[] userItems) {
		if (userItems != null) {
			for (int i = 0; i < userItems.length; i++) {
				updateOrAddContact(userItems[i], false);
			}
		}
	}

	/**
	 * 
	 * @param userItem
	 * @param isInvite 处理由男士端发起的邀请，添加到联系人
	 */
	private void updateOrAddContact(final LCUserItem userItem, final boolean isInvite) {
		ContactBean bean = null;
		if (userItem != null) {
			Log.d("contact", "ContactManager::updateOrAddContact() synchronized mContactList begin");
			synchronized (mContactList) {
				if (mContactsMap.containsKey(userItem.userId)) {
					/* 在联系人列表，更新联系人信息 */
					bean = mContactsMap.get(userItem.userId);
					bean.isOnline = isOnline(userItem.statusType);
					if ((userItem.chatType == ChatType.InChatCharge)
							|| (userItem.chatType == ChatType.InChatUseTryTicket)) {
						bean.isInchating = true;
					} else {
						bean.isInchating = false;
					}
					if (userItem.getMsgList().size() > 0) {
						for (int i = userItem.getMsgList().size() - 1; i >= 0; i--) {
							LCMessageItem item = userItem.getMsgList().get(i);
							if (item.msgType == MessageType.Text
									|| item.msgType == MessageType.Emotion
									|| item.msgType == MessageType.Photo
									|| item.msgType == MessageType.Voice
									|| item.msgType == MessageType.Video) {
								/* 通过最后一条正常通讯消息生成提示，否则界面使用默认最后更新时间提示 */
								if (item.createTime > bean.lasttime) {
									bean.lasttime = item.createTime;
									bean.msgHint = generateMsgHint(item);
								}
								break;
							}
						}
					}
					if (StringUtil.isEmpty(bean.msgHint)) {
						/* 无聊天信息 */
						if (bean.lasttime <= 0) {
							bean.lasttime = (int) (System.currentTimeMillis() / 1000);
						}
					}
				} else if ((userItem.chatType == ChatType.InChatCharge)
						|| (userItem.chatType == ChatType.InChatUseTryTicket)||
						isInvite) {
					Message msg = Message.obtain();
					msg.what = RequestFlag.REQUEST_LIVECHAT_MESSAGE_CONATCT_UPDATE.ordinal();
					msg.arg1 = isInvite?1:0;
					msg.obj = userItem;
					mHandler.sendMessage(msg);
				}
			}
			Log.d("contact", "ContactManager::updateOrAddContact() synchronized mContactList end");
		}
	}

	/**
	 * 根据女士状态判断是否在线
	 * 
	 * @param status
	 * @return
	 */
	private boolean isOnline(UserStatusType status) {
		boolean isOnline = false;
		if (status == UserStatusType.USTATUS_ONLINE) {
			isOnline = true;
		} else {
			isOnline = false;
		}
		return isOnline;
	}
	
	/**
	 * 获取本地联系人存储key （做分站区分及不同用户区分）
	 * @return
	 */
	private String getLocalContactKey(){
		String localKey = "";
		int siteId = WebSiteManager.getInstance().GetWebSite().getSiteId();
		LoginParam loginParam = LoginPerfence.GetLoginParam(mContext);
		if(loginParam != null && loginParam.item != null){
			localKey = loginParam.item.manid + "_" +siteId;
		}else{
			localKey = "_" + siteId;
		}
		return localKey;
	}

	/**
	 * ================================= Live chat 回调单线程处理，解决ContactList 同步问题
	 * =================================
	 */
	/**
	 * 收到聊天信息（文字，语音，图片，高级表情），更新联系人状态
	 * 
	 * @param item
	 */
	private void onReceiveMessage(final LCMessageItem item) {
		Runnable task = new Runnable() {

			@Override
			public void run() {
				// Add by Max for show notification
				if ( item.getUserItem().chatType == ChatType.InChatCharge
						|| item.getUserItem().chatType == ChatType.InChatUseTryTicket ) {
					// 消息为inchat用户发出
					if( item.getUserItem().userId.compareTo(mWomanId) != 0 ) {
						// 非当前在聊女士
						Message msg = Message.obtain();
						RequestBaseResponse obj = new RequestBaseResponse();
						obj.body = item;
						msg.obj = obj;
						msg.what = RequestFlag.REQUEST_LIVECHAT_NOTIFICATION_SUCCESS.ordinal();
						mHandler.sendMessage(msg);
					}
				}
				
				if(isNeedUpdateContactList(item)){
					updateOrAddContact(item.getUserItem(), false);
					Log.d("contact", "ContactManager::onReceiveMessage() synchronized mContactList begin");
					synchronized (mContactList) {
						if (mWomanId != null) {
							if (!item.fromId.equals(mWomanId)) {
								/* 不是当前正在聊天的对象且室联系人发来消息，通知未读条数 */
								if (mContactsMap.containsKey(item.fromId)) {
									mContactsMap.get(item.fromId).unreadCount++;
								}
							}
						}
					}
					Log.d("contact", "ContactManager::onReceiveMessage() synchronized mContactList end");
					onContactListUpdateCallback();
				}
				
				if(item.getUserItem().chatType == ChatType.Invite){
					/*通知邀请更新*/
					onNewInviteUpdateCallback();
				}
			}
		};
		handler.post(task);
	}

	/**
	 * 收到消息是否需要更新联系人列表状态
	 * 
	 * @param item
	 * @return
	 */
	private boolean isNeedUpdateContactList(LCMessageItem item) {
		boolean isUpdate = true;
		Log.d("contact", "ContactManager::isNeedUpdateContactList() synchronized mContactList begin");
		synchronized (mContactList) {
			if (!mContactsMap.containsKey(item.fromId)
					&& (item.getUserItem().chatType == ChatType.Invite)) {
				/*不是我的联系人，且状态为邀请*/
				isUpdate = false;
			}
		}
		Log.d("contact", "ContactManager::isNeedUpdateContactList() synchronized mContactList end");
		return isUpdate;
	}

	/**
	 * 新邮件通知，更新联系人状态
	 * 
	 * @param fromId
	 * @param noticeType
	 */
	private void onReceiveEmfUpdate(final String fromId,
			TalkEmfNoticeType noticeType) {
		if (noticeType == TalkEmfNoticeType.EMF) {
			/* 此处需要添加到联系人 */
			Runnable task = new Runnable() {

				@Override
				public void run() {
					updateOrAddContact(fromId);
				}
			};
			handler.post(task);
		}
	}

	/**
	 * 女士状态更新，更新联系人状态
	 * 
	 * @param userItem
	 */
	private void onReceiveUpdateStatus(final LCUserItem userItem) {
		Runnable task = new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				if (userItem != null) {
					updateOrAddContact(userItem, false);
					onContactListUpdateCallback();
				}
			}
		};
		handler.post(task);
	}

	/**
	 * 和女士聊天信息历史列表更新，更新联系人状态
	 * 
	 * @param userItems
	 */
	private void onReceiveHistoryMessages(final LCUserItem[] userItems) {
		Runnable task = new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				updateOrAddContact(userItems);
				onContactListUpdateCallback();
			}
		};
		handler.post(task);
	}

	/**
	 * 指定女士聊天信息历史列表更新，更新联系人状态
	 * 
	 * @param userItems
	 */
	private void onReceiveHistoryMessage(final LCUserItem userItem) {
		/* 获取在聊最近聊天列表，需更新联系人 */
		Runnable task = new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				updateOrAddContact(userItem, false);
				onContactListUpdateCallback();
			}
		};
		handler.post(task);

	}

	/**
	 * 在聊列表更新，更新联系人列表
	 */
	private void onReceiveTalkList() {
		Runnable task = new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				updateOrAddContact(mLiveChatManager.GetChatingUsers());
				onContactListUpdateCallback();
			}
		};
		handler.post(task);
	}

	/**
	 * 批量获取联系人状态通知，更新联系人状态
	 * 
	 * @param userStatusArray
	 */
	private void onReceiveUsersStatus(final LCUserItem[] userList) {
		/* 批量获取女士在线状态，更新联系人状态 */
		Runnable task = new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				updateOrAddContact(userList);
				onContactListUpdateCallback();
			}
		};
		handler.post(task);
	}
	
	/**
	 * 聊天状态改变时间接收，更新主界面右侧下方邀请列表状态（邀请转在聊等）
	 */
	private void onReceiveTalkEvent(LCUserItem userItem){
		Runnable task = new Runnable() {

			@Override
			public void run() {
				onNewInviteUpdateCallback();
			}
		};
		handler.post(task);
	}
	
	/**
	 * 注销时，通知界面列表清除，防止聊天界面等打开列表时异常，注销列表不清楚
	 */
	private void onLogoutDataUpdate(){
		Runnable task = new Runnable() {

			@Override
			public void run() {
				onContactListUpdateCallback();
			}
		};
		handler.post(task);
	}

	/**
	 * 监听livechat的来消息，邮件推送等
	 */
	private void addLiveChatListener() {
		mLiveChatManager
				.RegisterOtherListener(new LiveChatManagerOtherListener() {

					@Override
					public void OnUpdateStatus(LCUserItem userItem) {
						/* Live chat 女士状态更新推送 */
						onReceiveUpdateStatus(userItem);
					}
					
					@Override
					public void OnChangeOnlineStatus(LCUserItem userItem) {
						// TODO Auto-generated method stub
						
					}

					@Override
					public void OnSetStatus(LiveChatErrType errType,
							String errmsg) {
						// TODO Auto-generated method stub

					}

					@Override
					public void OnRecvKickOffline(KickOfflineType kickType) {
						/*Livechat 被踢处理*/
						Message msg = Message.obtain();
						msg.what = RequestFlag.REQUEST_LIVECHAT_KICK_OFF.ordinal();
						msg.arg1 = kickType.ordinal();
						mHandler.sendMessage(msg);
					}

					@Override
					public void OnRecvEMFNotice(String fromId,
							TalkEmfNoticeType noticeType) {
						onReceiveEmfUpdate(fromId, noticeType);
					}

					@Override
					public void OnGetUsersHistoryMessage(boolean success,
							String errno, String errmsg, LCUserItem[] userItems) {
						// TODO Auto-generated method stub
						onReceiveHistoryMessages(userItems);
					}

					@Override
					public void OnGetTalkList(LiveChatErrType errType,
							String errmsg) {
						// TODO Auto-generated method stub
						/* 获取在聊列表，更新联系人列表 */
						onReceiveTalkList();
					}

					@Override
					public void OnGetHistoryMessage(boolean success,
							String errno, String errmsg, LCUserItem userItem) {
						/* 获取指定用户聊天历史，更新联系人列表 */
						onReceiveHistoryMessage(userItem);
					}

					@Override
					public void OnLogin(LiveChatErrType errType, String errmsg,
							boolean isAutoLogin) {
						// TODO Auto-generated method stub
						/* 联系人下载成功开始更新在线状态 */
						handler.removeCallbacks(statusUpdate);
						handler.post(statusUpdate);
					}

					@Override
					public void OnLogout(LiveChatErrType errType,
							String errmsg, boolean isAutoLogin) {
						// TODO Auto-generated method stub

					}

					@Override
					public void OnGetUserStatus(LiveChatErrType errType,
							String errmsg, LCUserItem[] userList) {
						// TODO Auto-generated method stub
						onReceiveUsersStatus(userList);
					}
				});

		mLiveChatManager
				.RegisterEmotionListener(new LiveChatManagerEmotionListener() {

					@Override
					public void OnSendEmotion(LiveChatErrType errType,
							String errmsg, LCMessageItem item) {
						// TODO Auto-generated method stub

					}

					@Override
					public void OnRecvEmotion(LCMessageItem item) {
						// TODO Auto-generated method stub
						/* 收到高级表情，更新联系人列表 */
						onReceiveMessage(item);
					}

					@Override
					public void OnGetEmotionConfig(boolean success,
							String errno, String errmsg,
							OtherEmotionConfigItem item) {
						// TODO Auto-generated method stub

					}

					@Override
					public void OnGetEmotionImage(boolean success,
							LCEmotionItem emotionItem) {
						// TODO Auto-generated method stub

					}

					@Override
					public void OnGetEmotionPlayImage(boolean success,
							LCEmotionItem emotionItem) {
						// TODO Auto-generated method stub

					}

					@Override
					public void OnGetEmotion3gp(boolean success,
							LCEmotionItem emotionItem) {
						// TODO Auto-generated method stub

					}

				});

		mLiveChatManager
				.RegisterMessageListener(new LiveChatManagerMessageListener() {

					@Override
					public void OnSendMessage(LiveChatErrType errType,
							String errmsg, LCMessageItem item) {
						// TODO Auto-generated method stub

					}

					@Override
					public void OnRecvWarning(LCMessageItem item) {
						// TODO Auto-generated method stub

					}

					@Override
					public void OnRecvMessage(LCMessageItem item) {
						// TODO Auto-generated method stub
						/* 收到聊天信息，更新列表 */
						onReceiveMessage(item);
					}

					@Override
					public void OnRecvSystemMsg(LCMessageItem item) {
						// TODO Auto-generated method stub

					}

					@Override
					public void OnRecvEditMsg(String fromId) {
						// TODO Auto-generated method stub

					}

					@Override
					public void OnSendMessageListFail(LiveChatErrType errType,
							ArrayList<LCMessageItem> msgList) {
						// TODO Auto-generated method stub
						
					}
				});

		mLiveChatManager
				.RegisterPhotoListener(new LiveChatManagerPhotoListener() {

					@Override
					public void OnSendPhoto(LiveChatErrType errType,
							String errno, String errmsg, LCMessageItem item) {
						// TODO Auto-generated method stub

					}

					@Override
					public void OnRecvPhoto(LCMessageItem item) {
						// TODO Auto-generated method stub
						/* 获取照片类信息，更新联系人 */
						onReceiveMessage(item);
					}

					@Override
					public void OnPhotoFee(boolean success, String errno,
							String errmsg, LCMessageItem item) {
						// TODO Auto-generated method stub

					}

					@Override
					public void OnGetPhoto(LiveChatErrType errType,
							String errno, String errmsg, LCMessageItem item) {
						// TODO Auto-generated method stub

					}
				});
		
		mLiveChatManager.RegisterVideoListener(new LiveChatManagerVideoListener() {
			
			@Override
			public void OnVideoFee(boolean success, String errno, String errmsg,
					LCMessageItem item) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void OnRecvVideo(LCMessageItem item) {
				/* 获取小视频信息，更新联系人 */
				onReceiveMessage(item);
			}

			@Override
			public void OnGetVideoPhoto(LiveChatErrType errType, String errno,
					String errmsg, String userId, String inviteId,
					String videoId, VideoPhotoType type, String filePath,
					ArrayList<LCMessageItem> msgList) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void OnStartGetVideo(String userId,
					String videoId, String inviteId, String videoPath,
					ArrayList<LCMessageItem> msgList) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void OnGetVideo(LiveChatErrType errType, String userId,
					String videoId, String inviteId, String videoPath,
					ArrayList<LCMessageItem> msgList) {
				// TODO Auto-generated method stub
				
			}
		});

		mLiveChatManager
				.RegisterVoiceListener(new LiveChatManagerVoiceListener() {

					@Override
					public void OnSendVoice(LiveChatErrType errType,
							String errno, String errmsg, LCMessageItem item) {
						// TODO Auto-generated method stub

					}

					@Override
					public void OnRecvVoice(LCMessageItem item) {
						// TODO Auto-generated method stub
						/* 收取语音聊天信息 */
						onReceiveMessage(item);
					}

					@Override
					public void OnGetVoice(LiveChatErrType errType,
							String errmsg, LCMessageItem item) {
						// TODO Auto-generated method stub

					}
				});
		mLiveChatManager.RegisterTryTicketListener(new LiveChatManagerTryTicketListener() {
			
			@Override
			public void OnUseTryTicket(LiveChatErrType errType, String errno,
					String errmsg, String userId, TryTicketEventType eventType) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void OnRecvTryTalkEnd(LCUserItem userItem) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void OnRecvTryTalkBegin(LCUserItem userItem, int time) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void OnRecvTalkEvent(LCUserItem item) {
				if(item != null){
					onReceiveTalkEvent(item);
				}
			}
			
			@Override
			public void OnEndTalk(LiveChatErrType errType, String errmsg,
					LCUserItem userItem) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void OnCheckCoupon(boolean success, String errno, String errmsg,
					Coupon item) {
				// TODO Auto-generated method stub
				
			}
		});
	}

	/**
	 * 重置状态
	 */
	private void resetContact() {
		mListLastUpdate = 0;
		Log.d("contact", "ContactManager::resetContact() synchronized mContactList begin");
		synchronized (mContactList) {
			mContactList.clear();
			mContactsMap.clear();
			handler.removeCallbacks(statusUpdate);
		}
		Log.d("contact", "ContactManager::resetContact() synchronized mContactList end");
	}

	@Override
	public void OnLogin(boolean isSuccess, String errno, String errmsg,
			LoginItem item, LoginErrorItem errItem) {
		if (isSuccess) {
			resetContact();
			getContacts(null);
		}
	}

	@Override
	public void OnLogout(boolean bActive) {
		resetContact();
		onLogoutDataUpdate();
	}
}
