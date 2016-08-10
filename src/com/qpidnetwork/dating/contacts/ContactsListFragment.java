package com.qpidnetwork.dating.contacts;

import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;

import com.qpidnetwork.dating.R;
import com.qpidnetwork.dating.bean.ContactBean;
import com.qpidnetwork.dating.bean.RequestFailBean;
import com.qpidnetwork.dating.contacts.ContactsAdapter.OnContactListItemLongClickListener;
import com.qpidnetwork.dating.home.HomeActivity;
import com.qpidnetwork.dating.lady.LadyDetailActivity;
import com.qpidnetwork.framework.base.BaseFragmentActivity;
import com.qpidnetwork.framework.base.BaseListFragment;
import com.qpidnetwork.framework.util.ToastUtil;
import com.qpidnetwork.livechat.LiveChatManager;
import com.qpidnetwork.request.OnRequestCallback;
import com.qpidnetwork.request.RequestOperator;
import com.qpidnetwork.view.MaterialDialogAlert;
import com.qpidnetwork.view.MaterialDialogSingleChoice;

public class ContactsListFragment extends BaseListFragment implements
		OnContactUpdateCallback, OnGetContactListCallBack, OnContactListItemLongClickListener  {

	private static final int GET_CONTACTLIST_SUCCESS = 0;
	private static final int GET_CONTACTLIST_FAILED = 1;
	private static final int CONTACTLIS_UPDATE = 2;
	private static final int REMOVE_CONTACT_SUCCESS = 3;
	private static final int REMOVE_CONTACT_FAILED = 4;

	private ContactsAdapter mAdapter;
	private ContactManager mContactManager;
	private boolean isInited = false;

	public static ContactsListFragment newInstance() {
		ContactsListFragment fragment = new ContactsListFragment();
		return fragment;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onActivityCreated(savedInstanceState);
		mContactManager = ContactManager.getInstance();
		mAdapter = new ContactsAdapter(getActivity(), 0);
		/* 加入排序方法 */
		mAdapter.setComparator(ContactBean.getComparator());
		/* 关闭下拉刷新和上拉刷新 */
		closePullDownRefresh();
		closePullUpRefresh(true);

		mAdapter.setOnContactListItemLongClickListener(this);
		getPullToRefreshListView().setAdapter(mAdapter);

		mContactManager.registerContactUpdate(this);
		queryContactList();
	}

	/**
	 * 获取联系人列表
	 */
	private void queryContactList() {
		if (!isInited) {
			showInitLoading();
		}
		mContactManager.getContacts(this);
	}

	/**
	 * 删除联系人确认提示
	 * 
	 * @param contactBean
	 */
	private void showRemoveContactConfirm(final ContactBean bean) {
		MaterialDialogAlert dialog = new MaterialDialogAlert(getActivity());
		dialog.setMessage(getString(R.string.contact_delete_confirm_tips));
		dialog.addButton(dialog.createButton(getString(R.string.common_btn_delete),
				new OnClickListener() {

					@Override
					public void onClick(View v) {
						String[] womanId = new String[1];
						womanId[0] = bean.womanid;
						if(bean.isInchating){
							/*删除联系人，如果在聊，先EndChat*/
							LiveChatManager.getInstance().EndTalk(bean.womanid);
						}
						removeContact(womanId);
					}
				}));
		dialog.addButton(dialog.createButton(
				getString(R.string.common_btn_cancel), null));
		dialog.show();
	}

	/**
	 * 删除联系人
	 * 
	 * @param womanId
	 */
	private void removeContact(final String[] womanId) {
		((BaseFragmentActivity) getActivity()).showToastProgressing("Deleting");
		RequestOperator.getInstance().RemoveContactList(womanId,
				new OnRequestCallback() {

					@Override
					public void OnRequest(boolean isSuccess, String errno,
							String errmsg) {
						Message msg = Message.obtain();
						if (isSuccess) {
							msg.what = REMOVE_CONTACT_SUCCESS;
							msg.obj = womanId;
						} else {
							msg.what = REMOVE_CONTACT_FAILED;
							RequestFailBean bean = new RequestFailBean(errno,
									errmsg);
							msg.obj = bean;
						}
						sendUiMessage(msg);
					}
				});
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void handleUiMessage(Message msg) {
		// TODO Auto-generated method stub
		super.handleUiMessage(msg);
		switch (msg.what) {
		case GET_CONTACTLIST_SUCCESS:
			List<ContactBean> list = (List<ContactBean>) msg.obj;
			if (list != null && list.size() > 0) {
				mAdapter.replaceList(list);
				isInited = true;
				hideLoadingPage();
			} else {
				isInited = false;
				showInitEmpty(getEmptyView());
			}

			break;

		case GET_CONTACTLIST_FAILED:
			if (!isInited) {
				showInitError();
			} else {
				String errorMsg = (String) msg.obj;
				if (getActivity() != null) {
					ToastUtil.showToast(getActivity(), errorMsg);
				}
			}
			break;
		case CONTACTLIS_UPDATE:
			/* 联系人更新回调 */
			List<ContactBean> contactlist = (List<ContactBean>) msg.obj;
			if (contactlist != null && contactlist.size() > 0) {
				mAdapter.replaceList(contactlist);
				isInited = true;
				hideLoadingPage();
			}
			break;

		case REMOVE_CONTACT_SUCCESS:
			/* 删除联系人成功 */
			String[] womanId = (String[]) msg.obj;
			((BaseFragmentActivity) getActivity()).showToastDone("Done!");
			mContactManager.deleteContactByUserId(womanId);
			break;
		case REMOVE_CONTACT_FAILED:
			/* 删除联系人失败 */
			((BaseFragmentActivity) getActivity()).showToastFailed("Failed!");
			break;
		}
	}

	/**
	 * @return 设置empty view
	 */
	private View getEmptyView() {
		// TODO Auto-generated method stub
		View view  = LayoutInflater.from(mContext).inflate(R.layout.view_contact_empty, null);
		view.findViewById(R.id.btnSearch).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				getActivity().sendBroadcast(new Intent(HomeActivity.REFRESH_ONLINE_LADY));
				getActivity().finish();
			}
		});
		return view;
	}

	@Override
	public void onDetach() {
		// TODO Auto-generated method stub
		super.onDetach();
		mContactManager.unregisterContactUpdata(this);
	}

	@Override
	public void onInitRetry() {
		// TODO Auto-generated method stub
		super.onInitRetry();
		queryContactList();
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		isInited = false;
	}

	@Override
	public void onContactUpdate(List<ContactBean> contactList) {
		/* 联系人列表更新处理 */
		Message msg = Message.obtain();
		msg.what = CONTACTLIS_UPDATE;
		msg.obj = contactList;
		sendUiMessage(msg);
	}

	@Override
	public void onContactListCallback(boolean isSuccess, String errno,
			String errmsg) {
		Message msg = Message.obtain();
		if (isSuccess) {
			msg.what = GET_CONTACTLIST_SUCCESS;
			msg.obj = mContactManager.getContactList();
		} else {
			msg.what = GET_CONTACTLIST_FAILED;
			msg.obj = errmsg;
		}
		sendUiMessage(msg);		
	}

	@Override
	public void onContactListItemLongClick(final int position) {
		MaterialDialogSingleChoice dialog = new MaterialDialogSingleChoice(
				getActivity(), new String[] {
						getString(R.string.view_profile),
						getString(R.string.common_btn_delete),
						getString(R.string.common_btn_cancel) },
				new MaterialDialogSingleChoice.OnClickCallback() {

					@Override
					public void onClick(AdapterView<?> adptView,
							View v, int which) {
						// TODO Auto-generated method stub
						if (which == 0) {
							LadyDetailActivity
							.launchLadyDetailActivity(
									getActivity(),
									mAdapter.getDataList().get(
											position).womanid,
									true);
							
						} else if (which == 1) {
							ContactBean bean = mAdapter.getDataList()
									.get(position);
							showRemoveContactConfirm(bean);
						}
					}
				}, -1);

		dialog.show();
	}
}
