package com.starnet.snview.images.utils;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.TextView;

public class SelfDefViewPager extends ViewPager {

	private final String TAG = "SelfDefViewPager";
	boolean isScrolling = false;
	private TextView imagepreview_title_image_num;
	private int showSum;

	public SelfDefViewPager(Context context) {
		super(context);
		initial();
	}
	
	public SelfDefViewPager(Context context,TextView imagepreview_title_image_num,int showSum) {
		super(context);
		this.imagepreview_title_image_num = imagepreview_title_image_num;
		this.showSum = showSum;
		initial();
	}

	public SelfDefViewPager(Context context, AttributeSet attrs) {
		super(context, attrs);
		initial();
	}

	private void initial() {
		setOnPageChangeListener(listener);
	}

	OnPageChangeListener listener = new OnPageChangeListener() {// 重写了OnPageChangeListener的实现方法...

		@Override
		public void onPageScrollStateChanged(int arg0) {//判断滑动状态的改变...arg0={0,1,2};arg0=0，表示没有做什么；arg0=1，表示正在滑动；arg0=2，表示滑动结束；
			if (arg0 == 1) {
				isScrolling = true;
				Log.v(TAG, "正在滑动。。。arg0:"+arg0);
			} else if (arg0 == 2) {
				isScrolling = false;
				Log.v(TAG, "滑动结束。。。arg0:"+arg0);
			}else{
				isScrolling = false;
				Log.v(TAG, "什么也没做。。。arg0:"+arg0);
			}
		}

		@Override
		public void onPageScrolled(int arg0, float arg1, int arg2) {//当页面在滑动的时候会调用此方法，在滑动被停止之前，此方法回一直得到
			//arg0 :当前页面，及你点击滑动的页面；arg1:当前页面偏移的百分比；arg2:当前页面偏移的像素位置
		}

		@Override
		public void onPageSelected(int position) {// 此方法是页面跳转完后得到调用;arg0是你当前选中的页面的Position
			Log.i(TAG, "onPageSelected ----> position :" + (position+1));
			mPosition = position+1;
			imagepreview_title_image_num.setText("("+(mPosition)+"/"+showSum+")");
		}
	};
	
//	@Override
//    public boolean onTouchEvent(MotionEvent ev) {
//        try {
//            return super.onTouchEvent(ev);
//        } catch (IllegalArgumentException ex) {
//            ex.printStackTrace();
//        }
//        return false;
//    }
	
	@Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        try {
        	Log.i(TAG, "e.x:" + ev.getX());
            return super.onInterceptTouchEvent(ev);
        } catch (Exception ex) {
        	
        	Log.i(TAG, "onInterceptTouchEvent, catch");
            ex.printStackTrace();
            
            MotionEvent event = MotionEvent.obtain(ev);
            
            event.setAction(MotionEvent.ACTION_UP);
            
            event.setLocation(8, 8);

            Log.i(TAG, "onInterceptTouchEvent," + getChildAt(getCurrentItem()));
            
            return false;
        }
    }

	public boolean delete_flag = false;
	
	public boolean isDelete_flag() {
		return delete_flag;
	}

	public void setDelete_flag(boolean delete_flag) {
		this.delete_flag = delete_flag;
	}

	public int getShowSum() {
		return showSum;
	}

	public void setShowSum(int showSum) {
		this.showSum = showSum;
	}

	private int mPosition;
	public int getMPostion(){
		return mPosition;
	}
}