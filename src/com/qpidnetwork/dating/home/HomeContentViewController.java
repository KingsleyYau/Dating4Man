package com.qpidnetwork.dating.home;

import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.SwipeRefreshLayout;
import android.telephony.TelephonyManager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.TextView;

import com.qpidnetwork.dating.R;
import com.qpidnetwork.dating.advertisement.AdWomanListAdvertItem;
import com.qpidnetwork.dating.advertisement.AdvertPerfence;
import com.qpidnetwork.dating.authorization.LoginManager;
import com.qpidnetwork.dating.authorization.LoginPerfence;
import com.qpidnetwork.dating.contacts.ContactManager;
import com.qpidnetwork.dating.home.LadyListAdapter.ChatButtonType;
import com.qpidnetwork.dating.home.LadyListAdapter.OnLadyListAdapterCallback;
import com.qpidnetwork.dating.lady.LadyDetailActivity;
import com.qpidnetwork.dating.lady.LadyDetailManager;
import com.qpidnetwork.dating.lady.LadyListItem;
import com.qpidnetwork.dating.lady.LadyListManager;
import com.qpidnetwork.dating.lovecall.DirectCallManager;
import com.qpidnetwork.dating.lovecall.ScheduleCallActivity;
import com.qpidnetwork.framework.base.BaseFragmentActivity;
import com.qpidnetwork.framework.util.Log;
import com.qpidnetwork.framework.util.SystemUtil;
import com.qpidnetwork.framework.widget.pinterest.MultiColumnListView;
import com.qpidnetwork.framework.widget.pinterest.internal.PLA_AbsListView;
import com.qpidnetwork.framework.widget.pinterest.internal.PLA_AbsListView.OnScrollListener;
import com.qpidnetwork.livechat.LCMessageItem;
import com.qpidnetwork.livechat.LCUserItem.ChatType;
import com.qpidnetwork.livechat.LiveChatManager;
import com.qpidnetwork.manager.WebSiteManager;
import com.qpidnetwork.manager.WebSiteManager.WebSite;
import com.qpidnetwork.request.OnAdWomanListAdvertCallback;
import com.qpidnetwork.request.OnQueryLadyCallCallback;
import com.qpidnetwork.request.OnQueryLadyListCallback;
import com.qpidnetwork.request.OnRequestCallback;
import com.qpidnetwork.request.RequestJni;
import com.qpidnetwork.request.RequestJniAdvert;
import com.qpidnetwork.request.RequestJniLady.OnlineType;
import com.qpidnetwork.request.RequestJniLady.OrderType;
import com.qpidnetwork.request.RequestJniLady.SearchType;
import com.qpidnetwork.request.RequestOperator;
import com.qpidnetwork.request.item.AdWomanListAdvert;
import com.qpidnetwork.request.item.Lady;
import com.qpidnetwork.request.item.LadyCall;
import com.qpidnetwork.request.item.LadyDetail;
import com.qpidnetwork.view.GetMoreCreditDialog;
import com.qpidnetwork.view.HomeLadySearchWindow;
import com.qpidnetwork.view.MaterialAppBar;
import com.qpidnetwork.view.MaterialDialogAlert;
import com.qpidnetwork.view.MaterialDropDownList;
import com.qpidnetwork.view.MaterialProgressBar;
import com.qpidnetwork.view.MaterialThreeButtonDialog;
import com.qpidnetwork.view.MultiSwipeRefreshLayout;

@SuppressLint("RtlHardcoded")
public class HomeContentViewController implements View.OnClickListener {
	
	public interface HomeContentViewControllerCallback {
		void OnClickOpenDrawer(View v);
		void OnClickSearch(boolean bShowSearch);
		void OnClickContact(View v);
		void OnListSelected(int index);
		void OnRequest(String tips);
		void OnRequestFinish(boolean bSuccess, String tips);
	}
	
	private Context mContext;
	private Handler mHandler = null;
	private Handler mLiveChatHandler = null;
	private HomeContentViewControllerCallback mCallback = null;
	private MaterialDropDownList dropList;
	private HomeLadySearchWindow ladySearcher;
	private MaterialProgressBar progressbar;
	
	/*waterfall*/
	private View mView = null;
	private MaterialAppBar appbar;
	private MultiSwipeRefreshLayout refreshView;
	private MultiColumnListView mGridView;
	private LadyListAdapter mAdapter;
	private List<LadyListItem> mLadyList;
	private TextView noDataView;
	
	/*女士列表广告*/
	private AdWomanListAdvertItem mAdWomanListAdvertItem;
	
