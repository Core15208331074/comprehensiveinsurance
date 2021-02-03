package com.scdy.comprehensiveinsurance.model;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

/**
 * 串口请求条件模型
 */
@Data
public class ComRequestModel {
    /**
     * 串口数据行id
     */
    private String id;
    /**
     * 页码
     */
    private int pageIndex;
    /**
     * 每页显示多少条
     */
    private int pageSize;
    /**
     * 开始时间
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date startTime;
    /**
     * 结束时间
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date endTime;

}
