package com.qpidnetwork.view;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.qpidnetwork.dating.R;

public class MaterialDropDownList extends PopupWindow{
	
	public Context context;
	public Callback callback;
	private static int defaultHeight = LayoutParams.WRAP_CONTENT;
	private static int defaultWidth = LayoutParams.MATCH_PARENT;
	private String[] listItem;
	private int selectedItem = 0;
	private ThisAdapter adpt;
	private ListView listView;
	
	
	public MaterialDropDownList(Context context, String[] listItem){
		this(context, listItem,  defaultWidth, defaultHeight);
	}
	
	public MaterialDropDownList(Context context, String[] listItem, int width, int height){
		this(context, listItem,  defaultWidth, defaultHeight, null);
	}
	
	public MaterialDropDownList(Context context, String[] listItem, int width, int height, Callback callback){
		super(context);
		this.context = context;
		this.callback = callback;
		this.listItem = listItem;
		this.setContentView(createContentView());
		this.setFocusable(true);
		this.setTouchable(true);
		this.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
		this.setHeight(defaultHeight);
		this.setWidth(defaultWidth);
		this.setAnimationStyle(R.style.DropDownListAnimation);
	}
	
	
	private View createContentView(){
		View v = LayoutInflater.from(context).inflate(R.layout.view_material_drop_down_list, null);
		listView = (ListView)v.findViewById(R.id.listView);
        
        listView.setOnItemClickListener(new OnItemClickListener(){

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				// TODO Auto-generated method stub
				dismiss();
				selectedItem = arg2;
				if (callback != null) callback.onClick(arg0, arg1, arg2, arg3);
			}
        	
        });
        
        
        
        return v;
	}
	
	public void setSelectedItem(int selectedItem){
		this.selectedItem = selectedItem;
	}
	
	@Override
	public void showAsDropDown(View anchor){
		if (adpt == null) adpt = new ThisAdapter(context, listItem);
		adpt.setSelectedItem(selectedItem);
        listView.setAdapter(adpt);
        super.showAsDropDown(anchor);
	}
	
	
	class ThisAdapter extends ArrayAdapter<ArrayList<String>>{

	    	private String[] mListData;
	    	private Context mContext;
//	    	private ArrayList<View> views;
	    	private int selectedItem = -1;
	    	

	    	public ThisAdapter(Context context, String[] objects) {
	    		super(context, 0);
	    		mListData = objects;
	    		mContext = context;
//	    		views = new ArrayList<View>();
	    	}


	    	@Override
	    	public int getCount() {
	    		// TODO Auto-generated method stub
	    		return mListData.length;
	    	}



	    	@Override
	    	public long getItemId(int position) {
	    		// TODO Auto-generated method stub
	    		return 0;
	    	}

	    	@Override
	    	public View getView(int position, View convertView, ViewGroup parent) {
	    		// TODO Auto-generated method stub
	    		

	    		
	    		float density = mContext.getResources().getDisplayMetrics().density;
	    		int item_height = (int)(48.0f * density);

	    		
	    		AbsListView.LayoutParams params = new AbsListView.LayoutParams(LayoutParams.MATCH_PARENT, item_height);
	    		LinearLayout item = new LinearLayout(mContext);
	    		item.setOrientation(LinearLayout.HORIZONTAL);
	    		item.setLayoutParams(params);
	    		item.setClickable(false);
	    		item.setGravity(Gravity.CENTER);
	    		if (selectedItem == position) item.setBackgroundColor(context.getResources().getColor(R.color.thin_grey));
	    		
	    		View leftIndicator = new View(context);
	    		LinearLayout.LayoutParams lIndicationTp = new LinearLayout.LayoutParams((int)(6.0f * density), item_height);
	    		leftIndicator.setLayoutParams(lIndicationTp);
	    		leftIndicator.setBackgroundColor(context.getResources().getColor(R.color.green));
	    		leftIndicator.setVisibility(View.INVISIBLE);
	    		if (selectedItem == position) leftIndicator.setVisibility(View.VISIBLE);
	    		
	    		TextView text = new TextView(mContext);
	    		LinearLayout.LayoutParams tp = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
	    		tp.gravity = Gravity.CENTER;
	    		text.setPadding((int)((16.0f) * density), 0, 0, 0);
	    		text.setTextSize(18);
	    		text.setTextColor(mContext.getResources().getColor(R.color.text_color_grey));
	    		text.setLayoutParams(tp);
	    		text.setId(android.R.id.text1);
	    		text.setText(mListData[position]);
	    		text.setGravity(Gravity.LEFT|Gravity.CENTER);
	    		text.setClickable(false);
	    		
	    		item.addView(leftIndicator);
	    		item.addView(text);
	    		
	    		
	    		return item;
	    	
	    	
	    	}
	    	
	    	public void setSelectedItem(int selectedItem){
	    		this.selectedItem = selectedItem;
	    	};
	  }
	
	  
	
	public void setCallback(Callback callback){
		this.callback = callback;
	}
	
	public interface Callback{
		public void onClick(AdapterView<?> arg0, View arg1, int arg2, long arg3);
	}

}
