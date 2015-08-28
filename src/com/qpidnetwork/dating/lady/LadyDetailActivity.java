package com.qpidnetwork.dating.lady;

import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.webkit.HttpAuthHandler;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.Toast;

import com.qpidnetwork.dating.BaseActivity;
import com.qpidnetwork.dating.QpidApplication;
import com.qpidnetwork.dating.R;
import com.qpidnetwork.dating.advertisement.AdvertisementManager;
import com.qpidnetwork.dating.authorization.LoginManager;
import com.qpidnetwork.dating.authorization.LoginManager.LoginStatus;
import com.qpidnetwork.dating.authorization.LoginParam;
import com.qpidnetwork.dating.authorization.LoginPerfence;
import com.qpidnetwork.dating.bean.EMFAttachmentBean;
import com.qpidnetwork.dating.bean.EMFAttachmentBean.AttachType;
import com.qpidnetwork.dating.bean.RequestBaseResponse;
import com.qpidnetwork.dating.contacts.ContactManager;
import com.qpidnetwork.dating.emf.EMFAttachmentPreviewActivity;
import com.qpidnetwork.dating.emf.MailEditActivity;
import com.qpidnetwork.dating.lady.LadyDetailManager.OnLadyDetailManagerQueryLadyDetailCallback;
import com.qpidnetwork.dating.livechat.ChatActivity;
import com.qpidnetwork.dating.lovecall.DirectCallManager;
import com.qpidnetwork.framework.util.Log;
import com.qpidnetwork.framework.util.SystemUtil;
import com.qpidnetwork.manager.WebSiteManager;
import com.qpidnetwork.request.OnQueryLadyCallCallback;
import com.qpidnetwork.request.OnRequestCallback;
import com.qpidnetwork.request.RequestJniEMF.ReplyType;
import com.qpidnetwork.request.RequestOperator;
import com.qpidnetwork.request.item.LadyCall;
import com.qpidnetwork.request.item.LadyDetail;
import com.qpidnetwork.request.item.LadyDetail.ShowLoveCall;
import com.qpidnetwork.view.GetMoreCreditDialog;
import com.qpidnetwork.view.MaterialAppBar;
import com.qpidnetwork.view.MaterialDialogAlert;
import com.qpidnetwork.view.MaterialDropDownMenu;
import com.qpidnetwork.view.MaterialThreeButtonDialog;

@SuppressLint({ "SetJavaScriptEnabled", "RtlHardcoded" })
public class LadyDetailActivity extends BaseActivity {
	/**
	 * 其他界面进入参数
	 */
	public static final String WOMANID = "womanId";
	public static final String SHOW_BUTTONS = "SHOW_BUTTONS";
	
