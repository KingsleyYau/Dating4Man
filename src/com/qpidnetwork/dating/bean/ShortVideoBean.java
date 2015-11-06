package com.qpidnetwork.dating.bean;

import com.qpidnetwork.request.item.EMFShortVideoItem;

import android.os.Parcel;
import android.os.Parcelable;

public class ShortVideoBean extends EMFShortVideoItem implements Parcelable{

	
	public String messageid;
	public String womanid;
	
	public ShortVideoBean(){
		
	}
	
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		// TODO Auto-generated method stub
		dest.writeString(messageid);
		dest.writeString(womanid);
		dest.writeString(sendId);
		dest.writeString(videoId);
		dest.writeByte((byte) (videoFee ? 1 : 0));
		dest.writeString(videoDesc);
	}
	
	public static final Parcelable.Creator<ShortVideoBean> CREATOR = new Parcelable.Creator<ShortVideoBean>() {
		public ShortVideoBean createFromParcel(Parcel in) {
			return new ShortVideoBean(in);
		}
		public ShortVideoBean[] newArray(int size) {
			return new ShortVideoBean[size];
		}
	};
		     
	private ShortVideoBean(Parcel in) {
		messageid = in.readString();
		womanid = in.readString();
		sendId = in.readString();
		videoId = in.readString();
		videoFee = in.readByte() != 0;
		videoDesc = in.readString();
	}

}
