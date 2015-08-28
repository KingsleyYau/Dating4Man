package com.qpidnetwork.dating.credit;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.HttpAuthHandler;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebSettings.RenderPriority;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.RelativeLayout;

import com.qpidnetwork.dating.QpidApplication;
import com.qpidnetwork.dating.R;
import com.qpidnetwork.framework.base.BaseActionBarFragmentActivity;
import com.qpidnetwork.framework.util.Log;
import com.qpidnetwork.framework.util.StringUtil;
import com.qpidnetwork.manager.ConfigManager;
import com.qpidnetwork.manager.ConfigManager.OnConfigManagerCallback;
import com.qpidnetwork.manager.WebSiteManager;
import com.qpidnetwork.request.RequestJni;
import com.qpidnetwork.request.item.OtherSynConfigItem;
import com.qpidnetwork.view.MaterialAppBar;
import com.qpidnetwork.view.MaterialDialogAlert;

@SuppressLint("SetJavaScriptEnabled")
@SuppressWarnings("deprecation")
public class BuyCreditActivity extends BaseActionBarFragmentActivity{
	
	protected final String tag = getClass().getName();
	
	public static final String CREDIT_ORDER_NUMBER = "creditNum";
	
	public static final int FAILED_PAYMENT = 2020;
	public static final int SUCCESS_PAYMENT = 2021;// 支付成功返回跳转过来的页面
	public static final int PAYMENT_NOTIFY = 2025; //支付过程中的消息提示
	
	/*JS交互*/
	public static final int NEXT = 100;
	public static final int FIRST = 101;
	public static final int HOME = 102;
	
	
	public WebView mWebView;
	private RelativeLayout mWebPage;
	boolean isBlockLoadingNetworkImage = false;
	private String message;// 弹出框提示消息
	
	private String addCreditsUrl;
	private String addCredits2Url;
	
	private String orderId;//点击订单进入
	
	@SuppressLint("JavascriptInterface") @Override
	protected void onCreate(Bundle arg0) {
		// TODO Auto-generated method stub
		super.onCreate(arg0);
		setCustomContentView(R.layout.activity_buy_credit);
		
		getCustomActionBar().setTitle(getString(R.string.tv_item_creadits), getResources().getColor(R.color.text_color_dark));
		getCustomActionBar().getButtonById(R.id.common_button_back).setBackgroundResource(MaterialAppBar.TOUCH_FEEDBACK_HOLO_LIGHT);
		getCustomActionBar().setButtonIconById(R.id.common_button_back, R.drawable.ic_close_grey600_24dp);
		getCustomActionBar().setAppbarBackgroundColor(Color.WHITE);
		
		Bundle bundle = getIntent().getExtras();
		if(bundle != null){
			if(bundle.containsKey(CREDIT_ORDER_NUMBER)){
				orderId = bundle.getString(CREDIT_ORDER_NUMBER);
			}
		}
		
		mWebView = (WebView)findViewById(R.id.webView);
		mWebPage = (RelativeLayout)findViewById(R.id.webPage);
		
		JSInvokeClass js = new JSInvokeClass(handler);
//		if (Build.VERSION.SDK_INT != 17)
		mWebView.addJavascriptInterface(js, "payment");

		WebSettings webSettings = mWebView.getSettings();
		webSettings.setJavaScriptEnabled(true);
		webSettings.setJavaScriptCanOpenWindowsAutomatically(true);// 允许JS弹出框
//		webSettings.setLayoutAlgorithm(LayoutAlgorithm.SINGLE_COLUMN);
		
		webSettings.setRenderPriority(RenderPriority.HIGH);
		webSettings.setBlockNetworkImage(true);
		isBlockLoadingNetworkImage=true;
		
		webSettings.setDefaultTextEncodingName("utf-8");
		webSettings.setBuiltInZoomControls(true);
		webSettings.setDomStorageEnabled(true);
//		mWebView.setInitialScale(100);//解决打开后导致页面非常小不好看
		mWebView.setWebViewClient(wvc);
		mWebView.setWebChromeClient(client);
		
		showProgressDialog("Loading");
		
		/*getInstance 前必须createInstance */
		CookieSyncManager.createInstance(this);
		
		ConfigManager.getInstance().GetOtherSynConfigItem(new OnConfigManagerCallback() {
			
			@Override
			public void OnGetOtherSynConfigItem(boolean isSuccess, String errno,
					String errmsg, OtherSynConfigItem item) {
				// TODO Auto-generated method stub
				if(isSuccess){
					addCreditsUrl = item.pub.addCreditsUrl;
					addCredits2Url = item.pub.addCredits2Url;
					loadUrl();
				}
			}
		});
	}
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		if (mWebView != null) {
			mWebPage.removeView(mWebView);
			mWebView.clearCache(true);
			mWebView.removeAllViews();
			mWebView.destroy();
			mWebView = null;
		}
		
