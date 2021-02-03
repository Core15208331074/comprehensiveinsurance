package com.scdy.comprehensiveinsurance.entity;

import com.baomidou.mybatisplus.annotation.IdType;
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
 * @since 2020-11-05
 */
@Data
@TableName("sensor")
public class SensorEntity implements Serializable {

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
     * 传感器生产厂家
     */
    private String manufacturer;

    /**
     * 传感器生产厂家编码
     */
    private String manufacturerCode;

    /**
     * 传感器名称
     */
    private String sensorName;

    /**
     * 传感器名称编码
     */
    private String sensorNameCode;
    /**
     * 0表示主动发送请求指令，1表示被动接收指令，2表示既主动发送又被动接收返回结果
     */
    private String isSend;
    /**
     * 应用类型:0-输电，1-配电2-变电
     */
    private String applicationType;
    /**
     * 功能类:0-网关设备，1-采集设备，2-电力设备
     */
    private String funcType;
    /**
     * 类型名称 如：开关柜/变压器/摄像机
     */
    private String typeName;
    /**
     * 0:表示直连
     * 1:表示非直连
     */
    private String relation;


}
