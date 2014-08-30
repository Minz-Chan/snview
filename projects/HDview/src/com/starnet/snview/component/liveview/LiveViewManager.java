package com.starnet.snview.component.liveview;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.content.Context;
import android.util.Log;
import android.view.View;

import com.starnet.snview.protocol.Connection;
import com.starnet.snview.protocol.Connection.StatusListener;
import com.starnet.snview.realplay.PreviewDeviceItem;
import com.starnet.snview.util.ClickEventUtils;

public class LiveViewManager implements ClickEventUtils.OnActionListener {
	private static final String TAG = "LiveViewManager";
	
	private List<LiveViewItemContainer> liveviews; // 至多4个
	private List<Connection> connections;          // 对应liveviews
	
	private List<PreviewDeviceItem> devices;  // 预览设备列表
	
	private int showViewCount = 0;  // [0, 4]
	private int devicesCount;
	private int currentIndex;  // 当前LiveViewItemContainer在设备总数中的位置，从1开始
	
	private Boolean isMultiMode; // 是否支持多画面显示
	
	private ExecutorService executor;
	
	private Context context;
	
	private OnVideoModeChangedListener onVideoModeChangedListener;
	
	private StatusListener connectionStatusListener;
	
	
	private Pager pager;
	
	private ClickEventUtils callEventUtil;
	
	public LiveViewManager(Context context) {
		this.context = context;
		
		this.liveviews = new ArrayList<LiveViewItemContainer>();
		this.connections = new ArrayList<Connection>();
		
		isMultiMode = null;
		
		executor = Executors.newFixedThreadPool(4);
		callEventUtil = new ClickEventUtils(this);
	}
	
	public List<PreviewDeviceItem> getDeviceList() {
		return devices;
	}
	
	
	public void setDeviceList(List<PreviewDeviceItem> devices) {
		this.devices = devices;
		devicesCount = devices.size();
		
		pager = null;		
		pager = new Pager(devicesCount, 4);
	}
	
	
	
	public int getShowViewCount() {
		return showViewCount;
	}



	public void setShowViewCount(int showViewCount) {
		this.showViewCount = showViewCount;
	}

	

	public void setOnVideoModeChangedListener(
			OnVideoModeChangedListener onVideoModeChangedListener) {
		this.onVideoModeChangedListener = onVideoModeChangedListener;
	}
	
	public void setConnectionStatusListener(StatusListener listener) {
		this.connectionStatusListener = listener;
	}

	public Pager getPager() {
		return pager;
	}
	
	public boolean isMultiMode() {
		return isMultiMode;
	}

	public void invalidateLiveViews() {
		int i;
		
		for (i = 0; i < liveviews.size(); i++) {
			liveviews.get(i).getSurfaceView().invalidate();
		}
	}

	/**
	 * 切换预览模式
	 * @param isMultiMode true，多通道预览柜式； false，单画面预览模式 ；null, 页面不作初始化
	 */
	public void setMultiMode(Boolean isMultiMode) {
		if ((this.isMultiMode != null && this.isMultiMode.booleanValue() == isMultiMode)
				|| isMultiMode == null) {
			return;
		}
		
		this.isMultiMode = isMultiMode;
		
		if (onVideoModeChangedListener != null) {
			onVideoModeChangedListener.OnVideoModeChanged(isMultiMode);
		}
		
		switchPageMode();
	}

	private void switchPageMode() {
		int total = pager.getTotalCount();
		int pageCapacity = pager.getPageCapacity();
		int index = pager.getCurrentIndex();
		
		if (isMultiMode) { // 多通道模式
			if (pageCapacity != 4) {
				pager = null;
				pager = new Pager(total, 4, index);
			}
		} else { // 单多通道模式
			if (pageCapacity != 1) {
				pager = null;
				pager = new Pager(total, 1, index);
			}
		}
		
	}
	
	public void setCurrenSelectedLiveViewtIndex(int index) {
		pager.setCurrentIndex(index);
	}
	
	public LiveViewItemContainer getSelectedLiveView() {
		int currIndex = getSelectedLiveViewIndex();
		int capacity = getPageCapacity();
		int pos = ((currIndex % capacity) == 0) ? capacity : (currIndex % capacity);
		
		if (pos <= liveviews.size()) {
			return liveviews.get(pos - 1);
		}
		
		return null;
	}
	
	public int getSelectedLiveViewIndex() {
		return pager.getCurrentIndex();
	}
	
	public int getCurrentPageNumber() {
		return pager.getCurrentPage();
	}
	
	public int getCurrentPageCount() {
		return pager.getCurrentPageCount();
	}
	
	public int getPageCapacity() {
		return pager.getPageCapacity();
	}
	
	public int getLiveViewTotalCount() {
		return pager.getTotalCount();
	}
	
