package com.starnet.snview.alarmmanager;

import com.starnet.snview.R;
import com.starnet.snview.component.BaseActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;

public class AlarmImageActivity extends BaseActivity {

	private ImageView showImageView;
	private final int ALARMACTIVITY_REQUESTCODE = 0x0023;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.alarm_imge);
		initViews();
		setListeners();
	}
	
	private void setListeners(){
		super.getLeftButton().setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent data = new Intent();
				data.putExtra("alarmCancel", false);
				setResult(ALARMACTIVITY_REQUESTCODE, data);
				AlarmImageActivity.this.finish();
			}
		});
	}

	private void initViews() {
		super.hideRightButton();
		super.hideExtendButton();
		super.setToolbarVisiable(false);
		String title = getString(R.string.alarm_manageradapter_imagepreview);
		super.getTitleView().setText(title);
//		super.getTitleView().setVisibility(View.GONE);
		super.setLeftButtonBg(R.drawable.navigation_bar_back_btn_selector);
		
		showImageView = (ImageView) findViewById(R.id.img_net);
		Intent intent = getIntent();
		// setTitle(title);
		if (!intent.getBooleanExtra("cancel", false)) {
			if (intent.getBooleanExtra("isExist", false)) {
				String url = intent.getStringExtra("imageUrl");
				Bitmap bitmap = AlarmImageFileCache.getImage(url);
				showImageView.setScaleType(ScaleType.FIT_XY);
//				byte[] imgData = intent.getByteArrayExtra("image");
//				Bitmap bitmap = BitmapFactory.decodeByteArray(imgData, 0,imgData.length);
				showImageView.setImageBitmap(bitmap);
				// setTitle(title);
			}else if (intent.getBooleanExtra("isInExist", false)) {
				String url = intent.getStringExtra("imageUrl");
				Bitmap bitmap = AlarmImageFileCache.getImageInternal(url);
				showImageView.setScaleType(ScaleType.FIT_XY);
				showImageView.setImageBitmap(bitmap);
				// setTitle(title);
			} else {
				// String title = getIntent().getStringExtra("title");
				byte[] imgData = intent.getByteArrayExtra("image");
				Bitmap bitmap = BitmapFactory.decodeByteArray(imgData, 0,imgData.length);
				// setTitle(title);
				showImageView.setScaleType(ScaleType.FIT_XY);
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