	/**
	 * 所有打开ladydetail页面统一入口
	 * @param context
	 * @param womanId
	 */
	public static void launchLadyDetailActivity(Context context, String womanId, boolean showButtons){
		if(LoginManager.getInstance().GetLoginStatus() == LoginStatus.LOGINED){
			/*登陆成功，需要判断风控条件，如果女士详情被风控则不可以查看*/
			LoginParam loginParam = LoginPerfence.GetLoginParam(context);
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
	 * 界面返回
	 */
	private enum ActivityResultFlag {
		RESULT_LADY_LABEL,
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
	private MaterialDropDownMenu overflowMenu;
	
	/**
	 * 女士Id
	 */
	public String mWomanId = "";
	public boolean mShowButtons = true;
	
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
		
		// 刷新界面数据
		ReloadData();
		
		// 弹出广告
		AdvertisementManager advertManager = AdvertisementManager.getInstance();
		if (null != advertManager) {
			advertManager.showMainAdvert(this);
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
		
		appbar.addButtonToLeft(R.id.common_button_call, "", R.drawable.ic_call_grey600_24dp);
		appbar.getButtonById(R.id.common_button_call).setVisibility(View.GONE);
		appbar.addButtonToLeft(R.id.common_button_emf, "", R.drawable.ic_email_grey600_24dp);
		appbar.addButtonToLeft(R.id.common_button_online, "", R.drawable.ic_chat_greyc8c8c8_24dp);
		appbar.getButtonById(R.id.common_button_online).setEnabled(false);
		appbar.addButtonToLeft(R.id.common_button_back, "", R.drawable.ic_arrow_back_grey600_24dp);
		appbar.addButtonToRight(R.id.common_button_overflow, "overflow", R.drawable.ic_more_vert_grey600_24dp);
		appbar.getButtonById(R.id.common_button_overflow).setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (item == null) return;
				
				String[] menuItems = new String[]{getString(R.string.contact_add_to_favourite)};
				if (item.isfavorite) menuItems = new String[]{getString(R.string.contact_remove_from_favourite)};
				
				if (overflowMenu != null){
					overflowMenu.setMenuItems(menuItems);
					overflowMenu.showAsDropDown(v);
					return;
				}
				
				if (overflowMenu == null) overflowMenu = new MaterialDropDownMenu(mContext, menuItems, new MaterialDropDownMenu.OnClickCallback() {
					
					@Override
					public void onClick(AdapterView<?> adptView, View v, int which) {
						// TODO Auto-generated method stub

						if(  CheckLogin() ) {
							if ( item == null ) {
								return;
							}
							
							appbar.getButtonById(R.id.common_button_overflow).setEnabled(false);
							if ( item.isfavorite ) {
								RemoveFavour();
							} else {
								//finish add should execute showToastDone("Done!") to cancel this toast.
								AddFavour();
							}
						}

					}
				} , new Point((int)(208.0f * mContext.getResources().getDisplayMetrics().density), LayoutParams.WRAP_CONTENT));
				
				overflowMenu.showAsDropDown(v);
				return;
			}
			
		});
		
		if (!mShowButtons){
			appbar.getButtonById(R.id.common_button_call).setVisibility(View.GONE);
			appbar.getButtonById(R.id.common_button_emf).setVisibility(View.GONE);
			appbar.getButtonById(R.id.common_button_online).setVisibility(View.GONE);
		}
		
		
		

		
		appbar.setOnButtonClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				switch (v.getId()) {
				case R.id.common_button_back:{
					finish();
				}break;
				case R.id.common_button_online:{
					// 点击在线
					if( item != null ) {
						ChatActivity.launchChatActivity(mContext, item.womanid, item.firstname, item.photoMinURL);
					}else{
						ChatActivity.launchChatActivity(mContext, mWomanId, "", "");
					}
				}break;
				case R.id.common_button_emf:{
					// 点击emf
					if(  CheckLogin() ) {
						MailEditActivity.launchMailEditActivity(mContext, mWomanId, ReplyType.DEFAULT, "");
					}
				}break;
				case R.id.common_button_call:{
					// 点击打lovecall
					if(  CheckLogin() ) {
						QueryLadyCall(mWomanId);
					}
				}break;
				default:break;
				}
			}
		});
		
		// 浏览器控件
		mWebView = (WebView) findViewById(R.id.webView);
		mWebView.getSettings().setJavaScriptEnabled(true);
