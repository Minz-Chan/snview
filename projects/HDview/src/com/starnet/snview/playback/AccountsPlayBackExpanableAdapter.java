package com.starnet.snview.playback;

import java.util.List;

import com.starnet.snview.R;
import com.starnet.snview.channelmanager.Channel;
import com.starnet.snview.channelmanager.xml.ConnectionIdentifyTask;
import com.starnet.snview.devicemanager.DeviceItem;
import com.starnet.snview.global.GlobalApplication;
import com.starnet.snview.syssetting.CloudAccount;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

public class AccountsPlayBackExpanableAdapter extends BaseExpandableListAdapter {
	
	private final String TAG = "AccountsPlayBackExpanableAdapter";

	private Context ctx;
	private List<CloudAccount> users;// 星云账户
	private final int REQ = 0x0005;

	private Handler mHandler;
	private CloudAccount clickUser;
	private DeviceItem clickDItem;

	public void setHandler(Handler handler) {
		this.mHandler = handler;
	}

	public void setDeviceItem(DeviceItem item) {
		this.clickDItem = item;
	}

	public AccountsPlayBackExpanableAdapter(Handler handler, Context ctx,
			List<CloudAccount> users) {
		this.ctx = ctx;
		this.users = users;
		this.mHandler = handler;
	}

	@Override
	public int getGroupCount() {
		int size = 0;
		if (users != null) {
			size = users.size();
		}
		return size;
	}

	@Override
	public int getChildrenCount(int groupPosition) {
		int size = 0;
		List<DeviceItem> items = users.get(groupPosition).getDeviceList();
		if (items != null) {
			size = items.size();
		}
		return size;
	}

	@Override
	public Object getGroup(int groupPosition) {
		return users.get(groupPosition);
	}

	@Override
	public Object getChild(int groupPosition, int childPosition) {
		if (users == null) {
			return null;
		}
		CloudAccount ca = users.get(groupPosition);
		if (ca == null) {
			return null;
		}
		List<DeviceItem> dList = ca.getDeviceList();
		if (dList == null) {
			return null;
		}
		return dList.get(childPosition);
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
	public View getGroupView(int groupPosition, boolean isExpanded,View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = LayoutInflater.from(ctx).inflate(R.layout.playback_cloudaccount_preview_item, null);
		}
		ProgressBar prg = (ProgressBar) convertView.findViewById(R.id.prgBar);
		if (users.get(groupPosition).isRotate()) {
			prg.setVisibility(View.GONE);
		}
		TextView txt = (TextView) convertView.findViewById(R.id.account_name);
		txt.setText(users.get(groupPosition).getUsername());
		ImageView arrow = (ImageView) convertView.findViewById(R.id.arrow);
		if (users.get(groupPosition).isExpanded()) {
			arrow.setBackgroundResource(R.drawable.channel_listview_down_arrow_sel);
		} else {
			arrow.setBackgroundResource(R.drawable.channel_listview_right_arrow_sel);
		}
		return convertView;
	}

	@Override
	public View getChildView(int groupPosition, int childPosition,boolean isLastChild, View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = LayoutInflater.from(ctx).inflate(R.layout.playback_deviceitems_act, null);
		}
		LinearLayout tv_container = (LinearLayout) convertView.findViewById(R.id.tv_container);
		List<DeviceItem> list = users.get(groupPosition).getDeviceList();
		TextView txt = (TextView) convertView.findViewById(R.id.channel_name);
		// Button txt = (Button) convertView.findViewById(R.id.channel_name);
		txt.setText(list.get(childPosition).getDeviceName());
		final Button stateBtn = (Button) convertView.findViewById(R.id.stateBtn);

		/*
		 * DeviceItem item =
		 * users.get(groupPosition).getDeviceList().get(childPosition); boolean
		 * isSelected = judgeChannelIsSelected(item); if (isSelected) {
		 * stateBtn.setBackgroundResource(R.drawable.channellist_select_alled);
		 * } else {
		 * stateBtn.setBackgroundResource(R.drawable.channellist_select_empty);
		 * }
		 */
		if ((groupPosition == clickGroup) && (childPosition == clickChild)) {
			SharedPreferences sp = ctx.getSharedPreferences("step_over_xml", Context.MODE_PRIVATE);
			boolean isStep_over = sp.getBoolean("step_over", false);
			if (isStep_over) {
				stateBtn.setBackgroundResource(R.drawable.channellist_select_alled);
				Log.i(TAG, "++++isStep_over:="+isStep_over);
			}
//			if (GlobalApplication.getInstance().isStepOver()) {
//				stateBtn.setBackgroundResource(R.drawable.channellist_select_alled);
//			}
		} else {
			stateBtn.setBackgroundResource(R.drawable.channellist_select_empty);
		}

		final int group = groupPosition;
		final int child = childPosition;

