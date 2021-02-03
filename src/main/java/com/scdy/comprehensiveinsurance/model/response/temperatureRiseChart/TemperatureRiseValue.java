package com.scdy.comprehensiveinsurance.model.response.temperatureRiseChart;

import lombok.Data;

@Data
public class TemperatureRiseValue {
    /**
     * 时间
     */
    private Long time;
    /**
     * 温升
     */
    private Float maxAbsTemp;
}
