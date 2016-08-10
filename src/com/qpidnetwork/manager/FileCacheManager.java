package com.qpidnetwork.manager;

import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;

import android.annotation.SuppressLint;
import android.content.Context;

import com.qpidnetwork.request.RequestJniEMF.PrivatePhotoMode;
import com.qpidnetwork.request.RequestJniEMF.PrivatePhotoType;
import com.qpidnetwork.tool.Arithmetic;

/**
 * 文件缓存管理
 * @author Max.Chiu 
 */
public class FileCacheManager {
	static String CRASH = "crash";
	static String IMAGE_DIR = "image";
	static String LADY_DIR = "lady";
	static String VIRTUAL_GIFT = "virtual_gift";
	static String PRIVATE_PHOTO = "private_photo";
	static String LC_EMOTION_DIR = "livechat/emotion";
	static String LC_VOICE_DIR = "livechat/voice";
	static String LC_PHTOT_DIR = "livechat/photo";
	static String LC_VIDEO_DIR = "livechat/video";
	static String LC_MAGICICON_DIR = "livechat/magicIcon";
	static String LC_TAKE_PHOTO_TEM_DIR = "livechat/photo/temp";
	static String LC_THEME_DIR = "livechat/theme";
	static String LOG_DIR = "log";
	static String TEMP = "temp";
	static String EMF = "emf";
	static String HTTP = "http";
	static String EMF_VIDEO = "emf/video";
	
	private static FileCacheManager gFileCacheManager;
	
	private String mMainPath = "";
	
	public static FileCacheManager newInstance(Context context) {
		if( gFileCacheManager == null ) {
			gFileCacheManager = new FileCacheManager(context);
		} 
		return gFileCacheManager;
	}
	

	public static FileCacheManager getInstance() {
		return gFileCacheManager;
	}
	
	public FileCacheManager(Context contenxt) {
//		mContext = contenxt;
		
	} 
	
	/**
	 *  创建主路径
	 * @param path
	 */
	public void ChangeMainPath(String path) {
		/* 创建主路径 */
		mMainPath = path;
		File file = new File(mMainPath);
		file.mkdirs();
	}
	
	/**
	 * 获取站点主路径
	 * @return
	 */
	public String GetMainPath() {
		return mMainPath;
	}
	
	/**
	 * LiveChat 发送私密照拍照存储临时路径，当发送返回LCMessage即删除本地路径
	 * @return
	 */
	public String getPrivatePhotoTempSavePath(){
		/*创建图片目录*/
		String path = mMainPath + "/" + LC_TAKE_PHOTO_TEM_DIR + "/";
		File file = new File(path);
		file.mkdir();
		return path;
	}
	
	/**
	 * LiveChat 下载主题sd存放路径
	 * @return
	 */
	public String getThemeSavePath(){
		/*创建图片目录*/
		String path = mMainPath + "/" + LC_THEME_DIR + "/";
		File file = new File(path);
		file.mkdir();
		return path;
	}
	
	/**
	 * 获取EMF拍照目录
	 * @return
	 */
	private String GetEMFPath() {
		/* 创建图片目录 */
		String path = mMainPath + "/" + EMF + "/";
		File file = new File(path);
		file.mkdirs();
		return path;
	}
	
	/**
	 * 获取EMF微视频缓存路径
	 * @return
	 */
	public String GetEMFVideoPath() {
		/* 创建图片目录 */
		String path = mMainPath + "/" + EMF_VIDEO + "/";
		File file = new File(path);
		file.mkdirs();
		return path;
	}
	
	/**
	 * 获取http cache目录
	 * @return
	 */
	public String GetHttpPath() {
		/* 创建图片目录 */
		String path = mMainPath + "/" + HTTP + "/";
		File file = new File(path);
		file.mkdirs();
		return path;
	}
	
	/**
	 * 获取临时目录
	 * @return
	 */
	public String GetTempPath() {
		String path = mMainPath + "/" + TEMP + "/";
		File file = new File(path);
		file.mkdirs();
		return path;
	}
	
	/**
	 * 获取图片目录
	 * @return
	 */
	private String GetImagePath() {
		/* 创建图片目录 */
		String path = mMainPath + "/" + IMAGE_DIR + "/";
		File file = new File(path);
		file.mkdirs();
		return path;
	}
	
	/**
	 * 获取女士(图片)目录
	 * @return
	 */
	private String GetLadyPath() {
		/* 创建女士(图片)目录 */
		String path = mMainPath + "/" + LADY_DIR + "/";
		File file = new File(path);
		file.mkdirs();
		return path;
	}
	
	/**
	 * 获取图片目录
	 * @return
	 */
	private String GetPrivatePhotoPath() {
		/* 创建私密照路径 */
		String path = mMainPath + "/" + PRIVATE_PHOTO + "/";
		File file = new File(path);
		file.mkdirs();
		
		return path;
	}
	
