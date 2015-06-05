package com.video.hdview.protocol.codec.encoder;

import java.nio.ByteOrder;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.AttributeKey;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolEncoder;
import org.apache.mina.filter.codec.ProtocolEncoderOutput;

import com.video.hdview.protocol.codec.decoder.OwspDecoder;
import com.video.hdview.protocol.message.OwspBegin;
import com.video.hdview.protocol.message.OwspEnd;

public class OwspEncoder implements ProtocolEncoder {
	private final AttributeKey PACKET_BUFFER = new AttributeKey(getClass(), "packet_buffer");
	private final AttributeKey SEQUENCE = new AttributeKey(OwspDecoder.class, "sequence4send");
	
	private PacketBuffer getPacket(IoSession session) {
		PacketBuffer packet = (PacketBuffer) session.getAttribute(PACKET_BUFFER);
		
		if (packet == null) {
			packet = new PacketBuffer();
			session.setAttribute(PACKET_BUFFER, packet);
		}
		
		return packet;
	}
	
	@Override
	public void encode(IoSession session, Object message,
			ProtocolEncoderOutput out) throws Exception {
		PacketBuffer p = getPacket(session);
		
		if ( message instanceof OwspBegin ) {  // begin collect TLV message
			p.getBuffer().clear();
			p.init();
			p.setState(STATE.EDIT);
		} else if ( message instanceof OwspEnd ) {
			// Set the sequence of packet
			AtomicLong seq = (AtomicLong)session.getAttribute(SEQUENCE);
			if (seq != null) {
				p.setSeq((int)seq.incrementAndGet());  // make sequence increase by 1
				session.setAttribute(SEQUENCE, seq);
			} else {
				p.setSeq(1);  // first packet
				session.setAttribute(SEQUENCE, new AtomicLong(p.getSeq()));
			}
			  
			p.setState(STATE.READY);
			
			out.write(p.getBuffer());  // send complete packet
			
			p.setState(STATE.CREATED);
			//p.getBuffer().clear();
			
		} else {
			if ( p.getState() == STATE.EDIT ) {
				p.append((byte[])message);
			}
		}
		
		session.setAttribute(PACKET_BUFFER, p);
	}

	@Override
	public void dispose(IoSession session) throws Exception {
		// TODO Auto-generated method stub

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
			buffer.putInt(0);  // initialize length
			buffer.putInt(0);  // initialize sequence
			len = 8;
		}

		public STATE getState() {
			return state;
		}
		
		public void setState(STATE state) {
			this.state = state;
			if ( state == STATE.READY) {
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
			buffer.flip();  // set limit to current position, then set position to zero
			
			buffer.order(ByteOrder.BIG_ENDIAN).putInt(len - 4);     // fill length
			buffer.order(ByteOrder.LITTLE_ENDIAN).putInt(seq);  // fill sequence
			buffer.rewind();  
		}

		public void append(byte[] a) {
			if (state == STATE.EDIT 
					&& a != null) {
				len += a.length;
				buffer.put(a);
			}
		} 
		
		public IoBuffer getBuffer() {
			return buffer;
		}
	}
	
	private enum STATE {
		CREATED,
		EDIT,
		READY
	};
	
}
