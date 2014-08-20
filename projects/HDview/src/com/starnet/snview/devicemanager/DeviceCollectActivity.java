package com.starnet.snview.devicemanager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.DocumentException;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnCancelListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.text.Editable;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.starnet.snview.R;
import com.starnet.snview.channelmanager.Channel;
import com.starnet.snview.channelmanager.xml.CloudAccountXML;
import com.starnet.snview.channelmanager.xml.DVRDevice;
import com.starnet.snview.component.BaseActivity;
import com.starnet.snview.syssetting.CloudAccount;
import com.starnet.snview.util.IPAndPortUtils;
import com.starnet.snview.util.NetWorkUtils;
import com.starnet.snview.util.PinyinComparatorUtils;
import com.starnet.snview.util.SynObject;

@SuppressLint("SdCardPath")
public class DeviceCollectActivity extends BaseActivity {
	
	@SuppressWarnings("unused")
	private static final String TAG = "DeviceCollectActivity";

	private final String filePath = "/data/data/com.starnet.snview/deviceItem_list.xml";// 用于保存收藏设备...
	private final String fileName = "/data/data/com.starnet.snview/star_cloudAccount.xml";// 用于从文档中获取所有的用户
	private final int REQUESTCODE = 10;//用于进入DeviceChooseActivity.java的请求码；
	private final int RESULTCODE = 11;

	private Button leftButton;// 左边按钮
	private Button device_add_shdong_btn;// 右边按钮，手动添加,手动输入数据，进行"添加"...
	private Button device_add_choose_btn;//选择按钮，单击可从网络下载星云平台数据,"选择添加"...

	private CloudAccountXML caXML;
	private EditText et_device_add_record;
	private EditText et_device_add_server;
	private EditText et_device_add_port;
	private EditText et_device_add_username;
	private EditText et_device_add_password;
	private EditText et_device_add_channelnumber;
	private EditText et_device_add_defaultchannel;
	private EditText et_device_choose;
	
	private DeviceItem saveDeviceItem = new DeviceItem();

	private final int LOADNETDATADialog = 1;// 从网络下载数据
	private final int LOAD_SUCCESS = 2;
	private final int LOAD_FAILED = 3;
	private final int LOAD_WRONG = 100;
	private SynObject synObject = new SynObject();
	private List<DVRDevice> dvrDeviceList = new ArrayList<DVRDevice>();// 保存全部数据

