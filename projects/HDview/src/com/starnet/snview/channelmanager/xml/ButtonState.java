package com.starnet.snview.channelmanager.xml;

/**
 * 该显示按钮有三个状态，"empty","half","all"，当为"empty"时，该按钮加载空的显示图片；当为"half"
 * 时，该按钮加载半满的显示图片；当为"all"时，该按钮加载全满的显示图片；
 */
public class ButtonState {

	String state = "empty";

	private int groupPos;// 按钮所在的父位置
	private int childPos;// 按钮所在的子位置

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public int getGroupPos() {
		return groupPos;
	}

	public void setGroupPos(int groupPos) {
		this.groupPos = groupPos;
	}

	public int getChildPos() {
		return childPos;
	}

	public void setChildPos(int childPos) {
		this.childPos = childPos;
	}

}