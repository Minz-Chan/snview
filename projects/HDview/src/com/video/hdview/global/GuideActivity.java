package com.video.hdview.global;

import com.video.hdview.R;
import com.video.hdview.realplay.RealplayActivity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;

public class GuideActivity extends Activity implements OnPageChangeListener {

	private ViewPager viewPager;
	private int[] imgIdArray;
	private ViewPagerAdapter adapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.guideactivity);

		viewPager = (ViewPager) findViewById(R.id.viewPager);
		imgIdArray = new int[4];
		imgIdArray[0] = R.drawable.guide_1;
		imgIdArray[1] = R.drawable.guide_2;
		imgIdArray[2] = R.drawable.guide_3;
		imgIdArray[3] = R.drawable.guide_4;

		adapter = new ViewPagerAdapter();
		viewPager.setAdapter(adapter);
//		viewPager.setOnPageChangeListener(this);
		viewPager.setCurrentItem(0);
	}

	@Override
	public void onPageScrollStateChanged(int arg0) {
	}

	@Override
	public void onPageScrolled(int arg0, float arg1, int arg2) {
	}

	@Override
	public void onPageSelected(int arg0) {/* .... */
		Bitmap bitmap = BitmapFactory.decodeResource(getResources(),imgIdArray[arg0]);
		ImageView imageView = new ImageView(GuideActivity.this);
		imageView.setImageBitmap(bitmap);
	}

	class ViewPagerAdapter extends PagerAdapter {

		@Override
		public int getCount() {
			return imgIdArray.length;
		}

		@Override
		public Object instantiateItem(ViewGroup container, int position) {// 载入图片

			String text = getString(R.string.guide_begin_experiment);
			GuideImageTextView guideImageButton = new GuideImageTextView(GuideActivity.this);
			guideImageButton.setImageResource(imgIdArray[position]);
			guideImageButton.setText(text);

			if ((position != (imgIdArray.length - 1))) {
				String sentence = getString(R.string.guide_jump_experiment);
				guideImageButton.setText(sentence);
			}
			guideImageButton.getTextView().setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					Intent intent = new Intent();
					intent.setClass(GuideActivity.this, RealplayActivity.class);
					startActivity(intent);
					GuideActivity.this.finish();
				}
			});
			container.addView(guideImageButton);
			return guideImageButton;
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			// container.removeViewAt(position);
			// super.destroyItem(container, position, object);
		}

		@Override
		public boolean isViewFromObject(View arg0, Object arg1) {
			return arg0 == arg1;
		}
	}
}