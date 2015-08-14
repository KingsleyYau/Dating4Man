package com.qpidnetwork.dating.livechat.expression;

import java.util.ArrayList;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.text.TextUtils;

import com.qpidnetwork.framework.util.Log;

public class EmotionHistoryUtil {
	
	public  static ArrayList<String> getItemStringIds(Context ctt,String userId,String webName,String type) {
		ArrayList<String> ids = new ArrayList<String>(20);
		SharedPreferences sp = ctt.getSharedPreferences("expression"+userId+webName, Context.MODE_PRIVATE);
		String expression = sp.getString(type, "");
		if (!TextUtils.isEmpty(expression)) {
			String[] a = expression.split(",");
			for (String s : a) {
				if (!TextUtils.isEmpty(s))
					ids.add(s);
			}
		}
		return ids;
	}
	
	public static void saveItemStringIds(ArrayList<String> ids, Context ctt,String userId,String webName,String type) {
		if (ids == null)
			return;
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < ids.size(); i++) {
			sb.append(ids.get(i)).append(",");
		}
		if (sb.length() > 0) {
			sb.setLength(sb.length() - 1);
		}
		Log.i(type, "保存  ------> expression = " + sb.toString());

		SharedPreferences sp = ctt.getSharedPreferences("expression"+userId+webName, Context.MODE_PRIVATE);
		Editor editor = sp.edit();
		editor.putString(type, sb.toString());
		editor.commit();
	}
}
