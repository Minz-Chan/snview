package com.starnet.snview.channelmanager.xml;

import java.util.List;

import com.starnet.snview.R;
import com.starnet.snview.channelmanager.Channel;
import com.starnet.snview.channelmanager.ChannelExpandableListviewAdapter;
import com.starnet.snview.devicemanager.DeviceItem;
import com.starnet.snview.syssetting.CloudAccount;
import com.starnet.snview.util.ClickUtils;

import android.annotation.SuppressLint;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.Button;

@SuppressLint("SdCardPath")
public class ButtonOnTouchListener implements OnTouchListener {
	
	private final String filePath = "/data/data/com.starnet.snview/deviceItem_list.xml";

	private int parentPos;// 父元素的位置
	private int childPos;// 子元素的位置

	private Button state_button;
//	private ButtonState bs;

	private CloudAccountXML csxml;//
	private List<CloudAccount> cloudAccountList;// 星云账号信息
	private CloudAccount selectCloudAccount;
	private DeviceItem deviceItem;
	ChannelExpandableListviewAdapter cela;

	public ButtonOnTouchListener(ChannelExpandableListviewAdapter cela,int groupPosition, int childPosition,Button state_button,List<CloudAccount> groupAccountList) {
		super();
		this.parentPos = groupPosition;
		this.childPos = childPosition;
		this.state_button = state_button;
		this.cloudAccountList = groupAccountList;
		csxml = new CloudAccountXML();
		this.cela = cela;
	};
	
	public ButtonOnTouchListener(int groupPosition, int childPosition,Button state_button, ButtonState bs,List<CloudAccount> groupAccountList) {
		super();
		this.parentPos = groupPosition;
		this.childPos = childPosition;
		this.state_button = state_button;
//		this.bs = bs;
		this.cloudAccountList = groupAccountList;
		csxml = new CloudAccountXML();
	};

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		
		if (!ClickUtils.isFastDoubleClick()) {
			selectCloudAccount = cloudAccountList.get(parentPos);
			deviceItem = selectCloudAccount.getDeviceList().get(childPos);
			csxml = new CloudAccountXML();
			List<Channel> channels = deviceItem.getChannelList();
			String state = getChannelSelectNum(deviceItem);
			
			if ((state == "half")||(state.equals("half"))) {
				state_button.setBackgroundResource(R.drawable.channellist_select_alled);
//				bs.setState("all");					
				//将通道列表的状态写入到指定的XML状态文件中;1、修改某一组中某一个选项的通道列表的信息
				int channelSize = channels.size();
				for (int i = 0; i < channelSize; i++) {
					channels.get(i).setSelected(true);
				}	
				cela.notify_number = 2;
			}else if ((state == "all")||(state.equals("all"))) {
				state_button.setBackgroundResource(R.drawable.channellist_select_empty);
//				bs.setState("empty");					
				//将通道列表的状态写入到指定的XML状态文件中,1、修改某一组中某一个选项的通道列表的信息		
				int channelSize = channels.size();
				for (int i = 0; i < channelSize; i++) {
					channels.get(i).setSelected(false);
				}	
				cela.notify_number = 2;
			}else {					/*zz_empty_select*/
				state_button.setBackgroundResource(R.drawable.channellist_select_alled);
//				bs.setState("all");					
				//将通道列表的状态写入到指定的XML状态文件中 ;1、修改某一组中某一个选项的通道列表的信息
//				DeviceItem deviceItem = selectCloudAccount.getDeviceList().get(childPos);
				int channelSize = channels.size();
				for (int i = 0; i < channelSize; i++) {
					channels.get(i).setSelected(true);
				}
				cela.notify_number = 2;
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
			
		}
		
		return false;
	}
	
	private String getChannelSelectNum(DeviceItem deviceItem) {
		String state = "";
		int channelNum = 0 ;
		int channelSelectNum = 0;
		List<Channel> channels =deviceItem.getChannelList();
		int channelSize = channels.size();
		for (int k = 0; k < channelSize; k++) {		
			channelNum++;	
			if (channels.get(k).isSelected()) {
				channelSelectNum++;
			}
		}	
		if (channelNum == channelSelectNum) {
			state = "all";
		}else if ((channelSelectNum > 0)) {
			state = "half";
		}else {
			state = "empty";
		}
		return state;
	}
}