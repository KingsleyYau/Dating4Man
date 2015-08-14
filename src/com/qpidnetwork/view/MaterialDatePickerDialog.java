package com.qpidnetwork.view;

import com.qpidnetwork.dating.R;
import com.qpidnetwork.framework.base.BaseDialog;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.view.View.OnClickListener;


public class MaterialDatePickerDialog extends BaseDialog implements OnClickListener{
	
	private float density = this.getContext().getResources().getDisplayMetrics().density;
	
	private Context mContext;
	DateSelectCallback dateSelectCallback;
	
	private LinearLayout holder;
	private DatePicker datePicker;
	private Button cancel;
	private Button OK;

	public MaterialDatePickerDialog(Context context, DateSelectCallback callback,int year, int month, int day){
		super(context);
		mContext = context;
		this.dateSelectCallback = callback;
		this.setCancelable(true);
		this.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
		setContentView(createView(year, month, day));
		
	}
	
	
	private View createView(int year, int month, int day){
		

    	LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(this.getDialogSize(), LinearLayout.LayoutParams.WRAP_CONTENT);
		
		View view = LayoutInflater.from(mContext).inflate(R.layout.view_material_datepicker, null);
		
		holder = (LinearLayout)view.findViewById(R.id.holder);
		holder.setLayoutParams(params);
		
		datePicker = (DatePicker)view.findViewById(R.id.date_picker);
		cancel = (Button)view.findViewById(R.id.btn_cancel);
		OK = (Button)view.findViewById(R.id.btn_ok);
		
		datePicker.updateDate(year, month - 1, day);
		
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
			if (dateSelectCallback != null){
				dateSelectCallback.onDateSelected(datePicker.getYear(), datePicker.getMonth(), datePicker.getDayOfMonth());
			}
		}
	}
	
	public void setCallback(DateSelectCallback callback){
		this.dateSelectCallback = callback;
	}
	
	public interface DateSelectCallback{
		public void onDateSelected(int year, int month, int day);
	}


	
	/*public DatePicker datePicker;
	public TextView textViewConfirm;
	public int mYear = 1970;
	public int mMonth = 1;
	public int mDay = 1;
	
	public ChooseBirthdayDialog(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}
    
    public ChooseBirthdayDialog(Context context, int theme) {
        super(context, theme);
    }
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.dialog_choose_birthday);
        
        datePicker = (DatePicker) findViewById(R.id.datePicker);
        textViewConfirm = (TextView) findViewById(R.id.textViewConfirm);
        
        Calendar calendar = Calendar.getInstance();
        if( android.os.Build.VERSION.SDK_INT > 10 ) {
        	datePicker.setMaxDate(calendar.getTimeInMillis());
        }
        
        datePicker.init(mYear, mMonth - 1, mDay, new OnDateChangedListener() {
			@Override
			public void onDateChanged(DatePicker view, int year, int month, int day) {
				mYear = year;
				mMonth = month;
				mDay = day;
			}
		});
        
    }*/
}