	/**
	 * 搜索界面
	 */
	private final int mDefaultItem = 0;
	public String mTitle = "Online Ladies";
	public String mNoDataMessage = "";
	public SearchType mSearchType = SearchType.DEFAULT;
	public int mAge1;
	public int mAge2;
	public OnlineType mOnlineType = OnlineType.ONLINE;
	public String mWomanId;
	public OrderType mOrderType = OrderType.DEFAULT;
	
	public boolean mbLoadingMore = false;
	private int mOnlineLadyPageIndex = 1;
	
	/**
	 * 请求消息
	 */
	private enum RequestFlag {
		REQUEST_LADY_LIST_SUCCESS,
		REQUEST_LADY_LIST_FAIL,
		REQUEST_LADY_LIST_ADVERT_SUCCESS,
		REQUEST_ADD_FAVOUR_SUCCESS,
		REQUEST_ADD_FAVOUR_FAIL,
		REQUEST_GET_LOVE_CALL_SUCCESS,
		REQUEST_GET_LOVE_CALL_FAIL,
		LIVECHAT_CONTACT_MESSAGE,
	}
	
	private enum LiveChatFlag {
		LIVECHAT_RECV_MESSAGE,
	}
	
	/**
	 * 界面消息
	 */
	private class MessageCallbackItem {
		/**
		 * 
		 * @param errno				接口错误码
		 * @param errmsg			错误提示
		 */
		public MessageCallbackItem(
				String errno,
				String errmsg
				) {
			this.errno = errno;
			this.errmsg = errmsg;
			this.bLoadMore = false;
		}
		public String errno;
		public String errmsg;
		public Lady[] ladyList;
		public boolean bLoadMore;
		public LadyCall ladyCall;
	}
	
	private LadyListItem mCallingLadyItem = null;//记录当前make call 对象的信息，用于拨打成功添加到联系人使用
	
	public HomeContentViewController(Context context){
		mContext = context;
		
		LadyListManager.init(mContext);
		
		mLadyList = new LinkedList<LadyListItem>();
		mAdapter = new LadyListAdapter(mContext, mLadyList);
		mAdapter.SetOnLadyListAdapterCallback(new OnLadyListAdapterCallback() {
			
			@Override
			public void OnClickAddFavour(LadyListItem item) {
				// TODO Auto-generated method stub
				AddFavour(item.lady.womanid);
			}

			@Override
			public void OnClickCallLady(LadyListItem item) {
				// TODO Auto-generated method stub
				mCallingLadyItem = item;
				QueryLadyCall(item.lady);
			}

			@Override
			public void OnClickProfileDetail(LadyListItem item) {
				// TODO Auto-generated method stub
				LadyDetailActivity.launchLadyDetailActivity(mContext, item.lady.womanid, true);
			}
		});
		
		// 初始化事件
		InitHandler();
		
		// 初始化界面变量
		mSearchType = SearchType.DEFAULT;
    	mAge1 = -1;
    	mAge2 = -1;
    	mOnlineType = OnlineType.ONLINE;
    	mWomanId = "";
    	
		// 读取女士列表广告
    	mAdWomanListAdvertItem = AdvertPerfence.GetAdWomanListAdvertItem(mContext);
    	if (null == mAdWomanListAdvertItem) {
    		mAdWomanListAdvertItem = new AdWomanListAdvertItem();
    	}
	}
	
	public void SetCallback(HomeContentViewControllerCallback callback) {
		mCallback = callback;
	}
	
	public View getView() {
		if( mView == null ) {
			mView = CreateView();
		} 
		
		return mView;
	}
	
	@SuppressWarnings("deprecation")
	@SuppressLint("InflateParams")
	private View CreateView() {
		View view = LayoutInflater.from(mContext).inflate(R.layout.fragment_home_ladylist, null);
		refreshView = (MultiSwipeRefreshLayout)view.findViewById(R.id.swipeRefreshLayout);
		noDataView = (TextView)view.findViewById(R.id.no_data);
		progressbar = (MaterialProgressBar)view.findViewById(R.id.progress_bar);
		refreshView.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {  
		    @Override
		    public void onRefresh() {
		    	// 获取在线女士列表
		    	QueryOnlineLadyList(false);
		    }
		});

		final String[] dropDownListItem = new String[]{
			mContext.getString(R.string.lady_category_online),
			mContext.getString(R.string.lady_category_with_call),
			mContext.getString(R.string.lady_category_with_video),
			mContext.getString(R.string.lady_category_newest)
		};
		
