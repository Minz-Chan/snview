package com.video.hdview.component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.video.hdview.component.liveview.LiveView;
import com.video.hdview.protocol.Connection;


public class ConnectionManager {
	private static ConnectionManager mInstance = null;

	private List<LiveView> liveviewList;
	
	private Map<LiveView, Connection> connections = new HashMap<LiveView, Connection>();
	
	private ConnectionManager() {}
	
	public static ConnectionManager getInstance() {
		if (mInstance == null) {
			mInstance = new ConnectionManager();
		}
		
		return mInstance;
	}
	
	public void bindLiveViewList(List<LiveView> listviews) {
		liveviewList = listviews;
	}
	
	public void startPreview(final Connection conn) {
		if (connections.containsKey(liveviewList.get(0))) {
			connections.get(liveviewList.get(0)).disconnect();
		}
		
		if (conn == null) {
			return;
		}
		
//		conn.bindLiveViewListener(liveviewList.get(0));
		
		connections.put(liveviewList.get(0), conn);
		
		new Thread(new Runnable() {
			@Override
			public void run() {
				conn.connect();
			}
		}).start();
	}
	
	public void stopPreview() {
		connections.get(liveviewList.get(0)).disconnect();
	}
	
	
}
