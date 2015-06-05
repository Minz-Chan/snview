package com.video.hdview.images;

import java.util.ArrayList;
import java.util.List;

public class ImagesGroup {
	private String mDateInfo;
	private List<Image> mThumbnailList;

	public ImagesGroup(String dateInfo, List<Image> thumbnailList) {
		this.mDateInfo = dateInfo;
		this.mThumbnailList = thumbnailList;
	}

	public List<Object> getChildList() {
		ArrayList localArrayList = new ArrayList();
		localArrayList.add(new Object());
		return localArrayList;
	}

	public String getDateInfo() {
		return this.mDateInfo;
	}

	public int getGroupSize() {
		return this.mThumbnailList.size();
	}

	public List<Image> getThumbnailList() {
		return this.mThumbnailList;
	}
}
