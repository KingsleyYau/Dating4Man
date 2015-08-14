package com.qpidnetwork.view;

import android.content.Context;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * QuickMatch模块
 * 按长边缩放局顶imageview
 * @author Max.Chiu
 *
 */
public class FitTopImageView extends ImageView {
	
    private int viewWidth, viewHeight;
	private Matrix matrix  = new Matrix();
	
	public FitTopImageView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}
	
	public FitTopImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}
	
	public FitTopImageView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
	}

    
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        /* 获取布局大小 */
        viewWidth = MeasureSpec.getSize(widthMeasureSpec);
        viewHeight = MeasureSpec.getSize(heightMeasureSpec);
        
        /* 居中图片 */
        FitTopImage();
    }
    
    public void FitTopImage() {
        Drawable drawable = getDrawable();
        if (drawable == null || drawable.getIntrinsicWidth() == 0 || drawable.getIntrinsicHeight() == 0)
            return;
        
        int bmWidth = drawable.getIntrinsicWidth();
        int bmHeight = drawable.getIntrinsicHeight();

        matrix.reset();
        
        /* 等比长边放大*/
        float scale = 1.0f;
        float scaleX = (float) viewWidth / (float) bmWidth;
        float scaleY = (float) viewHeight / (float) bmHeight;
        scale = Math.max(scaleX, scaleY);
        matrix.setScale(scale, scale);
        
        setImageMatrix(matrix);
    }
}
