package com.qpidnetwork.dating.bean;

import android.os.Parcel;
import android.os.Parcelable;

import com.qpidnetwork.request.item.EMFAdmirerListItem;
import com.qpidnetwork.request.item.EMFInboxListItem;
import com.qpidnetwork.request.item.EMFOutboxListItem;

/**
 * 接口二次封装，用于界面数据传输显示使用
 * @author Hunter
 * @since 2015.4.20
 */
public class EMFBean implements Parcelable{
	
	public String id; //邮件ID
	public String womanid;
	public String firstname;
	public int age;
	public String country;
	public String photoURL;
	public String sendTime;
	public String mtab;
	public int type ; //邮件类型（0.收件箱  1.发件箱  2.意向信收件箱 ） 
	
	public EMFBean(EMFAdmirerListItem admirerItem){
		this.id = admirerItem.id;
		this.womanid = admirerItem.womanid;
		this.firstname = admirerItem.firstname;
		this.age = admirerItem.age;
		this.country = admirerItem.country;
		this.photoURL = admirerItem.photoURL;
		this.sendTime = admirerItem.sendTime;
		this.mtab = admirerItem.mtab;
		this.type = 2;
	}
	
	public EMFBean(EMFInboxListItem inboxItem){
		this.id = inboxItem.id;
		this.womanid = inboxItem.womanid;
		this.firstname = inboxItem.firstName;
		this.age = inboxItem.age;
		this.country = inboxItem.country;
		this.photoURL = inboxItem.photoURL;
		this.sendTime = inboxItem.sendTime;
		this.type = 0;
	}
	
	public EMFBean(EMFOutboxListItem outboxItem){
		this.id = outboxItem.id;
		this.womanid = outboxItem.womanid;
		this.firstname = outboxItem.firstName;
		this.age = outboxItem.age;
		this.country = outboxItem.country;
		this.photoURL = outboxItem.photoURL;
		this.sendTime = outboxItem.sendTime;
		this.type = 1;
	}
	
	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		// TODO Auto-generated method stub
		dest.writeString(id);
		dest.writeString(womanid);
		dest.writeString(firstname);
		dest.writeInt(age);
		dest.writeString(country);
		dest.writeString(photoURL);
		dest.writeString(sendTime);
		dest.writeInt(type);
	}
	
	public static final Parcelable.Creator<EMFBean> CREATOR = new Parcelable.Creator<EMFBean>() {
		public EMFBean createFromParcel(Parcel in) {
			return new EMFBean(in);
		}
		public EMFBean[] newArray(int size) {
			return new EMFBean[size];
		}
	};
		     
	private EMFBean(Parcel in) {
		id = in.readString();
		womanid = in.readString();
		firstname = in.readString();
		age = in.readInt();
		country = in.readString();
		photoURL = in.readString();
		sendTime = in.readString();
		type = in.readInt(); 
	}

}
