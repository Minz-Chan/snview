package com.starnet.snview.channelmanager;

import java.util.List;

import com.starnet.snview.R;
//import com.starnet.snview.channelmanager.xml.ButtonOnTouchListener;
import com.starnet.snview.channelmanager.xml.ButtonOnclickListener;
import com.starnet.snview.channelmanager.xml.ExpandableListViewUtils;
import com.starnet.snview.channelmanager.xml.StateBtnOnClickListener;
import com.starnet.snview.devicemanager.DeviceItem;
import com.starnet.snview.global.GlobalApplication;
import com.starnet.snview.realplay.PreviewDeviceItem;
import com.starnet.snview.syssetting.CloudAccount;
import com.starnet.snview.util.NetWorkUtils;
import com.starnet.snview.util.ReadWriteXmlUtils;

import android.content.Context;
import android.os.Handler;
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

public class ChannelExpandableListviewAdapter extends BaseExpandableListAdapter{
	private Context context;
	private TextView titleView;
	private Button stateBtn;
	public int notifyNum = 1;
	private Button chListBtn;
	private List<DeviceItem> deviceList;
	private ImageView channelFrame;
	private List<CloudAccount> groupAccountList;// 用于显示星云账号
	private List<PreviewDeviceItem> mPreviewDeviceItems;

	private Handler handler;
	private ButtonOnclickListener bol;
	private StateBtnOnClickListener sbocl;

	public ChannelExpandableListviewAdapter(Context curContext,List<CloudAccount> groupAccountList, TextView titleView) {
		super();
		this.titleView = titleView;
		this.groupAccountList = groupAccountList;
		this.context = curContext;
		ExpandableListViewUtils.context = context;
		mPreviewDeviceItems = GlobalApplication.getInstance().getRealplayActivity().getPreviewDevices();
//		mPreviewDeviceItems = GlobalApplication.getInstance().getLastPreviewItems();
//		mPreviewDeviceItems = ReadWriteXmlUtils.getPreviewItemListInfoFromXML(ChannelListActivity.previewFilePath);
		notifyNum = 3;
	}

	@Override
	public int getGroupCount() {// 获取组的个数
		int size = 0;
		if (groupAccountList != null) {
			size = groupAccountList.size();
		}
		return size;
	}

	@Override
	public int getChildrenCount(int groupPosition) {// 获取每个组对应的孩子的个数
		int size = 0;
		CloudAccount cloudAccount = groupAccountList.get(groupPosition);
		deviceList = cloudAccount.getDeviceList();
		if (deviceList != null) {
			size = deviceList.size();
		}
		return size;
	}

	@Override
	public Object getGroup(int groupPosition) {
		return groupAccountList.get(groupPosition);
	}

