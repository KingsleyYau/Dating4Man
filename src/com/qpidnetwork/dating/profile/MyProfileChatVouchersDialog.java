package com.qpidnetwork.dating.profile;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import com.qpidnetwork.dating.R;

/**
 * MyProfile模块
 * @author Max.Chiu
 */
public class MyProfileChatVouchersDialog extends Dialog {
	private Context mContext;
	
	public TextView textView;
	public Button buttonOK;
	public int mCount = 0;
	
	public MyProfileChatVouchersDialog(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
        mContext = context;
	}
    
    public MyProfileChatVouchersDialog(Context context, int theme) {
        super(context, theme);
        mContext = context;
    }
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.dialog_chat_vochers);
        
        textView = (TextView) findViewById(R.id.textView);
        String format = mContext.getResources().getString(R.string.You_have_vouchers);
        textView.setText(String.format(format, mCount));
        buttonOK = (Button) findViewById(R.id.buttonOK);
    }
}
