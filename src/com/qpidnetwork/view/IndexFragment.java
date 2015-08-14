package com.qpidnetwork.view;

import com.qpidnetwork.dating.BaseFragment;

import android.os.Bundle;
import android.support.v4.view.ViewPager.OnPageChangeListener;

/**
 * 带标记的Fragment
 * @author Max.Chiu
 */
public abstract class IndexFragment extends BaseFragment implements OnPageChangeListener {
	
	private int index = -1;
	
	public IndexFragment() {
	}
	
	public IndexFragment(int index) {
		this.index = index;
		
        Bundle bundle = new Bundle();
        bundle.putInt("index", index);
        setArguments(bundle);
	}
	
	public void setIndex(int index) {
		this.index = index;
	}
	
	public int getIndex() {
		return index;
	}
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if( getArguments() != null ) {
        	 setIndex(getArguments().getInt("index"));
        }
    }

}
