package com.qpidnetwork.dating;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AlphaAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.qpidnetwork.dating.advertisement.AdvertisementManager;
import com.qpidnetwork.dating.authorization.LoginManager.OnLoginManagerCallback;
import com.qpidnetwork.dating.authorization.LoginPerfence;
import com.qpidnetwork.dating.home.AppUrlHandler;
import com.qpidnetwork.dating.home.HomeActivity;
import com.qpidnetwork.framework.base.BaseFragmentActivity;
import com.qpidnetwork.framework.util.Log;
import com.qpidnetwork.framework.util.StringUtil;
import com.qpidnetwork.manager.ConfigManager;
import com.qpidnetwork.manager.ConfigManager.OnConfigManagerCallback;
import com.qpidnetwork.manager.WebSiteManager;
import com.qpidnetwork.request.item.LoginErrorItem;
import com.qpidnetwork.request.item.LoginItem;
import com.qpidnetwork.request.item.OtherSynConfigItem;
import com.qpidnetwork.view.MovingImageView;
import com.qpidnetwork.view.MovingImageView.Callback;
import com.qpidnetwork.view.MovingImageView.TranslateMode;


/**
 * @author Logical code: Max, animation design: Martin
 * 
 * if the app is first time started, play animation then proceed to next activity.
 * else 
 * 	if has chosen a site, do login then proceed to next activity
 * 	else
 * 	proceed to next activity
 */

public class DefaultActivity extends BaseFragmentActivity implements OnLoginManagerCallback {
	
	
	public ImageView backImage;
	public MovingImageView imageView;
	public ProgressBar progressBar;
	public TextView text;
	public LinearLayout skip;
	WebSiteManager siteManager = WebSiteManager.getInstance();
	
	//外部链接参数启动
	private String launchModule = "";

	
	private int[]  imgesResourceIds = new int[]{
			R.drawable.img_cool_360dp_width_1,
			R.drawable.img_cool_360dp_width_2,
			R.drawable.img_cool_360dp_width_3,
			R.drawable.long_gallery};
	
	private Images images = new Images(imgesResourceIds);
	private Texts texts = new Texts(R.array.cool_statements);
	private boolean loginCalled = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		// 同步配置
		ConfigManager cm = ConfigManager.getInstance();
		cm.GetOtherSynConfigItem(new OnConfigManagerCallback() {
			
			@Override
			public void OnGetOtherSynConfigItem(boolean isSuccess, String errno,
					String errmsg, OtherSynConfigItem item) {
			}
		});
		
		initLaunchData();
		
		/*浮窗广告，读取本次广告数据，并更新最新广告*/
		AdvertisementManager.getInstance().resetMainAdvertItem();

//		LoginManager.getInstance().AddListenner(this);
		
