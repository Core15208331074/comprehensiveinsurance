package com.scdy.comprehensiveinsurance.service;

import com.scdy.comprehensiveinsurance.entity.ComEntity;
import com.scdy.comprehensiveinsurance.entity.EthEntity;
import com.scdy.comprehensiveinsurance.model.SensorModel;

/**
 * 传感器数据构建.
 */
public interface SensorDataService {
    /**
     * 构建com传感器数据.
     *
     * @param comEntity
     * @return
     */
    SensorModel getSensorSendData(ComEntity comEntity);

    /**
     * 构建eth传感器数据.
     *
     * @param ethEntity
     * @return
     */
    SensorModel getSensorSendData(EthEntity ethEntity);
}
