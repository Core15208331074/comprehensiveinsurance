package com.scdy.comprehensiveinsurance.model.request.schedule;

import lombok.Data;

import java.util.List;

@Data
public class SchedulePacketModel {
    /**
     * 设备id,摄像头的uuid
     */
    private String id;
    /**
     * 当前程序需要轮询的设备总体数量，主要是指开关柜的所有数量
     */
    private int devTotal;
    /**
     * 表示当前已经轮询完成的设备数量，主要是指已经轮询完了的开关柜数量
     */
    private int hasBeenTouring;
    /**
     * 表示当前已经轮询完成的设备数量，主要是指已经轮询完了的开关柜数量
     */
    private List<CurTouringDevModel> curTouringDevs;
    /**
     * 时间
     */
    private String time;
}
