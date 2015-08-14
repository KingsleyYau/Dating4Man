package com.qpidnetwork.dating;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import at.technikum.mti.fancycoverflow.FancyCoverFlow;
import at.technikum.mti.fancycoverflow.FancyCoverFlowAdapter;

import com.qpidnetwork.dating.home.HomeActivity;
import com.qpidnetwork.framework.util.UnitConversion;
import com.qpidnetwork.manager.WebSiteManager;
import com.qpidnetwork.manager.WebSiteManager.WebSiteType;
import com.qpidnetwork.request.OnOtherOnlineCountCallback;
import com.qpidnetwork.request.RequestJniOther;
import com.qpidnetwork.request.item.OtherOnlineCountItem;

/**
 */
public class ChooseSiteActivity extends BaseActivity implements OnPageChangeListener {
	
	/**
	 * 分页适配器
	 */
	private class ChooseSitePagerAdapter extends FancyCoverFlowAdapter {
		
		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return 4;
		}

	    @Override
	    public Integer getItem(int i) {
	        return i;
	    }
	    
	    @Override
	    public long getItemId(int i) {
	        return i;
	    }
	    
	    @SuppressLint("NewApi") @Override
	    public View getCoverFlowItem(final int i, View reuseableView, final ViewGroup viewGroup) {
	    	RelativeLayout layout = null;
	    	ImageView imageView = null;

	        if (reuseableView != null) {
	            layout = (RelativeLayout) reuseableView;
	            imageView = (ImageView) reuseableView.findViewWithTag("imageView");

	        } else {
	        	layout = new RelativeLayout(viewGroup.getContext());
	        	layout.setLayoutParams(
	        			new FancyCoverFlow.LayoutParams(
	            				UnitConversion.dip2px(viewGroup.getContext(), 200 * 3 / 4), 
	            				UnitConversion.dip2px(viewGroup.getContext(), 200)
	            				)
	        			);
	        	
	            imageView = new ImageView(viewGroup.getContext());
	            imageView.setTag("imageView");
	            imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
	            imageView.setLayoutParams(
	            		new RelativeLayout.LayoutParams(
	            				RelativeLayout.LayoutParams.MATCH_PARENT,
	            				RelativeLayout.LayoutParams.MATCH_PARENT
	            				)
	            		);
	            layout.addView(imageView);
	            
	         
	        }
	        
	        imageView.setImageResource(images[i]);


	        return layout;
	    }
	}
	
	private ChooseSitePagerAdapter mAdapter;
	private FancyCoverFlow fancyCoverFlow;
	private TextView textViewOnline;
	public TextView textViewSiteName;
	public TextView textViewSiteDesc;
	
	
	/** CD, CL, IDA, LD **/
	public int[] images = {R.drawable.img_cd_selection, R.drawable.img_cl_selection, R.drawable.img_ida_selection, R.drawable.img_ld_selection};
	public String[] siteName;
	public String[] siteDescrip;
	
	private enum RequestFlag {
		REQUEST_ONLINE_SUCCESS,
		REQUEST_ONLINE_FAIL,
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
		}
		public String errno;
		public String errmsg;
		public OtherOnlineCountItem[] otherOnlineCountItem = null;
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		siteName = WebSiteManager.getInstance().getDefaultSortedSiteNames();
		siteDescrip = WebSiteManager.getInstance().getDefaultSortedSiteDescs();
	}
	
	/**
	 * 初始化界面
	 */
	@Override
	public void InitView() {
		setContentView(R.layout.activity_choose_site);
		
		fancyCoverFlow = (FancyCoverFlow) findViewById(R.id.fancyCoverFlow);
        fancyCoverFlow.setAdapter(new ChooseSitePagerAdapter());
        fancyCoverFlow.setUnselectedAlpha(1.0f);
        fancyCoverFlow.setUnselectedSaturation(0.0f);
        fancyCoverFlow.setUnselectedScale(0.5f);
        fancyCoverFlow.setSpacing(8);
        fancyCoverFlow.setMaxRotation(0);
        fancyCoverFlow.setScaleDownGravity(0.2f);
        fancyCoverFlow.setActionDistance(1);
        fancyCoverFlow.setActionDistance(FancyCoverFlow.ACTION_DISTANCE_AUTO);
        
        textViewSiteName = (TextView) findViewById(R.id.site_name);
        textViewSiteDesc = (TextView) findViewById(R.id.site_description);
        textViewOnline = (TextView) findViewById(R.id.textViewOnline);
        textViewOnline.setVisibility(View.GONE);
        
        fancyCoverFlow.setOnItemClickListener(new OnItemClickListener(){

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				// TODO Auto-generated method stub
				
				if (arg2 != fancyCoverFlow.getSelectedItemId()) return;
				
				final WebSiteManager wm = WebSiteManager.newInstance(ChooseSiteActivity.this);
				switch (arg2) {
				case 0:{
					wm.ChangeWebSite(WebSiteType.CharmDate);
				}break;
				case 1:{
					wm.ChangeWebSite(WebSiteType.ChnLove);
				}break;
				case 2:{
					wm.ChangeWebSite(WebSiteType.IDateAsia);
				}break;
				case 3:{
					wm.ChangeWebSite(WebSiteType.LatamDate);
				}break;
				default:
					break;
				}
				
				Intent intent = new Intent(mContext, HomeActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
				startActivity(intent);
				finish();
			}
        	
        });
        
        fancyCoverFlow.setOnItemSelectedListener(new OnItemSelectedListener(){

			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				textViewSiteName.setText(siteName[arg2]);
				textViewSiteDesc.setText(siteDescrip[arg2]);
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub
				
			}
        	
        });
        
       fancyCoverFlow.setSelection(1);
	}
	
	/**
	 * 初始化事件监听
	 */
	@Override
	public void InitHandler() {
		mHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				// 收起菊花
				hideProgressDialog();
				MessageCallbackItem obj = (MessageCallbackItem) msg.obj;
				switch ( RequestFlag.values()[msg.what] ) {
				case REQUEST_ONLINE_SUCCESS:{
					// 获取站点在线人数成功
//					for(int i = 0; i < obj.otherOnlineCountItem.length; i++) {
//						if( obj.otherOnlineCountItem[i].site == WebSiteManager.newInstance(mContext).GetWebSite().getSiteId() ) {
//							textViewOnline.setText(String.valueOf(obj.otherOnlineCountItem[i].onlineCount));
//							break;
//						}
//					}
				}break;
				default:break;
				}
			}
		};
	}

	@Override
	public void onPageScrollStateChanged(int arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onPageScrolled(int arg0, float arg1, int arg2) {
		// TODO Auto-generated method stub
		
		
	}

	@Override
	public void onPageSelected(int arg0) {
		// TODO Auto-generated method stub
		
	}
	
	/**
	 * 获取站点在线人数
	 */
	public void OnlineCount() {
		RequestJniOther.OnlineCount(
				WebSiteManager.newInstance(this).GetWebSite().getSiteId(), 
				new OnOtherOnlineCountCallback() {
					
					@Override
					public void OnOtherOnlineCount(boolean isSuccess, String errno,
							String errmsg, OtherOnlineCountItem[] item) {
						// TODO Auto-generated method stub
						Message msg = Message.obtain();
						MessageCallbackItem obj = new MessageCallbackItem(errno, errmsg);
						if( isSuccess ) {
							// 获取站点在线人数成功
							msg.what = RequestFlag.REQUEST_ONLINE_SUCCESS.ordinal();
							obj.otherOnlineCountItem = item;
						} else {
							// 获取验证码失败
							msg.what = RequestFlag.REQUEST_ONLINE_FAIL.ordinal();
						}
						msg.obj = obj;
						mHandler.sendMessage(msg);
					}
				});
	}
}
