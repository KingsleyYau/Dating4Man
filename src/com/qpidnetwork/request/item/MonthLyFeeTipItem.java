package com.qpidnetwork.request.item;

import java.io.Serializable;

import com.qpidnetwork.request.RequestJniMonthlyFee;
import com.qpidnetwork.request.RequestJniMonthlyFee.MemberType;

public class MonthLyFeeTipItem implements Serializable{

	private static final long serialVersionUID = 7120978351159031158L;
	
	private static final String firstPriceTitle = "Go Premium to Get All Benefits for only <font color=green>$%s</font> today!";//第一次缴月费
	
	private static final String normalPriceTitle = "<small>Your premium membership is inactive.</small><br/><font color=black><b>Activate to get all benefits</b></font>";//非第一次缴月费
	
	private static String defaultPriceTitle = "";//默认的priceTitle
	
	public MonthLyFeeTipItem(){

	}
	
	/**
	 * 月费提示Item
	 * @param memberType
	 * @param priceTitle
	 * @param tips
	 */
	public MonthLyFeeTipItem(int memberType,
			String priceTitle,
			String[] tips){
		this.memberType = RequestJniMonthlyFee.intToMemberType(memberType);
		this.priceTitle = priceTitle;
		this.tips = tips;
		if(this.memberType == MemberType.NO_FEED_FIRST_MONTHLY_MEMBER){
			//仅第一次月费用户提示使用价格
			this.priceDescribe = getPriceDescribe();
		}else{
			this.priceDescribe = normalPriceTitle;
		}
	}
	
	private String getPriceDescribe() {
//		String priceDescribe = firstPriceTitle.replace("price", priceTitle);
		String priceDescribe = String.format(firstPriceTitle, priceTitle);
		return priceDescribe;
	}
	
	/**
	 * @return 非第一次月费缴费priceTitle
	 */
	public String getNormalPriceTitle(){
		return normalPriceTitle;
	}
	
	/**
	 * @return 默认的PriceTitle
	 */
	public String getDefaultPriceTitle(MemberType type){
		if(type==MemberType.NO_FEED_FIRST_MONTHLY_MEMBER){//第一次月费默认
			defaultPriceTitle = String.format(firstPriceTitle, "9.99");
		}else {//非第一次默认
			defaultPriceTitle = normalPriceTitle;
		}
		return defaultPriceTitle;
	}
	
	/**
	 * @return 默认的tip
	 */
	public String[] getDefaultTips(){
		String[] tip = new String[]{"Access all Premium services","Unlock all profile photos","Plus 30 minutes free chat","Plus 10 free first letters"};
		return tip;
	}

	public MemberType memberType;
	public String priceTitle;
	public String[] tips;
	public String priceDescribe;
}
