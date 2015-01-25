package com.starnet.snview.playback.utils;

import android.os.Parcel;
import android.os.Parcelable;

public class SearchRecordResponse implements Parcelable {

	private int result;
	private int count;
	private int reserve;
	
	public SearchRecordResponse(){
		
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

	private SearchRecordResponse(Parcel in) {
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

	public final static Parcelable.Creator<SearchRecordResponse> CREATOR = new Parcelable.Creator<SearchRecordResponse>() {

		@Override
		public SearchRecordResponse createFromParcel(Parcel source) {
			return new SearchRecordResponse(source);
		}

		@Override
		public SearchRecordResponse[] newArray(int size) {
			return new SearchRecordResponse[size];
		}
	};
}
