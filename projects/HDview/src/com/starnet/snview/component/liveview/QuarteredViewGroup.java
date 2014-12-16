package com.starnet.snview.component.liveview;


import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Rect;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.Scroller;

@SuppressLint("NewApi")
public abstract class QuarteredViewGroup extends ViewGroup {
	private static final String TAG = "QuarteredViewGroup";
	private static final boolean debug = false;
	
	private static final int AVAILABLE_VIEW_COUNT 			= 16;
	private static final int SLIDING_VELOCITY 				= 300 ;
	private static final int PERSON_OF_SCREEN_WIDTH_TO_SNAP = 30;  // range from [1,100]
	private static final int PFLAGS_LAYOUT_REQUIRED 		= 0x00000001;
	private static final int PFLAGS_LAYOUT_NEED_ADJUST 		= 0x00000002;
	private static final int PFLAGS_LAYOUT_NEED_UPDATE_ALL 	= 0x00000004;
	private static final int PFLAGS_LAYOUT_NEED_RELAYOUT 	= 0x00000008;
	
	protected static final int TOUCH_STATE_RESET 			= 0;
	protected static final int TOUCH_STATE_SCROLLING 		= 1;
	
	private Context context;
	
	/*
	 * Same as sliding window protocol
	 */
	private int windowSize;  
	private int windowLeftIndex;
	private int windowRightIndex;
	
	private int capacity;
	private MODE mode;
	private int screenIndex;
	private int screenLimit;
	private int screenCapaticy;
	private List<Rect> screenRects;
	private List<View> reusedViews;
	
	private Scroller scroller;
	private VelocityTracker velocityTracker;
	private int touchSlop = 0 ;
	private int touchState = TOUCH_STATE_RESET;
	private float lastDownX = 0 ;
	private float lastDownY = 0 ;
	
	private int privateFlags;
	
	private int oldScreenIndex;
	private boolean isModeChanged;
	private int oldWidthMeasureSpec;
	private int oldHeightMeasureSpec;
	
	private OnScreenListener onScreenListener;
	private onSingleTapListener onSingleTapListener;
	private OnDoubleClickListener onDoubleClickListener;
	private GestureDetector simpleGestureDetector; // process single-click, double-click and long-pressed event
	
	
	public QuarteredViewGroup(Context context) {
		super(context);
		this.context = context;
		this.capacity = 4;
		this.mode = MODE.MULTIPLE;
	}
	
	public QuarteredViewGroup(Context context, int capacity) {
		this(context);
		this.capacity = capacity;
		this.mode = MODE.MULTIPLE;
		this.screenIndex = 0;
		init();
	}
	
	public QuarteredViewGroup(Context context, int capacity, MODE mode) {
		this(context);
		this.capacity = capacity;
		this.mode = mode;
		this.screenIndex = 0;
		init();
	}
	
	public QuarteredViewGroup(Context context, int capacity, MODE mode, int screenIndex) {
		this(context);
		this.capacity = capacity;
		this.mode = mode;
		this.screenIndex = screenIndex;
		init();
	}
	
	public void setOnScreenListener(OnScreenListener onScreenListener) {
		this.onScreenListener = onScreenListener;
	}

	public void setOnSingleTapListener(onSingleTapListener onSingleTapListener) {
		this.onSingleTapListener = onSingleTapListener;
	}

	public void setOnDoubleClickListener(OnDoubleClickListener onDoubleClickListener) {
		this.onDoubleClickListener = onDoubleClickListener;
	}
	
	public MODE getScreenMode() {
		return mode;
	}
	
	public int getScreenIndex() {
		return screenIndex;
	}
	
	public int getScreenLimit() {
		return screenLimit;
	}
	
	public int getCapacity() {
		return capacity;
	}
	
	protected List<View> getAllSubViews() {
		return reusedViews;
	}
	
	/**
	 * Get sub view by item index. For safety, it is recommended to use
	 * the index return from {@link #getCurrentScreenItemStartIndex()}
	 * and {@link #getCurrentScreenItemEndIndex()}.
	 * @param itemIndex The item index
	 * @return The view associated with the item index
	 */
	protected View getSubViewByItemIndex(int itemIndex) {
		if (mode == MODE.SINGLE) {
			return reusedViews.get(itemIndex - windowLeftIndex);
		} else {
			int currScreen = itemIndex / 4;
			return reusedViews.get((currScreen - windowLeftIndex)*4 
					+ (itemIndex%4)); 
		}
	}
	
