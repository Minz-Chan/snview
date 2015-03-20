package com.starnet.snview.component;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
//import android.widget.RelativeLayout.LayoutParams;






import android.widget.TextView;


//import com.mcu.iVMS.global.GlobalApplication;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;

import com.starnet.snview.R;
import com.starnet.snview.global.GlobalApplication;
import com.starnet.snview.realplay.RealplayActivity;

public class LandscapeToolbar extends FrameLayout {
	private static final String TAG = "LandscapeToolbar";
	
	private static final int EXTEND_SPACE = 26;
	private static final int LONG_CLICK_TIME = 500;
	private static final int TOUCH_SLOP = 10;
	private static final int AUTO_DISMISS_TIME = 8;
	
	private Context mContext;

	private ArrayList<View> mAllChildList = new ArrayList<View>();

	private LinearLayout mControlBar;
	private ArrayList<View> mControlChildList = new ArrayList<View>();
	private ImageButton mShotPictureButton;
	private ImageButton mRecordButton;
	private ImageButton mPtzButton;
	private ImageButton mQualityButton;
	private ImageButton mStopResumeButton;
	private ImageButton mSoundButton;
	private ImageButton mVoicetalkButton;
	private ImageButton mAlarmButton;
	private ImageButton mEnlargeButton;
	private LandControlbarClickListener mLandControlbarListener;

	private LinearLayout mPTZControlBar;
	private LinearLayout mPTZPopFrame;
	private ArrayList<View> mPTZControlChildList = new ArrayList<View>();
	private ImageButton mLandAutoButton;
	private ImageButton mLandFocalLengthButton;
	private ImageButton mLandFocusButton;
	private ImageButton mLandApertrueButton;
	private ImageButton mLandPresetPointButton;
	private ImageButton mLandBarBackButton;
	private PTZBarClickListener mPTZBarClickListener;

	private LinearLayout mQualityControlBar;
	private ArrayList<View> mQualityControlChildList = new ArrayList<View>();
	private ImageButton mClearButton;
	private ImageButton mBalanceButton;
	private ImageButton mFluentButton;
	private ImageButton mCustomButton;
	private ImageButton mQualityBarBackButton;
	private QualityClickListener mQualityClickListener;

	private View mClickImageButton;
	private boolean mIsLandToolbarShow = false;
	private boolean mClickMode = true;
	private boolean mIsCanMove = false;
	private boolean mIsDragging = false;
	private boolean mIsCancleLongTouch = false;
	private boolean mIsControlBarExpanded = false;
	private boolean mIsPTZBarExpanded = false;
	private boolean mIsPTZshow = false;
	private boolean mIsQualityExpaned = false;
	private boolean mIsQualityShow = false;
	
	private LinearLayout mPageIndicatorFrame;
	private TextView mPageIndicator;

	private FrameLayout mLandscapeBarContent;
	private int mLastX;
	private int mLastY;
	private int mOffSpace = 0;

//	private int mRealHeight = 0;
//	private int mRealSubHeight = 0;
//	private int mRealSubWidth = 0;
//	private int mRealWidth = 0;

	private int mTouchCount = 0;
	
	
	private Timer mTimer = new Timer();
	private TimerTask mLandscapeBarAutoDismissTask;
	

	public LandscapeToolbar(Context context) {
		super(context);
		
		this.mContext = context;
	}

	public LandscapeToolbar(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		this.mContext = context;
	}

	private void canclePressedStatus() {
		mShotPictureButton.setPressed(false);
		mRecordButton.setPressed(false);
		mPtzButton.setPressed(false);
		mQualityButton.setPressed(false);
		mVoicetalkButton.setPressed(false);
		mSoundButton.setPressed(false);
		mAlarmButton.setPressed(false);
		mStopResumeButton.setPressed(false);
		mEnlargeButton.setPressed(false);
		mLandAutoButton.setPressed(false);
		mLandFocalLengthButton.setPressed(false);
		mLandFocusButton.setPressed(false);
		mLandApertrueButton.setPressed(false);
		mLandPresetPointButton.setPressed(false);
		mLandBarBackButton.setPressed(false);
		mClearButton.setPressed(false);
		mBalanceButton.setPressed(false);
		mFluentButton.setPressed(false);
		mCustomButton.setPressed(false);
		mQualityBarBackButton.setPressed(false);
	}