	public void sendControlRequest(int cmdCode) {
		int index = pager.getCurrentIndex();
		int capacity = pager.getPageCapacity();
		int pos = ((index % capacity) == 0) ? capacity : (index % capacity);
		
		if (connections.get(pos - 1) != null) {
			connections.get(pos - 1).sendControlRequest(cmdCode);
			
			Log.i(TAG, "Send Control Request... pos: " + pos + ", cmdcode: " + cmdCode);
		}
	}
	
	
	/**
	 * 切换通道组至下一页，并根据下一页包含的通道个数进行预览操作。若下一页为当前页，则不执行任何操作。
	 */
	public synchronized void nextPage() {
		if ( pager == null || (pager.getTotalCount() <= pager.getPageCapacity())) {
			return;
		}
		
		pager.nextPage();
		
		callEventUtil.makeContinuousClickCalledOnce(0);
	}
	
	/**
	 * 切换通道组至上一页，并根据上一页包含的通道个数进行预览操作。若上一页为当前页，则不执行任何操作。
	 */
	public synchronized void previousPage() {
		if ( pager == null || pager.getTotalCount() <= pager.getPageCapacity()) {
			return;
		}

		pager.previousPage();
		
		callEventUtil.makeContinuousClickCalledOnce(0);
	}
	
	@Override
	public void OnAction(int clickCount, Object... params) {
		previewCurrentPage();  // 短时间多次调用的情况下只执行一次
		
	}


	private synchronized void previewCurrentPage() {
		closeAllConnection(false);
		
		int startIndex = pager.getCurrentIndex();
		int currPageCount = pager.getCurrentPageCount();
		
		preview(startIndex, currPageCount);
		
		selectLiveView(startIndex);
	}
	
	public void addLiveView(LiveViewItemContainer l) {
		liveviews.add(l);
	}
	
	public void clearLiveView() {
		closeAllConnection(false);
		liveviews.clear();
	}
	
	public void resetLiveView(int validCount) {
		int lvSize = liveviews.size();
		
		for (int i = 0; i < lvSize; i++) {
			if ( i < validCount) {
				liveviews.get(i).getSurfaceView().setValid(true);
				liveviews.get(i).getRefreshImageView().setVisibility(View.VISIBLE);
			} else {
				liveviews.get(i).getSurfaceView().setValid(false);
			}
		}
	}
	
	public void closeAllConnection(boolean canUpdateViewAfterClosed) {
		int i;
		int connSize = connections.size();
		
		for (i = 0; i < connSize; i++) {
			if (connections.get(i).isConnected()) {
				connections.get(i).disconnect();
			} else {
				connections.get(i).setDisposed(true);  // 若为非连接状态，则可能处于连接初始化阶段，此时将其设置为disposed状态
			}
			
			if (!canUpdateViewAfterClosed) {
				connections.get(i).getLiveViewItemContainer().setCurrentConnection(null);
			}
		}
	}
	
	/**
	 * 获取指定LiveViewItemContainer所在的位置
	 * @param lv
	 * @return 1/2/3/4
	 */
	public int getIndexOfLiveView(LiveViewItemContainer lv) {
		int index = -1;
		int lvSize = liveviews.size();
		
		for (int i = 0; i < lvSize; i++) {
			if (liveviews.get(i) == lv) {
				index = i;
				break;
			}
		}
		
		return index >= 0 ? ++index : index;
	}
	
	public int selectLiveView(int index) {
		int lastIndex = currentIndex;
		
		currentIndex = index;
		
		int pageCapacity = pager.getPageCapacity();
		int pos = ((index % pageCapacity) == 0) ? pageCapacity : (index % pageCapacity); // 在4(或1)个LiveViewItemContainer中的位置
		int lastPos = ((lastIndex % pageCapacity) == 0) ? pageCapacity : (lastIndex % pageCapacity);
		int i;
		int lvSize = liveviews.size();
		
		// 设置新视频项的选择框，并去除上一视频项的选择框
		WindowLinearLayout w = null;
		for (i = 0; i < lvSize; i++) {
			if (i == (pos - 1)) {
				w = liveviews.get(i).getWindowLayout();
				w.setWindowSelected(true);
				//w.invalidate();
			} else if (i == (lastPos - 1)) {
				w = liveviews.get(i).getWindowLayout();
				w.setWindowSelected(false);
				//w.invalidate();
			}	
			
			
		}
		
		w = null;
		
		return currentIndex;
	}
	
