package com.qpidnetwork.view;

import android.graphics.Bitmap;
import android.view.View;
import android.view.View.MeasureSpec;

public class ViewTools {
	public static void PreCalculateViewSize(View view) {
		view.setDrawingCacheEnabled(false);	
		view.measure(MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED), 
				MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
        view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
	}
	
	public static Bitmap ConvertViewToBitmap(View view) {
		view.destroyDrawingCache();
        view.buildDrawingCache();
        Bitmap bitmap = view.getDrawingCache();
        return bitmap;
	}
}
