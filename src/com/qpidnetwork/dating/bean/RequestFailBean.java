package com.qpidnetwork.dating.bean;

public class RequestFailBean {
	
	public String errno;
	public String errmsg;
	
	public RequestFailBean(String errno, String errmsg){
		this.errno = errno;
		this.errmsg = errmsg;
	}
}
