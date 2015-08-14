/*******************************************************************************
 * Copyright 2011, 2012 Chris Banes.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package com.qpidnetwork.framework.widget.pulltorefresh;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.util.AttributeSet;
import android.view.View;
import android.widget.GridView;

import com.qpidnetwork.dating.R;
import com.qpidnetwork.framework.widget.pinterest.MultiColumnListView;
import com.qpidnetwork.framework.widget.pulltorefresh.PullToRefreshBase.AnimationStyle;
import com.qpidnetwork.framework.widget.pulltorefresh.PullToRefreshBase.Mode;
import com.qpidnetwork.framework.widget.pulltorefresh.PullToRefreshBase.Orientation;
import com.qpidnetwork.framework.widget.pulltorefresh.internal.EmptyViewMethodAccessor;

/**
 * for custom pinterest to pulltorefresh and load more
 * @author Hunter
 *
 */
public class PullToRefreshGridView2 extends PullToRefreshAdapterViewBase2<MultiColumnListView> {

	public PullToRefreshGridView2(Context context) {
		super(context);
	}

	public PullToRefreshGridView2(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public PullToRefreshGridView2(Context context, Mode mode) {
		super(context, mode);
	}

	public PullToRefreshGridView2(Context context, Mode mode, AnimationStyle style) {
		super(context, mode, style);
	}

	@Override
	public final Orientation getPullToRefreshScrollDirection() {
		return Orientation.VERTICAL;
	}

	@Override
	protected final MultiColumnListView createRefreshableView(Context context, AttributeSet attrs) {
		final MultiColumnListView gv;

		gv = new MultiColumnListView(context, attrs);
		// Use Generated ID (from res/values/ids.xml)
		gv.setId(R.id.gridview);
		return gv;
	}

	class InternalGridView extends GridView implements EmptyViewMethodAccessor {

		public InternalGridView(Context context, AttributeSet attrs) {
			super(context, attrs);
		}

		@Override
		public void setEmptyView(View emptyView) {
			PullToRefreshGridView2.this.setEmptyView(emptyView);
		}

		@Override
		public void setEmptyViewInternal(View emptyView) {
			super.setEmptyView(emptyView);
		}
	}

	@TargetApi(9)
	final class InternalGridViewSDK9 extends InternalGridView {

		public InternalGridViewSDK9(Context context, AttributeSet attrs) {
			super(context, attrs);
		}

		@Override
		protected boolean overScrollBy(int deltaX, int deltaY, int scrollX, int scrollY, int scrollRangeX,
				int scrollRangeY, int maxOverScrollX, int maxOverScrollY, boolean isTouchEvent) {

			final boolean returnValue = super.overScrollBy(deltaX, deltaY, scrollX, scrollY, scrollRangeX,
					scrollRangeY, maxOverScrollX, maxOverScrollY, isTouchEvent);

			// Does all of the hard work...
			OverscrollHelper.overScrollBy(PullToRefreshGridView2.this, deltaX, scrollX, deltaY, scrollY, isTouchEvent);

			return returnValue;
		}
	}
}
