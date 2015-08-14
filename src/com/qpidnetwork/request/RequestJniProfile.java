package com.qpidnetwork.request;

import com.qpidnetwork.request.RequestEnum.*;

/**
 * 2.个人信息
 * @author Max.Chiu
 *
 */
public class RequestJniProfile {
	/**
	 * 2.1.查询个人信息
	 * @return				请求唯一标识
	 */
	static public native long GetMyProfile(OnGetMyProfileCallback callback);
	
	/**
	 * 2.2.修改个人信息
	 * @param weight		体重
	 * @param height		身高
	 * @param language		语言
	 * @param ethnicity		人种
	 * @param religion		宗教
	 * @param education		教育程度
	 * @param profession	职业
	 * @param income		收入情况
	 * @param children		子女状况
	 * @param smoke			吸烟情况
	 * @param drink			喝酒情况
	 * @param resume		个人简介
	 * @param interest		兴趣爱好
	 * @return				请求唯一标识
	 */
	static public long UpdateProfile(
			Weight weight, 
			Height height, 
			Language language, 
			Ethnicity ethnicity, 
			Religion religion, 
			Education education, 
			Profession profession, 
			Income income, 
			Children children, 
			Smoke smoke, 
			Drink drink, 
			String resume,
			String[] interest,
			OnUpdateMyProfileCallback callback
			) {
		return UpdateProfile(
				weight.ordinal(), 
				height.ordinal(), 
				language.ordinal(), 
				ethnicity.ordinal(), 
				religion.ordinal(), 
				education.ordinal(), 
				profession.ordinal(), 
				income.ordinal(), 
				children.ordinal(), 
				smoke.ordinal(), 
				drink.ordinal(), 
				resume, 
				interest, 
				callback
				);
	}
	static protected native long UpdateProfile(
			int weight, 
			int height, 
			int language, 
			int ethnicity, 
			int religion, 
			int education, 
			int profession, 
			int income, 
			int children, 
			int smoke, 
			int drink, 
			String resume, 
			String[] interest,
			OnUpdateMyProfileCallback callback
			);
	
	/**
	 * 2.3.开始编辑简介触发计时
	 * @return				请求唯一标识
	 */
	static public native long StartEditResume(OnRequestCallback callback);
	
	/**
	 * 2.4.保存联系电话
	 * @param telephone			电话号码
	 * @param telephone_cc		国家区号,参考枚举 <RequestEnum.Country>
	 * @param landline			固定电话号码
	 * @param landline_cc		固定电话号码国家区号,参考枚举 <RequestEnum.Country>
	 * @param landline_ac		固话区号
	 * @return					请求唯一标识
	 */
	static public long SaveContact(
			String telephone, 
			Country telephone_cc, 
			String landline, 
			Country landline_cc, 
			String landline_ac, 
			OnRequestCallback callback
			) {
		return SaveContact(telephone, telephone_cc.ordinal(), landline, landline_cc.ordinal(), 
				landline_ac, callback);
	}
	static protected native long SaveContact(String telephone, int telephone_cc, String landline, 
			int landline_cc, String landline_ac, OnRequestCallback callback);
	
	/**
	 * 2.5.上传头像
	 * @param fileName			文件名
	 * @return					请求唯一标识
	 */
	static public native long UploadHeaderPhoto(String fileName, OnRequestCallback callback);
}
