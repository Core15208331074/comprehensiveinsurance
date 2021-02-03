package com.scdy.comprehensiveinsurance.model.request.offlineUp;

import lombok.Data;

/**
 * online的信息包.
 */
@Data
public class OfflineServicePacketModel {
    /**
     * 信息包id
     */
    private String id;
    /**
     * 服务信息
     */
    private OfflineServiceModel service;
    /**
     * 传输时间
     */
    private String time;
}
