package com.starnet.snview.realplay;

import android.util.Log;

import com.starnet.snview.component.liveview.LiveViewManager;
import com.starnet.snview.protocol.message.Constants;

public class PTZRequestSender {
	private static final String TAG = "PTZRequestSender";
	
	private LiveViewManager lvManager;
	
	public PTZRequestSender(LiveViewManager liveViewmanager) {
		if (liveViewmanager == null) {
			throw new IllegalArgumentException("LiveViewManager can not be null");
		}
		
		this.lvManager = liveViewmanager;
	}
	
	public void moveUp() {
		Log.i(TAG, "moveUp");
		lvManager.sendControlRequest(Constants.OWSP_ACTION_CODE.OWSP_ACTION_MD_UP);
	}
	
	public void moveDown() {
		Log.i(TAG, "moveDown");
		lvManager.sendControlRequest(Constants.OWSP_ACTION_CODE.OWSP_ACTION_MD_DOWN);
	}
	
	public void moveLeft() {
		Log.i(TAG, "moveLeft");
		lvManager.sendControlRequest(Constants.OWSP_ACTION_CODE.OWSP_ACTION_MD_LEFT);
	}
	
	public void moveRight() {
		Log.i(TAG, "moveRight");
		lvManager.sendControlRequest(Constants.OWSP_ACTION_CODE.OWSP_ACTION_MD_RIGHT);
	}
	
	public void stopMove() {
		Log.i(TAG, "stopMove");
		lvManager.sendControlRequest(Constants.OWSP_ACTION_CODE.OWSP_ACTION_MD_STOP);
	}
	
	public void focalLengthIncrease() {
		Log.i(TAG, "focalLengthIncrease");
		lvManager.sendControlRequest(Constants.OWSP_ACTION_CODE.OWSP_ACTION_FOCAL_LENGTH_INC);
	}
	
	public void focalLengthDecrease() {
		Log.i(TAG, "focalLengthDecrease");
		lvManager.sendControlRequest(Constants.OWSP_ACTION_CODE.OWSP_ACTION_FOCAL_LENGTH_DEC);
	}
	
	public void focusIncrease() {
		Log.i(TAG, "focusIncrease");
		lvManager.sendControlRequest(Constants.OWSP_ACTION_CODE.OWSP_ACTION_FOCUS_INC);
	}
	
	public void focusDecrease() {
		Log.i(TAG, "focusDecrease");
		lvManager.sendControlRequest(Constants.OWSP_ACTION_CODE.OWSP_ACTION_FOCUS_DEC);
	}
	
	public void apertureIncrease() {
		Log.i(TAG, "apertureIncrease");
		lvManager.sendControlRequest(Constants.OWSP_ACTION_CODE.OWSP_ACTION_APERTURE_INC);
	}
	
	public void apertureDecrease() {
		Log.i(TAG, "apertureDecrease");
		lvManager.sendControlRequest(Constants.OWSP_ACTION_CODE.OWSP_ACTION_APERTURE_INC);
	}

}
