package com.qpidnetwork.request.item;

import android.os.Parcel;
import android.os.Parcelable;


public class LoginErrorItem  implements Parcelable {
	public LoginErrorItem() {
		email = "";
		firstname = "";
		lastname = "";	
		photoURL = "";
	}

	/**
	 * 登录错误回调
	 * @param email
	 * @param firstname
	 * @param lastname
	 * @param photoURL
	 */
	public LoginErrorItem(
			String email,
			String firstname,
			String lastname,	
			String photoURL
			) {
		this.email = email;
		this.firstname = firstname;
		this.lastname = lastname;	
		this.photoURL = photoURL;
	}
	
	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		// TODO Auto-generated method stub
    	 dest.writeString(email);
    	 dest.writeString(firstname);
    	 dest.writeString(lastname);
    	 dest.writeString(photoURL);
	}
	
    private LoginErrorItem(Parcel in) {
    	 email = in.readString();
    	 firstname = in.readString();
    	 lastname = in.readString();
    	 photoURL = in.readString();
     }
     
    public static final Parcelable.Creator<LoginErrorItem> CREATOR = new Parcelable.Creator<LoginErrorItem>() {
    	public LoginErrorItem createFromParcel(Parcel in) {
            return new LoginErrorItem(in);
        }

        public LoginErrorItem[] newArray(int size) {
            return new LoginErrorItem[size];
        }
     };
     
	public String email;
	public String firstname;
	public String lastname;	
	public String photoURL;

}
