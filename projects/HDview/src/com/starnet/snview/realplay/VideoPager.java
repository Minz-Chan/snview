package com.starnet.snview.realplay;

import android.content.Context;
import android.os.Debug;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;

public class VideoPager extends ViewPager {
	private static final String TAG = "VideoPager";
	
	private boolean left = false;  
    private boolean right = false;  
    private boolean isScrolling = false;  
    private int lastValue = Integer.MIN_VALUE;  
    private int lastPage = 0;
    private boolean isManualSetItem = false;
    private boolean isPTZMode = false;
    private VideoPagerChangedCallback mVideoPagerChangedCallback = null; 
	
	public VideoPager(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public VideoPager(Context context) {
		super(context);
		init(context);
	}
	
	private void init(Context context) {
		setOnPageChangeListener(listener);
	}	


	@Override
	public void setCurrentItem(int item) {
		isManualSetItem = true;
		super.setCurrentItem(item);
		isManualSetItem = false;
		lastPage = item;
	}
	
	

	@Override
	public void setCurrentItem(int item, boolean smoothScroll) {
		isManualSetItem = true;
		super.setCurrentItem(item, smoothScroll);
		isManualSetItem = false;
		lastPage = item;
	}

	public void setPTZMode(boolean isPTZMode) {
		this.isPTZMode = isPTZMode;
	}
	
	

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		if (isPTZMode) {
			return false;
		}
		
		return super.onInterceptTouchEvent(ev);
	}

	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		if (isPTZMode) {
			return false;
		}
		
		return super.onTouchEvent(ev);
	}


	private OnPageChangeListener listener = new OnPageChangeListener() {
		
		@Override
		public void onPageScrollStateChanged(int arg0) {
			Log.i(TAG, "onPageScrollStateChanged, arg0: " + arg0);
			if (arg0 == 1) {
				isScrolling = true;
			} else {
				isScrolling = false;
			}


			if (arg0 == 2) {

				// notify ....
				if (mVideoPagerChangedCallback != null) {
					mVideoPagerChangedCallback.changeView(left, right);
				}
				right = left = false;
			}

		}

		@Override
		public void onPageScrolled(int arg0, float arg1, int arg2) {
			Log.i(TAG, "onPageScrolled, arg0: " + arg0 + ", arg2: " + arg2);
			if (isScrolling) {
				if (lastValue > arg2) {
					// 递减，向右侧滑动
					right = true;
					left = false;
				} else if (lastValue < arg2) {
					// 递减，向右侧滑动
					right = false;
					left = true;
				} else if (lastValue == arg2) {
					right = left = false;
				}
			}
//			Log.i("meityitianViewPager",
//					"meityitianViewPager onPageScrolled  last :arg2  ,"
//							+ lastValue + ":" + arg2);
			lastValue = arg2;
		}

		@Override
		public void onPageSelected(int arg0) {
			Log.i(TAG, "onPageSelected, arg0: " + arg0);
			int action = 0; // -1, 左滑; 0, 不变; 1, 右滑
			
			
			action = lastPage == arg0 ? 0 : (arg0 - lastPage);
			
			Log.i(TAG, "onPageSelected, action:" + action);
			
			if (mVideoPagerChangedCallback != null && lastPage != Integer.MIN_VALUE
					&& !isManualSetItem && Math.abs(arg0 - lastPage) == 1) {
				mVideoPagerChangedCallback.getCurrentPageIndex(arg0, action);
			}
			
			isManualSetItem = false;
			
			lastPage = arg0;
		}
	};

	public boolean getMoveRight() {
		return right;
	}

	public boolean getMoveLeft() {
		return left;
	}


	public void setVideoPagerChangedCallback(VideoPagerChangedCallback callback) {
		mVideoPagerChangedCallback = callback;
	}
	
	public interface VideoPagerChangedCallback {
		void changeView(boolean left, boolean right);
		void getCurrentPageIndex(int index, int action);
	}
}
