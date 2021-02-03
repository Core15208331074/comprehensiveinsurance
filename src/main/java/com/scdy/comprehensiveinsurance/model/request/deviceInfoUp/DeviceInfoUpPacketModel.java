package com.scdy.comprehensiveinsurance.model.request.deviceInfoUp;

import lombok.Data;

import java.util.List;

/**
 * 信息包
 */
@Data
public class DeviceInfoUpPacketModel {
    /**
     * 信息包id
     */
    private String id;
    /**
     * 设备信息
     */
    private List<DeviceInfoUpDeviceInfoModel> devices;
    /**
     * 监测设备和被监测设备关系
     */
    private List<DeviceRelationShipModel> deviceRelationship;
    /**
     * 参数信息
     */
    private List<DeviceInfoUpParamInfoModel> params;
    /**
     * 时间
     */
    private String time;
}
