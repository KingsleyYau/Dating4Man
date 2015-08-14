package com.qpidnetwork.dating.contactus;

import java.io.File;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import android.text.TextUtils.TruncateAt;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.qpidnetwork.dating.R;
import com.qpidnetwork.dating.bean.RequestFailBean;
import com.qpidnetwork.framework.base.BaseActionBarFragmentActivity;
import com.qpidnetwork.framework.util.CompatUtil;
import com.qpidnetwork.framework.util.ImageUtil;
import com.qpidnetwork.framework.util.StringUtil;
import com.qpidnetwork.framework.util.ToastUtil;
import com.qpidnetwork.framework.util.UnitConversion;
import com.qpidnetwork.request.OnRequestCallback;
import com.qpidnetwork.request.RequestJniTicket;
import com.qpidnetwork.request.RequestOperator;
import com.qpidnetwork.view.FitTopImageView;
import com.qpidnetwork.view.FlatToast;
import com.qpidnetwork.view.MaterialAppBar;
import com.qpidnetwork.view.MaterialDialogSingleChoice;
import com.qpidnetwork.view.MaterialTextField;

public class TicketCreateActivity extends BaseActionBarFragmentActivity {
	
	private static final int CREATE_NEW_TICKET_SUCCESS = 0;
	private static final int CREATE_NEW_TICKET_FAILED = 1;
	
	/**
	 * 相册
	 */
	private static final int RESULT_LOAD_IMAGE_ALBUMN = 2;
	
	private MaterialTextField tvProblem;
	private MaterialTextField etSubject;
	private EditText etBody;
	private ImageView ivAddShot;
	private FitTopImageView ivShotPhoto;
	
	private MaterialDialogSingleChoice contactFormSelectDialog;
	private String[] contactFormOptions; 
	
	/*选择的截图当前路径*/
	private String filePath = "";
	private int typeId = 0; //当前problem type
	
	@Override
	protected void onCreate(Bundle arg0) {
		// TODO Auto-generated method stub
		super.onCreate(arg0);
		setCustomContentView(R.layout.activity_create_new_ticket);
		contactFormOptions= this.getResources().getStringArray(R.array.contact_ticket_form);
		initTitle();
		initViews();
	}
	
	private void initTitle(){
		getCustomActionBar().setTitle(getString(R.string.ticket_create_new), getResources().getColor(R.color.text_color_dark));
		getCustomActionBar().setAppbarBackgroundColor(getResources().getColor(R.color.theme_actionbar_secoundary));
		getCustomActionBar().setTouchFeedback(MaterialAppBar.TOUCH_FEEDBACK_HOLO_LIGHT);
		getCustomActionBar().addButtonToLeft(R.id.common_button_back, "back", R.drawable.ic_close_grey600_24dp);
		getCustomActionBar().addButtonToRight(R.id.common_button_save, "save", R.drawable.ic_done_grey600_24dp);
	}
	
	private void initViews(){
		
		tvProblem = (MaterialTextField)findViewById(R.id.tvProblem);
		etSubject = (MaterialTextField)findViewById(R.id.etSubject);
		etBody = (EditText)findViewById(R.id.etBody);
		etBody = (EditText)findViewById(R.id.etBody);
		ivAddShot = (ImageView)findViewById(R.id.ivAddShot); 
		ivShotPhoto = (FitTopImageView)findViewById(R.id.ivShotPhoto);
		ivShotPhoto.setOnClickListener(this);
		ivAddShot.setOnClickListener(this);
		
		
		etSubject.setHint(getString(R.string.ticket_subject));
		etBody.setHint(getString(R.string.describe_your_issue));
		
		tvProblem.setText(getString(R.string.contact_form) + contactFormOptions[0]);
		tvProblem.getEditor().setSingleLine();
		tvProblem.getEditor().setEllipsize(TruncateAt.END);
		
		tvProblem.setOnFocusChangedCallback(new MaterialTextField.OnFocuseChangedCallback() {
			
			@Override
			public void onFocuseChanged(View v, boolean hasFocus) {
				// TODO Auto-generated method stub
				if (hasFocus){
					if (contactFormSelectDialog != null && !contactFormSelectDialog.isShowing())
						contactFormSelectDialog.show();
					etSubject.getEditor().requestFocus();
				}
			}
		});
		
		contactFormSelectDialog = new MaterialDialogSingleChoice(this, contactFormOptions, contactFormChooseCallback);
		contactFormSelectDialog.setTitle(getString(R.string.What_is_your_problem_relatedto));
		contactFormSelectDialog.show();
		
		
	}
	
