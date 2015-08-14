package com.qpidnetwork.dating.dialog;

import android.view.View;

public interface CustomTermsDialogClickListener {
	public void onPositiveClick(View v, boolean isChecked, String tag);
	public void onNegativeClick(View v, String tag);
}
