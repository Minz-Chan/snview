package com.starnet.snview.syssetting;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

public class CloudAccountSetEditHandler extends Handler {

	private Context context;//上下文环境
	private int data=0;
	
	@Override
	public void handleMessage(Message msg) {
		super.handleMessage(msg);
		String printSentence = "";
		switch (msg.what) {
		case 0:
			printSentence = "信息保存成功...";
			Toast toast = Toast.makeText(context, printSentence, Toast.LENGTH_LONG);
			toast.show();
			data = 1;
			break;
		}
	}
	
	
	public int getData() {
		return data;
	}
	public void setData(int data) {
		this.data = data;
	}
	public CloudAccountSetEditHandler(Context context) {
		super();
		this.context = context;
	}
}