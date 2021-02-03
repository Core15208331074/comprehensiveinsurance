package com.scdy.comprehensiveinsurance.model;

import com.scdy.comprehensiveinsurance.entity.ComEntity;
import lombok.Data;

/**
 * 串口状态模型.
 */
@Data
public class ComStatusModel {
    /**
     * 传感器模型
     */
    private SensorModel sensorModel;
    /**
     * 串口行数据实体类
     */
    private ComEntity comEntity;
    /**
     * 是否启动
     */
    private Boolean isStart;

}
