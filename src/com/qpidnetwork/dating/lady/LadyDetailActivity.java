package com.qpidnetwork.dating.lady;

import java.util.HashMap;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Message;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.HttpAuthHandler;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.qpidnetwork.dating.QpidApplication;
import com.qpidnetwork.dating.R;
import com.qpidnetwork.dating.advertisement.AdvertisementManager;
import com.qpidnetwork.dating.authorization.LoginManager;
import com.qpidnetwork.dating.authorization.LoginManager.LoginStatus;
import com.qpidnetwork.dating.authorization.LoginParam;
import com.qpidnetwork.dating.authorization.LoginPerfence;
import com.qpidnetwork.dating.bean.RequestBaseResponse;
import com.qpidnetwork.dating.contacts.ContactManager;
import com.qpidnetwork.dating.home.AppUrlHandler;
import com.qpidnetwork.dating.lady.LadyDetailManager.OnLadyDetailManagerQueryLadyDetailCallback;
import com.qpidnetwork.dating.lovecall.DirectCallManager;
import com.qpidnetwork.dating.lovecall.ScheduleCallActivity;
import com.qpidnetwork.framework.base.BaseFragmentActivity;
import com.qpidnetwork.framework.util.Log;
import com.qpidnetwork.framework.util.SystemUtil;
import com.qpidnetwork.manager.WebSiteManager;
import com.qpidnetwork.request.OnQueryLadyCallCallback;
import com.qpidnetwork.request.OnRequestCallback;
import com.qpidnetwork.request.RequestJni;
import com.qpidnetwork.request.RequestOperator;
import com.qpidnetwork.request.item.LadyCall;
import com.qpidnetwork.request.item.LadyDetail;
import com.qpidnetwork.view.ButtonRaised;
import com.qpidnetwork.view.GetMoreCreditDialog;
import com.qpidnetwork.view.MaterialAppBar;
import com.qpidnetwork.view.MaterialDialogAlert;
import com.qpidnetwork.view.MaterialThreeButtonDialog;

@SuppressLint({ "SetJavaScriptEnabled", "RtlHardcoded" })
public class LadyDetailActivity extends BaseFragmentActivity {
	/**
	 * 常用重定向Url
	 */
	public static final String URL_OPEN_PHOTO_VIEW = "qpidnetwork://app/womanphoto";
	public static final String URL_OPEN_VIDEO_VIEW = "qpidnetwork://app/womanvideo";
	public static final String URL_MAKE_CALL_VIEW = "qpidnetwork://app/makecall";
	public static final String URL_ADD_OR_REMOVE_FAVORITE = "qpidnetwork://app/favorite";
	public static final String URL_OPEN_APP_MODULE = "qpidnetwork://app/open";
	
	/**
	 * 其他界面进入参数
	 */
	public static final String WOMANID = "womanId";
	public static final String SHOW_BUTTONS = "SHOW_BUTTONS";
	
	//解决Loadurl空指针异常
	private boolean isWebViewDestroy = false;
	
	/**
	 * 所有打开ladydetail页面统一入口
	 * @param context
	 * @param womanId
	 */
	public static void launchLadyDetailActivity(Context context, String womanId, boolean showButtons){
		if(LoginManager.getInstance().GetLoginStatus() == LoginStatus.LOGINED){
			/*登陆成功，需要判断风控条件，如果女士详情被风控则不可以查看*/
			LoginParam loginParam = LoginManager.getInstance().GetLoginParam();
			if(loginParam.item.ladyprofile){
				MaterialDialogAlert dialog = new MaterialDialogAlert(context);
				dialog.setMessage(context.getString(R.string.common_risk_control_notify));
				dialog.addButton(dialog.createButton(context.getString(R.string.common_btn_ok), null));
				dialog.show();
				return;
			}
		}
		Intent intent = new Intent(context, LadyDetailActivity.class);
		intent.putExtra(WOMANID, womanId);
		if(!showButtons){
			intent.putExtra(LadyDetailActivity.SHOW_BUTTONS, false);
		}
		context.startActivity(intent);
	}	
	
