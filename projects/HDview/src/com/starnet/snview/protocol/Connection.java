package com.starnet.snview.protocol;


import java.net.InetSocketAddress;

import org.apache.mina.core.RuntimeIoException;
import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.future.ConnectFuture;
import org.apache.mina.core.session.AttributeKey;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.handler.demux.DemuxingIoHandler;
import org.apache.mina.transport.socket.SocketConnector;
import org.apache.mina.transport.socket.nio.NioSocketConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.util.Log;
import android.view.View;

import com.starnet.snview.component.h264.H264DecodeUtil;
import com.starnet.snview.component.liveview.LiveView;
import com.starnet.snview.component.liveview.LiveViewItemContainer;
import com.starnet.snview.component.liveview.OnLiveViewChangedListener;
import com.starnet.snview.protocol.codec.factory.OwspFactory;
import com.starnet.snview.protocol.codec.factory.TlvMessageFactory;
import com.starnet.snview.protocol.message.ChannelResponse;
import com.starnet.snview.protocol.message.ControlRequest;
import com.starnet.snview.protocol.message.ControlResponse;
import com.starnet.snview.protocol.message.DVSInfoRequest;
import com.starnet.snview.protocol.message.LoginRequest;
import com.starnet.snview.protocol.message.LoginResponse;
import com.starnet.snview.protocol.message.OwspBegin;
import com.starnet.snview.protocol.message.OwspEnd;
import com.starnet.snview.protocol.message.PhoneInfoRequest;
import com.starnet.snview.protocol.message.StreamDataFormat;
import com.starnet.snview.protocol.message.VersionInfoRequest;
import com.starnet.snview.protocol.message.VideoFrameInfo;
import com.starnet.snview.protocol.message.VideoFrameInfoEx;
import com.starnet.snview.protocol.message.VideoIFrameData;
import com.starnet.snview.protocol.message.VideoPFrameData;
import com.starnet.snview.protocol.message.handler.ChannelResponseMessageHandler;
import com.starnet.snview.protocol.message.handler.ControlResponseMessageHandler;
import com.starnet.snview.protocol.message.handler.DVSInfoRequestMessageHandler;
import com.starnet.snview.protocol.message.handler.IoBufferMessageHandler;
import com.starnet.snview.protocol.message.handler.LoginResponseMessageHandler;
import com.starnet.snview.protocol.message.handler.StreamDataFormatMessageHandler;
import com.starnet.snview.protocol.message.handler.VersionInfoRequestMessageHandler;
import com.starnet.snview.protocol.message.handler.VideoFrameDataMessageHandler;
import com.starnet.snview.protocol.message.handler.VideoFrameInfoExMessageHandler;
import com.starnet.snview.protocol.message.handler.VideoFrameInfoMessageHandler;
import com.starnet.snview.util.RandomUtils;


public class Connection extends DemuxingIoHandler {
	private final static Logger LOGGER = LoggerFactory.getLogger(Connection.class);
	private final static String TAG = "Connection";
	
	private AttributeKey LIVEVIEW_ITEM = new AttributeKey(Connection.class, "liveview_item");
	private AttributeKey LIVEVIEW_LISTENER = new AttributeKey(Connection.class, "liveview_listener");
	private AttributeKey CONNECTION_LISTENER = new AttributeKey(Connection.class, "connection_listener");
	private AttributeKey H264DECODER = new AttributeKey(Connection.class, "h264decoder");
	
	private AttributeKey CONNECTION = new AttributeKey(Connection.class, "connection");

	public static final int CONNECT_TIMEOUT = 5000;

    private String host;
    private int port;
    private String username;
    private String password;
    private int channel;
    private SocketConnector connector;
    private IoSession session;
    
    private boolean isDisposed;
   
    
    private H264DecodeUtil mH264decoder;
    
    private LiveViewItemContainer mLiveViewItem;
    private OnLiveViewChangedListener mLiveViewChangedListener;
    private StatusListener mConnectionListener;
    
    public Connection() {
    	this.channel = 1;
    	init();
    }
    
    public Connection(String host, int port) {
        this.host = host;
        this.port = port;
        this.channel = 1;
        
        init();
    }
    
    
    private void init() {
    	isDisposed = false;
    	
    	mH264decoder = new H264DecodeUtil(host + ":" + port + "@" + RandomUtils.getRandomNumbers(6));

    	initConnector();        
        initMessageHandler();
    }
    
    public void reInit() {
    	isDisposed = false;
    	
    	if (!connector.isDisposed()) {
    		connector.dispose(true);
    		System.out.println(this + "@connector-disposed");
    	}
    	
    	connector = null;
    	initConnector();
    }
    
