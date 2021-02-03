package com.scdy.comprehensiveinsurance.model.response.datasDown;

import lombok.Data;

@Data
public class DatasDownPacketModel {
    /**
     * 信息包id
     */
    private String id;
    /**
     * 命令信息
     */
    private DatasDownCmdInfoModel cmd;
    /**
     * 传输时间
     */
    private String time;

}
