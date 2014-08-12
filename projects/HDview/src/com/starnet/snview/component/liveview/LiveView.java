package com.starnet.snview.component.liveview;

import java.nio.ByteBuffer;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class LiveView extends SurfaceView implements OnLiveViewChangedListener {
	public static final String TAG = "LiveView";
	
	private SurfaceHolder mHolder = null;
	
	private int width = 352;
	private int height = 288;
	
	private byte [] mPixel = new byte[width * height * 2];
    private ByteBuffer mBuffer;
	private Bitmap mVideoBit;  
	
	private boolean isValid = true;
	
	public LiveView(Context context) {
		super(context);
		init();
	}

	public LiveView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	public LiveView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}
	
	private void init() {
		if ( mHolder == null) {
			mHolder = getHolder();
			mHolder.addCallback(this);
		}
		

		mBuffer = null;
		mVideoBit = null;

		mBuffer = ByteBuffer.wrap(mPixel);
		mVideoBit = Bitmap.createBitmap(width, height, Config.RGB_565);
		// this.setScaleType(ImageView.ScaleType.FIT_XY);
	}
	
	
	
	public void setValid(boolean isValid) {
		this.isValid = isValid;
		
		onDisplayContentReset();
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
		onDisplayContentReset();
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		System.out.println(this + "@destroyed...");	
	}

	@SuppressLint("DrawAllocation")
	@Override
	protected void onDraw(Canvas canvas) {
		Log.i(TAG, "onDraw begin");
		Log.i(TAG, "mVideoBit: " + mVideoBit + ", canvas: " + canvas);
		
		
		super.onDraw(canvas);
		
		canvas = mHolder.lockCanvas();
	
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
        	
        	Log.i(TAG, "onDraw redraw");
        	
        }
		
		System.out.println(this + "@onDraw");

		mHolder.unlockCanvasAndPost(canvas); 
		
		Log.i(TAG, "onDraw end");
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
        	
        	Log.i(TAG, "refreshDisplay, width: " + getWidth() + ", height: " + getHeight());
        	
        	mHolder.unlockCanvasAndPost(canvas); 
        	
        	System.out.println(this + "@unlockCanvasAndPost" );
        	
        }
	}
	
	@Override
	public void onDisplayResulotionChanged(int width, int height) {
		if (this.width != width || this.height != height) {
			init();
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
				canvas.drawColor(Color.LTGRAY);
			}
			
			
			mHolder.unlockCanvasAndPost(canvas); 
		}
		
	}
	
	
	
	
}
