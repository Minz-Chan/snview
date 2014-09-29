package com.starnet.snview.realplay;

import java.util.List;

import com.starnet.snview.R;
import com.starnet.snview.component.SurfaceViewMultiLayout;
import com.starnet.snview.component.SurfaceViewSingleLayout;
import com.starnet.snview.component.VideoPager;
import com.starnet.snview.component.liveview.LiveViewItemContainer;
import com.starnet.snview.component.liveview.LiveViewManager;
import com.starnet.snview.global.Constants;
import com.starnet.snview.global.GlobalApplication;
import com.starnet.snview.protocol.Connection;
import com.starnet.snview.protocol.Connection.StatusListener;
import com.starnet.snview.util.ClickEventUtils;
import com.starnet.snview.util.ClickEventUtils.OnActionListener;

import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.widget.FrameLayout;

public class VideoRegion extends FrameLayout {
	private static String TAG = "VideoRegion"; 
	
	private Context mContext;
	
	private VideoPager mPager;
	
	private PTZControl mPtzControl;
	private LiveControl mLiveControl;
	
	private LiveViewManager mLiveViewManager;
	private GestureDetector mGestureDetector;             // 定义手势检测器实例
    private ScaleGestureDetector mScaleGestureDetector;   // 缩放手势检测器实例
    
    private SurfaceViewSingleLayout mSurfaceSingleLayout;
	private SurfaceViewMultiLayout mSurfaceMultiLayout;
	
	private Handler mHandler;

	public VideoRegion(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}

