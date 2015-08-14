package com.qpidnetwork.view;

import com.qpidnetwork.dating.R;
import com.qpidnetwork.framework.util.Log;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

/**
 * 分栏、分页界面所用到的点点点指示
 * 
 * @author Stan Yung
 * 
 */
public class DotsView extends View {

	private static final String tag = "DotView";

	private Paint mPaint;
	private float dotRadius = 2.5f;
	private float dotSpace = 15.0f;
	private int dotCount = 3;
	private int currDot = 0; // 选中的dot
	private int colorNormal, colorSelected;

	public DotsView(Context context) {
		super(context);
		init(context);
	}

	public DotsView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public DotsView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);

	}

	private void init(Context context) {
		mPaint = new Paint();
		mPaint.setAntiAlias(true);
		colorNormal = context.getResources().getColor(R.color.dot_normal);
		colorSelected = context.getResources().getColor(R.color.dot_selected);
	}

	public void setDotRadius(float dotRadius) {
		this.dotRadius = dotRadius;
		invalidate();
	}

	public void setDotSpace(float dotSpace) {
		this.dotSpace = dotSpace;
		invalidate();
	}

	public void setDotCount(int dotCount) {
		this.dotCount = dotCount;
		invalidate();
	}

	/**
	 * 选中哪一个点
	 * 
	 * @param position
	 */
	public void selectDot(int position) {
		currDot = position;
		invalidate();
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		setMeasuredDimension(measureWidth(widthMeasureSpec), measureHeight(heightMeasureSpec));
	}

	@Override
	protected void onDraw(Canvas canvas) {
		int width = getMeasuredWidth();
		int height = getMeasuredHeight();
		float left = (width - (dotRadius * 2 * dotCount + dotSpace * (dotCount - 1))) / 2;
		float top = (height - dotRadius * 2) / 2;
		canvas.save();
		for (int i = 0; i < dotCount; i++) {
			if (i == currDot) {
				// mPaint.setAlpha(50);
				mPaint.setColor(colorSelected);
			} else {
				// mPaint.setAlpha(150);
				mPaint.setColor(colorNormal);
			}
			canvas.drawCircle(left, top, dotRadius, mPaint);
			left += (dotRadius * 2 + dotSpace);
		}
		canvas.restore();
	}

	private int measureWidth(int widthMeasureSpec) {
		int specMode = MeasureSpec.getMode(widthMeasureSpec);
		int specSize = MeasureSpec.getSize(widthMeasureSpec);
		int result = 100;
		if (specMode == MeasureSpec.AT_MOST) {
			Log.d(tag, "measureWidth->AT_MOST");
			result = specSize;
		} else if (specMode == MeasureSpec.EXACTLY) {
			Log.d(tag, "measureWidth->EXACTLY");
			result = specSize;
		}
		return result;
	}

	private int measureHeight(int heightMeasureSpec) {
		int specMode = MeasureSpec.getMode(heightMeasureSpec);
		int specSize = MeasureSpec.getSize(heightMeasureSpec);
		int result = 50;
		if (specMode == MeasureSpec.AT_MOST) {
			Log.d(tag, "measureHeight->AT_MOST");
			result = specSize;
		} else if (specMode == MeasureSpec.EXACTLY) {
			Log.d(tag, "measureHeight->EXACTLY");
			result = specSize;
		}
		return result;
	}

}