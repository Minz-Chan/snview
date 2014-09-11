package com.starnet.snview.images;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.util.Log;
import android.widget.ImageView;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ImageLoader {
	private static final String TAG = "ImageLoader";
	
	private static final int MAX_THREAD_COUND_IN_POOL = 5;
	private static int IMAGE_MAX_SIZE = 0;
	
	static ImageLoader mInstance = null;
	private Handler mHandler = new Handler();
	private Map<String, SoftReference<Bitmap>> mImageCache = new HashMap<String, SoftReference<Bitmap>>();
	private ExecutorService mThreadPool = null;

	private Bitmap getBitmapFromSDCard(String path) {
		//BitmapFactory.Options opt = new BitmapFactory.Options();
		//opt.inSampleSize = 1;
		//return BitmapFactory.decodeFile(path, opt);
		
		Bitmap b = null;
		
		try {
			b = decodeFile(new File(path));
		} catch (IOException e) {
			
			e.printStackTrace();
		}
		
		return b;
	}
	
	private Bitmap decodeFile(File f) throws IOException{
	    Bitmap b = null;

	    // Decode image size
	    BitmapFactory.Options o = new BitmapFactory.Options();
	    o.inJustDecodeBounds = true;

	    FileInputStream fis = new FileInputStream(f);
	    BitmapFactory.decodeStream(fis, null, o);
	    fis.close();

	    int scale = 1;
	    if (o.outHeight > IMAGE_MAX_SIZE || o.outWidth > IMAGE_MAX_SIZE) {
	        scale = (int)Math.pow(2, (int) Math.ceil(Math.log(IMAGE_MAX_SIZE / 
	           (double) Math.max(o.outHeight, o.outWidth)) / Math.log(0.5)));
	    }
	    
	    Log.i(TAG, "scale: " + scale);

	    // Decode with inSampleSize
	    BitmapFactory.Options o2 = new BitmapFactory.Options();
	    o2.inSampleSize = scale;
	    fis = new FileInputStream(f);
	    b = BitmapFactory.decodeStream(fis, null, o2);
	    fis.close();

	    return b;
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
	
	public void setImageMaxSize(int max) {
		IMAGE_MAX_SIZE = max;
	}

	public static abstract interface ImgCallback {
		public abstract void refresh(Bitmap bitmap, ImageView imageView);
	}
}