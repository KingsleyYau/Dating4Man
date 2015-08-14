package com.qpidnetwork.framework.base;

import java.lang.ref.WeakReference;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;

import com.qpidnetwork.dating.R;
import com.qpidnetwork.dating.googleanalytics.GAFragmentActivity;
import com.qpidnetwork.view.FlatToast;
import com.qpidnetwork.view.MaterialProgressDialog;

/**
 * 提供刷新UI的Handler
 * @author Hunter 
 * @since 2015.5.13
 */
public class BaseFragmentActivity extends GAFragmentActivity implements OnClickListener{
	
	MaterialProgressDialog progressDialog;
	protected FlatToast mToast;
	
	private boolean isActivityVisible = true;//判断activity是否可见，用于处理异步Dialog显示 windowToken异常
	
	@Override
	protected void onCreate(Bundle arg0) {
		// TODO Auto-generated method stub
		super.onCreate(arg0);
		progressDialog = new MaterialProgressDialog(this);
		progressDialog.setCanceledOnTouchOutside(false);
	}
	
	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
	}
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		isActivityVisible = true;
	}
	
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		isActivityVisible = false;
	}
	
	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
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
	 * 显示progressDialog
	 * @param tips 提示文字
	 */
	public void showProgressDialog(String tips){
		if(!progressDialog.isShowing() && isActivityVisible){
			progressDialog.setMessage(tips);
			progressDialog.show();
		}
	}
	
	/**
	 * 隐藏progressDialog
	 */
	public void hideProgressDialog(){
		if((progressDialog != null)&&(progressDialog.isShowing())){
			progressDialog.dismiss();
		}
	}

	protected Handler mUiHandler = new UiHandler(this) {
        public void handleMessage(android.os.Message msg) {
            super.handleMessage(msg);
            if (getActivityReference() != null && getActivityReference().get() != null) {
                handleUiMessage(msg);
            }
        };
    };
    
    private static class UiHandler extends Handler {
        private final WeakReference<BaseFragmentActivity> mActivityReference;

        public UiHandler(BaseFragmentActivity activity) {
            mActivityReference = new WeakReference<BaseFragmentActivity>(activity);
        }

        public WeakReference<BaseFragmentActivity> getActivityReference() {
            return mActivityReference;
        }
    }
    
    /**
     * 判断当前activity是否可见，用于Dialog显示判断Token使用
     * @return
     */
    public boolean isActivityVisible(){
    	return isActivityVisible;
    }
    
    /**
     * 处理更新UI任务
     * 
     * @param msg
     */
    protected void handleUiMessage(Message msg) {
    }
    
    /**
     * 发送UI更新操作
     * 
     * @param msg
     */
    protected void sendUiMessage(Message msg) {
        mUiHandler.sendMessage(msg);
    }

    protected void sendUiMessageDelayed(Message msg, long delayMillis) {
        mUiHandler.sendMessageDelayed(msg, delayMillis);
    }

    /**
     * 发送UI更新操作
     * 
     * @param what
     */
    protected void sendEmptyUiMessage(int what) {
        mUiHandler.sendEmptyMessage(what);
    }

    protected void sendEmptyUiMessageDelayed(int what, long delayMillis) {
        mUiHandler.sendEmptyMessageDelayed(what, delayMillis);
    }
    
    /**
     * 隐藏软键盘
     */
    protected void hideSoftInput() {
        InputMethodManager manager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        // manager.toggleSoftInput(InputMethodManager.HIDE_NOT_ALWAYS, 0);
        if (getCurrentFocus() != null) {
            manager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),
                    InputMethodManager.HIDE_NOT_ALWAYS);
        }

    }

	public void shakeView(View v, boolean vibrate){
		
		if(vibrate){
			try{
				Vibrator vibrator = (Vibrator)this.getSystemService(Context.VIBRATOR_SERVICE);  
		        long [] pattern = {100, 200, 0};   //stop | vibrate | stop | vibrate
		        vibrator.vibrate(pattern, -1); 
			}catch(Exception e){
				//No vibrate if no permission
			}
		}
		v.requestFocus();
		Animation shake = AnimationUtils.loadAnimation(this, R.anim.shake_anim);
		v.startAnimation(shake);
	}
	
    
    /**
     * 显示软键盘
     */
    protected void showSoftInput() {
        InputMethodManager manager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        manager.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
    }

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		
	}
}