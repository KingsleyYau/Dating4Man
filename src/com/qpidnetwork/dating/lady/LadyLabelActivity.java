package com.qpidnetwork.dating.lady;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.qpidnetwork.dating.BaseActivity;
import com.qpidnetwork.dating.R;
import com.qpidnetwork.dating.R.color;
import com.qpidnetwork.framework.util.UnitConversion;
import com.qpidnetwork.framework.widget.wrap.WrapListView;
import com.qpidnetwork.request.OnLadySignListCallback;
import com.qpidnetwork.request.OnRequestCallback;
import com.qpidnetwork.request.RequestOperator;
import com.qpidnetwork.request.item.LadySignItem;
import com.qpidnetwork.view.FlowLayout;

public class LadyLabelActivity extends BaseActivity {
	
	public static final String LABEL_WOMAN_ID = "womanId";
	public static final String LADY_LABEL_ADD = "lady_label_add";
	public static final String LADY_LABEL_DEL = "lady_label_del";
	public static String INPUT_KEY_LADY_NAME = "INPUT_KEY_LADY_NAME";
	
	private String womanId;
	private FlowLayout labelList;
	private FadeLadyLabelAdapter labelAdapter;
	//private LadyLabelAdapter mAdapter;
	private FrameLayout ivSummit;
	private ImageView ivCancel;
	private boolean isSubmitting = false;
	
	/**
	 * 用户选择的标签列表
	 */
	private List<LadySignItem> mLabelList = new ArrayList<LadySignItem>();
	
	/**
	 * 接口返回的标签列表
	 * @see 用于计算增加或者减少的标签回调javascript接口
	 */
	private List<LadySignItem> mOrignalLabelList = new ArrayList<LadySignItem>();
	
	public static Intent getIntent(Context context, String womanId, String womanName){
		Intent intent = new Intent(context, LadyLabelActivity.class);
		intent.putExtra(LABEL_WOMAN_ID, womanId);
		intent.putExtra(INPUT_KEY_LADY_NAME, womanName);
		return intent;
	}
	
	private enum RequestFlag {
		REQUEST_GET_LABEL_SUCCESS,
		REQUEST_SUBMIT_LABEL_SUCCESS,
		REQUEST_FAIL,
	}
	
	/**
	 * 界面消息
	 */
	private class MessageCallbackItem {
		/**
		 * @param errno				接口错误码
		 * @param errmsg			错误提示
		 */
		public MessageCallbackItem(
				String errno, 
				String errmsg
				) {
			this.errno = errno;
			this.errmsg = errmsg;
		}
		public String errno;
		public String errmsg;
		public LadySignItem[] ladySignItem;
	}
	
	@Override
	protected void onCreate(Bundle arg0) {
		// TODO Auto-generated method stub
		super.onCreate(arg0);
		this.getWindow().setBackgroundDrawable(new ColorDrawable(getResources().getColor(color.transparent_black_windown_bg)));
		
		//this.getWindow().setLayout(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		
		Bundle extras  = this.getIntent().getExtras();
		if (extras == null || !extras.containsKey(INPUT_KEY_LADY_NAME)){
			finish();
			return;
		}
		
		
	}
	
	/**
	 * 提交女士标签列表
	 * @param womanId 女士Id
	 * @param ids 标签Id列表
	 */
	private void summitChoosedLabels(String womanId, String[] ids){
		
		if (isSubmitting) return;
		
		showToastProgressing("Submitting");
		isSubmitting = true;
		RequestOperator.getInstance().UploadSign(womanId, ids, new OnRequestCallback() {
			
			@Override
			public void OnRequest(boolean isSuccess, String errno, String errmsg) {
				// TODO Auto-generated method stub
				Message msg = Message.obtain();
				MessageCallbackItem obj = new MessageCallbackItem(errno, errmsg);
				if( isSuccess ) {
					// 获取个人信息成功
					msg.what = RequestFlag.REQUEST_SUBMIT_LABEL_SUCCESS.ordinal();
				} else {
					// 获取个人信息失败
					msg.what = RequestFlag.REQUEST_FAIL.ordinal();
				}
				msg.obj = obj;
				mHandler.sendMessage(msg);
			}
		});
	}
	