	/**
	 * 接口消息
	 *
	 */
	private enum RequestFlag {
		REQUEST_DETAIL_SUCCESS,
		REQUEST_PHOTO_SUCCESS,
		REQUEST_VIDEO_SUCCESS,
		REQUEST_FAIL,
		REQUEST_ADD_FAVOUR_SUCCESS,
		REQUEST_ADD_FAVOUR_FAIL,
		REQUEST_REMOVE_FAVOUR_SUCCESS,
		REQUEST_REMOVE_FAVOUR_FAIL,
		REQUEST_GET_LOVE_CALL_SUCCESS,
		REQUEST_GET_LOVE_CALL_FAIL,
		REQUEST_WEBVIEW_START,
		REQUEST_WEBVIEW_FINISH,
	}
	
	
	/**
	 * 界面控件
	 */
	private MaterialAppBar appbar;
	private WebView mWebView;
	private WebViewClient mWebViewClient;
	public LadyDetail item;
	
	/**
	 * 女士Id
	 */
	public String mWomanId = "";
	public boolean mShowButtons = true;
	
	//error page
	private View errorPage;
	private ButtonRaised btnErrorRetry;
	private boolean isLoadError = false;
	
	private String mVideoId = "";//当前点击选中VideoId
	
	/**
	 * Javascript调用java方法交互类
	 * @see 暂时没用
	 */
//    private class JavascriptInterface {  
//  
//        private Context mContext;  
//  
//        public JavascriptInterface(Context context) {  
//            mContext = context; 
//        }
//    }  
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		if (this.getIntent().getExtras().containsKey(SHOW_BUTTONS)){
			mShowButtons = this.getIntent().getExtras().getBoolean(SHOW_BUTTONS);
		}
		
		mWomanId = getIntent().getExtras().getString(WOMANID);
		
		// 刷新界面数据
		ReloadData();
		
