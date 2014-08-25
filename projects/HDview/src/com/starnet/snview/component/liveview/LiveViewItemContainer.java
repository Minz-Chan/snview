package com.starnet.snview.component.liveview;

import com.starnet.snview.R;
import com.starnet.snview.protocol.Connection;
import com.starnet.snview.realplay.RealplayActivity;
import com.starnet.snview.util.ToastUtils;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.GestureDetector.OnGestureListener;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

public class LiveViewItemContainer extends RelativeLayout {
	
	
	private String deviceRecordName;
	
	private WindowLinearLayout mWindowLayout;
	private FrameLayout mPlaywindowFrame;
	private LiveView mSurfaceView;
	private ProgressBar mProgressBar;
	private ImageView mRefresh;
	private TextView mWindowInfoText;
	
	
	
	// ViewFlipper实例
    ViewFlipper mFlipper;
    // 定义手势检测器实例
    GestureDetector detector;
    //定义一个动画数组，用于为ViewFlipper指定切换动画效果
    Animation[] animations = new Animation[4];
    //定义手势动作两点之间的最小距离
    final int FLIP_DISTANCE = 50;
    
    private boolean mIsDoubleClick = false;
	

	private OnLiveViewContainerClickListener mLvContainerClickListener;
	private OnRefreshButtonClickListener mRefreshButtonClickListener;
	private OnGestureListener mGestureListener;
	
	private Connection mCurrentConnection;
	
	
	
