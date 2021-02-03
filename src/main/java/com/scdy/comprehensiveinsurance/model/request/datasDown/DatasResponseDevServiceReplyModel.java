package com.scdy.comprehensiveinsurance.model.request.datasDown;

import lombok.Data;

@Data
public class DatasResponseDevServiceReplyModel {
    /**
     * 机器人服务id,可做认证使用
     */
    private String id;
    /**
     * 状态码：100：已接收，200：已执行，300：已完成
     */
    private String code;
    /**
     * 消息
     */
    private String message;
    /**
     * 类型
     * 0-心跳，1-查询,2-控制
     */
    private String tag;
//    /**
//     * 数据
//     */
//    private DataModel data;
}
