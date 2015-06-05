package com.video.hdview.component.video;

import java.util.LinkedList;
import java.util.Queue;




import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class VideoBufferQueue {
	private static final String TAG = "VideoBufferQueue";
	private Queue<VideoBuffer> writeBufferQueue;
	private Queue<VideoBuffer> readBufferQueue;
	
	private Handler videoHandler;
	
	public VideoBufferQueue(Handler videoHandler) {
		this.videoHandler = videoHandler;
		writeBufferQueue = new LinkedList<VideoBuffer>();
		for (int i = 0; i < 5; i++) {
			VideoBuffer v = new VideoBuffer();
			v.id = i;
			writeBufferQueue.offer(v);
		}	
		
		readBufferQueue = new LinkedList<VideoBuffer>();
		
	}
	
	/**
	 * Write video frame one by one
	 * @param src A buffer containing a complete frame
	 * @return Bytes count which is written
	 */
	public synchronized int write(byte[] src) {
		Log.i(TAG, "writeBufferQueue.size():" + writeBufferQueue.size());
		if (!writeBufferQueue.isEmpty()) {
			VideoBuffer buf = writeBufferQueue.peek();
			if (buf != null) {
				buf.set(src);
				Log.i(TAG, "writeBufferQueue set to id " + buf.id);
				readBufferQueue.offer(writeBufferQueue.poll());
				
				Message msg = Message.obtain();
				msg.what = VideoHandler.MSG_BUFFER_PROCESS;
				videoHandler.sendMessage(msg);
				
				return src.length;
			} else {
				writeBufferQueue.poll();  // if head element is null, then remove it
			}
		}
		
		return 0;
	}
	
//	public int write(byte[] src, int offset, int size) {
//		Log.i(TAG, "writeBufferQueue.size():" + writeBufferQueue.size());
//		if (!writeBufferQueue.isEmpty()) {
//			VideoBuffer buf = writeBufferQueue.peek();
//			byte[] vBuf = new byte[size];
//			System.arraycopy(src, offset, vBuf, 0, size);
//			buf.set(vBuf);
//			Log.i(TAG, "writeBufferQueue set to id " + buf.id);
//			readBufferQueue.offer(writeBufferQueue.poll());
//			Message msg = Message.obtain();
//			msg.what = VideoHandler.MSG_BUFFER_PROCESS;
//			videoHandler.sendMessage(msg);
//			
//			return size;
//		}
//		
//		return 0;
//	}
	
	public synchronized byte[] read() {
		Log.i(TAG, "readBufferQueue.size():" + readBufferQueue.size());
		if (!readBufferQueue.isEmpty()) {
			VideoBuffer buf = readBufferQueue.peek();
			if (buf != null) {
				byte[] data = buf.get();
				buf.set(null);
				Log.i(TAG, "readBufferQueue read from id " + readBufferQueue.peek().id);
				// data should be used before VideoBuffer's data is set another byte array reference
				writeBufferQueue.offer(readBufferQueue.poll());
				return data;
			} else {
				readBufferQueue.poll();  // if head element is null, then remove it
			}
		}
		
		return null;
	}
	
	
	private class VideoBuffer {
		public int id;
		private byte[] data;
		
		public VideoBuffer() {}
		
		public void set(byte[] src) {
			data = src;
		}
		
		public byte[] get() {
			return data;
		}
		
		
		
	}
	
}
