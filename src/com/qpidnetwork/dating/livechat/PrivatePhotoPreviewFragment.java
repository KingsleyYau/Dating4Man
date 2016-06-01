package com.qpidnetwork.dating.livechat;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.content.res.Resources.NotFoundException;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.qpidnetwork.dating.R;
import com.qpidnetwork.dating.googleanalytics.AnalyticsFragmentActivity;
import com.qpidnetwork.dating.livechat.downloader.LivechatPrivatePhotoDownloader;
import com.qpidnetwork.dating.livechat.downloader.LivechatPrivatePhotoDownloader.OnDownloadCallback;
import com.qpidnetwork.framework.base.BaseFragment;
import com.qpidnetwork.framework.util.ImageUtil;
import com.qpidnetwork.framework.util.StringUtil;
import com.qpidnetwork.framework.util.SystemUtil;
import com.qpidnetwork.livechat.LCMessageItem;
import com.qpidnetwork.livechat.LCMessageItem.SendType;
import com.qpidnetwork.livechat.LiveChatManager;
import com.qpidnetwork.livechat.LiveChatManagerPhotoListener;
import com.qpidnetwork.livechat.jni.LiveChatClientListener.LiveChatErrType;
import com.qpidnetwork.request.RequestJniLiveChat.PhotoSizeType;
import com.qpidnetwork.view.ButtonRaised;
import com.qpidnetwork.view.FlatToast;
import com.qpidnetwork.view.GetMoreCreditDialog;
import com.qpidnetwork.view.MaterialProgressBar;
import com.qpidnetwork.view.TouchImageView;

