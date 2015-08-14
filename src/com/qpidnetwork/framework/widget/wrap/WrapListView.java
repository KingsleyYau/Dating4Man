package com.qpidnetwork.framework.widget.wrap;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.RelativeLayout;

public class WrapListView extends RelativeLayout {

	private WrapBaseAdapter myCustomAdapter;
	private static boolean addChildType;
	private int dividerHeight = 0;
	private int dividerWidth = 0;

	private final Handler handler = new Handler(Looper.getMainLooper()) {
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			try {
				if (msg.getData().containsKey("getRefreshThreadHandler")) {
					WrapListView.setAddChildType(false);
					WrapListView.this.myCustomAdapter
							.notifyCustomListView(WrapListView.this);
				}
			} catch (Exception e) {

			}
		}
	};

	public WrapListView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	protected void onLayout(boolean arg0, int argLeft, int argTop,
			int argRight, int argBottom) {
		int count = getChildCount();
		int row = 0;
		int lengthX = 0;
		int lengthY = 0;
		for (int i = 0; i < count; ++i) {
			View child = getChildAt(i);
			int width = child.getMeasuredWidth();
			int height = child.getMeasuredHeight();

			if (lengthX == 0)
				lengthX += width;
			else {
				lengthX += width + getDividerWidth();
			}

			if ((i == 0) && (lengthX <= argRight)) {
				lengthY += height;
			}

			if (lengthX > argRight) {
				lengthX = width;
				lengthY += getDividerHeight() + height;
				++row;
				child.layout(lengthX - width, lengthY - height, lengthX,
						lengthY);
			} else {
				child.layout(lengthX - width, lengthY - height, lengthX,
						lengthY);
			}
		}
		ViewGroup.LayoutParams lp = getLayoutParams();
		lp.height = lengthY;
		setLayoutParams(lp);
		if (isAddChildType())
			new Thread(new RefreshCustomThread()).start();
	}

	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int width = View.MeasureSpec.getSize(widthMeasureSpec);
		int height = View.MeasureSpec.getSize(heightMeasureSpec);
		setMeasuredDimension(width, height);

		for (int i = 0; i < getChildCount(); ++i) {
			View child = getChildAt(i);
			child.measure(0, 0);
		}
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	}

	static final boolean isAddChildType() {
		return addChildType;
	}

	public static void setAddChildType(boolean type) {
		addChildType = type;
	}

	final int getDividerHeight() {
		return this.dividerHeight;
	}

	public void setDividerHeight(int dividerHeight) {
		this.dividerHeight = dividerHeight;
	}

	final int getDividerWidth() {
		return this.dividerWidth;
	}

	public void setDividerWidth(int dividerWidth) {
		this.dividerWidth = dividerWidth;
	}

	public void setAdapter(WrapBaseAdapter adapter) {
		this.myCustomAdapter = adapter;
		setAddChildType(true);
		adapter.notifyCustomListView(this);
	}

	public void setOnItemClickListener(OnItemClickListener listener) {
		this.myCustomAdapter.setOnItemClickListener(listener);
	}

	public void setOnItemLongClickListener(OnItemLongClickListener listener) {
		this.myCustomAdapter.setOnItemLongClickListener(listener);
	}

	private final void sendMsgHanlder(Handler handler, Bundle data) {
		Message msg = handler.obtainMessage();
		msg.setData(data);
		handler.sendMessage(msg);
	}

	private final class RefreshCustomThread implements Runnable {
		private RefreshCustomThread() {

		}

		public void run() {
			Bundle b = new Bundle();
			try {
				Thread.sleep(50L);
			} catch (Exception localException) {
			} finally {
				b.putBoolean("getRefreshThreadHandler", true);
				WrapListView.this.sendMsgHanlder(WrapListView.this.handler,
						b);
			}
		}
	}

	public interface OnItemClickListener {
		public void onItemClick(AdapterView<?> paramAdapterView,
				View paramView, int paramInt, long paramLong);
	}

	public interface OnItemLongClickListener {
		public boolean onItemLongClick(AdapterView<?> paramAdapterView,
				View paramView, int paramInt, long paramLong);
	}
}
