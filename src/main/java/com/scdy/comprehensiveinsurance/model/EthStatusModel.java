package com.scdy.comprehensiveinsurance.model;

import com.scdy.comprehensiveinsurance.entity.EthEntity;
import lombok.Data;

/**
 * 串口状态模型.
 */
@Data
public class EthStatusModel {
    /**
     * 传感器模型
     */
    private SensorModel sensorModel;
    /**
     * eth行数据实体类
     */
    private EthEntity ethEntity;
//    /**
//     * 是否启动
//     */
//    private boolean isStart;

}
