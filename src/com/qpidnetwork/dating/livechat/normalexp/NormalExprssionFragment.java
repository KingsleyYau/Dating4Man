package com.qpidnetwork.dating.livechat.normalexp;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.annotation.SuppressLint;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;

import com.qpidnetwork.dating.R;
import com.qpidnetwork.dating.livechat.expression.EmotionLocalFragment;
import com.qpidnetwork.dating.livechat.expression.HighExpressionFragment;
import com.qpidnetwork.dating.livechat.expression.MagicIconHistoryFragment;
import com.qpidnetwork.framework.base.BaseFragment;
import com.qpidnetwork.framework.util.SystemUtil;
import com.qpidnetwork.livechat.LCMagicIconItem;
import com.qpidnetwork.livechat.LCMessageItem;
import com.qpidnetwork.livechat.LiveChatManager;
import com.qpidnetwork.livechat.LiveChatManagerMagicIconListener;
import com.qpidnetwork.livechat.jni.LiveChatClientListener.LiveChatErrType;
import com.qpidnetwork.manager.FileCacheManager;
import com.qpidnetwork.manager.WebSiteManager;
import com.qpidnetwork.request.item.MagicIconConfig;
import com.qpidnetwork.request.item.MagicIconItem;
import com.qpidnetwork.request.item.MagicIconType;
import com.qpidnetwork.view.HorizontalScrollTabbar;
import com.qpidnetwork.view.HorizontalScrollTabbar.OnHorizontalScrollTitleBarSelected;

/**
 * @author Yanni
 * 
 * @version 2016-4-12
 */
