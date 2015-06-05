/*
 * FileName:BByteConvert.java
 * 
 * Package:com.starsecurity.util
 * 
 * Date:2013-03-19
 * 
 * Copyright: Copyright (c) 2013 Minz.Chan
 */
package com.video.hdview.playback.utils;


/**
 * @function     功能	  byte数组与各种数据类型间的互转
 *     工作项目需要在 java 和 c/c++ 之间进行 socket 通信， socket 通信是以字节流或者字节包
 * 进行的， socket发送方须将数据转换为字节流或者字节包，而接收方则将字节流和字节包再转换回
 * 相应的数据类型。如果发送方和接收方都是同种语言，则一般只涉及到字节序的调整。而对于 java
 * 和 c/c++的通信，则情况就要复杂一些，主要是因为 java 中没有 unsigned 类型，并且 java和c在
 * 某些数据类型上的长度不一致。本类就是针对这种情况，整理了 java 数据类型和网络字节流(Big-
 * Endian)或字节包 ( 相当于 java 的 byte 数组 ) 之间转换方法。
 * @author       创建人                 陈明珍
 * @date        创建日期           2013-03-19
 * @author       修改人                 陈明珍
 * @date        修改日期           2013-03-19
 * @description 修改说明	             首次增加
 */
public class BByteConvert {  
    
    /** 
     * 长整型转byte数组 
     *  
     * @param n 长整型数
     * @return 转换后的byte数组 
     */  
    public static byte[] longToBytes(long n) {  
        byte[] b = new byte[8];  
        b[7] = (byte) (n & 0xff);  
        b[6] = (byte) (n >> 8 & 0xff);  
        b[5] = (byte) (n >> 16 & 0xff);  
        b[4] = (byte) (n >> 24 & 0xff);  
        b[3] = (byte) (n >> 32 & 0xff);  
        b[2] = (byte) (n >> 40 & 0xff);  
        b[1] = (byte) (n >> 48 & 0xff);  
        b[0] = (byte) (n >> 56 & 0xff);  
        return b;  
    }  
  
    /** 
     * 长整型转byte数组 
     *  
     * @param n 长整型数字 
     * @param array 转换后的byte数组 
     * @param offset 从第offset位开始转换 
     */  
    public static void longToBytes(long n, byte[] array, int offset) {  
        array[7 + offset] = (byte) (n & 0xff);  
        array[6 + offset] = (byte) (n >> 8 & 0xff);  
        array[5 + offset] = (byte) (n >> 16 & 0xff);  
        array[4 + offset] = (byte) (n >> 24 & 0xff);  
        array[3 + offset] = (byte) (n >> 32 & 0xff);  
        array[2 + offset] = (byte) (n >> 40 & 0xff);  
        array[1 + offset] = (byte) (n >> 48 & 0xff);  
        array[0 + offset] = (byte) (n >> 56 & 0xff);  
    }  
  
    /** 
     * byte数组转长整型 
     *  
     * @param array 要转换的byte数组
     * @return 转换后的长整型数字
     */  
    public static long bytesToLong(byte[] array) {  
        return ((((long) array[0] & 0xff) << 56) | (((long) array[1] & 0xff) << 48) | (((long) array[2] & 0xff) << 40)  
                | (((long) array[3] & 0xff) << 32) | (((long) array[4] & 0xff) << 24)  
                | (((long) array[5] & 0xff) << 16) | (((long) array[6] & 0xff) << 8) | (((long) array[7] & 0xff) << 0));  
    }  
  
    /** 
     * byte数组转长整型数字 
     *  
     * @param array 要转换的byte数组 
     * @param offset 从第offset开始转换 
     * @return 转换后的长整型数字 
     */  
    public static long bytesToLong(byte[] array, int offset) {  
        return ((((long) array[offset + 0] & 0xff) << 56) | (((long) array[offset + 1] & 0xff) << 48)  
                | (((long) array[offset + 2] & 0xff) << 40) | (((long) array[offset + 3] & 0xff) << 32)  
                | (((long) array[offset + 4] & 0xff) << 24) | (((long) array[offset + 5] & 0xff) << 16)  
                | (((long) array[offset + 6] & 0xff) << 8) | (((long) array[offset + 7] & 0xff) << 0));  
    }  
  
