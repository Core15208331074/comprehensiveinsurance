package com.scdy.comprehensiveinsurance.utils;

import cn.hutool.core.util.StrUtil;

import java.math.BigDecimal;
import java.util.ArrayList;

public class ByteUtil {
    private static String hexStr = "0123456789ABCDEF";
    private static String[] binaryArray =
            {"0000", "0001", "0010", "0011",
                    "0100", "0101", "0110", "0111",
                    "1000", "1001", "1010", "1011",
                    "1100", "1101", "1110", "1111"};

    /**
     * 16进制转二进制并且返回顺序的int数组
     *
     * @param hexStr
     * @return
     */
    public static int[] hexToBinToInts(String hexStr) {
        String relayHighByteBin = ByteUtil.hexStrToBinaryStr(hexStr);
        int[] ints = reverseBinToInts(relayHighByteBin);
        return ints;
    }

    /**
     * 反转2进制字符串并生成int[].
     *
     * @param binaryStr 2进制字符串
     * @return
     */
    public static int[] reverseBinToInts(String binaryStr) {
        String reverse = StrUtil.reverse(binaryStr);//字符串反转

        int[] arr = new int[reverse.length()];
        for (int i = 0; i < reverse.length(); i++) {
            arr[i] = Integer.parseInt(reverse.substring(i, i + 1));
        }
        return arr;
    }

    /**
     * 生成带异或校验的全数据
     *
     * @param dataHex
     * @return
     */
    public static String getDataHexWithBCC(String dataHex) {
        dataHex = dataHex.replace(" ", "");
        int i = 0, j = 0;
        int len = dataHex.length();
        short inb[] = new short[len];
        for (i = 0; i < len; i++) {
            inb[i] = charToHex(dataHex.charAt(i));   //将String里的每一个char转换为Hex
        }

        for (i = 0; i < len; i++) {    //将每两个Hex合并成一个byte
            inb[j] = (byte) (((inb[i] << 4) & 0x00f0) | ((inb[i + 1]) & 0x000f));
            i++;
            j++;
        }
        byte temp = 0x00; //校验值
        for (i = 0; i < len / 2; i++) { //异或
            temp ^= inb[i];
        }

        String hex = Integer.toHexString(temp & 0xFF);
        if (hex.length() == 1) {
            hex = '0' + hex;
        }
        return dataHex + hex;
    }

    /**
     * 将单个char转换为Hex
     *
     * @param x
     * @return
     */
    public static short charToHex(char x) {
        short result = 0;
        switch (x) {
            case 'a':
                result = 10;
                break;
            case 'b':
                result = 11;
                break;
            case 'c':
                result = 12;
                break;
            case 'd':
                result = 13;
                break;
            case 'e':
                result = 14;
                break;
            case 'f':
                result = 15;
                break;

            case 'A':
                result = 10;
                break;
            case 'B':
                result = 11;
                break;
            case 'C':
                result = 12;
                break;
            case 'D':
                result = 13;
                break;
            case 'E':
                result = 14;
                break;
            case 'F':
                result = 15;
                break;
            default:
                result = (short) Character.getNumericValue(x);
                break;
        }
        return result;
    }


    /**
     * 字符串转字节数组.
     *
     * @param str
     * @return
     * @throws Exception
     */
    public static byte[] strToBytes(String str) throws Exception {
        if (str == null || str.trim().equals("")) {
            return new byte[0];
        }

        byte[] bytes = new byte[str.length() / 2];
        for (int i = 0; i < str.length() / 2; i++) {
            String subStr = str.substring(i * 2, i * 2 + 2);
            bytes[i] = (byte) Integer.parseInt(subStr, 16);
        }

        return bytes;
    }

    /**
     * byte[]转16进制字符串
     */
    public static String byteToHexString(byte[] data) {
        StringBuilder buf = new StringBuilder(data.length * 2);
        for (byte b : data) { // 使用String的format方法进行转换
            buf.append(String.format("%02x", new Integer(b & 0xff)));
        }
        return buf.toString();
    }

    /**
     * 16进制字符串转2进制字符串.
     *
     * @param hexStr 16进制字符串
     * @return
     */
    public static String hexStrToBinaryStr(String hexStr) {
        byte[] bArray = hexStringToBytes(hexStr);//转为16进制数组
        String outStr = "";
        int pos = 0;
        for (byte b : bArray) {
            //高四位
            pos = (b & 0xF0) >> 4;
            outStr += binaryArray[pos];
            //低四位
            pos = b & 0x0F;
            outStr += binaryArray[pos];
        }
        return outStr;

    }

    public static byte[] hexStringToBytes(String hexString) {
        if (hexString == null || hexString.equals("")) {
            return null;
        }
        // toUpperCase将字符串中的所有字符转换为大写
        hexString = hexString.toUpperCase();
        int length = hexString.length() / 2;
        // toCharArray将此字符串转换为一个新的字符数组。
        char[] hexChars = hexString.toCharArray();
        byte[] d = new byte[length];
        for (int i = 0; i < length; i++) {
            int pos = i * 2;
            d[i] = (byte) (charToByte(hexChars[pos]) << 4 | charToByte(hexChars[pos + 1]));
        }
        return d;
    }

    //charToByte返回在指定字符的第一个发生的字符串中的索引，即返回匹配字符
    private static byte charToByte(char c) {
        return (byte) "0123456789ABCDEF".indexOf(c);
    }

