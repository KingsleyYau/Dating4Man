package com.qpidnetwork.dating.setting;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.qpidnetwork.dating.BaseActivity;
import com.qpidnetwork.dating.QpidApplication;
import com.qpidnetwork.dating.R;
import com.qpidnetwork.dating.WebViewActivity;
import com.qpidnetwork.dating.authorization.LoginManager;
import com.qpidnetwork.dating.bean.RequestBaseResponse;
import com.qpidnetwork.dating.contactus.ContactTicketListActivity;
import com.qpidnetwork.dating.setting.SettingPerfence.NotificationItem;
import com.qpidnetwork.manager.FileCacheManager;
import com.qpidnetwork.manager.WebSiteManager;
import com.qpidnetwork.request.OnOtherVersionCheckCallback;
import com.qpidnetwork.request.RequestJniOther;
import com.qpidnetwork.request.item.OtherVersionCheckItem;
import com.qpidnetwork.view.MaterialAppBar;
import com.qpidnetwork.view.MaterialDialogAlert;
import com.qpidnetwork.view.MaterialDialogSingleChoice;

public class SettingActivity extends BaseActivity {
	private enum RequestFlag {
		REQUEST_VERSIONCHECK_SUCCESS,
		REQUEST_FAIL,
	}
	
	public MaterialAppBar appbar;
	
	/**
	 * Notification
	 */
	private TextView textViewChatNotification;
	private TextView textViewMailNotification;
	private TextView textViewPushNotification;
	
	/**
	 * Application
	 */
	private ImageView imageViewCheckUpdate;
	private TextView textViewCurrentVersion;
	
	private MaterialDialogAlert mUpdateDialog;
	
	public OtherVersionCheckItem mOtherVersionCheckItem;
	private NotificationItem mNotificationItem;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		mUpdateDialog = new MaterialDialogAlert(this);
		
		// 创建界面时候，获取缓存数据
		mNotificationItem = SettingPerfence.GetNotificationItem(mContext);
		
		// 刷新界面
		ReloadData();
		
