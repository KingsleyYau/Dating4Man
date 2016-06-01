package com.qpidnetwork.framework.base;

import java.lang.ref.WeakReference;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;

import com.qpidnetwork.dating.QpidApplication;
import com.qpidnetwork.dating.R;
import com.qpidnetwork.dating.authorization.LoginActivity;
import com.qpidnetwork.dating.authorization.RegisterActivity;
import com.qpidnetwork.dating.contacts.ContactManager;
import com.qpidnetwork.dating.googleanalytics.AnalyticsFragmentActivity;
import com.qpidnetwork.dating.home.HomeActivity;
import com.qpidnetwork.livechat.jni.LiveChatClientListener.KickOfflineType;
import com.qpidnetwork.view.FlatToast;
import com.qpidnetwork.view.MaterialProgressDialog;

/**
 * 提供刷新UI的Handler
 * @author Hunter 
 * @since 2015.5.13
 */
public abstract class BaseFragmentActivity extends AnalyticsFragmentActivity implements OnClickListener{
	
	public static final String LIVECHAT_KICKOFF_ACTION = "kickoff";
	
	protected Activity mContext;
	protected FlatToast mToast;
	protected MaterialProgressDialog progressDialog;
	protected int mProgressDialogCount = 0;
	
	private boolean isActivityVisible = false;//判断activity是否可见，用于处理异步Dialog显示 windowToken异常
	
	/**
	 * 初始化界面
	 */
	public abstract void InitView();
	
	
	private BroadcastReceiver kickoffReceiver = new BroadcastReceiver(){
		public void onReceive(android.content.Context context, android.content.Intent intent) {
			String action = intent.getAction();
			if(action.equals(LIVECHAT_KICKOFF_ACTION)){
				Bundle bundle = intent.getExtras();
				if(bundle != null && bundle.containsKey(ContactManager.LIVE_CHAT_KICK_OFF)){
					final KickOfflineType type = KickOfflineType.values()[bundle.getInt(ContactManager.LIVE_CHAT_KICK_OFF)];
					Intent jumpIntent = new Intent(BaseFragmentActivity.this, HomeActivity.class);
					jumpIntent.putExtra(ContactManager.LIVE_CHAT_KICK_OFF, type);
					jumpIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					startActivity(jumpIntent);
				}
			}
		};
	};
	
	@Override
	protected void onCreate(Bundle arg0) {
		// TODO Auto-generated method stub
		super.onCreate(arg0);
		
		mContext = this;
		
		mProgressDialogCount = 0;
		progressDialog = new MaterialProgressDialog(this);
		progressDialog.setCanceledOnTouchOutside(false);
		
		// 初始化界面
		InitView();
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
		IntentFilter filter = new IntentFilter();
		filter.addAction(LIVECHAT_KICKOFF_ACTION);
		registerReceiver(kickoffReceiver, filter);
		
		if(QpidApplication.isKickOff){
			Intent intent = null;
			if(!(this instanceof RegisterActivity)&& (!(this instanceof LoginActivity))){
				if(this instanceof HomeActivity){
					Intent loginIntent = new Intent(this,
							RegisterActivity.class);
					startActivity(loginIntent);
				}else{
					intent = new Intent(BaseFragmentActivity.this, HomeActivity.class);
					intent.putExtra(ContactManager.LIVE_CHAT_KICK_OFF, QpidApplication.kickOffType);
					intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					startActivity(intent);
				}
			}
		}
	}
	
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		isActivityVisible = false;
		try{
			unregisterReceiver(kickoffReceiver);
		}catch(IllegalArgumentException e){
			e.printStackTrace();
		}
	}
	
	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
	}
	

	@Override
	protected void onDestroy(){
		super.onDestroy();
		/*防止异常杀死界面重启后，dialog 失去windowDecor导致调用Dismiss IllegalArgumentException*/
		hideProgressDialog();
		cancelToastImmediately();
	}
	
	public void showToastProgressing(String text){
		if ( mToast != null ){
			if (mToast.isShowing())mToast.cancel();
			mToast = null;
		}
		mToast = new FlatToast(this);
		mToast.setProgressing(text);
		if(isActivityVisible){
			mToast.show();
		}
	}
	
	public void showToastDone(String text){
		if( mToast != null && mToast.isShowing() ) {
			mToast.setDone(text);
		}
	}
	
	public void showToastFailed(String text){
		if ( mToast != null && mToast.isShowing() ){
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
	public void hideProgressDialog(){
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

	protected Handler mHandler = new UiHandler(this) {
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
    	mHandler.sendMessage(msg);
    }

    protected void sendUiMessageDelayed(Message msg, long delayMillis) {
    	mHandler.sendMessageDelayed(msg, delayMillis);
    }

    /**
     * 发送UI更新操作
     * 
     * @param what
     */
    protected void sendEmptyUiMessage(int what) {
    	mHandler.sendEmptyMessage(what);
    }

    protected void sendEmptyUiMessageDelayed(int what, long delayMillis) {
    	mHandler.sendEmptyMessageDelayed(what, delayMillis);
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
