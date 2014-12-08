package com.starnet.snview.component;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteOrder;
import java.util.concurrent.atomic.AtomicLong;
import org.apache.mina.core.buffer.IoBuffer;
import com.starnet.snview.protocol.message.Constants;
import com.starnet.snview.protocol.message.LoginRequest;
import com.starnet.snview.protocol.message.OwspBegin;
import com.starnet.snview.protocol.message.OwspEnd;
import com.starnet.snview.protocol.message.PhoneInfoRequest;
import com.starnet.snview.protocol.message.VersionInfoRequest;

public class BufferSendManager {
	private static BufferSendManager singletonInstance;
	private PacketBuffer PACKET_BUFFER;
	private AtomicLong SEQUENCE;
	private OutputStream out;

	private BufferSendManager() {
	}

	public static BufferSendManager getInstance() {
		if (singletonInstance == null) {
			singletonInstance = new BufferSendManager();
		}
		return singletonInstance;
	}

	public void setOutStream(OutputStream out) {
		this.out = out;
	}

	public void write(Object message) {
		if (out == null) {
			return;
		}
		PacketBuffer p = getPacket();
		if (message instanceof OwspBegin) { // begin collect TLV message
			p.getBuffer().clear();
			p.init();
			p.setState(STATE.EDIT);
		} else if (message instanceof OwspEnd) {
			if (SEQUENCE != null) {
				p.setSeq((int) SEQUENCE.incrementAndGet()); // make sequence
															// increase by 1
			} else {
				p.setSeq(1); // first packet
				SEQUENCE = new AtomicLong(p.getSeq());
			}
			p.setState(STATE.READY);
			try {
				byte[] bufferToBeSent = new byte[p.getBuffer().remaining()];
				p.getBuffer().get(bufferToBeSent, 0, bufferToBeSent.length);
				out.write(bufferToBeSent); // send complete packet
			} catch (IOException e) {
				e.printStackTrace();
			}
			p.setState(STATE.CREATED);
		} else {
			if (p.getState() == STATE.EDIT) {
				p.append(serializeMessage(message));
			}
		}
	}

