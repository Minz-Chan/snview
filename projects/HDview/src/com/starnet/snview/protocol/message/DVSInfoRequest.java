package com.starnet.snview.protocol.message;

public class DVSInfoRequest {
	private String companyIdentity;
	private String equipmentIdentity;
	private String equipmentName;
	private String equipmentVersion;
	private OwspDate owspDate;
	private int channleNumber;
	private int reserve1;
	private int reserve2;
	private int reserve3;
	
	
	public String getCompanyIdentity() {
		return companyIdentity;
	}
	public void setCompanyIdentity(String companyIdentity) {
		this.companyIdentity = companyIdentity;
	}
	public String getEquipmentIdentity() {
		return equipmentIdentity;
	}
	public void setEquipmentIdentity(String equipmentIdentity) {
		this.equipmentIdentity = equipmentIdentity;
	}
	public String getEquipmentName() {
		return equipmentName;
	}
	public void setEquipmentName(String equipmentName) {
		this.equipmentName = equipmentName;
	}
	public String getEquipmentVersion() {
		return equipmentVersion;
	}
	public void setEquipmentVersion(String equipmentVersion) {
		this.equipmentVersion = equipmentVersion;
	}
	public OwspDate getOwspDate() {
		return owspDate;
	}
	public void setOwspDate(OwspDate owspDate) {
		this.owspDate = owspDate;
	}
	public int getChannleNumber() {
		return channleNumber;
	}
	public void setChannleNumber(int channleNumber) {
		this.channleNumber = channleNumber;
	}
	public int getReserve1() {
		return reserve1;
	}
	public void setReserve1(int reserve1) {
		this.reserve1 = reserve1;
	}
	public int getReserve2() {
		return reserve2;
	}
	public void setReserve2(int reserve2) {
		this.reserve2 = reserve2;
	}
	public int getReserve3() {
		return reserve3;
	}
	public void setReserve3(int reserve3) {
		this.reserve3 = reserve3;
	}
}
