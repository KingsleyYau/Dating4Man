package com.qpidnetwork.dating.profile;

import java.io.File;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.qpidnetwork.dating.BaseActivity;
import com.qpidnetwork.dating.R;
import com.qpidnetwork.dating.WebViewActivity;
import com.qpidnetwork.dating.authorization.LoginManager;
import com.qpidnetwork.dating.authorization.LoginParam;
import com.qpidnetwork.dating.authorization.LoginPerfence;
import com.qpidnetwork.dating.credit.BuyCreditActivity;
import com.qpidnetwork.dating.lady.LadyListManager;
import com.qpidnetwork.framework.util.CompatUtil;
import com.qpidnetwork.framework.util.Log;
import com.qpidnetwork.framework.util.StringUtil;
import com.qpidnetwork.framework.util.UnitConversion;
import com.qpidnetwork.framework.widget.CircleImageView;
import com.qpidnetwork.manager.FileCacheManager;
import com.qpidnetwork.manager.WebSiteManager;
import com.qpidnetwork.request.OnGetMyProfileCallback;
import com.qpidnetwork.request.OnOtherGetCountCallback;
import com.qpidnetwork.request.OnRequestCallback;
import com.qpidnetwork.request.RequestJniOther;
import com.qpidnetwork.request.RequestJniProfile;
import com.qpidnetwork.request.RequestOperator;
import com.qpidnetwork.request.item.OtherGetCountItem;
import com.qpidnetwork.request.item.ProfileItem;
import com.qpidnetwork.request.item.ProfileItem.Photo;
import com.qpidnetwork.tool.ImageViewLoader;
import com.qpidnetwork.view.ChoosePhotoDialog;
import com.qpidnetwork.view.MaterialDialogAlert;
import com.qpidnetwork.view.MaterialThreeButtonDialog;
import com.qpidnetwork.view.ViewTools;

public class MyProfileActivity extends BaseActivity {
	/**
	 * 拍照
	 */
	private static final int RESULT_LOAD_IMAGE_CAPTURE = 0;
	/**
	 * 相册
	 */
	private static final int RESULT_LOAD_IMAGE_ALBUMN = 1;
	/**
	 * 裁剪图片
	 */
	private static final int RESULT_LOAD_IMAGE_CUT = 2;
	
	private enum RequestFlag {
		REQUEST_UPLOAD_SUCCESS,
		REQUEST_PROFILE_SUCCESS,
		REQUEST_COUNT_SUCCESS,
		REQUEST_FAIL,
	}
	
	/**
	 * 用户头像
	 */
	private CircleImageView imageViewHeader;
	private ImageButton imageViewTakePhoto;
	private ImageViewLoader loader = new ImageViewLoader(this);
	/**
	 *  用户名基本资料
	 */
	private TextView textViewName;
	private TextView textViewAge;
	private TextView textViewCountry;
	
	/**
	 *  详细资料项目
	 */
	private RelativeLayout layoutCreditBalance;
	private RelativeLayout layoutBonusPoints;
	private RelativeLayout layoutChatVouchers;
	private RelativeLayout layoutProfileDetails;
	private RelativeLayout layoutPhoneVerification;
	private RelativeLayout layoutChangePassword;
	private RelativeLayout layoutLogout;

	private ProfileItem mProfileItem;
	private OtherGetCountItem mOtherGetCountItem;
	
	/**
	 * 界面消息
	 */
	private class MessageCallbackItem {
		/**
		 * 
		 * @param errno				接口错误码
		 * @param errmsg			错误提示
		 * @param profileItem		登录正常返回
		 * @param loginErlady		登录错误返回
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
		public ProfileItem profileItem = null;
		public OtherGetCountItem otItem = null;
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// 获取个人信息
		GetMyProfile();
		
		// 统计男士数据
		GetCount();
	}
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		
		// 创建界面时候，获取缓存数据
		mProfileItem = MyProfilePerfence.GetProfileItem(mContext);
	}
	
	/**
	 * 点击取消
	 * @param v
	 */
	public void onClickCancel(View v) {
		finish();
	}
	