	/**
	 * 获取虚拟礼物目录
	 * @return
	 */
	private String GetVirtualGiftPath() {
		/* 创建虚拟礼物路径 */
		String path = mMainPath + "/" + VIRTUAL_GIFT + "/";
		File file = new File(path);
		file.mkdirs();
		
		return path;
	}
	
	/**
	 * 获取livechat高级表情目录
	 * @return
	 */
	public String GetLCEmotionPath() {
		/* 创建虚拟礼物路径 */
		String path = mMainPath + "/" + LC_EMOTION_DIR + "/";
		File file = new File(path);
		file.mkdirs();
		
		return path;
	}
	
	/**
	 * 获取livechat语音目录
	 * @return
	 */
	public String GetLCVoicePath() {
		/* 创建虚拟礼物路径 */
		String path = mMainPath + "/" + LC_VOICE_DIR + "/";
		File file = new File(path);
		if(!file.exists()){
			file.mkdirs();
		}
		return path;
	}
	
	/**
	 * 获取livechat图片目录
	 * @return
	 */
	public String GetLCPhotoPath() {
		/* 创建虚拟礼物路径 */
		String path = mMainPath + "/" + LC_PHTOT_DIR + "/";
		File file = new File(path);
		file.mkdirs();
		
		return path;
	}
	
	/**
	 * 获取livechat视频目录
	 * @return
	 */
	public String GetLCVideoPath() {
		/* 创建虚拟礼物路径 */
		String path = mMainPath + "/" + LC_VIDEO_DIR + "/";
		File file = new File(path);
		file.mkdirs();
		
		return path;
	}
	
	/**
	 * 获取livechat小高情目录
	 * @return
	 */
	public String GetLCMagicIconPath() {
		/* 创建虚拟礼物路径 */
		String path = mMainPath + "/" + LC_MAGICICON_DIR + "/";
		File file = new File(path);
		file.mkdirs();
		
		return path;
	}
	
	/**
	 * 获取crash日志保存路径
	 * @return
	 */
	public String GetCrashInfoPath() {
		/* 创建crash日志路径*/
		String path = mMainPath + "/" + CRASH + "/";
		File file = new File(path);
		file.mkdirs();
		
		return path;
	}
	
	/**
	 * 获取log目录路径
	 * @return
	 */
	public String GetLogPath() {
		String path = mMainPath + "/" + LOG_DIR + "/";
		File file = new File(path);
		file.mkdirs();
		
		return path;
	}
	
	
	
	/**
	 * 根据私密照 发送ID 和 照片ID 获取图片缓存路径
	 * @param sendId		发送ID
	 * @param photoId		照片ID
	 * @return				缓存路径
	 */
	public String CachePrivatePhotoImagePath(String sendId, String photoId, PrivatePhotoType type, PrivatePhotoMode mode) {
		String path = "";
		String name = "";
		
		if( sendId != null && sendId.length() > 0 && photoId != null && photoId.length() > 0 ) {
			path = GetPrivatePhotoPath();
			name = Arithmetic.MD5(sendId.getBytes(), sendId.getBytes().length);
			name += photoId;
			name = Arithmetic.MD5(name.getBytes(), name.getBytes().length);
			name += ".";
			name += type.name();
			name += "_";
			name += mode.name();
			path += name;
		}
		
		return path;
	}
	
	/**
	 * 根据url获取图片缓存路径
	 * @param url			网址
	 * @return				缓存路径
	 */
	public String CacheVirtualGiftImagePath(String url) {
		String path = "";
		String name = "";
		
		if( url != null && url.length() > 0  ) {
			path = GetVirtualGiftPath();
			name = Arithmetic.MD5(url.getBytes(), url.getBytes().length);
			path += name;
			path += ".thumb";
		}
		
		return path;
	}
	
	/**
	 * 根据url获取视频缓存路径
	 * @param url			网址
	 * @return				缓存路径
	 */
	public String CacheVirtualGiftVideoPath(String url) {
		String path = "";
		String name = "";
		
		if( url != null && url.length() > 0  ) {
			path = GetVirtualGiftPath();
			name = Arithmetic.MD5(url.getBytes(), url.getBytes().length);
			path += name;
		}
		
		return path;
	}
	
	/**
	 * 根据url获取图片缓存路径
	 * @param url			网址
	 * @return				缓存路径
	 */
	public String CacheImagePathFromUrl(String url) {
		String path = "";
		String name = "";
		
		if( url != null && url.length() > 0 ) {
			path = GetImagePath();
			name = Arithmetic.MD5(url.getBytes(), url.getBytes().length);
			path += name;
		}
		
		return path;
	}
	
	
	public enum LadyFileType {
		/**
		 * 女士头像
		 */
		LADY_PHOTO
	}
	/**
	 * 根据url获取图片缓存路径
	 * @param womanId		女士ID
	 * @param fileType		文件类型
	 * @return				缓存路径
	 */
	public String CacheLadyPathFromUrl(String womanId, LadyFileType fileType) {
		String path = "";
		String name = "";
		
		if( womanId != null && !womanId.isEmpty() ) {
			path = GetLadyPath();
			String strMd5 = womanId;
			name = Arithmetic.MD5(strMd5.getBytes(), strMd5.getBytes().length);
			name += "_" + fileType.name();
			path += name;
		}
		
		return path;
	}
	
