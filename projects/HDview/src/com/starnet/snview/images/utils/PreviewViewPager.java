package com.starnet.snview.images.utils;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;

public class PreviewViewPager extends PagerAdapter {
	
	private List<String> imagePathList;
	private List<Bitmap> bitmapList;
	private List<Drawable> drawableList;
	
	public PreviewViewPager(List<String> imgPathList) {
		this.imagePathList = imgPathList;
		bitmapList = new ArrayList<Bitmap>();
		drawableList = new ArrayList<Drawable>();
		
		//将路径列表中的数据一一取出，并且将图片还原出Bitmap和Drawable
		int size = imagePathList.size();
		for (int i = 0; i < size; i++) {
			String path = imagePathList.get(i);
			Bitmap mBitmap = BitmapFactory.decodeFile(path);
			Drawable mDrawable = BitmapDrawable.createFromPath(path);
			drawableList.add(mDrawable);
			bitmapList.add(mBitmap);
		}
	}
	

	@Override
	public int getCount() {
		int size = 0 ;
		if (bitmapList != null) {
			size = bitmapList.size();
		}
		return size;
	}

	@Override
	public void destroyItem(ViewGroup container, int position, Object object) {
		
		super.destroyItem(container, position, object);
	}


	@Override
	public Object instantiateItem(ViewGroup container, int position) {
		Context context = container.getContext();
		PhotoView photoView = new PhotoView(context);
		Bitmap bitmap = bitmapList.get(position);
		photoView.setImageBitmap(bitmap);
		return super.instantiateItem(container, position);
	}


	@Override
	public boolean isViewFromObject(View arg0, Object arg1) {
		
		return false;
	}

}
