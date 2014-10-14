package com.starnet.snview.channelmanager.xml;

import java.util.List;

import com.starnet.snview.R;
import com.starnet.snview.channelmanager.Channel;
import com.starnet.snview.channelmanager.ChannelExpandableListviewAdapter;
import com.starnet.snview.devicemanager.DeviceItem;
import com.starnet.snview.syssetting.CloudAccount;
import com.starnet.snview.util.ClickUtils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.TextView;

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
	private TextView titleView;
	private Context context;
	
//	private int click_time ;

	public ButtonOnTouchListener(Context context,ChannelExpandableListviewAdapter cela,TextView titleView,int groupPosition, int childPosition,Button state_button,List<CloudAccount> groupAccountList) {
		super();
		this.parentPos = groupPosition;
		this.childPos = childPosition;
		this.state_button = state_button;
		this.cloudAccountList = groupAccountList;
		csxml = new CloudAccountXML();
		this.cela = cela;
		this.titleView = titleView;
		this.context = context;
//		this.click_time = click_time;
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
				//将通道列表的状态写入到指定的XML状态文件中;1、修改某一组中某一个选项的通道列表的信息
				int channelSize = channels.size();
				for (int i = 0; i < channelSize; i++) {
					channels.get(i).setSelected(true);
				}	
				cela.notify_number = 2;
				cela.notifyDataSetChanged();
			}else if ((state == "all")||(state.equals("all"))) {
				state_button.setBackgroundResource(R.drawable.channellist_select_empty);		
				//将通道列表的状态写入到指定的XML状态文件中,1、修改某一组中某一个选项的通道列表的信息		
				int channelSize = channels.size();
				for (int i = 0; i < channelSize; i++) {
					channels.get(i).setSelected(false);
				}	
				cela.notify_number = 2;
				cela.notifyDataSetChanged();
			}else {					/*zz_empty_select*/
				state_button.setBackgroundResource(R.drawable.channellist_select_alled);	
				//将通道列表的状态写入到指定的XML状态文件中 ;1、修改某一组中某一个选项的通道列表的信息
				int channelSize = channels.size();
				for (int i = 0; i < channelSize; i++) {
					channels.get(i).setSelected(true);
				}
				cela.notify_number = 2;
				cela.notifyDataSetChanged();
			}
			
			int number = getPreviewListFromCloudAccounts(cloudAccountList);
			if(number == 0){
				titleView.setText(context.getString(R.string.navigation_title_channel_list));// 设置列表标题名
			}else{
				titleView.setText(context.getString(R.string.navigation_title_channel_list)+"("+number+")");// 设置列表标题名
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
		
		return true;
	}
	
	private int getPreviewListFromCloudAccounts(List<CloudAccount> cloudAccountList2) {
		if((cloudAccountList2 == null)||(cloudAccountList2 !=null && cloudAccountList2.size() == 0)){
			return 0 ;
		}
		int number = 0 ;
		int size = cloudAccountList2.size();
		for(int i =0 ;i<size ;i++){
			CloudAccount cloudAccount = cloudAccountList2.get(i);
			List<DeviceItem> deviceItemList = cloudAccount.getDeviceList();
			if(deviceItemList != null){
				int deviceSize = deviceItemList.size();
				for(int j =0 ;j<deviceSize ;j++){
					DeviceItem deviceItem = deviceItemList.get(j);
					if(deviceItem != null){
						List<Channel> channelList = deviceItem.getChannelList();
						int channelSize = channelList.size();
						for(int k =0 ;k<channelSize;k++){
							Channel channel = channelList.get(k);
							if((channel!=null)&&channel.isSelected()){
								number++;
							}
						}
					}
				}
			}
		}
		return number;
	}

	public String getChannelSelectNum(DeviceItem deviceItem) {
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