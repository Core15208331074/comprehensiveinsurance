package com.scdy.comprehensiveinsurance.model.request.datasUp;

import lombok.Data;

/**
 * 分组数据模型.
 */
@Data
public class GroupDataValueModel {
    /**
     * 温升
     */
    private float temperatureRise;
    /**
     * 结果
     */
    private String result;
}