	/**
	 * 点击查看头像
	 * @param view
	 */
	public void onClickImageViewHeader(View view) {

		if ( mProfileItem == null ) return;
		
//		if ( mProfileItem.showUpload() || !mProfileItem.showPhoto() ){
//			onClickImageViewTakePhoto(view);
//			return;
//		}
		
		Intent intent = new Intent(this, MyProfilePhotoActivity.class);
		if( mProfileItem != null ) {
			intent.putExtra("profile", mProfileItem);
//			intent.putExtra(MyProfilePhotoActivity.PHOTO_URL, mProfileItem.photoURL);
//			intent.putExtra(MyProfilePhotoActivity.TIPS, "")
		}

		startActivity(intent);
	}
	
	/**
	 * 点击上传头像
	 * @param view
	 */
	public void onClickImageViewTakePhoto(View view) {
		
		MaterialThreeButtonDialog dialog = new MaterialThreeButtonDialog(this, new MaterialThreeButtonDialog.OnClickCallback() {
			
			@Override
			public void OnSecondButtonClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = CompatUtil.getSelectPhotoFromAlumIntent();
				startActivityForResult(intent, RESULT_LOAD_IMAGE_ALBUMN);
			}
			
			@Override
			public void OnFirstButtonClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
				intent.putExtra(
						android.provider.MediaStore.EXTRA_OUTPUT, 
						Uri.fromFile(new File(FileCacheManager.getInstance().GetTempImageUrl()))
								);
				
				startActivityForResult(intent, RESULT_LOAD_IMAGE_CAPTURE);
			}
			
			@Override
			public void OnCancelButtonClick(View v) {
				// TODO Auto-generated method stub
				
			}
		});
		
