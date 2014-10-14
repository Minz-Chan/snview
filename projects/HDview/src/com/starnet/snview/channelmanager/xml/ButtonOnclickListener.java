package com.starnet.snview.channelmanager.xml;

import java.util.List;

import com.starnet.snview.R;
import com.starnet.snview.channelmanager.Channel;
import com.starnet.snview.channelmanager.ChannelExpandableListviewAdapter;
import com.starnet.snview.channelmanager.ChannelListActivity;
import com.starnet.snview.channelmanager.ChannelListViewActivity;
import com.starnet.snview.devicemanager.DeviceItem;
import com.starnet.snview.realplay.PreviewDeviceItem;
import com.starnet.snview.syssetting.CloudAccount;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

/**
 * 
 * @author zhaohongxu
 * @Date Jul 12, 2014
 * @ClassName ButtonOnclickListener.java
 * @Description 封装了按钮的单击操作
 * @Modifier zhaohongxu
 * @Modify date Jul 12, 2014
 * @Modify description TODO
 */
public class ButtonOnclickListener implements OnClickListener {
	
	@SuppressLint("SdCardPath")
	private final String filePath = "/data/data/com.starnet.snview/deviceItem_list.xml";
	
	private Context context;//上下文环境
	private int parentPos;//父元素的位置
	private int childPos;//子元素的位置
	
	private Button state_button;
	private ButtonState bs;
	
	private TextView titleView;
	
	private CloudAccountXML csxml;//
	private List<CloudAccount> cloudAccountList;//星云账号信息
	private CloudAccount clickCloudAccount;//星云账号信息
	ChannelExpandableListviewAdapter cela;
	List<CloudAccount> groupAccountList;
	
	List<PreviewDeviceItem> previewChannelList;
	CloudAccount selectCloudAccount;
	
	public ButtonOnclickListener(int parentPos, int childPos,Button state_button, ButtonState bs,List<CloudAccount> cloudAccountList,TextView titleView) {
		super();
		this.parentPos = parentPos;
		this.childPos = childPos;
		this.state_button = state_button;
		this.bs = bs;
		this.cloudAccountList = cloudAccountList;
		this.titleView = titleView;
		csxml = new CloudAccountXML();
	}

	public ButtonOnclickListener(Button state_button,ButtonState bs,int parentPos, int childPos) {
		super();
		this.parentPos = parentPos;
		this.childPos = childPos;
		this.state_button = state_button;
		this.bs = bs;
		csxml = new CloudAccountXML();
	}

	public ButtonOnclickListener(Context context,int parentPos,int childPos) {
		super();
		this.context = context;
		this.parentPos = parentPos;
		this.childPos = childPos;
	}
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.button_channel_list:
			Intent intent = new Intent(context,ChannelListViewActivity.class);	
			Bundle bundle = new Bundle();
			bundle.putString("groupPosition", String.valueOf(parentPos));
			bundle.putString("childPosition", String.valueOf(childPos));
			String deviceName = clickCloudAccount.getDeviceList().get(childPos).getDeviceName();
			bundle.putString("deviceName", deviceName);
			
			bundle.putSerializable("clickCloudAccount", clickCloudAccount); 
			intent.putExtras(bundle);
//			cela.notify_number = 2;
//			cela.notifyDataSetChanged();
			((ChannelListActivity) context).startActivityForResult(intent, 31);
			break;
		default:
			break;
		}
	}

	public ButtonOnclickListener(Context context) {
		super();
		this.context = context;
		csxml = new CloudAccountXML();
	}

	public ButtonOnclickListener(Context context2,ChannelExpandableListviewAdapter cela,CloudAccount clickCloudAccount,List<CloudAccount> groupAccountList, int groupPosition, int childPosition,Button staButton,TextView titleView) {
		this.context = context2;
		csxml = new CloudAccountXML();
		this.clickCloudAccount = clickCloudAccount;
		this.parentPos = groupPosition;
		this.childPos = childPosition;
		this.state_button = staButton;
		this.titleView = titleView;
		this.cela = cela;
		this.groupAccountList = groupAccountList;
		}
}