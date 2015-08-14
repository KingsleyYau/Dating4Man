package com.qpidnetwork.dating.livechat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.StyleSpan;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewParent;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.qpidnetwork.dating.R;
import com.qpidnetwork.dating.authorization.LoginManager;
import com.qpidnetwork.dating.authorization.LoginParam;
import com.qpidnetwork.dating.authorization.LoginPerfence;
import com.qpidnetwork.dating.authorization.RegisterActivity;
import com.qpidnetwork.dating.emf.MailEditActivity;
import com.qpidnetwork.dating.livechat.downloader.EmotionPlayImageDownloader2;
import com.qpidnetwork.dating.livechat.downloader.EmotionPlayImageDownloader2.OnEmotionPlayImageDownloadListener;
import com.qpidnetwork.dating.livechat.downloader.LivechatVoiceDownloader;
import com.qpidnetwork.dating.livechat.downloader.PrivatePhotoDownloader;
import com.qpidnetwork.dating.livechat.voice.VoicePlayerManager;
import com.qpidnetwork.framework.util.ImageUtil;
import com.qpidnetwork.framework.util.StringUtil;
import com.qpidnetwork.framework.util.UnitConversion;
import com.qpidnetwork.livechat.LCEmotionItem;
import com.qpidnetwork.livechat.LCMessageItem;
import com.qpidnetwork.livechat.LCMessageItem.MessageType;
import com.qpidnetwork.livechat.LCMessageItem.SendType;
import com.qpidnetwork.livechat.LCMessageItem.StatusType;
import com.qpidnetwork.livechat.LCWarningLinkItem.LinkOptType;
import com.qpidnetwork.livechat.jni.LiveChatClient.UserStatusType;
import com.qpidnetwork.livechat.jni.LiveChatClientListener.LiveChatErrType;
import com.qpidnetwork.livechat.LiveChatManager;
import com.qpidnetwork.request.RequestJniEMF.ReplyType;
import com.qpidnetwork.view.EmotionPlayer;
import com.qpidnetwork.view.GetMoreCreditDialog;
import com.qpidnetwork.view.MaterialDialogAlert;
import com.qpidnetwork.view.MaterialProgressBar;