	public VideoRegion(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public VideoRegion(Context context) {
		super(context);
		init(context);
	}
	
	private RealplayActivity getRP() {
		return (RealplayActivity) mContext;
	}
	
	private void init(Context context) {
		mContext = context;
		
		mPager = getRP().getPager();
		mPtzControl = getRP().getPtzControl();
		mLiveControl = getRP().getLiveControl();
		mLiveViewManager = getRP().getLiveviewManager();
		mHandler = getRP().getHandler();
		
		mGestureDetector =  new GestureDetector(mContext, new GestureListener(onGestureListener));
		mScaleGestureDetector = new ScaleGestureDetector(mContext, new ScaleGestureListener(onGestureListener));
		
		this.setOnTouchListener(mOnTouchListener);
		
		SurfaceViewSingleLayout svsl = new SurfaceViewSingleLayout(mContext);
		SurfaceViewMultiLayout svml = new SurfaceViewMultiLayout(mContext);
		
		svsl.setLiveviewRefreshButtonClickListener(onRefreshButtonClickListener);
		svml.setLiveviewRefreshButtonClickListener(onRefreshButtonClickListener);
		
		mSurfaceSingleLayout = svsl;
		mSurfaceMultiLayout = svml;
		
		this.addView(mSurfaceSingleLayout);
		this.addView(mSurfaceMultiLayout);
		
		if (mLiveViewManager.isMultiMode()) {
			showSingleOrMultiMode(false);
		} else {
			showSingleOrMultiMode(true);
		}
		
		//mLiveViewManager.setOnVideoModeChangedListener(onVideoModeChangedListener);
		//mLiveViewManager.setConnectionStatusListener(connectionStatusListener);
		
		
	}
	
	public SurfaceViewSingleLayout getSurfaceSingleLayout() {
		return mSurfaceSingleLayout;
	}
	
	public SurfaceViewMultiLayout getSurfaceMultiLayout() {
		return mSurfaceMultiLayout;
	}
	
	public boolean checkIsPTZDeviceConnected() {
		Connection conn = mLiveViewManager.getSelectedLiveView().getCurrentConnection();
		
		if (conn != null && conn.isConnected()) {
			return true;
		}
		
		return false;
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
	
	public boolean checkIfPreviewDeviceListEmpty() {
		return getRP().checkIfPreviewDeviceListEmpty();
	}
	
	public void showSingleOrMultiMode(Boolean isSingle) {
		if (isSingle == null) {
			mSurfaceSingleLayout.setVisibility(View.GONE);
			mSurfaceSingleLayout.getLiveview().getSurfaceView().setVisibility(View.GONE);
			mSurfaceSingleLayout.getLiveview().getRefreshImageView().setVisibility(View.GONE);
			mSurfaceMultiLayout.setVisibility(View.GONE);
			mSurfaceMultiLayout.getLiveviews().get(0).getSurfaceView().setVisibility(View.GONE);
			mSurfaceMultiLayout.getLiveviews().get(1).getSurfaceView().setVisibility(View.GONE);
			mSurfaceMultiLayout.getLiveviews().get(2).getSurfaceView().setVisibility(View.GONE);
			mSurfaceMultiLayout.getLiveviews().get(3).getSurfaceView().setVisibility(View.GONE);
			mSurfaceMultiLayout.getLiveviews().get(0).getRefreshImageView().setVisibility(View.GONE);
			mSurfaceMultiLayout.getLiveviews().get(1).getRefreshImageView().setVisibility(View.GONE);
			mSurfaceMultiLayout.getLiveviews().get(2).getRefreshImageView().setVisibility(View.GONE);
			mSurfaceMultiLayout.getLiveviews().get(3).getRefreshImageView().setVisibility(View.GONE);
			
			return;
		}
		
		if (isSingle) {
			mSurfaceMultiLayout.setVisibility(View.GONE);
			mSurfaceMultiLayout.getLiveviews().get(0).getSurfaceView().setVisibility(View.GONE);
			mSurfaceMultiLayout.getLiveviews().get(1).getSurfaceView().setVisibility(View.GONE);
			mSurfaceMultiLayout.getLiveviews().get(2).getSurfaceView().setVisibility(View.GONE);
			mSurfaceMultiLayout.getLiveviews().get(3).getSurfaceView().setVisibility(View.GONE);
			mSurfaceMultiLayout.getLiveviews().get(0).getRefreshImageView().setVisibility(View.GONE);
			mSurfaceMultiLayout.getLiveviews().get(1).getRefreshImageView().setVisibility(View.GONE);
			mSurfaceMultiLayout.getLiveviews().get(2).getRefreshImageView().setVisibility(View.GONE);
			mSurfaceMultiLayout.getLiveviews().get(3).getRefreshImageView().setVisibility(View.GONE);
			mSurfaceSingleLayout.setVisibility(View.VISIBLE);
			mSurfaceSingleLayout.getLiveview().getSurfaceView().setVisibility(View.VISIBLE);
		} else {
			mSurfaceSingleLayout.setVisibility(View.GONE);
			mSurfaceSingleLayout.getLiveview().getSurfaceView().setVisibility(View.GONE);
			mSurfaceSingleLayout.getLiveview().getRefreshImageView().setVisibility(View.GONE);
			mSurfaceMultiLayout.setVisibility(View.VISIBLE);
			mSurfaceMultiLayout.getLiveviews().get(0).getSurfaceView().setVisibility(View.VISIBLE);
			mSurfaceMultiLayout.getLiveviews().get(1).getSurfaceView().setVisibility(View.VISIBLE);
			mSurfaceMultiLayout.getLiveviews().get(2).getSurfaceView().setVisibility(View.VISIBLE);
			mSurfaceMultiLayout.getLiveviews().get(3).getSurfaceView().setVisibility(View.VISIBLE);
			mSurfaceMultiLayout.getLiveviews().get(0).getRefreshImageView().setVisibility(View.GONE);
			mSurfaceMultiLayout.getLiveviews().get(1).getRefreshImageView().setVisibility(View.GONE);
			mSurfaceMultiLayout.getLiveviews().get(2).getRefreshImageView().setVisibility(View.GONE);
			mSurfaceMultiLayout.getLiveviews().get(3).getRefreshImageView().setVisibility(View.GONE);
		}
	}
	
	private LiveViewItemContainer.OnRefreshButtonClickListener onRefreshButtonClickListener = new LiveViewItemContainer.OnRefreshButtonClickListener() {

		@Override
		public void onClick(View v) {
			Log.i(TAG, "OnRefreshClick, " + v);
			if (checkIfPreviewDeviceListEmpty()) {
				return;
			}

			LiveViewItemContainer c = findVideoContainerByView(v);

			Log.i(TAG, "OnRefreshClick, c:" + c);

			if (c != null) {
				int pos = mLiveViewManager.getIndexOfLiveView(c);
				int page = mLiveViewManager.getCurrentPageNumber();
				int index = (page - 1) * mLiveViewManager.getPageCapacity()
						+ pos;

				mLiveViewManager.tryPreview(index);
			}

		}
	};
	
	// 单通道/多通道模式切换 事件
	private LiveViewManager.OnVideoModeChangedListener onVideoModeChangedListener = new LiveViewManager.OnVideoModeChangedListener() {

		@Override
		public void OnVideoModeChanged(boolean isMultiMode) {
			// mVideoRegion.removeAllViews();
			mLiveViewManager.clearLiveView();

			Log.i(TAG, "VideoRegion, width: " + VideoRegion.this.getWidth()
					+ ", height: " + VideoRegion.this.getHeight());

			List<PreviewDeviceItem> devices = mLiveViewManager.getDeviceList();
			if (devices == null || devices.size() == 0) {
				return;
			}

			if (isMultiMode) { // 多通道模式
				showSingleOrMultiMode(false);

				List<LiveViewItemContainer> l = mSurfaceMultiLayout
						.getLiveviews();
				for (int i = 0; i < l.size(); i++) {
					mLiveViewManager.addLiveView(l.get(i));
				}
			} else { // 单通道模式
				showSingleOrMultiMode(true);

				mLiveViewManager
						.addLiveView(mSurfaceSingleLayout.getLiveview());
			}

			getRP().onContentChanged();

		}

	};
	
	private StatusListener connectionStatusListener = new StatusListener() {

		@Override
		public void OnConnectionTrying(View v) {
			final LiveViewItemContainer c = (LiveViewItemContainer) v;

			c.setWindowInfoContent(getRP().getString(R.string.connection_status_connecting));

			// updateProgressbarStatus(c.getProgressBar(), true);
			mHandler.post(new Runnable() {
				@Override
				public void run() {
					if (c != null) {
						c.getRefreshImageView().setVisibility(View.GONE);
						c.getProgressBar().setVisibility(View.VISIBLE);
						c.getRefreshImageView().setVisibility(View.INVISIBLE); // 若使用View.GONE会导致部分情况下ProgressBar消失

						Log.i(TAG, "ProgressBar@" + c.getProgressBar()
								+ ", visible");
					}
				}
			});

			//bIsPlaying = true;
			getRP().setIsPlaying(true);
			mHandler.post(new Runnable() {
				@Override
				public void run() {
					getRP().updatePlayStatus(true);
				}
			});
		}

		@Override
		public void OnConnectionFailed(View v) {
			Log.i(TAG, "OnConnectionFailed");
			final LiveViewItemContainer c = (LiveViewItemContainer) v;

			c.setWindowInfoContent(getRP().getString(R.string.connection_status_failed));

			mHandler.post(new Runnable() {
				@Override
				public void run() {
					if (c != null) {
						c.getProgressBar().setVisibility(View.INVISIBLE);
						c.getRefreshImageView().setVisibility(View.VISIBLE);
					}
				}
			});

			boolean isAllVideoClosed = true;
			List<LiveViewItemContainer> liveviews = mLiveViewManager
					.getListviews();
			for (int i = 0; i < liveviews.size(); i++) {
				LiveViewItemContainer lv = liveviews.get(i);

				if (lv.getCurrentConnection() != null
						&& (lv.getCurrentConnection().isConnected() || lv
								.getCurrentConnection().isConnecting())) {
					isAllVideoClosed = false;
				}
			}

			if (isAllVideoClosed) {
				//bIsPlaying = false;
				getRP().setIsPlaying(false);
				mHandler.post(new Runnable() {
					@Override
					public void run() {
						getRP().updatePlayStatus(false);
					}
				});

			}

		}

		@Override
		public void OnConnectionEstablished(View v) {
			final LiveViewItemContainer c = (LiveViewItemContainer) v;

			c.setWindowInfoContent(getRP().getString(R.string.connection_status_established));
		}

		@Override
		public void OnConnectionBusy(View v) {

			final LiveViewItemContainer c = (LiveViewItemContainer) v;

			if (c.isManualStop()) {
				return;
			}

			// updateProgressbarStatus(c.getProgressBar(), false);
			mHandler.post(new Runnable() {
				@Override
				public void run() {
					if (c != null) {
						c.getProgressBar().setVisibility(View.INVISIBLE);
						c.getRefreshImageView().setVisibility(View.GONE);
						Log.i(TAG, "ProgressBar@" + c.getProgressBar()
								+ ", invisible");
					}
				}
			});
		}

		@Override
		public void OnConnectionClosed(View v) {
			final LiveViewItemContainer c = (LiveViewItemContainer) v;
			int currPageCount = mLiveViewManager.getCurrentPageCount();
			int index = mLiveViewManager.getIndexOfLiveView(c);

			if (index > currPageCount || c.isManualStop()) {
				return;
			}

			c.setWindowInfoContent(getRP().getString(R.string.connection_status_closed));

			mHandler.post(new Runnable() {
				@Override
				public void run() {
					if (c != null) {
						c.getProgressBar().setVisibility(View.INVISIBLE);
						c.getRefreshImageView().setVisibility(View.VISIBLE);

					}
				}
			});

		}

	};
	
	
	private ClickEventUtils mPTZStopMoveDelay = new ClickEventUtils(new OnActionListener() {
		@Override
		public void OnAction(int clickCount, Object... params) {
			mPtzControl.getPtzReqSender().stopMove();		
			mLiveViewManager.getSelectedLiveView().stopArrowAnimation();
		}
	}, 300);
	
	
	private boolean mIsScaleOperator = false;
	private OnTouchListener mOnTouchListener = new OnTouchListener() {
		@Override
		public boolean onTouch(View v, MotionEvent event) {
			Log.i(TAG, "mVideoRegion, onTouch");
			
			int action = event.getActionMasked();
			
			/* 若检测到多点事件，则将这以后的事件从mGestureDetector中过滤
			 * 即若检测到缩放事件，则余下事件仅交由mScaleGestureDetector处理
			 */
			if (action == MotionEvent.ACTION_POINTER_DOWN) {
				mIsScaleOperator = true;  
			}
			
			/*
			if (event.getActionMasked() == MotionEvent.ACTION_POINTER_UP) {
				mIsScaleOperator = false;
			}*/
			
			Log.i(TAG, "onTouch(), mIsPTZModeOn:" + mPtzControl.isPTZModeOn() + ", mIsScaleOperator:" + mIsScaleOperator);
			
			if (mIsScaleOperator) {
				if (action == MotionEvent.ACTION_UP) {
					mIsScaleOperator = false;
				}
				
				return mScaleGestureDetector.onTouchEvent(event);
			} else {
				boolean r1 = mScaleGestureDetector.onTouchEvent(event);
				boolean r2 = mGestureDetector.onTouchEvent(event);
				
				if (mPtzControl.isPTZModeOn() && !mPtzControl.isFlingAction() && action == MotionEvent.ACTION_UP) {
					onGestureListener.onSlidingMoveUp();
				} 
				
				return r1 || r2;
			}
		}	
	};
	
	private OnGestureListener onGestureListener = new OnGestureListener() {

		@Override
		public void onSingleClick(MotionEvent e) {
			Log.i(TAG, "On single click");

			if (mLiveViewManager.getPager() == null) {
				return;
			}

			int pos;

			if (mLiveViewManager.getPageCapacity() == 1) {
				pos = 1;
			} else {
				pos = getIndexOfLiveview(e.getX(), e.getY());
			}

			if ((mPtzControl.isPTZModeOn() && checkIsPTZDeviceConnected())
			/* || !liveViewManager.isMultiMode() */) { // PTZ模式情况下或单通道模式单击无效
				return;
			}

			int index = (mLiveViewManager.getCurrentPageNumber() - 1)
					* mLiveViewManager.getPageCapacity() + pos;

			Log.i(TAG, "single_click, getX:" + e.getX() + ", getY:" + e.getY()
					+ ", pos:" + pos + ", index:" + index);

			if (index > mLiveViewManager.getLiveViewTotalCount()) {
				return; // 非有效通道，不作处理
			}

			int oldPos = mLiveViewManager.getCurrentSelectedLiveViewPosition();

			mLiveViewManager.setCurrenSelectedLiveViewtIndex(index); // 变更当前选择索引

			mLiveViewManager.selectLiveView(index);

			mPager.setNum(mLiveViewManager.getSelectedLiveViewIndex());
			mPager.setAmount(mLiveViewManager.getLiveViewTotalCount());

			if (GlobalApplication.getInstance().isIsFullMode()) { // 若为横屏模式，则进行相应工具条显示/隐藏控制
				if (pos == oldPos) {
					if (mLiveControl.getLandscapeToolbar().isLandToolbarShow()) {
						mLiveControl.getLandscapeToolbar()
								.hideLandscapeToolbar();
					} else {
						mLiveControl.getLandscapeToolbar()
								.showLandscapeToolbar();
					}
				} else {
					mLiveControl.getLandscapeToolbar().showLandscapeToolbar();
				}
			}
		}

		@Override
		public void onDoubleClick(MotionEvent e) {
			if (mPtzControl.isPTZModeOn() && checkIsPTZDeviceConnected()) { // PTZ模式情况下双击无效
				return;
			}

			Log.i(TAG, "On Double click");

			if (mLiveViewManager.getPager() == null) {
				return;
			}

			int pos;

			if (mLiveViewManager.getPageCapacity() == 1) {
				pos = 1;
			} else {
				pos = getIndexOfLiveview(e.getX(), e.getY());
			}

			int index = (mLiveViewManager.getCurrentPageNumber() - 1)
					* mLiveViewManager.getPageCapacity() + pos;

			Log.i(TAG, "double_click, getX:" + e.getX() + ", getY:" + e.getY()
					+ ", pos:" + pos + ", index:" + index);

			if (index > mLiveViewManager.getLiveViewTotalCount()) {
				return; // 非有效通道，不作处理
			}

			mLiveViewManager.setCurrenSelectedLiveViewtIndex(index); // 变更当前选择索引

			if (mLiveViewManager.isMultiMode()) { // 切换到单通道模式
				if (checkIsPTZDeviceConnected()) {
					mLiveViewManager.prestoreConnectionByPosition(pos);
					mLiveViewManager.setMultiMode(false);
					mLiveViewManager.transferVideoWithoutDisconnect(pos);
				} else {
					mLiveViewManager.closeAllConnection(false);
					mLiveViewManager.setMultiMode(false);
					mLiveViewManager.preview(index);
				}

				mPtzControl.setIsEnterPTZInSingleMode(true);
			} else { // 切换到多通道模式
				int currPageStart;
				int currPageEnd;

				if (checkIsPTZDeviceConnected()) { // 若当前通道为连接状态，则切换时保持当前连接
					mLiveViewManager.prestoreConnectionByPosition(pos);
					mLiveViewManager.setMultiMode(true);

					currPageStart = (mLiveViewManager.getCurrentPageNumber() - 1) * 4 + 1;
					currPageEnd = (mLiveViewManager.getCurrentPageNumber() - 1)
							* 4 + mLiveViewManager.getCurrentPageCount();
					mLiveViewManager.preview(currPageStart, currPageEnd
							- currPageStart + 1, index);
				} else { // 若当前通道为非连接状态，则关闭所有连接
					mLiveViewManager.closeAllConnection(false);
					mLiveViewManager.setMultiMode(true);

					currPageStart = (mLiveViewManager.getCurrentPageNumber() - 1) * 4 + 1;
					currPageEnd = (mLiveViewManager.getCurrentPageNumber() - 1)
							* 4 + mLiveViewManager.getCurrentPageCount();
					mLiveViewManager.preview(currPageStart, currPageEnd
							- currPageStart + 1);
				}

				// 若发现此时PTZ模式开启，则重围PTZ模式，即退出PTZ模式
				if (mPtzControl.isPTZModeOn()) {
					mPtzControl.showPTZBar(false);
				}

				mPtzControl.setIsEnterPTZInSingleMode(false);
			}

			mLiveViewManager.selectLiveView(index);

			mPager.setNum(mLiveViewManager.getSelectedLiveViewIndex());
			mPager.setAmount(mLiveViewManager.getLiveViewTotalCount());
		}

		private int getIndexOfLiveview(float x, float y) {
			int videoWidth, videoHeight;

			if (GlobalApplication.getInstance().isIsFullMode()) {
				videoWidth = GlobalApplication.getInstance().getScreenWidth();
				videoHeight = GlobalApplication.getInstance().getScreenHeight();
			} else {
				videoWidth = GlobalApplication.getInstance().getScreenWidth();
				videoHeight = GlobalApplication.getInstance().getScreenWidth();
			}

			Log.i(TAG, "videoWidth:" + videoWidth + ", videoHeight:"
					+ videoHeight);

			return (x < videoWidth / 2) ? (y < videoHeight / 2 ? 1 : 3) // 触点属于第几个视频区域
					: (y < videoHeight / 2 ? 2 : 4);
		}

		@Override
		public void onSlidingLeft() {
			Log.i(TAG,
					"onSlidingLeft(), mIsPTZModeOn:" + mPtzControl.isPTZModeOn()
							+ " mPtzControl:" + mPtzControl.getPtzReqSender()
							+ " mIsScaleOperator:" + mIsScaleOperator);

			if (!mPtzControl.isPTZModeOn()) { // 向左滑屏
				if (mLiveViewManager.getPager() != null) {
					//mVideoRegion.startAnimation(mVideoSwitchAnim);

//					mLiveViewManager.nextPage();
//
//					mPager.setNum(mLiveViewManager.getSelectedLiveViewIndex());
//					mPager.setAmount(mLiveViewManager.getLiveViewTotalCount());
//
//					if (GlobalApplication.getInstance().isIsFullMode()) { // 若为横屏模式，且分页器显示，则刷新分页器
//						if (mLiveControl.getLandscapeToolbar()
//								.isLandToolbarShow()) {
//							mLiveControl.getLandscapeToolbar()
//									.showLandscapeToolbar();
//						}
//					}
				}
			} else { // PTZ, 向左
				Log.i(TAG, "PTZ Action");
				mLiveViewManager.getSelectedLiveView().showArrowAnimation(
						Constants.ARROW.LEFT);

				if (mPtzControl.getPtzReqSender() != null) {
					Log.i(TAG, "PTZ Action -----> left");
					mPtzControl.getPtzReqSender().moveLeft();
				}
			}

		}

		@Override
		public void onSlidingRight() {
			Log.i(TAG,
					"onSlidingRight(), mIsPTZModeOn:"
							+ mPtzControl.isPTZModeOn() + " mPtzControl:"
							+ mPtzControl.getPtzReqSender()
							+ " mIsScaleOperator:" + mIsScaleOperator);
			if (!mPtzControl.isPTZModeOn()) { // 向右滑屏
				if (mLiveViewManager.getPager() != null) {
					//mVideoRegion.startAnimation(mVideoSwitchAnim);

//					mLiveViewManager.previousPage();
//
//					mPager.setNum(mLiveViewManager.getSelectedLiveViewIndex());
//					mPager.setAmount(mLiveViewManager.getLiveViewTotalCount());
//
//					if (GlobalApplication.getInstance().isIsFullMode()) { // 若为横屏模式，且分页器显示，则刷新分页器
//						if (mLiveControl.getLandscapeToolbar()
//								.isLandToolbarShow()) {
//							mLiveControl.getLandscapeToolbar()
//									.showLandscapeToolbar();
//						}
//					}
				}
			} else { // PTZ, 向右
				Log.i(TAG, "PTZ Action");
				mLiveViewManager.getSelectedLiveView().showArrowAnimation(
						Constants.ARROW.RIGHT);

				if (mPtzControl.getPtzReqSender() != null) {
					Log.i(TAG, "PTZ Action -----> right");
					mPtzControl.getPtzReqSender().moveRight();
				}
			}

		}

		@Override
		public void onSlidingLeftUp() {
			mLiveViewManager.getSelectedLiveView().showArrowAnimation(
					Constants.ARROW.LEFT_UP);

			/*
			 * if (ptzControl != null) { ptzControl.moveLeft();
			 * ptzControl.moveUp(); }
			 */
		}

		@Override
		public void onSlidingLeftDown() {
			mLiveViewManager.getSelectedLiveView().showArrowAnimation(
					Constants.ARROW.LEFT_DOWN);

			/*
			 * if (ptzControl != null) { ptzControl.moveLeft();
			 * ptzControl.moveDown(); }
			 */
		}

		@Override
		public void onSlidingRightUp() {
			mLiveViewManager.getSelectedLiveView().showArrowAnimation(
					Constants.ARROW.RIGHT_UP);

			/*
			 * if (ptzControl != null) { ptzControl.moveRight();
			 * ptzControl.moveUp(); }
			 */
		}

		@Override
		public void onSlidingRightDown() {
			mLiveViewManager.getSelectedLiveView().showArrowAnimation(
					Constants.ARROW.RIGHT_DOWN);

			/*
			 * if (ptzControl != null) { ptzControl.moveRight();
			 * ptzControl.moveDown(); }
			 */
		}

		@Override
		public void onSlidingUp() {
			Log.i(TAG, "PTZ Action");
			mLiveViewManager.getSelectedLiveView().showArrowAnimation(
					Constants.ARROW.UP);

			if (mPtzControl.getPtzReqSender() != null) {
				Log.i(TAG, "PTZ Action -----> up");
				mPtzControl.getPtzReqSender().moveUp();
			}
		}

		@Override
		public void onSlidingDown() {
			Log.i(TAG, "PTZ Action");
			mLiveViewManager.getSelectedLiveView().showArrowAnimation(
					Constants.ARROW.DOWN);

			if (mPtzControl.getPtzReqSender() != null) {
				Log.i(TAG, "PTZ Action -----> down");
				mPtzControl.getPtzReqSender().moveDown();
			}
		}

		@Override
		public void onSlidingMoveUp() {
			Log.i(TAG, "PTZ Action, onSlidingMoveUp");

			mLiveViewManager.getSelectedLiveView().stopArrowAnimation();

			if (mPtzControl.getPtzReqSender() != null) {
				mPtzControl.getPtzReqSender().stopMove();
			}

			// mIsPTZInMoving = false;
			mPtzControl.setIsPTZInMoving(false);

		}

		@Override
		public void onZoomIn() {
			Log.i(TAG, "PTZ Action");
			// ToastUtils.show(RealplayActivity.this, "手势放大");
			mLiveViewManager.getSelectedLiveView()
					.showFocalLengthAnimation(true);

			if (mPtzControl.getPtzReqSender() != null) {
				Log.i(TAG, "PTZ Action -----> focal length increase");
				mPtzControl.getPtzReqSender().focalLengthIncrease();
			}

			mPTZStopMoveDelay.makeContinuousClickCalledOnce(this.hashCode(),
					new Object());

		}

		@Override
		public void onZoomOut() {
			Log.i(TAG, "PTZ Action");
			// ToastUtils.show(RealplayActivity.this, "手势缩小");
			mLiveViewManager.getSelectedLiveView().showFocalLengthAnimation(
					false);
			if (mPtzControl.getPtzReqSender() != null) {
				Log.i(TAG, "PTZ Action -----> focal length decrease");
				mPtzControl.getPtzReqSender().focalLengthDecrease();
			}

			mPTZStopMoveDelay.makeContinuousClickCalledOnce(this.hashCode(),
					new Object());
		}

		@Override
		public void onFling(int horizontalOffsetFlag, int vertitalOffsetFlag) {
			int h = horizontalOffsetFlag;
			int v = vertitalOffsetFlag;

			// 上下左右四个方向抛手势默认延时300ms后发送STOP_MOVE控制指令
			switch (h) {
			case -1:
				if (v == 0) {
					mPtzControl.setIsFlingAction(true);
					onSlidingLeft();
				}
				break;
			case 0:
				if (v == -1) {
					mPtzControl.setIsFlingAction(true);
					onSlidingUp();
				} else if (v == 1) {
					mPtzControl.setIsFlingAction(true);
					onSlidingDown();
				}
			case 1:
				if (v == 0) {
					mPtzControl.setIsFlingAction(true);
					onSlidingRight();
				}
				break;
			}

			VideoRegion.this.postDelayed(new Runnable() {
				@Override
				public void run() {
					mPtzControl.setIsFlingAction(false);
					onSlidingMoveUp();
				}

			}, 300);
		}
	};
	
	private class GestureListener extends SimpleOnGestureListener  
    {  	
	    final int FLIP_DISTANCE = 50;  //定义手势动作两点之间的最小距离
	    //private boolean mIsDoubleClick = false;
	    
	    private OnGestureListener mGestureListener;
		
		public GestureListener(OnGestureListener listener) {
			if (listener == null) {
        		throw new NullPointerException("Error parameter, listener is null");
        	}
			
			this.mGestureListener = listener;
		}
  
		@Override  
        public boolean onSingleTapUp(MotionEvent e)  
        {  
            
            return super.onSingleTapUp(e);  
        }  
		
		
		
        @Override
		public boolean onSingleTapConfirmed(MotionEvent e) {
        	if (mGestureListener != null) {
				mGestureListener.onSingleClick(e);
			}
        	
			return true;
		}

		@Override  
        public boolean onDoubleTap(MotionEvent e)  
        {   
            Log.i("TEST", "onDoubleTap");  
            final MotionEvent _e = e;
            
            //mIsDoubleClick = true;
            
            if (mGestureListener != null) {
				mGestureListener.onDoubleClick(_e);;
			}
            
            return true;  
        }  
        
        
  
        @Override  
        public boolean onDown(MotionEvent e)  
        {   
            Log.i("TEST", "onDown");
            return true;
        }  
  
        @Override  
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,  
                float velocityY)  
        {  
        	float a0 = e1.getX();
        	float a1 = e2.getX();
        	float b0 = e1.getY();
        	float b1 = e2.getY();
        	
        	int h = Math.abs(a1 - a0) > FLIP_DISTANCE ? (a1 - a0 > 0 ? 1 : -1) : 0;  // -1:左；0：水平无滑动；1：右
        	int v = Math.abs(b1 - b0) > FLIP_DISTANCE ? (b1 - b0 > 0 ? 1 : -1) : 0;  // -1：上；0：垂直无滑动；1：下

        	if (!mPtzControl.isPTZModeOn()) { // 非PTZ模式， 即左右滑屏
        		if (h == -1) {
        			// 为flipper设置切换的的动画效果
        			//mFlipper.setInAnimation(animations[0]);
        			//mFlipper.setOutAnimation(animations[1]);
        			//mFlipper.showPrevious();
        			
        			//ToastUtils.show(RealplayActivity.this, "向左滑动");
        			mGestureListener.onSlidingLeft();
        		} else if (h == 1) {
        			// 为flipper设置切换的的动画效果
        			//mFlipper.setInAnimation(animations[2]);
        			//mFlipper.setOutAnimation(animations[3]);
        			//mFlipper.showNext();
        			
        			//ToastUtils.show(RealplayActivity.this, "向右滑动");
        			mGestureListener.onSlidingRight();
        		}
        		
        		return true;
        	} else {  // PTZ模式
        		
        		mGestureListener.onFling(h, v);    		
        	}
    		
    		return false;  
        }  
  
        @Override  
        public void onLongPress(MotionEvent e)  
        {  
            Log.i("TEST", "onLongPress");  
            super.onLongPress(e);  
        }  
  
        @Override  
        public boolean onScroll(MotionEvent e1, MotionEvent e2,  
                float distanceX, float distanceY)  
        {  
            Log.i("TEST", "onScroll:distanceX = " + distanceX + " distanceY = " + distanceY);
            
            Log.i(TAG, "mIsPTZInMoving: " + mPtzControl.isPTZInMoving());
            
            /*
             * 目前服务端不支持连续发送 _OWSP_ACTIONCode事件，故若侦测到已发送云台移动事件
             * 则需等待 OWSP_ACTION_MD_STOP事件后方可再次发送
             */
            if (mPtzControl.isPTZInMoving()) {
            	return true;
            }
            
            float a0 = e1.getX();
        	float b0 = e1.getY();
        	float a1 = e2.getX();
        	float b1 = e2.getY();
        	
        	int h = Math.abs(a1 - a0) > FLIP_DISTANCE ? (a1 - a0 > 0 ? 1 : -1) : 0;  // -1:左；0：水平无滑动；1：右
        	int v = Math.abs(b1 - b0) > FLIP_DISTANCE ? (b1 - b0 > 0 ? 1 : -1) : 0;  // -1：上；0：垂直无滑动；1：下
            
        	if (!mPtzControl.isPTZModeOn() || !checkIsPTZDeviceConnected()) { // 若设备处于未连接或断开状态，则不启用云台控制手势        			
    			return true;
    		}
    		
    		Log.i(TAG, "h: " + h + ", v: " + v);
        	
//    		mIsPTZInMoving = true;
    		mPtzControl.setIsPTZInMoving(true);
    		
    		switch (h) {
    		case -1:
    			if (v == -1) {
    				mGestureListener.onSlidingLeftUp();
    			} else if (v == 0) {
    				mGestureListener.onSlidingLeft();
    			} else if (v == 1) {
    				mGestureListener.onSlidingLeftDown();
    			}
    			break;
    		case 0:
    			if (v == -1) {
    				mGestureListener.onSlidingUp();
    			} else if (v == 0) {
//    				mIsPTZInMoving = false;
    				mPtzControl.setIsPTZInMoving(false);
    			} else if (v == 1) {
    				mGestureListener.onSlidingDown();;
    			}
    			break;
    		case 1:
    			if (v == -1) {
    				mGestureListener.onSlidingRightUp();
    			} else if (v == 0) {
    				mGestureListener.onSlidingRight();
    			} else if (v == 1) {
    				mGestureListener.onSlidingRightDown();
    			}
    			break;
    		}
    		
    		
            
            return super.onScroll(e1, e2, distanceX, distanceY);  
        }  
    }
	
	private class ScaleGestureListener implements ScaleGestureDetector.OnScaleGestureListener {
		private float scaleFactorSum = 0;
		private int count = 0;
		
		private OnGestureListener mGestureListener;
		
		public ScaleGestureListener(OnGestureListener listener) {
			if (listener == null) {
        		throw new NullPointerException("Error parameter, listener is null");
        	}
			
			this.mGestureListener = listener;
		}
		
		@Override
		public boolean onScale(ScaleGestureDetector detector) {
			//scaleFactor = detector.getScaleFactor();
			
			scaleFactorSum += detector.getScaleFactor();
			count++;
			
			Log.i(TAG, "onScale, currentSpan:" + detector.getCurrentSpan()
					+ ", previousSpan:" + detector.getPreviousSpan());
			Log.i(TAG, "onScale, scaleFactor:" + detector.getScaleFactor());
			
			return true;
		}

		@Override
		public boolean onScaleBegin(ScaleGestureDetector detector) {
			return true;
		}

		@Override
		public void onScaleEnd(ScaleGestureDetector detector) {
			if ((mPtzControl.isPTZModeOn() && !checkIsPTZDeviceConnected())
					|| !mPtzControl.isPTZModeOn()) { // 未启用PTZ模式或未连接状态均不触发
				return;
			}
			
			float avg = scaleFactorSum / count; // 求均值确定放大还是缩小操作
			
			if (avg > 1) {
				mGestureListener.onZoomIn();
			} else {
				mGestureListener.onZoomOut();
			}
			
			scaleFactorSum = 0;
			count = 0;
			
		}
		
	}

	
	public static interface OnGestureListener {
		public void onSingleClick(MotionEvent e);
		public void onDoubleClick(MotionEvent e);
		public void onSlidingLeft();
		public void onSlidingLeftUp();
		public void onSlidingLeftDown();
		public void onSlidingRight();
		public void onSlidingRightUp();
		public void onSlidingRightDown();
		public void onSlidingUp();
		public void onSlidingDown();
		public void onSlidingMoveUp();
		public void onFling(int horizontalOffsetFlag, int verticalOffsetFlag);
		public void onZoomIn();
		public void onZoomOut();
	}
	
}
