/*
 * FileName:SocketInputStream.java
 * 
 * Package:com.starsecurity.component
 * 
 * Date:2013-04-25
 * 
 * Copyright: Copyright (c) 2013 Minz.Chan
 */
package com.starnet.snview.playback.utils;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

public class SocketInputStream extends FilterInputStream {

	protected SocketInputStream(InputStream in) {
		super(in);
	}

	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		int iRead = 0;
		int iShouldBeRead = len;
		while (iRead < iShouldBeRead) {
			iRead += super.read(b, iRead, iShouldBeRead - iRead);
		} 
		return iRead;
	}
}