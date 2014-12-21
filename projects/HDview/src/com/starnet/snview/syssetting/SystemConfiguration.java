package com.starnet.snview.syssetting;

import com.starnet.snview.global.Constants.PREVIEW_MODE;

public class SystemConfiguration {
	private PREVIEW_MODE defaultPreviewMode;

	public PREVIEW_MODE getDefaultPreviewMode() {
		return defaultPreviewMode;
	}

	public void setDefaultPreviewMode(PREVIEW_MODE defaultPreviewMode) {
		this.defaultPreviewMode = defaultPreviewMode;
	}
}