	/**
	 * Get the start index of items in current screen
	 * @return start index of items, [0,n]
	 */
	protected int getCurrentScreenItemStartIndex() {
		return mode == MODE.SINGLE ? screenIndex : screenIndex * 4;
	}
	
	/**
	 * Get the end index of items in current screen
	 * @return end index of items, [0,n]
	 */
	protected int getCurrentScreenItemEndIndex() {
		return mode == MODE.SINGLE ? screenIndex : screenIndex * 4 + (4-1);
	}
	
	private void init() {
		scroller = new Scroller(context);
		touchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
		
		oldWidthMeasureSpec = -1;
		oldHeightMeasureSpec = -1;
		screenRects = new ArrayList<Rect>();
		
		reusedViews = new ArrayList<View>();
		for (int i = 0; i < AVAILABLE_VIEW_COUNT; i++) {
			View v = (View) generateSubView(context);
			reusedViews.add(v);
		}
		for (View lv : reusedViews) {
			addView(lv);
		}
		
		simpleGestureDetector = new GestureDetector(context, 
				new SimpleGestureProcessor());
		privateFlags = 0x00000000;
		
		firstMeasureAndLayout();
	}

	private void firstMeasureAndLayout() {
		/*
		 * mode and capacity should be prepared before this
		 */
		//screenIndex = 0;
		isModeChanged = true;
		prepareScreenParams();
		calcWindowLeftRight(screenIndex);
		
		privateFlags |= PFLAGS_LAYOUT_REQUIRED;
		privateFlags |= PFLAGS_LAYOUT_NEED_UPDATE_ALL;
		
		requestLayout();
	}	
	
	/**
	 * If current mode is SINGLE, then switch to MULTIPLE. Otherwise, SINGLE.
	 * @param itemIndex current item index
	 */
	protected void switchMode(int itemIndex) {
		if (mode == MODE.SINGLE) {
			performRelayout(MODE.MULTIPLE, capacity, itemIndex);
		} else {
			performRelayout(MODE.SINGLE, capacity, itemIndex);
		}
	}
	
	/**
	 * Slide to previous screen.
	 */
	protected synchronized void previousScreen() {	
		if ((privateFlags & PFLAGS_LAYOUT_NEED_ADJUST)
				!= PFLAGS_LAYOUT_NEED_ADJUST) {  // layout adjustment has finished
			int oldScreen = screenIndex;
			screenIndex--;
			checkAndAdjustScreenIndex();
			if (screenIndex != oldScreen) {
				scroller.startScroll(oldScreen*getWidth(), 0, -getWidth(), 0, 250);

			}
			if (!adjustSlidingWindow()) {
				requestLayout();
			}
			invalidate();
		}	
	}
	
	/**
	 * Slide to next screen.
	 */
	protected synchronized void nextScreen() {		
		if ((privateFlags & PFLAGS_LAYOUT_NEED_ADJUST) 
				!= PFLAGS_LAYOUT_NEED_ADJUST) {  // layout adjustment has finished
			int oldScreen = screenIndex;
			screenIndex++;
			checkAndAdjustScreenIndex();
			if (screenIndex != oldScreen) {
				scroller.startScroll(oldScreen*getWidth(), 0, getWidth(), 0, 250);
			}
			if (!adjustSlidingWindow()) {
				requestLayout();
			}
			invalidate();
		}	
	}
	
	/**
	 * Providing interface for regenerating new layout
	 * @param m new {@link MODE}
	 * @param c new capacity of view items
	 * @param initialItemIndex initial item index
	 */
	protected void regenerateLayout(MODE m, int c, int initialItemIndex) {
		privateFlags |= PFLAGS_LAYOUT_NEED_RELAYOUT;
		performRelayout(m, c, initialItemIndex);
	}
	
	/**
	 * Regenerate new layout
	 * @param m new {@link MODE}}
	 * @param c new capacity of view items
	 * @param initialItemIndex initial item index
	 */
	private void performRelayout(MODE m, int c, int initialItemIndex) {
		isModeChanged = mode != m ? true : false;
		
		mode = m;
		capacity = c;
		screenIndex = mode == MODE.SINGLE ? initialItemIndex 
				:  initialItemIndex/4;
		prepareScreenParams();
		calcWindowLeftRight(screenIndex);
		
		invalidateParameters();
		
		privateFlags |= PFLAGS_LAYOUT_REQUIRED;
		privateFlags |= PFLAGS_LAYOUT_NEED_UPDATE_ALL;
		
		requestLayout();
	}
	
