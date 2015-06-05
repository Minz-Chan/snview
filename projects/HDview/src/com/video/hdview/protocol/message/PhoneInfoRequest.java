package com.video.hdview.protocol.message;

public class PhoneInfoRequest {
	private String equipmentIdentity;
	private String equipmentOS;
	private int reserve1;
	private int reserve2;
	private int reserve3;
	private int reserve4;
	
	
	public String getEquipmentIdentity() {
		return equipmentIdentity;
	}
	public void setEquipmentIdentity(String equipmentIdentity) {
		this.equipmentIdentity = equipmentIdentity;
	}
	public String getEquipmentOS() {
		return equipmentOS;
	}
	public void setEquipmentOS(String equipmentOS) {
		this.equipmentOS = equipmentOS;
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
	public int getReserve4() {
		return reserve4;
	}
	public void setReserve4(int reserve4) {
		this.reserve4 = reserve4;
	}
	
	
}
