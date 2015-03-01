package com.starnet.snview.component.liveview;

import java.io.File;

import com.starnet.snview.R;
import com.starnet.snview.component.audio.AudioBufferQueue;
import com.starnet.snview.component.audio.AudioPlayer;
import com.starnet.snview.component.h264.AVConfig;
import com.starnet.snview.component.h264.H264DecodeUtil;
import com.starnet.snview.component.h264.MP4Recorder;
import com.starnet.snview.images.LocalFileUtils;
import com.starnet.snview.playback.utils.PlaybackDeviceItem;
import com.starnet.snview.protocol.Connection;
import com.starnet.snview.protocol.Connection.StatusListener;
import com.starnet.snview.realplay.PreviewDeviceItem;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.AnimationDrawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class PlaybackLiveViewItemContainer extends RelativeLayout {
	private static final String TAG = "LiveViewItemContainer";
	
	private Context mContext;
	
	private int mItemIndex;
	private String mDeviceRecordName;
	private PlaybackDeviceItem mPreviewItem;
	private StatusListener mConnectionStatusListener;
	private OnRefreshButtonClickListener mRefreshButtonClickListener;
	
	private WindowLinearLayout mWindowLayout;
	private FrameLayout mPlaywindowFrame;
	private PlaybackLiveView mSurfaceView;
	private ProgressBar mProgressBar;
	private ImageView mRefresh;
	private TextView mWindowInfoText;
	private ImageView[] mPTZImageViewArray = new ImageView[8];
	
	private RelativeLayout mArrowAddFrame;
	private ImageView[] mAddFocalLengthArray = new ImageView[4];
	private RelativeLayout mArrowSubFrame;
	private ImageView[] mSubFocalLengthArray = new ImageView[4];
	
	private String mRecordFileName;
	private long mRecordFileHandler;
	private boolean mCanStartRecord;
	private AVConfig.Video mVideoConfig = new AVConfig.Video();
	
	private Paint mPaint = new Paint();	
	
	private H264DecodeUtil mH264decoder;

	
	
	public PlaybackLiveViewItemContainer(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
		mPaint.setColor(Color.RED);
		mH264decoder = new H264DecodeUtil(String.valueOf(this.hashCode()));
		
	}
	public PlaybackLiveViewItemContainer(Context context) {
		super(context);
		mContext = context;
		mPaint.setColor(Color.RED);
		mH264decoder = new H264DecodeUtil(String.valueOf(this.hashCode()));
	}

	public void findSubViews() {
		((LayoutInflater) (mContext
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE))).inflate(
				R.layout.playback_live_view_item, this, true);	
		
		mWindowLayout = (WindowLinearLayout) findViewById(R.id.liveview_surface_infotext_layout);
		mPlaywindowFrame = (FrameLayout) findViewById(R.id.liveview_playwindow_frame);
		mSurfaceView = (PlaybackLiveView) findViewById(R.id.liveview_surfaceview);
		mProgressBar = (ProgressBar) findViewById(R.id.liveview_progressbar);
		mRefresh = (ImageView) findViewById(R.id.liveview_refresh_imageview);
		mWindowInfoText = (TextView) findViewById(R.id.liveview_liveinfo_textview);
		
		mSurfaceView.setParent(this); // bind parent
		
		mPTZImageViewArray[0] = (ImageView) findViewById(R.id.arrow_left);
		mPTZImageViewArray[1] = (ImageView) findViewById(R.id.arrow_left_down);
		mPTZImageViewArray[2] = (ImageView) findViewById(R.id.arrow_down);
		mPTZImageViewArray[3] = (ImageView) findViewById(R.id.arrow_right_down);
		mPTZImageViewArray[4] = (ImageView) findViewById(R.id.arrow_right);
		mPTZImageViewArray[5] = (ImageView) findViewById(R.id.arrow_right_up);
		mPTZImageViewArray[6] = (ImageView) findViewById(R.id.arrow_up);
		mPTZImageViewArray[7] = (ImageView) findViewById(R.id.arrow_left_up);

		mArrowAddFrame = (RelativeLayout) findViewById(R.id.arrow_add_frame);
		mAddFocalLengthArray[0] = (ImageView) findViewById(R.id.arrow_add_left_down);
		mAddFocalLengthArray[1] = (ImageView) findViewById(R.id.arrow_add_right_down);
		mAddFocalLengthArray[2] = (ImageView) findViewById(R.id.arrow_add_right_up);
		mAddFocalLengthArray[3] = (ImageView) findViewById(R.id.arrow_add_left_up);
		
		mArrowSubFrame = (RelativeLayout) findViewById(R.id.arrow_sub_frame);
		mSubFocalLengthArray[0] = (ImageView) findViewById(R.id.arrow_sub_left_down);
		mSubFocalLengthArray[1] = (ImageView) findViewById(R.id.arrow_sub_right_down);
		mSubFocalLengthArray[2] = (ImageView) findViewById(R.id.arrow_sub_right_up);
		mSubFocalLengthArray[3] = (ImageView) findViewById(R.id.arrow_sub_left_up);
	}
	
	public void init() {		
//		mIsManualStop = false;
//		mIsResponseError = false;
		
		if (mRefreshButtonClickListener != null) {
			mRefresh.setOnClickListener(mRefreshButtonClickListener);
		}
		
		mWindowInfoText.setText(null);
	}	
	
	public H264DecodeUtil getH264Decoder() {
		return mH264decoder;
	}

