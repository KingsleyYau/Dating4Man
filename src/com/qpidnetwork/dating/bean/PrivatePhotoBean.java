package com.qpidnetwork.dating.bean;

import com.qpidnetwork.request.item.EMFPrivatePhotoItem;

import android.os.Parcel;
import android.os.Parcelable;

public class PrivatePhotoBean extends EMFPrivatePhotoItem implements Parcelable{
	
	public String messageid;
	public String womanid;
	
	public PrivatePhotoBean(){
		
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
		dest.writeString(photoId);
		dest.writeByte((byte) (photoFee ? 1 : 0));
		dest.writeString(photoDesc);
	}
	
	public static final Parcelable.Creator<PrivatePhotoBean> CREATOR = new Parcelable.Creator<PrivatePhotoBean>() {
		public PrivatePhotoBean createFromParcel(Parcel in) {
			return new PrivatePhotoBean(in);
		}
		public PrivatePhotoBean[] newArray(int size) {
			return new PrivatePhotoBean[size];
		}
	};
		     
	private PrivatePhotoBean(Parcel in) {
		messageid = in.readString();
		womanid = in.readString();
		sendId = in.readString();
		photoId = in.readString();
		photoFee = in.readByte() != 0;
		photoDesc = in.readString();
	}
}
