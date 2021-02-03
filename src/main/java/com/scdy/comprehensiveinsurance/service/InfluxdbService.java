package com.scdy.comprehensiveinsurance.service;

import com.scdy.comprehensiveinsurance.model.SensorModel;

public interface InfluxdbService {
    /**
     * 保持eth的解析信息到时序数据库
     *
     * @param sensorModel
     */
    void saveEthToInfluxdb(SensorModel sensorModel);
}
