package com.qpidnetwork.dating.emf;


import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.qpidnetwork.dating.R;
import com.qpidnetwork.dating.googleanalytics.AnalyticsFragmentActivity;
import com.qpidnetwork.tool.ImageViewLoader;
import com.qpidnetwork.tool.ImageViewLoader.ImageViewLoaderCallback;
import com.qpidnetwork.view.ButtonRaised;
import com.qpidnetwork.view.IndexFragment;
import com.qpidnetwork.view.MaterialProgressBar;
import com.qpidnetwork.view.TouchImageView;
import com.qpidnetwork.view.ViewTools;

/**
 * EMF模块
 * 显示附件私密照界面
 * @author Max.Chiu
 */
public class EMFAttachmentPrivatePhotoFragment extends IndexFragment
											   implements OnClickListener,
											   			  ImageViewLoaderCallback
{
	
	public interface OnClickBuy {
		public void onClickBuy(EMFAttachmentPrivatePhotoFragment fragment);
		public void onClickDownload(EMFAttachmentPrivatePhotoFragment fragment);
	}
	
	private boolean bCanStop = false;
	
	private OnClickBuy callback = null;
	
	/**
	 * 大图
	 */
	private TouchImageView imageView;
	private ImageViewLoader loader;
	
	/**
	 * 购买布局
	 */
	private LinearLayout layoutBuy;
	private TextView textViewTips;
	
	/**
	 * 描述
	 */
	private TextView textViewDescription;
	
	private ButtonRaised buttonView;
	
	private ImageView imageViewDownload;
	
	/**
	 * 菊花
	 */
	private MaterialProgressBar progressBar;
	
	private PrivatePhotoDirection privatePhotoDirection;
	private String localPath = "";
	private String description = "";
	private double credit = 0;
	
	public static enum PrivatePhotoDirection{
		WM, //Woman sent to man
		MW //Man sent to woman
	}

	public EMFAttachmentPrivatePhotoFragment() {
		super();
		localPath = "";
		description = "";
		credit = 0;
	}
	
	public EMFAttachmentPrivatePhotoFragment(int index, PrivatePhotoDirection direction) {
		super(index);
		// TODO Auto-generated constructor stub
		localPath = "";
		description = "";
		credit = 0;
		privatePhotoDirection = direction;
	}
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
    
    @SuppressLint("InflateParams")
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
                                                                                                                                                                                                                                                                                                                      
        View view = inflater.inflate(R.layout.fragment_emf_attachment_private_photo, null);
        
        loader = new ImageViewLoader(mContext);
        imageView = (TouchImageView) view.findViewById(R.id.imageView);
        layoutBuy = (LinearLayout) view.findViewById(R.id.layoutBuy);
        textViewTips = (TextView) view.findViewById(R.id.textViewTips);
        textViewDescription = (TextView) view.findViewById(R.id.textViewDescription);
        buttonView = (ButtonRaised) view.findViewById(R.id.buttonView);
        buttonView.setOnClickListener(this);
        
        imageViewDownload = (ImageView) view.findViewById(R.id.imageViewDownload);
        imageViewDownload.setOnClickListener(this);
        
        progressBar = (MaterialProgressBar) view.findViewById(R.id.progressBar);
        progressBar.setVisibility(View.GONE);
        
        if( privatePhotoDirection != null ) {
            if (privatePhotoDirection.equals(PrivatePhotoDirection.WM)){
            	UpdateViewWM();
            }else{
            	UpdateViewMW();
            }
        }

        return view;
    }
    
    public void SetOnClickBuyListener(OnClickBuy callback) {
    	this.callback = callback;
    }
    
    /**
     * 加载数据
     * @param url
     * @param localPath
     * @param description
     */
    public void ReloadData(String localPath, String description, double credit) {
    	this.localPath = localPath;
    	this.description = description;
    	this.credit = credit;
    	
    	if( privatePhotoDirection != null ) {
            if (privatePhotoDirection.equals(PrivatePhotoDirection.WM)){
            	UpdateViewWM();
            } else {
            	UpdateViewMW();
            }
    	}

    }
	
	/**
	 * 点击下载
	 * @param v
	 */
	public void onClickDownload(View v) {
		if( this.callback != null ) {
			this.callback.onClickDownload(this);
		}
	}
 
	/**
	 * 点击购买并查看
	 * @param v
	 */
	public void onClickView(View v) {
		if( this.callback != null ) {
			this.callback.onClickBuy(this);
		}
	}
	
	/**
	 * 刷新界面
	 */
	@SuppressWarnings("deprecation")
	private void UpdateViewWM() {
		if( imageView != null ) {
			imageView.SetCanScale(false);
		}
		
    	if( localPath.length() > 0 ) {
    		// 已经购买, 有图片， 则显示
    		if( loader != null ) {
    			ViewTools.PreCalculateViewSize(imageView);
    			loader.SetDefaultImage(mContext.getResources().getDrawable(R.drawable.img_default_blurred_image));
    			imageView.SetCanScale(true);
    			loader.DisplayImage(
    					imageView, 
    					"", 
    					localPath, 
    					this);
    		}
    		
    		// 隐藏购买按钮
    		if( layoutBuy != null ) {
    			layoutBuy.setVisibility(View.INVISIBLE);
    		}
    	} else {
    		// 还没购买，显示购买按钮
    		if( layoutBuy != null ) {
    			layoutBuy.setVisibility(View.VISIBLE);
    		}
    		
    		// 显示默认图片
    		if( imageView != null )
    			imageView.setImageResource(R.drawable.u109);
    	}
    	
    	if( textViewTips != null ) {
    		String format = mContext.getResources().getString(R.string.emf_private_photo_tips);
    		textViewTips.setText(String.format(format, this.credit));
    	}
    	
    	if( textViewDescription != null ) {
    		textViewDescription.setText(description);
    	}

	}
	
	/**
	 * 刷新界面
	 */
	private void UpdateViewMW() {
		if( imageView != null ) {
			imageView.SetCanScale(false);
			layoutBuy.setVisibility(View.INVISIBLE);
			imageView.setImageDrawable(new ColorDrawable(Color.TRANSPARENT));
		}
		
    	if( localPath.length() > 0 ) {
    		// 已经购买, 有图片， 则显示
    		if( loader != null ) {
    			ViewTools.PreCalculateViewSize(imageView);
    			loader.SetDefaultImage(new ColorDrawable(Color.TRANSPARENT));
    			imageView.SetCanScale(true);
    			loader.DisplayImage(
    					imageView, 
    					"", 
    					localPath, 
    					this);
    		}
    		

    	}

    	



	}

	@Override
	public void onPageScrollStateChanged(int arg0) {
		// TODO Auto-generated method stub
		if( arg0 == 0 && bCanStop ) {
			// 还原图片大小
			imageView.Reset();
		}
	}

	@Override
	public void onPageScrolled(int arg0, float arg1, int arg2) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onPageSelected(int arg0) {
		// TODO Auto-generated method stub
		if( arg0 != getIndex() ) {
			bCanStop = true;
		} else {
			bCanStop = false;
		}
	}
	
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		if (v.getId() == R.id.buttonView) {
			onClickView(v);
		}
		else if (v.getId() == R.id.imageViewDownload) {
			onClickDownload(v);
		}
	}

	@Override
	public void OnDisplayNewImageFinish(Bitmap bmp) {
		// TODO Auto-generated method stub
//		imageView.SetCanScale(true);
	}

	@Override
	public void OnLoadPhotoFailed() {
		// TODO Auto-generated method stub
		
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
		AnalyticsFragmentActivity activity = null;
		if (getActivity() instanceof AnalyticsFragmentActivity)
		{
			activity = (AnalyticsFragmentActivity)getActivity();
		}
		return activity;
	}
}
