package com.qpidnetwork.dating.contacts;

import java.util.List;

import com.qpidnetwork.dating.bean.ContactBean;

public interface OnContactUpdateCallback {
	public void onContactUpdate(List<ContactBean> contactList);
}



