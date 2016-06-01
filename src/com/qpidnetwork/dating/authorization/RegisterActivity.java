package com.qpidnetwork.dating.authorization;

import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.Session;
import com.facebook.SessionLoginBehavior;
import com.qpidnetwork.dating.R;
import com.qpidnetwork.dating.authorization.LoginManager.OnLoginManagerCallback;
import com.qpidnetwork.dating.home.AppUrlHandler;
import com.qpidnetwork.framework.base.BaseFragmentActivity;
import com.qpidnetwork.manager.ConfigManager;
import com.qpidnetwork.manager.ConfigManager.OnConfigManagerCallback;
import com.qpidnetwork.request.RequestErrorCode;
import com.qpidnetwork.request.item.LoginErrorItem;
import com.qpidnetwork.request.item.LoginItem;
import com.qpidnetwork.request.item.OtherSynConfigItem;
import com.qpidnetwork.view.ButtonRaised;
import com.qpidnetwork.view.MovingImageView;
import com.qpidnetwork.view.MovingImageView.Callback;
import com.qpidnetwork.view.MovingImageView.TranslateMode;

/**
 * 认证模块
 * 注册主界面
 * @author Max.Chiu
 *
 */
public class RegisterActivity extends BaseFragmentActivity 
							  implements OnLoginManagerCallback, 
							  			 OnConfigManagerCallback,
							  			 Callback
{

	@SuppressWarnings("unused")
	private class LoginMessageItem {
		public LoginMessageItem(		
				String errno,
				String errmsg,
				LoginItem item,
				LoginErrorItem errItem
				) {
			this.errno = errno;
			this.errmsg = errmsg;
			this.item = item;
			this.errItem = errItem;
		}
		
		public String errno;
		public String errmsg;
		public LoginItem item;
		public LoginErrorItem errItem;
	}
	
	
	public static final int REQUEST_FAIL = 0;
	public static final int REQUEST_SUCCESS = 1;
	
	private ButtonRaised mButtonLogin;
	private TextView mTextViewLogin;
	private ButtonRaised buttonFacebook;
	private MovingImageView mFloatingBg;
	private ImageView backImage;
	private MovingImageView.Images mImages;
	
	private String mParam = null;
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
//		
//		Bundle bundle = getIntent().getExtras();
//		if( bundle != null ) {
//			// 是否直接返回主界面
//			mbReturnHome = bundle.getBoolean((RETURN_HOME), false);
//		}
		
		Intent theIntent = getIntent();
		mParam = theIntent.getStringExtra("param");
		
		LoginManager.getInstance().AddListenner(this);
		
		LoginParam param = LoginManager.getInstance().GetLoginParam();
    	if( param != null ) {
    		switch (param.type) {
			case Default :{
				Intent intent = new Intent(mContext, LoginActivity.class);
				startActivity(intent);
			}break;
			default:
				break;
			}
    	}
    	
    	ConfigManager.getInstance().GetOtherSynConfigItem(this);
	}
	
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        Session session = LoginManager.getInstance().GetFacebookSession();
        if (session != null) {
        	session.onActivityResult(this, requestCode, resultCode, data);
        }
    }
    
    @Override
    protected void onResume() {
        super.onResume();
		
		// 增加登录状态改变监听
        if (!mFloatingBg.isAutoStopped) mFloatingBg.runAnimate(mFloatingBg.mode);
        
    }
    @Override
    public void onPause() {
        super.onPause();
        
		// 删除登录状态改变监听
        mFloatingBg.stopAnimate();
        
    }

    @Override
    public void onDestroy() {
    	mFloatingBg.stopAnimate();
		LoginManager.getInstance().RemoveListenner(this);
        super.onDestroy();
    }
    
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {		
		 if (keyCode == KeyEvent.KEYCODE_BACK )  {
			 LoginManager.getInstance().RemoveListenner(this);
			 finish();
			 return true;
		 }
		 return super.onKeyDown(keyCode, event);
	}
	
	/**
	 * 点击facebook
	 * @param v
	 */
	public void onClickFacebook(View v) {
		showProgressDialog("Loading");
		
		// 发送facebook授权请求
        Session.OpenRequest openRequest = new Session.OpenRequest(this);
        
        // 用当前facebook客户端已经用户
        SessionLoginBehavior loginBehavior = SessionLoginBehavior.SSO_WITH_FALLBACK;
        // 弹出web页面用户手动输入
//        SessionLoginBehavior loginBehavior = SessionLoginBehavior.SUPPRESS_SSO;
        
        openRequest.setLoginBehavior(loginBehavior);
        openRequest.setRequestCode(Session.DEFAULT_AUTHORIZE_ACTIVITY_CODE);
        
        LoginManager.getInstance().InitFacebook();
        Session session = LoginManager.getInstance().GetFacebookSession();
        if( session.isOpened() ) {
        	LoginManager.getInstance().LoginWithFacebook("", "", "", "", "", "");
        } else {
            session.openForRead(openRequest);
        }
	}
	
	/**
	 * 点击注册
	 * @param v
	 */
	public void onClickRegister(View v) {
		startActivity(new Intent(this, RegisterByEmailActivity.class));
	}
	
	/**
	 * 点击登录
	 * @param v
	 */
	public void onClickLogin(View v) {
		Intent intent = new Intent(mContext, LoginActivity.class);
		startActivity(intent);
	}
	
	@Override
	public void OnLogin(boolean isSuccess, String errno, String errmsg,
			LoginItem item, LoginErrorItem errItem) {
		// TODO Auto-generated method stub
		
		LoginMessageItem loginItem = new LoginMessageItem(
				errno,
				errmsg,
				item,
				errItem
				);
		
		Message msg = Message.obtain();
		if( isSuccess ) {
			// 登录成功
			msg.what = REQUEST_SUCCESS;
			finish();
		} else {
			// 登录失败
			msg.what = REQUEST_FAIL;

			msg.obj = loginItem;
		}
		sendUiMessage(msg);
	}

	@Override
	public void OnLogout(boolean bActive) {
		// TODO Auto-generated method stub
		
	}
	

	@Override
	public void InitView() {
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_register);

		int[]  imgesResourceIds = new int[]{R.drawable.img_cool_360dp_width_1,
				R.drawable.img_cool_360dp_width_3,
				R.drawable.long_gallery
		};
		
		mImages = new MovingImageView.Images(imgesResourceIds);
		
		backImage = (ImageView)findViewById(R.id.back_image);
		mTextViewLogin = (TextView) findViewById(R.id.textLogin);
		mButtonLogin = (ButtonRaised) findViewById(R.id.buttonLogin);
		buttonFacebook = (ButtonRaised) findViewById(R.id.buttonFacebook);
		
		mFloatingBg = (MovingImageView)findViewById(R.id.floatingBackground);
		mFloatingBg.setImageResource(mImages.getNext());
		mFloatingBg.setDuration(6000);
		mFloatingBg.setMode(TranslateMode.YTRANSLATE);
		mFloatingBg.runAnimate(500);
		
		mFloatingBg.setCallback(this);
	}
	
	@Override
	protected void handleUiMessage(Message msg) {
		// TODO Auto-generated method stub
		super.handleUiMessage(msg);
		// 收起菊花
		hideProgressDialog();
		switch ( msg.what ) {
		case REQUEST_SUCCESS:{
			// facebook登录成功跳转主界面
//			Intent intent = new Intent(RegisterActivity.this, HomeActivity.class);
//			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//			startActivity(intent);
			
			finish();
			
			AppUrlHandler.AppUrlHandleProc(mParam);
		}break;
		case REQUEST_FAIL:{
			// facebook登录失败, 根据错误码选择显示界面
			LoginMessageItem loginItem = (LoginMessageItem) msg.obj;
			switch (loginItem.errno) {
			case RequestErrorCode.MBCE64001:{
				// facebook没有邮箱，显示输入邮箱
        		startActivity(new Intent(RegisterActivity.this, RegisterByFacebookActivity.class));
			} break;
			case RequestErrorCode.MBCE64002:{
				// facebook有邮箱，并且已经被qpidnetwork注册，显示输入密码，重新绑定
				Intent intent = new Intent(RegisterActivity.this, RegisterFacebookPasswordActivity.class);
				intent.putExtra(
						RegisterFacebookPasswordActivity.REGISTER_FACEBOOK_LOGINERRORITEM_KEY,
						loginItem.errItem
						);
				startActivity(intent);
			}break;
			default:
				Toast.makeText(mContext, loginItem.errmsg, Toast.LENGTH_LONG).show();
				break;
			}
		}break;
		default:
			break;
		}
	}

	@Override
	public void OnGetOtherSynConfigItem(boolean isSuccess, String errno,
			String errmsg, OtherSynConfigItem item) {
		// TODO Auto-generated method stub
		if( isSuccess && item != null && item.pub != null && item.pub.facebook_enable ) {
			buttonFacebook.setVisibility(View.VISIBLE);
			mTextViewLogin.setVisibility(View.VISIBLE);
			mButtonLogin.setVisibility(View.GONE);
		}else {
			mTextViewLogin.setVisibility(View.GONE);
			mButtonLogin.setVisibility(View.VISIBLE);
		}
	}

	@Override
	public void onAnimationStopped() {
		// TODO Auto-generated method stub
		if (mImages.getNextPosition() == 1){
			mFloatingBg.setMode(TranslateMode.XTRANSLATE);
		}else{
			mFloatingBg.setMode(TranslateMode.YTRANSLATE);
		}
		
		int nextImage = mImages.getNext();
		backImage.setImageResource(nextImage);
		mFloatingBg.setNextPhoto(nextImage);
	}

	@Override
	public void onBeforeSetNext() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onNextPhotoSet() {
		// TODO Auto-generated method stub
		mFloatingBg.runAnimate(mFloatingBg.mode, 0);
	}
}
