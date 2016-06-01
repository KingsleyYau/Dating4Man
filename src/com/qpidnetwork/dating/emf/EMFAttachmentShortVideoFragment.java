package com.qpidnetwork.dating.emf;

import java.io.File;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.qpidnetwork.dating.R;
import com.qpidnetwork.dating.bean.RequestBaseResponse;
import com.qpidnetwork.dating.bean.ShortVideoBean;
import com.qpidnetwork.dating.emf.EMFVideoManager.OnEmfVideoDownloadCallback;
import com.qpidnetwork.dating.googleanalytics.AnalyticsFragmentActivity;
import com.qpidnetwork.dating.livechat.VideoPlayActivity;
import com.qpidnetwork.request.OnRequestFileCallback;
import com.qpidnetwork.request.RequestJniLiveChat.VideoPhotoType;
import com.qpidnetwork.tool.ImageViewLoader;
import com.qpidnetwork.view.IndexFragment;
import com.qpidnetwork.view.MaterialProgressBar;
import com.qpidnetwork.view.TouchImageView;
import com.qpidnetwork.view.ViewTools;

@SuppressLint("InflateParams")
public class EMFAttachmentShortVideoFragment extends IndexFragment implements OnClickListener{

	public static final String SHORT_VIDEO_ITEM = "shortVideoItem";

	private static final int GET_VIDEO_CALLBACK = 0;
	private static final int SHORT_VIDEO_FEE_CALLBACK = 1;
	private static final int GET_VIDEO_THUMBPHOTO_CALLBACK = 2; 

	private ShortVideoBean shortVideo;

	private TouchImageView imageView;
	private ImageViewLoader loader;// 图片显示处理
	private MaterialProgressBar progressBar;
	// 下载提示
	private LinearLayout llDonwloadPlayParent;
	private ImageButton ivBtnDownloadPlay;
	private TextView tvDownloadTips;
	private TextView tvPriceTips;

	private EMFVideoManager mEMFVideoManager;
	
	private boolean isFragmentVisible = false;//当前的fragment是否可见，下载视频完成直接跳转播放

