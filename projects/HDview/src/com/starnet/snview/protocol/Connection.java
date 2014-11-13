package com.starnet.snview.protocol;


import java.net.InetSocketAddress;
import java.nio.ByteOrder;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.mina.core.RuntimeIoException;
import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.future.ConnectFuture;
import org.apache.mina.core.session.AttributeKey;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.handler.demux.DemuxingIoHandler;
import org.apache.mina.transport.socket.SocketConnector;
import org.apache.mina.transport.socket.nio.NioSocketConnector;

import android.util.Log;
import android.view.View;

import com.starnet.snview.component.h264.H264DecodeUtil;
import com.starnet.snview.component.liveview.LiveViewItemContainer;
import com.starnet.snview.component.liveview.OnLiveViewChangedListener;
import com.starnet.snview.protocol.codec.decoder.OwspDecoder;
import com.starnet.snview.protocol.codec.factory.OwspFactory;
import com.starnet.snview.protocol.codec.factory.TlvMessageFactory;
import com.starnet.snview.protocol.message.ChannelResponse;
import com.starnet.snview.protocol.message.Constants;
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
	private final static String TAG = "Connection";
	
//	private AttributeKey LIVEVIEW_ITEM = new AttributeKey(Connection.class, "liveview_item");
//	private AttributeKey LIVEVIEW_LISTENER = new AttributeKey(Connection.class, "liveview_listener");
//	private AttributeKey CONNECTION_LISTENER = new AttributeKey(Connection.class, "connection_listener");
//	private AttributeKey H264DECODER = new AttributeKey(Connection.class, "h264decoder");
	
	private AttributeKey CONNECTION = new AttributeKey(Connection.class, "connection");
	private final AttributeKey SEQUENCE = new AttributeKey(OwspDecoder.class, "sequence4send");

	public static final int CONNECT_TIMEOUT = 5000;

    private String host;
    private int port;
    private String username;
    private String password;
    private int channel;
    private SocketConnector connector;
    private IoSession session;
    
    private boolean isDisposed;
    private boolean isConnecting;
    private boolean isJustAfterConnected;		// 标识刚刚连接上
    private boolean isShowComponentChanged;		// 标识接收显示数据的组件是否发生改变
   
    
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
    	isConnecting = false;
    	isJustAfterConnected = false;
    	isShowComponentChanged = false;
    	
    	mH264decoder = new H264DecodeUtil(host + ":" + port + "@" + RandomUtils.getRandomNumbers(6));

    	initConnector();        
        initMessageHandler();
    }
    
    public void reInit() {
    	isDisposed = false;
    	isConnecting = false;
    	isJustAfterConnected = false;
    	isShowComponentChanged = false;
    	
    	if (!connector.isDisposed()) {
    		connector.dispose(true);

    		Log.i(TAG, this + "@connector-disposed");
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
	
	public StatusListener getStatusListener() {
		return mConnectionListener;
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
    
    public boolean isConnecting() {
    	return isConnecting;
    }
    
    public boolean isJustAfterConnected() {
    	return isJustAfterConnected;
    }
    
    public void setIsJustAfterConnected(boolean isJustAfterConnected) {
    	this.isJustAfterConnected = isJustAfterConnected;
    }
    
    public boolean isShowComponentChanged() {
    	return isShowComponentChanged;
    }
    
    public void setShowComponentChanged(boolean changed) {
    	this.isShowComponentChanged = changed;
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
    	
    	if (isValid()) {
    		mConnectionListener.OnConnectionTrying(mLiveViewItem);
    	}
    	
    	isConnecting = true;
    	isJustAfterConnected = false;
    	
    	Log.i(TAG, "connector.connect");
    	
        ConnectFuture connectFuture = connector.connect(new InetSocketAddress(host, port));
        connectFuture.awaitUninterruptibly(CONNECT_TIMEOUT);
        try {
            session = connectFuture.getSession();
        }
        catch (RuntimeIoException e) {
            e.printStackTrace();
        } finally {
        	isConnecting = false;
        	if (isDisposed) {
        		if (session != null) {
        			session.close(true);
            		
            		if (isValid()) {
            			mConnectionListener.OnConnectionFailed(mLiveViewItem);
            		}
        		}
        	} else {
        		if (session == null) { // 连接建立失败
        			if (isValid()) {
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
    	l.setFlag(1);
    	l.setChannel(channel);
    	l.setReserve(new int[]{0, 0});
    	
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
    
    private void sendBuffer(int msgType, byte[] buffer) {
    	if (session == null) {
    		return;
    	}
    	
		int type = msgType;
		int length = buffer.length;
		byte[] value = buffer;
		int packetLength = 8 + length; // 4 + 2 + 2(Seq + type + length)
		AtomicLong seq = (AtomicLong) session.getAttribute(SEQUENCE);
		
		if (seq != null) {
			seq.incrementAndGet();  // make sequence increase by 1
			session.setAttribute(SEQUENCE, seq);
		} else {
			session.setAttribute(SEQUENCE, new AtomicLong(1));
		}
		
		
		IoBuffer sendBuf = IoBuffer.allocate(4 + packetLength).order(ByteOrder.BIG_ENDIAN);
		sendBuf.putUnsignedInt(packetLength);  // packet length
		sendBuf.order(ByteOrder.LITTLE_ENDIAN);
		sendBuf.putUnsignedInt(seq.intValue());	// sequence
		sendBuf.putUnsignedShort(type); //  type of type-length-value
		sendBuf.putUnsignedShort(length); // length of type-length-value
		sendBuf.put(value);	// value of type-length-value
		sendBuf.flip();
		
		session.write(sendBuf);
    }
    
    public void sendControlRequest(int comCode, int[] args) {
    	IoBuffer buffer = null;
    	
    	switch (comCode) {
    	case Constants.OWSP_ACTION_CODE.OWSP_ACTION_GOTO_PRESET_POSITION:
    	case Constants.OWSP_ACTION_CODE.OWSP_ACTION_SET_PRESET_POSITION:
    	case Constants.OWSP_ACTION_CODE.OWSP_ACTION_CLEAR_PRESET_POSITION:
    		buffer = IoBuffer.allocate(12).order(ByteOrder.LITTLE_ENDIAN);
    		buffer.putUnsignedInt(1);
    		buffer.putUnsigned((byte) channel);
    		buffer.putUnsigned((byte) comCode);
    		buffer.putUnsignedShort(4);
    		buffer.putUnsignedInt(args[0]); // uint, preset point number (1~200)
    		break;
    	}
    	
    	buffer.flip();
    	sendBuffer(Constants.MSG_TYPE.CONTROL_REQUEST, buffer.array());
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
        	Log.i(TAG, "###$$$session " + session + " closed...");
        	
            session.close(true).awaitUninterruptibly(CONNECT_TIMEOUT);
            session = null;
        } 
        
        isDisposed = true;
    }
    
    public LiveViewItemContainer getLiveViewContainer() {
    	return mLiveViewItem;
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
    
    public void updateLiveViewItem(LiveViewItemContainer item) {
    	bindLiveViewItem(item);
    	
    	isShowComponentChanged = true;
    }

	@Override
	public void sessionCreated(IoSession session) throws Exception {
//		session.setAttribute(LIVEVIEW_ITEM, mLiveViewItem);
//		session.setAttribute(LIVEVIEW_LISTENER, mLiveViewChangedListener);
//		session.setAttribute(CONNECTION_LISTENER, mConnectionListener);
//		session.setAttribute(H264DECODER, mH264decoder);

		session.setAttribute(CONNECTION, this);
	}

	@Override
	public void sessionOpened(IoSession session) throws Exception {
		if (isValid()) {
			mConnectionListener.OnConnectionEstablished(mLiveViewItem);
			isJustAfterConnected = true;
		}
		
		login(session, username, password);
		
		Log.i(TAG, session + ", user:" + username + ", pass:" + password);
	}

	@Override
	public void exceptionCaught(IoSession session, Throwable cause)
			throws Exception {
		Log.i(TAG, "Close the session");
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
		Log.i(TAG, "Session " + session.getId() + " is closed...");
		connector.dispose();
		
		if (isValid()) {
			mConnectionListener.OnConnectionClosed(mLiveViewItem);
			Log.i(TAG, "####$$$$");
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
		Log.i(TAG,  this + "@finalized");
		super.finalize();
	}
	
	
	public LiveViewItemContainer getLiveViewItemContainer() {
		return mLiveViewItem;
	}

	
	public H264DecodeUtil getH264decoder() {
		return mH264decoder;
	}

	public OnLiveViewChangedListener getLiveViewChangedListener() {
		return mLiveViewChangedListener;
	}

	public StatusListener getConnectionListener() {
		return mConnectionListener;
	}

	public boolean isValid() {
		//Log.i(TAG, "isDisposed: " + isDisposed + ", this == mLiveViewItem.getCurrentConnection(): " + (this == mLiveViewItem.getCurrentConnection()));
		return this == mLiveViewItem.getCurrentConnection();
	}
	
	
	public static interface StatusListener {
		public void OnConnectionTrying(View v);      // 尝试建立连接事件
		public void OnConnectionFailed(View v);      // 连接建立失败
		public void OnConnectionEstablished(View v); // 连接建立成功事件
		public void OnConnectionBusy(View v);        // 连接正在使用
		public void OnConnectionClosed(View v);      // 连接关闭事件
	}
}