	/**
	 * 获取女士标签列表
	 * @param womanId 女士Id
	 */
	private void getLabelList(String womanId){
		showProgressDialog("Loading");
		RequestOperator.getInstance().SignList(womanId, new OnLadySignListCallback() {
			
			@Override
			public void OnLadySignList(boolean isSuccess, String errno, String errmsg,
					LadySignItem[] listArray) {
				// TODO Auto-generated method stub
				Message msg = Message.obtain();
				MessageCallbackItem obj = new MessageCallbackItem(errno, errmsg);
				if( isSuccess ) {
					// 成功
					msg.what = RequestFlag.REQUEST_GET_LABEL_SUCCESS.ordinal();
					obj.ladySignItem = listArray;
				} else {
					// 失败
					msg.what = RequestFlag.REQUEST_FAIL.ordinal();
				}
				msg.obj = obj;
				mHandler.sendMessage(msg);
			}
		});
	}

	@Override
	public void InitView() {
		// TODO Auto-generated method stub
		setContentView(R.layout.activity_lady_label);
		Bundle bundle = getIntent().getExtras();
		if((bundle != null)&&(bundle.containsKey(LABEL_WOMAN_ID))){
			womanId = bundle.getString(LABEL_WOMAN_ID);
		}
		
		ivCancel = (ImageView)findViewById(R.id.ivCancel);
		ivCancel.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				finish();
			}
		});
		
		if (Build.VERSION.SDK_INT >= 21){
			ivCancel.getLayoutParams().height = UnitConversion.dip2px(this, 48);
			ivCancel.getLayoutParams().width = UnitConversion.dip2px(this, 48);
		}
		
		labelList = (FlowLayout)findViewById(R.id.labelList);
		labelAdapter = new FadeLadyLabelAdapter(this, labelList, mLabelList);
		
		//labelList.setDividerHeight((int)(8.0f * this.getResources().getDisplayMetrics().density));
		//labelList.setDividerWidth((int)(8.0f * this.getResources().getDisplayMetrics().density));
		
		//mAdapter = new LadyLabelAdapter(this, mLabelList);
		//labelList.addView(mAdapter.getView(0, null, null));
		//labelList.setAdapter(mAdapter);
		
		ivSummit = (FrameLayout)findViewById(R.id.ivSummit);
		ivSummit.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				// 提交女士标签选择
				summitChoosedLabels(womanId, (String[])labelAdapter.getChoosedLabelsId().toArray(new String[labelAdapter.getChoosedLabelsId().size()]));
			}
		});
		
		if( (womanId != null) && (!womanId.equals("")) ) {
			// 获取女士标签
			getLabelList(womanId);
		}
	}

	@Override
	public void InitHandler() {
		// TODO Auto-generated method stub
		mHandler = new Handler() {
			@Override
			public void handleMessage(android.os.Message msg) {
				// 收起菊花
				hideProgressDialog();
				MessageCallbackItem obj = (MessageCallbackItem) msg.obj;
				switch ( RequestFlag.values()[msg.what] ) {
				case REQUEST_GET_LABEL_SUCCESS:{
					// 获取标签成功
					mLabelList.clear();
					mOrignalLabelList.clear();
					if( obj.ladySignItem != null ) {
						// 保存原始数组
						mOrignalLabelList.addAll(Arrays.asList(obj.ladySignItem));
						// 复制用于界面改变数组
						for(LadySignItem item : obj.ladySignItem) {
							mLabelList.add(item.clone());
						}
					}
					labelAdapter.adaptView();
					ivSummit.setVisibility(View.VISIBLE);
				}break;
				case REQUEST_SUBMIT_LABEL_SUCCESS:{
					// 提交标签成功
					Intent intent = new Intent();
					
					// 获取增加和删除的标签
					List<String> labelListAdd = new ArrayList<>();
					List<String> labelListDel = new ArrayList<>();
					for(int i = 0; i < mOrignalLabelList.size() && i < mLabelList.size(); i++ ) {
						LadySignItem itemOrignal = mOrignalLabelList.get(i);
						LadySignItem item = mLabelList.get(i);
						
						if( !itemOrignal.isSigned && item.isSigned ) {
							// 增加的标签
							labelListAdd.add(item.signId);
						} else if( itemOrignal.isSigned && !item.isSigned ) {
							// 删除的标签
							labelListDel.add(item.signId);
						}
					}
					
					intent.putExtra(LADY_LABEL_ADD, labelListAdd.toArray(new String[labelListAdd.size()]));
					intent.putExtra(LADY_LABEL_DEL, labelListDel.toArray(new String[labelListDel.size()]));
					
					showToastDone("Done!");
					
					setResult(RESULT_OK, intent);
					finish();
				}break;
				case REQUEST_FAIL:{
					showToastFailed("Failed!");
					isSubmitting = false;
				}break;
				default:break;
				}
			}
		};
	}
	
}