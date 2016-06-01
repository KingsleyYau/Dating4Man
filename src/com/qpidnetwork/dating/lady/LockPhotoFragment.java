package com.qpidnetwork.dating.lady;

import java.util.List;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.qpidnetwork.dating.R;
import com.qpidnetwork.dating.authorization.LoginManager;
import com.qpidnetwork.dating.credit.BuyCreditActivity;
import com.qpidnetwork.dating.emf.MailEditActivity;
import com.qpidnetwork.dating.googleanalytics.AnalyticsFragmentActivity;
import com.qpidnetwork.dating.livechat.ChatActivity;
import com.qpidnetwork.framework.base.BaseFragment;
import com.qpidnetwork.manager.MonthlyFeeManager;
import com.qpidnetwork.manager.WebSiteManager;
import com.qpidnetwork.request.RequestJniEMF.ReplyType;
import com.qpidnetwork.request.RequestJniMonthlyFee.MemberType;
import com.qpidnetwork.request.item.LadyDetail;
import com.qpidnetwork.request.item.MonthLyFeeTipItem;
import com.qpidnetwork.view.ButtonRaised;

@SuppressLint("InflateParams")
public class LockPhotoFragment extends BaseFragment {

	private LadyDetail ladyDetail;
	private ImageButton btnChat;
	private ImageButton btnEmail;
	private CardView cvNormal;//普通锁

	// 月费相关
	private CardView cvMonthlyFee;//月费锁
	private ListView listView;
	private RelativeLayout header;
	private ButtonRaised btnSubscribe;// 购买月费
	private TextView tvPrice;// 支付价格
	
	private TipsAdapter mAdapter;
	private List<String> serverslist;// 服务列表

	private MonthlyFeeManager mMonthlyFeeManager;//月费管理器
	private WebSiteManager siteManager;//站点管理器
	
	private MonthLyFeeTipItem mMonthlyItem;
	private MemberType mMemberType;

	public static LockPhotoFragment getInstance(LadyDetail ladyDetail) {
		LockPhotoFragment fragment = new LockPhotoFragment();
		Bundle bundle = new Bundle();
		bundle.putSerializable(NormalPhotoPreviewActivity.LADY_DETAIL,ladyDetail);
		fragment.setArguments(bundle);
		return fragment;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
		
		initData();//初始化数据

		View view = inflater.inflate(R.layout.fragment_lock_photo, null);
		cvNormal = (CardView) view.findViewById(R.id.cvNormal);
		btnChat = (ImageButton) view.findViewById(R.id.btnChat);
		btnEmail = (ImageButton) view.findViewById(R.id.btnEmail);
		btnChat.setOnClickListener(this);
		btnEmail.setOnClickListener(this);

		// 月费相关
		cvMonthlyFee = (CardView) view.findViewById(R.id.cvMonthlyFee);
		header = (RelativeLayout) view.findViewById(R.id.header);
		listView = (ListView) view.findViewById(R.id.listView);
		btnSubscribe = (ButtonRaised) view.findViewById(R.id.btnSubscribe);
		tvPrice = (TextView) view.findViewById(R.id.tvPrice);
		btnSubscribe.setOnClickListener(this);
		
		
		updateMonthlyView(view);// 更新月费相关控件

		return view;
	}

	/**
	 * 初始化数据
	 */
	private void initData() {
		// TODO Auto-generated method stub
		mMonthlyFeeManager = MonthlyFeeManager.getInstance();
		siteManager = WebSiteManager.getInstance();
		if(mMonthlyFeeManager!=null){
			mMemberType = mMonthlyFeeManager.getMemberType();//获取月费类型
			mMonthlyItem = mMonthlyFeeManager.getMonthLyFeeTipItem(mMemberType);//获取显示数据
		}
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onActivityCreated(savedInstanceState);
		Bundle bundle = getArguments();
		if ((bundle != null)
				&& (bundle.containsKey(NormalPhotoPreviewActivity.LADY_DETAIL))) {
			ladyDetail = (LadyDetail) bundle
					.getSerializable(NormalPhotoPreviewActivity.LADY_DETAIL);
		}
	}

	@Override
	public void onClick(View v) {
		super.onClick(v);
		switch (v.getId()) {
		case R.id.btnChat: {
			if (ladyDetail != null) {
				if (LoginManager.getInstance().CheckLogin(getActivity())) {
					ChatActivity.launchChatActivity(getActivity(),
							ladyDetail.womanid, ladyDetail.firstname,
							ladyDetail.photoMinURL);
					// getActivity().finish();
				}
			}
		}
			break;
		case R.id.btnEmail: {
			if (ladyDetail != null) {
				if (LoginManager.getInstance().CheckLogin(getActivity())) {
					MailEditActivity.launchMailEditActivity(getActivity(),
							ladyDetail.womanid, ReplyType.DEFAULT, "");
					// getActivity().finish();
				}
			}
		}
			break;
		case R.id.btnSubscribe://购买月费
			mContext.startActivity(new Intent(getActivity(),BuyCreditActivity.class));
			break;
		default:
			break;
		}
	}

