package com.scdy.comprehensiveinsurance.service.antiControl;

import com.scdy.comprehensiveinsurance.model.SensorModel;
import com.scdy.comprehensiveinsurance.model.electronicFence.ClothRemovalSetModel;

/**
 * 电子围栏反控接口
 */
public interface ElectronicFenceService {
    /**
     * 布撤防设置
     *
     * @param clothRemovalSetModel
     * @return
     */
    SensorModel clothRemovalSet(ClothRemovalSetModel clothRemovalSetModel ) throws Exception;
}
