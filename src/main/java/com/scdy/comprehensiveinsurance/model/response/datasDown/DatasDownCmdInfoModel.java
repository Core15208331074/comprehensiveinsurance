package com.scdy.comprehensiveinsurance.model.response.datasDown;

import lombok.Data;

@Data
public class DatasDownCmdInfoModel {
    /**
     * 命令随机码id
     */
    private String id;
    /**
     * 0-心跳，1-查询,2-控制
     */
    private Integer type;
    /**
     * 为控制命令时填写控制
     */
    private String paramId;
    /**
     * 控制值，根据具体控制定义
     */
    private int operation;

}
