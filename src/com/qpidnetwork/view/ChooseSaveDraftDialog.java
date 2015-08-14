package com.qpidnetwork.view;

import com.qpidnetwork.dating.R;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.widget.TextView;

public class ChooseSaveDraftDialog extends Dialog{
	
	public TextView textViewSave;
	public TextView textViewDelete;
	public TextView textViewCancel;
	
	public ChooseSaveDraftDialog(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}
    
    public ChooseSaveDraftDialog(Context context, int theme) {
        super(context, theme);
    }
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.dialog_choose_save_draft);
        this.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        
    	textViewSave = (TextView) findViewById(R.id.textViewSave);
    	textViewDelete = (TextView) findViewById(R.id.textViewDelete);;
    	textViewCancel = (TextView) findViewById(R.id.textViewCancel);
    }
}
