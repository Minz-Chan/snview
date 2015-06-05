package com.video.hdview.images;

import java.util.Iterator;
import java.util.List;

import com.video.hdview.R;
import com.video.hdview.global.GlobalApplication;
import com.video.hdview.images.Image.ImageType;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ImageView.ScaleType;

public class ImagesGridViewAdapter extends BaseAdapter {
	private static final String TAG = "ImagesGridViewAdapter";
	private static final int THUMBNAIL_COLUMNS_NUM = 3;
	private Context mContext;
	private List<Image> mImageList;
	private LayoutInflater mLayoutInflater;

	ImagesGridViewAdapter(Context context, List<Image> imgList) {
		this.mContext = context;
		this.mImageList = imgList;
		this.mLayoutInflater = ((LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE));
	}

	@Override
	public int getCount() {
		return mImageList.size();
	}

	@Override
	public Object getItem(int position) {
		return mImageList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = this.mLayoutInflater.inflate(R.layout.images_listview_thumbnail_item_layout, null);
		}

		int width, height;

		width = height = GlobalApplication.getInstance().getScreenWidth()/THUMBNAIL_COLUMNS_NUM;

		RelativeLayout imageThumbnaiItemlLayout = (RelativeLayout) convertView.findViewById(R.id.images_thumbnail_item_layout);
		LinearLayout.LayoutParams imageThumbnaiItemParams = (LinearLayout.LayoutParams) imageThumbnaiItemlLayout.getLayoutParams();
		imageThumbnaiItemParams.width = width;
		imageThumbnaiItemParams.height = height;
		imageThumbnaiItemlLayout.setLayoutParams(imageThumbnaiItemParams);

		Image image = (Image) this.mImageList.get(position);
		
		ImageView imageView = (ImageView) convertView.findViewById(R.id.images_thumbnail_item_imageview);
		ImageLoader.getInstance().loadImages(image.getThumbnailsPath(),
				imageView, true, new ImageLoader.ImgCallback() {
					public void refresh(Bitmap bitmap, ImageView imageView) {
						if (imageView != null) {
							imageView.setImageDrawable(new BitmapDrawable(bitmap));
						}
					}
				});
		ImageView imageViewSeleted = (ImageView) convertView.findViewById(R.id.images_thumbnail_item_selected_bg_imageview);
		if (image.isSelected()) {
			imageViewSeleted.setVisibility(View.VISIBLE);
		} else {
			imageViewSeleted.setVisibility(View.GONE);
		}
		ImageView imageViewVideo = (ImageView) convertView.findViewById(R.id.images_thumbnail_item_video_bg_imageview);
		if (image.getType() == Image.ImageType.VIDEO) {//显示Video图标
			imageViewVideo.setVisibility(View.VISIBLE);
		}
		return convertView;
	}
		
	protected boolean checkThumnail(Image image, List<Image> mImageList2) {
		boolean isSameThumbnail = false;
		Iterator<Image> imageIterator = mImageList2.iterator();
		while (imageIterator.hasNext()) {
			Image image2 = imageIterator.next();
			if (image.getThumbnailsPath().equals(image2.getThumbnailsPath())) {
				isSameThumbnail = true;
				break;
			}
		}
		return isSameThumbnail;
	}
}