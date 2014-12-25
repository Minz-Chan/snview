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

	private Context ctx;
	private String dName;
	private List<Integer> indexes;
	private List<AlarmShowItem> almList;
	private final int REQUESTCODE = 0x0023;
	private AlarmImageDownLoadTask imgLoadTask;
	private final int IMAGE_LOAD_DIALOG = 0x0013;
	private final int ALARM_CONTEN_DIALOG = 0x0003;

	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			showToast(ctx.getString(R.string.alarm_img_load_timeout));
		}
	};

	public AlarmDeviceAdapter(List<AlarmShowItem> alarmInfoList,
			Context context, Handler handler) {
		this.ctx = context;
		this.almList = alarmInfoList;
	}

	public AlarmDeviceAdapter(List<AlarmShowItem> alarmInfoList, Context context) {
		this.almList = alarmInfoList;
		this.ctx = context;
	}

	private void showToast(String content) {
		Toast.makeText(ctx, content, Toast.LENGTH_LONG).show();
	}

	@Override
	public int getGroupCount() {
		int size = 0;
		if (almList != null) {
			size = almList.size();
		}
		return size;
	}

	@Override
	public int getChildrenCount(int groupPosition) {
		return 0;
	}

	@Override
	public Object getGroup(int groupPosition) {
		return almList.get(groupPosition);
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
			convertView = LayoutInflater.from(ctx).inflate(
					R.layout.alarm_listview_item_layout, null);
		}
		String time = almList.get(groupPosition).getAlarm().getAlarmTime();
		TextView dTxt = (TextView) convertView.findViewById(R.id.devicename);
		dTxt.setText(almList.get(groupPosition).getAlarm().getDeviceName());

		TextView almTime = (TextView) convertView.findViewById(R.id.alarm_time);
		TextView almType = (TextView) convertView.findViewById(R.id.alarm_type);
		almType.setText(almList.get(groupPosition).getAlarm().getAlarmType());
		time = time.replaceAll(",", " ");
		almTime.setText(time);

		ImageView aImg = (ImageView) convertView.findViewById(R.id.arrowimg);
		aImg.setBackgroundResource(R.drawable.channel_listview_right_arrow_sel);
//		if (isExpanded) {
//			aImg.setBackgroundResource(R.drawable.channel_listview_down_arrow_sel);
//		} else {
//			aImg.setBackgroundResource(R.drawable.channel_listview_right_arrow_sel);
//		}
//		if (groupPosition == almList.size() - 1) {
//			// View view = convertView.findViewById(R.id.view);
//			int resId = getColor(R.color.gray_transplate);
//			convertView.setBackgroundColor(resId);
//		}
		convertView.setTag(R.id.arrowimg, groupPosition);// 为父元素设置标签
		return convertView;
	}

	private static int groupPos = -1;

	@Override
	public View getChildView(int groupPosition, int childPosition,
			boolean isLastChild, View convertView, ViewGroup parent) {
		if (convertView == null) {
			LayoutInflater flt = LayoutInflater.from(ctx);
			convertView = flt.inflate(R.layout.alarm_listview_subitem_layout,
					null);
		}
		Button imgBtn = (Button) convertView.findViewById(R.id.imgbtn);
		Button vdoBtn = (Button) convertView.findViewById(R.id.vdobtn);
		Button cntBtn = (Button) convertView.findViewById(R.id.contentbtn);

		if (almList.get(groupPosition).getAlarm().getImageUrl().trim().length() == 0) {
			imgBtn.setVisibility(View.GONE);
		} else {
			imgBtn.setVisibility(View.VISIBLE);
		}

		final int pos = groupPosition;
		imgBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				AlarmImageFileCache.context = ctx;
				String imgUrl = almList.get(pos).getAlarm().getImageUrl();
				boolean isExist = AlarmImageFileCache.isExistImageFile(imgUrl);
				if (isExist) {
					getAlarmActivity().showDialog(IMAGE_LOAD_DIALOG);
					getImageFromUrl(imgUrl);
				} else {
					if (NetWorkUtils.checkNetConnection(ctx)) {
						getAlarmActivity().showDialog(IMAGE_LOAD_DIALOG);
						dName = almList.get(pos).getAlarm().getDeviceName();
						getImageFromUrl(imgUrl);
					} else {
						showToast(ctx.getString(R.string.alarm_net_notopen));
					}
				}
			}
		});

		AlarmDevice device = almList.get(pos).getAlarm();
		boolean isNull = isExistNull(device);
		if (isNull) {
			vdoBtn.setVisibility(View.GONE);
		} else {
			vdoBtn.setVisibility(View.VISIBLE);
		}

		vdoBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (NetWorkUtils.checkNetConnection(ctx)) {
					AlarmDevice device = almList.get(pos).getAlarm();
					Intent intent = new Intent(ctx, RealplayActivity.class);
					intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					intent.putExtra(AlarmActivity.ALARM_DEVICE_DETAIL, device);
					ctx.startActivity(intent);
					((AlarmActivity) ctx).finish();
				} else {
					showToast(ctx.getString(R.string.alarm_net_notopen));
				}
			}
		});

		cntBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(ctx, AlarmContentActivity.class);
				AlarmDevice device = almList.get(pos).getAlarm();
				intent.putExtra("alarmDevice", device);
				intent.putExtra("position", pos);
				((AlarmActivity) ctx).startActivityForResult(intent,
						ALARM_CONTEN_DIALOG);
			}
		});
		return convertView;
	}

	private boolean isExistNull(AlarmDevice device) {
		boolean isExist = false;
		if (device.getIp() == null || device.getIp().trim().length() == 0) {
			return true;
		}
		if (device.getUserName() == null
				|| device.getUserName().trim().length() == 0) {
			return true;
		}
		if (device.getPort() == 0) {
			return true;
		}
		return isExist;
	}

	/*** 获得一张图片,从是从文件中获取，如果没有,则从网络获取 ***/
	protected void getImageFromUrl(String imgUrl) {
		if (imgUrl == null || imgUrl.trim().length() == 0) {
			getAlarmActivity().dismissDialog(IMAGE_LOAD_DIALOG);
			showToast(ctx.getString(R.string.alarm_img_load_noturl));
			return;
		}

		boolean isExist = AlarmImageFileCache.isExistImageFile(imgUrl);
		if (!isExist) {
			imgLoadTask = new AlarmImageDownLoadTask(imgUrl, ctx, mHandler);
			imgLoadTask.setTitle(dName);
			imgLoadTask.start();
		} else {
			getAlarmActivity().dismissDialog(IMAGE_LOAD_DIALOG);
			Intent intent = new Intent();
			intent.putExtra("isExist", true);
			intent.putExtra("imageUrl", imgUrl);
			intent.putExtra("title", dName);
			intent.setClass(ctx, AlarmImageActivity.class);
			((AlarmActivity) ctx).startActivityForResult(intent, REQUESTCODE);
		}
	}

	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition) {
		return true;
	}

	private AlarmActivity getAlarmActivity() {
		return (AlarmActivity) ctx;
	}

	public void cancel(boolean isCanceld) {
		if (imgLoadTask != null) {
			imgLoadTask.setCancel(isCanceld);
		}
	}

	public void setExpandIndex(List<Integer> indexes) {
		this.indexes = indexes;
	}

	public List<Integer> getExpandedIndexes() {
		return indexes;
	}

	public static int getGroupPos() {
		return groupPos;
	}

	public static void setGroupPos(int groupPos) {
		AlarmDeviceAdapter.groupPos = groupPos;
	}
}