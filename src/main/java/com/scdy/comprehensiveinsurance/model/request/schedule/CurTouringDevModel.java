package com.scdy.comprehensiveinsurance.model.request.schedule;

import lombok.Data;

@Data
public class CurTouringDevModel {
    /**
     * 开关柜的uuid
     */
    private String curTouringDevId;
    /**
     * 0表示开始轮询，1表示正在轮询，2：表示轮询结束
     */
    private int curTouringStatus;
}
