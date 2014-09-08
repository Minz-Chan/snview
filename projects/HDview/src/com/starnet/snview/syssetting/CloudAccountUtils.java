package com.starnet.snview.syssetting;

public class CloudAccountUtils {
	
	/**
	 * 检测两个星云账户是否相同
	 * @param cloudAccount1
	 * @param cloudAccount2
	 * @return
	 */
	public static boolean checkSameBetweenCloudAccounts(
			CloudAccount cloudAccount1, CloudAccount cloudAccount2) {

		boolean isSample = false;

		if ((cloudAccount1 == null) || (cloudAccount2 == null)) {
			return isSample;
		}
		
		String domain1 = cloudAccount1.getDomain() ;
		String domain2 = cloudAccount2.getDomain();
		
		String password1 = cloudAccount1.getPassword();
		String password2 = cloudAccount2.getPassword();
		
		String port1 = cloudAccount1.getPort();
		String port2 = cloudAccount2.getPort();
		
		String username1 = cloudAccount1.getUsername();
		String username2 = cloudAccount2.getUsername();
		
		if (domain1.equals(domain2) && password1.equals(password2)
				&& port1.equals(port2) && username1.equals(username2)) {
			isSample = true;
		}
		return isSample;
	}
}