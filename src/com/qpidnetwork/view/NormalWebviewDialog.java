package com.qpidnetwork.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.HttpAuthHandler;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.RelativeLayout;

import com.qpidnetwork.dating.QpidApplication;
import com.qpidnetwork.dating.R;
import com.qpidnetwork.framework.base.BaseDialog;
import com.qpidnetwork.manager.WebSiteManager;
import com.qpidnetwork.request.RequestJni;

public class NormalWebviewDialog extends BaseDialog{
	
	private WebView mWebView;
	private View contentView;
	private RelativeLayout rlProgress;
	private RelativeLayout rlBody;
	private Context mContext;
	
	public NormalWebviewDialog(Context context){
		super(context);
		this.mContext = context;
		contentView  = LayoutInflater.from(context).inflate(R.layout.dialog_normal_webview, null);
		mWebView = (WebView)contentView.findViewById(R.id.webView);
		rlProgress = (RelativeLayout)contentView.findViewById(R.id.rlProgress);
		rlBody = (RelativeLayout)contentView.findViewById(R.id.rlBody);
		
		mWebView.getSettings().setJavaScriptEnabled(true);
		WebViewClient webViewClient = new WebViewClient() {  
			@Override
			public void onPageStarted(WebView view, String url, Bitmap favicon) {
				rlProgress.setVisibility(View.VISIBLE);
			}
			
			@Override
			public void onPageFinished(WebView view, String url) {
				rlProgress.setVisibility(View.GONE);
				rlBody.setVisibility(View.VISIBLE);
			}
			
		    @Override  
		    public boolean shouldOverrideUrlLoading(WebView view, String url) {
		    	if(url.contains("qpidnetwork://app/closewindow")){
		    		dismiss();
		    		return true;
		    	}
		        return super.shouldOverrideUrlLoading(view, url);  
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
		};
		
		mWebView.setWebViewClient(webViewClient);
    	rlBody.setVisibility(View.INVISIBLE);
		
		this.setContentView(contentView);
	}
	
	/**
	 * 加载Url
	 * @param url
	 */
	public void loadUrl(String url){
		if(mWebView != null
				&& !TextUtils.isEmpty(url)){
			// 域名
			String domain = WebSiteManager.getInstance().GetWebSite().getAppSiteHost();
			// Cookie 认证
			/*getInstance 前必须createInstance */
			CookieSyncManager.createInstance(mContext);
			CookieManager cookieManager = CookieManager.getInstance();
			cookieManager.setAcceptCookie(true);
			String phpSession = RequestJni.GetCookies(domain.substring(domain.indexOf("http://") + 7, domain.length()));
			cookieManager.setCookie(domain, phpSession);
			CookieSyncManager.getInstance().sync();
			
			mWebView.loadUrl(url);
		}
	}
}
