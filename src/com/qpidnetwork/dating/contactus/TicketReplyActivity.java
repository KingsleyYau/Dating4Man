package com.qpidnetwork.dating.contactus;

import java.io.File;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.qpidnetwork.dating.R;
import com.qpidnetwork.dating.bean.RequestFailBean;
import com.qpidnetwork.framework.base.BaseActionBarFragmentActivity;
import com.qpidnetwork.framework.util.CompatUtil;
import com.qpidnetwork.framework.util.ImageUtil;
import com.qpidnetwork.framework.util.StringUtil;
import com.qpidnetwork.framework.util.UnitConversion;
import com.qpidnetwork.request.OnRequestCallback;
import com.qpidnetwork.request.RequestOperator;
import com.qpidnetwork.view.FitTopImageView;
import com.qpidnetwork.view.FlatToast;
import com.qpidnetwork.view.MaterialAppBar;
import com.qpidnetwork.view.MaterialTextField;

public class TicketReplyActivity extends BaseActionBarFragmentActivity{
	
	private static final int REPLY_TICKET_SUCCESS = 0;
	private static final int REPLY_TICKET_FAILED = 1;
	
	/**
	 * 相册
	 */
	private static final int RESULT_LOAD_IMAGE_ALBUMN = 2;
	
	private MaterialTextField tvTicketSuject;
	private EditText  etBody;
	private ImageView ivAddShot;
	private FitTopImageView ivShotPhoto;
	
	/*选择的截图当前路径*/
	private String filePath = "";
	private String ticketId = "";
	private String title = "";
	
	@Override
	protected void onCreate(Bundle arg0) {
		// TODO Auto-generated method stub
		super.onCreate(arg0);
		setCustomContentView(R.layout.activity_reply_ticket);
		
		Bundle bundle = getIntent().getExtras();
		if(bundle != null){
			if(bundle.containsKey(TicketDetailListActivity.TICKET_ID)){
				ticketId = bundle.getString(TicketDetailListActivity.TICKET_ID);
			}
			if(bundle.containsKey(TicketDetailListActivity.TICKET_TITLE)){
				title = bundle.getString(TicketDetailListActivity.TICKET_TITLE);
			}
		}
		
		initTitle();
		initViews();
		
		if(StringUtil.isEmpty(ticketId)){
			/*ticketId 不能为空*/
			finish();
		}
	}
	
	private void initTitle(){
		getCustomActionBar().setTitle(getString(R.string.ticket_reply), getResources().getColor(R.color.text_color_dark));
		getCustomActionBar().setAppbarBackgroundColor(getResources().getColor(R.color.theme_actionbar_secoundary));
		getCustomActionBar().setTouchFeedback(MaterialAppBar.TOUCH_FEEDBACK_HOLO_LIGHT);
		
		getCustomActionBar().addButtonToLeft(R.id.common_button_back, "back", R.drawable.ic_close_grey600_24dp);
		getCustomActionBar().addButtonToRight(R.id.common_button_save, "save", R.drawable.ic_done_grey600_24dp);
	}
	
	private void initViews(){
		tvTicketSuject = (MaterialTextField)findViewById(R.id.tvTicketSuject);
		etBody = (EditText)findViewById(R.id.etBody);
		ivAddShot = (ImageView)findViewById(R.id.ivAddShot);
		ivShotPhoto = (FitTopImageView)findViewById(R.id.ivShotPhoto);
		ivShotPhoto.setOnClickListener(this);
		ivAddShot.setOnClickListener(this);
		
		tvTicketSuject.getEditor().setEnabled(false);
		tvTicketSuject.setText("Reply: " + title);
		
		etBody.requestFocus();
	}
	
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		super.onClick(v);
		switch (v.getId()) {
		case R.id.common_button_save:
			/*回复Ticket*/
			ReplyTicket();
			break;
		case R.id.ivShotPhoto:
		case R.id.ivAddShot:
			/*选择屏幕截图*/
			onScreenShotSelected();
			break;
		default:
			break;
		}
	}
	
	/**
	 * 回复Ticket
	 */
	private void ReplyTicket(){
		if (etBody.getText().length() == 0){
			shakeView(etBody, true);
			return;
		}
		
		showProgressDialog("Submitting");
		String body = etBody.getText().toString().trim();
		RequestOperator.newInstance(this).ReplyTicket(ticketId, body, filePath, new OnRequestCallback() {
			
			@Override
			public void OnRequest(boolean isSuccess, String errno, String errmsg) {
				Message msg = Message.obtain();
				if(isSuccess){
					msg.what = REPLY_TICKET_SUCCESS;
				}else{
					msg.what = REPLY_TICKET_FAILED;
					msg.obj = new RequestFailBean(errno, errmsg);
				}
				sendUiMessage(msg);
			}
		});
	}
	
	@Override
	protected void handleUiMessage(Message msg) {
		super.handleUiMessage(msg);
		switch (msg.what) {
		case REPLY_TICKET_SUCCESS:
			setResult(RESULT_OK, null);
			hideProgressDialog();
			//FlatToast.showStickToast(TicketReplyActivity.this, "Done!", FlatToast.StikyToastType.DONE);
			finish();
			break;
		case REPLY_TICKET_FAILED:
			RequestFailBean errorBean = (RequestFailBean)msg.obj;
			hideProgressDialog();
			FlatToast.showStickToast(TicketReplyActivity.this, "Failed!", FlatToast.StikyToastType.FAILED);
			Toast.makeText(TicketReplyActivity.this, errorBean.errmsg, Toast.LENGTH_LONG).show();
			break;
		default:
			break;
		}
	}
	
	/**
	 * 选择截屏图片
	 */
	private void onScreenShotSelected(){
		Intent intent = CompatUtil.getSelectPhotoFromAlumIntent();
		startActivityForResult(intent, RESULT_LOAD_IMAGE_ALBUMN);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if(resultCode == RESULT_OK){
			switch (requestCode) {
			case RESULT_LOAD_IMAGE_ALBUMN:
				/*相册选择图片成功返回*/
				if(null != data){
					Uri selectedImage = data.getData();
					filePath = CompatUtil.getSelectedPhotoPath(this, selectedImage);
					if(!StringUtil.isEmpty(filePath) && new File(filePath).exists()){
						Bitmap temp = ImageUtil.decodeAndScaleBitmapFromFile(filePath, UnitConversion.dip2px(this, 100), UnitConversion.dip2px(this, 120));
						ivShotPhoto.setImageBitmap(temp);
					}
				}
				break;

			default:
				break;
			}
		}
	}
}
