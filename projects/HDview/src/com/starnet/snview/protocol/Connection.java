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

import com.starnet.snview.component.h264.H264DecodeUtil;
import com.starnet.snview.component.liveview.OnLiveViewChangedListener;
import com.starnet.snview.protocol.codec.factory.OwspFactory;
import com.starnet.snview.protocol.codec.factory.TlvMessageFactory;
import com.starnet.snview.protocol.message.ChannelResponse;
import com.starnet.snview.protocol.message.DVSInfoRequest;
import com.starnet.snview.protocol.message.LoginRequest;
import com.starnet.snview.protocol.message.OwspBegin;
import com.starnet.snview.protocol.message.OwspEnd;
import com.starnet.snview.protocol.message.PhoneInfoRequest;
import com.starnet.snview.protocol.message.StreamDataFormat;
import com.starnet.snview.protocol.message.VersionInfoRequest;
import com.starnet.snview.protocol.message.VideoFrameInfo;
import com.starnet.snview.protocol.message.VideoIFrameData;
import com.starnet.snview.protocol.message.VideoPFrameData;
import com.starnet.snview.protocol.message.handler.ChannelResponseMessageHandler;
import com.starnet.snview.protocol.message.handler.DVSInfoRequestMessageHandler;
import com.starnet.snview.protocol.message.handler.IoBufferMessageHandler;
import com.starnet.snview.protocol.message.handler.StreamDataFormatMessageHandler;
import com.starnet.snview.protocol.message.handler.VersionInfoRequestMessageHandler;
import com.starnet.snview.protocol.message.handler.VideoFrameDataMessageHandler;
import com.starnet.snview.protocol.message.handler.VideoFrameInfoMessageHandler;
import com.starnet.snview.util.RandomUtils;


public class Connection extends DemuxingIoHandler {
	private final static Logger LOGGER = LoggerFactory.getLogger(Connection.class);
	private final AttributeKey LIVEVIEW_LISTENER = new AttributeKey(Connection.class, "liveview_listener");
	private final AttributeKey H264DECODER = new AttributeKey(Connection.class, "h264decoder");

	public static final int CONNECT_TIMEOUT = 5000;

    private String host;
    private int port;
    private String username;
    private String password;
    private SocketConnector connector;
    private IoSession session;
    
    private int channel;
    
    private H264DecodeUtil mH264decoder;
    
    private OnLiveViewChangedListener mLiveViewChangedListener;
    
    public Connection(String host, int port) {
        this.host = host;
        this.port = port;
        this.channel = 1;
        
        mH264decoder = new H264DecodeUtil(host + ":" + port + "@" + RandomUtils.getRandomNumbers(6));

        connector = new NioSocketConnector();
        //connector.getFilterChain().addLast("owsp-codec", new ProtocolCodecFilter(new OwspMessageFactory()));
        connector.getFilterChain().addLast("owsp-codec", new OwspProtocolCodecFilter(new OwspFactory()));
        connector.getFilterChain().addLast("tlv-codec", new ProtocolCodecFilter(new TlvMessageFactory()));
        connector.setHandler(this);
        
        initMessageHandler();
    }
    
