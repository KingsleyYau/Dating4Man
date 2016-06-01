package com.qpidnetwork.dating.admirer;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ScrollView;

import com.qpidnetwork.framework.util.Log;

public class ObservableScrollView extends ScrollView {  
	
	private static final String TAG = "ObservableScrollView";
  
    private ScrollViewListener scrollViewListener = null;  
    
    private int lastY = -1;
    private Handler mHandler;
  
    public ObservableScrollView(Context context) {  
        super(context); 
        init();
    }  
  
    public ObservableScrollView(Context context, AttributeSet attrs,  
            int defStyle) {  
        super(context, attrs, defStyle);  
        init();
    }  
  
    public ObservableScrollView(Context context, AttributeSet attrs) {  
        super(context, attrs);
        init();
    }  
    
    private void init(){
    	mHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				if (msg.what == MotionEvent.ACTION_UP) {
					if (lastY == getScrollY()) {
						if (scrollViewListener != null) { 
			    			Log.d(TAG, "ObservableScrollView onScrollComplete");
			                scrollViewListener.onScrollComplete();  
			            }
					} else {
						mHandler.sendMessageDelayed(mHandler.obtainMessage(MotionEvent.ACTION_UP, null), 1);
						lastY = getScrollY();
					}
				}
			}
		};
    }
  
    public void setScrollViewListener(ScrollViewListener scrollViewListener) {  
        this.scrollViewListener = scrollViewListener;  
    }  
  
    @Override  
    protected void onScrollChanged(int x, int y, int oldx, int oldy) {  
        super.onScrollChanged(x, y, oldx, oldy);  
        if (scrollViewListener != null) { 
        	Log.d(TAG, "ObservableScrollView onScrollChanged");
            scrollViewListener.onScrollChanged();  
        }  
    } 
    
    @Override
    public boolean onTouchEvent(MotionEvent ev) {
    	// TODO Auto-generated method stub
    	if(ev.getAction() == MotionEvent.ACTION_UP){
    		mHandler.sendMessageDelayed(mHandler.obtainMessage(MotionEvent.ACTION_UP, null), 5);
    	}else if(ev.getAction() == MotionEvent.ACTION_DOWN){
    		lastY = -1;
    	}
    	return super.onTouchEvent(ev);
    }
  
    public interface ScrollViewListener {  
    	  
        void onScrollChanged();  
        
        void onScrollComplete();
    }
}
