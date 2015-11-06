package com.qpidnetwork.dating.home;

import java.util.ArrayList;
import java.util.List;

import me.tangke.slidemenu.SlideMenu;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.qpidnetwork.dating.R;
import com.qpidnetwork.dating.authorization.LoginManager;
import com.qpidnetwork.dating.authorization.LoginManager.LoginStatus;
import com.qpidnetwork.dating.bean.ContactBean;
import com.qpidnetwork.dating.contacts.ContactManager;
import com.qpidnetwork.dating.contacts.ContactSearchActivity;
import com.qpidnetwork.dating.contacts.ContactsAdapter;
import com.qpidnetwork.dating.contacts.OnGetContactListCallBack;
import com.qpidnetwork.dating.lady.LadyDetailManager;
import com.qpidnetwork.dating.lady.LadyDetailManager.OnLadyDetailManagerQueryLadyDetailCallback;
import com.qpidnetwork.dating.livechat.ExpressionImageGetter;
import com.qpidnetwork.dating.livechat.LiveChatCallBackItem;
import com.qpidnetwork.dating.livechat.invite.LivechatInviteListActivity;
import com.qpidnetwork.framework.util.StringUtil;
import com.qpidnetwork.framework.util.UnitConversion;
import com.qpidnetwork.livechat.LCMessageItem;
import com.qpidnetwork.livechat.LCMessageItem.MessageType;
import com.qpidnetwork.livechat.LCUserItem;
import com.qpidnetwork.livechat.LiveChatManager;
import com.qpidnetwork.manager.FileCacheManager;
import com.qpidnetwork.request.item.LadyDetail;
import com.qpidnetwork.tool.ImageViewLoader;
import com.qpidnetwork.view.ButtonRaised;
import com.qpidnetwork.view.MaterialProgressBar;

