package com.video.hdview.channelmanager;

import java.io.Serializable;

public class Channel implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1216065285217692064L;
	private String channelName;
	private int channelNo;
	private boolean isSelected;
	
	
	
	public String getChannelName() {
		return channelName;
	}
	public void setChannelName(String channelName) {
		this.channelName = channelName;
	}
	public boolean isSelected() {
		return isSelected;
	}
	public void setSelected(boolean isSelected) {
		this.isSelected = isSelected;
	}
	public int getChannelNo() {
		return channelNo;
	}
	public void setChannelNo(int channelNo) {
		this.channelNo = channelNo;
	}
	
	
}
