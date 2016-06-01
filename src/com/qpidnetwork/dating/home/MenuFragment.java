package com.qpidnetwork.dating.home;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.qpidnetwork.dating.R;
import com.qpidnetwork.dating.WebViewActivity;
import com.qpidnetwork.dating.admirer.AdmirersListActivity;
import com.qpidnetwork.dating.authorization.LoginManager;
import com.qpidnetwork.dating.authorization.LoginManager.LoginStatus;
import com.qpidnetwork.dating.authorization.LoginManager.OnLoginManagerCallback;
import com.qpidnetwork.dating.authorization.LoginParam;
import com.qpidnetwork.dating.authorization.RegisterActivity;
import com.qpidnetwork.dating.contacts.ContactsListActivity;
import com.qpidnetwork.dating.credit.BuyCreditActivity;
import com.qpidnetwork.dating.emf.EMFListActivity;
import com.qpidnetwork.dating.home.MenuHelper.MenuItemBean;
import com.qpidnetwork.dating.home.MenuHelper.MenuType;
import com.qpidnetwork.dating.lovecall.LoveCallListActivity;
import com.qpidnetwork.dating.profile.MyProfileActivity;
import com.qpidnetwork.dating.quickmatch.QuickMatchActivity;
import com.qpidnetwork.dating.setting.SettingActivity;
import com.qpidnetwork.framework.base.BaseFragment;
import com.qpidnetwork.framework.util.UnitConversion;
import com.qpidnetwork.framework.widget.CircleImageView;
import com.qpidnetwork.manager.FileCacheManager;
import com.qpidnetwork.manager.MonthlyFeeManager;
import com.qpidnetwork.manager.MonthlyFeeManager.OnMemberMonthlyTypeUpdate;
import com.qpidnetwork.manager.WebSiteManager;
import com.qpidnetwork.manager.WebSiteManager.WebSite;
import com.qpidnetwork.manager.WebSiteManager.WebSiteType;
import com.qpidnetwork.request.RequestJniMonthlyFee.MemberType;
import com.qpidnetwork.request.item.LoginErrorItem;
import com.qpidnetwork.request.item.LoginItem;
import com.qpidnetwork.tool.ImageViewLoader;
import com.qpidnetwork.view.ChooseWebSiteDialog;
import com.qpidnetwork.view.ViewTools;

@SuppressLint("InflateParams")
public class MenuFragment extends BaseFragment implements OnLoginManagerCallback , OnMemberMonthlyTypeUpdate{
	
	private static final int ON_MEMBER_TYPE_UPDATE = 1;
	
	private ListView lvMainmenu;
	private MenuItemAdapter menuAdapter;
	public MenuHelper menuHelper;
	
	/*photo and name*/
	private FrameLayout llPhotoName;//宽高16:9
	private LinearLayout layoutPhotoName;
	private CircleImageView ivPhoto;
	private ImageViewLoader loader;
	private TextView tvUsername;
	
	/*footer menu*/
	private LinearLayout llSiteMenu;
	
	/*credit*/
	private RelativeLayout rlCredit;
	public TextView tvCredit;
	
	/*change site*/
	private LinearLayout layoutChangeWebsite;
	private TextView tvCurrentSite;
	private HomeActivity homeActivity;
	
	private ImageButton imageButtonHelp;
	private ImageButton imageButtonSetting;
	private View monthlyNoPaid;
	private View monthlyPaid;
	
	final static class HeaderClickId{
		public final static int HELP = 1001;
		public final static int SETTINGS = 1002;
		public final static int PHOTO_LOGINED = 1003;
		public final static int PHOTO_REGISTER = 1004;
		public final static int CREDIT_BALANCE = 1005;
	}
	

	
	@Override
	public void onAttach(Activity activity) {
		// TODO Auto-generated method stub
		super.onAttach(activity);
		mContext = activity;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_main_menu, null);
		lvMainmenu = (ListView)view.findViewById(R.id.lvMainmenu);
		addHeader(inflater);
		initMainMenu();
		addFooter(inflater);
		
		// 登录成功
		LoginManager.getInstance().AddListenner(this);
		
		//会员月费状态更新
		MonthlyFeeManager.getInstance().AddMemberTypeListener(this);
		
