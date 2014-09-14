package com.starnet.snview.component;


import java.util.ArrayList;
import java.util.List;

import com.starnet.snview.R;
import com.starnet.snview.component.liveview.LiveViewItemContainer;
import com.starnet.snview.component.liveview.LiveViewItemContainer.OnRefreshButtonClickListener;
import com.starnet.snview.global.GlobalApplication;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

public class SurfaceViewMultiLayout extends LinearLayout {
	private static final String TAG = "SurfaceViewMultiLayout";
	
	private Context mContext;
	
	private LinearLayout mViewVideoLinear1;
	private LinearLayout mViewVideoLinear2;
	
	private LiveViewItemContainer mLiveview1;
	private LiveViewItemContainer mLiveview2;
	private LiveViewItemContainer mLiveview3;
	private LiveViewItemContainer mLiveview4;
	
	private List<LiveViewItemContainer> mLiveviews;
	
	
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public SurfaceViewMultiLayout(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
		this.mContext = context;
		
		init();
	}

	public SurfaceViewMultiLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.mContext = context;
		
		init();
	}

	public SurfaceViewMultiLayout(Context context) {
		super(context);
		this.mContext = context;
		
		init();
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		/*
		Log.i(TAG,
				"onMeasure(), widthMeasureSpec:"
						+ MeasureSpec.getSize(widthMeasureSpec)
						+ ", heightMeasureSpec:"
						+ MeasureSpec.getSize(heightMeasureSpec));
		String wMode = MeasureSpec.getMode(widthMeasureSpec) == MeasureSpec.AT_MOST ? "AT_MOST"
				: (MeasureSpec.getMode(widthMeasureSpec) == MeasureSpec.EXACTLY ? "EXACTLY"
						: "UNSPECIFIED");
		String hMode = MeasureSpec.getMode(heightMeasureSpec) == MeasureSpec.AT_MOST ? "AT_MOST"
				: (MeasureSpec.getMode(heightMeasureSpec) == MeasureSpec.EXACTLY ? "EXACTLY"
						: "UNSPECIFIED");
		
		Log.i(TAG, "onMearsure()->mode, w:" + wMode + ", h:" + hMode);*/
		

		boolean isFullScreen = GlobalApplication.getInstance().isIsFullMode();
		int w, h;
		
		if (isFullScreen) {
			w = GlobalApplication.getInstance().getScreenWidth();
			h = GlobalApplication.getInstance().getScreenHeight();
		} else {
			w = GlobalApplication.getInstance().getScreenWidth();
			h = GlobalApplication.getInstance().getScreenWidth();
		}
		
		//Log.i(TAG, "onMeasure(), real_w:" + w + ", real_h:" + h);
		
		
		super.onMeasure(MeasureSpec.makeMeasureSpec(w, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(h, MeasureSpec.EXACTLY));
	}

	@SuppressLint("DrawAllocation")
	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		//Log.i(TAG, "onLayout(), changed:" + changed + ", l:" + l + ", t:" + t + ", r:" + r + ", b:" + b);
		
		if (changed) {
			int width = r - l;
			int height = b - t;
			
			// 视频单行布局(LinearLayout)
			LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(width, height / 2 );
			
			mViewVideoLinear1.setLayoutParams(param);
			mViewVideoLinear2.setLayoutParams(param);

			
			// 视频区域高度
			int surfaceHeight = (int) (height / 2.0 - getResources().getDimension(R.dimen.window_text_height) 
					- 2 * getResources().getDimension(R.dimen.surface_container_space));
			LinearLayout.LayoutParams param1 = new LinearLayout.LayoutParams(
					LinearLayout.LayoutParams.MATCH_PARENT, surfaceHeight);
			
			mLiveview1.getPlaywindowFrame().setLayoutParams(param1);
			mLiveview2.getPlaywindowFrame().setLayoutParams(param1);
			mLiveview3.getPlaywindowFrame().setLayoutParams(param1);
			mLiveview4.getPlaywindowFrame().setLayoutParams(param1);
		}
		
		
		super.onLayout(changed, l, t, r, b);
	}
	
	
	private void init() {
		//Log.i(TAG, "init()");
		
		this.setLayoutParams(new FrameLayout.LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		
		((LayoutInflater) (mContext
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE))).inflate(
				R.layout.surfaceview_multi_layout, this, true);		
		
		mViewVideoLinear1 = (LinearLayout) findViewById(R.id.view_video_linear1);
		mViewVideoLinear2 = (LinearLayout) findViewById(R.id.view_video_linear2);

		
		mLiveview1 = (LiveViewItemContainer) findViewById(R.id.liveview_liveitem1);
		mLiveview2 = (LiveViewItemContainer) findViewById(R.id.liveview_liveitem2);
		mLiveview3 = (LiveViewItemContainer) findViewById(R.id.liveview_liveitem3);
		mLiveview4 = (LiveViewItemContainer) findViewById(R.id.liveview_liveitem4);
		
		// 视频控件子控件引用初始化
		mLiveview1.findSubViews();
		mLiveview2.findSubViews();
		mLiveview3.findSubViews();
		mLiveview4.findSubViews();
		
		// 视频控件初始化
		mLiveview1.init();
		mLiveview2.init();
		mLiveview3.init();
		mLiveview4.init();
		
		mLiveviews = new ArrayList<LiveViewItemContainer>();
		mLiveviews.add(mLiveview1);
		mLiveviews.add(mLiveview2);
		mLiveviews.add(mLiveview3);
		mLiveviews.add(mLiveview4);
	}
	
	public void setLiveviewRefreshButtonClickListener(OnRefreshButtonClickListener listener) {
		if (listener == null) {
			throw new NullPointerException("OnRefreshButtonClickListener is null");
		}
		
		mLiveview1.setRefreshButtonClickListener(listener);
		mLiveview2.setRefreshButtonClickListener(listener);
		mLiveview3.setRefreshButtonClickListener(listener);
		mLiveview4.setRefreshButtonClickListener(listener);
	}
	
	public List<LiveViewItemContainer> getLiveviews() {
		return mLiveviews;
	}

	
	
}
