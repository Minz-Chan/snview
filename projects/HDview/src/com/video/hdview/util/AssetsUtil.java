package com.video.hdview.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import android.content.Context;

public class AssetsUtil {
	/**
	 * 复制assets中的文件到指定目录下
	 * 
	 * @param context
	 * @param assetsFileName
	 * @param targetPath
	 * @return
	 */
	public static boolean copyAssetData(Context context, String assetsFileName,
			String targetPath) {
		try {
			InputStream inputStream = context.getAssets().open(assetsFileName);
			FileOutputStream output = new FileOutputStream(targetPath
					+ File.separator + assetsFileName);
			byte[] buf = new byte[10240];
			int count = 0;
			while ((count = inputStream.read(buf)) > 0) {
				output.write(buf, 0, count);
			}
			output.close();
			inputStream.close();
		} catch (IOException e) {

			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		return true;
	}

	public static void copyAssetsDir(Context context, String dirName,
			String targetFolder) {
		try {
			File f = new File(targetFolder + File.separator + dirName);
			if (!f.exists() && !f.isDirectory())
				f.mkdirs();

			String[] filenames = context.getAssets().list(dirName);
			InputStream inputStream = null;
			for (String filename : filenames) {
				String name = dirName + File.separator + filename;

				// 如果是文件，则直接拷贝，如果是文件夹，就会抛出异常，捕捉后递归拷贝
				try {
					inputStream = context.getAssets().open(name);
					inputStream.close();
					copyAssetData(context, name, targetFolder);
				} catch (Exception e) {
					copyAssetsDir(context, name, targetFolder);
				} finally {
					inputStream = null;
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