//	public void setIsResponseError(boolean isResponseError) {
//		this.mIsResponseError = isResponseError;
//	}
//	
//	public boolean isResponseError() {
//		return mIsResponseError;
//	}
//	
//	public boolean isManualStop() {
//		return mIsManualStop;
//	}
//	
//	public void setIsManualStop(boolean isManualStop) {
//		this.mIsManualStop = isManualStop;
//	}
	
//	public Connection getConnection() {
//		return mConnection;
//	}
//	
//	public void setConnection(Connection conn) {
//		this.mConnection = conn;
//	}
	
	public int getItemIndex() {
		return mItemIndex;
	}
	public void setItemIndex(int itemIndex) {
		this.mItemIndex = itemIndex;
	}
	public String getDeviceRecordName() {
		return mDeviceRecordName;
	}
	
	public void setDeviceRecordName(String deviceRecordName) {
		this.mDeviceRecordName = deviceRecordName;
	}
	
	public PlaybackDeviceItem getPlaybackItem() {
		return mPreviewItem;
	}
	
	public void setPlaybackItem(PlaybackDeviceItem playbackItem) {
		this.mPreviewItem = playbackItem;
	}
	
	public boolean canStartRecord() {
		return mCanStartRecord;
	}
	
	public void setCanStartRecord(boolean canStartRecord) {
		this.mCanStartRecord = canStartRecord;
	}
	
	public long getRecordFileHandler() {
		return mRecordFileHandler;
	}
	
	public AVConfig.Video getVideoConfig() {
		return mVideoConfig;
	}
	
	public void setRefreshButtonClickListener(
			OnRefreshButtonClickListener refreshButtonClickListener) {
		this.mRefreshButtonClickListener = refreshButtonClickListener;
		
		mRefresh.setOnClickListener(mRefreshButtonClickListener);
	}
	
	public void setConnectionStatusListener(
			StatusListener connectionStatusListener) {
		this.mConnectionStatusListener = connectionStatusListener;
	}
	public WindowLinearLayout getWindowLayout() {
		return mWindowLayout;
	}
	
	public FrameLayout getPlaywindowFrame() {
		return mPlaywindowFrame;
	}
	
	public PlaybackLiveView getSurfaceView() {
		return mSurfaceView;
	}
	
	public ProgressBar getProgressBar() {
		return mProgressBar;
	}
	
	public ImageView getRefreshImageView() {
		return mRefresh;
	}
	
	public TextView getWindowInfoText() {
		return mWindowInfoText;
	}
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int width = MeasureSpec.getSize(widthMeasureSpec);
		int height = MeasureSpec.getSize(heightMeasureSpec);
		setMeasuredDimension(width, height);
		
		if (!invalidateComponent()) {
			return;
		} 
		
		int windowTextHeight = (int)getResources().getDimension(
				R.dimen.window_text_height);
		int surfaceWidth = width;
		int surfaceHeight = height - windowTextHeight;		
		mWindowLayout.measure(
				MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY), 
				MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY));
		mPlaywindowFrame.measure(
				MeasureSpec.makeMeasureSpec(surfaceWidth, MeasureSpec.EXACTLY), 
				MeasureSpec.makeMeasureSpec(surfaceHeight, MeasureSpec.EXACTLY));
		mSurfaceView.measure(
				MeasureSpec.makeMeasureSpec(surfaceWidth, MeasureSpec.EXACTLY), 
				MeasureSpec.makeMeasureSpec(surfaceHeight, MeasureSpec.EXACTLY));
		mWindowInfoText.setHeight(windowTextHeight);
		mArrowAddFrame.measure(widthMeasureSpec, heightMeasureSpec);
		mArrowSubFrame.measure(widthMeasureSpec, heightMeasureSpec);
		mRefresh.measure(
				MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED), 
				MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
	}
	
	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		if (!invalidateComponent()) {
			return;
		} 
		
		mWindowLayout.layout(0, 0, mWindowLayout.getMeasuredWidth(), 
				mWindowLayout.getMeasuredHeight());
		mPlaywindowFrame.layout(0, 0, mPlaywindowFrame.getMeasuredWidth(), 
				mPlaywindowFrame.getMeasuredHeight());
		mSurfaceView.layout(0, 0, mSurfaceView.getMeasuredWidth(), 
				mSurfaceView.getMeasuredHeight());	
		mWindowInfoText.layout(0, mPlaywindowFrame.getMeasuredHeight()-2, 
				mPlaywindowFrame.getMeasuredWidth(), getMeasuredHeight());
		mArrowAddFrame.layout(0, 0, mArrowAddFrame.getMeasuredWidth(), 
				mArrowAddFrame.getMeasuredWidth());
		mArrowSubFrame.layout(0, 0, mArrowSubFrame.getMeasuredWidth(), 
				mArrowSubFrame.getMeasuredWidth());
		int rLeft = (getMeasuredWidth() - mRefresh.getMeasuredWidth()) / 2;
		int rTop = (getMeasuredHeight() - mRefresh.getMeasuredHeight()) / 2;
		mRefresh.layout(rLeft, rTop, rLeft + mRefresh.getMeasuredWidth(), 
				rTop +  mRefresh.getMeasuredHeight());
	}
	
	/**
	 * Invalidate all component.
	 * @return
	 * 		<b>true</b>, all component is usable; 
	 * 		<b>false</b>, not all component is usable.
	 */
	private boolean invalidateComponent() {
		return mWindowLayout != null
				&& mPlaywindowFrame != null
				&& mSurfaceView != null
				&& mWindowInfoText != null
				&& mArrowAddFrame != null
				&& mArrowSubFrame != null
				&& mRefresh != null;
	}
	
