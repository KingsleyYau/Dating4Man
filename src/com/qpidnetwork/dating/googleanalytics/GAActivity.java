package com.qpidnetwork.dating.googleanalytics;

import android.app.Activity;

/**
 * GoogleAnalytics统计的Activity基类
 * @author Samson Fan
 *
 */
public class GAActivity extends Activity 
{
	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		GAManager.newInstance().getReportStart(this);
	}
	
	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		GAManager.newInstance().getReportStop(this);
		super.onStop();
	}
}