@SuppressLint("InflateParams")
public class PrivatePhotoPreviewFragment extends BaseFragment implements
		LiveChatManagerPhotoListener {

	private static final String LIVE_CHAT_MESSAGE_ITEM = "msgitem";
	
	private static final int PRIVATE_PHOTO_FEE = 0;
	
	private FlatToast flatToast;

	/**
	 * 购买view
	 */
	private RelativeLayout rlDimBody;
	private ImageView ivDim;
	private TextView textViewTips;
	private ButtonRaised buttonView;

	/**
	 * 已经购买
	 */
	private RelativeLayout rlChargedBody;
	private TouchImageView ivCharge;
	private MaterialProgressBar progressBar;

	/**
	 * 下载失败
	 */
	private LinearLayout llErrorPage;
	private ButtonRaised tvRetry;

	/**
	 * 底部描述及下载原图
	 */
	private TextView textViewDescription;
	private ImageButton downloadButton;

	/* data */
	private LCMessageItem mMsgItem;

	private LiveChatManager mLiveChatManager;
	
	private List<LivechatPrivatePhotoDownloader> mDownLoaderList;//存储下载器列表，退出界面如果还未下载完成，清除回调，防止异步回调调用界面元素导致异常死机

	public static PrivatePhotoPreviewFragment getFragment(LCMessageItem item) {
		PrivatePhotoPreviewFragment fragment = new PrivatePhotoPreviewFragment();
		Bundle bundle = new Bundle();
		bundle.putSerializable(LIVE_CHAT_MESSAGE_ITEM, item);
		fragment.setArguments(bundle);
		return fragment;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View view = inflater.inflate(R.layout.fragment_livechat_private_photo,
				null);
		initViews(view);
		return view;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onActivityCreated(savedInstanceState);
		mLiveChatManager = LiveChatManager.getInstance();
		Bundle bundle = getArguments();
		if ((bundle != null) && (bundle.containsKey(LIVE_CHAT_MESSAGE_ITEM))) {
			LCMessageItem tempItem = (LCMessageItem) bundle
					.getSerializable(LIVE_CHAT_MESSAGE_ITEM);
			if(tempItem != null && tempItem.getUserItem() != null){
				mMsgItem = mLiveChatManager.GetMessageWithMsgId(tempItem.getUserItem().userId, tempItem.msgId);
			}
		}
		if(mMsgItem != null){
			mLiveChatManager.RegisterPhotoListener(this);
			mDownLoaderList = new ArrayList<LivechatPrivatePhotoDownloader>();
			
			UpdateView();
		}
	}

	private void initViews(View view) {

		
		/**
		 * A view contain a blurred image which size is 370px * 370px, can be a default blurred image if failed to load
		 */
		rlDimBody = (RelativeLayout) view.findViewById(R.id.rlDimBody);
		ivDim = (ImageView) view.findViewById(R.id.ivDim);
		textViewTips = (TextView) view.findViewById(R.id.textViewTips);
		buttonView = (ButtonRaised) view.findViewById(R.id.buttonView);

		
		/**
		 * A view contain a clean image which size is 370 *370
		 */
		rlChargedBody = (RelativeLayout) view.findViewById(R.id.rlChargedBody);
		ivCharge = (TouchImageView) view.findViewById(R.id.ivCharge);
		progressBar = (MaterialProgressBar) view.findViewById(R.id.progressBar);

		
		/**
		 * A view contains a broken image with a retry button
		 */
		llErrorPage = (LinearLayout) view.findViewById(R.id.llErrorPage);
		tvRetry = (ButtonRaised) view.findViewById(R.id.tvRetry);

		
		/**
		 * photo description if there is.
		 */
		textViewDescription = (TextView) view
				.findViewById(R.id.textViewDescription);
		downloadButton = (ImageButton) view
				.findViewById(R.id.imageViewDownload);

		buttonView.setOnClickListener(this);
		tvRetry.setOnClickListener(this);
		downloadButton.setOnClickListener(this);
		
		
	}

	/**
	 * 刷新界面
	 */
	private void UpdateView() {
		if( ivCharge != null ) {
			ivCharge.SetCanScale(false);
		}
		
		/** Set photo description **/
		textViewDescription.setText(mMsgItem.getPhotoItem().photoDesc);
		
		/** Set private photo price **/
		if( textViewTips != null ) {
			double credit = 1.5;
    		String format = getResources().getString(R.string.emf_private_photo_tips);
    		textViewTips.setText(String.format(format, credit));
    	}
		
		if (mMsgItem.getPhotoItem().charge) {
			
			/*解决发送图片查看时，本地已有，但是还是显示现在失败等异常*/
			if(mMsgItem.sendType == SendType.Send){
				String filePath = mMsgItem.getPhotoItem().srcFilePath; 
				if(!StringUtil.isEmpty(filePath) && new File(filePath).exists()){
					setDownloadClearImageViewOnSuccessful(filePath);
					return;
				}
			}

			/**
			 * Download clear image if the photo is paid.
			 */
			
			setDownloadClearImageView();
			LivechatPrivatePhotoDownloader downloader = new LivechatPrivatePhotoDownloader(getActivity());
			downloader.startDownload(mMsgItem,
					PhotoSizeType.Large, new OnDownloadCallback() {

						@Override
						public void onPrivatePhotoDownloadSuccess(LivechatPrivatePhotoDownloader downloader, String filePath) {
							mDownLoaderList.remove(downloader);
							setDownloadClearImageViewOnSuccessful(filePath);
						}

						@Override
						public void onPrivatePhotoDownloadFail(LivechatPrivatePhotoDownloader downloader) {
							mDownLoaderList.remove(downloader);
							setDownloadClearImageViewOnError();
						}
			});
			mDownLoaderList.add(downloader);
			
		} else {
				/**
				 * Download blurred image if the photo is unpaid.
				 */
				
				
			if((!StringUtil.isEmpty(mMsgItem.getPhotoItem().showFuzzyFilePath))
					&& (new File(mMsgItem.getPhotoItem().showFuzzyFilePath).exists())){
				/** If the blurred image exists, show immediately **/
				setDownloadBlurredImageViewOnSuccessful(mMsgItem.getPhotoItem().showFuzzyFilePath);
				
			}else{
				setDownloadBlurredImageView();
				LivechatPrivatePhotoDownloader downloader = new LivechatPrivatePhotoDownloader(getActivity());
				downloader.startDownload(mMsgItem,
						PhotoSizeType.Large, new OnDownloadCallback() {

							@Override
							public void onPrivatePhotoDownloadSuccess(LivechatPrivatePhotoDownloader downloader, String filePath) {
								mDownLoaderList.remove(downloader);
								setDownloadBlurredImageViewOnSuccessful(filePath);
							}

							@Override
							public void onPrivatePhotoDownloadFail(LivechatPrivatePhotoDownloader downloader) {
								mDownLoaderList.remove(downloader);
								setDownloadBlurredImageViewOnError();
							}
				});
				mDownLoaderList.add(downloader);
			}
		}
	}
	
	
	private void setDownloadBlurredImageView(){
		rlDimBody.setVisibility(View.GONE);
		rlChargedBody.setVisibility(View.GONE);
		llErrorPage.setVisibility(View.GONE);
		downloadButton.setVisibility(View.GONE);
		progressBar.setVisibility(View.VISIBLE);
	}
	
	private void setDownloadBlurredImageViewOnSuccessful(String filePath){
		rlDimBody.setVisibility(View.VISIBLE);
		rlChargedBody.setVisibility(View.GONE);
		llErrorPage.setVisibility(View.GONE);
		downloadButton.setVisibility(View.GONE);
		progressBar.setVisibility(View.GONE);
		int screenWidth = SystemUtil.getDisplayMetrics(getActivity()).widthPixels;
		int screenHeight = SystemUtil.getDisplayMetrics(getActivity()).heightPixels;
		Bitmap bitmap = ImageUtil.decodeAndScaleBitmapFromFile(filePath, screenWidth, screenHeight);
		ivDim.setImageBitmap(bitmap);
	}
	
	private void setDownloadBlurredImageViewOnError(){
		rlDimBody.setVisibility(View.VISIBLE);
		rlChargedBody.setVisibility(View.GONE);
		llErrorPage.setVisibility(View.GONE);
		downloadButton.setVisibility(View.GONE);
		progressBar.setVisibility(View.GONE);
		ivDim.setImageResource(R.drawable.img_default_blurred_image);
	}
	
	
	
	private void setDownloadClearImageView(){
		rlDimBody.setVisibility(View.GONE);
		llErrorPage.setVisibility(View.GONE);
		downloadButton.setVisibility(View.GONE);
		rlChargedBody.setVisibility(View.VISIBLE);
		progressBar.setVisibility(View.VISIBLE);
		ivCharge.SetCanScale(false);
	}
	
	private void setDownloadClearImageViewOnSuccessful(String filePath){
		rlDimBody.setVisibility(View.GONE);
		llErrorPage.setVisibility(View.GONE);
		downloadButton.setVisibility(View.VISIBLE);
		rlChargedBody.setVisibility(View.VISIBLE);
		progressBar.setVisibility(View.GONE);
		int screenWidth = SystemUtil.getDisplayMetrics(getActivity()).widthPixels;
		int scrrenHeight = SystemUtil.getDisplayMetrics(getActivity()).heightPixels;
		Bitmap bitmap = ImageUtil.decodeAndScaleBitmapFromFile(filePath, screenWidth, scrrenHeight);
		if (bitmap != null) {
			ivCharge.setImageBitmap(bitmap);
			ivCharge.SetCanScale(true);
		}else{
			setDownloadClearImageViewOnError();
		}
		
	}
	
	private void setDownloadClearImageViewOnError(){
		progressBar.setVisibility(View.GONE);
		downloadButton.setVisibility(View.GONE);
		llErrorPage.setVisibility(View.VISIBLE);
		rlDimBody.setVisibility(View.GONE);
		rlChargedBody.setVisibility(View.GONE);
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		super.onClick(v);
		switch (v.getId()) {
		case R.id.buttonView:
			/* 购买按钮 */
			photoFee();
			break;
		case R.id.imageViewDownload:
			/* 下载原图按钮 */
			downloadSourcePicture();
			break;
		case R.id.tvRetry:
			/* 下载失败，重新下图 */
			UpdateView();
			break;
		case R.id.buttonCancel:
			getActivity().finish();
			break;
		default:
			break;
		}
	}
	
	private void downloadSourcePicture(){
		
		if (flatToast == null) 
			flatToast = new FlatToast(getActivity());
		flatToast.setProgressing("Downloading");
		flatToast.show();
		
		/*开始下载，disable下载按钮*/
		downloadButton.setClickable(false);
		
		LivechatPrivatePhotoDownloader downloader = new LivechatPrivatePhotoDownloader(getActivity());
		downloader.startDownload(mMsgItem, PhotoSizeType.Original, new OnDownloadCallback() {
			
			@Override
			public void onPrivatePhotoDownloadSuccess(LivechatPrivatePhotoDownloader downloader , String filePath) {
				mDownLoaderList.remove(downloader);
				flatToast.setDone("Done!");
				/*下载成功，enable下载按钮*/
				downloadButton.setClickable(true);
				try {
					// 直接保存到相册
					String fileName = mMsgItem.getPhotoItem().photoId + "-" + System.currentTimeMillis() + ".jpg";
					ImageUtil.SaveImageToGallery(getActivity(), mMsgItem.getPhotoItem().showSrcFilePath, filePath, fileName, null);
					
//					MediaStore.Images.Media.insertImage(getActivity().getContentResolver(), filePath, mMsgItem.getPhotoItem().photoDesc , getActivity().getResources().getString(R.string.app_name));
				} catch (NotFoundException e) {
					// TODO Auto-generated catch block
					flatToast.setFailed("Failed!");
				}
				Toast.makeText(getActivity(), getString(R.string.livechat_saved_origional_image), Toast.LENGTH_LONG).show();
			}
			
			@Override
			public void onPrivatePhotoDownloadFail(LivechatPrivatePhotoDownloader downloader) {
				mDownLoaderList.remove(downloader);
				flatToast.setFailed("Failed!");
				/*下载成功，enable下载按钮*/
				downloadButton.setClickable(true);
			}
		});
		mDownLoaderList.add(downloader);
	}
	
	/**
	 * 付费购买图片
	 */
	private void photoFee(){
		mLiveChatManager.PhotoFee(mMsgItem);
		setDownloadClearImageView();
	}
	
	@Override
	protected void handleUiMessage(Message msg) {
		// TODO Auto-generated method stub
		super.handleUiMessage(msg);
		switch (msg.what) {
		case PRIVATE_PHOTO_FEE:
			{
				LiveChatCallBackItem callbackItem = (LiveChatCallBackItem)msg.obj;
				progressBar.setVisibility(View.GONE);
				if(callbackItem.errType == 1){
					/*付费成功*/
					mMsgItem = (LCMessageItem)callbackItem.body;
					UpdateView();
				}else{
					/*付费失败*/
					if(callbackItem.errNo.equals("ERROR00003")){
						final GetMoreCreditDialog dialog = new GetMoreCreditDialog(getActivity(), R.style.ChoosePhotoDialog);
				        dialog.show();
				        break;
					}
					
					UpdateView();
				}
			}
			break;
		default:
			break;
		}
	}
	
	/**
	 * 图片放大查看，切换到下一张时需还原到为放大状态
	 */
	public void reset(){
		if(ivCharge != null){
			ivCharge.Reset();
		}
	}
	
	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		if(flatToast != null){
			flatToast.cancelImmediately();
		}
		mLiveChatManager.UnregisterPhotoListener(this);
		if(mDownLoaderList != null){
			/*清除下载器回调，防止界面结束仍回调导致界面异常*/
			for(LivechatPrivatePhotoDownloader downloader : mDownLoaderList){
				downloader.unregisterDownloaderCallback();
			}
			mDownLoaderList.clear();
		}
	}
	

	@Override
	public void OnSendPhoto(LiveChatErrType errType, String errno,
			String errmsg, LCMessageItem item) {
		// TODO Auto-generated method stub

	}

	@Override
	public void OnPhotoFee(boolean success, String errno, String errmsg,
			LCMessageItem item) {
		if(item.msgId == mMsgItem.msgId){
			Message msg = Message.obtain();
			msg.what = PRIVATE_PHOTO_FEE;
			LiveChatCallBackItem callbackItem = new LiveChatCallBackItem(success?1:0, errno, errmsg, item);
			msg.obj = callbackItem;
			sendUiMessage(msg);
		}
	}

	@Override
	public void OnGetPhoto(LiveChatErrType errType, String errno,
			String errmsg, LCMessageItem item) {
	}

	@Override
	public void OnRecvPhoto(LCMessageItem item) {
		// TODO Auto-generated method stub

	}
	
	@Override
	public void onFragmentSelected(int page) {
		// 统计
		AnalyticsFragmentActivity activity = getAnalyticsFragmentActivity();
		if (null != activity) {
			activity.onAnalyticsPageSelected(this, page);
		}
	}
	
	private AnalyticsFragmentActivity getAnalyticsFragmentActivity()
	{
		AnalyticsFragmentActivity activity = null;
		if (getActivity() instanceof AnalyticsFragmentActivity)
		{
			activity = (AnalyticsFragmentActivity)getActivity();
		}
		return activity;
	}
}
