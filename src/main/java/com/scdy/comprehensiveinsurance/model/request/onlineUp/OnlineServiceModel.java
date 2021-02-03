package com.scdy.comprehensiveinsurance.model.request.onlineUp;

import lombok.Data;

/**
 * servicePacket的service模型.
 */
@Data
public class OnlineServiceModel {
    /**
     * 服务id,认证识别码
     */
    private String id;
    /**
     * 服务名称
     */
    private String name;
    /**
     * 服务版本,规则:主版本号.子版本号.阶段版本号.日期版本号._希腊字母版本号,如1.1.1.20200901_beta
     */
    private String version;
    /**
     * 服务功能描述
     */
    private String description;
    /**
     * 服务类型
     */
    private String type;
}
