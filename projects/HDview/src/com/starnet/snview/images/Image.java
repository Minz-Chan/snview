package com.starnet.snview.images;

import android.os.Parcel;
import android.os.Parcelable;

public class Image implements Parcelable {//
	private String mDate;
	private String mImagePath;//图片的路径
	private boolean mIsSelected;
	private long mLastModified;
	private String mName;
	private String mThumbnailsPath;//图片的缩略图路径
	private ImageType mType;
	private boolean isThumnailUsed;//根据该标志位确定在缩略图中是否已经显示：true表示已经显示，FALSE表示缩略图尚未显示
	

	public Image() {
	}
	
	private Image(Parcel p) {
		this.mDate = p.readString();
		this.mImagePath = p.readString();
		this.mLastModified = p.readLong();
		this.mName = p.readString();
		this.mThumbnailsPath = p.readString();
		this.mType = (ImageType) p.readValue(null);
		//this.mType = 
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

	public void setDate(String date) {
		this.mDate = date;
	}

	public void setImagePath(String imagePath) {
		this.mImagePath = imagePath;
	}

	public void setLastModified(long lastModified) {
		this.mLastModified = lastModified;
	}

	public void setName(String name) {
		this.mName = name;
	}

	public void setSelected(boolean isSelected) {
		this.mIsSelected = isSelected;
	}

	public void setThumbnailsPath(String thumbPath) {
		this.mThumbnailsPath = thumbPath;
	}

	public void setType(ImageType type) {
		this.mType = type;
	}

	public static enum ImageType {
		PICTURE, VIDEO
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(mDate);
		dest.writeString(mImagePath);
		
//		dest.writeValue(mIsSelected);//????
		dest.writeLong(mLastModified);
		dest.writeString(mName);
		dest.writeString(mThumbnailsPath);
		//dest.writeInt((int)mType);
		dest.writeValue(mType);
//		dest.writeValue(mType);//????
	}
	
	 public static final Creator<Image> CREATOR = new Creator<Image>() {

		@Override
		public Image createFromParcel(Parcel source) {
			return new Image(source);
		}

		@Override
		public Image[] newArray(int size) {
			return new Image[size];
		}
	 };


	public boolean isThumnailUsed() {
		return isThumnailUsed;
	}

	public void setThumnailUsed(boolean isShowByThumnail) {
		this.isThumnailUsed = isShowByThumnail;
	}
}
