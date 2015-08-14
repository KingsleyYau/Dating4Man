package com.qpidnetwork.dating;

import java.util.ArrayList;

import javax.security.auth.callback.Callback;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.ImageView;

import com.facebook.internal.PlatformServiceClient;
import com.qpidnetwork.dating.authorization.LoginParam;
import com.qpidnetwork.dating.authorization.LoginPerfence;
import com.qpidnetwork.dating.authorization.RegisterActivity;
import com.qpidnetwork.dating.authorization.LoginManager.LoginStatus;
import com.qpidnetwork.dating.authorization.LoginParam.LoginType;
import com.qpidnetwork.dating.emf.EMFAttachmentUploader;
import com.qpidnetwork.dating.profile.MyProfileActivity;
import com.qpidnetwork.dating.quickmatch.QuickMatchActivity;
import com.qpidnetwork.framework.util.Log;
import com.qpidnetwork.livechat.LCMessageItem;
import com.qpidnetwork.livechat.LCUserItem;
import com.qpidnetwork.livechat.LCEmotionItem;
import com.qpidnetwork.livechat.LCUserItem.ChatType;
import com.qpidnetwork.livechat.LiveChatManager;
import com.qpidnetwork.livechat.LiveChatManagerEmotionListener;
import com.qpidnetwork.livechat.LiveChatManagerMessageListener;
import com.qpidnetwork.livechat.LiveChatManagerOtherListener;
import com.qpidnetwork.livechat.LiveChatManagerPhotoListener;
import com.qpidnetwork.livechat.LiveChatManagerTryTicketListener;
import com.qpidnetwork.livechat.LiveChatManagerVoiceListener;
import com.qpidnetwork.livechat.jni.LiveChatTalkListInfo;
import com.qpidnetwork.livechat.jni.LiveChatUserStatus;
import com.qpidnetwork.livechat.jni.LiveChatClient.UserStatusType;
import com.qpidnetwork.livechat.jni.LiveChatClientListener.KickOfflineType;
import com.qpidnetwork.livechat.jni.LiveChatClientListener.LiveChatErrType;
import com.qpidnetwork.livechat.jni.LiveChatClientListener.TalkEmfNoticeType;
import com.qpidnetwork.livechat.jni.LiveChatClientListener.TryTicketEventType;
import com.qpidnetwork.manager.FileCacheManager;
import com.qpidnetwork.manager.FileCacheManager.LadyFileType;
import com.qpidnetwork.manager.WebSiteManager;
import com.qpidnetwork.request.OnLoginCallback;
import com.qpidnetwork.request.OnOtherSynConfigCallback;
import com.qpidnetwork.request.OnQueryLadyListCallback;
import com.qpidnetwork.request.OnRequestCallback;
import com.qpidnetwork.request.OnTicketDetailCallback;
import com.qpidnetwork.request.OnTicketListCallback;
import com.qpidnetwork.request.RequestJni;
import com.qpidnetwork.request.RequestJniAuthorization;
import com.qpidnetwork.request.RequestJniLady;
import com.qpidnetwork.request.RequestJniLady.OnlineType;
import com.qpidnetwork.request.RequestJniLady.OrderType;
import com.qpidnetwork.request.RequestJniLady.SearchType;
import com.qpidnetwork.request.RequestJniLiveChat.PhotoSizeType;
import com.qpidnetwork.request.RequestJniOther;
import com.qpidnetwork.request.RequestJniTicket;
import com.qpidnetwork.request.item.Coupon;
import com.qpidnetwork.request.item.Coupon.CouponStatus;
import com.qpidnetwork.request.item.Lady;
import com.qpidnetwork.request.item.OtherEmotionConfigItem;
import com.qpidnetwork.request.item.OtherSynConfigItem;
import com.qpidnetwork.request.item.LoginItem;
import com.qpidnetwork.request.item.TicketDetailItem;
import com.qpidnetwork.request.item.TicketListItem;
import com.qpidnetwork.tool.ImageViewLoader;
import com.qpidnetwork.tool.ImageViewLoader.ImageViewLoaderCallback;

@SuppressLint("HandlerLeak")
public class MainActivity extends FragmentActivity {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
	