public class MessageListView extends ScrollLayout implements
		View.OnClickListener, OnEmotionPlayImageDownloadListener {

	private ReadWriteLock lock = new ReentrantReadWriteLock();

	private List<LCMessageItem> beanList = new ArrayList<LCMessageItem>();
	private HashMap<Integer, Integer> mPositionMap = new HashMap<Integer, Integer>();// 消息Id
																						// 与
																						// position索引，方便界面更新及MessgaeItem更新
	private LayoutInflater mLayoutInflater;
	private ExpressionImageGetter imageGetter;/* 表情图片获取 */

	private LiveChatManager mLiveChatManager;
	private Context mContext;

	private VoicePlayerManager mVoicePlayerManager;

	private boolean isAddHistory = false;// 添加历史消息时，针对not enough
											// money处理是只添加列表消息不弹窗
	private boolean isDestroyed = false; // 是否被回收，防止退出时高级表情仍回调导致高表播放，资源无法回收

	private List<LCMessageItem> currVisibleList = new ArrayList<LCMessageItem>();// 记录当前可见的item
	private boolean isScrolled = false;//存储当前列表是否滚动过，解决未滚动情况下收到高级表情，下载完不播放问题

	public MessageListView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public MessageListView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		mContext = context;
		isDestroyed = false;
		mLayoutInflater = LayoutInflater.from(context);
		mLiveChatManager = LiveChatManager.newInstance(null);
		imageGetter = new ExpressionImageGetter(context, UnitConversion.dip2px(
				context, 28), UnitConversion.dip2px(context, 28));
		mVoicePlayerManager = VoicePlayerManager.getInstance(context);
	}

	public void replaceAllRow(List<LCMessageItem> beanList) {
		LCMessageItem[] msgBeanArr = beanList
				.toArray(new LCMessageItem[beanList.size()]);
		lock.writeLock().lock();
		try {
			getContainer().removeAllViews();
			this.beanList.clear();
			this.mPositionMap.clear();
			isAddHistory = true;
			if (beanList != null) {
				for (LCMessageItem bean : msgBeanArr) {
					addRowInternal(bean);
				}
			}
			isAddHistory = false;
		} finally {
			lock.writeLock().unlock();
		}
	}

	public View addRow(LCMessageItem bean) {
		lock.readLock().lock();
		try {
			return addRowInternal(bean);
		} finally {
			lock.readLock().unlock();
		}
	}

	public View addRowInternal(LCMessageItem bean) {
		View row = null;
		int position = beanList.size();
		if (beanList != null) {
			beanList.add(bean);
			mPositionMap.put(bean.msgId, Integer.valueOf(position));
		}
		switch (bean.sendType) {
		case Recv:
			switch (bean.msgType) {
			case Text:
				row = getTextMessageViewIn(bean);
				break;
			case Emotion:
				row = getEmotionViewIn(bean, position);
				break;
			case Voice:
				row = getVoiceViewIn(bean, position);
				break;
			case Photo:
				row = getPhotoViewIn(bean, position);
				break;
			case Warning:
				row = getWarningView(bean);
				break;
			case System:
				row = getSystemMessageView(bean);
				break;
			default:
				break;
			}
			break;
		case Send:
			switch (bean.msgType) {
			case Text:
			case Warning:
				row = getTextMessageViewOut(bean);
				break;
			case Emotion:
				row = getEmotionViewOut(bean, position);
				break;
			case Voice:
				row = getVoiceViewOut(bean, position);
				break;
			case Photo:
				row = getPhotoViewOut(bean, position);
				break;
			case Custom:
				row = getCustomMessageView(bean);
				break;
			case System:
				row = getSystemMessageView(bean);
				break;
			default:
				break;
			}
			break;
		case System:
			switch (bean.msgType) {
			case Warning:
				row = getWarningView(bean);
				break;
			case System:
				row = getSystemMessageView(bean);
				break;
			default:
				break;
			}
			break;
		case Unknow:
			switch (bean.msgType) {
			case Warning:
				row = getWarningView(bean);
				break;
			case System:
				row = getSystemMessageView(bean);
				break;
			default:
				break;
			}
			break;
		default:
			break;
		}
		if (row != null) {
			row.setTag(position);
			getContainer().addView(row);
			if (bean.msgType == MessageType.Emotion) {
				/* add 完成再加载，防止本地有图主线程回调回去，view未add进container导致死机 */
				new EmotionPlayImageDownloader2().downloadEmotionPlayImage(
						bean, this);
			}
		}
		return row;
	}

	/**
	 * 收到警告消息（包括无点提示等）
	 * 
	 * @param bean
	 * @return
	 */
	private View getWarningView(LCMessageItem bean) {
		View row = mLayoutInflater.inflate(R.layout.item_warning_tips, null);
		TextView msgView = (TextView) row.findViewById(R.id.tvNotifyMsg);
		if ((bean.getWarningItem().linkItem != null)
				&& (bean.getWarningItem().linkItem.linkOptType == LinkOptType.Rechange)) {
			String tips = bean.getWarningItem().message
					+ " " + bean.getWarningItem().linkItem.linkMsg;
			SpannableString sp = new SpannableString(tips);
			ClickableSpan clickableSpan = new ClickableSpan() {

				@Override
				public void onClick(View widget) {
					// TODO Auto-generated method stub
					final GetMoreCreditDialog dialog = new GetMoreCreditDialog(
							mContext, R.style.ChoosePhotoDialog);
					dialog.show();
				}
			};
//			sp.setSpan(new UnderlineSpan(),
//					bean.getWarningItem().message.length(), tips.length(),
//					Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
			sp.setSpan(new StyleSpan(Typeface.BOLD), bean.getWarningItem().message.length() + 1, tips.length(),
					Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
			sp.setSpan(clickableSpan, bean.getWarningItem().message.length() + 1,
					tips.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
			msgView.setText(sp);
			msgView.setLinkTextColor(mContext.getResources().getColor(R.color.blue_color));
			msgView.setMovementMethod(LinkMovementMethod.getInstance());
			msgView.setFocusable(false);
			msgView.setClickable(false);
			msgView.setLongClickable(false);
			if (!isAddHistory) {
				/* 弹出充值dialog处理 */
				final GetMoreCreditDialog dialog = new GetMoreCreditDialog(
						mContext, R.style.ChoosePhotoDialog);
				dialog.show();
			}
		} else {
			msgView.setText(bean.getWarningItem().message);
		}
		return row;
	}

	/**
	 * 
	 * 系统消息View
	 * 
	 * @param bean
	 * @return
	 */
	private View getSystemMessageView(LCMessageItem bean) {
		View row = mLayoutInflater.inflate(R.layout.item_normal_notify, null);
		TextView notifyView = (TextView) row.findViewById(R.id.tvNotifyMsg);
		notifyView.setText(bean.getSystemItem().message);
		return row;
	}

	/**
	 * 用户自定义Item显示，如试聊提醒
	 * 
	 * @param bean
	 * @return
	 */
	private View getCustomMessageView(LCMessageItem bean) {
		View row = mLayoutInflater.inflate(R.layout.item_trychat_notify, null);
		TextView notifyView = (TextView) row.findViewById(R.id.tvNotifyMsg);
		notifyView.setText(mContext.getString(R.string.trychat_tips));
		return row;
	}

	/**
	 * 初始化收到文本 来信息view
	 * 
	 * @param bean
	 */
	private View getTextMessageViewIn(LCMessageItem bean) {
		View row = mLayoutInflater.inflate(R.layout.item_in_message, null);
		TextView msgView = (TextView) row.findViewById(R.id.chat_message);
		msgView.setText(imageGetter.getExpressMsgHTML(bean.getTextItem().message));
		return row;
	}

	/**
	 * 初始化收到Emotion view
	 * 
	 * @param bean
	 * @return
	 */
	private View getEmotionViewIn(LCMessageItem bean, int position) {
		View row = mLayoutInflater.inflate(R.layout.item_in_emotion, null);
		return row;
	}

	/**
	 * 获取收到照片View
	 * 
	 * @param bean
	 * @return
	 */
	private View getPhotoViewIn(LCMessageItem bean, int position) {
		View row = mLayoutInflater.inflate(R.layout.item_in_photo, null);

		MaterialProgressBar pbDownload = (MaterialProgressBar) row
				.findViewById(R.id.pbDownload);
		ImageView ivPrivatePhoto = (ImageView) row
				.findViewById(R.id.ivPrivatePhoto);
		ImageButton btnError = (ImageButton) row.findViewById(R.id.btnError);
		ivPrivatePhoto.setTag(position);
		btnError.setTag(position);
		ivPrivatePhoto.setOnClickListener(this);
		new PrivatePhotoDownloader(mContext).displayPrivatePhoto(
				ivPrivatePhoto, pbDownload, bean, btnError);
		return row;
	}

	/**
	 * 获取收到语音View
	 * 
	 * @param bean
	 * @return
	 */
	private View getVoiceViewIn(LCMessageItem bean, int position) {
		View row = mLayoutInflater.inflate(R.layout.item_in_voice, null);

		MaterialProgressBar pbDownload = (MaterialProgressBar) row
				.findViewById(R.id.pbDownload);
		ImageButton btnError = (ImageButton) row.findViewById(R.id.btnError);
		TextView timeView = (TextView) row.findViewById(R.id.chat_sound_time);
		timeView.setText(bean.getVoiceItem().timeLength + "''");
		timeView.setTag(position);
		timeView.setOnClickListener(this);
		new LivechatVoiceDownloader(mContext).downloadAndPlayVoice(pbDownload,
				btnError, bean);
		return row;
	}

	/**
	 * 初始化发送文本 来信息view
	 * 
	 * @param bean
	 */
	private View getTextMessageViewOut(LCMessageItem bean) {
		View row = mLayoutInflater.inflate(R.layout.item_out_message, null);
		MaterialProgressBar pbDownload = (MaterialProgressBar) row
				.findViewById(R.id.pbDownload);
		TextView msgView = (TextView) row.findViewById(R.id.chat_message);
		msgView.setText(imageGetter.getExpressMsgHTML(bean.getTextItem().message));
		if (bean.getTextItem().illegal) {
			/* 非法的，显示警告 */
			row.findViewById(R.id.includeWaring).setVisibility(View.VISIBLE);
			((TextView) row.findViewById(R.id.tvNotifyMsg)).setText(mContext
					.getResources().getString(
							R.string.livechat_lady_illeage_message));
		}
		if (bean.statusType == StatusType.Processing) {
			pbDownload.setVisibility(View.VISIBLE);
		} else if (bean.statusType == StatusType.Fail) {
			pbDownload.setVisibility(View.GONE);
			row.findViewById(R.id.btnError).setTag(bean);
			row.findViewById(R.id.btnError).setVisibility(View.VISIBLE);
			row.findViewById(R.id.btnError).setOnClickListener(
					normalErrorOnClickListener);
		}
		return row;
	}

	/**
	 * 初始化发送Emotion view
	 * 
	 * @param bean
	 * @return
	 */
	private View getEmotionViewOut(LCMessageItem bean, int position) {
		View row = mLayoutInflater.inflate(R.layout.item_out_emotion, null);
		MaterialProgressBar pbDownload = (MaterialProgressBar) row
				.findViewById(R.id.pbDownload);
		if (bean.statusType == StatusType.Processing) {
			pbDownload.setVisibility(View.VISIBLE);
		} else if (bean.statusType == StatusType.Fail) {
			pbDownload.setVisibility(View.GONE);
			row.findViewById(R.id.btnError).setTag(bean);
			row.findViewById(R.id.btnError).setVisibility(View.VISIBLE);
			row.findViewById(R.id.btnError).setOnClickListener(
					normalErrorOnClickListener);
		}
		return row;
	}

	/**
	 * 获取发送照片View
	 * 
	 * @param bean
	 * @return
	 */
	private View getPhotoViewOut(LCMessageItem bean, int position) {
		View row = mLayoutInflater.inflate(R.layout.item_out_photo, null);

		MaterialProgressBar pbDownload = (MaterialProgressBar) row
				.findViewById(R.id.pbDownload);
		ImageView ivPrivatePhoto = (ImageView) row
				.findViewById(R.id.ivPrivatePhoto);
		ivPrivatePhoto.setTag(position);
		ivPrivatePhoto.setOnClickListener(this);
		if (bean.statusType == StatusType.Processing) {
			pbDownload.setVisibility(View.VISIBLE);
		} else if (bean.statusType == StatusType.Fail) {
			pbDownload.setVisibility(View.GONE);
			row.findViewById(R.id.btnError).setTag(bean);
			row.findViewById(R.id.btnError).setVisibility(View.VISIBLE);
			row.findViewById(R.id.btnError).setOnClickListener(
					normalErrorOnClickListener);
		}

		new PrivatePhotoDownloader(mContext).displayPrivatePhoto(
				ivPrivatePhoto, null, bean, null);
		return row;
	}

	/**
	 * 获取发送语音View
	 * 
	 * @param bean
	 * @return
	 */
	private View getVoiceViewOut(LCMessageItem bean, int position) {
		View row = mLayoutInflater.inflate(R.layout.item_out_voice, null);

		MaterialProgressBar pbDownload = (MaterialProgressBar) row
				.findViewById(R.id.pbDownload);
		if (bean.statusType == StatusType.Processing) {
			pbDownload.setVisibility(View.VISIBLE);
		} else if (bean.statusType == StatusType.Fail) {
			pbDownload.setVisibility(View.GONE);
			row.findViewById(R.id.btnError).setTag(bean);
			row.findViewById(R.id.btnError).setVisibility(View.VISIBLE);
			row.findViewById(R.id.btnError).setOnClickListener(
					normalErrorOnClickListener);
		}
		TextView timeView = (TextView) row.findViewById(R.id.chat_sound_time);
		timeView.setText(bean.getVoiceItem().timeLength + "''");
		timeView.setTag(position);
		timeView.setOnClickListener(this);
		new LivechatVoiceDownloader(mContext).downloadAndPlayVoice(null, null,
				bean);
		return row;
	}

	@Override
	public void onClick(View v) {
		int vid = v.getId();
		if (vid == R.id.ivPrivatePhoto) {
			/* 点击图片看大图处理 */
			onPrivatePhotoClick(v);
		} else if (vid == R.id.chat_sound_time) {
			/* 语音Item，点击播放语音 */
			onVoiceItemClick(v);
		}
	}

	/**
	 * 私密照点击看大图
	 */
	private void onPrivatePhotoClick(View v) {
		int postion = getPosition(v);
		if (beanList.size() > postion) {
			LCMessageItem currItem = beanList.get(postion);
			List<LCMessageItem> mPrivatePhotoList = new ArrayList<LCMessageItem>();
			for (LCMessageItem item : beanList) {
				if (item.msgType == MessageType.Photo) {
					mPrivatePhotoList.add(item);
				}
			}
			if (mPrivatePhotoList.contains(currItem)) {
				/* private photo item存在，打开预览 */
				mContext.startActivity(LivechatPrivatePhotoPreview.getIntent(
						mContext,
						new PrivatePhotoPriviewBean(mPrivatePhotoList
								.indexOf(currItem), mPrivatePhotoList)));
			}
		}
	}

	/**
	 * 语音Item点击处理
	 * 
	 * @param v
	 */
	private void onVoiceItemClick(View v) {
		int postion = getPosition(v);
		if (beanList.size() > postion) {
			LCMessageItem item = beanList.get(postion);
			mVoicePlayerManager.startPlayVoice(v, item.msgId,
					item.getVoiceItem().filePath);
		}
	}

	/**
	 * 发送消息回调处理
	 * 
	 * @param errType
	 * @param item
	 */
	public void updateSendMessageCallback(final LiveChatCallBackItem callback) {
		LCMessageItem item = (LCMessageItem) callback.body;
		LiveChatErrType errType = LiveChatErrType.values()[callback.errType];
		/*解决Livechat聊天消息由于底层处理试聊及是否有钱等情况成功后才返回错误提示，在这个过程中受到女士邀请，导致失败返回列表消息中包含了收到的文本消息，导致无进度条异常失败*/
		if (item != null && (item.sendType == SendType.Send)) {
			if (mPositionMap.containsKey(item.msgId)) {
				/* 音频暂时单独处理 */
				int position = mPositionMap.get(item.msgId);
				/* 更新数据 */
				beanList.remove(position);
				beanList.add(position, item);
				/* 更新界面 */
				View row = getContainer().getChildAt(position);
				MaterialProgressBar pbDownload = (MaterialProgressBar) row
						.findViewById(R.id.pbDownload);
				ImageButton btnError = (ImageButton) row
						.findViewById(R.id.btnError);
				if(pbDownload != null){
					pbDownload.setVisibility(View.GONE);
				}
				if (errType != LiveChatErrType.Success) {
					btnError.setVisibility(View.VISIBLE);
					btnError.setTag(item);
					if (errType == LiveChatErrType.SideOffile ||
							errType == LiveChatErrType.UnbindInterpreter||
							errType == LiveChatErrType.BlockUser) {
						btnError.setOnClickListener(offlineOnClickListener);
					} else if (errType == LiveChatErrType.NoMoney) {
						btnError.setOnClickListener(noMoneyOnClickListener);
					} else if(errType == LiveChatErrType.InvalidUser ||
								errType == LiveChatErrType.InvalidPassword ||
								errType == LiveChatErrType.CheckVerFail ||
								errType == LiveChatErrType.LoginFail ||
								errType == LiveChatErrType.CanNotSetOffline){
						btnError.setOnClickListener(noLoginOnClickListener);
					}else {
						if (!StringUtil.isEmpty(callback.errNo)) {
							if (callback.errNo.equals("ERROR00003")) {
								btnError.setOnClickListener(noMoneyOnClickListener);
								return;
							}else if(!StringUtil.isEmpty(callback.errMsg)){
								/*Jaywar 接口返回错误提示*/
								btnError.setOnClickListener(new OnClickListener() {
									
									@Override
									public void onClick(View v) {
										MaterialDialogAlert dialog = new MaterialDialogAlert(mContext);
										dialog.setMessage(callback.errMsg);
										dialog.addButton(dialog.createButton(mContext.getString(R.string.common_btn_ok),null));
										dialog.show();
									}
								});
								return;
							}
						}
						btnError.setOnClickListener(normalErrorOnClickListener);
					}
				}
			}
		}
	}

	/**
	 * 图片购买成功回调更新数据状态
	 */
	public void onPhotoFeeSuccess(LCMessageItem bean) {
		if (beanList != null) {
			for (LCMessageItem item : beanList) {
				if (bean.msgId == item.msgId) {
					item.getPhotoItem().charge = bean.getPhotoItem().charge;
				}
			}
		}
	}

	/**
	 * 私密照购图界面下大图成功更新
	 * 
	 * @param bean
	 */
	public void onPrivatePhotoDownload(LCMessageItem bean) {
		if (bean != null) {
			if (mPositionMap.containsKey(bean.msgId)) {
				int position = mPositionMap.get(bean.msgId);
				if (!StringUtil.isEmpty(bean.getPhotoItem().showSrcFilePath)) {
					beanList.get(position).getPhotoItem().showSrcFilePath = bean
							.getPhotoItem().showSrcFilePath;
					/* 更新界面 */
					View row = getContainer().getChildAt(position);
					row.findViewById(R.id.btnError).setVisibility(View.GONE);
					row.findViewById(R.id.pbDownload).setVisibility(View.GONE);
					ImageView privatePhoto = (ImageView) row
							.findViewById(R.id.ivPrivatePhoto);
					Bitmap bitmap = ImageUtil
							.decodeHeightDependedBitmapFromFile(
									bean.getPhotoItem().showSrcFilePath,
									UnitConversion.dip2px(mContext, 112));
					if(bitmap != null){
						privatePhoto.setImageBitmap(ImageUtil.get2DpRoundedImage(
								mContext, bitmap));
					}
				}
			}
		}
	}

	/**
	 * 发送消息失败（女士不在线），点击重发按钮提示
	 */
	private OnClickListener offlineOnClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			final LCMessageItem item = (LCMessageItem) v.getTag();
			if(item.getUserItem().statusType == UserStatusType.USTATUS_ONLINE){
				/*女士上线后可以重新发送*/
				MaterialDialogAlert dialog = new MaterialDialogAlert(mContext);
				dialog.setMessage(mContext
						.getString(R.string.send_error_text_normal));
				dialog.addButton(dialog.createButton(
						mContext.getString(R.string.common_btn_retry),
						new OnClickListener() {

							@Override
							public void onClick(View v) {
								resendMsgItem(item);
							}
						}));
				dialog.addButton(dialog.createButton(
						mContext.getString(R.string.common_btn_cancel),
						new OnClickListener() {

							@Override
							public void onClick(View v) {

							}

						}));

				dialog.show();
			}else{
				MaterialDialogAlert dialog = new MaterialDialogAlert(mContext);
				dialog.setMessage(mContext
						.getString(R.string.send_error_lady_offline));
				dialog.addButton(dialog.createButton(
						mContext.getString(R.string.common_send_email),
						new OnClickListener() {
	
							@Override
							public void onClick(View v) {
								MailEditActivity.launchMailEditActivity(mContext, item.toId, ReplyType.DEFAULT, "");
							}
						}));
				dialog.addButton(dialog.createButton(
						mContext.getString(R.string.livechat_end_chat),
						new OnClickListener() {
	
							@Override
							public void onClick(View v) {
								// TODO Auto-generated method stub
								mLiveChatManager.EndTalk(item.toId);
								((ChatActivity) mContext).finish();
							}
	
						}));
	
				dialog.show();
			}
		}
	};

	/**
	 * 登陆异常错误处理，注销，清除密码，关闭聊天窗口，弹出登陆页面
	 */
	private OnClickListener noLoginOnClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			LoginManager.newInstance(mContext).Logout();
			/*清除密码*/
			LoginParam param = LoginPerfence.GetLoginParam(mContext);
			param.password = "";
			LoginPerfence.SaveLoginParam(mContext, param);
			
			((ChatActivity) mContext).finish();
			
			Intent loginIntent = new Intent(mContext, RegisterActivity.class);
			mContext.startActivity(loginIntent);
		}
	};
	
	/**
	 * 发送消息失败（男士余额不足），点击重发按钮提示
	 */
	private OnClickListener noMoneyOnClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			final LCMessageItem item = (LCMessageItem) v.getTag();
			MaterialDialogAlert dialog = new MaterialDialogAlert(mContext);
			dialog.setMessage(mContext
					.getString(R.string.send_error_not_enough_money));
			dialog.addButton(dialog.createButton(
					mContext.getString(R.string.common_btn_retry),
					new OnClickListener() {

						@Override
						public void onClick(View v) {
							resendMsgItem(item);
						}
					}));
			dialog.addButton(dialog.createButton(
					mContext.getString(R.string.common_btn_cancel),
					new OnClickListener() {

						@Override
						public void onClick(View v) {

						}

					}));

			dialog.show();
		}
	};

	/**
	 * 发送消息失败（普通错误），点击重发按钮提示
	 */
	private OnClickListener normalErrorOnClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			final LCMessageItem item = (LCMessageItem) v.getTag();
			MaterialDialogAlert dialog = new MaterialDialogAlert(mContext);
			dialog.setMessage(mContext
					.getString(R.string.send_error_text_normal));
			dialog.addButton(dialog.createButton(
					mContext.getString(R.string.common_btn_retry),
					new OnClickListener() {

						@Override
						public void onClick(View v) {
							resendMsgItem(item);
						}
					}));
			dialog.addButton(dialog.createButton(
					mContext.getString(R.string.common_btn_cancel),
					new OnClickListener() {

						@Override
						public void onClick(View v) {

						}

					}));

			dialog.show();
		}
	};

	/**
	 * 消息发送失败，重新发送，当成一条新的消息处理（隐藏原有消息，重新再底部生成一条）
	 * 
	 * @param item
	 */
	private void resendMsgItem(LCMessageItem item) {
		if (mPositionMap.containsKey(item.msgId)) {
			int position = mPositionMap.get(item.msgId);
			View row = getContainer().getChildAt(position);
			row.setVisibility(GONE);
			/*消息重发，删除列表中旧消息*/
			mLiveChatManager.RemoveHistoryMessage(item);
		}
		LCMessageItem newItem = null;
		switch (item.msgType) {
		case Text:
			newItem = mLiveChatManager.SendMessage(item.toId,
					item.getTextItem().message);
			break;
		case Emotion:
			newItem = mLiveChatManager.SendEmotion(item.toId,
					item.getEmotionItem().emotionId);
			break;
		case Photo:
			newItem = mLiveChatManager.SendPhoto(item.toId,
					item.getPhotoItem().srcFilePath);
			break;
		case Voice:
			newItem = mLiveChatManager.SendVoice(item.toId,
					item.getVoiceItem().filePath, ".aac",
					item.getVoiceItem().timeLength);
			break;
		default:
			break;
		}

		addRow(newItem);
		scrollToBottom(true);
	}

	/*
	 * chatActivity退出回收资源
	 */
	public void onDestroy() {
		/* 关闭所有正在播放的动画 */
		isDestroyed = true;
		stopPlaying();
		if (beanList != null) {
			for (LCMessageItem item : beanList) {
				if (item.msgType == MessageType.Emotion) {
					if (mPositionMap.containsKey(item.msgId)) {
						int postion = mPositionMap.get(item.msgId);
						((EmotionPlayer) getContainer().getChildAt(postion)
								.findViewById(R.id.emotionPlayer)).stop();
					}
				}
			}
			beanList.clear();
		}
		mPositionMap.clear();
	}

	/**
	 * 停止语音播放
	 */
	public void stopPlaying() {
		mVoicePlayerManager.stopPlaying();
	}

	/**
	 * 是否女士发来了消息
	 * 
	 * @return
	 */
	public boolean hasLadyInvited() {
		int index = 0;
		while (beanList.size() > index) {
			LCMessageItem bean = beanList.get(index);
			if (bean.sendType == SendType.Recv) {
				return true;
			}
			index++;
		}
		return false;
	}

	public int getPosition(View view) {
		Object value = view.getTag();
		if (value != null && value instanceof Integer) {
			return (Integer) value;
		} else {
			ViewParent parent = view.getParent();
			if (parent != null && parent instanceof View) {
				View p = (View) parent;
				return getPosition(p);
			} else {
				return -1;
			}
		}
	}

	/* 下载高级表情播放图片回调 */
	@Override
	public void onEmotionPlayImageDownloadStart(LCMessageItem item) {
		if (!isDestroyed) {
			if (item != null) {
				if (mPositionMap.containsKey(item.msgId)) {
					int position = mPositionMap.get(item.msgId);
					View row = getContainer().getChildAt(position);
					if (item.sendType == SendType.Recv) {
						/* 当接受时才处理加载及错误按钮 */
						row.findViewById(R.id.pbDownload).setVisibility(
								View.VISIBLE);
						row.findViewById(R.id.btnError)
								.setVisibility(View.GONE);
					}
					row.findViewById(R.id.emotionPlayer).setVisibility(
							View.GONE);
				}
			}
		}
	}

	@Override
	public void onEmotionPlayImageDownloadSuccess(LCMessageItem item) {
		if ((!isDestroyed)) {
			if (item != null) {
				if (mPositionMap.containsKey(item.msgId)) {
					int position = mPositionMap.get(item.msgId);
					View row = getContainer().getChildAt(position);
					if (item.sendType == SendType.Recv) {
						/* 当接受时才处理加载及错误按钮 */
						row.findViewById(R.id.pbDownload).setVisibility(
								View.GONE);
						row.findViewById(R.id.btnError)
								.setVisibility(View.GONE);
					}
					row.findViewById(R.id.ivEmotionDef)
							.setVisibility(View.GONE);
					LCEmotionItem emotionItem = mLiveChatManager
							.GetEmotionInfo(item.getEmotionItem().emotionId);
					EmotionPlayer player = (EmotionPlayer) row
							.findViewById(R.id.emotionPlayer);
					player.setVisibility(View.VISIBLE);
					player.setImageList(emotionItem.playBigImages);
					if(isScrolled){
						if ((currVisibleList != null)&& currVisibleList.contains(item)) {
							/* 可见的则播放，否则不播放 */
							player.play();
						}
					}else{
						/*未满一页的情况下直接播放*/
						player.play();
					}
				}
			}
		}
	}

	@Override
	public void onEmotionPlayImageDownloadFail(final LCMessageItem item) {
		if (!isDestroyed) {
			if (item != null) {
				if (mPositionMap.containsKey(item.msgId)) {
					int position = mPositionMap.get(item.msgId);
					View row = getContainer().getChildAt(position);
					if (item.sendType == SendType.Recv) {
						/* 当接受时才处理加载及错误按钮 */
						row.findViewById(R.id.pbDownload).setVisibility(
								View.GONE);
						ImageButton btnError = (ImageButton) row
								.findViewById(R.id.btnError);
						btnError.setVisibility(View.VISIBLE);
						btnError.setOnClickListener(new OnClickListener() {

							@Override
							public void onClick(View v) {
								/* 下载失败，提示及重新下载 */
								MaterialDialogAlert dialog = new MaterialDialogAlert(
										mContext);
								dialog.setMessage(mContext
										.getString(R.string.livechat_download_emotion_fail));
								dialog.addButton(dialog.createButton(mContext
										.getString(R.string.common_btn_retry),
										new OnClickListener() {
											@Override
											public void onClick(View v) {
												/* 文件下载失败重新下载 */
												new EmotionPlayImageDownloader2()
														.downloadEmotionPlayImage(
																item,
																MessageListView.this);
											}
										}));
								dialog.addButton(dialog.createButton(mContext
										.getString(R.string.common_btn_cancel),
										null));

								dialog.show();

							}
						});
					}
					row.findViewById(R.id.emotionPlayer).setVisibility(
							View.GONE);
					row.findViewById(R.id.ivEmotionDef).setVisibility(
							View.VISIBLE);
				}
			}
		}
	}

	@Override
	protected void onScrollChanged(int l, int t, int oldl, int oldt) {
		// TODO Auto-generated method stub
		super.onScrollChanged(l, t, oldl, oldt);
		Rect scrollBounds = new Rect();
		getHitRect(scrollBounds);
		isScrolled = true;
		currVisibleList.clear();
		for (int i = 0; i < getContainer().getChildCount(); i++) {
			if (getContainer().getChildAt(i).getLocalVisibleRect(scrollBounds)) {
				/**
				 * 可见的播放
				 */
				if (beanList.get(i).msgType == MessageType.Emotion) {
					EmotionPlayer player = (EmotionPlayer) getContainer()
							.getChildAt(i).findViewById(R.id.emotionPlayer);
					if ((!player.isPlaying()) && (player.canPlay())) {
						player.play();
					}
					currVisibleList.add(beanList.get(i));
				}
			} else {
				/**
				 * 不可见的，如果正在播放，关闭
				 */
				if (beanList.get(i).msgType == MessageType.Emotion) {
					EmotionPlayer player = (EmotionPlayer) getContainer()
							.getChildAt(i).findViewById(R.id.emotionPlayer);
					if (player.isPlaying()) {
						player.stop();
					}

				}
			}
		}
	}
}