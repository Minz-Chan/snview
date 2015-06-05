package com.video.hdview.global;

import com.video.hdview.syssetting.SystemConfiguration;

public class GlobalAppManager {
	private static GlobalAppManager singleton = new GlobalAppManager();
	
	private SystemConfiguration sysconfig;
	
	private GlobalAppManager() {
		sysconfig = new SystemConfiguration();
	}
	
	public static GlobalAppManager getInstance() {
		return singleton;
	}

	public SystemConfiguration getSysconfig() {
		return sysconfig;
	}
	
	
}
