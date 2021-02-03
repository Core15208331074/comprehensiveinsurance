package com.scdy.comprehensiveinsurance.model.response.datasDown;

import lombok.Data;

/**
 * 从mqtt接收到的json信息模型.
 */
@Data
public class DatasDownModel {
    /**
     * 信息包
     */
    private DatasDownPacketModel packet;
    /**
     * 使用MD5对packet值进行校验，该字段存储MD5摘要值
     */
    private String verify;
}
