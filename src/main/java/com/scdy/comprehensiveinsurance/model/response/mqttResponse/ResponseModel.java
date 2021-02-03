package com.scdy.comprehensiveinsurance.model.response.mqttResponse;

import lombok.Data;

/**
 * MQTT返回的信息模型.
 */
@Data
public class ResponseModel {
    /**
     * 信息包
     */
    private ReplyPacketModel packet;
    /**
     * 使用MD5对packet值进行校验，该字段存储MD5摘要值
     */
    private String verify;
}
