package com.qpidnetwork.dating.home;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;

public class MenuHelper {
	
	private Context mContext;
	private Map<MenuType, Integer> menuMatcher;
	private List<MenuItemBean> mMenuList;
	private MenuItemAdapter menuAdapter;
	
	public enum MenuType{
		MENU_QUICK_MATCH,
		MENU_MAIL_BOX,
		MENU_LOVE_CALLS,
		MENU_MY_ADMIRERS,
		MENU_MY_CONTACTS,
		MENU_SETTINGS,
		MENU_HELPS
	};
	
	public class MenuItemBean{
		public int iconResId;
		public String menuDesc;
		public int unreadCount;
	}
	
	public MenuHelper(Context context){
		mContext = context;
		menuMatcher = new HashMap<MenuType, Integer>();
		mMenuList = new ArrayList<MenuItemBean>();
	}
	
	/**
	 * 添加菜单
	 * @param menuType 菜单类型
	 * @param iconResId 菜单Icon资源Id（无效时默认隐藏）
	 * @param menuDesc 菜单名
	 * @param unreadCount 右侧未读提示，不显示给0
	 * @return 添加菜单是否成功
	 */
	public boolean addMenuItem(MenuType menuType, int iconResId, String menuDesc, int unreadCount){
		
		if(!menuMatcher.containsKey(menuType)){
			MenuItemBean item = new MenuItemBean();
			item.iconResId = iconResId;
			item.menuDesc = menuDesc;
			item.unreadCount = unreadCount;
			mMenuList.add(item);
			menuMatcher.put(menuType, (mMenuList.size()-1));
		}else{
			//已经存在，添加失败
			return false;
		}
		
		return true;
	}
	
	public boolean addMenuItem2(int id, int iconResId, String menuDesc, int unreadCount){
		
			MenuItemBean item = new MenuItemBean();
			item.iconResId = iconResId;
			item.menuDesc = menuDesc;
			item.unreadCount = unreadCount;
			mMenuList.add(item);		
		return true;
	}
	
	
	public MenuItemAdapter create(){
		if(menuAdapter == null){
			menuAdapter = new MenuItemAdapter(mContext, mMenuList);
		}
		return menuAdapter;
	}
	
	/**
	 * 更新指定menu未读条数显示
	 * @param menuType
	 * @param unreadCount
	 */
	public void updateMenuItem(MenuType menuType, int unreadCount){
		if(menuMatcher.containsKey(menuType)){
			int position = menuMatcher.get(menuType);
			if((position < mMenuList.size())&&(menuAdapter != null)){
				menuAdapter.update(position, unreadCount);
			}
		}
	}
	
	/**
	 * 获取指定菜单的属性消息
	 * @param menuType
	 * @return
	 */
	public MenuItemBean getMenuItemByType(MenuType menuType){
		MenuItemBean bean = null;
		if(menuMatcher != null && menuMatcher.containsKey(menuType)){
			int position = menuMatcher.get(menuType);
			if(position < mMenuList.size()){
				bean = mMenuList.get(position);
			}
		}
		return bean;
	}
}