	/**
	 * 预览多个设备
	 * @param startIndex 设备索引，从1开始，不能大于设备总数
	 * @param count 设备数量，取值[1,4]间的整数
	 */
	public void preview(int startIndex, int count) {
		if ((devicesCount - startIndex + 1 < count)
				|| startIndex > devicesCount
				|| count < 1 || count > 4) {
			throw new IllegalArgumentException("Error startIndex or count, startIndex = " + startIndex
					+ ", count = " + count);
		}
		
		if (liveviews.size() < count) {
			throw new IllegalArgumentException("Only " + liveviews.size()
					+ " LiveView(s) left, can not preview " + count
					+ "device(s) simultaneously");
		}
		
		int n;
		
		// 依据设备数量控制显示视频区域的底景（黑色，有效视频区域；灰色，无效视频区域）
		Log.i(TAG, "####$$$$live view count: " + liveviews.size());
		int lvCount = liveviews.size();
		for (n = 0; n < lvCount; n++) {
			liveviews.get(n).resetView();
			

			if (n >= count) {
				liveviews.get(n).getSurfaceView().setValid(false);
			} 

		}


		
		connections.clear();
		
		
		for (n = 1; n <= count; n++) {
			connections.add(new Connection());
		}
		
		
		for (n = 1; n <= count; n++) {
			final Connection conn = connections.get(n - 1);
			
			//if (conn.isConnected()) {
			//	conn.disconnect();
			//}
			
			PreviewDeviceItem p = devices.get(startIndex + (n - 1) - 1);
			
			// 注册连接状态监听器
			if (connectionStatusListener != null) {
				conn.SetConnectionListener(connectionStatusListener); 
			}
			
			conn.reInit();
			
			conn.setHost(p.getSvrIp());
			conn.setPort(Integer.valueOf(p.getSvrPort()));
			conn.setUsername(p.getLoginUser());
			conn.setPassword(p.getLoginPass());
			conn.setChannel(p.getChannel());
			
			liveviews.get(n - 1).setIsResponseError(false);
			liveviews.get(n - 1).setDeviceRecordName(p.getDeviceRecordName());
			liveviews.get(n - 1).setPreviewItem(p);
			
			conn.bindLiveViewItem(liveviews.get(n - 1));
			liveviews.get(n - 1).setCurrentConnection(conn);
						
			new Thread(new Runnable() {

				@Override
				public void run() {
					conn.connect();
				}
				
			}).start();
//			executor.execute(new Runnable() {
//				@Override
//				public void run() {
//					conn.connect();					
//				}
//			});
			
			
		}
	}
	
	/**
	 * 预览单个指定设备
	 * @param index 设备索引，从1开始，不能大于设备总数
	 */
	public void preview(int index) {
		preview(index, 1);		
	}
	
	/**
	 * 预览设备
	 */
	public void preview() {
		if (devices == null || devices.size() <= 0) {
			throw new IllegalStateException("Found not item in device list.");
		}
		
		int size = devices.size();
		
		if (size == 1) {
			setMultiMode(false);
			preview(1);
		} else if (size <= 4) {
			setMultiMode(true);
			preview(1, size);
		} else {
			setMultiMode(true);
			preview(1, 4);
		}
		
	}
	
	public void stopPreview(int index) {
		int pos = ((index % 4) == 0) ? 4 : (index % 4); // 在4个LiveViewItemContainer中的位置
		
		if ((pos - 1 < connections.size()) &&  connections.get(pos - 1).isConnected()) {
			connections.get(pos - 1).disconnect();
		}
	}
	
	public void stopPreview() {
//		int connCount = connections.size();
//		
//		for (int i = 0; i < connCount; i++) {
//			if (connections.get(i).isConnected()) {
//				connections.get(i).disconnect();
//			}
//		}
		closeAllConnection(true);
	}
	
	private void reloadConnections(int startIndex) {
		int pageCount = pager.getCurrentPageCount();
		int i;
		
		for (i = 0; i < pageCount; i++) {
			Connection conn = connections.get(i);
			
			PreviewDeviceItem p = devices.get(startIndex - 1 + i);
			
			conn.reInit();
			
			conn.setHost(p.getSvrIp());
			conn.setPort(Integer.valueOf(p.getSvrPort()));
			conn.setUsername(p.getLoginUser());
			conn.setPassword(p.getLoginPass());
			conn.setChannel(p.getChannel());
			
			liveviews.get(i).setIsResponseError(false);
			liveviews.get(i).setDeviceRecordName(p.getDeviceRecordName());
			liveviews.get(i).setPreviewItem(p);
		}
	}	
	
	public void tryPreview(int index) {
		int pageCapacity = pager.getPageCapacity();
		int pos = ((index % pageCapacity) == 0) ? pageCapacity : (index % pageCapacity);
		
		if (connections.size() < pageCapacity) { // 现存connection数不够时
			int i = 0;
			
			connections.clear();
			
			// 初始化connections
			for (i = 0; i < pageCapacity; i++) {
				Connection newConn = null;
				
				newConn = new Connection();
				
				// 注册连接状态监听器
				if (connectionStatusListener != null) {
					newConn.SetConnectionListener(connectionStatusListener); 
				}
				
				newConn.bindLiveViewItem(liveviews.get(i));
				liveviews.get(i).setCurrentConnection(newConn);
				
				connections.add(newConn);
			}
			
			// 将当前页通道数据装载进connections
			reloadConnections((pager.getCurrentPage() - 1) * pageCapacity + 1);
		}
		
		final Connection conn = connections.get(pos - 1);  // 取得对应的连接
		if (conn != null) {
			new Thread(new Runnable() {

				@Override
				public void run() {
					conn.connect();
				}
				
			}).start();
//			executor.execute(new Runnable() {
//				@Override
//				public void run() {
//					conn.connect();					
//				}
//			});
		}
	}
	
	
	public static interface OnVideoModeChangedListener {
		public void OnVideoModeChanged(boolean isMultiMode);
	}

	
}
