package com.qpidnetwork.dating;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;

import com.qpidnetwork.dating.googleanalytics.GAFragmentActivity;
import com.qpidnetwork.view.FlatToast;
import com.qpidnetwork.view.MaterialProgressDialog;

/**
 * Actiivity基类
 */
public abstract class BaseActivity extends GAFragmentActivity {
	protected Handler mHandler = null;
	protected Activity mContext;
	
	protected MaterialProgressDialog progressDialog;
	protected int mProgressDialogCount = 0;
	protected FlatToast mToast;
	
	protected boolean mbFirstOnResume = true;
	
	private boolean isActivityVisible = true;//判断activity是否可见，用于处理异步Dialog显示 windowToken异常
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mContext = this;
		
		mProgressDialogCount = 0;
		progressDialog = new MaterialProgressDialog(this);
		progressDialog.setCanceledOnTouchOutside(false);

		// 初始化界面
		InitView();
		
		// 初始化事件监听
		InitHandler();

	}
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		mbFirstOnResume = false;
		isActivityVisible = true;
	}
	
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		isActivityVisible = false; 
	}
	
	/**
	 * activity 是否可见
	 * @return
	 */
	public boolean isActivityVisible(){
		return isActivityVisible;
	}
	
	@Override
	protected void onDestroy(){
		super.onDestroy();
		cancelToastImmediately();
	}
	
	public void showToastProgressing(String text){
		
		if (mToast != null){
			if (mToast.isShowing())mToast.cancel();
			mToast = null;
		}
		mToast = new FlatToast(this);
		mToast.setProgressing(text);
		mToast.show();
	}
	
	public void showToastDone(String text){
		if( mToast != null && mToast.isShowing() )
			mToast.setDone(text);
	}
	
	public void showToastFailed(String text){
		
		if (mToast != null && mToast.isShowing()){
			mToast.setFailed(text);
		}
	}
	
	public void cancelToast() {
		if (mToast != null){
			if (mToast.isShowing()){
				mToast.cancel();
			}
			mToast = null;
		}
	}
	
	public void cancelToastImmediately() {
		if (mToast != null){
			if (mToast.isShowing()){
				mToast.cancelImmediately();
			}
			mToast = null;
		}
	}
	
	/**
	 * 初始化界面
	 */
	public abstract void InitView();
	
	/**
	 * 初始化事件监听
	 */
	public abstract void InitHandler();
	
	/**
	 * 显示progressDialog
	 * @param tips 提示文字
	 */
	public void showProgressDialog(String tips) {
		cancelToast();  // cancel flat toast if it's showing before popuping a dialog.
		mProgressDialogCount++;
		if( !progressDialog.isShowing() && isActivityVisible) {
			progressDialog.setMessage(tips);
			progressDialog.show();
		}
	}
	
	/**
	 * 隐藏progressDialog
	 */
	public void hideProgressDialog() {
		try {
			if( mProgressDialogCount > 0 ) {
				mProgressDialogCount--;
				if( mProgressDialogCount == 0 && progressDialog != null ) {
					progressDialog.dismiss();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}
