package com.qpidnetwork.dating.lady;

import java.util.Random;

import android.content.Context;

import com.qpidnetwork.dating.R;
import com.qpidnetwork.framework.util.SystemUtil;
import com.qpidnetwork.framework.util.UnitConversion;

public class LadyListManager {
	
	private static Random mRandom = new Random();
	
	// 女士列表item高度随机变量
	private static int proTemp = -1;
	private static double[] mRadio = new double[] {
		0.8,
		1.0,
		1.3
	};
	// 女士列表item宽度
	private static int mWidth = 0;
	// 女士列表item背景颜色数组
	private static int[] mBrandingColor = new int[]{
			R.color.brand_color_light11,
			R.color.brand_color_light12,
			R.color.brand_color_light13,
			R.color.brand_color_light14,
			R.color.brand_color_light15,
			R.color.brand_color_light16
	};
	private static int[] mBrandingColorArray = new int[mBrandingColor.length];
	
	/**
	 * 初始化
	 * @param context
	 */
	public static void init(Context context)
	{
		// 初始化女士列表item宽度
		initLadyListItemWidth(context);
		// 初始化女士列表背景颜色数组
		initBackgroundColorArray(context);
	}
	
	/**
	 * 由于数据缺少图片的宽高，需要算法设定图片宽高比（错位实现瀑布流）
	 * @return
	 */
	public static double getRadio(){
		double radio = 0.0;
		
		int temp = mRandom.nextInt(mRadio.length);
		if (proTemp >= 0){
			while(proTemp == temp) {
				temp = mRandom.nextInt(mRadio.length);
			}
		}
		proTemp = temp;
		
		radio = mRadio[temp];
//		Log.d("LadyListManager", "proTemp:%d, temp:%d", proTemp, temp);
		return radio;
	}
	
	/**
	 * 获取女士列表item宽度
	 * @param context
	 * @return
	 */
	public static int getLadyListItemWidth() {
		return mWidth;
	}
	
	/**
	 * 初始化女士列表item宽度
	 * @param context
	 * @return
	 */
	private static void initLadyListItemWidth(Context context) {
		mWidth = (SystemUtil.getDisplayMetrics(context).widthPixels - UnitConversion.dip2px(context, 24))/2;
	}
	
	/**
	 * 获取女士列表item最大高度
	 * @return
	 */
	public static int getMaxLadyListItemHeight() {
		int width = getLadyListItemWidth();
		return (int)((double)width * mRadio[mRadio.length - 1]);
	}
	
	/**
	 * 获取女士列表item高度
	 * @param radio
	 * @return
	 */
	public static int getLadyListItemHeight(double radio) {
		int width = getLadyListItemWidth();
		return (int)((double)width * radio);
	}
	
	/**
	 * 初始化背景颜色数组
	 * @param context
	 * @return
	 */
	private static void initBackgroundColorArray(Context context) {
		for (int i = 0; i < mBrandingColor.length; i++) {
			int color = context.getResources().getColor(mBrandingColor[i]);
			mBrandingColorArray[i] = color;
		}
	}
	
	/**
	 * 获取背景颜色
	 * @return
	 */
	public static int getRandomBackgroundColorType() {
		return mRandom.nextInt(mBrandingColor.length);
	}

	/**
	 * 获取背景颜色数组
	 * @return
	 */
	public static final int[] getBackgroundColorArray() {
		return mBrandingColorArray;
	}
}
