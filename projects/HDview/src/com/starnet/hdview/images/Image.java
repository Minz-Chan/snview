package com.starnet.hdview.images;

public class Image {
	private String mDate;
	private String mImagePath;
	private boolean mIsSelected;
	private long mLastModified;
	private String mName;
	private String mThumbnailsPath;
	private ImageType mType;

	public Image() {
	}

	public Image(ImageType type, String name, String imagePath,
			String thumbnailPath, String date, long lastModified) {
		this.mType = type;
		this.mName = name;
		this.mImagePath = imagePath;
		this.mThumbnailsPath = thumbnailPath;
		this.mDate = date;
		this.mLastModified = lastModified;
	}

	public int compareToByLastModified(Image image) {
		int i;
		if (this.mLastModified < image.mLastModified) {
			i = -1;
		} else if (this.mLastModified > image.mLastModified) {
			i = 1;
		} else {
			i = 0;
		}
		return i;
	}

	public String getDate() {
		return this.mDate;
	}

	public String getImagePath() {
		return this.mImagePath;
	}

	public long getLastModified() {
		return this.mLastModified;
	}

	public String getName() {
		return this.mName;
	}

	public String getThumbnailsPath() {
		return this.mThumbnailsPath;
	}

	public ImageType getType() {
		return this.mType;
	}

	public boolean isSelected() {
		return this.mIsSelected;
	}

	public void setDate(String paramString) {
		this.mDate = paramString;
	}

	public void setImagePath(String paramString) {
		this.mImagePath = paramString;
	}

	public void setLastModified(long paramLong) {
		this.mLastModified = paramLong;
	}

	public void setName(String paramString) {
		this.mName = paramString;
	}

	public void setSelected(boolean paramBoolean) {
		this.mIsSelected = paramBoolean;
	}

	public void setThumbnailsPath(String paramString) {
		this.mThumbnailsPath = paramString;
	}

	public void setType(ImageType paramImageType) {
		this.mType = paramImageType;
	}

	public static enum ImageType {
		PICTURE, VIDEO
	}
}
