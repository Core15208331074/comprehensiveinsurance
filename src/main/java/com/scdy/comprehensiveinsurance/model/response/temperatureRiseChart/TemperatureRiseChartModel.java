package com.scdy.comprehensiveinsurance.model.response.temperatureRiseChart;

import lombok.Data;

import java.util.List;

/**
 * 分组温升图模型
 */
@Data
public class TemperatureRiseChartModel {
    /**
     * 名称
     */
    private String name;
    /**
     * 数据
     */
    private List<List> data;
}
