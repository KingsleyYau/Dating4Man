package com.qpidnetwork.request.item;

import java.io.Serializable;

import com.qpidnetwork.request.RequestEnum.Children;
import com.qpidnetwork.request.RequestEnum.Education;
import com.qpidnetwork.request.RequestEnum.Marry;

public class LadyMatch implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -8450545611192612900L;
	public LadyMatch() {
		age1 = 0;
		age2 = 99;
		marry = Marry.Unknow;
		children = Children.Unknow;
		education = Education.Unknow;
	}

	/**
	 * 获取匹配女士回调
	 * @param age1			起始年龄
	 * @param age2			结束年龄
	 * @param marry			婚姻状况,参考枚举 <RequestEnum.Marry>
	 * @param children		子女状况,参考枚举 <RequestEnum.Children>
	 * @param education		教育程度,参考枚举 <RequestEnum.Education>
	 */
	public LadyMatch(
		 int age1,
		 int age2,
		 int marry,
		 int children,
		 int education
			) {
		this.age1 = age1;
		this.age2 = age2;
		
		if( marry < 0 || marry >= Marry.values().length ) {
			this.marry = Marry.values()[0];
		} else {
			this.marry = Marry.values()[marry];
		}
		
		if( children < 0 || children >= Children.values().length ) {
			this.children = Children.values()[0];  
		} else {
			this.children = Children.values()[children];  
		}

		if( education < 0 || education >= Education.values().length ) {
			this.education = Education.values()[0];  
		} else {
			this.education = Education.values()[education];  
		}

	}
	
	public int age1;
	public int age2;
	public Marry marry;
	public Children children;
	public Education education;
	
}
