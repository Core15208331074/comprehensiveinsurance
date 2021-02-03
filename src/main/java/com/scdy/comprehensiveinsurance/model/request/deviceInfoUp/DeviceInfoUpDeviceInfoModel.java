package com.scdy.comprehensiveinsurance.model.request.deviceInfoUp;

import lombok.Data;

/**
 * 设备信息.
 */
@Data
public class DeviceInfoUpDeviceInfoModel {
    /**
     * 设备id,PMS/设备标识码
     */
    private String id;
    /**
     * udid
     */
    private String udid;
    /**
     * 设备名称,全称
     */
    private String name;
    /**
     * 序号
     */
    private String tag="";
    /**
     * 安装位置
     */
    private String installPostion;
    /**
     * 组号
     */
    private String group="";
    /**
     * 应用类:0-输电，1-配电2-变电
     */
    private Integer applicationType;
    /**
     * 功能类:0-网关设备，1-采集设备，2-电力设备
     */
    private Integer funcType;
    /**
     * 开关柜/变压器/摄像机
     */
    private String typeName;
    /**
     * 被测设备宽度(图片像素)
     */
    private Integer width=0;
    /**
     * 被测设备高度（图片像素）
     */
    private Integer high=0;

}