//		mWebView.addJavascriptInterface(new JavascriptInterface(this), "labelListner");
		
		mWebViewClient = new WebViewClient() {  
			@Override
			public void onPageStarted(WebView view, String url, Bitmap favicon) {
				// TODO Auto-generated method stub
				super.onPageStarted(view, url, favicon);
				Message msg = Message.obtain();
				RequestBaseResponse obj = new RequestBaseResponse(true, "", "", null);
				msg.what = RequestFlag.REQUEST_WEBVIEW_START.ordinal();
				msg.obj = obj;
				mHandler.sendMessage(msg);
			}
			
			@Override
			public void onPageFinished(WebView view, String url) {
				// TODO Auto-generated method stub
				super.onPageFinished(view, url);
				Message msg = Message.obtain();
				RequestBaseResponse obj = new RequestBaseResponse(true, "", "", null);
				msg.what = RequestFlag.REQUEST_WEBVIEW_FINISH.ordinal();
				msg.obj = obj;
				mHandler.sendMessage(msg);
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
		};
		
		mWebView.setWebViewClient(mWebViewClient);

	}

	@Override
	public void InitHandler() {
		// TODO Auto-generated method stub
		mHandler = new Handler() {
			@Override
			public void handleMessage(android.os.Message msg) {
				// 收起菊花
				hideProgressDialog();
				// 处理消息
				RequestBaseResponse obj = (RequestBaseResponse) msg.obj;
				if((obj.body != null) && (obj.body instanceof LadyDetail)){
					LadyDetail ladyDetail = (LadyDetail)obj.body;
					// 是否在线
					if( ladyDetail.isonline ) {
						appbar.changeIconById(R.id.common_button_online, R.drawable.ic_chat_grey600_24dp);
						appbar.getButtonById(R.id.common_button_online).setEnabled(true);
						appbar.pushBadgeById(R.id.common_button_online, Color.parseColor("#228B22"));
					} else {
						appbar.changeIconById(R.id.common_button_online, R.drawable.ic_chat_greyc8c8c8_24dp);
						appbar.getButtonById(R.id.common_button_online).setEnabled(false);
						appbar.cancelBadgeById(R.id.common_button_online);
					}
					
					
					// 是否允许打电话
					if( ladyDetail.show_lovecall == ShowLoveCall.CallMeNow) {
						appbar.getButtonById(R.id.common_button_call).setVisibility(View.VISIBLE);
					} else {
						appbar.getButtonById(R.id.common_button_call).setVisibility(View.GONE);
					}
					
					
					if(!mShowButtons){
						appbar.getButtonById(R.id.common_button_call).setVisibility(View.GONE);
						appbar.setTitle(ladyDetail.firstname, getResources().getColor(R.color.text_color_dark));
					}
					
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
						ArrayList<EMFAttachmentBean> attachList = new ArrayList<EMFAttachmentBean>();
						LadyDetail photoLadyDetail = (LadyDetail)obj.body;
						if( photoLadyDetail != null && photoLadyDetail.photoList != null ) {
							for(String photo : photoLadyDetail.photoList) {
								EMFAttachmentBean normalItem = new EMFAttachmentBean();
								normalItem.type = AttachType.NORAML_PICTURE;
								normalItem.photoUrl = photo;
								attachList.add(normalItem);
							}
						}
						
						// 打开预览图片
						Intent intent = EMFAttachmentPreviewActivity.getIntent(mContext, attachList, 0);
						startActivity(intent);
					}
				}break;
				case REQUEST_VIDEO_SUCCESS:{
					// 判断是否登录
					if( CheckLogin() ) {
						// 打开预览视频
						LadyDetail videoLadyDetail = (LadyDetail)obj.body;
						VideoDetailActivity.launchLadyVideoDetailActivity(mContext, videoLadyDetail.womanid, videoLadyDetail.firstname);
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
					}
					/*添加favorite成功，添加到现有联系人或更新联系人*/
					ContactManager.getInstance().updateBySendEMF(item);
					
					appbar.getButtonById(R.id.common_button_overflow).setEnabled(true);
				}break;
				case REQUEST_ADD_FAVOUR_FAIL:{
					// 收藏失败
					cancelToastImmediately();
					Toast.makeText(mContext, obj.errmsg, Toast.LENGTH_LONG).show();	
					appbar.getButtonById(R.id.common_button_overflow).setEnabled(true);
				}break;
				case REQUEST_REMOVE_FAVOUR_SUCCESS:{
					showToastDone("Removed!");
					if( item != null ) {
						item.isfavorite = false;
						ReloadFavorite(item.isfavorite);
					}
					appbar.getButtonById(R.id.common_button_overflow).setEnabled(true);
					ContactManager.getInstance().updateFavoriteStatus(item.womanid, false);
				}break;
				case REQUEST_REMOVE_FAVOUR_FAIL:{
					// 删除收藏失败
					cancelToastImmediately();
					Toast.makeText(mContext, obj.errmsg, Toast.LENGTH_LONG).show();	
					appbar.getButtonById(R.id.common_button_overflow).setEnabled(true);
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
						dialog.addButton(dialog.createButton(getString(R.string.common_btn_cancel), null));
						dialog.show();
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
		};
	}
	
	/**
	 * 刷新界面数据
	 */
	public void ReloadData() {
		mWomanId = getIntent().getExtras().getString(WOMANID);
		
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
				mHandler.sendMessage(msg);
			}
		});
		
		// 域名
		String domain = WebSiteManager.newInstance(this).GetWebSite().getAppSiteHost();
//		// Cookie 认证
//		CookieManager cookieManager = CookieManager.getInstance();
//		cookieManager.setAcceptCookie(true);
//		String phpSession = RequestJni.GetCookies(domain.substring(domain.indexOf("http://") + 7, domain.length()));
//		cookieManager.setCookie(domain, phpSession);
//		CookieSyncManager.getInstance().sync();
		String url = domain;
		
		url += "/member/lady_profile/womanid/";
		url += mWomanId;
		
		url += "/versioncode/";
		url += QpidApplication.versionCode;
