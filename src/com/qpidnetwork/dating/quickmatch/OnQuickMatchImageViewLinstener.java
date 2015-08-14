package com.qpidnetwork.dating.quickmatch;

public interface OnQuickMatchImageViewLinstener {
	public void OnReleaseLeft();
	public void OnReleaseRight();
	public void OnMoveLeft();
	public void OnMoveRight();
	public void OnMoveCenter();
	public void OnDrag();
	public void OnRestore();
	public void OnMoveOut();
}
