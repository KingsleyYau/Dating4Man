package com.qpidnetwork.dating.analysis;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.telephony.TelephonyManager;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.view.WindowManager;

import com.qpidnetwork.dating.QpidApplication;
import com.qpidnetwork.dating.bean.RequestBaseResponse;
import com.qpidnetwork.framework.util.StringUtil;
import com.qpidnetwork.manager.WebSiteManager;
import com.qpidnetwork.request.OnRequestCallback;
import com.qpidnetwork.request.RequestErrorCode;
import com.qpidnetwork.request.RequestJni;
import com.qpidnetwork.request.RequestJniOther;

public class AdAnakysisManager implements OnRequestCallback 
{

	private String UTM_REFERENCE_FILE = "utm_reference";// 广告推广存放数据缓存文件名

	private static AdAnakysisManager mAdAnakysisManager;

	private AnalysisItem mAnalysisItem;
	private Context mContext;
	private Handler mHandler;

	/**
	 * 登录状态
	 * 
	 * @param DEFAULT
	 *            默认未上传状态
	 * @param UPLOADING
	 *            上传中
	 */
	public enum UploadUtmStatus {
		DEFAULT, UPLOADING
	}

	/**
	 * 当前上传状态，防止重复提交
	 */
	private UploadUtmStatus mUploadUtmStatus = UploadUtmStatus.DEFAULT;
	private List<OnRequestCallback> mUtmCallbackList;

	/**
	 * 创建单例
	 * 
	 * @param context
	 * @return
	 */
	public static AdAnakysisManager newInstance(Context context) {
		if (mAdAnakysisManager == null) {
			mAdAnakysisManager = new AdAnakysisManager(context);
		}
		return mAdAnakysisManager;
	}

	/**
	 * 获取单例使用
	 * 
	 * @return
	 */
	public static AdAnakysisManager getInstance() {
		return mAdAnakysisManager;
	}

	public AdAnakysisManager(Context context) {
		this.mContext = context;
		mUploadUtmStatus = UploadUtmStatus.DEFAULT;
		mUtmCallbackList = new ArrayList<OnRequestCallback>();
		mAnalysisItem = getUtmReferenceData();
		if ((mAnalysisItem == null) || (mAnalysisItem.versionCode != QpidApplication.versionCode)) {
			/* 无安装信息记录或者非当前版本，即升级等，在未收到广播时，初始化提交 */
			AnalysisItem item = new AnalysisItem();
			item.utm_referrer = "";
			item.isSummit = false;
			item.versionCode = QpidApplication.versionCode;
			item.installTime = (int) (System.currentTimeMillis() / 1000);
			setAnalysisItem(item);
		}
		mHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				RequestBaseResponse response = (RequestBaseResponse) msg.obj;
				if (response.isSuccess) {
					mAnalysisItem.isSummit = true;
					saveUtmReferenceData(mContext, mAnalysisItem);
				}
				for (OnRequestCallback callback : mUtmCallbackList) {
					callback.OnRequest(response.isSuccess, response.errno,
							response.errmsg);
				}
				mUtmCallbackList.clear();
				mUploadUtmStatus = UploadUtmStatus.DEFAULT;
			}
		};
	}

	/**
	 * 更新并存储utm reference信息及是否已上传状态
	 */
	public void setAnalysisItem(AnalysisItem item) {
		this.mAnalysisItem = item;
		saveUtmReferenceData(mContext, item);
	}

	/**
	 * 获取单例AnalysisItem
	 * 
	 * @return
	 */
	public AnalysisItem getAnalysisItem() {
		return mAnalysisItem;
	}

	/**
	 * 存放安装广播发送的Utm 数据
	 * 
	 * @param context
	 * @param item
	 */
	private void saveUtmReferenceData(Context context, AnalysisItem item) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ObjectOutputStream oos;
		try {
			oos = new ObjectOutputStream(baos);
			if (item != null) {
				oos.writeObject(item);
			}
			String utmReferenceBase64 = new String(Base64.encode(
					baos.toByteArray(), Base64.DEFAULT));

			// FileOutputStream outStream =
			// context.openFileOutput(getUtmReferencePath(),
			// Context.MODE_PRIVATE);//写到data目录下
			FileOutputStream outStream = new FileOutputStream(new File(
					getUtmReferencePath()));
			outStream.write(utmReferenceBase64.getBytes());
			outStream.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * 获取本地缓存中Utm reference 数据缓存
	 * 
	 * @return
	 */
	private AnalysisItem getUtmReferenceData() {
		AnalysisItem item = null;
		try {
			File file = new File(getUtmReferencePath());
			BufferedReader br = new BufferedReader(new FileReader(file));
			String readline = "";
			StringBuffer sb = new StringBuffer();
			while ((readline = br.readLine()) != null) {
				System.out.println("readline:" + readline);
				sb.append(readline);
			}
			br.close();
			String utmReferenceBase64 = sb.toString();

			byte[] base64Bytes = Base64.decode(utmReferenceBase64.getBytes(),
					Base64.DEFAULT);
			ByteArrayInputStream bais = new ByteArrayInputStream(base64Bytes);
			ObjectInputStream ois = new ObjectInputStream(bais);
			item = (AnalysisItem) ois.readObject();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return item;
	}

	private String getUtmReferencePath() {
		String path = WebSiteManager.getInstance().GetCachePath() + UTM_REFERENCE_FILE;
		File file = new File(WebSiteManager.getInstance().GetCachePath());
		if (!file.exists()) {
			file.mkdirs();
		}
		File reference = new File(path);
		if (!reference.exists()) {
			try {
				reference.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return path;
	}

	public void summitUtmReference(OnRequestCallback requestCallback) {

		if (requestCallback != null) {
			mUtmCallbackList.add(requestCallback);
		}

		if (mUploadUtmStatus != UploadUtmStatus.DEFAULT) {
			// 已经再上传安装信息，不重复上传
			return;
		}
		TelephonyManager tm = (TelephonyManager) mContext
				.getSystemService(Context.TELEPHONY_SERVICE);

		DisplayMetrics dm = new DisplayMetrics();
		WindowManager wm = (WindowManager) mContext
				.getSystemService(Context.WINDOW_SERVICE);
		wm.getDefaultDisplay().getMetrics(dm);

		String reference = "";
		int installTime = 0;
		int submitTime = (int) (System.currentTimeMillis() / 1000);
		AnalysisItem item = AdAnakysisManager.getInstance().getAnalysisItem();
		if ((item != null) && (item.utm_referrer != null)
				&& (item.utm_referrer.length() > 0)) {
			try {
				reference = URLEncoder.encode(item.utm_referrer, "US-ASCII");
			} catch (Exception e) {

			}
		}

		if (item != null) {
			installTime = item.installTime;
		}
		mUploadUtmStatus = UploadUtmStatus.UPLOADING;
		RequestJniOther.InstallLogs(RequestJni.GetDeviceId(tm), dm.widthPixels,
				dm.heightPixels, installTime, submitTime,
				QpidApplication.versionCode, reference,
				this);
	}

	@Override
	public void OnRequest(boolean isSuccess, String errno, String errmsg) {
		// TODO Auto-generated method stub
		Message msg = Message.obtain();
		boolean success = isSuccess;
		if (!StringUtil.isEmpty(errno) && (errno
						.equals(RequestErrorCode.LOCAL_ERROR_CODE_TIMEOUT))){
			success = false;
		}else{
			success = true;
		}
		RequestBaseResponse response = new RequestBaseResponse(
				success, errno, errmsg, null);
		msg.obj = response;
		mHandler.sendMessage(msg);
	}
}
