package com.qpidnetwork.dating.home;

import com.qpidnetwork.dating.R;
import com.qpidnetwork.framework.util.SystemUtil;

import android.content.Context;
import android.util.DisplayMetrics;

public class HomeHelper {
	
	/*根据设计drawer offset 计算菜单宽度*/
	public static int getMenuWidth(Context context){
		DisplayMetrics dm = SystemUtil.getDisplayMetrics(context);
		return (dm.widthPixels - (int)context.getResources().getDimension(R.dimen.home_drawer_offset));
	}
}
