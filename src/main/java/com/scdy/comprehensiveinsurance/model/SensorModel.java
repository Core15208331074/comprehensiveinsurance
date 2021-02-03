package com.scdy.comprehensiveinsurance.model;

import lombok.Data;

import java.util.List;

/**
 * 传感器发送和接收原始数据实体类.
 */
@Data
public class SensorModel {
    /**
     * 类型：查询：0，反控：1 设置为反控后前端就不查询显示/不传到MQTT
     */
    private String isControl;
    /**
     * 传感器名称id
     */
    private String sensorNameId;
    /**
     * 传感器名称
     */
    private String sensorName;
    /**
     * 点位list
     */
    private List<PointModel> pointModelList;
    /**
     * 类型 如：com ,TCP Client
     */
    private String type;
    /**
     * 传感器地址
     */
    private String deviceAddress;
    /**
     * 标记
     */
    private String tag;
    /**
     * 安装位置
     */
    private String installPostion;

    /**
     * 传感器id
     */
    private String sensorId;
    /**
     * eth行id
     */
    private String ethId;

}
