package com.video.hdview.images.utils;

import java.io.File;

public class FileManager {
	
	private static FileManager mFileUtils;
	
	private FileManager(){ }
	
	public static FileManager getInstance(){
		if (mFileUtils == null) {
			mFileUtils = new FileManager();
		}
		return mFileUtils;
	}
	
	//检测文档是否为空，如果为空返回true；否则，返回FALSE；
	public boolean checkFileIsNull(String filePath){
		boolean isNull = false;
		File file = new File(filePath);
		if (file.exists()) {
			String[] files = file.list();
			if (files.length == 0) {
				isNull = true;
			}
		}
		return isNull;
	}
	
	//删除文件
	public void deleteFile(String filePath){
		File file = new File(filePath);
		if (file.exists()) {
			file.delete();
		}
	}
}
