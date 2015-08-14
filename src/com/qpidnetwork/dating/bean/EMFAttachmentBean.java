package com.qpidnetwork.dating.bean;

import android.os.Parcel;
import android.os.Parcelable;

public class EMFAttachmentBean implements Parcelable{
	
	public AttachType type;
	public String photoUrl;
	public String vgId;
	public PrivatePhotoBean privatePhoto;
	
	public String photoLocalUrl;//用于编辑邮件时，附件预览
	
	public EMFAttachmentBean(){
		privatePhoto = new PrivatePhotoBean();
	}

	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		// TODO Auto-generated method stub
		dest.writeInt(type.ordinal());
		dest.writeString(photoUrl);
		dest.writeString(vgId);
		dest.writeParcelable(privatePhoto, PARCELABLE_WRITE_RETURN_VALUE);
		dest.writeString(photoLocalUrl);
	}
	
	public static final Parcelable.Creator<EMFAttachmentBean> CREATOR = new Parcelable.Creator<EMFAttachmentBean>() {
		public EMFAttachmentBean createFromParcel(Parcel in) {
			return new EMFAttachmentBean(in);
		}
		public EMFAttachmentBean[] newArray(int size) {
			return new EMFAttachmentBean[size];
		}
	};
		     
	private EMFAttachmentBean(Parcel in) {
		type = AttachType.values()[in.readInt()];
		photoUrl = in.readString();
		vgId = in.readString();
		privatePhoto = in.readParcelable(PrivatePhotoBean.class.getClassLoader());
		photoLocalUrl = in.readString();
	}
	
	public enum AttachType{
		NORAML_PICTURE,
		PRIVATE_PHOTO,
		VIRTUAL_GIFT
	}

}
