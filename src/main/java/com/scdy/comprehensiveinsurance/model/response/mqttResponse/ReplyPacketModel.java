package com.scdy.comprehensiveinsurance.model.response.mqttResponse;

import lombok.Data;

@Data
public class ReplyPacketModel {
    /**
     * 信息包id
     */
    private String id;
    /**
     * 应答
     */
    private ServiceReplyModel serviceReply;
    /**
     * 时间
     */
    private String time;
}
