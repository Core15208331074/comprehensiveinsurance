package com.scdy.comprehensiveinsurance.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 
 * </p>
 *
 * @author zl
 * @since 2020-11-24
 */
@Data
@TableName("eth")
public class EthEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.ASSIGN_UUID)
    private String id;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 0 表示未删除,1 表示删除,默认值为0
     */
    private Integer isDeleted;

    /**
     * eth名称
     */
    private String ethName;

    /**
     * 目标ip
     */
    private String targetIp;

    /**
     * 目标端口
     */
    private String targetPort;

    /**
     * 本机端口
     */
    private String localPort;

    /**
     * 传感器型号
     */
    private String sensorModel;

    /**
     * 设备地址
     */
    private String deviceAddress;
    /**
     * 安装位置
     */
    private String installPostion;
    /**
     * 标签，主要是传1，2，3...表示序号
     */
    private String tag;

    /**
     * 启动状态
     */
    @TableField(exist = false)
    private Boolean isStart;
}
