package com.starnet.snview.syssetting;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import com.starnet.snview.channelmanager.xml.MD5Util;

//import android.content.Context;
import android.os.Handler;
import android.os.Message;

/** 报警信息验证类 **/
public class AlarmPushIdentifyTask {
//	private Context context;
	private Handler handler;
	private boolean isTimOut;
	private boolean workOver;
	private boolean isCancel;
	private Thread workThread;
	private boolean timOutOver;
	private boolean isIOErrOver;

	private Thread timOutThread;
	private final int timeOut = 5;
	private CloudAccount cloudAccount;
	private boolean isStartIdentifyOver;
	private final int IDENTIFY_SUCCESS = 0x0001;
	private final int IDENTIFY_PSWD_ERR = 0x0006;
	private final int IDENTIFY_USER_ERR = 0x0007;
	private final int IDENTIFY_TIMEOUT_ERR = 0x0008;
	private final int IDENTIFY_DOMN_PORT_ERR = 0x0005;

	public AlarmPushIdentifyTask(Handler handler, CloudAccount cloudAccount) {
		this.handler = handler;
		this.cloudAccount = cloudAccount;
		initialVars();
	}

	private void initialVars() {
		isTimOut = false;
		workOver = false;
		isCancel = false;
		timOutOver = false;
		isIOErrOver = false;
		isStartIdentifyOver = false;
		timOutThread = new Thread() {
			@Override
			public void run() {
				super.run();
				boolean canRun = true;
				int timeCount = 0;
				while (canRun && !timOutOver && !isCancel) {
					try {
						Thread.sleep(1000);
						timeCount++;
						if (timeCount == timeOut) {
							canRun = false;
							onTimeOut();
						}
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		};

		workThread = new Thread() {
			@Override
			public void run() {
				super.run();
				try {
					if (!isCancel && !workOver) {
						startIdentifyWork();
					}
				} catch (IOException e) {
					e.printStackTrace();
					onIOErrWork();
				}
			}
		};
	}

	protected void onIOErrWork() {
		if (!isIOErrOver && !isCancel) {
			workOver = true;
			isTimOut = true;
			timOutOver = true;
			isIOErrOver = true;
			isStartIdentifyOver = true;
			Message msg = new Message();
			msg.what = IDENTIFY_DOMN_PORT_ERR;
			handler.sendMessage(msg);
		}
	}

	protected void startIdentifyWork() throws MalformedURLException,
			IOException {
		HttpURLConnection httpURLConnection;
		String port = cloudAccount.getPort();
		String domn = cloudAccount.getDomain();
		String pswd = cloudAccount.getPassword();
		String uName = cloudAccount.getUsername();
		URL url = new URL("http://" + domn + ":" + port + "/xml_device-list");
		httpURLConnection = (HttpURLConnection) url.openConnection(); // 获取连接
		httpURLConnection.setRequestMethod("POST"); // 设置请求方法为POST, 也可以为GET
		httpURLConnection.setDoOutput(true);
		httpURLConnection.setConnectTimeout(5000);
		String encoded = MD5Util.md5Encode(pswd);
		String reString = "wu=" + uName + "&wp=" + encoded + "&pn=";
		OutputStream os = httpURLConnection.getOutputStream();
		os.write(reString.getBytes());
		os.flush();
		os.close();
		InputStream inputStr = httpURLConnection.getInputStream();
		String result = inputStreamToString(inputStr);
		Message msg = new Message();
		if (result.contains("<resCode>1</resCode>")) {// 用户名错误
			if (!isCancel && !isStartIdentifyOver) {
				setOverTrue();
				msg.what = IDENTIFY_USER_ERR;
				handler.sendMessage(msg);
			}
		} else if (result.contains("<resCode>2</resCode>")) {// 密码错误
			if (!isCancel && !isStartIdentifyOver) {
				setOverTrue();
				msg.what = IDENTIFY_PSWD_ERR;
				handler.sendMessage(msg);
			}
		} else if (result.contains("<resCode>0</resCode>")) {// 登陆成功
			if (!isCancel && !isStartIdentifyOver) {
				setOverTrue();
				msg.what = IDENTIFY_SUCCESS;
				handler.sendMessage(msg);
			}
		}
	}

	private void setOverTrue() {
		workOver = true;
		isTimOut = true;
		timOutOver = true;
		isIOErrOver = true;
		isStartIdentifyOver = true;
	}

	private String inputStreamToString(InputStream is) throws IOException {
		String s = "";
		String line = "";
		BufferedReader rd = new BufferedReader(new InputStreamReader(is));
		while ((line = rd.readLine()) != null) {
			s += line;
		}
		return s;
	}

	public void start() {
		workThread.start();
		timOutThread.start();
	}

	protected void onTimeOut() {
		if (!isTimOut && !isCancel) {
			workOver = true;
			isTimOut = true;
			timOutOver = true;
			isIOErrOver = true;
			isStartIdentifyOver = true;
			Message msg = new Message();
			msg.what = IDENTIFY_TIMEOUT_ERR;
			handler.sendMessage(msg);
		}
	}

	public void setCancel(boolean isCancel) {
		this.isCancel = isCancel;
	}
}