package com.starnet.snview.channelmanager.xml;

import java.util.List;

import com.starnet.snview.R;
import com.starnet.snview.channelmanager.Channel;
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
	
	private CloudAccountXML csxml;//
	private List<CloudAccount> cloudAccountList;//星云账号信息
	private CloudAccount clickCloudAccount;//星云账号信息
	
	List<PreviewDeviceItem> previewChannelList;
	CloudAccount selectCloudAccount;
	
	public ButtonOnclickListener(int parentPos, int childPos,Button state_button, ButtonState bs,List<CloudAccount> cloudAccountList) {
		super();
		this.parentPos = parentPos;
		this.childPos = childPos;
		this.state_button = state_button;
		this.bs = bs;
		this.cloudAccountList = cloudAccountList;
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
			
			bundle.putSerializable("clickCloudAccount", clickCloudAccount);
			intent.putExtras(bundle);
			
			((ChannelListActivity) context).startActivityForResult(intent, 31);
			break;
		case R.id.button_state:
			selectCloudAccount = cloudAccountList.get(parentPos);
			csxml = new CloudAccountXML();
			
			if ((bs.getState() == "half")||(bs.getState().equals("half"))) {
				state_button.setBackgroundResource(R.drawable.channel_selected_half);
				bs.setState("all");					
				//将通道列表的状态写入到指定的XML状态文件中;1、修改某一组中某一个选项的通道列表的信息
				DeviceItem deviceItem = selectCloudAccount.getDeviceList().get(childPos);
				List<Channel> channels = deviceItem.getChannelList();
				int channelSize = channels.size();
				for (int i = 0; i < channelSize; i++) {
					channels.get(i).setSelected(false);
				}			
			}else if ((bs.getState() == "all")||(bs.getState().equals("all"))) {
				state_button.setBackgroundResource(R.drawable.channellist_select_alled);
				bs.setState("empty");					
				//将通道列表的状态写入到指定的XML状态文件中
				//1、修改某一组中某一个选项的通道列表的信息
				DeviceItem deviceItem = selectCloudAccount.getDeviceList().get(childPos);
				List<Channel> channels = deviceItem.getChannelList();
				int channelSize = channels.size();
				for (int i = 0; i < channelSize; i++) {
					channels.get(i).setSelected(true);
				}				
			}else {					
				state_button.setBackgroundResource(R.drawable.channellist_select_empty);
				bs.setState("all");					
				//将通道列表的状态写入到指定的XML状态文件中	
				//1、修改某一组中某一个选项的通道列表的信息
				DeviceItem deviceItem = selectCloudAccount.getDeviceList().get(childPos);
				List<Channel> channels = deviceItem.getChannelList();
				int channelSize = channels.size();
				for (int i = 0; i < channelSize; i++) {
					channels.get(i).setSelected(false);
				}
			}
			if(selectCloudAccount.getUsername().equals("收藏设备")&&(selectCloudAccount.getDomain().equals("com"))
					&&(selectCloudAccount.getPort().equals("808"))&&(selectCloudAccount.getPassword().equals("0208"))){
				Thread thread = new Thread(){
					@Override
					public void run() {
						super.run();
						List<DeviceItem> deviceList = selectCloudAccount.getDeviceList();
						int size = deviceList.size();
						for(int i =0 ;i<size;i++){
							try {
								csxml.addNewDeviceItemToCollectEquipmentXML(deviceList.get(i), filePath);
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					}
				};
				thread.start();
			}
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

	public ButtonOnclickListener(Context context2,CloudAccount clickCloudAccount, int groupPosition, int childPosition,Button staButton) {
		this.context = context2;
		csxml = new CloudAccountXML();
		this.clickCloudAccount = clickCloudAccount;
		this.parentPos = groupPosition;
		this.childPos = childPosition;
		this.state_button = staButton;
		}
}