public class NormalExprssionFragment extends BaseFragment implements
		LiveChatManagerMagicIconListener, OnPageChangeListener,
		OnHorizontalScrollTitleBarSelected {

	private static final int GET_MAGICICON_CALLBACK = 1;

	private HorizontalScrollTabbar mTitlebar;// 水平导航TAB
	private ViewPager mViewPager;

	private MagicIconConfig mMagicIconConfig;//小高表配置
	private LiveChatManager mLiveChatManager;
	
	private LivechatMagicIconAdapter mViewPagerAdapter;
	
	private String[] mTypeUrlArray;
	
	private RadioGroup rgColorFilerBody;
	private ImageView ivColorFilter;

	@SuppressLint("InflateParams")
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.viewstub_expression, null);
		mTitlebar = (HorizontalScrollTabbar) view.findViewById(R.id.titleBar);
		mViewPager = (ViewPager) view.findViewById(R.id.viewPager);
		ivColorFilter = (ImageView)view.findViewById(R.id.ivColorFilter);
		rgColorFilerBody = (RadioGroup)view.findViewById(R.id.rdGroup);
		return view;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onActivityCreated(savedInstanceState);
		mViewPager.setOnPageChangeListener(this);
		mTitlebar.setOnHorizontalScrollTitleBarSelected(this);
		
		mLiveChatManager = LiveChatManager.getInstance();
		mLiveChatManager.RegisterMagicIconListener(this);
		mMagicIconConfig = mLiveChatManager.GetMagicIconConfigItem();
		
		if(mMagicIconConfig == null){
			//本地无配置，调用接口获取或更新
			mLiveChatManager.GetMagicIconConfig();
		}
		updateViews();
	}
	
	@Override
	public void onDetach() {
		// TODO Auto-generated method stub
		super.onDetach();
		mLiveChatManager.UnregisterMagicIconListener(this);
	}
	
	private void updateViews(){
		String[] typeArray = null;
		mTypeUrlArray = null;
		MagicIconItem[] magicIconArray = null;
		int tabItemWidth = SystemUtil.getDisplayMetrics(getActivity()).widthPixels / 8;
		if(mMagicIconConfig != null){
			typeArray = getIconTypeList(mMagicIconConfig.magicTypeArray);
			if(typeArray != null){
				mTypeUrlArray = new String[typeArray.length];
				for(int i=0; i<typeArray.length; i ++){
					mTypeUrlArray[i] = getMagicIconTypeUrl(typeArray[i], mMagicIconConfig.path);
				}
			}
			magicIconArray = mMagicIconConfig.magicIconArray;
		}
		mTitlebar.setParams(mTypeUrlArray, 0, tabItemWidth);
		
		//初始化colorfilter
		RadioGroup.LayoutParams params = (RadioGroup.LayoutParams)ivColorFilter.getLayoutParams();
		params.width = tabItemWidth;
		params.height = tabItemWidth*3/5;
		
		mViewPagerAdapter = new LivechatMagicIconAdapter(getChildFragmentManager(), typeArray, magicIconArray);
		mViewPager.setAdapter(mViewPagerAdapter);
		
		mTitlebar.setSelected(1);
	}

	/**
	 * 获取小高表分类ID集合
	 * 
	 * @return
	 */
	private String[] getIconTypeList(MagicIconType[] typeArray) {
		List<String> iconType = new ArrayList<String>();
		if (typeArray != null && typeArray.length > 0) {
			for (MagicIconType type : typeArray) {
				iconType.add(type.id);
			}
		}
		String[] tempArray = new String[iconType.size()];
		iconType.toArray(tempArray);
		return tempArray;
	}

	/**
	 * @param typeId
	 *            表情分类ID
	 * @return 获取指定分类列表下的icon集合
	 */
	private List<MagicIconItem> getIconItemList(String typeId, MagicIconItem[] iconItemList) {
		List<MagicIconItem> itemList = new ArrayList<MagicIconItem>();
		if (iconItemList != null) {
			for (MagicIconItem magicIconItem : iconItemList) {
				if (magicIconItem.typeId.equals(typeId)) {
					itemList.add(magicIconItem);
				}
			}
		}
		return itemList;
	}

	/**
	 * 接受处理Message,更新主线程UI
	 */
	@Override
	protected void handleUiMessage(Message msg) {
		// TODO Auto-generated method stub
		super.handleUiMessage(msg);
//		progress.setVisibility(View.GONE);
		switch (msg.what) {
		case GET_MAGICICON_CALLBACK:
			if (msg.arg1 == 1) {// 请求成功
				MagicIconConfig item = (MagicIconConfig) msg.obj;
				if(item != null){
					if(mMagicIconConfig == null ||
							(mMagicIconConfig != null && mMagicIconConfig != item)){
						//本地无数据或者数据更新时，刷新界面
						mMagicIconConfig = mLiveChatManager.GetMagicIconConfigItem();
						updateViews();
					}
				}
			}
			break;

		default:
			break;
		}
	}

	/**
	 * @param id
	 *            图片的id
	 * @param directory
	 *            不同类型图片的目录
	 * @return 拼接ImageUrl
	 */
	private String getMagicIconTypeUrl(String id, String baseUrl) {
		StringBuffer imgUrl = new StringBuffer();
		if (!TextUtils.isEmpty(baseUrl)) {
			String appSiteHost = WebSiteManager.getInstance().GetWebSite()
					.getWebSiteHost();// 获取服务器地址
			imgUrl.append(appSiteHost).append(baseUrl).append("types")
					.append("/").append(id).append(".png");
		}
		return imgUrl.toString();
	}

	private class LivechatMagicIconAdapter extends FragmentPagerAdapter {

		private String[] typeArray;
		private MagicIconItem[] magicIconArray;
		private HashMap<Integer, WeakReference<Fragment>> mPageReference;

		public LivechatMagicIconAdapter(FragmentManager fm,
				String[] typeList, MagicIconItem[] magicIconArray) {
			super(fm);
			this.typeArray = typeList;
			this.magicIconArray = magicIconArray;
			mPageReference = new HashMap<Integer, WeakReference<Fragment>>();
		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			int count = 2;
			if (typeArray != null) {
				count += typeArray.length;
			}
			return count;
		}

		@Override
		public Fragment getItem(int position) {
			Fragment fragment = null;
			if (mPageReference.containsKey(position)) {
				fragment = mPageReference.get(position).get();
			}
			if (fragment == null) {
				if (position == 0) {
					fragment = new MagicIconHistoryFragment();// 历史表情
				} else if (position == 1) {
					fragment = new EmotionLocalFragment();// 本地小表情
				} else {
					fragment = new HighExpressionFragment(getIconItemList(typeArray[position-2], magicIconArray));// 高级表情
				}
				mPageReference.put(position, new WeakReference<Fragment>(fragment));
			}
			return fragment;
		}

	}

	/**
	 * 表情TAB页切换回调
	 * 
	 * @param position
	 */
	@Override
	public void onTitleBarSelected(int position) {
		// TODO Auto-generated method stub
		mViewPager.setCurrentItem(position);// 同步ViewPager选择页
		onButtonChange(position);
	}
	
	/**
	 * 添加滤镜效果
	 */
	private void onButtonChange(int position){
		for(int i=0; i < 2; i++){
			final View child = mTitlebar.getChild(i);
			if(child != null && child instanceof ImageView){
				if(i == 0){
					((ImageView)child).setImageResource(R.drawable.ic_history_grey600_24dp);
				}else if(i == 1){
					((ImageView)child).setImageResource(R.drawable.ic_tag_faces_grey600_24dp);
				}
			}
		}
		 View currentView = mTitlebar.getChild(position);
		 rgColorFilerBody.setVisibility(View.GONE);
		 if(position == 0){
			 ((ImageView)currentView).setImageResource(R.drawable.ic_history_blue_24dp);
		 }else if(position == 1){
			 ((ImageView)currentView).setImageResource(R.drawable.ic_tag_faces_blue_24dp);
		 }else{
			 rgColorFilerBody.setVisibility(View.VISIBLE);
			 int tabItemWidth = SystemUtil.getDisplayMetrics(getActivity()).widthPixels / 8;
			 RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)rgColorFilerBody.getLayoutParams();
			 params.leftMargin = tabItemWidth*position;
			 
			 String localPath = FileCacheManager.getInstance().CacheImagePathFromUrl(mTypeUrlArray[position-2]);
			 if(!TextUtils.isEmpty(localPath) && (new File(localPath).exists())){
				 ivColorFilter.setImageBitmap(BitmapFactory.decodeFile(localPath)); 
			 }else{
				 ivColorFilter.setImageResource(R.drawable.ic_tag_faces_blue_24dp);
			 }
		 }
	}

	/**
	 * ViewPager页面切换回调
	 * 
	 */
	@Override
	public void onPageSelected(int position) {
		// TODO Auto-generated method stub
		mTitlebar.setSelected(position);// 同步分类导航TAB选中页
	}

	@Override
	public void onPageScrollStateChanged(int arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onPageScrolled(int arg0, float arg1, int arg2) {
		// TODO Auto-generated method stub

	}
	
	//----------- Magic Icon 同步回调刷新 -----------------------
	
	@Override
	public void OnGetMagicIconConfig(boolean success, String errno,
			String errmsg, MagicIconConfig item) {
		Message msg = Message.obtain();
		msg.what = GET_MAGICICON_CALLBACK;
		msg.arg1 = success ? 1 : 0;
		msg.obj = item;
		sendUiMessage(msg);
	}
	
	@Override
	public void OnSendMagicIcon(LiveChatErrType errType, String errmsg,
			LCMessageItem item) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void OnRecvMagicIcon(LCMessageItem item) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void OnGetMagicIconSrcImage(boolean success,
			LCMagicIconItem magicIconItem) {
		// TODO Auto-generated method stub

	}

	@Override
	public void OnGetMagicIconThumbImage(boolean success,
			LCMagicIconItem magicIconItem) {
		// TODO Auto-generated method stub
		
	}

}
