package com.qpidnetwork.dating.emf;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.qpidnetwork.dating.R;
import com.qpidnetwork.framework.util.SystemUtil;
import com.qpidnetwork.framework.util.UnitConversion;
import com.qpidnetwork.tool.ImageViewLoader;

public class EMFVirtualGiftItemView extends RelativeLayout {

	private final int ITEM_SPACE = 8;// GridView item 间距 单位dp

	private FrameLayout flContainer;
	public ImageView imageView;
	public TextView textViewTips;
	public TextView textViewPreview;
	private ImageViewLoader mLoader = null;

	public EMFVirtualGiftItemView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
		init(context);
	}

	public EMFVirtualGiftItemView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		init(context);
	}

	private void init(Context context) {
		LayoutInflater.from(context).inflate(
				R.layout.gridview_emf_virtual_gift_item, this, true);
		imageView = (ImageView) findViewById(R.id.imageView);
		textViewTips = (TextView) findViewById(R.id.textViewTips);
		textViewPreview = (TextView) findViewById(R.id.textViewPreview);

		/* 修改GridView Item高度 */
		flContainer = (FrameLayout) findViewById(R.id.flContainer);
		RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) flContainer
				.getLayoutParams();
		params.height = (SystemUtil.getDisplayMetrics(context).widthPixels - UnitConversion
				.dip2px(context, ITEM_SPACE)) / 2;
		flContainer.setLayoutParams(params);
	}

	public void setImageViewLoader(ImageViewLoader loader)
	{
		// 先stop之前下载的
		if (null != mLoader) {
			mLoader.Stop();
		}
		
		mLoader = loader;
	}
}
