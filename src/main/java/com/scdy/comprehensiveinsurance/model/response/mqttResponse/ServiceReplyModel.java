package com.scdy.comprehensiveinsurance.model.response.mqttResponse;

import lombok.Data;

@Data
public class ServiceReplyModel {
    /**
     * 边缘代理服务id,可做认证使用
     */
    private String id;
    /**
     * 状态码：200：成功，400：客户端错误，500:服务端错误
     */
    private String code;
    /**
     * 应答
     */
    private String message;
}
