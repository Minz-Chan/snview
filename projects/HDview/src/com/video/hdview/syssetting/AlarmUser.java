package com.video.hdview.syssetting;

import android.os.Parcel;
import android.os.Parcelable;

public class AlarmUser implements Parcelable {

	private String userName;
	private String password;
	
	public AlarmUser() {
		
	}

	private AlarmUser(Parcel in) {
		this.userName = in.readString();
		this.password = in.readString();
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(userName);
		dest.writeString(password);
	}

	public static final Parcelable.Creator<AlarmUser> CREATOR = new Parcelable.Creator<AlarmUser>() {

		@Override
		public AlarmUser createFromParcel(Parcel in) {
			return new AlarmUser(in);
		}

		@Override
		public AlarmUser[] newArray(int size) {
			return new AlarmUser[size];
		}
	};

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

}
