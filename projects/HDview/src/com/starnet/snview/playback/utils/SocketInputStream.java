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

import android.annotation.SuppressLint;
import java.io.FileOutputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

@SuppressLint("SdCardPath")
public class SocketInputStream extends FilterInputStream {

	public SocketInputStream(InputStream in) {
		super(in);
	}

	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		int iRead = 0;
		int iShouldBeRead = len;
		while (iRead < iShouldBeRead) {
			iRead += super.read(b, iRead, iShouldBeRead - iRead);
		}
//		saveBytesToFile(b);
		return iRead;
	}

	private final String fileName = "/mnt/sdcard/audio_vedio_datas.txt";

	/** 保存byte数据到指定的文件中 **/
	protected void saveBytesToFile(byte[] b) {
		OutputStream fout = null;
		try {
			// 打开一个写文件器，构造函数中的第二个参数true表示以追加形式写文件
			fout = new FileOutputStream(fileName, true);
			fout.write(b);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (fout != null) {
					fout.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}