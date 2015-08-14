package com.qpidnetwork.dating.authorization;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.InputType;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.Session;
import com.facebook.SessionLoginBehavior;
import com.qpidnetwork.dating.BaseActivity;
import com.qpidnetwork.dating.R;
import com.qpidnetwork.dating.authorization.LoginManager.OnLoginManagerCallback;
import com.qpidnetwork.manager.WebSiteManager;
import com.qpidnetwork.request.OnOtherOnlineCountCallback;
import com.qpidnetwork.request.OnRequestOriginalCallback;
import com.qpidnetwork.request.RequestErrorCode;
import com.qpidnetwork.request.RequestJni;
import com.qpidnetwork.request.RequestJniAuthorization;
import com.qpidnetwork.request.RequestJniOther;
import com.qpidnetwork.request.item.LoginErrorItem;
import com.qpidnetwork.request.item.LoginItem;
import com.qpidnetwork.request.item.OtherOnlineCountItem;
import com.qpidnetwork.view.ButtonRaised;
import com.qpidnetwork.view.MaterialTextField;

/**
 * 认证模块
 * 登录界面 
 * @author Max.Chiu
 *
 */
public class LoginActivity extends BaseActivity implements OnLoginManagerCallback {
	private enum RequestFlag {
		REQUEST_SUCCESS,
		REQUEST_FAIL,
		REQUEST_CHECKCODE_SUCCESS,
		REQUEST_CHECKCODE_FAIL,
		REQUEST_ONLINE_SUCCESS,
		REQUEST_ONLINE_FAIL,
	}
	
	private TextView textViewOnline;
	
	private MaterialTextField editTextName;
	private MaterialTextField editTextPassword;
	private ImageButton imageViewVisiblePassword;
	
	private RelativeLayout layoutCheckCode;
	private MaterialTextField editTextCheckcode;
	
	private ButtonRaised buttonForget;
	private ButtonRaised buttonLoginWithFacebook;
	
	private ImageView imageViewCheckCode;
	private ButtonRaised buttonRetry;
	
	private boolean mbHasGetCheckCode = false;
	/**
	 * 界面消息
	 */
	private class MessageCallbackItem {
		/**
		 * 
		 * @param errno				接口错误码
		 * @param errmsg			错误提示
		 * @param bitmap			验证码
		 * @param loginItem			登录正常返回
		 * @param loginErrorItem	登录错误返回
		 */
		public MessageCallbackItem(
				String errno, 
				String errmsg
				) {
			this.errno = errno;
			this.errmsg = errmsg;
		}
		public String errno;
		public String errmsg;
		public Bitmap bitmap = null;
		public LoginItem loginItem = null;
		public LoginErrorItem loginErrorItem = null;
		public OtherOnlineCountItem[] otherOnlineCountItem = null;
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		this.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
		
		float density = getResources().getDisplayMetrics().density;
		Display display = this.getWindow().getWindowManager().getDefaultDisplay();
    	Point size = new Point();
    	
    	if (Build.VERSION.SDK_INT > 12){
    		display.getSize(size);
    	}else{
    		size.y = display.getHeight();
    		size.x = display.getWidth();
    	}
    	
    	int width_times =  Math.round((float)size.x / (56.0f * density));
    	float dialog_width = ((float)(width_times - 1) * 56.0f * density);
    	this.getWindow().setLayout((int)dialog_width, LayoutParams.WRAP_CONTENT);
    	
		
		// 读取本地缓存
    	LoginParam param = LoginPerfence.GetLoginParam(mContext);
    	if( param != null ) {
    		switch (param.type) {
			case Default :{
				if( param.email != null ) {
					editTextName.setText(param.email);
				}
				
				if( param.password != null ) {
					editTextPassword.setText(param.password);
				}
			}break;
			default:
				break;
			}
    	}
		
		// 增加登录状态改变监听
		LoginManager.getInstance().AddListenner(this);
		
		switch (LoginManager.getInstance().GetLoginStatus()) {
		case NONE: {
		}break;
		case LOGINING:{
			// 此处该弹菊花
			showProgressDialog("Logining...");
		}
		case LOGINED:{
		}break;
		default:
			break;
		}
		
		// 调用刷新注册码接口
		GetCheckCode();
		
