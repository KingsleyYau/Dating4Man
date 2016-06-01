package com.qpidnetwork.view;

import com.qpidnetwork.dating.R;
import com.qpidnetwork.framework.base.BaseDialog;
import com.qpidnetwork.framework.widget.NumberProgressBar.NumberProgressBar;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;

/**
 * @author Yanni
 * 
 * @version 2016-4-26
 */
public class ThemeProgressDialog extends BaseDialog {
	
	private float density = this.getContext().getResources().getDisplayMetrics().density;
	private LinearLayout contentView;
	
	private int DIALOG_MIN_WIDTH = (int)(280.0f * density);
	private int view_padding = (int)(24.0f * density);
	
	private NumberProgressBar npb;

	public ThemeProgressDialog(Context context) {
		super(context,R.style.themeDialog);
		this.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
		contentView=(LinearLayout) View.inflate(context, R.layout.dialog_theme_item, null);
		npb = (NumberProgressBar) contentView.findViewById(R.id.npb);
		LayoutParams params = new LayoutParams(this.getDialogSize(), LayoutParams.WRAP_CONTENT);
//		contentView.setMinimumWidth(DIALOG_MIN_WIDTH);
//		contentView.setLayoutParams(params);
//		contentView.setPadding(view_padding, view_padding, view_padding, view_padding);
		setContentView(contentView);
	}
	
	public void setProgress(int progress){
		npb.setProgress(progress);
	}

}
