package com.scdy.comprehensiveinsurance.model.request.statusUpdate;

import lombok.Data;

import java.util.List;

@Data
public class StatusUpPacketModel {
    /**
     * 信息包id
     */
    private String id;
    /**
     * 参数信息
     */
    private List<DeviceStatusModel> devsStatus;
    /**
     * 时间
     */
    private String time;

}
