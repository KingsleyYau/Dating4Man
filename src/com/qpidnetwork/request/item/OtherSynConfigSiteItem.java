package com.qpidnetwork.request.item;

public class OtherSynConfigSiteItem {
	public OtherSynConfigSiteItem() {
		
	}

	/**
	 * 
	 * @param host			LiveChat站点host
	 * @param proxyHost		LiveChat代理host列表
	 * @param port			LiveChat端口
	 * @param minChat		LiveChat所需最少点数
	 * @param minEmf		EMF所需最少点数
	 * @param countryList	站点可查询的女士国家列表
	 */
	public OtherSynConfigSiteItem(
			String host,
			String[] proxyHost,
			int port,
			double minChat,
			double minEmf,
			String[] countryList
			) {
		this.host = host;
		this.proxyHost = proxyHost;
		this.port = port;
		this.minChat = minChat;
		this.minEmf = minEmf;
		this.countryList = countryList;
	}
	
	public String host;
	public String[] proxyHost;
	public int port;
	public double minChat;
	public double minEmf;
	public String[] countryList;
}
