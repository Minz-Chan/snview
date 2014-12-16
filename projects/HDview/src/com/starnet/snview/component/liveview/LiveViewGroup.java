package com.starnet.snview.component.liveview;

import java.util.ArrayList;
import java.util.List;

import com.starnet.snview.component.VideoPager;
import com.starnet.snview.component.liveview.LiveViewItemContainer.OnRefreshButtonClickListener;
import com.starnet.snview.global.Constants;
import com.starnet.snview.global.GlobalApplication;
import com.starnet.snview.protocol.Connection;
import com.starnet.snview.protocol.Connection.StatusListener;
import com.starnet.snview.realplay.LiveControl;
import com.starnet.snview.realplay.PTZControl;
import com.starnet.snview.realplay.PreviewDeviceItem;
import com.starnet.snview.realplay.RealplayActivity;

import junit.framework.Assert;
import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.Toast;

public class LiveViewGroup extends QuarteredViewGroup {
	private static final String TAG = "LiveViewGroup";
	private boolean debug = true;
	
	private Context context;
	
	/**
	 * Save the current selected item index
	 */
	private int currentSelectedItemIndex;
	
	/**
	 * Save the last selected item index
	 */
	private int lastSelectedItemIndex;
	
	/**
	 * Whether current operator mode is in PTZ Mode
	 */
	private boolean isPTZMode = false;
	
	/**
	 * Whether it is in moving action under PTZ mode
	 */
	private boolean isPTZMoving = false;
	
	/**
	 * Whether it is in fling action under PTZ mode
	 */
	private boolean isPTZFling = false;
	
	/**
	 * Whether it is now under scale detector
	 */
	private boolean isScaleAction;
	
	/**
	 * If this value is true, {@link #previewCurrentScreen()}} will be executed
	 * next time when {@link #onScreenLayoutCompleted()} is called
	 */
	private boolean requestPreview;
	
	private GestureDetector directionGestureDetector;
	private ScaleGestureDetector scaleGestureDetector;
	
	
	private VideoPager mPager;
	private PTZControl mPtzControl;
	private LiveControl mLiveControl;
	private LiveViewManager mLiveViewManager;
	private Handler mHandler;
	
	private List<PreviewDeviceItem> mDevices;
	
	private List<LiveViewItemContainer> mCurrentLiveviews;
	private List<LiveViewItemContainer> mToBeRemovedLiveviews;
	
	private OnScreenListener screenChangedListener = new OnScreenListener() {
		
		@Override
		public void onScreenModeChanged(MODE mode) {
			Log.d(TAG, "onPageModeChanged, current mode:" + mode);
			// The selected item do not need to reconnect
			if (mode == MODE.SINGLE) { // from MODE.MULTIPLE to MODE.SINGLE
				mToBeRemovedLiveviews.remove(getLiveviewsInCurrentScreen().get(0));
			} else { // from MODE.SINGLE to MODE.MULTIPLE 
				mToBeRemovedLiveviews.clear();
			}
			
			highlightLiveviewBorder();
			previewCurrentScreen();
		}
		
		
		@Override
		public void onScreenChanged(int oldScreenIndex, int newScreenIndex) {
			Log.d(TAG, "onPageChanged, oldScreenIndex:" + oldScreenIndex + ", screenIndex:" + newScreenIndex);	
			previewCurrentScreen();
			updateLandToolbarPagerIndicator();
			mHandler.sendEmptyMessage(Constants.SCREEN_CHANGE);
		}
	};
	
	/**
	 * It will always be called before singleClickListener
	 */
	private onSingleTapListener singleTapListener = new onSingleTapListener() {
		
		@Override
		public void onSingleTap(View v) {
			int clickedItemIndex = calcClickItemIndex(getLastMotionX(), getLastMotionY());
			lastSelectedItemIndex = currentSelectedItemIndex;
			if (clickedItemIndex < mDevices.size()) {
				currentSelectedItemIndex = clickedItemIndex;
			} else {
				currentSelectedItemIndex = lastSelectedItemIndex;
			}
			
			highlightLiveviewBorder();
			updatePageInfo();
		}
	};
	
	private void highlightLiveviewBorder() {
		((LiveViewItemContainer)getSubViewByItemIndex(
				lastSelectedItemIndex)).getWindowLayout().setWindowSelected(false);
		((LiveViewItemContainer)getSubViewByItemIndex(
				currentSelectedItemIndex)).getWindowLayout().setWindowSelected(true);
	}
	
	public void updatePageInfo() {
		mPager.setNum(currentSelectedItemIndex+1);
		mPager.setAmount(getCapacity());
	}
	
