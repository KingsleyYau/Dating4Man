package com.qpidnetwork.dating.livechat.theme;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.util.DisplayMetrics;

import com.qpidnetwork.dating.livechat.theme.ThemeConfigItem.BgLocaType;
import com.qpidnetwork.framework.util.ImageUtil;
import com.qpidnetwork.framework.util.ImageUtil.TileType;
import com.qpidnetwork.framework.util.StringUtil;
import com.qpidnetwork.framework.util.SystemUtil;
import com.qpidnetwork.framework.util.UnitConversion;

/**
 * 主题图片处理类
 * 
 * @author Samson Fan
 * 
 */
public class ThemeImageHandler implements Runnable {
	/**
	 * 主题图片处理Listener
	 */
	public interface ThemeImageHandlerListener {
		/**
		 * 完成回调
		 * 
		 * @param handler
		 *            handler
		 * @param bgBitmap
		 *            背景图bitmap
		 */
		public void OnFinish(ThemeImageHandler handler, Bitmap bgBitmap);
	}

	/**
	 * 当前屏幕DPI
	 */
	private double mCurrDpi = 3;

	/**
	 * 背景图路径
	 */
	private String mBgPath = "";

	/**
	 * 背景图宽度
	 */
	private int mBgWidth = -1;
	/**
	 * 背景图高度
	 */
	private int mBgHeight = -1;
	/**
	 * 窗口宽度
	 */
	private int mWidowWidth = 0;
	/**
	 * 窗口高度
	 */
	private int mWindowHeight = 0;
	/**
	 * 标题高度
	 */
	private int mTitleHeight = 0;
	/**
	 * 主题配置item
	 */
	private ThemeConfigItem mConfig = null;

	/**
	 * 监听器
	 */
	private ThemeImageHandlerListener mListener = null;

	public ThemeImageHandler() {

	}

	/**
	 * 设置监听器
	 * 
	 * @param listener
	 *            监听器
	 */
	public void SetListener(ThemeImageHandlerListener listener) {
		mListener = listener;
	}

	/**
	 * 加载主题背景图
	 * 
	 * @param context
	 *            context
	 * @param bgPath
	 *            背景图生成路径
	 * @param width
	 *            背景图宽度
	 * @param height
	 *            背景图高度
	 * @param config
	 *            主题配置item
	 * @return
	 */
	public boolean LoadThemeBgImage(Context context, String bgPath, int width,
			int height, ThemeConfigItem config) {
		boolean result = false;

		if (null != context && !StringUtil.isEmpty(bgPath) && width > 0
				&& height > 0 && null != config) {
			mCurrDpi = context.getResources().getDisplayMetrics().density;
			DisplayMetrics dm = SystemUtil.getDisplayMetrics(context);
			mWidowWidth = dm.widthPixels;
			mWindowHeight = dm.heightPixels;
			mTitleHeight = UnitConversion.dip2px(context, 56);
			mConfig = config;
			mBgWidth = width;
			mBgHeight = height;
			mBgPath = bgPath;

			Thread thread = new Thread(this);
			thread.start();
			result = true;
		}

		return result;
	}

	/**
	 * 加载主题背景图处理函数
	 */
	private void LoadThememBgImageProc() {
		// 加载图片
		Bitmap bgBitmap = ImageUtil.loadImageFile(mBgPath);

		// 加载不成功，生成图片
		if (null == bgBitmap) {
			bgBitmap = BuildThemeBgImageProc();
		}

		// callback
		if (null != mListener) {
			mListener.OnFinish(this, bgBitmap);
		}
	}

	/**
	 * 生成主题背景图图片
	 */
	private Bitmap BuildThemeBgImageProc() {
		
		System.out.println("生成图片-------------");
		
		// 计算缩放比例
		float scale = (float) mCurrDpi / (float) mConfig.mDpi;

		// 生成聊天框背景图
		Bitmap bitmap = Bitmap.createBitmap(mBgWidth, mBgHeight,
				Bitmap.Config.ARGB_8888);

		// 画背景颜色
		ImageUtil.setImageColor(bitmap, mConfig.mColor);

		// 绘画background(背景平铺图)
		for (ThemeConfigItem.BgItem bgItem : mConfig.mBgImageList) {
			
			// 获取图片信息
			BitmapFactory.Options bgOptions = ImageUtil.getImageInfoWithFile(bgItem.mFilePath);
			
			if (null != bgOptions && bgOptions.outWidth > 0&& bgOptions.outHeight > 0) {
				
				
				
				// 加载图片
				Bitmap bgBitmap = ImageUtil.preciseScaleBitmap(bgItem.mFilePath, scale);

				// 转换绘画图片位置
				int tileType = -1;
				if (bgItem.mLoca == BgLocaType.TOP) {
					tileType = TileType.Top.ordinal();
				} else if (bgItem.mLoca == BgLocaType.BOTTOM) {
					tileType = TileType.Bottom.ordinal();
				}

				// 绘画图片
				if (tileType >= 0) {
					ImageUtil.tileImage(bitmap, bgBitmap,
							ImageUtil.TileType.values()[tileType]);
				}

				// 释放图片
				bgBitmap.recycle();
			}
		}

		// 绘画icon
		for (ThemeConfigItem.IconItem iconItem : mConfig.mIconList) {
			
			// 获取图片信息
			BitmapFactory.Options iconOptions = ImageUtil.getImageInfoWithFile(iconItem.mFilePath);
			if (null != iconOptions && iconOptions.outWidth > 0
					&& iconOptions.outHeight > 0) {
				// 加载图片
				Bitmap iconBitmap = ImageUtil.preciseScaleBitmap(iconItem.mFilePath, scale);

				// 转换绘画图片位置
				int cornerType = -1;
				if (iconItem.mLoca == ThemeConfigItem.IconLocaType.LEFTTOP) {
					cornerType = ImageUtil.CornerType.LeftTop.ordinal();
				} else if (iconItem.mLoca == ThemeConfigItem.IconLocaType.LEFTBOTTOM) {
					cornerType = ImageUtil.CornerType.LeftBottom.ordinal();
				} else if (iconItem.mLoca == ThemeConfigItem.IconLocaType.RIGHTTOP) {
					cornerType = ImageUtil.CornerType.RightTop.ordinal();
				} else if (iconItem.mLoca == ThemeConfigItem.IconLocaType.RIGHTBOTTOM) {
					cornerType = ImageUtil.CornerType.RightBottom.ordinal();
				}

				// 绘画图片
				if (cornerType >= 0) {
					ImageUtil.cornerImage(bitmap, iconBitmap,
							ImageUtil.CornerType.values()[cornerType]);
				}

				// 释放图片
				iconBitmap.recycle();
			}
		}
		
		// 生成聊天界面背景图
		Bitmap realBitMap = Bitmap.createBitmap(mWidowWidth, mWindowHeight,
				Bitmap.Config.ARGB_8888);

		ImageUtil.setImageColor(realBitMap, Color.WHITE);
		Canvas canvas = new Canvas(realBitMap);
		canvas.drawBitmap(bitmap, 0, mTitleHeight, null);
		canvas.save();
	

		// 保存背景图
		ImageUtil.saveBitmapToFile(mBgPath, realBitMap, CompressFormat.PNG, 100);

		return realBitMap;
	}

	/**
	 * 线程处理函数
	 */
	@Override
	public void run() {
		LoadThememBgImageProc();
	}
}