	private MaterialDialogSingleChoice.OnClickCallback contactFormChooseCallback = new MaterialDialogSingleChoice.OnClickCallback(){

		@Override
		public void onClick(AdapterView<?> adptView, View v, int which) {
			// TODO Auto-generated method stub
			String contactFormString = getString(R.string.contact_form) + contactFormOptions[which];
			typeId = convertTicketType(which);
			tvProblem.setText(contactFormString);
			etSubject.getEditor().requestFocus();
			etSubject.getEditor().setSelection(etSubject.getText().length());
		}
		
	};
	
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		super.onClick(v);
		switch (v.getId()) {
		case R.id.common_button_save:
			/*发送Ticket*/
			onNewTicketCreated();
			break;
		case R.id.ivAddShot:
		case R.id.ivShotPhoto:
			/*选择屏幕截图或更换当前截图*/
			onScreenShotSelected();
			break;
		default:
			break;
		}
	}
	
	private void onNewTicketCreated(){
		String subject = etSubject.getText().toString().trim();
		String body = etBody.getText().toString().trim();
		
		if (subject.length() == 0){
			etSubject.setError(Color.RED, true);
			return;
		}
		
		if (body.length() == 0){
			shakeView(etBody, true);
			return;
		}
		
		showProgressDialog("Submitting");
		
		RequestOperator.newInstance(this).AddTicket(typeId, subject, body, filePath, new OnRequestCallback() {
			
			@Override
			public void OnRequest(boolean isSuccess, String errno, String errmsg) {
				Message msg = Message.obtain();
				if(isSuccess){
					msg.what = CREATE_NEW_TICKET_SUCCESS;
				}else{
					msg.what = CREATE_NEW_TICKET_FAILED;
					msg.obj = new RequestFailBean(errno, errmsg);
				}
				sendUiMessage(msg);
			}
		});
	}
	
	
	@Override
	protected void handleUiMessage(Message msg) {
		super.handleUiMessage(msg);
		hideProgressDialog();
		switch (msg.what) {
		case CREATE_NEW_TICKET_SUCCESS:
			setResult(RESULT_OK, null);
			FlatToast.showStickToast(TicketCreateActivity.this, "Done!", FlatToast.StikyToastType.DONE);
			finish();
			break;
		case CREATE_NEW_TICKET_FAILED:
			RequestFailBean errorBean = (RequestFailBean)msg.obj;
			FlatToast.showStickToast(TicketCreateActivity.this, "Failed!", FlatToast.StikyToastType.FAILED);
			Toast.makeText(TicketCreateActivity.this, errorBean.errmsg, Toast.LENGTH_LONG).show();
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
	
	/**
	 * Ticket原因类型装换
	 * @param position
	 */
	private int convertTicketType(int position){
		int typeId = 20;
		switch (position) {
		case 0:
			typeId = RequestJniTicket.TicketTypeTechnicalSupport;
			break;
		case 1:
			typeId = RequestJniTicket.TicketTypeBillingIssue;
			break;
		case 2:
			typeId = RequestJniTicket.TicketTypeSuggestion;
			break;
		case 3:
			typeId = RequestJniTicket.TicketTypeComplaint;
			break;
		case 4:
			typeId = RequestJniTicket.TicketTypeConsultation;
			break;
		case 5:
			typeId = RequestJniTicket.TicketTypeOther;
			break;
		default:
			typeId = RequestJniTicket.TicketTypeOther;
			break;
		}
		return typeId;
	}
}
