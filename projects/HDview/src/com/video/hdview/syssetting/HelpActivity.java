package com.video.hdview.syssetting;

import java.util.Locale;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebView;

import com.video.hdview.R;
import com.video.hdview.component.BaseActivity;

@SuppressLint("SetJavaScriptEnabled")
public class HelpActivity extends BaseActivity {

	private WebView helpView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.help_activity);

		initViews();
		setListeners();
	}

	private void setListeners() {
		super.getLeftButton().setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				HelpActivity.this.finish();
			}
		});
	}

	private void initViews() {
		super.hideRightButton();
		super.hideExtendButton();
		super.getToolbarContainer().setVisibility(View.GONE);
		super.setTitleViewText(getString(R.string.system_setting_help_titile));
		super.setLeftButtonBg(R.drawable.navigation_bar_back_btn_selector);
		helpView = (WebView) findViewById(R.id.help_webview_doc);
		helpView.getSettings().setJavaScriptEnabled(true);
		helpView.loadUrl("file:///android_asset/help/index_cn.html");
	}

	@Override
	protected void onStart() {
		super.onStart();
		helpView.loadUrl("file:///android_asset/help/index_cn.html");
		/*
		if ("zh_CN".equals(Locale.getDefault().toString())) {
			helpView.loadUrl("file:///android_asset/help/index_cn.html");
		} else {
			helpView.loadUrl("file:///android_asset/help/index_en.html");
		}*/
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if ((keyCode == KeyEvent.KEYCODE_BACK) && (helpView.canGoBack())) {
			helpView.goBack();
			return false;
		} else {
			return super.onKeyDown(keyCode, event);
		}
	}
}