		// 弹出广告
		AdvertisementManager advertManager = AdvertisementManager.getInstance();
		if (null != advertManager) {
			advertManager.showMainAdvert(this);
		}
	}
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		if(LadyDetailManager.getInstance().getLadyDetailReloadFlag()){
			//界面返回需要刷新
			ReloadData();
		}
	}
	
	@Override
	public void InitView() {
		// TODO Auto-generated method stub
		setContentView(R.layout.activity_webview);
		
		// 获取女士Id
		mWomanId = getIntent().getExtras().getString(WOMANID);
		if (this.getIntent().getExtras().containsKey(SHOW_BUTTONS)){
			mShowButtons = this.getIntent().getExtras().getBoolean(SHOW_BUTTONS);
		}
		
		// 导航栏
		appbar = (MaterialAppBar)findViewById(R.id.appbar);
		appbar.setAppbarBackgroundColor(getResources().getColor(R.color.theme_actionbar_secoundary));
		appbar.setTouchFeedback(MaterialAppBar.TOUCH_FEEDBACK_HOLO_LIGHT);
		appbar.addButtonToLeft(R.id.common_button_back, "", R.drawable.ic_arrow_back_grey600_24dp);
		appbar.addButtonToRight(R.id.common_button_favorite, "", R.drawable.ic_more_vert_grey600_24dp);
		appbar.getButtonById(R.id.common_button_favorite).setVisibility(View.GONE);

		appbar.setOnButtonClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				switch (v.getId()) {
				case R.id.common_button_back:{
					finish();
				}break;
				case R.id.common_button_favorite:{
					//点击收藏或取消收藏
					if(CheckLogin()){
						if(item != null){
							if(item.isfavorite){
								RemoveFavour();
							}else{
								AddFavour();
							}
						}
					}
				}break;
				default:break;
				}
			}
		});
		
		//error page
		errorPage = (View)findViewById(R.id.errorPage);
		btnErrorRetry = (ButtonRaised)findViewById(R.id.btnErrorRetry);
		btnErrorRetry.setButtonTitle(getString(R.string.common_btn_tapRetry));
		btnErrorRetry.setOnClickListener(this);
		btnErrorRetry.requestFocus();
		
		// 浏览器控件
		mWebView = (WebView) findViewById(R.id.webView);
		mWebView.getSettings().setJavaScriptEnabled(true);
		mWebViewClient = new WebViewClient() {  
			@Override
			public void onPageStarted(WebView view, String url, Bitmap favicon) {
				// TODO Auto-generated method stub
				super.onPageStarted(view, url, favicon);
				Message msg = Message.obtain();
				RequestBaseResponse obj = new RequestBaseResponse(true, "", "", null);
				msg.what = RequestFlag.REQUEST_WEBVIEW_START.ordinal();
				msg.obj = obj;
				sendUiMessage(msg);
			}
			
			@Override
			public void onPageFinished(WebView view, String url) {
				// TODO Auto-generated method stub
				super.onPageFinished(view, url);
				if((!isLoadError)){
					errorPage.setVisibility(View.GONE);
				}
				Message msg = Message.obtain();
				RequestBaseResponse obj = new RequestBaseResponse(true, "", "", null);
				msg.what = RequestFlag.REQUEST_WEBVIEW_FINISH.ordinal();
				msg.obj = obj;
				sendUiMessage(msg);
			}
			
		    @Override  
		    public boolean shouldOverrideUrlLoading(WebView view, String url) {
		    	Log.d("LadyDetailActivity", "shouldOverrideUrlLoading : %s",  url);
		    	boolean bFlag = false;
		    	bFlag = StartActivityByUrl(url);
		    	
		    	if( !bFlag ) {
		    		view.loadUrl(url);
		    	}
		    	
		        return true;  
		    } 
		    
			@Override  
		    public void onReceivedHttpAuthRequest(WebView view,
		            HttpAuthHandler handler, String host, String realm) {
				if (QpidApplication.isDemo) {
					handler.proceed("test", "5179");
				} else {
			        handler.cancel();
				}
		    }
			
			@Override
			public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
				//普通页面错误
				isLoadError = true;
				errorPage.setVisibility(View.VISIBLE);
			};
		};
		
		mWebView.setWebViewClient(mWebViewClient);

	}
	
	@Override
	public void onClick(View v){
		super.onClick(v);
		switch (v.getId()) {
		case R.id.btnErrorRetry:{
//			errorPage.setVisibility(View.GONE);
			isLoadError = false;
			mWebView.reload();
		}break;

		default:
			break;
		}
	}
	
	@Override
	protected void handleUiMessage(Message msg) {
		// TODO Auto-generated method stub
		super.handleUiMessage(msg);
		// 收起菊花
		hideProgressDialog();
		// 处理消息
		RequestBaseResponse obj = (RequestBaseResponse) msg.obj;
		if((obj.body != null) && (obj.body instanceof LadyDetail)){
			LadyDetail ladyDetail = (LadyDetail)obj.body;
			appbar.getButtonById(R.id.common_button_favorite).setVisibility(View.VISIBLE);
			if(ladyDetail.isfavorite){
				appbar.setButtonIconById(R.id.common_button_favorite, R.drawable.ic_favorite_remove_yellow_24dp);
			}else{
				appbar.setButtonIconById(R.id.common_button_favorite, R.drawable.ic_favorite_add_yellow_24dp);
			}
			String title = String.format(getResources().getString(R.string.lady_profile_title), ladyDetail.firstname);
			appbar.setTitle(title, getResources().getColor(R.color.text_color_dark));
			ReloadFavorite(ladyDetail.isfavorite);
		}
		
		switch ( RequestFlag.values()[msg.what] ) {
		case REQUEST_DETAIL_SUCCESS:{
			item = (LadyDetail)obj.body;
		}break;
		case REQUEST_PHOTO_SUCCESS:{
			// 判断是否登录
			if( CheckLogin() ) {
				// 获取图片成功
				LadyDetail photoLadyDetail = (LadyDetail)obj.body;
				// 打开预览图片
				NormalPhotoPreviewActivity.launchNoramlPhotoActivity(mContext, photoLadyDetail, msg.arg1);
			}
		}break;
		case REQUEST_VIDEO_SUCCESS:{
			// 判断是否登录
			if( CheckLogin() ) {
				// 打开预览视频
				LadyDetail videoLadyDetail = (LadyDetail)obj.body;
				if(videoLadyDetail != null){
					VideoDetailActivity.launchLadyVideoDetailActivity(mContext, videoLadyDetail.womanid, videoLadyDetail.firstname, mVideoId);
				}
			}
		}break;
		case REQUEST_FAIL:{
			// 请求失败
			Toast.makeText(mContext, obj.errmsg, Toast.LENGTH_LONG).show();	
		}break;
		case REQUEST_ADD_FAVOUR_SUCCESS:{
			showToastDone("Added!");
			if( item != null ) {
				item.isfavorite = true;
				ReloadFavorite(item.isfavorite);
				appbar.setButtonIconById(R.id.common_button_favorite, R.drawable.ic_favorite_remove_yellow_24dp);
			}
			/*添加favorite成功，添加到现有联系人或更新联系人*/
			ContactManager.getInstance().updateBySendEMF(item);
		}break;
		case REQUEST_ADD_FAVOUR_FAIL:{
			// 收藏失败
			cancelToastImmediately();
			Toast.makeText(mContext, obj.errmsg, Toast.LENGTH_LONG).show();	
		}break;
		case REQUEST_REMOVE_FAVOUR_SUCCESS:{
			showToastDone("Removed!");
			if( item != null ) {
				item.isfavorite = false;
				ReloadFavorite(item.isfavorite);
				appbar.setButtonIconById(R.id.common_button_favorite, R.drawable.ic_favorite_add_yellow_24dp);
			}
			ContactManager.getInstance().updateFavoriteStatus(item.womanid, false);
		}break;
		case REQUEST_REMOVE_FAVOUR_FAIL:{
			// 删除收藏失败
			cancelToastImmediately();
			Toast.makeText(mContext, obj.errmsg, Toast.LENGTH_LONG).show();	
		}break;
		case REQUEST_GET_LOVE_CALL_SUCCESS:{
			// 请求lovecall成功
			showToastDone("Finish!");
			LadyCall ladyCall = (LadyCall)obj.body;
			if(ladyCall != null){
				makeCall(ladyCall.lc_centernumber, ladyCall.lovecallid);
			}
		}break;
		case REQUEST_GET_LOVE_CALL_FAIL:{
			// 请求lovecall失败
			//showToastFailed("Fail!");
			cancelToastImmediately();
			MaterialDialogAlert dialog = new MaterialDialogAlert(mContext);
			dialog.setMessage(obj.errmsg);
			
			if (!obj.errno.equals("MBCE61005")){   //RequestErrorCode 裏面沒有這個錯誤代碼.
				String ladyName = "The lady";
				if(item != null){
					ladyName = item.firstname;
				}
				dialog.setMessage(String.format(mContext.getResources().getString(R.string.lovecall_mail_schedule_makecall_error_tips), ladyName));
				dialog.addButton(dialog.createButton(mContext.getString(R.string.common_btn_ok), new OnClickListener(){
					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						LadyDetail ladyDetail = new LadyDetail();
						if(item != null){
							ladyDetail = item;
						}else{
							ladyDetail.womanid = mWomanId;
						}
						ScheduleCallActivity.launchScheduleCallActivity(mContext, ladyDetail);
					}
				}));
				
				dialog.addButton(dialog.createButton(getString(R.string.common_btn_cancel), null));
				if(isActivityVisible()){
					dialog.show();
				}
				return;
			}
			
			dialog.addButton(dialog.createButton(getString(R.string.common_btn_add_credit), new OnClickListener(){

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					GetMoreCreditDialog dialog = new GetMoreCreditDialog(mContext, R.style.ChoosePhotoDialog);
					dialog.show();
				}
				
			}));
			
			dialog.addButton(dialog.createButton(getString(R.string.common_btn_cancel), null));
			if(isActivityVisible()){
				dialog.show();
			}
		}break;
		case REQUEST_WEBVIEW_START:{
			showProgressDialog("Loading...");
		}break;
		case REQUEST_WEBVIEW_FINISH:{
			hideProgressDialog();
			if( item != null ) {
				ReloadFavorite(item.isfavorite);
			}
		}break;
		default:break;
		}

	}
	
	/**
	 * 刷新界面数据
	 */
	public void ReloadData() {
		LadyDetailManager.getInstance().updateLadyDetailReloadFlag(false);
		LadyDetailManager.getInstance().RemoveLadyDetailCache(mWomanId);
		LadyDetailManager.getInstance().QueryLadyDetail(mWomanId, new OnLadyDetailManagerQueryLadyDetailCallback() {
			
			@Override
			public void OnQueryLadyDetailCallback(boolean isSuccess, String errno,
					String errmsg, LadyDetail item) {
				// TODO Auto-generated method stub
				Message msg = Message.obtain();
				RequestBaseResponse obj = new RequestBaseResponse(isSuccess, errno, errmsg, null);
				if( isSuccess ) {
					// 获取个人信息成功
					msg.what = RequestFlag.REQUEST_DETAIL_SUCCESS.ordinal();
					obj.body = item;
				} else {
					// 获取个人信息失败
					msg.what = RequestFlag.REQUEST_FAIL.ordinal();
				}
				msg.obj = obj;
				sendUiMessage(msg);
			}
		});
		
		// 域名
		String domain = WebSiteManager.getInstance().GetWebSite().getAppSiteHost();
		// Cookie 认证
		/*getInstance 前必须createInstance */
		CookieSyncManager.createInstance(this);
		CookieManager cookieManager = CookieManager.getInstance();
		cookieManager.setAcceptCookie(true);
		String phpSession = RequestJni.GetCookies(domain.substring(domain.indexOf("http://") + 7, domain.length()));
		if(!TextUtils.isEmpty(phpSession)){
			cookieManager.setCookie(domain, phpSession);
		}else{
			cookieManager.removeAllCookie();
		}
		CookieSyncManager.getInstance().sync();
		
		String url = domain;
		url += "/member/lady_profile/womanid/";
		url += mWomanId;
		url += "/versioncode/";
		url += QpidApplication.versionCode;
		url += "/showButton/";
		url += mShowButtons?1:0;
		mWebView.loadUrl(url);
	}
	
	/**
	 * 重定向Url统一处理
	 * @param url
	 * @return
	 */
	private boolean StartActivityByUrl(String url) {
		boolean bFlag = false;
		if(!TextUtils.isEmpty(url)){
			if(url.contains(URL_OPEN_PHOTO_VIEW)
					|| url.contains(URL_OPEN_VIDEO_VIEW)
					|| url.contains(URL_MAKE_CALL_VIEW)
					|| url.contains(URL_ADD_OR_REMOVE_FAVORITE)){
				//点击功能响应
				if(CheckLogin()){
					onPageClickListen(url);
				}
				// 标记已经处理
				bFlag = true;
			}else if(url.contains(URL_OPEN_APP_MODULE)){
				//打开系统标准模块
				AppUrlHandler.AppUrlHandle(this, url);
				// 标记已经处理
				bFlag = true;
			}
		}
		return bFlag;
	}
	
	/**
	 * 重定向普通点击响应处理
	 * @param url
	 */
	private void onPageClickListen(String url){
		HashMap<String, String> args = parseUrlKeyValue(url);
		if(args != null){
			String womanId = "";
			if(args.containsKey("womanid")){
				womanId = args.get("womanid");
			}
			if(url.contains(URL_OPEN_PHOTO_VIEW)){
				//点击打开照片列表
				int photoIndex = 0;
				if(args.containsKey("photoindex")){
					photoIndex = Integer.valueOf(args.get("photoindex"));
				}
				onPhotoClick(womanId, photoIndex);
			}else if(url.contains(URL_OPEN_VIDEO_VIEW)){
				//点击打开Video列表预览
				mVideoId = "";
				if(args.containsKey("videoid")){
					mVideoId = args.get("videoid");
				}
				onVideoClick(womanId);
			}else if(url.contains(URL_MAKE_CALL_VIEW)){
				//点击拨打电话
				QueryLadyCall(womanId);
			}else if(url.contains(URL_ADD_OR_REMOVE_FAVORITE)){
				//点击添加或删除Favorite
				int operate = -1;
				if(args.containsKey("operate")){
					operate = Integer.valueOf(args.get("operate"));
				}
				if(operate == 0){
					AddFavour();
				}else if(operate == 1){
					RemoveFavour();
				}
			}
		}
	}
	
	/**
	 * 点击图片响应
	 * @param womanId
	 * @param photoIndex
	 */
	private void onPhotoClick(String womanId, final int photoIndex){
		// 点击图片
		showProgressDialog("Loading...");
		LadyDetailManager.getInstance().RemoveLadyDetailCache(womanId);
		LadyDetailManager.getInstance().QueryLadyDetail(womanId, new OnLadyDetailManagerQueryLadyDetailCallback() {
			
			@Override
			public void OnQueryLadyDetailCallback(boolean isSuccess, String errno,
					String errmsg, LadyDetail item) {
				// TODO Auto-generated method stub
				Message msg = Message.obtain();
				RequestBaseResponse obj = new RequestBaseResponse(isSuccess, errno, errmsg, null);
				if( isSuccess ) {
					// 获取个人信息成功
					msg.what = RequestFlag.REQUEST_PHOTO_SUCCESS.ordinal();
					obj.body = item;
				} else {
					// 获取个人信息失败
					msg.what = RequestFlag.REQUEST_FAIL.ordinal();
				}
				msg.obj = obj;
				msg.arg1 = photoIndex;
				sendUiMessage(msg);
			}
		});
	}
	
	/**
	 * 点击video响应
	 * @param womanId
	 */
	private void onVideoClick(String womanId){
		showProgressDialog("Loading...");
		LadyDetailManager.getInstance().QueryLadyDetail(womanId, new OnLadyDetailManagerQueryLadyDetailCallback() {
			
			@Override
			public void OnQueryLadyDetailCallback(boolean isSuccess, String errno,
					String errmsg, LadyDetail item) {
				// TODO Auto-generated method stub
				Message msg = Message.obtain();
				RequestBaseResponse obj = new RequestBaseResponse(isSuccess, errno, errmsg, null);
				if( isSuccess ) {
					// 获取个人信息成功
					msg.what = RequestFlag.REQUEST_VIDEO_SUCCESS.ordinal();
					obj.body = item;
				} else {
					// 获取个人信息失败
					msg.what = RequestFlag.REQUEST_FAIL.ordinal();
				}
				msg.obj = obj;
				sendUiMessage(msg);
			}
		});
	}
	
	/**
	 * 解析Url中参数
	 * @param url
	 * @return
	 */
	private HashMap<String, String> parseUrlKeyValue(String url){
		HashMap<String, String> argMap = new HashMap<String, String>();
		if(!TextUtils.isEmpty(url)){
			if(url.contains("?")){
				String[] result = url.split("\\?");
				if(result != null && result.length > 1){
					String[] params = result[1].split("&");
					if(params != null){
						for(String param : params){
							String[] keyValue = param.split("=");
							if(keyValue != null && keyValue.length > 1){
								argMap.put(keyValue[0], keyValue[1]);
							}
						}
					}
				}
			}
		}
		return argMap;
	}
	
	/**
	 * 重新加载favorite状态
	 * @param isFavour
	 */
	public void ReloadFavorite(boolean isFavour) {

		String url = "javascript:js_update_favorite(";
		url += "'" +  isFavour + "')";
		if(!isWebViewDestroy){
			mWebView.loadUrl(url);
		}
	}
	
	/**
	 * 判断是否登录
	 */
	public boolean CheckLogin() {
		return LoginManager.getInstance().CheckLogin(mContext);
	}
	
	/**
	 * 请求收藏女士
	 */
	public void AddFavour() {
		if( !CheckLogin() ) {
			return;
		}
		showToastProgressing("Adding");
		LadyDetailManager.getInstance().AddFavour(mWomanId, new OnRequestCallback() {
			@Override
			public void OnRequest(boolean isSuccess, String errno, String errmsg) {
				// TODO Auto-generated method stub
				Message msg = Message.obtain();
				RequestBaseResponse obj = new RequestBaseResponse(isSuccess, errno, errmsg, null);
				if( isSuccess ) {
					// 获取个人信息成功
					msg.what = RequestFlag.REQUEST_ADD_FAVOUR_SUCCESS.ordinal();
				} else {
					// 获取个人信息失败
					msg.what = RequestFlag.REQUEST_ADD_FAVOUR_FAIL.ordinal();
				}
				msg.obj = obj;
				sendUiMessage(msg);
			}
		});
	}
	
	public void RemoveFavour() {
		if( !CheckLogin() ) {
			return;
		}
		showToastProgressing("Removing");
		LadyDetailManager.getInstance().RemoveFavour(mWomanId, new OnRequestCallback() {
			@Override
			public void OnRequest(boolean isSuccess, String errno, String errmsg) {
				// TODO Auto-generated method stub
				Message msg = Message.obtain();
				RequestBaseResponse obj = new RequestBaseResponse(isSuccess, errno, errmsg, null);
				if( isSuccess ) {
					// 获取个人信息成功
					msg.what = RequestFlag.REQUEST_REMOVE_FAVOUR_SUCCESS.ordinal();
				} else {
					// 获取个人信息失败
					msg.what = RequestFlag.REQUEST_REMOVE_FAVOUR_FAIL.ordinal();
				}
				msg.obj = obj;
				sendUiMessage(msg);
			}
		});
	}
	
	/**
	 * 请求女士lovecall
	 * @param womanId
	 */
	public void QueryLadyCall(String womanId) {
		if( !CheckLogin() ) {
			return;
		}
		showToastProgressing("Calling");
		RequestOperator.getInstance().QueryLadyCall(womanId, new OnQueryLadyCallCallback() {
			
			@Override
			public void OnQueryLadyCall(boolean isSuccess, String errno, String errmsg,
					LadyCall item) {
				// TODO Auto-generated method stub
				Message msg = Message.obtain();
				RequestBaseResponse obj = new RequestBaseResponse(isSuccess, errno, errmsg, null);
				if( isSuccess ) {
					// 获取个人信息成功
					msg.what = RequestFlag.REQUEST_GET_LOVE_CALL_SUCCESS.ordinal();
					obj.body = item;
				} else {
					// 获取个人信息失败
					msg.what = RequestFlag.REQUEST_GET_LOVE_CALL_FAIL.ordinal();
				}
				msg.obj = obj;
				sendUiMessage(msg);
			}
		});
	}
	
	private void makeCall(final String callcenterNumber, final String callId) {
		/* 检测有无Sim卡 */
		if (SystemUtil.isSimCanUse(mContext)) {
			/*资费提示*/
			
			if (LoginPerfence.GetStringPreference(mContext, "donnot_show_love_call_fee").equals("true")){
				new DirectCallManager(mContext).makeCall(callcenterNumber, callId);
				return;
			}
			
			MaterialThreeButtonDialog dialog = new MaterialThreeButtonDialog(mContext, new MaterialThreeButtonDialog.OnClickCallback() {
				
				@Override
				public void OnSecondButtonClick(View v) {
					// TODO Auto-generated method stub
					new DirectCallManager(mContext).makeCall(callcenterNumber, callId);
					LoginPerfence.SaveStringPreference(mContext, "donnot_show_love_call_fee", "true");
				}
				
				@Override
				public void OnFirstButtonClick(View v) {
					// TODO Auto-generated method stub
					new DirectCallManager(mContext).makeCall(callcenterNumber, callId);
				}
				
				@Override
				public void OnCancelButtonClick(View v) {
					// TODO Auto-generated method stub
					
				}
			});
			
			dialog.hideImageView();
			dialog.setTitle(mContext.getString(R.string.lovecall_terms_title));
			dialog.setMessage(mContext.getString(R.string.lovecall_terms_detail));
			dialog.setFirstButtonText(mContext.getString(R.string.lovecall_call_now));
			dialog.setSecondButtonText(mContext.getString(R.string.love_call_dont_tell_again));
			dialog.getMessage().setGravity(Gravity.LEFT);
			dialog.getTitle().setGravity(Gravity.LEFT);
			dialog.show();

			
		} else {
			MaterialDialogAlert dialog = new MaterialDialogAlert(mContext);
			dialog.setTitle(mContext.getString(R.string.lovecall_no_sim_tips));
			dialog.setMessage(mContext.getString(R.string.lovecall_instruction, callcenterNumber));
			dialog.addButton(dialog.createButton(mContext.getString(R.string.common_btn_ok), null));
			dialog.show();
		}
	}
}
