package com.starnet.snview.images;

import java.io.File;
import java.io.FilenameFilter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;







import com.starnet.snview.images.Image.ImageType;

import android.util.Log;

public class ImagesManager {
	private static final String TAG = "ImagesManager";

	private static ImagesManager mInstance;

	static final int THUMBNAIL_COLUMNS_NUM = 4;
	private static FilenameFilter mCaptureFilter = new FilenameFilter() {
		@Override
		public boolean accept(File dir, String filename) {
			return filename.endsWith(".jpg");
		}
	};

	private static FilenameFilter mRecordFilter = new FilenameFilter() {
		@Override
		public boolean accept(File dir, String filename) {
			return filename.endsWith(".mp4");
		}
	};

	private static FilenameFilter mDateFolderFilter = new FilenameFilter() {
		@Override
		public boolean accept(File dir, String filename) {
			try {
				new SimpleDateFormat("yyyy-MM-dd").parse(filename);
				return true;
			} catch (Exception e) {
				e.printStackTrace();
				return false;
			}
		}
	};

	private static Comparator<String> mTreeComparator = new Comparator<String>() {

		@Override
		public int compare(String lhs, String rhs) {
			return rhs.compareTo(lhs);
		}
	};

	private final List<String> mDateList = new ArrayList();
	private final List<String> mVideoNameList = new ArrayList();
	private final TreeMap<String, List<Image>> mImagesMap = new TreeMap(
			mTreeComparator);

	
	public boolean addImage(Image image) {
		boolean result = false;
		
		if (image == null || image.getDate() == null) {
			return false;
		}
		
		Iterator it = mDateList.iterator();
		
		while (it.hasNext()) {
			String item = (String) it.next();
			
			if (item.endsWith(image.getDate())) {
				((LinkedList)(mImagesMap.get(item))).add(0, image);
				result = true;
				break;
			}
		}
		
		if (!result) {
			mDateList.add(image.getDate());
			LinkedList imageList = new LinkedList();
			imageList.add(image);
			mImagesMap.put(image.getDate(), imageList);
		}		
		return result;
	}
	
	public void deleteImage(Image image) {
		List imageList = (List) mImagesMap.get(image.getDate());

		if (imageList != null) {
			imageList.remove(image);

			File imageFile = new File(image.getImagePath());
			File thumbnailFile = new File(image.getThumbnailsPath());
			imageFile.delete();
			thumbnailFile.delete();

			File captureFolder = new File(
					LocalFileUtils.getCaptureFolderPathForDate(image.getDate()));
			File recordFolder = new File(
					LocalFileUtils.getRecordFolderPathForDate(image.getDate()));

			if (captureFolder.list() != null
					&& captureFolder.list().length == 0) {
				captureFolder.delete();
			}

			if (recordFolder.list() != null && recordFolder.list().length == 0) {
				recordFolder.delete();
			}

		}
	}
	
	/**
	 * 删除出现在待删除列表imageList的文件, 并且删除folderName对应的截图和录音文件夹
	 * @param folderName 文件夹名称
	 * @param imageList 待删除列表
	 */
	private void deleteImages(String folderName, List<Image> imageList) {
		Iterator<Image> it = imageList.iterator();
		while (it.hasNext()) {
			Image image = it.next();
			File imageFile = new File(image.getImagePath());
			File thumbnailFile = new File(image.getThumbnailsPath());
			imageFile.delete();
			thumbnailFile.delete();
		}

		String str1 = LocalFileUtils.getCaptureFolderPathForDate(folderName);
		String str2 = LocalFileUtils.getRecordFolderPathForDate(folderName);
		File file1 = new File(str1);
		File file2 = new File(str2);
		file1.delete();
		file2.delete();
	}
	@SuppressWarnings("rawtypes")
	public void deleteSelectedImages() {
		ArrayList foldersToBeDeleted = new ArrayList();
		LinkedList selectedImages = new LinkedList();
		Iterator itEntrySet = mImagesMap.entrySet().iterator();
		
		while (itEntrySet.hasNext()) {
			
			Map.Entry entry = (Map.Entry)itEntrySet.next();
			String strDate = (String) entry.getKey();
			List imageList = (List) entry.getValue();
			Iterator itImageList = imageList.iterator();
			
			while (itImageList.hasNext()) {
				Image image = (Image) itImageList.next();
				
				if (image.isSelected()) {
					selectedImages.add(image);
				}
			}
			
			if (!selectedImages.isEmpty()) {
				imageList.removeAll(selectedImages);
				deleteImages(strDate, selectedImages);
			}
			
			if (imageList.isEmpty()) {
				foldersToBeDeleted.add(strDate);
			}
			
		}
		

		Iterator it3 = foldersToBeDeleted.iterator();
		
		while (it3.hasNext()) {
			String folderName = (String) it3.next();
			
			mDateList.remove(folderName);
			mImagesMap.remove(folderName);
		}

	    
	}

