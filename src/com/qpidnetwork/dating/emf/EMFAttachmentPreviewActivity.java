package com.qpidnetwork.dating.emf;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.qpidnetwork.dating.R;
import com.qpidnetwork.dating.bean.EMFAttachmentBean;
import com.qpidnetwork.dating.bean.EMFAttachmentBean.AttachType;
import com.qpidnetwork.dating.bean.PrivatePhotoBean;
import com.qpidnetwork.dating.bean.ShortVideoBean;
import com.qpidnetwork.dating.emf.EMFAttachmentPrivatePhotoFragment.OnClickBuy;
import com.qpidnetwork.dating.emf.EMFAttachmentPrivatePhotoFragment.PrivatePhotoDirection;
import com.qpidnetwork.framework.base.BaseFragmentActivity;
import com.qpidnetwork.framework.util.ImageUtil;
import com.qpidnetwork.framework.util.Log;
import com.qpidnetwork.framework.util.UnitConversion;
import com.qpidnetwork.manager.FileCacheManager;
import com.qpidnetwork.manager.VirtualGiftManager;
import com.qpidnetwork.request.OnEMFInboxPhotoFeeCallback;
import com.qpidnetwork.request.OnEMFPrivatePhotoViewCallback;
import com.qpidnetwork.request.RequestErrorCode;
import com.qpidnetwork.request.RequestJni;
import com.qpidnetwork.request.RequestJniEMF.PrivatePhotoType;
import com.qpidnetwork.request.RequestOperator;
import com.qpidnetwork.view.GetMoreCreditDialog;
import com.qpidnetwork.view.IndexFragment;
import com.qpidnetwork.view.ViewPagerFixed;

/**
 * EMF模块 附件浏览
 * 
 * @author Max.Chiu
 */
