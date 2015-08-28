package com.qpidnetwork.dating.profile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileLock;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.qpidnetwork.dating.BaseActivity;
import com.qpidnetwork.dating.R;
import com.qpidnetwork.dating.bean.RequestBaseResponse;
import com.qpidnetwork.framework.util.CompatUtil;
import com.qpidnetwork.framework.util.StringUtil;
import com.qpidnetwork.framework.util.UnitConversion;
import com.qpidnetwork.manager.FileCacheManager;
import com.qpidnetwork.request.OnRequestCallback;
import com.qpidnetwork.request.RequestOperator;
import com.qpidnetwork.request.item.ProfileItem;
import com.qpidnetwork.request.item.ProfileItem.Photo;
import com.qpidnetwork.request.item.ProfileItem.VType;
import com.qpidnetwork.tool.ImageViewLoader;
import com.qpidnetwork.view.MaterialThreeButtonDialog;

/**
 * MyProfile模块
 * @author Max.Chiu
 */
public class MyProfilePhotoActivity extends BaseActivity {
	
	private ProfileItem mProfile;
	
	public static final String PHOTO_URL = "PHOTO_URL";
	private String mPhotoUrl;
	
	
	public static final String TIPS = "TIPS";
	private String mTips;
	
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
		REQUEST_FAIL,
	}
	
	/**
	 * 用户头像
	 */
	private ImageView imageViewHeader;
	private ImageViewLoader loader = new ImageViewLoader(this);
	
//	private LinearLayout imageViewChangePhoto;
	private ImageButton buttonChange;
	
	/**
	 * 提示
	 */
	private TextView textViewTips;
	private ImageButton buttonCancel;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		this.getWindow().setBackgroundDrawable(new ColorDrawable(Color.BLACK));
		
		mProfile = (ProfileItem) getIntent().getExtras().getSerializable("profile");
		mPhotoUrl = mProfile.photoURL;
		mTips = "";
		
		// QS
		if( mProfile.v_id == VType.Verifing && mProfile.photo == Photo.Yes ) {
			mTips = getResources().getString(R.string.my_profile_photo_tips_under_seal_temp_lock);
		} else if( mProfile.v_id == VType.Pass && mProfile.photo == Photo.Yes) {
			mTips = getResources().getString(R.string.my_profile_photo_tips_seal_fine_lock);
		} else  if(mProfile.photo == Photo.Verifing) {
			mTips = getResources().getString(R.string.my_profile_photo_tips_under_review);
		} else {
			// 允许改变
			mTips = getResources().getString(R.string.my_profile_change_photo);
			
			// 显示button
			buttonChange.setVisibility(View.VISIBLE);
		}
		
		textViewTips.setText(mTips);
		if( mProfile.showPhoto() ) {
			loader.DisplayImage(imageViewHeader, mPhotoUrl, 
					FileCacheManager.getInstance().CacheImagePathFromUrl(mPhotoUrl), null);
		}

	}
	
	/**
	 * 点击取消
	 * @param v
	 */
	public void onClickCancel(View v) {
		finish();
	}
	
	/**
	 * 点击改变图片
	 * @param view
	 */
	public void onClickChangePhoto(View view) {
		
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
	    	if( resultCode == RESULT_OK){
	//	    	imageViewHeader.setImageBitmap(BitmapFactory.decodeFile(FileCacheManager.getInstance().GetTempImageUrl()));
				try {
					Bitmap bitmap = BitmapFactory.decodeFile(FileCacheManager.getInstance().GetTempImageUrl());
					
					// 写入压纹图片数据
					File file = new File(FileCacheManager.getInstance().GetTempImageUrl());
					FileOutputStream fOut = null;
					fOut = new FileOutputStream(file);
				    FileLock fl = ((FileOutputStream) fOut).getChannel().tryLock();  
				    if (fl != null) {
				    	bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fOut);
				    	fl.release();
				    }
				    fOut.close();
			    	
			        // 上传头像
			        RequestOperator.getInstance().UploadHeaderPhoto(
			        		FileCacheManager.getInstance().GetTempImageUrl(), 
			        		new OnRequestCallback() {
						
						@Override
						public void OnRequest(boolean isSuccess, String errno, String errmsg) {
							// TODO Auto-generated method stub
							Message msg = Message.obtain();
							RequestBaseResponse obj = new RequestBaseResponse(isSuccess, errno, errmsg, null);
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
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
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
	
	/**
	 * 初始化界面
	 */
	public void InitView() {
		setContentView(R.layout.activity_my_profile_photo);
		
		imageViewHeader = (ImageView) findViewById(R.id.imageViewHeader);
//		imageViewChangePhoto = (LinearLayout ) findViewById(R.id.imageViewChangePhoto);
		textViewTips = (TextView) findViewById(R.id.textViewTips);
		buttonChange = (ImageButton) findViewById(R.id.buttonChange);
		buttonChange.setVisibility(View.GONE);
		buttonCancel = (ImageButton) findViewById(R.id.buttonCancel);
		
		if (Build.VERSION.SDK_INT >= 21){
			buttonCancel.getLayoutParams().height = UnitConversion.dip2px(this, 48);
			buttonCancel.getLayoutParams().width = UnitConversion.dip2px(this, 48);
			((RelativeLayout.LayoutParams)buttonCancel.getLayoutParams()).topMargin = UnitConversion.dip2px(this, 18);
		}
	}
	
	@Override
	public void InitHandler() {
		// TODO Auto-generated method stub
		mHandler = new Handler() {
			@Override
			public void handleMessage(android.os.Message msg) {
				// 收起菊花
				RequestBaseResponse obj = (RequestBaseResponse) msg.obj;
				switch ( RequestFlag.values()[msg.what] ) {
				case REQUEST_UPLOAD_SUCCESS:{
					// 上传头像成功
					
					// 清除旧头像缓存
					FileCacheManager.getInstance().CleanCacheImageFromUrl(mPhotoUrl);
					// 重新下载
					loader.DisplayImage(imageViewHeader, mPhotoUrl, 
							FileCacheManager.getInstance().CacheImagePathFromUrl(mPhotoUrl), null);
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