	public LiveViewItemContainer(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	public LiveViewItemContainer(Context context) {
		super(context);
	}
	
	

	public void findSubViews() {
		mWindowLayout = (WindowLinearLayout) findViewById(R.id.liveview_surface_infotext_layout);
		mPlaywindowFrame = (FrameLayout) findViewById(R.id.liveview_playwindow_frame);
		mSurfaceView = (LiveView) findViewById(R.id.liveview_surfaceview);
		mProgressBar = (ProgressBar) findViewById(R.id.liveview_progressbar);
		mRefresh = (ImageView) findViewById(R.id.liveview_refresh_imageview);
		mWindowInfoText = (TextView) findViewById(R.id.liveview_liveinfo_textview);
	}
	
	public void init() {
		if (mLvContainerClickListener != null) {
			this.setOnClickListener(mLvContainerClickListener);
		}
		
		if (mRefreshButtonClickListener != null) {
			mRefresh.setOnClickListener(mRefreshButtonClickListener);
		}
		
		mWindowInfoText.setText(null);
		
		
		detector = new GestureDetector(new GestureListener());
		
		animations[0] = AnimationUtils.loadAnimation(getContext(), R.anim.left_in);
        animations[1] = AnimationUtils.loadAnimation(getContext(), R.anim.left_out);
        animations[2] = AnimationUtils.loadAnimation(getContext(), R.anim.right_in);
        animations[3] = AnimationUtils.loadAnimation(getContext(), R.anim.right_in);
        
        
		
	}	
	
	public Connection getCurrentConnection() {
		return mCurrentConnection;
	}
	
	public void setCurrentConnection(Connection conn) {
		this.mCurrentConnection = conn;
	}
	
	public String getDeviceRecordName() {
		return deviceRecordName;
	}
	
	public void setDeviceRecordName(String deviceRecordName) {
		this.deviceRecordName = deviceRecordName;
	}
	
	public void setLiveViewContainerClickListener(
			OnLiveViewContainerClickListener lvContainerClickListener) {
		this.mLvContainerClickListener = lvContainerClickListener;
	}
	
	
	public void setRefreshButtonClickListener(
			OnRefreshButtonClickListener refreshButtonClickListener) {
		this.mRefreshButtonClickListener = refreshButtonClickListener;
	}
	
	
	
	public void setGestureListener(OnGestureListener gestureListener) {
		this.mGestureListener = gestureListener;
	}
	public WindowLinearLayout getWindowLayout() {
		return mWindowLayout;
	}
	
	public FrameLayout getPlaywindowFrame() {
		return mPlaywindowFrame;
	}
	
	public LiveView getSurfaceView() {
		return mSurfaceView;
	}
	
	public ProgressBar getProgressBar() {
		return mProgressBar;
	}
	
	public ImageView getRefreshImageView() {
		return mRefresh;
	}
	
	public TextView getWindowInfoText() {
		return mWindowInfoText;
	}
	
	public void setFlipper(ViewFlipper mFlipper) {
		this.mFlipper = mFlipper;
	}
	
	public void setWindowInfoContent(String info) {
		final StringBuffer s;
		
		if (deviceRecordName != null && info != null) {
			s = new StringBuffer(deviceRecordName);
			s.append("[");
			s.append(info);
			s.append("]");
		} else {
			s = new StringBuffer("");
		}
		
		mWindowInfoText.post(new Runnable() {
			@Override
			public void run() {
				mWindowInfoText.setText(s.toString());
			}
		});
	}
	
	public void resetView() {
		mSurfaceView.setValid(true);
		mProgressBar.setVisibility(View.INVISIBLE);
		mRefresh.setVisibility(View.GONE);
	}
	
	
	public static interface OnLiveViewContainerClickListener extends View.OnClickListener {}
	public static interface OnRefreshButtonClickListener extends View.OnClickListener {}
	public static interface OnGestureListener {
		public void onSlidingLeft();
		public void onSlidingRight();
	}
	
	class GestureListener extends SimpleOnGestureListener  
    {  
  
        @Override  
        public boolean onDoubleTap(MotionEvent e)  
        {  
            // TODO Auto-generated method stub  
            Log.i("TEST", "onDoubleTap");  
            
            mIsDoubleClick = true;
            
            return super.onDoubleTap(e);  
        }  
  
        @Override  
        public boolean onDown(MotionEvent e)  
        {  
            // TODO Auto-generated method stub  
            Log.i("TEST", "onDown");  
            //return super.onDown(e);  
            return true;
        }  
  
        @Override  
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,  
                float velocityY)  
        {  
        	/*
    		 * 如果第一个触点事件的X座标大于第二个触点事件的X座标超过FLIP_DISTANCE 也就是手势从右向左滑。
    		 */
    		if (e1.getX() - e2.getX() > FLIP_DISTANCE) {
    			// 为flipper设置切换的的动画效果
    			mFlipper.setInAnimation(animations[0]);
    			mFlipper.setOutAnimation(animations[1]);
    			mFlipper.showPrevious();
    			
    			ToastUtils.show(getContext(), "向左滑动");

    			if (mGestureListener != null) {
    				mGestureListener.onSlidingLeft();
    			}
    			
    			return true;
    		}
    		/*
    		 * 如果第二个触点事件的X座标大于第一个触点事件的X座标超过FLIP_DISTANCE 也就是手势从右向左滑。
    		 */
    		else if (e2.getX() - e1.getX() > FLIP_DISTANCE) {
    			// 为flipper设置切换的的动画效果
    			mFlipper.setInAnimation(animations[2]);
    			mFlipper.setOutAnimation(animations[3]);
    			mFlipper.showNext();
    			
    			ToastUtils.show(getContext(), "向右滑动");
    			
    			if (mGestureListener != null) {
    				mGestureListener.onSlidingRight();
    			}
    			
    			return true;
    		}
    		return false;  
        }  
  
        @Override  
        public void onLongPress(MotionEvent e)  
        {  
            // TODO Auto-generated method stub  
            Log.i("TEST", "onLongPress");  
            super.onLongPress(e);  
        }  
  
        @Override  
        public boolean onScroll(MotionEvent e1, MotionEvent e2,  
                float distanceX, float distanceY)  
        {  
            // TODO Auto-generated method stub  
            Log.i("TEST", "onScroll:distanceX = " + distanceX + " distanceY = " + distanceY);  
            return super.onScroll(e1, e2, distanceX, distanceY);  
        }  
  
        @Override  
        public boolean onSingleTapUp(MotionEvent e)  
        {  
            // TODO Auto-generated method stub  
                
            
            LiveViewItemContainer.this.postDelayed(new Runnable() {

				@Override
				public void run() {
					if (!mIsDoubleClick) {
						Log.i("TEST", "onSingleTapUp");  
					} else {
						mIsDoubleClick = false;
					}
					
				}
            	
            }, 300);
            
            
            return super.onSingleTapUp(e);  
        }  
          
    }
	

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		// 将该Activity上的触碰事件交给GestureDetector处理
		return detector.onTouchEvent(event);
	}

}
