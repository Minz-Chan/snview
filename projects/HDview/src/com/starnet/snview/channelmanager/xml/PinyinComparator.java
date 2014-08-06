package com.starnet.snview.channelmanager.xml;

import java.util.Comparator;

import com.starnet.snview.devicemanager.DeviceItem;

import net.sourceforge.pinyin4j.PinyinHelper;

/**
 * @function     功能	                  拼音排序实现类
 * @author       创建人                        陈明珍
 * @date        创建日期           2013-11-05
 * @author       修改人                        陈明珍
 * @date        修改日期           2013-11-05
 * @description 修改说明	            首次增加
 */
public class PinyinComparator implements Comparator<Object> {

    public int compare(Object o1, Object o2) {  
    	DeviceItem deviceItem1 = (DeviceItem) o1;
    	DeviceItem deviceItem2 = (DeviceItem) o2;
    	String key1 = deviceItem1.getDeviceName();
    	String key2 = deviceItem2.getDeviceName();
//    	String key1 = o1.toString();
//     	String key2 = o2.toString();
     	
     	if (key1.substring(0, 4).equals(key2.substring(0, 4))) {
     		for (int i = 0; i < key1.length() && i < key2.length(); i++) { 
                int codePoint1 = key1.charAt(i);  
                int codePoint2 = key2.charAt(i);  

                if (Character.isSupplementaryCodePoint(codePoint1)  
                        || Character.isSupplementaryCodePoint(codePoint2)) {
                    i++; 
                }  

                if (codePoint1 != codePoint2) { 
                    if (Character.isSupplementaryCodePoint(codePoint1)
                            || Character.isSupplementaryCodePoint(codePoint2)) {
                        return codePoint1 - codePoint2;  
                    }  

                    String pinyin1 = pinyin((char) codePoint1);
                    String pinyin2 = pinyin((char) codePoint2);  

                    if (pinyin1 != null && pinyin2 != null) { // 两个字符都是汉字  
                        if (!pinyin1.equals(pinyin2)) {  
                            return pinyin1.compareTo(pinyin2);  
                        }  
                    } else {  
                        return codePoint1 - codePoint2;  
                    }  
                }  
            }  

            return key1.length() - key2.length();
     	} else {
     		if (key1.substring(0, 4).equals("(在线)")) {
     			return -1;
     		} else {
     			return 1;
     		}
     	}
    }  

 

    private String pinyin(char c) {  
        String[] pinyins = PinyinHelper.toHanyuPinyinStringArray(c);  

        if (pinyins == null) {  
            return null;   //如果转换结果为空，则返回null
        } 
        return pinyins[0];   //如果为多音字返回第一个音节

    }

}