package com.video.hdview.syssetting;

import com.video.hdview.R;
import android.app.Activity;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

public class SystemNewFuncActivity extends Activity {
	
	private ViewPager viewPager;
	private int[] imgIdArray;
	private ViewPagerAdapter adapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.guideactivity);
		
		imgIdArray = new int[4];
		imgIdArray[0] = R.drawable.guide_1;
		imgIdArray[1] = R.drawable.guide_2;
		imgIdArray[2] = R.drawable.guide_3;
		imgIdArray[3] = R.drawable.guide_4;
		
		adapter = new ViewPagerAdapter();
		viewPager = (ViewPager) findViewById(R.id.viewPager);
		viewPager.setAdapter(adapter);
	}
	
	class ViewPagerAdapter extends PagerAdapter {
		@Override
		public int getCount() {
			return imgIdArray.length;
		}

		@Override
		public Object instantiateItem(ViewGroup container, int position) {// 载入图片
			ImageView imageView = new ImageView(SystemNewFuncActivity.this);
			imageView.setImageResource(imgIdArray[position]);
			container.addView(imageView);
			return imageView;
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {}

		@Override
		public boolean isViewFromObject(View arg0, Object arg1) {
			return arg0 == arg1;
		}
	}
}