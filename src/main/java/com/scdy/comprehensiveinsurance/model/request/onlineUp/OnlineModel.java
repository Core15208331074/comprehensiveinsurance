package com.scdy.comprehensiveinsurance.model.request.onlineUp;

import cn.hutool.json.JSONObject;
import lombok.Data;

/**
 * 上线模型.
 */
@Data
public class OnlineModel {
    /**
     * 信息包
     */
    private JSONObject packet;
    /**
     * 使用MD5对packet值进行校验，该字段存储MD5摘要值
     */
    private String verify;
}
