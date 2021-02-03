package com.scdy.comprehensiveinsurance.service.analysis;

import com.scdy.comprehensiveinsurance.model.SensorModel;

/**
 * 协议解析.
 */
public interface ProtocolAnalysisService {
    /**
     * 协议解析.
     *
     * @param sensorModel 传感器发送和返回指令对象.
     */
    SensorModel analysis(SensorModel sensorModel) throws Exception;
}
