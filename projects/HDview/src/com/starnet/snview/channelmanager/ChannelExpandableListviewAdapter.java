package com.starnet.snview.channelmanager;

import java.util.ArrayList;
import java.util.List;

import com.starnet.snview.R;
import com.starnet.snview.channelmanager.xml.ButtonOnTouchListener;
import com.starnet.snview.channelmanager.xml.ButtonOnclickListener;
import com.starnet.snview.channelmanager.xml.ButtonState;
import com.starnet.snview.channelmanager.xml.ExpandableListViewUtils;
import com.starnet.snview.devicemanager.DeviceItem;
import com.starnet.snview.global.GlobalApplication;
import com.starnet.snview.realplay.PreviewDeviceItem;
import com.starnet.snview.syssetting.CloudAccount;
import com.starnet.snview.util.NetWorkUtils;
import com.starnet.snview.util.ReadWriteXmlUtils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

/**
 * 
 * @author zhaohongxu
 * @Date Jul 11, 2014
 * @ClassName ChannelExpandableListviewAdapter.java
 * @Description 显示扩展列表下的内容，组元素，子元素等；在每次进行界面的动态加载时，都是从文档中进行信息读取；
 */
@SuppressLint("SdCardPath")
public class ChannelExpandableListviewAdapter extends BaseExpandableListAdapter {

	private final String TAG = "ChannelExpandableListviewAdapter";
	private ButtonState bs;
	private boolean isOpen;
	private Context context;
	private TextView titleView;
	private Button state_button;
	public int notify_number = 1;
	private Button button_channel_list;
	private List<DeviceItem> deviceList;
	private LayoutInflater layoutInflater;
	private CloudAccount clickCloudAccount;
	private ImageView channelStateFrame;
	private List<CloudAccount> groupAccountList;// 用于显示星云账号
	private List<PreviewDeviceItem> mPreviewDeviceItems;// 从RealplayActivity中获取预览通道
	private List<Integer> colorPosList = new ArrayList<Integer>();// 用于记录需要显示不同颜色的位置

	private Handler handler;

