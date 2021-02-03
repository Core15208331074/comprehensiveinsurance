package com.scdy.comprehensiveinsurance.model.electronicFence;

import lombok.Data;

/**
 * 布撤防设置模型
 */
@Data
public class ClothRemovalSetModel {
    /**
     * 目标服务器ip
     */
    private String serverIp;
    /**
     * 目标服务器端口
     */
    private String port;
    /**
     * 防区编号16进制
     */
    private String defenseZoneNumberHex;
    /**
     * 布撤防标志16进制 01=布防，00=撤防
     */
    private String clothRemovalMarkHex;
}
