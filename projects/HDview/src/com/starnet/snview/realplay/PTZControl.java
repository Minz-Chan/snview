package com.starnet.snview.realplay;

import com.starnet.snview.component.liveview.LiveViewManager;
import com.starnet.snview.protocol.message.Constants;

public class PTZControl {
	private LiveViewManager lvManager;
	
	public PTZControl(LiveViewManager liveViewmanager) {
		if (liveViewmanager == null) {
			throw new IllegalArgumentException("LiveViewManager can not be null");
		}
		
		this.lvManager = liveViewmanager;
	}
	
	public void moveUp() {
		lvManager.sendControlRequest(Constants.OWSP_ACTION_CODE.OWSP_ACTION_MD_UP);
	}
	
	public void moveDown() {
		lvManager.sendControlRequest(Constants.OWSP_ACTION_CODE.OWSP_ACTION_MD_DOWN);
	}
	
	public void moveLeft() {
		lvManager.sendControlRequest(Constants.OWSP_ACTION_CODE.OWSP_ACTION_MD_LEFT);
	}
	
	public void moveRight() {
		lvManager.sendControlRequest(Constants.OWSP_ACTION_CODE.OWSP_ACTION_MD_RIGHT);
	}
	
	public void stopMove() {
		lvManager.sendControlRequest(Constants.OWSP_ACTION_CODE.OWSP_ACTION_MD_STOP);
	}
	
	public void focalLengthIncrease() {
		lvManager.sendControlRequest(Constants.OWSP_ACTION_CODE.OWSP_ACTION_FOCAL_LENGTH_INC);
	}
	
	public void focalLengthDecrease() {
		lvManager.sendControlRequest(Constants.OWSP_ACTION_CODE.OWSP_ACTION_FOCAL_LENGTH_DEC);
	}
	
	public void focusIncrease() {
		lvManager.sendControlRequest(Constants.OWSP_ACTION_CODE.OWSP_ACTION_FOCUS_INC);
	}
	
	public void focusDecrease() {
		lvManager.sendControlRequest(Constants.OWSP_ACTION_CODE.OWSP_ACTION_FOCUS_DEC);
	}
	
	public void apertureIncrease() {
		lvManager.sendControlRequest(Constants.OWSP_ACTION_CODE.OWSP_ACTION_APERTURE_INC);
	}
	
	public void apertureDecrease() {
		lvManager.sendControlRequest(Constants.OWSP_ACTION_CODE.OWSP_ACTION_APERTURE_INC);
	}

}
