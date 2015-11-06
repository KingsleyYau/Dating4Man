package com.qpidnetwork.dating.quickmatch;

import java.util.List;
import java.util.Random;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.qpidnetwork.dating.R;
import com.qpidnetwork.dating.lady.LadyDetailActivity;
import com.qpidnetwork.dating.quickmatch.QuickMatchManager.OnQueryQuickMatchManagerLadyListCallback;
import com.qpidnetwork.framework.util.Log;
import com.qpidnetwork.manager.FileCacheManager;
import com.qpidnetwork.request.item.QuickMatchLady;
import com.qpidnetwork.tool.ImageViewLoader;
import com.qpidnetwork.view.ViewTools;

/**
 * QuickMatch模块
 * 选择like/pass界面
 * @author Max.Chiu
 *
 */
public class QuickMatchMatchesFragment extends Fragment implements OnClickListener {
	private static QuickMatchMatchesFragment gFragment = null;
	
	private enum RequestFlag {
		REQUEST_SUCCESS,
		REQUEST_FAIL,
	}
	
	// 放大背景
	public ImageView imageViewManBackground;
	//public ImageView imageViewManBackground2;
	private TextView no_quota;
	
	
	Point screenSize = new Point();
	Display display;
	private boolean isAnimateCanclled = false;
    private AnimationSet anSet;
    //private AnimationSet anSet2;
    public ScaleAnimation mAnimation;
    //public ScaleAnimation mAnimation2;
    public AlphaAnimation mAlphaAmin;
    //public AlphaAnimation mAlphaAmin2;
    
	// 当前女士静态界面
	private ImageViewLoader loader;
	private View layoutLady;
	private ImageView imageViewLadyHeader;
	private TextView textViewName;
	private TextView textViewAge;
	private TextView textViewCountry;
	
	// 下个女士静态界面
	private ImageViewLoader loaderNext;
	private View layoutLadyNext;
	private ImageView imageViewLadyHeaderNext;
	private TextView textViewNameNext;
	private TextView textViewAgeNext;
	private TextView textViewCountryNext;
	private int[] brandColors = new int[]{
		R.color.brand_color_light11,
		R.color.brand_color_light12,
		R.color.brand_color_light13,
		R.color.brand_color_light14,
		R.color.brand_color_light15
	};
	
	// 女士拖动界面 
	private QuickMatchImageView imageViewLady;
	// like界面
	private ImageView imageViewLike;
	// pass界面
	private ImageView imageViewPass;
	// 当前女士序号界面
	private TextView textViewTipsIndex;
	// 总女士数量界面 
	private TextView textViewTipsCount;
	
	// 女士数量提示
	private LinearLayout layoutIndex;
	public LinearLayout layoutTipsBackground;
	
	protected Handler mHandler = null;
	private Context mContext = null;
	
	// 数据
	private List<QuickMatchLady> mQuickMatchLadyList = null;
	private int mIndex = 0;
	
	public static QuickMatchMatchesFragment newInstance() {
		if( gFragment == null ) {
			gFragment = new QuickMatchMatchesFragment();
		}
        return gFragment;
    }
	
    @SuppressWarnings("deprecation")
	@SuppressLint("NewApi") @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        display = getActivity().getWindowManager().getDefaultDisplay();
        if (Build.VERSION.SDK_INT > 12){
        	display.getSize(screenSize);
        }else{
        	screenSize.x = display.getWidth();
        	screenSize.y = display.getHeight();
        }
        
        float waveSize = 110 * this.getResources().getDisplayMetrics().density;
        float scaleTimes = (screenSize.y / waveSize) * 1.2f;
        mAnimation = new ScaleAnimation(
        		0.0f, scaleTimes, 0.0f, scaleTimes, 
        		Animation.RELATIVE_TO_SELF, 0.5f, 
        		Animation.RELATIVE_TO_SELF, 0.5f
        		);
        
       /* mAnimation2 = new ScaleAnimation(
        		0.0f, scaleTimes, 0.0f, scaleTimes, 
        		Animation.RELATIVE_TO_SELF, 0.5f, 
        		Animation.RELATIVE_TO_SELF, 0.5f
        		);*/
        
