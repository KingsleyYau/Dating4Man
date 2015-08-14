package com.qpidnetwork.dating.authorization;

import java.util.logging.LogManager;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcel;
import android.os.Parcelable;
import android.telephony.TelephonyManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;

import com.qpidnetwork.dating.BaseActivity;
import com.qpidnetwork.dating.MainActivity;
import com.qpidnetwork.dating.R;
import com.qpidnetwork.dating.authorization.LoginManager.OnLoginManagerCallback;
import com.qpidnetwork.dating.home.HomeActivity;
import com.qpidnetwork.manager.ConfigManager;
import com.qpidnetwork.manager.WebSiteManager;
import com.qpidnetwork.manager.ConfigManager.OnConfigManagerCallback;
import com.qpidnetwork.request.OnRegisterCallback;
import com.qpidnetwork.request.OnRequestCallback;
import com.qpidnetwork.request.RequestEnum.Country;
import com.qpidnetwork.request.RequestErrorCode;
import com.qpidnetwork.request.RequestJni;
import com.qpidnetwork.request.RequestJniAuthorization;
import com.qpidnetwork.request.RequestJniProfile;
import com.qpidnetwork.request.item.LoginErrorItem;
import com.qpidnetwork.request.item.LoginItem;
import com.qpidnetwork.request.item.OtherSynConfigItem;
import com.qpidnetwork.request.item.RegisterItem;
import com.qpidnetwork.view.MaterialAppBar;
import com.qpidnetwork.view.MaterialDialogAlert;
import com.qpidnetwork.view.MaterialTextField;

/**
 * 认证模块
 * Email注册输入密码界面
 * @author Max.Chiu
 *
 */
public class RegisterPasswordActivity extends BaseActivity implements OnLoginManagerCallback {
	private enum RequestFlag {
		REQUEST_SUCCESS,
		REQUEST_FAIL,
		UPLOAD_SUCCESS,
		UPLOAD_FAIL,
	}
	
	public static String REGISTER_PARAM_KEY = "RegisterParam";
	
	public static class RegisterParam implements Parcelable {
		public String email;
		public String password;
		public String firstname;
		public String lastname;
		public Country country; 
		public String year;
		public String month;
		public String day;
		public Boolean male;
		public String picturePath;
		
		public RegisterParam() {
			email = "";
			password = "";
			firstname = "";
			lastname = "";
			country = Country.Afghanistan;
			year = "";
			month = "";
			day = "";
			male = true;
			picturePath = "";
		}

		@Override
		public int describeContents() {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public void writeToParcel(Parcel dest, int flags) {
			// TODO Auto-generated method stub
	    	 dest.writeString(email);
	    	 dest.writeString(password);
	    	 dest.writeString(firstname);
	    	 dest.writeString(lastname);
	    	 dest.writeInt(country.ordinal());
	    	 dest.writeString(year);
	    	 dest.writeString(month);
	    	 dest.writeString(day);
	    	 dest.writeValue(male);
	    	 dest.writeString(picturePath);
		}
		
	    private RegisterParam(Parcel in) {
	    	 email = in.readString();
	    	 password = in.readString();
	    	 firstname = in.readString();
	    	 lastname = in.readString();
	    	 country = Country.values()[in.readInt()];
	    	 year = in.readString();
	    	 month = in.readString();
	    	 day = in.readString();
	    	 male = (Boolean) in.readValue(ClassLoader.getSystemClassLoader());
	    	 picturePath = in.readString();
	     }
	     
	    public static final Parcelable.Creator<RegisterParam> CREATOR = new Parcelable.Creator<RegisterParam>() {
	    	public RegisterParam createFromParcel(Parcel in) {
	            return new RegisterParam(in);
	        }
	
	        public RegisterParam[] newArray(int size) {
	            return new RegisterParam[size];
	        }
	     };
	}
	
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
		 * 
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
		public String email;
		public String password;
		public RegisterItem item;
	}
	
	private WebSiteManager siteManager;
	private MaterialTextField editTextEmail;
	private MaterialTextField editTextPassword;
	private MaterialAppBar appbar;
	
	private RegisterParam mRegisterParam = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.getWindow().setBackgroundDrawable(new ColorDrawable(getResources().getColor(WebSiteManager.getInstance().GetWebSite().getSiteColor())));
		// 读取界面数据
		InitFromIntent();
		
