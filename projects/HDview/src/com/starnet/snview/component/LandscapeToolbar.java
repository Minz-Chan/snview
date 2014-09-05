package com.starnet.snview.component;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
//import android.widget.RelativeLayout.LayoutParams;


//import com.mcu.iVMS.global.GlobalApplication;
import java.util.ArrayList;
import java.util.Iterator;

import com.starnet.snview.R;
import com.starnet.snview.global.GlobalApplication;

public class LandscapeToolbar extends FrameLayout {
	private static final String TAG = "LandscapeToolbar";
	
	private static final int EXTEND_SPACE = 26;
	private static final int LONG_CLICK_TIME = 500;
	private static final int TOUCH_SLOP = 10;

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
	private ArrayList<View> mPTZControlChildList = new ArrayList<View>();
	private ImageButton mAutoButton;
	private ImageButton mFocalLengthButton;
	private ImageButton mFocusButton;
	private ImageButton mApertrueButton;
	private ImageButton mPresetPointButton;
	private ImageButton mBarBackButton;
	private PTZBarClickListener mPTZBarClickListener;

	private LinearLayout mQualityControlBar;
	private ArrayList<View> mQualityControlChildList = new ArrayList<View>();
	private Button mClearButton;
	private Button mBalanceButton;
	private Button mFluentButton;
	private Button mCustomButton;
	private ImageButton mQualityBarBackButton;
	private QualityClickListener mQualityClickListener;

	private View mClickImageButton;
	private boolean mClickMode = true;
	private boolean mIsCanMove = false;
	private boolean mIsCancleLongTouch = false;
	private boolean mIsControlBarExpanded = false;
	private boolean mIsPTZBarExpanded = false;
	private boolean mIsPTZshow = false;
	private boolean mIsQualityExpaned = false;
	private boolean mIsQualityShow = false;

	private FrameLayout mLandscapeBarContent;
	private int mLastX;
	private int mLastY;
	private int mOffSpace = 0;

	private int mRealHeight = 0;
	private int mRealSubHeight = 0;
	private int mRealSubWidth = 0;
	private int mRealWidth = 0;

	private int mTouchCount = 0;

	public LandscapeToolbar(Context context) {
		super(context);
	}

	public LandscapeToolbar(Context context, AttributeSet attrs) {
		super(context, attrs);
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
		mAutoButton.setPressed(false);
		mFocalLengthButton.setPressed(false);
		mFocusButton.setPressed(false);
		mApertrueButton.setPressed(false);
		mPresetPointButton.setPressed(false);
		mBarBackButton.setPressed(false);
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
			Iterator controlChildIt;
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
			Iterator ptzControlChildIt = mPTZControlChildList.iterator();
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
			Iterator qualityControlChildIt = mQualityControlChildList
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
			mAutoButton.setPressed(true);
			break;
		case R.id.landscape_liveview_ptz_focal_length:
			mFocalLengthButton.setPressed(true);
			break;
		case R.id.landscape_liveview_ptz_focus:
			mFocusButton.setPressed(true);
			break;
		case R.id.landscape_liveview_ptz_aperture:
			mApertrueButton.setPressed(true);
			break;
		case R.id.landscape_liveview_ptz_preset_point:
			mPresetPointButton.setPressed(true);
			break;
		case R.id.landscape_liveview_ptz_bar_back:
			mBarBackButton.setPressed(true);
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
		mAutoButton = ((ImageButton) findViewById(R.id.landscape_liveview_ptz_auto));
		mFocalLengthButton = ((ImageButton) findViewById(R.id.landscape_liveview_ptz_focal_length));
		mFocusButton = ((ImageButton) findViewById(R.id.landscape_liveview_ptz_focus));
		mApertrueButton = ((ImageButton) findViewById(R.id.landscape_liveview_ptz_aperture));
		mPresetPointButton = ((ImageButton) findViewById(R.id.landscape_liveview_ptz_preset_point));
		mBarBackButton = ((ImageButton) findViewById(R.id.landscape_liveview_ptz_bar_back));

		mPTZControlChildList.add(mAutoButton);
		mPTZControlChildList.add(mFocalLengthButton);
		mPTZControlChildList.add(mFocusButton);
		mPTZControlChildList.add(mApertrueButton);
		mPTZControlChildList.add(mPresetPointButton);
		mPTZControlChildList.add(mBarBackButton);

		/* 视频质量控制条 */
		mQualityControlBar = ((LinearLayout) findViewById(R.id.landscape_liveview_quality_control_bar));
		mClearButton = ((Button) findViewById(R.id.landscape_liveview_quality_clear_button));
		mBalanceButton = ((Button) findViewById(R.id.landscape_liveview_quality_balance_button));
		mFluentButton = ((Button) findViewById(R.id.landscape_liveview_quality_fluent_button));
		mCustomButton = ((Button) findViewById(R.id.landscape_liveview_quality_custom_button));
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
	
	public void showControlbar() {
		setPTZShow(false);
		setQualityShow(false);
		mControlBar.setVisibility(View.VISIBLE);
		mPTZControlBar.setVisibility(View.GONE);
		mQualityControlBar.setVisibility(View.GONE);
	}

	public void showPTZControlbar() {
		setPTZShow(true);
		mPTZControlBar.setVisibility(View.VISIBLE);
		mControlBar.setVisibility(View.GONE);
		mQualityControlBar.setVisibility(View.GONE);
	}

	public void showQualityControlBar() {
		setQualityShow(true);
		mQualityControlBar.setVisibility(View.VISIBLE);
		mPTZControlBar.setVisibility(View.GONE);
		mControlBar.setVisibility(View.GONE);
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
			mLastX = ((int) e.getRawX());
			mLastY = ((int) e.getRawY());
			mClickImageButton = isPressAction(e);
			if (mClickImageButton != null) {
				setActionButtonStatus(mClickImageButton.getId(), action);
			}
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

			canclePressedStatus();
			mIsCancleLongTouch = true;
			if (mClickMode) {
				mClickMode = false;
				expandedControl(true);
			}

			mLastX = rawX;
			mLastY = rawY;
			RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) getLayoutParams();
			lp.leftMargin = newLeft;
			lp.topMargin = newTop;
			setLayoutParams(lp);

			requestLayout();
			
		case MotionEvent.ACTION_UP:
			mIsCanMove = false;
			mIsCancleLongTouch = true;

			if (!mClickMode) {
				expandedControl(false);
			}
			
			canclePressedStatus();

			if (mClickImageButton != null) {
				clickAction(mClickImageButton);
				playSoundEffect(0);
			}
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

	public void setRecordButtonSelected(boolean isSelected) {
		this.mRecordButton.setSelected(isSelected);
	}
	
	public ImageButton getAlarmButton() {
		return mAlarmButton;
	}

	public ImageButton getApertureButton() {
		return mApertrueButton;
	}

	public ImageButton getAutoButton() {
		return mAutoButton;
	}

	public Button getBalanceButton() {
		return mBalanceButton;
	}

	public Button getClearButton() {
		return mClearButton;
	}

	public Button getCustomButton() {
		return mCustomButton;
	}

	public ImageButton getEnlargeButton() {
		return mEnlargeButton;
	}

	public Button getFluentButton() {
		return mFluentButton;
	}

	public ImageButton getFocalLengthButton() {
		return mFocalLengthButton;
	}

	public ImageButton getFocusButton() {
		return mFocusButton;
	}

	public ImageButton getPresetPointButton() {
		return mPresetPointButton;
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