//	/**
//	 * 获取一个新的连接，为避免Connection重复创建，可用工厂类产生对象
//	 * @return 初始化好的Connection对象
//	 */
//	private Connection obtainConnection() {
//		final Connection conn = new Connection();
//		if (mConnectionStatusListener != null) {
//			conn.SetConnectionListener(mConnectionStatusListener);
//		}
//		return conn;
//	}
	
	/**
	 * 是否已连接
	 * @return true, 已连接; false, 未连接或无连接存在
	 */
//	public boolean isConnected() {
//		return getConnection() != null 
//				&& getConnection().isConnected();
//	}
	
//	/**
//	 * 是否正在连接
//	 * @return true, 正在连接; false, 未处于连接中
//	 */
//	public boolean isConnecting() {
//		return getConnection() != null 
//				&& getConnection().isConnecting();
//	}
	
//	public void sendControlRequest(int cmdCode) {
//		final Connection conn = getConnection();
//		if (conn != null) {
//			conn.sendControlRequest(cmdCode);
//			Log.i(TAG, "Send Control Request... mItemIndex: " + mItemIndex + ", cmdcode: "
//					+ cmdCode);
//		}
//	}
//	
//	public void sendControlRequest(int cmdCode, int[] args) {
//		final Connection conn = getConnection();
//		if (conn != null) {
//			conn.sendControlRequest(cmdCode, args);
//
//			Log.i(TAG, "Send Control Request... mItemIndex: " + mItemIndex + ", cmdcode: "
//					+ cmdCode);
//		}
//	}
	