	private Handler mHandler = new Handler() {
		@SuppressWarnings("deprecation")
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			if (synObject.getStatus() == SynObject.STATUS_RUN) {
				return;
			}
			dismissDialog(LOADNETDATADialog);
			synObject.resume();// 解除线程挂起,向下继续执行...
			switch (msg.what) {
			case LOAD_SUCCESS:
				if (dvrDeviceList.size() > 0) {
					Collections.sort(dvrDeviceList, new PinyinComparatorUtils());
				}
				dismissDialog(LOADNETDATADialog);
				Intent intent = new Intent();
				Bundle bundle = new Bundle();
				bundle.putParcelableArrayList("dvrDeviceList",(ArrayList<? extends Parcelable>) dvrDeviceList);
				intent.putExtras(bundle);
				intent.setClass(DeviceCollectActivity.this,DeviceChooseActivity.class);
				startActivityForResult(intent, REQUESTCODE);
//				DeviceCollectActivity.this.finish();
				break;
			case LOAD_WRONG:
				break;
			case LOAD_FAILED:
				break;
			default:
				break;
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.device_manage_add_baseactivity);
		superChangeViewFromBase();
		leftButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				DeviceCollectActivity.this.finish();
			}
		});

		device_add_shdong_btn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// 判断用户设置为空的时候
				String recordName = getEditTextString(et_device_add_record);
				String serverIP = getEditTextString(et_device_add_server);//IP地址
				String serverPort = getEditTextString(et_device_add_port);//端口号
				String userName = getEditTextString(et_device_add_username);
				String password = getEditTextString(et_device_add_password);
				String channelNumber = getEditTextString(et_device_add_channelnumber);
				String defaultChannel = getEditTextString(et_device_add_defaultchannel);

				// 当所有的内容都不为空的时候，则保存到指定的文档中
				if (!recordName.equals("") && !serverIP.equals("")
					&& !serverPort.equals("") && !userName.equals("")
					&& !defaultChannel.equals("")&& !channelNumber.equals("")) {
					
					
					//进行IP与端口号的检测
					IPAndPortUtils ipapu = new IPAndPortUtils();
					boolean isIP = ipapu.isIp(serverIP);
					boolean isPort = ipapu.isNetPort(serverPort);
					if (isPort&&isIP) {
						int dChannel = Integer.valueOf(defaultChannel);
						int channelNum = Integer.valueOf(channelNumber);
						saveDeviceItem.setDeviceName(recordName);
						saveDeviceItem.setChannelSum(channelNumber);
						saveDeviceItem.setLoginUser(userName);
						saveDeviceItem.setLoginPass(password);
						saveDeviceItem.setDefaultChannel(dChannel);
						saveDeviceItem.setSvrIp(serverIP);
						saveDeviceItem.setSvrPort(serverPort);
						saveDeviceItem.setSecurityProtectionOpen(true);

						try {// 测试saveDeviceItem的数据；？？？？？？？？？？？？
							if (dChannel <= channelNum) {
								List<Channel> channelList = new ArrayList<Channel>();
								for (int i = 0; i < channelNum; i++) {
									Channel channel = new Channel();
									String text = getString(R.string.device_manager_channel);
									channel.setChannelName(text+(i+1));
									channel.setChannelNo((i+1));
									channel.setSelected(false);
									channelList.add(channel);
								}

								saveDeviceItem.setChannelList(channelList);
								String saveResult = caXML.addNewDeviceItemToCollectEquipmentXML(saveDeviceItem, filePath);// 保存
								Toast toast = Toast.makeText(DeviceCollectActivity.this, saveResult,Toast.LENGTH_SHORT);
								toast.show();
								Intent intent = new Intent();
								Bundle bundle = new Bundle();
								bundle.putSerializable("saveDeviceItem",saveDeviceItem);
								intent.putExtras(bundle);
								setResult(11, intent);
								DeviceCollectActivity.this.finish();// 添加成功后，关闭页面...
							} else {
								// 文档读写异常
								String text = getString(R.string.device_manager_deCh_chNum);
								Toast toast = Toast.makeText(DeviceCollectActivity.this, text,Toast.LENGTH_SHORT);
								toast.show();
							}
						} catch (Exception e) {
							String text = getString(R.string.device_manager_save_failed);
							Toast toast = Toast.makeText(DeviceCollectActivity.this, text,Toast.LENGTH_SHORT);
							toast.show();
						}// 保存到指定的文档中
					}else if (!isPort) {
						String text = getString(R.string.device_manager_port_wrong);
						Toast toast = Toast.makeText(DeviceCollectActivity.this, text,Toast.LENGTH_SHORT);
						toast.show();
					}else {
						String text = getString(R.string.device_manager_collect_ip_wrong);
						Toast toast = Toast.makeText(DeviceCollectActivity.this, text,Toast.LENGTH_SHORT);
						toast.show();
					}
				} else {
					String text = getString(R.string.device_manager_collect_null_wrong);
					Toast toast = Toast.makeText(DeviceCollectActivity.this,text, Toast.LENGTH_SHORT);
					toast.show();
				}
			}
		});

		device_add_choose_btn.setOnClickListener(new OnClickListener() {

			// 从网络获取数据，获取后，进入DeviceChooseActivity界面；单击返回后，则不进入；

			@Override
			public void onClick(View v) {
				
				Context context = DeviceCollectActivity.this;
				NetWorkUtils netWorkUtils = new NetWorkUtils();
				boolean isConn = netWorkUtils.checkNetConnection(context);
				if (isConn) {
					String printSentence;
					try {
						List<CloudAccount> cloudAccountList = caXML.getCloudAccountList(fileName);
						int size = cloudAccountList.size();
						if (size > 0) {
							boolean usable = checkAccountUsable(cloudAccountList);
							if (usable) {
								if (dvrDeviceList.size() > 0) {
									dvrDeviceList.clear();
								}
								requestNetDataFromNet();// 请求网络数据；
								synObject.suspend();// 挂起线程
							} else {
								printSentence = getString(R.string.check_account_enabled);
								Toast toast = Toast.makeText(DeviceCollectActivity.this, printSentence,Toast.LENGTH_SHORT);
								toast.show();
							}
						} else {
							printSentence = getString(R.string.check_account_addable);
							Toast toast = Toast.makeText(DeviceCollectActivity.this, printSentence,Toast.LENGTH_SHORT);
							toast.show();
						}
					} catch (Exception e) {
						e.printStackTrace();
						printSentence = getString(R.string.check_account_addable);
						Toast toast = Toast.makeText(DeviceCollectActivity.this,printSentence, Toast.LENGTH_SHORT);
						toast.show();
					}
				}else {
					String printSentence = getString(R.string.network_not_conn);
					Toast toast3 = Toast.makeText(context,printSentence, Toast.LENGTH_LONG);
					toast3.show();
				}
			}	
		});
	}

	@SuppressWarnings("deprecation")
	private void requestNetDataFromNet() {
		showDialog(LOADNETDATADialog);// 显示从网络的加载圈...
		new ObtainDeviceDataFromNetThread(mHandler).start();
	}

	private boolean checkAccountUsable(List<CloudAccount> cloudAccountList) {
		boolean usable = false;
		int size = cloudAccountList.size();
		for (int i = 0; i < size; i++) {
			CloudAccount cloudAccount = cloudAccountList.get(i);
			if (cloudAccount.isEnabled()) {
				usable = true;
				break;
			}
		}
		return usable;
	}

	private void superChangeViewFromBase() {// 得到从父类继承的控件，并修改

		leftButton = super.getLeftButton();
		device_add_shdong_btn = super.getRightButton();

		super.setRightButtonBg(R.drawable.navigation_bar_savebtn_selector);
		super.setLeftButtonBg(R.drawable.navigation_bar_back_btn_selector);
		super.setTitleViewText(getString(R.string.device_manager));
		super.hideExtendButton();
		super.setToolbarVisiable(false);

		caXML = new CloudAccountXML();
		et_device_add_record = (EditText) findViewById(R.id.et_device_add_record);
		et_device_add_server = (EditText) findViewById(R.id.et_device_add_server);
		et_device_add_port = (EditText) findViewById(R.id.et_device_add_port);
		et_device_add_username = (EditText) findViewById(R.id.et_device_add_username);
		et_device_add_password = (EditText) findViewById(R.id.et_device_add_password);
		et_device_add_defaultchannel = (EditText) findViewById(R.id.et_device_add_defaultChannel);
		et_device_add_channelnumber = (EditText) findViewById(R.id.et_device_add_channelnumber);
		et_device_choose = (EditText) findViewById(R.id.device_add_choose_et);
		device_add_choose_btn = (Button) findViewById(R.id.device_add_button_state);
		et_device_choose.setKeyListener(null);
	}

	private String getEditTextString(EditText editText) {
		String content = "";
		Editable editable = editText.getText();
		if (editable != null) {
			content = editable.toString();
		}
		return content;
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case LOADNETDATADialog:
			ProgressDialog progress = ProgressDialog.show(this, "",getString(R.string.loading_devicedata_wait), true, true);
			progress.setOnCancelListener(new OnCancelListener() {
				@SuppressWarnings("deprecation")
				@Override
				public void onCancel(DialogInterface dialog) {
					dismissDialog(LOADNETDATADialog);
					synObject.resume();
				}
			});
			return progress;
		default:
			return null;
		}
	}

	class ObtainDeviceDataFromNetThread extends Thread {
		private Handler handler;

		public ObtainDeviceDataFromNetThread(Handler handler) {
			super();
			this.handler = handler;
		}

		@Override
		public void run() {
			super.run();
			Message msg = new Message();
			caXML = new CloudAccountXML();
			List<CloudAccount> cloudAccountList = new ArrayList<CloudAccount>();
			try {
				cloudAccountList = caXML.getCloudAccountList(fileName);
			} catch (Exception e1) {
				e1.printStackTrace();
			}
			CloudService cloudService = new CloudServiceImpl("");
			int size = cloudAccountList.size();
			try {
				for (int i = 0; i < size; i++) {
					CloudAccount cloudAccount = cloudAccountList.get(i);
					if (cloudAccount.isEnabled()) {
						String dman = cloudAccount.getDomain();
						String port = cloudAccount.getPort();
						String usnm = cloudAccount.getUsername();
						String pasd = cloudAccount.getPassword();
						Document document = cloudService.SendURLPost(dman,port, usnm, pasd);
						String status = cloudService.readXmlStatus(document);
						if (status == null) {// 加载成功...
							List<DVRDevice> deviceList = cloudService.readXmlDVRDevices(document);
							int deviceListSize = deviceList.size();
							for (int j = 0; j < deviceListSize; j++) {
								dvrDeviceList.add(deviceList.get(j));
							}
						} else {// 加载不成功...

						}
					}
				}
				msg.what = LOAD_SUCCESS;
				handler.sendMessage(msg);
			} catch (IOException e) {
				e.printStackTrace();
				if (dvrDeviceList.size() > 0) {
					msg.what = LOAD_SUCCESS;
					handler.sendMessage(msg);
				} else {
					msg.what = LOAD_WRONG;
					handler.sendMessage(msg);
				}
			} catch (DocumentException e) {
				e.printStackTrace();
				if (dvrDeviceList.size() > 0) {
					msg.what = LOAD_SUCCESS;
					handler.sendMessage(msg);
				} else {
					msg.what = LOAD_WRONG;
					handler.sendMessage(msg);
				}
			}
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if ((requestCode == REQUESTCODE)&&(resultCode == RESULTCODE)) {
			if (data != null) {
				Bundle bundle = data.getExtras();
				if (bundle != null) {
					DeviceItem chooseDeviceItem = (DeviceItem) bundle.getSerializable("chooseDeviceItem");
					//填充信息....
					
					String lgUsr = chooseDeviceItem.getLoginUser();
					String lgPas = chooseDeviceItem.getLoginPass();
					String svrIp = chooseDeviceItem.getSvrIp();
					String svrPt = chooseDeviceItem.getSvrPort();
					
					String dName = chooseDeviceItem.getDeviceName();
					String chSum = chooseDeviceItem.getChannelSum();
					int defltChl = chooseDeviceItem.getDefaultChannel();
					String dChnl = String.valueOf(defltChl);
					
					et_device_choose.setText(dName);
					et_device_add_record.setText(dName);
					et_device_add_server.setText(svrIp);
					et_device_add_port.setText(svrPt);
					et_device_add_username.setText(lgUsr);
					
					et_device_add_password.setText(lgPas);
					et_device_add_channelnumber.setText(chSum);
					et_device_add_defaultchannel.setText(dChnl);
					
					et_device_add_channelnumber.setKeyListener(null);
					et_device_choose.setKeyListener(null);
				}
			}
		}
	}
}