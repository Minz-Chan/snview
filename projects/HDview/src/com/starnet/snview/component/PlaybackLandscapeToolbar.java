package com.starnet.snview.component;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;

import com.starnet.snview.R;
import com.starnet.snview.global.GlobalApplication;
import com.starnet.snview.playback.PlaybackActivity;
import com.starnet.snview.playback.TimeBar;
import com.starnet.snview.playback.TimeBar.OnActionMoveCallback;
import com.starnet.snview.util.ActivityUtility;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.MeasureSpec;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

public class PlaybackLandscapeToolbar extends LinearLayout {
	private static final String TAG = "PlayBackLandscapeToolBar";
	
	private static final int EXTEND_SPACE = 16;
	private static final int LONG_CLICK_TIME = 500;
	private static final int TOUCH_SLOP = 10;
	
	private Context mContext;
	private LinearLayout mControlBarContent;
	
	private int mLastX;
	private int mLastY;
	private int mOffSpace = 0;
	private boolean mClickMode = true;
	private boolean mIsCanMove = false;
	private boolean mIsCancleLongTouch = false;
	private boolean mIsControlBarExpanded = false;
	
	private LandscapeToolbar.LandControlbarClickListener mLandControlbarListener;
	private ArrayList<ImageButton> mControlChildList = new ArrayList<ImageButton>();
	private ImageButton mClickImageButton;
	private ImageButton mPausePlayButton;
	private ImageButton mRecordButton;
	private ImageButton mShotPictureButton;
	private ImageButton mSoundButton;
	private ImageButton mStopButton;
	private ImageButton mEnlargeButton;
	
	
	private TimeBar mTimeBar;
	private int mTimeBarLayoutHeight = 0;
	private int mTimeBarLayoutWidth = 0;
	private LinearLayout mTimerBarLayout;
	
	private int mLandScapeWidth = 0;
	private int mToolbarHeight = 0;
	private int mToolbarWidth = 0;
	private int mTouchCount = 0;
	private int mShowButtonCount = 0;

	public PlaybackLandscapeToolbar(Context context) {
		super(context);
		this.mContext = context;
	}

	public PlaybackLandscapeToolbar(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.mContext = context;
	}
	
	public TimeBar getTimeBar() {
		return mTimeBar;
	}

	private void cancleAction() {
		mPausePlayButton.setPressed(false);
		mShotPictureButton.setPressed(false);
		mRecordButton.setPressed(false);
		mStopButton.setPressed(false);
		mSoundButton.setPressed(false);
		mEnlargeButton.setPressed(false);
	}

	private void clickAction(ImageButton btn) {
		switch (btn.getId()) {
		default:
			mLandControlbarListener.landControlbarClick(btn);
			break;
		}
	}

	private boolean isCanMove(int offsetX, int offsetY) {
		if (Math.sqrt(offsetX * offsetX + offsetY * offsetY) > TOUCH_SLOP) {
			return true;
		} else {
			return false;
		}
	}

