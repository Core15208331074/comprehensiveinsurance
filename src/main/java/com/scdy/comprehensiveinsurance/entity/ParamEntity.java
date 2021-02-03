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
 * @since 2020-12-26
 */
@Data
@TableName("param")
public class ParamEntity implements Serializable {

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
     * 传感器编码
     */
    private String sensorNameCode;


    /**
     * 参数
     */
    private String parameter;

    /**
     * 参数代码
     */
    private String parameterCode;

    /**
     * 单位
     */
    private String unit;

    /**
     * 数据类型
     */
    private String dataType;
    /**
     * 压板、旋钮、指示灯、仪表/电缆终端
     */
    private String objType;
    /**
     * 值,开/合，亮/灭，远方/就地
     */
    private String value;
    /**
     * 同类型器件的序号
     */
    private String tag;
    /**
     * 监测类型编码
     */
    private String monitoringTypeCode;
    /**
     * 监测项代码
     */
    private String monitoringItemCode;
    /**
     * 从机地址
     */
    private String slaveAddressHex;
    /**
     * 功能码
     */
    private String functionCodeHex;
    /**
     * 服务端端口
     */
    private String serverPort;

}
