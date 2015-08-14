package com.qpidnetwork.dating.livechat.expression;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.qpidnetwork.framework.base.BaseFragmentActivity;

public class PlatinumEmoticonsActivity extends BaseFragmentActivity{
	
	public static Intent getIntent(Context context, String emotionId){
		Intent intent = new Intent(context, PlatinumEmoticonsActivity.class);
		intent.putExtra("emotionId", emotionId);
		return intent;
	}
	
	@Override
	protected void onCreate(Bundle arg0) {
		// TODO Auto-generated method stub
		super.onCreate(arg0);
	}

}