         mAlphaAmin = new AlphaAnimation(1, 0);
        // mAlphaAmin2 = new AlphaAnimation(1, 0);
        
        
        mIndex = 0;
    }
    
    @Override
    public void onAttach(Activity activity) {
    	// TODO Auto-generated method stub
    	super.onAttach(activity);
    	mContext = activity;
    }
    
    @Override
    public void onDetach() {
    	// TODO Auto-generated method stub
    	super.onDetach();
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
    	
        loader = new ImageViewLoader(mContext); 
        loaderNext = new ImageViewLoader(mContext); 
        
    	// 初始化界面
		View view = InitView(inflater, container);
		
		// 初始化事件监听
		InitHandler();
		
		// 显示本地数据 
//		ShowCurrentLady();
		
		// 请求接口
		LoadDataFromServer();
        
        return view;
    }
    
	/**
	 * 显示或者隐藏like/pass标记
	 * @param bShowLike			是否显示like标记
	 * @param bShowPass			是否显示pass标记
	 */
	private void ShowLikeOrPass(boolean bShowLike, boolean bShowPass) {

		if( bShowLike ) {
			imageViewLike.setVisibility(View.VISIBLE);
		} else {
			imageViewLike.setVisibility(View.INVISIBLE);
		}
		
		if( bShowPass ) {
			imageViewPass.setVisibility(View.VISIBLE);
		} else {
			imageViewPass.setVisibility(View.INVISIBLE);
		}
		
		// 显示当前女士静态界面并截图
		int visible = layoutLady.getVisibility();
		layoutLady.setVisibility(View.VISIBLE);
		layoutLady.invalidate();
		Bitmap bitmap = ViewTools.ConvertViewToBitmap(layoutLady);
		imageViewLady.setImageBitmap(bitmap);
		layoutLady.setVisibility(visible);
	}
	
	/**
	 * 请求服务器接口
	 */
	private void LoadDataFromServer() {
		// 此处应有菊花
		QuickMatchManager.getInstance().QueryMatchLadyList(new OnQueryQuickMatchManagerLadyListCallback() {
			
			@Override
			public void OnQueryLadyList(
					boolean isSuccess, 
					String errno, 
					String errmsg, 
					List<QuickMatchLady> itemList,
					int index
					) {
				// TODO Auto-generated method stub
				Message msg = Message.obtain();
				if( isSuccess ) {
					// 获取匹配女士列表成功
					msg.what = RequestFlag.REQUEST_SUCCESS.ordinal();
					mQuickMatchLadyList = itemList;
					msg.arg1 = index;
				} else {
					// 获取匹配女士列表失败
					msg.what = RequestFlag.REQUEST_FAIL.ordinal();
				}
				mHandler.sendMessage(msg);

			}
		});
	}
	
	/**
	 * 刷新女士资料
	 */
	private void ShowCurrentLady() {
		
		layoutLady.setVisibility(View.VISIBLE);
		layoutLadyNext.setVisibility(View.VISIBLE);
		layoutIndex.setVisibility(View.VISIBLE);
		layoutTipsBackground.setVisibility(View.VISIBLE);
		
		isAnimateCanclled = true;
		mAnimation.cancel();
		mAlphaAmin.cancel();
		anSet.cancel();
		//anSet2.cancel();
		
		if( mQuickMatchLadyList == null || mIndex >= mQuickMatchLadyList.size() ) {
			// 没有女士了
			layoutLady.setVisibility(View.INVISIBLE);
			layoutLadyNext.setVisibility(View.INVISIBLE);
			layoutIndex.setVisibility(View.INVISIBLE);
			layoutTipsBackground.setVisibility(View.INVISIBLE);
			
			isAnimateCanclled = false;
			mAnimation.start();
			mAnimation.start();
			anSet.start();
			//anSet2.start();
			
			no_quota.setVisibility(View.VISIBLE);
			
			return;
		}
		
		imageViewLady.setVisibility(View.VISIBLE);
		
		textViewTipsIndex.setText(String.valueOf(mIndex + 1));

		// 切换数据 
		QuickMatchLady lady = mQuickMatchLadyList.get(mIndex);

		// 当前女士
		textViewName.setText(lady.firstname);
		textViewAge.setText(String.valueOf(lady.age));
		textViewCountry.setText(lady.country);
		if (null != loader) {
			loader.ResetImageView();
		}
		loader = new ImageViewLoader(mContext);
		
		loader.SetDefaultImage(new ColorDrawable(mContext.getResources().getColor(brandColors[new Random().nextInt(brandColors.length - 1)])));
		String localPath = FileCacheManager.getInstance().CacheImagePathFromUrl(lady.image);
		loader.DisplayImage(imageViewLadyHeader, lady.image, localPath, null);
		
		// 下个女士
		QuickMatchLady ladyNext = null;
		if( mIndex + 1 < mQuickMatchLadyList.size() ) {
			ladyNext = mQuickMatchLadyList.get(mIndex + 1);
		}
		if( ladyNext != null ) {
			textViewNameNext.setText(ladyNext.firstname);
			textViewAgeNext.setText(String.valueOf(ladyNext.age));
			textViewCountryNext.setText(ladyNext.country);
			if (null != loaderNext) {
				loaderNext.ResetImageView();
			}
			loaderNext = new ImageViewLoader(mContext);
			loaderNext.SetDefaultImage(new ColorDrawable(mContext.getResources().getColor(brandColors[new Random().nextInt(brandColors.length - 1)])));
			String localPathNext = FileCacheManager.getInstance().CacheImagePathFromUrl(ladyNext.image);
			loaderNext.DisplayImage(imageViewLadyHeaderNext, ladyNext.image, localPathNext, null);
		} else {
			// 没有下个女士
			layoutLadyNext.setVisibility(View.INVISIBLE);
			layoutTipsBackground.setVisibility(View.INVISIBLE);
		}

	}
	
	/**
	 * 初始化界面
	 * @param inflater
	 * @param container
	 */
	@SuppressLint("InflateParams")
	public View InitView(LayoutInflater inflater, ViewGroup container) {
        View view = inflater.inflate(R.layout.fragment_quick_match_matches, null);
        
        // 初始化放大背景
        imageViewManBackground = (ImageView) view.findViewById(R.id.imageViewManBackground);
        //imageViewManBackground2 = (ImageView) view.findViewById(R.id.imageViewManBackground2);
        no_quota = (TextView) view.findViewById(R.id.no_quota);
        
         long duration = 4000;

        anSet = new AnimationSet(false);
       // anSet2 = new AnimationSet(false);
        
        anSet.addAnimation(mAnimation);
        anSet.addAnimation(mAlphaAmin);
        anSet.setDuration(duration);
        
       // anSet2.addAnimation(mAnimation2);
       // anSet2.addAnimation(mAlphaAmin2);
       // anSet2.setDuration(duration);
        
		imageViewManBackground.startAnimation(anSet);
		anSet.setAnimationListener(new AnimationListener(){

			@Override
			public void onAnimationEnd(Animation animation) {
				// TODO Auto-generated method stub
				if (!isAnimateCanclled){
					anSet.reset();
					anSet.start();
				}
				
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onAnimationStart(Animation animation) {
				// TODO Auto-generated method stub
				
			}
			
		});
		
		/*imageViewManBackground2.postDelayed(new Runnable(){

			@Override
			public void run() {
				// TODO Auto-generated method stub
				if(!isAnimateCanclled)imageViewManBackground2.startAnimation(anSet2);
			}
			
		}, 2000);
		
		anSet2.setAnimationListener(new AnimationListener(){

			@Override
			public void onAnimationEnd(Animation animation) {
				// TODO Auto-generated method stub
				if (!isAnimateCanclled){
					anSet2.reset();
					anSet2.start();
				}
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onAnimationStart(Animation animation) {
				// TODO Auto-generated method stub
				
			}
			
		});*/
        
        // 初始化女士序号和女士总量
		layoutIndex = (LinearLayout) view.findViewById(R.id.layoutIndex);
		layoutIndex.setVisibility(View.INVISIBLE);
        textViewTipsIndex = (TextView) view.findViewById(R.id.textViewTipsIndex);
        textViewTipsIndex.setText("0");
        textViewTipsCount = (TextView) view.findViewById(R.id.textViewTipsCount);
        textViewTipsCount.setText("0");
        layoutTipsBackground = (LinearLayout) view.findViewById(R.id.layoutTipsBackground);
        layoutTipsBackground.setVisibility(View.INVISIBLE);
        
		// 初始化like/pass界面
		imageViewLike = (ImageView) view.findViewById(R.id.imageViewLike);
		imageViewPass = (ImageView) view.findViewById(R.id.imageViewPass);
		
		// 初始化当前女士静态界面
		imageViewLadyHeader =  (ImageView) view.findViewById(R.id.imageViewLadyHeader);
		textViewName = (TextView) view.findViewById(R.id.textViewName);
		textViewAge = (TextView) view.findViewById(R.id.textViewAge);
		textViewCountry = (TextView) view.findViewById(R.id.textViewCountry);
		
		// 初始化下个女士静态界面
		imageViewLadyHeaderNext =  (ImageView) view.findViewById(R.id.imageViewLadyHeaderNext);
		textViewNameNext = (TextView) view.findViewById(R.id.textViewNameNext);
		textViewAgeNext = (TextView) view.findViewById(R.id.textViewAgeNext);
		textViewCountryNext = (TextView) view.findViewById(R.id.textViewCountryNext);
		
		// 初始化女士拖动界面 
		imageViewLady = (QuickMatchImageView) view.findViewById(R.id.imageViewLady);
		imageViewLady.setVisibility(View.GONE);
		imageViewLady.SetOnQuickMatchImageViewLinstener(new OnQuickMatchImageViewLinstener() {
			@SuppressWarnings("deprecation")
			@Override
			public void OnDrag() {
				// TODO Auto-generated method stub
				Log.d("QuickMatch.QuickMatchMatchesFragment", "OnDrag()");
				// 复位女士拖动界面 
				imageViewLady.CenterImage();
				
				// 隐藏like和pass标记
				ShowLikeOrPass(false, false);
				
				// 半透女士拖动界面 
		        if( Build.VERSION.SDK_INT >= 16 ) {
		        	imageViewLady.setImageAlpha(255);
		        } else {
		        	imageViewLady.setAlpha(255);
		        }
		        
				// 隐藏当前女士静态界面
				layoutLady.setVisibility(View.INVISIBLE);
			}
			
			@SuppressWarnings("deprecation")
			@Override
			public void OnRestore() {
				Log.d("QuickMatch.QuickMatchMatchesFragment", "OnRestore()");
				// TODO Auto-generated method stub
				// 隐藏like和pass标记
				ShowLikeOrPass(false, false);
				
				// 半透隐藏女士拖动界面 
		        if( Build.VERSION.SDK_INT >= 16 ) {
		        	imageViewLady.setImageAlpha(0);
		        } else {
		        	imageViewLady.setAlpha(0);
		        }
				// 隐藏女士拖动界面
//				imageViewLady.setVisibility(View.INVISIBLE);
				
				// 显示当前女士静态界面
				layoutLady.setVisibility(View.VISIBLE);
			}

			@Override
			public void OnMoveCenter() {
				// TODO Auto-generated method stub
				Log.d("QuickMatch.QuickMatchMatchesFragment", "OnMoveCenter()");
				// 隐藏like和pass标记
				ShowLikeOrPass(false, false);
			}

			@Override
			public void OnMoveRight() {
				// TODO Auto-generated method stub
				Log.d("QuickMatch.QuickMatchMatchesFragment", "OnMoveRight()");
				// 隐藏like显示pass标记
				ShowLikeOrPass(false, true);
			}
			
			@Override
			public void OnMoveLeft() {
				// TODO Auto-generated method stub
				Log.d("QuickMatch.QuickMatchMatchesFragment", "OnMoveLeft()");
				// 显示like和pass标记
				ShowLikeOrPass(true, false);
			}

			@Override
			public void OnReleaseLeft() {
				// TODO Auto-generated method stub
				Log.d("QuickMatch.QuickMatchMatchesFragment", "OnReleaseLeft()");
				// 隐藏like和pass标记
				ShowLikeOrPass(false, false);
				
				// 标记当前女士为like
				QuickMatchManager.getInstance().MarkLady(mIndex, true);
				mIndex++;
				
				// 显示下个女士
				ShowCurrentLady();
			}

			@Override
			public void OnReleaseRight() {
				// TODO Auto-generated method stub
				Log.d("QuickMatch.QuickMatchMatchesFragment", "OnReleaseRight()");
				ShowLikeOrPass(false, false);
				
				// 标记当前女士为pass
				QuickMatchManager.getInstance().MarkLady(mIndex, false);
				mIndex++;
				
				// 显示
				ShowCurrentLady();
			}

			@Override
			public void OnMoveOut() {
				// TODO Auto-generated method stub
				Log.d("QuickMatch.QuickMatchMatchesFragment", "OnMoveOut()");
				if( mQuickMatchLadyList == null || mIndex >= mQuickMatchLadyList.size() ) {
					// 没有女士了
					imageViewLady.setVisibility(View.INVISIBLE);
				}
			}
		});
		
		imageViewLady.setClickable(true);
		imageViewLady.setOnClickListener(this);
		
		// 预计算女士拖动界面 大小
		layoutLady = (View) view.findViewById(R.id.linearLayoutLady);
		ViewTools.PreCalculateViewSize(layoutLady);
		layoutLady.setVisibility(View.INVISIBLE);
		layoutLadyNext = (View) view.findViewById(R.id.linearLayoutLadyNext);
		layoutLadyNext.setVisibility(View.INVISIBLE);
		
		return view;
	}
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		QuickMatchLady lady = mQuickMatchLadyList.get(mIndex);
		LadyDetailActivity.launchLadyDetailActivity(mContext, lady.womanid, true);
	}
	/**
	 * 初始化事件监听
	 */
	public void InitHandler() {
		mHandler = new Handler() {
			@Override
			public void handleMessage(android.os.Message msg) {
				// 收起菊花
				switch ( RequestFlag.values()[msg.what] ) {
				case REQUEST_SUCCESS:{
					// 获取匹配女士列表成功
					mIndex = msg.arg1;
					textViewTipsCount.setText(String.valueOf(mQuickMatchLadyList.size()));
					
					// 刷新界面
					ShowCurrentLady();
				} break;
				case REQUEST_FAIL:{
					// 获取匹配女士列表失败
				}break;
				default:break;
				}
			}
		};
	}
	
	@Override public void onDestroy(){
		super.onDestroy();
		anSet.reset();
		anSet.cancel();
		//anSet2.reset();
		//anSet2.cancel();
		mAnimation.cancel();
		//mAnimation2.cancel();
		
	}
}
