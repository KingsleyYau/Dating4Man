package com.qpidnetwork.dating;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;

public abstract class BaseFragment extends Fragment {
	protected Handler mHandler = null;
	protected Context mContext;
	
	protected ProgressDialog progressDialog;
	protected int mProgressDialogCount = 0;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
		// 初始化事件监听
		InitHandler();
    }
	
    @Override
    public void onAttach(Activity activity) {
    	// TODO Auto-generated method stub
    	super.onAttach(activity);
    	mContext = activity;
    }
    
	/**
	 * 初始化事件监听
	 */
	public abstract void InitHandler();
}