	private OnClickListener singleClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			// PTZ模式情况下控制横屏工具条的显示或隐藏 
			if (isPTZMode && isInPreviewing()) {
				showOrHideLandToolbar();
			} else {
				int clickedItemIndex  = calcClickItemIndex(getLastMotionX(), getLastMotionY());
				if (clickedItemIndex != currentSelectedItemIndex) { // 非有效区域单击
					showOrHideLandToolbar();
				} else { // 有效区域单击
					if (clickedItemIndex == lastSelectedItemIndex) { // 两次点击为同一区域
						showOrHideLandToolbar();
					} else { // 两次点击为非同一区域
						if (GlobalApplication.getInstance().isIsFullMode()) {
							mLiveControl.getLandscapeToolbar().showLandscapeToolbar();	
						}
					}
					updatePageInfo();
					syncUIElementsStatus(); // 同步录像按钮状态
				}
			}
			
			
			/*
			Log.d(TAG, "on single click");
			if (mLiveViewManager.getPager() == null) {
				return;
			}

			int pos;

			if (mLiveViewManager.getPageCapacity() == 1) {
				pos = 1;
			} else {
				//pos = getIndexOfLiveview(e.getX(), e.getY());
				pos = (currentSelectedItemIndex + 1)%4 == 0 ? 4 
						: (currentSelectedItemIndex + 1)%4;
			}

			if ((mPtzControl.isPTZModeOn() && isInPreviewing())) { // PTZ模式情况下或单通道模式单击处理PTZ工具条的显示和隐藏
				if (GlobalApplication.getInstance().isIsFullMode()) { // 若为横屏模式，则进行相应工具条显示/隐藏控制
					if (mLiveControl.getLandscapeToolbar().isLandToolbarShow()) {
						mLiveControl.getLandscapeToolbar()
								.hideLandscapeToolbar();
					} else {
						mLiveControl.getLandscapeToolbar()
								.showLandscapeToolbar();
					}
				}
				return;
			}

			int index = (mLiveViewManager.getCurrentPageNumber() - 1)
					* mLiveViewManager.getPageCapacity() + pos;

			//Log.i(TAG, "single_click, getX:" + e.getX() + ", getY:" + e.getY()
			//		+ ", pos:" + pos + ", index:" + index);

			if (index > mLiveViewManager.getLiveViewTotalCount()) {
				if (GlobalApplication.getInstance().isIsFullMode()) { // 若为横屏模式，则进行相应工具条显示/隐藏控制
					if (mLiveControl.getLandscapeToolbar().isLandToolbarShow()) {
						mLiveControl.getLandscapeToolbar()
								.hideLandscapeToolbar();
					} else {
						mLiveControl.getLandscapeToolbar()
								.showLandscapeToolbar();
					}
				}
				return; // 非有效通道，不作其他处理
			}

			int oldPos = mLiveViewManager.getCurrentSelectedLiveViewPosition();

			mLiveViewManager.setCurrenSelectedLiveViewtIndex(index); // 变更当前选择索引

			mLiveViewManager.selectLiveView(index);

			mPager.setNum(mLiveViewManager.getSelectedLiveViewIndex());
			mPager.setAmount(mLiveViewManager.getLiveViewTotalCount());

			syncUIElementsStatus();
			
			if (GlobalApplication.getInstance().isIsFullMode()) { // 若为横屏模式，则进行相应工具条显示/隐藏控制
				if (pos == oldPos) {
					if (mLiveControl.getLandscapeToolbar().isLandToolbarShow()) {
						mLiveControl.getLandscapeToolbar()
								.hideLandscapeToolbar();
					} else {
						mLiveControl.getLandscapeToolbar()
								.showLandscapeToolbar();
					}
				} else {
					mLiveControl.getLandscapeToolbar().showLandscapeToolbar();
				}
			}*/
		}
	};
	
	/**
	 * 显示或隐藏横屏工具条
	 */
	private void showOrHideLandToolbar() {
		if (GlobalApplication.getInstance().isIsFullMode()) {
			if (mLiveControl.getLandscapeToolbar().isLandToolbarShow()) {
				mLiveControl.getLandscapeToolbar()
						.hideLandscapeToolbar();
			} else {
				mLiveControl.getLandscapeToolbar()
						.showLandscapeToolbar();
			}
		}
	}
	
	/**
	 * 若为横屏模式，且分页器显示，则刷新分页器
	 */
	private void updateLandToolbarPagerIndicator() {
		if (GlobalApplication.getInstance().isIsFullMode()) {
			if (mLiveControl.getLandscapeToolbar().isLandToolbarShow()) {
				mLiveControl.getLandscapeToolbar().showLandscapeToolbar();
			}
		}
	}
	
	private OnDoubleClickListener doubleClickListener = new OnDoubleClickListener() {
		@Override
		public void onDoubleClick(View v) {
			Log.d(TAG, "on double click");
			switchMode(calcClickItemIndex(getLastMotionX(), getLastMotionY()));
			
			updatePageInfo();
			if (GlobalApplication.getInstance().isIsFullMode()
					&& mLiveControl.getLandscapeToolbar().isLandToolbarShow()) { // 更新工具栏页码
				mLiveControl.getLandscapeToolbar().showLandscapeToolbar();
			}
			/*
			if (mPtzControl.isPTZModeOn() && checkIsPTZDeviceConnected()) { // PTZ模式情况下双击无效
				return;
			}

			Log.i(TAG, "On Double click");

			if (mLiveViewManager.getPager() == null) {
				return;
			}

			int pos;

			if (mLiveViewManager.getPageCapacity() == 1) {
				pos = 1;
			} else {
				//pos = getIndexOfLiveview(e.getX(), e.getY());
				pos = (currentSelectedItemIndex + 1)%4 == 0 ? 4 
						: (currentSelectedItemIndex + 1)%4;
			}

			int index = (mLiveViewManager.getCurrentPageNumber() - 1)
					* mLiveViewManager.getPageCapacity() + pos;

			//Log.i(TAG, "double_click, getX:" + e.getX() + ", getY:" + e.getY()
			//		+ ", pos:" + pos + ", index:" + index);

			if (index > mLiveViewManager.getLiveViewTotalCount()) {
				return; // 非有效通道，不作处理
			}

			mLiveViewManager.setCurrenSelectedLiveViewtIndex(index); // 变更当前选择索引

			if (mLiveViewManager.isMultiMode()) { // 切换到单通道模式
				if (checkIsPTZDeviceConnected()) {
					mLiveViewManager.prestoreConnectionByPosition(pos);
					mLiveViewManager.setMultiMode(false);
					switchMode(currentSelectedItemIndex);
					mLiveViewManager.transferVideoWithoutDisconnect(pos);
				} else {
					mLiveViewManager.closeAllConnection(false);
					mLiveViewManager.setMultiMode(false);
					switchMode(currentSelectedItemIndex);
					mLiveViewManager.preview(index);
				}

				mPtzControl.setIsEnterPTZInSingleMode(true);
			} else { // 切换到多通道模式
				int currPageStart;
				int currPageEnd;

				if (checkIsPTZDeviceConnected()) { // 若当前通道为连接状态，则切换时保持当前连接
					mLiveViewManager.prestoreConnectionByPosition(pos);
					mLiveViewManager.setMultiMode(true);
					switchMode(currentSelectedItemIndex);

					currPageStart = (mLiveViewManager.getCurrentPageNumber() - 1) * 4 + 1;
					currPageEnd = (mLiveViewManager.getCurrentPageNumber() - 1)
							* 4 + mLiveViewManager.getCurrentPageCount();
					mLiveViewManager.preview(currPageStart, currPageEnd
							- currPageStart + 1, index);
				} else { // 若当前通道为非连接状态，则关闭所有连接
					mLiveViewManager.closeAllConnection(false);
					mLiveViewManager.setMultiMode(true);
					switchMode(currentSelectedItemIndex);

					currPageStart = (mLiveViewManager.getCurrentPageNumber() - 1) * 4 + 1;
					currPageEnd = (mLiveViewManager.getCurrentPageNumber() - 1)
							* 4 + mLiveViewManager.getCurrentPageCount();
					mLiveViewManager.preview(currPageStart, currPageEnd
							- currPageStart + 1);
				}

				// 若发现此时PTZ模式开启，则重围PTZ模式，即退出PTZ模式
				if (mPtzControl.isPTZModeOn()) {
					mPtzControl.showPTZBar(false);
				}

				mPtzControl.setIsEnterPTZInSingleMode(false);
			}

			mLiveViewManager.selectLiveView(index);

			mPager.setNum(mLiveViewManager.getSelectedLiveViewIndex());
			mPager.setAmount(mLiveViewManager.getLiveViewTotalCount());
			
			if (GlobalApplication.getInstance().isIsFullMode()
					&& mLiveControl.getLandscapeToolbar().isLandToolbarShow()) { // 更新工具栏页码
				mLiveControl.getLandscapeToolbar().showLandscapeToolbar();
			}*/
		}
	};
	
	private OnLongClickListener longClickListener = new OnLongClickListener() {
		@Override
		public boolean onLongClick(View v) {
			Log.d(TAG, "on long click");
			Toast.makeText(context, "Long Press", Toast.LENGTH_LONG).show();
			return true;
		}
	};
	
	/**
	 * The real processor for all gesture event
	 */
	private PTZGestureListener ptzGestureListener = new PTZGestureListener() {

		@Override
		public void onSlidingLeft() {
			Log.d(TAG, "onSlidingLeft");
		}

		@Override
		public void onSlidingLeftUp() {
			Log.d(TAG, "onSlidingLeftUp");
		}

		@Override
		public void onSlidingLeftDown() {
			Log.d(TAG, "onSlidingLeftDown");
		}

		@Override
		public void onSlidingRight() {
			Log.d(TAG, "onSlidingRight");
		}

		@Override
		public void onSlidingRightUp() {
			Log.d(TAG, "onSlidingRightUp");
		}

		@Override
		public void onSlidingRightDown() {
			Log.d(TAG, "onSlidingRightDown");
		}

		@Override
		public void onSlidingUp() {
			Log.d(TAG, "onSlidingUp");
		}

		@Override
		public void onSlidingDown() {
			Log.d(TAG, "onSlidingDown");
		}

		@Override
		public void onSlidingMoveUp() {
			Log.d(TAG, "onSlidingMoveUp");
			isPTZMoving = false;
		}

		@Override
		public void onFlingLeft() {
			Log.d(TAG, "onFlingLeft");
		}

		@Override
		public void onFlingLeftUp() {
			Log.d(TAG, "onFlingLeftUp");
		}

		@Override
		public void onFlingLeftDown() {
			Log.d(TAG, "onFlingLeftDown");
		}

		@Override
		public void onFlingRight() {
			Log.d(TAG, "onFlingRight");
		}

		@Override
		public void onFlingRightUp() {
			Log.d(TAG, "onFlingRightUp");
		}

		@Override
		public void onFlingRightDown() {
			Log.d(TAG, "onFlingRightDown");
		}

		@Override
		public void onFlingUp() {
			Log.d(TAG, "onFlingUp");
		}

		@Override
		public void onFlingDown() {
			Log.d(TAG, "onFlingDown");
		}

		@Override
		public void onZoomIn() {
			Log.d(TAG, "onZoomIn");
		}

		@Override
		public void onZoomOut() {
			Log.d(TAG, "onZoomOut");
		}
		
	};
	
	private LiveViewItemContainer.OnRefreshButtonClickListener mRefreshButtonClickListener = 
			new LiveViewItemContainer.OnRefreshButtonClickListener() {
		@Override
		public void onClick(View v) {
			Log.i(TAG, "OnRefreshClick, " + v);
			if (mDevices == null || mDevices.size() == 0) {
				return;
			}
			
			LiveViewItemContainer c = findVideoContainerByView(v);
			if (c != null) {
				c.preview();
			}
		}
	};
	
	private StatusListener mConnectionStatusListener = new StatusListener() {
		@Override
		public void OnConnectionTrying(View v) {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void OnConnectionFailed(View v) {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void OnConnectionEstablished(View v) {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void OnConnectionClosed(View v) {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void OnConnectionBusy(View v) {
			// TODO Auto-generated method stub
			
		}
	};

	public LiveViewGroup(Context context) {
		super(context);
		this.context = context;
		init();
	}
	
	public LiveViewGroup(Context context, int capacity) {
		super(context, capacity);
		this.context = context;
		init();
	}
	
	public LiveViewGroup(Context context, int capacity, MODE mode) {
		super(context, capacity, mode);
		this.context = context;
		init();
	}
	
	public LiveViewGroup(Context context, int capacity, MODE mode, int screenIndex) {
		super(context, capacity, mode, screenIndex);
		this.context = context;
		init();
	}
	
	private void log(String msg) {
		if (debug) {
			Log.d(TAG, msg);
		}
	}

	public void init() {
		mPager = getRP().getPager();
		mPtzControl = getRP().getPtzControl();
		mLiveControl = getRP().getLiveControl();
//		mLiveViewManager = getRP().getLiveviewManager();
		mHandler = getRP().getHandler();
		
		mCurrentLiveviews = new ArrayList<LiveViewItemContainer>();
		mToBeRemovedLiveviews = new ArrayList<LiveViewItemContainer>();
		
		directionGestureDetector = new GestureDetector(context, new DirectionGestureProcessor());
		scaleGestureDetector = new ScaleGestureDetector(context, new ScaleGestureProcessor());
		
		super.setOnScreenListener(screenChangedListener);
		this.setOnSingleTapListener(singleTapListener);
		this.setOnClickListener(singleClickListener);
		this.setOnDoubleClickListener(doubleClickListener);
		this.setOnLongClickListener(longClickListener);
		setOnRefreshButtonClickListener(mRefreshButtonClickListener);
	}
	
	private RealplayActivity getRP() {
		return (RealplayActivity) context;
	}
	
	public boolean checkIfPreviewDeviceListEmpty() {
		return getRP().checkIfPreviewDeviceListEmpty();
	}
	
	private void syncUIElementsStatus() {
		getRP().updateUIElementsStatus();
	}
	
	private LiveViewItemContainer findVideoContainerByView(View v) {
		View curr = v;
		while (curr != null) {
			if (curr instanceof LiveViewItemContainer) {
				break;
			}
			curr = (View) curr.getParent();
		}
		return (LiveViewItemContainer) curr;
	}
	
	/**
	 * 当前选择设备是否正在预览
	 * @return true, 是; false, 否
	 */
	public boolean isInPreviewing() {
		final Connection conn = ((LiveViewItemContainer) getSubViewByItemIndex(
				currentSelectedItemIndex)).getCurrentConnection();
		return conn != null && conn.isConnected();
	}
	
	/**
	 * Calculate current item index according to current screen index and
	 * pointer information.
	 * @param x x-coordinate of current touch point
	 * @param y y-coordinate of current touch point
	 * @return item index, ranging from [0, screenLimit-1]/[0, screenLimit*4-1].
	 * 		It may be out of range [0, n]
	 */
	private int calcClickItemIndex(float x, float y) {
		int pos = 0;		
		if (getScreenMode() == MODE.SINGLE) {
			pos = 0;
			return getScreenIndex() + pos;
		} else {
			int xPos = (int) (x / (getWidth()/2));
			int yPos = (int) (y / (getHeight()/2));
			
			if (xPos == 0 && yPos == 0) {
				pos = 0;
			} else if (xPos == 1 && yPos == 0) {
				pos = 1;
			} else if (xPos == 0 && yPos == 1) {
				pos = 2;
			} else if (xPos == 1 && yPos == 1) {
				pos = 3;
			}
			return getScreenIndex()*4 + pos;
		}
	}
	
	/**
	 * Get views in current screen.
	 * @return A list contains views showed on current screen. Its size may
	 * 		1, 2, 3 or 4.
	 */
	public List<LiveViewItemContainer> getLiveviewsInCurrentScreen() {
		List<LiveViewItemContainer> currentScreenLiveviews = 
				new ArrayList<LiveViewItemContainer>();
		int startIndex = getCurrentScreenItemStartIndex();
		int endIndex = getCurrentScreenItemEndIndex();
		for (int i = startIndex; i <= endIndex; i++) {
			currentScreenLiveviews.add((LiveViewItemContainer)
					getSubViewByItemIndex(i));
		}
		
		return currentScreenLiveviews;
	}	
	
	/**
	 * Get current selected LiveViewContainer.
	 * @return The LiveViewContainer selected
	 */
	public LiveViewItemContainer getSelectedLiveview() {
		return (LiveViewItemContainer) getSubViewByItemIndex(
				currentSelectedItemIndex);
	}
	
	@Override
	protected View generateSubView(Context context) {
		LiveViewItemContainer c = new LiveViewItemContainer(context);
		c.findSubViews();
		return c;
	}
	
	public void setOnRefreshButtonClickListener(
			OnRefreshButtonClickListener onRefreshButtonClickListener) {
		if (onRefreshButtonClickListener == null) {
			throw new NullPointerException(
					"OnRefreshButtonClickListener can't be null");
		}
		
		this.mRefreshButtonClickListener = onRefreshButtonClickListener;
		List<View> allSubViews = getAllSubViews();
		for (View v : allSubViews) {
			LiveViewItemContainer c = (LiveViewItemContainer) v;
			c.setRefreshButtonClickListener(mRefreshButtonClickListener);
		}
	}
	
	public void setConnectionStatusListener(
			StatusListener connectionStatusListener) {
		if (connectionStatusListener == null) {
			throw new NullPointerException(
					"ConnectionStatusListener can't be null");
		}
		
		this.mConnectionStatusListener = connectionStatusListener;
		List<View> allSubViews = getAllSubViews();
		for (View v : allSubViews) {
			LiveViewItemContainer c = (LiveViewItemContainer) v;
			c.setConnectionStatusListener(mConnectionStatusListener);
		}
	}

	public void setDevices(List<PreviewDeviceItem> devices) {
		if (devices == null || devices.size() == 0) {
			return;
		}
		
		this.mDevices = devices;
		mToBeRemovedLiveviews.clear();
		mCurrentLiveviews.clear();
		regenerateLayout(getScreenMode(), mDevices.size(), 0);
	}	
	
	public void setPTZMode(boolean isPTZMode) {
		this.isPTZMode = isPTZMode;
	}
	
	/**
	 * 计划一个预览请求，它将在下一次布局完成时触发
	 */
	public void schedulePreview() {
		requestPreview = true;
	}
	
	public void previewCurrentScreen() {
		for (LiveViewItemContainer c1 : mCurrentLiveviews) {
			if (!c1.isConnected()) {
				c1.preview();
			}
		}
		
		for (LiveViewItemContainer c2 : mToBeRemovedLiveviews) {
			if (c2.isConnected()) {
				c2.stopPreview(false);
				c2.resetView();
			}
		}
	}
	
	public void stopPreviewCurrentScreen() {
		List<LiveViewItemContainer> lvs = getLiveviewsInCurrentScreen();
		for (LiveViewItemContainer c : lvs) {
			if (c.isConnected()) {
				c.stopPreview(false);
				c.resetView();
			}
		}
	}
	
	public void switchMode() {
		switchMode(currentSelectedItemIndex);
	}

	public void previous() {
		super.previousScreen();
	}
	
	public void next() {
		super.nextScreen();
	}
	
	public void regenerateLayout(MODE m, int c, int initialItemIndex) {
		super.regenerateLayout(m, c, initialItemIndex);
	}
	
	public boolean isMultiScreenMode() {
		return getScreenMode() == MODE.MULTIPLE ? true : false;
	}

	@Override
	protected void onScreenLayoutCompleted() {		
		if (mCurrentLiveviews == null 
				|| mToBeRemovedLiveviews == null
				|| mDevices == null) {
			return;
		}
		
		// Manage the LiveViewItemContainer added and removed
		mToBeRemovedLiveviews.clear();
		for (LiveViewItemContainer c : mCurrentLiveviews) {
			mToBeRemovedLiveviews.add(c);
		}
		
		mCurrentLiveviews.clear();
		int startIndex = getCurrentScreenItemStartIndex();
		int endIndex = getCurrentScreenItemEndIndex();
		for (int i = startIndex; i <= endIndex; i++) {
			LiveViewItemContainer c = (LiveViewItemContainer)
					getSubViewByItemIndex(i);
			c.setItemIndex(i);
			c.setPreviewItem(mDevices.get(i)); // load connection info
			mCurrentLiveviews.add(c);
		}
		
		// Highlight the current selected view
		lastSelectedItemIndex = currentSelectedItemIndex;
		currentSelectedItemIndex = getCurrentScreenItemStartIndex();
		highlightLiveviewBorder();
		
		// Update page information
		updatePageInfo();
		
		// Request to preview devices
		if (requestPreview) {
			requestPreview = false;
			previewCurrentScreen();
		}
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent e) {
		return super.onInterceptTouchEvent(e);
	}

	@Override
	public boolean onTouchEvent(MotionEvent e) {
		if (isPTZMode) {
			/*
			 * Once multi-touch event is detected, MotionEvent following should
			 * be filtered in PTZGestureDetector. That is to say that the following
			 * MotionEvent should be taken charge of only by ScaleGestureDetector
			 */
			int action = e.getActionMasked();
			if (action == MotionEvent.ACTION_POINTER_DOWN) {
				isScaleAction = true;  
			}
			
			if (isScaleAction) {
				if (action == MotionEvent.ACTION_UP) {
					isScaleAction = false;
				}
				return scaleGestureDetector.onTouchEvent(e);
			} else {
				boolean r1 = scaleGestureDetector.onTouchEvent(e);
				boolean r2 = directionGestureDetector.onTouchEvent(e);
				
				if (isPTZMode && !isPTZFling && action == MotionEvent.ACTION_UP) {
					ptzGestureListener.onSlidingMoveUp();
				} 
				
				return r1 || r2;
			}
		} else {
			return super.onTouchEvent(e);  // single-click, double-click, long-pressed, sliding-left 
										   // (next screen) and sliding-right(previous screen) event
		}
	}
	
	
	private class DirectionGestureProcessor extends GestureDetector.SimpleOnGestureListener {
		private final int FLING_DISTANCE = 35;  
		private final int FLING_VELOCITY = 300;
		private final int SLIDING_DISTANCE = 35;
		private final int SLIDING_VELOCITY_MAX = 300;

		@Override
		public boolean onDown(MotionEvent e) {
			return true;
		}

		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
				float velocityY) {
//			Log.d(TAG, "velocityX:" + velocityX + ", velocityY:" + velocityY);
			if (!isVelocityMeetFlingActionCondition(
					velocityX, velocityY)) {
				return false;
			}
			
        	int[] flingDirections = calcActionDirection(e1, e2, FLING_DISTANCE);
        	isPTZFling = true;
        	dispatchFlingAction(flingDirections[0], flingDirections[1]);
        	postDelayed(new Runnable() {
				@Override
				public void run() {
					isPTZFling = false;
					ptzGestureListener.onSlidingMoveUp();
				}
			}, 300);
        	
        	return false;
		}
		
		private boolean isVelocityMeetFlingActionCondition(float velocityX, float velocityY) {
			if (Math.abs(velocityX) < FLING_VELOCITY 
					&& Math.abs(velocityY) < FLING_VELOCITY) {
				return false;
			} else {
				return true;
			}
		}
		
		/**
		 * Dispatch fling event to concrete listener.
		 * @param horizontalFlag value get from {@link #calcActionDirection(MotionEvent, MotionEvent)} 
		 * @param verticalFlag value get from {@link #calcActionDirection(MotionEvent, MotionEvent)} 
		 */
		private void dispatchFlingAction(int horizontalFlag, int verticalFlag) {
			if (debug) {
				Assert.assertEquals(true, ptzGestureListener != null);
			}
			
			int h = horizontalFlag;
			int v = verticalFlag;
			if (h == 0 && v == 0) {  // none
				// do nothing
			} else if (h == -1 && v == -1) { // left up
				ptzGestureListener.onFlingLeftUp();
			} else if (h == -1 && v == 0) {  // left
				ptzGestureListener.onFlingLeft();
			} else if (h == -1 && v == 1) {  // left down
				ptzGestureListener.onFlingLeftDown();
			} else if (h == 0 && v == -1) {  // up
				ptzGestureListener.onFlingUp();
			} else if (h == 0 && v == 1) {   // down
				ptzGestureListener.onFlingDown();
			} else if (h == 1 && v == -1) {  // right up
				ptzGestureListener.onFlingRightUp();
			} else if (h == 1 && v == 0) {   // right
				ptzGestureListener.onFlingRight();
			} else if (h == 1 && v == 1) {   // right down
				ptzGestureListener.onFlingRightDown();
			}
		}
		
		@Override
		public boolean onScroll(MotionEvent e1, MotionEvent e2,
				float distanceX, float distanceY) {
			//Log.d(TAG, "distanceX:" + distanceX + ", distanceY:" + distanceY);
			//isVelocityMeetScrollCondition(e1, e2);
			if (isPTZMoving 
					|| !isVelocityMeetScrollCondition(e1, e2)) {
				return true;
			}
			
			int[] slideDirections = calcActionDirection(e1, e2, SLIDING_DISTANCE);
			dispatchSlidingAction(slideDirections[0], slideDirections[1]);	
			isPTZMoving = (slideDirections[0] == 0 && slideDirections[1] == 0) ? false : true;
			
			return super.onScroll(e1, e2, distanceX, distanceY);
		}
		
		private boolean isVelocityMeetScrollCondition(MotionEvent e1, MotionEvent e2) {
			float timeInSeconds = (float) ((e2.getEventTime() - e1.getEventTime()) / 1000.0);
			int velocityX = (int) (Math.abs(e2.getX() - e1.getX()) / timeInSeconds);
			int velocityY = (int) (Math.abs(e2.getY() - e1.getY()) / timeInSeconds);
//			Log.d(TAG, "isVelocityMeetScrollCondition, velocityX:" + velocityX + ", velocityY:" + velocityY);
			if(velocityX < SLIDING_VELOCITY_MAX 
					&& velocityY < SLIDING_VELOCITY_MAX) {
				return true;
			} else {
				return false;
			}
		}

		/**
		 * Dispatch sliding event to concrete listener.
		 * @param horizontalFlag value get from {@link #calcActionDirection(MotionEvent, MotionEvent)} 
		 * @param verticalFlag value get from {@link #calcActionDirection(MotionEvent, MotionEvent)} 
		 */
		private void dispatchSlidingAction(int horizontalFlag, int verticalFlag) {
			if (debug) {
				Assert.assertEquals(true, ptzGestureListener != null);
			}
			
			int h = horizontalFlag;
			int v = verticalFlag;
			
			if (h == 0 && v == 0) {  // none
				// do nothing
			} else if (h == -1 && v == -1) { // left up
				ptzGestureListener.onSlidingLeftUp();
			} else if (h == -1 && v == 0) {  // left
				ptzGestureListener.onSlidingLeft();
			} else if (h == -1 && v == 1) {  // left down
				ptzGestureListener.onSlidingLeftDown();
			} else if (h == 0 && v == -1) {  // up
				ptzGestureListener.onSlidingUp();
			} else if (h == 0 && v == 1) {   // down
				ptzGestureListener.onSlidingDown();
			} else if (h == 1 && v == -1) {  // right up
				ptzGestureListener.onSlidingRightUp();
			} else if (h == 1 && v == 0) {   // right
				ptzGestureListener.onSlidingRight();
			} else if (h == 1 && v == 1) {   // right down
				ptzGestureListener.onSlidingRightDown();
			}
		}


		/**
		 * Calculate direction of action.
		 * @param e1 The first down motion event that started the fling.
		 * @param e2 The move motion event that triggered the current onFling.
		 * @param distance The valid action distance
		 * @return An array of two integers in which to hold the direction 
		 * 		  information of fling event.<br>
		 * 		  directions[0]:<br>
		 * 			horizontal direction, -1:left 0:non-slip 1:right<br>
		 * 		  directions[1]:<br>
		 * 			vertical direction, -1:up 0:non-slip 1:down<br>
		 *        If directions[0]=-1 and directions[1]=-1, it means the direction
		 *        of fling event is "left up".
		 */
		private int[] calcActionDirection(MotionEvent e1, MotionEvent e2, int distance) {
			float a0 = e1.getX();
        	float a1 = e2.getX();
        	float b0 = e1.getY();
        	float b1 = e2.getY();
        	
        	int h = Math.abs(a1 - a0) > distance ? (a1 - a0 > 0 ? 1 : -1) : 0;  // -1:��0��ˮƽ�޻�����1����
        	int v = Math.abs(b1 - b0) > distance ? (b1 - b0 > 0 ? 1 : -1) : 0;  // -1���ϣ�0����ֱ�޻�����1����
        	float k = (b1 - b0) / (a1 - a0);
        	
        	if (h != 0 || v != 0) {
    			if (k <= -2F || k >= 2F) {
    				if (b1 < b0) { // up
    					h = 0; v = -1;
    				} else { // down
    					h = 0; v = 1;
    				}
    			}
    			if (k >= -0.375F && k <= 0.375F) {
    				if (a1 < a0) { // left
    					h = -1; v = 0;
    				} else { // right
    					h = 1; v = 0;
    				}
    			}
    			if (k > -2F && k < -0.375F) {
    				if (b1 > b0) { // left down
    					h = -1; v = 1;
    				} else { // right up
    					h = 1; v = -1;
    				}
    			}
    			if (k > 0.375F && k < 2F) {
    				if (b1 < b0) { // left up
    					h = -1; v = -1;
    				} else { // right down
    					h = 1; v = 1;
    				}
    			}
    		} else {
    			h = 0; v = 0;  // none
    		}
        	
        	int[] directions = new int[2];
        	directions[0] = h;
        	directions[1] = v;
        	
        	return directions;
		}
	}
	
	private class ScaleGestureProcessor implements ScaleGestureDetector.OnScaleGestureListener {
		/**
		 * The calculated scale factor when a scaled gesture is completed 
		 */
		private float scaleFactor; 
		
		/**
		 * A long value to save last gesture event time
		 */
		private long lastGestureEventTime;
		
		/**
		 * The time(in millisecond) defines a minimum interval value between 
		 * consecutive gestures. It is used to eliminate "jitter" at the moment
		 * that one of the fingers leaves screen.
		 */
		private long INTERVAL_BETWEEN_TWO_GESTURES_MIN = 300;
		
		@Override
		public boolean onScale(ScaleGestureDetector detector) {
			scaleFactor *= detector.getScaleFactor();
			return true;
		}

		@Override
		public boolean onScaleBegin(ScaleGestureDetector detector) {
			scaleFactor = 1;
			return true;
		}

		@Override
		public void onScaleEnd(ScaleGestureDetector detector) {
			// ...
			// Condition whether to response scale gesture
			// ...
			if (!isPTZMode) {
				return;
			}
			
			long intervalBetweenTwoGesture = System.currentTimeMillis() - lastGestureEventTime;
			if (intervalBetweenTwoGesture < INTERVAL_BETWEEN_TWO_GESTURES_MIN) {
				return;
			}
			lastGestureEventTime = System.currentTimeMillis();
			
			if (debug) {
				Assert.assertEquals(true, ptzGestureListener != null);
				Log.d(TAG, "scaleFactor => " + scaleFactor);
				Log.d(TAG, "intervalBetweenTwoGesture: " + intervalBetweenTwoGesture);
			}
			
			if (scaleFactor > 1.1F) {
				ptzGestureListener.onZoomIn();
			} else if (scaleFactor < 0.9F) {
				ptzGestureListener.onZoomOut();
			}
		}
		
	}
	
	public interface PTZGestureListener {
		void onSlidingLeft();
		void onSlidingLeftUp();
		void onSlidingLeftDown();
		void onSlidingRight();
		void onSlidingRightUp();
		void onSlidingRightDown();
		void onSlidingUp();
		void onSlidingDown();
		void onSlidingMoveUp();
		void onFlingLeft();
		void onFlingLeftUp();
		void onFlingLeftDown();
		void onFlingRight();
		void onFlingRightUp();
		void onFlingRightDown();
		void onFlingUp();
		void onFlingDown();
		void onZoomIn();
		void onZoomOut();
	}
	
}