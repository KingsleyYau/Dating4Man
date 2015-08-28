package com.qpidnetwork.dating.emf;


import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.qpidnetwork.dating.R;
import com.qpidnetwork.framework.util.Log;
import com.qpidnetwork.tool.ImageViewLoader;
import com.qpidnetwork.tool.ImageViewLoader.ImageViewLoaderCallback;
import com.qpidnetwork.view.IndexFragment;
import com.qpidnetwork.view.MaterialProgressBar;
import com.qpidnetwork.view.TouchImageView;
import com.qpidnetwork.view.ViewTools;

/**
 * EMF模块
 * 显示附件图片界面
 * @author Max.Chiu
 */
public class EMFAttachmentPhotoFragment extends IndexFragment {
	
	private boolean bCanStop = false;
	
	/**
	 * 大图
	 */
	private MaterialProgressBar progress;
	private TouchImageView imageView;
	private ImageViewLoader loader;
	private String url = "";
	private String localPath = "";
	
	public EMFAttachmentPhotoFragment() {
		super();
		url = "";
		localPath = "";
	}
	
	public EMFAttachmentPhotoFragment(int index) {
		super(index);
		// TODO Auto-generated constructor stub
		url = "";
		localPath = "";
	}
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
                                                                                                                                                                                                                                                                                                                      
        View view = inflater.inflate(R.layout.fragment_emf_attachment_photo, null);
        imageView = (TouchImageView) view.findViewById(R.id.imageView);
        
        progress = (MaterialProgressBar)view.findViewById(R.id.progress);
        
        
        UpdateView();
        Log.v("on create view", "true");
        return view;
    }
    @Override
    public void onAttach(Activity activity) {
    	// TODO Auto-generated method stub
    	super.onAttach(activity);
    	loader = new ImageViewLoader(mContext);
    	loader.SetBigFileDontUseCache(true);
    }
	public EMFAttachmentPhotoFragment(int index, String url, String localPath) {
		super(index);
		// TODO Auto-generated constructor stub
		this.url = url;
		this.localPath = localPath;
	}
    
   /* public void ReloadData(String url, String localPath) {
    	this.url = url;
    	this.localPath = localPath;
    	
    	UpdateView();
    }*
    
	/**
	 * 刷新界面
	 */
	private void UpdateView() {
		progress.setVisibility(View.VISIBLE);
    	if( localPath.length() > 0 ) {
    		if( loader != null ) {
        		Context context = getActivity();
        		if( context != null ) {
        			loader.SetDefaultImage(new ColorDrawable(Color.TRANSPARENT));
        		}
        		if( imageView != null ) {
        			imageView.SetCanScale(false);
        		}
        		
        		ViewTools.PreCalculateViewSize(imageView);
                loader.DisplayImage(
                		imageView, 
                		url, 
                		localPath, 
                		new ImageViewLoaderCallback() {
					
					@Override
					public void OnDisplayNewImageFinish() {
						// TODO Auto-generated method stub
						imageView.SetCanScale(true);
						((Activity) mContext).runOnUiThread(new Runnable(){

							@Override
							public void run() {
								// TODO Auto-generated method stub
								progress.setVisibility(View.GONE);
							}
							
						});
					}

					@Override
					public void OnLoadPhotoFailed() {
						// TODO Auto-generated method stub
						imageView.SetCanScale(true);
						((Activity) mContext).runOnUiThread(new Runnable(){

							@Override
							public void run() {
								// TODO Auto-generated method stub
								progress.setVisibility(View.GONE);
							}
							
						});
						
					}
				});
    		}
    	}
	}

	@Override
	public void InitHandler() {
		// TODO Auto-generated method stub
		
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
 
}
