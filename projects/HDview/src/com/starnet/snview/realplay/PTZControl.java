package com.starnet.snview.realplay;

import com.starnet.snview.R;
import com.starnet.snview.component.LandscapeToolbar;
import com.starnet.snview.component.liveview.LiveViewManager;
import com.starnet.snview.global.GlobalApplication;
import com.starnet.snview.realplay.RealplayActivity.TOOLBAR_EXTEND_MENU;

import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;

public class PTZControl {
	private static final String TAG = "PTZControl";
	
	private RealplayActivity mLiveActivity;
	private LiveViewManager mLiveviewManager;
	
	private boolean mIsPTZModeOn = false;
	private boolean mIsPTZInMoving = false;
	private boolean mIsFlingAction = false;
	private boolean mIsEnterPTZInSingleMode = false;  // 在开启PTZ功能时是否处于单通道模式
	
	private LinearLayout mPTZControlbarMenu;
	private LinearLayout mPTZPopFrame;
	
	private ImageButton mPTZMenuScan;
	private ImageButton mPTZMenuFocalLength;
	private ImageButton mPTZMenuFocus;
	private ImageButton mPTZMenuAperture;
	private ImageButton mPTZMenuPreset;
	
	private ImageButton mPTZPopFocalLengthIncrease;  
	private ImageButton mPTZPopFocalLengthDecrease;
	private ImageButton mPTZPopFocusIncrease;  
	private ImageButton mPTZPopFocusDecrease;
	private ImageButton mPTZPopApertureIncrease;  
	private ImageButton mPTZPopApertureDecrease;
	
	private LandscapeToolbar mLandscapeToolbar;
	
	private PTZRequestSender mPtzReqSender;
	
	public PTZControl(RealplayActivity activity) {
		this.mLiveActivity = activity;
		this.mLiveviewManager = activity.getLiveviewManager();
		
		init();
	}
	
	private void init() {
		mPTZControlbarMenu = (LinearLayout) findViewById(R.id.ptz_controlbar_menu);
		mPTZMenuScan = (ImageButton) findViewById(R.id.ptz_controlbar_menu_scan);
		mPTZMenuFocalLength = (ImageButton) findViewById(R.id.ptz_controlbar_menu_focal_length);
		mPTZMenuFocus = (ImageButton) findViewById(R.id.ptz_controlbar_menu_focus);
		mPTZMenuAperture = (ImageButton) findViewById(R.id.ptz_controlbar_menu_aperture);
		mPTZMenuPreset = (ImageButton) findViewById(R.id.ptz_controlbar_menu_preset);

		mPTZMenuScan.setOnClickListener(mOnPTZMenuClickListener);
		mPTZMenuFocalLength.setOnClickListener(mOnPTZMenuClickListener);
		mPTZMenuFocus.setOnClickListener(mOnPTZMenuClickListener);
		mPTZMenuAperture.setOnClickListener(mOnPTZMenuClickListener);
		mPTZMenuPreset.setOnClickListener(mOnPTZMenuClickListener);
		
		
		mPTZPopFocalLengthIncrease = (ImageButton) findViewById(R.id.ptz_pop_focal_length_increase);  
		mPTZPopFocalLengthDecrease = (ImageButton) findViewById(R.id.ptz_pop_focal_length_decrease);
		mPTZPopFocusIncrease = (ImageButton) findViewById(R.id.ptz_pop_focus_increase);  
		mPTZPopFocusDecrease = (ImageButton) findViewById(R.id.ptz_pop_focus_decrease);
		mPTZPopApertureIncrease = (ImageButton) findViewById(R.id.ptz_pop_aperture_increase);  
		mPTZPopApertureDecrease = (ImageButton) findViewById(R.id.ptz_pop_aperture_decrease);
		
		//mPTZPopFocalLengthIncrease.setOnClickListener(mOnPTZPopClickListener);  
		//mPTZPopFocalLengthDecrease.setOnClickListener(mOnPTZPopClickListener);mOnPTZFocalLengthListener
		mPTZPopFocalLengthIncrease.setOnTouchListener(mOnPTZFocalLengthListener);
		mPTZPopFocalLengthDecrease.setOnTouchListener(mOnPTZFocalLengthListener);
		mPTZPopFocusIncrease.setOnClickListener(mOnPTZPopClickListener);  
		mPTZPopFocusDecrease.setOnClickListener(mOnPTZPopClickListener);
		mPTZPopApertureIncrease.setOnClickListener(mOnPTZPopClickListener);  
		mPTZPopApertureDecrease.setOnClickListener(mOnPTZPopClickListener);
		
		mPTZPopFrame = (LinearLayout) findViewById(R.id.ptz_pop_frame);
		
		mLandscapeToolbar = (LandscapeToolbar) findViewById(R.id.landscape_liveview_control_frame);
		
		mPtzReqSender = new PTZRequestSender(mLiveviewManager);
		
		showPTZBar(false);
	}
	