		if (LoginPerfence.GetStringPreference(this, "startup").length() < 1){
			LoginPerfence.SaveStringPreference(this, "startup", "true");
			animate();

		}else{
			
			proceedToNextActivity();	
			/*if (siteManager.GetWebSite() != null){
				proceedToNextActivity();		
				animate();
						LoginManager.getInstance().AutoLogin();
					}else{
						proceedToNextActivity();
			}*/
		}
		
	}
	
	/**
	 * 初始化外部初始化参数
	 */
	private void initLaunchData(){
		Intent i_getvalue = getIntent();  
		String action = i_getvalue.getAction();  
		if(Intent.ACTION_VIEW.equals(action)){  
		    Uri uri = i_getvalue.getData();  
		    if(uri != null){  
//		    	launchModule = uri.getQueryParameter("module");
		    	
		    	// 增加GA跟踪代码
		    	String moduleName = uri.getQueryParameter("module");
		    	String ladyID = uri.getQueryParameter("ladyid");
		    	String source = uri.getQueryParameter("source");
		    	Log.i("hunter", "launchModule: " + launchModule + " source: " + source + " ladyId:" + ladyID);
		    	launchModule = AppUrlHandler.CreateLinkUrl(moduleName, ladyID, source);

		    	String gaAction = !StringUtil.isEmpty(source) ? source : "unknow";
		    	String gaLabel = !StringUtil.isEmpty(moduleName) ? moduleName : "index";
		    	String gaCategory = getString(R.string.OpenApp_Category);
		    	onAnalyticsEvent(gaCategory, gaAction, gaAction + getString(R.string.OpenApp_Label) + gaLabel);
		    }  
		}
	}
	
	@Override public void finish(){
		super.finish();
		overridePendingTransition(R.anim.anim_alpha_in_za, R.anim.anim_scale_out_az);  

	}
	
	@Override public void onDestroy(){
		imageView.stopAnimate();
		imageView.setKeepScreenOn(false);
//		LoginManager.getInstance().RemoveListenner(this);
		super.onDestroy();
	}

	@Override public void onPause(){
		imageView.stopAnimate();
		super.onPause();
	}
	
	@Override public void onResume(){
		imageView.runAnimate(imageView.mode);
		super.onResume();
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			proceedToNextActivity();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}
	
	@Override
	public void InitView() {
		setContentView(R.layout.activity_default);
		
		backImage = (ImageView)findViewById(R.id.back_image);
		imageView = (MovingImageView) findViewById(R.id.imageView);
		progressBar = (ProgressBar) findViewById(R.id.progressBar);
		text = (TextView)findViewById(R.id.text);
		skip = (LinearLayout)findViewById(R.id.skip);

		
		text.setText(texts.getNext());
		progressBar.setVisibility(View.GONE);
		//skip.setVisibility(View.GONE);
		
		final AlphaAnimation alAnimZA = new AlphaAnimation(1.0f, 0.0f);
		final AlphaAnimation alAnimAZ = new AlphaAnimation(0.0f, 1.0f);
		
		alAnimZA.setDuration(800);
		alAnimZA.setFillEnabled(true);
		alAnimZA.setFillAfter(true);
		alAnimAZ.setDuration(800);
		
		imageView.setCallback(new Callback(){

			@Override
			public void onAnimationStopped() {
				// TODO Auto-generated method stub
				
				//Log.v("next position", images.getNextPosition() + "");
				
				if (skip.getVisibility() == View.GONE){
					//skip.startAnimation(alAnimAZ);
					skip.setVisibility(View.VISIBLE);
				}
				
				//Don't repeat animation
				if (images.getNextPosition() == images.getSize()){
					proceedToNextActivity();
					return;
				}
				
				//Last image use YTRANSLATE mode
				if (images.getNextPosition() == images.getSize() - 1){
					imageView.setMode(TranslateMode.YTRANSLATE);
				}else{
					imageView.setMode(TranslateMode.XTRANSLATE);
				}
				
				
				
				int nextImage = images.getNext();
				backImage.setImageResource(nextImage);
				imageView.setNextPhoto(nextImage);
				text.startAnimation(alAnimZA);
			}
			
			@Override
			public void onBeforeSetNext() {
				// TODO Auto-generated method stub
				

				
				text.setText(texts.getNext());
				text.startAnimation(alAnimAZ);
			}
			
			@Override
			public void onNextPhotoSet() {
				// TODO Auto-generated method stub
				imageView.runAnimate(imageView.mode, 0);
			}


			
		});
		
		
		skip.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				proceedToNextActivity();
			}
			
		});

		//runAnimate();
		
	}
	
	private void animate(){
		imageView.setImageResource(images.getNext());
		imageView.setFrequency(10);
		imageView.setDuration(6000);
		imageView.runAnimate(TranslateMode.XTRANSLATE);
		imageView.setKeepScreenOn(true);
	}
	
	
	private void proceedToNextActivity(){
		
		text.setVisibility(View.GONE);
		skip.setVisibility(View.GONE);
		
		if( siteManager.IsDefaultWebSite() ) {
			Intent intent = new Intent(mContext, ChooseSiteActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
			startActivity(intent);
		} else {
			Intent intent = new Intent(mContext, HomeActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
			if(!TextUtils.isEmpty(launchModule)){
				intent.putExtra(HomeActivity.START_BROWSER_LINK, launchModule);
			}
			startActivity(intent);
		}
		
		finish();
	}
	
	class Images{
		
		public int next = 0;
		public int[] images;
		
		public Images(int[] imageResourceIds){
			images = imageResourceIds;
		}
		
		public void moveToFirst(){
			next = 0;
		}
		
		public int getNext(){
			if (next > images.length - 1) next = 0;//throw  new Exception("No more images, please use moveToFirst().");
			int image =  images[next];
			next++;
			return image;
		}
		
		public int getNextPosition(){
			return next;
		}
		
		public int getSize(){
			return images.length;
		}
		
	}
	
	class Texts{
		
		public int next = 0;
		public String[] texts;
		
		public Texts(int textResourceId){
			texts = QpidApplication.getContext().getResources().getStringArray(textResourceId);
		}
		
		public void moveToFirst(){
			next = 0;
		}

		public int getNextPosition(){
			return next;
		}
		
		public String getNext(){
			if (next > texts.length - 1) next = 0;//throw  new Exception("No more images, please use moveToFirst().");
			String text =  texts[next];
			next++;
			return text;
		}
		
		public int getSize(){
			return texts.length;
		}
		
	}
	
	@Override
	public void OnLogin(boolean isSuccess, String errno, String errmsg,
			LoginItem item, LoginErrorItem errItem) {
		// TODO Auto-generated method stub
		//Log.v("login", "login callback");
		if (loginCalled) return; //Sometimes it calls back more then once.
		loginCalled = true;
		proceedToNextActivity();
	}

	@Override
	public void OnLogout(boolean bActive) {
		// TODO Auto-generated method stub
		
	}
}