	private View isPressAction(MotionEvent e) {
		int rawX = (int) e.getRawX();
		int rawY = (int) e.getRawY();

		View viewBeClicked = null;

		if (isShown()) {
			Iterator<ImageButton> controlChildIt;
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

		return viewBeClicked;
	}

	private void setActionButtonStatus(int viewId, int action) {
		cancleAction();
		switch (viewId) {
		case R.id.playback_landscape_capture_button:
			mShotPictureButton.setPressed(true);
			break;
		case R.id.playback_landscape_record_button:
			mRecordButton.setPressed(true);
			break;
		case R.id.playback_landscape_pause_play_button:
			mPausePlayButton.setPressed(true);
			break;
		case R.id.playback_landscape_sound_button:
			mSoundButton.setPressed(true);
			break;
		case R.id.playback_landscape_stop_button:
			mStopButton.setPressed(true);
			break;
		default:
			break;
		}
	}

	public void findViews() {
		mControlChildList.clear();
		mControlBarContent = ((LinearLayout) findViewById(R.id.playback_landscape_toolbar_layout));
		mPausePlayButton = ((ImageButton) findViewById(R.id.playback_landscape_pause_play_button));
		mShotPictureButton = ((ImageButton) findViewById(R.id.playback_landscape_capture_button));
		mRecordButton = ((ImageButton) findViewById(R.id.playback_landscape_record_button));
		mStopButton = ((ImageButton) findViewById(R.id.playback_landscape_stop_button));
		mSoundButton = ((ImageButton) findViewById(R.id.playback_landscape_sound_button));
		mEnlargeButton = ((ImageButton) findViewById(R.id.playback_landscape_enlarge_button));
		mTimerBarLayout = ((LinearLayout) findViewById(R.id.playback_landscape_timebar_frame));
		mTimeBar = ((TimeBar) findViewById(R.id.landscape_timebar_control));
		mControlChildList.add(mPausePlayButton);
		mControlChildList.add(mShotPictureButton);
		mControlChildList.add(mRecordButton);
		mControlChildList.add(mStopButton);
		mControlChildList.add(mSoundButton);
		mControlChildList.add(mEnlargeButton);
		mShowButtonCount = 5;
		
		mPausePlayButton.setSelected(true);
		mTimeBar.setMiddleTimeColor(Color.WHITE);
		mTimeBar.setMiddleLineColor(Color.YELLOW);
		mTimeBar.setScaleColor(Color.LTGRAY);
		mTimeBar.setOnActionMoveCallback(new OnActionMoveCallback() {
			@Override
			public void onActionMove(MotionEvent e) {
				Log.d(TAG, "onActionMove");
				onTimeBarActionMove(e);
				((PlaybackActivity)mContext).onTimebarActionMove(e);
			}
		});
	}
	
	private void onTimeBarActionMove(MotionEvent e) {
		scheduleLandbarAutoDismiss();
	}

	public int getLandscapeWidth() {
		Drawable bgToolbarLeft = mContext.getResources().getDrawable(
				R.drawable.playback_fullscreen_toolbar_left);
		Rect r1 = new Rect();
		bgToolbarLeft.getPadding(r1);
		Drawable bgToolbarCenter = mContext.getResources().getDrawable(
				R.drawable.playback_fullscreen_toolbar_center);
		Rect r2 = new Rect();
		bgToolbarCenter.getPadding(r2);
		Drawable bgToolbarRight = mContext.getResources().getDrawable(
				R.drawable.playback_fullscreen_toolbar_right);
		Rect r3 = new Rect();
		bgToolbarRight.getPadding(r3);
		LinearLayout.LayoutParams l = (LinearLayout.LayoutParams) mControlBarContent
				.getLayoutParams();
		Drawable btnDrawable = mContext.getResources().getDrawable(
				R.drawable.playback_fullscreen_pause);
		return getTimeBarWidth()
				+ (l.leftMargin + l.rightMargin + r1.left + r1.right + r2.left
						+ r2.right + r3.left + r3.right + btnDrawable
						.getIntrinsicWidth() * mShowButtonCount);
	}

	public int getTimeBarWidth() {
		return ActivityUtility.dip2px(mContext, 250);
	}
	
	

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		
//		mTimeBarLayoutWidth = 200;
//		mTimeBarLayoutHeight = getResources().getDimensionPixelSize(R.dimen.toolbar_height);
//		mTimeBar.measure(
//				MeasureSpec.makeMeasureSpec(mTimeBarLayoutWidth, MeasureSpec.EXACTLY), 
//				MeasureSpec.makeMeasureSpec(mTimeBarLayoutHeight, MeasureSpec.EXACTLY));
	}

	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		super.onLayout(changed, l, t, r, b);
		if (mLandScapeWidth <= 0) {
			mLandScapeWidth = getMeasuredWidth();
		}
	}

	private boolean mIsDragging;

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
			mClickImageButton = (ImageButton) isPressAction(e);
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

			// if (mIsCanMove) {
			// mClickMode = true;
			// mIsCancleLongTouch = false;
			// mTouchCount = (1 + mTouchCount);
			// mIsCanMove = false;
			// mLastX = ((int) e.getRawX());
			// mLastY = ((int) e.getRawY());
			// mClickImageButton = isPressAction(e);
			// if (mClickImageButton != null) {
			// setActionButtonStatus(mClickImageButton.getId(),
			// action);
			// }
			// } else {
			// return true;
			// }

			// canclePressedStatus();
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
			
			scheduleLandbarAutoDismiss();

			requestLayout();

			break;
		case MotionEvent.ACTION_UP:
			mIsCanMove = false;
			mIsCancleLongTouch = true;

			if (!mClickMode) {
				expandedControl(false);
			}

			// canclePressedStatus();
			cancleAction();

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

		// if (!mClickMode) {
		// expandedControl(false);
		// }
		// for (;;)
		// {
		// canclePressedStatus();
		// break;
		// if (mClickImageButton != null)
		// {
		// clickAction(mClickImageButton);
		// playSoundEffect(0);
		// }
		// }
	}

	private boolean isLandscapeShow;
	private Timer mTimer = new Timer();
	private static final int AUTO_DISMISS_TIME = 12;
	
	private TimerTask mLandscapeBarAutoDismissTask;

	public void showLandscapeToolbar() {
		isLandscapeShow = true;
		mControlBarContent.setVisibility(View.VISIBLE);

		scheduleLandbarAutoDismiss();
	}
	
	private void scheduleLandbarAutoDismiss() {
		if (mLandscapeBarAutoDismissTask != null) {
			mLandscapeBarAutoDismissTask.cancel();
		}

		mLandscapeBarAutoDismissTask = new TimerTask() {
			@Override
			public void run() {
				PlaybackLandscapeToolbar.this.post(new Runnable() {
					@Override
					public void run() {
						mControlBarContent.setVisibility(View.GONE);
					}
				});
			}
		};

		mTimer.schedule(mLandscapeBarAutoDismissTask, AUTO_DISMISS_TIME * 1000);
	}
	
	public void hideLandscapeToolbar() {
		isLandscapeShow = false;
		if (mLandscapeBarAutoDismissTask != null) {
			mLandscapeBarAutoDismissTask.cancel();
		}
		mControlBarContent.setVisibility(View.GONE);
	}
	
	public void controlLandscapeToolbarShowOrHide() {
		if (GlobalApplication.getInstance().isIsFullMode()) {
			if (isLandscapeShow) {
				hideLandscapeToolbar();
			} else {
				showLandscapeToolbar();
			}
		}
	}

	public void expandedControl(boolean paramBoolean) {

	}

	public void setOnLandControlbarListener(
			LandscapeToolbar.LandControlbarClickListener playbackLandControlbarClickListener) {
		this.mLandControlbarListener = playbackLandControlbarClickListener;
	}
	
	@Override
	protected void finalize() throws Throwable {
		mTimer.cancel();
		super.finalize();
	}

	public ImageButton getPausePlayButton() {
		return mPausePlayButton;
	}

	public void setmPausePlayButton(ImageButton mPausePlayButton) {
		this.mPausePlayButton = mPausePlayButton;
	}

	public ImageButton getRecordButton() {
		return mRecordButton;
	}

	public void setmRecordButton(ImageButton mRecordButton) {
		this.mRecordButton = mRecordButton;
	}

	public ImageButton getSoundButton() {
		return mSoundButton;
	}

	public void setmSoundButton(ImageButton mSoundButton) {
		this.mSoundButton = mSoundButton;
	}
}
