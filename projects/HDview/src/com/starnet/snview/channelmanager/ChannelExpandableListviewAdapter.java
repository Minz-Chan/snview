package com.starnet.snview.channelmanager;

import java.util.ArrayList;
import java.util.List;

import com.starnet.snview.R;
import com.starnet.snview.channelmanager.xml.ButtonOnTouchListener;
import com.starnet.snview.channelmanager.xml.ButtonOnclickListener;
import com.starnet.snview.channelmanager.xml.ButtonState;
import com.starnet.snview.devicemanager.DeviceItem;
import com.starnet.snview.global.GlobalApplication;
import com.starnet.snview.realplay.PreviewDeviceItem;
import com.starnet.snview.syssetting.CloudAccount;
import com.starnet.snview.util.NetWorkUtils;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
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
public class ChannelExpandableListviewAdapter extends BaseExpandableListAdapter {
	
	private final String TAG = "ChannelExpandableListviewAdapter";
	
	private List<PreviewDeviceItem> mPreviewDeviceItems;//从RealplayActivity中获取预览通道
	
	private List<CloudAccount> groupAccountList;// 用于显示星云账号
	private Context context;
	private LayoutInflater layoutInflater;
	private CloudAccount clickCloudAccount;
	
	private Button button_channel_list;
	private Button state_button;
	private ButtonState bs;	
	private List<DeviceItem> deviceList;
	private boolean isOpen;//判断网络是否打开...
	
	public int notify_number = 1;
	
	private List<Integer> posList = new ArrayList<Integer>();//用于记录需要显示不同颜色的位置
		
	public ChannelExpandableListviewAdapter(Context curContext,List<CloudAccount> groupAccountList) {
		super();
		this.groupAccountList = groupAccountList;
		this.context = curContext;
		this.layoutInflater = ((LayoutInflater) curContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE));
		int size = groupAccountList.size();
		for(int i = 0; i < size; i++){
			CloudAccount cloudAccount = groupAccountList.get(i);
			if(!cloudAccount.isEnabled()){//不需要网络加载的用户，记录其位置...
				posList.add(i);
			}
		}
		isOpen = NetWorkUtils.checkNetConnection(context);
		
