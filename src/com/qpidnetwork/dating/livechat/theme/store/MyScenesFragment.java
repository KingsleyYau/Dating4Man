package com.qpidnetwork.dating.livechat.theme.store;

import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.qpidnetwork.dating.R;
import com.qpidnetwork.framework.base.BaseSortedGridViewFragment;
import com.qpidnetwork.framework.widget.stickygridheaders.StickyGridHeadersGridView;
import com.qpidnetwork.livechat.LiveChatManager;
import com.qpidnetwork.livechat.LiveChatManagerThemeListener;
import com.qpidnetwork.livechat.jni.LCPaidThemeInfo;
import com.qpidnetwork.livechat.jni.LiveChatClientListener.LiveChatErrType;
import com.qpidnetwork.manager.ThemeConfigManager;
import com.qpidnetwork.request.item.ThemeItem;

public class MyScenesFragment extends BaseSortedGridViewFragment implements LiveChatManagerThemeListener{
	
	private static final int GET_MY_SCENE_CALLBACK = 1;
	
	private StickyGridHeadersGridView sgvTheme;// 带标题排列的gridview
	private MyScenesAdapter mMyScenesAdapter;
	
	private LiveChatManager mLiveChatManager;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		mLiveChatManager = LiveChatManager.getInstance();
		mLiveChatManager.RegisterThemeListener(this);
		return super.onCreateView(inflater, container, savedInstanceState);
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
		if(!mLiveChatManager.GetAllPaidTheme()){
			sgvTheme = getSortedGridView();
			updateView(new ArrayList<String>(), 
					new ArrayList<List<ThemeItem>>(), new ArrayList<ThemeItem>());// 获取主题数据后填充到view中
			onRefreshComplete(false);
		}
	}
	
	@Override
	protected void handleUiMessage(Message msg) {
		// TODO Auto-generated method stub
		super.handleUiMessage(msg);
		switch (msg.what) {
		case GET_MY_SCENE_CALLBACK:{
			List<String> womanIds = new ArrayList<String>();
			List<List<ThemeItem>> womanIdThemes = new ArrayList<List<ThemeItem>>();
			List<ThemeItem> themeList = new ArrayList<ThemeItem>();
			if (msg.arg1 == 1) {// 请求成功
				LCPaidThemeInfo[] paidThemeList = (LCPaidThemeInfo[])msg.obj;
				if(paidThemeList != null && paidThemeList.length > 0){
					for (LCPaidThemeInfo info : paidThemeList) {
						if (!womanIds.contains(info.womanId)) {
							womanIds.add(info.womanId);
						}
					}
					for (String womanId : womanIds) {// 筛选主题分类下的item集合
						List<ThemeItem> tempList = new ArrayList<ThemeItem>();
						for (LCPaidThemeInfo info : paidThemeList) {
							if (info.womanId.equals(womanId)){
								ThemeItem item = ThemeConfigManager.newInstance().getThemeItemByThemeId(info.themeId);
								if(item != null){
									tempList.add(item);
								}
							}
						}
						if (tempList != null && tempList.size() > 0)
							womanIdThemes.add(tempList);
					}
					for(List<ThemeItem> womanThemeList: womanIdThemes){
						themeList.addAll(womanThemeList);
					}
				}
				setEmptyMessage(getResources().getString(R.string.livechat_theme_nothemes_tips));
			}else{
				setEmptyMessage("");
			}
			updateView(womanIds, womanIdThemes, themeList);// 获取主题数据后填充到view中
			onRefreshComplete(msg.arg1 == 1? true:false);
		}break;

		default:
			break;
		}
	}
	
	/**
	 * 更新UI
	 */
	private void updateView(List<String> womanIds, List<List<ThemeItem>> womanIdThemes, List<ThemeItem> themeList) {
		// TODO Auto-generated method stub
		mMyScenesAdapter = new MyScenesAdapter(mContext, womanIds, womanIdThemes, themeList);
		sgvTheme.setAdapter(mMyScenesAdapter);
	}
	
	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		mLiveChatManager.UnregisterThemeListener(this);
	}

	/*********************  Theme relate callback *****************************/
	@Override
	public void OnGetPaidTheme(LiveChatErrType errType, String errmsg,
			String userId, LCPaidThemeInfo[] paidThemeList) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void OnGetAllPaidTheme(boolean isSuccess, String errmsg,
			LCPaidThemeInfo[] paidThemeList, ThemeItem[] themeList) {
		Message msg = Message.obtain();
		msg.what = GET_MY_SCENE_CALLBACK;
		msg.arg1 = isSuccess ? 1 : 0;
		msg.obj = paidThemeList;
		sendUiMessage(msg);
		
	}

	@Override
	public void OnManFeeTheme(LiveChatErrType errType, String womanId,
			String themeId, String errmsg, LCPaidThemeInfo paidThemeInfo) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void OnManApplyTheme(LiveChatErrType errType, String womanId,
			String themeId, String errmsg, LCPaidThemeInfo paidThemeInfo) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void OnPlayThemeMotion(LiveChatErrType errType, String errmsg,
			String womanId, String themeId) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void OnRecvThemeMotion(String themeId, String manId, String womanId) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void OnRecvThemeRecommend(String themeId, String manId,
			String womanId) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onThemeDownloadUpdate(String themeId, int progress) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onThemeDownloadFinish(boolean isSuccess, String themeId,
			String sourceDir) {
		// TODO Auto-generated method stub
		
	}

}
