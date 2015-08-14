package com.qpidnetwork.request.item;

import java.io.Serializable;

public class LadySignItem implements Serializable{
	
	private static final long serialVersionUID = -3726842120907074291L;
	
	public LadySignItem() {
		
	}

	/**
	 * 女士标签列表item（ver3.0起）
	 * @param signId	标签 ID
	 * @param name		标签名称
	 * @param color		标签颜色
	 * @param isSigned	是否已标记（选中）
	 */
	public LadySignItem(
			String signId,
			String name,
			String color,
			boolean isSigned
			) 
	{
		this.signId = signId;
		this.name = name;
		this.color = color;
		this.isSigned = isSigned;
	}
	
	public LadySignItem clone() {
		return new LadySignItem(signId, name, color, isSigned);
	}
	
	public String signId;
	public String name;
	public String color;
	public boolean isSigned;
}
