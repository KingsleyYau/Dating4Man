package com.qpidnetwork.dating.advertisement;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.HttpAuthHandler;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.qpidnetwork.dating.QpidApplication;
import com.qpidnetwork.dating.R;
import com.qpidnetwork.dating.R.color;
import com.qpidnetwork.dating.home.AppUrlHandler;
import com.qpidnetwork.framework.base.BaseFragmentActivity;
import com.qpidnetwork.manager.WebSiteManager;
import com.qpidnetwork.request.RequestJni;
import com.qpidnetwork.request.item.AdMainAdvert.OpenType;
import com.qpidnetwork.view.MaterialAppBar;

@SuppressWarnings("deprecation")
public class AdvertWebviewActivity extends BaseFragmentActivity
{

	
	public static final String WEB_URL = "web_url";
	public static final String WEB_TITLE = "web_title";
	public static final String ADVERT_OPEN_TYPE = "opentype";
	
	
	private WebView mWebView;
	private String mUrl = "";
	private String mTitle = "";
	private OpenType mOpenType = OpenType.UNKNOW;
	private MaterialAppBar appbar;
	
	public static Intent getIntent(Context context, String url, OpenType openType){
		Intent intent = new Intent(context, AdvertWebviewActivity.class);
		intent.putExtra(WEB_URL, url);
		intent.putExtra(ADVERT_OPEN_TYPE, openType.ordinal());
		return intent;
	}
	
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
			
			if(bundle.containsKey(ADVERT_OPEN_TYPE)){
				mOpenType = OpenType.values()[bundle.getInt(ADVERT_OPEN_TYPE)];
			}
		}
			
		if( mUrl != null && mUrl.length() > 0 ) {
			mWebView.loadUrl(mUrl);
		}
		
		if( mTitle != null && mTitle.length() > 0 ) {
			appbar.setTitle(mTitle, getResources().getColor(color.text_color_dark));
		}

	}
	
	@Override
	protected void onDestroy(){
		super.onDestroy();
	}
	
	@Override
	public void InitView() {
		setContentView(R.layout.activity_webview);
		mWebView = (WebView) findViewById(R.id.webView);

		// 域名
		String domain = WebSiteManager.newInstance(this).GetWebSite().getAppSiteHost();
		// Cookie 认证
		CookieSyncManager.createInstance(this);
		CookieManager cookieManager = CookieManager.getInstance();
		cookieManager.setAcceptCookie(true);
		String phpSession = RequestJni.GetCookies(domain.substring(domain.indexOf("http://") + 7, domain.length()));
		cookieManager.setCookie(domain, phpSession);	
		CookieSyncManager.getInstance().sync();
		
		mWebView.setWebViewClient(new WebViewClient() { 
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
				hideProgressDialog();
			}
			
		    @Override  
		    public boolean shouldOverrideUrlLoading(WebView view, String url) {
		    	if(mOpenType == OpenType.HIDE){
		    		/*打开指定模块*/
		    		AppUrlHandler.AppUrlHandle(AdvertWebviewActivity.this, url);
		    		finish();
		    	}else{
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
		}); 
		
		appbar = (MaterialAppBar)findViewById(R.id.appbar);
		appbar.setAppbarBackgroundColor(getResources().getColor(R.color.theme_actionbar_secoundary));
		appbar.setTouchFeedback(MaterialAppBar.TOUCH_FEEDBACK_HOLO_LIGHT);
		appbar.addButtonToLeft(R.id.common_button_back, "", R.drawable.ic_close_grey600_24dp);
		appbar.setTitle("Redirecting...", getResources().getColor(R.color.text_color_dark));
		appbar.setOnButtonClickListener(this);
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
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.common_button_back:{
			// 点击取消
			if( mWebView.canGoBack() ) {
				mWebView.goBack();
			} else {
				finish();
			}
		}break;
		}
	}
}