    private void initConnector() {
    	connector = new NioSocketConnector();
        //connector.getFilterChain().addLast("owsp-codec", new ProtocolCodecFilter(new OwspMessageFactory()));
        connector.getFilterChain().addLast("owsp-codec", new OwspProtocolCodecFilter(new OwspFactory()));
        connector.getFilterChain().addLast("tlv-codec", new ProtocolCodecFilter(new TlvMessageFactory()));
        connector.setHandler(this);
    }
    
    private void initMessageHandler() {
    	this.addReceivedMessageHandler(VersionInfoRequest.class, new VersionInfoRequestMessageHandler());
    	this.addReceivedMessageHandler(LoginResponse.class, new LoginResponseMessageHandler());
        this.addReceivedMessageHandler(DVSInfoRequest.class, new DVSInfoRequestMessageHandler());  
        this.addReceivedMessageHandler(ChannelResponse.class, new ChannelResponseMessageHandler());
        this.addReceivedMessageHandler(ControlResponse.class, new ControlResponseMessageHandler());
        this.addReceivedMessageHandler(StreamDataFormat.class, new StreamDataFormatMessageHandler());
        this.addReceivedMessageHandler(VideoFrameInfo.class, new VideoFrameInfoMessageHandler());
        this.addReceivedMessageHandler(VideoFrameInfoEx.class, new VideoFrameInfoExMessageHandler());
//        this.addReceivedMessageHandler(VideoIFrameData.class, new VideoIFrameDataMessageHandler());
//        this.addReceivedMessageHandler(VideoPFrameData.class, new VideoPFrameDataMessageHandler());
        this.addReceivedMessageHandler(IoBuffer.class, new IoBufferMessageHandler());
        this.addReceivedMessageHandler(VideoIFrameData.class, new VideoFrameDataMessageHandler());
        this.addReceivedMessageHandler(VideoPFrameData.class, new VideoFrameDataMessageHandler());
    }
    
    
    
