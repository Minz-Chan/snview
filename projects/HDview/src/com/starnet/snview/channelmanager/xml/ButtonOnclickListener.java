package com.starnet.snview.channelmanager.xml;

import java.util.List;

import com.starnet.snview.R;
import com.starnet.snview.channelmanager.ChannelExpandableListviewAdapter;
import com.starnet.snview.channelmanager.ChannelListActivity;
import com.starnet.snview.channelmanager.ChannelListViewActivity;
import com.starnet.snview.devicemanager.DeviceItem;
import com.starnet.snview.realplay.PreviewDeviceItem;
import com.starnet.snview.syssetting.CloudAccount;
import com.starnet.snview.util.NetWorkUtils;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class ButtonOnclickListener implements OnClickListener {

//	private final String filePath = "/data/data/com.starnet.snview/deviceItem_list.xml";

	ButtonState bs;
	TextView titleView;
	Button state_button;
	private int childPos;
	private int parentPos;
	private Context context;
	private Handler handler;
	CloudAccount selectCloudAccount;
	List<CloudAccount> groupAccountList;
	List<CloudAccount> cloudAccountList;// 星云账号信息
	ChannelExpandableListviewAdapter cela;
	private CloudAccount clickCloudAccount;// 星云账号信息
	private final int CONNIDENTIFYDIALOG = 5;
	private ConnectionIdentifyTask connTask;// 连接验证线程
	List<PreviewDeviceItem> previewChannelList;

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.button_channel_list:
			DeviceItem dItem = clickCloudAccount.getDeviceList().get(childPos);
			if (!dItem.isIdentify()) {// 如果用户没有经过验证，则进行验证
				if (!NetWorkUtils.checkNetConnection(context)) {
					gotoChanelListViewActivity(dItem);
				} else {
					isClick = true;
					((ChannelListActivity) context).showDialog(CONNIDENTIFYDIALOG);
					connTask = new ConnectionIdentifyTask(handler, clickCloudAccount,dItem, parentPos, childPos);
					connTask.setContext(context);
					connTask.start();
				}
			} else {// 验证过后的直接弹出对话框
				gotoChanelListViewActivity(dItem);
			}
			break;
		default:
			break;
		}
	}

	private void gotoChanelListViewActivity(DeviceItem dItem) {
		Intent data = new Intent(context, ChannelListViewActivity.class);
		Bundle bundle = new Bundle();
		bundle.putString("groupPosition", String.valueOf(parentPos));
		bundle.putString("childPosition", String.valueOf(childPos));
		String deviceName = dItem.getDeviceName();
		bundle.putString("deviceName", deviceName);
		data.putExtra("clickCloudA", clickCloudAccount);
		bundle.putSerializable("clickCloudAccount", clickCloudAccount);
		data.putExtras(bundle);
		((ChannelListActivity) context).startActivityForResult(data, 31);
	}

	private void showToast(String content) {
		Toast.makeText(context, content, Toast.LENGTH_SHORT).show();
	}

	public ButtonOnclickListener(Context context2, Handler handler,
			ChannelExpandableListviewAdapter cela,
			CloudAccount clickCloudAccount,
			List<CloudAccount> groupAccountList, int groupPosition,
			int childPosition, Button staButton, TextView titleView,ConnectionIdentifyTask connTask) {
		this.context = context2;
		this.clickCloudAccount = clickCloudAccount;
		this.parentPos = groupPosition;
		this.childPos = childPosition;
		this.state_button = staButton;
		this.titleView = titleView;
		this.cela = cela;
		this.groupAccountList = groupAccountList;
		this.handler = handler;
		this.connTask = connTask;
	}
	
	public ButtonOnclickListener(Context context2, Handler handler,
			ChannelExpandableListviewAdapter cela,
			CloudAccount clickCloudAccount,
			List<CloudAccount> groupAccountList, int groupPosition,
			int childPosition, Button staButton, TextView titleView) {
		this.context = context2;
		this.clickCloudAccount = clickCloudAccount;
		this.parentPos = groupPosition;
		this.childPos = childPosition;
		this.state_button = staButton;
		this.titleView = titleView;
		this.cela = cela;
		this.groupAccountList = groupAccountList;
		this.handler = handler;
	}

	public void setCancel(boolean isCanceled) {
		connTask.setCanceled(isCanceled);
	}
	
	private boolean isClick;
	public boolean isClick(){
		return isClick;
	}
	
	public void setClick(boolean isClick){
		this.isClick = isClick;
	}
}