package com.qpidnetwork.dating.bean;

import android.os.Parcel;
import android.os.Parcelable;

import com.qpidnetwork.request.item.LoveCall;

public class LoveCallBean extends LoveCall implements Parcelable{
	
	public LoveCallBean(){
		
	}
	
	public LoveCallBean(LoveCall item){
		orderid = item.orderid;
		womanid = item.womanid;
		image = item.image;
		firstname = item.firstname;
		country = item.country;
		age = item.age;
		begintime = item.begintime;
		endtime = item.endtime;
		longbegintime = item.longbegintime;
		longendtime = item.longendtime;
		needtr = item.needtr;
		isconfirm = item.isconfirm;
		confirmmsg = item.confirmmsg;
		callid = item.callid;
		centerid = item.centerid;
	}

	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		// TODO Auto-generated method stub
		dest.writeString(orderid);
		dest.writeString(womanid);
		dest.writeString(image);
		dest.writeString(firstname);
		dest.writeString(country);
		dest.writeInt(age);
		dest.writeInt(begintime);
		dest.writeInt(endtime);
		dest.writeLong(longbegintime);
		dest.writeLong(longendtime);
		dest.writeByte((byte) (needtr ? 1 : 0)); 
		dest.writeByte((byte) (isconfirm ? 1 : 0)); 
		dest.writeString(confirmmsg);
		dest.writeString(callid);
		dest.writeString(centerid);
	}
	
	public static final Parcelable.Creator<LoveCallBean> CREATOR = new Parcelable.Creator<LoveCallBean>() {
		public LoveCallBean createFromParcel(Parcel in) {
			return new LoveCallBean(in);
		}
		public LoveCallBean[] newArray(int size) {
			return new LoveCallBean[size];
		}
	};
		     
	private LoveCallBean(Parcel in) {
		orderid = in.readString();
		womanid = in.readString();
		image = in.readString();
		firstname = in.readString();
		country = in.readString();
		age = in.readInt();
		begintime = in.readInt();
		endtime = in.readInt();
		longbegintime = in.readLong();
		longendtime = in.readLong();
		needtr = in.readByte() != 0; 
		isconfirm = in.readByte() != 0; 
		confirmmsg = in.readString();
		callid = in.readString();
		centerid = in.readString();
	}

}
