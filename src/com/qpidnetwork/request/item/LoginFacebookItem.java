package com.qpidnetwork.request.item;

import java.io.Serializable;

import com.qpidnetwork.request.RequestEnum.Country;

public class LoginFacebookItem implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -899283677195011786L;
	public LoginFacebookItem() {
		this.manid = "";
		this.email = "";
		this.firstname = "";
		this.lastname = "";	
		this.photoURL = "";
		this.reg_step = "";
		this.is_reg = false;
		this.country = Country.values()[0];
		
		this.telephone = "";
		this.telephone_verify = false;
		this.telephone_cc = Country.values()[0];
		
		this.sessionid = "";
		this.ga_uid = "";
		this.ticketid = "";
		
		this.photosend = true;
		this.photoreceived = true;
		this.videoreceived = true;
		
		this.premit = true;
		this.ladyprofile = false;
		this.livechat = false;
		this.admirer = false;
		this.bpemf = false;
		
		this.rechargeCredit = 0;
	}
	/**
	 * 登录成功回调
	 * @param manid				用户id
	 * @param email				电子邮箱
	 * @param firstname			用户first name
	 * @param lastname			用户last name
	 * @param photoURL			头像URL
	 * @param reg_step			已进行的注册步骤数
	 * @param is_reg 			是否注册且登录成功
	 * @param country			国家，参考枚举 <RequestEnum.Country>
	 * @param telephone			手机号码
	 * @param telephone_verify	手机是否已认证
	 * @param telephone_cc		国家区号，参考枚举 <RequestEnum.Country>
	 * @param sessionid			跨服务器的唯一标识
	 * @param ga_uid			Google Analytics UserID参数
	 * @param ticketid			客服邮件强制阅读的事项ID
	 * 
	 * @param photosend			私密照片发送权限
	 * @param photoreceived		私密照片接收权限
	 * @param videoreceived		微视频接收权限（true：允许，false：不能）
	 * 
	 * @param premit			帐号可用状态
	 * @param ladyprofile		女士详细信息风控标识（true：有风控，false：无）
	 * @param livechat			LiveChat详细风控标识（true：有风控，false：无）
	 * @param admirer			意向信风控标识（true：有风控，false：无）
	 * @param bpemf				EMF风控标识（true：有风控，false：无）
	 * 
	 * @param rechargeCredit	自动买点配置（0：无权限，大于0：每次买点的数量）
	 */
	public LoginFacebookItem(
			String manid,
			String email,
			String firstname,
			String lastname,	
			String photoURL,
			String reg_step,
			boolean is_reg,
			int country,
			String telephone,
			boolean telephone_verify,
			int telephone_cc,
			String sessionid,
			String ga_uid,
			String ticketid,
			
			boolean photosend,
			boolean photoreceived,
			boolean videoreceived,
			
			boolean premit,
			boolean ladyprofile,
			boolean livechat,
			boolean admirer,
			boolean bpemf,
			
			int rechargeCredit
			) {
		this.manid = manid;
		this.email = email;
		this.firstname = firstname;
		this.lastname = lastname;	
		this.photoURL = photoURL;
		this.reg_step = reg_step;
		this.is_reg = is_reg;
		
		if( country < 0 || country >= Country.values().length ) {
			this.country = Country.values()[0];
		} else {
			this.country = Country.values()[country];
		}
		
		this.telephone = telephone;
		this.telephone_verify = telephone_verify;
		
		if( telephone_cc < 0 || telephone_cc >= Country.values().length ) {
			this.telephone_cc = Country.values()[0];
		} else {
			this.telephone_cc = Country.values()[telephone_cc];
		}
		
		this.sessionid = sessionid;
		this.ga_uid = ga_uid;
		this.ticketid = ticketid;
		
		this.photosend = photosend;
		this.photoreceived = photoreceived;
		this.videoreceived = videoreceived;
		
		this.premit = premit;
		this.ladyprofile = ladyprofile;
		this.livechat = livechat;
		this.admirer = admirer;
		this.bpemf = bpemf;
		
		this.rechargeCredit = rechargeCredit;
	}
	
	public String manid;
	public String email;
	public String firstname;
	public String lastname;	
	public String photoURL;
	public String reg_step;
	public boolean is_reg;
	public Country country;
	public String telephone;
	public boolean telephone_verify;
	public Country telephone_cc;
	public String sessionid;
	public String ga_uid;
	public String ticketid;
	
	public boolean photosend;
	public boolean photoreceived;
	public boolean videoreceived;
	
	public boolean premit;
	public boolean ladyprofile;
	public boolean livechat;
	public boolean admirer;
	public boolean bpemf;
	
	public int rechargeCredit;
}