		dialog.show();
	}
	
	/**
	 * 点击详细
	 * @param view
	 */
	public void onClickProfileDetail(View view) {
		Intent intent = new Intent(this, MyProfileDetailActivity.class);
		startActivity(intent);
	}
	
	/**
	 * 点击手机认证
	 * @param view
	 */
	public void onClickPhoneVerify(View view) {
		Intent intent = new Intent(this, MyProfilePhoneVerifyActivity.class);
		startActivity(intent);
	}
	
	/**
	 * 点击改变密码
	 * @param view
	 */
	public void onClickChangePassword(View view) {
		Intent intent = new Intent(this, MyProfileChangePasswordActivity.class);
		startActivity(intent);
	}
	
	/**
	 * 点击注销
	 * @param view
	 */
	public void onClickLogout(View view) {
		
	}
	
	/**
	 * 获取个人信息
	 */
	public void GetMyProfile() {
		// 此处应有菊花
		showProgressDialog("Loading...");
		RequestOperator.getInstance().GetMyProfile(new OnGetMyProfileCallback() {
			@Override
			public void OnGetMyProfile(boolean isSuccess, String errno, String errmsg,
					ProfileItem item) {
				// TODO Auto-generated method stub
				Message msg = Message.obtain();
				MessageCallbackItem obj = new MessageCallbackItem(errno, errmsg);
				if( isSuccess ) {
					// 获取个人信息成功
					msg.what = RequestFlag.REQUEST_PROFILE_SUCCESS.ordinal();
					obj.profileItem = item;
				} else {
					// 获取个人信息失败
					msg.what = RequestFlag.REQUEST_FAIL.ordinal();
				}
				msg.obj = obj;
				mHandler.sendMessage(msg);
			}
		});
	}
	
	/**
	 * 统计男士数据
	 */
	public void GetCount() {
		// 此处应有菊花
		showProgressDialog("Loading...");
		RequestOperator.getInstance().GetCount(true, true, true, true, true, true, new OnOtherGetCountCallback() {
			
			@Override
			public void OnOtherGetCount(boolean isSuccess, String errno, String errmsg,
					OtherGetCountItem item) {
				// TODO Auto-generated method stub
				Message msg = Message.obtain();
				MessageCallbackItem obj = new MessageCallbackItem(errno, errmsg);
				if( isSuccess ) {
					// 统计男士数据成功
					msg.what = RequestFlag.REQUEST_COUNT_SUCCESS.ordinal();
					obj.otItem = item;
					
				} else {
					// 统计男士数据失败
					msg.what = RequestFlag.REQUEST_FAIL.ordinal();
				}
				msg.obj = obj;
				mHandler.sendMessage(msg);
			}
		});
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	    super.onActivityResult(requestCode, resultCode, data);
	 
	    switch(requestCode) {
	    case RESULT_LOAD_IMAGE_CAPTURE:{
	    	if( resultCode == RESULT_OK ) {
		        doStartPhotoZoom(
		        		Uri.fromFile(new File(FileCacheManager.getInstance().GetTempImageUrl())), 
		        		Uri.fromFile(new File(FileCacheManager.getInstance().GetTempImageUrl())));
		 
	    	}
	    }break;
	    case RESULT_LOAD_IMAGE_ALBUMN:{
	    	if( resultCode == RESULT_OK && null != data ) {
		        Uri selectedImage = data.getData();
//		        String[] filePathColumn = { MediaStore.Images.Media.DATA };
//		 
//		        Cursor cursor = getContentResolver().query(
//		        		selectedImage, filePathColumn, null, null, null);
//		        
//		        if( cursor != null ) {
//			        cursor.moveToFirst();
//					 
//			        int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
//			        String picturePath = cursor.getString(columnIndex);
//			        cursor.close();
//			        
//			        doStartPhotoZoom(
//			        		Uri.fromFile(new File(picturePath)), 
//			        		Uri.fromFile(new File(FileCacheManager.getInstance().GetTempImageUrl())));
//		        }
		        String picturePath = CompatUtil.getSelectedPhotoPath(this, selectedImage);
	    		if(!StringUtil.isEmpty(picturePath)){
	    			doStartPhotoZoom(
			        		Uri.fromFile(new File(picturePath)), 
			        		Uri.fromFile(new File(FileCacheManager.getInstance().GetTempImageUrl())));
	    		}

	    	}
	    }break;
	    case RESULT_LOAD_IMAGE_CUT:{
	    	if( resultCode == RESULT_OK) {
		        // 上传头像
		        RequestOperator.getInstance().UploadHeaderPhoto(
		        		FileCacheManager.getInstance().GetTempImageUrl(), 
		        		new OnRequestCallback() {
					
					@Override
					public void OnRequest(boolean isSuccess, String errno, String errmsg) {
						// TODO Auto-generated method stub
						Message msg = Message.obtain();
						MessageCallbackItem obj = new MessageCallbackItem(errno, errmsg);
						if( isSuccess ) {
							// 上传头像成功
							msg.what = RequestFlag.REQUEST_UPLOAD_SUCCESS.ordinal();
						} else {
							// 上传头像失败
							msg.what = RequestFlag.REQUEST_FAIL.ordinal();
						}
						msg.obj = obj;
						mHandler.sendMessage(msg);
					}
				});
	    	}
	    }break;
	    default:break;
	    }
	}
	
	/**
	 * 裁剪图片方法实现
	 * 
	 * @param photoUri	要裁剪的图片路径
	 * @param cutUri 	指定裁剪后的图片存储路径
	 */
	public void doStartPhotoZoom(Uri src, Uri dest) {
		Intent intent = new Intent("com.android.camera.action.CROP");
		intent.setDataAndType(src, "image/*");
		// 下面这个crop=true是设置在开启的Intent中设置显示的VIEW可裁剪
		intent.putExtra("crop", "true");

		// aspectX aspectY 是宽高(x y方向上)的比例，其小于1的时候可以操作截图框,若不设定，则可以任意宽度和高度
		intent.putExtra("aspectX", 4);
		intent.putExtra("aspectY", 5);
		intent.putExtra("scale", true);

		intent.putExtra("output", dest);// 指定裁剪后的图片存储路径
		intent.putExtra("outputX", 400);// outputX outputY裁剪保存的宽高(使各手机截取的图片质量一致)
		intent.putExtra("outputY", 500);

		intent.putExtra("noFaceDetection", true);// 取消人脸识别功能(系统的裁剪图片默认对图片进行人脸识别,当识别到有人脸时，会按aspectX和aspectY为1来处理)
		intent.putExtra("return-data", false);// 将相应的数据与URI关联起来，返回裁剪后的图片URI,true返回bitmap
		intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
		startActivityForResult(intent, RESULT_LOAD_IMAGE_CUT);
	}

	@Override
	public void InitView() {
		setContentView(R.layout.activity_my_profile);
		
		if (Build.VERSION.SDK_INT >= 21){
			((RelativeLayout.LayoutParams)findViewById(R.id.layoutImageHeader).getLayoutParams()).topMargin += UnitConversion.dip2px(this, 18);
			((RelativeLayout.LayoutParams)findViewById(R.id.buttonCancel).getLayoutParams()).topMargin += UnitConversion.dip2px(this, 18);
		}
		
		imageViewHeader = (CircleImageView)findViewById(R.id.imageViewHeader);
		imageViewTakePhoto = (ImageButton)findViewById(R.id.imageViewTakePhoto);
		textViewName = (TextView) findViewById(R.id.textViewName);
		textViewAge = (TextView) findViewById(R.id.textViewAge);
		textViewCountry = (TextView) findViewById(R.id.textViewCountry);
		
		layoutCreditBalance = (RelativeLayout) findViewById(R.id.layoutCreditBalance);
		((TextView) layoutCreditBalance.findViewById(R.id.textViewLeft)).setText("Credit Balance");
		((TextView) layoutCreditBalance.findViewById(R.id.textViewRight)).setText("");
		layoutCreditBalance.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(mContext, BuyCreditActivity.class);
				startActivity(intent);
			}
			
		});
		
		layoutBonusPoints = (RelativeLayout) findViewById(R.id.layoutBonusPoints);
		((TextView) layoutBonusPoints.findViewById(R.id.textViewLeft)).setText("Bonus Points");
		((TextView) layoutBonusPoints.findViewById(R.id.textViewRight)).setText("");
		layoutBonusPoints.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				String url = WebSiteManager.newInstance(mContext).GetWebSite().getBounsLink();
				Intent intent = WebViewActivity.getIntent(mContext, url);
				intent.putExtra(WebViewActivity.WEB_TITLE, "Bouns Points");
				startActivity(intent);
			}
		});
		
		layoutChatVouchers = (RelativeLayout) findViewById(R.id.layoutChatVouchers);
		((TextView) layoutChatVouchers.findViewById(R.id.textViewLeft)).setText("Chat Vouchers");
		((TextView) layoutChatVouchers.findViewById(R.id.textViewRight)).setText("");
		layoutChatVouchers.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				
				String title;
				
				if (mOtherGetCountItem == null){
					title = "You have " + 0 + " vourchers";
				}else{
					title = "You have " + mOtherGetCountItem.coupon + " vourchers";
				}
				
				final MaterialDialogAlert dialog = new MaterialDialogAlert(MyProfileActivity.this);
				dialog.setTitle(title);
				dialog.setMessage(getString(R.string.myprofile_free_chat));
				dialog.addButton(dialog.createButton("OK", null));
				
				dialog.show();
			}
		});
		
		
		layoutProfileDetails = (RelativeLayout) findViewById(R.id.layoutProfileDetails);
		((TextView) layoutProfileDetails.findViewById(R.id.textViewLeft)).setText("Profile Details");
		((TextView) layoutProfileDetails.findViewById(R.id.textViewRight)).setText("");
		layoutProfileDetails.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				onClickProfileDetail(v);
			}
		});
		
		layoutPhoneVerification = (RelativeLayout) findViewById(R.id.layoutPhoneVerification);
		((TextView) layoutPhoneVerification.findViewById(R.id.textViewLeft)).setText("Phone Verification");
		((TextView) layoutPhoneVerification.findViewById(R.id.textViewRight)).setText("");
		layoutPhoneVerification.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				onClickPhoneVerify(v);
			}
		});
		
		layoutChangePassword = (RelativeLayout) findViewById(R.id.layoutChangePassword);
		((TextView) layoutChangePassword.findViewById(R.id.textViewLeft)).setText("Change Password");
		((TextView) layoutChangePassword.findViewById(R.id.textViewRight)).setText("");
		layoutChangePassword.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				onClickChangePassword(v);
			}
		});
		
		layoutLogout = (RelativeLayout) findViewById(R.id.layoutLogout);
		((TextView) layoutLogout.findViewById(R.id.textViewLeft)).setText("Log Out");
		((TextView) layoutLogout.findViewById(R.id.textViewRight)).setText("");
		layoutLogout.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				LoginManager.getInstance().LogoutAndClean();
				finish();
			}
		});
	}
	
	@Override
	public void InitHandler() {
		// TODO Auto-generated method stub
		mHandler = new Handler() {
			@Override
			public void handleMessage(android.os.Message msg) {
				// 收起菊花
				hideProgressDialog();
				MessageCallbackItem obj = (MessageCallbackItem) msg.obj;
				switch ( RequestFlag.values()[msg.what] ) {
				case REQUEST_UPLOAD_SUCCESS:{
					// 上传头像成功
					// 重新获取个人信息
					GetMyProfile();
					
				}break;
				case REQUEST_PROFILE_SUCCESS:{
					// 获取个人信息成功
					
					// 缓存个人信息
					mProfileItem = obj.profileItem;
					MyProfilePerfence.SaveProfileItem(mContext, mProfileItem);

					// 刷新界面
					if( mProfileItem != null ) {
						String url = mProfileItem.photoURL;
						String localPath = FileCacheManager.getInstance().CacheImagePathFromUrl(url);
						
						// Add by Martin
						// 只有不是未审核的状态才显示头像
						if ( mProfileItem.showPhoto() ) {
							Log.v("photo load", mProfileItem.photoURL);
							ViewTools.PreCalculateViewSize(imageViewHeader);ViewTools.PreCalculateViewSize(imageViewHeader);
							loader.DisplayImage(
									imageViewHeader, 
									url, 
									imageViewHeader.getWidth(),
									imageViewHeader.getHeight(),
									0,
									0,
									localPath,
									null
									);
						}
						
						textViewName.setText(mProfileItem.firstname +" " + mProfileItem.lastname);
						textViewAge.setText(String.valueOf(mProfileItem.age));
						textViewCountry.setText(mProfileItem.country.name());
						
						if ( mProfileItem.showUpload() ){
							imageViewTakePhoto.setVisibility(View.VISIBLE);
						} else {
							imageViewTakePhoto.setVisibility(View.GONE);
						}
					}

				}break;
				case REQUEST_COUNT_SUCCESS:{
					// 统计男士数据成功
					// 缓存信用点
					mOtherGetCountItem = obj.otItem;
					MyProfilePerfence.SaveOtherGetCountItem(mContext, mOtherGetCountItem);
					
					// 刷新界面
					if( mOtherGetCountItem != null ) {
						((TextView) layoutCreditBalance.findViewById(R.id.textViewRight)).setText(
								String.valueOf(mOtherGetCountItem.money));
						
						((TextView) layoutBonusPoints.findViewById(R.id.textViewRight)).setText(
								String.valueOf(mOtherGetCountItem.integral));
						
						((TextView) layoutChatVouchers.findViewById(R.id.textViewRight)).setText(
								String.valueOf(mOtherGetCountItem.coupon));
					}

				}break;
				case REQUEST_FAIL:{
					// 请求失败
					Toast.makeText(mContext, obj.errmsg, Toast.LENGTH_LONG).show();	
				}break;
				default:
					break;
				}
			};
		};
	}
}