		hideProgressDialog();
	}
	
	Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case SUCCESS_PAYMENT:
				message = (String) msg.obj;
				doShowSucPaymentDialog(message);
				break;

			case FAILED_PAYMENT:
				message = (String) msg.obj;
				doShowFailedPaymentDialog(message);
				break;
			case PAYMENT_NOTIFY:
				message = (String) msg.obj;
				doShowNotifyDialog(message);
				break;
			}
		}
	};
	
	WebViewClient wvc = new WebViewClient() {

		@Override
		public void onPageStarted(WebView view, String url, android.graphics.Bitmap favicon) {
			showProgressDialog("Loading");
			super.onPageStarted(view, url, favicon);
		};

		@Override
		public void onPageFinished(WebView view, String url) {
			hideProgressDialog();
			
			if(isBlockLoadingNetworkImage){
				mWebView.getSettings().setBlockNetworkImage(false);
				isBlockLoadingNetworkImage = false;
			}
			super.onPageFinished(view, url);
		}

		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url) {
			Log.d("test_credits", "WebViewClient.shouldOverrideUrlLoading(" + url + ")");
			if(url.contains("term") || url.contains("privacy")){
				// 修改如下：（链接以默认浏览器打开）
				Intent intent = new Intent(Intent.ACTION_VIEW);
				intent.setData(Uri.parse(url));
				startActivity(intent);
				return true;
			}else{
				super.shouldOverrideUrlLoading(view, url);
				return false;
			}
		}

		public void onReceivedHttpAuthRequest(WebView view, HttpAuthHandler handler, String host, String realm) {
			handler.proceed("test", "5179");
		}

		public void onFormResubmission(WebView view, android.os.Message dontResend, android.os.Message resend) {
			Log.d(tag, "WebViewClient.onFormResubmission()");
			resend.sendToTarget();
		};

		@Override
		public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
			handler.proceed();
		}
	};
	
	WebChromeClient client = new WebChromeClient() {

		public boolean onJsAlert(WebView view, String url, String message, final android.webkit.JsResult result) {
			
			
			MaterialDialogAlert dialog = new MaterialDialogAlert(BuyCreditActivity.this);
			dialog.setCancelable(false);
			dialog.setMessage(message);
			dialog.addButton(dialog.createButton(getString(R.string.common_btn_ok), new OnClickListener(){

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					result.confirm();
				}
				
			}));
			
			dialog.show();
			
			return true;
		};

		public boolean onJsConfirm(WebView view, String url, String message, final android.webkit.JsResult result) {
			
			MaterialDialogAlert dialog = new MaterialDialogAlert(BuyCreditActivity.this);
			dialog.setTitle(getString(R.string.title_payment_failed));
			dialog.setMessage(message);
			dialog.addButton(dialog.createButton(getString(R.string.btn_try_again), new OnClickListener(){

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					result.confirm();
				}
				
			}));
			
			dialog.addButton(dialog.createButton(getString(R.string.common_btn_cancel), null));
			dialog.show();
			
			return true;
		};
	};
	
	
	/**
	 * 显示支付成功对话框
	 * 
	 * @param msg
	 */
	private void doShowSucPaymentDialog(String msg) {
		
		
		MaterialDialogAlert dialog = new MaterialDialogAlert(BuyCreditActivity.this);
		dialog.setTitle(getString(R.string.title_payment_success));
		dialog.setMessage(message);
		dialog.setCancelable(false);
		dialog.addButton(dialog.createButton(getString(R.string.common_btn_ok), new OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				DoFinish();
			}
			
		}));

		dialog.show();
		
	}
	
	/**
	 * 显示支付失败对话框
	 * 
	 * @param msg
	 */
	public void doShowFailedPaymentDialog(String msg) {
		
		MaterialDialogAlert dialog = new MaterialDialogAlert(BuyCreditActivity.this);
		dialog.setTitle(getString(R.string.title_payment_failed));
		dialog.setMessage(message);
		dialog.setCancelable(false);
		dialog.addButton(dialog.createButton(getString(R.string.common_btn_ok), new OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				DoFinish();
			}
			
		}));
		
		dialog.show();

	}
	
	/**
	 * 显示提示信息
	 * 
	 * @param msg
	 */
	public void doShowNotifyDialog(String msg) {
		
		MaterialDialogAlert dialog = new MaterialDialogAlert(BuyCreditActivity.this);
		dialog.setMessage(message);
		dialog.setCancelable(false);
		dialog.addButton(dialog.createButton(getString(R.string.common_btn_ok), null));
		
		dialog.show();

	}
	

	

	private void DoFinish() {
//		backToFirstPage();
		finish();
	}
	
	public void backToFirstPage(){
		Log.i("test_credits","the size is: " + mWebView.copyBackForwardList().getSize());
		if(mWebView.canGoBackOrForward(1 - mWebView.copyBackForwardList().getSize())){
			mWebView.goBackOrForward(1 - mWebView.copyBackForwardList().getSize());
		}
	}
	
	private void loadUrl(){
		/*Cookie 认证*/
		CookieManager cookieManager = CookieManager.getInstance();
		cookieManager.setAcceptCookie(true);
		String domain = WebSiteManager.newInstance(this).GetWebSite().getAppSiteHost();
		String phpSession = RequestJni.GetCookies(domain.substring(domain.indexOf("http://") + 7, domain.length()));
		cookieManager.setCookie(domain, phpSession); // 这样行 TODO 报异常
		CookieSyncManager.getInstance().sync();
		if((!StringUtil.isEmpty(addCredits2Url)) && (!StringUtil.isEmpty(orderId))){
			//有订单号
			mWebView.clearCache(true);
			String url = StringUtil.mergeMultiString(domain, "/",
					"member/qpidnetworkurl/toid/99/device_type/", QpidApplication.DEVICE_TYPE , "/?url=" , addCredits2Url , orderId, "&ismobile=1");
			mWebView.loadUrl(url);
		}else if(!addCreditsUrl.equals("")){
			mWebView.clearCache(true);
			String url = StringUtil.mergeMultiString(domain, "/",
					"member/qpidnetworkurl/toid/99/device_type/", QpidApplication.DEVICE_TYPE , "/?url=" , addCreditsUrl , "&ismobile=1");
			mWebView.loadUrl(url);
		}
		mWebView.requestFocusFromTouch();
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		if (mWebView != null && mWebView.canGoBack()) {
			backToFirstPage();	
			return false;
		}
		return super.onKeyDown(keyCode, event);
	}
	
}