public class EMFAttachmentPreviewActivity extends BaseFragmentActivity
		implements OnPageChangeListener {
	public static final String NEED_RELOAD = "need_reload";
	/**
	 * 其他界面交互时候参数 已经选择的下标
	 */
	public static final String INDEX = "index";
	public static final String INSERT = "insert";
	public static final String VGTIPS = "virtual_tips";
	public static final String ATTACH_DIRECTION = "attachment_direction";
	private PrivatePhotoDirection privatePhotoDirection;

	// private HashMap<String, Long> requestMap = new HashMap<String, Long>();
	/**
	 * 分页适配器
	 */
	private class EMFAttachmentActivityPagerAdapter extends
			FragmentStatePagerAdapter {
		// 当前显示的
		public List<IndexFragment> mIndexFragment = new LinkedList<IndexFragment>();
		// 普通图片
		public List<IndexFragment> mEMFAttachmentPhotoFragment = new LinkedList<IndexFragment>();
		// 私密照
		public List<IndexFragment> mEMFAttachmentPrivatePhotoragment = new LinkedList<IndexFragment>();
		// 微视频
		public List<IndexFragment> mEMFAttachmentShortVideoFragment = new LinkedList<IndexFragment>();
		// 虚拟礼物
		public List<IndexFragment> mEMFAttachmentVirtualGiftFragment = new LinkedList<IndexFragment>();

		public EMFAttachmentActivityPagerAdapter(FragmentManager fm) {
			super(fm);
			// TODO Auto-generated constructor stub
		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			int count = 0;
			if (mAttachList != null) {
				count = mAttachList.size();
			}
			return count;
		}

		@Override
		public Fragment getItem(final int arg0) {
			// TODO Auto-generated method stub
			IndexFragment fragment = null;
			final EMFAttachmentBean item = mAttachList.get(arg0);

			switch (item.type) {
			case NORAML_PICTURE: {
				// 普通照片
				// 获取已经存在的，并且不显示的

				// 创建新的

				Log.i("hunter", "the url: " + item.photoUrl);
				// 显示
				String localPath = "";
				if (item.photoLocalUrl != null
						&& item.photoLocalUrl.length() > 0) {
					localPath = item.photoLocalUrl;
				} else {
					localPath = FileCacheManager.getInstance()
							.CacheImagePathFromUrl(item.photoUrl);
				}
				// ((EMFAttachmentPhotoFragment)
				// fragment).ReloadData(item.photoUrl, localPath);

				fragment = new EMFAttachmentPhotoFragment(arg0, item.photoUrl,
						localPath);
				mEMFAttachmentPhotoFragment.add(fragment);
				mIndexFragment.add(fragment);

			}
				break;
			case PRIVATE_PHOTO: {
				// 显示私密照
				// 获取已经存在的，并且不显示的

				// 创建新的
				fragment = new EMFAttachmentPrivatePhotoFragment(arg0,
						privatePhotoDirection);
				mEMFAttachmentPrivatePhotoragment.add(fragment);
				mIndexFragment.add(fragment);

				// 设置购买事件
				((EMFAttachmentPrivatePhotoFragment) fragment)
						.SetOnClickBuyListener(new OnClickBuy() {
							@Override
							public void onClickBuy(
									EMFAttachmentPrivatePhotoFragment fragment) {
								// TODO Auto-generated method stub
								// 下载预览大图
								if (item.privatePhoto.photoFee) {
									// 生成缓存路径
									String localPhotoPath = FileCacheManager
											.getInstance()
											.CachePrivatePhotoImagePath(
													item.privatePhoto.sendId,
													item.privatePhoto.photoId,
													PrivatePhotoType.LARGE);

									// 是否已经缓存
									File file = new File(localPhotoPath);
									if (file.exists() && file.isFile()) {
										// 已经下载过, 直接显示
										((EMFAttachmentPrivatePhotoFragment) fragment)
												.ReloadData(
														localPhotoPath,
														item.privatePhoto.photoDesc,
														1);
									} else {
										// 请求接口获取私密照预览图
										PrivatePhotoView(item.privatePhoto,
												arg0, PrivatePhotoType.LARGE);
									}
								} else {
									// 请求付费
									InboxPhotoFee(item.privatePhoto, arg0,
											PrivatePhotoType.LARGE);
								}
							}

							@Override
							public void onClickDownload(
									EMFAttachmentPrivatePhotoFragment fragment) {
								// TODO Auto-generated method stub
								// 下载原图，保存图片到相册
								if (item.privatePhoto.photoFee) {
									// 先获取预览图
									// 生成缓存路径
									String localPhotoPath = FileCacheManager
											.getInstance()
											.CachePrivatePhotoImagePath(
													item.privatePhoto.sendId,
													item.privatePhoto.photoId,
													PrivatePhotoType.LARGE);

									// 是否已经缓存
									File file = new File(localPhotoPath);
									if (!file.exists()) {
										// 请求接口获取私密照预览图
										PrivatePhotoView(item.privatePhoto,
												arg0, PrivatePhotoType.LARGE);
									}

									// 生成缓存路径
									String localPhotoPathOriginal = FileCacheManager
											.getInstance()
											.CachePrivatePhotoImagePath(
													item.privatePhoto.sendId,
													item.privatePhoto.photoId,
													PrivatePhotoType.ORIGINAL);

									// 是否已经缓存
									file = new File(localPhotoPathOriginal);
									if (file.exists() && file.isFile()) {
										// 直接保存到相册
										String fileName = item.privatePhoto.womanid
												+ "-"
												+ System.currentTimeMillis()
												+ ".jpg";
										ImageUtil.SaveImageToGallery(
												fragment.getActivity(),
												localPhotoPath,
												localPhotoPathOriginal,
												fileName, null);
										// if(
										// CanSaveOriginal(localPhotoPathOriginal)
										// ) {
										// // 保存原图
										// SaveImageToGallery(localPhotoPathOriginal,
										// fileName);
										// } else {
										// // 保存大图
										// SaveImageToGallery(localPhotoPath,
										// fileName);
										// }
									} else {
										// 请求接口获取私密照原图
										PrivatePhotoView(item.privatePhoto,
												arg0, PrivatePhotoType.ORIGINAL);
									}
								} else {
									// 请求付费
									InboxPhotoFee(item.privatePhoto, arg0,
											PrivatePhotoType.ORIGINAL);
								}
							}
						});

				// 是否已经购买的私密照
				if (item.privatePhoto.photoFee) {
					// 生成缓存路径
					String localPhotoPath = FileCacheManager.getInstance()
							.CachePrivatePhotoImagePath(
									item.privatePhoto.sendId,
									item.privatePhoto.photoId,
									PrivatePhotoType.LARGE);

					// 是否已经缓存
					File file = new File(localPhotoPath);
					if (file.exists() && file.isFile()) {
						// 已经下载过, 直接显示
						((EMFAttachmentPrivatePhotoFragment) fragment)
								.ReloadData(localPhotoPath,
										item.privatePhoto.photoDesc, 1);
					} else {
						// 请求接口获取私密照
						PrivatePhotoView(item.privatePhoto, arg0,
								PrivatePhotoType.LARGE);
					}
				} else {
					// 没有购买, 清空显示
					((EMFAttachmentPrivatePhotoFragment) fragment).ReloadData(
							"", "", 1);
				}
			}
				break;
			case SHORT_VIDEO: {
				// 微视频
				fragment = new EMFAttachmentShortVideoFragment();
				Bundle bundle = new Bundle();
				bundle.putInt("index", arg0);
				bundle.putParcelable(
						EMFAttachmentShortVideoFragment.SHORT_VIDEO_ITEM,
						item.shortVideo);
				fragment.setArguments(bundle);
				
				mEMFAttachmentShortVideoFragment.add(fragment);
				mIndexFragment.add(fragment);
			}
				break;
			case VIRTUAL_GIFT: {
				// 虚拟礼物
				// 获取已经存在的，并且不显示的

				// 创建新的
				fragment = new EMFAttachmentVirtualGiftFragment(arg0);
				mEMFAttachmentVirtualGiftFragment.add(fragment);
				mIndexFragment.add(fragment);
				((EMFAttachmentVirtualGiftFragment) fragment)
						.SetInsert(mbShowInsert);
				((EMFAttachmentVirtualGiftFragment) fragment)
						.SetTips(mbShowTips);

				// 生成虚拟礼物的视频图片路径
				String photoUrl = VirtualGiftManager.getInstance()
						.GetVirtualGiftImage(item.vgId);
				String localPhotoPath = VirtualGiftManager.getInstance()
						.CacheVirtualGiftImagePath(item.vgId);
				String videoUrl = VirtualGiftManager.getInstance()
						.GetVirtualGiftVideo(item.vgId);
				String localVideoPath = VirtualGiftManager.getInstance()
						.CacheVirtualGiftVideoPath(item.vgId);

				// 虚拟礼物描述
				String format = getResources().getString(
						R.string.emf_virtual_gift_name_credit);
				String name = VirtualGiftManager.getInstance()
						.GetVirtualGiftName(item.vgId);
				String tips = String.format(format, name, 1);

				// 显示
				((EMFAttachmentVirtualGiftFragment) fragment).ReloadData(
						photoUrl, localPhotoPath, videoUrl, localVideoPath,
						tips);

			}
				break;
			default:
				break;
			}

			return fragment;
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			// TODO Auto-generated method stub
			// 清空引用
			Log.i("hunter", "current destroy position: " + position);
			mEMFAttachmentPhotoFragment.remove(object);
			mEMFAttachmentPrivatePhotoragment.remove(object);
			mEMFAttachmentShortVideoFragment.remove(object);
			mEMFAttachmentVirtualGiftFragment.remove(object);
			mIndexFragment.remove(object);

			super.destroyItem(container, position, object);
		}

	}

	private enum RequestFlag {
		REQUEST_BUY_SUCCESS, REQUEST_BUY_FAIL, REQUEST_GET_PHOTO_SUCCESS, REQUEST_GET_PHOTO_FAIL,
	}

	/**
	 * 界面消息
	 */
	private class MessageCallbackItem {
		/**
		 * 
		 * @param errno
		 *            接口错误码
		 * @param errmsg
		 *            错误提示
		 * @param privatePhotoBean
		 */
		public MessageCallbackItem(String errno, String errmsg) {
			this.errno = errno;
			this.errmsg = errmsg;
		}

		public String errno;
		public String errmsg;
		public PrivatePhotoBean privatePhotoBean = null;
		public PrivatePhotoType type;
	}

	private ViewPagerFixed mViewPager;
	private EMFAttachmentActivityPagerAdapter mAdapter;

	private ImageButton buttonCancel;

	private static final String EMF_ATTACHMENT_LIST = "attachmentlist";
	private static final String EMF_ATTACHMENT_CURRE_INDEX = "currIndex";

	private List<EMFAttachmentBean> mAttachList = null;
	private int mCurrIndex = 0;
	private boolean mbShowInsert = false;
	private boolean mbShowTips = true;

	public static Intent getIntent(Context context,
			ArrayList<EMFAttachmentBean> attachment, int currIndex) {
		Intent intent = new Intent(context, EMFAttachmentPreviewActivity.class);
		intent.putParcelableArrayListExtra(EMF_ATTACHMENT_LIST, attachment);
		intent.putExtra(EMF_ATTACHMENT_CURRE_INDEX, currIndex);
		return intent;
	}

	@Override
	protected void onCreate(Bundle arg0) {
		// TODO Auto-generated method stub
		super.onCreate(arg0);

		// 创建界面时候读取数据
		Bundle bundle = getIntent().getExtras();
		if (bundle != null) {
			if (bundle.containsKey(EMF_ATTACHMENT_LIST)) {
				mAttachList = bundle
						.getParcelableArrayList(EMF_ATTACHMENT_LIST);
			}

			if (bundle.containsKey(EMF_ATTACHMENT_CURRE_INDEX)) {
				mCurrIndex = bundle.getInt(EMF_ATTACHMENT_CURRE_INDEX);
			}

			if (bundle.containsKey(INSERT)) {
				mbShowInsert = bundle.getBoolean(INSERT);
			}

			if (bundle.containsKey(VGTIPS)) {
				mbShowTips = bundle.getBoolean(VGTIPS);
			}

			if (bundle.containsKey(ATTACH_DIRECTION)) {
				privatePhotoDirection = PrivatePhotoDirection.valueOf(bundle
						.getString(ATTACH_DIRECTION));
			}
		}

		if (privatePhotoDirection == null)
			privatePhotoDirection = PrivatePhotoDirection.WM;

		// 刷新界面
		ReloadData();

		// 选择默认项
		mViewPager.setCurrentItem(mCurrIndex, true);
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		RequestJni.StopAllRequest();
		super.onDestroy();
	}

	/**
	 * 点击取消
	 * 
	 * @param v
	 */
	public void onClickCancel(View v) {
		finish();
	}

	@SuppressWarnings("deprecation")
	@Override
	public void InitView() {
		// TODO Auto-generated method stub
		this.getWindow().setBackgroundDrawable(new ColorDrawable(Color.BLACK));

		setContentView(R.layout.activity_emf_attachment);

		// 分页控件
		mAdapter = new EMFAttachmentActivityPagerAdapter(
				getSupportFragmentManager());
		mViewPager = (ViewPagerFixed) findViewById(R.id.viewPager);
		mViewPager.setOnPageChangeListener(this);
		mViewPager.setOffscreenPageLimit(1);
		mViewPager.setAdapter(mAdapter);

		buttonCancel = (ImageButton) findViewById(R.id.buttonCancel);

		if (Build.VERSION.SDK_INT >= 21) {
			buttonCancel.getLayoutParams().height = UnitConversion.dip2px(this,
					48);
			buttonCancel.getLayoutParams().width = UnitConversion.dip2px(this,
					48);
			((RelativeLayout.LayoutParams) buttonCancel.getLayoutParams()).topMargin = UnitConversion
					.dip2px(this, 18);
		}
	}

	/**
	 * 刷新界面
	 */
	public void ReloadData() {
		mAdapter.notifyDataSetChanged();
	}

	/**
	 * 获取已经存在的fragment
	 * 
	 * @param list
	 * @param index
	 * @return
	 */
	public IndexFragment GetFragmentInList(List<IndexFragment> list, int index) {
		for (IndexFragment item : list) {
			// if( item.getIndex() == index || item.getIndex() < (index - 1) ||
			// item.getIndex() > (index + 1) ) {
			if (item.getIndex() == index) {
				return item;
			}
		}
		return null;
	}

	/**
	 * 请求购买私密照片
	 */
	public void InboxPhotoFee(final PrivatePhotoBean item, final int index,
			final PrivatePhotoType type) {
		// 此处应有菊花
		showProgressDialog("Loading...");
		RequestOperator.getInstance().InboxPhotoFee(item.womanid, item.photoId,
				item.sendId, item.messageid, new OnEMFInboxPhotoFeeCallback() {

					@Override
					public void OnEMFInboxPhotoFee(boolean isSuccess,
							String errno, String errmsg) {
						// TODO Auto-generated method stub
						Message msg = Message.obtain();
						MessageCallbackItem obj = new MessageCallbackItem(
								errno, errmsg);
						if (isSuccess) {
							// 成功
							msg.what = RequestFlag.REQUEST_BUY_SUCCESS
									.ordinal();
							msg.arg1 = index;
							obj.privatePhotoBean = item;
							obj.type = type;
						} else {
							// 失败
							msg.what = RequestFlag.REQUEST_BUY_FAIL.ordinal();
						}
						msg.obj = obj;
						sendUiMessage(msg);
					}
				});
	}

	/**
	 * 获取已购买的私密照
	 */
	public void PrivatePhotoView(PrivatePhotoBean item, final int index,
			final PrivatePhotoType type) {
		// 此处应有菊花
		showProgressDialog("Loading...");

		// 生成缓存路径
		String localPhotoPath = FileCacheManager.getInstance()
				.CachePrivatePhotoImagePath(item.sendId, item.photoId, type);

		// if( requestMap.containsKey(localPhotoPath) ) {
		// Long request = requestMap.get(localPhotoPath);
		// RequestJni.StopRequest(request);
		// requestMap.remove(localPhotoPath);
		// }

		// long requestId = RequestOperator.getInstance().PrivatePhotoView(
		RequestOperator.getInstance().PrivatePhotoView(item.womanid,
				item.photoId, item.sendId, item.messageid, localPhotoPath,
				type, new OnEMFPrivatePhotoViewCallback() {

					@Override
					public void OnEMFPrivatePhotoView(boolean isSuccess,
							String errno, String errmsg, String filePath) {
						// TODO Auto-generated method stub
						Message msg = Message.obtain();
						MessageCallbackItem obj = new MessageCallbackItem(
								errno, errmsg);
						if (isSuccess) {
							// 成功
							msg.what = RequestFlag.REQUEST_GET_PHOTO_SUCCESS
									.ordinal();
							obj.type = type;
							msg.arg1 = index;
						} else {
							// 失败
							msg.what = RequestFlag.REQUEST_GET_PHOTO_FAIL
									.ordinal();
							;
						}
						msg.obj = obj;
						sendUiMessage(msg);
					}
				});

		// if( requestId != RequestJni.InvalidRequestId ) {
		// requestMap.put(localPhotoPath, requestId);
		// }
	}

	@Override
	protected void handleUiMessage(Message msg) {
		// TODO Auto-generated method stub
		super.handleUiMessage(msg);
		// 收起菊花
		hideProgressDialog();
		MessageCallbackItem callbackItem = (MessageCallbackItem) msg.obj;
		switch (RequestFlag.values()[msg.what]) {
		case REQUEST_BUY_SUCCESS: {
			// 付费私密照成功, 请求获取私密照
			EMFAttachmentBean item = mAttachList.get(msg.arg1);
			item.privatePhoto.photoFee = true;
			Intent intent = getIntent();
			intent.putExtra(NEED_RELOAD, true);
			setResult(RESULT_OK, intent);

			PrivatePhotoView(callbackItem.privatePhotoBean, msg.arg1,
					callbackItem.type);
			if (callbackItem.type == PrivatePhotoType.ORIGINAL) {
				PrivatePhotoView(callbackItem.privatePhotoBean, msg.arg1,
						PrivatePhotoType.LARGE);
			}
		}
			break;
		case REQUEST_BUY_FAIL: {
			// 付费私密照失败, 弹出购买更多credit
			switch (callbackItem.errno) {
			case RequestErrorCode.MBCE62002: {
				// 不够信用点
				final GetMoreCreditDialog dialog = new GetMoreCreditDialog(
						mContext, R.style.ChoosePhotoDialog);
				dialog.show();
			}
				break;
			default: {
				Toast.makeText(mContext, callbackItem.errmsg, Toast.LENGTH_LONG)
						.show();
				break;
			}
			}

		}
			break;
		case REQUEST_GET_PHOTO_SUCCESS: {
			// 获取私密照功

			// 生成缓存路径
			EMFAttachmentBean item = mAttachList.get(msg.arg1);
			item.privatePhoto.photoFee = true;
			String localPhotoPath = FileCacheManager.getInstance()
					.CachePrivatePhotoImagePath(item.privatePhoto.sendId,
							item.privatePhoto.photoId, PrivatePhotoType.LARGE);

			String localPhotoPathOriginal = FileCacheManager.getInstance()
					.CachePrivatePhotoImagePath(item.privatePhoto.sendId,
							item.privatePhoto.photoId,
							PrivatePhotoType.ORIGINAL);

			if (callbackItem.type == PrivatePhotoType.LARGE) {
				// 获取大图, 刷新界面
				EMFAttachmentPrivatePhotoFragment fragment = (EMFAttachmentPrivatePhotoFragment) GetFragmentInList(
						mAdapter.mEMFAttachmentPrivatePhotoragment, msg.arg1);

				if (fragment != null && !fragment.isDetached()) {
					fragment.ReloadData(localPhotoPath,
							item.privatePhoto.photoDesc, 1);
				}
			} else {
				// 获取原图, 下载到相册
				localPhotoPathOriginal = FileCacheManager.getInstance()
						.CachePrivatePhotoImagePath(item.privatePhoto.sendId,
								item.privatePhoto.photoId,
								PrivatePhotoType.ORIGINAL);
				String fileName = item.privatePhoto.womanid + "-"
						+ System.currentTimeMillis() + ".jpg";
				if (ImageUtil.SaveImageToGallery(this, localPhotoPath,
						localPhotoPathOriginal, fileName, null)) {
					Toast.makeText(this,
							getString(R.string.livechat_saved_origional_image),
							Toast.LENGTH_LONG).show();
				}
				// if( CanSaveOriginal(localPhotoPathOriginal) ) {
				// // 保存原图
				// SaveImageToGallery(localPhotoPathOriginal, fileName);
				// } else {
				// // 保存大图
				// SaveImageToGallery(localPhotoPath, fileName);
				// }
			}
		}
			break;
		case REQUEST_GET_PHOTO_FAIL: {
			// 获取私密照功失败
			Toast.makeText(mContext, callbackItem.errmsg, Toast.LENGTH_LONG)
					.show();
		}
			break;
		default:
			break;
		}
	}

	@Override
	public void onPageScrollStateChanged(int arg0) {
		// TODO Auto-generated method stub
		Log.d("EMF.EMFAttachmentPreviewActivity", "onPageScrollStateChanged( "
				+ "arg0 : " + String.valueOf(arg0) + " )");
		for (IndexFragment item : mAdapter.mIndexFragment) {
			item.onPageScrollStateChanged(arg0);
		}
	}

	@Override
	public void onPageScrolled(int arg0, float arg1, int arg2) {
		// TODO Auto-generated method stub
		for (IndexFragment item : mAdapter.mIndexFragment) {
			item.onPageScrolled(arg0, arg1, arg2);
		}
	}

	@Override
	public void onPageSelected(int arg0) {
		// TODO Auto-generated method stub
		Log.d("EMF.EMFAttachmentPreviewActivity", "onPageSelected( "
				+ "arg0 : " + String.valueOf(arg0) + " )");
		for (IndexFragment item : mAdapter.mIndexFragment) {
			item.onPageSelected(arg0);
		}
	}

	/**
	 * 是否有足够内存处理原图
	 * 
	 * @return
	 */
	public boolean CanSaveOriginal(String localPhotoPath) {
		boolean bFlag = true;
		// try {
		// Runtime info = Runtime.getRuntime();
		// long freeSize = info.freeMemory();
		// long totalSize = info.totalMemory();
		//
		// BitmapFactory.Options options = new BitmapFactory.Options();
		// options.inJustDecodeBounds = true;
		// BitmapFactory.decodeFile(localPhotoPath, options);
		// long needSize = options.outHeight * options.outWidth * 4;
		//
		// ActivityManager activityManager = (ActivityManager)
		// mContext.getSystemService(Context.ACTIVITY_SERVICE);
		// int getMemoryClass = activityManager.getMemoryClass();
		//
		// Log.d("Runtime",
		// "needSize : " + needSize + ", " +
		// "freeSize : " + freeSize + ", " +
		// "totalSize : " + totalSize + ", " +
		// "outHeight : " + options.outHeight + ", " +
		// "outWidth : " + options.outWidth + ", " +
		// "inDensity : " + options.inDensity + ", " +
		// "inPreferredConfig : " + options.inPreferredConfig.name() + ", " +
		// "getMemoryClass : " + getMemoryClass
		// );
		//
		// if( freeSize - needSize > 500 * 1024 ) {
		// // > 500k
		// bFlag = true;
		// }
		//
		// } catch (Exception e) {
		// e.printStackTrace();
		// }
		return bFlag;
	}
	
	/**
	 * 视频付费成功跟新数据
	 */
	public void updateVideoFeeStatus(ShortVideoBean shortVideo){
		for(int i=0; i<mAttachList.size(); i++){
			ShortVideoBean tempVideoBean = null;
			if(mAttachList.get(i).type == AttachType.SHORT_VIDEO){
				tempVideoBean =	mAttachList.get(i).shortVideo;
				if(tempVideoBean != null &&
						tempVideoBean.videoId.equals(shortVideo.videoId)){
					tempVideoBean.videoFee = true;
					
					Intent intent = new Intent(EMFDetailActivity.ACTION_SHORT_VIDEO_FEE);
					intent.putExtra(EMFDetailActivity.SHORT_VIDEO_VIDEOID, shortVideo.videoId);
					sendBroadcast(intent);
				}
			}
		}
	}
}
