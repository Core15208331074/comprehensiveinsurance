package com.scdy.comprehensiveinsurance.model.request.datasUp;

import lombok.Data;

/**
 * 数据值模型
 */
@Data
public class DataValueModel {
    /**
     * 最大值
     */
    private Float max;
    /**
     * 最小值
     */
    private Float min;
    /**
     * 平均值
     */
    private Float avg;
    /**
     * 当前值
     */
    private Float current;
    /**
     * 结果
     */
    private String result;
}
