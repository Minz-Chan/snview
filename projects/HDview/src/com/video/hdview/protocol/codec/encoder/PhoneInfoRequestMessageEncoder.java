package com.video.hdview.protocol.codec.encoder;

import java.nio.ByteOrder;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;

import com.video.hdview.protocol.message.Constants;
import com.video.hdview.protocol.message.PhoneInfoRequest;

public class PhoneInfoRequestMessageEncoder extends AbstractMessageEncoder<PhoneInfoRequest> {

	@Override
	protected void encodeBody(IoSession session, PhoneInfoRequest message,
			IoBuffer out) {
		IoBuffer tmp = IoBuffer.allocate(16).order(ByteOrder.LITTLE_ENDIAN);
		
		String equipmentIdentity = message.getEquipmentIdentity().trim();
		if (equipmentIdentity == null || equipmentIdentity.length() > 16) {
			throw new IllegalArgumentException(
					"Error equipmentIdentity of PhoneInfoRequest, be null or it's length is greater than 16.");
		}
		tmp.put(equipmentIdentity.getBytes());
		tmp.rewind();
		out.put(tmp);  // put equipmentIdentity
		
		tmp.clear();
		tmp.fillAndReset(16);
		
		String equipmentOS = message.getEquipmentOS().trim();
		if (equipmentOS == null || equipmentOS.length() > 16) {
			throw new IllegalArgumentException(
					"Error equipmentOS of PhoneInfoRequest, be null or it's length is greater than 16.");
		}
		tmp.put(equipmentOS.getBytes());
		tmp.rewind();
		out.put(tmp);  // put equipmentOS
		
		out.put((byte)message.getReserve1());  // put reserve1, reserve2, reserve3 and reserve4
		out.put((byte)message.getReserve2());
		out.put((byte)message.getReserve3());
		out.put((byte)message.getReserve4());
	}

	@Override
	protected int getMessageLength() {
		return Constants.MSG_LEN.PHONE_INFO_REQUEST;
	}

	@Override
	protected int getMessageType() {
		return Constants.MSG_TYPE.PHONE_INFO_REQUEST;
	}

}
