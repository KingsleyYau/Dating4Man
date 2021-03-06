package com.qpidnetwork.framework.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import android.content.Context;
import android.os.Environment;

public class FileUtil {
	
	
	/**
	 * 判断是否存在应用外部存储
	 * 
	 * @return
	 */
	public static boolean existSdcard() {
		return Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState());
	}
	
	/**
	 * 获取持久化目录
	 * 
	 * @param context
	 * @param type 如："aaa/bbb"
	 * @return 如果不存在SD卡，则返回null, 不建议持久化文件在应用目录下
	 */
	public static File getStoreDir(Context context, String type) {
		if (existSdcard()) {
			return context.getExternalFilesDir(type);
		}
		return null;
	}

	/**
	 * 获取持久化文件
	 * 
	 * @param context
	 * @param type
	 * @param fileName
	 * @return
	 */
	public static File getStoreFile(Context context, String type, String fileName) {
		File dir = getStoreDir(context, type);
		File file = new File(dir, fileName);
		return file;
	}
	
    public static int upZipFile(String zipFilePath, String folderPath)throws ZipException,IOException {
    	ZipFile zfile=new ZipFile(new File(zipFilePath));
    	Enumeration zList=zfile.entries();
    	ZipEntry ze=null;
    	byte[] buf=new byte[1024];
    	while(zList.hasMoreElements()){
    		ze=(ZipEntry)zList.nextElement();    
    		if(ze.isDirectory()){
    			String dirstr = folderPath + ze.getName();
    			dirstr = new String(dirstr.getBytes("8859_1"), "GB2312");
    			Log.d("upZipFile", "str = "+dirstr);
    			File f=new File(dirstr);
    			f.mkdir();
    			continue;
    		}
    		OutputStream os=new BufferedOutputStream(new FileOutputStream(getRealFileName(folderPath, ze.getName())));
    		InputStream is=new BufferedInputStream(zfile.getInputStream(ze));
    		int readLen=0;
    		while ((readLen=is.read(buf, 0, 1024))!=-1) {
    			os.write(buf, 0, readLen);
    		}
    		is.close();
    		os.close();    
    	}
    	zfile.close();
    	return 0;
    }
    
    public static File getRealFileName(String baseDir, String absFileName){
        String[] dirs=absFileName.split("/"); 
        String lastDir=baseDir;
        if(dirs.length>1)
        { 
            for (int i = 0; i < dirs.length-1;i++) 
            { 
                lastDir +=(dirs[i]+"/");
                File dir =new File(lastDir); 
                if(!dir.exists())
                {
                    dir.mkdirs();
                    Log.d("getRealFileName", "create dir = "+(lastDir+"/"+dirs[i]));
                }
            } 
            File ret = new File(lastDir,dirs[dirs.length-1]);
            Log.d("upZipFile", "2ret = "+ret);
            return ret;
        }
        else 
        {
        
            return new File(baseDir,absFileName);
         
        }

    }
}
