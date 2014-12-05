package com.starnet.snview.alarmmanager;

import java.util.List;

import com.starnet.snview.R;
import com.starnet.snview.realplay.RealplayActivity;
import com.starnet.snview.util.NetWorkUtils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
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

@SuppressLint("HandlerLeak")
@SuppressWarnings("deprecation")
public class AlarmDeviceAdapter extends BaseExpandableListAdapter {

	private Context context;
	private String deviceName;
	private List<Integer> indexes;
	private final int REQUESTCODE = 0x0023;
	private List<AlarmShowItem> alarmInfoList;
	private AlarmImageDownLoadTask imgLoadTask;
	private final int IMAGE_LOAD_DIALOG = 0x0013;

	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			Toast.makeText(context, R.string.alarm_img_load_timeout,Toast.LENGTH_LONG).show();
		}
	};

	public AlarmDeviceAdapter(List<AlarmShowItem> alarmInfoList,
			Context context, Handler handler) {
		this.context = context;
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
		return childPosition;
	}

	@Override
	public boolean hasStableIds() {
		return false;
	}

	@Override
	public View getGroupView(int groupPosition, boolean isExpanded,
			View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = LayoutInflater.from(context).inflate(R.layout.alarm_listview_item_layout, null);
		}
		TextView deviceTxt = (TextView) convertView.findViewById(R.id.device_item_name);
		deviceTxt.setText(alarmInfoList.get(groupPosition).getAlarm().getDeviceName());

		TextView alarm_time = (TextView) convertView.findViewById(R.id.alarm_time);
		TextView alarm_type = (TextView) convertView.findViewById(R.id.alarm_type);
		String time = alarmInfoList.get(groupPosition).getAlarm().getAlarmTime().replaceAll(",", " ");
		time = time.replaceAll(",", " ");
		alarm_time.setText(time);
		alarm_type.setText(alarmInfoList.get(groupPosition).getAlarm().getAlarmType());
		ImageView arrowImg = (ImageView) convertView
				.findViewById(R.id.alarm_arrow_img);
		if (isExpanded) {
			arrowImg.setBackgroundResource(R.drawable.channel_listview_down_arrow_sel);
		} else {
			arrowImg.setBackgroundResource(R.drawable.channel_listview_right_arrow_sel);
		}	
		convertView.setTag(R.id.alarm_arrow_img, groupPosition);//为父元素设置标签
		return convertView;
	}

	
	@Override
	public View getChildView(int groupPosition, int childPosition,
			boolean isLastChild, View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = LayoutInflater.from(context).inflate(R.layout.alarm_listview_subitem_layout, null);
		}
		Button imgLoadBtn = (Button) convertView.findViewById(R.id.image_load_btn);
		Button vdoLoadBtn = (Button) convertView.findViewById(R.id.video_load_btn);
		Button contentBtn = (Button) convertView.findViewById(R.id.alarm_content_btn);
		final int pos = groupPosition;

		imgLoadBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (NetWorkUtils.checkNetConnection(context)) {
					getAlarmActivity().showDialog(IMAGE_LOAD_DIALOG);
					deviceName = alarmInfoList.get(pos).getAlarm().getDeviceName();
					String imgUrl = alarmInfoList.get(pos).getAlarm().getImageUrl();
					getImageFromUrl(imgUrl);
				} else {
					String netNotOpenContent = context.getString(R.string.alarm_net_notopen);
					Toast.makeText(context, netNotOpenContent,Toast.LENGTH_SHORT).show();
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
					String netNotOpenContent = context.getString(R.string.alarm_net_notopen);
					Toast.makeText(context, netNotOpenContent,Toast.LENGTH_SHORT).show();
				}
			}
		});

		contentBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(context, AlarmContentActivity.class);
				AlarmDevice device = alarmInfoList.get(pos).getAlarm();
				intent.putExtra("alarmDevice", device);
				context.startActivity(intent);
			}
		});
		return convertView;
	}

	/*** 获得一张图片,从是从文件中获取，如果没有,则从网络获取 ***/
	protected void getImageFromUrl(String imgUrl) {
		if (imgUrl == null || imgUrl.trim().length() == 0) {
			getAlarmActivity().dismissDialog(IMAGE_LOAD_DIALOG);
			String noturl = context.getString(R.string.alarm_img_load_noturl);
			Toast.makeText(context, noturl, Toast.LENGTH_SHORT).show();
			return;
		}

		boolean isExist = AlarmImageFileCache.isExistImageFile(imgUrl);
		if (!isExist) {
			imgLoadTask = new AlarmImageDownLoadTask(imgUrl, context, mHandler);
			imgLoadTask.setTitle(deviceName);
			imgLoadTask.start();
		} else {
			getAlarmActivity().dismissDialog(IMAGE_LOAD_DIALOG);
			Intent intent = new Intent();
			intent.putExtra("isExist", true);
			intent.putExtra("imageUrl", imgUrl);
			intent.putExtra("title", deviceName);
			intent.setClass(context, AlarmImageActivity.class);
			((AlarmActivity) context).startActivityForResult(intent,
					REQUESTCODE);
		}
	}

	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition) {
		return true;
	}

	private AlarmActivity getAlarmActivity() {
		return (AlarmActivity) context;
	}

	public void cancel(boolean isCanceld) {
		if (imgLoadTask != null) {
			imgLoadTask.setCancel(isCanceld);
		}
	}
	
	public void setExpandIndex(List<Integer> indexes){
		this.indexes = indexes;
	}
	
	public List<Integer> getExpandedIndexes() {
		return indexes;
	}
}