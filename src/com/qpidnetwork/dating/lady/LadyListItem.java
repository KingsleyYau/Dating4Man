package com.qpidnetwork.dating.lady;

import com.qpidnetwork.request.item.Lady;

public class LadyListItem {
	
	public LadyListItem(
			float radio,
			Lady lady,
			int backgroundColorType) 
	{
		this.radio = radio;
		this.lady = lady;
		this.backgroundColorType = backgroundColorType;
	}
	public float radio;
	public Lady lady = null;
	public int backgroundColorType;
}
