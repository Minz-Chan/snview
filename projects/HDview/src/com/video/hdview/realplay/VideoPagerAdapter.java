package com.video.hdview.realplay;

import java.util.List;

import com.video.hdview.component.SurfaceViewMultiLayout;
import com.video.hdview.global.GlobalApplication;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

public class VideoPagerAdapter extends PagerAdapter {
	private Context mContext;
	private PageMode mPageMode;
	private List<PreviewDeviceItem> mPreviewDevices;
	
	public VideoPagerAdapter(Context context, PageMode mode,
			List<PreviewDeviceItem> devices) {
		this.mContext = context;
		this.mPageMode = mode;
		this.mPreviewDevices = devices;
	}
	
	public void setPageMode(PageMode mode) {
		mPageMode = mode;
	}
	
	@Override
	public int getCount() { // 返回实际页数
		int sum = mPreviewDevices.size();
		return mPageMode == PageMode.SINGLE ? sum / 1 : (sum % 4 == 0 ? sum / 4
				: sum / 4 + 1);
	}

	@Override
	public boolean isViewFromObject(View view, Object object) {
		return view == object;
	}

	@Override
	public Object instantiateItem(ViewGroup container, int position) {
		// 生成SurfaceViewMultiLayout或SurfaceViewSingleLayout
		
		VideoRegion v = new VideoRegion(mContext);
		
		//final int screenWidth = GlobalApplication.getInstance().getScreenWidth();
		FrameLayout.LayoutParams param = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
		
		v.setLayoutParams(param);
		v.setTag(position);
		
		container.addView(v);
		
		return v;
	}

	@Override
	public void destroyItem(ViewGroup container, int position, Object object) {
		container.removeView((View) object);
	}

	@Override
	public int getItemPosition(Object object) {
		return super.getItemPosition(object);
	}


	@Override
	public void setPrimaryItem(ViewGroup container, int position, Object object) {
		// TODO Auto-generated method stub
		super.setPrimaryItem(container, position, object);
	}


	@Override
	public void startUpdate(ViewGroup container) {
		// TODO Auto-generated method stub
		super.startUpdate(container);
	}


	@Override
	public void finishUpdate(ViewGroup container) {
		// TODO Auto-generated method stub
		super.finishUpdate(container);
	}
	
	
	
}

enum PageMode {
	SINGLE,
	MULTIPLE
}


