package com.qpidnetwork.dating.emf;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;

import com.qpidnetwork.dating.BaseActivity;
import com.qpidnetwork.dating.R;
import com.qpidnetwork.dating.bean.EMFAttachmentBean;
import com.qpidnetwork.dating.bean.EMFAttachmentBean.AttachType;
import com.qpidnetwork.framework.base.UpdateableAdapter;
import com.qpidnetwork.manager.VirtualGiftManager;
import com.qpidnetwork.manager.VirtualGiftManager.OnOnGetVirtualGiftCallback;
import com.qpidnetwork.request.item.Gift;
import com.qpidnetwork.tool.ImageViewLoader;
import com.qpidnetwork.view.MaterialAppBar;

/**
 * EMF模块
 * 选择虚拟礼物界面
 * @author Max.Chiu
 */
public class EmotionChooseActivity extends BaseActivity {
	/**
	 * 其他界面交互时候参数
	 * 已经选择的虚拟礼物Id
	 */
	public static final String VIRTUAL_GIFT_ID = "vg_id";

	/**
	 * 选择虚拟礼物
	 */
	private static final int RESULT_INDEX = 0;
	
	private MaterialAppBar appbar;
	
	private class EMFVirtualGiftAdapter extends UpdateableAdapter<Gift> {
		
		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			if( convertView == null ){
				convertView = new EMFVirtualGiftItemView(mContext);
			}
			
			// 虚拟礼物
			Gift item = getItem(position);
			EMFVirtualGiftItemView view = (EMFVirtualGiftItemView) convertView;
			
			//title
			String format = getResources().getString(R.string.emf_virtual_gift_name_credit);
			String tips = String.format(format, item.title, 1);	
			view.textViewTips.setText(tips);
			
			// 获取虚拟礼物Id
			final String vgId = item.vgid;
//			view.imageView.setImageResource(R.drawable.attachment_gift_unloaded_110_150dp);
			String photoUrl = VirtualGiftManager.getInstance().GetVirtualGiftImage(vgId);
			String localPhotoPath = VirtualGiftManager.getInstance().CacheVirtualGiftImagePath(vgId);
			
			ImageViewLoader loader = new ImageViewLoader(mContext);
			view.setImageViewLoader(loader);
			loader.SetDefaultImage(getResources().getDrawable(R.drawable.attachment_gift_unloaded_110_150dp));
			loader.DisplayImage(view.imageView, photoUrl, localPhotoPath, null);
			
//			view.textViewTips.setText(item.title);
			view.textViewPreview.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					
					ArrayList<EMFAttachmentBean> attachList = new ArrayList<EMFAttachmentBean>();
					for(Gift item : mVirtualGiftList) {
						EMFAttachmentBean virtualItem = new EMFAttachmentBean();
						virtualItem.type = AttachType.VIRTUAL_GIFT;
						virtualItem.vgId = item.vgid;
						attachList.add(virtualItem);
					}
					
					Intent intent = (EMFAttachmentPreviewActivity.getIntent(mContext, attachList, position));
					intent.putExtra(EMFAttachmentPreviewActivity.INSERT, true);
					startActivityForResult(intent, RESULT_INDEX);
				}
			});
			
			return convertView;
		}
	}
	
	private GridView girdView;
	private EMFVirtualGiftAdapter mAdapter = new EMFVirtualGiftAdapter();
	private List<Gift> mVirtualGiftList;
	
	private enum RequestFlag {
		REQUEST_SUCCESS,
		REQUEST_FAIL,
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		GetVirtualGift();
	}
	
	@Override
	public void InitView() {
		// TODO Auto-generated method stub
		setContentView(R.layout.activity_emf_virtual_gift);
		
		
		appbar = (MaterialAppBar)findViewById(R.id.appbar);
		appbar.setTouchFeedback(MaterialAppBar.TOUCH_FEEDBACK_HOLO_LIGHT);
		appbar.addButtonToLeft(R.id.common_button_back, "back", R.drawable.ic_arrow_back_grey600_24dp);
		appbar.setTitle("Select virtaul gift", getResources().getColor(R.color.text_color_dark));
		appbar.setAppbarBackgroundColor(Color.WHITE);
		appbar.setOnButtonClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				switch (v.getId()) {
				case R.id.common_button_back:
					finish();
					break;

				default:
					break;
				}
				finish();
			}
			
		});
		
		girdView = (GridView) findViewById(R.id.gridView);
		girdView.setAdapter(mAdapter);
		girdView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO Auto-generated method stub
				
				Activity activity = (Activity) mContext;
				Intent intent = new Intent();
				intent.putExtra(VIRTUAL_GIFT_ID, mVirtualGiftList.get(position).vgid);
				activity.setResult(RESULT_OK, intent);
				activity.finish();
			}
		});
	}

	@Override
	public void InitHandler() {
		// TODO Auto-generated method stub
		mHandler = new Handler() {
			@Override
			public void handleMessage(android.os.Message msg) {
				// 收起菊花
				hideProgressDialog();
				switch ( RequestFlag.values()[msg.what] ) {
				case REQUEST_SUCCESS:{
					// 请求成功
					ReloadData();
				}break;
				case REQUEST_FAIL:{
					// 请求失败
				}break;
				default:
					break;
				}
			}
		};
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	    super.onActivityResult(requestCode, resultCode, data);
	 
	    switch(requestCode) {
	    case RESULT_INDEX:{
	    	// 选择虚拟礼物
	    	if( resultCode == RESULT_OK ) {
	    		int postion = data.getExtras().getInt(EMFAttachmentPreviewActivity.INDEX);
	    		Intent intent = new Intent();
	    		intent.putExtra(VIRTUAL_GIFT_ID, mVirtualGiftList.get(postion).vgid);
	    		setResult(Activity.RESULT_OK, intent);
	    		finish();
	    	}
	    }break;
	    default:break;
	    }
	}
	
	/**
	 * 刷新界面
	 */
	public void ReloadData() {
		mAdapter.replaceList(mVirtualGiftList);
	}
	
	/**
	 * 获取虚拟礼物列表
	 */
	public void GetVirtualGift() {
		VirtualGiftManager.getInstance().GetVirtualGift(new OnOnGetVirtualGiftCallback() {
			
			@Override
			public void OnGetVirtualGift(boolean isSuccess, List<Gift> itemList,
					String errno, String errmsg) {
				// TODO Auto-generated method stub
				Message msg = Message.obtain();
				if( isSuccess ) {
					msg.what = RequestFlag.REQUEST_SUCCESS.ordinal();
					mVirtualGiftList = itemList;
					if(mVirtualGiftList != null){
					}
				} else {
					msg.what = RequestFlag.REQUEST_FAIL.ordinal();
				}
				mHandler.sendMessage(msg);
			}
		});
	}
	
	/**
	 * 点击去取消
	 * @param v
	 */
	public void onClickCancel(View v) {
		setResult(RESULT_CANCELED, null);
		finish();
	}
}
