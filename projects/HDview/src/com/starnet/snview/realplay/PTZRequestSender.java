package com.starnet.snview.realplay;

import android.util.Log;

import com.starnet.snview.component.liveview.LiveViewGroup;
import com.starnet.snview.protocol.message.Constants;

public class PTZRequestSender {
	private static final String TAG = "PTZRequestSender";
	
	private LiveViewGroup mLiveViewGroup;
	
	public PTZRequestSender() {}
	
	public PTZRequestSender(LiveViewGroup liveViewGroup) {
		if (liveViewGroup == null) {
			throw new IllegalArgumentException("LiveViewGroup can not be null");
		}
		
		this.mLiveViewGroup = liveViewGroup;
	}
	
	public void setLiveViewGroup(LiveViewGroup liveViewGroup) {
		this.mLiveViewGroup = liveViewGroup;
	}
	
	public void autoScan() {
		Log.i(TAG, "auto scan");
		mLiveViewGroup.getSelectedLiveview().sendControlRequest(Constants.OWSP_ACTION_CODE.OWSP_ACTION_AUTO_CRUISE);
	}
	
	public void moveUp() {
		Log.i(TAG, "moveUp");
		mLiveViewGroup.getSelectedLiveview().sendControlRequest(Constants.OWSP_ACTION_CODE.OWSP_ACTION_MD_UP);
	}
	
	public void moveDown() {
		Log.i(TAG, "moveDown");
		mLiveViewGroup.getSelectedLiveview().sendControlRequest(Constants.OWSP_ACTION_CODE.OWSP_ACTION_MD_DOWN);
	}
	
	public void moveLeft() {
		Log.i(TAG, "moveLeft");
		mLiveViewGroup.getSelectedLiveview().sendControlRequest(Constants.OWSP_ACTION_CODE.OWSP_ACTION_MD_LEFT);
	}
	
	public void moveRight() {
		Log.i(TAG, "moveRight");
		mLiveViewGroup.getSelectedLiveview().sendControlRequest(Constants.OWSP_ACTION_CODE.OWSP_ACTION_MD_RIGHT);
	}
	
	public void moveLeftUp() {
		Log.i(TAG, "moveLeftUp");
		mLiveViewGroup.getSelectedLiveview().sendControlRequest(Constants.OWSP_ACTION_CODE.OWSP_ACTION_MV_LEFTUP);
	}
	
	public void moveLeftDown() {
		Log.i(TAG, "moveLeftDown");
		mLiveViewGroup.getSelectedLiveview().sendControlRequest(Constants.OWSP_ACTION_CODE.OWSP_ACTION_MV_LEFTDOWN);
	}
	
	public void moveRightUp() {
		Log.i(TAG, "moveRightUp");
		mLiveViewGroup.getSelectedLiveview().sendControlRequest(Constants.OWSP_ACTION_CODE.OWSP_ACTION_MV_RIGHTUP);
	}
	
	public void moveRightDown() {
		Log.i(TAG, "moveRightDown");
		mLiveViewGroup.getSelectedLiveview().sendControlRequest(Constants.OWSP_ACTION_CODE.OWSP_ACTION_MV_RIGHTDOWN);
	}
	
	public void stopMove() {
		Log.i(TAG, "stopMove");
		mLiveViewGroup.getSelectedLiveview().sendControlRequest(Constants.OWSP_ACTION_CODE.OWSP_ACTION_MD_STOP);
	}
	
	public void focalLengthIncrease() {
		Log.i(TAG, "focalLengthIncrease");
		mLiveViewGroup.getSelectedLiveview().sendControlRequest(Constants.OWSP_ACTION_CODE.OWSP_ACTION_FOCAL_LENGTH_INC);
	}
	
	public void focalLengthDecrease() {
		Log.i(TAG, "focalLengthDecrease");
		mLiveViewGroup.getSelectedLiveview().sendControlRequest(Constants.OWSP_ACTION_CODE.OWSP_ACTION_FOCAL_LENGTH_DEC);
	}
	
	public void focusIncrease() {
		Log.i(TAG, "focusIncrease");
		mLiveViewGroup.getSelectedLiveview().sendControlRequest(Constants.OWSP_ACTION_CODE.OWSP_ACTION_FOCUS_INC);
	}
	
	public void focusDecrease() {
		Log.i(TAG, "focusDecrease");
		mLiveViewGroup.getSelectedLiveview().sendControlRequest(Constants.OWSP_ACTION_CODE.OWSP_ACTION_FOCUS_DEC);
	}
	
	public void apertureIncrease() {
		Log.i(TAG, "apertureIncrease");
		mLiveViewGroup.getSelectedLiveview().sendControlRequest(Constants.OWSP_ACTION_CODE.OWSP_ACTION_APERTURE_INC);
	}
	
	public void apertureDecrease() {
		Log.i(TAG, "apertureDecrease");
		mLiveViewGroup.getSelectedLiveview().sendControlRequest(Constants.OWSP_ACTION_CODE.OWSP_ACTION_APERTURE_INC);
	}
	
	public void gotoPresetPoint(int num) {
		Log.i(TAG, "gotoPresetPoint, num:" + num);
		mLiveViewGroup.getSelectedLiveview().sendControlRequest(Constants.OWSP_ACTION_CODE.OWSP_ACTION_GOTO_PRESET_POSITION, new int[] { num });
	}
	
	public void setPresetPoint(int num) {
		Log.i(TAG, "setPresetPoint, num:" + num);
		mLiveViewGroup.getSelectedLiveview().sendControlRequest(Constants.OWSP_ACTION_CODE.OWSP_ACTION_SET_PRESET_POSITION, new int[] { num });
	}
	
	public void clearPresetPoint(int num) {
		Log.i(TAG, "clearPresetPoint, num:" + num);
		mLiveViewGroup.getSelectedLiveview().sendControlRequest(Constants.OWSP_ACTION_CODE.OWSP_ACTION_CLEAR_PRESET_POSITION, new int[] { num });
	}

}
