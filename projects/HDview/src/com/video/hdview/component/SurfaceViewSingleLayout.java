package com.video.hdview.component;

import com.video.hdview.R;
import com.video.hdview.component.liveview.LiveViewItemContainer;
import com.video.hdview.component.liveview.LiveViewItemContainer.OnRefreshButtonClickListener;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

public class SurfaceViewSingleLayout extends LinearLayout {
private static final String TAG = "SurfaceViewSingleLayout";
	
	private Context mContext;
	
	private LiveViewItemContainer mLiveview;
	
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public SurfaceViewSingleLayout(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
		this.mContext = context;
		init();
	}

	public SurfaceViewSingleLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.mContext = context;
		init();
	}

	public SurfaceViewSingleLayout(Context context) {
		super(context);
		this.mContext = context;
		init();
	}
	
	
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		//Log.i(TAG, "onMeasure(), widthMeasureSpec:" + widthMeasureSpec + ", heightMeasureSpec:" + heightMeasureSpec);
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	}

	@SuppressLint("DrawAllocation")
	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		//Log.i(TAG, "onLayout(), changed:" + changed + ", l:" + l + ", t:" + t + ", r:" + r + ", b:" + b);
		
		if (changed) {
			//int width = r - l;
			int height = b - t;
			
			// 视频区域高度
			int surfaceHeight = (int) (height - getResources().getDimension(R.dimen.window_text_height) 
					- 2 * getResources().getDimension(R.dimen.surface_container_space));
			LinearLayout.LayoutParams param1 = new LinearLayout.LayoutParams(
					LinearLayout.LayoutParams.MATCH_PARENT, surfaceHeight);
			
			mLiveview.getPlaywindowFrame().setLayoutParams(param1);
		}
		
		super.onLayout(changed, l, t, r, b);
	}

	private void init() {
		//Log.i(TAG, "init()");
		
		this.setLayoutParams(new FrameLayout.LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		
		((LayoutInflater) (mContext
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE))).inflate(
				R.layout.surfaceview_single_layout, this, true);
		
		mLiveview = (LiveViewItemContainer) findViewById(R.id.liveview_liveitem);
		
//		mLiveview.findSubViews();
		mLiveview.init();
	}
	
	public void setLiveviewRefreshButtonClickListener(OnRefreshButtonClickListener listener) {
		if (listener == null) {
			throw new NullPointerException("OnRefreshButtonClickListener is null");
		}
		
		mLiveview.setRefreshButtonClickListener(listener);
	}
	
	public LiveViewItemContainer getLiveview() {
		return mLiveview;
	}
	
	
	
	
}