		// 增加登录状态改变监听
//		LoginManager.getInstance().AddListenner(this);
	}
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		
		// 去除登录状态改变监听
//		LoginManager.getInstance().RemoveListenner(this);
	}
	
	/**
	 * 点击取消
	 * @param v
	 */
	public void onClickCancel(View v) {
		finish();
	}
	
	/**
	 * 点击Create Profile
	 * @param v
	 */
	public void onClickCreateProfile(View v) {
		
		if(editTextEmail.getText().length() < 9){
			editTextEmail.setError(Color.RED, true);
			return;
		}
		
		// check email format([a-z] ,[A-Z] ,[0-9] ,"." ,"_")
		String email_compiler = "[a-z0-9A-Z\\.\\-_]{3,36}@[a-z0-9A-Z\\.\\-]{3,36}";
		Pattern _pattern = Pattern.compile(email_compiler);
		Matcher _matcher = _pattern.matcher(editTextEmail.getText());
		
		if (!_matcher.matches()){
			editTextEmail.setError(Color.RED, true);
			return;
		}
		
		if (editTextPassword.getText().length() < 4){
			editTextPassword.setError(Color.RED, true);
			return;
		}
		
		String pwd_compiler = "[a-zA-Z0-9]{4,12}";
		_pattern = Pattern.compile(pwd_compiler);
		_matcher = _pattern.matcher(editTextPassword.getText().toString());
		
		if (!_matcher.matches()){
			MaterialDialogAlert dialog = new MaterialDialogAlert(this);
			dialog.setMessage(getString(R.string.your_password_should_be_4_12_characters_only_numbers_and_english_letters_allowed));
			dialog.addButton(dialog.createButton(getString(R.string.common_btn_ok), null));
			dialog.show();
			return;
		}
		
		if( mRegisterParam == null ) {
			return;
		}
		
		// 此处该有菊花
		showProgressDialog("Loading...");
		
		final String email = editTextEmail.getText().toString();
		final String password = editTextPassword.getText().toString();
		
		// 先同步配置
		ConfigManager.getInstance().GetOtherSynConfigItem(new OnConfigManagerCallback() {
			
			@Override
			public void OnGetOtherSynConfigItem(boolean isSuccess, String errno,
					String errmsg, OtherSynConfigItem item) {
				// TODO Auto-generated method stub
				if( isSuccess ) {
					// 同步配置成功, 这里是主线程
					
					// 调用注册接口
					TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
					RequestJniAuthorization.Register(
							email,
							password,
							mRegisterParam.male, 
							mRegisterParam.firstname, 
							mRegisterParam.lastname, 
							mRegisterParam.country,
							mRegisterParam.year, 
							mRegisterParam.month, 
							mRegisterParam.day, 
							false,
							Build.MODEL, 
							RequestJni.GetDeviceId(tm), 
							Build.MANUFACTURER, new OnRegisterCallback() {
								
								@Override
								public void OnRegister(boolean isSuccess, String errno, String errmsg,
										RegisterItem item) {
									// TODO Auto-generated method stub
									Message msg = Message.obtain();
									MessageCallbackItem obj = new MessageCallbackItem(errno, errmsg);
									if( isSuccess ) {
										// 登录成功
										msg.what = RequestFlag.REQUEST_SUCCESS.ordinal();
										obj.item = item;
									} else {
										// 登录失败
										msg.what = RequestFlag.REQUEST_FAIL.ordinal();
									}
									obj.email = email;
									obj.password = password;
									msg.obj = obj;
									mHandler.sendMessage(msg);
								}
							});
				} else {
					Message msg = Message.obtain();
					MessageCallbackItem obj = new MessageCallbackItem(errno, errmsg);
					msg.what = RequestFlag.REQUEST_FAIL.ordinal();
					obj.email = email;
					obj.password = password;
					msg.obj = obj;
					mHandler.sendMessage(msg);
				}
			}
		});

	}

	@Override
	public void InitView() {
		// TODO Auto-generated method stub
		setContentView(R.layout.activity_register_password);
		
		editTextEmail = (MaterialTextField) findViewById(R.id.editTextEmail);
		editTextPassword = (MaterialTextField) findViewById(R.id.editTextPassword);
		
		appbar =(MaterialAppBar) findViewById(R.id.appbar);
		appbar.setAppbarBackgroundColor(getResources().getColor(R.color.white));
		appbar.setTitle(getString(R.string.Choose_a_password), Color.WHITE);
		appbar.addButtonToLeft(android.R.id.button1, "", R.drawable.ic_arrow_back_white_24dp);
		appbar.setOnButtonClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				finish();
			}
			
		});
		
		editTextEmail.setEmail();
		editTextPassword.setPassword();
		editTextEmail.setHint(getString(R.string.Enter_your_email_address));
		editTextPassword.setHint(getString(R.string.Choose_a_password));
		
		siteManager = WebSiteManager.newInstance(mContext);
		if (siteManager != null) appbar.setAppbarBackgroundColor((mContext.getResources().getColor(siteManager.GetWebSite().getSiteColor())));
		
	}

	@Override
	public void InitHandler() {
		// TODO Auto-generated method stub
		mHandler = new Handler() {
			@Override
			public void handleMessage(android.os.Message msg) {
				// 收起菊花
				hideProgressDialog();
				final MessageCallbackItem obj = (MessageCallbackItem) msg.obj;
				switch ( RequestFlag.values()[msg.what] ) {
				case REQUEST_SUCCESS:{
					if( mRegisterParam.picturePath.length() > 0 ) {
						// 此处该弹菊花
						// 注册成功, 上传头像
						RequestJniProfile.UploadHeaderPhoto(mRegisterParam.picturePath, 
								new OnRequestCallback() {
									@Override
									public void OnRequest(boolean isSuccess,
											String errno, String errmsg) {
										// TODO Auto-generated method stub
										Message msg = Message.obtain();
										if( isSuccess ) {
											// 上传头像成功
											msg.what = RequestFlag.UPLOAD_SUCCESS.ordinal();
											obj.errmsg = errmsg;
											obj.errno = errno;
										} else {
											// 上传头像失败
											msg.what = RequestFlag.REQUEST_FAIL.ordinal();
										}
										msg.obj = obj;
										mHandler.sendMessage(msg);
									}
									
								});
					} else {
						// 注册成功(自动登录成功), 跳转主界面
//						LoginManager.getInstance().Login(
//								obj.item.manid, 
//								editTextPassword.getText().toString()
//								);
						OnRegisterSuccess(obj.email, obj.password, obj.item);
					}
				}break;
				case REQUEST_FAIL:{
					// 注册失败
					Toast.makeText(mContext, obj.errmsg, Toast.LENGTH_LONG).show();
					
					// 不允许注册
					switch (obj.errno) {
					case RequestErrorCode.MBCE1004:{
						// 账号不允许注册, 但已经被记录
						editTextEmail.setError(Color.RED, true);
					}break;
					default:
						break;
					}
				}break;
				case UPLOAD_SUCCESS:{
					// 上传头像成功, 跳转主界面
				}
				case UPLOAD_FAIL:{
//					LoginManager.getInstance().Login(
//							obj.item.manid, 
//							editTextPassword.getText().toString()
//							);
					OnRegisterSuccess(obj.email, obj.password, obj.item);
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
		mRegisterParam = getIntent().getExtras().getParcelable(REGISTER_PARAM_KEY);
	}

	/**
	 * 注册成功
	 */
	public void OnRegisterSuccess(String email, String password, RegisterItem item) {
		if( item.login ) {
			// 自动登录成功, 跳转主界面
			Intent intent = new Intent(mContext, HomeActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
			
			// 通知登录模块
			LoginManager.getInstance().OnRegisterSuccess(email, password, item);
		} else {
			// 自动登录不成功, 跳转注册界面
			Intent intent = new Intent(mContext, RegisterActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
		}
		finish();
	}
	
	@Override
	public void OnLogin(boolean isSuccess, String errno, String errmsg,
			LoginItem item, LoginErrorItem errItem) {
		// TODO Auto-generated method stub
//		if( isSuccess ) {
//			// 注册成功(自动登录成功), 跳转主界面
//			Intent intent = new Intent(mContext, HomeActivity.class);
//			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//			startActivity(intent);
//		} else {
//			Intent intent = new Intent(mContext, RegisterActivity.class);
//			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//			startActivity(intent);
//		}
//		finish();
	}

	@Override
	public void OnLogout() {
		// TODO Auto-generated method stub
		
	}
}
