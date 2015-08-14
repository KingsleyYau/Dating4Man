package com.qpidnetwork.dating.googleanalytics;

import android.app.ListActivity;

/**
 * GoogleAnalytics统计的ListActivity基类
 * @author Samson Fan
 *
 */
public class GAListActivity extends ListActivity 
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
