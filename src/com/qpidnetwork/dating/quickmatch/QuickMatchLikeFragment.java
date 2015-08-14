package com.qpidnetwork.dating.quickmatch;

import java.util.List;

import com.qpidnetwork.dating.R;
import com.qpidnetwork.dating.lady.LadyDetailActivity;
import com.qpidnetwork.dating.quickmatch.QuickMatchManager.OnQueryQuickMatchManagerLikeLadyListCallback;
import com.qpidnetwork.framework.widget.CircleImageView;
import com.qpidnetwork.livechat.jni.LiveChatClientListener.TalkEmfNoticeType;
import com.qpidnetwork.manager.FileCacheManager;
import com.qpidnetwork.request.item.EMFMsgTotalItem;
import com.qpidnetwork.request.item.OtherGetCountItem;
import com.qpidnetwork.request.item.QuickMatchLady;
import com.qpidnetwork.tool.ImageViewLoader;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

/**
 * QuickMatch模块
 * 显示like列表界面
 * @author Max.Chiu
 */
public class QuickMatchLikeFragment extends Fragment {
	private static QuickMatchLikeFragment gFragment = null;
	private ListView mListView = null;
	
	/**
	 * 界面消息
	 */
	private class MessageCallbackItem {
		/**
		 * @param errno				接口错误码
		 * @param errmsg			错误提示
		 */
		public MessageCallbackItem(
				String errno,
				String errmsg
				) {
			this.errno = errno;
			this.errmsg = errmsg;
		}
		public String errno;
		public String errmsg;
		public List<QuickMatchLady> itemList;
		public boolean hasMore;
	}
	
	private class QuickMatchLikeListAdapter extends BaseAdapter implements ListAdapter {
		
		private List<QuickMatchLady> mArrayList = null;
		private LayoutInflater mInflater = null;
		
		public QuickMatchLikeListAdapter(Context context) {
			mInflater = LayoutInflater.from(context);
		}
		
		public void SetList(List<QuickMatchLady> arrayList) {
			mArrayList = arrayList;
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder = null;
			if ( convertView == null ) {
				holder = new ViewHolder();
				convertView = mInflater.inflate(R.layout.list_item_quick_match_like, parent, false);
				holder.textViewTitle = (TextView) convertView.findViewById(R.id.textViewTitle);
				holder.textViewSubTitle = (TextView) convertView.findViewById(R.id.textViewSubTitle);
				holder.imageView = (CircleImageView) convertView.findViewById(R.id.imageView);
				holder.imageDownLoader = null;
				convertView.setTag(holder);
			}else{
				holder = (ViewHolder)convertView.getTag();
			}
			
			if( mArrayList != null ) {
				QuickMatchLady item = mArrayList.get(position);
				holder.textViewTitle.setText(item.firstname);
				holder.textViewSubTitle.setText(String.valueOf(item.age));
				
				if ( null != holder.imageDownLoader ) {
					// 停止回收旧Downloader
					holder.imageDownLoader.ResetImageView();
				}
				if((item.photoURL != null)&&(!item.photoURL.equals(""))){
					String localPath = FileCacheManager.getInstance().CacheImagePathFromUrl(item.photoURL);
					holder.imageDownLoader = new ImageViewLoader(getActivity());
					holder.imageDownLoader.SetDefaultImage(getResources().getDrawable(R.drawable.female_default_profile_photo_40dp));
					holder.imageDownLoader.DisplayImage(holder.imageView, item.photoURL, localPath, null);
				}
			}
			
			return convertView;
		}
		
		public List<QuickMatchLady>  getListData(){
			return mArrayList;
		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			int count = 0;
			if( mArrayList != null ) {
				count = mArrayList.size();
			}
			return count;
		}

		@Override
		public QuickMatchLady getItem(int position) {
			// TODO Auto-generated method stub
			QuickMatchLady item = null;
			if( mArrayList != null ) {
				item = mArrayList.get(position);
			}
			return item;
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return position;
		}
		
		private class ViewHolder{
			public TextView textViewTitle;
			public TextView textViewSubTitle;
			public CircleImageView imageView;
			public ImageViewLoader imageDownLoader;
		}
	}
	
	public static final int REQUEST_FAIL = 0;
	public static final int REQUEST_SUCCESS = 1;
	
	protected Handler mHandler = null;
	
	// 上次获取到最大值
	private int mMaxCount = 10;
	private boolean mHasMore = true;
	private QuickMatchLikeListAdapter mAdapter;
	
	public static QuickMatchLikeFragment newInstance() {
		if( gFragment == null ) {
			gFragment = new QuickMatchLikeFragment();
		}
        return gFragment;
    }
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
                                                                                                                                                                                                                                                                                                                      
        View view = inflater.inflate(R.layout.fragment_quick_match_like, null);
        mListView = (ListView) view.findViewById(R.id.listViewLike);
        mAdapter = new QuickMatchLikeListAdapter(getActivity());
        mListView.setAdapter(mAdapter);
        
        InitHandler();
        
        // 刷新数据
        LoadData(false);
        
        mListView.setOnItemClickListener(new OnItemClickListener(){

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				LadyDetailActivity.launchLadyDetailActivity(getActivity(), mAdapter.getListData().get(arg2).womanid, true);
			}
        	
        });
        
        return view;
    }
    
	/**
	 * 初始化事件监听
	 */
	public void InitHandler() {
		mHandler = new Handler() {
			@Override
			public void handleMessage(android.os.Message msg) {
				// 收起菊花
				MessageCallbackItem obj = (MessageCallbackItem) msg.obj;
				switch ( msg.what ) {
				case REQUEST_SUCCESS:{
					// 获取喜爱的女士列表成功
					// 刷新界面
					// 刷新喜爱女士列表，包含本地喜爱列表
					if( obj != null ) {
						if( obj.itemList != null ) {
							if( obj.itemList.size() > mMaxCount ) {
								mMaxCount = obj.itemList.size();
							}
							mAdapter.SetList(obj.itemList);
						}
						mHasMore = obj.hasMore;
					}
					mAdapter.notifyDataSetChanged();
				} break;
				case REQUEST_FAIL:{
					// 失败
				}break;
				default:break;
				}
			}
		};
	}
	
    public void LoadData(boolean loadMore) {
    	QuickMatchManager.getInstance().QueryLikeLadyList(
    			loadMore, 
    			new OnQueryQuickMatchManagerLikeLadyListCallback() {
			
			@Override
			public void OnQueryLikeLadyList(
					boolean isSuccess,
					String errno, 
					String errmsg, 
					List<QuickMatchLady> itemList, 
					boolean hasMore
					) {
				// TODO Auto-generated method stub
				Message msg = Message.obtain();
				MessageCallbackItem obj = new MessageCallbackItem(errno, errmsg);
				msg.obj = obj;
				if( isSuccess ) {
					// 获取匹配女士列表成功
					msg.what = REQUEST_SUCCESS;
					obj.itemList = itemList;
					obj.hasMore = hasMore;
				} else {
					// 获取匹配女士列表失败
					msg.what = REQUEST_FAIL;
				}
				mHandler.sendMessage(msg);
			}
		});
    }
}