    /**
     * 整型转byte数组
     * 
     * @param n 要转换的整型数
     * @return 转换后的byte数组
     */
    public static byte[] intToBytes(int n) {  
        byte[] b = new byte[4];  
        b[3] = (byte) (n & 0xff);  
        b[2] = (byte) (n >> 8 & 0xff);  
        b[1] = (byte) (n >> 16 & 0xff);  
        b[0] = (byte) (n >> 24 & 0xff);  
        return b;  
    }  
  
    /**
     * 整型转byte数组
     * 
     * @param n 要转换的整型数
     * @param array 转换后的byte数组
     * @param offset 从第offset位开始转换
     */
    public static void intToBytes(int n, byte[] array, int offset) {  
        array[3 + offset] = (byte) (n & 0xff);  
        array[2 + offset] = (byte) (n >> 8 & 0xff);  
        array[1 + offset] = (byte) (n >> 16 & 0xff);  
        array[offset] = (byte) (n >> 24 & 0xff);  
    }  
  
    /** 
     * byte数组转整型
     * 
     * @param b 要转换的byte数组
     * @return 转换后的整型
     */  
    public static int bytesToInt(byte b[]) {  
        return b[3] & 0xff | (b[2] & 0xff) << 8 | (b[1] & 0xff) << 16 | (b[0] & 0xff) << 24;  
    }  
  
    /** 
     * byte数组转int 
     *  
     * @param b 要转换的byte数组 
     * @param offset 从数组的第几位开始转 
     * @return 转换后的整型数 
     */  
    public static int bytesToInt(byte b[], int offset) {  
        return b[offset + 3] & 0xff | (b[offset + 2] & 0xff) << 8 | (b[offset + 1] & 0xff) << 16  
                | (b[offset] & 0xff) << 24;  
    }  
  
    /** 
     * 无符号整型转byte数组 
     *  
     * @param n 要转换的整型 
     * @return 转换后的byte数组 
     */
    public static byte[] uintToBytes(long n) {  
        byte[] b = new byte[4];  
        b[3] = (byte) (n & 0xff);  
        b[2] = (byte) (n >> 8 & 0xff);  
        b[1] = (byte) (n >> 16 & 0xff);  
        b[0] = (byte) (n >> 24 & 0xff);  
  
        return b;  
    }  
  

    /**
     * 无符号整型转byte数组
     * 
     * @param n 要转换的长整型数
     * @param array 转换后的byte数组
     * @param offset 从第offset位开始转换 
     */
    public static void uintToBytes(long n, byte[] array, int offset) {  
        array[3 + offset] = (byte) (n);  
        array[2 + offset] = (byte) (n >> 8 & 0xff);  
        array[1 + offset] = (byte) (n >> 16 & 0xff);  
        array[offset] = (byte) (n >> 24 & 0xff);  
    }  
  
    /**
     * byte数组组无符号整型
     * 
     * @param array 要转换的byte数组
     * @return 转换后的长整型数
     */
    public static long bytesToUint(byte[] array) {  
        return ((long) (array[3] & 0xff)) | ((long) (array[2] & 0xff)) << 8 | ((long) (array[1] & 0xff)) << 16  
                | ((long) (array[0] & 0xff)) << 24;  
    }  
  
    /**
     * byte数组组无符号整型
     * 
     * @param array 要转换的byte数组
     * @param offset 从第offset位开始转换
     * @return 转换后的长整型数
     */
    public static long bytesToUint(byte[] array, int offset) {  
        return ((long) (array[offset + 3] & 0xff)) | ((long) (array[offset + 2] & 0xff)) << 8  
                | ((long) (array[offset + 1] & 0xff)) << 16 | ((long) (array[offset] & 0xff)) << 24;  
    }  
  