		// 获取在线人数
//		OnlineCount();
	}
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		
		// 去除登录状态改变监听
		LoginManager.getInstance().RemoveListenner(this);
	}
	
	/**
	 * 点击登录
	 * @param view
	 */
	public void onClickLogin(View view) {
		if( editTextName.getText().toString().length() == 0 ) {
			editTextName.setError(Color.RED, true);
			return;
		}
		
		if( editTextPassword.getText().toString().length() == 0 ) {
			editTextPassword.setError(Color.RED, true);
			return;
		}
		
		// 此处该弹菊花
		showProgressDialog("Login...");
		LoginManager.getInstance().Login(
				editTextName.getText().toString(), 
				editTextPassword.getText().toString(), 
				editTextCheckcode.getText().toString());
	}
	
	/**
	 * 点击忘记密码
	 * @param view
	 */
	public void onClickForget(View view) {
		startActivity(new Intent(this, RegisterResetPasswordActivity.class));
	}
	
	
	
	/**
	 * toggle visibility of password
	 */
	public void onClickVisiblePassword(View view){
		if (editTextPassword.getEditor().getInputType() == InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD){
			editTextPassword.setPassword();
			imageViewVisiblePassword.setImageResource(R.drawable.ic_visible_grey600_24dp);
		}else{
			editTextPassword.setVisiblePassword();
			imageViewVisiblePassword.setImageResource(R.drawable.ic_invisible_grey600_24dp);
		}
		
		editTextPassword.getEditor().setSelection(editTextPassword.getText().length());
		
	}
	
	/**
	 * 点击facebook登录
	 * @param view
	 */
	public void onClickLoginWithFacebook(View view) {
		// 发送facebook授权请求
        Session.OpenRequest openRequest = new Session.OpenRequest(this);
        
        // 用当前facebook客户端已经用户
        SessionLoginBehavior loginBehavior = SessionLoginBehavior.SSO_WITH_FALLBACK;
        // 弹出web页面用户手动输入
//        SessionLoginBehavior loginBehavior = SessionLoginBehavior.SUPPRESS_SSO;
        
        openRequest.setLoginBehavior(loginBehavior);
        openRequest.setRequestCode(Session.DEFAULT_AUTHORIZE_ACTIVITY_CODE);
        
        Session session = LoginManager.getInstance().GetFacebookSession();
        if( session.isOpened() ) {
        	LoginManager.getInstance().LoginWithFacebook("", "", "", "", "", "");
        } else {
            session.openForRead(openRequest);
        }
	}
	
	/**
	 * 获取站点在线人数
	 */
	public void OnlineCount() {
		RequestJniOther.OnlineCount(
				WebSiteManager.newInstance(this).GetWebSite().getSiteId(), 
				new OnOtherOnlineCountCallback() {
					
					@Override
					public void OnOtherOnlineCount(boolean isSuccess, String errno,
							String errmsg, OtherOnlineCountItem[] item) {
						// TODO Auto-generated method stub
						Message msg = Message.obtain();
						MessageCallbackItem obj = new MessageCallbackItem(errno, errmsg);
						if( isSuccess ) {
							// 获取站点在线人数成功
							msg.what = RequestFlag.REQUEST_ONLINE_SUCCESS.ordinal();
							obj.otherOnlineCountItem = item;
						} else {
							// 获取验证码失败
							msg.what = RequestFlag.REQUEST_ONLINE_FAIL.ordinal();
						}
						msg.obj = obj;
						mHandler.sendMessage(msg);
					}
				});
	}
	
	/**
	 * 验证码码
	 */
	public void GetCheckCode() {
		buttonRetry.setVisibility(View.GONE);
		imageViewCheckCode.setClickable(false);
		RequestJni.StopAllRequest();
		RequestJniAuthorization.GetCheckCode(new OnRequestOriginalCallback() {
			
			@Override
			public void OnRequestData(boolean isSuccess, String errno, String errmsg,
					byte[] data) {
				// TODO Auto-generated method stub
				Message msg = Message.obtain();
				MessageCallbackItem obj = new MessageCallbackItem(errno, errmsg);
				if( isSuccess ) {
					// 获取验证码成功
					msg.what = RequestFlag.REQUEST_CHECKCODE_SUCCESS.ordinal();
					if( data.length != 0 ) {
						obj.bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
					}
				} else {
					// 获取验证码失败
					msg.what = RequestFlag.REQUEST_CHECKCODE_FAIL.ordinal();
				}
				msg.obj = obj;
				mHandler.sendMessage(msg);
			}
		});
	}
	
	/**
	 * 点击验证码码
	 * @param view
	 */
	public void onClickCheckCode(View view) {
		// 调用刷新注册码接口
		GetCheckCode();
	}

	/**
	 * 登录状态改变
	 */
	@Override
	public void OnLogin(boolean isSuccess, String errno, String errmsg,
			LoginItem item, LoginErrorItem errItem) {
		// TODO Auto-generated method stub
		Message msg = Message.obtain();
		MessageCallbackItem obj = new MessageCallbackItem(errno, errmsg);
		if( isSuccess ) {
			// 登录成功
			msg.what = RequestFlag.REQUEST_SUCCESS.ordinal();
			obj.loginItem = item;
		} else {
			// 登录失败
			msg.what = RequestFlag.REQUEST_FAIL.ordinal();
			obj.loginErrorItem = errItem;
		}
		msg.obj = obj;
		mHandler.sendMessage(msg);
	}

	/**
	 * 注销
	 */
	@Override
	public void OnLogout() {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void InitView() {
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		setContentView(R.layout.activity_login);
		

		editTextName = (MaterialTextField) findViewById(R.id.editTextName);
		editTextName.setHint(getString(R.string.login_email_or_id));
		editTextName.setEmail();
		
		editTextPassword = (MaterialTextField) findViewById(R.id.editTextPassword);
		editTextPassword.setHint(getString(R.string.login_password));
		editTextPassword.setPassword();
		
		imageViewVisiblePassword = (ImageButton)findViewById(R.id.imageViewVisiblePassword);
		
		layoutCheckCode = (RelativeLayout) findViewById(R.id.layoutCheckCode);
		layoutCheckCode.setVisibility(View.GONE);
		buttonRetry = (ButtonRaised) findViewById(R.id.buttonRetry);
		buttonRetry.setVisibility(View.GONE);
		
		editTextCheckcode = (MaterialTextField) findViewById(R.id.editTextCheckCode);
		editTextCheckcode.setHint(getString(R.string.logn_secure_code));
		editTextCheckcode.setNoPredition();
		
		buttonForget = (ButtonRaised) findViewById(R.id.buttonForget);
		buttonLoginWithFacebook = (ButtonRaised) findViewById(R.id.buttonLoginWithFacebook);
		
		imageViewCheckCode = (ImageView) findViewById(R.id.imageViewCheckCode);
		imageViewCheckCode.setImageDrawable(null);
		
		buttonForget.setVisibility(View.GONE);
		buttonLoginWithFacebook.setVisibility(View.GONE);

	}
	
	@Override
	public void InitHandler() {
		mHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				// 收起菊花
				hideProgressDialog();
				MessageCallbackItem obj = (MessageCallbackItem) msg.obj;
				switch ( RequestFlag.values()[msg.what] ) {
				case REQUEST_SUCCESS:{
					// 登录成功
					finish();
				}break;
				case REQUEST_FAIL:{
					// 登录失败显示其他方式
					buttonForget.setVisibility(View.VISIBLE);
					//buttonLoginWithFacebook.setVisibility(View.VISIBLE);
					
					switch (obj.errno) {
					case RequestErrorCode.LOCAL_ERROR_CODE_TIMEOUT:{
						// 本地错误
					}
					case RequestErrorCode.LOCAL_ERROR_CODE_PARSEFAIL:{
						Toast.makeText(mContext, obj.errmsg, Toast.LENGTH_SHORT).show();
					}break;
					case RequestErrorCode.MBCE1001:{
						// 用户名与密码不正确
						editTextName.setError(Color.RED, true);
						editTextPassword.setError(Color.RED, false, false);
					}break;
					case RequestErrorCode.MBCE1002:{
						// 会员帐号暂停
					}
					case RequestErrorCode.MBCE1003:{
						// 帐号被冻结
						editTextName.setError(Color.RED, true);
					}break;
					case RequestErrorCode.MBCE1012: {
						// 验证码为空
					}
					case RequestErrorCode.MBCE1013:{
						// 验证码无效
						
						// 重新获取验证码
						layoutCheckCode.setVisibility(View.VISIBLE);
						editTextCheckcode.setError(Color.RED, true);
						GetCheckCode();
					}break;
					default:{
						// 其他会员账号错误
						editTextName.setError(Color.RED, true);
						editTextPassword.setError(Color.RED, true);
					}break;
					}
				}break;
				case REQUEST_CHECKCODE_SUCCESS:{
					// 获取验证码成功
					layoutCheckCode.setVisibility(View.VISIBLE);
					buttonRetry.setVisibility(View.GONE);
					mbHasGetCheckCode = true;
					imageViewCheckCode.setClickable(true);
					
					if( obj != null && obj.bitmap != null ) {
						Bitmap bitmap = obj.bitmap;
						imageViewCheckCode.setImageBitmap(bitmap);
						imageViewCheckCode.setVisibility(View.VISIBLE);
					} else {
						if( !mbHasGetCheckCode ) {
							// 从来未获取到验证码
							buttonRetry.setVisibility(View.VISIBLE);
							imageViewCheckCode.setVisibility(View.GONE);
						} else {
							// 已经获取成功过
							buttonRetry.setVisibility(View.GONE);
							imageViewCheckCode.setVisibility(View.VISIBLE);
						}
					}
				}break;
				case REQUEST_CHECKCODE_FAIL:{
					// 获取验证码失败
					switch (obj.errno) {
					case RequestErrorCode.LOCAL_ERROR_CODE_TIMEOUT:{
						// 本地错误
					}
					case RequestErrorCode.LOCAL_ERROR_CODE_PARSEFAIL:{
						Toast.makeText(mContext, obj.errmsg, Toast.LENGTH_SHORT).show();
					}break;
					default:{
					}break;
					}
				}break;
				case REQUEST_ONLINE_SUCCESS:{
					// 获取站点在线人数成功
					for(int i = 0; i < obj.otherOnlineCountItem.length; i++) {
						if( obj.otherOnlineCountItem[i].site == WebSiteManager.newInstance(mContext).GetWebSite().getSiteId() ) {
							textViewOnline.setText(String.valueOf(obj.otherOnlineCountItem[i].onlineCount));
							break;
						}
					}

				}break;
				case REQUEST_ONLINE_FAIL:{
					
				}break;
				default:
					break;
				}
			};
		};
	}

}
