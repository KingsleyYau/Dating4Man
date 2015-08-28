package com.qpidnetwork.dating.quickmatch;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.format.Time;
import android.util.Base64;

import com.qpidnetwork.manager.WebSiteManager.WebSite;
import com.qpidnetwork.request.item.QuickMatchLady;

@SuppressWarnings("deprecation")
public class QuickMatchPerfence {
	/**
	 * 清空缓存
	 * @param context
	 */
	static public void CleanCache(Context context, WebSite website) {
		SaveLastUpdateTime(context, website, null);
		SaveLastIndex(context, website, 0);
		SaveQuickMatchLadyList(context, website, null);
		SaveQuickMatchLadyLocalLikeList(context, website, null);
		SaveQuickMatchLadyLikeList(context, website, null);
		SaveQuickMatchLadyRemoveList(context, website, null);
		SaveQuickMatchLadyUnLikeList(context, website, null);
	}
	
	/**
	 * 缓存最后更新时间
	 * @param context	上下文
	 * @param item		最后更新时间
	 */
	public static void SaveLastUpdateTime(Context context, WebSite website, Time item) {
		SharedPreferences mSharedPreferences = context.getSharedPreferences("base64", Context.MODE_PRIVATE); 
        SharedPreferences.Editor editor = mSharedPreferences.edit();  
        if( website != null ) {
    		if( item != null ) {
    			editor.putLong("LastUpdateTime" + website.getSiteId(), item.toMillis(true));  
    		} else {
    			editor.putLong("LastUpdateTime" + website.getSiteId(), 0);  
    		}
        }
        editor.commit(); 
	} 
	
	/**
	 * 获取缓存最后更新时间
	 * @param context	上下文
	 * @return			最后更新时间
	 */
	public static Time GetLastUpdateTime(Context context, WebSite website) {
		Time item = new Time();
		if( website != null ) {
	        SharedPreferences mSharedPreferences = context.getSharedPreferences("base64", Context.MODE_PRIVATE);  
	        long millis = mSharedPreferences.getLong("LastUpdateTime" + website.getSiteId(), 0);  
	        item.set(millis);
		}
		return item;
	}
	
	/**
	 * 缓存最后选择下标
	 * @param context	上下文
	 * @param item		最后更新时间
	 */
	public static void SaveLastIndex(Context context, WebSite website, int index) {
		if( website != null ) {
			SharedPreferences mSharedPreferences = context.getSharedPreferences("base64", Context.MODE_PRIVATE); 
	        SharedPreferences.Editor editor = mSharedPreferences.edit();  
	        editor.putInt("LastIndex" + website.getSiteId(), index);  
	        editor.commit(); 
		}

	} 
	
	/**
	 * 获取缓存最后选择下标
	 * @param context	上下文
	 * @return			最后更新时间
	 */
	public static int GetLastIndex(Context context, WebSite website) {
		int index = 0;
		if( website != null ) {
	        SharedPreferences mSharedPreferences = context.getSharedPreferences("base64", Context.MODE_PRIVATE);  
	        index = mSharedPreferences.getInt("LastIndex" + website.getSiteId(), 0); 
		}
		return index;
	}
	
