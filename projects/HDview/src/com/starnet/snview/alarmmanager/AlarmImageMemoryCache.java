package com.starnet.snview.alarmmanager;

import java.lang.ref.SoftReference;
import java.util.LinkedHashMap;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v4.util.LruCache;

//内存缓存
public class AlarmImageMemoryCache {
	// 获取一张图片，从三个地方进行获取，首先是内存缓存，然后是文件缓存，最后才从网络中获取。
	private static final int SOFT_CACHE_SIZE = 15; // 软引用缓存容量
	private LruCache<String, Bitmap> mLruCache;// 硬引用缓存
	private static LinkedHashMap<String, SoftReference<Bitmap>> mSoftCache;

	private int MAXMEMONRY = (int) (Runtime.getRuntime().maxMemory() / 1024);

	public AlarmImageMemoryCache(Context context) {
		if (mLruCache == null) {
			mLruCache = new LruCache<String, Bitmap>(MAXMEMONRY / 8) {
				@Override
				protected int sizeOf(String key, Bitmap value) {
					if (value != null)
						return value.getRowBytes() * value.getHeight();
					else
						return 0;
				}

				@Override
				protected void entryRemoved(boolean evicted, String key,
						Bitmap oldValue, Bitmap newValue) {
					if (oldValue != null) {
						// 硬引用缓存容量满的时候，会根据LRU算法把最近没有被使用的图片转入此软引用缓存
						mSoftCache
								.put(key, new SoftReference<Bitmap>(oldValue));
					}
				}
			};
		}

		mSoftCache = new LinkedHashMap<String, SoftReference<Bitmap>>(
				SOFT_CACHE_SIZE, 0.75f, true) {
			/**
			 * 
			 */
			private static final long serialVersionUID = -7901479486602060989L;
			@Override
			protected boolean removeEldestEntry(
					Entry<String, SoftReference<Bitmap>> eldest) {
				if (size() > SOFT_CACHE_SIZE) {
					return true;
				}
				return false;
			}
		};
	}
	/**
	 * 从缓存中获取图片
	 */
	public Bitmap getBitmapFromCache(String url) {
		Bitmap bitmap;
		synchronized (mLruCache) { // 先从硬引用缓存中获取
			bitmap = mLruCache.get(url);
			if (bitmap != null) {// 如果找到的话，把元素移到LinkedHashMap的最前面，从而保证在LRU算法中是最后被删除
				mLruCache.remove(url);
				mLruCache.put(url, bitmap);
				return bitmap;
			}
		}
		synchronized (mSoftCache) {
			SoftReference<Bitmap> bitmapReference = mSoftCache.get(url);
			if (bitmapReference != null) {
				bitmap = bitmapReference.get();
				if (bitmap != null) {
					// 将图片移回硬缓存
					mLruCache.put(url, bitmap);
					mSoftCache.remove(url);
					return bitmap;
				} else {
					mSoftCache.remove(url);
				}
			}
		}
		return null;
	}

	/**
	 * 添加图片到缓存
	 */
	public void addBitmapToCache(String url, Bitmap bitmap) {
		if (bitmap != null) {
			synchronized (mLruCache) {
				mLruCache.put(url, bitmap);
			}
		}
	}
	public void clearCache() {
		mSoftCache.clear();
	}
}
