package com.starnet.snview.alarmmanager;

import com.starnet.snview.R;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Window;
import android.widget.ImageView;

public class AlarmImageActivity extends Activity {

	private ImageView showImageView;
	private final int ALARMACTIVITY_REQUESTCODE = 0x0023;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.alarm_imge);
		showImageView = (ImageView) findViewById(R.id.img_net);
		Intent intent = getIntent();
//		setTitle(title);
		if (!intent.getBooleanExtra("cancel", false)) {
			if (intent.getBooleanExtra("isExist", false)) {
				String url = intent.getStringExtra("imageUrl");
				Bitmap bitmap = AlarmImageFileCache.getImage(url);
				String title = getIntent().getStringExtra("title");
				showImageView.setImageBitmap(bitmap);
				setTitle(title);
			}else {
				String title = getIntent().getStringExtra("title");
				byte[] imgData = getIntent().getByteArrayExtra("image");
				Bitmap bitmap = BitmapFactory.decodeByteArray(imgData, 0,imgData.length);
				setTitle(title);
				showImageView.setImageBitmap(bitmap);
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