	private View findViewById(int resid) {
		return mLiveActivity.findViewById(resid);
	}
	
	private enum PTZ_POP_FRAME {
		SCAN, FOCAL_LENGTH, FOCUS, APERTURE, PRESET
	};
	
	private OnClickListener mOnPTZMenuClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {

			switch (v.getId()) {
			case R.id.ptz_controlbar_menu_scan:
				Log.i(TAG, "ptz_controlbar_menu_scan");
				ptzAuto();
				break;
			case R.id.ptz_controlbar_menu_focal_length:
				Log.i(TAG, "ptz_controlbar_menu_focal_length");
				if (!mPTZMenuFocalLength.isSelected()) {
					// mToolbarSubMenu.setVisibility(View.VISIBLE);
					// mToolbarSubMenuText.setText(getString(R.string.toolbar_sub_menu_focal_length));
					ptzFocalLength();
					//showPTZFrame(PTZ_POP_FRAME.FOCAL_LENGTH, true);
					mPTZMenuScan.setSelected(false);
					mPTZMenuFocalLength.setSelected(true);
					mPTZMenuFocus.setSelected(false);
					mPTZMenuAperture.setSelected(false);
					mPTZMenuPreset.setSelected(false);
				} else {
					// mToolbarSubMenu.setVisibility(View.GONE);
					showPTZFrame(PTZ_POP_FRAME.FOCAL_LENGTH, false);
					mPTZMenuFocalLength.setSelected(false);
				}
				break;
			case R.id.ptz_controlbar_menu_focus:
				Log.i(TAG, "ptz_controlbar_menu_focus");
				if (!mPTZMenuFocus.isSelected()) {
					// mToolbarSubMenu.setVisibility(View.VISIBLE);
					// mToolbarSubMenuText.setText(getString(R.string.toolbar_sub_menu_focus));
					ptzFocus();
					//showPTZFrame(PTZ_POP_FRAME.FOCUS, true);
					mPTZMenuScan.setSelected(false);
					mPTZMenuFocalLength.setSelected(false);
					mPTZMenuFocus.setSelected(true);
					mPTZMenuAperture.setSelected(false);
					mPTZMenuPreset.setSelected(false);
				} else {
					// mToolbarSubMenu.setVisibility(View.GONE);
					showPTZFrame(PTZ_POP_FRAME.FOCUS, false);
					mPTZMenuFocus.setSelected(false);
				}
				break;
			case R.id.ptz_controlbar_menu_aperture:
				Log.i(TAG, "ptz_controlbar_menu_aperture");
				if (!mPTZMenuAperture.isSelected()) {
					// mToolbarSubMenu.setVisibility(View.VISIBLE);
					// mToolbarSubMenuText.setText(getString(R.string.toolbar_sub_menu_aperture));
					ptzAperture();
					//showPTZFrame(PTZ_POP_FRAME.APERTURE, true);
					mPTZMenuScan.setSelected(false);
					mPTZMenuFocalLength.setSelected(false);
					mPTZMenuFocus.setSelected(false);
					mPTZMenuAperture.setSelected(true);
					mPTZMenuPreset.setSelected(false);
				} else {
					// mToolbarSubMenu.setVisibility(View.GONE);
					showPTZFrame(PTZ_POP_FRAME.APERTURE, false);
					mPTZMenuAperture.setSelected(false);
				}
				break;
			case R.id.ptz_controlbar_menu_preset:
				Log.i(TAG, "ptz_controlbar_menu_preset");
				ptzPresetPoint();
				break;
			}

		}
	};
	
	private void showPTZFrame(PTZ_POP_FRAME ppf, boolean isShow) {
		if (isShow) {
			switch (ppf) {
			case SCAN:
				((LinearLayout) findViewById(R.id.ptz_pop_focal_length_frame))
						.setVisibility(View.GONE);
				((LinearLayout) findViewById(R.id.ptz_pop_focus_frame))
						.setVisibility(View.GONE);
				((LinearLayout) findViewById(R.id.ptz_pop_aperture_frame))
						.setVisibility(View.GONE);
				break;
			case FOCAL_LENGTH:
				((LinearLayout) findViewById(R.id.ptz_pop_focal_length_frame))
						.setVisibility(View.VISIBLE);
				((LinearLayout) findViewById(R.id.ptz_pop_focus_frame))
						.setVisibility(View.GONE);
				((LinearLayout) findViewById(R.id.ptz_pop_aperture_frame))
						.setVisibility(View.GONE);
				break;
			case FOCUS:
				((LinearLayout) findViewById(R.id.ptz_pop_focal_length_frame))
						.setVisibility(View.GONE);
				((LinearLayout) findViewById(R.id.ptz_pop_focus_frame))
						.setVisibility(View.VISIBLE);
				((LinearLayout) findViewById(R.id.ptz_pop_aperture_frame))
						.setVisibility(View.GONE);
				break;
			case APERTURE:
				((LinearLayout) findViewById(R.id.ptz_pop_focal_length_frame))
						.setVisibility(View.GONE);
				((LinearLayout) findViewById(R.id.ptz_pop_focus_frame))
						.setVisibility(View.GONE);
				((LinearLayout) findViewById(R.id.ptz_pop_aperture_frame))
						.setVisibility(View.VISIBLE);
				break;
			case PRESET:
				((LinearLayout) findViewById(R.id.ptz_pop_focal_length_frame))
						.setVisibility(View.GONE);
				((LinearLayout) findViewById(R.id.ptz_pop_focus_frame))
						.setVisibility(View.GONE);
				((LinearLayout) findViewById(R.id.ptz_pop_aperture_frame))
						.setVisibility(View.GONE);
				break;
			}
		} else {
			((LinearLayout) findViewById(R.id.ptz_pop_focal_length_frame))
					.setVisibility(View.GONE);
			((LinearLayout) findViewById(R.id.ptz_pop_focus_frame))
					.setVisibility(View.GONE);
			((LinearLayout) findViewById(R.id.ptz_pop_aperture_frame))
					.setVisibility(View.GONE);
		}
	}
	
	private OnTouchListener mOnPTZFocalLengthListener = new OnTouchListener() {
		
		@Override
		public boolean onTouch(View v, MotionEvent event) {
			int action = event.getActionMasked();
			
			switch (v.getId()) {
			case R.id.ptz_pop_focal_length_increase:
				if (action == MotionEvent.ACTION_DOWN) {
					mPtzReqSender.focalLengthIncrease();
				} else if (action == MotionEvent.ACTION_UP) {
					mPtzReqSender.stopMove();
				}
				break;
			case R.id.ptz_pop_focal_length_decrease:
				if (action == MotionEvent.ACTION_DOWN) {
					mPtzReqSender.focalLengthDecrease();
				} else if (action == MotionEvent.ACTION_UP) {
					mPtzReqSender.stopMove();
				}
				break;
			}
			
			
			
			return false;
		}
	};
	
	private OnClickListener mOnPTZPopClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			
			switch (v.getId()) {
			case R.id.ptz_pop_focal_length_increase:
				//Log.i(TAG, "ptz_pop_focal_length_increase");
				//mPtzControl.focalLengthIncrease();
				break;
			case R.id.ptz_pop_focal_length_decrease:
				//Log.i(TAG, "ptz_pop_focal_length_decrease");
				//mPtzControl.focalLengthDecrease();
				break;
			case R.id.ptz_pop_focus_increase:
				Log.i(TAG, "ptz_pop_focus_increase");
				mPtzReqSender.focusIncrease();
				break;
			case R.id.ptz_pop_focus_decrease:
				Log.i(TAG, "ptz_pop_focus_decrease");
				mPtzReqSender.focusDecrease();
				break;
			case R.id.ptz_pop_aperture_increase:
				Log.i(TAG, "ptz_pop_aperture_increase");
				mPtzReqSender.apertureIncrease();
				break;
			case R.id.ptz_pop_aperture_decrease:
				Log.i(TAG, "ptz_pop_aperture_decrease");
				mPtzReqSender.apertureDecrease();
				break;
			}
			
		}
		
	};
	
	
	public void ptzButtonAction() {
		Log.i(TAG, "ptzButtonAction");
		
		if (mLiveActivity.checkIfPreviewDeviceListEmpty()) {
			return;
		}
		
		if (!checkIsPTZDeviceConnected()) {
			if (mIsPTZModeOn) {
				closePTZ();
			} 
			
			return;
		}

		if (!mIsPTZModeOn) {
			openPTZ();
		} else {
			closePTZ();
		}
	}
	
	/**
	 * 横/竖屏状态下显示、隐藏PTZ控制条
	 * @param isShow true, 显示; false, 隐藏
	 */
	public void showPTZBar(boolean isShow) {
		if (!GlobalApplication.getInstance().isIsFullMode()) { // 竖屏状态
			if (isShow) {
				mPTZControlbarMenu.setVisibility(View.VISIBLE);
				mPTZPopFrame.setVisibility(View.VISIBLE);
				showPTZFrame(PTZ_POP_FRAME.SCAN, false);
			} else {
				mPTZControlbarMenu.setVisibility(View.GONE);
				mPTZPopFrame.setVisibility(View.GONE);
				showPTZFrame(PTZ_POP_FRAME.SCAN, false);
				mPTZMenuScan.setSelected(false);
				mPTZMenuFocalLength.setSelected(false);
				mPTZMenuFocus.setSelected(false);
				mPTZMenuAperture.setSelected(false);
				mPTZMenuPreset.setSelected(false);
			}
		} else {
			if (isShow) {
				mLandscapeToolbar.showPTZControlbar();
			} else {
				mLandscapeToolbar.hidePTZbar();
			}
		}
		
		mIsPTZModeOn = isShow;
	}
	
	/**
	 * 横竖屏切换时，PTZ控制栏状态同步
	 */
	public void syncPTZStatus() {
		if (mIsPTZModeOn) {
			if (GlobalApplication.getInstance().isIsFullMode()) { // 当前为横屏状态
				mPTZControlbarMenu.setVisibility(View.GONE);
				mPTZPopFrame.setVisibility(View.GONE);
				mLandscapeToolbar.setVisibility(View.VISIBLE);
				mLandscapeToolbar.showPTZControlbar();
			} else { // 当前为竖屏状态
				mLandscapeToolbar.setVisibility(View.GONE);
				mPTZControlbarMenu.setVisibility(View.VISIBLE);
				mPTZPopFrame.setVisibility(View.VISIBLE);
				showToolbarExtendMenu(TOOLBAR_EXTEND_MENU.MENU_PTZ);
			}
		} else {
			if (GlobalApplication.getInstance().isIsFullMode()) { 
				mPTZControlbarMenu.setVisibility(View.GONE);
				mPTZPopFrame.setVisibility(View.GONE);
				mLandscapeToolbar.setVisibility(View.VISIBLE);
				mLandscapeToolbar.showControlbar();
			} else {
				mLandscapeToolbar.setVisibility(View.GONE);
				mPTZControlbarMenu.setVisibility(View.VISIBLE);
				mPTZPopFrame.setVisibility(View.VISIBLE);
				showToolbarExtendMenu(TOOLBAR_EXTEND_MENU.PAGER);
			}
		}
	}
	
	public void openPTZ() {
		mIsPTZModeOn = true;
		
		mLiveActivity.getVideoPager().setPTZMode(true);
		
		if (!GlobalApplication.getInstance().isIsFullMode()) {
			showToolbarExtendMenu(TOOLBAR_EXTEND_MENU.MENU_PTZ);
		} else {
			mLandscapeToolbar.showPTZControlbar();
		}
		
		
		if (mIsEnterPTZInSingleMode) {
			return;  // 若开启前PTZ模式时已是单通道模式，则仅显示PTZ菜单
		}	
		
		int index = mLiveviewManager.getSelectedLiveViewIndex();
		
		if (index > mLiveviewManager.getLiveViewTotalCount()) {  
			return;  // 非有效通道，不作处理 
		}
		
		if (mLiveviewManager.isMultiMode()) { // 切换到单通道模式
			mLiveviewManager.setCurrenSelectedLiveViewtIndex(index);  // 变更当前选择索引
			
			int pos = ((index % 4) == 0) ? 4 : (index % 4);
			
			//mLiveviewManager.closeAllConnection(false);  // 关闭正在预览的设备
			mLiveviewManager.prestoreConnectionByPosition(pos);
			
			mLiveviewManager.setMultiMode(false);							
			//mLiveviewManager.preview(index);
			mLiveviewManager.transferVideoWithoutDisconnect(pos);
		}
		
		mLiveviewManager.selectLiveView(index);
	}

	
	public void closePTZ() {
		mIsPTZModeOn = false;
		
		mLiveActivity.getVideoPager().setPTZMode(false);
		
		if (!GlobalApplication.getInstance().isIsFullMode()) {
			showPTZBar(false);

			showToolbarExtendMenu(TOOLBAR_EXTEND_MENU.PAGER);
		} else {
			mPTZPopFrame.setVisibility(View.GONE);
			mLandscapeToolbar.hidePTZbar();
		}
		
		if (mIsEnterPTZInSingleMode) {
			return;  // 若进入前PTZ模式时已是单通道模式，则仅隐藏PTZ菜单
		}		
		
		int index = mLiveviewManager.getSelectedLiveViewIndex();
		
		if (index > mLiveviewManager.getLiveViewTotalCount()) {  
			return;  // 非有效通道，不作处理 
		}
		
		//mLiveviewManager.closeAllConnection(false);
		mLiveviewManager.prestoreConnectionByPosition(mLiveviewManager.getPositionOfIndex(index));
		
		mLiveviewManager.setMultiMode(true);
		
		int currPageStart = (mLiveviewManager.getCurrentPageNumber() - 1) * 4 + 1;
		int currPageEnd = (mLiveviewManager.getCurrentPageNumber() - 1) * 4 + mLiveviewManager.getCurrentPageCount();
		
		mLiveviewManager.preview(currPageStart, currPageEnd - currPageStart + 1, index);
		mLiveviewManager.selectLiveView(index);
	}
	
	private void initPTZPopFramePos() {
		GlobalApplication g = GlobalApplication.getInstance();
		RelativeLayout.LayoutParams lp = new LayoutParams(
				g.getPTZPopFrameWidth(), g.getPTZPopFrameHeight());
		
		lp.addRule(RelativeLayout.CENTER_HORIZONTAL);
		
		if (g.isIsFullMode()) {
			lp.topMargin = g.getVideoRegionHeight()
					- g.getPTZPopFrameHeight()
					- (int) mLiveActivity.getResources().getDimension(
							R.dimen.landscape_pop_frame_bottom_margin);
		} else {
			lp.topMargin = ((RelativeLayout) mPTZPopFrame.getParent())
					.getHeight() - g.getPTZPopFrameHeight()
					- (int) mLiveActivity.getResources().getDimension(
							R.dimen.portrait_pop_frame_bottom_margin);
		}
		
		Log.i(TAG, "lp.topMargin:" + lp.topMargin + ", lp.bottomMargin:" + lp.bottomMargin);
		
		mPTZPopFrame.setLayoutParams(lp);
	}
	
	public void ptzAuto() {
		mPTZPopFrame.setVisibility(View.GONE);
		showPTZFrame(PTZ_POP_FRAME.SCAN, true);
		
		ImageButton ptzAuto;
		boolean isSelected;
		if (GlobalApplication.getInstance().isIsFullMode()) {
			ptzAuto = (ImageButton) findViewById(R.id.landscape_liveview_ptz_auto);
		} else {
			ptzAuto = (ImageButton) findViewById(R.id.ptz_controlbar_menu_scan);
		}		
		isSelected = ptzAuto.isSelected();
		if (isSelected) {
			ptzAuto.setSelected(false);
			getPtzReqSender().stopMove();
		} else {
			ptzAuto.setSelected(true);
			getPtzReqSender().autoScan();
		}
	}
	
	public void ptzFocalLength() {
		initPTZPopFramePos();
		
		mPTZPopFrame.setVisibility(View.VISIBLE);
		showPTZFrame(PTZ_POP_FRAME.FOCAL_LENGTH, true);
	}
	
	public void ptzFocus() {
		initPTZPopFramePos();
		
		mPTZPopFrame.setVisibility(View.VISIBLE);
		showPTZFrame(PTZ_POP_FRAME.FOCUS, true);
	}
	
	public void ptzAperture() {
		initPTZPopFramePos();
		
		mPTZPopFrame.setVisibility(View.VISIBLE);
		showPTZFrame(PTZ_POP_FRAME.APERTURE, true);
	}
	
	public void ptzPresetPoint() {
		mPTZPopFrame.setVisibility(View.GONE);
		showPTZFrame(PTZ_POP_FRAME.PRESET, true);
	}
	
	private boolean checkIsPTZDeviceConnected() {
		return mLiveActivity.checkIsPTZDeviceConnected();
	}
	
	private void showToolbarExtendMenu(TOOLBAR_EXTEND_MENU menuId) {
		mLiveActivity.showToolbarExtendMenu(menuId);
	}

	public boolean isPTZModeOn() {
		return mIsPTZModeOn;
	}

	public void setIsPTZModeOn(boolean isPTZModeOn) {
		this.mIsPTZModeOn = isPTZModeOn;
	}

	public boolean isPTZInMoving() {
		return mIsPTZInMoving;
	}

	public void setIsPTZInMoving(boolean isPTZInMoving) {
		this.mIsPTZInMoving = isPTZInMoving;
	}
	
	public boolean isFlingAction() {
		return mIsFlingAction;
	}
	
	public void setIsFlingAction(boolean isFlingAction) {
		this.mIsFlingAction = isFlingAction;
	}
	
	public void setIsEnterPTZInSingleMode(boolean isEnterPTZInSingleMode) {
		this.mIsEnterPTZInSingleMode = isEnterPTZInSingleMode;
	}

	public PTZRequestSender getPtzReqSender() {
		return mPtzReqSender;
	}
	
	
	
}
