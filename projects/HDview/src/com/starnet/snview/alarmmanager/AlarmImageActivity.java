package com.starnet.snview.alarmmanager;

import com.starnet.snview.R;

import android.app.Activity;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.KeyEvent;
import android.widget.ImageView;

public class AlarmImageActivity extends Activity {

	private ImageView imgNet;
	private final int REQUESTCODE = 0x0023;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.alarm_imge);
		byte[] imgData = getIntent().getByteArrayExtra("image");
		imgNet = (ImageView) findViewById(R.id.img_net);
		imgNet.setImageBitmap(BitmapFactory.decodeByteArray(imgData, 0,imgData.length));
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(keyCode == KeyEvent.KEYCODE_BACK
				&& event.getAction() == KeyEvent.ACTION_DOWN){
			Intent data = new Intent();
			data.putExtra("cancel", false);
			setResult(REQUESTCODE, data);
//			startActivity(data);
			AlarmImageActivity.this.finish();
		}
		return super.onKeyDown(keyCode, event);
	}
	
	

}
