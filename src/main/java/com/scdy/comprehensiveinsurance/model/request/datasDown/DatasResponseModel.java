package com.scdy.comprehensiveinsurance.model.request.datasDown;

import cn.hutool.json.JSONObject;
import lombok.Data;

/**
 * 从MQTT接收到数据返回的模型.
 */
@Data
public class DatasResponseModel {
    /**
     * 信息包
     */
    private JSONObject packet;
    /**
     * 使用MD5对packet值进行校验，该字段存储MD5摘要值
     */
    private String verify;
}
