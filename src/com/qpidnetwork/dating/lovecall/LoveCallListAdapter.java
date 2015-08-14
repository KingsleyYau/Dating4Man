package com.qpidnetwork.dating.lovecall;

import java.text.SimpleDateFormat;
import java.util.Locale;

import android.app.Activity;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.qpidnetwork.dating.R;
import com.qpidnetwork.dating.authorization.LoginPerfence;
import com.qpidnetwork.dating.bean.LoveCallBean;
import com.qpidnetwork.dating.contacts.ContactManager;
import com.qpidnetwork.framework.base.UpdateableAdapter;
import com.qpidnetwork.framework.util.Log;
import com.qpidnetwork.framework.util.SystemUtil;
import com.qpidnetwork.framework.widget.CircleImageView;
import com.qpidnetwork.manager.FileCacheManager;
import com.qpidnetwork.tool.ImageViewLoader;
import com.qpidnetwork.view.MaterialDialogAlert;
import com.qpidnetwork.view.MaterialThreeButtonDialog;

public class LoveCallListAdapter extends UpdateableAdapter<LoveCallBean>{
	
	public Activity mContext;
	private SimpleDateFormat scheduleFormat = new SimpleDateFormat("HH:mm dd MMM", Locale.getDefault());
	private SimpleDateFormat requestFormat = new SimpleDateFormat("dd MMM", Locale.getDefault());
	
	public LoveCallListAdapter(Activity context){
		this.mContext = context;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		ViewHolder holder;
		if(convertView == null){
			holder = new ViewHolder();
			convertView = LayoutInflater.from(mContext).inflate(R.layout.adapter_love_call_item, null);
			holder.llContainer = (LinearLayout)convertView.findViewById(R.id.llContainer);
			holder.ivPhoto = (CircleImageView)convertView.findViewById(R.id.ivPhoto);
			holder.tvName = (TextView)convertView.findViewById(R.id.tvName);
			holder.tvDesc = (TextView)convertView.findViewById(R.id.tvDesc);
			holder.btnCall = (ImageView)convertView.findViewById(R.id.btnCall);
			holder.tvTips = (TextView)convertView.findViewById(R.id.tvTips);
			holder.imageDownLoader = null;
			convertView.setTag(holder);
		}else{
			holder = (ViewHolder)convertView.getTag();
		}
		
		final LoveCallBean item = getItem(position);
		holder.tvName.setText(item.firstname);
		holder.btnCall.setVisibility(View.GONE);
		
		if(item.isconfirm){
			//预约列表
			holder.tvTips.setVisibility(View.GONE);
			holder.tvDesc.setText(mContext.getResources().getString(R.string.lovecall_schedule_time, scheduleFormat.format(item.longbegintime)));
			if (item.isCallActive()){
				holder.btnCall.setVisibility(View.VISIBLE);
				holder.btnCall.setOnClickListener(new OnClickListener(){

					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						makeCall(item);
					}
					
				});
				
			}
		}else{
			//预约申请列表
			holder.tvTips.setVisibility(View.VISIBLE);
			holder.tvDesc.setText(mContext.getResources().getString(R.string.lovecall_request_time, requestFormat.format(item.longendtime)));
		}
		
		holder.ivPhoto.setImageResource(R.drawable.female_default_profile_photo_40dp);
		/*头像处理*/
		if ( null != holder.imageDownLoader ) {
			// 停止回收旧Downloader
			holder.imageDownLoader.ResetImageView();
		}
		if((item.image != null)&&(!item.image.equals(""))){
			String localPath = FileCacheManager.getInstance().CacheImagePathFromUrl(item.image);
			holder.imageDownLoader = new ImageViewLoader(mContext);
			holder.imageDownLoader.SetDefaultImage(mContext.getResources().getDrawable(R.drawable.female_default_profile_photo_40dp));
			holder.imageDownLoader.DisplayImage(holder.ivPhoto, item.image, localPath, null);
		}

		holder.llContainer.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				mContext.startActivityForResult(LoveCallDetailActivity.getIntent(mContext, item), 0);
			}
		});
		
		return convertView;
	}
	
	
	private void makeCall(final LoveCallBean lovecallBean) {
		/* 检测有无Sim卡 */
		if (SystemUtil.isSimCanUse(mContext)) {
			/*资费提示*/
			
			if (LoginPerfence.GetStringPreference(mContext, "donnot_show_love_call_fee").equals("true")){
				new DirectCallManager(mContext).makeCall(lovecallBean.centerid, lovecallBean.callid);
				/*获取token成功，去拨号，添加到现有联系人*/
				if(lovecallBean != null){
					ContactManager.getInstance().addOrUpdateContact(lovecallBean);
				}
				return;
			}
			
			MaterialThreeButtonDialog dialog = new MaterialThreeButtonDialog(mContext, new MaterialThreeButtonDialog.OnClickCallback() {
				
				@Override
				public void OnSecondButtonClick(View v) {
					// TODO Auto-generated method stub
					new DirectCallManager(mContext).makeCall(lovecallBean.centerid, lovecallBean.callid);
					/*获取token成功，去拨号，添加到现有联系人*/
					if(lovecallBean != null){
						ContactManager.getInstance().addOrUpdateContact(lovecallBean);
					}
					LoginPerfence.SaveStringPreference(mContext, "donnot_show_love_call_fee", "true");
				}
				
				@Override
				public void OnFirstButtonClick(View v) {
					// TODO Auto-generated method stub
					new DirectCallManager(mContext).makeCall(lovecallBean.centerid, lovecallBean.callid);
					/*获取token成功，去拨号，添加到现有联系人*/
					if(lovecallBean != null){
						ContactManager.getInstance().addOrUpdateContact(lovecallBean);
					}
					Log.v(lovecallBean.centerid, lovecallBean.callid);
				}
				
				@Override
				public void OnCancelButtonClick(View v) {
					// TODO Auto-generated method stub
					
				}
			});
			
			dialog.hideImageView();
			dialog.setTitle(mContext.getString(R.string.lovecall_terms_title));
			dialog.setMessage(mContext.getString(R.string.lovecall_terms_detail));
			dialog.setFirstButtonText(mContext.getString(R.string.lovecall_call_now));
			dialog.setSecondButtonText(mContext.getString(R.string.love_call_dont_tell_again));
			dialog.getMessage().setGravity(Gravity.LEFT);
			dialog.getTitle().setGravity(Gravity.LEFT);
			dialog.show();

			
		} else {
			MaterialDialogAlert dialog = new MaterialDialogAlert(mContext);
			dialog.setTitle(mContext.getString(R.string.lovecall_no_sim_tips));
			dialog.setMessage(mContext.getString(R.string.lovecall_instruction, lovecallBean.centerid));
			dialog.addButton(dialog.createButton(mContext.getString(R.string.common_btn_ok), null));
			dialog.show();
		}
	}
	
	private class ViewHolder{
		LinearLayout llContainer;
		CircleImageView ivPhoto;
		TextView tvName;
		TextView tvDesc;
		ImageView btnCall;
		TextView tvTips;
		public ImageViewLoader imageDownLoader;
	}

}
