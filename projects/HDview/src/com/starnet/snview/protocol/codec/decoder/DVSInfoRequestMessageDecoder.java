package com.starnet.snview.protocol.codec.decoder;

import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;

import com.starnet.snview.protocol.message.Constants;
import com.starnet.snview.protocol.message.DVSInfoRequest;
import com.starnet.snview.protocol.message.OwspDate;

public class DVSInfoRequestMessageDecoder extends
		AbstractMessageDecoder<DVSInfoRequest> {

	@Override
	protected int getMessageType() {
		return Constants.MSG_TYPE.DVS_INFO_REQUEST;
	}

	@Override
	protected DVSInfoRequest decodeBody(IoSession session, IoBuffer body)
			throws Exception {
		DVSInfoRequest d = new DVSInfoRequest();
		CharsetDecoder decoder = (Charset.forName("UTF-8")).newDecoder();

		d.setCompanyIdentity(body.getString(16, decoder));
		d.setEquipmentIdentity(body.getString(16, decoder));
		d.setEquipmentName(body.getString(16, decoder));
		d.setEquipmentVersion(body.getString(16, decoder));
		
		OwspDate od = new OwspDate();
		od.setYear(body.getShort());
		od.setMonth(body.get());
		od.setDay(body.get());
		d.setOwspDate(od);
		
		d.setChannleNumber(body.get());
		d.setReserve1(body.get());
		d.setReserve2(body.get());
		d.setReserve3(body.get());

		return d;
	}

}