	@Override
	public void onFragmentSelected(int page) {
		// 统计
		AnalyticsFragmentActivity activity = getAnalyticsFragmentActivity();
		if (null != activity) {
			activity.onAnalyticsPageSelected(this, page);
		}
	}

	@Override
	public void onFragmentPause(int page) {
		// 统计关闭Fragment event
		// AnalyticsEvent(
		// getString(R.string.AlbumLockedPhoto_Category)
		// , getString(R.string.AlbumLockedPhoto_Action_Close)
		// , "");
	}

	private AnalyticsFragmentActivity getAnalyticsFragmentActivity() {
		AnalyticsFragmentActivity activity = null;
		if (getActivity() instanceof AnalyticsFragmentActivity) {
			activity = (AnalyticsFragmentActivity) getActivity();
		}
		return activity;
	}

	/**
	 * @param view
	 * 
	 *            更新月费相关的控件
	 */
	private void updateMonthlyView(View view) {
		// TODO Auto-generated method stub
		
		ImageView lockIcon = (ImageView)view.findViewById(R.id.qpid_logo);
		TextView textView1 = (TextView) view.findViewById(R.id.textViewHeadline);
		TextView textView2 = (TextView) view.findViewById(R.id.textViewSubheadline);
		
		lockIcon.setImageResource(R.drawable.ic_lock_white_48dp);
		textView1.setText(R.string.this_photo_is_locked);
		textView2.setText(R.string.to_unlock_all_her_photos);
		
		//根据月费类型显示提示
		if(mMonthlyItem!=null){
			switch (mMemberType) {
			case NO_FEED_FIRST_MONTHLY_MEMBER:
				showMonthlyView(true);//显示月费提示
				tvPrice.setText(Html.fromHtml(mMonthlyItem.priceDescribe));
				mAdapter = new TipsAdapter(mMonthlyItem.tips);
				listView.setAdapter(mAdapter);
				break;
			case NO_FEED_MONTHLY_MEMBER:
				showMonthlyView(true);//显示月费提示
				tvPrice.setText(Html.fromHtml(mMonthlyItem.priceDescribe));
				mAdapter = new TipsAdapter(mMonthlyItem.tips);
				listView.setAdapter(mAdapter);
				break;
			default:
				showMonthlyView(false);//隐藏月费提示
				break;
			}
		}else{
			showMonthlyView(false);//隐藏月费提示
		}
		
		//根据站点设置背景
		if (siteManager != null) {
			switch (siteManager.GetWebSite().getSiteColor()) {
			case R.color.theme_actionbar_bg_cd:
				header.setBackgroundResource(R.drawable.round_top_rectangle_cd_theme_2dp);
				btnSubscribe.setButtonBackground(mContext.getResources().getColor(R.color.theme_actionbar_bg_cd));
				break;
			case R.color.them_actionbar_bg_cl:
				header.setBackgroundResource(R.drawable.round_top_rectangle_cl_theme_2dp);
				btnSubscribe.setButtonBackground(mContext.getResources().getColor(R.color.them_actionbar_bg_cl));
				break;
			case R.color.them_actionbar_bg_ida:
				header.setBackgroundResource(R.drawable.round_top_rectangle_ida_theme_2dp);
				btnSubscribe.setButtonBackground(mContext.getResources().getColor(R.color.them_actionbar_bg_ida));
				break;
			case R.color.them_actionbar_bg_ld:
				header.setBackgroundResource(R.drawable.round_top_rectangle_ld_theme_2dp);
				btnSubscribe.setButtonBackground(mContext.getResources().getColor(R.color.them_actionbar_bg_ld));
				break;
			default: // default is blue color
				break;
			}
		}
	}

	/**
	 * @param isMonthly
	 * 	显示月费控件
	 */
	private void showMonthlyView(boolean isMonthly) {
		// TODO Auto-generated method stub
		if(isMonthly){
			cvNormal.setVisibility(View.GONE);
			cvMonthlyFee.setVisibility(View.VISIBLE);
		}else{
			cvNormal.setVisibility(View.VISIBLE);
			cvMonthlyFee.setVisibility(View.GONE);
		}
	}

	class TipsAdapter extends BaseAdapter {
		
		private String[] tips;

		public TipsAdapter(String[] tips) {
			super();
			this.tips = tips;
			// TODO Auto-generated constructor stub
		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return tips.length;
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return tips[position];
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			if (convertView == null) {
				convertView = LayoutInflater.from(mContext).inflate(R.layout.item_dialog_monthly_fee, null);
			}
			TextView tv_servers = (TextView) convertView.findViewById(R.id.tv_servers);
			tv_servers.setText(tips[position]);
			return convertView;
		}

	}
}
