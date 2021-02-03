package com.scdy.comprehensiveinsurance.model.request.datasDown;

import lombok.Data;

@Data
public class DatasResponseReplyPacketModel {
    /**
     * 信息包id
     */
    private String id;
    /**
     * 应答
     */
    private DatasResponseDevServiceReplyModel devServiceReply;
    /**
     * 时间
     */
    private String time;
}
