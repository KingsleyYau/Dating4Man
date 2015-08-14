package com.qpidnetwork.request.item;

import com.qpidnetwork.request.RequestJniEMF.BlockReasonType;

public class EMFBlockListItem {	
	public EMFBlockListItem() {
	}
	 /**
	  * 
	  * @param womanid		女士ID
	  * @param firstname	女士first name
	  * @param age			年龄
	  * @param weight		体重
	  * @param height		身高
	  * @param country		国家
	  * @param province		省份
	  * @param city			城市
	  * @param photoURL		头像URL
	  * @param blockreason	加入黑名单原因
	  */
	public EMFBlockListItem(
		 String womanid,
		 String firstname,
		 int age,
		 String weight,
		 String height,
		 String country,
		 String province,
		 String city,
		 String photoURL,
		 int blockreason
		 	) {
		this.womanid = womanid;
		this.firstname = firstname;
		this.age = age;
		this.weight = weight;
		this.height = height;
		this.country = country;
		this.province = province;
		this.city = city;
		this.photoURL = photoURL;
		this.blockreason = BlockReasonType.values()[blockreason];
	}
	
	public String womanid;
	public String firstname;
	public int age;
	public String weight;
	public String height;
	public String country;
	public String province;
	public String city;
	public String photoURL;
	public BlockReasonType blockreason;
	
	@Override
	public boolean equals(Object o) {
		if((o != null)&&(o instanceof EMFBlockListItem)){
			EMFBlockListItem object = (EMFBlockListItem)o;
			return (object.womanid.equals(womanid));
		}
		return super.equals(o);
	}
}