	private void clickAction(View v) {
		Log.i(TAG, "###clickAction");
		switch (v.getId()) {
		case 0:
		case 1: // 功能部分按钮
			break;
		case R.id.landscape_liveview_capture_button:
		case R.id.landscape_liveview_ptz_button:
		case R.id.landscape_liveview_quality_button:
		case R.id.landscape_liveview_delete_button:
		case R.id.landscape_liveview_record_button:
		case R.id.landscape_liveview_voicetalk_button:
		case R.id.landscape_liveview_sound_button:
			mLandControlbarListener.landControlbarClick(v);
			break;
		case 2:
		case 3: // PTZ部分按钮
			break;
		case R.id.landscape_liveview_ptz_auto:
		case R.id.landscape_liveview_ptz_focal_length:
		case R.id.landscape_liveview_ptz_focus:
		case R.id.landscape_liveview_ptz_aperture:
		case R.id.landscape_liveview_ptz_preset_point:
		case R.id.landscape_liveview_ptz_bar_back:
			mPTZBarClickListener.ptzBarClick(v);
			break;
		case 4:
		case 5: // 视频质量部分按钮
		case R.id.landscape_liveview_quality_clear_button:
		case R.id.landscape_liveview_quality_balance_button:
		case R.id.landscape_liveview_quality_fluent_button:
		case R.id.landscape_liveview_quality_custom_button:
		case R.id.landscape_liveview_quality_back_button:
			mQualityClickListener.qualityClick(v);
			break;
		}
	}

