package com.starnet.snview.playback.utils;

import android.os.Parcel;
import android.os.Parcelable;

public class TLV_V_SearchRecordResponse implements Parcelable {

	private int result;
	private int count;
	private int reserve;
	
	public TLV_V_SearchRecordResponse(){
		
	}

	public int getResult() {
		return result;
	}

	public void setResult(int result) {
		this.result = result;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public int getReserve() {
		return reserve;
	}

	public void setReserve(int reserve) {
		this.reserve = reserve;
	}

	private TLV_V_SearchRecordResponse(Parcel in) {
		this.result = in.readInt();
		this.count = in.readInt();
		this.reserve = in.readInt();
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(result);
		dest.writeInt(count);
		dest.writeInt(reserve);
	}

	public final static Parcelable.Creator<TLV_V_SearchRecordResponse> CREATOR = new Parcelable.Creator<TLV_V_SearchRecordResponse>() {

		@Override
		public TLV_V_SearchRecordResponse createFromParcel(Parcel source) {
			return new TLV_V_SearchRecordResponse(source);
		}

		@Override
		public TLV_V_SearchRecordResponse[] newArray(int size) {
			return new TLV_V_SearchRecordResponse[size];
		}
	};
}
