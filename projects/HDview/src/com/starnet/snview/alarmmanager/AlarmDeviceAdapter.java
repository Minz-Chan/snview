package com.starnet.snview.alarmmanager;

import java.util.List;
import com.starnet.snview.R;
import com.starnet.snview.util.NetWorkUtils;
import android.content.Context;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class AlarmDeviceAdapter extends BaseExpandableListAdapter implements
		OnClickListener {

	private final int IMAGE_LOAD_DIALOG = 0x0013;
	private final int VIDEO_LOAD_DIALOG = 0x0014;
	private AlarmImageDownLoadTask imgLoadTask;
	private List<AlarmShowItem> alarmInfoList;
	private Context context;
	private Handler handler;

	public AlarmDeviceAdapter(List<AlarmShowItem> alarmInfoList,
			Context context, Handler handler) {
		this.alarmInfoList = alarmInfoList;
		this.context = context;
		this.handler = handler;
	}

	@Override
	public int getGroupCount() {
		int size = 0;
		if (alarmInfoList != null) {
			size = alarmInfoList.size();
		}
		return size;
	}

	@Override
	public int getChildrenCount(int groupPosition) {
		return 1;
	}

	@Override
	public Object getGroup(int groupPosition) {
		return alarmInfoList.get(groupPosition);
	}

	@Override
	public Object getChild(int groupPosition, int childPosition) {
		return null;
	}

	@Override
	public long getGroupId(int groupPosition) {
		return groupPosition;
	}

	@Override
	public long getChildId(int groupPosition, int childPosition) {
		return 0;
	}

	@Override
	public boolean hasStableIds() {
		return false;
	}

	@Override
	public View getGroupView(int groupPosition, boolean isExpanded,
			View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = LayoutInflater.from(context).inflate(R.layout.alarm_listview_item_layout, null);
		}
		TextView deviceTxt = (TextView) convertView.findViewById(R.id.device_item_name);
		deviceTxt.setText(alarmInfoList.get(groupPosition).getAlarm().getDeviceName());
		ImageView arrowImg = (ImageView) convertView.findViewById(R.id.alarm_arrow_img);
		if (alarmInfoList.get(groupPosition).isExpanded()) {
			arrowImg.setBackgroundResource(R.drawable.channel_listview_down_arrow_sel);
		} else {
			arrowImg.setBackgroundResource(R.drawable.channel_listview_right_arrow_sel);
		}
		return convertView;
	}

	@Override
	public View getChildView(int groupPosition, int childPosition,
			boolean isLastChild, View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = LayoutInflater.from(context).inflate(
					R.layout.alarm_listview_subitem_layout, null);
		}

		Button imgLoadBtn = (Button) convertView.findViewById(R.id.image_load_btn);
		Button vdoLoadBtn = (Button) convertView.findViewById(R.id.video_load_btn);
		imgLoadBtn.setOnClickListener(this);
		vdoLoadBtn.setOnClickListener(this);
		return convertView;
	}

	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition) {
		return false;
	}

	@Override
	public void onClick(View v) {

		String netNotOpenContent = context
				.getString(R.string.alarm_net_notopen);
		switch (v.getId()) {
		case R.id.image_load_btn:
			if (NetWorkUtils.checkNetConnection(context)) {
				showImgLoadDialog(IMAGE_LOAD_DIALOG);
				String imgUrl = "http://img.my.csdn.net/uploads/201402/24/1393242467_3999.jpg";// 测试地址
				// startImageLoadTask(imageUrl);// 开启图片下载线程
				imgLoadTask = new AlarmImageDownLoadTask(imgUrl, context);
				imgLoadTask.setHandler(handler);
				imgLoadTask.start();
			} else {
				Toast.makeText(context, netNotOpenContent, Toast.LENGTH_SHORT)
						.show();
			}
			break;
		case R.id.video_load_btn:
			if (NetWorkUtils.checkNetConnection(context)) {
				showImgLoadDialog(VIDEO_LOAD_DIALOG);
				String imageUrl = "";
				startVideoLoadTask(imageUrl);// 开启视频下载线程
			} else {
				context.getString(R.string.alarm_net_notopen);
				Toast.makeText(context, netNotOpenContent, Toast.LENGTH_SHORT)
						.show();
			}
			break;
		default:
			break;
		}
	}

	@SuppressWarnings("deprecation")
	private void showImgLoadDialog(int id) {
		getAlarmActivity().showDialog(id);
	}

	private void startVideoLoadTask(String videoUrl) {
		// TODO Auto-generated method stub

	}

	private AlarmActivity getAlarmActivity() {
		return (AlarmActivity) context;
	}

	public void cancel(boolean isCanceld) {
		// this.isCanceled = isCanceld;
		imgLoadTask.setCancel(isCanceld);
	}

	// private final int TIMEOUT = 5;
	// private final int REQUESTCODE = 0x0010;
	// private ImageLoaderAyncTask imgLoadTask ;
	// private boolean isCanceled = false;
	// private boolean isTimeOut = false;
	// protected void startImageLoadTask(String imageUrl) {
	// imgLoadTask = new ImageLoaderAyncTask(imageUrl);
	// imgLoadTask.execute(imageUrl);
	// new TimeOutThread().start();
	// }

	// public class ImageLoaderAyncTask extends AsyncTask<String, Void, Bitmap>
	// {
	//
	// private String imageUrl;
	//
	// public ImageLoaderAyncTask(String imageUrl) {
	// this.imageUrl = imageUrl;
	// }
	//
	// @Override
	// protected Bitmap doInBackground(String... params) {// 从网络获取图片
	// Bitmap bitmap = null;
	// try {
	// byte[] data = getImageFromNet();
	// if (data!= null) {
	// bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
	// if (!isCanceled&&!isTimeOut) {
	// getAlarmActivity().dismissProgressDialog();
	// Intent intent = new Intent();
	// intent.setClass(context, AlarmImageActivity.class);
	// intent.putExtra("image", data);
	// getAlarmActivity().startActivityForResult(intent, REQUESTCODE);
	// }
	// }
	// } catch (Exception e) {
	// e.printStackTrace();
	// }
	// return bitmap;
	// }
	//
	// @Override
	// protected void onPostExecute(Bitmap result) {
	// super.onPostExecute(result);
	// }
	//
	// private byte[] getImageFromNet() throws Exception {
	// URL url = new URL(imageUrl);
	// HttpURLConnection conn = (HttpURLConnection) url.openConnection();
	// conn.setConnectTimeout(TIMEOUT * 1000);
	// conn.setRequestMethod("GET");
	// InputStream inStream = conn.getInputStream();
	// if(conn.getResponseCode() == HttpURLConnection.HTTP_OK){
	// return readStream(inStream);
	// }
	// return null;
	// }
	// }
	//
	// private byte[] readStream(InputStream inStream) throws IOException {
	// ByteArrayOutputStream outStream = new ByteArrayOutputStream();
	// byte[] buffer = new byte[1024];
	// int len = 0;
	// while( (len=inStream.read(buffer)) != -1){
	// outStream.write(buffer, 0, len);
	// }
	// outStream.close();
	// inStream.close();
	// return outStream.toByteArray();
	// };

	// class TimeOutThread extends Thread {
	// @Override
	// public void run() {
	// super.run();
	// boolean canRun = true;
	// int countNum = 0;
	// while (canRun && !isCanceled) {
	// try {
	// Thread.sleep(1000);
	// countNum++;
	// if (countNum == TIMEOUT) {
	// canRun = false;
	// onTimeOut();
	// }
	// } catch (InterruptedException e) {
	// e.printStackTrace();
	// }
	// }
	// }
	//
	// private void onTimeOut(){
	// isTimeOut = true;
	// getAlarmActivity().dismissProgressDialog();
	// // Toast.makeText(getAlarmActivity(), "访问网络超时了",
	// Toast.LENGTH_LONG).show();
	// }
	// }
}