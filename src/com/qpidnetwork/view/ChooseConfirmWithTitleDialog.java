package com.qpidnetwork.view;

import com.qpidnetwork.dating.R;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

public class ChooseConfirmWithTitleDialog extends Dialog {
	
	public TextView textViewTitle;
	public TextView textViewTips;
	public Button buttonConfirm;
	
	public ChooseConfirmWithTitleDialog(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}
    
    public ChooseConfirmWithTitleDialog(Context context, int theme) {
        super(context, theme);
    }
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.dialog_confirm_with_title);
        this.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        
        textViewTitle = (TextView)findViewById(R.id.textViewTitle);
        textViewTips = (TextView)findViewById(R.id.textViewTips);
        buttonConfirm = (Button) findViewById(R.id.buttonConfirm);
    }
    
}