    public void setHost(String host) {
		this.host = host;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public void setPassword(String password) {
		this.password = password;
	}
    
	public void setChannel(int channel) {
		this.channel = channel;
	}
	
	public void setDisposed(boolean isDisposed) {
		this.isDisposed = isDisposed;
	}
	
	public void SetConnectionListener(StatusListener listener) {
		if (listener == null) {
			throw new IllegalArgumentException("Connection Listener can not be null");
		}
		
		this.mConnectionListener = listener;
	}
    
    public boolean isConnected() {
        return (session != null && session.isConnected());
    }

    private void checkIfEverythingPrepared() {
    	if (mH264decoder == null) {
    		throw new IllegalStateException("H264 decoder should be initialized.");
    	}
    	
    	if (mLiveViewItem == null) {
    		throw new IllegalStateException("LiveViewItemContainer should be initialized.");
    	}
    	
    	if (mLiveViewChangedListener == null) {
    		throw new IllegalStateException("Surface view of LiveViewItemContainer should be initialized.");
    	}
    	
    	if (mConnectionListener == null) {
    		throw new IllegalStateException("Connection status listener should be initialized.");
    	}
    }
    
    public synchronized void connect() {
    	checkIfEverythingPrepared();
    	
    	if (connector.isDisposed()) {
    		initConnector();
    		isDisposed = false;
    	}
    	
    	if (this == mLiveViewItem.getCurrentConnection()) {
    		mConnectionListener.OnConnectionTrying(mLiveViewItem);
    	}
    	
        ConnectFuture connectFuture = connector.connect(new InetSocketAddress(host, port));
        connectFuture.awaitUninterruptibly(CONNECT_TIMEOUT);
        try {
            session = connectFuture.getSession();
        }
        catch (RuntimeIoException e) {
            e.printStackTrace();
        } finally {
        	if (isDisposed) {
        		if (session != null) {
        			session.close(true);
            		System.out.println("isDisposed: true");
            		
            		if (this == mLiveViewItem.getCurrentConnection()) {
            			mConnectionListener.OnConnectionFailed(mLiveViewItem);
            		}
                	
        		}
        	} else {
        		if (session == null) { // 连接建立失败
        			if (this == mLiveViewItem.getCurrentConnection()) {
        				mConnectionListener.OnConnectionFailed(mLiveViewItem);
        			}
        		}
        	}
        	
        }
    }

    private void login(IoSession session, String username, String password) {
    	VersionInfoRequest v = new VersionInfoRequest();
    	v.setVersionMajor(3);
    	v.setVersionMinor(8);
    	
    	PhoneInfoRequest p = new PhoneInfoRequest();
    	p.setEquipmentIdentity("");
    	p.setEquipmentOS("Android");

    	LoginRequest l = new LoginRequest();
    	l.setUserName(username);
    	l.setPassword(password);
    	l.setDeviceId(1);
    	l.setChannel(channel);
    	
    	session.write(new OwspBegin());
    	
    	session.write(v);
    	session.write(p);
    	session.write(l);
    	
    	session.write(new OwspEnd());
    	
    	/*
    	WriteFuture writeResult = session.write(buf); 

    	writeResult.addListener(new IoFutureListener(){
			@Override
			public void operationComplete(IoFuture future) {
				WriteFuture wFuture = (WriteFuture)future;
				if (wFuture.isWritten()) {
					return;
				} else {
					System.out.println("Send fail...");
				}
				
			}
    	});*/
    	
    	
    }
    
    public void sendControlRequest(int cmdCode) {
    	ControlRequest c = new ControlRequest();
    	c.setDeviceId(1);
    	c.setChannel(channel);
    	c.setCmdCode(cmdCode);
    	c.setSize(0);
    	
    	session.write(new OwspBegin());
    	session.write(c);
    	session.write(new OwspEnd());
    }

    public synchronized void disconnect() {
        if (session != null) {
        	System.out.println("###$$$session " + session + " closed...");
        	
            session.close(true).awaitUninterruptibly(CONNECT_TIMEOUT);
            session = null;
        } 
        
        isDisposed = true;
    }
    
    public void bindLiveViewItem(LiveViewItemContainer item) {
    	if (item == null) {
    		throw new IllegalArgumentException("Parameter LiveViewItemContainer can not be null");
    	}
    	
    	this.mLiveViewItem = item;
    	
    	if (item.getSurfaceView() == null) {
    		throw new IllegalArgumentException("Found not surface view in LiveViewItemContainer");
    	}
    	
    	this.mLiveViewChangedListener = item.getSurfaceView();
    	
    	
    }

	@Override
	public void sessionCreated(IoSession session) throws Exception {
		session.setAttribute(LIVEVIEW_ITEM, mLiveViewItem);
		session.setAttribute(LIVEVIEW_LISTENER, mLiveViewChangedListener);
		session.setAttribute(CONNECTION_LISTENER, mConnectionListener);
		session.setAttribute(H264DECODER, mH264decoder);

		session.setAttribute(CONNECTION, this);
	}

	@Override
	public void sessionOpened(IoSession session) throws Exception {
		if (this == mLiveViewItem.getCurrentConnection()) {
			mConnectionListener.OnConnectionEstablished(mLiveViewItem);
		}
		
		login(session, username, password);
	}

	@Override
	public void exceptionCaught(IoSession session, Throwable cause)
			throws Exception {
		LOGGER.warn("Close the session");
		cause.printStackTrace();
		session.close(true);
	}

	@Override
	public void messageSent(IoSession session, Object message) throws Exception {
		// TODO Auto-generated method stub
		//super.messageSent(session, message);
	}
	
	
	@Override
	public void sessionClosed(IoSession session) throws Exception {
		System.out.println("Session " + session.getId() + " is closed...");
		connector.dispose();
		
		if (this == mLiveViewItem.getCurrentConnection()) {
			mConnectionListener.OnConnectionClosed(mLiveViewItem);
			mLiveViewChangedListener.onDisplayContentReset();
		}

		if (mH264decoder != null) {
			mH264decoder.uninit();
		}

//		this.mH264decoder = null;
//		this.mLiveViewItem = null;
//		this.mLiveViewChangedListener = null;
//		this.mConnectionListener = null;
//		this.connector = null;
//		this.H264DECODER = null;
//		this.LIVEVIEW_ITEM = null;
//		this.LIVEVIEW_LISTENER = null;
//		this.CONNECTION_LISTENER = null;
//
//		this.session = null;
	}

	@Override
	protected void finalize() throws Throwable {
		System.out.println(this + "@finalized");
		super.finalize();
	}
	
	
	public LiveViewItemContainer getLiveViewItemContainer() {
		return mLiveViewItem;
	}
	
	public boolean isValid() {
		Log.i(TAG, "isDisposed: " + isDisposed + ", this == mLiveViewItem.getCurrentConnection(): " + (this == mLiveViewItem.getCurrentConnection()));
		return this == mLiveViewItem.getCurrentConnection() && !isDisposed;
	}
	
	
	public static interface StatusListener {
		public void OnConnectionTrying(View v);      // 尝试建立连接事件
		public void OnConnectionFailed(View v);      // 连接建立失败
		public void OnConnectionEstablished(View v); // 连接建立成功事件
		public void OnConnectionBusy(View v);        // 连接正在使用
		public void OnConnectionClosed(View v);      // 连接关闭事件
	}
}
