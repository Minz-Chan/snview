package com.video.hdview.playback.utils;

/***
 * 
 * 日期定义
 * @author  	创建人                 肖远东
 * @date        创建日期           2013-03-18
 * @author      修改人                 肖远东
 * @date        修改日期           2013-03-18
 * @description 修改说明	             首次增加
 *
 */
public class OWSP_DATE {
	private int m_year;			//年,2009
	private short m_month;		//月,1-12
	private short m_day;		//日,1-31
	public int getM_year() {
		return m_year;
	}
	public void setM_year(int m_year) {
		this.m_year = m_year;
	}
	public short getM_month() {
		return m_month;
	}
	public void setM_month(short m_month) {
		this.m_month = m_month;
	}
	public short getM_day() {
		return m_day;
	}
	public void setM_day(short m_day) {
		this.m_day = m_day;
	}
}