		mPreviewDeviceItems = GlobalApplication.getInstance().getRealplayActivity().getPreviewDevices();
		notify_number = 3;
		
	}
	@Override
	public int getGroupCount() {// 获取组的个数
		int size ;
		if (groupAccountList!=null) {
			size = groupAccountList.size();
		}else {
			size = 0;
		}
		return size ;
	}

	@Override
	public int getChildrenCount(int groupPosition) {// 获取每个组对应的孩子的个数
		int size ;
		CloudAccount cloudAccount = groupAccountList.get(groupPosition);
		deviceList = cloudAccount.getDeviceList();
		if (deviceList != null) {
			size = deviceList.size();
		}else {
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
	public View getGroupView(int groupPosition, boolean isExpanded,View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = layoutInflater.inflate(R.layout.channel_listview_account_item_layout, null);
		}
		
		//为组元素设置背景颜色...
		ProgressBar progressBar_net_load = (ProgressBar) convertView.findViewById(R.id.progressBar_net_load);
		if (groupAccountList.get(groupPosition).isRotate()||(!isOpen)) {//判断加载框设置是否为“FALSE”，若是，则显示加载框；否则，不显示；
			progressBar_net_load.setVisibility(View.GONE);
		}	
		
		TextView title = (TextView) convertView.findViewById(R.id.channel_listview_account_item_name);
		
		CloudAccount cloudAccount = groupAccountList.get(groupPosition);
		
		if (notify_number == 3) {
			Log.v(TAG, "===notify_number:==="+notify_number);
			if (mPreviewDeviceItems != null) {
				int size = mPreviewDeviceItems.size();
				for (int i = 0; i < size; i++) {
					PreviewDeviceItem previewDeviceItem = mPreviewDeviceItems.get(i);
					boolean isContained = checkPreviewDeviceItemFromCA(previewDeviceItem,cloudAccount);
					if (isContained) {
						setCloudAccountChannelChoose(previewDeviceItem,cloudAccount);//根据PreviewDeviceItem设置用户的通道选择情况
					}
				}
			}
		}else {
			Log.v(TAG, "&&&&notify_number:&&&"+notify_number);
		}
		
//		CloudAccount cloudAccount = (CloudAccount) getGroup(groupPosition);
		
		String tileName = cloudAccount.getUsername();
		title.setText(tileName);// 设置组名      // 单击之后，箭头该为向下，背景颜色改变
		ImageView itemIcon = (ImageView) convertView.findViewById(R.id.channel_listview_account_item_icon);// 设备图像的展示
		ImageView arrow = (ImageView) convertView.findViewById(R.id.channel_listview_arrow);// 组列表的小箭头的展示
		if (groupAccountList.get(groupPosition).isExpanded()) {// 判断组列表是否展开
			convertView.setBackgroundColor(getColor(R.color.channel_listview_account_item_bg_expanded));// 设置背景颜色
			itemIcon.setBackgroundResource(R.drawable.channel_listview_account_sel);// 设置设备的图像
			arrow.setBackgroundResource(R.drawable.channel_listview_down_arrow_sel);// 设置小箭头的图像
			((ExpandableListView) parent).expandGroup(groupPosition);
		} else {
			convertView.setBackgroundColor(getColor(R.color.channel_listview_account_item_bg_collapsed));// 设置背景颜色
			itemIcon.setBackgroundResource(R.drawable.channel_listview_account);// 设置设备的图像
			arrow.setBackgroundResource(R.drawable.channel_listview_right_arrow);// 设置小箭头的图像
			((ExpandableListView) parent).collapseGroup(groupPosition);
		}
		
		boolean isContain = containPositon(groupPosition,posList);
		if (isContain) {
			convertView.setBackgroundColor(getColor(R.color.listview_bg_noisenable));
		}
		
		
		return convertView;
	}
	
	private void setCloudAccountChannelChoose(PreviewDeviceItem previewDeviceItem, CloudAccount cloudAccount) {
		
		String logPass = previewDeviceItem.getLoginPass();
		String logUser = previewDeviceItem.getDeviceRecordName();
		String logSvrIp = previewDeviceItem.getSvrIp();
		String logSvrPot = previewDeviceItem.getSvrPort();
		
		//定位到cloudAccount的设备
		List<DeviceItem> deviceItems = cloudAccount.getDeviceList();
		if (deviceItems != null) {
			int size = deviceItems.size();
			for (int i =0 ;i<size;i++) {
				DeviceItem deviceItem = deviceItems.get(i);
				String dSvrIp = deviceItem.getSvrIp();
				String dSvrPort = deviceItem.getSvrPort();
				String dUser = deviceItem.getDeviceName();
				String dPass = deviceItem.getLoginPass();
				if (dUser != null) {
					if (dUser.length() >= 4) {
						dUser = dUser.substring(4);
					}
				}
				
				if (logUser.equals(dUser)) {//dUser.contains(logUser) logUser.equals(dUser) logPass.equals(dPass)&&logUser.equals(dUser)&&logSvrIp.equals(dSvrIp)&&logSvrPot.equals(dSvrPort)
					int cur_channel = previewDeviceItem.getChannel();
					Log.v(TAG, "cur_channel:"+cur_channel);
					
					List<Channel> channelList = deviceItem.getChannelList();
					if (channelList != null) {
						int channelSize = channelList.size();
						for (int j = 0; j < channelSize; j++) {
							if (cur_channel == j+1) {
								channelList.get(j).setSelected(true);
							}
						}
					}
				}
			}
		}
	}
	private boolean containPositon(int groupPosition,List<Integer> pList){
		boolean result = false;
		int size = pList.size();
		for(int i =0 ;i<size;i++){
			if(pList.get(i) == groupPosition){
				result = true;
				break;
			}
		}
		return result;
	}

	@Override
	public View getChildView(int groupPosition,int childPosition,boolean isLastChild, View convertView, ViewGroup parent) {//加载子元素
		if (convertView == null) {
			convertView = layoutInflater.inflate(R.layout.channel_listview_channel_item_layout_wadgets, null);
		}		
		TextView title = (TextView) convertView.findViewById(R.id.channel_listview_device_item_name);
		CloudAccount cloudAccount = groupAccountList.get(groupPosition);
		deviceList = cloudAccount.getDeviceList();
		
//		title.setFocusable(true);
//		title.setFocusableInTouchMode(true);
		
		DeviceItem deviceItem = deviceList.get(childPosition);
		String deviceName = deviceItem.getDeviceName();
		title.setText(deviceName);

		// 发现“状态显示按钮”并为之添加单击事件,若选择了该按钮为全满时，需要将该行的"通道列表"置为全选；
		state_button = (Button) convertView.findViewById(R.id.button_state);
		//根据每一组、每一行的通道列表选择情况，来加载对应的state_button的全/半选状态
		String state = getChannelSelectNum(groupPosition, childPosition);
		changeStateButton(state_button,state);
		bs = new ButtonState();
		bs.setState(state);
		ButtonOnTouchListener bolc = new ButtonOnTouchListener(ChannelExpandableListviewAdapter.this,groupPosition, childPosition,state_button,groupAccountList);
		state_button.setOnTouchListener(bolc);

		// 发现“通道列表按钮”并为之添加单击事件
		button_channel_list = (Button) convertView.findViewById(R.id.button_channel_list);
		clickCloudAccount = groupAccountList.get(groupPosition);
		ButtonOnclickListener bol = new ButtonOnclickListener(context,clickCloudAccount,groupPosition,childPosition,state_button);//获取了所在的位置//通过第一个位置，可以获取用户的登陆用户名；通过第二个位置，可以获得是哪一个设备；groupAccountList.get(groupPosition).getDeviceList().get(childPosition);//定位到
		button_channel_list.setOnClickListener(bol);
		
		return convertView;
	}

	protected boolean checkPreviewDeviceItemFromCA(PreviewDeviceItem previewDeviceItem,CloudAccount cloudAccount){
		
		boolean isContained = false;
		if (previewDeviceItem!=null) {
			String platUsername = previewDeviceItem.getPlatformUsername();
			String username = cloudAccount.getUsername();
			if (platUsername!=null) {
				if (platUsername.equals(username)){
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
	private void changeStateButton(Button state_button,String state) {
		if ((state == "all")||(state.equals("all"))) {
			state_button.setBackgroundResource(R.drawable.channellist_select_alled);
		}else if ((state == "half")||(state.equals("half"))) {
			state_button.setBackgroundResource(R.drawable.channel_selected_half);
		}else {
			state_button.setBackgroundResource(R.drawable.channellist_select_empty);
		}
	}
	/**
	 * 
	 * @author zhaohongxu
	 * @Date Jul 11, 2014
	 * @Description 获取通道列表的选择的多少
	 * @param groupPos
	 * @param childPos
	 * @return
	 */
	private String getChannelSelectNum(int groupPos, int childPos) {
		String state = "";
		int channelNum = 0 ;
		int channelSelectNum = 0;
		CloudAccount cloudAccount = groupAccountList.get(groupPos);
		List<DeviceItem> deviceList = cloudAccount.getDeviceList();
		DeviceItem deviceItem = deviceList.get(childPos);
		List<Channel> channels =deviceItem.getChannelList();//得到一个设备对应的通道列表...
		int channelSize = channels.size();
		if(channelSize==0){
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
		}else if ((channelSelectNum > 0)) {
			state = "half";
		}else {
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
	
	
	
}