	/**
	 * 缓存服务器女士列表
	 * @param context	上下文
	 * @param item		个人资料
	 */
	public static void SaveQuickMatchLadyList(Context context, WebSite website, List<QuickMatchLady> list) {
		SharedPreferences mSharedPreferences = context.getSharedPreferences("base64", Context.MODE_PRIVATE); 
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();  
        ObjectOutputStream oos;
		try {
			if( website != null ) {
				oos = new ObjectOutputStream(baos);
				if( list!= null ) {
					for(QuickMatchLady item : list) {
						oos.writeObject(item); 
					}
				}

		        String personBase64 = new String(Base64.encode(baos.toByteArray(), Base64.DEFAULT));  
		        SharedPreferences.Editor editor = mSharedPreferences.edit();  
		        editor.putString("QuickMatchLadyList" + website.getSiteId(), personBase64);  
		        editor.commit();
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}  
	}
	
	/**
	 * 获取缓存服务器女士列表
	 * @param context	上下文
	 * @return			个人资料
	 */
	public static List<QuickMatchLady> GetQuickMatchLadyList(Context context, WebSite website) {
		List<QuickMatchLady> list = new ArrayList<QuickMatchLady>();
		
        try {  
        	if( website != null ) {
                SharedPreferences mSharedPreferences = context.getSharedPreferences("base64", Context.MODE_PRIVATE);  
                String personBase64 = mSharedPreferences.getString("QuickMatchLadyList" + website.getSiteId(), "");  
                byte[] base64Bytes = Base64.decode(personBase64.getBytes(), Base64.DEFAULT);  
                ByteArrayInputStream bais = new ByteArrayInputStream(base64Bytes);  
                ObjectInputStream ois = new ObjectInputStream(bais);  
                
                QuickMatchLady item = (QuickMatchLady) ois.readObject();  
                while( item != null ) {
                	list.add(item);
                	item = (QuickMatchLady) ois.readObject();
                }
        	}
        } catch (Exception e) {  
            e.printStackTrace();  
        }  
        
		return list;
	}
	
	/**
	 * 缓存本地喜爱女士列表
	 * @param context	上下文
	 * @param item		个人资料
	 */
	public static void SaveQuickMatchLadyLocalLikeList(Context context, WebSite website, List<QuickMatchLady> list) {
		SharedPreferences mSharedPreferences = context.getSharedPreferences("base64", Context.MODE_PRIVATE); 
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();  
        ObjectOutputStream oos;
		try {
			if( website != null ) {
				oos = new ObjectOutputStream(baos);
				
				if( list != null ) {
					for(QuickMatchLady item : list) {
						oos.writeObject(item); 
					}
				}

		        String personBase64 = new String(Base64.encode(baos.toByteArray(), Base64.DEFAULT));  
		        SharedPreferences.Editor editor = mSharedPreferences.edit();  
		        editor.putString("QuickMatchLadyLocalLikeList" + website.getSiteId(), personBase64);  
		        editor.commit();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}  
	}
	
	/**
	 * 获取缓存本地喜爱女士列表
	 * @param context	上下文
	 * @return			个人资料
	 */
	public static List<QuickMatchLady> GetQuickMatchLadyLocalLikeList(Context context, WebSite website) {
		List<QuickMatchLady> list = new ArrayList<QuickMatchLady>();
		
        try {  
        	if( website != null ) {
                SharedPreferences mSharedPreferences = context.getSharedPreferences("base64", Context.MODE_PRIVATE);  
                String personBase64 = mSharedPreferences.getString("QuickMatchLadyLocalLikeList" + website.getSiteId(), "");  
                byte[] base64Bytes = Base64.decode(personBase64.getBytes(), Base64.DEFAULT);  
                ByteArrayInputStream bais = new ByteArrayInputStream(base64Bytes);  
                ObjectInputStream ois = new ObjectInputStream(bais);  
                
                QuickMatchLady item = (QuickMatchLady) ois.readObject();  
                while( item != null && website != null ) {
                	list.add(item);
                	item = (QuickMatchLady) ois.readObject();
                }
        	}
        } catch (Exception e) {  
            e.printStackTrace();  
        }  
        
		return list;
	}
	
	/**
	 * 缓存服务器喜爱女士列表
	 * @param context	上下文
	 * @param item		个人资料
	 */
	public static void SaveQuickMatchLadyLikeList(Context context, WebSite website, List<QuickMatchLady> list) {
		SharedPreferences mSharedPreferences = context.getSharedPreferences("base64", Context.MODE_PRIVATE); 
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();  
        ObjectOutputStream oos;
		try {
			if( website != null ) {
				oos = new ObjectOutputStream(baos);
				
				if( list != null && website != null ) {
					for(QuickMatchLady item : list) {
						oos.writeObject(item); 
					}
				}

		        String personBase64 = new String(Base64.encode(baos.toByteArray(), Base64.DEFAULT));  
		        SharedPreferences.Editor editor = mSharedPreferences.edit();  
		        editor.putString("QuickMatchLadyLikeList" + website.getSiteId(), personBase64);  
		        editor.commit();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}  
	}
	
	/**
	 * 获取缓存服务器喜爱女士列表
	 * @param context	上下文
	 * @return			个人资料
	 */
	public static List<QuickMatchLady> GetQuickMatchLadyLikeList(Context context, WebSite website) {
		List<QuickMatchLady> list = new ArrayList<QuickMatchLady>();
		
        try {  
        	if( website != null ) {
                SharedPreferences mSharedPreferences = context.getSharedPreferences("base64", Context.MODE_PRIVATE);  
                String personBase64 = mSharedPreferences.getString("QuickMatchLadyLikeList" + website.getSiteId(), "");  
                byte[] base64Bytes = Base64.decode(personBase64.getBytes(), Base64.DEFAULT);  
                ByteArrayInputStream bais = new ByteArrayInputStream(base64Bytes);  
                ObjectInputStream ois = new ObjectInputStream(bais);  
                
                QuickMatchLady item = (QuickMatchLady) ois.readObject();  
                while( item != null && website != null ) {
                	list.add(item);
                	item = (QuickMatchLady) ois.readObject();
                }
        	}
        } catch (Exception e) {  
            e.printStackTrace();  
        }  
        
		return list;
	}
	
	/**
	 * 缓存需要服务器删除的喜爱女士列表
	 * @param context	上下文
	 * @param item		个人资料
	 */
	public static void SaveQuickMatchLadyRemoveList(Context context, WebSite website, List<QuickMatchLady> list) {
		SharedPreferences mSharedPreferences = context.getSharedPreferences("base64", Context.MODE_PRIVATE); 
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();  
        ObjectOutputStream oos;
		try {
			if( website != null ) {
				oos = new ObjectOutputStream(baos);
				
				if( list != null && website != null ) {
					for(QuickMatchLady item : list) {
						oos.writeObject(item); 
					}
				}

		        String personBase64 = new String(Base64.encode(baos.toByteArray(), Base64.DEFAULT));  
		        SharedPreferences.Editor editor = mSharedPreferences.edit();  
		        editor.putString("QuickMatchLadyRemoveList" + website.getSiteId(), personBase64);  
		        editor.commit();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}  
	}
	
	/**
	 * 获取缓存需要服务器删除的喜爱女士列表
	 * @param context	上下文
	 * @return			个人资料
	 */
	public static List<QuickMatchLady> GetQuickMatchLadyRemoveList(Context context, WebSite website) {
		List<QuickMatchLady> list = new ArrayList<QuickMatchLady>();
		
        try {  
        	if( website != null ) {
                SharedPreferences mSharedPreferences = context.getSharedPreferences("base64", Context.MODE_PRIVATE);  
                String personBase64 = mSharedPreferences.getString("QuickMatchLadyRemoveList" + website.getSiteId(), "");  
                byte[] base64Bytes = Base64.decode(personBase64.getBytes(), Base64.DEFAULT);  
                ByteArrayInputStream bais = new ByteArrayInputStream(base64Bytes);  
                ObjectInputStream ois = new ObjectInputStream(bais);  
                
                QuickMatchLady item = (QuickMatchLady) ois.readObject();  
                while( item != null && website != null ) {
                	list.add(item);
                	item = (QuickMatchLady) ois.readObject();
                }
        	}
        } catch (Exception e) {  
            e.printStackTrace();  
        }  
        
		return list;
	}
	
	/**
	 * 缓存本地不喜爱女士列表
	 * @param context	上下文
	 * @param item		个人资料
	 */
	public static void SaveQuickMatchLadyUnLikeList(Context context, WebSite website, List<QuickMatchLady> list) {
		SharedPreferences mSharedPreferences = context.getSharedPreferences("base64", Context.MODE_PRIVATE); 
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();  
        ObjectOutputStream oos;
		try {
			if( website != null ) {
				oos = new ObjectOutputStream(baos);
				
				if( list != null && website != null ) {
					for(QuickMatchLady item : list) {
						oos.writeObject(item); 
					}
				}

		        String personBase64 = new String(Base64.encode(baos.toByteArray(), Base64.DEFAULT));  
		        SharedPreferences.Editor editor = mSharedPreferences.edit();  
		        editor.putString("QuickMatchLadyUnLikeList" + website.getSiteId(), personBase64);  
		        editor.commit();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}  
	}
	
	/**
	 * 获取缓存本地不喜爱女士列表
	 * @param context	上下文
	 * @return			个人资料
	 */
	public static List<QuickMatchLady> GetQuickMatchLadyUnLikeList(Context context, WebSite website) {
		List<QuickMatchLady> list = new ArrayList<QuickMatchLady>();
		
        try {  
        	if( website != null ) {
                SharedPreferences mSharedPreferences = context.getSharedPreferences("base64", Context.MODE_PRIVATE);  
                String personBase64 = mSharedPreferences.getString("QuickMatchLadyUnLikeList" + website.getSiteId(), "");  
                byte[] base64Bytes = Base64.decode(personBase64.getBytes(), Base64.DEFAULT);  
                ByteArrayInputStream bais = new ByteArrayInputStream(base64Bytes);  
                ObjectInputStream ois = new ObjectInputStream(bais);  
                
                QuickMatchLady item = (QuickMatchLady) ois.readObject();  
                while( item != null && website != null ) {
                	list.add(item);
                	item = (QuickMatchLady) ois.readObject();
                }
        	}

        } catch (Exception e) {  
            e.printStackTrace();  
        }  
        
		return list;
	}
}