		RequestJni.SetWebSite("http://demo.chnlove.com", "http://demo-mobile.chnlove.com");
		RequestJni.SetPublicWebSite("http://58.64.141.117:9901");
//		RequestJni.SetWebSite("http://192.168.88.140:81", "http://192.168.88.140:81");
		RequestJni.SetAuthorization("test", "5179");
		
//		EMFAttachmentUploader emfAttachmentUploader = new EMFAttachmentUploader();
//		ProgressImageHorizontalView view = (ProgressImageHorizontalView) findViewById(R.id.progressImageHorizontalView);
//		String localPath = FileCacheManager.getInstance().CacheImagePathFromUrl("http://192.168.88.140/Share/u213.png");
//		emfAttachmentUploader.Upload(
//				final view,
//				UploadAttachType.IMAGE,
//				localPath,
//				localPath,
//				new EMFAttachmentUploaderCallback() {
//					
//					@Override
//					public void OnUploadFinish(boolean isSuccess, String errno, String errmsg,
//							String attachId) {
//						// TODO Auto-generated method stub
//						view 
//						
//					}
//				});
		
//		uploader.Upload(view, UploadAttachType.
		
//		btnFacebook = (Button)this.findViewById(R.id.facebook);
//		btnFacebook.setOnClickListener(new OnClickListener() {
//			@Override
//			public void onClick(View v) {
//			}
//		});
		
//		RequestJniAuthorization.Register("test123458@gmail.com", "123456", "firstname", "lastname", 
//				Country.Albania, "1960", "1", "1", true, "Android", "123456", 
//				"samsung", new OnRegisterCallback() {
//					
//				@Override
//				public void OnRegister(boolean isSuccess, String errno, String errmsg,
//						RegisterItem item) {
//					// TODO Auto-generated method stub
//					Log.d("max.MainActivity", "OnRegister( isSuccess : " + String.valueOf(isSuccess) + ", errno : " +
//							errno + ", errmsg : " + errmsg + " )");
//					if( item != null ) {
//						
//					}
//				}
//			});

//		RequestJniAuthorization.Login("CM28171208", "123456", "000000000000000", "1", "Android",
////		RequestJniAuthorization.Login("samson.fan@qpidnetwork.com", "123456", "000000000000000", "1", "Android",
//				"samsung", new OnLoginCallback() {
//					
//					@Override
//					public void OnLogin(boolean isSuccess, String errno, String errmsg,
//							LoginItem item) {
//						// TODO Auto-generated method stub
//						Log.d("max.MainActivity", "OnLogin( isSuccess : " + String.valueOf(isSuccess) + ", errno : " +
//								errno + ", errmsg : " + errmsg + " )");
//						if( item != null ) {
//							Log.d("max.MainActivity", "OnLogin( manid : " + item.manid + "," 
//									+ " email: " + item.email + "," 
//									+ " firstname: " + item.firstname + ","
//									+ " lastname: " + item.lastname + ","
//									+ " sessionid: " + item.sessionid + ","
//									+ " photoURL: " + item.photoURL + ","
//									+ " reg_step: " + item.reg_step + ","
//									+ " country: " + item.country.ordinal() + ","
//									+ " telephone: " + item.telephone + ","
//									+ " telephone_verify: " + item.telephone_verify + ","
//									+ ")");
//						}
//					}
//				});
		
//		RequestJniAuthorization.GetSms("13751872204", Country.China, "1234567890", new OnRequestCallback() {
//			
//			@Override
//			public void OnRequest(boolean isSuccess, String errno, String errmsg,
//					String tips) {
//				// TODO Auto-generated method stub
//				
//			}
//		});
		
//		RequestJniAuthorization.VerifySms("6148", Verify.NoNeed, new OnRequestCallback() {
//			
//			@Override
//			public void OnRequest(boolean isSuccess, String errno, String errmsg,
//					String tips) {
//				// TODO Auto-generated method stub
//				
//			}
//		});
		
//		RequestJniLady.QueryLadyList(0, 30, SearchType.DEFAULT, "", true, -1, -1, -1, -1, new OnQueryLadyListCallback() {
//		
//		@Override
//		public void OnQueryLadyList(boolean isSuccess, String errno, String errmsg,
//				Lady[] ladyList) {
//			// TODO Auto-generated method stub
//			Log.d("max.MainActivity", "OnQueryLadyList( isSuccess : " + String.valueOf(isSuccess) + ", errno : " +
//					errno + ", errmsg : " + errmsg + " )");
//			if( ladyList != null ) {
//				Log.d("max.MainActivity", "OnQueryLadyList( ladyList.length : " + ladyList.length + " )");
//			}
//		}
//	});
		
		
//		testEMFRequest();
//		testVideoShowRequest();
//		testOther();
//		testLady();
//		testAdvert();
//		testLiveChat();
//		testLiveChatManager();
//		testImageView();
//		testRequest();
		testTicket();
	}
	
	protected void testEMFRequest() {
		// Samson test OK 2015-04-21 
//		RequestJniEMF.InboxList(0, 15, SortType.READ, "", new OnEMFInboxListCallback() {
//			@Override
//			public void OnEMFInboxList(boolean isSuccess, String errno, String errmsg,
//					int pageIndex, int pageSize, int dataCount, EMFInboxListItem[] listArray) {
//				// TODO Auto-generated method stub
//				Log.d("MainActivity.EMF", "OnEMFInboxList( isSuccess : " + String.valueOf(isSuccess) + "" 
//						+ ", errno : " + errno 
//						+ ", errmsg : " + errmsg 
//						+ ", pageIndex:" + String.valueOf(pageIndex) 
//						+ ", pageSize:" + String.valueOf(pageSize)
//						+ ", dataCount:" + String.valueOf(dataCount)
//						+ ", listArray.length:" + (null == listArray ? "0" : String.valueOf(listArray.length))
//						+ " )");
//			}
//		});
		
		// Samson test OK 2015-04-21 
//		RequestJniEMF.InboxMsg("19361975", new OnEMFInboxMsgCallback() {
//			
//			@Override
//			public void OnEMFInboxMsg(boolean isSuccess, String errno, String errmsg,
//					EMFInboxMsgItem item) {
//				// TODO Auto-generated method stub
//				Log.d("MainActivity.EMF", "OnEMFInboxMsg( isSuccess : " + String.valueOf(isSuccess) + "" 
//						+ ", errno : " + errno 
//						+ ", errmsg : " + errmsg 
//						+ " )");
//			}
//		});
		
		// Samson test OK 2015-04-21 
//		String[] gifts = {"V0008", "V0009"};
//		String[] attachs = {"1001", "1002"};
//		RequestJniEMF.SendMsg("P844666", "test 2015-03-11 15:34", false, gifts, attachs, new OnEMFSendMsgCallback() {
//			
//			@Override
//			public void OnEMFSendMsg(boolean isSuccess, String errno, String errmsg,
//					EMFSendMsgItem item, EMFSendMsgErrorItem errItem) 
//			{
//				// TODO Auto-generated method stub
//				Log.d("MainActivity.EMF", "OnEMFSendMsg( isSuccess : " + String.valueOf(isSuccess) + "" 
//						+ ", errno : " + errno 
//						+ ", errmsg : " + errmsg
//						+ ", item.id:" + item.id
//						+ ", item.sendTime:" + String.valueOf(item.sendTime)
//						+ ", errItem.money:" + errItem.money 
//						+ " )");
//			}
//		});
		
		// Samson test OK 2015-03-13
//		String sdPath = Environment.getExternalStorageDirectory().getAbsolutePath();
//		sdPath += "/DCIM/Camera/1425086298398.jpg";
//		String[] fileName = {sdPath};
//		RequestJniEMF.UploadImage("19371289", fileName, new OnEMFUploadImageCallback() {
//			
//			@Override
//			public void OnEMFUploadImage(boolean isSuccess, String errno, String errmsg) {
//				// TODO Auto-generated method stub
//				Log.d("MainActivity.EMF", "OnEMFUploadImage( isSuccess : " + String.valueOf(isSuccess) + "" 
//					+ ", errno : " + errno 
//					+ ", errmsg : " + errmsg
//					+ " )");
//			}
//		});

		// Samson test OK 2015-04-21
//		String sdPath = "/sdcard/image1.png";
//		RequestJniEMF.UploadAttach(UploadAttachType.IMAGE, sdPath, new OnEMFUploadAttachCallback() {
//			
//			@Override
//			public void OnEMFUploadAttach(boolean isSuccess, String errno, String errmsg) {
//				// TODO Auto-generated method stub
//				Log.d("MainActivity.EMF", "OnEMFUploadAttach( isSuccess : " + String.valueOf(isSuccess) + "" 
//						+ ", errno : " + errno 
//						+ ", errmsg : " + errmsg
//						+ " )");
//			}
//		});
		
		// Samson test OK 2015-04-21
//		RequestJniEMF.OutboxList(0, 15, "", ProgressType.DEFAULT, new OnEMFOutboxListCallback() {
//			
//			@Override
//			public void OnEMFOutboxList(boolean isSuccess, String errno, String errmsg,
//					int pageIndex, int pageSize, int dataCount,
//					EMFOutboxListItem[] listArray) {
//				// TODO Auto-generated method stub
//				Log.d("MainActivity.EMF", "OnEMFOutboxList( isSuccess : " + String.valueOf(isSuccess) + "" 
//						+ ", errno : " + errno 
//						+ ", errmsg : " + errmsg 
//						+ ", pageIndex:" + String.valueOf(pageIndex) 
//						+ ", pageSize:" + String.valueOf(pageSize)
//						+ ", dataCount:" + String.valueOf(dataCount)
//						+ ", listArray.length:" + String.valueOf(listArray.length)
//						+ " )");
//			}
//		});
		
		// Samson test OK 2015-04-21
//		RequestJniEMF.OutboxMsg("19371289", new OnEMFOutboxMsgCallback() {
//			
//			@Override
//			public void OnEMFOutboxMsg(boolean isSuccess, String errno, String errmsg,
//					EMFOutboxMsgItem item) {
//				// TODO Auto-generated method stub
//				Log.d("MainActivity.EMF", "OnEMFOutboxList( isSuccess : " + String.valueOf(isSuccess) + "" 
//						+ ", errno : " + errno 
//						+ ", errmsg : " + errmsg 
//						+ " )");
//			}
//		});
		
		// Samson test OK 2015-03-13
//		RequestJniEMF.MsgTotal(SortType.READ, new OnEMFMsgTotalCallback() {
//			
//			@Override
//			public void OnEMFMsgTotal(boolean isSuccess, String errno, String errmsg,
//					EMFMsgTotalItem item) {
//				// TODO Auto-generated method stub
//				Log.d("MainActivity.EMF", "OnEMFMsgTotal( isSuccess : " + String.valueOf(isSuccess) + "" 
//						+ ", errno : " + errno 
//						+ ", errmsg : " + errmsg
//						+ ", item.msgTotal:" + String.valueOf(item.msgTotal)
//						+ " )");
//			}
//		});
		
		// Samson test OK 2015-03-13
//		RequestJniEMF.DeleteMsg("19371289", MailType.OUTBOX, new OnEMFDeleteMsgCallback() {
//			
//			@Override
//			public void OnEMFDeleteMsg(boolean isSuccess, String errno, String errmsg) {
//				// TODO Auto-generated method stub
//				Log.d("MainActivity.EMF", "OnEMFDeleteMsg( isSuccess : " + String.valueOf(isSuccess) + "" 
//					+ ", errno : " + errno 
//					+ ", errmsg : " + errmsg
//					+ " )");				
//			}
//		});
		
		// Samson test OK 2015-03-13
//		RequestJniEMF.AdmirerList(0, 15, SortType.READ, "", new OnEMFAdmirerListCallback() {
//			
//			@Override
//			public void OnEMFAdmirerList(boolean isSuccess, String errno,
//					String errmsg, int pageIndex, int pageSize, int dataCount,
//					EMFAdmirerListItem[] listArray) {
//				// TODO Auto-generated method stub
//				Log.d("MainActivity.EMF", "OnEMFAdmirerList( isSuccess : " + String.valueOf(isSuccess) + "" 
//						+ ", errno : " + errno 
//						+ ", errmsg : " + errmsg
//						+ ", listArray.length" + String.valueOf(listArray.length)
//						+ " )");
//			}
//		});
		
		// Samson test OK 2015-03-13
//		RequestJniEMF.AdmirerViewer("3872196", new OnEMFAdmirerViewerCallback() {
//			
//			@Override
//			public void OnEMFAdmirerViewer(boolean isSuccess, String errno,
//					String errmsg, EMFAdmirerViewerItem item) {
//				// TODO Auto-generated method stub
//				Log.d("MainActivity.EMF", "OnEMFAdmirerViewer( isSuccess : " + String.valueOf(isSuccess) + "" 
//						+ ", errno : " + errno 
//						+ ", errmsg : " + errmsg
//						+ " )");
//			}
//		});
		
		// Samson test OK 2015-03-13
//		RequestJniEMF.Block("P580502", BlockReasonType.REASON_1, new OnEMFBlockCallback() {
//			
//			@Override
//			public void OnEMFBlock(boolean isSuccess, String errno, String errmsg) {
//				// TODO Auto-generated method stub
//				Log.d("MainActivity.EMF", "OnEMFBlock( isSuccess : " + String.valueOf(isSuccess) + "" 
//					+ ", errno : " + errno 
//					+ ", errmsg : " + errmsg
//					+ " )");				
//			}
//		});
		
		// Samson test OK 2015-03-13
//		RequestJniEMF.BlockList(0, 15, "", new OnEMFBlockListCallback() {
//			
//			@Override
//			public void OnEMFBlockList(boolean isSuccess, String errno, String errmsg,
//					int pageIndex, int pageSize, int dataCount,
//					EMFBlockListItem[] listArray) {
//				// TODO Auto-generated method stub
//				Log.d("MainActivity.EMF", "OnEMFBlockList( isSuccess : " + String.valueOf(isSuccess) + "" 
//					+ ", errno : " + errno 
//					+ ", errmsg : " + errmsg
//					+ ", listArray.length" + String.valueOf(listArray.length)
//					+ " )");				
//			}
//		});
		
		// Samson test OK 2015-03-13
//		String[] womanIdArray = {"P580502", "P844666"};
//		RequestJniEMF.Unblock(womanIdArray, new  OnEMFUnblockCallback() {
//			
//			@Override
//			public void OnEMFUnblock(boolean isSuccess, String errno, String errmsg) {
//				// TODO Auto-generated method stub
//				Log.d("MainActivity.EMF", "OnEMFUnblock( isSuccess : " + String.valueOf(isSuccess) + "" 
//					+ ", errno : " + errno 
//					+ ", errmsg : " + errmsg
//					+ " )");				
//			}
//		});		
		
		// Samson test OK 2015-04-21
//		RequestJniEMF.InboxPhotoFee("GZA881", "sendid", "messageid", new OnEMFInboxPhotoFeeCallback() {
//			
//			@Override
//			public void OnEMFInboxPhotoFee(boolean isSuccess, String errno, String errmsg) {
//				// TODO Auto-generated method stub
//				Log.d("MainActivity.EMF", "OnEMFInboxPhotoFee( isSuccess : " + String.valueOf(isSuccess) + "" 
//					+ ", errno : " + errno 
//					+ ", errmsg : " + errmsg
//					+ " )");
//			}
//		});
		
		// Samson test OK 2015-04-21
//		RequestJniEMF.PrivatePhotoView("GZA881", "sendid", "messageid", "/sdcard/image1.png", new OnEMFPrivatePhotoViewCallback() {
//			
//			@Override
//			public void OnEMFPrivatePhotoView(boolean isSuccess, String errno,
//					String errmsg, String filePath) {
//				// TODO Auto-generated method stub
//				Log.d("MainActivity.EMF", "OnEMFPrivatePhotoView( isSuccess : " + String.valueOf(isSuccess) + "" 
//						+ ", errno : " + errno 
//						+ ", errmsg : " + errmsg
//						+ " )");
//			}
//		});
	}	
	
	
	protected void testVideoShowRequest() {
		// Samson test OK 2015-03-13
//		RequestJniVideoShow.VideoList(0, 15, 1, 99, OrderByType.NEWEST, new OnVSVideoListCallback() {
//			
//			@Override
//			public void OnVSVideoList(boolean isSuccess, String errno, String errmsg,
//					int pageIndex, int pageSize, int dataCount,
//					VSVideoListItem[] listArray) {
//				// TODO Auto-generated method stub
//				Log.d("MainActivity.VideoShow", "OnVSVideoList( isSuccess : " + String.valueOf(isSuccess) + "" 
//					+ ", errno : " + errno 
//					+ ", errmsg : " + errmsg
//					+ ", listArray.length" + String.valueOf(listArray.length)
//					+ " )");
//			}
//		});
		
		// Samson test OK 2015-03-13
//		RequestJniVideoShow.VideoDetail("P2180824", new OnVSVideoDetailCallback() {
//			
//			@Override
//			public void OnVSVideoDetail(boolean isSuccess, String errno, String errmsg,
//					VSVideoDetailItem[] item) {
//				// TODO Auto-generated method stub
//				Log.d("MainActivity.VideoShow", "OnVSVideoDetail( isSuccess : " + String.valueOf(isSuccess) + "" 
//						+ ", errno : " + errno 
//						+ ", errmsg : " + errmsg
//						+ " )");
//			}
//		});
		
		// Samson test OK 2015-03-13
//		RequestJniVideoShow.SaveVideo("V73433", new OnVSSaveVideoCallback() {
//			
//			@Override
//			public void OnVSSaveVideo(boolean isSuccess, String errno, String errmsg) {
//				// TODO Auto-generated method stub
//				Log.d("MainActivity.VideoShow", "OnVSSaveVideo( isSuccess : " + String.valueOf(isSuccess) + "" 
//						+ ", errno : " + errno 
//						+ ", errmsg : " + errmsg
//						+ " )");								
//			}
//		});
		
		// Samson test OK 2015-03-13
//		RequestJniVideoShow.SavedVideoList(0, 15, new OnVSSavedVideoListCallback() {
//			
//			@Override
//			public void OnVSSavedVideoList(boolean isSuccess, String errno,
//					String errmsg, int pageIndex, int pageSize, int dataCount,
//					VSSavedVideoListItem[] listArray) {
//				// TODO Auto-generated method stub
//				Log.d("MainActivity.VideoShow", "OnVSSavedVideoList( isSuccess : " + String.valueOf(isSuccess) + "" 
//						+ ", errno : " + errno 
//						+ ", errmsg : " + errmsg
//						+ " )");
//			}
//		});
		
		// Samson test OK 2015-03-13
//		RequestJniVideoShow.RemoveVideo("V73433", new OnVSRemoveVideoCallback() {
//			
//			@Override
//			public void OnVSRemoveVideo(boolean isSuccess, String errno, String errmsg) {
//				// TODO Auto-generated method stub
//				Log.d("MainActivity.VideoShow", "OnVSRemoveVideo( isSuccess : " + String.valueOf(isSuccess) + "" 
//					+ ", errno : " + errno 
//					+ ", errmsg : " + errmsg
//					+ " )");
//			}
//		});
		
		// Samson test OK 2015-03-13
//		RequestJniVideoShow.WatchedVideoList(0, 15, new OnVSWatchedVideoListCallback() {
//			
//			@Override
//			public void OnVSWatchedVideoList(boolean isSuccess, String errno,
//					String errmsg, int pageIndex, int pageSize, int dataCount,
//					VSWatchedVideoListItem[] listArray) {
//				// TODO Auto-generated method stub
//				Log.d("MainActivity.VideoShow", "OnVSWatchedVideoList( isSuccess : " + String.valueOf(isSuccess) + "" 
//						+ ", errno : " + errno 
//						+ ", errmsg : " + errmsg
//						+ ", listArray.length: " + String.valueOf(listArray.length)
//						+ " )");
//			}
//		});
		
		// Samson test OK 2015-03-13
//		RequestJniVideoShow.PlayVideo("GZB273195", "V74914", new OnVSPlayVideoCallback() {
//			
//			@Override
//			public void OnVSPlayVideo(boolean isSuccess, String errno, String errmsg,
//					VSPlayVideoItem item) {
//				// TODO Auto-generated method stub
//				Log.d("MainActivity.VideoShow", "OnVSPlayVideo( isSuccess : " + String.valueOf(isSuccess) + "" 
//					+ ", errno : " + errno 
//					+ ", errmsg : " + errmsg
//					+ " )");				
//			}
//		});
	}
	
	protected void testOther() {
		// Samson test OK 2015-03-18
//		RequestJniOther.EmotionConfig(new OnOtherEmotionConfigCallback() {
//			
//			@Override
//			public void OnOtherEmotionConfig(boolean isSuccess, String errno,
//					String errmsg, OtherEmotionConfigItem item) {
//				// TODO Auto-generated method stub
//				Log.d("MainActivity.Other", "OnOtherEmotionConfig( isSuccess : " + String.valueOf(isSuccess) + "" 
//					+ ", errno : " + errno 
//					+ ", errmsg : " + errmsg
//					+ " )");
//			}
//		});
		
		// Samson test OK 2015-04-23
//		RequestJniOther.GetCount(true, true, true, true, true, new OnOtherGetCountCallback() {
//			
//			@Override
//			public void OnOtherGetCount(boolean isSuccess, String errno, String errmsg,
//					OtherGetCountItem item) {
//				// TODO Auto-generated method stub
//				Log.d("MainActivity.Other", "OnOtherGetCount( isSuccess : " + String.valueOf(isSuccess) + "" 
//					+ ", errno : " + errno 
//					+ ", errmsg : " + errmsg
//					+ " )");
//			}
//		});
		
		// Samson test OK 2015-03-18
//		RequestJniOther.PhoneInfo("CM28171208", 1, "1.0", RequestJniOther.ActionType.NEWUSER, RequestJniOther.SiteTypeCL, 2.0, 240, 360, "+8613580600000", "UnknowSimOptName", "UnknowSimOpt", "UnknowSimCountryIso", "UnknowSimState", 1, 7, "UnknowDeviceId", new OnOtherPhoneInfoCallback() {
//			
//			@Override
//			public void OnOtherPhoneInfo(boolean isSuccess, String errno, String errmsg) {
//				// TODO Auto-generated method stub
//				Log.d("MainActivity.Other", "OnOtherPhoneInfo( isSuccess : " + String.valueOf(isSuccess) + "" 
//					+ ", errno : " + errno 
//					+ ", errmsg : " + errmsg
//					+ " )");				
//			}
//		});

		// Samson test OK 2015-03-18
//		RequestJniOther.IntegralCheck("P815445", new OnOtherIntegralCheckCallback() {
//			
//			@Override
//			public void OnOtherIntegralCheck(boolean isSuccess, String errno,
//					String errmsg, OtherIntegralCheckItem item) {
//				// TODO Auto-generated method stub
//				Log.d("MainActivity.Other", "OnOtherIntegralCheck( isSuccess : " + String.valueOf(isSuccess) + "" 
//					+ ", errno : " + errno 
//					+ ", errmsg : " + errmsg
//					+ ", integral: " + String.valueOf(item.integral)
//					+ " )");
//			}
//		});
		
		// Samson test OK 2015-03-18
//		RequestJniOther.VersionCheck(282, new OnOtherVersionCheckCallback() {
//			
//			@Override
//			public void OnOtherVersionCheck(boolean isSuccess, String errno,
//					String errmsg, OtherVersionCheckItem item) {
//				// TODO Auto-generated method stub
//				Log.d("MainActivity.Other", "OnOtherVersionCheck( isSuccess : " + String.valueOf(isSuccess) + "" 
//					+ ", errno : " + errno 
//					+ ", errmsg : " + errmsg
//					+ " )");
//			}
//		});
		
		// Samson test OK 2015-03-18
		RequestJniOther.SynConfig(new OnOtherSynConfigCallback() {
			
			@Override
			public void OnOtherSynConfig(boolean isSuccess, String errno,
					String errmsg, OtherSynConfigItem item) {
				// TODO Auto-generated method stub
				Log.d("MainActivity.Other", "OnOtherSynConfig( isSuccess : " + String.valueOf(isSuccess) + "" 
					+ ", errno : " + errno 
					+ ", errmsg : " + errmsg
					+ " )");
			}
		});
		
		// Samson test OK 2015-04-23
//		RequestJniOther.OnlineCount(RequestJniOther.SiteTypeAll, new OnOtherOnlineCountCallback() {
//			
//			@Override
//			public void OnOtherOnlineCount(boolean isSuccess, String errno,
//					String errmsg, OtherOnlineCountItem[] item) {
//				// TODO Auto-generated method stub
//				Log.d("MainActivity.Other", "OnOtherOnlineCount( isSuccess : " + String.valueOf(isSuccess) + "" 
//					+ ", errno: " + errno 
//					+ ", errmsg: " + errmsg
//					+ " )");
//			}
//		});
	}
	
	protected void testLady() {
		// Samson test OK 2015-04-21
//		RequestJniLady.QueryLadyList(0, 30, SearchType.NEWEST, "", false, 0, 0, "", new OnQueryLadyListCallback() {
//			
//			@Override
//			public void OnQueryLadyList(boolean isSuccess, String errno, String errmsg,
//					Lady[] ladyList, int total) {
//				// TODO Auto-generated method stub
//				Log.d("MainActivity.Lady", "OnQueryLadyList ( isSuccess:" + String.valueOf(isSuccess) 
//						+ ", errno:" + errno
//						+ ", errmsg:" + errmsg
//						+ ", ladyList.length:" + String.valueOf(ladyList.length)
//						+ " )");
//			}
//		});
		
		// Samson test OK 2015-04-21
//		RequestJniLady.RecentContact(new OnLadyRecentContactListCallback() {
//			
//			@Override
//			public void OnLadyRecentContactList(boolean isSuccess, String errno,
//					String errmsg, LadyRecentContactItem[] listArray) {
//				// TODO Auto-generated method stub
//				Log.d("MainActivity.Lady", "OnLadyRecentContactList ( isSuccess:" + String.valueOf(isSuccess)
//						+ ", errno:" + errno
//						+ ", errmsg:" + errmsg
//						+ ", listArray.length:" + String.valueOf(listArray.length)
//						+ " )");
//			}
//		});
		
		// Samson test OK 2015-04-21
//		RequestJniLady.SignList("GZA881", new OnLadySignListCallback() {
//			
//			@Override
//			public void OnLadySignList(boolean isSuccess, String errno, String errmsg,
//					LadySignItem[] listArray) {
//				// TODO Auto-generated method stub
//				Log.d("MainActivity.Lady", "OnLadySignList ( isSuccess:" + String.valueOf(isSuccess)
//						+ ", errno:" + errno
//						+ ", errmsg:" + errmsg
//						+ ", listArray.length:" + String.valueOf(listArray.length)
//						+ " )");
//			}
//		});
		
		// Samson test OK 2015-04-21
//		String[] signIdArray = {"1001", "1002"};
//		RequestJniLady.UploadSign("GZA881", signIdArray, new OnRequestCallback() {
//			
//			@Override
//			public void OnRequest(boolean isSuccess, String errno, String errmsg) {
//				// TODO Auto-generated method stub
//				Log.d("MainActivity.Lady", "OnRequest ( isSuccess:" + String.valueOf(isSuccess)
//					+ ", errno:" + errno
//					+ ", errmsg:" + errmsg
//					+ " )");
//			}
//		});
	}
	
	public void testAdvert()
	{
		// Samson test OK 2015-04-22
//		RequestJniAdvert.MainAdvert("123456", "321654", 1, 2, new OnAdMainAdvertCallback() {
//			
//			@Override
//			public void OnAdMainAdvert(boolean isSuccess, String errno, String errmsg,
//					AdMainAdvert advert) {
//				// TODO Auto-generated method stub
//				Log.d("MainActivity.Lady", "OnAdMainAdvert ( isSuccess:" + String.valueOf(isSuccess)
//					+ ", errno:" + errno
//					+ ", errmsg:" + errmsg
//					+ ", advert.advertId:" + advert.id
//					+ ", advert.image:" + advert.image
//					+ ", advert.width:" + String.valueOf(advert.width)
//					+ ", advert.height:" + String.valueOf(advert.height)
//					+ ", advert.adurl:" + advert.adurl
//					+ ", advert.openType:" + String.valueOf(advert.openType)
//					+ ", advert.isShow:" + String.valueOf(advert.isShow)
//					+ ", advert.validTime:" + String.valueOf(advert.validTime)
//					+ " )");				
//			}
//		});
		
		// Samson test OK 2015-04-22
//		RequestJniAdvert.WomanListAdvert("123456", "3214", 1, 2, new OnAdWomanListAdvertCallback() {
//			
//			@Override
//			public void OnAdWomanListAdvert(boolean isSuccess, String errno,
//					String errmsg, AdWomanListAdvert advert) {
//				// TODO Auto-generated method stub
//				Log.d("MainActivity.Lady", "OnAdWomanListAdvert ( isSuccess:" + String.valueOf(isSuccess)
//					+ ", errno:" + errno
//					+ ", errmsg:" + errmsg
//					+ ", advert.advertId:" + advert.id
//					+ ", advert.image:" + advert.image
//					+ ", advert.width:" + String.valueOf(advert.width)
//					+ ", advert.height:" + String.valueOf(advert.height)
//					+ ", advert.adurl:" + advert.adurl
//					+ ", advert.openType:" + String.valueOf(advert.openType)
//					+ " )");`
//			}
//		});

		// Samson test OK 2015-04-22
//		RequestJniAdvert.PushAdvert("123456", "1001", new OnAdPushAdvertCallback() {
//			
//			@Override
//			public void OnAdPushAdvert(boolean isSuccess, String errno, String errmsg,
//					AdPushAdvert[] advert) {
//				// TODO Auto-generated method stub
//				Log.d("MainActivity.Lady", "OnAdPushAdvert ( isSuccess:" + String.valueOf(isSuccess)
//					+ ", errno:" + errno
//					+ ", errmsg:" + errmsg
//					+ " )");
//			}
//		});
	}
	
	public void testLiveChat() {
		// Samson test OK 2015-04-23
//		String sdPath = "/sdcard/image1.png";
//		RequestJniLiveChat.SendPhoto("123456", "321654", sdPath, new OnLCSendPhotoCallback() {
//			
//			@Override
//			public void OnLCSendPhoto(boolean isSuccess, String errno, String errmsg,
//					LCSendPhotoItem item) {
//				// TODO Auto-generated method stub
//				Log.d("MainActivity.LiveChat", String.format("OnLCSendPhoto ( isSuccess:" + String.valueOf(isSuccess)
//					+ ", errno:" + errno
//					+ ", errmsg:" + errmsg
//					+ " )");				
//			}
//		});
		
		// Samson test OK 2015-04-23
//		RequestJniLiveChat.PhotoFee("123456", "321564", "123456", new OnLCPhotoFeeCallback() {
//			
//			@Override
//			public void OnLCPhotoFee(boolean isSuccess, String errno, String errmsg) {
//				// TODO Auto-generated method stub
//				Log.d("MainActivity.LiveChat", String.format("OnLCPhotoFee ( isSuccess:" + String.valueOf(isSuccess)
//						+ ", errno:" + errno
//						+ ", errmsg:" + errmsg
//						+ " )");
//			}
//		});
		
		// Samson test OK 2015-04-23
//		String imgPath = "/sdcard/image2.png";
//		RequestJniLiveChat.GetPhoto(
//				RequestJniLiveChat.ToFlagType.ManGetWoman
//				, "123456"
//				, "321654"
//				, RequestJniLiveChat.PhotoSizeType.Middle
//				, RequestJniLiveChat.PhotoModeType.Clear
//				, imgPath
//				, new OnLCGetPhotoCallback() {
//					
//					@Override
//					public void OnLCGetPhoto(boolean isSuccess, String errno, String errmsg,
//							String filePath) {
//						// TODO Auto-generated method stub
//						Log.d("MainActivity.LiveChat", String.format("OnLCPhotoFee ( isSuccess:" + String.valueOf(isSuccess)
//							+ ", errno:" + errno
//							+ ", errmsg:" + errmsg
//							+ " )");
//					}
//				});
	}
	
	private enum LiveChatOpt {
		CheckCoupon,
		UseTryTicket,
		GetPhoto,
		PhotoFee,
		SendPhoto,
		GetVoice,
		SendVoice,
	}
	
	private HandlerThread mHandlerThread = null;
	private Handler mHandler = null;
	static private LiveChatManager liveChatMgr = LiveChatManager.newInstance(null);
	static private int mRecvMsgCount = 0;
	static private String lastEmotionId = "";
	static private boolean isGetEmotionConfig = false;
	public void testLiveChatManager() {
		String[] ips = {"58.64.141.117", "97.74.124.191", "50.62.144.124", "95.211.9.37"};
		int port = 5000;
		String emotionPath = "/sdcard";
		String photoPath = "/sdcard";
		String voicePath = "/sdcard";
//		liveChatMgr.Init(getApplicationContext(), ips, port, "http://demo.chnlove.com", emotionPath, photoPath, voicePath);
		liveChatMgr.RegisterOtherListener(new LiveChatManagerOtherListener() {
			
			@Override
			public void OnUpdateStatus(LCUserItem userItem) {
				// TODO Auto-generated method stub
				Log.d("MainActivity.LiveChat", String.format("OnUpdateStatus() userId:%s, status:%s", userItem.userId, userItem.statusType.name()));
			}
			
			@Override
			public void OnChangeOnlineStatus(LCUserItem userItem) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			// test by samson 2015-05-04
			public void OnSetStatus(LiveChatErrType errType, String errmsg) {
				// TODO Auto-generated method stub
				Log.d("MainActivity.LiveChat", String.format("OnSetStatus() errType:%s, errmsg:%s", errType.name(), errmsg));
			}
			
			@Override
			// test by samson 2015-05-05
			public void OnRecvKickOffline(KickOfflineType kickType) {
				// TODO Auto-generated method stub
				Log.d("MainActivity.LiveChat", String.format("OnRecvKickOffline() kickType:%s", kickType.name()));
			}
			
			@Override
			public void OnRecvEMFNotice(String fromId, TalkEmfNoticeType noticeType) {
				// TODO Auto-generated method stub
				Log.d("MainActivity.LiveChat", String.format("OnRecvEMFNotice() fromId:%s, noticeType:%s", fromId, noticeType));
			}
			
			@Override
			// test by samson 2015-05-04
			public void OnLogout(LiveChatErrType errType, String errmsg, boolean isAutoLogin) {
				// TODO Auto-generated method stub
				Log.d("MainActivity.LiveChat", String.format("OnLogout() errType:%s, errmsg:%s", errType.name(), errmsg));
//				if (errType == LiveChatErrType.ConnectFail) {
//					liveChatMgr.Login("CM28171208", "lmlhh5f4gg11obunk27tv0617j", "000000000000000");
//				}
				isToLogin = true;
			}
			
			@Override
			// test by samson 2015-05-04
			public void OnLogin(LiveChatErrType errType, String errmsg, boolean isAutoLogin) {
				// TODO Auto-generated method stub
				Log.d("MainActivity.LiveChat", String.format("OnLogin() errType:%s, errmsg:%s", errType.name(), errmsg));
				if (errType == LiveChatErrType.Success) {
					liveChatMgr.SetStatus(UserStatusType.USTATUS_ONLINE);
					synchronized(isToLogin) {
						isToLogin = false;
					}
				}
			}
			
			@Override
			// test by samson 2015-05-05
			public void OnGetUserStatus(LiveChatErrType errType, String errmsg,
					LCUserItem[] userList) {
				// TODO Auto-generated method stub
				Log.d("MainActivity.LiveChat", String.format("OnGetUserStatus() errType:%s, errmsg:%s", errType.name(), errmsg));
			}
			
			@Override
			public void OnGetTalkList(LiveChatErrType errType, String errmsg) {
				// TODO Auto-generated method stub
				Log.d("MainActivity.LiveChat", String.format("OnGetTalkList() errType:%s, errmsg:%s", errType.name(), errmsg));
				if (errType == LiveChatErrType.Success) {
					ArrayList<LCUserItem> chatingUsers = liveChatMgr.GetChatingUsers();
					ArrayList<LCUserItem> inviteUsers = liveChatMgr.GetInviteUsers();
					Log.d("MainActivity.LiveChat", String.format("OnGetTalkList() chatingUsers.size:%d, inviteUsers:%d", chatingUsers.size(), inviteUsers.size()));
					
					if (inviteUsers.size() > 0) {
						Message msg = new Message();
						msg.what = LiveChatOpt.CheckCoupon.ordinal();
						msg.obj = inviteUsers.get(0).userId;
						mHandler.sendMessage(msg);
					}
				}
			}
			
			@Override
			public void OnGetHistoryMessage(boolean success, String errno, String errmsg, LCUserItem userItem)
			{
				Log.d("MainActivity.LiveChat", String.format("OnGetHistoryMessage() errno:%s, errmsg:%s", errno, errmsg));
			}
			
			@Override
			public void OnGetUsersHistoryMessage(boolean success, String errno, String errmsg,
					LCUserItem[] userItems) {
				// TODO Auto-generated method stub
				Log.d("MainActivity.LiveChat", String.format("OnGetUsersHistoryMessage() errno:%s, errmsg:%s", errno, errmsg));
			}
		});
		
		liveChatMgr.RegisterTryTicketListener(new LiveChatManagerTryTicketListener() {
			
			@Override
			public void OnUseTryTicket(LiveChatErrType errType, String errno, String errmsg,
					String userId, TryTicketEventType eventType) {
				// TODO Auto-generated method stub
				Log.d("MainActivity.LiveChat", String.format("OnUseTryTicket() errType:%s, errno:%s, errmsg:%s, userId:%s, eventType:%s"
						, errType.name(), errno, errmsg, userId, eventType.name()));
				if (errType == LiveChatErrType.Success) {
					if (eventType == TryTicketEventType.Normal) {
						liveChatMgr.SendMessage(userId, "hi");
					}
				}
			}
			
			@Override
			public void OnRecvTryTalkEnd(LCUserItem userItem) {
				// TODO Auto-generated method stub
				Log.d("MainActivity.LiveChat", String.format("OnRecvTryTalkEnd()"));
			}
			
			@Override
			public void OnRecvTryTalkBegin(LCUserItem userItem, int time) {
				// TODO Auto-generated method stub
				Log.d("MainActivity.LiveChat", String.format("OnRecvTryTalkBegin()"));
			}
			
			@Override
			// test by samson 2015-05-06
			public void OnRecvTalkEvent(LCUserItem item) {
				// TODO Auto-generated method stub
				Log.d("MainActivity.LiveChat", String.format("OnRecvTalkEvent()"));
			}
			
			@Override
			// test by samson 2015-05-06
			public void OnEndTalk(LiveChatErrType errType, String errmsg,
					LCUserItem userItem) {
				// TODO Auto-generated method stub
				Log.d("MainActivity.LiveChat", String.format("OnEndTalk() errType:%s, errmsg:%s", errType.name(), errmsg));
				if (errType == LiveChatErrType.Success) {
//					liveChatMgr.GetTalkList();
				}
			}
			
			@Override
			// test by samson 2015-05-05
			public void OnCheckCoupon(boolean success, String errno, String errmsg, Coupon item) {
				// TODO Auto-generated method stub
				Log.d("MainActivity.LiveChat", String.format("OnCheckCoupon() errno:%s errmsg:%s", errno, errmsg));
				if (item.status == CouponStatus.Yes) {
//					liveChatMgr.UseTryTicket(item.userId);
					Message msg = new Message();
					msg.what = LiveChatOpt.UseTryTicket.ordinal();
					msg.obj = item.userId;
					mHandler.sendMessage(msg);
				}
				else {
					liveChatMgr.SendMessage(item.userId, "hi");
				}
			}
		});
		liveChatMgr.RegisterMessageListener(new LiveChatManagerMessageListener() {
			
			@Override
			// test by samson 2015-05-06
			public void OnSendMessage(LiveChatErrType errType, String errmsg,
					LCMessageItem item) {
				// TODO Auto-generated method stub
				Log.d("MainActivity.LiveChat", String.format("OnSendMessage() errType:%s, errmsg:%s", errType.name(), errmsg));
			}
			
			@Override
			public void OnRecvWarning(LCMessageItem item) {
				// TODO Auto-generated method stub
				Log.d("MainActivity.LiveChat", String.format("OnRecvWarning()"));
			}
			
			@Override
			// test by samson 2015-05-04
			public void OnRecvMessage(LCMessageItem item) {
				// TODO Auto-generated method stub
				Log.d("MainActivity.LiveChat", String.format("OnRecvMessage()"));
				
				if (item.getUserItem().chatType == ChatType.InChatCharge
					|| item.getUserItem().chatType == ChatType.InChatUseTryTicket)
				{
					mRecvMsgCount++;
					liveChatMgr.SendMessage(item.fromId, item.getTextItem().message);
					
					if (mRecvMsgCount % 3 == 0) {
						if (!isGetEmotionConfig) {
							liveChatMgr.GetEmotionConfig();
						}					
						else if (!lastEmotionId.isEmpty()) {
							liveChatMgr.SendEmotion(item.fromId, lastEmotionId);
						}
					}
					
					if (mRecvMsgCount % 5 == 0) {
//						liveChatMgr.GetTalkList();
					}
					
					if (mRecvMsgCount % 8 == 0) {
						String[] userIds = {item.fromId, "P580502"};
						liveChatMgr.GetUserStatus(userIds);
					}
					
					if (mRecvMsgCount % 10 == 0) {
						liveChatMgr.GetHistoryMessage(item.fromId);
					}
					
					if (mRecvMsgCount % 15 == 0) {
						boolean endResult = liveChatMgr.EndTalk(item.fromId);
						Log.d("MainActivity.LiveChat", String.format("OnRecvMessage() endResult:%s", String.valueOf(endResult)));
					}
					
					if (mRecvMsgCount % 30 == 0) {
						liveChatMgr.Logout();
					}
				}
				else if (item.getUserItem().chatType == ChatType.Invite) {
					liveChatMgr.CheckCoupon(item.getUserItem().userId);
				}
			}
			
			@Override
			public void OnRecvSystemMsg(LCMessageItem item) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			// test by samson 2015-05-04
			public void OnRecvEditMsg(String fromId) {
				// TODO Auto-generated method stub
				Log.d("MainActivity.LiveChat", String.format("OnRecvEditMsg() fromId:%s", fromId));
			}

			@Override
			public void OnSendMessageListFail(LiveChatErrType errType,
					ArrayList<LCMessageItem> msgList) {
				// TODO Auto-generated method stub
				
			}
		});
		
		liveChatMgr.RegisterEmotionListener(new LiveChatManagerEmotionListener() {
			
			@Override
			// test by samson 2015-05-06
			public void OnSendEmotion(LiveChatErrType errType, String errmsg,
					LCMessageItem item) {
				// TODO Auto-generated method stub
				Log.d("MainActivity.LiveChat", String.format("OnSendEmotion() errType:%s, errmsg:%s", errType.name(), errmsg));
				liveChatMgr.GetEmotionImage(item.getEmotionItem().emotionId);
			}
			
			@Override
			// test by samson 2015-05-04
			public void OnRecvEmotion(LCMessageItem item) {
				// TODO Auto-generated method stub
				Log.d("MainActivity.LiveChat", String.format("OnRecvEmotion()"));
				liveChatMgr.GetEmotionPlayImage(item.getEmotionItem().emotionId);
			}
			
			@Override
			// test by samson 2015-05-06
			public void OnGetEmotionConfig(boolean success, String errno, String errmsg, OtherEmotionConfigItem item) {
				// TODO Auto-generated method stub
				Log.d("MainActivity.LiveChat", String.format("OnGetEmotionConfig() errno:%s, errmsg:%s", errno, errmsg));
				if (errno.isEmpty()) {
					isGetEmotionConfig = true;
					if (item.manEmotionList.length > 0) {
						lastEmotionId = item.manEmotionList[item.manEmotionList.length-1].fileName;
						LCEmotionItem emotionItem = liveChatMgr.GetEmotionInfo(lastEmotionId);
						if (null == emotionItem || emotionItem.imagePath.isEmpty()) {
							liveChatMgr.GetEmotionImage(lastEmotionId);
						}
					}
				}
			}
			
			@Override
			// test by samson 2015-05-06
			public void OnGetEmotionImage(boolean success, LCEmotionItem item) {
				// TODO Auto-generated method stub
				Log.d("MainActivity.LiveChat", String.format("OnGetEmotionImage() success:%b, item.imagePath:%s"
						, success, item.imagePath));
			}
			
			@Override
			// test by samson 2015-05-06
			public void OnGetEmotion3gp(boolean success, LCEmotionItem item) {
				// TODO Auto-generated method stub
				Log.d("MainActivity.LiveChat", String.format("OnGetEmotion3gp() success:%b, item.f3gpPath:%s"
						, success, item.f3gpPath));
			}

			@Override
			public void OnGetEmotionPlayImage(boolean success,
					LCEmotionItem item) {
				// TODO Auto-generated method stub
				Log.d("MainActivity.LiveChat", String.format("OnGetEmotionPlayImage() success:%b, item.playBigImages.size():%d"
						, success, item.playBigImages.size()));				
			}
		});
		liveChatMgr.RegisterPhotoListener(new LiveChatManagerPhotoListener() {
			
			@Override
			public void OnSendPhoto(LiveChatErrType errType, String errno,
					String errmsg, LCMessageItem item) {
				// TODO Auto-generated method stub
				Log.d("MainActivity.LiveChat", String.format("OnSendPhoto() errType:%s, errmsg:%s", errType.name(), errmsg));
			}
			
			@Override
			public void OnRecvPhoto(LCMessageItem item) {
				// TODO Auto-generated method stub
				Log.d("MainActivity.LiveChat", String.format("OnRecvPhoto()"));
//				liveChatMgr.SendPhoto(item.fromId, item.getPhotoItem().filePath);
//				liveChatMgr.PhotoFee(item);
//				liveChatMgr.GetPhoto(item);
				Message msg = new Message();
				msg.what = LiveChatOpt.GetPhoto.ordinal();
				msg.obj = item;
				mHandler.sendMessage(msg);
			}
			
			@Override
			public void OnPhotoFee(boolean success, String errno, String errmsg, LCMessageItem item) {
				// TODO Auto-generated method stub
				Log.d("MainActivity.LiveChat", String.format("OnPhotoFee() errno:%s, errmsg:%s", errno, errmsg));
//				liveChatMgr.GetPhoto(item);
				Message msg = new Message();
				msg.what = LiveChatOpt.GetPhoto.ordinal();
				msg.obj = item;
				mHandler.sendMessage(msg);
			}
			
			@Override
			public void OnGetPhoto(LiveChatErrType errType, String errno,
					String errmsg, LCMessageItem item) {
				// TODO Auto-generated method stub
				Log.d("MainActivity.LiveChat", String.format("OnGetPhoto() errType:%s, errmsg:%s"
						, errType.name(), errmsg));
				if (item.getPhotoItem().charge) {
//					liveChatMgr.SendPhoto(item.fromId, item.getPhotoItem().filePath);
					Message msg = new Message();
					msg.what = LiveChatOpt.SendPhoto.ordinal();
					msg.obj = item;
					mHandler.sendMessage(msg);
				}
				else {
//					liveChatMgr.PhotoFee(item);
					Message msg = new Message();
					msg.what = LiveChatOpt.PhotoFee.ordinal();
					msg.obj = item;
					mHandler.sendMessage(msg);
				}
			}
		});
		liveChatMgr.RegisterVoiceListener(new LiveChatManagerVoiceListener() {
			
			@Override
			// test by samson 2015-05-04
			public void OnSendVoice(LiveChatErrType errType, String errno,
					String errmsg, LCMessageItem item) {
				// TODO Auto-generated method stub
				Log.d("MainActivity.LiveChat", String.format("OnSendVoice() errType:%s, errmsg:%s", errType.name(), errmsg));
			}
			
			@Override
			// test by samson 2015-05-04
			public void OnRecvVoice(LCMessageItem item) {
				// TODO Auto-generated method stub
				Log.d("MainActivity.LiveChat", String.format("OnRecvVoice()"));
//				liveChatMgr.GetVoice(item);
				Message msg = new Message();
				msg.what = LiveChatOpt.GetVoice.ordinal();
				msg.obj = item;
				mHandler.sendMessage(msg);
			}
			
			@Override
			// test by samson 2015-05-04
			public void OnGetVoice(LiveChatErrType errType, String errmsg,
					LCMessageItem item) {
				// TODO Auto-generated method stub
				Log.d("MainActivity.LiveChat", String.format("OnGetVoice() errType:%s, errmsg:%s", errType.name(), errmsg));
				if (errType == LiveChatErrType.Success) {
//					liveChatMgr.SendVoice(item.fromId, item.getVoiceItem().filePath, item.getVoiceItem().fileType, item.getVoiceItem().timeLength);
					Message msg = new Message();
					msg.what = LiveChatOpt.SendVoice.ordinal();
					msg.obj = item;
					mHandler.sendMessage(msg);
				}
			}
		});
		
		mHandlerThread = new HandlerThread("livechat handler");
		mHandlerThread.start();
		
		mHandler = new Handler(mHandlerThread.getLooper()) {
			@Override
            public void handleMessage(Message msg) {
            	LiveChatManager liveChatManager = LiveChatManager.newInstance(null);
            	LiveChatOpt opt = LiveChatOpt.values()[msg.what];
                switch (opt) {
                case GetPhoto: {
                	LCMessageItem item = (LCMessageItem)msg.obj;
//                	liveChatManager.GetPhoto(item, PhotoSizeType.Middle);
                }break;
                case PhotoFee: {
                	LCMessageItem item = (LCMessageItem)msg.obj;
                	liveChatManager.PhotoFee(item);
                }break;
                case SendPhoto: {
                	LCMessageItem item = (LCMessageItem)msg.obj;
                	liveChatManager.SendPhoto(item.fromId, item.getPhotoItem().srcFilePath);
                }break;
                case CheckCoupon: {
                	String userId = (String)msg.obj;
                	liveChatMgr.CheckCoupon(userId);
                }break;
                case UseTryTicket: {
                	String userId = (String)msg.obj;
                }break;
                case GetVoice: {
                	LCMessageItem item = (LCMessageItem)msg.obj;
                	liveChatMgr.GetVoice(item);
                }break;
                case SendVoice: {
                	LCMessageItem item = (LCMessageItem)msg.obj;
                	liveChatMgr.SendVoice(item.fromId, item.getVoiceItem().filePath, item.getVoiceItem().fileType, item.getVoiceItem().timeLength);
                }break;
                }
            }
		};
	}
	
	public void onClickRegister(View v) {
		startActivity(new Intent(this, RegisterActivity.class));
	}
	
	public void onClickQuickMatch(View v) {
		startActivity(new Intent(this, QuickMatchActivity.class));
	}
	
	public void onClickMyProfile(View v) {
		startActivity(new Intent(this, MyProfileActivity.class));
	}

	private Boolean isToLogin = true;
	public void onClickLiveChat(View v) {
		synchronized(isToLogin) 
		{
			if (isToLogin) {
				Log.d("MainActivity.LiveChat", "onClickLiveChat() Login");
				liveChatMgr.Login("CM28171208", "lmlhh5f4gg11obunk27tv0617j", "000000000000000");
			}
			else {
				Log.d("MainActivity.LiveChat", "onClickLiveChat() Logout");
				liveChatMgr.Logout();
			}
		}
	}
	
	private ImageViewLoader mLoader = null;
	public void testImageView() {
		mLoader = new ImageViewLoader(getApplicationContext());
		
	    final BitmapFactory.Options options = new BitmapFactory.Options();
	    options.inJustDecodeBounds = false;
//	    Bitmap src = BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.test1, options);
//		mLoader.SetDefaultImage(new BitmapDrawable(src));
	}
	
	public void onClickTestImageView(View v) {
		if (null != mLoader ) {
			String filePath = FileCacheManager.getInstance().CacheLadyPathFromUrl("GZA881", LadyFileType.LADY_PHOTO);
			mLoader.DisplayImage((ImageView)v, "http://demo.chnlove.com/woman_photo/GZA/33/GZA881-b.jpg"
					, 300, 300, 2, 0, filePath
					, new ImageViewLoaderCallback(){

				@Override
				public void OnDisplayNewImageFinish()
				{
					
				}

				@Override
				public void OnLoadPhotoFailed() {
					// TODO Auto-generated method stub
					
				}
			});
		}
	}
	
	private long requestTime = 0;
	private long time = 0;
	private int requestCount = 0;
	public void testRequest() 
	{
		mHandler = new Handler() {
			@Override
            public void handleMessage(Message msg) {
				switch (msg.what) {
//				case 0: {
//					requestCount++;
//					RequestJniAuthorization.Login(
//							"CM28171208", 
//							"1234", 
//							"",
//							"00000000000", 
//							String.valueOf(QpidApplication.versionCode), 
//							Build.MODEL, 
//							Build.MANUFACTURER, 
//							new OnLoginCallback() {
//								
//								@Override
//								public void OnLogin(boolean isSuccess, String errno, String errmsg,
//										LoginItem item) {
//									// TODO Auto-generated method stub
//									if (requestCount <= 1000) {
//										mHandler.sendEmptyMessage(0);
//									}
//									else {
//										requestCount = 0;
//										mHandler.sendEmptyMessage(1);
//									}
//								}
//							});
//				}break;
				case 0: {
					requestCount++;
					Log.d("MainActivity", "RequestJniLady requestCount:%d", requestCount);
					requestTime = System.currentTimeMillis();
					RequestJniLady.QueryLadyList(0, 100, SearchType.DEFAULT, null, OnlineType.DEFAULT, -1, -1, null, OrderType.NEWST, "00000000000", new OnQueryLadyListCallback() {
						
						@Override
						public void OnQueryLadyList(boolean isSuccess, String errno, String errmsg,
								Lady[] ladyList, int totalCount) {
							// TODO Auto-generated method stub
							long procTime = System.currentTimeMillis() - requestTime; 
							if (requestCount <= 1000) {
								time = System.currentTimeMillis();
								mHandler.sendEmptyMessage(1);
								
								Log.d("MainActivity", "QueryLadyList() ok, procTime:%d, requestCount:%d, success:%s", procTime, requestCount, String.valueOf(isSuccess));
							}
							else {
								Log.d("MainActivity", "QueryLadyList() finish, procTime:%d, requestCount:%d, success:%s", procTime, requestCount, String.valueOf(isSuccess));
							}
						}
					});
				}break;
				case 1: {
					final long second = 3 * 1000;
					if (time + second <= System.currentTimeMillis()) {
						mHandler.sendEmptyMessage(0);
					}
					else {
						mHandler.sendEmptyMessage(1);
					}
				}break;
				}
			}
		};
		mHandler.sendEmptyMessage(0);
	}
	
	private void testTicket()
	{
//		RequestJniTicket.TicketList(0, 20, new OnTicketListCallback() {
//			
//			@Override
//			public void OnTicketList(boolean isSuccess, String errno, String errmsg,
//					int pageIndex, int pageSize, int dataCount, TicketListItem[] list) {
//				// TODO Auto-generated method stub
//				Log.d("RequestJniTicket", "success:%s, errno:%s, errmsg:%s, pageIndex:%d, pageSize:%d, dataCount:%d, list.length:%d"
//						, String.valueOf(isSuccess), errno, errmsg, pageIndex, pageSize, dataCount, list.length);
//			}
//		});
		
//		RequestJniTicket.TicketDetail("abc", new OnTicketDetailCallback() {
//			
//			@Override
//			public void OnTicketDetail(boolean isSuccess, String errno, String errmsg,
//					TicketDetailItem item) {
//				// TODO Auto-generated method stub
//				Log.d("RequestJniTicket", "success:%s, errno:%s, errmsg:%s, item.title:%s, item.status:%s, item.contentList.length:%d"
//						, String.valueOf(isSuccess), errno, errmsg, item.title, item.status.name(), item.contentList.length);
//			}
//		});
		
//		RequestJniTicket.ReplyTicket("abc", "abc", "xxxx", new OnRequestCallback() {
//			
//			@Override
//			public void OnRequest(boolean isSuccess, String errno, String errmsg) {
//				// TODO Auto-generated method stub
//				Log.d("RequestJniTicket", "success:%s, errno:%s, errmsg:%s"
//						, String.valueOf(isSuccess), errno, errmsg);
//			}
//		});
		
//		RequestJniTicket.ResolvedTicket("abc", new OnRequestCallback() {
//			
//			@Override
//			public void OnRequest(boolean isSuccess, String errno, String errmsg) {
//				// TODO Auto-generated method stub
//				Log.d("RequestJniTicket", "success:%s, errno:%s, errmsg:%s"
//						, String.valueOf(isSuccess), errno, errmsg);
//			}
//		});
		
//		RequestJniTicket.AddTicket(RequestJniTicket.TicketTypeConsultation, "test", "test", "xxx", new OnRequestCallback() {
//			
//			@Override
//			public void OnRequest(boolean isSuccess, String errno, String errmsg) {
//				// TODO Auto-generated method stub
//				Log.d("RequestJniTicket", "success:%s, errno:%s, errmsg:%s"
//						, String.valueOf(isSuccess), errno, errmsg);
//			}
//		});
	}
}

