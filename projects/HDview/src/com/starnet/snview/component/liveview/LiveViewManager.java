package com.starnet.snview.component.liveview;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.FrameLayout;

import com.starnet.snview.protocol.Connection;
import com.starnet.snview.realplay.PreviewDeviceItem;
import com.starnet.snview.realplay.RealplayActivity;

public class LiveViewManager {
	private List<LiveViewItemContainer> liveviews; // 至多4个
	private List<Connection> connections;          // 对应liveviews
	
	private List<PreviewDeviceItem> devices;  // 预览设备列表
	
	private int showViewCount = 0;  // [0, 4]
	private int devicesCount;
	private int currentIndex;  // 当前LiveViewItemContainer在设备总数中的位置，从1开始
	
	private boolean isMultiMode; // 是否支持多画面显示
	
	private ExecutorService executor;
	
	private Context context;
	
	private OnVideoModeChangedListener onVideoModeChangedListener;
	
	public LiveViewManager(Context context) {
		this.context = context;
		
		this.liveviews = new ArrayList<LiveViewItemContainer>();
		this.connections = new ArrayList<Connection>();
		
		isMultiMode = false;
		
		executor = Executors.newFixedThreadPool(4);
	}
	
	public void setDeviceList(List<PreviewDeviceItem> devices) {
		this.devices = devices;
		devicesCount = devices.size();
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

	public boolean isMultiMode() {
		return isMultiMode;
	}



	public void setMultiMode(boolean isMultiMode) {
		if (this.isMultiMode == isMultiMode) {
			return;
		}
		
		this.isMultiMode = isMultiMode;
		
		if (onVideoModeChangedListener != null) {
			onVideoModeChangedListener.OnVideoModeChanged(isMultiMode);
		}
	}



	public void addLiveView(LiveViewItemContainer l) {
		liveviews.add(l);
	}
	
	public void clearLiveView() {
		int i;
		int connSize = connections.size();
		
		for (i = 0; i < connSize; i++) {
			if (connections.get(i).isConnected()) {
				connections.get(i).disconnect();
			}
		}
		
		liveviews.clear();
	}
	
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
		currentIndex = index;
		
		int pos = ((index % 4) == 0) ? 4 : (index % 4); // 在4个LiveViewItemContainer中的位置
		int i;
		int lvSize = liveviews.size();
		
		for (i = 0; i < lvSize; i++) {
			WindowLinearLayout w = liveviews.get(i).getWindowLayout();
			if (i == (pos - 1)) {
				w.setWindowSelected(true);
			} else {
				w.setWindowSelected(false);
			}
			
			w.requestLayout();
			
		}
		
		return 1;
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
		
		int n;
		
		// 保证当前connection池资源足够
//		int connCount = connections.size();
//		for (n = 1; n <= count - connCount; n++) {
//			connections.add(new Connection());
//		}
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
			
			conn.reInit();
			
			conn.setHost(p.getSvrIp());
			conn.setPort(Integer.valueOf(p.getSvrPort()));
			conn.setUsername(p.getLoginUser());
			conn.setPassword(p.getLoginPass());
			conn.setChannel(p.getChannel());
			
			conn.bindLiveViewListener(liveviews.get(n - 1).getSurfaceView());
			
			executor.execute(new Runnable() {
				@Override
				public void run() {
					conn.connect();					
				}
			});
			
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
		int connCount = connections.size();
		
		for (int i = 0; i < connCount; i++) {
			if (connections.get(i).isConnected()) {
				connections.get(i).disconnect();
			}
		}
	}
	
	public static interface OnVideoModeChangedListener {
		public void OnVideoModeChanged(boolean isMultiMode);
	}
	
}
