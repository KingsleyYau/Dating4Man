package com.qpidnetwork.dating;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.HttpAuthHandler;
import android.webkit.WebView;

import com.qpidnetwork.dating.R.color;
import com.qpidnetwork.dating.authorization.LoginManager;
import com.qpidnetwork.dating.authorization.LoginManager.OnLoginManagerCallback;
import com.qpidnetwork.dating.bean.RequestBaseResponse;
import com.qpidnetwork.framework.base.BaseCustomWebViewClient;
import com.qpidnetwork.framework.base.BaseFragmentActivity;
import com.qpidnetwork.manager.WebSiteManager;
import com.qpidnetwork.request.RequestJni;
import com.qpidnetwork.request.item.CookiesItem;
import com.qpidnetwork.request.item.LoginErrorItem;
import com.qpidnetwork.request.item.LoginItem;
import com.qpidnetwork.view.ButtonRaised;
import com.qpidnetwork.view.MaterialAppBar;

@SuppressWarnings("deprecation")
public class WebViewActivity extends BaseFragmentActivity implements OnLoginManagerCallback {
	
	private static final int LOGIN_CALLBACK = 10001;
	
	public static final String WEB_URL = "web_url";
	public static final String WEB_TITLE = "web_title";
	
	private WebView mWebView;
	private String mUrl = "";
	private String mTitle = "";
	private MaterialAppBar appbar;
	
	//error page
	private View errorPage;
	private ButtonRaised btnErrorRetry;
	
	private boolean isSessionOutTimeError = false;
	private boolean isLoadError = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// 创建界面时候读取数据
		Bundle bundle = getIntent().getExtras();
		if(bundle != null){
			if(bundle.containsKey(WEB_URL)){
				mUrl = bundle.getString(WEB_URL);
			}
			
			if(bundle.containsKey(WEB_TITLE)){
				mTitle = bundle.getString(WEB_TITLE);
			}
		}
//		mUrl = "http://demo-mobile.chnlove.com/member/bonus_points";
		if( mUrl != null && mUrl.length() > 0 ) {
			mWebView.loadUrl(mUrl);
		}
		
		if( mTitle != null && mTitle.length() > 0 ) {
			appbar.setTitle(mTitle, getResources().getColor(color.text_color_dark));
		}

