package com.scdy.comprehensiveinsurance.model.response.temperatureRiseChart;

import lombok.Data;

@Data
public class TemperatureRiseValueModel {
    /**
     * 时间
     */
    private Long x;
    /**
     * 值
     */
    private Float y;
}
