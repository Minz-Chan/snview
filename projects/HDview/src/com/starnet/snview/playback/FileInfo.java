package com.starnet.snview.playback;

import java.util.Calendar;

public class FileInfo
  implements Comparable<FileInfo>
{
  private Calendar mStartTime = null;
  private Calendar mStopTime = null;
  private int mType = 0;
  
  public FileInfo(int paramInt, Calendar paramCalendar1, Calendar paramCalendar2)
  {
    this.mType = paramInt;
    this.mStartTime = paramCalendar1;
    this.mStopTime = paramCalendar2;
  }
  
  public int compareTo(FileInfo paramFileInfo)
  {
    return this.mStartTime.compareTo(paramFileInfo.mStartTime);
  }
  
  public Calendar getStartTime()
  {
    return this.mStartTime;
  }
  
  public long getStartTimeInMillis()
  {
    return this.mStartTime.getTimeInMillis();
  }
  
  public Calendar getStopTime()
  {
    return this.mStopTime;
  }
  
  public long getStopTimeInMillis()
  {
    return this.mStopTime.getTimeInMillis();
  }
  
  public int getType()
  {
    return this.mType;
  }
}


/* Location:           D:\kuaipan\我的资料\研究生阶段\项目\星网安防\star-security\iVMS-4500\classes_dex2jar.jar
 * Qualified Name:     com.mcu.iVMS.playback.FileInfo
 * JD-Core Version:    0.7.0-SNAPSHOT-20130630
 */