package com.qpidnetwork.view;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TimePicker;

import com.qpidnetwork.dating.R;
import com.qpidnetwork.framework.base.BaseDialog;

public class MaterialTimePickerDialog extends BaseDialog implements OnClickListener{
	
	private Context mContext;
	TimeSelectCallback timeSelectCallback;
	
	private LinearLayout holder;
	private TimePicker timePicker;
	private Button cancel;
	private Button OK;

	public MaterialTimePickerDialog(Context context, TimeSelectCallback callback, int hour, int minute){
		super(context);
		mContext = context;
		this.timeSelectCallback = callback;
		this.setCancelable(true);
		this.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
		setContentView(createView(hour, minute));
		
	}
	
	
	private View createView(int hour, int minute){
		

    	LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(this.getDialogSize(), LinearLayout.LayoutParams.WRAP_CONTENT);
		
		View view = LayoutInflater.from(mContext).inflate(R.layout.view_material_timepicker, null);
		
		holder = (LinearLayout)view.findViewById(R.id.holder);
		holder.setLayoutParams(params);
		
		timePicker = (TimePicker)view.findViewById(R.id.time_picker);
		cancel = (Button)view.findViewById(R.id.btn_cancel);
		OK = (Button)view.findViewById(R.id.btn_ok);
		
		timePicker.setIs24HourView(true);
		timePicker.setCurrentHour(hour);
		timePicker.setCurrentMinute(minute);
		
		cancel.setOnClickListener(this);
		OK.setOnClickListener(this);
		
		return view;
	}


	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		dismiss();
		if (v.getId() == R.id.btn_cancel){
			
		}else if(v.getId() == R.id.btn_ok){
			if (timeSelectCallback != null){
				timeSelectCallback.onTimeSelected(timePicker.getCurrentHour(), timePicker.getCurrentMinute());
			}
		}
	}
	
	public void setCallback(TimeSelectCallback callback){
		this.timeSelectCallback = callback;
	}
	
	public interface TimeSelectCallback{
		public void onTimeSelected(int hour, int minute);
	}

}
