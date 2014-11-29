package com.starnet.snview.alarmmanager;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import com.hp.hpl.sparta.xpath.ThisNodeTest;
import com.starnet.snview.R;
import com.starnet.snview.realplay.RealplayActivity;
import com.starnet.snview.util.NetWorkUtils;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

@SuppressWarnings("deprecation")
public class AlarmDeviceAdapter extends BaseExpandableListAdapter {

	private Context context;
	private String deviceName;
	
	private List<AlarmShowItem> alarmInfoList;
	private AlarmImageDownLoadTask imgLoadTask;
	private final int IMAGE_LOAD_DIALOG = 0x0013;
	
	private ImageLoadThread imgThread;
	private Handler mHandler = new Handler(){

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			Bundle data = msg.getData();
			byte[]imgData = data.getByteArray("image");
			if (imgData != null) {
				String url = data.getString("url");
				Bitmap result = AlarmImageGetFromHttp.Bytes2Bimap(imgData);
				fileCache.saveBitmap(result, url);
				memoryCache.addBitmapToCache(url, result);
				Intent intent = new Intent();
				intent.putExtra("image", imgData);
				intent.putExtra("imageUrl", url);
				intent.setClass(context, AlarmImageActivity.class);
				context.startActivity(intent);
			}
		}
	};
	// private final int VIDEO_LOAD_DIALOG = 0x0014;

	public AlarmDeviceAdapter(List<AlarmShowItem> alarmInfoList,
			Context context, Handler handler) {
		this.context = context;
		this.mHandler = handler;
		this.alarmInfoList = alarmInfoList;
	}

	public AlarmDeviceAdapter(List<AlarmShowItem> alarmInfoList, Context context) {
		this.alarmInfoList = alarmInfoList;
		this.context = context;
	}

	@Override
	public int getGroupCount() {
		int size = 0;
		if (alarmInfoList != null) {
			size = alarmInfoList.size();
		}
		return size;
	}

	@Override
	public int getChildrenCount(int groupPosition) {
		return 1;
	}

	@Override
	public Object getGroup(int groupPosition) {
		return alarmInfoList.get(groupPosition);
	}

	@Override
	public Object getChild(int groupPosition, int childPosition) {
		return null;
	}

	@Override
	public long getGroupId(int groupPosition) {
		return groupPosition;
	}

	@Override
	public long getChildId(int groupPosition, int childPosition) {
		return 0;
	}

	@Override
	public boolean hasStableIds() {
		return false;
	}

	@Override
	public View getGroupView(int groupPosition, boolean isExpanded,
			View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = LayoutInflater.from(context).inflate(
					R.layout.alarm_listview_item_layout, null);
		}
		TextView deviceTxt = (TextView) convertView
				.findViewById(R.id.device_item_name);
		deviceTxt.setText(alarmInfoList.get(groupPosition).getAlarm()
				.getDeviceName());
		ImageView arrowImg = (ImageView) convertView
				.findViewById(R.id.alarm_arrow_img);
		if (alarmInfoList.get(groupPosition).isExpanded()) {
			arrowImg.setBackgroundResource(R.drawable.channel_listview_down_arrow_sel);
		} else {
			arrowImg.setBackgroundResource(R.drawable.channel_listview_right_arrow_sel);
		}
		return convertView;
	}

	@Override
	public View getChildView(int groupPosition, int childPosition,
			boolean isLastChild, View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = LayoutInflater.from(context).inflate(
					R.layout.alarm_listview_subitem_layout, null);
		}

		Button imgLoadBtn = (Button) convertView
				.findViewById(R.id.image_load_btn);
		Button vdoLoadBtn = (Button) convertView
				.findViewById(R.id.video_load_btn);
		Button contentBtn = (Button) convertView
				.findViewById(R.id.alarm_content_btn);
		final int pos = groupPosition;

		imgLoadBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (NetWorkUtils.checkNetConnection(context)) {
					getAlarmActivity().showDialog(IMAGE_LOAD_DIALOG);
					String imgUrl = alarmInfoList.get(pos).getAlarm()
							.getImageUrl();
					if (imgUrl == null) {
						imgUrl = "http://img.my.csdn.net/uploads/201402/24/1393242467_3999.jpg";// 网络测试地址
					}
					deviceName = alarmInfoList.get(pos).getAlarm().getDeviceName();
					
//					fileCache = new AlarmImageFileCache();
//					memoryCache = new AlarmImageMemoryCache(getAlarmActivity());
//					getBitmap(imgUrl);
					
					imgLoadTask = new AlarmImageDownLoadTask(imgUrl, context);
					imgLoadTask.setTitle(deviceName);
					imgLoadTask.start();
				} else {
					String netNotOpenContent = context
							.getString(R.string.alarm_net_notopen);
					Toast.makeText(context, netNotOpenContent,
							Toast.LENGTH_SHORT).show();
				}
			}
		});
		vdoLoadBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (NetWorkUtils.checkNetConnection(context)) {
					AlarmDevice device = alarmInfoList.get(pos).getAlarm();
					Intent intent = new Intent(context, RealplayActivity.class);
					intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					intent.putExtra(AlarmActivity.ALARM_DEVICE_DETAIL, device);
					context.startActivity(intent);
					((AlarmActivity) context).finish();
				} else {
					String netNotOpenContent = context
							.getString(R.string.alarm_net_notopen);
					Toast.makeText(context, netNotOpenContent,
							Toast.LENGTH_SHORT).show();
				}
			}
		});

		contentBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				AlarmDevice device = alarmInfoList.get(pos).getAlarm();
				Intent intent = new Intent(context, AlarmContentActivity.class);
				intent.putExtra("alarmDevice", device);
				context.startActivity(intent);
			}
		});

		return convertView;
	}

	// 采用缓存机制来保存图片:内存-文件-网络的缓存机制
	protected byte[] getImage(Bitmap bitmap) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
		return baos.toByteArray();
	}

	private AlarmImageFileCache fileCache;
	private AlarmImageMemoryCache memoryCache;

	/*** 获得一张图片,从三个地方获取,首先是内存缓存,然后是文件缓存,最后从网络获取 ***/
	protected Bitmap getBitmap(String url) {
		Bitmap result = memoryCache.getBitmapFromCache(url);// 从内存缓存中获取图片
		if (result == null) {
			result = fileCache.getImage(url);// 文件缓存中获取
			if (result == null) {
				// 从网络获取
				try {
//					result = AlarmImageGetFromHttp.downloadBitmap(url);
					imgThread = new ImageLoadThread(url,mHandler);
					imgThread.start();
//					imgLoadTask = new AlarmImageDownLoadTask(url, context);
//					imgLoadTask.setTitle(deviceName);
//					imgLoadTask.start();
				} catch (Exception e) {
					e.printStackTrace();
				}
//				if (result != null) {
////					fileCache.saveBitmap(result, url);
////					memoryCache.addBitmapToCache(url, result);
//				}
			} else {
				memoryCache.addBitmapToCache(url, result);// 添加到内存缓存
			}
		}
		return result;
	}

	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition) {
		return false;
	}

	private AlarmActivity getAlarmActivity() {
		return (AlarmActivity) context;
	}

	public void cancel(boolean isCanceld) {
		if (imgLoadTask != null) {
			imgLoadTask.setCancel(isCanceld);
		}
	}
	
	class ImageLoadThread extends Thread {
		private String imageUrl;
		private Handler handler;
		public ImageLoadThread(String imageUrl,Handler handler){
			this.imageUrl = imageUrl;
			this.handler = handler;
		}
		@Override
		public void run() {
			super.run();
			byte[] imgData = null;
			Message msg = new Message();
			try {
				imgData = AlarmImageGetFromHttp.startDownloadImage(imageUrl);
				if (imgData!=null) {
//					AlarmImageGetFromHttp.Bytes2Bimap(imgData);
					Bundle data = new Bundle();
					data.putByteArray("image", imgData);
					data.putString("url", imageUrl);
					msg.setData(data);
					handler.sendMessage(msg);
				}else {
					
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