    /**
     * 短整型转byte数组
     * 
     * @param n 要转换的短整型数
     * @return 转换后的byte数组
     */
    public static byte[] shortToBytes(short n) {  
        byte[] b = new byte[2];  
        b[1] = (byte) (n & 0xff);  
        b[0] = (byte) ((n >> 8) & 0xff);  
        return b;  
    }  
  
    /**
     * 短整型转byte数组
     * 
     * @param n 要转换的短整型数
     * @param array 转换后的byte数组
     * @param offset 从第offset位开始转换
     */
    public static void shortToBytes(short n, byte[] array, int offset) {  
        array[offset + 1] = (byte) (n & 0xff);  
        array[offset] = (byte) ((n >> 8) & 0xff);  
    }  
  
    /**
     * byte数组转短整型数
     * 
     * @param b 要转换的byte数组
     * @return 转换后的短整型数
     */
    public static short bytesToShort(byte[] b) {  
        return (short) (b[1] & 0xff | (b[0] & 0xff) << 8);  
    }  
  
    /**
     * byte数组转短整型数
     * 
     * @param b 要转换的byte数组
     * @param offset 从第offset位开始转换
     * @return 转换后的短整型数
     */
    public static short bytesToShort(byte[] b, int offset) {  
        return (short) (b[offset + 1] & 0xff | (b[offset] & 0xff) << 8);  
    }  
  
    /**
     * 无符号短整型转byte数组
     * 
     * @param n 要转换的整型数
     * @return 转换后的byte数组
     */
    public static byte[] ushortToBytes(int n) {  
        byte[] b = new byte[2];  
        b[1] = (byte) (n & 0xff);  
        b[0] = (byte) ((n >> 8) & 0xff);  
        return b;  
    }  
  
    /**
     * 无符号短整型转byte数组
     * 
     * @param n 要转换的整型数
     * @param array 转换后的byte数组
     * @param offset 从第offset位开始转换
     */
    public static void ushortToBytes(int n, byte[] array, int offset) {  
        array[offset + 1] = (byte) (n & 0xff);  
        array[offset] = (byte) ((n >> 8) & 0xff);  
    }  
  
    /**
     * byte数组转无符号短整型
     * 
     * @param b 要转换的byte数组
     * @return 整型数
     */
    public static int bytesToUshort(byte b[]) {  
        return b[1] & 0xff | (b[0] & 0xff) << 8;  
    }  
  
    /**
     * byte数组组无符号短整型数
     * 
     * @param b 要转换的byte数组
     * @param offset 从第offset位开始转换
     * @return 转换后的无符号短整型数
     */
    public static int bytesToUshort(byte b[], int offset) {  
        return b[offset + 1] & 0xff | (b[offset] & 0xff) << 8;  
    }  
  
    /**
     * 无符号字节型转byte数组
     * 
     * @param n 要转换的整型数
     * @return 转换后的byte数组
     */
    public static byte[] ubyteToBytes(int n) {  
        byte[] b = new byte[1];  
        b[0] = (byte) (n & 0xff);  
        return b;  
    }  
  
    /**
     * 无符号字节型转byte数组
     * 
     * @param n 要转换的整型数
     * @param array 转换后的byte数组
     * @param offset 从第offset位开始转换
     */
    public static void ubyteToBytes(int n, byte[] array, int offset) {  
        array[offset] = (byte) (n & 0xff);  
    }  
  
    /**
     * byte数组转无符号字节型
     * 
     * @param array 转换的数组
     * @return 转换后的无符号字节型整数
     */
    public static int bytesToUbyte(byte[] array) {  
        return array[0] & 0xff;  
    }  
  
    /**
     * byte数组转无符号字节型
     * 
     * @param array 要转换的byte数组
     * @param offset 从第offset位开始转换
     * @return 转换后的无符号字节型整数
     */
    public static int bytesToUbyte(byte[] array, int offset) {  
        return array[offset] & 0xff;  
    }  
    // char 类型、 float、double 类型和 byte[] 数组之间的转换关系未实现 
}  