@SuppressLint("InflateParams")
public class HomeContactViewController implements OnClickListener,
		OnRefreshListener, OnGetContactListCallBack,
		OnLadyDetailManagerQueryLadyDetailCallback {

	private static final int GET_CONTACTLIST_SUCCESS = 0;
	private static final int GET_CONTACTLIST_FAILED = 1;
	private static final int QUERY_LADY_DETAIL = 2;

	private Context mContext;
	private HomeActivity mActivity;

	private View mView;
	private SwipeRefreshLayout mRefreshView;
	private ListView mContactList;
	private ImageButton expand;
	private ImageButton search;
	private ContactsAdapter mAdapter;
	private ContactManager mContactManager;
	private boolean isInited = false;
	private MaterialProgressBar progressBar;
	private LinearLayout noDataView;
	private ButtonRaised noDataViewLogin;
	private TextView noDataViewTips;

	private List<ContactBean> mListData;

	/* invite */
	private LinearLayout footer;
	private ImageView ivInvitePhoto;
	private TextView tvInviteMsg;
	private TextView tvInviteNum;
	private LiveChatManager mLiveChatManager;
	private ImageViewLoader mImageViewLoader;

	public HomeContactViewController(Context context) {
		mContext = context;
		mActivity = (HomeActivity) context;
		getView();
		mImageViewLoader = new ImageViewLoader(context);
	}

	public View getView() {

		if (mView != null)
			return mView;

		mView = LayoutInflater.from(mContext).inflate(
				R.layout.fragment_contacts, null);
		Point size = getScreenSize();
		SlideMenu.LayoutParams params = new SlideMenu.LayoutParams(size.x
				- getStaticOffset(), LayoutParams.MATCH_PARENT);
		mView.setLayoutParams(params);

		mRefreshView = (SwipeRefreshLayout) mView
				.findViewById(R.id.swipeRefreshLayout);
		mContactList = (ListView) mView.findViewById(R.id.lvContacts);
		expand = (ImageButton) mView.findViewById(R.id.expand);
		search = (ImageButton) mView.findViewById(R.id.search);
		progressBar = (MaterialProgressBar) mView
				.findViewById(R.id.progress_bar);
		noDataView = (LinearLayout) mView.findViewById(R.id.no_content);
		noDataView.setVisibility(View.GONE);
		noDataViewLogin = (ButtonRaised) mView
				.findViewById(R.id.btn_no_data_login);
		noDataViewTips = (TextView) mView.findViewById(R.id.no_data_view_tips);

		if (Build.VERSION.SDK_INT >= 21) {
			expand.getLayoutParams().height = UnitConversion.dip2px(mContext,
					48);
			expand.getLayoutParams().width = UnitConversion
					.dip2px(mContext, 48);
			search.getLayoutParams().height = UnitConversion.dip2px(mContext,
					48);
			search.getLayoutParams().width = UnitConversion
					.dip2px(mContext, 48);
		}

		noDataViewLogin.setOnClickListener(this);
		search.setOnClickListener(this);
		expand.setOnClickListener(this);

		mContactManager = ContactManager.getInstance();
		mAdapter = new ContactsAdapter((Activity) mContext, 1);
		mAdapter.setComparator(ContactBean.getComparator());
		mContactList.setAdapter(mAdapter);

		mRefreshView.setOnRefreshListener(this);

		/* invite */
		initFootInvite(mView);

		queryContactList();

		return mView;

	}

	private void queryContactList() {
		if (LoginManager.getInstance().GetLoginStatus() == LoginStatus.LOGINED) {
			/* 已登录再调用获取联系人接口 */
			if (!isInited) {
				// showInitLoading();
			}
			noDataView.setVisibility(View.GONE);
			mContactManager.getContacts(this);
			mRefreshView.setRefreshing(true);
		} else {
			/* 未登录，刷新登陆失败 */
			Message msg = Message.obtain();
			msg.what = GET_CONTACTLIST_FAILED;
			msg.obj = "";
			handleUiMessage(msg);
		}
	}

	private void handleUiMessage(Message msg) {
		uiMessageHandler.sendMessage(msg);
	}

	private Handler uiMessageHandler = new Handler() {
		@SuppressWarnings("unchecked")
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);

			switch (msg.what) {
			case GET_CONTACTLIST_SUCCESS:
				mListData = (List<ContactBean>) msg.obj;
				mAdapter.replaceList(mListData);
				isInited = true;
				mRefreshView.setRefreshing(false);
				if (progressBar != null) {
					progressBar.setVisibility(View.GONE);
					progressBar = null;
				}

				showListOrNoContent();

				break;

			case GET_CONTACTLIST_FAILED:
				if (!isInited) {
					// showInitError();
				} else {

				}
				mRefreshView.setRefreshing(false);
				if (progressBar != null) {
					progressBar.setVisibility(View.GONE);
					progressBar = null;
				}

				showListOrNoContent();
				break;

			case QUERY_LADY_DETAIL:
				LiveChatCallBackItem item = (LiveChatCallBackItem) msg.obj;
				if (item.errType == 1) {
					LadyDetail ladyDetail = (LadyDetail) item.body;
					/* 获取女士资料成功，下载女士头像 */
					ivInvitePhoto
							.setImageResource(R.drawable.female_default_profile_photo_40dp);
					if (!StringUtil.isEmpty(ladyDetail.photoMinURL)) {
						String localPath = FileCacheManager.getInstance()
								.CacheImagePathFromUrl(ladyDetail.photoMinURL);
						mImageViewLoader.DisplayImage(ivInvitePhoto,
								ladyDetail.photoMinURL, localPath, null);
					}
				}
				break;
			}
		}

	};

	private void showListOrNoContent() {
		if (mAdapter.getDataList() == null
				|| mAdapter.getDataList().size() == 0) {
			noDataView.setVisibility(View.VISIBLE);
			if (LoginManager.getInstance().CheckLogin(mContext, false)) {
				noDataViewTips
						.setText(R.string.contact_no_data_tips_logined_in);
				noDataViewLogin.setVisibility(View.GONE);
			} else {
				noDataViewLogin.setVisibility(View.VISIBLE);
				noDataViewTips.setText(R.string.contact_no_data_tips_not_login);
			}

		} else {
			noDataView.setVisibility(View.GONE);
		}
	}

	public void reloadDataIfNull() {
		if (mAdapter.getDataList() == null
				|| mAdapter.getDataList().size() == 0)
			queryContactList();
	}

	public void listenToContactListUpdate(List<ContactBean> list) {
		mAdapter.replaceList(list);
		showListOrNoContent();

	}

	/* 登陆处理 */
	public void listenToLoginActivity() {
		/* 站点切换，登陆成功，刷新底部邀请处理 */
		if (mLiveChatManager != null && footer != null) {
			/* 防止未初始化完成 */
			updateInviteView();
		}
	}

	/* 注销处理 */
	public void listenToLogoutActivity() {
		mAdapter.getDataList().clear();
		mAdapter.notifyDataSetChanged();

		/* 站点切换，注销成功，隐藏底部邀请 */
		if (footer != null) {
			footer.setVisibility(View.GONE);
		}
	}

	public Point getSize() {
		Point size = getScreenSize();
		size.x -= getOffset();
		return size;
	}

	public int getStaticOffset() {
		return (int) (56.0 * mContext.getResources().getDisplayMetrics().density);
	}

	public void setOffset(int offset) {

		if (offset == 0) {
			expand.setImageResource(R.drawable.ic_launch_left_back_white_24dp);
		} else {
			expand.setImageResource(R.drawable.ic_launch_left_white_24dp);
		}

		SlideMenu.LayoutParams params = (SlideMenu.LayoutParams) getView()
				.getLayoutParams();
		params.width = getScreenSize().x - offset;
		getView().requestLayout();

	}

	public int getOffset() {
		SlideMenu.LayoutParams params = (SlideMenu.LayoutParams) getView()
				.getLayoutParams();
		return getScreenSize().x - params.width;
	}

	private Point getScreenSize() {
		Point size = null;
		if (Build.VERSION.SDK_INT > 12) {
			size = getScreenSizeSDK13();
		} else {
			size = getScreenSizeSDK1();
		}

		return size;

	}

	@SuppressLint("NewApi")
	private Point getScreenSizeSDK13() {
		Point size = new Point();
		Display display = mActivity.getWindowManager().getDefaultDisplay();

		display.getSize(size);
		return size;
	}

	@SuppressWarnings("deprecation")
	private Point getScreenSizeSDK1() {
		Point size = new Point();
		Display display = mActivity.getWindowManager().getDefaultDisplay();

		size.x = display.getWidth();
		size.y = display.getHeight();
		return size;
	}

	/****************************** invite处理 *************************************/

	private void initFootInvite(View view) {
		footer = (LinearLayout) mView.findViewById(R.id.footer);
		ivInvitePhoto = (ImageView) mView.findViewById(R.id.ivInvitePhoto);
		tvInviteMsg = (TextView) mView.findViewById(R.id.tvInviteMsg);
		tvInviteNum = (TextView) mView.findViewById(R.id.tvInviteNum);

		mLiveChatManager = LiveChatManager.newInstance(mActivity);
		updateInviteView();

		footer.setOnClickListener(this);
	}

	/**
	 * 邀请数据刷新，更新界面
	 */
	private void updateInviteView() {
		ArrayList<LCUserItem> inviteList = mLiveChatManager.GetInviteUsers();
		if (inviteList != null && inviteList.size() > 0) {
			LCUserItem lastInvite = mLiveChatManager.GetLastInviteUser();

			if (lastInvite != null && !StringUtil.isEmpty(lastInvite.userId)) {
				/* 邀请列表不为null且最后一条记录不为空显示底部,并且最后一条邀请用户的最近一条消息必须是text */
				footer.setVisibility(View.VISIBLE);
				QueryLadyDetail(lastInvite.userId);

				// 设置显示邀请数字符串
				String strInviteNum = "";
				if (inviteList.size() > 99) {
					strInviteNum = "99+";
				} else {
					strInviteNum = String.valueOf(inviteList.size());
				}
				tvInviteNum.setText(strInviteNum);

				/* 提示消息内容 */
				LCMessageItem lastMsg = lastInvite.getTheOtherLastMessage();
				if (lastMsg != null) {
					if ((lastMsg.msgType == MessageType.Text)
							&& (lastMsg.getTextItem() != null && lastMsg
									.getTextItem().message != null)) {
						ExpressionImageGetter imageGetter = new ExpressionImageGetter(
								mContext, UnitConversion.dip2px(mContext, 28),
								UnitConversion.dip2px(mContext, 28));
						tvInviteMsg
								.setText(imageGetter.getExpressMsgHTML(lastMsg
										.getTextItem().message));
					} else {
						String inviteMsg = ContactManager.getInstance()
								.generateMsgHint(lastMsg);
						tvInviteMsg.setText(inviteMsg);
					}
				}
			}
		} else {
			/* 无邀请直接隐藏底部，不显示. */
			footer.setVisibility(View.GONE);
		}
	}

	/* 新邀请来更新界面显示 */
	public void onNewInviteUpdate() {
		updateInviteView();
	}

	/**
	 * 邀请列表无女士头像，需异步获取联系人资料获得更新
	 * 
	 * @param womanid
	 */
	private void QueryLadyDetail(String womanid) {
		LadyDetailManager.getInstance().QueryLadyDetail(womanid, this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_no_data_login: {
			mActivity.getSlideMenu().close(true);
			mActivity.getSlideMenu().postDelayed(new Runnable() {

				@Override
				public void run() {
					// TODO Auto-generated method stub
					LoginManager.getInstance().CheckLogin(mContext);
				}

			}, 200);
		}
			break;
		case R.id.expand: {
			mActivity.scrollToLeftEdge();
		}
			break;
		case R.id.search: {
			ContactSearchActivity.launchContactSearchActivity(mContext, 1);
			mActivity.overridePendingTransition(R.anim.anim_donot_animate,
					R.anim.anim_donot_animate);
		}
			break;
		case R.id.footer: {
			Intent intent = new Intent(mContext,
					LivechatInviteListActivity.class);
			mContext.startActivity(intent);
		}
			break;
		default:
			break;
		}
	}

	@Override
	public void onRefresh() {
		queryContactList();
	}

	@Override
	public void onContactListCallback(boolean isSuccess, String errno,
			String errmsg) {
		Message msg = Message.obtain();
		if (isSuccess) {
			msg.what = GET_CONTACTLIST_SUCCESS;
			msg.obj = mContactManager.getContactList();
		} else {
			msg.what = GET_CONTACTLIST_FAILED;
			msg.obj = errmsg;
		}
		handleUiMessage(msg);
	}

	@Override
	public void OnQueryLadyDetailCallback(boolean isSuccess, String errno,
			String errmsg, LadyDetail item) {
		Message msg = Message.obtain();
		msg.what = QUERY_LADY_DETAIL;
		LiveChatCallBackItem ladyDetail = new LiveChatCallBackItem(
				isSuccess ? 1 : 0, errno, errmsg, item);
		msg.obj = ladyDetail;
		handleUiMessage(msg);
	}

}
