package com.starnet.snview.test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import com.starnet.snview.component.BufferSendManager;
import com.starnet.snview.protocol.message.LoginRequest;
import com.starnet.snview.protocol.message.OwspBegin;
import com.starnet.snview.protocol.message.OwspEnd;
import com.starnet.snview.protocol.message.PhoneInfoRequest;
import com.starnet.snview.protocol.message.VersionInfoRequest;

import android.test.AndroidTestCase;
import android.util.Log;

public class GetChannelNumTester extends AndroidTestCase {
	private static final String TAG = "GetChannelNumTester";
	Socket client;
	BufferSendManager sender;

	@Override
	protected void setUp() throws Exception {
		client = new Socket("113.225.252.145", 8080);
		sender = BufferSendManager.getInstance();
		sender.setOutStream(client.getOutputStream());
	}

	public void testConnect() throws Exception {
		
		//发送请求
		VersionInfoRequest v = new VersionInfoRequest();
		v.setVersionMajor(3);
		v.setVersionMinor(8);

		PhoneInfoRequest p = new PhoneInfoRequest();
		p.setEquipmentIdentity("");
		p.setEquipmentOS("Android");

		LoginRequest l = new LoginRequest();
		l.setUserName("admin");
		l.setPassword("25676166");//"25676166"
		l.setDeviceId(1);
		l.setFlag(1);
		l.setChannel(1);
		l.setReserve(new int[] { 0, 0 });

		sender.write(new OwspBegin());
		sender.write(v);
		sender.write(p);
		sender.write(l);
		sender.write(new OwspEnd());
		
		//读取返回信息
		InputStream in = client.getInputStream();
		byte[] head = new byte[8];
		in.read(head);// 报错
		ByteBuffer headBuffer = ByteBuffer.allocate(8).wrap(head);
		headBuffer.order(ByteOrder.BIG_ENDIAN);
		int len = headBuffer.getInt();
		if (len == 140) {//连接成功
			byte[] recvData = new byte[len - 4];
			in.read(recvData);
			int channelNumber = recvData[80];
			Log.d(TAG, "channelNumber："+channelNumber);
		}else if (len==20) {
			Log.d(TAG, "miMa cuowu + lianjieshibai：len:"+len);
		}else{
			Log.d(TAG, "lianjieshibai：len:"+len);
		}
		Log.d(TAG, "111");
	}

	private void getContent() throws UnsupportedEncodingException, IOException {
		BufferedReader rd = new BufferedReader(new InputStreamReader(
				client.getInputStream(), "UTF-8"));
		// 读取每一行的数据.注意大部分端口操作都需要交互数据。
		String str;
		while ((str = rd.readLine()) != null) {
			System.out.println(str);
		}
	}

	@Override
	protected void tearDown() throws Exception {

	}

}
