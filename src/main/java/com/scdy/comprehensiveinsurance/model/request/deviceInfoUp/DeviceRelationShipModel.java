package com.scdy.comprehensiveinsurance.model.request.deviceInfoUp;

import lombok.Data;

@Data
public class DeviceRelationShipModel {
    /**
     * 监测设备id
     */
    private String dev1;
    /**
     * 父
     */
    private String actor1;
    /**
     * 被监测设备id
     */
    private String dev2;
    /**
     * 子
     */
    private String actor2;
    /**
     * 0表示直连/1表示非直连
     */
    private Integer relation;
}
