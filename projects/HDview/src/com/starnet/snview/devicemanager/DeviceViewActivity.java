package com.starnet.snview.devicemanager;

import java.io.IOException;
import java.util.List;

import org.dom4j.DocumentException;

import android.annotation.SuppressLint;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.ListView;

import com.starnet.snview.R;
import com.starnet.snview.channelmanager.xml.CloudAccountXML;
import com.starnet.snview.component.BaseActivity;

@SuppressLint("SdCardPath")
public class DeviceViewActivity extends BaseActivity {
	@SuppressWarnings("unused")
	private static final String TAG = "DeviceViewActivity";
	private final String filePath = "/data/data/com.starnet.snview/deviceItem_list.xml";
	private CloudAccountXML caxml;
	
	private ListView mDeviceList;
	private Button navigation_bar_add_btn;//zk
	private DeviceListAdapter dLAdapter;
	List<DeviceItem> deviceItemList;
	private DeviceItem deleteDeviceItem;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.device_manager_activity);
		
		initView();
		
		mDeviceList.setOnItemClickListener(new OnItemClickListener(){

			@Override
			public void onItemClick(AdapterView<?> parent, View view,int position, long id) {
				//进入该设备的信息界面
				DeviceItem clickDeviceItem = deviceItemList.get(position);
				Intent intent = new Intent();
				intent.setClass(DeviceViewActivity.this, DeviceScanActivity.class);
				Bundle bundle = new Bundle();
				bundle.putSerializable("clickDeviceItem", clickDeviceItem);
				intent.putExtras(bundle);
				startActivityForResult(intent,20);
			}
		});
		
		mDeviceList.setOnItemLongClickListener(new OnItemLongClickListener(){

			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view,final int position, long id) {
				Builder builder = new Builder(DeviceViewActivity.this);
				deleteDeviceItem = deviceItemList.get(position);
				String titleName = deleteDeviceItem.getDeviceName().substring(4);
				builder.setTitle(getString(R.string.system_setting_delete_device)+" "+titleName+" ?");
				
				builder.setPositiveButton(getString(R.string.channel_listview_ok),new DialogInterface.OnClickListener(){
					@Override
					public void onClick(DialogInterface dialog, int which) {
						//从文档中删除操作....
						
						caxml = new CloudAccountXML();
						try {
							caxml.removeDeviceItemToCollectEquipmentXML(deleteDeviceItem, filePath);
						} catch (DocumentException e) {
							e.printStackTrace();
						} catch (IOException e) {
							e.printStackTrace();
						}
						//从列表中删除操作，并且通知列表更新；
						deviceItemList.remove(position);
						dLAdapter.notifyDataSetChanged();//列表的更新操作。。。
					}
				 });
				
				 builder.setNegativeButton(getString(R.string.channel_listview_cancel),new DialogInterface.OnClickListener(){
						@Override
						public void onClick(DialogInterface dialog, int which) {
							
						} 
					 });
				builder.show();
				return true;
			}
		});
	}
	
	
	private void initView() {
		super.setTitleViewText(getString(R.string.navigation_title_device_management));
		super.hideExtendButton();
		super.setRightButtonBg(R.drawable.navigation_bar_add_btn_selector);
		super.setToolbarVisiable(false);
		
		caxml = new CloudAccountXML();
		mDeviceList = (ListView) findViewById(R.id.device_listview);
		navigation_bar_add_btn = (Button) findViewById(R.id.base_navigationbar_right_btn);//zk
		
		try {
			deviceItemList = caxml.getCollectDeviceListFromXML(filePath);
			dLAdapter = new DeviceListAdapter(this, deviceItemList);
			mDeviceList.setAdapter(dLAdapter);
		} catch (Exception e) {
			System.out.println(e.toString());
		}
		navigation_bar_add_btn.setOnClickListener(new OnClickListener() {//手动与选择增加设备...
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent();
				intent.setClass(DeviceViewActivity.this, DeviceCollectActivity.class);
				startActivityForResult(intent,10);
			}
		});
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if(requestCode == 10){//从添加设备界面返回后...
			if (data != null) {
				Bundle bundle = data.getExtras();
				if (bundle != null) {
					DeviceItem saveDeviceItem = (DeviceItem) bundle.getSerializable("saveDeviceItem");
					boolean result = checkContainDeviceItem(saveDeviceItem,deviceItemList);//检查设备中是否包含saveDeviceItem
					if (!result) {
						deviceItemList.add(saveDeviceItem);
					}
				}
			}
			dLAdapter.notifyDataSetChanged();	
		}else if(requestCode == 20){//从查看\编辑设备界面返回后...
			dLAdapter.notifyDataSetChanged();
		}
	}


	private boolean checkContainDeviceItem(DeviceItem saveDeviceItem,List<DeviceItem> deviceItemList) {
		boolean result = false;
		int size = deviceItemList.size();
		for (int i = 0; i < size; i++) {
			DeviceItem deviceItem = deviceItemList.get(i);
			
			String dName = deviceItem.getDeviceName();
			String svrIp = deviceItem.getSvrIp();
			String sPort = deviceItem.getSvrPort();
			String lUser = deviceItem.getLoginUser();
			String lPass = deviceItem.getLoginPass();
			
			String saveLPass = saveDeviceItem.getLoginPass();
			String saveLUser = saveDeviceItem.getLoginUser();
			String saveSvrIp = saveDeviceItem.getSvrIp();
			String saveSPort = saveDeviceItem.getSvrPort();
			String saveDName = saveDeviceItem.getDeviceName();
			
			if ((dName.equals(saveDName)||(dName == saveDName))&&(sPort.equals(saveSPort)||(sPort == saveSPort))
				&&(svrIp.equals(saveSvrIp)||(svrIp == saveSvrIp))&&(lUser.equals(saveLUser)||(lUser == saveLUser))
				&&(lPass.equals(saveLPass)||(lPass == saveLPass))) {
				result = true;
				break;
			}
		}
		return result;
	}
}