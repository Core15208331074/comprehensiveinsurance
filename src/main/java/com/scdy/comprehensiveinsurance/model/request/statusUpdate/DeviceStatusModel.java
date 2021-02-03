package com.scdy.comprehensiveinsurance.model.request.statusUpdate;

import lombok.Data;

@Data
public class DeviceStatusModel {
    /**
     * 设备id
     */
    private String id;
    /**
     * 名称
     */
    private String name;
    /**
     * 0-正常，1-离线等
     */
    private Integer status;
    /**
     * 0-本体，1-同组，影响范围
     */
    private Integer influence;
    /**
     * influence为1时有效，小组序号
     */
    private int group;

}