		// 刷新数th
		ReloadData();
		
		return view;
	}
	
	@Override
	public void onDetach() {
		// TODO Auto-generated method stub
		super.onDetach();
		//会员月费状态更新
		MonthlyFeeManager.getInstance().RemoveMemberTypeListener(this);
	}
	
	private void addHeader(LayoutInflater inflater){
		View header = (View)inflater.inflate(R.layout.mainmenu_header, null);

		
		
		imageButtonHelp = (ImageButton) header.findViewById(R.id.imageButtonHelp);
		imageButtonHelp.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				// 点击弹出设置界面
				performMenuOnClick(HeaderClickId.HELP);
			}
		});
		
		imageButtonSetting = (ImageButton) header.findViewById(R.id.imageButtonSetting);
		imageButtonSetting.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				// 点击弹出设置界面
				performMenuOnClick(HeaderClickId.SETTINGS);
			}
		});
		
		
		float header_mminHeight = 164.0f * mContext.getResources().getDisplayMetrics().density;
		llPhotoName = (FrameLayout)header.findViewById(R.id.llPhotoName);
		LinearLayout.LayoutParams params = (LinearLayout.LayoutParams)llPhotoName.getLayoutParams();
		int height = (HomeHelper.getMenuWidth(mContext)*9)/16;
		if (height < header_mminHeight) height = (int)header_mminHeight ;
		params.height = height;
		if (Build.VERSION.SDK_INT >= 21) {
			((LinearLayout.LayoutParams)header.findViewById(R.id.top_button_bar)
					.getLayoutParams()).topMargin = UnitConversion.dip2px(mContext, 18);
			
			params.height +=  UnitConversion.dip2px(mContext, 18);
		}
			
		
		// 头像
		ivPhoto = (CircleImageView)header.findViewById(R.id.ivPhoto);
		tvCurrentSite = (TextView) header.findViewById(R.id.tvCurrentSite);
		layoutChangeWebsite = (LinearLayout) header.findViewById(R.id.layoutChangeWebsite);
		layoutChangeWebsite.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				final ChooseWebSiteDialog dialog = new ChooseWebSiteDialog(mContext); 
				
				final WebSite website = WebSiteManager.getInstance().GetWebSite();
				final WebSite websiteCD = WebSiteManager.getInstance().mWebSiteMap.get(WebSiteType.CharmDate.name());
				final WebSite websiteCL = WebSiteManager.getInstance().mWebSiteMap.get(WebSiteType.ChnLove.name());
				final WebSite websiteIDA = WebSiteManager.getInstance().mWebSiteMap.get(WebSiteType.IDateAsia.name());
				final WebSite websiteLD = WebSiteManager.getInstance().mWebSiteMap.get(WebSiteType.LatamDate.name());
				
				View view = dialog.AddItem(R.drawable.img_cd_selection_40dp, websiteCD.getSiteName(), websiteCD.getSiteDesc(), (website.getSiteId() == websiteCD.getSiteId()? true : false));
				view.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						dialog.dismiss();
						onWebSiteChange(WebSiteType.CharmDate);
					}
				});
				view = dialog.AddItem(R.drawable.img_cl_selection_40dp, websiteCL.getSiteName(), websiteCL.getSiteDesc(), (website.getSiteId() == websiteCL.getSiteId()? true : false));
				view.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						dialog.dismiss();
						onWebSiteChange(WebSiteType.ChnLove);
					}
				});
				view = dialog.AddItem(R.drawable.img_ida_selection_40dp, websiteIDA.getSiteName(), websiteIDA.getSiteDesc(), (website.getSiteId() == websiteIDA.getSiteId()? true : false));
				view.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						dialog.dismiss();
						onWebSiteChange(WebSiteType.IDateAsia);
					}
				});
				view = dialog.AddItem(R.drawable.img_ld_selection_40dp, websiteLD.getSiteName(), websiteLD.getSiteDesc(), (website.getSiteId() == websiteLD.getSiteId()? true : false));
				view.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						dialog.dismiss();
						onWebSiteChange(WebSiteType.LatamDate);
					}
				});
				dialog.show();
				
				// 跟踪dialog行为
				homeActivity.onAnalyticsPageSelected(3);
				dialog.setOnDismissListener(new OnDismissListener() {
					
					@Override
					public void onDismiss(DialogInterface arg0) {
						homeActivity.onAnalyticsPageSelected(0);
					}
				});
			}
		});
		
		layoutPhotoName = (LinearLayout) header.findViewById(R.id.layoutPhotoName);
		layoutPhotoName.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				switch (LoginManager.getInstance().GetLoginStatus()) {
				case NONE: {
					// 处于未登录状态
					performMenuOnClick(HeaderClickId.PHOTO_REGISTER);
				}break;
				case LOGINING:{
					// 处于未登录状态, 点击弹出登录界面
					performMenuOnClick(HeaderClickId.PHOTO_REGISTER);
				}break;
				case LOGINED:{
					// 处于登录状态, 点击弹出个人资料界面
					performMenuOnClick(HeaderClickId.PHOTO_LOGINED);
				}break;
				default:
					break;
				}
			}
		});

		tvUsername = (TextView)header.findViewById(R.id.tvUsername);
		
		rlCredit = (RelativeLayout)header.findViewById(R.id.rlCredit);
		tvCredit =(TextView)header.findViewById(R.id.tvCredit);
		rlCredit.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				performMenuOnClick(HeaderClickId.CREDIT_BALANCE);
			}
		});
		
		monthlyNoPaid = (View)header.findViewById(R.id.monthlyNoPaid);
		monthlyNoPaid.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// 跳去充值模块
				Intent intent = new Intent(mContext,
						BuyCreditActivity.class);
				startActivity(intent);
			}
		});
		monthlyPaid = (View)header.findViewById(R.id.monthlyPaid);
		if(LoginManager.getInstance().GetLoginStatus() == LoginStatus.LOGINED){
			MemberType type = MonthlyFeeManager.getInstance().getMemberType();
			switch (type) {
			case NORMAL_MEMBER:{
				monthlyNoPaid.setVisibility(View.GONE);
				monthlyPaid.setVisibility(View.GONE);	
			}break;
			case FEED_MONTHLY_MEMBER:{
				monthlyNoPaid.setVisibility(View.GONE);
				monthlyPaid.setVisibility(View.VISIBLE);	
			}break;
			case NO_FEED_FIRST_MONTHLY_MEMBER:
			case NO_FEED_MONTHLY_MEMBER:{
				monthlyNoPaid.setVisibility(View.VISIBLE);
				monthlyPaid.setVisibility(View.GONE);	
			}break;

			default:
				break;
			}
		}
		
		lvMainmenu.addHeaderView(header);
		
	}
	
	private void addFooter(LayoutInflater inflater){
		View footer = (View)inflater.inflate(R.layout.mainmenu_footer, null);
		llSiteMenu = (LinearLayout)footer.findViewById(R.id.llSiteMenu);
		final WebSite website = WebSiteManager.getInstance().GetWebSite();
		final WebSite websiteCD = WebSiteManager.getInstance().mWebSiteMap.get(WebSiteType.CharmDate.name());
		final WebSite websiteCL = WebSiteManager.getInstance().mWebSiteMap.get(WebSiteType.ChnLove.name());
		final WebSite websiteIDA = WebSiteManager.getInstance().mWebSiteMap.get(WebSiteType.IDateAsia.name());
		final WebSite websiteLD = WebSiteManager.getInstance().mWebSiteMap.get(WebSiteType.LatamDate.name());
		View cdView = createSiteItem(R.drawable.img_cd_selection_40dp, websiteCD.getSiteName(), websiteCD.getSiteDesc());
		cdView.setId(R.id.site_charmdating);
		cdView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if(llSiteMenu != null){
					llSiteMenu.findViewById(R.id.site_charmdating).setVisibility(View.GONE);
					llSiteMenu.findViewById(R.id.site_chnlove).setVisibility(View.VISIBLE);
					llSiteMenu.findViewById(R.id.site_idateasia).setVisibility(View.VISIBLE);
					llSiteMenu.findViewById(R.id.site_latamdate).setVisibility(View.VISIBLE);
				}
				onWebSiteChange(WebSiteType.CharmDate);
			}
		});
		View clView = createSiteItem(R.drawable.img_cl_selection_40dp, websiteCL.getSiteName(), websiteCL.getSiteDesc());
		clView.setId(R.id.site_chnlove);
		clView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if(llSiteMenu != null){
					llSiteMenu.findViewById(R.id.site_charmdating).setVisibility(View.VISIBLE);
					llSiteMenu.findViewById(R.id.site_chnlove).setVisibility(View.GONE);
					llSiteMenu.findViewById(R.id.site_idateasia).setVisibility(View.VISIBLE);
					llSiteMenu.findViewById(R.id.site_latamdate).setVisibility(View.VISIBLE);
				}
				onWebSiteChange(WebSiteType.ChnLove);
			}
		});
		View idaView = createSiteItem(R.drawable.img_ida_selection_40dp, websiteIDA.getSiteName(), websiteIDA.getSiteDesc());
		idaView.setId(R.id.site_idateasia);
		idaView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if(llSiteMenu != null){
					llSiteMenu.findViewById(R.id.site_charmdating).setVisibility(View.VISIBLE);
					llSiteMenu.findViewById(R.id.site_chnlove).setVisibility(View.VISIBLE);
					llSiteMenu.findViewById(R.id.site_idateasia).setVisibility(View.GONE);
					llSiteMenu.findViewById(R.id.site_latamdate).setVisibility(View.VISIBLE);
				}
				onWebSiteChange(WebSiteType.IDateAsia);
			}
		});
		View ldView = createSiteItem(R.drawable.img_ld_selection_40dp, websiteLD.getSiteName(), websiteLD.getSiteDesc());
		ldView.setId(R.id.site_latamdate);
		ldView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if(llSiteMenu != null){
					llSiteMenu.findViewById(R.id.site_charmdating).setVisibility(View.VISIBLE);
					llSiteMenu.findViewById(R.id.site_chnlove).setVisibility(View.VISIBLE);
					llSiteMenu.findViewById(R.id.site_idateasia).setVisibility(View.VISIBLE);
					llSiteMenu.findViewById(R.id.site_latamdate).setVisibility(View.GONE);
				}
				onWebSiteChange(WebSiteType.LatamDate);
			}
		});
		
		if(website.getSiteId() == websiteCD.getSiteId()){
			cdView.setVisibility(View.GONE);
		}else if(website.getSiteId() == websiteCL.getSiteId()){
			clView.setVisibility(View.GONE);
		}else if(website.getSiteId() == websiteIDA.getSiteId()){
			idaView.setVisibility(View.GONE);
		}else if(website.getSiteId() == websiteLD.getSiteId()){
			ldView.setVisibility(View.GONE);
		}
		
		llSiteMenu.addView(cdView);
		llSiteMenu.addView(clView);
		llSiteMenu.addView(idaView);
		llSiteMenu.addView(ldView);
		
		lvMainmenu.addFooterView(footer);
	}
	
	private View createSiteItem(int imageId, String tips, String desc) {
    	LayoutInflater layoutInfalter = LayoutInflater.from(getActivity());
    	View view = layoutInfalter.inflate(R.layout.layout_website_item, null);
    	
    	ImageView imageView = (ImageView) view.findViewById(R.id.imageView);
    	imageView.setImageResource(imageId);
    	TextView textViewName = (TextView) view.findViewById(R.id.textViewName);
    	textViewName.setText(tips);
    	TextView textViewDesc = (TextView) view.findViewById(R.id.textViewDesc);
    	textViewDesc.setText(desc);
    	ImageView location = (ImageView) view.findViewById(R.id.select_indicator);
    	location.setVisibility(View.GONE);
    	return view;
    }
	
	private void onWebSiteChange(WebSiteType type){
		WebSite website = WebSiteManager.getInstance().GetWebSite();
		WebSite site = WebSiteManager.getInstance().mWebSiteMap.get(type.name());
		
		WebSiteManager.getInstance().ChangeWebSite(type);
		ReloadData();
		LoginManager.getInstance().Logout();
		LoginManager.getInstance().AutoLogin();
		
		// 统计event
		homeActivity.onAnalyticsEvent(
			getString(R.string.ChooseSite_Category)
			, website.getSiteShortName() + getString(R.string.ChooseSite_Action) + site.getSiteShortName()
			, website.getSiteShortName() + getString(R.string.ChooseSite_Label) + site.getSiteShortName());
	
	}
	
	private void initMainMenu(){
		homeActivity = (HomeActivity)mContext;
		
		menuHelper = new MenuHelper(mContext);
		menuHelper.addMenuItem(MenuType.MENU_QUICK_MATCH, R.drawable.ic_favorite_white_24dp, getString(R.string.menu_quickmatch), 0);
		menuHelper.addMenuItem(MenuType.MENU_MAIL_BOX, R.drawable.ic_email_white_24dp, getString(R.string.menu_mailbox), 0);
		menuHelper.addMenuItem(MenuType.MENU_MY_CONTACTS, R.drawable.ic_female_contact_white_24dp, getString(R.string.menu_contacts), 0);
		menuHelper.addMenuItem(MenuType.MENU_MY_ADMIRERS, R.drawable.ic_female_symble_white_24dp, getString(R.string.menu_admirers), 0);
		menuHelper.addMenuItem(MenuType.MENU_LOVE_CALLS, R.drawable.ic_love_call_white_24dp, getString(R.string.menu_lovecalls), 0);
		//menuHelper.addMenuItem(MenuType.MENU_SETTINGS, R.drawable.menu_icon_settings, getString(R.string.menu_setting), 0);
		//menuHelper.addMenuItem(MenuType.MENU_HELPS, R.drawable.menu_icon_helps, getString(R.string.menu_helps), 0);
		menuAdapter = menuHelper.create();
		lvMainmenu.setAdapter(menuAdapter);
		lvMainmenu.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO Auto-generated method stub
				performMenuOnClick(position);

			}
		});
		

	}
	
	
	private void performMenuOnClick(int p){
		if (homeActivity.mDrawerLayout != null ){
			homeActivity.mDrawerLayout.closeDrawers();
		}
		homeActivity.mDrawerLayout.postDelayed(execDrawerClick(p), 200);
	}
	
	private Runnable execDrawerClick(final int p){
		
		Runnable runnable = new Runnable(){

			@Override
			public void run() {
				// TODO Auto-generated method stub
				Intent intent = new Intent();
				
				switch (p) {
				
				case 1:
					intent.setClass(mContext, QuickMatchActivity.class);
					startActivity(intent);
					break;
				case 2:
					if(!inNeedLogined()){
						intent.setClass(mContext, EMFListActivity.class);
						startActivity(intent);
					}
					break;
				case 5:
					if(!inNeedLogined()){
						MenuItemBean bean = menuHelper.getMenuItemByType(MenuType.MENU_LOVE_CALLS);
						intent.setClass(mContext, LoveCallListActivity.class);
						if(bean.unreadCount > 0){
							/*直接跳到request列表*/
							intent.putExtra(LoveCallListActivity.LOVECALL_NEW_REQUEST, true);
						}
						startActivity(intent);
					}
					break;
				case 4:
					if(!inNeedLogined()){
						intent.setClass(mContext, AdmirersListActivity.class);
						startActivity(intent);
					}
					break;
				case 3:
					if(!inNeedLogined()){
						intent.setClass(mContext, ContactsListActivity.class);
						startActivity(intent);
					}
					break;
				case 6:
					intent.setClass(mContext, RegisterActivity.class);
					startActivity(intent);
					break;
				case 7:
					break;
				case HeaderClickId.PHOTO_REGISTER:
					intent.setClass(mContext, RegisterActivity.class);
					startActivity(intent);
					break;
				case HeaderClickId.PHOTO_LOGINED:
					intent.setClass(mContext, MyProfileActivity.class);
					startActivity(intent);
					break;
				case HeaderClickId.HELP:
					String url = WebSiteManager.getInstance().GetWebSite().getHelpLink();
					intent = WebViewActivity.getIntent(mContext, url);
					intent.putExtra(WebViewActivity.WEB_TITLE, "Help");
					startActivity(intent);
					break;
				case HeaderClickId.SETTINGS:
					intent.setClass(mContext, SettingActivity.class);
					startActivity(intent);
					break;
				case HeaderClickId.CREDIT_BALANCE:
					if( LoginManager.getInstance().CheckLogin(mContext) ) {
						//Intent intent = new Intent(mContext, BuyCreditActivity.class);
						intent.setClass(mContext, BuyCreditActivity.class);
						startActivity(intent);
					}
					break;
				default:
					break;
				}
				Log.d("MenuFragment", "intent : " + intent.toString());
				Log.d("MenuFragment", "toUri : " + intent.toUri(0));
			}
			
		};
		
		return runnable;
	}

	@Override
	public void OnLogin(boolean isSuccess, String errno, String errmsg,
			LoginItem item, LoginErrorItem errItem) {
		// TODO Auto-generated method stub
		Message msg = Message.obtain();
		if( isSuccess ) {
			// 登录成功
			sendUiMessage(msg);
		}
	}
	
	@Override
	protected void handleUiMessage(Message msg) {
		super.handleUiMessage(msg);
		if(msg.what == ON_MEMBER_TYPE_UPDATE){
			MemberType type = MemberType.values()[msg.arg1];
			if(LoginManager.getInstance().GetLoginStatus() == LoginStatus.LOGINED){
				switch (type) {
				case NORMAL_MEMBER:{
					monthlyNoPaid.setVisibility(View.GONE);
					monthlyPaid.setVisibility(View.GONE);	
				}break;
				case FEED_MONTHLY_MEMBER:{
					monthlyNoPaid.setVisibility(View.GONE);
					monthlyPaid.setVisibility(View.VISIBLE);	
				}break;
				case NO_FEED_FIRST_MONTHLY_MEMBER:
				case NO_FEED_MONTHLY_MEMBER:{
					monthlyNoPaid.setVisibility(View.VISIBLE);
					monthlyPaid.setVisibility(View.GONE);	
				}break;

				default:
					break;
				}
			}
			return;
		}
		ReloadData();
	}
	
	/**
	 * 刷新界面
	 */
	@SuppressWarnings("deprecation")
	public void ReloadData() {
		switch (LoginManager.getInstance().GetLoginStatus()) {
		case NONE: {
			// 处于未登录状态
			tvUsername.setText("Login");
			ivPhoto.setImageResource(R.drawable.default_photo_64dp);
			monthlyNoPaid.setVisibility(View.GONE);
			monthlyPaid.setVisibility(View.GONE);
			tvCredit.setText("0");
		}break;
		case LOGINED:{
			// 处于登录状态
			LoginParam param = LoginManager.getInstance().GetLoginParam();

			if( param != null && param.item != null ) {
				tvUsername.setText(param.item.firstname + " " + param.item.lastname);
				String url = param.item.photoURL;
				String localPath = FileCacheManager.getInstance().CacheImagePathFromUrl(url);
				if( loader != null ) {
					loader.ResetImageView();
				}
				loader = new ImageViewLoader(mContext);
				ViewTools.PreCalculateViewSize(ivPhoto);
				loader.SetDefaultImage(mContext.getResources().getDrawable(R.drawable.default_photo_64dp));
				loader.DisplayImage(
						ivPhoto, 
						url, 
						localPath,
						ivPhoto.getWidth(),
						ivPhoto.getHeight(),
						null
						);
			} else {
				tvUsername.setText("");
				ivPhoto.setImageResource(R.drawable.default_photo_64dp);
			}
			
			tvCredit.setText("0");
		}break;
		default:
			break;
		}
		tvCurrentSite.setText(WebSiteManager.getInstance().GetWebSite().getSiteName());
	}
	
	private boolean inNeedLogined(){
		if(LoginManager.getInstance().GetLoginStatus() != LoginStatus.LOGINED){
			Intent intent = new Intent();
			intent.setClass(mContext, RegisterActivity.class);
			startActivity(intent);
			return true;
		}
		return false;
	}

	@Override
	public void OnLogout(boolean bActive) {
		// TODO Auto-generated method stub
		ReloadData();
	}

	@Override
	public void onMemberTypeUpdate(MemberType memberType) {
		Message msg = Message.obtain();
		msg.what = ON_MEMBER_TYPE_UPDATE;
		msg.arg1 = memberType.ordinal();
		sendUiMessage(msg);
	}
}
