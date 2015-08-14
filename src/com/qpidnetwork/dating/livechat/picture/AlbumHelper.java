package com.qpidnetwork.dating.livechat.picture;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.qpidnetwork.framework.util.StringUtil;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore.Images.Media;
import android.provider.MediaStore.Images.Thumbnails;

/**
 * 系统图库工具类,获取系统图片列表
 * @author Hunter
 * @since 2015.5.6
 */
public class AlbumHelper {
	
	final String TAG = getClass().getSimpleName();
	
	Context context;
	ContentResolver cr;
	// 缩略图列表
	HashMap<String, String> thumbnailList = new HashMap<String, String>();
	
	
	public AlbumHelper(Context context) {
		this.context = context;
		cr = context.getContentResolver();
	}
	
	public List<ImageBean> getAlbumImageList(){
		List<ImageBean> albumList = new ArrayList<ImageBean>();
		// 构造缩略图索引
		getThumbnail();
		
		// 构造相册索引
		String columns[] = new String[] { Media._ID, Media.BUCKET_ID,
				Media.PICASA_ID, Media.DATA, Media.DISPLAY_NAME, Media.TITLE,
				Media.SIZE, Media.BUCKET_DISPLAY_NAME };
		// 得到一个游标
		Cursor cur = cr.query(Media.EXTERNAL_CONTENT_URI, columns, null, null,
				null);
		if(cur != null){
			if (cur.moveToFirst()) {
				// 获取指定列的索引
				int photoIDIndex = cur.getColumnIndexOrThrow(Media._ID);
				int photoPathIndex = cur.getColumnIndexOrThrow(Media.DATA);
			
				do {
					String _id = cur.getString(photoIDIndex);
					String path = cur.getString(photoPathIndex);
					ImageBean item = new ImageBean();
					item.imageId = _id;
					item.imagePath = path;
					item.thumbnailPath = thumbnailList.get(_id);
					/*无thumb图片，不加入可显示列表*/
					if(!StringUtil.isEmpty(item.thumbnailPath)){
						/*加入图片列表前清除图片thumb，每次使用src显示，防止图片不清晰*/
						item.thumbnailPath = "";
						albumList.add(item);
					}
	
				} while (cur.moveToNext());
			}
		}
		return albumList;
	}
	
	/**
	 * 得到缩略图
	 */
	private void getThumbnail() {
		String[] projection = { Thumbnails._ID, Thumbnails.IMAGE_ID,
				Thumbnails.DATA };
		Cursor cursor = cr.query(Thumbnails.EXTERNAL_CONTENT_URI, projection,
				null, null, null);
		getThumbnailColumnData(cursor);
	}
	
	/**
	 * 从数据库中得到缩略图
	 * 
	 * @param cur
	 */
	private void getThumbnailColumnData(Cursor cur) {
		if (cur.moveToFirst()) {
			int _id;
			int image_id;
			String image_path;
			int _idColumn = cur.getColumnIndex(Thumbnails._ID);
			int image_idColumn = cur.getColumnIndex(Thumbnails.IMAGE_ID);
			int dataColumn = cur.getColumnIndex(Thumbnails.DATA);

			do {
				_id = cur.getInt(_idColumn);
				image_id = cur.getInt(image_idColumn);
				image_path = cur.getString(dataColumn);
				thumbnailList.put("" + image_id, image_path);
			} while (cur.moveToNext());
		}
	}
}
