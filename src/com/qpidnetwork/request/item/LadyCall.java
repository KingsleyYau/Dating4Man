package com.qpidnetwork.request.item;

public class LadyCall {
	public LadyCall() {
		
	}
	/**
	 * 4.7.获取女士Direct Call TokenID成功回调
	 * @param womanid				被呼叫女士ID
	 * @param lovecallid			被呼叫女士TokenID
	 * @param lc_centernumber		call center number
	 */
	public LadyCall(
		 String womanid,
		 String lovecallid,
		 String lc_centernumber
			) {
		this.womanid = womanid;
		this.lovecallid = lovecallid;
		this.lc_centernumber = lc_centernumber;
	}
	
	public String womanid;
	public String lovecallid;
	public String lc_centernumber;
}