	private void invalidateParameters() {
		if (screenIndex > screenLimit-1 || screenIndex < 0
				|| windowLeftIndex < 0 || windowRightIndex > screenLimit-1) {
			throw new IllegalStateException("Screen Limit:" + screenLimit 
					+ ", Screen Index:" + screenIndex + ", Window Left Index:" 
					+ windowLeftIndex + ", Window Right Index:" + windowRightIndex);
		}
	}
	
	private void prepareScreenParams() {
		if (mode == MODE.SINGLE) {
			windowSize = AVAILABLE_VIEW_COUNT;
			screenLimit = screenCapaticy = capacity;
		} else {
			windowSize = AVAILABLE_VIEW_COUNT / 4;
			screenCapaticy = capacity;
			screenLimit = (capacity%4 == 0) ? capacity/4 : (capacity/4 + 1);
		}	
		
		if (debug) {
			Log.d(TAG, "Prepare screen parameters [windowSize:" + windowSize
					+ ", screenCapacity:" + screenCapaticy + ", screenLimit:"
					+ screenLimit + "]");
		}
	}
	
	/**
	 * Calculate "Sliding Window" left and right index
	 * @param startScreenIndex screen index that shows now
	 */
	private void calcWindowLeftRight(int startScreenIndex) {
		if (screenLimit <= windowSize) {
			windowLeftIndex = 0;
			windowRightIndex = screenLimit - 1;
		} else {
			if (startScreenIndex == 0) {
				windowLeftIndex = 0;
				windowRightIndex = windowLeftIndex + (windowSize - 1);
			} else if (startScreenIndex == (screenLimit - 1)) {
				windowRightIndex = screenLimit - 1;
				windowLeftIndex = windowRightIndex - (windowSize - 1);
			} else {
				int find = 0;
				while (find < screenLimit) {
					if (find < startScreenIndex
							&& (find + (windowSize - 1) > startScreenIndex)) {
						break;
					}
					find++;
				}
				windowLeftIndex = find;
				windowRightIndex = windowLeftIndex + (windowSize - 1);
			}
		}
		
		if (debug) {
			Log.d(TAG, "Calculate \"window\" left and right index [wLeft:"
					+ windowLeftIndex + ", wRight:" + windowRightIndex + "]");
		}
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int width = MeasureSpec.getSize(widthMeasureSpec);
		int height = MeasureSpec.getSize(heightMeasureSpec);
		
		if (debug) {
			Log.i(TAG, "onMeasure, width:" + width + " height:" + height);
		}

		if (oldWidthMeasureSpec != widthMeasureSpec 
				|| oldHeightMeasureSpec != heightMeasureSpec
				|| (privateFlags & PFLAGS_LAYOUT_REQUIRED) == PFLAGS_LAYOUT_REQUIRED) {
			generatePageRects(width, height);
			
			int size = getChildCount();
			for (int i = 0; i < size; i++) {
				View v = getChildAt(i);
				if (mode == MODE.SINGLE) {
					v.measure(MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY)
							, MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY));
				} else {
					v.measure(MeasureSpec.makeMeasureSpec(width/2, MeasureSpec.EXACTLY), 
							MeasureSpec.makeMeasureSpec(height/2, MeasureSpec.EXACTLY));
				}
			}
			
			oldWidthMeasureSpec = widthMeasureSpec;
			oldHeightMeasureSpec = heightMeasureSpec;
			
