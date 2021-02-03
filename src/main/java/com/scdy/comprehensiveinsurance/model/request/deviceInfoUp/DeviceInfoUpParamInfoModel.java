package com.scdy.comprehensiveinsurance.model.request.deviceInfoUp;

import lombok.Data;

@Data
public class DeviceInfoUpParamInfoModel {
    /**
     * 参数id
     */
    private String id;
    /**
     * 名称
     */
    private String name;
    /**
     * 同类型器件的序号 标签
     */
    private String tag="";
    /**
     * 压板、旋钮、指示灯、仪表
     */
    private String objType;
    /**
     * 值,开/合，亮/灭，远方/就地
     */
    private String value="";
    /**
     * 参数所属设备
     */
    private String deviceId;
    /**
     * 0-只读，1-只写，2-读写，表示是否可控
     */
    private String operation;
    /**
     * 十六进制
     */
    private String color="";
    /**
     * 元件左上角坐标的left值
     */
    private Integer left=0;
    /**
     * 元件左上角坐标的top值
     */
    private Integer top=0;
    /**
     * 元器件的宽
     */
    private Integer width=0;
    /**
     * 元器件的高
     */
    private Integer high=0;
    /**
     * 信息
     */
    private String msg="";
    /**
     * 监测类型编码
     */
    private String monitoringTypeCode="";
    /**
     * 监测项代码
     */
    private String monitoringItemCode="";
}
