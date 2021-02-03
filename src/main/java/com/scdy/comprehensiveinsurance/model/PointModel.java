package com.scdy.comprehensiveinsurance.model;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 点位模型.
 */
@Data
public class PointModel {
    /**
     * 点位名称
     */
    private String pointName;
    /**
     * 原始值
     */
    private String originalValue;
    /**
     * 值
     */
    private String value;
    /**
     * 单位
     */
    private String unit;
    /**
     * 请求指令
     */
    private String requestInstruction;
    /**
     * 返回指令
     */
    private String responseInstruction;

    /**
     * 从机地址
     */
    private String slaveAddressHex;
    /**
     * 功能码
     */
    private String functionCodeHex;
    /**
     * 数据字节总数
     */
    private String totalDataBytesHex;
    /**
     * 数据Map hex
     */
    private Map<String,String> dataMapHex;
    /**
     * CRC校验值
     */
    private String CRCHex;
    /**
     * 时间
     */
    private String time;
    /**
     * 点位list
     */
    private List<PointModel> pointList;
    /**
     * 数据起始地址
     */
    private String dataStartAddress;
    /**
     * 数据地址
     */
    private String dataAddress;

    /**
     * 标签
     */
    private String tag;
    /**
     * 传感器id
     */
    private String sensorId;
    /**
     * 参数id
     */
    private String paramId;
    /**
     * eth行id
     */
    private String ethId;




}
