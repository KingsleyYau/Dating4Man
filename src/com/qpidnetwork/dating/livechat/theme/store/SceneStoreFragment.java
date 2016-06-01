package com.qpidnetwork.dating.livechat.theme.store;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.os.Bundle;
import android.os.Message;

import com.qpidnetwork.dating.R;
import com.qpidnetwork.dating.authorization.LoginManager;
import com.qpidnetwork.dating.authorization.LoginParam;
import com.qpidnetwork.framework.base.BaseSortedGridViewFragment;
import com.qpidnetwork.framework.widget.stickygridheaders.StickyGridHeadersGridView;
import com.qpidnetwork.manager.ThemeConfigManager;
import com.qpidnetwork.request.OnGetThemeConfigCallback;
import com.qpidnetwork.request.item.ThemeConfig;
import com.qpidnetwork.request.item.ThemeItem;
import com.qpidnetwork.request.item.ThemeTagItem;

/**
 * 主题商店界面
 * @author Hunter
 * @since 2016.5.18
 */
public class SceneStoreFragment extends BaseSortedGridViewFragment implements OnGetThemeConfigCallback{
	
	private static final int GET_THEME_CALLBACK = 1;
	
	private StickyGridHeadersGridView sgvTheme;// 带标题排列的gridview
	private SceneStoreAdapter mSceneStoreAdapter;
	
	private String mWomanId;
	
	public SceneStoreFragment(String womanId) {
		super();
		this.mWomanId = womanId;
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onActivityCreated(savedInstanceState);
		sgvTheme = getSortedGridView();
	}
	
	@Override
	public void onDataNeedRefresh() {
		// TODO Auto-generated method stub
		super.onDataNeedRefresh();
		getThemeConfig();
	}

	/**
	 * 获取主题配置信息
	 */
	private void getThemeConfig() {
		LoginParam params = LoginManager.getInstance().GetLoginParam();
		if(params != null && params.item != null){
			String user_sid = params.item.sessionid;
			String user_id = params.item.manid;
			ThemeConfigManager.newInstance().GetThemeConfig(user_sid, user_id, this);
		}else{
			//加载主题失败
			Message msg = Message.obtain();
			msg.what = GET_THEME_CALLBACK;
			msg.arg1 = 0;
			sendUiMessage(msg);
		}
	}
	
	@Override
	protected void handleUiMessage(Message msg) {
		// TODO Auto-generated method stub
		super.handleUiMessage(msg);
		switch (msg.what) {
		case GET_THEME_CALLBACK:{
			List<ThemeTagItem> TagItemList = new ArrayList<ThemeTagItem>();
			List<List<ThemeItem>> tagList = new ArrayList<List<ThemeItem>>();
			List<ThemeItem> themeList  = new ArrayList<ThemeItem>();
			if (msg.arg1 == 1) {// 请求成功
				ThemeConfig config = (ThemeConfig) msg.obj;
				if(config.themeList.length>0){
					TagItemList = Arrays.asList(config.themeTagList);
	
					for (ThemeTagItem tagItem : config.themeTagList) {// 筛选主题分类下的item集合
						List<ThemeItem> tempList = new ArrayList<ThemeItem>();
						for (ThemeItem themeItem : config.themeList) {
							if (themeItem.tagId.equals(tagItem.tagId))
								tempList.add(themeItem);
						}
						if (tempList != null && tempList.size() > 0)
							tagList.add(tempList);
					}
	
					for (List<ThemeItem> itemList : tagList) {// 根据分类顺序将所有主题item排序
						themeList.addAll(itemList);
					}
					
				}else{
					setEmptyMessage(getResources().getString(R.string.livechat_theme_nothemes_tips));
				}
			}else{
				setEmptyMessage("");
			}
			updateView(TagItemList, tagList, themeList);// 获取主题数据后填充到view中
			onRefreshComplete(msg.arg1 == 1? true:false);
		}break;

		default:
			break;
		}
	}
	
	/**
	 * 更新UI
	 */
	private void updateView(List<ThemeTagItem> TagItemList, List<List<ThemeItem>> tagList, List<ThemeItem> themeList) {
		// TODO Auto-generated method stub
		mSceneStoreAdapter = new SceneStoreAdapter(mContext, TagItemList, tagList,themeList, mWomanId);
		sgvTheme.setAdapter(mSceneStoreAdapter);
	}

	@Override
	public void OnGetThemeConfig(boolean isSuccess, String errno,
			String errmsg, ThemeConfig config) {
		Message msg = Message.obtain();
		msg.what = GET_THEME_CALLBACK;
		msg.arg1 = isSuccess ? 1 : 0;
		msg.obj = config;
		sendUiMessage(msg);		
	}
	
}