	//删除空的文件夹
	public void deleteNullFolders(String filePath) {
		File file = new File(filePath);
		if (!file.exists()) {
			return ;
		}
		String[] pathList = file.list();
		if (pathList.length == 0) {
			file.delete();
		}
	}
	
	//删除文件，但是不考虑空的情况...
	public void deleteSelectedImages_copy() {
		ArrayList foldersToBeDeleted = new ArrayList();
		LinkedList selectedImages = new LinkedList();
		Iterator itEntrySet = mImagesMap.entrySet().iterator();
		
		while (itEntrySet.hasNext()) {
			
			Map.Entry entry = (Map.Entry)itEntrySet.next();
			String strDate = (String) entry.getKey();
			List imageList = (List) entry.getValue();
			Iterator itImageList = imageList.iterator();
			
			while (itImageList.hasNext()) {
				Image image = (Image) itImageList.next();
				
				if (image.isSelected()) {
					selectedImages.add(image);
				}
			}
			
			if (!selectedImages.isEmpty()) {
				imageList.removeAll(selectedImages);
				deleteImages(strDate, selectedImages);
			}
		}
	}
	
	public static ImagesManager getInstance() {
		if (mInstance == null) {
			mInstance = new ImagesManager();
		}
		return mInstance;
	}

	public void loadLocalImages() {//加载本地图片
		this.mDateList.clear();
		this.mImagesMap.clear();
		String str1 = LocalFileUtils.getCaptureFolderRootPath();//获取capture路径
		String str2 = LocalFileUtils.getRecordFolderRootPath();//获取record路径
		loadImageFiles(Image.ImageType.VIDEO, str2);
		loadImageFiles(Image.ImageType.PICTURE, str1);
		Collections.sort(this.mDateList, new Comparator<String>() {
			@Override
			public int compare(String lhs, String rhs) {
				return rhs.compareTo(lhs);
			}
		});
	}

	//构建image文件，将不同image加入到对应的日期文件中
	private void loadImageFiles(Image.ImageType imageType, String path) {
		File folder = new File(path);
		if ((!folder.exists()) || (!folder.isDirectory())) {
			Log.i("ImagesManager", "dirPath not exist: " + path);
			return;
		}
		String thumbnailFolderPath = LocalFileUtils.getThumbnailsFolderPath();
		FilenameFilter filenameFilter = null;
		if (imageType == Image.ImageType.PICTURE) {//如果图像类型为PICTURE则构造thumbnails-->capture表;否则，构造thumbnails->capture->record表
			filenameFilter = mCaptureFilter;
		} else {
			filenameFilter = mRecordFilter;
		}

		for (File dateFolder : folder.listFiles(mDateFolderFilter)) {
			if (dateFolder.isDirectory()) {
				File[] dateFolderFiles = dateFolder
						.listFiles(filenameFilter);
				if (dateFolderFiles.length != 0) {
					String dateFolderName = dateFolder.getName();
					Object images = (List) this.mImagesMap.get(dateFolderName);
					
					if (images == null) {
						images = new LinkedList();
						this.mImagesMap.put(dateFolderName,(List<Image>) images);//加入树中
						this.mDateList.add(dateFolderName);
					}
					
					int count = dateFolderFiles.length;
					for (int i = 0; i < count; i++) {
						File f1 = dateFolderFiles[i];
						if (f1.isFile()) {
							String f1Name = f1.getName();
							Image imgTmp = new Image(imageType,
									f1.getName(), f1.getAbsolutePath(),
									thumbnailFolderPath + File.separator
											+ getThumbnailsName(f1Name),
									dateFolderName, f1.lastModified());
							
							if (imageType == Image.ImageType.VIDEO) {
								mVideoNameList.add(f1.getName().replace(
										LocalFileUtils.RECORD_EXT_NAME, ""));
								((List) images).add(imgTmp);
							} else {
								
								if (!mVideoNameList.contains(f1.getName()
									.replace(LocalFileUtils.PICTURE_EXT_NAME, ""))) {
									((List) images).add(imgTmp);
								}
							}
						}
					}

					Comparator<Image> comparator = new Comparator<Image>() {
						public int compare(Image image1, Image image2) {
							return image2.compareToByLastModified(image1);
						}
					};
					Collections.sort((List) images, comparator);

				}
			}
		}
		

	}

	private String getThumbnailsName(String fileName) {
		int i = fileName.lastIndexOf(".");
		if (i != -1) {
			return fileName.substring(0, i) + ".jpg";
		} else {
			return "";
		}
	}

	public List<String> getDateList() {
		return mDateList;
	}

	public List<Image> getImageListForDate(String strDate) {
		return mImagesMap.get(strDate);
	}

	
	
}
