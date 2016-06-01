package com.qpidnetwork.view;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.qpidnetwork.dating.R;
import com.qpidnetwork.dating.credit.BuyCreditActivity;
import com.qpidnetwork.dating.googleanalytics.AnalyticsDialog;
import com.qpidnetwork.framework.util.Log;
import com.qpidnetwork.manager.WebSiteManager;

@SuppressLint("InflateParams")
public class GetMoreCreditDialog extends AnalyticsDialog implements OnClickListener{
	
	private float density = this.getContext().getResources().getDisplayMetrics().density;
	private GridView gridView;
	private RelativeLayout header;
	private TextView tvMore;
	private ArrayList<Map<String, String>> data;
	private String keyCredit = "credit";
	private String keyMoney = "money";
	private String keySPID = "spid";
	private WebSiteManager siteManager;
	
	
	public GetMoreCreditDialog(Context context) {
		super(context);
	}
    
    @SuppressLint("NewApi") public GetMoreCreditDialog(Context context, int theme) {
        super(context, theme);
    }
    
    @SuppressWarnings("deprecation")
	@SuppressLint("NewApi") @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        this.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        this.getWindow().setLayout(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        
        
        Display display = this.getWindow().getWindowManager().getDefaultDisplay();
    	Point size = new Point();
    	
    	if (Build.VERSION.SDK_INT > 12){
    		display.getSize(size);
    	}else{
    		size.y = display.getHeight();
    		size.x = display.getWidth();
    	}
    	
    	
    	int width_times =  Math.round((float)size.x / (56.0f * density));
    	float dialog_width = ((float)(width_times - 1) * 56.0f * density);
        
        this.setContentView(R.layout.dialog_get_credit);
        header = (RelativeLayout)findViewById(R.id.header);
        gridView = (GridView)findViewById(R.id.gridView);
        tvMore = (TextView)findViewById(R.id.tvMore);
        header.setLayoutParams(new LinearLayout.LayoutParams((int)dialog_width, (int)(72.0f * density)));
        
        siteManager = WebSiteManager.getInstance();
        if (siteManager != null){
        	switch (siteManager.GetWebSite().getSiteColor()){
        	case R.color.theme_actionbar_bg_cd:
        		header.setBackgroundResource(R.drawable.round_top_rectangle_cd_theme_2dp);
        		break;
        	case R.color.them_actionbar_bg_cl:
        		header.setBackgroundResource(R.drawable.round_top_rectangle_cl_theme_2dp);
        		break;
        	case R.color.them_actionbar_bg_ida:
        		header.setBackgroundResource(R.drawable.round_top_rectangle_ida_theme_2dp);
        		break;
        	case R.color.them_actionbar_bg_ld:
        		header.setBackgroundResource(R.drawable.round_top_rectangle_ld_theme_2dp);
        		break;
        	default:  //default is blue color
        		break;
        	}
        }
		
        
        
        data = createSelectData();
        ThisAdapter adpt = new ThisAdapter(data);
        gridView.setAdapter(adpt);
        tvMore.setOnClickListener(this);
        
        gridView.setOnItemClickListener(new OnItemClickListener(){

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				// TODO Auto-generated method stub
				Map<String, String> map = data.get(arg2);
				Intent intent = new Intent(mContext, BuyCreditActivity.class);
				intent.putExtra(BuyCreditActivity.CREDIT_ORDER_NUMBER, map.get(keySPID));
				mContext.startActivity(intent);
				
				Log.v(keySPID, map.get(keySPID));
			}
        	
        });
    }
    
    private ArrayList<Map<String, String>> createSelectData(){

    	
    	ArrayList<Map<String, String>> selection = new ArrayList<Map<String, String>>();
    	
    	Map<String, String> map1 = new HashMap<String, String>();
    	map1.put(keyCredit, "8");
    	map1.put(keyMoney, "$52");
    	map1.put(keySPID, "SP002");
    	
    	Map<String, String> map2 = new HashMap<String, String>();
    	map2.put(keyCredit, "16");
    	map2.put(keyMoney, "$96");
    	map2.put(keySPID, "SP003");
    	
    	Map<String, String> map3 = new HashMap<String, String>();
    	map3.put(keyCredit, "32");
    	map3.put(keyMoney, "$179");
    	map3.put(keySPID, "SP004");
    	
    	Map<String, String> map4 = new HashMap<String, String>();
    	map4.put(keyCredit, "60");
    	map4.put(keyMoney, "$299");
    	map4.put(keySPID, "SP005");
    	
    	selection.add(map1);
    	selection.add(map2);
    	selection.add(map3);
    	selection.add(map4);
    	
    	return selection;
    }

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		Intent intent = new Intent(mContext, BuyCreditActivity.class);
		mContext.startActivity(intent);
		dismiss();
	}
	
	
	class ThisAdapter extends BaseAdapter{
		
		private ArrayList<Map<String, String>> data;
		
		public ThisAdapter(ArrayList<Map<String, String>> data){
			this.data = data;
		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return data.size();
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return data.get(position);
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			if (convertView == null){
				convertView = LayoutInflater.from(mContext).inflate(R.layout.item_dialog_get_credit_creditpack, null);
			}
			
			TextView credit = (TextView)convertView.findViewById(R.id.credit);
			TextView money = (TextView)convertView.findViewById(R.id.money);
			Map<String, String> map = data.get(position);
			String stringCredit = map.get(keyCredit);
			String stringMoney = map.get(keyMoney);
			
			credit.setText(stringCredit);
			money.setText(stringMoney);
			
			
			return convertView;
		}
		
	}
}
