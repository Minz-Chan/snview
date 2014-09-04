package com.starnet.snview.images;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.widget.ImageView;

import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ImageLoader {
	private static final String TAG = "ImageLoader";
	private static final int MAX_THREAD_COUND_IN_POOL = 5;
	static ImageLoader mInstance = null;
	private Handler mHandler = new Handler();
	private Map<String, SoftReference<Bitmap>> mImageCache = new HashMap<String, SoftReference<Bitmap>>();
	private ExecutorService mThreadPool = null;

	private Bitmap getBitmapFromSDCard(String path) {
		BitmapFactory.Options opt = new BitmapFactory.Options();
		opt.inSampleSize = 1;
		return BitmapFactory.decodeFile(path, opt);
	}

	private Bitmap getImageFromCache(String key) {
		Bitmap bmp = null;
		synchronized (mImageCache) {
			if (mImageCache.containsKey(key)) {
				SoftReference softRef = (SoftReference) mImageCache.get(key);
				if (softRef != null) {
					bmp = (Bitmap) softRef.get();
				}
			}
			return bmp;
		}
	}

	public static ImageLoader getInstance() {
		if (mInstance == null) {
			mInstance = new ImageLoader();
		}
		return mInstance;
	}

	public void loadImages(final String path, final ImageView imageView,
			boolean useThreadPool, final ImgCallback imgCallback) {
		Bitmap bmpFromCache = getImageFromCache(path);
		if (bmpFromCache != null) {
			imgCallback.refresh(bmpFromCache, imageView);//从缓存中加载
		} else {
			if (!useThreadPool) {
				Bitmap bmpFromSDCard = getBitmapFromSDCard(path);
				if (bmpFromSDCard != null) {
					synchronized (mImageCache) {
						mImageCache.put(path, new SoftReference(bmpFromSDCard));
						imgCallback.refresh(bmpFromSDCard, imageView);
					}
				}
			} else {
				if (mThreadPool == null) {
					mThreadPool = Executors.newFixedThreadPool(MAX_THREAD_COUND_IN_POOL);
				}
				mThreadPool.submit(new Runnable() {
					public void run() {
						final Bitmap bmpFromSDCard = ImageLoader.this
								.getBitmapFromSDCard(path);
						if (bmpFromSDCard != null) {
							synchronized (ImageLoader.this.mImageCache) {
								ImageLoader.this.mImageCache.put(path,
										new SoftReference(bmpFromSDCard));
								ImageLoader.this.mHandler.post(new Runnable() {
									public void run() {
										imgCallback.refresh(bmpFromSDCard,
												imageView);
									}
								});
								return;
							}
						}						
					}
				});
			}
		}
	}

	public void release() {
		if (mThreadPool != null) {
			if (!mThreadPool.isShutdown()) {
				mThreadPool.shutdown();
			}
			mThreadPool = null;
		}
		mImageCache.clear();
	}

	public static abstract interface ImgCallback {
		public abstract void refresh(Bitmap bitmap, ImageView imageView);
	}
}