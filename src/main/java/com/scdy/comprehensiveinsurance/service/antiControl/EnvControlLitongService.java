package com.scdy.comprehensiveinsurance.service.antiControl;

import com.scdy.comprehensiveinsurance.model.lighting.LightingModel;

/**
 * 环控(利通)反控
 */
public interface EnvControlLitongService {
    /**
     * 灯控
     * 1-4，要么前面两个有值，要么后面两个有值
     *
     * @param lightingModel
     * @return
     */
    void lightingOperation(LightingModel lightingModel) throws Exception;
}
