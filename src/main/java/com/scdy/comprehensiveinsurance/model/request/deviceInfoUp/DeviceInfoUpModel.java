package com.scdy.comprehensiveinsurance.model.request.deviceInfoUp;

import cn.hutool.json.JSONObject;
import lombok.Data;

/**
 * 设备信息.
 */
@Data
public class DeviceInfoUpModel {
    /**
     * 信息包
     */
    private JSONObject packet;
    /**
     * 使用MD5对packet值进行校验，该字段存储MD5摘要值
     */
    private String verify;
}
