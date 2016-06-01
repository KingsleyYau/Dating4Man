package com.qpidnetwork.request.item;

import com.qpidnetwork.request.RequestJniEMF.ReplyFlagType;

public class EMFAdmirerListItem {	
	public EMFAdmirerListItem() {
	}
	 /**
	  * 
	  * @param id			意向信ID
	  * @param idcode		意向信外部ID
	  * @param readflag		是否未读
	  * @param replyflag	回复状态
	  * @param womanid		女士ID
	  * @param firstname	女士first name
	  * @param lastname		女士last name
	  * @param weight		体重
	  * @param height		身高
	  * @param country		国家
	  * @param province		省份
	  * @param adddate1		同sendTime参数
	  * @param mtab			表的后缀
	  * @param age			年龄
	  * @param photoURL		头像URL
	  * @param sendTime		发送时间
	  * @param attachnum	附件数量
	  */
	public EMFAdmirerListItem(
		 String id,
		 String idcode,
		 boolean readflag,
		 int replyflag,
		 String womanid,
		 String firstname,
		 String weight,
		 String height,
		 String country,
		 String province,
		 String mtab,
		 int age,
		 String photoURL,
		 String sendTime,
		 int attachnum,
		 int templateType
		 	) {
		this.id = id;
		this.idcode = idcode;
		this.readflag = readflag;
		this.replyflag = ReplyFlagType.values()[replyflag];
		this.womanid = womanid;
		this.firstname = firstname;
		this.weight = weight;
		this.height = height;
		this.country = country;
		this.province = province;
		this.mtab = mtab;
		this.age = age;
		this.photoURL = photoURL;
		this.sendTime = sendTime;
		this.attachnum = attachnum;
		
		if( templateType < 0 || templateType >= TemplateType.values().length ) {
			this.mTemplateType = TemplateType.values()[0];
		} else {
			this.mTemplateType = TemplateType.values()[templateType];
		}
	}
	
	public String id;
	public String idcode;
	public boolean readflag;
	public ReplyFlagType replyflag;
	public String womanid;
	public String firstname;
	public String weight;
	public String height;
	public String country;
	public String province;
	public String mtab;
	public int age;
	public String photoURL;
	public String sendTime;
	public int attachnum;
	
	public enum TemplateType {
		Text,
		VirtualGift
	}
	public TemplateType mTemplateType;
	
	@Override
	public boolean equals(Object o) {
		if((o != null)&&(o instanceof EMFAdmirerListItem)){
			EMFAdmirerListItem object = (EMFAdmirerListItem)o;
			return (object.id.equals(id));
		}
		return super.equals(o);
	}
}
