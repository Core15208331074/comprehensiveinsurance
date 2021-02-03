package com.scdy.comprehensiveinsurance.model.lighting;

import lombok.Data;

/**
 * 照明模型
 */
@Data
public class LightingModel {
    /**
     * 目标服务器端口
     */
    private String localPort;
    /**
     * 灯控1 -- 1键
     */
    private Boolean lightControl1_1;
    /**
     * 灯控1 -- 2键
     */
    private Boolean lightControl1_2;
    /**
     * 灯控2 -- 1键
     */
    private Boolean lightControl2_1;
    /**
     * 灯控2 -- 2键
     */
    private Boolean lightControl2_2;
}