		tv_container.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				selectDeviceForPlayBack(group, child);
			}

			private void selectDeviceForPlayBack(final int group,final int child) {
				clickChild = child;
				clickGroup = group;
				Intent intent = new Intent();
				clickUser = users.get(clickGroup);
				List<DeviceItem> dList = clickUser.getDeviceList();
				clickDItem = dList.get(clickChild);
				intent.putExtra("group", clickGroup);
				intent.putExtra("child", clickChild);
				intent.putExtra("device", clickDItem);
				if (clickGroup == 0) {
					if (clickDItem.isConnPass()) {
						intent.setClass(ctx,PlayBackChannelListViewActivity.class);
						((TimeSettingActivity) ctx).startActivityForResult(
								intent, REQ);
					} else {// 进行联网验证
						((TimeSettingActivity) ctx)
								.showDialog(TimeSettingActivity.CONNECTIDENTIFY_PROGRESSBAR);
						task = new ConnectionIdentifyTask(mHandler, clickUser,
								clickDItem, clickGroup, clickChild, false);
						task.setContext(ctx);
						task.setCancel(false);
						task.start();
					}
				} else {
					intent.setClass(ctx, PlayBackChannelListViewActivity.class);
					((TimeSettingActivity) ctx).startActivityForResult(intent,
							REQ);
				}
			}
		});

		stateBtn.setOnClickListener(new OnClickListener() {// 考虑收藏设备的联网验证情形
			@Override
			public void onClick(View v) {
				selectDeviceForPlayBack(group, child);
			}
			private void selectDeviceForPlayBack(final int group,final int child) {
					clickChild = child;
					clickGroup = group;
					Intent intent = new Intent();
					clickUser = users.get(clickGroup);
					List<DeviceItem> dList = clickUser.getDeviceList();
					clickDItem = dList.get(clickChild);
					intent.putExtra("group", clickGroup);
					intent.putExtra("child", clickChild);
					intent.putExtra("device", clickDItem);
					if (clickGroup == 0) {
						if (clickDItem.isConnPass()) {
							intent.setClass(ctx,PlayBackChannelListViewActivity.class);
							((TimeSettingActivity) ctx).startActivityForResult(intent, REQ);
						} else {// 进行联网验证
							((TimeSettingActivity) ctx).showDialog(TimeSettingActivity.CONNECTIDENTIFY_PROGRESSBAR);
							task = new ConnectionIdentifyTask(mHandler, clickUser, clickDItem, clickGroup, clickChild, false);
							task.setContext(ctx);
							task.setCancel(false);
							task.start();
						}
					} else {
						intent.setClass(ctx, PlayBackChannelListViewActivity.class);
						((TimeSettingActivity) ctx).startActivityForResult(intent,REQ);
					}
			}
		});

		txt.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				selectDeviceForPlayBack(group, child);
			}

			private void selectDeviceForPlayBack(final int group,final int child) {
				clickChild = child;
				clickGroup = group;
				Intent intent = new Intent();
				clickUser = users.get(clickGroup);
				List<DeviceItem> dList = clickUser.getDeviceList();
				clickDItem = dList.get(clickChild);
				intent.putExtra("group", clickGroup);
				intent.putExtra("child", clickChild);
				intent.putExtra("device", clickDItem);
				if (clickGroup == 0) {
					if (clickDItem.isConnPass()) {
						intent.setClass(ctx,PlayBackChannelListViewActivity.class);
						((TimeSettingActivity) ctx).startActivityForResult(intent, REQ);
					} else {// 进行联网验证
						((TimeSettingActivity) ctx).showDialog(TimeSettingActivity.CONNECTIDENTIFY_PROGRESSBAR);
						task = new ConnectionIdentifyTask(mHandler, clickUser,
								clickDItem, clickGroup, clickChild, false);
						task.setContext(ctx);
						task.setCancel(false);
						task.start();
					}
				} else {
					intent.setClass(ctx, PlayBackChannelListViewActivity.class);
					((TimeSettingActivity) ctx).startActivityForResult(intent,REQ);
				}
			}
		});

		return convertView;
	}

	private boolean judgeChannelIsSelected(DeviceItem item) {
		boolean isSelected = false;
		if (item != null) {
			List<Channel> chList = item.getChannelList();
			if (chList != null && chList.size() > 0) {
				for (Channel channel : chList) {
					if (channel.isSelected()) {
						isSelected = true;
						break;
					}
				}
			}
		}
		return isSelected;
	}

	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition) {
		return false;
	}

	private ConnectionIdentifyTask task;

	public void setCancel(boolean isCancel) {
		task.setCanceled(isCancel);
	}

	private static int clickGroup;
	private static int clickChild;
	protected boolean okFlag;

	public void setOkFlag(boolean okFlag) {
		this.okFlag = okFlag;
	}

	public void setGroup(int group) {
		clickGroup = group;
	}

	public void setChild(int child) {
		clickChild = child;
	}
}
