package com.qpidnetwork.dating.authorization;

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
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;

import com.qpidnetwork.framework.base.BaseFragmentActivity;
import com.qpidnetwork.dating.R;
import com.qpidnetwork.dating.authorization.RegisterPasswordActivity.RegisterParam;
import com.qpidnetwork.dating.profile.MyProfileSelectCountryActivity;
import com.qpidnetwork.framework.util.CompatUtil;
import com.qpidnetwork.framework.util.StringUtil;
import com.qpidnetwork.manager.ConfigManager;
import com.qpidnetwork.manager.FileCacheManager;
import com.qpidnetwork.manager.WebSiteManager;
import com.qpidnetwork.request.RequestEnum.Country;
import com.qpidnetwork.view.CheckButton;
import com.qpidnetwork.view.CheckButton.OnCheckLinstener;
import com.qpidnetwork.view.MaterialAppBar;
import com.qpidnetwork.view.MaterialDatePickerDialog;
import com.qpidnetwork.view.MaterialTextField;
import com.qpidnetwork.view.MaterialTextField.OnFocuseChangedCallback;
import com.qpidnetwork.view.MaterialThreeButtonDialog;

/**
 * 认证模块
 * Email注册界面
 * @author Max.Chiu
 *
 */
public class RegisterByEmailActivity extends BaseFragmentActivity
									 implements MaterialThreeButtonDialog.OnClickCallback,
									 			MaterialDatePickerDialog.DateSelectCallback,
									 			OnCheckLinstener,
									 			OnFocuseChangedCallback
{
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
	/**
	 * 编辑国家
	 */
	private static final int RESULT_COUNTRY = 4;
	
	
	private MaterialAppBar appbar;
	private View lastFocusedView;
	private ImageView imageViewHeader;
	private CheckButton checkButtonMale;
	private CheckButton checkButtonFemale;
	private MaterialTextField editTextFirstName;
	private MaterialTextField editTextLastName;
	private MaterialTextField editTextViewCountry;
	private MaterialTextField editTextViewBirthday;
//	private ButtonRaised buttonCoutinue;
	private WebSiteManager siteManager;
	
	private RegisterParam mRegisterParam = new RegisterParam();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.getWindow().setBackgroundDrawable(new ColorDrawable(getResources().getColor(WebSiteManager.getInstance().GetWebSite().getSiteColor())));
		// 初始化界面
		InitView();
		
		siteManager = WebSiteManager.newInstance(mContext);
		if (siteManager != null) appbar.setAppbarBackgroundColor((mContext.getResources().getColor(siteManager.GetWebSite().getSiteColor())));
		
		//初始化根据Ip初始化设置
		ConfigManager cm = ConfigManager.getInstance();
		String[] countries = getResources().getStringArray(R.array.country_without_code);
		if((cm != null) && (cm.getSynConfigItem() != null)){
			Country ipCountry = cm.getSynConfigItem().pub.ipcountry;
			if(ipCountry != Country.Other){
				editTextViewCountry.setText(countries[ipCountry.ordinal()]);
				mRegisterParam.country = ipCountry;
			}
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
	 * 点击头像
	 * @param v
	 */
	public void onClickImageHeader(View v) {
		/*final ChoosePhotoDialog dialog = new ChoosePhotoDialog(this, R.style.ChoosePhotoDialog);
        dialog.show();
        dialog.setOnTakePhotoClickListerner(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				dialog.dismiss();
				Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
				intent.putExtra(
						MediaStore.EXTRA_OUTPUT, 
						Uri.fromFile(new File(FileCacheManager.getInstance().GetTempCameraImageUrl()))
								);
				startActivityForResult(intent, RESULT_LOAD_IMAGE_CAPTURE);
			}
		});
        dialog.setOnSelectPhotoClickListerner(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				dialog.dismiss();
				Intent i = CompatUtil.getSelectPhotoFromAlumIntent();
				startActivityForResult(i, RESULT_LOAD_IMAGE_ALBUMN);
			}
		});
        dialog.setOnCancelClickListerner(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				dialog.dismiss();
			}
		});*/
		
		
		MaterialThreeButtonDialog dialog = new MaterialThreeButtonDialog(this, this);
		
		dialog.show();
	}
	
	/**
	 * 点击国家
	 * @param v
	 */
	public void onClickCountry(View v) {
//		mRegisterParam.country = Country.China;
		Intent intent = new Intent(this, MyProfileSelectCountryActivity.class);
		intent.putExtra(MyProfileSelectCountryActivity.WITHOUT_CODE, true);
		startActivityForResult(intent, RESULT_COUNTRY);
	}
	
	/**
	 * 点击出生日期
	 * @param v
	 */
	public void onClickBirthday(View v) {
		int year = (mRegisterParam.year.length() > 0) ? Integer.parseInt(mRegisterParam.year) : 1970;
		int month = (mRegisterParam.month.length() > 0) ? Integer.parseInt(mRegisterParam.month): 01;
		int date = (mRegisterParam.day.length() > 0) ? Integer.parseInt(mRegisterParam.day) : 01;
		
        MaterialDatePickerDialog datePicker = new MaterialDatePickerDialog(this, this,  year,  month, date);
        datePicker.show();



		
		
	}
	
	/**
	 * 点击Continue
	 * @param v
	 */
	public void onClickContinue(View v) {
		mRegisterParam.firstname = editTextFirstName.getText().toString();
		mRegisterParam.lastname = editTextLastName.getText().toString();
		
		if (editTextFirstName.getText().length() < 3){
			editTextFirstName.setError(Color.RED, true);
			return;
		}
		
		if (editTextLastName.getText().length() < 3){
			editTextLastName.setError(Color.RED, true);
			return;
		}

		if (editTextViewCountry.getText().length() < 3){
			editTextViewCountry.setError(Color.RED, true);
			return;
		}
		
		if (editTextViewBirthday.getText().length() < 3){
			editTextViewBirthday.setError(Color.RED, true);
			return;
		}
		
		if( checkButtonMale.IsChecked() ) {
			mRegisterParam.male = true;
		} else {
			mRegisterParam.male = false;
		}
		
		Intent intent = new Intent(this, RegisterPasswordActivity.class);
		intent.putExtra(RegisterPasswordActivity.REGISTER_PARAM_KEY, mRegisterParam);
		startActivity(intent);
	}
	
	@Override
	public void InitView() {
		setContentView(R.layout.activity_register_by_email);
		
		appbar = (MaterialAppBar)findViewById(R.id.appbar);
		appbar.setAppbarBackgroundColor(getResources().getColor(R.color.blue_color));
		appbar.addButtonToLeft(android.R.id.button1, "", R.drawable.ic_arrow_back_white_24dp);
		appbar.setTitle(getString(R.string.Create_your_account), Color.WHITE);
		appbar.setOnButtonClickListener(this);
		
		imageViewHeader = (ImageView) findViewById(R.id.imageViewHeader);
//		buttonCoutinue = (ButtonRaised)findViewById(R.id.buttonCoutinue);
		
		checkButtonMale = (CheckButton) findViewById(R.id.buttonMale);
		checkButtonMale.SetText("Man looking for woman");
		checkButtonMale.SetOnCheckChangeListener(this);
		checkButtonMale.SetChecked(true);
		
		checkButtonFemale = (CheckButton) findViewById(R.id.buttonFemale);
		checkButtonFemale.SetText("Woman looking for man");
		checkButtonFemale.SetOnCheckChangeListener(this);
		checkButtonFemale.SetChecked(false);
		
		editTextFirstName = (MaterialTextField) findViewById(R.id.editTextFirstName);
		editTextLastName = (MaterialTextField) findViewById(R.id.editTextLastName);
		editTextViewCountry = (MaterialTextField) findViewById(R.id.editTextViewCountry);
		editTextViewBirthday = (MaterialTextField) findViewById(R.id.editTextViewBirthday);
		
		editTextFirstName.setNoPredition();
		editTextFirstName.setHint(getString(R.string.first_name));

		editTextLastName.setNoPredition();
		editTextLastName.setHint(getString(R.string.last_name));
		
		editTextViewCountry.setNoPredition();
		editTextViewCountry.setHint(getString(R.string.your_nationality));
		editTextViewCountry.setFocusable(false);
		
		editTextViewBirthday.setNoPredition();
		editTextViewBirthday.setHint(getString(R.string.your_birthday));
		
		lastFocusedView = editTextFirstName.getEditor();
		editTextFirstName.setOnFocusChangedCallback(this);
		editTextLastName.setOnFocusChangedCallback(this);
		editTextViewCountry.setOnFocusChangedCallback(this);
		editTextViewBirthday.setOnFocusChangedCallback(this);
		
		
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	    super.onActivityResult(requestCode, resultCode, data);
	 
	    switch(requestCode) {
	    case RESULT_LOAD_IMAGE_CAPTURE:{
	    	if( resultCode == RESULT_OK ) {
		        doStartPhotoZoom(
		        		Uri.fromFile(new File(FileCacheManager.getInstance().GetTempCameraImageUrl())), 
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
		    	// 先压缩
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
					
				    imageViewHeader.setScaleType(ScaleType.CENTER_CROP);
			        imageViewHeader.setImageBitmap(BitmapFactory.decodeFile(FileCacheManager.getInstance().GetTempImageUrl()));
			        mRegisterParam.picturePath = FileCacheManager.getInstance().GetTempImageUrl();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	    	}

	    }break;
	    case RESULT_COUNTRY:{
	    	// 选择国家码返回
	    	if( resultCode == RESULT_OK ) {
	    		int postion = data.getExtras().getInt(MyProfileSelectCountryActivity.RESULT_COUNTRY_INDEX);
	    		String[] countries = getResources().getStringArray(R.array.country_without_code);
	    		editTextViewCountry.setText(countries[postion]);
	    		mRegisterParam.country = Country.values()[postion];
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
	public void OnFirstButtonClick(View v) {
		// TODO Auto-generated method stub
		Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		intent.putExtra(
				android.provider.MediaStore.EXTRA_OUTPUT, 
				Uri.fromFile(new File(FileCacheManager.getInstance().GetTempCameraImageUrl()))
						);
		
		startActivityForResult(intent, RESULT_LOAD_IMAGE_CAPTURE);
	}

	@Override
	public void OnSecondButtonClick(View v) {
		// TODO Auto-generated method stub
		Intent intent = CompatUtil.getSelectPhotoFromAlumIntent();
		startActivityForResult(intent, RESULT_LOAD_IMAGE_ALBUMN);
	}

	@Override
	public void OnCancelButtonClick(View v) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onDateSelected(int year, int month, int day) {
		// TODO Auto-generated method stub
		String year1 = String.valueOf(year);
	    String month1 = String.valueOf(month + 1);
	    String day1 = String.valueOf(day);
	    
	    mRegisterParam.year = year1;
	    mRegisterParam.month = month1;
	    mRegisterParam.day = day1;
	    editTextViewBirthday.setText(day1 + "/" + month1 + "/" + year1);
	}
	
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch(v.getId())
		{
		case android.R.id.button1: {
			finish();
		} break;
		}
	}

	@Override
	public void onCheckedChange(View v, boolean bChecked) {
		// TODO Auto-generated method stub
		if (v == checkButtonMale) {
			checkButtonFemale.SetChecked(!bChecked);
		}
		else if (v == checkButtonFemale) {
			checkButtonMale.SetChecked(!bChecked);
		}
	}

	@Override
	public void onFocuseChanged(View v, boolean hasFocus) {
		// TODO Auto-generated method stub
		if (v.equals(editTextViewCountry.getEditor())){
			
			if (hasFocus){
				onClickCountry(v);
				lastFocusedView.requestFocus();
			}
			
			
		}else if(v.equals(editTextViewBirthday.getEditor())){
			if (hasFocus){
				onClickBirthday(v);
				lastFocusedView.requestFocus();
			}
		}else{
			if (hasFocus) lastFocusedView = v;
		}
	}
}
