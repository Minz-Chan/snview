package com.starnet.snview.channelmanager.xml;

import java.util.List;

import com.starnet.snview.R;
import com.starnet.snview.channelmanager.Channel;
import com.starnet.snview.channelmanager.ChannelExpandableListviewAdapter;
import com.starnet.snview.channelmanager.ChannelListActivity;
import com.starnet.snview.devicemanager.DeviceItem;
import com.starnet.snview.global.GlobalApplication;
import com.starnet.snview.realplay.PreviewDeviceItem;
import com.starnet.snview.syssetting.CloudAccount;
import com.starnet.snview.util.CollectDeviceParams;
import com.starnet.snview.util.NetWorkUtils;
import com.starnet.snview.util.ReadWriteXmlUtils;

import android.content.Context;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class StateBtnOnClickListener implements OnClickListener {

	private int childPos;
	private int parentPos;
	private Context context;
	private TextView titleView;
	private Button stateBtn;
	private DeviceItem deviceItem;
	ChannelExpandableListviewAdapter cela;
	private CloudAccount selectCloudAccount;
	private ConnectionIdentifyTask connTask;
	private final int CONNIDENTIFYDIALOG = 5;
	private List<CloudAccount> cloudAccountList;// 星云账号信息
	private Handler handler;

	public StateBtnOnClickListener(Context context, Handler handler,
			ChannelExpandableListviewAdapter cela, TextView titleView,
			int groupPosition, int childPosition, Button state_button,
			List<CloudAccount> groupAccountList) {
		super();
		this.handler = handler;
		this.parentPos = groupPosition;
		this.childPos = childPosition;
		this.stateBtn = state_button;
		this.cloudAccountList = groupAccountList;
		this.cela = cela;
		this.titleView = titleView;
		this.context = context;
	};

	@Override
	public void onClick(View v) {
		selectCloudAccount = cloudAccountList.get(parentPos);
		deviceItem = selectCloudAccount.getDeviceList().get(childPos);
		List<Channel> channels = deviceItem.getChannelList();
		String state = getChannelSelectNum(deviceItem);
		if ((state == "half") || (state.equals("half"))) {
			stateBtn.setBackgroundResource(R.drawable.channellist_select_alled);
			// 将通道列表的状态写入到指定的XML状态文件中;1、修改某一组中某一个选项的通道列表的信息
			int channelSize = channels.size();
			for (int i = 0; i < channelSize; i++) {
				channels.get(i).setSelected(true);
			}
		} else if ((state == "all") || (state.equals("all"))) {
			stateBtn.setBackgroundResource(R.drawable.channellist_select_empty);
			// 将通道列表的状态写入到指定的XML状态文件中,1、修改某一组中某一个选项的通道列表的信息
			int channelSize = channels.size();
			for (int i = 0; i < channelSize; i++) {
				channels.get(i).setSelected(false);
			}
		} else { /* zz_empty_select */
			stateBtn.setBackgroundResource(R.drawable.channellist_select_alled);
			// 将通道列表的状态写入到指定的XML状态文件中 ;1、修改某一组中某一个选项的通道列表的信息
			int channelSize = channels.size();
			for (int i = 0; i < channelSize; i++) {
				channels.get(i).setSelected(true);
			}
			if (parentPos == 0) {
				startVisitNet();
			}
		}
		cela.notifyNum = 2;
		cela.notifyDataSetChanged();
		int number = getPreviewListFromCloudAccounts(cloudAccountList);
		if (number == 0) {
			titleView.setText(context.getString(R.string.navigation_title_channel_list));// 设置列表标题名
		} else {
			titleView.setText(context.getString(R.string.navigation_title_channel_list) + "(" + number + ")");// 设置列表标题名
		}

		if (selectCloudAccount.getUsername().equals(CollectDeviceParams.COLLECTDEVICENAME_CHANNEL_TOUCH)) {
			new Thread() {
				@Override
				public void run() {
					super.run();
					List<DeviceItem> deviceList = selectCloudAccount.getDeviceList();
					int size = deviceList.size();
					for (int i = 0; i < size; i++) {
						try {
							ReadWriteXmlUtils.addNewDeviceItemToCollectEquipmentXML(deviceList.get(i),ChannelListActivity.filePath);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
			}.start();
		}
		List<PreviewDeviceItem> touchPreviewItem = ExpandableListViewUtils.getPreviewChannelList(cloudAccountList);
		GlobalApplication.getInstance().getRealplayActivity().setPreviewDevices_copy(touchPreviewItem);
	}

	@SuppressWarnings("deprecation")
	private void startVisitNet() {
		if (!deviceItem.isConnPass() && NetWorkUtils.checkNetConnection(context)) {// 需要进行验证
			isTouch = true;
			((ChannelListActivity) context).showDialog(CONNIDENTIFYDIALOG);
			connTask = new ConnectionIdentifyTask(handler,cloudAccountList.get(parentPos), deviceItem, parentPos,childPos,true);
			connTask.setContext(context);
			connTask.start();
		} else {
			cela.notifyNum = 2;
			cela.notifyDataSetChanged();
		}
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

	private int getPreviewListFromCloudAccounts(
			List<CloudAccount> cloudAccountList2) {
		if ((cloudAccountList2 == null)
				|| (cloudAccountList2 != null && cloudAccountList2.size() == 0)) {
			return 0;
		}
		int number = 0;
		int size = cloudAccountList2.size();
		for (int i = 0; i < size; i++) {
			CloudAccount cloudAccount = cloudAccountList2.get(i);
			List<DeviceItem> deviceItemList = cloudAccount.getDeviceList();
			if (deviceItemList != null) {
				int deviceSize = deviceItemList.size();
				for (int j = 0; j < deviceSize; j++) {
					DeviceItem deviceItem = deviceItemList.get(j);
					if (deviceItem != null) {
						List<Channel> channelList = deviceItem.getChannelList();
						int channelSize = channelList.size();
						for (int k = 0; k < channelSize; k++) {
							Channel channel = channelList.get(k);
							if ((channel != null) && channel.isSelected()) {
								number++;
							}
						}
					}
				}
			}
		}
		return number;
	}

	public void setCancel(boolean isCanceled) {
		connTask.setCanceled(isCanceled);
	}

	private boolean isTouch;

	public boolean isTouch() {
		return isTouch;
	}

	public void setTouch(boolean isTouch) {
		this.isTouch = isTouch;
	}
}
