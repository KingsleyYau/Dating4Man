package com.qpidnetwork.request.item;

public class OtherSynConfigItem {
	public OtherSynConfigItem() {
		
	}

	/**
	 * 
	 * @param cl	CL站点配置
	 * @param ida	IDA站点配置
	 * @param ch	CH站点配置
	 * @param la	LA站点配置
	 * @param pub	公共配置
	 */
	public OtherSynConfigItem(
			OtherSynConfigSiteItem cl,
			OtherSynConfigSiteItem ida,
			OtherSynConfigSiteItem ch,
			OtherSynConfigSiteItem la,
			OtherSynConfigPublicItem pub
			) {
		this.cl = cl;
		this.ida = ida;
		this.ch = ch;
		this.la = la;
		this.pub = pub;
	}
	
	public OtherSynConfigSiteItem cl;
	public OtherSynConfigSiteItem ida;
	public OtherSynConfigSiteItem ch;
	public OtherSynConfigSiteItem la;
	public OtherSynConfigPublicItem pub;
}
