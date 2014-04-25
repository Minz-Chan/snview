package com.starnet.hdview.images;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;

import com.starnet.hdview.global.GlobalApplication;
import com.starnet.hdview.util.FileUtility;
import com.starnet.hdview.util.SDCardUtils;

public class LocalFileUtils {
	public static final String CAPTURE_FOLDER_NAME = "capture";
	public static final String RECORD_FOLDER_NAME = "record";
	public static final String THUMBNAILS_FOLDER_NAME = ".thumbnails";
	public static final String PICTURE_EXT_NAME = ".jpg";
	public static final String RECORD_EXT_NAME = ".mp4";

	public static boolean createDirectory(String directory) {
		try {
			return FileUtility.makeDirectory(directory, true);
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}

	public static String getCaptureFileFullPath(String fileName,
			boolean isCreateFolder) {
		String str = getCaptureFolderPathToday();
		if (isCreateFolder) {
			createDirectory(str);
		}
		return str + File.separator + fileName + PICTURE_EXT_NAME;
	}

	public static String getCaptureFolderPathForDate(String strDate) {
		return getLocalFileRootPath() + "/" + CAPTURE_FOLDER_NAME + "/"
				+ strDate;
	}

	public static String getCaptureFolderPathToday() {
		StringBuilder sbCaptureFolderPath = new StringBuilder(
				String.valueOf(getLocalFileRootPath())).append("/")
				.append(CAPTURE_FOLDER_NAME).append("/");
		return sbCaptureFolderPath.append(
				String.format("%tF", Calendar.getInstance())).toString();
	}

	public static String getCaptureFolderRootPath() {
		return getLocalFileRootPath() + "/" + CAPTURE_FOLDER_NAME;
	}

	public static String getFormatedFileName(String paramString, int paramInt) {
		Calendar localCalendar = Calendar.getInstance();
		Object[] arrayOfObject1 = new Object[7];
		arrayOfObject1[0] = Integer.valueOf(localCalendar.get(1));
		arrayOfObject1[1] = Integer.valueOf(1 + localCalendar.get(2));
		arrayOfObject1[2] = Integer.valueOf(localCalendar.get(5));
		arrayOfObject1[3] = Integer.valueOf(localCalendar.get(11));
		arrayOfObject1[4] = Integer.valueOf(localCalendar.get(12));
		arrayOfObject1[5] = Integer.valueOf(localCalendar.get(13));
		arrayOfObject1[6] = Integer.valueOf(localCalendar.get(14));
		String str = String.format("%04d%02d%02d%02d%02d%02d%03d",
				arrayOfObject1);
		Object[] arrayOfObject2 = new Object[3];
		arrayOfObject2[0] = paramString;
		arrayOfObject2[1] = Integer.valueOf(paramInt);
		arrayOfObject2[2] = str;
		return String.format("%s_%02d_%s", arrayOfObject2);
	}

	public static String getLocalFileRootPath() {
//		return SDCardUtils.getSDCardPath()
//				+ GlobalApplication.getInstance().getAppName();
		return SDCardUtils.getSDCardPath()
				+ "iVMS-4500";
		
	}

	public static String getRecordFileFullPath(String paramString,
			boolean paramBoolean) {
		String str = getRecordFolderPathToday();
		if (paramBoolean) {
			createDirectory(str);
		}
		return str + File.separator + paramString + RECORD_EXT_NAME;
	}

	public static String getRecordFolderPathForDate(String strDate) {
		return getLocalFileRootPath() + "/" + RECORD_FOLDER_NAME + "/"
				+ strDate;
	}

	public static String getRecordFolderPathToday() {
		StringBuilder sbRecordFolderPath = new StringBuilder(
				String.valueOf(getLocalFileRootPath())).append("/")
				.append(RECORD_FOLDER_NAME).append("/");
		return sbRecordFolderPath.append(
				String.format("%tF", Calendar.getInstance())).toString();
	}

	public static String getRecordFolderRootPath() {
		return getLocalFileRootPath() + "/" + RECORD_FOLDER_NAME;
	}

	public static String getThumbnailsFileFullPath(String paramString,
			boolean paramBoolean) {
		String str = getThumbnailsFolderPath();
		if (paramBoolean) {
			createDirectory(str);
		}
		return str + File.separator + paramString + PICTURE_EXT_NAME;
	}

	public static String getThumbnailsFolderPath() {
		return getLocalFileRootPath() + "/" + THUMBNAILS_FOLDER_NAME;
	}
}
