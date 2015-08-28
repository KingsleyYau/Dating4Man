package com.qpidnetwork.view;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.widget.TextView;

import com.qpidnetwork.dating.R;

public class ChooseRemoveAttachmentDialog extends Dialog {
	
	public TextView textViewBounds;
	public TextView textViewRemoveAttachment;
	public TextView textViewAddCredit;
	
	public ChooseRemoveAttachmentDialog(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}
    
    public ChooseRemoveAttachmentDialog(Context context, int theme) {
        super(context, theme);
    }
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.dialog_choose_remove_attachment);
        this.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        
        textViewBounds = (TextView)findViewById(R.id.textViewBounds);
        textViewRemoveAttachment = (TextView) findViewById(R.id.textViewRemoveAttachment);
        textViewAddCredit = (TextView) findViewById(R.id.textViewAddCredit);
    }
}