//	public void showArrowAnimation(int showPos) {
//		int i = 0;
//		
//		for (i = 0; i < mPTZImageViewArray.length; i++) {
//			final ImageView img = mPTZImageViewArray[i];
//			
//			if (i == showPos) {
//				img.setVisibility(View.VISIBLE);
//				((AnimationDrawable)img.getBackground()).start();
//				
//			} else {
//				img.setVisibility(View.INVISIBLE);
//				((AnimationDrawable)img.getBackground()).stop();
//			}
//		}
//	}	
//	
//	public void stopArrowAnimation() {
//		int i = 0;
//		
//		for (i = 0; i < mPTZImageViewArray.length; i++) {
//			mPTZImageViewArray[i].setVisibility(View.INVISIBLE);
//			((AnimationDrawable)mPTZImageViewArray[i].getBackground()).stop();
//		}
//	}
	
//	public void showFocalLengthAnimation(boolean isAddFocalLength) {
//		final ImageView[] imgArray = isAddFocalLength ? mAddFocalLengthArray : mSubFocalLengthArray;
//		final boolean _isAddFocalLength = isAddFocalLength;
//		int i = 0;
//		
//		Log.i(TAG, "imgArray.length: " + imgArray.length + ", isAddFocalLength: " + isAddFocalLength);
//		
//		if (_isAddFocalLength){
//			mArrowAddFrame.setVisibility(View.VISIBLE);
//		} else {
//			mArrowSubFrame.setVisibility(View.VISIBLE);
//		}
//		
//		for (i = 0; i < imgArray.length; i++) {
//			imgArray[i].setVisibility(View.VISIBLE);
//			((AnimationDrawable)imgArray[i].getBackground()).start();
//		}
//		
//		this.postDelayed(new Runnable() {
//			@Override
//			public void run() {
//				int j = 0;
//				for (j = 0; j < imgArray.length; j++) {
//					imgArray[j].setVisibility(View.INVISIBLE);
//					((AnimationDrawable)imgArray[j].getBackground()).stop();
//				}
//				
//				if (_isAddFocalLength){
//					mArrowAddFrame.setVisibility(View.GONE);
//				} else {
//					mArrowSubFrame.setVisibility(View.GONE);
//				}
//			}
//		}, 500);
//		
//	}
	
	public void setWindowInfoContent(int resid) {
		setWindowInfoContent(getString(resid));
	}

	public void setWindowInfoContent(String info) {
//		if (mIsResponseError) { 
//			return; // 若返回错误，则相应提示信息已更新，不进行覆盖
//		}
		
		final StringBuffer s;
		if (mDeviceRecordName != null && info != null) {
			s = new StringBuffer(mDeviceRecordName);
			s.append("[");
			s.append(info);
			s.append("]");
		} else {
			s = new StringBuffer("");
		}
		
		mWindowInfoText.post(new Runnable() {
			@Override
			public void run() {
				mWindowInfoText.setText(s.toString());
			}
		});
	}
	
	public void setWindowInfoText(final String s) {
//		if (mIsResponseError) { 
//			return; // 若返回错误，则相应提示信息已更新，不进行覆盖
//		}
		
		mWindowInfoText.post(new Runnable() {
			@Override
			public void run() {
				mWindowInfoText.setText(s);
			}
		});
	}
	
