package com.starnet.snview.realplay;

import com.starnet.snview.R;
import com.starnet.snview.component.LandscapeToolbar;
import com.starnet.snview.component.liveview.LiveViewManager;
import com.starnet.snview.global.GlobalApplication;
import com.starnet.snview.realplay.RealplayActivity.TOOLBAR_EXTEND_MENU;
import com.starnet.snview.util.ActivityUtility;

import android.content.res.Resources;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.EditText;
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
	private ImageButton mPTZLandMenuScan;
	private ImageButton mPTZLandMenuFocalLength;
	private ImageButton mPTZLandMenuFocus;
	private ImageButton mPTZLandMenuAperture;
	private ImageButton mPTZLandMenuPreset;	
	
	private ImageButton mPTZPopFocalLengthIncrease;  
	private ImageButton mPTZPopFocalLengthDecrease;
	private ImageButton mPTZPopFocusIncrease;  
	private ImageButton mPTZPopFocusDecrease;
	private ImageButton mPTZPopApertureIncrease;  
	private ImageButton mPTZPopApertureDecrease;
	private EditText mPTZPresetEdit;
	private Button mPTZPresetCall;
	private Button mPTZPresetSet;
	
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
		mPTZLandMenuScan = (ImageButton) findViewById(R.id.landscape_liveview_ptz_auto);
		mPTZLandMenuFocalLength = (ImageButton) findViewById(R.id.landscape_liveview_ptz_focal_length);
		mPTZLandMenuFocus = (ImageButton) findViewById(R.id.landscape_liveview_ptz_focus);
		mPTZLandMenuAperture = (ImageButton) findViewById(R.id.landscape_liveview_ptz_aperture);
		mPTZLandMenuPreset = (ImageButton) findViewById(R.id.landscape_liveview_ptz_preset_point);

		mPTZMenuScan.setOnClickListener(mOnPTZMenuClickListener);
		mPTZMenuFocalLength.setOnClickListener(mOnPTZMenuClickListener);
		mPTZMenuFocus.setOnClickListener(mOnPTZMenuClickListener);
		mPTZMenuAperture.setOnClickListener(mOnPTZMenuClickListener);
		mPTZMenuPreset.setOnClickListener(mOnPTZMenuClickListener);
		mPTZLandMenuScan.setOnClickListener(mOnPTZMenuClickListener);
		mPTZLandMenuFocalLength.setOnClickListener(mOnPTZMenuClickListener);
		mPTZLandMenuFocus.setOnClickListener(mOnPTZMenuClickListener);
		mPTZLandMenuAperture.setOnClickListener(mOnPTZMenuClickListener);
		mPTZLandMenuPreset.setOnClickListener(mOnPTZMenuClickListener);
		
		
		mPTZPopFocalLengthIncrease = (ImageButton) findViewById(R.id.ptz_pop_focal_length_increase);  
		mPTZPopFocalLengthDecrease = (ImageButton) findViewById(R.id.ptz_pop_focal_length_decrease);
		mPTZPopFocusIncrease = (ImageButton) findViewById(R.id.ptz_pop_focus_increase);  
		mPTZPopFocusDecrease = (ImageButton) findViewById(R.id.ptz_pop_focus_decrease);
		mPTZPopApertureIncrease = (ImageButton) findViewById(R.id.ptz_pop_aperture_increase);  
		mPTZPopApertureDecrease = (ImageButton) findViewById(R.id.ptz_pop_aperture_decrease);
		mPTZPresetEdit = (EditText) findViewById(R.id.ptz_pop_preset_edit);
		mPTZPresetCall = (Button) findViewById(R.id.ptz_pop_preset_call);
		mPTZPresetSet = (Button) findViewById(R.id.ptz_pop_preset_set);
		
		//mPTZPopFocalLengthIncrease.setOnClickListener(mOnPTZPopClickListener);  
		//mPTZPopFocalLengthDecrease.setOnClickListener(mOnPTZPopClickListener);mOnPTZFocalLengthListener
		mPTZPopFocalLengthIncrease.setOnTouchListener(mOnPTZFocalLengthListener);
		mPTZPopFocalLengthDecrease.setOnTouchListener(mOnPTZFocalLengthListener);
		mPTZPopFocusIncrease.setOnClickListener(mOnPTZPopClickListener);  
		mPTZPopFocusDecrease.setOnClickListener(mOnPTZPopClickListener);
		mPTZPopApertureIncrease.setOnClickListener(mOnPTZPopClickListener);  
		mPTZPopApertureDecrease.setOnClickListener(mOnPTZPopClickListener);
		mPTZPresetCall.setOnClickListener(mOnPTZPopClickListener);
		mPTZPresetSet.setOnClickListener(mOnPTZPopClickListener);
		
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
			case R.id.landscape_liveview_ptz_auto:
				Log.i(TAG, "ptz_controlbar_menu_scan");
				ptzAuto();
				break;
			case R.id.ptz_controlbar_menu_focal_length:
			case R.id.landscape_liveview_ptz_focal_length:
				Log.i(TAG, "ptz_controlbar_menu_focal_length");
				ptzFocalLength();
				break;
			case R.id.ptz_controlbar_menu_focus:
			case R.id.landscape_liveview_ptz_focus:
				Log.i(TAG, "ptz_controlbar_menu_focus");
				ptzFocus();
				break;
			case R.id.ptz_controlbar_menu_aperture:
			case R.id.landscape_liveview_ptz_aperture:
				Log.i(TAG, "ptz_controlbar_menu_aperture");
				ptzAperture();
				break;
			case R.id.ptz_controlbar_menu_preset:
			case R.id.landscape_liveview_ptz_preset_point:
				Log.i(TAG, "ptz_controlbar_menu_preset");
				ptzPresetPoint();
				break;
			case R.id.landscape_liveview_ptz_bar_back:
				closePTZ();
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
				((LinearLayout) findViewById(R.id.ptz_pop_preset_frame))
						.setVisibility(View.GONE);
				break;
			case FOCAL_LENGTH:
				((LinearLayout) findViewById(R.id.ptz_pop_focal_length_frame))
						.setVisibility(View.VISIBLE);
				((LinearLayout) findViewById(R.id.ptz_pop_focus_frame))
						.setVisibility(View.GONE);
				((LinearLayout) findViewById(R.id.ptz_pop_aperture_frame))
						.setVisibility(View.GONE);
				((LinearLayout) findViewById(R.id.ptz_pop_preset_frame))
						.setVisibility(View.GONE);
				break;
			case FOCUS:
				((LinearLayout) findViewById(R.id.ptz_pop_focal_length_frame))
						.setVisibility(View.GONE);
				((LinearLayout) findViewById(R.id.ptz_pop_focus_frame))
						.setVisibility(View.VISIBLE);
				((LinearLayout) findViewById(R.id.ptz_pop_aperture_frame))
						.setVisibility(View.GONE);
				((LinearLayout) findViewById(R.id.ptz_pop_preset_frame))
						.setVisibility(View.GONE);
				break;
			case APERTURE:
				((LinearLayout) findViewById(R.id.ptz_pop_focal_length_frame))
						.setVisibility(View.GONE);
				((LinearLayout) findViewById(R.id.ptz_pop_focus_frame))
						.setVisibility(View.GONE);
				((LinearLayout) findViewById(R.id.ptz_pop_aperture_frame))
						.setVisibility(View.VISIBLE);
				((LinearLayout) findViewById(R.id.ptz_pop_preset_frame))
						.setVisibility(View.GONE);
				break;
			case PRESET:
				((LinearLayout) findViewById(R.id.ptz_pop_focal_length_frame))
						.setVisibility(View.GONE);
				((LinearLayout) findViewById(R.id.ptz_pop_focus_frame))
						.setVisibility(View.GONE);
				((LinearLayout) findViewById(R.id.ptz_pop_aperture_frame))
						.setVisibility(View.GONE);
				((LinearLayout) findViewById(R.id.ptz_pop_preset_frame))
						.setVisibility(View.VISIBLE);
				break;
			}
		} else {
			((LinearLayout) findViewById(R.id.ptz_pop_focal_length_frame))
					.setVisibility(View.GONE);
			((LinearLayout) findViewById(R.id.ptz_pop_focus_frame))
					.setVisibility(View.GONE);
			((LinearLayout) findViewById(R.id.ptz_pop_aperture_frame))
					.setVisibility(View.GONE);
			((LinearLayout) findViewById(R.id.ptz_pop_preset_frame))
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
			case R.id.ptz_pop_preset_call:
				Log.i(TAG, "ptz_pop_preset_call, text:" + mPTZPresetEdit.getText().toString());
				String num = mPTZPresetEdit.getText().toString();
				if (num != null && !num.equals("")) {
					mPtzReqSender.gotoPresetPoint(Integer.valueOf(num));
				}
				break;
			case R.id.ptz_pop_preset_set:
				Log.i(TAG, "ptz_pop_preset_set, text:" + mPTZPresetEdit.getText().toString());
				String num1 = mPTZPresetEdit.getText().toString();
				if (num1 != null && !num1.equals("")) {
					mPtzReqSender.setPresetPoint(Integer.valueOf(num1));
				}
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
				//mPTZPopFrame.setVisibility(View.VISIBLE);
				//showPTZFrame(PTZ_POP_FRAME.SCAN, false);
			} else {
				mPTZControlbarMenu.setVisibility(View.GONE);
				//mPTZPopFrame.setVisibility(View.GONE);
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
				initPTZPopFramePos(mPTZMenuPreset.isSelected()); // 调整弹窗位置
				mPTZControlbarMenu.setVisibility(View.GONE);
				mPTZPopFrame.setVisibility(View.VISIBLE);
				mLandscapeToolbar.setVisibility(View.VISIBLE);
				
				mPTZLandMenuScan.setSelected(mPTZMenuScan.isSelected());
				mPTZLandMenuFocalLength.setSelected(mPTZMenuFocalLength.isSelected());
				mPTZLandMenuFocus.setSelected(mPTZMenuFocus.isSelected());
				mPTZLandMenuAperture.setSelected(mPTZMenuAperture.isSelected());
				mPTZLandMenuPreset.setSelected(mPTZMenuPreset.isSelected());
				
				mLandscapeToolbar.showPTZControlbar();
			} else { // 当前为竖屏状态
				initPTZPopFramePos(mPTZLandMenuPreset.isSelected());
				mLandscapeToolbar.setVisibility(View.GONE);
				mPTZControlbarMenu.setVisibility(View.VISIBLE);
				mPTZPopFrame.setVisibility(View.VISIBLE);
				
				mPTZMenuScan.setSelected(mPTZLandMenuScan.isSelected());
				mPTZMenuFocalLength.setSelected(mPTZLandMenuFocalLength.isSelected());
				mPTZMenuFocus.setSelected(mPTZLandMenuFocus.isSelected());
				mPTZMenuAperture.setSelected(mPTZLandMenuAperture.isSelected());
				mPTZMenuPreset.setSelected(mPTZLandMenuPreset.isSelected());
				
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
	
	private void initPTZPopFramePos(boolean isPreset) {
		GlobalApplication g = GlobalApplication.getInstance();
		Resources res = mLiveActivity.getResources();
		RelativeLayout.LayoutParams lp;
		int w, h;
		
		if (isPreset) {
			w = LayoutParams.WRAP_CONTENT;
			h = res.getDrawable(R.drawable.ptz_presetpanelbg1).getIntrinsicHeight()
					+ res.getDrawable(R.drawable.ptz_presetpanelbg2).getIntrinsicHeight();
		} else {
			w = g.getPTZPopFrameWidth();
			h = g.getPTZPopFrameHeight();
		}
		lp = new LayoutParams(w, h);
		lp.addRule(RelativeLayout.CENTER_HORIZONTAL);
		
		if (g.isIsFullMode()) {
			lp.topMargin = g.getScreenHeight() - h
					- (int) mLiveActivity.getResources().getDimension(
							R.dimen.landscape_pop_frame_bottom_margin);
		} else {
			lp.topMargin = g.getScreenHeight()
					- ActivityUtility.getStatusBarHeight(mLiveActivity)
					- mLiveActivity.getNavbarContainer().getHeight()
					- h
					- (int) mLiveActivity.getResources().getDimension(
							R.dimen.toolbar_height) * 2
					- (int) mLiveActivity.getResources().getDimension(
							R.dimen.portrait_pop_frame_bottom_margin);
		}
		
		Log.i(TAG, "lp.topMargin:" + lp.topMargin + ", lp.bottomMargin:" + lp.bottomMargin);
		
		mPTZPopFrame.setLayoutParams(lp);
	}
	
	private void resetButtonStatus() {
		//mPTZMenuScan.setSelected(false);
		mPTZMenuFocalLength.setSelected(false);
		mPTZMenuFocus.setSelected(false);
		mPTZMenuAperture.setSelected(false);
		mPTZMenuPreset.setSelected(false);
		//mPTZLandMenuScan.setSelected(false);
		mPTZLandMenuFocalLength.setSelected(false);
		mPTZLandMenuFocus.setSelected(false);
		mPTZLandMenuAperture.setSelected(false);
		mPTZLandMenuPreset.setSelected(false);
	}
	
	public void ptzAuto() {
		resetButtonStatus();
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
		if (!GlobalApplication.getInstance().isIsFullMode()) {
			if (!mPTZMenuFocalLength.isSelected()) {
				initPTZPopFramePos(false);
				mPTZPopFrame.setVisibility(View.VISIBLE);
				showPTZFrame(PTZ_POP_FRAME.FOCAL_LENGTH, true);
				mPTZMenuScan.setSelected(false);
				mPTZMenuFocalLength.setSelected(true);
				mPTZMenuFocus.setSelected(false);
				mPTZMenuAperture.setSelected(false);
				mPTZMenuPreset.setSelected(false);
			} else {
				showPTZFrame(PTZ_POP_FRAME.FOCAL_LENGTH, false);
				mPTZMenuFocalLength.setSelected(false);
			}
		} else {
			if (!mPTZLandMenuFocalLength.isSelected()) {
				initPTZPopFramePos(false);
				mPTZPopFrame.setVisibility(View.VISIBLE);
				showPTZFrame(PTZ_POP_FRAME.FOCAL_LENGTH, true);
				mPTZLandMenuScan.setSelected(false);
				mPTZLandMenuFocalLength.setSelected(true);
				mPTZLandMenuFocus.setSelected(false);
				mPTZLandMenuAperture.setSelected(false);
				mPTZLandMenuPreset.setSelected(false);
			} else {
				showPTZFrame(PTZ_POP_FRAME.FOCAL_LENGTH, false);
				mPTZLandMenuFocalLength.setSelected(false);
			}
		}
		
	}
	
	public void ptzFocus() {
		if (!GlobalApplication.getInstance().isIsFullMode()) {
			if (!mPTZMenuFocus.isSelected()) {
				initPTZPopFramePos(false);
				mPTZPopFrame.setVisibility(View.VISIBLE);
				showPTZFrame(PTZ_POP_FRAME.FOCUS, true);
				mPTZMenuScan.setSelected(false);
				mPTZMenuFocalLength.setSelected(false);
				mPTZMenuFocus.setSelected(true);
				mPTZMenuAperture.setSelected(false);
				mPTZMenuPreset.setSelected(false);
			} else {
				showPTZFrame(PTZ_POP_FRAME.FOCUS, false);
				mPTZMenuFocus.setSelected(false);
			}
		} else {
			if (!mPTZLandMenuFocus.isSelected()) {
				initPTZPopFramePos(false);
				mPTZPopFrame.setVisibility(View.VISIBLE);
				showPTZFrame(PTZ_POP_FRAME.FOCUS, true);
				mPTZLandMenuScan.setSelected(false);
				mPTZLandMenuFocalLength.setSelected(false);
				mPTZLandMenuFocus.setSelected(true);
				mPTZLandMenuAperture.setSelected(false);
				mPTZLandMenuPreset.setSelected(false);
			} else {
				showPTZFrame(PTZ_POP_FRAME.FOCUS, false);
				mPTZLandMenuFocus.setSelected(false);
			}
		}		
	}
	
	public void ptzAperture() {
		if (!GlobalApplication.getInstance().isIsFullMode()) {
			if (!mPTZMenuAperture.isSelected()) {
				initPTZPopFramePos(false);
				mPTZPopFrame.setVisibility(View.VISIBLE);
				showPTZFrame(PTZ_POP_FRAME.APERTURE, true);
				mPTZMenuScan.setSelected(false);
				mPTZMenuFocalLength.setSelected(false);
				mPTZMenuFocus.setSelected(false);
				mPTZMenuAperture.setSelected(true);
				mPTZMenuPreset.setSelected(false);
			} else {
				showPTZFrame(PTZ_POP_FRAME.APERTURE, false);
				mPTZMenuAperture.setSelected(false);
			}
		} else {
			if (!mPTZLandMenuAperture.isSelected()) {
				initPTZPopFramePos(false);
				mPTZPopFrame.setVisibility(View.VISIBLE);
				showPTZFrame(PTZ_POP_FRAME.APERTURE, true);
				mPTZLandMenuScan.setSelected(false);
				mPTZLandMenuFocalLength.setSelected(false);
				mPTZLandMenuFocus.setSelected(false);
				mPTZLandMenuAperture.setSelected(true);
				mPTZLandMenuPreset.setSelected(false);
			} else {
				showPTZFrame(PTZ_POP_FRAME.APERTURE, false);
				mPTZLandMenuAperture.setSelected(false);
			}
		}
	}
	
	public void ptzPresetPoint() {
		if (!GlobalApplication.getInstance().isIsFullMode()) {
			if (!mPTZMenuPreset.isSelected()) {
				initPTZPopFramePos(true);
				mPTZPopFrame.setVisibility(View.VISIBLE);
				showPTZFrame(PTZ_POP_FRAME.PRESET, true);
				mPTZMenuScan.setSelected(false);
				mPTZMenuFocalLength.setSelected(false);
				mPTZMenuFocus.setSelected(false);
				mPTZMenuAperture.setSelected(false);
				mPTZMenuPreset.setSelected(true);
			} else {
				showPTZFrame(PTZ_POP_FRAME.PRESET, false);
				mPTZMenuPreset.setSelected(false);
			}
		} else {
			if (!mPTZLandMenuPreset.isSelected()) {
				initPTZPopFramePos(true);
				mPTZPopFrame.setVisibility(View.VISIBLE);
				showPTZFrame(PTZ_POP_FRAME.PRESET, true);
				mPTZLandMenuScan.setSelected(false);
				mPTZLandMenuFocalLength.setSelected(false);
				mPTZLandMenuFocus.setSelected(false);
				mPTZLandMenuAperture.setSelected(false);
				mPTZLandMenuPreset.setSelected(true);
			} else {
				showPTZFrame(PTZ_POP_FRAME.PRESET, false);
				mPTZLandMenuPreset.setSelected(false);
			}
		}
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