	@Override
	public Object getChild(int groupPosition, int childPosition) { 
		return groupAccountList.get(groupPosition).getDeviceList().get(childPosition);
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
	public View getGroupView(int groupPosition, boolean isExpanded,View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.channel_listview_account_item_layout_copy, null);
		}
		ProgressBar prgBar = (ProgressBar) convertView.findViewById(R.id.progressBar_net_load);
		if (groupAccountList.get(groupPosition).isRotate() || (!NetWorkUtils.checkNetConnection(context))) {// 判断加载框设置是否为“FALSE”，若是，则显示加载框；否则，不显示；
			prgBar.setVisibility(View.GONE);
		}
		TextView title = (TextView) convertView.findViewById(R.id.channel_listview_account_item_name);
		CloudAccount account = groupAccountList.get(groupPosition);
		if (notifyNum == 3) {
			if (mPreviewDeviceItems != null) {
				int size = mPreviewDeviceItems.size();
				for (int i = 0; i < size; i++) {
					PreviewDeviceItem pItem = mPreviewDeviceItems.get(i);
					boolean isContained = containPreviewItem(pItem, account);
					if (isContained) {
						setAccountChoose(pItem,account);// 根据PreviewDeviceItem设置用户的通道选择情况
					}
				}
			}
		}
		title.setText(account.getUsername());
		ImageView itemIcon = (ImageView) convertView.findViewById(R.id.channel_listview_account_item_icon);
		ImageView arrow = (ImageView) convertView.findViewById(R.id.channel_listview_arrow);
		if (groupAccountList.get(groupPosition).isExpanded()) {// 判断组列表是否展开
			convertView.setBackgroundColor(getColor(R.color.channel_listview_account_item_bg_expanded));
			itemIcon.setBackgroundResource(R.drawable.channel_listview_account_sel);
			arrow.setBackgroundResource(R.drawable.channel_listview_down_arrow_sel);
			((ExpandableListView) parent).expandGroup(groupPosition);
		} else {
			convertView.setBackgroundColor(getColor(R.color.channel_listview_account_item_bg_collapsed));
			itemIcon.setBackgroundResource(R.drawable.channel_listview_account);
			arrow.setBackgroundResource(R.drawable.channel_listview_right_arrow);
			((ExpandableListView) parent).collapseGroup(groupPosition);
		}
//		boolean isContain = containPositon(groupPosition, colorPosList);
//		if (isContain) {
//			convertView.setBackgroundColor(getColor(R.color.listview_bg_noisenable));
//		}
		final int pos = groupPosition;
		channelFrame = (ImageView) convertView.findViewById(R.id.channelFrame);
		String state = ExpandableListViewUtils.getStateForCloudAccount(groupAccountList.get(pos));
		if (state.equals("all")) {
			channelFrame.setBackgroundResource(R.drawable.channellist_select_alled);
		} else if (state.equals("half")) {
			channelFrame.setBackgroundResource(R.drawable.channel_selected_half);
		} else {
			channelFrame.setBackgroundResource(R.drawable.channellist_select_empty);
		}
		channelFrame.setOnClickListener(new OnClickListener() {// 考虑点击全选状态按钮时，考虑为空的情况
					@Override
					public void onClick(View v) {
						String state = ExpandableListViewUtils.getStateForCloudAccount(groupAccountList.get(pos));// 判断当前的选择状态(全选、半选和未选)
						if (state.equals("all")) {
							channelFrame.setBackgroundResource(R.drawable.channellist_select_empty);
							ExpandableListViewUtils.setStateForCloudAccount("empty", groupAccountList.get(pos));// 改变通道列表的选择状态
						} else if (state.equals("half")) {
							channelFrame.setBackgroundResource(R.drawable.channellist_select_alled);
							ExpandableListViewUtils.setStateForCloudAccount("all", groupAccountList.get(pos));
						} else {
							channelFrame.setBackgroundResource(R.drawable.channellist_select_alled);
							ExpandableListViewUtils.setStateForCloudAccount("all", groupAccountList.get(pos));
						}
						groupAccountList.set(pos, groupAccountList.get(pos));
						notifyDataSetChanged();
						notifyNum = 30;
						List<PreviewDeviceItem> devices = ExpandableListViewUtils.getPreviewChannelList(groupAccountList);
						GlobalApplication.getInstance().getRealplayActivity().setPreviewDevices_copy(devices);
//						if (groupAccountList.get(pos).getUsername().equals(context.getString(R.string.device_manager_collect_device))) {
//							final List<DeviceItem> deviceList = groupAccountList.get(pos).getDeviceList();
//							final int size = deviceList.size();
//							new Thread() {
//								@Override
//								public void run() {
//									super.run();
//									for (int i = 0; i < size; i++) {
//										try {
//											ReadWriteXmlUtils.addNewDeviceItemToCollectEquipmentXML(deviceList.get(i),ChannelListActivity.filePath);
//										} catch (Exception e) {
//											e.printStackTrace();
//										}
//									}
//								}
//							}.start();
//						}
					}
				});
		int number = ExpandableListViewUtils.getPreviewListFromCloudAccounts(groupAccountList);
		if (number == 0) {
			titleView.setText(context.getString(R.string.navigation_title_channel_list));
		} else {
			titleView.setText(context.getString(R.string.navigation_title_channel_list) + "(" + number + ")");
		}
		return convertView;
	}

	private void setAccountChoose(PreviewDeviceItem pItem, CloudAccount account) {
		String logUser = pItem.getDeviceRecordName();
		String on = context.getString(R.string.device_manager_online_en);
		String off = context.getString(R.string.device_manager_offline_en);
		// 定位到cloudAccount的设备
		List<DeviceItem> dItems = account.getDeviceList();
		if (dItems != null) {
			int size = dItems.size();
			for (int i = 0; i < size; i++) {
				DeviceItem deviceItem = dItems.get(i);
				String dUser = deviceItem.getDeviceName();
				if (dUser != null) {
					if (dUser.contains(on) || dUser.contains(off)) {
						dUser = dUser.substring(4);
					}
				}
				if (logUser.equals(dUser)) {
					List<Channel> channelList = deviceItem.getChannelList();
					if (channelList != null) {
						int channelSize = channelList.size();
						for (int j = 0; j < channelSize; j++) {
							if (pItem.getChannel() == (j + 1)) {
								channelList.get(j).setSelected(true);
							}
						}
					}
				}
			}
		}
	}

	@Override
	public View getChildView(int groupPosition, int childPosition,
			boolean isLastChild, View convertView, ViewGroup parent) {// 加载子元素
		if (convertView == null) {
			convertView = ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.channel_listview_channel_item_layout_wadgets,null);
		}
		TextView title = (TextView) convertView.findViewById(R.id.channelName);
		deviceList = groupAccountList.get(groupPosition).getDeviceList();
		DeviceItem deviceItem = deviceList.get(childPosition);
		String deviceName = deviceItem.getDeviceName();
		title.setText(deviceName);
		stateBtn = (Button) convertView.findViewById(R.id.button_state);
		String state = getChannelSelectNum(groupPosition, childPosition);
		changeStateButton(stateBtn, state);
		sbocl = new StateBtnOnClickListener(context,handler,ChannelExpandableListviewAdapter.this, titleView, groupPosition, childPosition, stateBtn,groupAccountList);
		stateBtn.setOnClickListener(sbocl);
		
		chListBtn = (Button) convertView.findViewById(R.id.button_channel_list);
		bol = new ButtonOnclickListener(context, handler,ChannelExpandableListviewAdapter.this, groupAccountList, groupPosition, childPosition,stateBtn, titleView);
		chListBtn.setVisibility(View.VISIBLE);
		chListBtn.setOnClickListener(bol);
		return convertView;
	}

	public void setHandler(Handler handler) {
		this.handler = handler;
	}

	protected boolean containPreviewItem(PreviewDeviceItem pItem,
			CloudAccount account) {
		boolean isContained = false;
		if (pItem != null) {
			String platUsername = pItem.getPlatformUsername();
			String username = account.getUsername();
			if (platUsername != null) {
				if (platUsername.equals(username)) {
					isContained = true;
				}
			}
		}
		return isContained;
	}

	private void changeStateButton(Button stateBtn, String state) {
		if ((state == "all") || (state.equals("all"))) {
			stateBtn.setBackgroundResource(R.drawable.channellist_select_alled);
		} else if ((state == "half") || (state.equals("half"))) {
			stateBtn.setBackgroundResource(R.drawable.channel_selected_half);
		} else {
			stateBtn.setBackgroundResource(R.drawable.channellist_select_empty);
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
		if ((bol != null) && bol.isClick()) {
			bol.setCancel(isCanceled);
		} else if ((sbocl != null) && sbocl.isTouch()) {
			sbocl.setCancel(isCanceled);
		}
	}
//	private boolean containPositon(int groupPosition, List<Integer> pList) {
//	boolean result = false;
//	int size = pList.size();
//	for (int i = 0; i < size; i++) {
//		if (pList.get(i) == groupPosition) {
//			result = true;
//			break;
//		}
//	}
//	return result;
//}
}