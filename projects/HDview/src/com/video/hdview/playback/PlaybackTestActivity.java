package com.video.hdview.playback;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import com.video.hdview.R;
import com.video.hdview.component.liveview.PlaybackLiveViewItemContainer;
import com.video.hdview.playback.utils.ByteArray2Object;
import com.video.hdview.playback.utils.DataProcessServiceImpl;
import com.video.hdview.playback.utils.OWSP_LEN;
import com.video.hdview.playback.utils.SocketInputStream;
import com.video.hdview.playback.utils.TLV_V_PacketHeader;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.FrameLayout;

public class PlaybackTestActivity extends Activity {

	private static final String TAG = "PlaybackTestActivity";
	private PlaybackLiveViewItemContainer mVideoContainer;
	private DataProcessServiceImpl dataProcessService;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.playback_test_activity);
		
		initView();
//		dataProcessService = new DataProcessServiceImpl(this, "PlaybackTestActivity");
	}
	
	
	private void initView() {
		FrameLayout playbackVideoRegion = (FrameLayout) findViewById(R.id.playback_test_video_region);
		mVideoContainer = new PlaybackLiveViewItemContainer(this);
		mVideoContainer.findSubViews();
		playbackVideoRegion.addView(mVideoContainer, 
				new FrameLayout.LayoutParams(480, 480));
		
		Button playback = (Button) findViewById(R.id.playback_test11_button);
		playback.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				try {
					testPlayback();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
	
	private void testPlayback() throws Exception {
		InputStream fin = new FileInputStream("/mnt/sdcard/avdata.raw");
		SocketInputStream sockIn = new SocketInputStream(fin);
		byte[] packetHeaderBuf = new byte[8];
		sockIn.read(packetHeaderBuf);
		TLV_V_PacketHeader owspPacketHeader = (TLV_V_PacketHeader) ByteArray2Object
				.convert2Object(TLV_V_PacketHeader.class, packetHeaderBuf, 0,
						OWSP_LEN.OwspPacketHeader);
		if (!(owspPacketHeader.getPacket_length() >= 4 && owspPacketHeader
				.getPacket_seq() > 0)) {
			return;
		}
		Log.i(TAG, "Packet seq:" + owspPacketHeader.getPacket_seq() + ", len:"
				+ (owspPacketHeader.getPacket_length() - 4));
		byte[] tlvContent = new byte[655350];
		tlvContent = makesureBufferEnough(tlvContent,
				(int) owspPacketHeader.getPacket_length() - 4);
		sockIn.read(tlvContent, 0,
				(int) owspPacketHeader.getPacket_length() - 4);
		while (!tlvContent.equals("")) {
			int result = dataProcessService.process(tlvContent,
					(int) owspPacketHeader.getPacket_length());
			if (result == -1) {/* 表示读到了TLV_T_RECORD_EOF包,则需要退出 */
				break;
			}
			do {
				for (int i = 0; i < 8; i++) {/* 数据重置 */
					packetHeaderBuf[i] = 0;
				}
				sockIn.read(packetHeaderBuf, 0, 8);/* 读取公共包头 */
				owspPacketHeader = (TLV_V_PacketHeader) ByteArray2Object
						.convert2Object(TLV_V_PacketHeader.class,
								packetHeaderBuf, 0, OWSP_LEN.OwspPacketHeader);
			} while (owspPacketHeader.getPacket_length() <= 0);
			tlvContent = makesureBufferEnough(tlvContent,
					(int) owspPacketHeader.getPacket_length() - 4);
			resetArray(tlvContent);/* 重置数据数组 */
			sockIn.read(tlvContent, 0,
					(int) owspPacketHeader.getPacket_length() - 4);
		}
		
		sockIn.close();
	}
	
	public static void resetArray(byte[] b) {
		int i = 0;
		for (i = 0; i < b.length; i++) {
			b[i] = 0;
		}
	}

	private static byte[] makesureBufferEnough(byte[] buffer, int realSize) {
		byte[] result = buffer;
		int size = buffer.length;

		if (size < realSize) {
			buffer = null;
			buffer = new byte[(int) (realSize * 1.2)];
			result = buffer;
		}

		return result;
	}

	public PlaybackLiveViewItemContainer getVideoContainer() {
		return mVideoContainer;
	}
	
}
