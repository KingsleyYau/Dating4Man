package com.qpidnetwork.view;

import android.content.Context;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;

import com.qpidnetwork.dating.R;
import com.qpidnetwork.dating.livechat.downloader.MagicIconTypeDownloader;
import com.qpidnetwork.framework.util.SystemUtil;
import com.qpidnetwork.framework.util.UnitConversion;
import com.qpidnetwork.manager.FileCacheManager;

public class HorizontalScrollTabbar extends HorizontalScrollView {

	private Context mContext;
	private RadioGroup rg_nav_content;
	private View indicator;

	private int itemWidth = 0;
	private int currPosition = 0;
	private int widowWidth = 0;

	private OnHorizontalScrollTitleBarSelected mOnHorizontalScrollTitleBarSelected;

	public HorizontalScrollTabbar(Context context) {
		super(context);
		initView(context);
	}

	public HorizontalScrollTabbar(Context context, AttributeSet attrs) {
		super(context, attrs);
		initView(context);
	}

	private void initView(Context context) {
		this.mContext = context;

		DisplayMetrics dm = SystemUtil.getDisplayMetrics(context);
		widowWidth = dm.widthPixels;

		View view = LayoutInflater.from(context).inflate(
				R.layout.view_horizontal_title_bar, null);
		rg_nav_content = (RadioGroup) view.findViewById(R.id.rg_nav_content);
		// rg_nav_content.setOnCheckedChangeListener(new
		// OnCheckedChangeListener() {
		//
		// @Override
		// public void onCheckedChanged(RadioGroup group, int checkedId) {
		// // TODO Auto-generated method stub
		// Toast.makeText(mContext, ""+checkedId, Toast.LENGTH_SHORT).show();
		// }
		// });
		indicator = (View) view.findViewById(R.id.indicator);
		addView(view, new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.MATCH_PARENT));
	}

	/**
	 * 初始化titlebar
	 * 
	 * @param title
	 *            标题数组（以此判断title数目）
	 * @param currPosition
	 *            当前选中按钮
	 * @param itemWidth
	 *            单Item宽度
	 */
	public void setParams(String[] icons, int currPosition, int itemWidth) {
		this.itemWidth = itemWidth;
		// 先清除所有子，防止重复添加
		rg_nav_content.removeAllViews();
		createLocalItem();//添加本地TabItem
		if (icons != null) {
			for (int i = 0; i < icons.length; i++) {
				rg_nav_content.addView(createTitleItem(i, icons[i]));
			}
		}
		indicator.setLayoutParams(new FrameLayout.LayoutParams(itemWidth,
				UnitConversion.dip2px(mContext, 2)));
		setSelected(currPosition);
		initListener();
	}

	private void initListener() {
		rg_nav_content
				.setOnCheckedChangeListener(new OnCheckedChangeListener() {

					@Override
					public void onCheckedChanged(RadioGroup group, int checkedId) {
						if (checkedId != currPosition) {
							setSelected(checkedId);
						}
					}
				});
	}

	/**
	 * 创建单个菜单
	 * 
	 * @param position
	 * @param title
	 * @return
	 */
	private View createTitleItem(final int position, String iconsUrl) {
		String localPath = FileCacheManager.getInstance().CacheImagePathFromUrl(iconsUrl);//获取本地缓存路径
		final ImageView ivTab = new ImageView(mContext);
		ivTab.setId(position);
		ivTab.setLayoutParams(new LayoutParams(itemWidth,itemWidth*3/5));
		MagicIconTypeDownloader downloader = new MagicIconTypeDownloader(mContext);
		downloader.SetDefaultImage(mContext.getResources().getDrawable(R.drawable.ic_tag_faces_grey600_24dp));
		downloader.DisplayImage(ivTab, iconsUrl, localPath, null);//为控件装载图片
		ivTab.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				setSelected(position+2);//本地有两个tab
			}
		});
		return ivTab;
	}
	
	/**
	 * 创建本地TABItem 历史表情+本地小表情
	 */
	private void createLocalItem(){
		ImageView ivHistory = (ImageView) LayoutInflater.from(mContext).inflate(R.layout.item_horizontal_scroll_title, null);
		ivHistory.setImageResource(R.drawable.ic_history_grey600_24dp);
		ivHistory.setLayoutParams(new LayoutParams(itemWidth,LayoutParams.WRAP_CONTENT));
		ivHistory.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				setSelected(0);
			}
		});
		rg_nav_content.addView(ivHistory);
		ImageView ivLocal = (ImageView) LayoutInflater.from(mContext).inflate(R.layout.item_horizontal_scroll_title, null);
		ivLocal.setImageResource(R.drawable.ic_tag_faces_grey600_24dp);
		ivLocal.setLayoutParams(new LayoutParams(itemWidth,LayoutParams.WRAP_CONTENT));
		ivLocal.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				setSelected(1);
			}
		});
		rg_nav_content.addView(ivLocal);
	}

	/**
	 * 控制当前选中按钮
	 * 
	 * @param position
	 */
	public void setSelected(int position) {
		this.currPosition = position;
		View child = rg_nav_content.getChildAt(position);
		// if (child != null) {
		// ((RadioButton) child).setChecked(true);
		// }

		/* 移动指示图标 */
		FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) indicator
				.getLayoutParams();
		params.leftMargin = itemWidth * position;
		indicator.setLayoutParams(params);

		int needScroll = (position + 1) * itemWidth - widowWidth;
		if (needScroll > 0) {
			scrollTo(needScroll, 0);
		} else {
			scrollTo(0, 0);
		}

		if (mOnHorizontalScrollTitleBarSelected != null) {
			mOnHorizontalScrollTitleBarSelected.onTitleBarSelected(position);
		}
	}
	
	/**
	 * 获取当前位置子View
	 * @param position
	 * @return
	 */
	public View getChild(int position){
		View child = null;
		if(rg_nav_content != null && rg_nav_content.getChildCount() > position){
			child = rg_nav_content.getChildAt(position);
		}
		return child;
	}

	public void setOnHorizontalScrollTitleBarSelected(
			OnHorizontalScrollTitleBarSelected listener) {
		mOnHorizontalScrollTitleBarSelected = listener;
	}

	public interface OnHorizontalScrollTitleBarSelected {
		public void onTitleBarSelected(int position);
	}
}
