package com.qpidnetwork.dating.bean;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * 分页处理
 * @author Hunter
 * @since 2015.5.16
 */
public class PageBean implements Serializable {
	
	private static final long serialVersionUID = 1L;
	private int dataCount;
	private int pageIndex; // 第一页的index为1;
	private int pageSize;

	public PageBean() {
		super();
	}

	public PageBean(int pageSize) {
		this.pageIndex = 0;
		this.pageSize = pageSize;
	}

	public PageBean(int pageIndex, int pageSize) {
		this.pageIndex = pageIndex;;
		this.pageSize = pageSize;
	}
	
	public PageBean(int dataCount, int pageIndex, int pageSize, List<Object> dataList) {
		super();
		this.dataCount = dataCount;
		this.pageIndex = pageIndex;
		this.pageSize = pageSize;
	}

	public PageBean(Map<?, ?> data) {
		super();
		this.dataCount = (Integer) data.get("dataCount");
		this.pageIndex = (Integer) data.get("pageIndex");
		this.pageSize = (Integer) data.get("pageSize");
	}

	public void updateNew(PageBean pb) {
		this.dataCount = pb.getDataCount();
		this.pageIndex = pb.getPageIndex();
	}

	/**
	 * 重置当前页
	 */
	public void resetPageIndex() {
		this.pageIndex = 0;
	}

	/**
	 * 获取下一页
	 * 
	 * @return
	 */
	public int getNextPageIndex() {
		return this.pageIndex + 1;
	}
	
	public int decreasePageIndex(){
		return this.pageIndex - 1;
	}

	/**
	 * 判断是否有下一页
	 * 
	 * @return
	 */
	public boolean hasNextPage() {
		return (pageIndex * pageSize < dataCount);
	}

	public int getDataCount() {
		return dataCount;
	}

	public void setDataCount(int dataCount) {
		this.dataCount = dataCount;
	}

	public int getPageIndex() {
		return pageIndex;
	}

	public void setPageIndex(int pageIndex) {
		this.pageIndex = pageIndex;
	}

	public int getPageSize() {
		return pageSize;
	}

	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
	}
	
	/**
	 * 获取当前请求页，下拉刷新时传0，load更多则传当前列表数目
	 * @param curCount
	 * @return
	 */
	public int getNextPager(int curCount){
		return (curCount/pageSize + 1);
	}
}
