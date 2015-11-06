package com.qpidnetwork.dating.analysis;

import java.io.Serializable;

public class AnalysisItem implements Serializable{
	
	private static final long serialVersionUID = -8271879745627541123L;
	
	public String utm_referrer = ""; //广播utm Reference
	public boolean isSummit = true; //是否已上传
	public int installTime = 0;//安装时间 unix 时间戳
	public int versionCode = 0;//记录当前安装应用版本号
	
	public AnalysisItem(){
		
	}
}