		LoginManager.getInstance().AddListenner(this);
	}
	
	@Override
	protected void onDestroy() {
		LoginManager.getInstance().RemoveListenner(this);
		super.onDestroy();
	}
	
	@Override
	public void InitView() {
		setContentView(R.layout.activity_webview);
		mWebView = (WebView) findViewById(R.id.webView);

		// 域名
		String domain = WebSiteManager.getInstance().GetWebSite().getAppSiteHost();
		// Cookie 认证
		CookieSyncManager.createInstance(this);
		CookieManager cookieManager = CookieManager.getInstance();
		cookieManager.setAcceptCookie(true);
		cookieManager.removeSessionCookie();
		CookiesItem[] cookieList = RequestJni.GetCookiesItem();
		if(cookieList != null && cookieList.length > 0){
			for(CookiesItem item : cookieList){
				if(item != null){
					String sessionString = item.cName + "=" + item.value;
					cookieManager.setCookie(item.domain, sessionString);	
				}
			}
		}
//		String phpSession = RequestJni.GetCookies(domain.substring(domain.indexOf("http://") + 7, domain.length()));
//		cookieManager.setCookie(domain, phpSession);
		CookieSyncManager.getInstance().sync();
		
		mWebView.setWebViewClient(new BaseCustomWebViewClient(this) { 
			@Override
			public void onPageStarted(WebView view, String url, Bitmap favicon) {
				// TODO Auto-generated method stub
				super.onPageStarted(view, url, favicon);
				showProgressDialog("Loading...");
			}
			
			@Override
			public void onPageFinished(WebView view, String url) {
				// TODO Auto-generated method stub
				super.onPageFinished(view, url);
				if((!isLoadError)&&(!isSessionOutTimeError)){
					errorPage.setVisibility(View.GONE);
				}
				hideProgressDialogIgnoreCount();
			}
			
		    @Override  
		    public boolean shouldOverrideUrlLoading(WebView view, String url) { 
		    	if( url.contains("MBCE0003")) {
		    		//处理session过期重新登陆
					isSessionOutTimeError = true;
					errorPage.setVisibility(View.VISIBLE);
		    	} else {
			    	return super.shouldOverrideUrlLoading(view, url); 
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
		}); 
		
		appbar = (MaterialAppBar)findViewById(R.id.appbar);
		appbar.setAppbarBackgroundColor(getResources().getColor(R.color.theme_actionbar_secoundary));
		appbar.setTouchFeedback(MaterialAppBar.TOUCH_FEEDBACK_HOLO_LIGHT);
		appbar.addButtonToLeft(R.id.common_button_back, "", R.drawable.ic_close_grey600_24dp);
		appbar.setOnButtonClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				switch (v.getId()) {
				case R.id.common_button_back:{
					// 点击取消，直接关闭当前界面，防止部分重定向页面无法关闭
//					if( mWebView.canGoBack() ) {
//						mWebView.goBack();
//					} else {
						finish();
//					}
				}break;
				}
			}
		});
		
		//error page
		errorPage = (View)findViewById(R.id.errorPage);
		btnErrorRetry = (ButtonRaised)findViewById(R.id.btnErrorRetry);
		btnErrorRetry.setButtonTitle(getString(R.string.common_btn_tapRetry));
		btnErrorRetry.setOnClickListener(this);
		btnErrorRetry.requestFocus();

	}
	
	@Override
	public void onClick(View v) {
		super.onClick(v);
		switch (v.getId()) {
		case R.id.btnErrorRetry:{
			if(isSessionOutTimeError){
				showProgressDialog("Loading...");
				LoginManager.getInstance().Logout();
				LoginManager.getInstance().AutoLogin();
			}else{
				isLoadError = false;
				mWebView.reload();
			}
		}break;

		default:
			break;
		}
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		if ( keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN ) {
			if( mWebView.canGoBack() ) {
				mWebView.goBack();
			} else {
				finish();
			}
			return false;
		} else {
			
		}
		return super.onKeyDown(keyCode, event);
	}
	
	@Override
	protected void handleUiMessage(Message msg) {
		super.handleUiMessage(msg);
		switch (msg.what) {
		case LOGIN_CALLBACK:{
			RequestBaseResponse response = (RequestBaseResponse)msg.obj;
			if(response.isSuccess){
				//session 过期重新登陆
				isSessionOutTimeError = false;
				reloadDestUrl();
			}else{
				//显示错误页（加载页面错误）
				hideProgressDialogIgnoreCount();
				errorPage.setVisibility(View.VISIBLE);
			}
		}break;

		default:
			break;
		}
	}
	
	/***
	 * Session 过期重登录后重新同步Cookie 然后重现加载Url
	 */
	private void reloadDestUrl(){
		/*加载男士资料*/
		String domain = WebSiteManager.getInstance().GetWebSite().getAppSiteHost();
		
		CookieSyncManager.createInstance(this);
		CookieManager cookieManager = CookieManager.getInstance();
		cookieManager.setAcceptCookie(true);
		
		String phpSession = RequestJni.GetCookies(domain.substring(domain.indexOf("http://") + 7, domain.length()));
		cookieManager.setCookie(domain, phpSession); // 
		CookieSyncManager.getInstance().sync();
		
		mWebView.clearCache(true);
		
		if( mUrl != null && mUrl.length() > 0 ) {
			mWebView.loadUrl(mUrl);
		}
	}
	
	public static Intent getIntent(Context context, String url){
		Intent intent = new Intent(context, WebViewActivity.class);
		intent.putExtra(WEB_URL, url);
		return intent;
	}

	@Override
	public void OnLogin(boolean isSuccess, String errno, String errmsg,
			LoginItem item, LoginErrorItem errItem) {
		Message msg = Message.obtain();
		msg.what = LOGIN_CALLBACK;
		RequestBaseResponse response = new RequestBaseResponse(isSuccess, errno, errmsg, item);
		msg.obj = response;
		sendUiMessage(msg);
	}

	@Override
	public void OnLogout(boolean bActive) {
		// TODO Auto-generated method stub
		
	}
	
	/**
	 * 忽视计数器直接隐藏progressDialog
	 */
	public void hideProgressDialogIgnoreCount(){
		try {
			if( mProgressDialogCount > 0 ) {
				mProgressDialogCount = 0;
				if( progressDialog != null ) {
					progressDialog.dismiss();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}