			// Need to perform a layout
			privateFlags |= PFLAGS_LAYOUT_REQUIRED;
			privateFlags |= PFLAGS_LAYOUT_NEED_UPDATE_ALL;
		}
		
		setMeasuredDimension(width, height);
	}
	
	private void generatePageRects(int width, int height) {
		screenRects.clear();
		
		int left = 0;
		for (int i = 0; i < capacity; i++) {
			Rect r = new Rect();
			r.set(left, 1, left + width - 1, height - 1);
			screenRects.add(r);
			left += width;
		}
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		if (debug) {
			Log.i(TAG, "onLayout, measuredWidth:" + getMeasuredWidth()
					+ " measuredHeight:" + getMeasuredHeight());
		}
		
		if ((privateFlags & PFLAGS_LAYOUT_REQUIRED) == PFLAGS_LAYOUT_REQUIRED) {
			privateFlags &= ~PFLAGS_LAYOUT_REQUIRED; // Clear PFLAGS_LAYOUT_REQUIRED flag
			/*
			 * windowLeftIndex and windowRightIndex should be prepared 
			 * before start layout
			 */
			if (mode == MODE.SINGLE) {
				if ((privateFlags & PFLAGS_LAYOUT_NEED_UPDATE_ALL) == PFLAGS_LAYOUT_NEED_UPDATE_ALL) {
					scrollTo(getWidth()*screenIndex, 0); // Scroll to specific index
					performViewsLayoutSingle();
					privateFlags &= ~PFLAGS_LAYOUT_NEED_UPDATE_ALL;  // Clear flag
					
					adjustSlidingWindow();  // adjust sliding window
				} else if ((privateFlags & PFLAGS_LAYOUT_NEED_ADJUST) == PFLAGS_LAYOUT_NEED_ADJUST) {
					adjustViewsLayoutSingle();					
					privateFlags &= ~PFLAGS_LAYOUT_NEED_ADJUST; 
				}
					
			} else {
				if ((privateFlags & PFLAGS_LAYOUT_NEED_UPDATE_ALL) == PFLAGS_LAYOUT_NEED_UPDATE_ALL) {
					scrollTo(getWidth()*screenIndex, 0);
					performViewsLayoutMultiple();
					privateFlags &= ~PFLAGS_LAYOUT_NEED_UPDATE_ALL;  // Clear flag
					
					adjustSlidingWindow();  // adjust sliding window
				} else if ((privateFlags & PFLAGS_LAYOUT_NEED_ADJUST) == PFLAGS_LAYOUT_NEED_ADJUST) {
					adjustViewsLayoutMultiple();					
					privateFlags &= ~PFLAGS_LAYOUT_NEED_ADJUST; 
				}
			}
			
			if (debug) {
				Log.d(TAG, "windowLeftIndex:" + windowLeftIndex + ", windowRightIndex:" + windowRightIndex);
			}
		}
		
		boolean isRelayoutRequest = (privateFlags & 
				PFLAGS_LAYOUT_NEED_RELAYOUT) == PFLAGS_LAYOUT_NEED_RELAYOUT;
		if (isModeChanged || screenIndex != oldScreenIndex
				|| isRelayoutRequest) {
			onScreenLayoutCompleted();
			if (!isRelayoutRequest) {
				detectModeChanged();
				detectPageChanged();
			}
			privateFlags &= ~PFLAGS_LAYOUT_NEED_RELAYOUT; 
		}
	}
	
	/**
	 * Called when it needs to layout all sub views
	 */
	private void performViewsLayoutSingle() {
		// Other views will be moved to invisible area
		int startIndex = windowRightIndex - windowLeftIndex + 1;
		int endIndex = reusedViews.size() - 1;
		for (int i = startIndex; 
				i <= endIndex; i++) {
			//reusedViews.get(i).setVisibility(View.GONE);
			reusedViews.get(i).layout(-2000, -2000, -2000, -2000);  // Move it to an invisible area
		}
		
		// Sub views apply new positions, pageRects[windowLeftIndex, windowRightIndex]
		int size = windowRightIndex - windowLeftIndex + 1;
		for (int i = 0; i < size; i++) {
			Rect pageRect = screenRects.get(windowLeftIndex + i);
			reusedViews.get(i).layout(pageRect.left, pageRect.top, pageRect.right, pageRect.bottom);
			onSubViewLayoutCompleted(reusedViews.get(i), true);
		}
	}
	
	/**
	 * Called when screenIndex is the bound of "sliding window". That is 
	 * to say screenIndex is equal to windowLeftIndex or windowRightIndex,
	 * but not first or last screen.
	 */
	private void adjustViewsLayoutSingle() {
		View viewToBeMoved;
		Rect newPos;
		if (screenIndex == windowLeftIndex) { // Move content of screen[windowRightIndex] 
											  // to screen[windowLeftIndex - 1]
			viewToBeMoved = reusedViews.get(windowSize - 1);
			// Adjust view position in reused view array
			reusedViews.remove(viewToBeMoved);
			reusedViews.add(0, viewToBeMoved);
			
			newPos = screenRects.get(windowLeftIndex - 1);
			
			// Adjust the position of  "sliding window"
			windowLeftIndex--;
			windowRightIndex--;
		} else { // Move content of screen[windowLeftIndex] to screen[windowRightIndex + 1]
			viewToBeMoved = reusedViews.get(0);
			reusedViews.remove(viewToBeMoved);
			reusedViews.add(viewToBeMoved);
			
			newPos = screenRects.get(windowRightIndex + 1);
			
			// Adjust the position of  "sliding window"
			windowLeftIndex++;
			windowRightIndex++;
		}
		
		validateInterData();
		
		// Apply new position to view
		viewToBeMoved.layout(newPos.left, newPos.top, newPos.right, newPos.bottom);
		onSubViewLayoutCompleted(viewToBeMoved, true);
	}
	
	private void performViewsLayoutMultiple() {
		// Other views will be moved to invisible area
		int startIndex = (windowRightIndex - windowLeftIndex + 1) * 4;
		int endIndex = reusedViews.size() - 1;
		for (int i = startIndex; 
				i <= endIndex; i++) {
			//reusedViews.get(i).setVisibility(View.GONE);
			reusedViews.get(i).layout(-2000, -2000, -2000, -2000);  // Move it to an invisible area
		}
		
		// Sub views apply new positions, pageRects[windowLeftIndex, windowRightIndex],
		// it means that we are making layout to screen[windowLeftIndex, windowRightIndex]
		int size = (windowRightIndex - windowLeftIndex + 1) * 4;
		for (int i = 0; i < size; i += 4) {
			Rect pageRect = screenRects.get(windowLeftIndex + i/4);     
			int itemsOneScreen = getAvailableItemCountInCurrentScreen(windowLeftIndex + i/4);
			
			List<View> viewsInCurrentScreen = new ArrayList<View>();
			viewsInCurrentScreen.add(reusedViews.get(i));
			viewsInCurrentScreen.add(reusedViews.get(i+1));
			viewsInCurrentScreen.add(reusedViews.get(i+2));
			viewsInCurrentScreen.add(reusedViews.get(i+3));
			
			layoutOneScreenMultiple(viewsInCurrentScreen, pageRect, itemsOneScreen);
		}		
	}
	
	/**
	 * Refer to {@link #adjustViewsLayoutSingle()}
	 */
	private void adjustViewsLayoutMultiple() {
		List<View> viewsToBeMoved = new ArrayList<View>();
		int itemsOneScreen;
		Rect newPos;
		if (screenIndex == windowLeftIndex) { // Move content of screen[windowRightIndex] 
											  // to screen[windowLeftIndex - 1]
			// Move last 4 views to head of reused views array
			int startIndex = (windowSize - 1) * 4;
			for (int i = startIndex; 
					i < AVAILABLE_VIEW_COUNT; i++) {
				viewsToBeMoved.add(reusedViews.get(i));
			}
			for (int i = AVAILABLE_VIEW_COUNT - 1;
					i >= startIndex; i--) {
				reusedViews.remove(i);
			}
			for (int i = 4 - 1; i >= 0; i--) {
				reusedViews.add(0, viewsToBeMoved.get(i));
			}
			
			itemsOneScreen = 4;  // screen[windowLeftIndex - 1]'s child 
								 // count will always be 4
			newPos = screenRects.get(windowLeftIndex - 1);
			
			// Adjust the position of  "sliding window"
			windowLeftIndex--;
			windowRightIndex--;
		} else { // Move content of screen[windowLeftIndex] to screen[windowRightIndex + 1]
			// Move first 4 views to last of reused views array
			for (int i = 0; i < 4; i++) {
				viewsToBeMoved.add(reusedViews.get(i));
			}
			for (int i = 0; i < 4; i++) {
				reusedViews.remove(0);
			}
			for (int i = 0; i < 4; i++) {
				reusedViews.add(viewsToBeMoved.get(i));
			}
			
			itemsOneScreen = getAvailableItemCountInCurrentScreen(windowRightIndex + 1);
			newPos = screenRects.get(windowRightIndex + 1);
			
			// Adjust the position of  "sliding window"
			windowLeftIndex++;
			windowRightIndex++;
		}
		
		validateInterData();
		
		layoutOneScreenMultiple(viewsToBeMoved, newPos, itemsOneScreen);
	}
	
	private int getAvailableItemCountInCurrentScreen(int currentScreenIndex) {
		if (currentScreenIndex < screenLimit - 1) {
			return 4;
		} else {
			return (capacity % 4 == 0) ? 4 : capacity % 4;
		}
	}
	
	private void validateInterData() {
		if (debug) {
			Assert.assertEquals(AVAILABLE_VIEW_COUNT, reusedViews.size());
			Assert.assertEquals(windowSize, windowRightIndex-windowLeftIndex+1);
			Assert.assertEquals(true, screenIndex > windowLeftIndex && screenIndex < windowRightIndex);
		}
	}
	
	/**
	 * Make the views layout like '锟斤拷' in specific rectangle region
	 * @param viewsInCurrentScreen Views to be layout, child count of it
	 * 							should be 4   
	 * @param posRect The destination rectangle that contains views
	 * @param itemsOneScreen view count in current screen
	 */
	private void layoutOneScreenMultiple(List<View> viewsInCurrentScreen
			, Rect posRect, int itemsOneScreen) {
		if (viewsInCurrentScreen .size() != 4) {
			throw new IllegalStateException("Child count of views in one "
					+ "screen must be 4.");
		}
		
		int vHalfWidth = (posRect.right - posRect.left + 1) / 2;  // Half of video Width
		int vHalfHeight = (posRect.bottom - posRect.top + 1) / 2; // Half of video Height
		
		// No matter whether there has content inside every view, the
		// view layout should be done 
		viewsInCurrentScreen.get(0).layout(posRect.left, posRect.top,
				posRect.left + vHalfWidth - 1, posRect.top + vHalfHeight - 1);
		viewsInCurrentScreen.get(1).layout(posRect.left + vHalfWidth,
				posRect.top, posRect.right, posRect.top + vHalfHeight - 1);
		viewsInCurrentScreen.get(2).layout(posRect.left, posRect.top + vHalfHeight, 
				posRect.left + vHalfWidth - 1, posRect.bottom);
		viewsInCurrentScreen.get(3).layout(posRect.left + vHalfWidth,
				posRect.top + vHalfHeight, posRect.right, posRect.bottom);
		
		
		for (int i = 0; i < 4; i++) {
			onSubViewLayoutCompleted(viewsInCurrentScreen.get(i), 
					itemsOneScreen > i ? true : false);
		}
	}
	
	/**
	 * Called after view has been layout
	 * @param v View that was layout
	 * @param hasContent Whether the view has content 
	 */
	private void onSubViewLayoutCompleted(View v, boolean hasContent) {
		if (hasContent) {
			v.setVisibility(View.VISIBLE);	
		} else {
			//v.setVisibility(View.GONE);	
			v.layout(-2000, -2000, -2000, -2000);  // Move it to an invisible area
		}
	}
	
	private void detectModeChanged() {
		if (isModeChanged) {
			if (onScreenListener != null) {
				onScreenListener.onScreenModeChanged(mode);
			}
			
			oldScreenIndex = screenIndex;
			isModeChanged = false;
		}
	}
	
	private void detectPageChanged() {
		if (oldScreenIndex != screenIndex && !isModeChanged) {
			if (onScreenListener != null) {
				onScreenListener.onScreenChanged(oldScreenIndex, screenIndex);
			}
			oldScreenIndex = screenIndex;
		}
	}
	
	protected float getLastMotionX() {
		return lastDownX;
	}
	
	protected float getLastMotionY() {
		return lastDownY;
	}
	
	@Override
	public boolean onInterceptTouchEvent(MotionEvent e) {
		final int action = e.getAction();
		// Return when touch state is scrolling
		if ((action == MotionEvent.ACTION_MOVE)
				&& (touchState != TOUCH_STATE_RESET)) {
			return true;
		}

		final float x = e.getX();
		final float y = e.getY();
		switch (action) {
		case MotionEvent.ACTION_DOWN:
			if (debug) {
				Log.d(TAG, "onInterceptTouchEvent down");
			}
			
			lastDownX = x;
			lastDownY = y;
			touchState = scroller.isFinished() ? TOUCH_STATE_RESET
					: TOUCH_STATE_SCROLLING;
			break;
		}
		return touchState != TOUCH_STATE_RESET;
	}

	@Override
	public boolean onTouchEvent(MotionEvent e) {
		if (velocityTracker == null) {
			velocityTracker = VelocityTracker.obtain();
		}
		
		float x = e.getX();
		float y = e.getY();
		int action = e.getAction();
		switch(action){
		case MotionEvent.ACTION_DOWN:
			if (scroller != null && !scroller.isFinished()) {
				scroller.abortAnimation();
			}
			velocityTracker.addMovement(e);
			lastDownX = x ;
			lastDownY = y;
			touchState = scroller.isFinished() ? TOUCH_STATE_RESET
					: TOUCH_STATE_SCROLLING;
			break ;
		case MotionEvent.ACTION_MOVE:
			if ((screenIndex == 0 && x - lastDownX > 0) 
					|| (screenIndex == screenLimit-1 && x - lastDownX < 0)) {
				if (x - lastDownX > 0) {
					onScreenLeftOverflow();
				} else {
					onScreenRightOverflow();
				}
			} else {
				scrollBy((int)(lastDownX - x ), 0);
				if (Math.abs(lastDownX - x) > touchSlop
						|| Math.abs(lastDownY - y) > touchSlop) {
					touchState = TOUCH_STATE_SCROLLING;
				}
			}
			
			lastDownX = x ;
			lastDownY = y;
			break ;
		case MotionEvent.ACTION_UP:
			velocityTracker.computeCurrentVelocity(1000);
			int velocityX = (int) velocityTracker.getXVelocity() ;
			if (velocityX < -SLIDING_VELOCITY && screenIndex < (screenLimit - 1)) {
				// slide left rapidly
				snapToScreen(screenIndex + 1);
			} else if (velocityX > SLIDING_VELOCITY && screenIndex > 0) {
				// slide right rapidly
				snapToScreen(screenIndex - 1);
			} else {
				// slide slowly
				snapToDestination();
			}
			
			if (velocityTracker != null) {
				velocityTracker.recycle();
				velocityTracker = null;
			}
			
			touchState = TOUCH_STATE_RESET ;
		    break;
		case MotionEvent.ACTION_CANCEL:
			touchState = TOUCH_STATE_RESET ;
			break;
		}
		
		return simpleGestureDetector.onTouchEvent(e) || true;
	}
	
	
	
	/**
	 * Calculate destination screen index according to current scrolled X. 
	 * Called when ACTION_UP event occurs or view is't at the right position.
	 */
	private void snapToDestination(){
		int destScreen = caclulateDestinationScreen();
	    
	    if (debug) {
	    	 Log.d(TAG, "Destination screen index => " + destScreen);
	    }
		
		snapToScreen(destScreen);
	}
	
	private int caclulateDestinationScreen() {
		/*
		// If current scroll X plus half of screen width, and the result is 
		// divided by screen width, then we can get destination position.
		// For example, our screen width is 320dip, we now has scrolled to
		// 500dip. Current scroll X 500dip(320 + 180) exceeds half screen,
		// so new screen index 1(500/320) is our destination screen index.
		// NOTICE: Screen index ranges from 0 to n
		int destScreen = (getScrollX() + getWidth()/2) / getWidth() ;*/
		
		int destScreen;
		int oldScrollX = screenIndex * getWidth();
		int newScrollX = getScrollX();
		// when screen has scrolled PERSON_OF_SCREEN_WIDTH_TO_SNAP percent  
		// of screen width, it's time to snap to previous/next screen
		float percent = PERSON_OF_SCREEN_WIDTH_TO_SNAP;  
		int deltaXShouldScroll = (int) (1.0F*percent / 100 * getWidth());
			
		if (newScrollX > oldScrollX) { // sliding left
			destScreen = (getScrollX() + (getWidth()-deltaXShouldScroll)) / getWidth();
		} else {
			if (oldScrollX - newScrollX > deltaXShouldScroll) {
				destScreen = screenIndex - 1;
			} else {
				destScreen = screenIndex;
			}
		}
		
		return destScreen;
	}
	
	/**
	 * Snap to destination screen slowly, which is controlled by 
	 * {@link android.widget.Scroller Scroller}. That differ from 
	 * making a call to {@link #scrollTo()}(rapidly).
	 * @param destScreen destination screen index
	 */
	private void snapToScreen(int destScreen){
		if (debug) {
			Log.d(TAG, "Original screen index => " + screenIndex
					+ " , Scrolled X => " + getScrollX());
		}
		
	    screenIndex = destScreen ;
	    checkAndAdjustScreenIndex();
	    
	    int offsetXToScroll = screenIndex*getWidth() - getScrollX() ;

	    if (debug) {
			Log.d(TAG, "Curent screen index => " + screenIndex
					+ " ,Offset X to scroll => " + offsetXToScroll);
	    }
	    
		scroller.startScroll(getScrollX(), 0, offsetXToScroll, 0,
				Math.abs(offsetXToScroll) * 2);
		if (!adjustSlidingWindow()) {
			requestLayout();  // Trigger a layout pass without performing
							  // real layout or adjustment 
		}
	    invalidate();  // Invalidate to trigger scrolling animation
    }
	
	/**
	 * Check whether current screen index is out of range [0, screenLimit-1].
	 * If yes, then adjust it to the 0 or screenLimit-1.
	 */
	private void checkAndAdjustScreenIndex() {
		if(screenIndex > screenLimit - 1) {
	    	screenIndex = screenLimit - 1 ;
	    }
	    if (screenIndex < 0) {
	    	screenIndex = 0;
	    }
	}	

	/**
	 * Called by parent when a new draw pass comes
	 */
	@Override
	public void computeScroll() {
		// Calculate the new position, and return true when animation
		// is not yet finished
		if (scroller.computeScrollOffset()) {
			scrollTo(scroller.getCurrX(), scroller.getCurrY()); // Move view slowly at every called-time
			postInvalidate(); // Refresh again to make new-show position correct
		} else {  // Animation is finished
			// ...
			// do something, such as onScreenChange
			// ...
			
		}
	}
	
	/**
	 * Trigger the "sliding window" adjustment process
	 * @return Whether "Sliding window" needs to adjust
	 */
	private boolean adjustSlidingWindow() {
		boolean needToAdjust = needToAdjustSlidingWindow();
		if (needToAdjust) {
			privateFlags |= PFLAGS_LAYOUT_REQUIRED;
			privateFlags |= PFLAGS_LAYOUT_NEED_ADJUST;
			requestLayout();
		}
		
		return needToAdjust;
	}
	
	private boolean needToAdjustSlidingWindow() {
		if ((screenLimit > 0 && windowRightIndex - windowLeftIndex + 1 >= 4)
				&& ((screenIndex == windowLeftIndex && screenIndex != 0)
				|| (screenIndex == windowRightIndex && screenIndex != screenLimit-1 ))) {
			return true;
		}
		
		return false;
	}
	
	/**
	 * Called when current screen index is 0, and the gesture of 
	 * sliding to previous page is detected. Subclass can override
	 * this method to process the overflow of "left page boundary".
	 */
	protected void onScreenLeftOverflow() {
		// do nothing
	}
	
	/**
	 * Called when current screen index is screenLimit-1, and the 
	 * gesture of sliding to next page is detected. Subclass can 
	 * override this method to process the overflow of "right page 
	 * boundary".
	 */
	protected void onScreenRightOverflow() {
		// do nothing
	}
	
	/**
	 * Called when layout process or adjustment of sliding window 
	 * has finished. 
	 */
	protected void onScreenLayoutCompleted() {
		// do nothing
	}
	
	/**
	 * Generate the sub view you wants to show
	 * @param context The context the view is running in
	 * @return the non-null sub view instance
	 */
	protected abstract View generateSubView(Context context);

	public enum MODE {
		SINGLE,
		MULTIPLE
	}
	
	public interface OnScreenListener {
		/**
		 * Called when page changes after layout process is completed.
		 * @param oldScreenIndex Last screen index
		 * @param newScreenIndex Current screen index
		 */
		void onScreenChanged(int oldScreenIndex, int newScreenIndex);
		
		/**
		 * Called when mode changes after layout process is completed.
		 * @param mode The new mode now
		 */
		void onScreenModeChanged(MODE mode);
	}
	
	public interface onSingleTapListener {
		void onSingleTap(View v);
	}
	
	public interface OnDoubleClickListener {
		void onDoubleClick(View v);
	}
	
	private class SimpleGestureProcessor extends GestureDetector.SimpleOnGestureListener {

		@Override
		public void onLongPress(MotionEvent e) {
			performLongClick();
		}

		@Override
		public boolean onDoubleTap(MotionEvent e) {
			if (onDoubleClickListener != null) {
				onDoubleClickListener.onDoubleClick(QuarteredViewGroup.this);
			}
			return true;
		}

		@Override
		public boolean onSingleTapConfirmed(MotionEvent e) {
			performClick();
			return true;
		}

		@Override
		public boolean onDown(MotionEvent e) {
			if (onSingleTapListener != null) {
				onSingleTapListener.onSingleTap(QuarteredViewGroup.this);
			}
			return super.onDown(e);
		}		
	}
}
