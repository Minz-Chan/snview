package com.starnet.snview.alarmmanager;

import com.starnet.snview.R;
import com.starnet.snview.util.BitmapUtils;
import com.starnet.snview.util.SDCardUtils;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.ImageView;

public class AlarmImageActivity extends Activity {

	private final static String TAG = "AlarmImageActivity";
	private ImageView imgNet;
	private final int ALARMACTIVITY_REQUESTCODE = 0x0023;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.alarm_imge);
		imgNet = (ImageView) findViewById(R.id.img_net);
		if (!getIntent().getBooleanExtra("cancel", false)) {
			byte[] imgData = getIntent().getByteArrayExtra("image");
			Bitmap bitmap = BitmapFactory.decodeByteArray(imgData, 0,imgData.length);
			String title = getIntent().getStringExtra("title");
			setTitle(title);
			imgNet.setImageBitmap(bitmap);
			if (SDCardUtils.IS_MOUNTED) {//保存下载的图像文件
				String imageUrl = getIntent().getStringExtra("imageUrl");
				Log.i(TAG, "imageUrl:" + imageUrl);
				String []imageUrls = imageUrl.split("/");
				String imagename = imageUrls[imageUrls.length-1];
				String sdPath = SDCardUtils.getSDCardPath();
				String fullImgPath = sdPath+"/"+imagename;
				BitmapUtils.saveBmpFile(bitmap, fullImgPath);
			}
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK
				&& event.getAction() == KeyEvent.ACTION_DOWN) {
			Intent data = new Intent();
			data.putExtra("alarmCancel", false);
			setResult(ALARMACTIVITY_REQUESTCODE, data);
			AlarmImageActivity.this.finish();
		}
		return super.onKeyDown(keyCode, event);
	}
}