	/**
	 * 根据url获取清除图片缓存
	 * @param url
	 */
	public void CleanCacheImageFromUrl(String url) {
		String path = CacheImagePathFromUrl(url);
		File file = new File(path);
		if( file.exists() ) {
			file.delete();
		}
	}

	/**
	 * 获取一个临时拍照图片的路径
	 * @return
	 */
	public String GetTempCameraImageUrl() {
		String temp = "";
		temp += GetTempPath() + "cameraphoto.jpg";
		return temp;
	}
	
	/**
	 * 获取一个临时图片的路径
	 * @return
	 */
	public String GetTempImageUrl() {
		String temp = "";
		temp += GetTempPath() + "uploadphoto.jpg";
		return temp;
	}
	
	/**
	 * 获取一个以时间命名EMF拍照图片的路径
	 * @return
	 */
	@SuppressLint("SimpleDateFormat")
	public String GetEMFCameraUrl() {
		String temp ="";
		
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss_SS");
    	String fileName = format.format( new Timestamp( System.currentTimeMillis()) ) + ".jpg";
    	
		temp += GetEMFPath() + fileName;
		return temp;
	}
	
	/**
	 * 删除指定目录下所有文件
	 * @param dirPath 目录路径
	 * @return
	 */
	public void doDelete(String dirPath) {
		delete(new File(dirPath), false);
//		String cmd = "rm -rf " + dirPath;
//		try {
//			Runtime.getRuntime().exec(cmd);
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
	 }
	
	
	/**
	 * 清空缓存路径文件
	 */
	public void ClearCache() {
		delete(new File(GetHttpPath()), false);
		delete(new File(GetImagePath()), false);
		delete(new File(GetTempPath()), false);
		delete(new File(GetEMFPath()), false);
		delete(new File(GetEMFVideoPath()), false);
		delete(new File(GetLadyPath()), false);
		
		delete(new File(GetVirtualGiftPath()), false);
		delete(new File(GetPrivatePhotoPath()), false);
		
		delete(new File(getPrivatePhotoTempSavePath()), false);
		delete(new File(GetLCEmotionPath()), false);
		delete(new File(GetLCPhotoPath()), false);
		delete(new File(GetLCVoicePath()), false);
		delete(new File(GetLCVideoPath()), false);
		
//		String cmd = "rm -rf " + mMainPath;
//		try {
//			Runtime.getRuntime().exec(cmd);
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
	}
	
	/**
	 * 清空虚拟礼物缓存
	 */
	public void ClearVirtualGift() {
		delete(new File(GetVirtualGiftPath()), false);
//		String cmd = "rm -rf " + GetVirtualGiftPath();
//		try {
//			Runtime.getRuntime().exec(cmd);
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
	}
	
	/**
	 * 清空crash log
	 */
	public void ClearCrashLog() {
		delete(new File(GetCrashInfoPath()), false);
//		String cmd = "rm -rf " + GetCrashInfoPath();
//		try {
//			Runtime.getRuntime().exec(cmd);
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
	}
	
	private static void delete(File file, boolean deleteSelf) {
		if ( file != null && file.exists() ) {
			if (file.isFile()) {
				file.delete();
			} else if ( file.isDirectory() ) {
				File[] files = file.listFiles();
				if(files != null){
					for (int i = 0; i < files.length; i++) {
						delete(files[i], true);
					}
				}
			}			
		}
		
		if( deleteSelf )
			file.delete();
	}
	
	/**
	 * 清除指定目录下private photo缓存（EMF相关）
	 */
	public void clearAllPrivatePhotoCache(){
		String path = mMainPath + "/" + PRIVATE_PHOTO + "/";
		clearAllFileByDirectory(path);
	}
	
	/**
	 * 清除指定目录下vide缓存（EMF相关）
	 */
	public void clearAllVideoCache(){
		String path = mMainPath + "/" + EMF_VIDEO + "/";
		clearAllFileByDirectory(path);
	}
	
	/**
	 * 清除指定目录下所有文件
	 * @param directory
	 */
	private void clearAllFileByDirectory(String directory){
		if (!directory.isEmpty()){
			String dirPath = directory + "*";
			String cmd = "rm -f " + dirPath;
			try {
				Runtime.getRuntime().exec(cmd);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}	
		}
	}
	
	
}
