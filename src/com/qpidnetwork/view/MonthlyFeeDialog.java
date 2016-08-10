package com.qpidnetwork.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.text.Html;
import android.view.Display;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.qpidnetwork.dating.R;
import com.qpidnetwork.dating.credit.BuyCreditActivity;
import com.qpidnetwork.dating.credit.MonthlyFeeNotifyAdapter;
import com.qpidnetwork.dating.googleanalytics.AnalyticsDialog;
import com.qpidnetwork.manager.WebSiteManager;
import com.qpidnetwork.request.item.MonthLyFeeTipItem;

@SuppressLint("InflateParams")
public class MonthlyFeeDialog extends AnalyticsDialog implements OnClickListener {

	private float density = this.getContext().getResources().getDisplayMetrics().density;
	private Context mContext;
	private RelativeLayout header;
	private TextView tvPrice;
	private ListView listView;
	private ButtonRaised btnSubscribe;
	
	private MonthLyFeeTipItem mMonthlyFeeTipItem;
	private WebSiteManager siteManager;
	
	private MonthlyFeeNotifyAdapter mAdapter;

	public MonthlyFeeDialog(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
		mContext = context;
		initView();
	}

	@SuppressLint("NewApi")
	public MonthlyFeeDialog(Context context, int theme) {
		super(context, theme);
		mContext = context;
		initView();
	}
	

	@SuppressLint("NewApi")
	protected void initView() {
		// TODO Auto-generated method stub
		siteManager = WebSiteManager.getInstance();
		
		this.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
		this.getWindow().setLayout(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);

		Display display = this.getWindow().getWindowManager().getDefaultDisplay();
		Point size = new Point();

		if (Build.VERSION.SDK_INT > 12) {
			display.getSize(size);
		} else {
			size.y = display.getHeight();
			size.x = display.getWidth();
		}
		
		int width_times =  Math.round((float)size.x / (56.0f * density));
    	float dialog_width = ((float)(width_times - 1) * 56.0f * density);

		this.setContentView(R.layout.dialog_mothly_fee);
		header = (RelativeLayout) findViewById(R.id.header);
		listView = (ListView) findViewById(R.id.listView);
		tvPrice = (TextView) findViewById(R.id.tvPrice);
		btnSubscribe = (ButtonRaised) findViewById(R.id.btnSubscribe);
		btnSubscribe.setOnClickListener(this);
		header.setLayoutParams(new LinearLayout.LayoutParams((int)dialog_width, (int)(72.0f * density)));

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
	 * @param item
	 * 
	 * 设置填充数据
	 */
	public void setData(MonthLyFeeTipItem item){
		this.mMonthlyFeeTipItem = item;
		updateView();//更新UI控件
	}
	
	/**
	 * 更新UI控件
	 */
	private void updateView() {
		// TODO Auto-generated method stub
		tvPrice.setText(Html.fromHtml(mMonthlyFeeTipItem.priceDescribe));
		mAdapter = new MonthlyFeeNotifyAdapter(mContext, mMonthlyFeeTipItem.tips);
		listView.setAdapter(mAdapter);
	}

	public void setPriceTitle(String priceTitle){
		tvPrice.setText(priceTitle+"");
	}


	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		Intent intent = new Intent(mContext, BuyCreditActivity.class);
		mContext.startActivity(intent);
		dismiss();
	}
}
