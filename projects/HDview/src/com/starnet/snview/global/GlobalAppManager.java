package com.starnet.snview.global;

import com.starnet.snview.syssetting.SystemConfiguration;

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
