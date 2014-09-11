package com.starnet.snview.component.liveview;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import com.starnet.snview.R;
import com.starnet.snview.global.Constants;
import com.starnet.snview.global.GlobalApplication;
import com.starnet.snview.images.LocalFileUtils;
import com.starnet.snview.util.BitmapUtils;
import com.starnet.snview.util.SDCardUtils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

public class LiveView extends SurfaceView implements OnLiveViewChangedListener {
	public static final String TAG = "LiveView";
	
	private SurfaceHolder mHolder = null;
	
	private int width = 352;
	private int height = 288;
	
	private byte [] mPixel = new byte[width * height * 2];
    private ByteBuffer mBuffer;
	private Bitmap mVideoBit;  
	
	private boolean isValid = true;
	
	private boolean canTakePicture = false;
	
	public LiveView(Context context) {
		super(context);
		init(width, height);
	}

	public LiveView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(width, height);
	}

	public LiveView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(width, height);
	}
	
	public void init(int width, int height) {
		if ( mHolder == null) {
			mHolder = getHolder();
			mHolder.addCallback(this);
		}
		
		if (mPixel != null) {
			mPixel = null;
			mPixel = new byte[width * height * 2];
		}
		
		mBuffer = null;
		
		if (mVideoBit != null) {
			mVideoBit.recycle();
			mVideoBit = null;
		}
		

		mBuffer = ByteBuffer.wrap(mPixel);
		mVideoBit = Bitmap.createBitmap(width, height, Config.RGB_565);
		// this.setScaleType(ImageView.ScaleType.FIT_XY);
	}
	
	
	public boolean isValid() {
		return isValid;
	}
	
	public void setValid(boolean isValid) {
		this.isValid = isValid;
		
		onDisplayContentReset();
	}
	
	public void setTakePicture(boolean canTakePicture) {
		this.canTakePicture = canTakePicture;
	}

	public byte[] retrievetDisplayBuffer() {
		return mPixel;
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		
		
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		System.out.println(this + "@created... ProgressBar");
		
		setBackgroundColor(Color.TRANSPARENT);
		
		onDisplayContentReset();
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		System.out.println(this + "@destroyed...");	
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		//Log.i(TAG, "onMeasure(), w:" + MeasureSpec.getSize(widthMeasureSpec)
		//		+ ", h:" + MeasureSpec.getSize(heightMeasureSpec));
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	}
	
	@Override
	protected void onLayout(boolean changed, int left, int top, int right,
			int bottom) {
		//Log.i(TAG, "onLayout()");
		super.onLayout(changed, left, top, right, bottom);
	}

	private void refreshDisplay() {
		Canvas canvas = mHolder.lockCanvas();
		
		// 刷屏
		if (mVideoBit != null && canvas != null) {
        	/* 此处rewind用意
         	 * 4.2中对copyPixelsFromBuffer( )执行的缓冲区进行了调整，每次拷贝结束后，将下次拷贝
        	 * 的起始位置置为前一次拷贝结束时的位置。这样，如果对同一个ByteBuffer执行多次连续拷贝，
        	 * 就要注意每次起始位置。
        	 */
        	mBuffer.rewind();	
        	
        	if ((mVideoBit.getWidth() * mVideoBit.getHeight() * 2) 
        			!= (mBuffer.position() + mBuffer.remaining())) {
        		return;
        	}
        	
        	mVideoBit.copyPixelsFromBuffer(mBuffer);	
        	
        	Bitmap video = mVideoBit;
        	
        	canvas.drawBitmap(Bitmap.createScaledBitmap(video, getWidth(), getHeight(), true)
            		, 0, 0, null); 
        	
        	//Log.i(TAG, "refreshDisplay, width: " + getWidth() + ", height: " + getHeight());
        	
        	if (canTakePicture) {
        		savePictureAndThumbnail(video);
        		canTakePicture = false;
        	}
        	
        	mHolder.unlockCanvasAndPost(canvas);         	
        }
	}
	
	private int THUMBNAIL_HEIGHT = 200;
	
	private void savePictureAndThumbnail(Bitmap bmp) {
		
		LiveViewItemContainer c = findVideoContainerByView(this);
		if (c == null) {
			return;
		}
		

		boolean result = false;
		String imgPath =  null;
		
		Log.i(TAG, "Has Sdcard: " + SDCardUtils.IS_MOUNTED);
		
		if (SDCardUtils.IS_MOUNTED) { // SDcard可用
			// 获取拍照截图及其缩略图完整路径
			String fileName = LocalFileUtils.getFormatedFileName(c.getPreviewItem()
					.getDeviceRecordName(), c.getPreviewItem().getChannel());
			String fullImgPath = LocalFileUtils.getCaptureFileFullPath(fileName, true);
			String fullThumbImgPath = LocalFileUtils.getThumbnailsFileFullPath(fileName, true);;
			
			Log.i(TAG, "fileName: " + fileName);
			Log.i(TAG, "fileImgPath: " + fullImgPath);
			Log.i(TAG, "fullThumbImgPath: " + fullThumbImgPath);
			
			imgPath = fullImgPath;
			
			// 取得缩略图
			int thumbnailHeight = THUMBNAIL_HEIGHT;
			int thumbnailWidth = THUMBNAIL_HEIGHT * bmp.getWidth() / bmp.getHeight();
			Bitmap thumbnail = BitmapUtils.extractMiniThumb(bmp, thumbnailWidth, thumbnailHeight, false); 

			Log.i(TAG, "tW: " + thumbnailWidth + ", tH: " + thumbnailHeight);
			Log.i(TAG, "Bitmap thumbnail: " + thumbnail);
			
			// 保存拍照截图
			if (saveBmpFile(bmp, fullImgPath)
					&& saveBmpFile(thumbnail, fullThumbImgPath)) {
				result = true;
				Log.i(TAG, "Save pictures successfully !");
			}
		} else { // 不存在SDCard的情况（分有/无内置内存情况）
			
		}
		
		
		// 通知主界面
		Handler h = GlobalApplication.getInstance().getHandler();
		if (h != null && result) {
			Bundle b = new Bundle();
			b.putString("PICTURE_FULL_PATH", imgPath);
			
			Message m = h.obtainMessage();
			m.what = Constants.TAKE_PICTURE;
			m.setData(b);
			m.sendToTarget();
			
			Log.i(TAG, "Send msg notification for TAKE_PICTURE");
			Log.i(TAG, "Image path: " + imgPath);
		}
		
	}
	
	
	private boolean saveBmpFile(Bitmap b, String fullImgPath) {
		File f = new File(fullImgPath);
		FileOutputStream fout =  null;
		
		try {
			fout =  new FileOutputStream(f);
			
			b.compress(Bitmap.CompressFormat.JPEG, 100, fout);
			
			fout.close();
			
			return true;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			if (fout != null) {
				try {
					fout.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
			e.printStackTrace();
		}
		
		return false;
	}

	@Override
	public void onDisplayResulotionChanged(int width, int height) {
		if (this.width != width || this.height != height) {
			init(width, height);
		}
	}
	
	@Override
	public void onDisplayContentUpdated() {
		//this.postInvalidate();
		refreshDisplay();
	}

	@Override
	public void onDisplayContentReset() {	
		Canvas canvas = mHolder.lockCanvas();
		
		if (canvas != null) {
			if (isValid) {
				canvas.drawColor(Color.BLACK);
			} else {
				canvas.drawColor(getResources().getColor(R.color.liveview_bg_invalid));
				
				LiveViewItemContainer c = findVideoContainerByView(this);
				if (c != null) {
					c.setWindowInfoContent(null);
				}
			}
			
			
			mHolder.unlockCanvasAndPost(canvas); 
		}
		
		
		
	}
	
	private LiveViewItemContainer findVideoContainerByView(View v) {
		View curr = v;
		
		while (curr != null) {
			if (curr instanceof LiveViewItemContainer) {
				break;
			}
			
			curr = (View) curr.getParent();
		}
		
		return (LiveViewItemContainer) curr;
	}
	
	
	
	
}