//		url = "http://demo-mobile.idateasia.com/member/lady_profile/womanid/P580502";
		mWebView.loadUrl(url);
		
	}

	public boolean StartActivityByUrl(String url) {
		boolean bFlag = false;
		String womanId = "";
		String[] reslult = url.split("womanid=", 2);
		if( reslult != null && reslult.length > 1) {
			womanId = reslult[1];
		}
		
		if( url.contains("qpidnetwork://app/womanphoto") ){
			// 点击图片
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
						msg.what = RequestFlag.REQUEST_PHOTO_SUCCESS.ordinal();
						obj.body = item;
					} else {
						// 获取个人信息失败
						msg.what = RequestFlag.REQUEST_FAIL.ordinal();
					}
					msg.obj = obj;
					mHandler.sendMessage(msg);
				}
			});
			
			// 标记已经处理
			bFlag = true;
		} else if( url.contains("qpidnetwork://app/womanvideo") ) {
			// 点击video
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
					mHandler.sendMessage(msg);
				}
			});
			
			// 标记已经处理
			bFlag = true;
		} else if( url.contains("qpidnetwork://app/womansign") ) {
			if( CheckLogin() ) {
				// 打开标签页
				mContext.startActivityForResult(LadyLabelActivity.getIntent(mContext, womanId, ""), ActivityResultFlag.RESULT_LADY_LABEL.ordinal());
			}
			// 标记已经处理
			bFlag = true;
		}
		
		return bFlag;
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	    super.onActivityResult(requestCode, resultCode, data);
	 
	    switch( ActivityResultFlag.values()[requestCode] ) {
	    case RESULT_LADY_LABEL:{
	    	// 标签页返回
	    	if( resultCode == RESULT_OK ) {
	    		String[] ladyLabelAdd = data.getExtras().getStringArray(LadyLabelActivity.LADY_LABEL_ADD);
	    		String[] ladyLabelDel = data.getExtras().getStringArray(LadyLabelActivity.LADY_LABEL_DEL);
	    		
	    		// 刷新女士标签
	    		ReloadLadyLabel(ladyLabelAdd, ladyLabelDel);
	    	}
	    }
	    default:break;
	    }
	}
	
	/**
	 * 调用Javascript刷新女士标签
	 * @param ladyLabelAdd		增加的女士标签数组
	 * @param ladyLabelDel		删除的女士标签数组
	 */
	public void ReloadLadyLabel(String[] ladyLabelAdd, String[] ladyLabelDel) {
		String paramAdd = "";
		for( String item :  ladyLabelAdd ) {
			paramAdd += item;
			paramAdd += ",";
		}
		if( paramAdd.length() > 0 ) {
			paramAdd = paramAdd.substring(0, paramAdd.length() - 1);
		}
		
		String paramDel = "";
		for( String item :  ladyLabelDel ) {
			paramDel += item;
			paramDel += ",";
		}
		
		if( paramDel.length() > 0 ) {
			paramDel = paramDel.substring(0, paramDel.length() - 1);
		}
		
		String url = "javascript:js_update_sign(";
		url += "'" +  paramAdd + "'";
		url += ",";
		url += "'" + paramDel + "'";
		url += ")";
		
		mWebView.loadUrl(url);
	}
	
	
	/**
	 * 调用Javascript刷新女士标签
	 * @param ladyLabelAdd		增加的女士标签数组
	 * @param ladyLabelDel		删除的女士标签数组
	 */
	public void ReloadFavorite(boolean isFavour) {

		String url = "javascript:js_update_favorite(";
		url += "'" +  isFavour + "')";
		
		mWebView.loadUrl(url);
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
//		RequestOperator.getInstance().AddFavouritesLady(mWomanId, new OnRequestCallback() {
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
				mHandler.sendMessage(msg);
			}
		});
	}
	
	public void RemoveFavour() {
		if( !CheckLogin() ) {
			return;
		}
		showToastProgressing("Removing");
//		RequestOperator.getInstance().RemoveFavouritesLady(item.womanid, new OnRequestCallback() {
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
				mHandler.sendMessage(msg);
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
				mHandler.sendMessage(msg);
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
