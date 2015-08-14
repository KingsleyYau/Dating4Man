package com.qpidnetwork.dating.contacts;

import com.qpidnetwork.dating.R;

import android.content.Context;

public class ContactSearchType {

	public enum LabelType{
		ONLINE_ONLY,
		OFFLINE_ONLY,
		MY_FAVORITES,
		WITH_VIDEOS
	}
	
	/**
	 * 获取对应类型的描述
	 * @param context
	 * @param type
	 * @return
	 */
	public String getDescByType(Context context, LabelType type){
		String title = "";
		switch (type) {
		case ONLINE_ONLY:
			title = context.getResources().getString(R.string.contact_label_online_only);
			break;
		case OFFLINE_ONLY:
			title = context.getResources().getString(R.string.contact_label_offline_only);
			break;
		case MY_FAVORITES:
			title = context.getResources().getString(R.string.contact_label_my_favorites);
			break;
		case WITH_VIDEOS:
			title = context.getResources().getString(R.string.contact_label_with_videos);
			break;
		default:
			break;
		}
		return title;
	}
}