		// 检测版本
//		VersionCheck();
	}
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
	}
	
	/**
	 * 点击取消
	 * @param v
	 */
	public void onClickCancel(View v) {
		finish();
	}
	
	

	/**
	 * 点击ChatNotification
	 * @param v
	 */
	public void onClickChatNotification(View v) {
		
		MaterialDialogSingleChoice dialog = new MaterialDialogSingleChoice(this, getResources().getStringArray(R.array.notification), new MaterialDialogSingleChoice.OnClickCallback() {
			
			@Override
			public void onClick(AdapterView<?> adptView, View v, int which) {
				// TODO Auto-generated method stub
				if( which > -1 && which < SettingPerfence.Notification.values().length ) {
					mNotificationItem.mChatNotification = SettingPerfence.Notification.values()[which];
					SettingPerfence.SaveNotificationItem(mContext, mNotificationItem);
					ReloadData();
				}
			}
		}, mNotificationItem.mChatNotification.ordinal());

		dialog.setTitle(getString(R.string.Chat_Notification));
		dialog.show();
		
	}
	
	/**
	 * 点击MailNotification
	 * @param v
	 */
	public void onClickMailNotification(View v) {
		
		MaterialDialogSingleChoice dialog = new MaterialDialogSingleChoice(this, getResources().getStringArray(R.array.notification), new MaterialDialogSingleChoice.OnClickCallback() {
			
			@Override
			public void onClick(AdapterView<?> adptView, View v, int which) {
				// TODO Auto-generated method stub
				if( which > -1 && which < SettingPerfence.Notification.values().length ) {
					mNotificationItem.mMailNotification = SettingPerfence.Notification.values()[which];
					SettingPerfence.SaveNotificationItem(mContext, mNotificationItem);
					ReloadData();
				}
			}
		}, mNotificationItem.mMailNotification.ordinal());

		dialog.setTitle(getString(R.string.Mail_Notification));
		dialog.show();

	}
	
	/**
	 * 点击PushNotication
	 * @param v
	 */
	public void onClickPushNotification(View v) {
		
		MaterialDialogSingleChoice dialog = new MaterialDialogSingleChoice(this, getResources().getStringArray(R.array.notification), new MaterialDialogSingleChoice.OnClickCallback() {
			
			@Override
			public void onClick(AdapterView<?> adptView, View v, int which) {
				// TODO Auto-generated method stub
				if( which > -1 && which < SettingPerfence.Notification.values().length ) {
					mNotificationItem.mPushNotification = SettingPerfence.Notification.values()[which];
					SettingPerfence.SaveNotificationItem(mContext, mNotificationItem);
					ReloadData();
				}
			}
		}, mNotificationItem.mPushNotification.ordinal());

		dialog.setTitle(getString(R.string.Push_news_offers));
		dialog.show();
		

	}
	
	
	/**
	 * 点击CleanCache
	 * @param v
	 */
	public void onClickCache(View v) {
		MaterialDialogAlert dialog = new MaterialDialogAlert(mContext);
		dialog.setMessage(getString(R.string.myprofile_sure_clean_cache));
		dialog.addButton(dialog.createButton(getString(R.string.common_btn_ok), new OnClickListener(){
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				FileCacheManager.getInstance().ClearCache();
				
				MaterialDialogAlert alert = new MaterialDialogAlert(mContext);
				alert.setMessage("All cache has been clean!");
				alert.addButton(alert.createButton(getString(R.string.common_btn_ok), null));
				alert.show();
			}
		}));
		dialog.addButton(dialog.createButton(getString(R.string.common_btn_cancel), null));
		dialog.show();
	}
	
	/**
	 * 点击CheckUpdate
	 * @param v
	 */
	public void onClickCheckUpdate(View v) {
		VersionCheck();
	}	
	
	/**
	 * 点击Recommand
	 * @param v
	 */
	public void onClickRecommand(View v) {
		Intent sendIntent = new Intent();
		sendIntent.setAction(Intent.ACTION_SEND);
		sendIntent.putExtra(Intent.EXTRA_TEXT, getResources().getString(R.string.share_text));
		sendIntent.setType("text/plain");
		startActivity(sendIntent);
	}
	
	/**
	 * 点击Facebook
	 * @param v
	 */
	public void onClickFacebook(View v) {
		// 使用系統瀏覽器打開
		String link = WebSiteManager.newInstance(mContext).GetFacebookLink();
		Uri uri = Uri.parse(link);
		Intent intent = new Intent(Intent.ACTION_VIEW, uri);
		startActivity(intent);
	}
	
	/**
	 * 点击FAQ and Terms
	 * @param v
	 */
	public void onClickFAQTerms(View v){
		/*跳转到帮助FAQ页面*/
		String url = WebSiteManager.newInstance(mContext).GetWebSite().getHelpLink();
		Intent intent = WebViewActivity.getIntent(mContext, url);
		intent.putExtra(WebViewActivity.WEB_TITLE, "Help");
		startActivity(intent);
	}
	
	/**
	 * 点击Contact Us
	 * @param v
	 */
	public void onClickContactUs(View v){
		if(LoginManager.getInstance().CheckLogin(mContext)){
			Intent intent = new Intent(this, ContactTicketListActivity.class);
			startActivity(intent);
		}
	}
	
	@Override
	public void InitView() {
		setContentView(R.layout.activity_setting);
		appbar = (MaterialAppBar)findViewById(R.id.appbar);
		appbar.setTouchFeedback(MaterialAppBar.TOUCH_FEEDBACK_HOLO_LIGHT);
		appbar.addButtonToLeft(R.id.common_button_back, "", R.drawable.ic_arrow_back_grey600_24dp);
		appbar.setTitle(getString(R.string.Settings), getResources().getColor(R.color.text_color_dark));
		appbar.setOnButtonClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (v.getId() == R.id.common_button_back){
					finish();
				}
			}
			
		});
		
		/**
		 * Notification
		 */
		textViewChatNotification = (TextView) findViewById(R.id.textViewChatNotification);
		textViewMailNotification = (TextView) findViewById(R.id.textViewMailNotification);
		textViewPushNotification = (TextView) findViewById(R.id.textViewPushNotification);
	
		
		/**
		 * Application
		 */
		imageViewCheckUpdate = (ImageView) findViewById(R.id.imageViewCheckUpdate);
		imageViewCheckUpdate.setVisibility(View.INVISIBLE);
		textViewCurrentVersion = (TextView) findViewById(R.id.textViewCurrentVersion);
		
	}
	
	@Override
	public void InitHandler() {
		// TODO Auto-generated method stub
		mHandler = new Handler() {
			@Override
			public void handleMessage(android.os.Message msg) {
				// 收起菊花
				hideProgressDialog();
				RequestBaseResponse obj = (RequestBaseResponse) msg.obj;
				switch ( RequestFlag.values()[msg.what] ) {
				case REQUEST_VERSIONCHECK_SUCCESS:{
					// 版本检测成功
					mOtherVersionCheckItem = (OtherVersionCheckItem)obj.body;
					
					ReloadData();
					
					if( mOtherVersionCheckItem != null && mOtherVersionCheckItem.verCode > QpidApplication.versionCode ) {
						// 有更新
						mUpdateDialog.setTitle(mContext.getString(R.string.upgrade_title));
						mUpdateDialog.setMessage(mOtherVersionCheckItem.verDesc);
						mUpdateDialog.removeAllButton();
						mUpdateDialog.addButton(mUpdateDialog.createButton(getString(R.string.common_btn_go), new OnClickListener(){
							@Override
							public void onClick(View v) {
								// TODO Auto-generated method stub
								Uri uri = Uri.parse(mOtherVersionCheckItem.storeUrl);
								Intent intent = new Intent();
								intent.setAction(Intent.ACTION_VIEW);
								intent.setData(uri);
								startActivity(intent);
							}
						}));
						mUpdateDialog.addButton(mUpdateDialog.createButton(getString(R.string.common_btn_cancel), null));
						mUpdateDialog.show();
						
					} else {
						// 无更新
						MaterialDialogAlert alert = new MaterialDialogAlert(mContext);
						alert.setMessage("You are the latest version!");
						alert.addButton(alert.createButton(getString(R.string.common_btn_ok), null));
						alert.show();
					}
				}break;
				case REQUEST_FAIL:{
					// 请求失败
					Toast.makeText(mContext, obj.errmsg, Toast.LENGTH_LONG).show();	
				}break;
				default:
					break;
				}
			};
		};
	}
	
	/**
	 * 版本检测
	 */
	private void VersionCheck() {
		// 此处应有菊花
		showProgressDialog("Loading...");
		RequestJniOther.VersionCheck(1, new OnOtherVersionCheckCallback() {
			@Override
			public void OnOtherVersionCheck(boolean isSuccess, String errno,
					String errmsg, OtherVersionCheckItem item) {
				// TODO Auto-generated method stub
				Message msg = Message.obtain();
				RequestBaseResponse response = new RequestBaseResponse(isSuccess, errno, errmsg, item);
				if( isSuccess ) {
					// 版本检测成功
					msg.what = RequestFlag.REQUEST_VERSIONCHECK_SUCCESS.ordinal();
				} else {
					// 失败
					msg.what = RequestFlag.REQUEST_FAIL.ordinal();
				}
				msg.obj = response;
				mHandler.sendMessage(msg);
			}
		});
	}
	
	/**
	 * 刷新界面
	 */
	private void ReloadData() {
		// 刷新Notification配置
		if( mNotificationItem != null ) {
			textViewChatNotification.setText(getResources().getStringArray(R.array.notification)[mNotificationItem.mChatNotification.ordinal()]);
			textViewMailNotification.setText(getResources().getStringArray(R.array.notification)[mNotificationItem.mMailNotification.ordinal()]);
			textViewPushNotification.setText(getResources().getStringArray(R.array.notification)[mNotificationItem.mPushNotification.ordinal()]);
		}
		
		// 刷新版本信息
		if( mOtherVersionCheckItem != null && mOtherVersionCheckItem.verCode > QpidApplication.versionCode ) {
			imageViewCheckUpdate.setVisibility(View.VISIBLE);
		} else {
			imageViewCheckUpdate.setVisibility(View.INVISIBLE);
		}
		
		// 版本号
		PackageManager pm = mContext.getPackageManager();
		PackageInfo info = null;
		try {
			info = pm.getPackageInfo(mContext.getPackageName(), 0);
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		String format = getResources().getString(R.string.Current_Version);
		if( info != null ) {
			textViewCurrentVersion.setText(String.format(format, info.versionName));
		}
	}
}
