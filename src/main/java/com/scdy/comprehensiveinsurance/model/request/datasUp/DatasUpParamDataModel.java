package com.scdy.comprehensiveinsurance.model.request.datasUp;

import lombok.Data;

@Data
public class DatasUpParamDataModel {
    /**
     * 参数id
     */
    private String id;
    /**
     * 名称
     */
    private String name;
    /**
     * 同类型器件的序号
     */
    private String tag="";
    /**
     * 压板、旋钮、指示灯、仪表
     */
    private String type;
    /**
     * 值,开/合，亮/灭，远方/就地
     */
    private String value;
    /**
     * 参数所属设备
     */
    private String deviceId;
    private String unit="";
    /**
     * 十六进制
     */
    private String color="";
    /**
     * 告警等级:0-正常，1-警告，2-危险，4-危急
     */
    private Integer alarmLevel=0;
}
