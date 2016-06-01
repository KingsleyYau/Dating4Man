package com.qpidnetwork.request.item;

import java.io.Serializable;

import com.qpidnetwork.dating.QpidApplication;
import com.qpidnetwork.dating.R;
import com.qpidnetwork.request.RequestJniMonthlyFee;
import com.qpidnetwork.request.RequestJniMonthlyFee.MemberType;

public class MonthLyFeeTipItem implements Serializable{

	private static final long serialVersionUID = 7120978351159031158L;
	
	//private static final String firstPriceTitle = "Go Premium to Get All Benefits for only <font color=#006600>$%s</font> today!";//第一次缴月费
	//private static final String firstPriceTitle = "";//非第一次缴月费
	
	//private static final String normalPriceTitle = "<font color=#999999 size=2>Your premium membership is inactive.</font> <br/> <font color=#333333 size=3><b>Activate to get all benefits</b></font>";//非第一次缴月费
	
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
		this.priceDescribe = getDefaultPriceTitle(this.memberType);
		this.priceDescribe = String.format(this.priceDescribe, this.priceTitle);
		
	}

	
	/**
	 * @return 默认的PriceTitle
	 */
	/**
	 * @return 默认的PriceTitle
	 */
	public String getDefaultPriceTitle(MemberType type){
		if(type==MemberType.NO_FEED_FIRST_MONTHLY_MEMBER){//第一次月费默认
			defaultPriceTitle = QpidApplication.getContext().getString(R.string.first_monthly_fee_title);
		}else if(type==MemberType.NO_FEED_MONTHLY_MEMBER){//非第一次默认
			defaultPriceTitle = QpidApplication.getContext().getString(R.string.second_monthly_fee_title);
		}
		return defaultPriceTitle;
	}
	
	/**
	 * @return 默认的tip
	 */
	public String[] getDefaultTips(){
		String[] tip = QpidApplication.getContext().getResources().getStringArray(R.array.default_monthly_fee_package_item);
		return tip;
	}

	public MemberType memberType;
	public String priceTitle;
	public String[] tips;
	public String priceDescribe;
}
