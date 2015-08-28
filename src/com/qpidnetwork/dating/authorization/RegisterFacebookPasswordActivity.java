package com.qpidnetwork.dating.authorization;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.qpidnetwork.dating.BaseActivity;
import com.qpidnetwork.dating.R;
import com.qpidnetwork.dating.authorization.LoginManager.OnLoginManagerCallback;
import com.qpidnetwork.dating.bean.RequestBaseResponse;
import com.qpidnetwork.framework.widget.CircleImageView;
import com.qpidnetwork.manager.FileCacheManager;
import com.qpidnetwork.manager.WebSiteManager;
import com.qpidnetwork.request.RequestErrorCode;
import com.qpidnetwork.request.item.LoginErrorItem;
import com.qpidnetwork.request.item.LoginItem;
import com.qpidnetwork.tool.ImageViewLoader;
import com.qpidnetwork.view.MaterialAppBar;
import com.qpidnetwork.view.MaterialTextField;

/**
 * 认证模块
 * Facebook输入密码界面
 * @author Max.Chiu
 *
 */
public class RegisterFacebookPasswordActivity extends BaseActivity implements OnLoginManagerCallback {
	public static String REGISTER_FACEBOOK_LOGINERRORITEM_KEY = "LoginErrorItem";
	
	private enum RequestFlag {
		REQUEST_SUCCESS,
		REQUEST_FAIL,
	}
	
	
	/**
	 *  用户头像
	 */
	private CircleImageView imageViewHeader;
	private ImageViewLoader loader = new ImageViewLoader(this);
	/**
	 *  用户名称
	 */
	private TextView textViewName;
	private MaterialAppBar appbar;
	/**
	 *  用户密码
	 */
	private MaterialTextField editTextPassword;
	/**
	 * 用户邮箱
	 */
	private TextView textViewErrorTips;
	
	private LoginErrorItem mLoginErrorItem;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.getWindow().setBackgroundDrawable(new ColorDrawable(getResources().getColor(WebSiteManager.getInstance().GetWebSite().getSiteColor())));
		// 增加登录状态改变监听
		LoginManager.getInstance().AddListenner(this);
		
		// 读取界面数据
		InitFromIntent();
	}
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		
		// 删除登录状态改变监听
		LoginManager.getInstance().RemoveListenner(this);
	}
	
	/**
	 * 点击取消
	 * @param v
	 */
	public void onClickCancel(View v) {
		finish();
	}
	
	/**
	 * 点击Complete
	 * @param v
	 */
	public void onClickComplete(View v) {
		// 此处该有菊花
		// 调用facebook绑定接口
		if (editTextPassword.getText().length() == 0){
			editTextPassword.setError(Color.RED, true);
			return;
		}
		
		showProgressDialog("Requesting...");
		LoginManager.getInstance().LoginWithFacebook(
				mLoginErrorItem.email, 
				editTextPassword.getText().toString(),
				RequestErrorCode.MBCE64002,
				"",
				"",
				""
				);
	}
	
	@Override
	public void InitView() {
		setContentView(R.layout.activity_register_facebook_password);
		
		appbar = (MaterialAppBar) findViewById(R.id.appbar);
		appbar.setTouchFeedback(MaterialAppBar.TOUCH_FEEDBACK_HOLO_LIGHT);
		appbar.addButtonToLeft(android.R.id.button1, "", R.drawable.ic_arrow_back_grey600_24dp);
		appbar.setTitle(getString(R.string.facebook_connect), getResources().getColor(R.color.text_color_dark));
		appbar.setOnButtonClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				onClickCancel(v);
			}
			
		});
		
		imageViewHeader = (CircleImageView) findViewById(R.id.imageViewHeader);
		textViewName = (TextView) findViewById(R.id.textViewName);
		
		editTextPassword = (MaterialTextField) findViewById(R.id.editTextPassword);
		editTextPassword.setPassword();
		editTextPassword.setHint(getString(R.string.password_on_qpid_network_site));
		
		textViewErrorTips = (TextView) findViewById(R.id.textViewErrorTips);
	}
	
	@Override
	public void InitHandler() {
		mHandler = new Handler() {
			@Override
			public void handleMessage(android.os.Message msg) {
				// 收起菊花
				hideProgressDialog();
//				MessageCallbackItem obj = (MessageCallbackItem) msg.obj;
				switch ( RequestFlag.values()[msg.what] ) {
				case REQUEST_SUCCESS:{
					// 绑定成功
					finish();
				}break;
				case REQUEST_FAIL:{
//					Toast.makeText(mContext, obj.errmsg, Toast.LENGTH_LONG).show();
				}break;
				default:
					break;
				}
			};
		};
	}
	
	/**
	 * 根据界面传参数初始化界面
	 */
	public void InitFromIntent() {
		mLoginErrorItem = getIntent().getExtras().getParcelable(REGISTER_FACEBOOK_LOGINERRORITEM_KEY);
		
		if( mLoginErrorItem != null ) {
			String url = mLoginErrorItem.photoURL;//"http://192.168.70.1/Share/u387.png";
			String localPath = FileCacheManager.getInstance().CacheImagePathFromUrl(url);
			loader.DisplayImage(imageViewHeader, url, localPath, null);
			
			textViewName.setText(mLoginErrorItem.firstname);
			
			String format = getResources().getString(R.string.register_facebook_error_tips);  
			textViewErrorTips.setText(String.format(format, mLoginErrorItem.email));
		}

	}

	@Override
	public void OnLogin(boolean isSuccess, String errno, String errmsg,
			LoginItem item, LoginErrorItem errItem) {
		// TODO Auto-generated method stub
		// facebook登录状态改变
		Message msg = Message.obtain();
		RequestBaseResponse obj = new RequestBaseResponse(isSuccess, errno, errmsg, null);
		if( isSuccess ) {
			// 登录成功
			msg.what = RequestFlag.REQUEST_SUCCESS.ordinal();
			obj.body = item;
		} else {
			// 登录失败
			msg.what = RequestFlag.REQUEST_FAIL.ordinal();
			obj.body = errItem;
		}
		msg.obj = obj;
		mHandler.sendMessage(msg);
	}
	
	@Override
	public void OnLogout(boolean bActive) {
		// TODO Auto-generated method stub
		
	}
}