	/**
	 * 工具栏扩大
	 * 
	 * @param v
	 * @param expanded
	 * @param width
	 * @param height
	 */
	private void expandedContainerControl(View v, boolean expanded, int width,
			int height) {
		int expandSpace = EXTEND_SPACE / 2;

		if (expanded) { // 扩大
			RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) v
					.getLayoutParams();
			lp.topMargin = (lp.topMargin - expandSpace / 2);
			lp.leftMargin = (lp.leftMargin - expandSpace / 2);
			lp.width = (width + expandSpace);
			lp.height = (height + expandSpace);
			v.setLayoutParams(lp);
			v.requestLayout();
		} else { // 还原
			RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) v
					.getLayoutParams();
			lp.topMargin = (lp.topMargin + expandSpace / 2);
			lp.leftMargin = (lp.leftMargin + expandSpace / 2);
			lp.width = width;
			lp.height = height;
			v.setLayoutParams(lp);
			v.requestLayout();
		}
	}

	/**
	 * 子工具栏扩大
	 * 
	 * @param v
	 * @param expanded
	 * @param width
	 * @param height
	 */
	private void expandedSubControl(View v, boolean expanded, int width,
			int height) {
		int expandSpace = EXTEND_SPACE / 2;

		if (expanded) {
			FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) v
					.getLayoutParams();
			lp.width = (width + expandSpace);
			lp.height = (height + expandSpace);
			v.setLayoutParams(lp);
			v.requestLayout();
		} else {
			FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) v
					.getLayoutParams();
			lp.width = width;
			lp.height = height;
			v.setLayoutParams(lp);
			v.requestLayout();

		}
	}

	private boolean isCanMove(int offsetX, int offsetY) {
		if (Math.sqrt(offsetX * offsetX + offsetY * offsetY) > TOUCH_SLOP) {
			return true;
		} else {
			return false;
		}
	}

	private boolean isPTZShow() {
		return mIsPTZshow;
	}

	private View isPressAction(MotionEvent e) {
		int rawX = (int) e.getRawX();
		int rawY = (int) e.getRawY();

		View viewBeClicked = null;

		if (mControlBar.isShown()) {
			Iterator<View> controlChildIt;
			controlChildIt = mControlChildList.iterator();
			while (controlChildIt.hasNext()) {
				View v = (View) controlChildIt.next();
				int[] locations = new int[2];
				v.getLocationInWindow(locations);
				if (rawX > locations[0] && rawX < locations[0] + v.getWidth()
						&& rawY > locations[1]
						&& rawY < locations[1] + v.getHeight()) {
					if (!v.isEnabled()) {
						v = null;
					} else {
						viewBeClicked = v;
					}
				}
			}

		}

		if (mPTZControlBar.isShown()) {
			Iterator<View> ptzControlChildIt = mPTZControlChildList.iterator();
			while (ptzControlChildIt.hasNext()) {
				View v = (View) ptzControlChildIt.next();
				int[] locations = new int[2];
				v.getLocationInWindow(locations);
				if (rawX > locations[0] && rawX < locations[0] + v.getWidth()
						&& rawY > locations[1]
						&& rawY < locations[1] + v.getHeight()) {
					if (!v.isEnabled()) {
						v = null;
					} else {
						viewBeClicked = v;
					}
				}
			}
		}

		if (mQualityControlBar.isShown()) {
			Iterator<View> qualityControlChildIt = mQualityControlChildList
					.iterator();
			while (qualityControlChildIt.hasNext()) {
				View v = (View) qualityControlChildIt.next();
				int[] locations = new int[2];
				v.getLocationInWindow(locations);
				if (rawX > locations[0] && rawX < locations[0] + v.getWidth()
						&& rawY > locations[1]
						&& rawY < locations[1] + v.getHeight()) {
					if (!v.isEnabled()) {
						v = null;
					} else {
						viewBeClicked = v;
					}
				}
			}
		}

		return viewBeClicked;

	}

	private boolean isQualityShow() {
		return mIsQualityShow;
	}

	private void setActionButtonStatus(int viewId, int action) {

		mShotPictureButton.setPressed(false);
		mRecordButton.setPressed(false);
		mPtzButton.setPressed(false);
		mQualityButton.setPressed(false);
		mVoicetalkButton.setPressed(false);
		mSoundButton.setPressed(false);
		mAlarmButton.setPressed(false);
		mStopResumeButton.setPressed(false);
		mEnlargeButton.setPressed(false);
		mClearButton.setPressed(false);
		mBalanceButton.setPressed(false);
		mFluentButton.setPressed(false);

		switch (viewId) {
		case R.id.landscape_liveview_capture_button:
			mShotPictureButton.setPressed(true);
			break;
		case R.id.landscape_liveview_record_button:
			mRecordButton.setPressed(true);
			mRecordButton.setSelected(!mRecordButton.isSelected());
			break;
		case R.id.landscape_liveview_ptz_button:
			mPtzButton.setPressed(true);
			break;
		case R.id.landscape_liveview_quality_button:
			mQualityButton.setPressed(true);
			break;
		case R.id.landscape_liveview_delete_button:
			mStopResumeButton.setPressed(true);
			break;
		case R.id.landscape_liveview_sound_button:
			mSoundButton.setPressed(true);
			break;
		case R.id.landscape_liveview_voicetalk_button:
			mVoicetalkButton.setPressed(true);
			break;
		case R.id.landscape_liveview_alarm_button:
			mAlarmButton.setPressed(true);
			break;
		case R.id.landscape_liveview_enlarge_button:
			mEnlargeButton.setPressed(true);
			break;
		case R.id.landscape_liveview_ptz_auto:
			mLandAutoButton.setPressed(true);
			break;
		case R.id.landscape_liveview_ptz_focal_length:
			mLandFocalLengthButton.setPressed(true);
			break;
		case R.id.landscape_liveview_ptz_focus:
			mLandFocusButton.setPressed(true);
			break;
		case R.id.landscape_liveview_ptz_aperture:
			mLandApertrueButton.setPressed(true);
			break;
		case R.id.landscape_liveview_ptz_preset_point:
			mLandPresetPointButton.setPressed(true);
			break;
		case R.id.landscape_liveview_ptz_bar_back:
			mLandBarBackButton.setPressed(true);
			break;
		case R.id.landscape_liveview_quality_clear_button:
			mClearButton.setPressed(true);
			break;
		case R.id.landscape_liveview_quality_balance_button:
			mBalanceButton.setPressed(true);
			break;
		case R.id.landscape_liveview_quality_fluent_button:
			mFluentButton.setPressed(true);
			break;
		case R.id.landscape_liveview_quality_custom_button:
			mCustomButton.setPressed(true);
			break;
		case R.id.landscape_liveview_quality_back_button:
			mQualityBarBackButton.setPressed(true);
			break;
		default:
			break;
		}

	}

	private void setLandToolbarShow(boolean isShow) {
		this.mIsLandToolbarShow = isShow;
	}
	
	public boolean isLandToolbarShow() {
		return mIsLandToolbarShow;
	}
	
	private void setPTZShow(boolean isShow) {
		this.mIsPTZshow = isShow;
	}

	private void setQualityShow(boolean isShow) {
		this.mIsQualityShow = isShow;
	}

	private void unSelectedAllQualityButton() {
		mClearButton.setSelected(false);
		mBalanceButton.setSelected(false);
		mFluentButton.setSelected(false);
		mCustomButton.setSelected(false);
	}


	public void expandedControl(boolean paramBoolean) {
		
	}

	public void findViews() {
		mControlChildList.clear();
		mPTZControlChildList.clear();
		mLandscapeBarContent = ((FrameLayout) findViewById(R.id.landscape_liveview_controlbar_content));

		mPageIndicator = (TextView) getRP().findViewById(R.id.landscape_liveview_pageindicator);
		mPageIndicatorFrame = (LinearLayout) getRP().findViewById(R.id.landscape_liveview_pageindicator_frame);
		
		
		/* 功能工具条 */
		mControlBar = ((LinearLayout) findViewById(R.id.landscape_liveview_controlbar));
		mShotPictureButton = ((ImageButton) findViewById(R.id.landscape_liveview_capture_button));
		mRecordButton = ((ImageButton) findViewById(R.id.landscape_liveview_record_button));
		mPtzButton = ((ImageButton) findViewById(R.id.landscape_liveview_ptz_button));
		mQualityButton = ((ImageButton) findViewById(R.id.landscape_liveview_quality_button));
		mStopResumeButton = ((ImageButton) findViewById(R.id.landscape_liveview_delete_button));
		mSoundButton = ((ImageButton) findViewById(R.id.landscape_liveview_sound_button));
		mVoicetalkButton = ((ImageButton) findViewById(R.id.landscape_liveview_voicetalk_button));
		mAlarmButton = ((ImageButton) findViewById(R.id.landscape_liveview_alarm_button));
		mEnlargeButton = ((ImageButton) findViewById(R.id.landscape_liveview_enlarge_button));

		mControlChildList.add(mShotPictureButton);
		mControlChildList.add(mRecordButton);
		mControlChildList.add(mPtzButton);
		mControlChildList.add(mQualityButton);
		mControlChildList.add(mVoicetalkButton);
		mControlChildList.add(mAlarmButton);
		mControlChildList.add(mStopResumeButton);
		mControlChildList.add(mSoundButton);
		mControlChildList.add(mEnlargeButton);

		/* PTZ控制条 */
		mPTZControlBar = ((LinearLayout) findViewById(R.id.landscape_liveview_ptz_control_bar));
		mPTZPopFrame = (LinearLayout) getRP().findViewById(R.id.ptz_pop_frame);
		mLandAutoButton = ((ImageButton) findViewById(R.id.landscape_liveview_ptz_auto));
		mLandFocalLengthButton = ((ImageButton) findViewById(R.id.landscape_liveview_ptz_focal_length));
		mLandFocusButton = ((ImageButton) findViewById(R.id.landscape_liveview_ptz_focus));
		mLandApertrueButton = ((ImageButton) findViewById(R.id.landscape_liveview_ptz_aperture));
		mLandPresetPointButton = ((ImageButton) findViewById(R.id.landscape_liveview_ptz_preset_point));
		mLandBarBackButton = ((ImageButton) findViewById(R.id.landscape_liveview_ptz_bar_back));

		mPTZControlChildList.add(mLandAutoButton);
		mPTZControlChildList.add(mLandFocalLengthButton);
		mPTZControlChildList.add(mLandFocusButton);
		mPTZControlChildList.add(mLandApertrueButton);
		mPTZControlChildList.add(mLandPresetPointButton);
		mPTZControlChildList.add(mLandBarBackButton);

		/* 视频质量控制条 */
		mQualityControlBar = ((LinearLayout) findViewById(R.id.landscape_liveview_quality_control_bar));
		mClearButton = ((ImageButton) findViewById(R.id.landscape_liveview_quality_clear_button));
		mBalanceButton = ((ImageButton) findViewById(R.id.landscape_liveview_quality_balance_button));
		mFluentButton = ((ImageButton) findViewById(R.id.landscape_liveview_quality_fluent_button));
		mCustomButton = ((ImageButton) findViewById(R.id.landscape_liveview_quality_custom_button));
		mQualityBarBackButton = ((ImageButton) findViewById(R.id.landscape_liveview_quality_back_button));

		mQualityControlChildList.add(mClearButton);
		mQualityControlChildList.add(mBalanceButton);
		mQualityControlChildList.add(mFluentButton);
		mQualityControlChildList.add(mCustomButton);
		mQualityControlChildList.add(mQualityBarBackButton);

		mAllChildList.addAll(mControlChildList);
		mAllChildList.addAll(mPTZControlChildList);
		mAllChildList.addAll(mQualityControlChildList);
	}

	public void switchStopResumeButtonStatus(boolean isStop) {
		if (isStop) {
			mStopResumeButton.setSelected(false);
		} else {
			mStopResumeButton.setSelected(true);
		}
	}
	
	
	
	public void showLandscapeToolbar() {
		setLandToolbarShow(true);
		mLandscapeBarContent.setVisibility(View.VISIBLE);
		mPageIndicatorFrame.setVisibility(View.VISIBLE);
		syncUIElementsStatus();
		
		mPageIndicator.setText(getRP().getPager().getPagerText());
		
		//mTimer.cancel();
		if (mLandscapeBarAutoDismissTask != null) {
			mLandscapeBarAutoDismissTask.cancel();
		}
		
		mLandscapeBarAutoDismissTask = new TimerTask() {
			@Override
			public void run() {
				LandscapeToolbar.this.post(new Runnable() {
					@Override
					public void run() {
						setLandToolbarShow(false);
						mLandscapeBarContent.setVisibility(View.GONE);
						mPageIndicatorFrame.setVisibility(View.GONE);
						if (GlobalApplication.getInstance().isIsFullMode()) {
							mPTZPopFrame.setVisibility(View.GONE);
						}
					}
				});
			}
		};
		
		mTimer.schedule(mLandscapeBarAutoDismissTask, AUTO_DISMISS_TIME * 1000);
	}
	
	private void syncUIElementsStatus() {
		restoreLandscapePTZPopFrameStatus();
		getRP().updateUIElementsStatus();
	}
	
	private void restoreLandscapePTZPopFrameStatus() {
		boolean canPopFrameShow = mLandFocalLengthButton.isSelected()
				|| mLandFocusButton.isSelected()
				|| mLandApertrueButton.isSelected()
				|| mLandPresetPointButton.isSelected();
		
		mPTZPopFrame.setVisibility(canPopFrameShow ? View.VISIBLE : View.GONE);
	}
	
	private RealplayActivity getRP() {
		return (RealplayActivity) mContext;
	}
	
	public void showControlbar() {
		showLandscapeToolbar();
		
		setPTZShow(false);
		setQualityShow(false);
		mControlBar.setVisibility(View.VISIBLE);
		mPTZControlBar.setVisibility(View.GONE);
		mQualityControlBar.setVisibility(View.GONE);
	}

	public void showPTZControlbar() {
		showLandscapeToolbar();
		
		setPTZShow(true);
		mPTZControlBar.setVisibility(View.VISIBLE);
		mControlBar.setVisibility(View.GONE);
		mQualityControlBar.setVisibility(View.GONE);
	}

	public void showQualityControlBar() {
		showLandscapeToolbar();
		
		setQualityShow(true);
		mQualityControlBar.setVisibility(View.VISIBLE);
		mPTZControlBar.setVisibility(View.GONE);
		mControlBar.setVisibility(View.GONE);
	}
	
	public void hideLandscapeToolbar() {
		setLandToolbarShow(false);
		mLandscapeBarContent.setVisibility(View.GONE);
		mPageIndicatorFrame.setVisibility(View.GONE);
		mPTZPopFrame.setVisibility(View.GONE);
		
		if (mLandscapeBarAutoDismissTask != null) {
			mLandscapeBarAutoDismissTask.cancel();
		}
		
		mLandscapeBarAutoDismissTask = null;
	}

	public void hidePTZbar() {
		setPTZShow(false);
		mPTZControlBar.setVisibility(View.GONE);
		if (isQualityShow()) {
			mQualityControlBar.setVisibility(View.VISIBLE);
			mControlBar.setVisibility(View.GONE);
		} else {
			mQualityControlBar.setVisibility(View.GONE);
			mControlBar.setVisibility(View.VISIBLE);
		}
	}

	public void hideQualitybar() {
		setQualityShow(false);
		mQualityControlBar.setVisibility(View.GONE);
		if (isPTZShow()) {
			mPTZControlBar.setVisibility(View.VISIBLE);
			mControlBar.setVisibility(View.GONE);
		} else {
			mPTZControlBar.setVisibility(View.GONE);
			mControlBar.setVisibility(View.VISIBLE);
		}
	}

	public boolean isControlBarExpanded() {
		return mIsControlBarExpanded;
	}

	public boolean isPTZbarExpanded() {
		return mIsPTZBarExpanded;
	}

	public boolean isQualityBarExpanded() {
		return mIsQualityExpaned;
	}

	protected void onAttachedToWindow() {
		super.onAttachedToWindow();
		expandedControl(false);
	}

	protected void onLayout(boolean change, int left, int top, int right,
			int bottom) {
		super.onLayout(change, left, top, right, bottom);
	}
	
	

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		return mIsDragging;
	}

	public boolean onTouchEvent(MotionEvent e) {
		int sWidth = GlobalApplication.getInstance().getScreenWidth();
		int sHeight = GlobalApplication.getInstance().getScreenHeight();
		int action = e.getAction();
		switch (action) {
		case MotionEvent.ACTION_DOWN:
			mLastX = (int) e.getRawX();
			mLastY = (int) e.getRawY();
			mClickMode = true;
			mIsCancleLongTouch = false;
			mTouchCount = (1 + mTouchCount);
			mIsCanMove = false;
			mIsDragging = false;
			mLastX = ((int) e.getRawX());
			mLastY = ((int) e.getRawY());
			mClickImageButton = isPressAction(e);
			if (mClickImageButton != null) {
				setActionButtonStatus(mClickImageButton.getId(), action);
			}
			
			showLandscapeToolbar();
			break;
		case MotionEvent.ACTION_MOVE:
			int rawX = (int) e.getRawX();
			int rawY = (int) e.getRawY();
			int offsetX = rawX - mLastX;
			int offsetY = rawY - mLastY;
			int newLeft = offsetX + getLeft();
			int newTop = offsetY + getTop();
			int newRight = offsetX + getRight();
			int newBottom = offsetY + getBottom();
			
			if (newLeft < mOffSpace) {
				newLeft = mOffSpace;
				newRight = newLeft + getWidth();
			}
			if (newRight > sWidth + mOffSpace) {
				newLeft = sWidth + mOffSpace - getWidth();
			}
			if (newTop < mOffSpace) {
				newTop = mOffSpace;
				newBottom = newTop + getHeight();
			}
			if (newBottom > sHeight + mOffSpace) {
				newTop = sHeight + mOffSpace - getHeight();
			}
			if (!mIsCanMove) {
				mIsCanMove = isCanMove(offsetX, offsetY);
			}
			
			if (!mIsCanMove) {
				return true;
			}

//			if (this.mIsCanMove) {
//				this.mClickMode = true;
//				this.mIsCancleLongTouch = false;
//				this.mTouchCount = (1 + this.mTouchCount);
//				this.mIsCanMove = false;
//				this.mLastX = ((int) e.getRawX());
//				this.mLastY = ((int) e.getRawY());
//				this.mClickImageButton = isPressAction(e);
//				if (this.mClickImageButton != null) {
//					setActionButtonStatus(this.mClickImageButton.getId(),
//							action);
//				}
//			} else {
//				return true;
//			}

			//canclePressedStatus();
			mIsCancleLongTouch = true;
			if (mClickMode) {
				mClickMode = false;
				expandedControl(true);
			}
			
			mIsDragging = true;

			mLastX = rawX;
			mLastY = rawY;
			RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) getLayoutParams();
			lp.leftMargin = newLeft;
			lp.topMargin = newTop;
			setLayoutParams(lp);

			requestLayout();
			
			break;			
		case MotionEvent.ACTION_UP:
			mIsCanMove = false;
			mIsCancleLongTouch = true;

			if (!mClickMode) {
				expandedControl(false);
			}
			
			canclePressedStatus();

			if (mClickImageButton != null && !mIsDragging) {
				Log.i(TAG, "Click Action occur!!!!!");
				clickAction(mClickImageButton);
				playSoundEffect(0);
			}
			
			showLandscapeToolbar();
			
			mIsDragging = false;
			break;
		case MotionEvent.ACTION_CANCEL:
			Log.i(TAG, "ACTION_CANCEL occur");
			break;
		default:
			break;
		}

		

		return true;

		// if (!this.mClickMode) {
		// expandedControl(false);
		// }
		// for (;;)
		// {
		// canclePressedStatus();
		// break;
		// if (this.mClickImageButton != null)
		// {
		// clickAction(this.mClickImageButton);
		// playSoundEffect(0);
		// }
		// }
	}

	public void setControlBarExpanded(boolean isExpanded) {
		this.mIsControlBarExpanded = isExpanded;
	}

	public void setOnControlbarClickListener(
			LandControlbarClickListener listener) {
		this.mLandControlbarListener = listener;
	}

	public void setOnPTZBarClickListener(
			PTZBarClickListener listener) {
		this.mPTZBarClickListener = listener;
	}

	public void setOnQualityClickListener(
			QualityClickListener listener) {
		this.mQualityClickListener = listener;
	}

	public void setPTZBarExpanded(boolean isExpanded) {
		this.mIsPTZBarExpanded = isExpanded;
	}

	public void setQualityBarExpaned(boolean isExpanded) {
		this.mIsQualityExpaned = isExpanded;
	}

	public void setQualityLevel(QUALITY_LEVEL level) {
		unSelectedAllQualityButton();

		switch (level) {
		case CLEAR:
			mClearButton.setSelected(true);
			break;
		case BLANCE:
			mBalanceButton.setSelected(true);
			break;
		case FLUENT:
			mFluentButton.setSelected(true);
			break;
		case CUSTOM:
			mCustomButton.setSelected(true);
			break;
		}

	}
	

	@Override
	protected void finalize() throws Throwable {
		mTimer.cancel();
		
		super.finalize();
	}

	public void setRecordButtonSelected(boolean isSelected) {
		this.mRecordButton.setSelected(isSelected);
	}
	
	public ImageButton getAlarmButton() {
		return mAlarmButton;
	}

	public ImageButton getApertureButton() {
		return mLandApertrueButton;
	}

	public ImageButton getAutoButton() {
		return mLandAutoButton;
	}

	public ImageButton getBalanceButton() {
		return mBalanceButton;
	}

	public ImageButton getClearButton() {
		return mClearButton;
	}

	public ImageButton getCustomButton() {
		return mCustomButton;
	}

	public ImageButton getEnlargeButton() {
		return mEnlargeButton;
	}

	public ImageButton getFluentButton() {
		return mFluentButton;
	}

	public ImageButton getFocalLengthButton() {
		return mLandFocalLengthButton;
	}

	public ImageButton getFocusButton() {
		return mLandFocusButton;
	}

	public ImageButton getPresetPointButton() {
		return mLandPresetPointButton;
	}

	public ImageButton getPtzButton() {
		return mPtzButton;
	}

	public ImageButton getQualityButton() {
		return mQualityButton;
	}

	public ImageButton getRecoredButton() {
		return mRecordButton;
	}

	public ImageButton getSoundButton() {
		return mSoundButton;
	}

	public ImageButton getStopResumeButton() {
		return mStopResumeButton;
	}

	public ImageButton getVoiceTalkButton() {
		return mVoicetalkButton;
	}

	

	public static abstract interface LandControlbarClickListener {
		public abstract void landControlbarClick(View paramView);
	}

	private class LongPressRunnable implements Runnable {
		private LongPressRunnable() {
		}

		public void run() {
			LandscapeToolbar localLandscapeToolbar = LandscapeToolbar.this;
			localLandscapeToolbar.mTouchCount = (-1 + localLandscapeToolbar.mTouchCount);
			if ((LandscapeToolbar.this.mTouchCount > 0)
					|| (LandscapeToolbar.this.mIsCancleLongTouch)) {

			} else {
				if (LandscapeToolbar.this.mClickMode) {
					LandscapeToolbar.this.mClickMode = false;
					LandscapeToolbar.this.canclePressedStatus();
					LandscapeToolbar.this.expandedControl(true);
					LandscapeToolbar.this.mIsCanMove = true;
				}
			}
		}
	}

	public static abstract interface PTZBarClickListener {
		public abstract void ptzBarClick(View v);
	}

	public static abstract interface QualityClickListener {
		public abstract void qualityClick(View v);
	}
	
	public static enum QUALITY_LEVEL {
		CLEAR,
		BLANCE,
		FLUENT,
		CUSTOM
	}
}
