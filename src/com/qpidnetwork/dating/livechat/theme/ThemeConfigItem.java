package com.qpidnetwork.dating.livechat.theme;

import java.util.ArrayList;

import android.graphics.Color;

/**
 * 主题配置item
 * @author Samson Fan
 *
 */
public class ThemeConfigItem {
	// ---------- 背景图定义 ----------
	/**
	 * 背景图(平铺图)位置定义
	 */
	public enum BgLocaType {
		UNKNOW,		// 未知
		TOP,		// 置顶
		BOTTOM,		// 置底
	}
	/**
	 * 背景图(平铺图)item定义
	 */
	public static class BgItem {
		public String mFilePath = "";
		public BgLocaType mLoca = BgLocaType.UNKNOW;
		
		public BgItem() {}
	}
	/**
	 * 生成BgItem
	 */
	public BgItem createBgItem(String filePath, BgLocaType loca) {
		BgItem item = new BgItem();
		item.mFilePath = filePath;
		item.mLoca = loca;
		return item;
	}
	
	// ---------- icon定义 ----------
	/**
	 * icon位置定义
	 */
	public enum IconLocaType {
		UNKNOW,			// 未知
		LEFTTOP,		// 左上角
		LEFTBOTTOM,		// 左下角
		RIGHTTOP,		// 右上角
		RIGHTBOTTOM,	// 右下角
	}
	/**
	 * icon item定义
	 */
	public static class IconItem {
		public String mFilePath = "";
		public IconLocaType mLoca = IconLocaType.UNKNOW;
		
		public IconItem() {}
	}
	/**
	 * 生成IconItem
	 */
	public IconItem createIconItem(String filePath, IconLocaType loca) {
		IconItem item = new IconItem();
		item.mFilePath = filePath;
		item.mLoca = loca;
		return item;
	}
	
	// ---------- 动画定义 ----------
	/**
	 * 动画播放位置定义
	 */
	public enum MotionLocaType {
		UNKNOW,		// 未知
		TOP,		// 置顶
		CENTER,		// 中间
		BOTTOM,		// 置底
	}
	
	// ---------- 主题背景配置参数定义 ----------
	public double mDpi = 3;
	public int mColor = Color.WHITE;
	public ArrayList<BgItem> mBgImageList = new ArrayList<BgItem>();
	public ArrayList<IconItem> mIconList = new ArrayList<IconItem>(); 
	
	// ---------- 主题动画配置参数定义 ----------
	public MotionLocaType mMotionLoca = MotionLocaType.UNKNOW;
	public int mMotionFrame = 0;
	public int mMotionRepeat = 1;
	public ArrayList<String> mMotionFiles = new ArrayList<String>();
}