    private void initMessageHandler() {
    	this.addReceivedMessageHandler(VersionInfoRequest.class, new VersionInfoRequestMessageHandler());
        this.addReceivedMessageHandler(DVSInfoRequest.class, new DVSInfoRequestMessageHandler());  
        this.addReceivedMessageHandler(ChannelResponse.class, new ChannelResponseMessageHandler());
        this.addReceivedMessageHandler(StreamDataFormat.class, new StreamDataFormatMessageHandler());
        this.addReceivedMessageHandler(VideoFrameInfo.class, new VideoFrameInfoMessageHandler());
//        this.addReceivedMessageHandler(VideoIFrameData.class, new VideoIFrameDataMessageHandler());
//        this.addReceivedMessageHandler(VideoPFrameData.class, new VideoPFrameDataMessageHandler());
        this.addReceivedMessageHandler(IoBuffer.class, new IoBufferMessageHandler());
        this.addReceivedMessageHandler(VideoIFrameData.class, new VideoFrameDataMessageHandler());
        this.addReceivedMessageHandler(VideoPFrameData.class, new VideoFrameDataMessageHandler());
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
    
    public boolean isConnected() {
        return (session != null && session.isConnected());
    }

    public void connect() {
        ConnectFuture connectFuture = connector.connect(new InetSocketAddress(host, port));
        connectFuture.awaitUninterruptibly(CONNECT_TIMEOUT);
        try {
            session = connectFuture.getSession();
        }
        catch (RuntimeIoException e) {
            e.printStackTrace();
        }
    }

    private void login(IoSession session, String username, String password) {
    	/*
    	ByteBuffer buf = ByteBuffer.allocate(116);
    	ByteBuffer tmp = ByteBuffer.allocate(16);
    	
    	// Owsp header
    	buf.order(ByteOrder.BIG_ENDIAN).putInt(112);
    	buf.order(ByteOrder.LITTLE_ENDIAN).putInt(0);
    	
    	// Version Info Request
    	buf.putShort((short)Constants.MSG_TYPE.VERSION_INFO_REQUEST);
    	buf.putShort((short)Constants.MSG_LEN.VERSION_INFO_REQUEST);
    	buf.putShort((short)3);  // major version
    	buf.putShort((short)8);  // minor version
    	
    	// Phone Info Request
    	buf.putShort((short)Constants.MSG_TYPE.PHONE_INFO_REQUEST);
    	buf.putShort((short)Constants.MSG_LEN.PHONE_INFO_REQUEST);
    	
    	tmp.put(new String("111111").getBytes());
    	tmp.rewind();
    	buf.put(tmp);	// equipmentIdentity[STR_LEN_16]
    	
    	tmp.clear();
    	tmp.put(new String("Android").getBytes());
    	tmp.rewind();
    	buf.put(tmp);	// equipmentOS[STR_LEN_16]
    	
    	buf.putInt(0);  // reserve1,2,3,4
    	
    	// Login Request
    	buf.putShort((short)Constants.MSG_TYPE.LOGIN_REQUEST);
    	buf.putShort((short)Constants.MSG_LEN.LOGIN_REQUEST);
    	
    	tmp = ByteBuffer.allocate(32);
    	tmp.put(new String("admin").getBytes());
    	tmp.rewind();
    	buf.put(tmp);  // userName[STR_LEN_32]
    	
    	tmp = ByteBuffer.allocate(16);
    	tmp.put(new String("123456").getBytes());
    	tmp.rewind();
    	buf.put(tmp);  // password[STR_LEN_16]
    	
    	buf.putInt(0);  // deviceId
    	buf.put((byte)1);  // flag , should be set to 1 to be compatible with the previous version.
    	buf.put((byte)0);  // channel
    	buf.putShort((short)0); // u_int8 reserve[2]
    	
    	buf.flip();
    	
    	session.write(buf);
    	*/
    	
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

    public void disconnect() {
        if (session != null) {
            session.close(true).awaitUninterruptibly(CONNECT_TIMEOUT);
            session = null;
        }
    }
    
    public void bindLiveViewListener(OnLiveViewChangedListener listener) {
    	this.mLiveViewChangedListener = listener;
    }

	@Override
	public void sessionCreated(IoSession session) throws Exception {
		if (mLiveViewChangedListener != null) {
			session.setAttribute(LIVEVIEW_LISTENER, mLiveViewChangedListener);
		}
		
		if (mH264decoder != null) {
			session.setAttribute(H264DECODER, mH264decoder);
		}
		
	}

	@Override
	public void sessionOpened(IoSession session) throws Exception {
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
	}
	
}