//	public void setValid(boolean valid) {
//		this.isValid = valid;
//	}
//	
//	public boolean isValid() {
//		return isValid;
//	}
	
	/**
	 * Reset the view for future use. It is equal to call {@link 
	 * #reset(boolean)} with <b>false</b>.
	 */
	public void reset() {
		reset(false);
	}
	
	/**
	 * Reset the view for future use. If force is false and the connection 
	 *  associated with this view is connected/connecting, then just set 
	 *  the flag.
	 * @param force Whether to force view to be discarded
	 */
	public void reset(boolean force) {
//		isValid = true;
		if (!force && (isConnected()
				|| isConnecting())) {
			return;
		}
		
		mWindowLayout.setWindowSelected(false);
		mSurfaceView.onContentReset();
		setWindowInfoContent(getString(R.string.connection_status_closed));
		mProgressBar.setVisibility(View.INVISIBLE);
		mRefresh.setVisibility(View.VISIBLE);
	}
	
	/**
	 * Let this view discarded. It is equal to call {@link #abort(boolean)}
	 * with <b>false</b>.
	 */
	public void abort() {
		abort(false);
	}
	
	/**
	 * Let this view discarded. If force is false and the connection
	 * associated with this view is connected/connecting, then just 
	 * set the flag.
	 * @param force Whether to force view to be discarded
	 */
	public void abort(boolean force) {
//		isValid = false;
		if (!force && (isConnected()
				|| isConnecting())) {
			return;
		}
		
		mWindowLayout.setWindowSelected(false);
		mSurfaceView.onContentReset();
		setWindowInfoContent(null);
		mProgressBar.setVisibility(View.INVISIBLE);
		mRefresh.setVisibility(View.GONE);
	}
	
	public void takePicture() {
		mSurfaceView.takePicture();
	}
	
	public void startMP4Record() {
//		if (isConnected()) {
			String fileName = LocalFileUtils.getFormatedFileName(
					getPlaybackItem().getDeviceRecordName(), getPlaybackItem()
							.getChannel());
			String fullRecPath = LocalFileUtils.getRecordFileFullPath(fileName, true);
			
			mRecordFileName = fileName;
			mCanStartRecord = false;
			mSurfaceView.setStartRecord(true);
			mSurfaceView.makeVideoSnapshot(fileName);
			mRecordFileHandler = MP4Recorder.createRecordFile(fullRecPath,
					mVideoConfig.getWidth(), mVideoConfig.getHeight(),
					mVideoConfig.getFramerate(), mVideoConfig.getSps()[1],
					mVideoConfig.getSps()[2], mVideoConfig.getSps()[3]);
			invalidate();
//		}
	}
	
	public void stopMP4Record() {
//		if (isConnected()) {
			mCanStartRecord = false;
			mSurfaceView.setStartRecord(false);
			MP4Recorder.closeRecordFile(mRecordFileHandler);
			invalidate();
			
			if (mRecordFileName != null) {
				String fullRecPath = LocalFileUtils.getRecordFileFullPath(mRecordFileName, true);
				File f = new File(fullRecPath);
				
				if (!f.exists()) { // 若视频太短，则视频文件不存在，不产生相应的快照及缩略图
					String fullImgPath = LocalFileUtils.getCaptureFileFullPath(mRecordFileName, true);
					String fullThumbPath = LocalFileUtils.getThumbnailsFileFullPath(mRecordFileName, true);
					File f1 = new File(fullImgPath);
					File f2 = new File(fullThumbPath);
					
					if (f1.exists()) {
						f1.delete();
					}
					if (f2.exists()) {
						f2.delete();
					}
					
				}
			}
//		}
	}
	
	public boolean isInRecording() {
		return MP4Recorder.isInRecording(mRecordFileHandler);
	}
	
	/**
	 * Whether the connection for video play back is connected. 
	 * @return true, the connection is connected.
	 * 		false, the connection is disconnected or connecting. 
	 */
	public boolean isConnected() {
		return false;
	}
	
	/**
	 * Whether the connection for video play back is connecting. 
	 * @return true, the connection is connecting;
	 * 		false, the connection is not in connecting status.
	 */
	public boolean isConnecting() {
		return false;
	}
	
	private String getString(int resId) {
		return getResources().getString(resId);
	}
	
	

	public static interface OnRefreshButtonClickListener extends View.OnClickListener {}
}
