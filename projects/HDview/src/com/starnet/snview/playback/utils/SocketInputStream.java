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

import java.io.FileWriter;
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
		saveBytesToFile(b);
		return iRead;
	}

	private final String fileName = "/data/data/com.starnet.snview/audio_vedio_datas.txt";

	/** 保存byte数据到指定的文件中 **/
	private void saveBytesToFile(byte[] b) {
		FileWriter writer = null;
		try {
			// 打开一个写文件器，构造函数中的第二个参数true表示以追加形式写文件
			writer = new FileWriter(fileName, true);
			for (int i = 0; i < b.length; i++) {
				writer.write("" + b[i]);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (writer != null) {
					writer.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}