	private byte[] serializeMessage(Object msg) {
		IoBuffer outBuffer = IoBuffer.allocate(200).order(ByteOrder.LITTLE_ENDIAN);
		if (msg instanceof VersionInfoRequest) {
			outBuffer.putUnsignedShort(Constants.MSG_TYPE.VERSION_INFO_REQUEST);
			outBuffer.putUnsignedShort(Constants.MSG_LEN.VERSION_INFO_REQUEST);

			VersionInfoRequest message = (VersionInfoRequest) msg;
			outBuffer.putUnsignedShort(message.getVersionMajor());
			outBuffer.putUnsignedShort(message.getVersionMinor());
		} else if (msg instanceof PhoneInfoRequest) {
			outBuffer.putUnsignedShort(Constants.MSG_TYPE.PHONE_INFO_REQUEST);
			outBuffer.putUnsignedShort(Constants.MSG_LEN.PHONE_INFO_REQUEST);

			PhoneInfoRequest message = (PhoneInfoRequest) msg;
			IoBuffer tmp = IoBuffer.allocate(16).order(ByteOrder.LITTLE_ENDIAN);

			String equipmentIdentity = message.getEquipmentIdentity().trim();
			if (equipmentIdentity == null || equipmentIdentity.length() > 16) {
				throw new IllegalArgumentException(
						"Error equipmentIdentity of PhoneInfoRequest, be null or it's length is greater than 16.");
			}
			tmp.put(equipmentIdentity.getBytes());
			tmp.rewind();
			outBuffer.put(tmp); // put equipmentIdentity

			tmp.clear();
			tmp.fillAndReset(16);

			String equipmentOS = message.getEquipmentOS().trim();
			if (equipmentOS == null || equipmentOS.length() > 16) {
				throw new IllegalArgumentException(
						"Error equipmentOS of PhoneInfoRequest, be null or it's length is greater than 16.");
			}
			tmp.put(equipmentOS.getBytes());
			tmp.rewind();
			outBuffer.put(tmp); // put equipmentOS

			outBuffer.put((byte) message.getReserve1()); // put reserve1,
															// reserve2,
															// reserve3 and
															// reserve4
			outBuffer.put((byte) message.getReserve2());
			outBuffer.put((byte) message.getReserve3());
			outBuffer.put((byte) message.getReserve4());
		} else if (msg instanceof LoginRequest) {
			outBuffer.putUnsignedShort(Constants.MSG_TYPE.LOGIN_REQUEST);
			outBuffer.putUnsignedShort(Constants.MSG_LEN.LOGIN_REQUEST);

			LoginRequest message = (LoginRequest) msg;
			IoBuffer tmp = IoBuffer.allocate(32).order(ByteOrder.LITTLE_ENDIAN);

			String userName = message.getUserName().trim();
			if (userName == null || userName.length() > 32) {
				throw new IllegalArgumentException(
						"Error userName of LoginRequest, be null or it's length is greater than 32.");
			}
			tmp.put(userName.getBytes());
			tmp.rewind();
			outBuffer.put(tmp); // put userName

			tmp.sweep(); // reset the data to NUL, position to 0, and limit to
							// capacity
			tmp.limit(16);

			String password = message.getPassword().trim();
			if (password == null || password.length() > 16) {
				throw new IllegalArgumentException(
						"Error password of LoginRequest, be null or it's length is greater than 16.");
			}
			tmp.put(password.getBytes());
			tmp.rewind();
			outBuffer.put(tmp); // put password

			outBuffer.putInt(message.getDeviceId()); // put deviceId
			outBuffer.put((byte) 1); // should be set to 1 to be compatible with
										// the previous version
			outBuffer.put((byte) (message.getChannel() - 1)); // put channel,
																// start from 0
			outBuffer.put((byte) message.getReserve()[0]); // reserve[0]
			outBuffer.put((byte) message.getReserve()[1]); // reserve[1]
		}
		outBuffer.flip();
		byte[] data = new byte[outBuffer.remaining()];
		outBuffer.get(data, 0, data.length);
		return data;
	}

	private PacketBuffer getPacket() {
		if (PACKET_BUFFER == null) {
			PACKET_BUFFER = new PacketBuffer();
		}
		return PACKET_BUFFER;
	}

	private class PacketBuffer {
		private STATE state;
		private IoBuffer buffer;
		private int len = 0;
		private int seq = -1;

		public PacketBuffer() {
			state = STATE.CREATED;
			buffer = IoBuffer.allocate(1024).setAutoExpand(true);
			init();
		}

		public void init() {
			buffer.order(ByteOrder.LITTLE_ENDIAN);
			buffer.putInt(0); // initialize length
			buffer.putInt(0); // initialize sequence
			len = 8;
		}

		public STATE getState() {
			return state;
		}

		public void setState(STATE state) {
			this.state = state;
			if (state == STATE.READY) {
				wrapPacket();
			}
		}

		public int getSeq() {
			return seq;
		}

		public void setSeq(int seq) {
			this.seq = seq;
		}

		private void wrapPacket() {
			buffer.flip(); // set limit to current position, then set position
							// to zero
			buffer.order(ByteOrder.BIG_ENDIAN).putInt(len - 4); // fill length
			buffer.order(ByteOrder.LITTLE_ENDIAN).putInt(seq); // fill sequence
			buffer.rewind();
		}

		public void append(byte[] a) {
			if (state == STATE.EDIT && a != null) {
				len += a.length;
				buffer.put(a);
			}
		}

		public IoBuffer getBuffer() {
			return buffer;
		}
	}

	private enum STATE {
		CREATED, EDIT, READY
	};
}