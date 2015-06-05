package com.video.hdview.protocol.codec.decoder;

import java.nio.ByteOrder;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.AttributeKey;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.CumulativeProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;


public class OwspDecoder extends CumulativeProtocolDecoder {
	private final AttributeKey SEQUENCE = new AttributeKey(getClass(), "sequence4recv");
	
	@Override
	protected boolean doDecode(IoSession session, IoBuffer in,
			ProtocolDecoderOutput out) throws Exception {
		int oldPos = in.position();
		
		// If buffer length is greater than owsp header's 		
		if ( in.remaining() < 8 ) {
			return false;
		}
		
		
		long length = in.order(ByteOrder.BIG_ENDIAN).getUnsignedInt();
		long seq = in.order(ByteOrder.LITTLE_ENDIAN).getUnsignedInt();
		
		// Check whether sequence of packet is correct
		AtomicLong s = (AtomicLong)session.getAttribute(SEQUENCE);
		if ( s == null) {
			session.setAttribute(SEQUENCE, new AtomicLong(seq));
		} else {
			if (s.get() != (seq - 1)) {
				in.position(oldPos);
				throw new IllegalStateException("Unexpected packet sequence "
						+ seq + ", sequence " + (s.get() + 1) + " is expected");
			}
		}
		
		// Check if buffer contains a complete packet
		if ( in.remaining() < length - 4) {
			in.position(oldPos);
			return false;
		}
		
		// Make sequence increase by 1
		if ( s != null ) {
			s.incrementAndGet();
			session.setAttribute(SEQUENCE, s); // save sequence
		}
		
		byte[] bodyData = new byte[(int)(length - 4)];
		in.get(bodyData);
		
		out.write(IoBuffer.wrap(bodyData));  // transfer TLVs to next filter
		
//		if (in.remaining() < 8) {
//			return false;
//		}
		
		return true;
	}

}
