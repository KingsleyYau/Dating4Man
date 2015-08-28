package com.qpidnetwork.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.SpinnerAdapter;

import com.qpidnetwork.dating.R;


public class QpidGallery extends HorizontalScrollView {

	private SpinnerAdapter adapter;
	private AdapterView.OnItemClickListener onItemClickListener;
	private int spacing;
	private LinearLayout container;
	private int gravity;

	public QpidGallery(Context context) {
		super(context);
		init(context);
	}

	public QpidGallery(Context context, AttributeSet attrs) {
		super(context, attrs);
		TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.QpidGallery);
		spacing = (int) ta.getDimension(R.styleable.QpidGallery_spacing, 0);
		gravity = ta.getInteger(R.styleable.QpidGallery_gravity, 0);
		init(context);
	}

	@SuppressLint("RtlHardcoded")
	public QpidGallery(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.QpidGallery);
		spacing = (int) ta.getDimension(R.styleable.QpidGallery_spacing, 0);
		gravity = ta.getInteger(R.styleable.QpidGallery_gravity, Gravity.LEFT);
		init(context);
	}

	@SuppressWarnings("deprecation")
	private void init(Context context) {
		setHorizontalFadingEdgeEnabled(false);
		setHorizontalScrollBarEnabled(false);
		container = new LinearLayout(context);
		FrameLayout.LayoutParams prm = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.FILL_PARENT, FrameLayout.LayoutParams.FILL_PARENT);
		prm.gravity = gravity;

		addView(container, prm);
	}

	public void setAdapter(SpinnerAdapter adapter) {
		this.adapter = adapter;
		int count = adapter.getCount();
		for (int i = 0; i < count; i++) {
			View child = adapter.getView(i, null, this);
			child.setTag(i);
			child.setOnClickListener(childOnClick);
			LinearLayout.LayoutParams prm = (android.widget.LinearLayout.LayoutParams) child.getLayoutParams();
			prm.leftMargin = spacing;
			prm.rightMargin = spacing;
			prm.topMargin = spacing;
			prm.bottomMargin = spacing;
			container.addView(child, prm);
		}
	}

	private View.OnClickListener childOnClick = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			int position = (Integer) v.getTag();
			View child = ((ViewGroup) QpidGallery.this.getChildAt(0)).getChildAt(position);
			// child.setSelected(true);
			setItemSelected(position);
			onItemClickListener.onItemClick(null, child, position, adapter.getItemId(position));
		}
	};

	public void setOnItemClickListener(AdapterView.OnItemClickListener onItemClickListener) {
		this.onItemClickListener = onItemClickListener;
	}

	public void setSpacing(int spacing) {
		this.spacing = spacing;
	}

	/**
	 * 选中某一项
	 * 
	 * @param position
	 */
	public void setItemSelected(int position) {
		for (int i = 0; i < adapter.getCount(); i++) {
			if (i == position) {
				((ViewGroup) QpidGallery.this.getChildAt(0)).getChildAt(i).setSelected(true);
			} else {
				((ViewGroup) QpidGallery.this.getChildAt(0)).getChildAt(i).setSelected(false);
			}
		}
	}

//	/**
//	 * 取消下载任务， Activity退出时调用
//	 */
//	public void cancelLoadTask() {
//		try {
//			ViewGroup container = (ViewGroup) getChildAt(0);
//			int childCount = container.getChildCount();
//			// Log.d("QpidGallery", "count = " + childCount);
//			for (int i = 0; i < childCount; i++) {
//				View child = container.getChildAt(i);
//				RemoteImageView imageView = (RemoteImageView) child.findViewById(R.id.photoImageView);
//				boolean suc = imageView.cancelLoadTask();
//				// Log.d("QpidGallery", "i = " + i + ", suc = " + suc);
//			}
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//	}

}
