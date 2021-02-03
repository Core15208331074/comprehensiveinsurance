package com.scdy.comprehensiveinsurance.model.request.datasUp;

import cn.hutool.json.JSONObject;
import lombok.Data;

/**
 * 更新的数据
 */
@Data
public class DatasUpModel {
    /**
     * 信息包
     */
    private JSONObject packet;//DataUpPacketModel
    /**
     * 使用MD5对packet值进行校验，该字段存储MD5摘要值
     */
    private String verify;
}