	public EMFAttachmentShortVideoFragment() {
		super();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Bundle bundle = getArguments();
		if (bundle != null) {
			if (bundle.containsKey(SHORT_VIDEO_ITEM)) {
				shortVideo = bundle.getParcelable(SHORT_VIDEO_ITEM);
			}
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(
				R.layout.fragment_emf_attachment_short_video, null);

		imageView = (TouchImageView) view.findViewById(R.id.imageView);
		imageView.SetCanScale(false);

		loader = new ImageViewLoader(mContext);
		mEMFVideoManager = EMFVideoManager.newInstance(mContext);

		progressBar = (MaterialProgressBar) view.findViewById(R.id.progressBar);
		llDonwloadPlayParent = (LinearLayout) view.findViewById(R.id.llDonwloadPlayParent);
		ivBtnDownloadPlay = (ImageButton) view
				.findViewById(R.id.ivBtnDownloadPlay);
		ivBtnDownloadPlay.setOnClickListener(this);
		tvDownloadTips = (TextView) view.findViewById(R.id.tvDownloadTips);
		tvPriceTips = (TextView) view.findViewById(R.id.tvPriceTips);
		// 更新当前UI界面
		initVideoThumbPhoto(true);
		updateUI();
		return view;
	}

	@Override
	public void onPageScrollStateChanged(int arg0) {

	}

	@Override
	public void onPageScrolled(int arg0, float arg1, int arg2) {

	}

	@Override
	public void onPageSelected(int arg0) {

	}
	
	@Override
	protected void handleUiMessage(Message msg) {
		super.handleUiMessage(msg);
		RequestBaseResponse response = (RequestBaseResponse) msg.obj;
		switch (msg.what) {
		case GET_VIDEO_CALLBACK: {
			if (!response.isSuccess) {
				String tips = "";
				if(TextUtils.isEmpty(response.errmsg)){
					tips = mContext.getString(R.string.emf_short_video_download_fail);
				}else{
					tips = response.errmsg;
				}
				Toast.makeText(mContext, tips,
						Toast.LENGTH_LONG).show();
			}
			updateUI();
			if(response.isSuccess && isFragmentVisible){
				// 下载成功，且在当前页面直接跳转播放页面
				String videoPath = mEMFVideoManager.getVideoPath(shortVideo.womanid,
						shortVideo.sendId, shortVideo.videoId, shortVideo.messageid);
				String videoThumbPhotoPath = mEMFVideoManager.getVideoThumbPhotoPath(
						shortVideo.womanid, shortVideo.sendId, shortVideo.videoId,
						shortVideo.messageid, VideoPhotoType.Big);
				VideoPlayActivity.launchVideoPlayActivity(mContext,
						videoThumbPhotoPath, videoPath, true);
			}
		}
			break;
		case GET_VIDEO_THUMBPHOTO_CALLBACK: {
			if(response.isSuccess){
				initVideoThumbPhoto(false);
			}
		}
			break;
		case SHORT_VIDEO_FEE_CALLBACK: {
			if(response.isSuccess){
				shortVideo.videoFee = true;
				((EMFAttachmentPreviewActivity)mContext).updateVideoFeeStatus(shortVideo);
			}
		}
			break;
		default:
			break;
		}
	}
	
	/**
	 * 初始化视频ThumbPhoto缩略图
	 */
	@SuppressWarnings("deprecation")
	private void initVideoThumbPhoto(boolean isDownload){
		// 视频缩略图相关
		String loaclPath = mEMFVideoManager.getVideoThumbPhotoPath(
				shortVideo.womanid, shortVideo.sendId, shortVideo.videoId,
				shortVideo.messageid, VideoPhotoType.Big);
		File file = new File(loaclPath);
		if (file.exists() && file.isFile()) {
			//视频缩略图本地存在
			if( loader != null ) {
    			ViewTools.PreCalculateViewSize(imageView);
    			loader.SetDefaultImage(mContext.getResources().getDrawable(R.drawable.img_default_blurred_image));
    			loader.DisplayImage(
    					imageView, 
    					"", 
    					loaclPath, 
    					null);
    		}
		}else{
			//本地无缩略图，去下载
			if(isDownload){
				mEMFVideoManager.GetVideoThumbPhoto(shortVideo.womanid, shortVideo.sendId, shortVideo.videoId, shortVideo.messageid, VideoPhotoType.Big, new OnRequestFileCallback() {
					
					@Override
					public void OnRequestFile(long requestId, boolean isSuccess, String errno,
							String errmsg, String filePath) {
						Message msg = Message.obtain();
						RequestBaseResponse response = new RequestBaseResponse(isSuccess, errno, errmsg, filePath);
						msg.what = GET_VIDEO_THUMBPHOTO_CALLBACK;
						msg.obj = response;
						sendUiMessage(msg);
					}
				});
			}
		}
	}

	/**
	 * 根据已有数据更新界面
	 */
	private void updateUI() {
		if (shortVideo != null) {
			
			// 视频相关
			if (mEMFVideoManager.isGetVideoNow(shortVideo.videoId)) {
				// 已购买正在下载中
				progressBar.setVisibility(View.VISIBLE);
				llDonwloadPlayParent.setVisibility(View.GONE);
				tvPriceTips.setVisibility(View.GONE);
			} else {
				tvDownloadTips.setVisibility(View.VISIBLE);
				llDonwloadPlayParent.setVisibility(View.VISIBLE);
				progressBar.setVisibility(View.GONE);
				// 未在正在下载(或购买)中
				if (shortVideo.videoFee) {
					// 已付费
					ivBtnDownloadPlay.setVisibility(View.VISIBLE);
					tvPriceTips.setVisibility(View.GONE);
					if (isVideoLoaclExist()) {
						// 本地文件已存在，直接转成播放按钮
						tvDownloadTips.setVisibility(View.GONE);
						ivBtnDownloadPlay.setImageResource(R.drawable.ic_play_circle_outline_white_48dp);
					} else {
						ivBtnDownloadPlay.setImageResource(R.drawable.ic_file_download_white_24dp);
					}
				} else {
					// 未付费
					ivBtnDownloadPlay.setVisibility(View.VISIBLE);
					tvPriceTips.setVisibility(View.VISIBLE);
					ivBtnDownloadPlay.setImageResource(R.drawable.ic_file_download_white_24dp);
				}
			}
		}
	}


	/**
	 * 视频文件本地是否存在
	 */
	private boolean isVideoLoaclExist() {
		boolean isExist = false;
		String loaclPath = mEMFVideoManager.getVideoPath(shortVideo.womanid,
				shortVideo.sendId, shortVideo.videoId, shortVideo.messageid);
		File file = new File(loaclPath);
		if (file.exists() && file.isFile()) {
			isExist = true;
		}

		return isExist;
	}

	/**
	 * 点击下载或播放
	 * 
	 * @param v
	 */
	public void onClickDownload(View v) {
		if (isVideoLoaclExist()&&(shortVideo.videoFee)) {
			// 本地已存在，去播放
			String videoPath = mEMFVideoManager.getVideoPath(shortVideo.womanid,
					shortVideo.sendId, shortVideo.videoId, shortVideo.messageid);
			String videoThumbPhotoPath = mEMFVideoManager.getVideoThumbPhotoPath(
					shortVideo.womanid, shortVideo.sendId, shortVideo.videoId,
					shortVideo.messageid, VideoPhotoType.Big);
			VideoPlayActivity.launchVideoPlayActivity(mContext,
					videoThumbPhotoPath, videoPath, true);
		} else {
			// 本地不存在去下载
			progressBar.setVisibility(View.VISIBLE);
			llDonwloadPlayParent.setVisibility(View.GONE);
			tvPriceTips.setVisibility(View.GONE);
			mEMFVideoManager.getVideo(shortVideo.womanid, shortVideo.sendId,
					shortVideo.videoId, shortVideo.messageid,
					new OnEmfVideoDownloadCallback() {

						@Override
						public void onEmfVideoDownload(boolean isSuccess,
								String errno, String errmsg, String videoUrl) {
							Message msg = Message.obtain();
							RequestBaseResponse response = new RequestBaseResponse(
									isSuccess, errno, errmsg, videoUrl);
							msg.what = GET_VIDEO_CALLBACK;
							msg.obj = response;
							sendUiMessage(msg);
						}
						
						@Override
						public void onEMfFeeCallback(boolean isFeed) {
							//购买成功回调
							Message msg = Message.obtain();
							RequestBaseResponse response = new RequestBaseResponse(
									true, "", "", "");
							msg.what = SHORT_VIDEO_FEE_CALLBACK;
							msg.obj = response;
							sendUiMessage(msg);
						}
					});
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.ivBtnDownloadPlay:
			onClickDownload(v);
			break;

		default:
			break;
		}
		
	}
	
	@Override
	/*用来处理fragment的可见与不可见*/
	public void setUserVisibleHint(boolean isVisibleToUser) {
		// TODO Auto-generated method stub
		super.setUserVisibleHint(isVisibleToUser);
		isFragmentVisible = isVisibleToUser;
	}
	
	@Override
	public void onFragmentSelected(int page) 
	{
		// 判断是否本页
		if (getIndex() == page)
		{
			// 统计
			AnalyticsFragmentActivity activity = getAnalyticsFragmentActivity();
			if (null != activity) {
				activity.onAnalyticsPageSelected(this, page);
			}
		}
	}
	
	private AnalyticsFragmentActivity getAnalyticsFragmentActivity()
	{
		AnalyticsFragmentActivity analyticsActivity = null;
		FragmentActivity activity = getActivity();
		if (activity instanceof AnalyticsFragmentActivity)
		{
			analyticsActivity = (AnalyticsFragmentActivity)getActivity();
		}
		return analyticsActivity;
	}
}
