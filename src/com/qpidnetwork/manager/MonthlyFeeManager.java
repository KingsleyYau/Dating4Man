package com.qpidnetwork.manager;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;

import com.qpidnetwork.request.OnGetMonthlyFeeTipsCallback;
import com.qpidnetwork.request.OnQueryMemberTypeCallback;
import com.qpidnetwork.request.RequestJniMonthlyFee;
import com.qpidnetwork.request.RequestJniMonthlyFee.MemberType;
import com.qpidnetwork.request.RequestOperator;
import com.qpidnetwork.request.item.MonthLyFeeTipItem;

/**
 * @author Yanni
 * 
 * @version 2016-5-9
 * 
 *          月费manager
 */
public class MonthlyFeeManager {
	/**
	 * 用户类型
	 */
	private MemberType mMemberType;
	/**
	 * 
	 */
	private MonthLyFeeTipItem[] mMonthlyFeeTipArray;
	
	/**
	 * MonthlyFeeManager 单例
	 */
	private static MonthlyFeeManager mMonthlyFeeManager;
	
	/**
	 * 会员状态改变监听回调
	 */
	private List<OnMemberMonthlyTypeUpdate> mCallbackList;

	public static MonthlyFeeManager newInstance(Context context) {
		if (mMonthlyFeeManager == null) {
			mMonthlyFeeManager = new MonthlyFeeManager(context);
		}
		return mMonthlyFeeManager;
	}

	public static MonthlyFeeManager getInstance() {
		return mMonthlyFeeManager;
	}

	public MonthlyFeeManager(Context context) {
		mMemberType = MemberType.NORMAL_MEMBER;
		mCallbackList = new ArrayList<OnMemberMonthlyTypeUpdate>();
	}

	private List<String> initData() {
		// TODO Auto-generated method stub
		List<String> list = new ArrayList<>();
		list.add("Plus 2 welcome credits");
		list.add("Access all Premium services");
		list.add("Unlock all profile photos");
		list.add("Plus 30 minutes free chat");
		return list;
	}
	
	/**
	 * 获取用户类型，更新本地数据
	 */
	public void QueryMemberType(){
		RequestOperator.getInstance().QueryMemberType(new OnQueryMemberTypeCallback() {
			
			@Override
			public void OnQueryMemberType(boolean isSuccess, String errno,
					String errmsg, int memberType) {
				if(isSuccess){
					MemberType type = RequestJniMonthlyFee.intToMemberType(memberType);
					onMemberTypeUpdate(type);
				}
			}
		});
	}
	
	/**
	 * 获取月费相关提示列表
	 */
	public void GetMonthlyFeeTips(){
		RequestOperator.getInstance().GetMonthlyFeeTips(new OnGetMonthlyFeeTipsCallback() {
			
			@Override
			public void OnGetMonthlyFeeTips(boolean isSuccess, String errno,
					String errmsg, MonthLyFeeTipItem[] tipList) {
				if(isSuccess && tipList != null){
					mMonthlyFeeTipArray = tipList;
				}
			}
		});
	}
	
	/**
	 * 获取当前用户类型
	 * @return
	 */
	public MemberType getMemberType(){
		return mMemberType;
	}
	
	/**
	 * 获取指定类型的提示列表
	 * @param type
	 * @return
	 */
	public MonthLyFeeTipItem getMonthLyFeeTipItem(MemberType type){
		MonthLyFeeTipItem item = null;
		if(mMonthlyFeeTipArray == null){
			//刷新提示列表
			GetMonthlyFeeTips();
		}else{
			for(MonthLyFeeTipItem tipItem : mMonthlyFeeTipArray){
				if(tipItem.memberType.ordinal() == type.ordinal()){
					item = tipItem;
					break;
				}
			}
		}
		if(item == null){
			//设置默认的Item
		}
		return item;
	}
	
	/**
	 * 添加用户类型改变刷新监听
	 */
	public void AddMemberTypeListener(OnMemberMonthlyTypeUpdate listener){
		synchronized (mCallbackList) {
			mCallbackList.add(listener);
		}
	}
	
	/**
	 * 移除用户类型状态改变修改
	 */
	public void RemoveMemberTypeListener(OnMemberMonthlyTypeUpdate listener){
		synchronized (mCallbackList){
			mCallbackList.remove(listener);
		}
	}
	
	/**
	 * 会员状态更新界面
	 * @param memberType
	 */
	public void onMemberTypeUpdate(MemberType memberType){
		mMemberType = memberType;
		synchronized (mCallbackList) {
			for(OnMemberMonthlyTypeUpdate callback : mCallbackList){
				callback.onMemberTypeUpdate(memberType);
			}
		}
	}
	
	public interface OnMemberMonthlyTypeUpdate{
		public void onMemberTypeUpdate(MemberType memberType);
	}

}