    /**
     * 将字节数组转换为16进制字符串
     */
    public static String BinaryToHexString(byte[] bytes) {
        String hexStr = "0123456789ABCDEF";
        String result = "";
        String hex = "";
        for (byte b : bytes) {
            hex = String.valueOf(hexStr.charAt((b & 0xF0) >> 4));
            hex += String.valueOf(hexStr.charAt(b & 0x0F));
            result += hex;
        }
        return result;
    }

    /**
     * 将字节数组转换为16进制字符串
     *
     * @param bytes
     * @return
     */
    public static String bytesToString(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte b : bytes) {
            String sTemp = Integer.toHexString(0xFF & b);
            if (sTemp.length() < 2)
                result.append(0);
            result.append(sTemp.toUpperCase());
        }
        return result.toString();
    }

    /**
     * 在前补位
     *
     * @param str      源字符
     * @param length   补足多少位（字节数*2）
     * @param patchStr 补位符
     * @return
     */
    public static String beforePatch(String str, int length, String patchStr) {
        while (str.length() < length) {
            str = patchStr + str;
        }
        return str;
    }

    /**
     * 在后补位
     *
     * @param str      源字符
     * @param length   补足多少位
     * @param patchStr 补位符
     * @return
     */
    public static String afterPatch(String str, int length, String patchStr) {
        while (str.length() < length) {
            str = str + patchStr;
        }
        return str;
    }

    /**
     * 将数字转化 为16进制的字符
     *
     * @param num
     * @return
     */
    public static String numToHexString(Integer num) {
        return Integer.toHexString(num);
    }

    /**
     * 将两个字节的16进制字符口串 高低位互换
     *
     * @param hexStr
     * @return
     */
    public static String exchangePosition(String hexStr) {
        int length = hexStr.length();
        String handHexString = "";
        if (length % 2 != 0) {
            handHexString = beforePatch(hexStr, length + 1, "0");
        } else {
            handHexString = hexStr;
        }
        length = handHexString.length();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length / 2; i++) {
            sb.append(handHexString.substring(length - (i + 1) * 2, length - i * 2));
        }
        return sb.toString();
    }

    /**
     * 将数字转换成16进制的字符串。
     *
     * @param num    被转的数字
     * @param length 该字段要求的长度
     * @param str    长度不足时，用这个字符补位
     * @return
     */
    public static String numToLengthHexStr(Integer num, int length, String str) {
        String hexString = Integer.toHexString(num);
        if (hexString.length() > length) {
            hexString = hexString.substring(0, length);
        }
        if (hexString.length() < length) {
            hexString = beforePatch(hexString, length, str);
        }
        return hexString;
    }

    /**
     * 将数字转换成16进制的字符串。
     *
     * @param num    被转的数字
     * @param length 该字段要求的长度
     * @return
     */
    public static String numToLengthHexStr(Integer num, int length) {
        String hexString = Integer.toHexString(num);
        if (hexString.length() < length) {
            hexString = beforePatch(hexString, length, "0");
        }
        return hexString;
    }

    public static Integer hexStringToNum(String hexString) {

        return Integer.parseInt(hexString, 16);
    }

    public static Long hexStringToLong(String hexString) {
        return Long.parseLong(hexString, 16);
    }

    /**
     * 16进制字符串转float
     *
     * @param hexString
     * @return
     */
    public static Float hexStringToFloat(String hexString) {

        return Float.intBitsToFloat(Long.valueOf(hexString, 16).intValue());
    }

    public static String floatToHexString(Float value) {
        return Integer.toHexString(Float.floatToIntBits(value));

    }

    /**
     * float转16进制字符串
     *
     * @param f
     * @return
     */
    public static String floatToHexString(float f) {
        return Integer.toHexString(Float.floatToIntBits(f));
    }

    public static Float hexStringToFloat2(String hexString) {
        Integer integer = ByteUtil.hexStringToNum(hexString);
        //将解析出来的整数 计算成温度 //double result=String/100 - 100;  保留两位小数
        BigDecimal result = new BigDecimal(integer).divide(new BigDecimal(10), 2, BigDecimal.ROUND_HALF_UP);
        return result.floatValue();
    }

    /**
     * 16进制转浮点数，保留三位小数.
     *
     * @param hexString 16进制
     * @return
     */
    public static Float hexStringToFloat3(String hexString) {
        Integer integer = ByteUtil.hexStringToNum(hexString);
        //将解析出来的整数 计算成温度 //double result=String/100 - 100;  保留两位小数
        BigDecimal result = new BigDecimal(integer).divide(new BigDecimal(10), 3, BigDecimal.ROUND_HALF_UP);
        return result.floatValue();
    }

    /**
     * 16进制转浮点数，保留一位小数.
     *
     * @param hexString 16进制
     * @return
     */
    public static Float hexStringToFloat1(String hexString) {
        //除以10，保留一位小数
        BigDecimal result = new BigDecimal(ByteUtil.hexStringToNum(hexString)).divide(new BigDecimal(10), 1, BigDecimal.ROUND_HALF_UP);
        return result.floatValue();
    }

    /**
     * 把字符按照2个分割到集合.
     *
     * @param str
     * @return
     */
    public static ArrayList<String> strsToList(String str) {

        char[] chars = new char[2];
        ArrayList<String> stringList = new ArrayList<>();
        for (int i = 0; i < str.length(); i++) {
            if (i % 2 == 0) {//每隔两个bai
                chars = new char[2];
                chars[0] = str.charAt(i);
            } else {
                chars[1] = str.charAt(i);
                stringList.add(new String(chars));
                chars = null;
            }
        }
        return stringList;
    }


    public static void main(String[] args) {
        String hexStr = "0012";
        String binaryStr = hexStrToBinaryStr(hexStr);
        System.out.println(binaryStr);
    }
}