		/*appbar 使用參考連接: https://github.com/DanielShum/BadgeAppbar*/
		Drawable dropDownIndicator = mContext.getResources().getDrawable(R.drawable.ic_expand_more_white_24dp);
		appbar = (MaterialAppBar)view.findViewById(R.id.appbar);
		appbar.setAppbarBackgroundColor(mContext.getResources().getColor(WebSiteManager.getInstance().GetWebSite().getSiteColor()));
		appbar.addButtonToLeft(R.id.common_button_mainmenu, "", R.drawable.ic_menu_white_24dp);
		appbar.addButtonToRight(R.id.common_button_search, "", R.drawable.ic_search_white_24dp);
		appbar.addButtonToRight(R.id.common_button_contacts, "", R.drawable.ic_menu_contact_list_24dp);
		appbar.setTitle(mContext.getString(R.string.lady_category_online), Color.WHITE, dropDownIndicator, true);
		appbar.setOnButtonClickListener(this);
		appbar.setOnTitleClick(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (dropList != null){
					dropList.showAsDropDown(v);
					return;
				}
			}
		});
		
		appbar.getButtonById(R.id.common_button_search).setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (ladySearcher != null){
					ladySearcher.showAsDropDown(v);
					return;
				}
			}
			
		});
		
		dropList = new MaterialDropDownList(mContext, dropDownListItem);
		dropList.setCallback(new MaterialDropDownList.Callback() {
			
			@Override
			public void onClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				// TODO Auto-generated method stub
				resetSearchCriteria();
				SelectProc(arg2);
			}
		});
		
		ladySearcher = new HomeLadySearchWindow(mContext);
		ladySearcher.setCallback(new HomeLadySearchWindow.Callback() {
			@Override
			public void OnClickGo(View v, String ladyId) {
				// TODO Auto-generated method stub
				appbar.getTitileView().setText(mContext.getResources().getString(R.string.common_btn_search));
				dropList.setSelectedItem(-1);
				resetSearchCriteria();
				mSearchType = SearchType.BYID;
		    	mAge1 = -1;
		    	mAge2 = -1;
		    	mOnlineType = OnlineType.DEFAULT;
		    	mWomanId = ladyId;
		    	mOrderType = OrderType.DEFAULT;
				SelectProc(-1);
			}

			@Override
			public void OnClickSearch(View v, int minAge, int maxAge,
					boolean isOnline) {
				// TODO Auto-generated method stub
				appbar.getTitileView().setText(mContext.getResources().getString(R.string.common_btn_search));
				dropList.setSelectedItem(-1);
				resetSearchCriteria();
				mSearchType = SearchType.DEFAULT;
		    	mAge1 = minAge;
		    	mAge2 = maxAge;
		    	mOnlineType = (isOnline)?OnlineType.ONLINE:OnlineType.DEFAULT;
		    	Log.v("is online", isOnline + "");
		    	mWomanId = "";
		    	mOrderType = OrderType.DEFAULT;
				SelectProc(-1);
			}
		});
		
		mGridView = (MultiColumnListView)view.findViewById(R.id.ladylist);
		mGridView.setSmoothScrollbarEnabled(true);
		mGridView.setAdapter(mAdapter);
		mGridView.setOnScrollListener(new OnScrollListener(){

			@Override
			public void onScrollStateChanged(PLA_AbsListView view,
					int scrollState) {
				// TODO Auto-generated method stub
                switch (scrollState) {  
                case SCROLL_STATE_IDLE: {
                	// 下拉刷新
    				
					if ( view.getLastVisiblePosition() == mLadyList.size() - 1 ) {  
                		// 上拉更多
                		if( !mbLoadingMore ) {
                			QueryOnlineLadyList(true); 
//                			refreshView.setRefreshing(false);
                		} 
                	} 
                }break;
                default:break;
				}
			}

			@Override
			public void onScroll(PLA_AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {
			}
			
		});
		
		
		refreshView.setSwipeableChildren(R.id.ladylist);
		
		// 设置默认页
		dropList.setSelectedItem(mDefaultItem);
		resetSearchCriteria();
		SelectProc(mDefaultItem);
		
		return view;
	}
	
	private void resetSearchCriteria(){
		mSearchType = SearchType.DEFAULT;
    	mAge1 = -1;
    	mAge2 = -1;
    	mOnlineType = OnlineType.DEFAULT;
    	mOrderType = OrderType.DEFAULT;
    	mWomanId = "";
	}
	
	/**
	 * 刷新女士在线列表
	 */
	public void refreshOnlineLady(){
		dropList.setSelectedItem(mDefaultItem);
		resetSearchCriteria();
		SelectProc(mDefaultItem);
	}
	
	/**
	 * 刷新女士在线列表
	 */
	public void refreshNewestLady(){
		dropList.setSelectedItem(3);
		resetSearchCriteria();
		SelectProc(3);
	}
	
	/**
	 * 刷新女士在线列表
	 */
	public void refreshAvaiableCallLady(){
		dropList.setSelectedItem(1);
		resetSearchCriteria();
		SelectProc(1);
	}
	
	
	
	private void SelectProc(int arg)
	{
		ClearLadyList();
		switch (arg){
		case 0:
			mTitle = mContext.getString(R.string.lady_category_online);
			appbar.getTitileView().setText(mTitle);
			mSearchType = SearchType.DEFAULT;
			mOnlineType = OnlineType.ONLINE;
			QueryOnlineLadyList(false);
			break;
			
		case 1:
			mTitle = mContext.getString(R.string.lady_category_with_call);
			appbar.getTitileView().setText(mTitle);
			mSearchType = SearchType.WITHPHONE;
			QueryOnlineLadyList(false);
			break;
			
		case 2:
			mTitle = mContext.getString(R.string.lady_category_with_video);
			appbar.getTitileView().setText(mTitle);
			mSearchType = SearchType.WITHVIDEO;
			QueryOnlineLadyList(false);
			break;
			
		case 3:
			mTitle = mContext.getString(R.string.lady_category_newest);
			appbar.getTitileView().setText(mTitle);
			mSearchType = SearchType.DEFAULT;  /**Newest 改爲使用default, 按最新的排在最前面**/
			mOrderType = OrderType.NEWST;
			QueryOnlineLadyList(false);
			break;
		case -1:
			QueryOnlineLadyList(false);
			break;
		default :
			break;
		}
		
		if (null != mCallback) {
			mCallback.OnListSelected(arg);
		}
	}
	

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.common_button_mainmenu:
			if( mCallback != null ) {
				mCallback.OnClickOpenDrawer(v);
			}
			break;
		case R.id.common_button_search:
			break;
		case R.id.common_button_contacts:
			// 如果没有INCHAT, 去除右边红点
			if( mCallback != null ) {
				mCallback.OnClickContact(v);
			}
			break;
		default:
			break;
		}
	}
	
	// *********************	接口相关	*********************
	/**
	 * 消息分发
	 */
	@SuppressLint("HandlerLeak")
	public void InitHandler() {
		// TODO Auto-generated method stub
		mHandler = new Handler() {
			public void handleMessage(android.os.Message msg) {
				// 收起菊花
				refreshView.setRefreshing(false);
				if (progressbar != null){
					//Use only once
					progressbar.setVisibility(View.GONE);
					progressbar = null;
				}
				switch ( RequestFlag.values()[msg.what] ) {
				case REQUEST_LADY_LIST_SUCCESS:{
					MessageCallbackItem obj = (MessageCallbackItem) msg.obj;
					if( obj.bLoadMore ) {
						// 上拉更多成功
						mbLoadingMore = false;
					} 
					else {
						// 刷新成功
						mLadyList.clear();
						RequestLadyListAdvert();
					}
					
					// 添加女士
		    		if( obj.ladyList != null ) {
		    			for(Lady item : obj.ladyList) {
		    				LadyListItem ladyItem = GetLadyExist(mLadyList, item.womanid);
		    				if( ladyItem == null ) {
		    					// 创建新的女士
					    		float radio = (float)LadyListManager.getRadio();
					    		int backgroundColorType = LadyListManager.getRandomBackgroundColorType();
		    					ladyItem = new LadyListItem(radio, item, backgroundColorType);
		    					mLadyList.add(ladyItem);
		    				} else {
		    					ladyItem.lady = item;
		    				}
		    			}

		    		}
		    		
					// 刷新列表
		    		if (mLadyList == null || mLadyList.size() == 0){
	    				noDataView.setText(mNoDataMessage);
	    				noDataView.setVisibility(View.VISIBLE);
	    				
	    			}else{
	    				noDataView.setVisibility(View.GONE);
	    			}
		    		
					mAdapter.notifyDataSetChanged();
				}break;
				case REQUEST_LADY_LIST_FAIL:{
					MessageCallbackItem obj = (MessageCallbackItem) msg.obj;
					if( obj.bLoadMore ) {
						// 上拉更多成功
						mbLoadingMore = false;
					} 
					if (mLadyList == null || mLadyList.size() == 0){
	    				noDataView.setText(mContext.getString(R.string.error_msg));
	    				noDataView.setVisibility(View.VISIBLE);
	    				
	    			}else{
	    				noDataView.setVisibility(View.GONE);
	    			}
					
				}break;
				case REQUEST_LADY_LIST_ADVERT_SUCCESS: {
					AdWomanListAdvert advert = (AdWomanListAdvert)msg.obj;
					mAdWomanListAdvertItem.SetWomanListAdvert(mContext, advert);
					mAdapter.SetAdvert(mAdWomanListAdvertItem);
					mAdapter.notifyDataSetChanged();
				}break;
				case REQUEST_ADD_FAVOUR_SUCCESS:{
					// 收藏女士成功
					if( mCallback != null ) {
						mCallback.OnRequestFinish(true, "Added!");
					}
				}break;
				case REQUEST_ADD_FAVOUR_FAIL:{
					// 收藏失败
					if( mCallback != null ) {
						mCallback.OnRequestFinish(false, "Fail!");
					}
				}break;
				case REQUEST_GET_LOVE_CALL_SUCCESS:{
					MessageCallbackItem obj = (MessageCallbackItem) msg.obj;
					// 请求lovecall成功
					makeCall(obj.ladyCall.lc_centernumber, obj.ladyCall.lovecallid);
					if( mCallback != null ) {
						mCallback.OnRequestFinish(true, "Finish!");
					}
				}break;
				case REQUEST_GET_LOVE_CALL_FAIL:{
					// 请求lovecall失败
					//Toast.makeText(mContext, obj.errmsg, Toast.LENGTH_LONG).show();
					MessageCallbackItem obj = (MessageCallbackItem) msg.obj;
					MaterialDialogAlert dialog = new MaterialDialogAlert(mContext);
					
					if (!obj.errno.equals("MBCE61005")){   //RequestErrorCode 裏面沒有這個錯誤代碼.
						Lady[] ladyList = (Lady[])obj.ladyList;
						if(ladyList != null && ladyList.length > 0){
							final Lady lady = ladyList[0];
							dialog.setMessage(String.format(mContext.getResources().getString(R.string.lovecall_mail_schedule_makecall_error_tips), lady.firstname));
							dialog.addButton(dialog.createButton(mContext.getString(R.string.common_btn_ok), new OnClickListener(){
								@Override
								public void onClick(View v) {
									// TODO Auto-generated method stub
									LadyDetail ladyDetail = new LadyDetail();
									ladyDetail.womanid = lady.womanid;
									ladyDetail.age = lady.age;
									ladyDetail.firstname = lady.firstname;
									ladyDetail.country = lady.country;
									ScheduleCallActivity.launchScheduleCallActivity(mContext, ladyDetail);
								}
							}));
						}else{
							dialog.setMessage(obj.errmsg);
						}
						dialog.addButton(dialog.createButton(mContext.getString(R.string.common_btn_no), null));
						if((mContext != null) && (mContext instanceof BaseFragmentActivity)){
							if(((BaseFragmentActivity)mContext).isActivityVisible()){
								dialog.show();
							}
						}else{
							dialog.show();
						}
						if( mCallback != null ) {
							mCallback.OnRequestFinish(false, null);
						}
						return;
					}
					dialog.setMessage(obj.errmsg);
					dialog.addButton(dialog.createButton(mContext.getString(R.string.common_btn_add_credit), new OnClickListener(){

						@Override
						public void onClick(View v) {
							// TODO Auto-generated method stub
							GetMoreCreditDialog dialog = new GetMoreCreditDialog(mContext, R.style.ChoosePhotoDialog);
							dialog.show();
						}
						
					}));
					
					dialog.addButton(dialog.createButton(mContext.getString(R.string.common_btn_cancel), null));
					if((mContext != null) && (mContext instanceof BaseFragmentActivity)){
						if(((BaseFragmentActivity)mContext).isActivityVisible()){
							dialog.show();
						}
					}else{
						dialog.show();
					}
					if( mCallback != null ) {
						mCallback.OnRequestFinish(false, null);
					}
				}
				case LIVECHAT_CONTACT_MESSAGE:{
					Log.d("HomeContentViewController", "LIVECHAT_CONTACT_MESSAGE()");
//					appbar.cancelBadgeById(R.id.common_button_contacts);
				}break;
				default:
					break;
				}
			};
		};
		
		mLiveChatHandler = new Handler() {
			public void handleMessage(android.os.Message msg) {
				switch (LiveChatFlag.values()[msg.what])
				{
				case LIVECHAT_RECV_MESSAGE: {
					if (msg.obj instanceof LCMessageItem) {
						boolean isShowBadge = false;
						LCMessageItem item = (LCMessageItem) msg.obj;
						if( ContactManager.getInstance().mWomanId.compareTo(item.fromId) != 0 ) {
							if( item != null && item.getUserItem() != null && LiveChatManager.getInstance().GetChatingUsers() != null ) {
								if (item.getUserItem().chatType == ChatType.Invite
										&& LiveChatManager.getInstance().GetChatingUsers().size() == 0) 
									{
										// 邀请消息而且没有inchat用户
										// 若当前没有inchat状态会话，每当收到邀请消息，则显示“点”。
										isShowBadge = true;
									}
									else if (item.getUserItem().chatType == ChatType.InChatCharge
											|| item.getUserItem().chatType == ChatType.InChatUseTryTicket) 
									{
										// 消息为inchat用户发出
										// 若当前有inchat状态会话，每当收到inchat消息，则显示“点”。
										isShowBadge = true;
									}
							}
						}
						
						Log.d("HomeContentViewController", "LIVECHAT_RECV_MESSAGE( " + isShowBadge + " )");
						if (isShowBadge) {
							appbar.pushBadgeById(R.id.common_button_contacts, mContext.getResources().getColor(R.color.white));
						}
					}
				}break;
				default:
					break;
				}
			}
		};
	}
	
	/**
	 * 查找在线女士列表
	 * @param loadMore
	 */
	public void QueryOnlineLadyList(final boolean loadMore) {
		// 每页最大纪录数
		final int pageSize = 10;
		
		if( !loadMore ) {
			// 刷最新
			mOnlineLadyPageIndex = 1;
		} else {
			mbLoadingMore = true;
			mOnlineLadyPageIndex++;
		}
		
		mAdapter.mChatButtonType = ChatButtonType.Default;
		refreshView.setRefreshing(true); /** load more 還是刷新都照顯示菊花 (Martin) **/
		noDataView.setVisibility(View.GONE);
		mNoDataMessage = "";
		if (mSearchType == SearchType.DEFAULT){
			if (mOnlineType == OnlineType.ONLINE){
				
				if (mAge1 == -1){ //All online ladies
					mNoDataMessage = mContext.getResources().getString(R.string.no_online_lady_at_the_moment);
				}else{
					/**Search online ladies**/
					mNoDataMessage = mContext.getResources().getString(R.string.no_ladies_which_age_between_x_and_x_are_online).replace("age1", ""+mAge1).replace("age2", ""+mAge2);
				}
				mAdapter.mChatButtonType = ChatButtonType.Chat;
			}else{
				if (mAge1 == -1){
					/**All ladies**/
					mNoDataMessage = mContext.getResources().getString(R.string.error_msg); //if there is no ladies and there are errors or lies.
				}else{
					/**Search ladies no matter online or not**/
					mNoDataMessage = mContext.getResources().getString(R.string.no_ladies_found_whoes_age_between_x_and_x).replace("age1", ""+mAge1).replace("age2", ""+mAge2);
				}
				mAdapter.mChatButtonType = ChatButtonType.Default;
			}
		}else if (mSearchType == SearchType.BYCONDITION){  ///Search by condition doesn't fuxking work and i don't know why. But i user default searcher plus age material and it works.
			if (mOnlineType == OnlineType.ONLINE){
				mNoDataMessage = mContext.getResources().getString(R.string.no_ladies_which_age_between_x_and_x_are_online).replace("age1", ""+mAge1).replace("age2", ""+mAge2);
			}else{
				mNoDataMessage = mContext.getResources().getString(R.string.no_ladies_found_whoes_age_between_x_and_x).replace("age1", ""+mAge1).replace("age2", ""+mAge2);
			}
			mAdapter.mChatButtonType = ChatButtonType.Default;
		}else if(mSearchType == SearchType.WITHPHONE){
			mNoDataMessage = mContext.getResources().getString(R.string.no_ladies_available_for_call_at_the_moment);
			mAdapter.mChatButtonType = ChatButtonType.Call;
		}else if(mSearchType == SearchType.WITHVIDEO){
			mNoDataMessage = mContext.getResources().getString(R.string.error_msg);
			mAdapter.mChatButtonType = ChatButtonType.Video;
		}else if(mSearchType == SearchType.FAVOURITE){
			mNoDataMessage = mContext.getResources().getString(R.string.your_favorite_list_is_empty);
			mAdapter.mChatButtonType = ChatButtonType.Default;
		}else if(mSearchType == SearchType.BYID){
			mNoDataMessage = mContext.getResources().getString(R.string.profile_id_x_does_not_exist, mWomanId.toUpperCase(Locale.ENGLISH));
			mAdapter.mChatButtonType = ChatButtonType.Default;
		}
		
		TelephonyManager tm = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
		LadyDetailManager.getInstance().QueryLadyList(
//		RequestJniLady.QueryLadyList(
				mOnlineLadyPageIndex,
				pageSize,
				mSearchType, 
				mWomanId,
				mOnlineType, 
				mAge1,
				mAge2, 
				"",
				mOrderType,
				RequestJni.GetDeviceId(tm),
				new OnQueryLadyListCallback() {
					
					@Override
					public void OnQueryLadyList(boolean isSuccess, String errno, String errmsg,
							Lady[] ladyList, int totalCount) {
						// TODO Auto-generated method stub
						MessageCallbackItem obj = new MessageCallbackItem(errno, errmsg);
			    		Message msg = Message.obtain();
				    	if( isSuccess ) {
				    		msg.what = RequestFlag.REQUEST_LADY_LIST_SUCCESS.ordinal();
		    				obj.ladyList = ladyList;
				    	} else {
				    		msg.what = RequestFlag.REQUEST_LADY_LIST_FAIL.ordinal();
				    	}
				    	obj.bLoadMore = loadMore;
				    	msg.obj = obj;
				    	mHandler.sendMessage(msg);
					}
				});

	}
	
	private void ClearLadyList() {
		mLadyList.clear();
		mAdapter.notifyDataSetChanged();
	}
	/**
	 * 判断女士是否已经存在列表
	 * @param list			女士列表
	 * @param womanId		女士id
	 * @return				是否已经存在
	 */
	public boolean RemoveLadyExist(List<LadyListItem> list, String womanId) {
		boolean bFlag = false;
		int i = 0;
		for( LadyListItem item : list ) {
			if( item.lady.womanid.compareTo(womanId) == 0 ) {
				list.remove(i);
				break;
			}
			i++;
		}
		return bFlag;
	}
	
	/**
	 * 判断女士是否已经存在列表
	 * @param list			女士列表
	 * @param womanId		女士id
	 * @return				是否已经存在
	 */
	public boolean CheckLadyExist(List<LadyListItem> list, String womanId) {
		boolean bFlag = false;
		for( LadyListItem item : list ) {
			if( item.lady.womanid.compareTo(womanId) == 0 ) {
				bFlag = true;
				break;
			}
		}
		return bFlag;
	}
	
	/**
	 * 获取已经存在的女士
	 * @param list			女士列表
	 * @param womanId		女士id
	 * @return				
	 */
	public LadyListItem GetLadyExist(List<LadyListItem> list, String womanId) {
		for( LadyListItem item : list ) {
			if( item.lady.womanid.compareTo(womanId) == 0 ) {
				return item;
			}
		}
		return null;
	}
	
	/**
	 * 请求女士lovecall
	 * @param womanId
	 */
	public void QueryLadyCall(final Lady lady) {
		if( !LoginManager.getInstance().CheckLogin(mContext) ) {
			return;
		}
		
		if( mCallback != null ) {
			mCallback.OnRequest("Calling");
		}
		RequestOperator.getInstance().QueryLadyCall(lady.womanid, new OnQueryLadyCallCallback() {
			
			@Override
			public void OnQueryLadyCall(boolean isSuccess, String errno, String errmsg,
					LadyCall item) {
				// TODO Auto-generated method stub
				Message msg = Message.obtain();
				MessageCallbackItem obj = new MessageCallbackItem(errno, errmsg);
				if( isSuccess ) {
					// 获取个人信息成功
					msg.what = RequestFlag.REQUEST_GET_LOVE_CALL_SUCCESS.ordinal();
					obj.ladyCall = item;
				} else {
					// 获取个人信息失败
					msg.what = RequestFlag.REQUEST_GET_LOVE_CALL_FAIL.ordinal();
					obj.ladyList = new Lady[]{lady};
				}
				msg.obj = obj;
				mHandler.sendMessage(msg);
			}
		});
	}
	
	/**
	 * 请求收藏女士
	 */
	public void AddFavour(String womanId) {
		if( !LoginManager.getInstance().CheckLogin(mContext) ) {
			return;
		}
		
		if( mCallback != null ) {
			mCallback.OnRequest("Adding");
		}
		RequestOperator.getInstance().AddFavouritesLady(womanId, new OnRequestCallback() {
			
			@Override
			public void OnRequest(boolean isSuccess, String errno, String errmsg) {
				// TODO Auto-generated method stub
				Message msg = Message.obtain();
				MessageCallbackItem obj = new MessageCallbackItem(errno, errmsg);
				if( isSuccess ) {
					// 获取个人信息成功
					msg.what = RequestFlag.REQUEST_ADD_FAVOUR_SUCCESS.ordinal();
				} else {
					// 获取个人信息失败
					msg.what = RequestFlag.REQUEST_ADD_FAVOUR_FAIL.ordinal();
				}
				msg.obj = obj;
				mHandler.sendMessage(msg);
			}
		});
	}
	
	
	public void OnChangeWebsite(WebSite website) {
		// TODO Auto-generated method stub
		appbar.setAppbarBackgroundColor(mContext.getResources().getColor(website.getSiteColor()));

		// 设置默认页
		dropList.setSelectedItem(mDefaultItem);
		resetSearchCriteria();
		SelectProc(mDefaultItem);
	}
	
	public void OnRecvMessage(LCMessageItem item) {
		Message msg = Message.obtain();
		msg.what = LiveChatFlag.LIVECHAT_RECV_MESSAGE.ordinal();
		msg.obj = item;
		mLiveChatHandler.sendMessage(msg);
	}
	
	public void OnOpen() {
		// 当用户操作（点击或左拉），显示主界面右侧列表，“点”则消失。
		appbar.cancelBadgeById(R.id.common_button_contacts);
	}
	
	private void makeCall(final String callcenterNumber, final String callId) {
		/* 检测有无Sim卡 */
		if (SystemUtil.isSimCanUse(mContext)) {
			/*资费提示*/
			
			if (LoginPerfence.GetStringPreference(mContext, "donnot_show_love_call_fee").equals("true")){
				new DirectCallManager(mContext).makeCall(callcenterNumber, callId);
				/*获取token成功，去拨号，添加到现有联系人*/
				if(mCallingLadyItem != null && mCallingLadyItem.lady != null){
					ContactManager.getInstance().updateOrAddContact(mCallingLadyItem.lady.womanid);
				}
				return;
			}
			
			MaterialThreeButtonDialog dialog = new MaterialThreeButtonDialog(mContext, new MaterialThreeButtonDialog.OnClickCallback() {
				
				@Override
				public void OnSecondButtonClick(View v) {
					new DirectCallManager(mContext).makeCall(callcenterNumber, callId);
					/*获取token成功，去拨号，添加到现有联系人*/
					if(mCallingLadyItem != null && mCallingLadyItem.lady != null){
						ContactManager.getInstance().updateOrAddContact(mCallingLadyItem.lady.womanid);
					}
					LoginPerfence.SaveStringPreference(mContext, "donnot_show_love_call_fee", "true");
				}
				
				@Override
				public void OnFirstButtonClick(View v) {
					new DirectCallManager(mContext).makeCall(callcenterNumber, callId);
					/*获取token成功，去拨号，添加到现有联系人*/
					if(mCallingLadyItem != null && mCallingLadyItem.lady != null){
						ContactManager.getInstance().updateOrAddContact(mCallingLadyItem.lady.womanid);
					}
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
			if((mContext != null) && (mContext instanceof BaseFragmentActivity)){
				if(((BaseFragmentActivity)mContext).isActivityVisible()){
					dialog.show();
				}
			}else{
				dialog.show();
			}
		} else {
			MaterialDialogAlert dialog = new MaterialDialogAlert(mContext);
			dialog.setTitle(mContext.getString(R.string.lovecall_no_sim_tips));
			dialog.setMessage(mContext.getString(R.string.lovecall_instruction, callcenterNumber));
			dialog.addButton(dialog.createButton(mContext.getString(R.string.common_btn_ok), null));
			if((mContext != null) && (mContext instanceof BaseFragmentActivity)){
				if(((BaseFragmentActivity)mContext).isActivityVisible()){
					dialog.show();
				}
			}else{
				dialog.show();
			}
		}
	}
	
	/**
	 * 请求女士列表广告
	 */
	private void RequestLadyListAdvert()
	{
		TelephonyManager tm = (TelephonyManager) mContext
				.getSystemService(Context.TELEPHONY_SERVICE);
		
		RequestJniAdvert
			.WomanListAdvert(
					RequestJni.GetDeviceId(tm),
					(mAdWomanListAdvertItem != null && mAdWomanListAdvertItem.adWomanListAdvert != null) ? mAdWomanListAdvertItem.adWomanListAdvert.id
							: "", mAdWomanListAdvertItem.showTimes,
					mAdWomanListAdvertItem.clickTimes,
					new OnAdWomanListAdvertCallback() {
	
						@Override
						public void OnAdWomanListAdvert(boolean isSuccess,
								String errno, String errmsg,
								AdWomanListAdvert advert) {
							// TODO Auto-generated method stub
							if (isSuccess) 
							{
								Message msg = Message.obtain();
								msg.what = RequestFlag.REQUEST_LADY_LIST_ADVERT_SUCCESS.ordinal();
								msg.obj = advert;
								mHandler.sendMessage(msg);
							}
						}
					});
	}
}