	public ChannelExpandableListviewAdapter(Context curContext,
			List<CloudAccount> groupAccountList, TextView titleView) {
		super();
		this.titleView = titleView;
		this.groupAccountList = groupAccountList;
		this.context = curContext;
		this.layoutInflater = ((LayoutInflater) curContext
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE));
		int size = groupAccountList.size();
		for (int i = 0; i < size; i++) {
			CloudAccount cloudAccount = groupAccountList.get(i);
			if (!cloudAccount.isEnabled()) {// 不需要网络加载的用户，记录其位置...
				colorPosList.add(i);
			}
		}
		isOpen = NetWorkUtils.checkNetConnection(context);
		ExpandableListViewUtils.context = context;
		mPreviewDeviceItems = GlobalApplication.getInstance()
				.getRealplayActivity().getPreviewDevices();
		notify_number = 3;

	}

	public ChannelExpandableListviewAdapter(Context curContext,
			List<CloudAccount> cloudAccounts) {
		super();
		this.groupAccountList = cloudAccounts;
		this.context = curContext;
		this.layoutInflater = ((LayoutInflater) curContext
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE));
		int size = groupAccountList.size();
		for (int i = 0; i < size; i++) {
			CloudAccount cloudAccount = groupAccountList.get(i);
			if (!cloudAccount.isEnabled()) {// 不需要网络加载的用户，记录其位置...
				colorPosList.add(i);
			}
		}
		isOpen = NetWorkUtils.checkNetConnection(context);
		ExpandableListViewUtils.context = context;
		mPreviewDeviceItems = GlobalApplication.getInstance()
				.getRealplayActivity().getPreviewDevices();
		notify_number = 3;
	}

	@Override
	public int getGroupCount() {// 获取组的个数
		int size;
		if (groupAccountList != null) {
			size = groupAccountList.size();
		} else {
			size = 0;
		}
		return size;
	}

	@Override
	public int getChildrenCount(int groupPosition) {// 获取每个组对应的孩子的个数
		int size;
		CloudAccount cloudAccount = groupAccountList.get(groupPosition);
		deviceList = cloudAccount.getDeviceList();
		if (deviceList != null) {
			size = deviceList.size();
		} else {
			size = 0;
		}
		return size;
	}

	@Override
	public Object getGroup(int groupPosition) {
		CloudAccount cloudAccount = groupAccountList.get(groupPosition);
		return cloudAccount;
	}

	@Override
	public Object getChild(int groupPosition, int childPosition) {
		CloudAccount cloudAccount = groupAccountList.get(groupPosition);
		deviceList = cloudAccount.getDeviceList();
		DeviceItem deviceItem = deviceList.get(childPosition);
		return deviceItem;
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
		return true;
	}

	@Override
	public View getGroupView(int groupPosition, boolean isExpanded,
			View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = layoutInflater.inflate(
					R.layout.channel_listview_account_item_layout_copy, null);
		}
		// 为组元素设置背景颜色...
		ProgressBar prgBar = (ProgressBar) convertView
				.findViewById(R.id.progressBar_net_load);
		if (groupAccountList.get(groupPosition).isRotate() || (!isOpen)) {// 判断加载框设置是否为“FALSE”，若是，则显示加载框；否则，不显示；
			prgBar.setVisibility(View.GONE);
		}

		TextView title = (TextView) convertView
				.findViewById(R.id.channel_listview_account_item_name);

		CloudAccount cloudAccount = groupAccountList.get(groupPosition);

		if (notify_number == 3) {
			if (mPreviewDeviceItems != null) {
				int size = mPreviewDeviceItems.size();
				for (int i = 0; i < size; i++) {
					PreviewDeviceItem previewDeviceItem = mPreviewDeviceItems
							.get(i);
					boolean isContained = checkPreviewDeviceItemFromCA(
							previewDeviceItem, cloudAccount);
					if (isContained) {
						setCloudAccountChannelChoose(previewDeviceItem,
								cloudAccount);// 根据PreviewDeviceItem设置用户的通道选择情况
					}
				}
			}
		}
		String tileName = cloudAccount.getUsername();
		title.setText(tileName);
		ImageView itemIcon = (ImageView) convertView
				.findViewById(R.id.channel_listview_account_item_icon);
		ImageView arrow = (ImageView) convertView
				.findViewById(R.id.channel_listview_arrow);
		if (groupAccountList.get(groupPosition).isExpanded()) {// 判断组列表是否展开
			convertView
					.setBackgroundColor(getColor(R.color.channel_listview_account_item_bg_expanded));
			itemIcon.setBackgroundResource(R.drawable.channel_listview_account_sel);
			arrow.setBackgroundResource(R.drawable.channel_listview_down_arrow_sel);
			((ExpandableListView) parent).expandGroup(groupPosition);
		} else {
			convertView
					.setBackgroundColor(getColor(R.color.channel_listview_account_item_bg_collapsed));
			itemIcon.setBackgroundResource(R.drawable.channel_listview_account);
			arrow.setBackgroundResource(R.drawable.channel_listview_right_arrow);
			((ExpandableListView) parent).collapseGroup(groupPosition);
		}

		boolean isContain = containPositon(groupPosition, colorPosList);
		if (isContain) {
			convertView
					.setBackgroundColor(getColor(R.color.listview_bg_noisenable));
		}

		final int pos = groupPosition;
		channelStateFrame = (ImageView) convertView
				.findViewById(R.id.channel_listview_select);
		String state = ExpandableListViewUtils
				.getStateForCloudAccount(groupAccountList.get(pos));
		if (state.equals("all")) {
			channelStateFrame
					.setBackgroundResource(R.drawable.channellist_select_alled);
		} else if (state.equals("half")) {
			channelStateFrame
					.setBackgroundResource(R.drawable.channel_selected_half);
		} else {
			channelStateFrame
					.setBackgroundResource(R.drawable.channellist_select_empty);
		}
		channelStateFrame.setOnClickListener(new OnClickListener() {// 考虑点击全选状态按钮时，考虑为空的情况
					@Override
					public void onClick(View v) {

						String state = ExpandableListViewUtils
								.getStateForCloudAccount(groupAccountList
										.get(pos));// 判断当前的选择状态(全选、半选和未选)
						if (state.equals("all")) {
							channelStateFrame
									.setBackgroundResource(R.drawable.channellist_select_empty);

							ExpandableListViewUtils.setStateForCloudAccount(
									"empty", groupAccountList.get(pos));// 改变通道列表的选择状态
						} else if (state.equals("half")) {
							channelStateFrame
									.setBackgroundResource(R.drawable.channellist_select_alled);
							ExpandableListViewUtils.setStateForCloudAccount(
									"all", groupAccountList.get(pos));
						} else {
							channelStateFrame
									.setBackgroundResource(R.drawable.channellist_select_alled);
							ExpandableListViewUtils.setStateForCloudAccount(
									"all", groupAccountList.get(pos));
						}
						int number = ExpandableListViewUtils
								.getPreviewListFromCloudAccounts(groupAccountList);// 显示数据选择情形
						if (number == 0) {
							titleView.setText(context
									.getString(R.string.navigation_title_channel_list));// 设置列表标题名
						} else {
							titleView.setText(context
									.getString(R.string.navigation_title_channel_list)
									+ "(" + number + ")");// 设置列表标题名
						}
						groupAccountList.set(pos, groupAccountList.get(pos));// 重置星云平台用户信息
						notifyDataSetChanged();
						notify_number = 30;

						List<PreviewDeviceItem> devices = ExpandableListViewUtils
								.getPreviewChannelList(groupAccountList);
						GlobalApplication.getInstance().getRealplayActivity()
								.setPreviewDevices_copy(devices);
						if (groupAccountList
								.get(pos)
								.getUsername()
								.equals(context
										.getString(R.string.device_manager_collect_device))) {
							final String filePath = "/data/data/com.starnet.snview/deviceItem_list.xml";
							final List<DeviceItem> deviceList = groupAccountList
									.get(pos).getDeviceList();
							final int size = deviceList.size();
							Thread thread = new Thread() {
								@Override
								public void run() {
									super.run();
									for (int i = 0; i < size; i++) {
										try {
											ReadWriteXmlUtils
													.addNewDeviceItemToCollectEquipmentXML(
															deviceList.get(i),
															filePath);
										} catch (Exception e) {
											e.printStackTrace();
										}
									}
								}
							};
							thread.start();
						}
					}
				});

		int number = ExpandableListViewUtils
				.getPreviewListFromCloudAccounts(groupAccountList);// 显示数据选择情形
		if (number == 0) {
			titleView.setText(context
					.getString(R.string.navigation_title_channel_list));// 设置列表标题名
		} else {
			titleView.setText(context
					.getString(R.string.navigation_title_channel_list)
					+ "("
					+ number + ")");// 设置列表标题名
		}
		return convertView;
	}

	private void setCloudAccountChannelChoose(
			PreviewDeviceItem previewDeviceItem, CloudAccount cloudAccount) {

		String logUser = previewDeviceItem.getDeviceRecordName();
		String on = context.getString(R.string.device_manager_online_en);
		String off = context.getString(R.string.device_manager_offline_en);
		// 定位到cloudAccount的设备
		List<DeviceItem> deviceItems = cloudAccount.getDeviceList();
		if (deviceItems != null) {
			int size = deviceItems.size();
			for (int i = 0; i < size; i++) {
				DeviceItem deviceItem = deviceItems.get(i);
				String dUser = deviceItem.getDeviceName();
				if (dUser != null) {
					if (dUser.contains(on) || dUser.contains(off)) {
						dUser = dUser.substring(4);
					}
				}

				if (logUser.equals(dUser)) {// dUser.contains(logUser)
											// logUser.equals(dUser)
											// logPass.equals(dPass)&&logUser.equals(dUser)&&logSvrIp.equals(dSvrIp)&&logSvrPot.equals(dSvrPort)
					int cur_channel = previewDeviceItem.getChannel();
					Log.v(TAG, "cur_channel:" + cur_channel);

					List<Channel> channelList = deviceItem.getChannelList();
					if (channelList != null) {
						int channelSize = channelList.size();
						for (int j = 0; j < channelSize; j++) {
							if (cur_channel == j + 1) {
								channelList.get(j).setSelected(true);
							}
						}
					}
				}
			}
		}
	}

	private boolean containPositon(int groupPosition, List<Integer> pList) {
		boolean result = false;
		int size = pList.size();
		for (int i = 0; i < size; i++) {
			if (pList.get(i) == groupPosition) {
				result = true;
				break;
			}
		}
		return result;
	}

	@Override
	public View getChildView(int groupPosition, int childPosition,
			boolean isLastChild, View convertView, ViewGroup parent) {// 加载子元素
		if (convertView == null) {
			convertView = layoutInflater
					.inflate(
							R.layout.channel_listview_channel_item_layout_wadgets,
							null);
		}
		TextView title = (TextView) convertView
				.findViewById(R.id.channel_listview_device_item_name);
		CloudAccount cloudAccount = groupAccountList.get(groupPosition);
		deviceList = cloudAccount.getDeviceList();

		DeviceItem deviceItem = deviceList.get(childPosition);
		String deviceName = deviceItem.getDeviceName();
		title.setText(deviceName);

		state_button = (Button) convertView.findViewById(R.id.button_state);// 发现“状态显示按钮”并为之添加单击事件,若选择了该按钮为全满时，需要将该行的"通道列表"置为全选；
		String state = getChannelSelectNum(groupPosition, childPosition);// 根据每一组、每一行的通道列表选择情况，来加载对应的state_button的全/半选状态
		changeStateButton(state_button, state);
		bs = new ButtonState();
		bs.setState(state);

		if (groupPosition == 0) {
			touchL = new ButtonOnTouchListener(context, handler,
					ChannelExpandableListviewAdapter.this, clickCloudAccount,
					titleView, groupPosition, childPosition, state_button,
					groupAccountList);
			state_button.setOnTouchListener(touchL);// 原来的情形
		} else {
			touchL = new ButtonOnTouchListener(context,
					ChannelExpandableListviewAdapter.this, titleView,
					groupPosition, childPosition, state_button,
					groupAccountList);
			state_button.setOnTouchListener(touchL);// 原来的情形
		}
		// 发现“通道列表按钮”并为之添加单击事件
		button_channel_list = (Button) convertView
				.findViewById(R.id.button_channel_list);
		clickCloudAccount = groupAccountList.get(groupPosition);

		if ((groupPosition == 0)
				&& clickCloudAccount.getDeviceList().get(childPosition)
						.isIdentify()
				&& !clickCloudAccount.getDeviceList().get(childPosition)
						.isConnPass()) {//
			button_channel_list.setVisibility(View.GONE);
		} else {
			clickL = new ButtonOnclickListener(context, handler,
					ChannelExpandableListviewAdapter.this, clickCloudAccount,
					groupAccountList, groupPosition, childPosition,
					state_button, titleView);
			button_channel_list.setVisibility(View.VISIBLE);
			button_channel_list.setOnClickListener(clickL);
		}
		return convertView;
	}

	private ButtonOnTouchListener touchL;
	private ButtonOnclickListener clickL;

	public void setHandler(Handler handler) {
		this.handler = handler;
	}

	public String getChannelSelectNum(DeviceItem deviceItem) {
		String state = "";
		int channelNum = 0;
		int channelSelectNum = 0;
		List<Channel> channels = deviceItem.getChannelList();
		int channelSize = channels.size();
		for (int k = 0; k < channelSize; k++) {
			channelNum++;
			if (channels.get(k).isSelected()) {
				channelSelectNum++;
			}
		}
		if (channelNum == channelSelectNum) {
			state = "all";
		} else if ((channelSelectNum > 0)) {
			state = "half";
		} else {
			state = "empty";
		}
		return state;
	}

	protected boolean checkPreviewDeviceItemFromCA(
			PreviewDeviceItem previewDeviceItem, CloudAccount cloudAccount) {

		boolean isContained = false;
		if (previewDeviceItem != null) {
			String platUsername = previewDeviceItem.getPlatformUsername();
			String username = cloudAccount.getUsername();
			if (platUsername != null) {
				if (platUsername.equals(username)) {
					isContained = true;
				}
			}
		}
		return isContained;
	}

	/**
	 * 
	 * @author zhaohongxu
	 * @Date Jul 11, 2014
	 * @Description 根据通道列表的选择的情况，加载状态显示
	 * @param state_button
	 * @param state
	 * @param groupPosition
	 * @param childPosition
	 */
	private void changeStateButton(Button state_button, String state) {
		if ((state == "all") || (state.equals("all"))) {
			state_button
					.setBackgroundResource(R.drawable.channellist_select_alled);
		} else if ((state == "half") || (state.equals("half"))) {
			state_button
					.setBackgroundResource(R.drawable.channel_selected_half);
		} else {
			state_button
					.setBackgroundResource(R.drawable.channellist_select_empty);
		}
	}

	/** 获取通道列表的选择的多少 */
	private String getChannelSelectNum(int groupPos, int childPos) {
		String state = "";
		int channelNum = 0;
		int channelSelectNum = 0;
		CloudAccount cloudAccount = groupAccountList.get(groupPos);
		List<DeviceItem> deviceList = cloudAccount.getDeviceList();
		DeviceItem deviceItem = deviceList.get(childPos);
		List<Channel> channels = deviceItem.getChannelList();// 得到一个设备对应的通道列表...
		int channelSize = channels.size();
		if (channelSize == 0) {
			state = "empty";
			return state;
		}
		for (int k = 0; k < channelSize; k++) {
			channelNum++;
			if (channels.get(k).isSelected()) {
				channelSelectNum++;
			}
		}
		if (channelNum == channelSelectNum) {
			state = "all";
		} else if ((channelSelectNum > 0)) {
			state = "half";
		} else {
			state = "empty";
		}
		return state;
	}

	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition) {
		return true;
	}

	private int getColor(int resid) {
		return context.getResources().getColor(resid);
	}

	public void setCancel(boolean isCanceled) {
		if (clickL.isClick()) {
			clickL.setCancel(isCanceled);
		} else if (touchL.isTouch()) {
			touchL.setCancel(isCanceled);
		}
	}
}